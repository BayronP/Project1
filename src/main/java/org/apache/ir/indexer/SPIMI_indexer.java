package org.apache.ir.indexer;

import javafx.geometry.Pos;
import org.apache.ir.dict.Dict;
import org.apache.ir.dict.Dict_entity;
import org.apache.ir.parser.Parser;
import org.apache.ir.parser.TrecParser;
import org.apache.ir.util.Postlistings;
import org.apache.ir.util.Token;
import org.apache.ir.util.Util_func;

import java.io.*;
import java.util.*;

/**
 * Created by bayron on 2016/11/24.
 */
public class SPIMI_indexer implements Indexer {
    private String filePath = "./";
    private TrecParser trecParser = new TrecParser(filePath);
    private Util_func util_func = new Util_func();
    private int maxSpace = 10000;
    private int blockNum = 0;
    private int numWord = -1;

    public SPIMI_indexer() {
    }

    public int getNumTerm() {
        if (numWord == -1) {
            getNumTermFromFile();
        }
        return numWord;
    }

    public void getNumTermFromFile() {
        try {
            RandomAccessFile file = new RandomAccessFile("./index/index_final", "r");
            int ss = util_func.readInt(file);
            int ds = util_func.readInt(file);
            this.numWord = ds;
            file.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void outputStatisticalInfo() {
        System.out.println("*******************************************************");
        System.out.println("Basic statistical info for index : ");
        System.out.println("Tokens Count : " + trecParser.getTokenNum());
        System.out.println("Terms Count : " + this.getNumTerm());
        System.out.println("Documents Count : " + trecParser.getDoc_cnt());
        System.out.println("Average Length for Doc : " + trecParser.getAverDocLen());
        System.out.println("*******************************************************");
    }

    public void construct_index() {
        util_func.removeAllFile("./tmp/");

        while(trecParser.hasNextToken()) {
            blockNum++;
            TreeMap<String, Postlistings> dict = new TreeMap<String, Postlistings>();
            int numUsed = 0;
            while (numUsed < maxSpace) {
                if (trecParser.hasNextToken()) {
                    Token token = trecParser.nextToken();
                    String word = token.getWord();
                    int docId = token.getDocId();
                    Postlistings postlistings = new Postlistings();
                    if (dict.containsKey(word)) {
                        postlistings = dict.get(word);
                    }
                    postlistings.add_record(docId);
                    dict.put(word, postlistings);

                    numUsed++;
                } else break;
            }

            write_index_to_file(dict, "block", blockNum);
            //System.out.println(blockNum);
            //Token token = trecParser.nextToken();
        }
        try {
            trecParser.write_id_to_doc();
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("END");
        // merge
        merge_index_from_disk();

    }

    public void write_index_to_file(TreeMap<String, Postlistings> dict, String name, int block) {
        try {
            File file = new File("./tmp/" + name + "_" + block);

            if (!file.exists()) {
                file.createNewFile();
            }
            //FileWriter fw = new FileWriter(file.getAbsoluteFile());
            FileOutputStream fop = new FileOutputStream(file);

            Dict dicts = new Dict();

            int offset1 = 0;
            int offset2 = 0;
            byte [] posRec = new byte[0];

            for (Map.Entry<String, Postlistings> entry : dict.entrySet()) {
                String word = entry.getKey();
                Postlistings pos = entry.getValue();
                int num = pos.getSize();
                Dict_entity dict_entity = new Dict_entity(num, offset1, offset2);
                dicts.add_to_dict(dict_entity, word);
                offset1 += word.length();
                byte[] pstr = pos.compress_to_byte();
                offset2 += pstr.length;
                posRec = util_func.byte_combine(posRec,pstr);
            }
            // write to dict file
            //Dict dicts = new Dict();
            dicts.write_to_file(fop);

            fop.write(posRec);
            fop.close();
            // write to index file

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void merge_two_file(File fir, File sec, String fileName) throws IOException {
        File file = new File(fileName + "_tmp");
        File output = new File(fileName);

        FileOutputStream fos1 = new FileOutputStream(file);
        FileOutputStream fos2 = new FileOutputStream(output);

        // 从文件中构造 Dict 结构
        // 重点，此处的索引表不能读入内存，必须从文件中读取。
        try {
            RandomAccessFile f1 = new RandomAccessFile(fir,"r");
            RandomAccessFile f2 = new RandomAccessFile(sec, "r");

            Dict d1 = new Dict();
            d1.read_from_file(f1);

            Dict d2 = new Dict();
            d2.read_from_file(f2);

            long pos1 = f1.getFilePointer();
            long pos2 = f2.getFilePointer();

            ArrayList<Dict_entity> del1 = d1.getTermList();
            ArrayList<Dict_entity> del2 = d2.getTermList();

            Dict d3 = new Dict();
            int i = 0, j = 0;
            int o1 = 0, o2 = 0;

            while (i < del1.size() && j < del2.size()) {
                Dict_entity a = del1.get(i);
                Dict_entity b = del2.get(j);
                String w1 = d1.getWord(i);
                String w2 = d2.getWord(j);
                Postlistings p = new Postlistings();
                byte [] pl = null;
                if (w1.equals(w2)) {
                    Postlistings p1 = new Postlistings();
                    Postlistings p2 = new Postlistings();
                    p1.decompress_from_byte(f1, pos1 + a.getPos_offset());
                    p2.decompress_from_byte(f2, pos2 + b.getPos_offset());
                    p.combine_pos(p1, p2);
                    Dict_entity c = new Dict_entity(p.getSize(), o1, o2);
                    // 写出p3
                    d3.add_to_dict(c, w1);
                    pl = p.compress_to_byte();
                    fos1.write(pl);
                    o1 += w1.length();
                    i++; j++;
                } else if (w1.compareTo(w2) > 0){
                    //写出p2
                    p.decompress_from_byte(f2, pos2 + b.getPos_offset());

                    Dict_entity c = new Dict_entity(p.getSize(), o1, o2);
                    d3.add_to_dict(c, w2);
                    pl = p.compress_to_byte();
                    fos1.write(pl);
                    o1 += w2.length();
                    j++;
                } else {
                    p.decompress_from_byte(f1, pos1 + a.getPos_offset());

                    Dict_entity c = new Dict_entity(p.getSize(), o1, o2);
                    d3.add_to_dict(c, w1);
                    pl = p.compress_to_byte();
                    fos1.write(pl);

                    o1 += w1.length();
                    i++;
                }

                o2 += pl.length;
            }
            while (i < del1.size()) {
                Dict_entity a = del1.get(i);
                String w1 = d1.getWord(i);
                Postlistings p = new Postlistings();
                p.decompress_from_byte(f1, pos1 + a.getPos_offset());

                Dict_entity c = new Dict_entity(p.getSize(), o1, o2);
                d3.add_to_dict(c, w1);
                byte [] pl = p.compress_to_byte();
                fos1.write(pl);
                i++;

                o1 += w1.length();
                o2 += pl.length;
            }
            while (j < del2.size()) {
                Dict_entity a = del2.get(j);
                String w2 = d2.getWord(j);
                Postlistings p = new Postlistings();
                p.decompress_from_byte(f2, pos2 + a.getPos_offset());

                Dict_entity c = new Dict_entity(p.getSize(), o1, o2);
                d3.add_to_dict(c, w2);
                byte [] pl = p.compress_to_byte();
                fos1.write(pl);
                j++;

                o1 += w2.length();
                o2 += pl.length;
            }

            d3.write_to_file(fos2);

            fos1.close();
            fos2.close();

            util_func.concatFileUsingFileChannels(file, output);
            file.delete();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void merge_index_from_disk() {
        int nw = 0;

        // 讲tmp中的文件进行merge
        // merge 直到只剩下一个文件
        int cnt = 1;
        while (true) {
            File file = new File("./tmp/");
            File[] files = file.listFiles();
            ArrayList<File> fileList = new ArrayList<File>();
            for (File f : files) {
                String name = f.getName();
                if (!f.isDirectory() && f.getName().contains("block"))
                    fileList.add(f);
            }
            if (fileList.size() == 1) {
                File f1 = fileList.get(0);
                File dest = new File("./index/index_final");
                //System.out.println(f1.getName());
                try {
                    util_func.copyFileUsingFileChannels(f1, dest);
                    f1.delete();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }
            int numBlock = 1;
            // 2,2 merge
            // filename : block_cnt_numBlock
            try {
                if (fileList.size() % 2 == 1) {
                    File dest = new File("./tmp/block_" + cnt + "_" + numBlock);
                    // dest.createNewFile();
                    util_func.copyFileUsingFileChannels(fileList.get(0), dest);
                    numBlock ++;
                    fileList.get(0).delete();
                    fileList.remove(0);
                }
                //System.out.println("gg");

                for (int i = 0; i < fileList.size(); i += 2) {
                    //System.out.println(fileList.get(i).getName());
                    //System.out.println(fileList.get(i + 1).getName());
                    String fileOutput = "./tmp/block_" + cnt + "_" + numBlock;
                    merge_two_file(fileList.get(i), fileList.get(i+1), fileOutput);
                }

                for (File f : fileList) {
                    f.delete();
                }
                cnt += 1;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    public static void main( String[] args )
    {
        SPIMI_indexer indexer = new SPIMI_indexer();
        indexer.construct_index();
        /*int a = 1000000;
        File f = new File("./tmp/tmp");
        try {
            FileWriter fw = new FileWriter(f.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            String c = "123456789";
            bw.write(c);
            bw.write(a+"");
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }
}
