package org.apache.ir.dict;

import javafx.geometry.Pos;
import org.apache.ir.util.Postlistings;
import org.apache.ir.util.Util_func;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by bayron on 2016/10/31.
 */
public class Dict {
    private String asString = new String("");
    private ArrayList<Dict_entity> termList = new ArrayList<Dict_entity>();
    private Util_func util = new Util_func();

    public Dict() {

    }

    public Dict(HashMap<String, Postlistings> dict) {
        // construct asString and termList

    }

    public String getWord(int i) {
        int be = termList.get(i).getWord_offset();
        //System.out.println(be);
        int ed = asString.length();
        if (i != termList.size()-1)
            ed = termList.get(i+1).getWord_offset();
        //System.out.println();
        return asString.substring(be, ed);
    }

    public ArrayList<Dict_entity> getTermList() {
        return termList;
    }

    public void setTermList(ArrayList<Dict_entity> tl) {
        for (int i = 0; i < tl.size(); i++) {
            termList.add(tl.get(i));
        }
    }

    public void add_to_dict(Dict_entity de, String word) {
        termList.add(de);
        asString += word;
    }



    // find word postlist from disk


    public void read_from_file(RandomAccessFile file) throws IOException {
        //System.out.println(file);

        int ss = util.readInt(file);
        int ds = util.readInt(file);
        //System.out.println(ds);

        //System.out.println(ds);
        //System.out.println(ss);

        byte [] buf = new byte[ss];
        file.read(buf, 0, ss);

        this.asString = new String(buf);
        //System.out.println(this.asString);
        for (int i = 0; i < ds; i++) {
            int df = util.readInt(file);
            int wo = util.readInt(file);
            int po = util.readInt(file);
            Dict_entity de = new Dict_entity(df, wo, po);
            termList.add(de);

            //System.out.println(df + " " + wo + " " + po);
        }
    }

    public void write_to_file(FileOutputStream bw) {
        // 写入size
        int dictSize = termList.size();
        //System.out.println(dictSize);
        int strSize = asString.length();
        //System.out.println(strSize);
        try {
            bw.write(util.int_to_byte(strSize));
            bw.write(util.int_to_byte(dictSize));
            bw.write(asString.getBytes());
            //System.out.println(asString.getBytes().length);
            for (int i = 0; i < termList.size(); i++) {
                Dict_entity tmp = termList.get(i);
                //System.out.println(tmp.getDoc_fre());
                bw.write(util.int_to_byte(tmp.getDoc_fre()));
                bw.write(util.int_to_byte(tmp.getWord_offset()));
                bw.write(util.int_to_byte(tmp.getPos_offset()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main( String[] args ) {
        try {
            RandomAccessFile file  = new RandomAccessFile("./tmp/block_2_1", "r");
            Dict d1 = new Dict();
            d1.read_from_file(file);
            System.out.println(d1.getWord(1));
            Dict_entity a = d1.getTermList().get(1);

            long dd = file.getFilePointer() + a.getPos_offset();
            Postlistings k = new Postlistings();
            k.decompress_from_byte(file, dd);
            for (Integer xx : k.getPoslist()) {
                System.out.println(xx);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
