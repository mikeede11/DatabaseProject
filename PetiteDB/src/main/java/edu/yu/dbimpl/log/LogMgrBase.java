package edu.yu.dbimpl.log;

/** Specifies the public API for the LogMgr implementation by requiring all
 * LogMgr implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * A LogMgr is (conceptually) a singleton.
 *
 * A LogMgr is responsible for writing log records but has no knowledge of the
 * structure of these records: as far as its concerned it's "just a sequence of
 * bytes"
 *
 * The LogMgr latest-sequence-number (LSN) must be initialized (to facilitate
 * my testing) to 0, and incremented each time that an append() operation
 * completes succesfully.

 * @author Avraham Leff
 */

import java.util.Iterator;
import edu.yu.dbimpl.file.FileMgrBase;

/** The log manager, which is responsible for writing log records into a log
 * file. The tail of the log is kept in a bytebuffer, which is flushed to disk
 * when needed.
 */
public abstract class LogMgrBase {

    /** Creates the manager for the specified log file.  If the log file does
     * not yet exist, it is created with an empty first block.
     *
     * @param FileMgr the file manager
     * @param logfile the name of the log file
     */
    public LogMgrBase(FileMgrBase fm, String logfile) {
        // fill me in your implementation class!
    }

    /** Ensures that the log record corresponding to the specified LSN has been
     * written to disk.  All earlier log records will also be written to disk.
     *
     * @param lsn the LSN of a log record
     */
    public abstract void flush(int lsn);

    /** First flushes the log to disk, then return an LogIterator to the contents
     * of the log on disk.
     *
     * @return a LogIterator (typed as an Iterator<byte[]>)
     */
    public abstract Iterator<byte[]> iterator();

    /** Appends a log record (as an arbitray byte array).
     *
     * Note: appending a record to the log does NOT guarantee that the record
     * will be immediately written to disk.  In general, the log manager
     * implementation chooses when to write log records to disk (see discussion
     * in lecture).  To guarantee that the log record is immediately written to
     * disk, the client must invoke flush().
     *
     * Suggested implementation (non-despositive, as long as the other API
     * semantics (including the Iterator) are provided) follows. Log records are
     * written from "right to left" order in a given Page (i.e., at decreasing
     * byte offsets the in the main-memory page).  Storing the records backwards
     * makes it easy to read them in reverse order.
     *
     * Specifically: the LogMgr first writes the size of the record before the
     * byte array, and then writes the byte array itself.
     *
     * @param logrec a byte buffer containing the bytes.  The only constraint is
     * that the array must fits inside a single Page.
     * @return the LSN of the final value
     * @throws IllegalArgumentException if the log record is too large to fit
     * into a single page
     * @see #flush
     */
    public abstract int append(byte[] logrec);
}