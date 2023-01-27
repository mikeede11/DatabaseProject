package edu.yu.dbimpl.buffer;

import edu.yu.dbimpl.file.*;
import edu.yu.dbimpl.log.LogMgr;
import org.junit.Assert;
import org.junit.Test;
import org.junit.Assert.*;

import java.io.File;
import java.util.Properties;

import static java.lang.Thread.sleep;

public class BufferModuleTester {
    private final int BLOCK_SIZE = 4096;

    FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
    /*Starting from scratch tests - no data in file structure */
    @Test
    public void pinAndModifyBlockWhenBlockIsNotAssignedToAnyBufferAndThereAreAvailableBuffersTest(){
        LogMgr logger = new LogMgr(manager, "logFile.txt");
        BufferMgrBase bMgr = new BufferMgr(manager,logger,2, 10000);
        Assert.assertTrue(bMgr.available() > 0);//true
        manager.append("data0.txt");//a user file already on disc
        BlockIdBase blk = new BlockId("data0.txt", 0);
        BufferBase buff = bMgr.pin(blk);//pin that file and block abd read it into a buffer
        assert buff != null;
        assert buff.contents() != null;
        String s = "Hello this is user data!!!!";
        buff.contents().setString(0,s );
        buff.setModified(1, 1);
        PageBase p = new Page(manager.blockSize());
//        bMgr.unpin(buff);//unnecessary
        bMgr.flushAll(1);
        //buff = bMgr.pin(new BlockId("data0.txt", 0));
        manager.read(blk,p);
        Assert.assertEquals(s,p.getString(0));
        Assert.assertEquals(s, buff.contents().getString(0));//should be Hello this is user data!!!!
    }

    @Test
    public void pinAndModifyBlockWhenBlockIsAlreadyAssignedToABufferThatIsPinnedTest(){
        LogMgr logger = new LogMgr(manager, "logFile.txt");
        BufferMgrBase bMgr = new BufferMgr(manager,logger,3, 10000);
        Assert.assertTrue(bMgr.available() > 0);//true
        manager.append("data1.txt");//create the file
        BlockIdBase blk = new BlockId("data1.txt", 0);
        BufferBase buff = bMgr.pin(blk);//pin that file and block abd read it into a buffer
        assert buff != null;
        assert buff.contents() != null;
        String s = "Hello this is user data!!!!";
        buff.contents().setString(0,s );//modify it
        buff.setModified(1, 1);
        bMgr.flushAll(1);//save changes to disc
        buff = bMgr.pin(blk);//pin it again - it should use the buffer that already is assigned to that block. also that block is still pinned.
        String modS = "Hello This is the MODIFIED data";
        buff.contents().setString(0,modS );
        buff.setModified(2, 1);
        bMgr.flushAll(2);
        Page p = new Page(manager.blockSize());
        manager.read(blk,p);
        Assert.assertEquals(modS,p.getString(0));
        Assert.assertEquals(modS, buff.contents().getString(0));//should be Hello this is user data!!!!
    }

    @Test
    public void pinAndModifyBlockWhenBlockIsAlreadyAssignedToABufferThatIsUnpinnedTest(){
        LogMgr logger = new LogMgr(manager, "logFile.txt");
        BufferMgrBase bMgr = new BufferMgr(manager,logger,3, 10000);
        Assert.assertTrue(bMgr.available() > 0);//true
        manager.append("data1.txt");//create the file
        BlockIdBase blk = new BlockId("data1.txt", 0);
        BufferBase buff = bMgr.pin(blk);//pin that file and block abd read it into a buffer
        assert buff != null;
        assert buff.contents() != null;
        String s = "Hello this is user data!!!!";
        buff.contents().setString(0,s );//modify it
        buff.setModified(1, 1);
        bMgr.flushAll(1);//save changes to disc
        bMgr.unpin(buff);
        buff = bMgr.pin(blk);//pin it again - it should use the buffer that already is assigned to that block. also that block is still pinned.
        String modS = "Hello This is the MODIFIED data";
        buff.contents().setString(0,modS );
        buff.setModified(2, 1);
        bMgr.flushAll(2);
        Page p = new Page(manager.blockSize());
        manager.read(blk,p);
        Assert.assertEquals(modS,p.getString(0));
        Assert.assertEquals(modS, buff.contents().getString(0));//should be Hello this is user data!!!!
    }

