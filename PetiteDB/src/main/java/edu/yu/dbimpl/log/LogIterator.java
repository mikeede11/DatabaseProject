package edu.yu.dbimpl.log;

import edu.yu.dbimpl.file.*;

import java.nio.ByteBuffer;

public class LogIterator extends LogIteratorBase {
    private Page page;
    private ByteBuffer ramBlock;
    private int offsetOfCurrentRec;
    private BlockIdBase currentBlock;
    private FileMgrBase fm;
    /**
     * Constructor: creates an iterator for the records in the log file,
     * positioned after the LAST log record.
     *
     * @param fm
     * @param blk
     */
    //gives it a fileMgr to read the log files, the block contains the last log record
    //two ways to get to that log file within the block
    //1) each block at the beginning has the location of its last log record (int that says the offset)
    //2) you can put the size of each log file in bytes at the end of each log file and use that to hop to the last record
    //2 is bad because when you actually iterate how do you read the log files you dont know their size.
    // yeah I think 1 is the way to go
    //FIXME Edge case when ierator() is called and no log records have been written  - since wether its a new file or not flush is called and the last block
    // will be an empty one we should go one block back. what if its a new file and there is no block back - I guess if blkNum < 0 offsetofCurrentrec = -1 hasNext return false
    // and next() returns byte[0] ?
    //
    public LogIterator(FileMgrBase fm, BlockIdBase blk) {
        super(fm, blk);
        this.page = new Page(fm.blockSize());
        this.fm = fm;
        this.fm.read(blk, page);
        this.currentBlock = blk;
        this.ramBlock = ByteBuffer.wrap(page.getAllBytes());
        this.offsetOfCurrentRec = this.ramBlock.getInt(0);
        if(blk.number() < 0 || ramBlock.getInt(0) == 0 || this.currentBlock.number() >= fm.length(this.currentBlock.fileName())){// the iterator could have been given an invalid block (empty)
            throw new IllegalArgumentException("Invalid Block supplied - 0 <= Block.number() <= last block Index which in this case is: " + (fm.length(this.currentBlock.fileName()) - 1));
        }
    }

    @Override
    public boolean hasNext() {
        if(this.currentBlock.number() == 0 && endOfBlock()){//current index is end of block
            return false;//first block and its the end
        }
        else{
            return true;//if its not the end of the block OR block # not zero then return true
        }

    }

    @Override
    public byte[] next() {
        //check if their is a next if yes great if no throw exception
        if(!hasNext()){
            throw new IllegalStateException("There are no more Logs!!!");
        }
        //within block simply skip to the next record specified by the # bytes at the current log record
        //if current index is not end of block return that log then increment index
        //else if end of block then do what you already have
        if(!endOfBlock()){//offset is not at end of block
            int sizeOfCurrentRec = this.ramBlock.getInt(offsetOfCurrentRec);
            byte[] log = new byte[sizeOfCurrentRec];
            this.ramBlock.position(offsetOfCurrentRec + Integer.BYTES);
            offsetOfCurrentRec += (Integer.BYTES + sizeOfCurrentRec);
            this.ramBlock.get(log, 0, sizeOfCurrentRec);
            return log;
        }
        else{
            //read in the block before the current
            this.currentBlock = new BlockId(currentBlock.fileName(),currentBlock.number() - 1);
            this.fm.read(this.currentBlock, page);
            this.ramBlock = ByteBuffer.wrap(page.getAllBytes());//UH OH
            this.offsetOfCurrentRec = this.ramBlock.getInt(0);
            //read the offset set offsetofcurrentrec then do what you did above
            int sizeOfCurrentRec = this.ramBlock.getInt(offsetOfCurrentRec);
            byte[] log = new byte[sizeOfCurrentRec];
            this.ramBlock.position(offsetOfCurrentRec + Integer.BYTES);
            this.ramBlock.get(log, 0, sizeOfCurrentRec);
            offsetOfCurrentRec += (Integer.BYTES + sizeOfCurrentRec);
            return log;
            //update state
        }
        //if last record in block navigate to the earlier block by going back 2 * blocksize bytes read the int
        //and skip to that offset to get the next record

        //have a page that is contains the relevant block for the log record we are at.
        //Use page.getBytes(offset) to get the next logrecs contents.
        //return
    }

    private boolean endOfBlock(){//is the current log the last one (rightmost) log in the block (the first one to be put in)
        return offsetOfCurrentRec == fm.blockSize();
    }

    private int getNextRecordOffset(){
        int sizeOfCurrentRec = this.ramBlock.getInt(offsetOfCurrentRec);
        return offsetOfCurrentRec + Integer.BYTES + sizeOfCurrentRec;
    }
}
