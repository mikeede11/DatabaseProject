package edu.yu.dbimpl.buffer;

import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.file.FileMgrBase;
import edu.yu.dbimpl.log.LogMgrBase;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class BufferMgr extends BufferMgrBase {
    private int unpinnedBuffersCount;
    private BufferBase[] bufferPool;
    private Object unpinnedBufferFull;
    private Object notEmpty;
    private ArrayBlockingQueue<BufferBase> unpinnedBuffersQueue;
    private long maxWaitTime;
    private Logger bufferLogger;
    /**
     * Creates a buffer manager having the specified number
     * of buffer slots.
     *
     * @param fileMgr     file manager singleton
     * @param logMgr      log manager singleton
     * @param nBuffers
     * @param maxWaitTime maximum number of milliseconds to wait before throwing
     *                    a BufferAbortException to a client invoking pin().  Must be greater than 0.
     * @see #pin
     */
    public BufferMgr(FileMgrBase fileMgr, LogMgrBase logMgr, int nBuffers, int maxWaitTime) {
        super(fileMgr, logMgr, nBuffers, maxWaitTime);
        if(nBuffers <= 0 || maxWaitTime <= 0){throw new IllegalArgumentException();}
        this.maxWaitTime = maxWaitTime;
        bufferPool = new BufferBase[nBuffers];
        //create an array of nBuffers buffers instances [that have page sizes allocated at a filemgr.blocksize()]
        for (int i = 0; i < bufferPool.length; i++) {
            bufferPool[i] = new Buffer(fileMgr, logMgr);//TODO should we reserve space for them??? assign to block
        }
        this.unpinnedBufferFull = new Object();
        this.notEmpty = new Object();
        this.unpinnedBuffersQueue = new ArrayBlockingQueue<BufferBase>(nBuffers);
        this.unpinnedBuffersCount = nBuffers;
        this.bufferLogger = Logger.getLogger(FileMgr.class.getName());
        bufferLogger.info("BM created");

    }

    @Override
    public int available() {
        return this.unpinnedBuffersCount;
    }

    @Override
    public void flushAll(int txnum) {
        bufferLogger.info("BM FlushAll() w/ txNum " + txnum);
        for (BufferBase b: bufferPool) {
            if(b.modifyingTx() == txnum) {
                b.flush();
            }
        }
    }

    @Override
    public synchronized void unpin(BufferBase buffer) {
            if (buffer.isPinned()) {
                while(unpinnedBuffersCount == bufferPool.length){
                    try {
                        wait(1);//we will never have to wait since we can never overflow the buffer pool (we only have n buffers!)
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                buffer.unpin();
                //check if buffer is still pinned
                // if yes do nothing if no -> unpinnedBuffers + 1 and notify waiting clients
                if (!buffer.isPinned()) {
                    this.unpinnedBuffersCount++;
                    bufferLogger.info("BM buffer " +  buffer.block() + " is unpinned - notifyAll()");
                    notifyAll();
                }
            }
    }

    @Override
    public BufferBase pin(BlockIdBase blk)
    {
        BufferBase bufferToPin = null;
        bufferToPin = pinAvailableBuffer(blk);
        return bufferToPin;
    }

    private synchronized BufferBase pinAvailableBuffer(BlockIdBase blk){
        BufferBase bufferToPin = null;
        for (BufferBase b: bufferPool) {
                if (b.block().fileName().equals(blk.fileName()) && b.block().number() == blk.number()) {//can replace with b.block.equals(blk)
                    bufferToPin = b;
                    boolean blockWasntPinned = !b.isPinned();
                    bufferToPin.pin();
                    bufferLogger.info("BM pinned buffer that already had block " + blk);
                    if(blockWasntPinned) {
                        unpinnedBuffersCount--;//what if its already pinned?
                    }
                    break;
                }
        }
        if(bufferToPin == null){//if no buffer has the blk
            while(unpinnedBuffersCount == 0){
                bufferLogger.info("BM no buffer available block is on disk...we'll wait() to pin");
                try {
                    long start = System.currentTimeMillis();
                    wait(maxWaitTime);
                    long end =  System.currentTimeMillis();
                    long waited = end - start;
                    if(waited >= maxWaitTime){
                        throw new BufferAbortException();
                    }
                    bufferLogger.info("BM done waiting! there must be a buffer available, lets see if I can pin it...");
                } catch (InterruptedException e) {
                }
            }
            //CHECK IF AVAILABLE/uNPINNED BUFFERS IF YES DO THIS - GET IT!
            for (BufferBase b: bufferPool) {//naive approach
                    if (!b.isPinned()) {//the first unpinned buffer in bufPool
                        bufferToPin = b;
                        bufferToPin.assignToBlock(blk);//ok now we can reuse the buffer
                        bufferToPin.pin();//pin it now
                        unpinnedBuffersCount--; //update
                        bufferLogger.info("BM pinned buffer " + blk);
                        break;//were done we got what we came for
                    }
            }
            //ELSE WAIT!!!/BLOCK!!!
            notifyAll();
        }
        return bufferToPin;//return the buffer
    }
}
