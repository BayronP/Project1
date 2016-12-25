package org.apache.ir.parser;

import org.apache.ir.util.Token;

import java.io.FileNotFoundException;

/**
 * Created by bayron on 2016/11/24.
 */
public interface Parser {
    String file_path = "";
    boolean hasNextToken() throws FileNotFoundException;
    Token nextToken();
}
