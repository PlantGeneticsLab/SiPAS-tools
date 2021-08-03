/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Available apps in SiPAS-tools
 */
public enum AppNames implements Comparable <AppNames> {
    /**
     * parsing samples according barcodes.
     */
    Parsing ("parsing"),

    /**
     * Quality control.
     */
    QC ("qc"),

    /**
     * align of fq file
     */
    Alignment ("align"),

    /**
     * The validation of samples with DNA and RNA-seq data.
     */
    SampleValidation ("sampleValidation"),

    /**
     * Quantification of gene expression and get coutTable.
     */
    Counting ("counting");

    public final String name;

    AppNames(String name) {
        this.name = name;
    }

    public String getName () {
        return name;
    }
}
