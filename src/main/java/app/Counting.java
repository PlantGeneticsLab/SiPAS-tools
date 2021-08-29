package app;

import pgl.infra.utils.IOUtils;
import pgl.infra.utils.PStringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;
import utils.MonitorUtils;

public class Counting {

    String inputFileDirS = null;
    String GTFDir = null;
    int threads = 32;
    int currentThreads = 0;
    int total = 0;
    String[] subDirS = {"geneCount","countTable"};

    public Counting(String[] arg) {
        inputFileDirS = arg[0];GTFDir = arg[1];threads=Integer.parseInt(arg[2]);
        this.HTseqCount();
    }
    public void HTseqCount()  {
        for (int i =0 ; i< subDirS.length;i++){
            if (!new File(this.inputFileDirS, subDirS[i]).exists()){
                new File(this.inputFileDirS, subDirS[i]).mkdir();
            }
        }
        String inputDirS = new File (this.inputFileDirS, "/sams/").getAbsolutePath();
        File[] fs = new File(inputDirS).listFiles();
        List<File> fList = new ArrayList(Arrays.asList());
        for(int i=0; i< fs.length;i++){
            if(fs[i].getName().endsWith(".bam")){
                fList.add(fs[i]);
            }
        }
        List<File> fL1 = new ArrayList(Arrays.asList());
        for (int i=0;i<threads;i++){
            fL1.add(fList.get(i));
        }
        total=threads;
        this.runHTSeqCount(fL1);
        fL1.clear();
        try {
            while (total<fs.length){
                currentThreads=MonitorUtils.monitor("htseq-count");
                if (currentThreads<threads){
                    if(total==fs.length)break;
                    for (int j=total;j<(total+threads-currentThreads);j++){
                        fL1.add(fList.get(j));
                    }
                    this.runHTSeqCount(fL1);
                    total=total+threads-currentThreads;
                    fL1.clear();
                }else{
                    TimeUnit.MINUTES.sleep(1);
                }
            }
            while(MonitorUtils.monitor("htseq-count")!=0){
                TimeUnit.MINUTES.sleep(1);
            }
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
    }
    public static String monitor (String arg){
        String currentThreads =null;
        String command = "ps aux | grep "+arg+" | wc -l ";
        String [] cmdarry ={"/bin/bash","-c",command};
        try{
            Process p =Runtime.getRuntime().exec(cmdarry,null);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = null;
            while ((temp = br.readLine()) != null) {
//                currentThreads=Integer.parseInt(temp);
                currentThreads=temp;
            }
            p.waitFor();br.close();
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
        return currentThreads;
    }
    public void runHTSeqCount(List<File> fList){
        fList.parallelStream().forEach(f -> {
            StringBuilder sb = new StringBuilder();
            sb.append("htseq-count").append(" -f bam -m intersection-nonempty -s no ");
            sb.append(f);
            sb.append(" "+this.GTFDir).append(" > ");
            if (f.getName().contains("Aligned.out.bam") ){
                sb.append(f.getName().replace("Aligned.out.bam", "Count.txt"));
            }
            if (f.getName().contains("Aligned.out.sorted.bam")){
                sb.append(f.getName().replace("Aligned.out.sorted.bam", "Count.txt"));
            }
            String command = sb.toString();
            System.out.println(command);
            try {
                File dir = new File(new File (this.inputFileDirS,subDirS[0]).getAbsolutePath());
                String[] cmdarry ={"/bin/bash","-c",command};
                Process p=Runtime.getRuntime().exec(cmdarry,null,dir);
                p.waitFor(0,TimeUnit.MINUTES);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
