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

import java.util.*;

/**
 *
 * @author ehab
 */
public class Index5 {

    //--------------------------------------------
    int N = 0;
    int pos = 1;
    public Map<Integer, SourceRecord> sources = new HashMap<>();
    private Map<String, Map<Integer, Integer>> index = new HashMap<>();
    public static Map<Integer, SourceRecord> source;  // store the doc_id and the file name.
    public static Integer wordCount=0;
    public HashMap<String, DictEntry> ind; // THe inverted index
    //    public HashMap<String, DictEntry> biIndex; // THe inverted index
    public static HashMap<String, HashMap<String, List<Integer>>> positionalIndex;
    //-------------
    // +-------------------------------

    public Index5() {
        source = new HashMap<Integer, SourceRecord>();
        ind = new HashMap<String, DictEntry>();
//        biIndex = new HashMap<String, DictEntry>();
        positionalIndex = new HashMap<>();
    }

    public void setN(int n) {
        N = n;
    }

    //----------------------------------------------for positionalIndex Functions
    public static void printPosIndex() {
        for (String word : positionalIndex.keySet()) {
            System.out.println("Word: " + word);
            HashMap<String, List<Integer>> docIndex = positionalIndex.get(word);
            for (String doc : docIndex.keySet()) {
                List<Integer> indexes = docIndex.get(doc);
                System.out.print("  Document: " + doc + " - Indexes: ");
                for (Integer index : indexes) {
                    System.out.print(index + ", ");
                }
                System.out.println();
            }
            System.out.println();
        }
    }
    public static void IndexBuild(String[] files) {
        int fileId = 0;
        for (String fileName : files) {
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!source.containsKey(fileName)) {
                    source.put(fileId, new SourceRecord(fileId, fileName, fileName, "notext"));
                }
                String line;
                int position = 0;
                while ((line = file.readLine()) != null) {
                    position += PositionalIndex(line, fileId, position);
                }
                source.get(fileId).length = position;
            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fileId++;
        }
        printPosIndex();
    }
    public static int PositionalIndex(String line, int fileId, int startPosition) {
        wordCount = 0;
        String[] wordsWithStops = line.split("\\W+");
        ArrayList<String> words = new ArrayList<String>();

        for(String item: wordsWithStops){
            if (stopWord(item)) {
                continue;
            }
            words.add(item);
        }
//        wordCount += words.size();
        for (String word : words) {
            word = word.toLowerCase();
            if (stopWord(word)) {
                continue;
            }
            word = stemWord(word);
            // Update the positional index
            positionalIndex.putIfAbsent(word, new HashMap<>());
            HashMap<String, List<Integer>> postings = positionalIndex.get(word);
            postings.putIfAbsent(String.valueOf(fileId), new ArrayList<>());
            List<Integer> positions = postings.get(String.valueOf(fileId));
            positions.add(startPosition + wordCount);
            wordCount++;
        }
        return wordCount;
    }

    // New function for search query
    public static void QuarayFind(String query) {

        // Step 1: Remove stop words
        String[] wordsWithStops = query.split("\\W+");
        ArrayList<String> words = new ArrayList<>();
        for (String word : wordsWithStops) {
            if (!stopWord(word)) {
                words.add(stemWord(word.toLowerCase()));
            }
        }

        // Step 2: Get docs containing the first word
        String firstWord = words.get(0);
        HashMap<String, List<Integer>> firstWordDocs = positionalIndex.get(firstWord);
        if (firstWordDocs == null) {
            System.out.println(" No documents found for the query.");
            return;
        }


        // Step 3: Check intersection of docs for subsequent words
        List<String> resultDocs = new ArrayList<>(firstWordDocs.keySet());
        for (int i = 1; i < words.size(); i++) {
            String word = words.get(i);
            HashMap<String, List<Integer>> wordDocs = positionalIndex.get(word);
            if (wordDocs == null) {
                System.out.println(" No documents found for the query.");
                return;
            }
            resultDocs.retainAll(wordDocs.keySet()); // Intersection of docs

            // Check indexes intersection
            if(!resultDocs.isEmpty()) {
                //we make temp because when we remove in the below if the for loop not effected
                List<String> tempResultDocs = new ArrayList<>(resultDocs);
                for (String doc : tempResultDocs) {
                    List<Integer> positionsFirstWord = firstWordDocs.get(doc);
                    // System.out.println(positionsFirstWord);
                    List<Integer> positionsCurrentWord = wordDocs.get(doc);
                    //System.out.println(wordDocs);
                    if (!PFollowing(positionsFirstWord, positionsCurrentWord)) {
                        // If indexes don't follow each other, remove doc from result
                        resultDocs.remove(doc);
                    }
                }
                firstWordDocs=wordDocs;
            }
        }

        // Step 4: Print result
        if (resultDocs.isEmpty()) {
            System.out.println(" No documents found for the query.");
        } else {
            System.out.println("Nuber of documents found in :");
            for (String doc : resultDocs) {
                System.out.println("Document: " + doc);
            }
        }
    }

    private static boolean PFollowing(List<Integer> positionsFirstWord, List<Integer> positionsCurrentWord) {
        //check to show if there is posIndex of the first word is followed by another
        for (int posFirstWord : positionsFirstWord) {
            for (int posCurrentWord : positionsCurrentWord) {
                if (posCurrentWord - posFirstWord == 1) {
                    return true;
                }
            }
        }
        return false;
    }

    //---------------------------------------------
    public void printPostingList(Posting p) {
        // Iterator<Integer> it2 = hset.iterator();
        System.out.print("[");
        while (p != null) {
            /// -4- **** complete here ****
            // fix get rid of the last comma
            System.out.print(" " + p.docId );
            if(p.next!=null){
                System.out.print(",");
            }
            p = p.next;
        }
        System.out.println("]");
    }

    //---------------------------------------------
    public void printDictionary() {
        Iterator it = ind.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry) it.next();
            DictEntry dd = (DictEntry) pair.getValue();
            System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "]       =--> ");
            printPostingList(dd.pList);
        }
        System.out.println("------------------------------------------------------");
        System.out.println("*** Number of terms = " + ind.size());
    }

    //-----------------------------------------------
    public void buildIndex(String[] files) {  // from disk not from the internet
        int fid = 1;
        for (String fileName : files) {
            pos = 1;
            try (BufferedReader file = new BufferedReader(new FileReader(fileName))) {
                if (!source.containsKey(fileName)) {
                    source.put(fid, new SourceRecord(fid, fileName, fileName, "notext"));
                }
                String ln;
                int flen = 0;

                while ((ln = file.readLine()) != null) {
                    /// -2- **** complete here ****
                    ///**** hint   flen +=  ________________(ln, fid);
                    flen += indexOneLine(ln,fid,pos);
                }
                source.get(fid).length = flen;

            } catch (IOException e) {
                System.out.println("File " + fileName + " not found. Skip it");
            }
            fid++;
        }
        //   printDictionary();
    }
    public void ListAdding(String ln, String word, int fid){
        // add document id to the posting list
        if (!ind.get(word).postingListContains(fid)) {
            ind.get(word).doc_freq += 1; //set doc freq to the number of doc that contain the term
            if (ind.get(word).pList == null) {
                ind.get(word).pList = new Posting(fid);
                ind.get(word).last = ind.get(word).pList;
            } else {
                ind.get(word).last.next = new Posting(fid);
                ind.get(word).last = ind.get(word).last.next;
            }
        } else {
            ind.get(word).last.dtf += 1;
        }
        //set the term_fteq in the collection
        ind.get(word).term_freq += 1;
        if (word.equalsIgnoreCase("lattice")) {

            System.out.println("  <<" + ind.get(word).getPosting(1) + ">> " + ln);
        }
    }

    //----------------------------------------------------------------------------
    public int indexOneLine(String ln, int fid,int pos) {//phrase to index
        int flen = 0;

        String[] words = ln.split("\\W+");
//         String[] words = ln.replaceAll("(?:[^a-zA-Z0-9 -]|(?<=\\w)-(?!\\S))", " ").toLowerCase().split("\\s+");
        flen += words.length;//number of terms in the document
        HashMap<String,List<Integer>> wordWithPositionsMap = new HashMap<String,List<Integer>>();
        List<String> filteredWords = new ArrayList<>();
        for (int i = 0; i < words.length; i++) {
            pos++;
            if (stopWord(words[i])) {
                continue;
            }
            words[i] = words[i].toLowerCase();
            words[i] = stemWord(words[i]);
            filteredWords.add(words[i]);
            if(wordWithPositionsMap.containsKey(words[i])){
                wordWithPositionsMap.get(words[i]).add(pos);
            }else{
                wordWithPositionsMap.put(words[i],new ArrayList<>(Collections.singletonList(pos)));
            }
        }

        for (int i = 0 ;i < filteredWords.size()-1;i++){//hamda omar sallam
            // check to see if the word is not in the dictionary
            // if not add it
            //edit here
            String word1 = filteredWords.get(i);
            String word2  = filteredWords.get(i+1);
            String biWord  = word1+'_'+word2;
            if (!ind.containsKey(word1)) {
                ind.put(word1, new DictEntry());
            }
            if (!ind.containsKey(word2)) {
                ind.put(word2, new DictEntry());
            }
            if (!ind.containsKey(biWord)) {
                ind.put(biWord, new DictEntry());
            }
//            addDocToPostingList(ln,word1,fid,wordWithPositionsMap.get(word1));
//            addDocToPostingList(ln,word2,fid,wordWithPositionsMap.get(word2));
            ListAdding(ln,word1,fid);
            ListAdding(ln,word2,fid);
            ListAdding(ln,biWord,fid);
        }
        return flen;
    }

    //----------------------------------------------------------------------------
    static boolean stopWord(String word) {
        if (word.equals("the") || word.equals("to") || word.equals("be") || word.equals("for") || word.equals("from") || word.equals("in")
                || word.equals("a") || word.equals("into") || word.equals("by") || word.equals("or") || word.equals("and") || word.equals("that")) {
            return true;
        }
        if (word.length() < 2) {
            return true;
        }
        return false;

    }
