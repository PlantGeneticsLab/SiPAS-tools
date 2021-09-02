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

    private static String[] phreds = {"!", "\"", "#", "$", "%", "&", " ", "(", ")", "*", "+", ",", "-", ".", "/", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", ":", ";", "<", "=", ">", "?", "@", "A", "B", "C", "D", "E", "F", "G", "H", "I","J","K"};
    private static int[] score = new int[43];
    private static HashMap<Integer, String> scoreQMap = null;
    private static HashMap<String, Integer> phredQMap = null;
    private static boolean build = buildMaps();

    private static boolean buildMaps() {
        scoreQMap = new HashMap<>();
        phredQMap = new HashMap<>();
        for (int i = 0; i < score.length; i++) {
            score[i] = i;
        }
        for (int i = 0; i < score.length; i++) {
            scoreQMap.put(score[i], phreds[i]);
        }
        for (int i = 0; i < score.length; i++) {
            phredQMap.put(phreds[i], score[i]);
        }
        return true;
    }

    public static String getphred(int score) {
        return scoreQMap.get(score);
    }

    public static int getscore(String phred) {
        return phredQMap.get(phred);
    }
}
