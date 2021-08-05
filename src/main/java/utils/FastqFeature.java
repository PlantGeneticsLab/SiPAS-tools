package utils;

import pgl.infra.anno.gene.GeneFeature;
import pgl.infra.range.Range;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Common utils with Fastq files
 *
 * @ author: yxh
 * @ created: 2021-08-05 : 10:11 AM
 */
public class FastqFeature {

    String[] phreds = {"!", "\"", "#", "$", "%", "&", " ", "(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@", "A", "B", "C", "D", "E", "F", "G", "H", "I"};
    int[] score = new int[41];
    static HashMap<Integer, String> scoreQMap = new HashMap<>();
    static HashMap<String, Integer> phredQMap = new HashMap<>();

    public FastqFeature() {
        this.getHashMap();
    }

    public void getHashMap() {
        for (int i = 0; i < 41; i++) {
            scoreQMap.put(score[i], phreds[i]);
        }
        for (int i = 0; i < 41; i++) {
            phredQMap.put(phreds[i], score[i]);
        }
    }

    public static String getphred(int score) {
        return scoreQMap.get(score);
    }

    public static int getscore(String phred) {
        return phredQMap.get(phred);
    }

}
