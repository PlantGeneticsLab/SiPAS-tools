/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;

import app.*;
import app.align;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import pgl.infra.utils.CLIInterface;


public class Entrance implements CLIInterface {
    Options options = new Options();
    HelpFormatter optionFormat = new HelpFormatter();
    String introduction = this.createIntroduction();
    String app = null;
    String parameterPath = null;
    String parameters = null;

    String inputFileDirS = null;
    String outputFileDirS=null;
    String sampleInformationFileS = null;
    String library = null;

    String GTFDir = null;

    public Entrance (String[] args) {
        this.createOptions();
        this.retrieveParameters (args);
    }

    @Override
    public void createOptions() {
        options = new Options();
        options.addOption("a", true, "App. e.g. -a parsing");
        options.addOption("f", true, "Parameter file path of an app. e.g. parameter_Alignment.txt");
        options.addOption("inputFileDirS", true, "-inputFileDirS /User/bin/");
        options.addOption("outputFileDirS", true, "-outputFileDirS /User/bin/");
        options.addOption("sampleInformationFileS", true, "-sampleInformationFileS /User/bin/");
        options.addOption("library", true, "-library /User/bin/");
    }

    @Override
    public void retrieveParameters(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            app = line.getOptionValue("a");
            parameterPath = line.getOptionValue("f");

            if( line.hasOption( "inputFileDirS" ) ) {
                inputFileDirS=line.getOptionValue("inputFileDirS");
            }
            if( line.hasOption( "outputFileDirS" ) ) {
                outputFileDirS=line.getOptionValue("outputFileDirS");
            }
            if( line.hasOption( "sampleInformationFileS" ) ) {
                sampleInformationFileS=line.getOptionValue("sampleInformationFileS");
            }
            if( line.hasOption( "library" ) ) {
                library=line.getOptionValue("library");
            }
            if( line.hasOption( "GTFDir")){
                GTFDir=line.getOptionValue("GTFDir");
            }
        }
        catch(Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
        if (app == null) {
            System.out.println("App does not exist");
            this.printIntroductionAndUsage();
            System.exit(0);
        }
        if (app.equals(AppNames.Parsing.getName())) {
            String[] news = {this.inputFileDirS, this.outputFileDirS, this.sampleInformationFileS, this.library};
            new parsing(news);
        }
        else if (app.equals(AppNames.QC.getName())) {
            new qc(this.parameterPath);
        }
        else if (app.equals(AppNames.Alignment.getName())) {
            new align(this.parameterPath);
        }
        else if (app.equals(AppNames.SampleValidation.getName())) {
            new sampleValidation(this.parameterPath);
        }
        else if (app.equals(AppNames.Counting.getName())) {
            String[] news = {this.inputFileDirS, this.GTFDir};
            new counting(news);
        }
        else {
            System.out.println("App does not exist");
            this.printIntroductionAndUsage();
            System.exit(0);
        }
        if (this.parameterPath == null) {
            System.out.println("Parametar file does not exist");
            this.printIntroductionAndUsage();
            System.exit(0);
        }
        File f = new File (this.parameterPath);
        if (!f.exists()) {
            System.out.println("Parametar file does not exist");
            this.printIntroductionAndUsage();
            System.exit(0);
        }
    }

    @Override
    public void printIntroductionAndUsage() {
        System.out.println("Incorrect options input. Program stops.");
        System.out.println(introduction);
    }

    @Override
    public String createIntroduction() {
        StringBuilder sb = new StringBuilder();
        sb.append("\nBioinformatic toolkits of RNA-seq data is designed to simplify its usage.\n");
        sb.append("It uses two options to run its apps. \"-a\" is used to select an app. \"-f\" is used to provide a parameter file of an app. \"-p\" is used to provide some parameters of an app \n");
        sb.append("e.g. The command line usage of the app SiPAS-tools is: ");
        sb.append("java -Xmx100g -jar SiPAS-tools.jar -a parsing -f parameter_parsing.txt > log.txt &\n");
        sb.append("\nAvailable apps in SiPAS-tools include,\n");
        for (int i = 0; i < AppNames.values().length; i++) {
            sb.append(AppNames.values()[i].getName()).append("\n");
        }
        sb.append("\nPlease visit https://github.com/PlantGeneticsLab/SiPAS-tools for details.\n");
        return sb.toString();
    }

    public static void main (String[] args) {
        new Entrance(args);
    }

}











