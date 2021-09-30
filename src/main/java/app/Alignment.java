package app;

import pgl.infra.utils.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class Alignment {
    String starLib =null;
    String inputFileDirS = null;
    String mode = null;
    int multiLoci = -1 ;
    double misMatchRate = 0.0 ;
    int miniMatch = -1;
    int miniReads = -1 ;
    int threads = -1;
    String GTFDir = null;
    String refDir = null;
    String STARPath = null;
    String[] subDirS = {"sams","QC"};

    public Alignment(String parameterFileS) {
        this.parseParameters(parameterFileS);
        this.starAlignmentPEAndSortIndex();
    }

    private void starAlignmentPEAndSortIndex () {
        if (!new File(this.inputFileDirS, subDirS[0]).exists()){
            new File(this.inputFileDirS, subDirS[0]).mkdir();
        }
        new File(this.inputFileDirS, subDirS[1]).mkdir();
        String subFqDirS = new File(this.inputFileDirS +"/subFastqs/").getAbsolutePath();
        File[] fs = new File(subFqDirS).listFiles();
        ArrayList fList = new ArrayList(Arrays.asList());
        fs = IOUtils.listFilesEndsWith(fs, ".fq");
        HashSet<String> nameSet = new HashSet();
        HashSet<String> name1 = new HashSet();HashSet<String> name2 = new HashSet();
        for (int i = 0; i < fs.length; i++) {
            if (fs[i].isHidden()) continue;
            nameSet.add(fs[i].getName().replace(fs[i].getName().split("_")[fs[i].getName().split("_").length-1],""));
        }
        nameSet.stream().forEach(f ->{
            String infile1 = new File (subFqDirS,f+"R1.fq").getAbsolutePath();
            String infile2 = new File (subFqDirS,f+"R2.fq").getAbsolutePath();
            StringBuilder sb = new StringBuilder();
            sb.append(this.STARPath).append(" --runThreadN ").append(this.threads);
            sb.append(" --genomeDir ").append(new File(this.starLib).getAbsolutePath());
            sb.append(" --genomeLoad LoadAndKeep ");
            sb.append(" --readFilesIn ").append(infile1+" "+infile2);
            sb.append(" --outFileNamePrefix ").append(new File(new File(this.inputFileDirS, subDirS[0]).getAbsolutePath(), f)
                    .getAbsolutePath()).append(" --outFilterMultimapNmax ").append(this.multiLoci);
            sb.append(" --outFilterMismatchNoverLmax ").append(this.misMatchRate)
                    .append(" --outFilterIntronMotifs RemoveNoncanonicalUnannotated ");
            sb.append(" --outSAMtype BAM Unsorted");
            sb.append(" --outFilterScoreMinOverLread 0 --outFilterMatchNminOverLread 0 --outFilterMatchNmin ").append(this.miniMatch);
            String command = sb.toString();
            System.out.println(command);

            String infile3 = new File(new File(this.inputFileDirS, subDirS[0]).getAbsolutePath(), f).getAbsolutePath();
            StringBuilder sb1= new StringBuilder();
            sb1.append("samtools sort -o ").append(infile3).append("Aligned.out.sorted.bam ").append(infile3).append("Aligned.out.bam -@ ").append(this.threads);
            String command1 = sb1.toString();
            System.out.println(command1);

            StringBuilder sb2= new StringBuilder();
            sb2.append("rm ").append(infile3).append("Aligned.out.bam");
            String command2 = sb2.toString();
            System.out.println(command2);

            StringBuilder sb3= new StringBuilder();
            sb3.append("samtools index ").append(infile3).append("Aligned.out.sorted.bam -@ ").append(this.threads);
            String command3=sb3.toString();
            System.out.println(command3);

            try {
                Runtime rt = Runtime.getRuntime();
                Process p = rt.exec(command);p.waitFor();
                Process p1 = rt.exec(command1);p1.waitFor();
                Process p2 = rt.exec(command2);p2.waitFor();
                Process p3 = rt.exec(command3);p3.waitFor();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Finished "+f);
        });
    }
    public void parseParameters(String parameterFileS) {
        List<String> pLineList = new ArrayList<>();
        try {
            BufferedReader br = IOUtils.getTextReader(parameterFileS);
            String temp = null;
            while ((temp = br.readLine()) != null) {
                if (temp.startsWith("#") || temp.startsWith("@")  ) continue;
                if (temp.isEmpty()) continue;
                pLineList.add(temp);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        this.inputFileDirS = pLineList.get(0);
        this.mode=pLineList.get(1);
        this.multiLoci=Integer.parseInt(pLineList.get(2));
        this.misMatchRate=Double.parseDouble(pLineList.get(3));
        this.miniMatch=Integer.parseInt(pLineList.get(4));
        this.miniReads=Integer.parseInt(pLineList.get(5));
        this.threads=Integer.parseInt(pLineList.get(6));
        this.starLib=pLineList.get(7);
        this.GTFDir=pLineList.get(8);
        this.refDir=pLineList.get(9);
        this.STARPath=pLineList.get(10);
    }
}
