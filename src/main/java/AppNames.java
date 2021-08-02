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
     * Parsing samples according barcodes.
     */
    Parsing ("Parsing"),

    /**
     * Quality control.
     */
    QC ("QC"),

    /**
     * Alignment of fq file
     */
    Alignment ("Alignment"),

    /**
     * The validation of samples with DNA and RNA-seq data.
     */
    SampleValidation ("SampleValidation"),

    /**
     * Quantification of gene expression.
     */
    Counting ("Counting"),

    /**
     * Genotyping pipeline of FastCall2
     */
    CountTable ("CountTable");

    public final String name;

    AppNames(String name) {
        this.name = name;
    }

    public String getName () {
        return name;
    }
}
