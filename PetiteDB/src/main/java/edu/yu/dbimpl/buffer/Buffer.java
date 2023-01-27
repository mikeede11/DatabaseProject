package edu.yu.dbimpl.buffer;

import edu.yu.dbimpl.file.*;
import edu.yu.dbimpl.log.LogMgrBase;

public class Buffer extends BufferBase {
    private FileMgrBase fMgr;
    private LogMgrBase lMgr;
    private PageBase contents;
    private BlockIdBase assignedBlock;
    private boolean modified;
    private int txnum;
    private int lsn;
    private int pinCount;

    public Buffer(FileMgrBase fileMgr, LogMgrBase logMgr) {
        super(fileMgr, logMgr);
        this.fMgr = fileMgr;
        this.lMgr = logMgr;
        this.txnum = -1;//no txn has taken place yet
        this.assignedBlock = new BlockId("NA", -1);
        this.contents = new Page(fileMgr.blockSize());
        //fmgr to write to read from disc
        //idk whats up with log
    }



    @Override
    public PageBase contents() {
        return this.contents;
    }

    @Override
    public BlockIdBase block() {
        return this.assignedBlock;
    }

    @Override
    public void setModified(int txnum, int lsn) {//lsn is -1 when no log
        this.modified = true;
        this.txnum = txnum;
        this.lsn = lsn;
    }

    @Override
    public boolean isPinned() {
        return pinCount > 0;
    }

    @Override
    public int modifyingTx() {
        return this.txnum;
    }

    @Override
    public void flush() {
        if(modified) {
            lMgr.flush(lsn);
            fMgr.write(this.assignedBlock, this.contents);
        }
    }

    @Override
    public void unpin() {
        //if(this.pinCount <= 0){throw new IllegalStateException("already unpinned!");}
        if(this.pinCount > 0) {
            this.pinCount--;
        }
    }

    @Override
    public void pin() {
        this.pinCount++;
    }

    @Override
    public void assignToBlock(BlockIdBase b) {
        //why no flush log record??? perhaps this is updating the block on disc but its not necessarily a logical tx yet and therefore
        //we dont want to log a tx we did b/c we didnt do a tx - yeah this is prob the reason.
        if(modified){
            fMgr.write(this.assignedBlock, this.contents);//TODO Flush?
        }
        fMgr.read(b, this.contents);//FIXME??? theres already stuff in the page will it work?
        this.assignedBlock = b;
    }
}
