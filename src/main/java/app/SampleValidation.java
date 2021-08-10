package app;

import pgl.app.hapScanner.HapScanner;
import pgl.infra.utils.Dyad;
import pgl.infra.utils.IOUtils;
import utils.AppUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.util.HashSet;
import java.util.List;


public class SampleValidation {

    int chrNumber = 0;
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


    public SampleValidation(String parameterPath) {
        this.getVCF(parameterPath);
//        this.correction();
//        this.getIBS();
    }

    public void getVCF(String parameterPath) {
        this.parseparameters(parameterPath);
        this.parameter();
        this.taxaRefBAM();
        this.Hapscanner();
        this.getRNA();
        this.getDNA();
        this.getIBS();
        this.getheter();
        this.getQuality();
        this.filtersample();
    }

    private void filtersample() {
    }

    private void getheter() {
    }

    private void getQuality() {
    }

    private void getDNA() {
    }

    private void getRNA() {
    }


    public void parseparameters(String infileS) {
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

        File posdir = new File(new File(posDir).getAbsolutePath());
        File posAlleledir = new File(new File(posAlleleDir).getAbsolutePath());
        if (!posdir.exists() || !posAlleledir.exists()) {
            posdir.mkdir();
            posAlleledir.mkdir();
            this.poswithAllele();
        }

        System.out.printf("WTF");

        File output = new File(new File(outputDir).getAbsolutePath());
        output.mkdir();

        File RNAdir = new File(new File(outputDir, "RNA").getAbsolutePath());
        File DNAdir = new File(new File(outputDir, "DNA").getAbsolutePath());
        File Heterdir = new File(new File(outputDir,"Heter").getAbsolutePath());
        File Qualitydir = new File(new File(outputDir,"Quality").getAbsolutePath());
        File Summarydir = new File(new File(outputDir,"Summary").getAbsolutePath());

//        System.out.println(new File(outputDir, "RNA").getAbsolutePath());

        RNAdir.mkdir();
        DNAdir.mkdir();
        Heterdir.mkdir();
        Qualitydir.mkdir();
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

    public void poswithAllele() {
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
                        "#HapScanner is used to perform genotyping of diploid species from whole genome sequenceing data, based on an existing genetic variation library.\n" +
                        "#To run and pipeline, the machine should have both Java 8 and samtools installed. The lib directory should stay with TIGER.jar in the same folder.\n" +
                        "#Command line example. java -Xmx100g -jar TIGER.jar -a HapScanner -p parameter_hapscanner.txt > log.txt &\n" +
                        "#To specify options, please edit the the parameters below. Also, please keep the order of parameters.\n" +
                        "\n" +
                        "#Parameter 1: The taxaRefBam file containing information of taxon and its corresponding refernece genome and bam files. The bam file should have .bai file in the same folder\n" +
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
                    sb.append(namelist[j].replace("_Aligned.out.sorted.bam","")).append("\t");
                    sb.append(new File(referenceDir, "chr" + chr + ".fa").getAbsolutePath() + "\t");
                    sb.append(new File(BamDir,namelist[j]).getAbsolutePath());
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

    private void Hapscanner() {
        for (int i = 0; i < chrNumber; i++) {
            int chr = i + 1;
            String infileS = new File(parameterDir, plate + "_parameter_chr" + chr + ".txt").getAbsolutePath();
            new HapScanner(infileS);
        }
    }

    public void getIBS() {

    }


//    public void correction(String[] args) {
//        String input = args[0];
//        String output = args[1];
//    }

}
