package app;

import pgl.infra.utils.IOUtils;
import pgl.infra.utils.PStringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class Counting {

    String inputFileDirS = null;
    String GTFDir = null;
    int threads = 32;
    int currentThreads = 0;
    int total = 0;
    String subDirS[] = {"geneCount","countTable"};

    public Counting(String[] arg) {
        inputFileDirS = arg[0];GTFDir = arg[1];threads=Integer.parseInt(arg[2]);
        this.HTseqCount();
        this.countTable();
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
        while (total<fs.length){
            currentThreads=this.monitor();
            if (currentThreads<threads){
                for (int j=total;j<(total+threads-currentThreads);j++){
                    fL1.add(fList.get(j));
                }
                this.runHTSeqCount(fL1);
                total=total+threads-currentThreads;
                fL1.clear();
            }else{
                try{
                    TimeUnit.MINUTES.sleep(1);
                }
                catch (Exception ex){
                    ex.getStackTrace();
                }
            }
        }
    }
    public int monitor (){
        try{
            String [] cmdarry ={"/bin/bash","-c","ps aux | grep htseq-count | wc -l"};
            Process p =Runtime.getRuntime().exec(cmdarry,null);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                currentThreads=(Integer.parseInt(temp)-1)/2;
            }
            p.waitFor();
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
                String []cmdarry ={"/bin/bash","-c",command};
                Process p=Runtime.getRuntime().exec(cmdarry,null,dir);
                p.waitFor(2,TimeUnit.MINUTES);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    public void countTable(){
            String subCountDirS = new File (this.inputFileDirS,subDirS[0]).getAbsolutePath();
            File[] fs = new File(subCountDirS).listFiles();
            fs = IOUtils.listFilesEndsWith(fs, "Count.txt");
            ArrayList fileList = (ArrayList) Arrays.asList(fs);
            //Begin to merge
            int geneNumber=0;
            Set<String> geneSet = new HashSet<String>();
            StringBuilder wc = new StringBuilder();
            wc.append("grep -v \"^__\" ").append(fileList.get(0)).append(" | wc -l");//通配符\
            String command = wc.toString();
            System.out.println(command);
            try {
                String [] cmdarry ={"/bin/bash","-c",command};
                Process p =Runtime.getRuntime().exec(cmdarry,null);
                BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String temp = null;
                while ((temp = br.readLine()) != null) {
                    geneNumber=Integer.valueOf(temp.split(" ")[0]);
                }
                p.waitFor();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println(geneNumber);
            Set<String> nameSet = new HashSet<String>();
            List <String> nameList=new ArrayList<>();
            int [][] count = new int [geneNumber][fileList.size()];
            List<File> list = fileList;
            list.stream().forEach(f -> {
                String temp=null;String[] tem = null;
                try{
                    BufferedReader br = IOUtils.getTextReader(f.getAbsolutePath());
                    int rowN=0;
                    while((temp = br.readLine()) != null){
                        List<String> tList= PStringUtils.fastSplit(temp);
                        tem = tList.toArray(new String[tList.size()]);
                        if(!tem[0].startsWith("__")){
                            if(!nameSet.contains(tem[0])){
                                nameList.add(tem[0]);
                            }
                            nameSet.add(tem[0]);
                            count[rowN][list.indexOf(f)]=Integer.parseInt(tem[1]);
                            rowN++;
                        }
                    }
                }
                catch (Exception ex) {
                    System.out.println(tem[0]+"\t"+geneSet.size()+"\t1234");
                    ex.printStackTrace();
                }
            });
            File subDir = new File (this.inputFileDirS,subDirS[1]);
            String outputFileS=null;
            outputFileS = new File (subDir,"countResult.txt").getAbsolutePath();
            try{
                StringBuilder sb = new StringBuilder();
                BufferedWriter bw = IOUtils.getTextWriter(new File (outputFileS).getAbsolutePath());
                sb.append("Gene"+"\t");
                for(int i=0;i<fileList.size();i++){
                    sb.append(fs[i].getName().replace("_Count.txt","")+"\t");
                }
                bw.write(sb.toString().replaceAll("\\s+$", ""));
                bw.newLine();
                for(int k=0;k<count.length;k++){
                    sb = new StringBuilder();
                    for(int i=0;i<fileList.size();i++){
                        if(i==0){
                            sb.append(nameList.get(k)+"\t");
                        }
                        sb.append(count[k][i]+"\t");
                    }
                    bw.write(sb.toString().replaceAll("\\s+$", ""));
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
    }
}
