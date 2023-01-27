package edu.yu.dbimpl.tx;

import edu.yu.dbimpl.buffer.BufferMgr;
import edu.yu.dbimpl.buffer.BufferMgrBase;
import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.log.LogMgr;
import edu.yu.dbimpl.log.LogMgrBase;
import org.junit.Assert;
import org.junit.Test;

import java.awt.geom.PathIterator;
import java.io.File;
import java.util.Iterator;

import static java.lang.Thread.sleep;

public class TxModuleTester {

    private final int BLOCK_SIZE = 4096;

    FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);

    @Test
    public void basicLeffTest(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager,lm,2, 2000);
        manager.append("testfile.txt");
        TxBase tx1 = new Transaction(manager,lm,bm);
        BlockIdBase blk = new BlockId("testfile.txt", 1);
        tx1.pin(blk);
        boolean okToLog = false ;
        tx1.setInt ( blk , 80 , 1, okToLog );
        tx1.setString ( blk, 40, "one", okToLog);
        tx1.commit();

        final TxBase tx2 = new Transaction ( manager , lm , bm) ;
        tx2.pin( blk );
        final int intValue = tx2.getInt ( blk , 80) ;
        final String stringValue = tx2.getString ( blk , 40) ;

        Assert.assertEquals(1, intValue);
        Assert.assertEquals("one", stringValue);

        int newIntValue = intValue + 1;
        String newStringValue = stringValue + "!";
        // we definitely need to log these operations
        okToLog = true ;
        tx2.setInt (blk, 80, newIntValue, okToLog);//we have slock but we want to upgrade
        tx2.setString ( blk , 40 , newStringValue , okToLog );
        tx2.commit () ;

        final TxBase tx3 = new Transaction ( manager , lm , bm) ;
        tx3.pin( blk );

        Assert.assertEquals(newIntValue, tx3.getInt(blk,80));
        Assert.assertEquals(newStringValue, tx3.getString(blk, 40));

        tx3.setInt(blk, 80, 9999, okToLog);
        Assert.assertEquals(9999, tx3.getInt(blk, 80));
        tx3.rollback();

        final TxBase tx4 = new Transaction ( manager, lm, bm);
        tx4.pin ( blk );
        Assert.assertEquals(newIntValue,tx4.getInt(blk, 80));
        tx4.commit () ;

    }

    /**Tx Tests
     * Txs are logically atomic(do or do not) operations consisting of one or many actual modifications.
     * You can also test the rollback method - tests it two ways A) do a tx then rollback (already kind of done) B)
     * you can do the ISE tests for formailty but kind of trivial.
     * next would be to tests a txs pinning and buffer mgmnt pinning.
     * tests get int - value , get string - already sort of did
     * tests set int  - vaue, set string - already sort of did
     * test the above lines log records
     * size
     * append
     *
     * txNum uniqueness only matter during a runtime session or across runtime sessions? so A is only during runtime - effectively
     * **/

    @Test
    public void ensureUniqueTxNumsTest(){

    }

    /** To ensure this atomicity we ensure that whenever we logically executed a tx, we push
     * to disc and post a COMMIT log. This log tells that the operation has been completed. This commit behavior
     * can be tested by executing a Tx and ensuring that the bufferMgr has enough buffers
     * that it would not push its data to disc on its own. Commit the tx. Then clear the bufferMgr/reinstantiate it
     * this means that if commit() does not ensure that its tx's modifications are written to disc (and it was only
     * in the buffers of the bufferMgr - which we just wiped!) then That Txs will have been as if it never happened.
     * What we would hope for is that upon checking the values again A) that Txs change are reflected and B) the
     * the commit log record is there as well.**/
    @Test
    public void executeTXAndCommitTest (){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager,lm,10, 2000);
        manager.append("testfile.txt");
        TxBase tx1 = new Transaction(manager,lm,bm);
        BlockIdBase blk = new BlockId("testfile.txt", 1);
        tx1.pin(blk);
        boolean okToLog = false ;
        tx1.setInt ( blk , 80 , 1, okToLog );
        tx1.setString ( blk, 40, "one", okToLog);
        tx1.commit();

        final TxBase tx2 = new Transaction ( manager , lm , bm) ;
        int tx2Txnum = tx2.txnum();
        tx2.pin( blk );
        final int intValue = tx2.getInt ( blk , 80) ;
        final String stringValue = tx2.getString ( blk , 40) ;

        Assert.assertEquals(1, intValue);
        Assert.assertEquals("one", stringValue);

        int newIntValue = intValue + 1;
        String newStringValue = stringValue + "!";
        // we definitely need to log these operations
        okToLog = true ;
        tx2.setInt (blk, 80, newIntValue, okToLog);//we have slock but we want to upgrade
        tx2.setString ( blk , 40 , newStringValue , okToLog );
        tx2.commit () ;
        bm = new BufferMgr(manager,lm, 20, 2000);//reinstnatiate the pages being maintained will be lost

        final TxBase tx3 = new Transaction ( manager , lm , bm) ;
        tx3.pin( blk );
        final int actualIntValue = tx3.getInt ( blk , 80) ;
        final String actualStringValue = tx3.getString ( blk , 40) ;
        Assert.assertEquals(newIntValue, actualIntValue);
        Assert.assertEquals(newStringValue, actualStringValue);
        Iterator<byte[]> iterator = lm.iterator();
        iterator.next();//get past the start log for tx3
        Assert.assertEquals("COMMIT," + tx2Txnum, new String(iterator.next()));
    }

     /** Another way to test our atomicity is to execute a tx and NOT commit it
     * - specifically when the bufferMgr flushes it anyways. We should run a recover() algorithm and then with the
     * reinstantiated bufferMgr check to see the values and they should A) Not reflect that txs changes at all and
     * B) have a checkpoint log at the first(last) slot in the log file. This is important so that these actions arent
     * unnecesarily undone if a rollback was called - which i believe is impossible anyways or should be - yeah if you call a tx rollback
     * it will stop before it gets to a previous log of a tx w/ an identical num (b/c of restart) b/c it will have to hit its own start
     * before it gets to a tx w/ the same number actions because that tx will be behind a checkpoint and thus any new tx w/ same number of the current instance
     * will come after. **/
     @Test
     public void testRecoveryBehaviorForAnUnCommitedTXAndBufferMgrPushesToDiscAnyways(){
         LogMgrBase lm = new LogMgr(manager, "logfile.txt");
         BufferMgrBase bm = new BufferMgr(manager,lm,1, 2000);
         manager.append("testfile.txt");
         TxBase tx1 = new Transaction(manager,lm,bm);
         BlockIdBase blk = new BlockId("testfile.txt", 1);
         tx1.pin(blk);//ok one and only buffer is occupied
         boolean okToLog = false ;
         tx1.setInt ( blk , 80 , 1, okToLog );//set
         tx1.setString ( blk, 40, "one", okToLog);//set
         tx1.commit();

         final TxBase tx2 = new Transaction ( manager , lm , bm) ;
         tx2.pin( blk );//pins the same block literally no problem anyways also we commited above which unpins
         final int intValue = tx2.getInt ( blk , 80) ;
         final String stringValue = tx2.getString ( blk , 40) ;

         Assert.assertEquals(1, intValue);
         Assert.assertEquals("one", stringValue);

         int newIntValue = intValue + 1;
         String newStringValue = stringValue + "!";
         // we definitely need to log these operations
         okToLog = true ;
         tx2.setInt (blk, 80, newIntValue, okToLog);//we have slock but we want to upgrade
         tx2.setString ( blk , 40 , newStringValue , okToLog );
//         tx2.commit (); DO NOT COMMIT TX3 WILL FORCE THIS BLK1 TO GO TO DISC THEN AFTER WE WILL "SHUTDOWN" AND RECOVER()
         //AND SEE IF THE VALUE IN THIS BLK AND THESE OFFSETS ARE TX2'S VALUE OR TX 1 - THEY SHOULD BE TX1.
         //bm = new BufferMgr(manager,lm, 20, 2000);//reinstnatiate the pages being maintained will be lost

         final TxBase tx3 = new Transaction ( manager , lm , bm) ;
         BlockIdBase newBlk = new BlockId("testfile.txt", 2);
         tx3.pin( blk );
         bm = new BufferMgr(manager,lm,1, 2000);

         final TxBase tx4 = new Transaction ( manager , lm , bm) ;
         tx4.pin(blk);
         int tx4Txnum = tx4.txnum();

         tx4.recover();
         Assert.assertEquals(1, tx4.getInt(blk, 80));
         Assert.assertEquals("one", tx4.getString(blk, 40));
         Iterator<byte[]> iterator = lm.iterator();
         Assert.assertEquals("CHECKPOINT," + tx4Txnum, new String(iterator.next()));//get past the start log for tx3
     }

     /**do a tx and then many txs and then rollback test **/

    @Test
     public void rollbackTXAfterSubsequentTX(){
         LogMgrBase lm = new LogMgr(manager, "logfile.txt");
         BufferMgrBase bm = new BufferMgr(manager,lm,10, 2000);
         manager.append("testfile.txt");
         TxBase tx1 = new Transaction(manager,lm,bm);
         BlockIdBase blk = new BlockId("testfile.txt", 1);
         tx1.pin(blk);
         boolean okToLog = false ;
         tx1.setInt ( blk , 80 , 1, okToLog );
         tx1.setString ( blk, 40, "one", okToLog);
         tx1.commit();
//         value is one 1
         final TxBase tx2 = new Transaction ( manager , lm , bm) ;
         int tx2Txnum = tx2.txnum();
         tx2.pin( blk );//blk already in bufer
         final int intValue = tx2.getInt ( blk , 80) ;
         final String stringValue = tx2.getString ( blk , 40) ;

         int newIntValue = intValue + 1;
         String newStringValue = stringValue + "!";
         // we definitely need to log these operations
         okToLog = true ;
         tx2.setInt (blk, 80, newIntValue, okToLog);//we have slock but we want to upgrade
         tx2.setString ( blk , 40 , newStringValue , okToLog );
         //value one! 2 - this is logged - we have xlock - dont commit
//         tx2.commit () ;
         final TxBase tx3 = new Transaction ( manager , lm , bm) ;
         BlockIdBase newBlk= new BlockId(blk.fileName(), blk.number() + 1);
         tx3.pin( newBlk );
         okToLog = true ;
         tx3.setInt (newBlk, 80, 100, okToLog);//we have slock but we want to upgrade
         tx3.setString ( newBlk , 40 , "just doing a Tx!" , okToLog );
//         tx2.commit () ;final TxBase tx2 = new Transaction ( manager , lm , bm) ;
         final TxBase tx4 = new Transaction ( manager , lm , bm) ;
         BlockIdBase newBlk4= new BlockId(blk.fileName(), blk.number() + 2);
         tx4.pin( newBlk4 );
         okToLog = true ;
         tx4.setInt (newBlk4, 80, 100, okToLog);//we have slock but we want to upgrade
         tx4.setString ( newBlk4 , 40 , "just doing another Tx!" , okToLog );
         tx2.rollback();
         //tx4.unpin(newBlk4);
         tx4.pin(blk);
         Assert.assertEquals(1, tx4.getInt(blk, 80));
         Assert.assertEquals("one", tx4.getString(blk, 40));
         Iterator<byte[]> iterator = lm.iterator();
         Assert.assertEquals("ROLLBACK," + tx2Txnum, new String(iterator.next()));
         tx3.commit();
         tx4.commit();
     }
    /**Alot more tests desired like interleaving txs and seeing if concurrencyMgr gives locks and acces to right people
     * test wait functionality test recover with mutiple txs theres so much. but I would say if you do the concurrency tests this tx
     * module does show a base level funtionality and durability. and can move on to record.
     */

    /**RecoveryMgr Tests
     * In general from a Transaction API perspective it seems we have established
     * that commit(), rollback(), recover(), SetInt(), and SetString() all display basic
     * working functionality. I am happy with this. We can always do more thourough testing though.
     * like make sure rollback() rollsback ONLY its tx and NOTHING more etc,.
     **/
    /**ConcurrencyMgr Tests
     * These tests also test release() as well when they can
     * **/

    /** Same Block Tests **/
    @Test
    public void multipleTxsAquireSLockOnSameBlockTest(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager,lm,2, 2000);
        manager.append("testfile.txt");
        TxBase tx1 = new Transaction(manager,lm,bm);
        BlockIdBase blk = new BlockId("testfile.txt", 1);
        tx1.pin(blk);
        boolean okToLog = false ;
        tx1.setInt ( blk , 80 , 1, okToLog );
        tx1.setString ( blk, 40, "one", okToLog);
        tx1.commit();

        final TxBase tx2 = new Transaction ( manager , lm , bm) ;
        tx2.pin( blk );


        final TxBase tx3 = new Transaction ( manager , lm , bm) ;
        tx3.pin( blk );

        final TxBase tx4 = new Transaction ( manager , lm , bm) ;
        tx4.pin( blk );

        final int intValue3 = tx4.getInt ( blk , 80) ;
        final String stringValue3 = tx4.getString ( blk , 40);


        final int intValue2 = tx3.getInt ( blk , 80) ;
        final String stringValue2 = tx3.getString ( blk , 40);

        final int intValue = tx2.getInt ( blk , 80) ;
        final String stringValue = tx2.getString ( blk , 40) ;
        Assert.assertEquals(1, intValue);
        Assert.assertEquals("one", stringValue);
        Assert.assertEquals(1, intValue2);
        Assert.assertEquals("one", stringValue2);
        Assert.assertEquals(1, intValue3);
        Assert.assertEquals("one", stringValue3);
        tx2.commit();
        tx3.commit();
        tx4.commit();

    }

    @Test
    public void txObtainsXLockAndBlocksTxTryingToObtainSLockAndReadTest(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager,lm,2, 2000);
        manager.append("testfile.txt");
        TxBase tx1 = new Transaction(manager,lm,bm);
        BlockIdBase blk = new BlockId("testfile.txt", 1);
        tx1.pin(blk);
        boolean okToLog = false ;
        tx1.setInt ( blk , 80 , 1, okToLog );
        tx1.setString ( blk, 40, "one", okToLog);
        tx1.commit();


        final TxBase tx2 = new Transaction ( manager , lm , bm) ;
        tx2.pin( blk );
        okToLog = true;
        int expectedValue = 20;
        tx2.setInt(blk, 80, 1, okToLog);//this gets tx2 the XLOCK


