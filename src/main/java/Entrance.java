/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;

import app.*;
import app.Alignment;
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

    String inputFile = null;
    String outputFileDirS=null;
    String sampleInformationFileS = null;
    String library = null;
    int threads = 32;
    String GTFDir = null;
    String QCmethod = null;
    String readsNumber = null;

    public Entrance (String[] args) {
        this.createOptions();
        this.retrieveParameters (args);
    }

    @Override
    public void createOptions() {
        options = new Options();
        options.addOption("a", true, "App. e.g. -a Parsing");
        options.addOption("f", true, "Parameter file path of an app. e.g. parameter_Alignment.txt");
        options.addOption("i", true, "-inputFile /User/bin/");
        options.addOption("o", true, "-outputFileDirS /User/bin/");
        options.addOption("s", true, "-sampleInformationFileS /User/bin/");
        options.addOption("l", true, "-library /User/bin/");
        options.addOption("anno", true, "-anno /User/bin/");
        options.addOption("t", true, "-t 32");
        options.addOption("m",true,"method mean or median");
        options.addOption("r",true,"readsNumber");
    }

    @Override
    public void retrieveParameters(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine line = parser.parse(options, args);
            app = line.getOptionValue("a");
            parameterPath = line.getOptionValue("f");

            if( line.hasOption( "i" ) ) {
                inputFile =line.getOptionValue("i");
            }
            if( line.hasOption( "o" ) ) {
                outputFileDirS=line.getOptionValue("o");
            }
            if( line.hasOption( "s" ) ) {
                sampleInformationFileS=line.getOptionValue("s");
            }
            if( line.hasOption( "l" ) ) {
                library=line.getOptionValue("l");
            }
            if( line.hasOption( "anno")){
                GTFDir=line.getOptionValue("anno");
            }
            if( line.hasOption( "t")){
                threads=Integer.parseInt(line.getOptionValue("t"));
            }
            if(line.hasOption("m")){
                QCmethod=line.getOptionValue("m");
            }
            if (line.hasOption("r")){
                readsNumber=line.getOptionValue("r");
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
            String[] news = {this.inputFile, this.outputFileDirS, this.sampleInformationFileS, this.library};
            new Parsing(news);
        }
        else if (app.equals(AppNames.QC.getName())) {
            String[] news ={this.inputFile,this.QCmethod,this.readsNumber};
            new QC(news);
        }
        else if (app.equals(AppNames.Alignment.getName())) {
            new Alignment(this.parameterPath);
        }
        else if (app.equals(AppNames.SampleValidation.getName())) {
            String[] news = {};
            new SampleValidation(this.parameterPath);
//            new SampleValidation(news);
        }
        else if (app.equals(AppNames.Counting.getName())) {
            String[] news = {this.inputFile, this.GTFDir,String.valueOf(this.threads)};
            new Counting(news);
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
        sb.append("It uses two options to run its apps. \"-a\" is used to select an app. \"-f\" is used to provide a parameter file of an app.\n");
        sb.append("e.g. The command line usage of the app SiPAS-tools is: ");
        sb.append("java -Xmx100g -jar SiPAS-tools.jar -a Parsing -f parameter_parsing.txt > log.txt &\n");
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











