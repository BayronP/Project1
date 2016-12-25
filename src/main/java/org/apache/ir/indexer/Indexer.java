package org.apache.ir.indexer;

import org.apache.ir.util.Postlistings;

import java.io.File;
import java.util.TreeMap;

/**
 * Created by bayron on 2016/11/24.
 */
public interface Indexer {
    void construct_index();
    void write_index_to_file(TreeMap<String, Postlistings> dict, String name, int block);
    void merge_index_from_disk();
}