//        final int[] actualValue = {};//tx3.getInt(blk, 80);//this may result in deadlock
        Thread t1 = new Thread((new Runnable() {
            @Override
            public void run() {
//                    sleep(1000);
                final TxBase tx3 = new Transaction ( manager , lm , bm) ;
                tx3.pin( blk );
                    Assert.assertEquals(expectedValue, tx3.getInt(blk, 80));
                    tx3.commit();
            }
        }));
        t1.start();//ok so tx2 has XLock. start tx3 and tx3 will try to get sLock - it should be blocked. main thread waits a bit to make sure that tx3 requests slock while tx2 still has slock(before commit). then does it and presumably gives up lock and tx3 will be able to obtain it and should read the expected value.
        try {
            sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        tx2.setInt(blk, 80, expectedValue, okToLog);
        tx2.commit();
        //now the thread above can execute
        //Assert.assertEquals(expectedValue, actualValue[0]);//tx3 should wait to read the val after tx2 changes it to 20

    }

    @Test
    public void txObtainsSLockAndBlocksTxTryingToObtainXlockAndChangeValueTest(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager,lm,2, 2000);
        manager.append("testfile.txt");
        TxBase tx1 = new Transaction(manager,lm,bm);
        BlockIdBase blk = new BlockId("testfile.txt", 1);
        tx1.pin(blk);
        boolean okToLog = false ;
        final int valueBeforeModification = 1;
        tx1.setInt ( blk , 80 , valueBeforeModification, okToLog );
        tx1.setString ( blk, 40, "one", okToLog);
        tx1.commit();

        final int valueAfterModification = 36;
        final TxBase tx2 = new Transaction ( manager , lm , bm) ;
        tx2.pin( blk );
        okToLog = true;
        int expectedValue = 20;
        tx2.getInt(blk, 80);//this gets tx2 the SLOCK

        final TxBase tx3 = new Transaction ( manager , lm , bm) ;
        tx3.pin( blk );

