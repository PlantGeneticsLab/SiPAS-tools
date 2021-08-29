package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * command line for threadpool
 *
 * @ author: yxh
 * @ created: 2021-08-09 : 9:05 AM
 */
public class Command implements Callable<Command> {
    String command = null;
    File dir = null;

    public Command(String command, File dir) {
        this.command = command;
        this.dir = dir;
    }

    @Override
    public Command call() {
        try {
            System.out.println(command);
            String[] cmdarry1 = {"/bin/bash", "-c", command};
            Process p = Runtime.getRuntime().exec(cmdarry1, null,dir);
            p.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return this;
    }
}
