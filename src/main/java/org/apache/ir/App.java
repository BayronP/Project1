package org.apache.ir;

import org.apache.ir.finder.Finder;
import org.apache.ir.indexer.Indexer;
import org.apache.ir.indexer.SPIMI_indexer;
import org.apache.ir.util.Postlistings;

import javax.print.attribute.standard.Fidelity;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Hello world!
 *
 */
public class App 
{
    public static void main( String[] args )
    {

        System.out.println("Shakespeare-Merchant Retrieval System");
        System.out.println("********** OPTION ***********");
        System.out.println("1. Construct Index and Show Doc Info");
        System.out.println("2. Load Index");
        System.out.println("3. Search Doc by Word");
        System.out.println("4. Exit System");
        Scanner sc = new Scanner(System.in);
        Finder finder = new Finder();

        while (true) {
            System.out.println("Please Choose : ");
            int opt = sc.nextInt();
            sc.nextLine();
            switch (opt) {
                case 1:
                    SPIMI_indexer indexer = new SPIMI_indexer();
                    indexer.construct_index();
                    System.out.println("Finished Construction");
                    indexer.outputStatisticalInfo();
                    break;
                case 2:
                    try {
                        finder.load_index();
                        System.out.println("Finished Loading");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 3:
                    //sc.next();
                    System.out.println("Input Your Word : ");
                    String word = sc.nextLine();
                    try {
                        System.out.println("Searching Result : ");
                        Postlistings postlistings = finder.find(word);
                        ArrayList<String> res = finder.get_doc_name(postlistings);

                        for (String s : res) {
                            System.out.println(s);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case 4:
                    System.out.println("EXITING!");
                    return;
                default:
                    break;
            }

        }
    }
}
