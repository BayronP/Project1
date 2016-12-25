package org.apache.ir.finder;

import javafx.geometry.Pos;
import org.apache.ir.dict.Dict;
import org.apache.ir.dict.Dict_entity;
import org.apache.ir.parser.Stemmer;
import org.apache.ir.util.Postlistings;
import org.apache.ir.util.Util_func;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;

/**
 * Created by bayron on 2016/12/24.
 */
public class Finder {
    private String path = "./index/index_final";
    private HashMap<Integer, String> index_to_name = new HashMap<Integer, String>();
    private Dict dict = new Dict();
    private Stemmer stemmer = new Stemmer();
    private long base_offset = 0;
    private RandomAccessFile file = null;
    private Util_func util_func = new Util_func();

    public void load_index() throws IOException {
        file = new RandomAccessFile(path, "r");
        dict = new Dict();
        dict.read_from_file(file);
        base_offset = file.getFilePointer();

        // load index to name

        File file = new File("./index/id_to_doc");
        try {
            BufferedReader rdr = new BufferedReader(new FileReader(file));
            while(true) {
                String line = rdr.readLine();
                if (line == null) {
                    break;
                }
                String[] fields = line.split(" ");
                index_to_name.put(new Integer(fields[0]), fields[1]);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Postlistings find(String word) throws IOException {

        word = word.toLowerCase();
        stemmer.add(word.toCharArray(), word.length());
        stemmer.stem();
        word = stemmer.toString();
        System.out.println(word);

        ArrayList<Dict_entity> termList = dict.getTermList();
        int l = 0, r = termList.size();
        Postlistings postlistings = new Postlistings();
        int mid = 0;
        while (l < r) {
            mid = (l + r) >> 1;
            String w = dict.getWord(mid);
            if (w.equals(word)) {
                postlistings.decompress_from_byte(file, base_offset + termList.get(mid).getPos_offset());
                break;
            } else if (w.compareTo(word) > 0) {
                // w > word
                r = mid + 1;
            } else {
                l = mid;
            }
        }

        return postlistings;
    }

    public ArrayList<String> get_doc_name(Postlistings postlistings) {
        ArrayList<String> res = new ArrayList<String>();
        for (Integer k : postlistings.getPoslist()) {
            res.add(index_to_name.get(k));
        }
        return res;
    }

    public static void main( String[] args ) {

        Finder finder = new Finder();
        try {
            finder.load_index();
            Postlistings postlistings = finder.find("a");
            TreeSet<Integer> a = postlistings.getPoslist();
            ArrayList<String> res = finder.get_doc_name(postlistings);
            for (String term : res) {
                System.out.println(term);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
