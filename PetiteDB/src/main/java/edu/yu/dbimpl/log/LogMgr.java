package edu.yu.dbimpl.log;

import edu.yu.dbimpl.MyFormatter;
import edu.yu.dbimpl.file.*;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class LogMgr extends LogMgrBase{
    FileMgrBase fileMgr;
    int lsn;//doesn't need to be persistent - why? b/c its just to keep track of sequence of txs within a given session I think idk
    BlockIdBase lastBlock;
    private ByteBuffer ramBlock;
    private String logfile;
    private boolean blockFromDisc;
    private Logger logLogger;
    private ConsoleHandler ch;
    /**
     * Creates the manager for the specified log file.  If the log file does
     * not yet exist, it is created with an empty first block.
     *
     * @param fm
     * @param logfile the name of the log file
     */
    public LogMgr(FileMgrBase fm, String logfile) {//create file if doesn't exist code (call append() if no exist)
        super(fm, logfile);

        /*Logging operations*/
        this.logLogger = Logger.getLogger(FileMgr.class.getName());;

        /*Logging operations*/

        logLogger.info("LM constructing a LogMgr");

        //assign IV
        this.fileMgr = fm;
        this.lsn = 0;
        this.logfile = logfile;
        int NumLogBlocks = fileMgr.length(logfile);//length returns actual # of blocks not the indexed version (-1)
        Page p = new Page(fm.blockSize());//RAM
        blockFromDisc = true;

        if(NumLogBlocks == 0){
            //if no log file make a log file
            lastBlock = fileMgr.append(logfile);
            logLogger.info("LM no log file, created one named " + logfile + " with append() with the first block returned");

        }
        else{//if there is a logfile get the lastBlock
            int lastBlockIndex = NumLogBlocks - 1;//0 indexing
            lastBlock = new BlockId(logfile, lastBlockIndex);
            logLogger.info("LM There is a log file get last block Index (" + lastBlockIndex + ")...");
        }
        logLogger.info("LM read that block from disk into RAM...");
        fileMgr.read(lastBlock, p);//read that block from disk into RAM
        if(ByteBuffer.wrap(p.getAllBytes()).getInt(0) == 0){//if the block is empty allocate a fresh empty buffer
            ramBlock = ByteBuffer.allocate(fm.blockSize());
            //FIXME ISNT THIS REDUNDANT. THE ELSE BLOCK WOULD COVER BOTH CASES PRESUMABLY
            logLogger.info("LM The block has an integer of 0 at the begin which we are assuming means there has been no data written to the block. So we allocated a new buffer to read/write to that block");
        }
        else {
            ramBlock = ByteBuffer.wrap(p.getAllBytes());//if the block read from disk contains info wrap buffer around that info
            logLogger.info( "LM The block has info so we wrapped it in a buffer which we'll use to read/write to that block");
        }
    }

    @Override
    public void flush(int lsn) {//write bytebuffer to disk
        logLogger.info("LM flushing lsn" + lsn );
        //if there is nothing in the current buffer don't do anything - all logs are on disk
        //only two situations where there will be nothing in the current buffer - upon start up and after a flush
        if(ramBlock.getInt(0) == 0){
            //do nothing
            logLogger.info("LM actually do not flush. There is no information on this so all logs are on disk. don't waste a write.");
        }
        else{
            //if its the first block we need to special case because we can not append since we already did so upon
            //creation of the file. In other words block 0 is already on disk we just need to write to it.
            if(blockFromDisc){
                PageBase p = new Page(ramBlock.array());
                p.setBytes(0, ramBlock.array());
                fileMgr.write(this.lastBlock, p);//write buffer to disk
                logLogger.info("LM line 86");
                blockFromDisc = false;
            }
            else {
//                BlockIdBase newLastBlock = this.lastBlock;
//                if(lastBlock.number() != 0) {
                BlockIdBase newLastBlock = fileMgr.append(logfile);//allocate memory for this RAM block on disk
//                }
                PageBase p = new Page(ramBlock.array());
                p.setBytes(0, ramBlock.array());
                fileMgr.write(newLastBlock, p);//write to that block on disk
                this.lastBlock = newLastBlock;//update last block (incremented)
                logLogger.info("LM Just flushed a new block (" + newLastBlock.number() + ") to disk by append(logfile) and then write() the current RAM block to disc.");
            }
            this.ramBlock = ByteBuffer.allocate(fileMgr.blockSize());//clears the buffer and Java Garbage Collection will deal with the de-referenced buffer
        }
    }

    @Override
    public Iterator<byte[]> iterator() {
        logLogger.info("LM flushing and creating an Iterator...");

        //flush - make sure all records are on disk before we return an iterator of the logrecs on disk
        flush(lsn);
        return new LogIterator(fileMgr, new BlockId(logfile,lastBlock.number()));
    }

    @Override
    public int append(byte[] logrec) {
        logLogger.info("LM Appending a logrec...");

        // Is the log record too big?
        if(logrec.length > (fileMgr.blockSize() - 8)){
            throw new IllegalArgumentException("LM log record is too large!!!");
        }

        //check beginning of bytebuffer getInt to get the offset of the beginning of the next open spot
        int offsetOfLastRec = ramBlock.getInt(0);
        if(offsetOfLastRec == 0){
            offsetOfLastRec = fileMgr.blockSize();//for calculation purposes
        }
        //say that offset is X so logrec can fit in this block if logrec.length() + 4 <= X - 4
        if(logrec.length + Integer.BYTES <= offsetOfLastRec - Integer.BYTES){//if there is room in the block

            insertLogRecord(offsetOfLastRec, logrec);
        }
        else{
            //the log record can not fit we need to flush this RAM block to disk and
            //put this log record into a new RAM block which will represent the next block on disk
            flush(lsn);
            offsetOfLastRec = fileMgr.blockSize();//calculation purposes
            logLogger.info("LM New block: we will insert the log record at the end of it...");
            insertLogRecord(offsetOfLastRec, logrec);
        }
        return lsn++;
    }

    private void insertLogRecord(int offsetOfLastRec, byte[] logrec){
        logLogger.info("LM Inserting Log record...");

        int offsetOfNewRec = offsetOfLastRec - (logrec.length + Integer.BYTES);//calculate the offset of the newRec
        ramBlock.putInt(offsetOfNewRec, logrec.length);
        ramBlock.position(offsetOfNewRec + Integer.BYTES);
        ramBlock.put(logrec, 0, logrec.length);//put the log record in
        ramBlock.putInt(0, offsetOfNewRec);//put the offset of the last log record at the beginning of the block
        logLogger.info("LM The offset of the new record is " + offsetOfNewRec);
    }
}
