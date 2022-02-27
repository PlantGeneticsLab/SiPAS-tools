package app;

import pgl.app.hapScanner.HapScanner;
import pgl.infra.dna.genot.GenoIOFormat;
import pgl.infra.dna.genot.GenotypeGrid;
import pgl.infra.dna.genot.GenotypeOperation;
import pgl.infra.dna.genot.summa.SumTaxaDivergence;
import pgl.infra.table.RowTable;
import pgl.infra.utils.Dyad;
import pgl.infra.utils.IOFileFormat;
import pgl.infra.utils.IOUtils;
import pgl.infra.utils.PStringUtils;
import utils.AppUtils;
import utils.ArrayUtils;
import utils.MathUtils;
import utils.VCFutils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.*;


public class SampleValidation {

    int chrNumber = 0;
    double rate = 0;
    String plate = null;

    String outputDir = null;
    String parameterDir = null;
    String taxaRefBAMDir = null;
    String posDir = null;
    String posAlleleDir = null;
    String referenceDir = null;
    String samtoolsPath = null;
    String threads = null;
    String parameterPath = null;

    String genotypeDir = null;
    String BamDir = null;
    String genotypesuffix = ".vcf.gz";

    String QCdir = null;
    String method = null;

    public SampleValidation(String parameterPath) {
        this.getVCF(parameterPath);
        this.getRNA();
        this.getDNA();
        this.getMerge();
        this.getIBS();
        this.getHeterozygosity();
        this.getDensityHeatmap();
        this.filtersample();
    }

    public void getVCF(String parameterPath) {
        this.parseParameters(parameterPath);
        this.parameter();
        this.taxaRefBAM();
        this.HapScanner();

    }

