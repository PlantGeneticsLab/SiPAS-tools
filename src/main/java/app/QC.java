package app;

import pgl.infra.table.RowTable;
import pgl.infra.utils.IOUtils;
import utils.FastqFeature;
import utils.MathUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

public class QC {

    String inputdir = null;
    String outputdir = null;
    String method = null;
    String readsNumber = null;

    public QC(String[] args) {
        long startTimePoint = System.nanoTime();
        this.getQuality(args);
        long endTimePoint = System.nanoTime();
        System.out.println("Times:" + (endTimePoint - startTimePoint));
    }

    public void getQuality(String[] args) {
        this.inputdir = args[0];
        this.outputdir = args[1];
        this.method = args[2];
        this.readsNumber = args[3];
        File[] fs = new File(inputdir).listFiles();
        fs = IOUtils.listFilesEndsWith(fs, "R1.fq.gz");
        HashSet<String> nameSet = new HashSet<>();
        for (int i = 0; i < fs.length; i++) {
            nameSet.add(fs[i].getName().replace("_R1.fq.gz", ""));
        }
        String[] names = nameSet.toArray(new String[0]);
        Arrays.sort(names);
        HashMap<String, Integer> nameMap = new HashMap<>();
        for (int i = 0; i < names.length; i++) {
            nameMap.put(names[i], i);
        }
        double[][] Q_R1 = new double[150][nameSet.size()];
        double[][] Q_R2 = new double[150][nameSet.size()];
        nameSet.parallelStream().forEach(f -> {
            System.out.println(f);
            BufferedReader br1 = IOUtils.getTextGzipReader(new File(inputdir, f + "_R1.fq.gz").getAbsolutePath());
            BufferedReader br2 = IOUtils.getTextGzipReader(new File(inputdir, f + "_R2.fq.gz").getAbsolutePath());
            String read1 = null;
            String seq1 = null;
            String des1 = null;
            String quality1 = null;
            String read2 = null;
            String seq2 = null;
            String des2 = null;
            String quality2 = null;
            int countline = 0;
            double[] Q1 = new double[150];
            double[] Q2 = new double[150];
            try {
                for (int i = 0; i < 150; i++) {
                    Q1[i] = 0;
                    Q2[i] = 0;
                }
                while ((read1 = br1.readLine()) != null) {
                    if(countline >= Integer.parseInt(readsNumber))break;
                    countline++;
                    if (countline % 5000 == 0) {
                        System.out.println(countline);
                    }
                    seq1 = br1.readLine();
                    des1 = br1.readLine();
                    quality1 = br1.readLine();
                    read2 = br2.readLine();
                    seq2 = br2.readLine();
                    des2 = br2.readLine();
                    quality2 = br2.readLine();
                    for (int i = 0; i < quality1.length(); i++) {
                        Q1[i] += FastqFeature.getscore(quality1.substring(i, i + 1));
                        Q2[i] += FastqFeature.getscore(quality2.substring(i, i + 1));
                    }
                }
                br1.close();
                br2.close();
                for (int i = 0; i < 150; i++) {
                    Q_R1[i][nameMap.get(f)] = (double) Q1[i] / countline;
                    Q_R2[i][nameMap.get(f)] = (double) Q2[i] / countline;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        BufferedWriter bw1 = IOUtils.getTextWriter(new File(outputdir, "Quality_R1.txt").getAbsolutePath());
        BufferedWriter bw2 = IOUtils.getTextWriter(new File(outputdir, "Quality_R2.txt").getAbsolutePath());
        try {
            DecimalFormat defor = new DecimalFormat("0.000");
            for (int i = 0; i < names.length; i++) {
                if(method.equals("mean")) {
                    double value1 = MathUtils.getmean(Q_R1[i]);
                    double value2 = MathUtils.getmean(Q_R1[i]);
                }else if (method.equals("median")) {
                    bw1.write(names[i] + "\t" + Q_R1[i] + "\n");
                    bw2.write(names[i] + "\t" + Q_R2[i] + "\n");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
