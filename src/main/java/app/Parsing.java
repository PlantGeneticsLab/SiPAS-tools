package app;

import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pgl.infra.table.RowTable;
import pgl.infra.utils.Benchmark;
import pgl.infra.utils.IOUtils;

import java.io.BufferedReader;
import java.io.File;
import java.util.*;

public class Parsing {
    String inputFile = null;
    String outputFileDirS=null;
    String sampleInformationFileS = null;
    String libraryInformationFileS = null;
    String library = null;
    HashMap<String, String> fqR1R2Map = new HashMap();

    List<String> barcodeLists = null;

    HashMap<String, String> barcodeTaxaMaps = null;

    List<String> taxaLists = null;

    List<String> barcodeLengthLists = null;

    ArrayList barcodeLengths = null;

    String outputDirs=null;

    String[] subDirS = {"subFastqs", "sams", "geneCount", "countTable"};

    public Parsing(String[] arg){
        long start = System.currentTimeMillis();
        this.parseParameters(arg);
        this.processTaxaAndBarcode();
        this.PEParse();
        long end = System.currentTimeMillis();
        System.out.println("Times:"+(end-start)/1000F);
    }

    private void PEParse () {
        String subFqDirS = new File(this.outputDirs,subDirS[0]).getAbsolutePath();
        String[] subFqFileS1 = new String[barcodeLists.size()];
        String[] subFqFileS2 = new String[barcodeLists.size()];
        HashMap<String, String> btMap = new HashMap();btMap=barcodeTaxaMaps;
        Set<String> barcodeSet = new HashSet(barcodeLists);
        BufferedWriter[] bws1 = new BufferedWriter[barcodeLists.size()];
        BufferedWriter[] bws2 = new BufferedWriter[barcodeLists.size()];
        HashMap<String,BufferedWriter> barcodeWriterMap1 = new HashMap();
        HashMap<String,BufferedWriter> barcodeWriterMap2 = new HashMap();
        for (int i =0; i < subFqFileS1.length; i++){
            String taxon = btMap.get(barcodeLists.get(i));
            subFqFileS1[i] = new File(subFqDirS,taxon+"_R1.fq").getAbsolutePath();
            subFqFileS2[i] = new File(subFqDirS,taxon+"_R2.fq").getAbsolutePath();
            bws1[i]=IOUtils.getTextWriter(subFqFileS1[i]);
            bws2[i]=IOUtils.getTextWriter(subFqFileS2[i]);
            barcodeWriterMap1.put(barcodeLists.get(i),bws1[i]);
            barcodeWriterMap2.put(barcodeLists.get(i),bws2[i]);
        }
        try {
            BufferedReader br1 = null;
            BufferedReader br2 = null;
            String f2= fqR1R2Map.get(this.inputFile);
            if (this.inputFile.endsWith(".gz")) {
                br1 = IOUtils.getTextGzipReader(this.inputFile);
                br2 = IOUtils.getTextGzipReader(f2);
            }
            else {
                br1 = IOUtils.getTextReader(this.inputFile);
                br2 = IOUtils.getTextGzipReader(f2);
            }
            String temp = null;String seq = null;
            String currentBarcode = null; String currentUMI = null;
            BufferedWriter tw1 = null;BufferedWriter tw2 = null;
            int cnt = 0;int cnt2 = 0;
            while((temp = br1.readLine())!=null){
                cnt2++;
                seq = br1.readLine();
                //*************************
                //barcode needs  to have at least 2 mismathches between each other, currently at 3 mismathces between any 8-bp barcodes
                //barcode can be redesigned to have 4-8 bp in length for even efficiency between barcdes
                //*************************
                currentBarcode = seq.substring(0, 8);
                if (barcodeSet.contains(currentBarcode)) {
                    cnt++;
                    tw1 = barcodeWriterMap1.get(currentBarcode);
                    tw1.write(temp);tw1.newLine();
                    tw1.write(seq);tw1.newLine();
                    tw1.write(br1.readLine());tw1.newLine();
                    tw1.write(br1.readLine());tw1.newLine();
                    tw2 = barcodeWriterMap2.get(currentBarcode);
                    tw2.write(br2.readLine());tw2.newLine();
                    tw2.write(br2.readLine());tw2.newLine();
                    tw2.write(br2.readLine());tw2.newLine();
                    tw2.write(br2.readLine());tw2.newLine();
                } else{
                    br2.readLine();br2.readLine();br2.readLine();br2.readLine();
                    br1.readLine();br1.readLine();
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.append(cnt).append(" out of ").append(cnt2).append(", ").append(((float)(double)cnt/cnt2)).append(" of total reads were parsed from " + this.library).append(" ").append(f2);
            System.out.println(sb.toString());
            for (int i =0;i<subFqFileS1.length;i++){
                bws1[i].flush();bws1[i].close();
                bws2[i].flush();bws2[i].close();
            }
            br1.close();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    private void parseParameters (String[] arg) {
        this.inputFile =arg[0];
        this.outputFileDirS=arg[1];
        this.sampleInformationFileS=arg[2];
        this.library=arg[3];
        try{
            this.libraryInformationFileS=new File(this.outputFileDirS).getAbsolutePath()+"/SampleInformation_"+this.library+".txt";
            try{
                BufferedReader br = IOUtils.getTextReader(this.sampleInformationFileS);
                BufferedWriter bw = IOUtils.getTextWriter(this.libraryInformationFileS);
                String temp = null;bw.write("Sample ID\tTaxa ID\tStage\tTissue\tBarcode\n");
                while ((temp = br.readLine()) != null) {
                    if (temp.split("\t")[0].startsWith(this.library)){
                        bw.write(temp+"\n");
                    }
                }
                br.close();
                bw.flush();bw.close();
            }
            catch (Exception ex){
                ex.getStackTrace();
            }
        }
        catch(Exception ex){
            ex.getStackTrace();
        }
    }
    private void processTaxaAndBarcode () {
        RowTable<String> t = new RowTable<>(this.libraryInformationFileS);
        String R2=null;
        if (this.inputFile.contains("R1.fq.gz")){
            R2= inputFile.replace("R1.fq.gz","R2.fq.gz");
        }else{
            R2= inputFile.replace("R2.fq.gz","R1.fq.gz");
        }
        fqR1R2Map.putIfAbsent(inputFile, R2);
        barcodeLengths = new ArrayList<Integer>();
        barcodeLists = new ArrayList();
        taxaLists = new ArrayList();
        barcodeTaxaMaps = new HashMap();
        for (int i = 0; i < t.getRowNumber(); i++) {
            String taxon = t.getCell(i, 2)+t.getCell(i, 3)+"_"+t.getCell(i,0)+"_"+t.getCell(i, 1);//输出的文件的名字
            taxaLists.add(taxon);
            barcodeLists.add(t.getCell(i, 4));
            barcodeTaxaMaps.put(t.getCell(i, 4), taxon);
            barcodeLengths.add(t.getCell(i, 4).length());
        }
        new File(this.outputFileDirS,"/"+this.library).mkdir();
        outputDirs=new File(this.outputFileDirS,"/"+this.library).toString();
        for (int i =0;i< subDirS.length;i++){
            new File(outputDirs, subDirS[i]).mkdir();
        }
    }

}
