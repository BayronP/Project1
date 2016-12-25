package org.apache.ir.util;

/**
 * Created by bayron on 2016/10/31.
 */
public class Token {
    private String word;
    private int position;
    private int docId;

    public Token(String word, int docid, int position) {
        this.word = word;
        this.docId = docid;
        this.position = position;
    }

    public int getDocId() {
        return docId;
    }

    public void setDocId(int docId) {
        this.docId = docId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int pos) {
        this.position = pos;
    }
    
}
