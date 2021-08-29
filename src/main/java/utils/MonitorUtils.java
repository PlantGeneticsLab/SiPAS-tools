package utils;

/**
 * Common sorting utils with Array
 *
 * @ author: JunXu
 * @ created: 2021-08-10 : 10:03 PM
 */

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class MonitorUtils {
    /**
     * @param arg
     * get the current threads
     * @return
     */
    public static int monitor (String arg){
        int currentThreads =0;
        String command = "ps aux | grep "+arg+" | wc -l ";
        try{
            String [] cmdarry ={"/bin/bash","-c",command};
            Process p =Runtime.getRuntime().exec(cmdarry,null);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                currentThreads=(Integer.parseInt(temp.replaceAll(" ",""))-1)/2;
            }
            p.waitFor();br.close();
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
        return currentThreads;
    }
    /**
     * @param user task
     * get the current threads
     * @return
     */
    public static int monitor (String user, String task){
        int currentThreads =0;
        try{
            String command = "ps aux | grep "+user+" | grep "+task+" | wc -l ";
            String [] cmdarry ={"/bin/bash/","-c",command};
            Process p =Runtime.getRuntime().exec(cmdarry,null);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                System.out.println(temp);
                currentThreads=(Integer.parseInt(temp.replaceAll(" ",""))-1)/2;
            }
            p.waitFor();
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
        return currentThreads;
    }
}
