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
        try{
            String command = "top -bn1 -n 1 | grep "+arg+" | wc -l ";
            String [] cmdarry ={"/bin/bash","-c",command};
            Process p =Runtime.getRuntime().exec(cmdarry,null);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String temp = null;
            while ((temp = br.readLine()) != null) {
                currentThreads=Integer.parseInt(temp);
            }
            p.waitFor();
        }
        catch (Exception ex){
            ex.getStackTrace();
        }
        return currentThreads;
    }
}