    /**This test establishes that the producer consumer model works. It also establishes that data will be written
     * when an unpinned buffer is having an old modified block replaced with a new block. This happens with having
     * to explicitly call flushAll(). These tests overall prove the four main cases and basic functionality.
     */
    @Test
    public void pinAndModifyBlockWhenBlockIsNotAssignedToAnyBufferAndNoBufferIsUnpinnedTest(){
        LogMgr logger = new LogMgr(manager, "logFile.txt");
        BufferMgrBase bMgr = new BufferMgr(manager,logger,3, 10000);
        Assert.assertTrue(bMgr.available() > 0);//true
        manager.append("data2.txt");//create the file
        manager.append("data3.txt");//create the file
        manager.append("data4.txt");//create the file
        manager.append("data5.txt");//create the file
        BlockIdBase blk2 = new BlockId("data2.txt", 0);
        BlockIdBase blk3 = new BlockId("data3.txt", 0);
        BlockIdBase blk4 = new BlockId("data4.txt", 0);
        BlockIdBase blk5 = new BlockId("data5.txt", 0);
        BufferBase buff1 = bMgr.pin(blk2);//pin that file and block abd read it into a buffer
        BufferBase buff2 = bMgr.pin(blk3);//pin that file and block abd read it into a buffer
        BufferBase buff3 = bMgr.pin(blk4);//pin that file and block abd read it into a buffer
        Assert.assertEquals(0, bMgr.available());
        String s = "Hello this is user data!!!!";
        buff1.contents().setString(0,s );//modify it
        buff1.setModified(1, 1);
        buff2.contents().setString(0,s );//modify it
        buff2.setModified(1, 1);
        buff3.contents().setString(0,s );//modify it
        buff3.setModified(1, 1);
        //bMgr.flushAll(1);//save changes to disc
        //no buffers available
        //execute a thread here that waits for a few seconds and then unpins the second buffer
        Thread t1 = new Thread((new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                    bMgr.unpin(buff2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        t1.start();//this should start
        BufferBase buff4 = bMgr.pin(blk5);//This block is not assigned to any buffer and no buffers are available - we need to wait
        //it will wait we need a thread now to unpin say blk 2 (presumably it will flush those contents) and then give it to this guy

        String modS = "Hello This is the MODIFIED data";
        buff4.contents().setString(0,modS );
        buff4.setModified(2, 1);
        bMgr.flushAll(2);
        Page p = new Page(manager.blockSize());
        Page p2 = new Page(manager.blockSize());
        manager.read(blk3,p);
        manager.read(blk5,p2);
        Assert.assertEquals(s,p.getString(0));
        //Assert.assertEquals(s, buff2.contents().getString(0));//should be Hello this is user data!!!!
        Assert.assertEquals(modS,p2.getString(0));
        Assert.assertEquals(modS, buff4.contents().getString(0));//should be Hello this is user data!!!!
    }

    @Test(expected = BufferAbortException.class)
    public void noBufferAvailableAndTimeoutReachedTest(){
        LogMgr logger = new LogMgr(manager, "logFile.txt");
        BufferMgrBase bMgr = new BufferMgr(manager,logger,3, 400);
        Assert.assertTrue(bMgr.available() > 0);//true
        manager.append("data2.txt");//create the file
        manager.append("data3.txt");//create the file
        manager.append("data4.txt");//create the file
        manager.append("data5.txt");//create the file
        BlockIdBase blk2 = new BlockId("data2.txt", 0);
        BlockIdBase blk3 = new BlockId("data3.txt", 0);
        BlockIdBase blk4 = new BlockId("data4.txt", 0);
        BlockIdBase blk5 = new BlockId("data5.txt", 0);
        BufferBase buff1 = bMgr.pin(blk2);//pin that file and block abd read it into a buffer
        BufferBase buff2 = bMgr.pin(blk3);//pin that file and block abd read it into a buffer
        BufferBase buff3 = bMgr.pin(blk4);//pin that file and block abd read it into a buffer
        Assert.assertEquals(0, bMgr.available());
        String s = "Hello this is user data!!!!";
        buff1.contents().setString(0,s );//modify it
        buff1.setModified(1, 1);
        buff2.contents().setString(0,s );//modify it
        buff2.setModified(1, 1);
        buff3.contents().setString(0,s );//modify it
        buff3.setModified(1, 1);
        //bMgr.flushAll(1);//save changes to disc
        //no buffers available
        //execute a thread here that waits for a few seconds and then unpins the second buffer
        Thread t2 = new Thread((new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(5000);
                    bMgr.unpin(buff2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }));
        t2.start();//this should start
        BufferBase buff4 = bMgr.pin(blk5);//This block is not assigned to any buffer and no buffers are available - we need to wait
        //it will wait we need a thread now to unpin say blk 2 (presumably it will flush those contents) and then give it to this guy

    }



}
