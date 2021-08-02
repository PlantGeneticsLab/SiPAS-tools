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
    int misMatchRate = -1 ;
    int miniMatch = -1;
    int miniReads = -1 ;
    int threads = -1;
    String GTFDir = null;
    String refDir = null;
    String STARPath = null;
    String subDirS = "sams";

    public Alignment(String parameterPath) {
        this.parseParameters(parameterPath);
        this.starAlignmentPEAndSortIndex();
    }

    private void starAlignmentPEAndSortIndex () {
        if (!new File(this.inputFileDirS, subDirS).exists()){
            new File(this.inputFileDirS, subDirS).mkdir();
        }
        String subFqDirS = new File(this.inputFileDirS +"/subFastqs/").getAbsolutePath();
        File[] fs = new File(subFqDirS).listFiles();
        List<File> fList = new ArrayList(Arrays.asList());
        fs = IOUtils.listFilesEndsWith(fs, ".fq.gz");
        HashSet<String> nameSet = new HashSet();
        for (int i = 0; i < fs.length; i++) {
            if (fs[i].isHidden()) continue;
            nameSet.add(fs[i].getName().replace(fs[i].getName().split("_")[fs[i].getName().split("_").length-1],""));
        }
        File LN = new File(new File (subFqDirS)+"/"+"LN.txt");
        if (LN.exists()) LN.delete();
        List nameList = new ArrayList();
        for (String t : nameSet){
            nameList.add(t);
        }
        for (int k=0;k<nameList.size();k++){
                StringBuilder sb = new StringBuilder();
                sb.append("zcat ").append(subFqDirS).append(nameList.get(k)+"R1.fq.gz");
                sb.append(" | wc -l >> ").append(subFqDirS).append("LN.txt");
                String command = sb.toString();
                System.out.println(command);
                try{
                    File dir = new File(new File (subFqDirS).getAbsolutePath());
                    String [] cmdarry ={"/bin/bash","-c",command};
                    Process p1=Runtime.getRuntime().exec(cmdarry,null,dir);
                    p1.waitFor();p1.destroy();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            try{
                BufferedReader br = IOUtils.getTextReader(new File(subFqDirS)+"/"+"LN.txt");
                String temp = null; int k =0;
                while ((temp = br.readLine()) != null) {
                    if (Integer.valueOf(temp)/4 < this.miniReads){
                        nameSet.remove(nameList.get(k));
                    }
                    k++;
                }
                br.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            nameSet.stream().forEach(f ->{
                String infile1 = new File (subFqDirS,f+"R1.fq.gz").getAbsolutePath();
                String infile2 = new File (subFqDirS,f+"R2.fq.gz").getAbsolutePath();
                StringBuilder sb = new StringBuilder();
                sb.append(this.STARPath).append(" --runThreadN ").append(this.threads);
//            sb.append(" --genomeDir ").append(new File(this.outputDirS,"starLib").getAbsolutePath());
                sb.append(" --genomeDir ").append(new File(this.starLib).getAbsolutePath());
                sb.append(" --genomeLoad LoadAndKeep --readFilesCommand zcat");
                sb.append(" --readFilesIn ").append(infile1+" "+infile2);
                sb.append(" --outFileNamePrefix ").append(new File(new File(this.inputFileDirS, subDirS).getAbsolutePath(), f)
                        .getAbsolutePath()).append(" --outFilterMultimapNmax ").append(this.multiLoci);
                sb.append(" --outFilterMismatchNoverLmax ").append(this.misMatchRate)
                        .append(" --outFilterIntronMotifs RemoveNoncanonicalUnannotated ");
                sb.append(" --outSAMtype BAM Unsorted");
                sb.append(" --outFilterScoreMinOverLread 0 --outFilterMatchNminOverLread 0 --outFilterMatchNmin ").append(this.miniMatch);
                String command = sb.toString();
                System.out.println(command);


                String infile3 = new File(new File(this.inputFileDirS, subDirS).getAbsolutePath(), f).getAbsolutePath();
                StringBuilder sb1= new StringBuilder();
                sb1.append("samtools sort -o "+ infile3+"Aligned.out.sorted.bam "+infile3+"Aligned.out.bam -@ "+ this.threads);
                String command1 = sb1.toString();
                System.out.println(command1);

                StringBuilder sb2= new StringBuilder();
                sb2.append("rm "+ infile3+"Aligned.out.bam");
                String command2 = sb2.toString();
                System.out.println(command2);

                StringBuilder sb3= new StringBuilder();
                sb3.append("samtools index "+infile3+"Aligned.out.sorted.bam -@ "+this.threads);
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
            StringBuilder time = new StringBuilder();
            System.out.println(time.toString());
    }
    public void parseParameters(String parameterFileS) {
        List<String> pLineList = new ArrayList<>();
        try {
            BufferedReader br = IOUtils.getTextReader(parameterFileS);
            String temp = null;
            while ((temp = br.readLine()) != null) {
                if (temp.startsWith("#")) continue;
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
        this.misMatchRate=Integer.parseInt(pLineList.get(3));
        this.miniMatch=Integer.parseInt(pLineList.get(4));
        this.threads=Integer.parseInt(pLineList.get(5));
        this.GTFDir=pLineList.get(6);
        this.refDir=pLineList.get(7);
        this.STARPath=pLineList.get(8);
    }
}