/**
 * Copyright 2014, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package YaraParser.Accessories;

import YaraParser.Structures.IndexMaps;
import YaraParser.Structures.Sentence;
import YaraParser.TransitionBasedSystem.Configuration.CompactTree;
import YaraParser.TransitionBasedSystem.Configuration.GoldConfiguration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Random;

public class CoNLLReader {
    /**
     * An object for reading the CoNLL file
     */
    BufferedReader fileReader;

    /**
     * Initializes the file reader
     *
     * @param filePath Path to the file
     * @throws Exception If the file path is not correct or there are not enough permission to read the file
     */
    public CoNLLReader(String filePath) throws Exception {
        fileReader = new BufferedReader(new FileReader(filePath));
    }

    public static IndexMaps createIndices(String filePath, boolean labeled, boolean lowercased, String clusterFile, String clusterInDomainFile)
            throws Exception {
        HashMap<String, Integer> wordMap = new HashMap<String, Integer>();
        HashMap<Integer, Integer> labels = new HashMap<Integer, Integer>();
        HashMap<String, Integer> clusterMap = new HashMap<String, Integer>();
        HashMap<Integer, Integer> cluster4Map = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> cluster6Map = new HashMap<Integer, Integer>();
        HashMap<String, Integer> clusterMapInDomain = new HashMap<String, Integer>();
        HashMap<Integer, Integer> cluster4MapInDomain = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> cluster6MapInDomain = new HashMap<Integer, Integer>();

        int labelCount = 1;
        String rootString = "ROOT";

        int wi = 1;
        wordMap.put("ROOT", 0);
        labels.put(0, 0);

        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String line;
        while ((line = reader.readLine()) != null) {
            String[] spl = line.trim().split("\t");
            if (spl.length > 7) {
                String label = spl[7];
                int head = Integer.parseInt(spl[6]);
                if (head == 0)
                    rootString = label;

                if (label.equals("_"))
                    label = "-";
                if (!labeled)
                    label = "~";
                if (!wordMap.containsKey(label)) {
                    labels.put(wi, labelCount++);
                    wordMap.put(label, wi++);
                }
            }
        }

        reader = new BufferedReader(new FileReader(filePath));
        while ((line = reader.readLine()) != null) {
            String[] spl = line.trim().split("\t");
            if (spl.length > 7) {
                String pos = spl[3];
                if (!wordMap.containsKey(pos)) {
                    wordMap.put(pos, wi++);
                }
            }
        }

        if (clusterFile.length() > 0) {
            reader = new BufferedReader(new FileReader(clusterFile));
            while ((line = reader.readLine()) != null) {
                String[] spl = line.trim().split("\t");
                if (spl.length >= 2) {
                    String cluster = spl[0];
                    String word = spl[1];

                    String prefix4 = cluster.substring(0, Math.min(4, cluster.length()));
                    String prefix6 = cluster.substring(0, Math.min(6, cluster.length()));
                    int clusterNum = wi;

                    if (!wordMap.containsKey(cluster)) {
                        clusterMap.put(word, wi);
                        wordMap.put(cluster, wi++);
                    } else {
                        clusterNum = wordMap.get(cluster);
                        clusterMap.put(word, clusterNum);
                    }

                    int pref4Id = wi;
                    if (!wordMap.containsKey(prefix4)) {
                        wordMap.put(prefix4, wi++);
                    } else {
                        pref4Id = wordMap.get(prefix4);
                    }

                    int pref6Id = wi;
                    if (!wordMap.containsKey(prefix6)) {
                        wordMap.put(prefix6, wi++);
                    } else {
                        pref6Id = wordMap.get(prefix6);
                    }

                    cluster4Map.put(clusterNum, pref4Id);
                    cluster6Map.put(clusterNum, pref6Id);
                }
            }
        }

        if (clusterInDomainFile.length() > 0) {
            reader = new BufferedReader(new FileReader(clusterInDomainFile));
            while ((line = reader.readLine()) != null) {
                String[] spl = line.trim().split("\t");
                if (spl.length > 2) {
                    String cluster = spl[0];
                    String word = spl[1];

                    String prefix4 = cluster.substring(0, Math.min(4, cluster.length()));
                    String prefix6 = cluster.substring(0, Math.min(6, cluster.length()));
                    int clusterNum = wi;

                    if (!wordMap.containsKey(cluster)) {
                        clusterMapInDomain.put(word, wi);
                        wordMap.put(cluster, wi++);
                    } else {
                        clusterNum = wordMap.get(cluster);
                        clusterMapInDomain.put(word, clusterNum);
                    }

                    int pref4Id = wi;
                    if (!wordMap.containsKey(prefix4)) {
                        wordMap.put(prefix4, wi++);
                    } else {
                        pref4Id = wordMap.get(prefix4);
                    }

                    int pref6Id = wi;
                    if (!wordMap.containsKey(prefix6)) {
                        wordMap.put(prefix6, wi++);
                    } else {
                        pref6Id = wordMap.get(prefix6);
                    }

                    cluster4MapInDomain.put(clusterNum, pref4Id);
                    cluster6MapInDomain.put(clusterNum, pref6Id);
                }
            }
        }

        reader = new BufferedReader(new FileReader(filePath));
        while ((line = reader.readLine()) != null) {
            String[] spl = line.trim().split("\t");
            if (spl.length > 7) {
                String word = spl[1];
                if (lowercased)
                    word = word.toLowerCase();
                if (!wordMap.containsKey(word) && !word.equals("_")) {
                    wordMap.put(word, wi++);
                }
            }
        }

        System.out.println("OOD cluster words#: " + clusterMap.size());
        System.out.println("ID cluster words#: " + clusterMapInDomain.size());
        return new IndexMaps(wordMap, labels, rootString, cluster4Map, cluster6Map, clusterMap, cluster4MapInDomain, cluster6MapInDomain,
                clusterMapInDomain);
    }

    /**
     * @param limit it is used if we want to read part of the data
     * @return
     */
    public ArrayList<GoldConfiguration> readData(int limit, boolean keepNonProjective, boolean labeled, boolean rootFirst, boolean lowerCased,
                                                 IndexMaps maps, boolean shuffle) throws Exception {
        HashMap<String, Integer> wordMap = maps.getWordMap();
        ArrayList<GoldConfiguration> configurationSet = new ArrayList<GoldConfiguration>();

        String line;
        ArrayList<Integer> tokens = new ArrayList<Integer>();
        ArrayList<String> lemmas = new ArrayList<String>();
        ArrayList<Integer> tags = new ArrayList<Integer>();
        ArrayList<Integer> cluster4Ids = new ArrayList<Integer>();
        ArrayList<Integer> cluster6Ids = new ArrayList<Integer>();
        ArrayList<Integer> clusterIds = new ArrayList<Integer>();
        ArrayList<Integer> cluster4IdsInDomain = new ArrayList<Integer>();
        ArrayList<Integer> cluster6IdsInDomain = new ArrayList<Integer>();
        ArrayList<Integer> clusterIdsInDomain = new ArrayList<Integer>();
        String langId = "";
        int updateWeight = 1;
        HashMap<Integer, Pair<Integer, Integer>> goldDependencies = new HashMap<Integer, Pair<Integer, Integer>>();
        int sentenceCounter = 0;
        while ((line = fileReader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                if (tokens.size() >= 1) {
                    sentenceCounter++;
                    if (!rootFirst) {
                        for (int gold : goldDependencies.keySet()) {
                            if (goldDependencies.get(gold).first.equals(0))
                                goldDependencies.get(gold).setFirst(tokens.size() + 1);
                        }
                        tokens.add(0);
                        tags.add(0);
                        lemmas.add("ROOT");
                        cluster4Ids.add(0);
                        cluster6Ids.add(0);
                        clusterIds.add(0);
                        cluster4IdsInDomain.add(0);
                        cluster6IdsInDomain.add(0);
                        clusterIdsInDomain.add(0);
                    }
                    Sentence currentSentence = new Sentence(tokens, lemmas, tags, cluster4Ids, cluster6Ids, clusterIds, cluster4IdsInDomain,
                            cluster6IdsInDomain, clusterIdsInDomain);
                    GoldConfiguration goldConfiguration = new GoldConfiguration(currentSentence, goldDependencies, updateWeight, langId);
                    if (keepNonProjective || !goldConfiguration.isNonprojective())
                        configurationSet.add(goldConfiguration);
                    goldDependencies = new HashMap<Integer, Pair<Integer, Integer>>();
                    tokens = new ArrayList<Integer>();
                    tags = new ArrayList<Integer>();
                    lemmas = new ArrayList<String>();
                    cluster4Ids = new ArrayList<Integer>();
                    cluster6Ids = new ArrayList<Integer>();
                    clusterIds = new ArrayList<Integer>();
                    cluster4IdsInDomain = new ArrayList<Integer>();
                    cluster6IdsInDomain = new ArrayList<Integer>();
                    clusterIdsInDomain = new ArrayList<Integer>();
                } else {
                    goldDependencies = new HashMap<Integer, Pair<Integer, Integer>>();
                    tokens = new ArrayList<Integer>();
                    tags = new ArrayList<Integer>();
                    cluster4Ids = new ArrayList<Integer>();
                    cluster6Ids = new ArrayList<Integer>();
                    clusterIds = new ArrayList<Integer>();
                    cluster4IdsInDomain = new ArrayList<Integer>();
                    cluster6IdsInDomain = new ArrayList<Integer>();
                    clusterIdsInDomain = new ArrayList<Integer>();
                }
                if (sentenceCounter >= limit) {
                    System.out.println("buffer full..." + configurationSet.size());
                    break;
                }
            } else {
                String[] splitLine = line.split("\t");
                if (splitLine.length < 8)
                    throw new Exception("wrong file format");
                if (splitLine.length >= 8) {
                    try {
                        updateWeight = Integer.parseInt(splitLine[8]);
                    } catch (Exception ex) {
                        updateWeight = 1;
                    }
                } else {
                    updateWeight = 1;
                }
                int wordIndex = Integer.parseInt(splitLine[0]);
                String word = splitLine[1].trim();
                String lemma = splitLine[2].trim();
                if (lowerCased)
                    word = word.toLowerCase();
                String pos = splitLine[3].trim();
                langId = splitLine[5].trim();

                int wi = -1;
                if (wordMap.containsKey(word) && !word.equals("_"))
                    wi = wordMap.get(word);

                int pi = -1;
                if (wordMap.containsKey(pos))
                    pi = wordMap.get(pos);

                tags.add(pi);
                tokens.add(wi);
                lemmas.add(word);

                int headIndex = Integer.parseInt(splitLine[6]);
                String relation = splitLine[7];
                if (relation.equals("_"))
                    relation = "-";
                if (!labeled)
                    relation = "~";

                if (headIndex == 0)
                    relation = "ROOT";

                int ri = -1;
                if (wordMap.containsKey(relation))
                    ri = wordMap.get(relation);
                if (headIndex == -1)
                    ri = -1;

                int[] ids = maps.clusterId(lemma, false);
                clusterIds.add(ids[0]);
                cluster4Ids.add(ids[1]);
                cluster6Ids.add(ids[2]);

                int[] idsInDomain = maps.clusterId(word, true);
                clusterIdsInDomain.add(idsInDomain[0]);
                cluster4IdsInDomain.add(idsInDomain[1]);
                cluster6IdsInDomain.add(idsInDomain[2]);

                if (headIndex >= 0)
                    goldDependencies.put(wordIndex, new Pair<Integer, Integer>(headIndex, ri));
            }
        }
        if (tokens.size() > 0) {
            if (!rootFirst) {
                for (int gold : goldDependencies.keySet()) {
                    if (goldDependencies.get(gold).first.equals(0))
                        goldDependencies.get(gold).setFirst(goldDependencies.size() + 1);
                }
                tokens.add(0);
                tags.add(0);
                lemmas.add("ROOT");
                cluster4Ids.add(0);
                cluster6Ids.add(0);
                clusterIds.add(0);
                cluster4IdsInDomain.add(0);
                cluster6IdsInDomain.add(0);
                clusterIdsInDomain.add(0);
            }
            sentenceCounter++;
            Sentence currentSentence = new Sentence(tokens, lemmas, tags, cluster4Ids, cluster6Ids, clusterIds, cluster4IdsInDomain,
                    cluster6IdsInDomain, clusterIdsInDomain);
            configurationSet.add(new GoldConfiguration(currentSentence, goldDependencies, updateWeight, langId));
        }
        if (shuffle)
            Collections.shuffle(configurationSet, new Random());
        return configurationSet;
    }

    public ArrayList<CompactTree> readStringData() throws Exception {
        ArrayList<CompactTree> treeSet = new ArrayList<CompactTree>();

        String line;
        ArrayList<String> tags = new ArrayList<String>();

        HashMap<Integer, Pair<Integer, String>> goldDependencies = new HashMap<Integer, Pair<Integer, String>>();
        while ((line = fileReader.readLine()) != null) {
            line = line.trim();
            if (line.length() == 0) {
                if (tags.size() >= 1) {
                    CompactTree goldConfiguration = new CompactTree(goldDependencies, tags);
                    treeSet.add(goldConfiguration);
                }
                tags = new ArrayList<String>();
                goldDependencies = new HashMap<Integer, Pair<Integer, String>>();
            } else {
                String[] splitLine = line.split("\t");
                if (splitLine.length < 8)
                    throw new Exception("wrong file format");
                int wordIndex = Integer.parseInt(splitLine[0]);
                String pos = splitLine[3].trim();

                tags.add(pos);

                int headIndex = Integer.parseInt(splitLine[6]);
                String relation = splitLine[7];

                if (headIndex == 0) {
                    relation = "ROOT";
                }

                if (pos.length() > 0)
                    goldDependencies.put(wordIndex, new Pair<Integer, String>(headIndex, relation));
            }
        }


        if (tags.size() > 0) {
            treeSet.add(new CompactTree(goldDependencies, tags));
        }

        return treeSet;
    }

}