//----------------------------------------------------------------------------

    static String stemWord(String word) { //skip for now
        return word;
//        Stemmer s = new Stemmer();
//        s.addString(word);
//        s.stem();
//        return s.toString();
    }

    //----------------------------------------------------------------------------
    Posting intersect(Posting pL1, Posting pL2) {
        // Initialize the answer to an empty Posting
        Posting answer = null;
        Posting last = null;

        // While both postings are not empty
        while (pL1 != null && pL2 != null) {
            // If the document IDs match
            if (pL1.docId == pL2.docId) {
                // Add the document ID to the answer
                if (answer == null) {
                    answer = new Posting(pL1.docId);
                    last = answer;
                } else {
                    last.next = new Posting(pL1.docId);
                    last = last.next;
                }
                // Move to the next postings in both lists
                pL1 = pL1.next;
                pL2 = pL2.next;
            } else if (pL1.docId < pL2.docId) { // If docID(pL1) < docID(pL2)
                // Move to the next posting in the first list
                pL1 = pL1.next;
            } else { // If docID(pL1) > docID(pL2)
                // Move to the next posting in the second list
                pL2 = pL2.next;
            }
        }
        // Return the intersection
        return answer;
    }


    public String find_24_01(String phrase) { // any mumber of terms non-optimized search
        String result = "";
        String[] words = phrase.split("\\W+");
        int len = words.length;

        //fix this if word is not in the hash table will crash...
        if(ind.containsKey(words[0].toLowerCase())){
            Posting posting = ind.get(words[0].toLowerCase()).pList;
            int i = 1;
            while (i < len) {
                if(ind.containsKey(words[i].toLowerCase())){
                    posting = intersect(posting, ind.get(words[i].toLowerCase()).pList);
                }
                i++;
            }
            while (posting != null) {
                //System.out.println("\t" + sources.get(num));
                result += "\t" + posting.docId + " - " + source.get(posting.docId).title + " - " + source.get(posting.docId).length + "\n";
                posting = posting.next;
            }
        }

        return result;
    }


    //---------------------------------
    String[] sort(String[] words) {  //bubble sort
        boolean sorted = false;
        String sTmp;
        //-------------------------------------------------------
        while (!sorted) {
            sorted = true;
            for (int i = 0; i < words.length - 1; i++) {
                int compare = words[i].compareTo(words[i + 1]);
                if (compare > 0) {
                    sTmp = words[i];
                    words[i] = words[i + 1];
                    words[i + 1] = sTmp;
                    sorted = false;
                }
            }
        }
        return words;
    }

    //---------------------------------

    public void store(String storageName) {
        try {
            String pathToStorage = "tmp11\\rl\\"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            for (Map.Entry<Integer, SourceRecord> entry : source.entrySet()) {
                System.out.println("Key = " + entry.getKey() + ", Value = " + entry.getValue().URL + ", Value = " + entry.getValue().title + ", Value = " + entry.getValue().text);
                wr.write(entry.getKey().toString() + ",");
                wr.write(entry.getValue().URL.toString() + ",");
                wr.write(entry.getValue().title.replace(',', '~') + ",");
                wr.write(entry.getValue().length + ","); //String formattedDouble = String.format("%.2f", fee );
                wr.write(String.format("%4.4f", entry.getValue().norm) + ",");
                wr.write(entry.getValue().text.toString().replace(',', '~') + "\n");
            }
            wr.write("section2" + "\n");

            Iterator it = ind.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry) it.next();
                DictEntry dd = (DictEntry) pair.getValue();
                //  System.out.print("** [" + pair.getKey() + "," + dd.doc_freq + "] <" + dd.term_freq + "> =--> ");
                wr.write(pair.getKey().toString() + "," + dd.doc_freq + "," + dd.term_freq + ";");
                Posting p = dd.pList;
                while (p != null) {
                    //    System.out.print( p.docId + "," + p.dtf + ":");
                    wr.write(p.docId + "," + p.dtf + ":");
                    p = p.next;
                }
                wr.write("\n");
            }
            wr.write("end" + "\n");
            wr.close();
            System.out.println("=============EBD STORE=============");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void createStore(String storageName) {
        // Initialize storage if needed
    }

    public void addDocument(String text, int docId) {
        String[] words = text.split("\\W+");
        for (String word : words) {
            word = word.toLowerCase();
            if (!ind.containsKey(word)) {
                ind.put(word, new DictEntry());
            }
            DictEntry entry = ind.get(word);
            if (!entry.postingListContains(docId)) {
                entry.doc_freq += 1;
                if (entry.pList == null) {
                    entry.pList = new Posting(docId);
                    entry.last = entry.pList;
                } else {
                    entry.last.next = new Posting(docId);
                    entry.last = entry.last.next;
                }
            } else {
                entry.last.dtf += 1;
            }
            entry.term_freq += 1;
        }
    }


    public Map<Integer, Double> calculateCosineSimilarities(String query) {
        Map<String, Integer> queryTermFrequency = new HashMap<>();
        String[] queryWords = query.split("\\W+");

        for (String word : queryWords) {
            queryTermFrequency.merge(word.toLowerCase(), 1, Integer::sum);
        }

        Map<Integer, Double> cosineSimilarities = new HashMap<>();
        for (int docId : source.keySet()) {
            double dotProduct = 0.0;
            double queryNorm = 0.0;
            double docNorm = 0.0;

            for (String word : queryTermFrequency.keySet()) {
                int queryFreq = queryTermFrequency.get(word);
                DictEntry dictEntry = ind.get(word);
                int docFreq = 0;
                if (dictEntry != null) {
                    Posting p = dictEntry.pList;
                    while (p != null) {
                        if (p.docId == docId) {
                            docFreq = p.dtf;
                            break;
                        }
                        p = p.next;
                    }
                }

                dotProduct += queryFreq * docFreq;
                queryNorm += Math.pow(queryFreq, 2);
                docNorm += Math.pow(docFreq, 2);
            }

            queryNorm = Math.sqrt(queryNorm);
            docNorm = Math.sqrt(docNorm);

            if (queryNorm != 0 && docNorm != 0) {
                double cosineSimilarity = dotProduct / (queryNorm * docNorm);
                cosineSimilarities.put(docId, cosineSimilarity);
            }
        }

        return cosineSimilarities;
    }

