/**
 * Copyright 2014, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package YaraParser.Structures;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

public class IndexMaps implements Serializable {
    public final String rootString;
    public String[] revWords;
    private HashMap<String, Integer> wordMap;
    private HashMap<Integer, Integer> labels;
    private HashMap<Integer, Integer> brown4Clusters;
    private HashMap<Integer, Integer> brown6Clusters;
    private HashMap<String, Integer> brownFullClusters;

    private HashMap<Integer, Integer> brown4ClustersInDomain;
    private HashMap<Integer, Integer> brown6ClustersInDomain;
    private HashMap<String, Integer> brownFullClustersInDomain;

    private HashMap<String, ArrayList<String>> languageIdInformation;

    public IndexMaps(HashMap<String, Integer> wordMap, HashMap<Integer, Integer> labels, String rootString,
                     HashMap<Integer, Integer> brown4Clusters, HashMap<Integer, Integer> brown6Clusters, HashMap<String, Integer> brownFullClusters,
                     HashMap<Integer, Integer> brown4ClustersInDomain, HashMap<Integer, Integer> brown6ClustersInDomain, HashMap<String, Integer>
                             brownFullClustersInDomain) {
        this.wordMap = wordMap;
        this.labels = labels;

        revWords = new String[wordMap.size() + 1];
        revWords[0] = "ROOT";

        for (String word : wordMap.keySet()) {
            revWords[wordMap.get(word)] = word;
        }

        this.brown4Clusters = brown4Clusters;
        this.brown6Clusters = brown6Clusters;
        this.brownFullClusters = brownFullClusters;
        this.brown4ClustersInDomain = brown4ClustersInDomain;
        this.brown6ClustersInDomain = brown6ClustersInDomain;
        this.brownFullClustersInDomain = brownFullClustersInDomain;
        this.rootString = rootString;
        languageIdInformation = new HashMap<String, ArrayList<String>>();
    }

    public void readLanguageIdInformation(String path) throws Exception {
        BufferedReader reader = new BufferedReader(new FileReader(path));

        String line;
        languageIdInformation = new HashMap<String, ArrayList<String>>();
        while ((line = reader.readLine()) != null) {
            String[] spl = line.split("\t");
            if (spl.length < 3)
                continue;
            ArrayList<String> info = new ArrayList<String>();
            for (int i = 2; i < spl.length; i++)
                info.add(spl[i].trim());
            languageIdInformation.put(spl[1].trim(), info);
        }
    }

    public Sentence makeSentence(String[] words, String[] lms, String[] posTags, boolean rootFirst, boolean lowerCased) {
        ArrayList<Integer> tokens = new ArrayList<Integer>();
        ArrayList<String> lemmas = new ArrayList<String>();
        ArrayList<Integer> tags = new ArrayList<Integer>();
        ArrayList<Integer> bc4 = new ArrayList<Integer>();
        ArrayList<Integer> bc6 = new ArrayList<Integer>();
        ArrayList<Integer> bcf = new ArrayList<Integer>();
        ArrayList<Integer> bc4id = new ArrayList<Integer>();
        ArrayList<Integer> bc6id = new ArrayList<Integer>();
        ArrayList<Integer> bcfid = new ArrayList<Integer>();

        int i = 0;
        for (String word : words) {
            if (word.length() == 0)
                continue;
            String lowerCaseWord = word.toLowerCase();
            if (lowerCased)
                word = lowerCaseWord;

            int[] clusterIDs = clusterId(lms[i], false);
            int[] clusterIDsInDomain = clusterId(word, true);
            bcf.add(clusterIDs[0]);
            bc4.add(clusterIDs[1]);
            bc6.add(clusterIDs[2]);
            bcfid.add(clusterIDsInDomain[0]);
            bc4id.add(clusterIDsInDomain[1]);
            bc6id.add(clusterIDsInDomain[2]);

            String pos = posTags[i];

            int wi = -1;
            if (wordMap.containsKey(word) && !word.equals("_"))
                wi = wordMap.get(word);

            int li = -1;
            if (wordMap.containsKey(lms[i]) && !lms[i].equals("_"))
                li = wordMap.get(lms[i]);

            int pi = -1;
            if (wordMap.containsKey(pos))
                pi = wordMap.get(pos);

            tokens.add(wi);
            tags.add(pi);
            lemmas.add(word);

            i++;
        }

        if (!rootFirst) {
            tokens.add(0);
            tags.add(0);
            bcf.add(0);
            bc6.add(0);
            bc4.add(0);
            bcfid.add(0);
            bc6id.add(0);
            bc4id.add(0);
        }

        return new Sentence(tokens, lemmas, tags, bc4, bc6, bcf, bc4id, bc6id, bcfid);
    }

    public HashMap<String, Integer> getWordMap() {
        return wordMap;
    }


    public HashMap<Integer, Integer> getLabels() {
        return labels;
    }

    public int[] clusterId(String word, boolean inDomain) {
        int[] ids = new int[3];
        ids[0] = -100;
        ids[1] = -100;
        ids[2] = -100;
        if (!inDomain) {
            if (brownFullClusters.containsKey(word))
                ids[0] = brownFullClusters.get(word);

            if (ids[0] > 0) {
                ids[1] = brown4Clusters.get(ids[0]);
                ids[2] = brown6Clusters.get(ids[0]);
            }
        } else {
            if (brownFullClustersInDomain.containsKey(word))
                ids[0] = brownFullClustersInDomain.get(word);

            if (ids[0] > 0) {
                ids[1] = brown4ClustersInDomain.get(ids[0]);
                ids[2] = brown6ClustersInDomain.get(ids[0]);
            }
        }
        return ids;
    }

    public boolean hasClusters(boolean inDomain) {
        if (!inDomain && brownFullClusters != null && brownFullClusters.size() > 0)
            return true;
        if (inDomain && brownFullClustersInDomain != null && brownFullClustersInDomain.size() > 0)
            return true;
        return false;
    }

    public int langInfoSize() {
        return languageIdInformation.size();
    }

    public ArrayList<String> getLangInfo(String id) {
        return languageIdInformation.get(id);

    }
}
