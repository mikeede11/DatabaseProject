package edu.yu.dbimpl.buffer;

import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.file.FileMgrBase;
import edu.yu.dbimpl.log.LogMgr;

import java.io.File;

public class BufferTester {
    public static void main(String[] args) {
        final int BLOCK_SIZE = 256;
        FileMgrBase manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
        LogMgr logger = new LogMgr(manager, "logFile.txt");
        BufferMgrBase bMgr = new BufferMgr(manager,logger,2, 10000);
        System.out.println(bMgr.available());//true
        manager.append("data0.txt");
        BufferBase buff = bMgr.pin(new BlockId("data0.txt", 0));
        assert buff != null;
        assert buff.contents() != null;
        buff.contents().setString(0, "Hello this is user data!!!!");
        buff.setModified(1, 1);
        bMgr.unpin(buff);
        bMgr.flushAll(1);
        buff = bMgr.pin(new BlockId("data0.txt", 0));
        System.out.println(buff.contents().getString(0));//should be Hello this is user data!!!!
    }
}
