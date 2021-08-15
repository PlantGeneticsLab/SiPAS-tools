package app;

import org.checkerframework.checker.units.qual.A;
import pgl.infra.utils.IOUtils;
import pgl.infra.utils.PStringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class Merging {
    String outputDirS = null;
    HashSet<String> plateSet = null;
    HashSet<String> excludeSample = null;
    HashSet<String> sampleSet = null;
    List sampleList = null;
    HashMap<String,List<File>> hm = new HashMap();
    String filePath=null;


    public Merging(String parameterFileS) {
        this.parseParameters(parameterFileS);
    }
    public void countTable(){
        for (String path : plateSet) {
            String geneCountDirS = new File(path,"/geneCount").getAbsolutePath();
            File[] fs = new File(geneCountDirS).listFiles();
            fs = IOUtils.listFilesEndsWith(fs, "Count.txt");
            for (int i=0;i<fs.length;i++){
                String name = fs[i].getName().replace("_Count.txt","");
                if (excludeSample.contains(name)){
                    fs[i].delete();continue;
                }
                int length = name.split("_").length;
                String sample = name.split("_")[0]+"_"+name.split("_")[length-1];
                if (!sampleSet.contains(sample)){
                    sampleSet.add(sample);
                    List <File> temp = new ArrayList<>();
                    temp.add(fs[i]);
                    hm.put(sample,temp);
                }else{
                    List <File> temp = hm.get(sample);
                    temp.add(fs[i]);
                    hm.put(sample,temp);
                }
                filePath =fs[i].getPath();
            }
        }

        int geneNumber=0;
        StringBuilder wc = new StringBuilder();
        wc.append("grep -v \"^__\" ").append(filePath).append(" | wc -l");
        String command = wc.toString();
        System.out.println(command);
        try {
            String[] cmdarry ={"/bin/bash","-c",command};
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

        sampleList= Arrays.asList(sampleSet.toArray());
        List geneList = new ArrayList();
        int [][] count = new int [geneNumber][sampleSet.size()];
        for (Map.Entry<String, List<File>> entry : hm.entrySet()) {
            String sample = entry.getKey();
            List<File> list = entry.getValue();
            list.stream().forEach(f -> {
                String temp=null;String[] tem = null;
                try{
                    BufferedReader br = IOUtils.getTextReader(f.getAbsolutePath());
                    int rowN=0;
                    while((temp = br.readLine()) != null){
                        List<String> tList= PStringUtils.fastSplit(temp);
                        tem = tList.toArray(new String[tList.size()]);
                        if(!tem[0].startsWith("__")){
                            geneList.add(tem[0]);
                            count[rowN][sampleList.indexOf(sample)]+=Integer.parseInt(tem[1]);
                            rowN++;
                        }
                    }
                }
                catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
        }

        String outputFileS = new File (this.outputDirS,"countResult.txt").getAbsolutePath();
        try{
            StringBuilder sb = new StringBuilder();
            BufferedWriter bw = IOUtils.getTextWriter(new File (outputFileS).getAbsolutePath());
            sb.append("Gene"+"\t");
            for(int i=0;i<sampleList.size();i++){
                sb.append(sampleList.get(i)+"\t");
            }
            bw.write(sb.toString().replaceAll("\\s+$", ""));
            bw.newLine();
            for(int k=0;k<count.length;k++){
                sb = new StringBuilder();
                for(int i=0;i<sampleList.size();i++){
                    if(i==0){
                        sb.append(geneList.get(k)).append("\t");
                    }
                    sb.append(count[k][i]).append("\t");
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
    public void parseParameters(String parameterFileS) {
        plateSet = new HashSet();
        excludeSample = new HashSet();
        try {
            BufferedReader br = IOUtils.getTextReader(parameterFileS);
            String temp = null;
            while ((temp = br.readLine()) != null) {
                if (temp.startsWith("#Plate")){
                    plateSet.add(br.readLine());
                    continue;
                }
                if (temp.startsWith("#exclude")){
                    excludeSample.add(br.readLine());
                    continue;
                }
                if (temp.startsWith("#outputDirS")){
                    outputDirS=br.readLine();
                    continue;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
