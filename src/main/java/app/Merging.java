package app;

import org.checkerframework.checker.units.qual.A;
import pgl.infra.utils.IOUtils;
import pgl.infra.utils.PStringUtils;

import javax.swing.table.TableStringConverter;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.InputStreamReader;
import java.util.*;

public class Merging {
    String inputDirS = null;
    String outputDirS = null;
    List<String> tissueList = null;
    HashMap<String,ArrayList>[] taxonSample = null;
    int count [][][] = null;
    HashSet sampleInfor []=null;
    List sampleList = null;
    HashMap<String,List<File>> hm = new HashMap();


    public Merging(String[] parameters) {
        this.countResult(parameters);
    }
    public void countResult(String [] parameters) {
        this.inputDirS=parameters[0];
        this.outputDirS=parameters[1];
        List<String> fileList = new ArrayList();
        try {
            String command = "find "+this.inputDirS+"/*_[0-9] -name correction.txt";
            String[] cmdarry ={"/bin/bash","-c",command};
            Process p =Runtime.getRuntime().exec(cmdarry,null);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                fileList.add(temp);
            }
            p.waitFor();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        String temp =null; String [] tem =null;tissueList= new ArrayList<>();
        for (int i=0;i<fileList.size();i++){
            try{
                BufferedReader br = IOUtils.getTextReader(new File(fileList.get(i)).getAbsolutePath());
                while ((temp=br.readLine())!=null){
                    List<String> tList= PStringUtils.fastSplit(temp);
                    tem = tList.toArray(new String[tList.size()]);
                    String tissue = tem[0].split("_")[0].replace("RNA","");
                    if (!tissueList.contains(tissue)){
                        tissueList.add(tissue);
                    }
                }
            }
            catch (Exception ex){
                ex.getStackTrace();
            }
        }
        String file=null;taxonSample=new HashMap[tissueList.size()];
        for (int i=0;i<tissueList.size();i++){
            taxonSample[i]= new HashMap<String,ArrayList>();
        }
        for (int i=0;i<fileList.size();i++){
            try{
                BufferedReader br = IOUtils.getTextReader(new File(fileList.get(i)).getAbsolutePath());
                ArrayList samples = new ArrayList();
                while ((temp=br.readLine())!=null){
                    List<String> tList= PStringUtils.fastSplit(temp);
                    tem = tList.toArray(new String[tList.size()]);
                    String tissue = tem[0].split("_")[0].replace("RNA","");
                    int index = tissueList.indexOf(tissue);
                    if (tem[1]==tem[3] && Integer.parseInt(tem[2]) < 0.1){
                        if (!taxonSample[index].containsKey(tem[1])){
                            samples = new ArrayList(Arrays.asList(tem[0].replace("RNA","")));
                        }else{
                            samples = taxonSample[index].get(tem[1]);
                            samples.add(tem[0].replace("RNA",""));
                        }
                        taxonSample[index].put(tem[1],samples);
                    }else {
                        if ( tem[5].equals("0.0") || tem[5].equals("NaN") || Double.parseDouble(tem[4]) > 0.1)continue;
                        double dis = Math.abs(Double.parseDouble(tem[2])-Double.parseDouble(tem[4]));
                        if(dis<0.1 ){
                            if (!taxonSample[index].containsKey(tem[1])){
                                samples = new ArrayList(Arrays.asList(tem[0].replace("RNA","")));
                            }else{
                                samples = taxonSample[index].get(tem[1]);
                                samples.add(tem[0].replace("RNA",""));
                            }
                            taxonSample[index].put(tem[1],samples);
                        }else{
                            if(!taxonSample[index].containsKey(tem[3])){
                                samples = new ArrayList(Arrays.asList(tem[0].replace("RNA","")));
                            }else{
                                samples = taxonSample[index].get(tem[3]);
                                samples.add(tem[0].replace("RNA",""));
                            }
                            taxonSample[index].put(tem[3],samples);
                            file=tem[0].replace("RNA","");
                        }
                    }
                }
            }
            catch (Exception ex){
                ex.getStackTrace();
            }
        }
        String filePath = this.outputDirS+"/"+file.split("_")[1]+"_"+file.split("_")[2]+"/geneCount/"+file+"_Count.txt";
        System.out.println(filePath);
        StringBuilder wc = new StringBuilder();int geneNumber=0;
        wc.append("grep -v \"^__\" ").append(filePath).append(" | wc -l");
        String command = wc.toString();
        System.out.println(command);
        try {
            String[] cmdarry ={"/bin/bash","-c",command};
            Process p =Runtime.getRuntime().exec(cmdarry,null);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((temp = br.readLine()) != null) {
                geneNumber=Integer.valueOf(temp.replaceAll(" ",""));
            }
            p.waitFor();
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
        count = new int[tissueList.size()][][];
        sampleInfor = new HashSet[tissueList.size()];
        List<String> fileNames = new ArrayList();String sample = null; int sampleIndex = 0;
        HashSet geneSet = new HashSet();List geneList = new ArrayList();
        for (int i =0;i<tissueList.size();i++){
            count[i]=new int[geneNumber][taxonSample[i].size()];
            sampleInfor[i]=new HashSet();
            Set<String> sampleSet = taxonSample[i].keySet();
            sampleList= Arrays.asList(sampleSet.toArray());
            Collections.sort(sampleList);
            for (Map.Entry<String, ArrayList> entry : taxonSample[i].entrySet()) {
                fileNames=entry.getValue(); sample=entry.getKey();
                sampleIndex=sampleList.indexOf(sample); sampleInfor[i].add(sampleIndex);
                for (int j =0;j<fileNames.size();j++){
                    String folder = fileNames.get(j).split("_")[1]+"_"+fileNames.get(j).split("_")[2];
                    String path = this.outputDirS +"/"+folder+"/geneCount/"+fileNames.get(j)+"_Count.txt";
                    int rowN=0;
                    try{
                        BufferedReader br = IOUtils.getTextReader(new File(path).getAbsolutePath());
                        while((temp = br.readLine()) != null){
                            List<String> tList= PStringUtils.fastSplit(temp);
                            tem = tList.toArray(new String[tList.size()]);
                            if(!tem[0].startsWith("__")){
                                if (!geneSet.contains(tem[0])){
                                    geneSet.add(tem[0]);geneList.add(tem[0]);
                                }
                                count[i][rowN][sampleIndex]+=Integer.parseInt(tem[1]);
                                rowN++;
                            }
                        }
                    }
                    catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                int sum =0;
                for (int j =0; j<geneNumber;j++){
                    sum+=count[i][j][sampleIndex];
                    if (sum>=100000){
                        break;
                    }
                }
                if (sum<100000){
                    sampleInfor[i].remove(sampleIndex);
                }
            }
        }
        new File(this.outputDirS,"/countResult").mkdir();
        String resultDirS = this.outputDirS+"/countResult/";
        String[] outputFileS = new String[tissueList.size()];
        for (int i =0;i<tissueList.size();i++){
            outputFileS[i]=new File(resultDirS,tissueList.get(i)+"_countResult.txt").getAbsolutePath();
            Set<String> sampleSet = taxonSample[i].keySet();
            sampleList= Arrays.asList(sampleSet.toArray());
            Collections.sort(sampleList);
            try{
                StringBuilder sb = new StringBuilder();
                BufferedWriter bw = IOUtils.getTextWriter(new File (outputFileS[i]).getAbsolutePath());
                sb.append("Gene"+"\t");
                for(int j=0;j<sampleList.size();j++){
                    if (sampleInfor[i].contains(j)){
                        sb.append(sampleList.get(j)+"\t");
                    }
                }
                bw.write(sb.toString().replaceAll("\\s+$", ""));
                bw.newLine();
                for(int j=0;j<count[i].length;j++){
                    sb = new StringBuilder();
                    for(int k=0;k<sampleList.size();k++){
                        if(k==0){
                            sb.append(geneList.get(j)).append("\t");
                        }
                        if(sampleInfor[i].contains(k)){
                            sb.append(count[i][j][k]).append("\t");
                        }
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
}
