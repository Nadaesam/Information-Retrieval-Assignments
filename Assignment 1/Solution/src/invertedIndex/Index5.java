/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package invertedIndex;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.Writer;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.log10;
import static java.lang.Math.sqrt;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.io.PrintWriter;

/**
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------
    int N = 0;

    public Map<Integer, SourceRecord> sources;  // store the doc_id and the file name.

    public HashMap<String, DictEntry> index; // THe inverted index
    //--------------------------------------------

    public Index5() {
        sources = new HashMap<Integer, SourceRecord>();
        index = new HashMap<String, DictEntry>();
    }

    public void setN(int n) {
        N = n;
    }


    //---------------------------------------------
    public void printPostingList(Posting p) { //to print the posting list
        System.out.print("[");
        while (p != null) { //if it is not mull so print it and the doc id
            System.out.print("" + p.docId);
            if (p.next != null) {  //and then print comma
                System.out.print(",");
            }
            p = p.next;
        }
        System.out.println("]");
    }

    //---------------------------------------------
    public void printDictionary() { //function to print the dictionary
        Iterator it = index.entrySet().iterator();//Iterator to iterate the entries
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next(); //to Get the next entry in the index map
            DictEntry dd = (DictEntry) pair.getValue();
            //print the term and the frequency of the document and posting list
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + index.size()); //Print the number of terms in the index
    }
 
    //-----------------------------------------------
    public void buildIndex(String[] files) {// from disk not from the internet
        // Initialize file ID = 0
        int fid = 0;
        // Loop on all files in the array
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                // Check if source map doesn't contain the file ID
                if (!sources.containsKey(fid)) {
                    // Create a new source record and put it in the map
                    sources.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                // Initialize line variable
                String ln;
                // Initialize file length variable
                int flen = 0;
                // Read each line on the file
                while ((ln = file.readLine()) != null) {
                    ///** hint   flen +=  ______(ln, fid);
                    // Call method index one line and update file length
                    flen += indexOneLine(ln, fid);
                }
                // Update file length in the source record
                sources.get(fid).length = flen;
            } catch (IOException e) {
                // if file not found or IO exception print not found
                System.out.println("File " + fileName + " not found. Skip it");
            }
            // Increment file ID
            fid++;
        }
        // Print the dictionary after indexing
        printDictionary();
    }

    //----------------------------------------------------------------------------  
    public int indexOneLine(String ln, int fid) {
        int flen = 0;

        String[] words = ln.split("\\W+");
      //   String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        flen += words.length;
        for (String word : words) {
            word = word.toLowerCase();
            if (stopWord(word)) {
                continue;
            }
            word = stemWord(word);
            // check to see if the word is not in the dictionary
            // if not add it
            if (!index.containsKey(word)) {
                index.put(word, new DictEntry());
            }
            // add document id to the posting list
            if (!index.get(word).postingListContains(fid)) {
                index.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term 
                if (index.get(word).pList == null) {
                    index.get(word).pList = new Posting(fid);
                    index.get(word).last = index.get(word).pList;
                } else {
                    index.get(word).last.next = new Posting(fid);
                    index.get(word).last = index.get(word).last.next;
                }
            } else {
                index.get(word).last.dtf += 1;
            }
            //set the term_fteq in the collection
            index.get(word).term_freq += 1;
            if (word.equalsIgnoreCase("lattice")) {

                System.out.println("  <<" + index.get(word).getPosting(1) + ">> " + ln);
            }

        }
        return flen;
    }

//----------------------------------------------------------------------------  
boolean stopWord(String word) {
    // Check if the word is one of the commonly used stop words.
    if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
            || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
        // If it is a stop word, return true.
        return true;
    }
    // Check if the length of the word is less than 2 characters.
    if (word.length() < 2) {
        // If it is less than 2 characters, return true.
        return true;
    }
    // If the word is not a stop word and its length is at least 2 characters, return false.
    return false;

}

//----------------------------------------------------------------------------  

    String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------
    Posting intersect(Posting pL1, Posting pL2) {
        ///****  -1-   complete after each comment ****
        //   INTERSECT ( p1 , p2 )
        //          1  answer ←      {}
        Posting answer = null;
        Posting last = null;
        //2- While both posting lists are not empty
        while (pL1 != null && pL2 != null) {
            //3- If the document IDs match
            if (pL1.docId == pL2.docId) {
                //4- Add the document ID to the answer posting
                if (answer == null) {
                    answer = new Posting(pL1.docId);
                    last = answer;
                } else {
                    last.next = new Posting(pL1.docId);
                    last = last.next;
                }
                //5,6- Move to the next postings in both lists
                pL1 = pL1.next;
                pL2 = pL2.next;
            }
            //7- If the document ID of pL1 is less than pL2
            else if (pL1.docId < pL2.docId) {
                //8- Move to the next posting in pL1
                pL1 = pL1.next;
            }
            else {
                //9- Move to the next posting in pL2
                pL2 = pL2.next;
            }
        }
        // Return the intersection of the two posting lists
        return answer;
    }

    //function take the phrase to search for it
     public String find_24_01(String phrase) { // any mumber of terms non-optimized search
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        //check the first word if it exists in the index
        Posting posting = null;
        if (index.containsKey(words[0].toLowerCase())) { //if found it
            posting = index.get(words[0].toLowerCase()).pList; //so get this word
        } else { //it means that there is one term not in the index so it will print this message
            return "Your search phrase has at least one term is not in the index.";
        }
        int i = 1;
        while (i < len) {  //check the next word if it exists in the index
            if (index.containsKey(words[i].toLowerCase())) {
                //If it exists so call function intersect to intersect its posting list with the current posting list
                posting = intersect(posting, index.get(words[i].toLowerCase()).pList);
            } else{ //it means that it is not in the index
                return "Your search phrase has at least one term is not in the index.";
            }
            i++;
        }

        //This get the result of posting list and print the document information
        while (posting != null) {
            result += "\t" + posting.docId + " - " + sources.get(posting.docId).title + " - " + sources.get(posting.docId).length + "\n";
            posting = posting.next;
        }
        return result; //return the answer
    }
    
    
    //---------------------------------
    //    sort array of strings using bubble sort
    String[] sort(String[] words) {
//        var to check if the array sorted or not
        boolean sorted = false;
//        temp string variable for swapping
        String sTmp;
        //-------------------------------------------------------
        // intialize the array is sorted
        while (!sorted) {
            sorted = true;
            // loop through the array
            for (int i = 0; i < words.length - 1; i++) {
                // Compare all elements
                int compare = words[i].compareTo(words[i + 1]);
                // If current element is greater than the next element
                if (compare > 0) {
                    // Swap the elements
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        // Return the sorted array of words
        return words;
    }

     //---------------------------------

    public void store(String storageName) {
        try {
            // Define the path to the storage file
            String pathToStorage = "/home/ehab/tmp11/rl/" + storageName;

            // Create a FileWriter to write to the storage file
            Writer wr = new FileWriter(pathToStorage);

            // Iterate through the sources map
            for (Map.Entry<Integer, SourceRecord> entry : sources.entrySet()) {
                // Print information about each source record to console
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);

                // Write data from each source record to the storage file
                wr.write(entry.getKey().toString() + ","); // Write source record key
                wr.write(entry.getValue().URL.toString() + ","); // Write source record URL
                wr.write(entry.getValue().title.replace(',', '~') + ","); // Write source record title, replace commas with ~
                wr.write(entry.getValue().length + ","); // Write source record length
                wr.write(String.format("%4.4f", entry.getValue().norm) + ","); // Write normalized value
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n"); // Write source record text, replace commas with ~
            }

            // Write a marker indicating the end of section 1
            wr.write("section2" + "\n");

            // Iterate through the index map
            Iterator it = index.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();

                // Write term information to the storage file
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";"); // Write term, document frequency, and term frequency

                Posting p = dd.pList;
                while (p != null) {
                    // Write posting list information to the storage file
                    wr.write(p.docId + "," + p.dtf + ":"); // Write document ID and document term frequency
                    p = p.next;
                }
                wr.write("\n"); // Move to the next line
            }

            // Write a marker indicating the end of section 2
            wr.write("end" + "\n");

            // Close the FileWriter
            wr.close();

            // Print message indicating the completion of the storage process
            System.out.println("=============EBD STORE=============");
        } catch (Exception e) {
            // Print stack trace if an exception occurs
            e.printStackTrace();
        }
    }

    //=========================================
    public boolean storageFileExists(String storageName){
        // Create a File object with the path to the storage file
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/"+storageName);

        // Check if the file exists and is not a directory
        if (f.exists() && !f.isDirectory())
            // If the file exists and is not a directory, return true
            return true;

        // If the file does not exist or is a directory, return false
        return false;
    }

    //----------------------------------------------------
    public void createStore(String storageName) {
        try {
            // Define the path to the storage file
            String pathToStorage = "/home/ehab/tmp11/" + storageName;

            // Create a FileWriter to write to the storage file
            Writer wr = new FileWriter(pathToStorage);

            // Write a marker indicating the end of the storage file
            wr.write("end" + "\n");

            // Close the FileWriter
            wr.close();
        } catch (Exception e) {
            // Print stack trace if an exception occurs
            e.printStackTrace();
        }
    }

    //----------------------------------------------------
     //load index from hard disk into memory
    public HashMap<String, DictEntry> load(String storageName) {
        try {
            // Define the path to the storage file
            String pathToStorage = "/home/ehab/tmp11/rl/" + storageName;

            // Initialize HashMaps to store loaded data
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();

            // Open the storage file for reading
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;

            // Read lines from the file until reaching "section2" marker
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                // Split the line into components
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    // Create a SourceRecord object from the line's components
                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    // Add the SourceRecord to the sources HashMap
                    sources.put(fid, sr);
                } catch (Exception e) {
                    // Print error message if an exception occurs during SourceRecord creation
                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Read lines from the file until reaching "end" marker
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                // Split the line into components
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");

                // Create a new DictEntry and add it to the index HashMap
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx; // Posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    // Add Postings to the corresponding DictEntry
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            // Print a message indicating the end of loading
            System.out.println("============= END LOAD =============");
        } catch (Exception e) {
            // Print stack trace if an exception occurs
            e.printStackTrace();
        }
        // Return the loaded index HashMap
        return index;
    }

}

//=====================================================================
