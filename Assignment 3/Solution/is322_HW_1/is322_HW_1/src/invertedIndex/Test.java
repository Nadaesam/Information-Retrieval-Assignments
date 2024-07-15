/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import crawler.WebCrawlerWithDepth;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static invertedIndex.Index5.printPosIndex;

/**
 *
 * @author ehab
 */
public class Test {

    public static void main(String args[]) throws IOException {
        Index5 index = new Index5();
        
        //|**  change it to your collection directory
        //|**  in windows "C:\\tmp11\\rl\\collection\\"
        String files = "F:/Lamiaa_20201230_20200096_20210613_20200517_20210557/Lamiaa_20201230_20200096_20210613_20200517_20210557/is322_HW_1/is322_HW_1/tmp11/rl/collection/";

        File file = new File(files);
        //|** String[] 	list()
        //|**  Returns an array of strings naming the files and directories in the directory denoted by this abstract pathname.
        String[] fileList = file.list();

        fileList = index.sort(fileList);
        index.N = fileList.length;

        for (int i = 0; i < fileList.length; i++) {
            fileList[i] = files + fileList[i];
        }
        System.out.println("POSITIONAL INDEX");
        Index5.IndexBuild(fileList);
        printPosIndex();

        System.out.println("BI WORD");
        index.buildIndex(fileList);
        index.store("index");
        index.printDictionary();

        String test3 = "data  should plain greatest comif"; // data  should plain greatest comif
        System.out.println("Boo0lean Model result = \n" + index.find_24_01(test3));

        String phrase = "";

        do {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Choose 1 for BI Word Index or Choose  2 for Positional Index or choose 3 for web crawler ");
            String option = in.readLine();
            if (option.equals("1")) {
                System.out.println("Enter search Querey: ");

                phrase = in.readLine();

                // Split the input string by space outside of quotes
                String[] words = phrase.split(" (?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                // Replace spaces with underscores between words in quotes
                for (int i = 0; i < words.length; i++) {
                    if (words[i].startsWith("\"") && words[i].endsWith("\"")) {
                        words[i] = words[i].substring(1, words[i].length() - 1).replace(" ", "_");
                    }
                }

                // Join the modified words back into a single string
                String output = String.join(" ", words);

                // Print the modified string
                System.out.println(output);
                // automated specific_info
/// -3- **** complete here ****
                String res = index.find_24_01(output);
                System.out.println(res);
            } else if (option.equals("2")) {
                System.out.println("Enter search Querey: ");
                phrase = in.readLine();
                Index5.QuarayFind(phrase);
            }
            else{
                WebCrawlerWithDepth wc = new WebCrawlerWithDepth();

                Index5 index2 = wc.initialize("test");

                String query = "narmer giza pyramid";
                Map<Integer, Double> cosineSimilarities = index2.calculateCosineSimilarities(query);

                List<Map.Entry<Integer, Double>> sortedEntries = cosineSimilarities.entrySet().stream()
                        .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                        .limit(10)
                        .collect(Collectors.toList());

                for (Map.Entry<Integer, Double> entry : sortedEntries) {
                    System.out.println("Document ID: " + entry.getKey() + ", URL: " + wc.getSourceName(entry.getKey()) + ", Cosine Similarity: " + entry.getValue());
                }

            }



        } while (!phrase.isEmpty());

    }
}