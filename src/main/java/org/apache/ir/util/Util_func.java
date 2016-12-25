package org.apache.ir.util;

import java.io.*;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

/**
 * Created by bayron on 2016/12/22.
 */
public class Util_func {
    public byte [] int_to_byte(int k) {
        byte [] res = new byte[4];
        res[0] =  (byte)((0xff000000 & k) >> (8*3));
        res[1] = (byte)((0x00ff0000 & k) >> (8*2));
        res[2] = (byte)((0x0000ff00 & k) >> (8*1));
        res[3] = ((byte)(0x000000ff & k));
        return res;
    }

    public byte [] long_to_byte(long k) {
        int high = (int) (k >> 32);
        int low = (int) k;

        byte [] h = int_to_byte(high);
        byte [] l = int_to_byte(low);

        byte [] res = byte_combine(h, l);
        return res;
    }

    public byte [] byte_combine(byte [] a, byte [] b) {
        int sa = a.length;
        int sb = b.length;
        byte [] res = new byte[sa + sb];
        for (int i = 0;i < sa;i++) {
            res[i] = a[i];
        }

        for (int i = 0;i < sb;i++) {
            res[i + sa] = b[i];
        }
        return res;
    }

    public int byte_to_int(byte [] k) {
        int res = 0;
        for (int i = 0; i < k.length; i++) {
            res = (res << 8) + (0x000000ff & k[i]);
        }
        return res;
    }

    public long byte_to_long(byte [] k) {
        long res = 0;
        for (int i = 0; i < k.length; i++) {
            res = (res << 8) + (0x000000ff & k[i]);
        }
        return res;
    }

    public void copyFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest).getChannel();
            outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    public void concatFileUsingFileChannels(File source, File dest) throws IOException {
        FileChannel inputChannel = null;
        FileChannel outputChannel = null;
        try {
            inputChannel = new FileInputStream(source).getChannel();
            outputChannel = new FileOutputStream(dest, true).getChannel();
            outputChannel.transferFrom(inputChannel, outputChannel.size(), inputChannel.size());
        } finally {
            inputChannel.close();
            outputChannel.close();
        }
    }

    public void removeAllFile(String folder) {
        File file = new File(folder);
        File[] files = file.listFiles();
        //ArrayList<File> fileList = new ArrayList<File>();
        for (File f : files) {
            f.delete();
        }
    }

    public int readInt(RandomAccessFile f) throws IOException {
        byte [] res = new byte[4];
        f.read(res, 0, 4);
        return byte_to_int(res);
    }

    public static void main( String[] args ) {
        long ab = (long)1 << 40;
        System.out.println(ab);
        Util_func util_func = new Util_func();
        byte [] x =util_func.long_to_byte(ab);
        //System.out.println(x);
        //System.out.println(x);
        System.out.println(util_func.byte_to_long(x));
    }
}
