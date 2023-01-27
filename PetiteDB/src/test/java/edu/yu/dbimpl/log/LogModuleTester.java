package edu.yu.dbimpl.log;

import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.file.Page;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import static org.junit.Assert.*;


import java.io.File;
import java.io.PushbackInputStream;
import java.net.PortUnreachableException;
import java.nio.ByteBuffer;

public class LogModuleTester {
    private final int BLOCK_SIZE = 4096;


    @Test
    public void microTest(){//append one log and iterate test
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");

        String logMsg = "This is a log message :)";
        logMgr.append(logMsg.getBytes());
        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        assertEquals(logMsg, new String(it.next()));
    }

    @Test
    public void nanoTest1(){
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");
        String s = "Length7";
        byte[] bs = s.getBytes();
        byte[] b = ByteBuffer.allocate(15).put(bs, 0, bs.length).putInt(42).array();
        logMgr.append(b);
    }

    @Test
    public void nanoTest2(){
        byte[] s1 = "7777777".getBytes();
        int i = 42;
        byte[] s2 = "88888888".getBytes();
        System.out.println(Page.maxLength(s1.length));
        System.out.println(Page.maxLength(s2.length));
        byte[] b = ByteBuffer.allocate(27).put(s1, 0, s1.length).putInt(42).put(s2, 0, s2.length).array();

        Page p = new Page(b);
    }

