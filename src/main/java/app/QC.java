package app;

import pgl.infra.utils.IOUtils;
import utils.Command;
import utils.FastqFeature;
import utils.MathUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Quality Control
 * @author yxh
 */


public class QC {

    String inputDir = null;
    String outputDir = null;
    String method = null;
    String readsNumber = null;

    public QC(String[] args) {
        long startTimePoint = System.nanoTime();
//        this.subSample(args);
        this.getQuality(args);
        long endTimePoint = System.nanoTime();
        System.out.println("Times:" + (endTimePoint - startTimePoint));
    }

    public void subSample(String[] args) {
        this.inputDir = new File(args[0]).getAbsolutePath();
        this.outputDir = new File(args[1]).getAbsolutePath();
        this.method = args[2];
        this.readsNumber = args[3];
        File[] fs = new File(inputDir).listFiles();
        fs = IOUtils.listFilesEndsWith(fs, ".fq.gz");
        try {
            ExecutorService pool = Executors.newFixedThreadPool(12);
            File dir = new File(new File(inputDir).getAbsolutePath());
            for (int i = 0; i < fs.length; i++) {
                StringBuilder sb = new StringBuilder();
                sb.append("seqtk sample -s100 " + new File(inputDir, fs[i].getName()).getAbsolutePath() + " " + readsNumber + " | gzip > " + new File(outputDir, fs[i].getName()).getAbsolutePath());
                String command = sb.toString();
                Command com = new Command(command, dir);
                Future<Command> chrom = pool.submit(com);
            }
            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void getQuality(String[] args) {
        this.inputDir = new File(args[1]).getAbsolutePath();
        this.outputDir = new File(args[1]).getAbsolutePath();
        this.method = args[2];
        this.readsNumber = args[3];
        File[] fs = new File(inputDir).listFiles();
        fs = IOUtils.listFilesEndsWith(fs, "R1.fq.gz");
        HashSet<String> nameSet = new HashSet<>();
        for (int i = 0; i < fs.length; i++) {
            nameSet.add(fs[i].getName().replace("_R1.fq.gz", ""));
        }
        String[] names = nameSet.toArray(new String[0]);
        Arrays.sort(names);
        System.out.println("Total " + names.length + " samples...");
        HashMap<String, Integer> nameMap = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            nameMap.put(names[i], i);
        }
        double[][] Q_R1 = new double[nameSet.size()][150];
        double[][] Q_R2 = new double[nameSet.size()][150];
        nameSet.stream().forEach(f -> {
            System.out.println(f);
            BufferedReader br1 = IOUtils.getTextGzipReader(new File(inputDir, f + "_R1.fq.gz").getAbsolutePath());
            BufferedReader br2 = IOUtils.getTextGzipReader(new File(inputDir, f + "_R2.fq.gz").getAbsolutePath());
            String read1 = null;
            String seq1 = null;
            String des1 = null;
            String quality1 = null;
            String read2 = null;
            String seq2 = null;
            String des2 = null;
            String quality2 = null;
//            int countline = 0;
            int[] countline = new int[150];
            double[] Q1 = new double[150];
            double[] Q2 = new double[150];
            try {
                for (int i = 0; i < 150; i++) {
                    Q1[i] = 0;
                    Q2[i] = 0;
                    countline[i] = 0;
                }
                while ((read1 = br1.readLine()) != null) {
//                    countline++;
                    if (countline[0] >= Integer.parseInt(readsNumber)) break;
                    if (countline[0] % 5000 == 0) {
//                        System.out.println(countline[0]);
                    }
                    seq1 = br1.readLine();
                    des1 = br1.readLine();
                    quality1 = br1.readLine();
                    read2 = br2.readLine();
                    seq2 = br2.readLine();
                    des2 = br2.readLine();
                    quality2 = br2.readLine();
                    if (quality1.length() != quality2.length())continue;
                    for (int i = 0; i < quality1.length(); i++) {
                        System.out.println(quality1.length());
                        countline[i]++;
                        Q1[i] += (double) FastqFeature.getscore(quality1.substring(i, i + 1));
                        Q2[i] += (double) FastqFeature.getscore(quality2.substring(i, i + 1));
                    }
                }
                br1.close();
                br2.close();
                for (int i = 0; i < 150; i++) {
                    Q_R1[nameMap.get(f)][i] = (double) Q1[i] / countline[i];
                    Q_R2[nameMap.get(f)][i] = (double) Q2[i] / countline[i];
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        BufferedWriter bw1 = IOUtils.getTextWriter(new File(outputDir, "Quality_" + method + "_R1.txt").getAbsolutePath());
        BufferedWriter bw2 = IOUtils.getTextWriter(new File(outputDir, "Quality_" + method + "_R2.txt").getAbsolutePath());
        try {
            DecimalFormat defor = new DecimalFormat("0.000");
            double value1 = 0;
            double value2 = 0;
            for (int i = 0; i < names.length; i++) {
                if (method.equals("mean")) {
                    value1 = MathUtils.getMean(Q_R1[i]);
                    value2 = MathUtils.getMean(Q_R2[i]);
                } else if (method.equals("median")) {
                    value1 = MathUtils.getMedian(Q_R1[i]);
                    value2 = MathUtils.getMedian(Q_R2[i]);
                }
                bw1.write(names[i] + "\t" + defor.format(value1) + "\n");
                bw2.write(names[i] + "\t" + defor.format(value2) + "\n");
            }
            bw1.flush();
            bw1.close();
            bw2.flush();
            bw2.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
