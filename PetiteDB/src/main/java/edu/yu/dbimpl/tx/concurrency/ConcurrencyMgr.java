package edu.yu.dbimpl.tx.concurrency;

import edu.yu.dbimpl.buffer.BufferAbortException;
import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.tx.TxMgr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

public class ConcurrencyMgr extends ConcurrencyMgrBase {
//    private static int[][] lockTable = new int[1000][2];//FIXME CHANGE TO LIST OF LISTS
    private static ConcurrentHashMap<BlockIdBase, int[]> lockTable = new ConcurrentHashMap<>();
    private Set<BlockIdBase> sLocks;//a set of the blockIDs that we have acquired sLocks for (and given to our tx)
    private Set<BlockIdBase> xLocks;//set of blockIDS with xLocks
    private TxMgr tm;
    private final static Object monitor = new Object();
    private Logger concurLogger;

    public ConcurrencyMgr(){
        super();
        sLocks = new HashSet<>();
        xLocks = new HashSet<>();
        tm = TxMgr.SINGLETON;
        this.concurLogger = Logger.getLogger(FileMgr.class.getName());
//        monitor = new Object();
    }
    @Override
    public synchronized void sLock(BlockIdBase blk) {
        lockTable.putIfAbsent(blk, new int[2]);
        boolean waited = false;
        if(!sLocks.contains(blk)){ // If we do not already posses the slock for this block
            // see if this block does not have an xlock on it
            //whats the point of an slock - oh I guess If I have an slock it tells me I can and will/am reading this block rn so
            //nobody can obtain an xlock on this block, but they can obtain slocks.
            synchronized (monitor) {
                while (lockTable.get(blk)[1] == 1 && !xLocks.contains(blk)) {
                    try {
                        concurLogger.info("CM Waiting for an S-Lock...");
                        long start = System.currentTimeMillis();
                        monitor.wait(tm.getMaxWaitTimeInMillis());
                        waited = true;
                        long end = System.currentTimeMillis();
                        long timeWaited = end - start;
                        if (timeWaited >= tm.getMaxWaitTimeInMillis()) {
                            throw new LockAbortException();
                            //TODO on tx or client to deal with this?
                        }
                    } catch (InterruptedException e) {
                    }
                }
                lockTable.get(blk)[0]++;
                sLocks.add(blk);
            }
            concurLogger.info("CM this Tx has aquired S-Lock");
        }

    }

    @Override
    public void xLock(BlockIdBase blk) {
        lockTable.putIfAbsent(blk, new int[2]);
        boolean waited = false;
        if(!xLocks.contains(blk)) {
            // If we do not already posses the xlock for this block
            // see if this block does not have an xlock on it
            //whats the point of an slock - oh I guess If I have an slock it tells me I can and will/am reading this block rn so
            //nobody can obtain an xlock on this block, but they can obtain slocks.
            synchronized (monitor) {
                //while A)someone has an xLock on this block or B) someone has an sLock on this block, but its not us or C) multiple people have an sLock on this block
                while (lockTable.get(blk)[1] == 1 || (lockTable.get(blk)[0] == 1 && !sLocks.contains(blk)) || lockTable.get(blk)[0] > 1) {
                    try {
                        long start = System.currentTimeMillis();
                        concurLogger.info("CM Waiting for an XLock...");
                        monitor.wait(tm.getMaxWaitTimeInMillis());
                        waited = true;
                        long end = System.currentTimeMillis();
                        long timeWaited = end - start;
                        if (timeWaited >= tm.getMaxWaitTimeInMillis()) {
                            throw new LockAbortException();
                            //TODO on tx or client to deal with this?
                        }
                    } catch (InterruptedException e) {
                        System.out.println("interrupted!!!");
                    }
                }
                lockTable.get(blk)[1] = 1;
                xLocks.add(blk);
            }
            concurLogger.info("CM this Tx has aquired X-Lock");

        }
    }

    @Override
    public void release() {

        synchronized (monitor) {
            for (BlockIdBase block : xLocks) {
                lockTable.get(block)[1] = 0;
            }
            for (BlockIdBase block : sLocks) {
                lockTable.get(block)[0]--;
            }
            sLocks.clear();
            xLocks.clear();
            monitor.notifyAll();//locks have been released!!! try again and see if its one of yours!
            concurLogger.info("CM Notified and released");
//        }
        }
    }

    @Override
    public void resetAllLockState() {
        lockTable.clear();
        xLocks.clear();
        sLocks.clear();
        concurLogger.info("CM reset the lock state.");
    }
}