//        final int[] actualValue = {};//tx3.getInt(blk, 80);//this may result in deadlock
        Thread t1 = new Thread((new Runnable() {
            @Override
            public void run() {
//                    sleep(1000);

                tx3.setInt(blk, 80, valueAfterModification, true);//waits for XLock
                tx3.commit();
            }
        }));
        t1.start();//ok so tx2 has slock. now t1 will start and tx3 will request an xlock. it will be blocked b/c tx2 has slock.
        try {
            sleep(500);//we add this to make sure tx3 will request an xlock while the main thread (tx2) still has slock
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(valueBeforeModification, tx2.getInt(blk, 80));
        tx2.commit();
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final TxBase tx4 = new Transaction ( manager , lm , bm) ;
        tx4.pin( blk );
        Assert.assertEquals(valueAfterModification, tx4.getInt(blk, 80));

    }

    @Test
    public void txObtainsXLockAndBlocksTxTryingToObtainXLock(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager,lm,2, 2000);
        manager.append("testfile.txt");
        TxBase tx1 = new Transaction(manager,lm,bm);
        BlockIdBase blk = new BlockId("testfile.txt", 1);
        tx1.pin(blk);
        boolean okToLog = false ;
        final int valueBeforeModification = 1;
        tx1.setInt ( blk , 80 , valueBeforeModification, okToLog );
        tx1.setString ( blk, 40, "one", okToLog);
        tx1.commit();

        final int valueAfterModification = 36;
        final TxBase tx2 = new Transaction ( manager , lm , bm) ;
        tx2.pin( blk );
        okToLog = true;
        int firstModValue = 10;
        int secondModValue = 20;
        tx2.setInt(blk, 80, firstModValue, okToLog);//this gets tx2 the XLOCK

        final TxBase tx3 = new Transaction ( manager , lm , bm) ;
        tx3.pin( blk );

