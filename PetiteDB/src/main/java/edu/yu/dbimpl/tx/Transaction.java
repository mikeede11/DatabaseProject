package edu.yu.dbimpl.tx;

import edu.yu.dbimpl.buffer.Buffer;
import edu.yu.dbimpl.buffer.BufferBase;
import edu.yu.dbimpl.buffer.BufferMgr;
import edu.yu.dbimpl.buffer.BufferMgrBase;
import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.file.FileMgrBase;
import edu.yu.dbimpl.log.LogMgrBase;
import edu.yu.dbimpl.tx.concurrency.ConcurrencyMgr;
import edu.yu.dbimpl.tx.concurrency.ConcurrencyMgrBase;
import edu.yu.dbimpl.tx.recovery.RecoveryMgr;
import edu.yu.dbimpl.tx.recovery.RecoveryMgrBase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

public class Transaction extends TxBase {
    private FileMgrBase fm;
    private LogMgrBase lm;
    private BufferMgrBase bm;
    private RecoveryMgrBase rm;
    private ConcurrencyMgrBase cm;
    private int personalTxNum;
    private static AtomicInteger txNumCounter = new AtomicInteger(1);
    private HashMap<BlockIdBase, BufferBase> pinnedBuffers;
    private boolean terminated;
    private Logger txLogger;
    /**
     * Create a new transaction and its associated recovery and concurrency
     * managers.  This constructor depends on the file, log, and buffer
     * managers.
     *
     * @param fm
     * @param lm
     * @param bm
     * @see ConcurrencyMgrBase
     * @see RecoveryMgrBase
     */
    public Transaction(FileMgrBase fm, LogMgrBase lm, BufferMgrBase bm) {
        super(fm, lm, bm);
        this.fm = fm;
        this.lm = lm;
        this.bm = bm;
        this.rm = new RecoveryMgr(this, lm, bm);//this will also log a <START,txNum>
        this.cm = new ConcurrencyMgr();
        this.personalTxNum = txNumCounter.getAndIncrement();
        this.pinnedBuffers = new HashMap<>();
        this.txLogger = Logger.getLogger(FileMgr.class.getName());

    }

    @Override
    public int txnum() {
        if(terminated){throw new IllegalStateException();}
        return personalTxNum;
    }

    @Override
    public void commit() {
        if(terminated){throw new IllegalStateException();}
        txLogger.info("Tx committing...");
        rm.commit();
        cm.release();
        for(BufferBase b: pinnedBuffers.values()){
            b.unpin();
        }
        txLogger.info("Tx committed");
        this.terminated = true;
    }

    @Override
    public void rollback() {
        if(terminated){throw new IllegalStateException();}
        txLogger.info("Tx rollingback....");
        rm.rollback();
        cm.release();
        for(BufferBase b: pinnedBuffers.values()){
            b.unpin();
        }
        txLogger.info("Tx rolledback");
        this.terminated = true;

    }

    @Override
    public void recover() {
        txLogger.info("Tx Recovering");
        //TODO FLUSHES ALL MODIFIED BUFFERS BEFORE HAND? WHY? this makes no sense recover undoes uncommited/unrolledback txs
        // that relies on the logs being logged to disk. So either the tx is commited/rolledback in which case it is already
        // on disc and this flushing would be a waste or it is not commited/rolledback and it may or may not be on disc.
        // but either way upon recvoer() those actions will be undone anyways so the same result will be obtained wether
        // you flush those changes or not - so why flush at all - it is an unnecessary write to disc???
        cm.resetAllLockState();
        rm.recover();
        txLogger.info("Tx Recovered");
    }

    @Override
    public void pin(BlockIdBase blk) {
        if(terminated){throw new IllegalStateException();}
        pinnedBuffers.put(blk, bm.pin(blk));//map good b/c it wont double pin block even if client does
        txLogger.info("Tx pinned block " + blk);
    }

    @Override
    public void unpin(BlockIdBase blk) {
        if(terminated){throw new IllegalStateException();}
        bm.unpin(pinnedBuffers.get(blk));
        pinnedBuffers.remove(blk);
        txLogger.info("Tx unpinned block " + blk);
    }

    @Override
    public int getInt(BlockIdBase blk, int offset) {
        if(terminated){throw new IllegalStateException();}
        if(!pinnedBuffers.containsKey(blk)){throw new IllegalStateException();}
        cm.sLock(blk);

        return pinnedBuffers.get(blk).contents().getInt(offset);
    }

    @Override
    public String getString(BlockIdBase blk, int offset) {
        if(terminated){throw new IllegalStateException();}
        if(!pinnedBuffers.containsKey(blk)){throw new IllegalStateException();}
        cm.sLock(blk);
        return pinnedBuffers.get(blk).contents().getString(offset);
    }

    @Override
    public void setInt(BlockIdBase blk, int offset, int val, boolean okToLog) {
        if(terminated){throw new IllegalStateException();}
        if(!pinnedBuffers.containsKey(blk)){throw new IllegalStateException();}
        cm.xLock(blk);
        int lsn = -1;//if no log record for this change
        if(okToLog) {
            lsn = rm.setInt(pinnedBuffers.get(blk), offset, val);
        }
        BufferBase buf = pinnedBuffers.get(blk);
        buf.setModified(personalTxNum, lsn);
        buf.contents().setInt(offset, val);
        txLogger.info("Tx setInt() puts value " + val + " in " + blk + " at offset " + offset);

    }

    @Override
    public void setString(BlockIdBase blk, int offset, String val, boolean okToLog) {
        if(terminated){throw new IllegalStateException();}
        if(!pinnedBuffers.containsKey(blk)){throw new IllegalStateException();}
        cm.xLock(blk);
        int lsn = -1;//if no log record for this change
        if(okToLog) {
            lsn = rm.setString(pinnedBuffers.get(blk), offset, val);
        }
        BufferBase buf = pinnedBuffers.get(blk);
        buf.setModified(personalTxNum, lsn);
        buf.contents().setString(offset, val);
        txLogger.info("Tx setString() puts value " + val + " in " + blk + " at offset " + offset);
    }

    @Override
    public int size(String filename) {
        if(terminated){throw new IllegalStateException();}
        // TODO Acquire an S-lock - how?
        return fm.length(filename);
    }

    @Override
    public BlockIdBase append(String filename) {
        if(terminated){throw new IllegalStateException();}
        BlockIdBase virtualBlock = new BlockId(filename, size(filename));
        cm.xLock(virtualBlock);
        txLogger.info("Tx append() to " + filename);
        return fm.append(filename);
    }

    @Override
    public int blockSize() {
        if(terminated){throw new IllegalStateException();}

        return fm.blockSize();
    }

    @Override
    public int availableBuffs() {
        if(terminated){throw new IllegalStateException();}
        return bm.available();//is this unpinned buffers in bufferpool or unpinned buffers for this tx - is the latter even a thing?
    }

}
