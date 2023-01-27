package edu.yu.dbimpl.tx;

/** Specifies the public API for the Transaction implementation by requiring all
 * Transaction implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * Transactions provide clients with the classic "ACID" properties, interfacing
 * with concurrency and recovery managers as necessary.
 *
 * @author Avraham Leff
 */

import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgrBase;
import edu.yu.dbimpl.log.LogMgrBase;
import edu.yu.dbimpl.buffer.BufferMgrBase;

public abstract class TxBase {

    /** Create a new transaction and its associated recovery and concurrency
     * managers.  This constructor depends on the file, log, and buffer
     * managers.
     *
     * @see edu.yu.dbimpl.tx.concurrency.ConcurrencyMgrBase
     * @see edu.yu.dbimpl.tx.recovery.RecoveryMgrBase
     */
    public TxBase(FileMgrBase fm, LogMgrBase lm, BufferMgrBase bm) {
        // fill me in in your implementation class!
    }

    /** Every tx is associated with a unique integer id: this method returns the
     * tx's id.
     *
     * @return the unique tx id
     */
    public abstract int txnum();

    /** Commits the current transaction: first flush all modified buffers (and
     * their log records); then write and flush a commit record to the log; then
     * release all locks, and unpin any pinned buffers.
     */
    public abstract void commit();

    /** Roll the current transaction back: first undoes any modified values; then
     * flushes those buffers; then write and flush a rollback record to the log;
     * then releases all locks, and unpins any pinned buffers.
     */
    public abstract void rollback();

    /** Flushes all modified buffer, then traverse the log, rolling back all
     * uncommitted transactions.  Finally, writees a quiescent checkpoint record"
     * to the log.  This method is called during system startup, before user
     * transactions begin.
     */
    public abstract void recover();

    /** Pins the specified block to a page buffer.  Going forward, the
     * transaction will manage the buffer on behalf of the client (until "unpin"
     * is invoked)
     *
     * @param blk a reference to the disk block
     * @see #unpin
     */
    public abstract void pin(BlockIdBase blk);

    /** Unpins the specified block.
     *
     * @param blk a reference to the disk block
     */
    public abstract void unpin(BlockIdBase blk);

    /** Returns the integer value stored at the specified offset of the specified
     * block.  The transaction acquires an "s-lock" on behalf of the client
     * before returning the value.
     *
     * @param blk a reference to a disk block
     * @param offset the byte offset within the block
     * @return the integer stored at that offset
     * @throws IllegalStateException if specified block isn't currently pinned
     */
    public abstract int getInt(BlockIdBase blk, int offset);

    /** Returns the string value stored at the specified offset of the specified
     * block.  The transaction acquires an "s-lock" on beghalf of the client
     * before returning the value.
     *
     * @param blk a reference to a disk block
     * @param offset the byte offset within the block
     * @return the string stored at that offset
     * @throws IllegalStateException if specified block isn't currently pinned
     */
    public abstract String getString(BlockIdBase blk, int offset);

    /** Stores an integer at the specified offset of the specified block.  The
     * transaction acquires an "x-lock" on behalf of the client before reading
     * the value, creating the appropriate "update" log record and adding the
     * record to the log file.  Finally, the modified value is written to the
     * buffer.  The transaction is responsible for invoking the buffer
     * setModified() method, passing in the appropriate parameter values.
     *
     * @param blk a reference to the disk block
     * @param offset a byte offset within that block
     * @param val the value to be stored
     * @param okToLog true iff the client wants the operation to be logged, false
     * otherwise.
     * @throws IllegalStateException if specified block isn't currently pinned   */
    public abstract void
    setInt(BlockIdBase blk, int offset, int val, boolean okToLog);

    /** Stores a string at the specified offset of the specified block. The
     * transaction acquires an "x-lock" on behalf of the client before reading
     * the value, creating the appropriate "update" log record and adding the
     * record to the log file.  Finally, the modified value is written to the
     * buffer.  The transaction is responsible for invoking the buffer
     * setModified() method, passing in the appropriate parameter values.
     *
     * @param blk a reference to the disk block
     * @param offset a byte offset within that block
     * @param val the value to be stored
     * @param okToLog true iff the client wants the operation to be logged, false
     * otherwise.
     * @throws IllegalStateException if specified block isn't currently pinned
     */
    public abstract void
    setString(BlockIdBase blk, int offset, String val, boolean okToLog);

    /** Returns the number of blocks in the specified file.
     *
     * Note: be sure to provide transactional semantics for this method.
     *
     * @param filename the name of the file
     * @return the number of blocks in the file
     */
    public abstract int size(String filename);

    /** Appends a new block to the end of the specified file and returns a
     * reference to it.
     *
     * Note: be sure to provide transactional semantics for this method.
     *
     * @param filename the name of the file
     * @return a reference to the newly-created disk block
     */
    public abstract BlockIdBase append(String filename);

    /** Returns the size of blocks, uniform across all disk blocks managed by the
     * DBMS.
     */
    public abstract int blockSize();

    /** Returns the number of available (i.e. unpinned) buffers.
     *
     * @return the number of available buffers
     */
    public abstract int availableBuffs();
}