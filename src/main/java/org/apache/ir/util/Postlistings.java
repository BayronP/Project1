package org.apache.ir.util;

import javafx.geometry.Pos;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

/**
 * Created by bayron on 2016/10/31.
 */
public class Postlistings {
    private TreeSet<Integer> poslist;
    private Util_func util_func = new Util_func();

    public Postlistings() {
        poslist = new TreeSet<Integer>();
    }

    public int getSize() {
        return poslist.size();
    }

    public void add_record(int docid) {
        poslist.add(docid);
    }

    public TreeSet<Integer> getPoslist() {
        return poslist;
    }

    public void combine_pos(Postlistings a, Postlistings b) {
        TreeSet<Integer> p1 = a.getPoslist();
        TreeSet<Integer> p2 = b.getPoslist();
        for (Integer num : p1) {
            this.poslist.add(num);
        }
        for (Integer num : p2) {
            this.poslist.add(num);
        }
    }

    public byte [] gap_to_byte(int num) {
        ArrayList<Byte> res = new ArrayList<Byte>();
        while(true) {
            res.add((byte) (0xff & (num % 128)));
            if (num < 128) break;
            num = num >> 7;
        }
        int tail = res.size()-1;
        res.set(0, (byte)(res.get(0) + 128));
        byte [] gap = new byte[res.size()];
        int i = tail;
        for (Byte b : res) {
            gap[i] = b;
            i--;
        }
        return gap;
    }

    public void byte_to_gap(byte [] arr) {
        int num = 0;
        int last = 0;
        //System.out.println(arr.length);
        for (int i = 0; i < arr.length; i++) {
            //System.out.println(arr[i] & 0x0000007F);
            num = (num << 7) + (arr[i] & 0x0000007F);
            if ((arr[i] & 0x80) != 0) {
                this.poslist.add(last + num);
                last = last + num;
                num = 0;
            }
        }
    }

    public byte [] compress_to_byte() {
        byte [] res = new byte[0];
        //System.out.println(res.length);
        int last = 0;
        for(Integer pos : poslist) {
            byte [] tmp = gap_to_byte(pos - last);
            res = util_func.byte_combine(res, tmp);
            last = pos;
        }
        int s = res.length;
        res = util_func.byte_combine(util_func.int_to_byte(s), res);
        return res;
    }

    public Postlistings decompress_from_byte(RandomAccessFile f, long offset) throws IOException {
        f.seek(offset);
        int last = 0;
        int ps = util_func.readInt(f);
        byte [] buf = new byte[ps];
        f.read(buf, 0, ps);
        byte_to_gap(buf);
        //for (int i = 0; i < ps; i++) {
        //    this.poslist.add(util_func.readInt(f));
        //}
        return this;
    }

    public Postlistings decompress_from_byte(byte [] buf) {
        byte_to_gap(buf);
        return this;
    }

    public static void main( String[] args ) {
        int a = 824;
        Postlistings postlistings = new Postlistings();

        for (int i = 0; i < 100; i++) {
            postlistings.add_record(i);
        }
        byte [] tmp = postlistings.compress_to_byte();
        postlistings.decompress_from_byte(tmp);
        for (Integer k : postlistings.getPoslist()) {
            System.out.println(k);
        }
        //byte [] res = postlistings.gap_to_byte(a);
        //int num = postlistings.byte_to_gap(res);
        //System.out.println(num);

        //System.out.println(num);
        //for (int i = 0; i < res.length; i++) {
        //    System.out.println((int)res[i]);
        //}

    }
}