//        final int[] actualValue = {};//tx3.getInt(blk, 80);//this may result in deadlock
        Thread t1 = new Thread((new Runnable() {
            @Override
            public void run() {
//                    sleep(1000);

                tx3.setInt(blk, 80, secondModValue, true);//waits for XLock
                tx3.commit();
            }
        }));
        t1.start();//ok so tx2 has Xlock. now t1 will start and tx3 will request an xlock. it will be blocked b/c tx2 has Xlock.
        try {
            sleep(500);//we add this to make sure tx3 will request an xlock while the main thread (tx2) still has slock
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Assert.assertEquals(firstModValue, tx2.getInt(blk, 80));//test if tx3 was succesfully blocked and is waiting for the xlock to do its modification.
        tx2.commit();//release tx2's XLock. now tx3 should be woken up and do its mod
        try {
            sleep(2000);//wait a bit to make sure it happens
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        final TxBase tx4 = new Transaction ( manager , lm , bm) ;
        tx4.pin( blk );
        Assert.assertEquals(secondModValue, tx4.getInt(blk, 80));//test if now tx3 was woken up and was able to aquire the xlock to execute its modification
    }

    /** Same Block Tests **/

    @Test
    public void txObtainsXLockAndOtherTxObtainsSLockOnDifBlocksTest(){
        //1st tx on both blocks
//        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
//        BufferMgrBase bm = new BufferMgr(manager,lm,2, 2000);
//        manager.append("testfile.txt");
//        TxBase tx1 = new Transaction(manager,lm,bm);
//        BlockIdBase blk = new BlockId("testfile.txt", 1);
//        tx1.pin(blk);
//        boolean okToLog = false ;
//        final int valueBeforeModification = 1;
//        tx1.setInt ( blk , 80 , valueBeforeModification, okToLog );
//        tx1.setString ( blk, 40, "one", okToLog);
//        tx1.commit();
//
//        TxBase tx2 = new Transaction(manager,lm,bm);
//        BlockIdBase blk2 = new BlockId("testfile.txt", 2);
//        tx2.pin(blk2);
//        tx2.setInt ( blk2 , 80 , valueBeforeModification, okToLog );
//        tx2.setString ( blk2, 40, "one", okToLog);
//        tx2.commit();
//        //then try to modify both blocks interleaving the txs
//
//        final Transaction tx3 = new Transaction(manager,lm, bm);
//        tx3.
        //test both

    }

    @Test
    public void txsObtainsXLocksOnDifBlocksTest(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager,lm,2, 2000);
        manager.append("testfile.txt");
        TxBase tx1 = new Transaction(manager,lm,bm);
        BlockIdBase blk = new BlockId("testfile.txt", 1);
        tx1.pin(blk);
        boolean okToLog = false ;
        tx1.setInt ( blk , 80 , 1, okToLog );
        tx1.setString ( blk, 40, "one", okToLog);
        tx1.unpin(blk);
//        tx1.pin(blk);
        tx1.getInt(blk, 80);
//        tx1.unpin(blk);

    }

    @Test
    public void repTest(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager,lm,2, 2000);
        manager.append("testfile.txt");
        TxBase tx1 = new Transaction(manager,lm,bm);
        BlockIdBase blk = new BlockId("testfile.txt", 1);
        tx1.pin(blk);
        boolean okToLog =  false;
        tx1.setInt ( blk , 80 , 1, okToLog );
        tx1.setString ( blk, 40, "one", okToLog);
        tx1.unpin(blk);
        tx1.commit();

        TxBase tx2 = new Transaction(manager,lm,bm);
        tx2.pin(blk);
        okToLog = true;
        Assert.assertEquals(1, tx2.getInt(blk, 80));
        Assert.assertEquals("one", tx2.getString(blk, 40));
        tx2.setInt(blk, 80, 2, okToLog);
        tx2.setString(blk, 40, "one!", okToLog);
        tx2.commit();

        TxBase tx3 = new Transaction(manager,lm,bm);
        tx3.pin(blk);
        Assert.assertEquals(2, tx3.getInt(blk, 80));
        Assert.assertEquals("one!", tx3.getString(blk, 40));
        tx3.setInt(blk, 80, 9999, okToLog);
        Assert.assertEquals(9999, tx3.getInt(blk, 80));
        tx3.rollback();

        TxBase tx4 = new Transaction(manager,lm,bm);
        tx4.pin(blk);
        Assert.assertEquals(2, tx4.getInt(blk, 80));
        Assert.assertEquals("one!", tx4.getString(blk, 40));

    }



}
