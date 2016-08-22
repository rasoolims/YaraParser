/**
 * Copyright 2014, Yahoo! Inc.
 * Licensed under the terms of the Apache License 2.0. See LICENSE file at the project root for terms.
 */

package YaraParser.Structures;


import java.util.ArrayList;

public class Sentence implements Comparable {
    /**
     * shows the tokens of a specific sentence
     */
    private int[] words;
    private String[] lemmas;
    private int[] tags;

    private int[] brownCluster4thPrefix;
    private int[] brownCluster6thPrefix;
    private int[] brownClusterFullString;

    private int[] brownCluster4thPrefixInDomain;
    private int[] brownCluster6thPrefixInDomain;
    private int[] brownClusterFullStringInDomain;


    public Sentence(ArrayList<Integer> tokens, ArrayList<String> lemmas, ArrayList<Integer> pos, ArrayList<Integer> brownCluster4thPrefix,
                    ArrayList<Integer> brownCluster6thPrefix, ArrayList<Integer> brownClusterFullString, ArrayList<Integer>
                            brownCluster4thPrefixInDomain,
                    ArrayList<Integer> brownCluster6thPrefixInDomain, ArrayList<Integer> brownClusterFullStringInDomain) {
        words = new int[tokens.size()];
        this.lemmas = new String[tokens.size()];
        tags = new int[tokens.size()];
        this.brownCluster4thPrefix = new int[tokens.size()];
        this.brownCluster6thPrefix = new int[tokens.size()];
        this.brownClusterFullString = new int[tokens.size()];
        this.brownCluster4thPrefixInDomain = new int[tokens.size()];
        this.brownCluster6thPrefixInDomain = new int[tokens.size()];
        this.brownClusterFullStringInDomain = new int[tokens.size()];
        for (int i = 0; i < tokens.size(); i++) {
            words[i] = tokens.get(i);
            this.lemmas[i] = lemmas.get(i);
            tags[i] = pos.get(i);
            this.brownCluster4thPrefix[i] = brownCluster4thPrefix.get(i);
            this.brownCluster6thPrefix[i] = brownCluster6thPrefix.get(i);
            this.brownClusterFullString[i] = brownClusterFullString.get(i);
            this.brownCluster4thPrefixInDomain[i] = brownCluster4thPrefixInDomain.get(i);
            this.brownCluster6thPrefixInDomain[i] = brownCluster6thPrefixInDomain.get(i);
            this.brownClusterFullStringInDomain[i] = brownClusterFullStringInDomain.get(i);
        }
    }

    public int size() {
        return words.length;
    }

    public int posAt(int position) {
        if (position == 0)
            return 0;

        return tags[position - 1];
    }

    public int[] getWords() {
        return words;
    }

    public String[] getLemmas() {
        return lemmas;
    }

    public int[] getTags() {
        return tags;
    }


    public int[] getBrownCluster4thPrefix(boolean inDomain) {
        if (!inDomain)
            return brownCluster4thPrefix;
        else
            return brownCluster4thPrefixInDomain;
    }


    public int[] getBrownCluster6thPrefix(boolean inDomain) {
        if (!inDomain)
        return brownCluster6thPrefix;
        else
            return brownCluster6thPrefixInDomain;
    }

    public int[] getBrownClusterFullString(boolean inDomain) {
        if (!inDomain)
        return brownClusterFullString;
        else
            return brownClusterFullStringInDomain;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Sentence) {
            Sentence sentence = (Sentence) obj;
            if (sentence.words.length != words.length)
                return false;
            for (int i = 0; i < sentence.words.length; i++) {
                if (sentence.words[i] != words[i])
                    return false;
                if (sentence.tags[i] != tags[i])
                    return false;
                if (sentence.lemmas[i] != lemmas[i])
                    return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int compareTo(Object o) {
        if (equals(o))
            return 0;
        return hashCode() - o.hashCode();
    }

    @Override
    public int hashCode() {
        int hash = 0;
        for (int tokenId = 0; tokenId < words.length; tokenId++) {
            hash ^= (words[tokenId] * tags[tokenId]);
        }
        return hash;
    }

}
