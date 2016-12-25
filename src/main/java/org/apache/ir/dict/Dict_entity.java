package org.apache.ir.dict;

import org.apache.ir.util.Postlistings;
import org.apache.ir.util.Util_func;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by bayron on 2016/10/31.
 */
public class Dict_entity {
    private Integer doc_fre;
    private Integer word_offset;
    private Integer pos_offset;
    private Util_func util = new Util_func();

    public Dict_entity(Integer df, Integer wo, Integer po) {
        this.doc_fre = df;
        this.word_offset = wo;
        this.pos_offset = po;
    }

    public Postlistings getPostlisting(RandomAccessFile file, long offset) throws IOException {
        Postlistings pl = new Postlistings();

        file.seek(offset);
        // read pos size
        int pSize = util.readInt(file);
        for (int i = 0; i < pSize; i++) {
            int docId = util.readInt(file);
            pl.add_record(docId);
        }

        return pl;
    }

    public Integer getDoc_fre() {
        return doc_fre;
    }

    public Integer getPos_offset() {
        return pos_offset;
    }

    public Integer getWord_offset() {
        return word_offset;
    }

    public void setDoc_fre(Integer doc_fre) {
        this.doc_fre = doc_fre;
    }

    public void setPos_offset(Integer pos_offset) {
        this.pos_offset = pos_offset;
    }

    public void setWord_offset(Integer word_offset) {
        this.word_offset = word_offset;
    }

}
