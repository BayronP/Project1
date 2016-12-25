package org.apache.ir.parser;

import java.io.*;
import java.util.*;

import org.apache.ir.util.Token;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.ir.util.Util_func;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import javax.print.Doc;

/**
 * Created by bayron on 2016/11/24.
 */
public class TrecParser implements Parser {
    String file_dir = "";  //目录
    private Queue<Token> tokenQueue = new LinkedList<Token>();
    private Queue<File> fileQueue = new LinkedList<File>();
    private Stemmer stemmer = new Stemmer();
    private int doc_cnt = 1;       // 文档数目
    private HashMap<String, Integer> doc_to_id = new HashMap<String, Integer>();
    private Util_func util_func = new Util_func();

    // 统计量
    private long tokenNum = 0;      // 词条总数

    public long getTokenNum() {
        return tokenNum;
    }

    public long getAverDocLen() {
        return (long)Math.floor(tokenNum / ((doc_cnt-1) * 1.0));
    }

    public int getDoc_cnt() {
        return doc_cnt - 1;
    }

    public TrecParser(String fd) {
        file_dir = fd;
        File file = new File(fd);
        File [] files = file.listFiles();
        for (File f : files) {
            String name = f.getName();
            if (!f.isDirectory() && name.contains("shakespeare-merchant"))
                fileQueue.add(f);
        }
    }

    private ArrayList<String> parseSingleStr(String line) {
        ArrayList<String> res = new ArrayList<String>();
        // 1.去除<tag>或者</tag>
        line = line.replaceAll("<\\S+>"," ").replaceAll("\\W"," ").replaceAll("\\s+", " ");
        String[] fields = line.split(" ");
        for (String f : fields) {
            if (!f.equals("")) {
                res.add(f);
            }
        }
        return res;
    }

    public boolean hasNextToken() {
        if (tokenQueue.isEmpty() && !fileQueue.isEmpty()) {
            // load new file (lazy)
            File f = fileQueue.poll();
            BufferedReader rdr = null;
            try {
                rdr = new BufferedReader(new FileReader(f));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Pattern docno_tag = Pattern.compile("<DOCNO>\\s*(\\S+)\\s*</DOCNO>");

            // title 可能分2行

            boolean in_doc = false;

            String line;
            //int i = 10;
            try {
                while(true) {
                    line = rdr.readLine();
                    //System.out.println(line);
                    //i--;
                    //if (i!=0) continue;
                    // find doc no
                    // get title token
                    // method string to token
                    // 考虑标签匹配
                    if (line == null) {
                        break;
                    }
                    if (!in_doc) {
                        if (line.startsWith("<DOC>")) {
                            //System.out.println("DOC");
                            in_doc = true;
                        }
                        continue;
                    }

                    if (line.startsWith("</DOC>")) {
                        in_doc = false;
                        //System.out.println("Another DOC");
                        continue;
                    }

                    //内容 content 解析
                    // 先做标签匹配 目前支持DOCNO, title和spearker
                    //System.out.println("content");
                    Matcher m = docno_tag.matcher(line);
                    if (m.find()) {
                        String docno = m.group(1);
                        doc_to_id.put(docno, doc_cnt);
                        doc_cnt++;
                        //System.out.println("docno is " + docno);
                        continue;
                    }

                    // match other tags
                    line = line.trim();
                    if (!line.equals("")) {
                        ArrayList<String> fs = parseSingleStr(line);
                        tokenNum += fs.size();
                        for (String ft : fs) {
                            ft = ft.toLowerCase();
                            stemmer.add(ft.toCharArray(), ft.length());
                            stemmer.stem();
                            Token k = new Token(stemmer.toString(), doc_cnt-1, 0);
                            tokenQueue.add(k);
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return !tokenQueue.isEmpty();
    }

    public Token nextToken() {
        if (hasNextToken()) {
            return tokenQueue.poll();
        }
        return null;
    }

    public void write_id_to_doc() throws IOException {
        File file = new File("./index/id_to_doc");
        if (!file.exists()) {
            file.createNewFile();
        }
        FileOutputStream fos = new FileOutputStream(file);
        // id doc
        // int str \n
        for(Map.Entry<String, Integer> entry : doc_to_id.entrySet()) {
            fos.write((entry.getValue().toString() + " ").getBytes());
            fos.write((entry.getKey()+"\n").getBytes());
        }
        fos.close();
    }

    public void test2(RandomAccessFile raf) throws IOException {
        System.out.println(raf.readLine());
        System.out.println(raf.readLine());
        System.out.println(raf.readLine());
    }

    public void testReadFile() throws IOException {
        try {
            RandomAccessFile raf = new RandomAccessFile(new File("./tmp/tmp"),"rw");
            //raf.writeLong(1999);
            //raf.writeChars("1234567");
            //long pos = raf.getFilePointer();
            //raf.seek(0);
            //byte [] k = new byte[2];

            //System.out.println(raf.readLong());
            //raf.read(k);
            //raf.readChar();
            String k = "123";
            byte [] b = k.getBytes();
            System.out.println(b.length);
            System.out.println(raf.readByte());
            System.out.println((char) raf.readByte());
            System.out.println((char) raf.readByte());
            raf.seek(raf.length());
            raf.writeByte(49);
            //System.out.println((char) raf.readByte());
            //System.out.println(raf.readChar());
            //System.out.println(raf.readChar());
            //System.out.println(raf.readChar());
            //System.out.println(raf.readChar());
            //System.out.println(raf.readChar());System.out.println(raf.readChar());
            //if (raf.length() == raf.getFilePointer()) {
            //    System.out.println("hh");
            //}
            //System.out.println(raf.readChar());
            //raf.read(k,(int)raf.getFilePointer(),8);
            //System.out.println(new String(k));
            //System.out.println(raf.getFilePointer());
            //test2(raf);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

    public static void main( String[] args ) throws IOException {
        TrecParser tmp = new TrecParser("./");
        //tmp.testReadFile();

        tmp.hasNextToken();
        while (tmp.hasNextToken()) {
            System.out.println(tmp.nextToken().getDocId());
        }
    }
}