//=========================================
    /*public boolean storageFileExists(String storageName){
        java.io.File f = new java.io.File("/home/ehab/tmp11/rl/"+storageName);
        if (f.exists() && !f.isDirectory())
            return true;
        return false;

    }*/
//----------------------------------------------------
   /* public void createStore(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/"+storageName;
            Writer wr = new FileWriter(pathToStorage);
            wr.write("end" + "\n");
            wr.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }*/
//----------------------------------------------------
    //load index from hard disk into memory
   /* public HashMap<String, DictEntry> load(String storageName) {
        try {
            String pathToStorage = "/home/ehab/tmp11/rl/"+storageName;
            sources = new HashMap<Integer, SourceRecord>();
            index = new HashMap<String, DictEntry>();
            BufferedReader file = new BufferedReader(new FileReader(pathToStorage));
            String ln = "";
            int flen = 0;
            while ((ln = file.readLine()) != null) {
                if (ln.equalsIgnoreCase("section2")) {
                    break;
                }
                String[] ss = ln.split(",");
                int fid = Integer.parseInt(ss[0]);
                try {
                    System.out.println("**>>" + fid + " " + ss[1] + " " + ss[2].replace('~', ',') + " " + ss[3] + " [" + ss[4] + "]   " + ss[5].replace('~', ','));

                    SourceRecord sr = new SourceRecord(fid, ss[1], ss[2].replace('~', ','), Integer.parseInt(ss[3]), Double.parseDouble(ss[4]), ss[5].replace('~', ','));
                    //   System.out.println("**>>"+fid+" "+ ss[1]+" "+ ss[2]+" "+ ss[3]+" ["+ Double.parseDouble(ss[4])+ "]  \n"+ ss[5]);
                    sources.put(fid, sr);
                } catch (Exception e) {

                    System.out.println(fid + "  ERROR  " + e.getMessage());
                    e.printStackTrace();
                }
            }
            while ((ln = file.readLine()) != null) {
                //     System.out.println(ln);
                if (ln.equalsIgnoreCase("end")) {
                    break;
                }
                String[] ss1 = ln.split(";");
                String[] ss1a = ss1[0].split(",");
                String[] ss1b = ss1[1].split(":");
                index.put(ss1a[0], new DictEntry(Integer.parseInt(ss1a[1]), Integer.parseInt(ss1a[2])));
                String[] ss1bx;   //posting
                for (int i = 0; i < ss1b.length; i++) {
                    ss1bx = ss1b[i].split(",");
                    if (index.get(ss1a[0]).pList == null) {
                        index.get(ss1a[0]).pList = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).pList;
                    } else {
                        index.get(ss1a[0]).last.next = new Posting(Integer.parseInt(ss1bx[0]), Integer.parseInt(ss1bx[1]));
                        index.get(ss1a[0]).last = index.get(ss1a[0]).last.next;
                    }
                }
            }
            System.out.println("============= END LOAD =============");
            //    printDictionary();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return index;
    }*/
}

//=====================================================================
