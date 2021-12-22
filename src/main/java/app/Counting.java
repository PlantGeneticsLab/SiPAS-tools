package app;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import utils.Command;
import utils.MonitorUtils;

public class Counting {

    String inputFileDirS = null;
    String GTFDir = null;
    int threads = 32;
    String[] subDirS = {"geneCount","countTable"};

    public Counting(String[] arg) {
        inputFileDirS = arg[0];GTFDir = arg[1];threads=Integer.parseInt(arg[2]);
        this.HTseqCount();
    }
    public void HTseqCount() {
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
        try{
            ExecutorService pool = Executors.newFixedThreadPool(this.threads);
            File dir = new File(new File (this.inputFileDirS,subDirS[0]).getAbsolutePath());
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
                Command com = new Command(command,dir);
                Future<Command> chrom = pool.submit(com);
                System.out.println(command);
            });
            pool.shutdown();
            pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS);
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
    }
}
