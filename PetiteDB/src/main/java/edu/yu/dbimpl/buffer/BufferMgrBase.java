package edu.yu.dbimpl.buffer;

/** Specifies the public API for the BufferMgr implementation by requiring all
 * BufferMgr implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 *  A BufferMgr manages the pinning and unpinning of buffers to blocks.
 *
 * A BufferMgr is conceptually a singleton.

 * @author Avraham Leff
 */

import edu.yu.dbimpl.file.*;
import edu.yu.dbimpl.log.LogMgrBase;

public abstract class BufferMgrBase {

    /** Creates a buffer manager having the specified number
     * of buffer slots.
     *
     * @param fileMgr file manager singleton
     * @param logMgr  log manager singleton
     * @param nBuffers number of buffers to allocate in main-memory
     * @param maxWaitTime maximum number of milliseconds to wait before throwing
     * a BufferAbortException to a client invoking pin().  Must be greater than 0.
     * @see #pin
     */
    public BufferMgrBase
    (FileMgrBase fileMgr, LogMgrBase logMgr, int nBuffers, int maxWaitTime) {
        // fill me in in your implementation class!

    }

    /** Returns the number of available (i.e. unpinned) buffers.
     *
     * @return the number of available buffers
     */
    public abstract int available();

    /** Flushes all modified ("dirty") buffers modified by the specified
     * transaction.
     *
     * @param txnum the transaction's id number
     */
    public abstract void flushAll(int txnum);

    /** Unpins the specified data buffer. If its pin count goes to zero, must
     * inform all clients currently blocked invoking pin() that a buffer instance
     * is now available.
     *
     * @param buffer the buffer to be unpinned
     * @see #pin
     */
    public abstract void unpin(BufferBase buffer);

    /**  Pins a buffer to the specified block, writing the current contents to
     * disk if modified by the previous client and a new disk block is being read
     * from disk.  If no buffers are currently available, the client will block,
     * waiting for a buffer instance to become available.  If no buffer becomes
     * available within the time specified by the "maxWaitTime" value supplied to
     * the constructor, throws a {@link BufferAbortException}.
     *
     * @param blk a reference to a disk block
     * @return the buffer pinned to that block
     * @throws BufferAbortException if the client times out waiting for a buffer
     * to become available.
     */
    public abstract BufferBase pin(BlockIdBase blk);
}