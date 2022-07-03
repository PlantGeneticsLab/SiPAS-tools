package utils;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author Chris Yang
 * created 2022-06-23 19:46
 **/
public class FileUtils {

    public static double returnLineNumber(File file) {
        double line = 0;
        try {
            LineNumberReader lineNumberReader = new LineNumberReader(getInFile(file));
            lineNumberReader.skip(Long.MAX_VALUE);
            line = lineNumberReader.getLineNumber();
            System.out.println(line);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return line;
    }

    public static BufferedReader getInFile(File file) {
        BufferedReader br = null;
        try {
            if (file.getAbsolutePath().endsWith("gz")) {
                br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(file))));
            } else {
                br = new BufferedReader(new FileReader(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return br;
    }

    public static BufferedWriter getOutFile(File file) {
        BufferedWriter bw = null;
        try {
            if (file.getAbsolutePath().endsWith("gz")) {
                bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(file))));
            } else {
                bw = new BufferedWriter(new FileWriter(file));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bw;
    }

    public static LinkedList<Double> returnComponentAlignment(String inputFile) {
        LinkedList<Double> list = new LinkedList<>();
        BufferedReader br = getInFile(new File(inputFile));
        double MappedCount = 0.0;
        double UniqueMapped = 0.0;
        double MultiMapped = 0.0;
        double UnMapped = 0.0;
        try {
            String temp = null;
            int count = 0;
            for (int i = 0; i < 34; i++) {
                temp = br.readLine();
                count++;
                if (count == 9) {
                    MappedCount = Double.parseDouble(temp.split("\t")[1]);
                } else if (count == 10) {
                    UniqueMapped = Double.parseDouble(temp.split("\t")[1].split("%")[0]);
                } else if (count == 25 || count == 27) {
                    MultiMapped += Double.parseDouble(temp.split("\t")[1].split("%")[0]);
                } else if (count == 29 || count == 30 || count == 31) {
                    UnMapped += Double.parseDouble(temp.split("\t")[1].split("%")[0]);
                } else continue;
            }

            br.close();
            list.add(UniqueMapped);
            list.add(MultiMapped);
            list.add(UnMapped);
            list.add(MappedCount);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
}
