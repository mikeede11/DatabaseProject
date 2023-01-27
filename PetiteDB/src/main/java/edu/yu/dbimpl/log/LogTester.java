package edu.yu.dbimpl.log;

import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.file.FileMgrBase;

import java.io.File;

import static java.lang.Thread.sleep;

public class LogTester {
    public static void main(String[] args) throws InterruptedException {
        final int BLOCK_SIZE = 256;
        FileMgrBase manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logger = new LogMgr(manager, "logFile.txt");
        String logMessage = "This is a log message :)";
        int numOfrecords = 20;
        for (int i = 0; i < numOfrecords; i++) {
            logger.append((logMessage + i).getBytes());
        }
        LogIterator it = (LogIterator) logger.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = 0; i < 40; i++) {
            System.out.println(new String(it.next()));
        }
        //FIXED
    }
}
