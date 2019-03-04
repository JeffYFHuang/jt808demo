package com.liteon.jt808app;

import com.liteon.javacint.logging.Logger;
import java.util.Timer;
import java.util.TimerTask;
public class App {
    public static void main(String[] argv) {
        String host = argv[0];
        int port = Integer.parseInt(argv[1]);
        jt808App jt808 = new jt808App(host, port);
        jt808.connectToServer(host, port);
        long freeMem = 0;
        Runtime r = Runtime.getRuntime();
        while (true) {
            try
            {
                Thread.sleep(5000);
            }
            catch(InterruptedException ex)
            {
                Thread.currentThread().interrupt();
            }
 
            if (jt808.isClosed()) {
               Logger.log("free memory before connectToServer: " + freeMem);
               jt808.connectToServer(host, port);
               Logger.log("free memory after connectToServer: " + freeMem);
               r.gc();
               freeMem = r.freeMemory();
               Logger.log("free memory after running gc():    " + freeMem);
            }
        }
    }
}
