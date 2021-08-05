package app;

import pgl.infra.utils.IOUtils;
import utils.FastqFeature;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class QC {
    public QC(String parameterPath) {
        long startTimePoint = System.nanoTime();
        long endTimePoint = System.nanoTime();
        System.out.println("Times:"+(endTimePoint-startTimePoint));
    }


}