    @Test
    public void appendMultipleLogsWithinABlockAndIterateTest(){
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");
        String logMessage = "This is a log message :)";//24
        int sizeOfLogRec = Page.maxLength(logMessage.length());//28 b/c of int
        int logsThatCanFitInABlock = (BLOCK_SIZE / sizeOfLogRec) - 15; // - 5 just to be safe

        for (int i = 0; i < logsThatCanFitInABlock; i++) {
            logMgr.append((logMessage + i).getBytes());
    }
        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = logsThatCanFitInABlock - 1; i >= 0; i--) {
            assertEquals((logMessage + i), new String(it.next()));
        }
    }
    @Test
    public void appendMultipleLogsSpanMultipleBlocksAndIterateTest(){
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");
        String logMessage = "This is a log message :)";//24
        int sizeOfLogRec = Page.maxLength(logMessage.length());//28 b/c of int
        int logsThatCanNOTFitInABlock = (BLOCK_SIZE / sizeOfLogRec) * 4; // - 5 just to be safe
        for (int i = 0; i < logsThatCanNOTFitInABlock; i++) {
            logMgr.append((logMessage + i).getBytes());
        }
        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = logsThatCanNOTFitInABlock - 1; i >= 0; i--) {
            assertEquals((logMessage + i), new String(it.next()));
        }
    }

    @Test
    public void flushBeforeAppendAndIterateTest(){
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");
        logMgr.flush(0);
        String logMsg = "This is a log message :)";
        logMgr.append(logMsg.getBytes());
        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        assertEquals(logMsg, new String(it.next()));

    }
    @Test
    public void appendLogFlushAndIterateTest(){
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");
        String logMsg = "This is a log message :)";
        logMgr.append(logMsg.getBytes());
        logMgr.flush(1);
        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        assertEquals(logMsg, new String(it.next()));

    }

    @Test
    public void appendLogsAndFlushBeforeAnAutomaticFlushAndIterateTest(){
        FileMgr manager = new FileMgr(new File("dbDirectory"), 70);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");
        String logMsg = "This is a log message :)";
        logMgr.append(logMsg.getBytes());
        logMgr.append(logMsg.getBytes());
        logMgr.flush(0);
        logMgr.append(logMsg.getBytes());

        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = 0; i < 3; i++) {
            assertEquals(logMsg, new String(it.next()));
        }
    }

    @Test
    public void appendLogsAndFlushAfterAnAutomaticFlushAndIterateTest(){
        FileMgr manager = new FileMgr(new File("dbDirectory"), 70);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");
        String logMsg = "This is a log message :)";
        logMgr.append(logMsg.getBytes());
        logMgr.append(logMsg.getBytes());
        logMgr.append(logMsg.getBytes());
        logMgr.flush(2);

        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = 0; i < 3; i++) {
            assertEquals(logMsg, new String(it.next()));
        }
    }
    @Ignore
    @Test
    public void appendThousandsOfLogsAndIterateTest(){
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, "templogFile.txt");
        String logMessage = "This is a log message :)";//24
        int sizeOfLogRec = Page.maxLength(logMessage.length());//28 b/c of int
        int logsThatCanNOTFitInABlock = (BLOCK_SIZE / sizeOfLogRec) * 1000; // - 5 just to be safe
        for (int i = 0; i < logsThatCanNOTFitInABlock; i++) {
            logMgr.append((logMessage + i).getBytes());
        }
        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = logsThatCanNOTFitInABlock - 1; i >= 0; i--) {
            assertEquals((logMessage + i), new String(it.next()));
        }
    }

    /**
    * NOTE: naming the log files with the prefix "temp" does not ruin these tests
    * (which you may have thought since they are testing how the logMgr will work with
    * a pre-existing log file. If the temp files are deleted it would defeat the purpose
    * of these tests) since temp files are deleted only with the creation of a new
    * fileManager and not a log manager
    * */
    @Test
    public void logTestsWithAPreExistingEmptyLogFile(){
        String file = "templogFile.txt";
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, file);
        logMgr = new LogMgr(manager, file);//pre-existing and empty

        String logMessage = "This is a log message :)";//24
        int sizeOfLogRec = Page.maxLength(logMessage.length());//28 b/c of int
        int logsThatCanNOTFitInABlock = (BLOCK_SIZE / sizeOfLogRec) * 4; // - 5 just to be safe
        for (int i = 0; i < logsThatCanNOTFitInABlock; i++) {
            logMgr.append((logMessage + i).getBytes());
        }
        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = logsThatCanNOTFitInABlock - 1; i >= 0; i--) {
            assertEquals((logMessage + i), new String(it.next()));
        }
    }

    @Test
    public void logTestsWithAPreExistingLogFileWithLogs(){
        String file = "templogFile.txt";
        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logMgr = new LogMgr(manager, file);
        String logMessage = "This is a log message :)";//24

        logMgr.append((logMessage + 1).getBytes());
        logMgr.append((logMessage + 2).getBytes());
        logMgr.append((logMessage + 3).getBytes());
        logMgr.flush(2);
        logMgr = new LogMgr(manager, file);
        logMgr.append((logMessage + 4).getBytes());
        logMgr.append((logMessage + 5).getBytes());
        logMgr.append((logMessage + 6).getBytes());

        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = 6; i > 0; i--) {
            assertEquals((logMessage + i), new String(it.next()));
        }

    }

    @Test
    public void logTestsWithAPreExistingLogFileWithTheLastBlockJustFilled(){
        String file = "templogFile.txt";
        FileMgr manager = new FileMgr(new File("dbDirectory"), 70);
        LogMgr logMgr = new LogMgr(manager, file);
        String logMessage = "This is a log message :)";//24

        logMgr.append((logMessage + 1).getBytes());
        logMgr.append((logMessage + 2).getBytes());

        logMgr.flush(2);
        logMgr = new LogMgr(manager, file);

        logMgr.append((logMessage + 3).getBytes());
        logMgr.append((logMessage + 4).getBytes());

        LogIterator it = (LogIterator) logMgr.iterator();// new LogIterator(manager, new BlockId("logFile.txt", manager.length("logFile.txt") - 1));
        for (int i = 4; i > 0; i--) {
            assertEquals((logMessage + i), new String(it.next()));
        }
    }

    //tests that deal with logging when there already is a log file
    //Case A: nothing
    //Case B: something assumption: (user has to give the correct block (last block))
    //Case C: A block that was just filled

    //tests that deal with page constructor - what even is that?

    //flush happens in a few circumstances
    //A - when we append but the ram is now full - so we append a new block and write that ram block to that block. increment the last block - C
    //B - when we construct a logMgr and it appends (creates) a log file. and then we append a few records to that block and flush
    //either b/c block is full or b/c we called it explicitly or wth iterator()
    //C - we read block from disk append a few logs and then either b/c its full or because flush/iteroator is explicitly called its flushed

    //In B and C what happens when its block 0 or when we do not actually fill it up
    //we shouldnt increment
    //but when we do fill it up with block 0 we should do what we do with all other blocks no?
}