    private void getDensityHeatmap() {
        System.out.println("This is writing file of RNA and DNA IBS plot ***********************************************");
        String infileDir = new File(outputDir, "Summary").getAbsolutePath();
        String infile = new File(infileDir, "check.txt").getAbsolutePath();
        String outfile = new File(infileDir, "IBSdensity.txt").getAbsolutePath();
        String outfile1 = new File(infileDir, "IBSheatmap.txt").getAbsolutePath();
        BufferedReader br = IOUtils.getTextReader(infile);
        BufferedReader brinfo = IOUtils.getTextReader(infile);
        BufferedWriter bw = IOUtils.getTextWriter(outfile);
        BufferedWriter bw1 = IOUtils.getTextWriter(outfile1);
        String temp = null;
        String[] temps = null;
        int countlines = 0;
        try {
            HashMap<String, Integer> nameIndexMap = new HashMap<>();
            LinkedHashSet<String> samples = new LinkedHashSet<>();
            LinkedHashSet<String> DNAsamples = new LinkedHashSet<>();
            while ((temp = brinfo.readLine()) != null) {
                if (temp.startsWith("Dxy")) {
                    temps = temp.split("\t");
                    for (int i = 0; i < temps.length; i++) {
                        nameIndexMap.put(temps[i], i);
                        if (temps[i].startsWith("RNA")) {
                            samples.add(temps[i]);
                        } else if (temps[i].startsWith("E")) {
                            DNAsamples.add(temps[i]);
                        }
                    }
                }
            }
            brinfo.close();
            String[] samplelist = samples.toArray(new String[samples.size()]);
            String[] DNAsamplelist = DNAsamples.toArray(new String[DNAsamples.size()]);

            bw.write("RNA\tDNA\tIBSdistance\n");
            RowTable<String> t = new RowTable<>(infile);
            for (int i = 0; i < samplelist.length; i++) {
                String RNA = samplelist[i];
                String DNA = null;
                if (RNA.contains("JM22")) {
                    DNA = "E025";
                } else if (RNA.contains("CS")) {
                    DNA = "E360";
                } else {
//                    DNA = RNA.substring(3, 7);
                    DNA = RNA.split("_")[4];
                }
                if (DNA.equals("NULL")) continue;
                int RNAindex = nameIndexMap.get(RNA);
                int DNAindex = nameIndexMap.get(DNA) - 1;
                bw.write(RNA + "\t" + DNA + "\t");
                bw.write(t.getCell(DNAindex, RNAindex));
                bw.write("\n");
            }
            bw.flush();
            bw.close();

            bw1.write("RNA\tDNA\tIBSdistance\n");
            for (int i = 0; i < samplelist.length; i++) {
                System.out.println(samplelist[i]);
                String RNA = samplelist[i];
                for (int j = 0; j < DNAsamplelist.length; j++) {
                    String DNA = DNAsamplelist[j];
                    int RNAindex = nameIndexMap.get(RNA);
                    int DNAindex = nameIndexMap.get(DNA) - 1;
                    bw1.write(RNA + "\t" + DNA + "\t");
                    bw1.write(t.getCell(DNAindex, RNAindex));
                    bw1.write("\n");
                }
            }
            bw1.flush();
            bw1.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void filtersample() {
        long startTime = System.currentTimeMillis();   //获取开始时间
        System.out.println("start");
        System.out.println("This is filtering samples ******************************************************************");
        String outfileDir = new File(outputDir, "Summary").getAbsolutePath();
        String infile = new File(outfileDir, "IBSdensity.txt").getAbsolutePath();
        String infor = new File(outfileDir, "check.txt").getAbsolutePath();
        HashSet<String> sampleSet = new HashSet<>();
        HashSet<String> notsampleSet = new HashSet<>();
        HashSet<String> addingsampleSet = new HashSet<>();
        HashMap<String, String> RNADNAmap = new HashMap<>();

        HashMap<String, String> heterMap = getheter(new File(outputDir, "Heter/heterRNA.txt").getAbsolutePath());
        System.out.println(new File(outputDir, "Heter/heterRNA.txt").getAbsolutePath());

        try {
            String temp = null;
            String[] temps = null;
            BufferedReader br = IOUtils.getTextReader(infile);

            while ((temp = br.readLine()) != null) {
                if (temp.startsWith("RNA\t")) continue;
                temps = temp.split("\t");
                RNADNAmap.put(temps[0], temps[1]);
                if (Double.parseDouble(temps[2]) < 0.1) {
                    sampleSet.add(temps[0]);
                } else {
                    notsampleSet.add(temps[0]);
                }
            }
            br.close();

            System.out.println("original RNA samples");
            String[] temp1 = sampleSet.toArray(new String[sampleSet.size()]);
            for (int i = 0; i < temp1.length; i++) {
                System.out.println(temp1[i]);
            }

            BufferedReader brinfo = IOUtils.getTextReader(infor);

            RowTable<String> t = new RowTable<>(infor);
            //index of RNA samples and DNA samples : 1,2,3,...,RNAsamplecount,...,total
            String[] header = t.getHeader().toArray(new String[0]);
            List<String> RNASet = new ArrayList<>();
            List<String> DNASet = new ArrayList<>();
            for (int i = 0; i < header.length; i++) {
                if (header[i].startsWith("RNA")) {
                    RNASet.add(header[i]);
                } else if (!header[i].startsWith("Dxy")) {
                    DNASet.add(header[i]);
                }
            }

            HashMap<String, Integer> nameIndexMap = new HashMap<>();
            BufferedWriter bw = IOUtils.getTextWriter(new File(outfileDir, "correction.txt").getAbsolutePath());
            BufferedWriter bw1 = IOUtils.getTextWriter(new File(outfileDir, "list1.txt").getAbsolutePath());
            BufferedWriter bw2 = IOUtils.getTextWriter(new File(outfileDir, "list2.txt").getAbsolutePath());
            BufferedWriter bw3 = IOUtils.getTextWriter(new File(outfileDir, "list3.txt").getAbsolutePath());

            for (int i = 0; i < RNASet.size(); i++) {
                String RNA = RNASet.get(i);
//                if(Double.parseDouble(heterMap.get(RNA)) > 0.08)continue;
                String DNA = null;
                if (RNA.contains("JM22")) {
                    DNA = "E025";
                } else if (RNA.contains("CS")) {
                    DNA = "E360";
                } else {
//                    DNA = RNA.substring(3, 7);
                    DNA = RNA.split("_")[4].substring(0, 4);
                }
                if (DNA.equals("NULL")) continue;

                System.out.println(RNA);
                System.out.println(DNA);
                double[] IBS = t.getColumnAsDoubleArray(t.getColumnIndex(RNA));
                double[] subIBS = Arrays.copyOfRange(IBS, RNASet.size(), header.length - 1);
                // 对应值
                double IBSvalue = Double.parseDouble(t.getCell(t.getColumnIndex(RNA) - 1, t.getColumnIndex(DNA)));
                double[] Min = MathUtils.Min(subIBS);

                // 最小的DNA
                String DNATrue = header[RNASet.size() + (int) Min[1] + 1];
                // 最小的IBS
                double IBSDNAvalue = Min[0];

                if (Double.parseDouble(heterMap.get(RNA)) <= 0.08) {
                    bw.write(RNA + "\t" + DNA + "\t" + IBSvalue + "\t" + DNATrue + "\t" + IBSDNAvalue + "\t" + heterMap.get(RNA) + "\t");

                    if (DNA.equals(DNATrue)) {
                        bw.write("TRUE" + "\n");
                        bw1.write(RNA + "\t" + DNA + "\t" + heterMap.get(RNA) + "\n");
                    } else {
                        bw.write("False" + "\n");
                        if (Math.abs(IBSDNAvalue - IBSvalue) < 0.1) {
                            bw2.write(RNA + "\t" + DNA + "\t" + heterMap.get(RNA) + "\n");
                        } else if (IBSDNAvalue < 0.1) {
                            bw3.write(RNA + "\t" + DNATrue + "\t" + heterMap.get(RNA) + "\n");
                        }
                    }
                }
            }
            bw.flush();
            bw.close();
            bw1.flush();
            bw1.close();
            bw2.flush();
            bw2.close();
            bw3.flush();
            bw3.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("******* Filtering samples takes " + (endTime - startTime) + "ms");
        System.out.println("End of program.");
    }

    public HashMap<String, String> getheter(String infile) {
        BufferedReader br = IOUtils.getTextReader(infile);
        HashMap<String, String> heterMap = new HashMap<>();
        String temp = null;
        String[] temps = null;
        try {
            while ((temp = br.readLine()) != null) {
                temps = temp.split("\t");
                heterMap.put(temps[0], temps[3]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return heterMap;
    }

    private void getHeterozygosity() {
        File out = new File(new File(outputDir, "Heter").getAbsolutePath());
        String input = new File(outputDir, "RNA/RNAall.vcf.gz").getAbsolutePath();
        String output = new File(out, "heterRNA.txt").getAbsolutePath();
        VCFutils.getHeterozygosityGZ(new File(input).getAbsolutePath(), new File(output).getAbsolutePath());
    }

    private void getMerge() {
        String infileDirRNA = new File(outputDir, "RNA").getAbsolutePath();
        String infileDirDNA = new File(outputDir, "DNA").getAbsolutePath();
        BufferedWriter bwRNA = IOUtils.getTextWriter(new File(infileDirRNA, "RNAall.vcf").getAbsolutePath());
        BufferedWriter bwDNA = IOUtils.getTextWriter(new File(infileDirDNA, "DNAall.vcf").getAbsolutePath());
        String temp = null;
        try {
            for (int i = 0; i < chrNumber; i++) {
                int chr = i + 1;
                BufferedReader br = IOUtils.getTextReader(new File(infileDirRNA, "RNA_chr" + PStringUtils.getNDigitNumber(3, chr) + ".vcf").getAbsolutePath());
                while ((temp = br.readLine()) != null) {
                    if (chr != 1 && temp.startsWith("#")) {
                        continue;
                    } else {
                        bwRNA.write(temp + "\n");
                    }
                }
                br.close();
            }
            bwRNA.flush();
            bwRNA.close();
            for (int i = 0; i < chrNumber; i++) {
                int chr = i + 1;
                BufferedReader br = IOUtils.getTextReader(new File(infileDirDNA, "DNA_chr" + PStringUtils.getNDigitNumber(3, chr) + ".vcf").getAbsolutePath());
                while ((temp = br.readLine()) != null) {
                    if (chr != 1 && temp.startsWith("#")) {
                        continue;
                    } else {
                        bwDNA.write(temp + "\n");
                    }
                }
                br.close();
            }
            bwDNA.flush();
            bwDNA.close();
            StringBuilder sb = new StringBuilder();
            sb.append("rm ./RNA/RNA_chr* \n");
            sb.append("rm ./DNA/DNA_chr* \n");
            sb.append("bgzip ./RNA/RNAall.vcf \n");
            sb.append("bgzip ./DNA/DNAall.vcf \n");
            String command = sb.toString();
            File dir = new File(new File(outputDir).getAbsolutePath());
            String[] cmdarry = {"/bin/bash", "-c", command};
            Process p = Runtime.getRuntime().exec(cmdarry, null, dir);
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getDNA() {
        long startTime = System.currentTimeMillis();   //获取开始时间
        System.out.println("This is getting intersection ***************************************************************");
        String infileDir1 = new File(outputDir, "RNA").getAbsolutePath();
        String infileDir2 = new File(genotypeDir).getAbsolutePath();
        String infileDir3 = new File(outputDir, "DNA").getAbsolutePath();
        HashSet<String> nameSet = new HashSet<>();
        File[] fs = new File(infileDir1).listFiles();
        fs = IOUtils.listFilesEndsWith(fs, ".vcf");
        for (int i = 0; i < fs.length; i++) {
            nameSet.add(fs[i].getName());
        }
        nameSet.parallelStream().forEach(f -> {
            try {
                String temp = null;
                String[] temps = null;
                int chr = Integer.parseInt(String.valueOf(f).replace(".vcf", "").replace("RNA_chr", ""));
                String infile1 = new File(infileDir1, f).getAbsolutePath();
                String infile2 = new File(infileDir2, chr + genotypesuffix).getAbsolutePath();
                String outfile = new File(infileDir3, f.replace("RNA", "DNA")).getAbsolutePath();
                HashSet<String> positions = new HashSet<>();
                BufferedReader br = IOUtils.getTextReader(infile1);
                while ((temp = br.readLine()) != null) {
                    temps = temp.split("\t");
                    if (temp.startsWith("#")) continue;
                    positions.add(temps[1]);
                }
                br.close();
                BufferedReader br1 = IOUtils.getTextGzipReader(infile2);
                BufferedWriter bw = IOUtils.getTextWriter(outfile);
                while ((temp = br1.readLine()) != null) {
                    if (temp.startsWith("#")) {
                        bw.write(temp + "\n");
                        continue;
                    }
                    temps = temp.split("\t");
                    if (positions.contains(temps[1])) {
                        bw.write(temp + "\n");
                    }
                }
                br1.close();
                bw.flush();
                bw.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void getRNA() {
        long startTime = System.currentTimeMillis();   //获取开始时间
        System.out.println("This is getting subRNA genotype ************************************************************");
        System.out.println(outputDir);
        String RNADir = new File(outputDir, "RNA").getAbsolutePath();
        String originalRNA = new File(outputDir, "VCF").getAbsolutePath();
        HashSet<String> nameSet = new HashSet<>();
        File[] fs = new File(originalRNA).listFiles();
        fs = IOUtils.listFilesEndsWith(fs, ".vcf");
        for (int i = 0; i < fs.length; i++) {
            System.out.println(fs[i].getName());
            nameSet.add(fs[i].getName());
        }
        nameSet.parallelStream().forEach(f -> {
            BufferedReader br = IOUtils.getTextReader(new File(originalRNA, f).getAbsolutePath());
            BufferedWriter bw = IOUtils.getTextWriter(new File(RNADir, f.replace("chr", "RNA_chr")).getAbsolutePath());
            String temp = null;
            String[] temps = null;
            StringBuilder sb = new StringBuilder();
            try {
                while ((temp = br.readLine()) != null) {
                    temps = temp.split("\t");
                    if (temp.startsWith("##")) {
                        bw.write(temp + "\n");
                        continue;
                    }
                    if (temp.startsWith("#C")) {
                        for (int i = 0; i < 9; i++) {
                            sb.append(temps[i] + "\t");
                        }
                        for (int i = 9; i < temps.length; i++) {
                            sb.append("RNA" + temps[i] + "\t");
                        }
                        bw.write(sb.toString().replaceAll("\\s+$", "") + "\n");
                        continue;
                    }
                    int num = 0;
//                    int total = (int) (temps.length * 0.4);
                    int total = temps.length - 9;
                    for (int i = 9; i < temps.length; i++) {
                        if (!temps[i].split(";")[0].equals("./.")) {
                            num++;
                        }
                    }
                    if (num >= total * rate) {
                        bw.write(temp + "\n");
                    }
                }
                br.close();
                bw.flush();
                bw.close();
            } catch (Exception e) {
                System.exit(1);
            }
        });
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("******* Getting subRNA genotype vcf takes " + (endTime - startTime) + "ms");
    }


    public void parseParameters(String infileS) {
        Dyad<List<String>, List<String>> d = AppUtils.getParameterList(infileS);
        List<String> pLineList = d.getFirstElement();
        genotypeDir = pLineList.get(0);
        BamDir = pLineList.get(1);
        plate = pLineList.get(2);
        outputDir = pLineList.get(3) + "/" + plate;
        System.out.println(outputDir);

        parameterDir = pLineList.get(4);
        taxaRefBAMDir = pLineList.get(5);
        posDir = pLineList.get(6);
        posAlleleDir = pLineList.get(7);
        referenceDir = pLineList.get(8);

        samtoolsPath = pLineList.get(9);
        threads = pLineList.get(10);
        chrNumber = Integer.parseInt(pLineList.get(11));
        rate = Double.parseDouble(pLineList.get(12));
//        QCdir = pLineList.get(13);
//        method = pLineList.get(14);

        File posdir = new File(new File(posDir).getAbsolutePath());
        File posAlleledir = new File(new File(posAlleleDir).getAbsolutePath());
        if (!posdir.exists() || !posAlleledir.exists()) {
            posdir.mkdir();
            posAlleledir.mkdir();
            this.posWithAllele();
        }

        File output = new File(new File(outputDir).getAbsolutePath());
        output.mkdir();

        File RNAdir = new File(new File(outputDir, "RNA").getAbsolutePath());
        File DNAdir = new File(new File(outputDir, "DNA").getAbsolutePath());
        File Heterdir = new File(new File(outputDir, "Heter").getAbsolutePath());
        File Summarydir = new File(new File(outputDir, "Summary").getAbsolutePath());

        RNAdir.mkdir();
        DNAdir.mkdir();
        Heterdir.mkdir();
        Summarydir.mkdir();

        if (RNAdir.isDirectory()) {
            System.out.println("Yes");
            if (!RNAdir.exists()) {
                System.out.println("Ooops! Not made!");
                RNAdir.mkdir();
                if (RNAdir.exists()) {
                    System.out.println("Ohyeah! Made it");
                }
            } else {
                System.out.println("Already exist!");
            }
        }
    }

    public void posWithAllele() {
        long startTime = System.currentTimeMillis();
        System.out.println("This is writing pos file ****************************************************");
        HashSet<String> nameSet = new HashSet<>();
        File[] fs = new File(genotypeDir).listFiles();
        fs = IOUtils.listFilesEndsWith(fs, "360.vcf.gz");
        for (int i = 0; i < fs.length; i++) {
            System.out.println(fs[i].getAbsolutePath());
            nameSet.add(fs[i].getName());
        }
        nameSet.parallelStream().forEach(p -> {
            System.out.println("Reading file :" + p);
            int chr = Integer.parseInt(p.split("\\.")[0]);
            String infile = new File(genotypeDir, p).getAbsolutePath();
            String outfile = new File(posDir, "pos_chr" + chr + ".txt").getAbsolutePath();
            String outfile1 = new File(posAlleleDir, "posAllele_chr" + chr + ".txt").getAbsolutePath();
            BufferedReader br = IOUtils.getTextGzipReader(infile);
            BufferedWriter bw = IOUtils.getTextWriter(outfile);
            BufferedWriter bw1 = IOUtils.getTextWriter(outfile1);
            try {
                String temp = null;
                String[] temps = null;
                bw1.write("Chr\tPos\tRef\tAlt(maximum 2 alternative alleles, which is seperated by \",\", e.g. A,C)\n");
                while ((temp = br.readLine()) != null) {
                    if (temp.startsWith("#")) continue;
                    temps = temp.split("\t");
                    bw.write(temps[0] + "\t" + temps[1] + "\n");
                    bw1.write(temps[0] + "\t" + temps[1] + "\t" + temps[3] + "\t" + temps[4] + "\n");
                }
                br.close();
                bw.flush();
                bw.close();
                bw1.flush();
                bw1.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("******* Writing pos and posAllele files takes " + (endTime - startTime) + "ms");
    }

    private void parameter() {
        long startTime = System.currentTimeMillis();
        System.out.println("This is writing parameters files ***********************************************************");
        try {
            for (int i = 0; i < chrNumber; i++) {
                int chr = i + 1;
                BufferedWriter bw = IOUtils.getTextWriter(new File(parameterDir, plate + "_parameter_chr" + chr + ".txt").getAbsolutePath());
                bw.write("@App:\tHapScanner\n" +
                        "@Author:\tFei Lu\n" +
                        "@Email:\tflu@genetics.ac.cn; dr.lufei@gmail.com\n" +
                        "@Homepage:\thttps://plantgeneticslab.weebly.com/\n" +
                        "\n" +
                        "#HapScanner is used to perform genotyping of diploid species from whole genome sequencing data, based on an existing genetic variation library.\n" +
                        "#To run and pipeline, the machine should have both Java 8 and samtools installed. The lib directory should stay with TIGER.jar in the same folder.\n" +
                        "#Command line example. java -Xmx100g -jar TIGER.jar -a HapScanner -p parameter_hapscanner.txt > log.txt &\n" +
                        "#To specify options, please edit the the parameters below. Also, please keep the order of parameters.\n" +
                        "\n" +
                        "#Parameter 1: The taxaRefBam file containing information of taxon and its corresponding reference genome and bam files. The bam file should have .bai file in the same folder\n" +
                        "#If one taxon has n bam files, please list them in n rows.\n");
                bw.write(new File(taxaRefBAMDir, plate + "_taxaRefBAM_chr" + chr + ".txt").getAbsolutePath() + "\n" +
                        "\n");
                bw.write("#Parameter 2: The posAllele file (with header), the format is Chr\\tPos\\tRef\\tAlt (from VCF format). The positions come from genetic variation library. \n" +
                        "#A maximum of 2 alternative alleles are supported, which is seperated by \",\", e.g. A,C.\n" +
                        "#Deletion and insertion are supported, denoted as \"D\" and \"I\".\n");
                bw.write(new File(posAlleleDir, "posAllele_chr" + chr + ".txt").getAbsolutePath() + "\n" +
                        "\n");
                bw.write("#Parameter 3: The pos files (without header), the format is Chr\\tPos. The positions come from haplotype library, which is used in mpileup.\n");
                bw.write(new File(posDir, "pos_chr" + chr + ".txt").getAbsolutePath() + "\n" +
                        "\n");
                bw.write("#Parameter 4: The chromosome which will be scanned.\n" +
                        chr + "\n" +
                        "\n" +
                        "#Parameter 5: Combined error rate of sequencing and misalignment. Heterozygous read mapping are more likely to be genotyped as homozygote when the combined error rate is high.\n" +
                        "0.05\n" +
                        "\n" +
                        "#Parameter 6: The path of samtools\n" +
                        samtoolsPath + "\n" +
                        "\n" +
                        "#Parameter 7: Number of threads\n" +
                        threads + "\n" +
                        "\n" +
                        "#Parameter 8: The directory of output\n");
                bw.write(outputDir + "\n");
                bw.flush();
                bw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("******* Writing parameter files takes " + (endTime - startTime) + "ms");
    }

    private void taxaRefBAM() {
        long startTime = System.currentTimeMillis();
        System.out.println("This is writing taxaRefBam files *************************************************************************");
        File[] fs = new File(BamDir).listFiles();
        fs = IOUtils.listFilesEndsWith(fs, "_Aligned.out.sorted.bam");
        HashSet<String> nameSet = new HashSet();
        for (int i = 0; i < fs.length; i++) {
            System.out.println(fs[i]);
            nameSet.add(fs[i].getName());
        }
        try {
            String[] namelist = nameSet.toArray(new String[nameSet.size()]);
            for (int i = 0; i < chrNumber; i++) {
                int chr = i + 1;
                BufferedWriter bw = IOUtils.getTextWriter(new File(taxaRefBAMDir, plate + "_taxaRefBAM_chr" + chr + ".txt").getAbsolutePath());
                bw.write("Taxa\tReference\tBamPath\n");
                for (int j = 0; j < namelist.length; j++) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(namelist[j].replace("_Aligned.out.sorted.bam", "")).append("\t");
                    sb.append(new File(referenceDir, "chr" + chr + ".fa").getAbsolutePath() + "\t");
                    sb.append(new File(BamDir, namelist[j]).getAbsolutePath());
                    bw.write(sb.toString());
                    bw.newLine();
                }
                bw.flush();
                bw.close();
            }
        } catch (
                Exception e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("******* Writing taxaRefBam files takes " + (endTime - startTime) + "ms");
    }

    private void HapScanner() {
        for (int i = 0; i < chrNumber; i++) {
            int chr = i + 1;
            String infileS = new File(parameterDir, plate + "_parameter_chr" + chr + ".txt").getAbsolutePath();
            new HapScanner(infileS);
        }
    }

    public void getIBS() {
        System.out.println("This is getting IBS matrix *****************************************************************");
        String infileDir = new File(outputDir).getAbsolutePath();
        String infileS1 = new File(infileDir, "RNA/RNAall.vcf.gz").getAbsolutePath();
        String infileS2 = new File(infileDir, "DNA/DNAall.vcf.gz").getAbsolutePath();
        String ibsOutfileS = new File(infileDir, "Summary/check.txt").getAbsolutePath();
        GenotypeGrid g1 = new GenotypeGrid(infileS1, GenoIOFormat.VCF);
        GenotypeGrid g2 = new GenotypeGrid(infileS2, GenoIOFormat.VCF);
        GenotypeGrid g = GenotypeOperation.mergeGenotypesByTaxon(g1, g2);
        SumTaxaDivergence std = new SumTaxaDivergence(g);
        std.writeDxyMatrix(ibsOutfileS, IOFileFormat.Text);
        g.getIBSDistanceMatrix();
    }

}
