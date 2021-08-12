package utils;

import pgl.infra.dna.genot.GenoIOFormat;
import pgl.infra.dna.genot.GenotypeGrid;
import pgl.infra.dna.genot.GenotypeOperation;
import pgl.infra.dna.genot.summa.SumTaxaDivergence;
import pgl.infra.utils.IOFileFormat;
import pgl.infra.utils.IOUtils;
import pgl.infra.utils.PStringUtils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Common utils with VCF files
 *
 * @ author: yxh
 * @ created: 2021-08-05 : 3:28 PM
 */
public class VCFutils {

    String inputfile = null;
    String outputfile = null;
    String temp = null;
    List<String> temps = null;
    String[] tems = null;

    HashMap<String, Integer> geneIndexMap = new HashMap<>();
    int[] homosite;
    int[] hetersite;
    double heterozygosity;

    public VCFutils(String[] args) {
    }

    public void getHeter(String input, String output) {
        this.inputfile = input;
        this.outputfile = output;
        List<String> samplelist = new ArrayList<>();
        BufferedReader br;
        if (input.endsWith("gz")) {
            br = IOUtils.getTextGzipReader(inputfile);
        } else {
            br = IOUtils.getTextReader(inputfile);
        }
        BufferedWriter bw = IOUtils.getTextWriter(outputfile);
        try {
            while ((temp = br.readLine()) != null) {
                if (temp.startsWith("##")) continue;
                if (temp.startsWith("#C")) {
                    temps = PStringUtils.fastSplit(temp);
                    homosite = new int[temps.size() - 9];
                    hetersite = new int[temps.size() - 9];
                    for (int i = 9; i < temps.size(); i++) {
                        geneIndexMap.put(temps.get(i), i);
                        samplelist.add(temps.get(i));
                        homosite[i - 9] = 0;
                        hetersite[i - 9] = 0;
                    }
                    continue;
                }
                temps = PStringUtils.fastSplit(temp);
                for (int i = 9; i < temps.size(); i++) {
                    tems = temps.get(i).split(":");
                    if (tems[0].equals("./.")) continue;
                    if ((Integer.parseInt(tems[1].split(",")[0]) + Integer.parseInt(tems[1].split(",")[1])) < 2)
                        continue;
//                    System.out.println(tems[0]);
                    switch (tems[0]) {
                        case "1/1":
                            homosite[i - 9] += 1;
                            break;
                        case "0/0":
                            homosite[i - 9] += 1;
                            break;
                        case "0/1":
                            hetersite[i - 9] += 1;
                            break;
                    }
                }
            }
            br.close();
            DecimalFormat decfor = new DecimalFormat("0.0000");
            for (int i = 0; i < samplelist.size(); i++) {
//                System.out.println(hetersite[i]);
                heterozygosity = (double) hetersite[i] / (hetersite[i] + homosite[i]);
//                System.out.println(heterozygosity);
                bw.write(samplelist.get(i) + "\t" + decfor.format(heterozygosity) + "\n");
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void getHeterozygosity(String VCF, String output) {
        String input = new File(VCF).getAbsolutePath();
        GenotypeGrid g = new GenotypeGrid(input, GenoIOFormat.VCF);
        BufferedWriter bw = IOUtils.getTextWriter(output);
        String[] names = g.getTaxaNames();
        int homosite = 0;
        int hetersite = 0;
        double heterozygosity = 0;
        try {
            for (int i = 0; i < names.length; i++) {
                hetersite = g.getHeterozygoteNumberByTaxon(i);
                homosite = g.getHomozygoteNumberByTaxon(i);
                heterozygosity = (double) hetersite / (hetersite + homosite);
                bw.write(names[i] + "\t" + hetersite + "\t" + homosite + "\t" + heterozygosity + "\n");
            }
            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void getIBS(String VCF1, String VCF2, String outputfile) {
        String vcf1 = new File(VCF1).getAbsolutePath();
        String vcf2 = new File(VCF2).getAbsolutePath();
        String out = new File(outputfile).getAbsolutePath();
        GenotypeGrid g1 = new GenotypeGrid(vcf1, GenoIOFormat.VCF);
        GenotypeGrid g2 = new GenotypeGrid(vcf2, GenoIOFormat.VCF);
        GenotypeGrid g = GenotypeOperation.mergeGenotypesByTaxon(g1, g2);
        SumTaxaDivergence std = new SumTaxaDivergence(g);
        std.writeDxyMatrix(out, IOFileFormat.Text);
    }


}
