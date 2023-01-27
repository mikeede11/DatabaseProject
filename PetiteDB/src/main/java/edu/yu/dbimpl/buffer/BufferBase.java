package edu.yu.dbimpl.buffer;

/** Specifies the public API for the Buffer implementation by requiring all
 * Buffer implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * An single buffer in BufferMgr's buffer pool: it encapsulates a Page, adding
 * additional information such as whether the Page's contents have been
 * modified since it was fetched from disk.
 *
 * @author Avraham Leff
 */

import edu.yu.dbimpl.file.*;
import edu.yu.dbimpl.log.LogMgrBase;

public abstract class BufferBase {

    public BufferBase(FileMgrBase fileMgr, LogMgrBase logMgr) {
        // fill me in in your implementation class!
    }

    /** Returns the Page encapsulated by this Buffer instance.
     *
     * @return the encapsulated Page.
     */
    public abstract PageBase contents();

    /** Returns a reference to the disk block allocated to the buffer.
     *
     * @return a reference to a disk block
     */
    public abstract BlockIdBase block();

    /** Sets the buffer's "modified" bit.
     *
     * @param txnum
     * @param lsn The LSN of the most recent log record, set to a negative number
     * to indicate that the client didn't generate a log record when modifying
     * the Buffer.
     */
    public abstract void setModified(int txnum, int lsn);

    /** Return true iff the buffer is currently pinned, defined as "has a pin
     * count that is greater than 0".
     *
     * @return true iff the buffer is pinned.
     */
    public abstract boolean isPinned();

    /** Returns the id of the transaction that modified this buffer instance.
     *
     * @return transaction id.
     */
    public abstract int modifyingTx();

    /** Write the buffer to its disk block if it has been modified, syncing the
     * state of the Buffer's assigned disk block has the same value as its
     * in-memory Page. If the Page has not been modiﬁed, the implementation
     * method need not do anything. If it has been modiﬁed, the method ﬁrst calls
     * LogMgr.flush() to ensure that the corresponding log record is on disk;
     * only afterwards does the Buffer write the page to disk.
     *
     * @see edu.yu.dbimpl.log.LogMgrBase#flush
     * @see BufferMgrBase#flushAll
     */
    public abstract void flush();

    /** Decrement the buffer's pin count.
     *
     * @see BufferMgrBase#unpin
     */
    public abstract void unpin();

    /** Increment the buffer's pin count (initialized to 0)
     *
     * @see BufferMgrBase#pin
     */
    public abstract void pin();

    /** Reads the contents of the specified block into the contents of the
     * buffer.  If the buffer was dirty, then its previous contents must first be
     * written to disk.
     *
     * @param b a reference to the data block
     */
    public abstract void assignToBlock(BlockIdBase b);


}