package edu.yu.dbimpl.tx;

/** A singleton API for specifying global transaction state, explicitly
 * designed to NOT be extended (an enum is "final")
 *
 * Students MAY NOT modify this class in any way.
 *
 * Design note: a TxMgr is explictly singleton for the database, using Joshua
 * Bloch's "singleton as enum" design pattern.
 *
 * @see https://java-design-patterns.com/patterns/singleton/
 *
 * Design note: the getter/setter method is synchronized, not bothering with
 * AtomicLong since this method should in non-test usage be used exactly once
 * per DBMS instantiation.
 *
 * @author Avraham Leff
 */

public enum TxMgr {

    /** Clients access the singleton via TxMgr.SINGLETON
     */
    SINGLETON(5_000); // specifies maxWaitTimeInMillis & names the singleton
    // instance

    TxMgr(final long maxWaitTimeInMillis) {
        if (maxWaitTimeInMillis < 1) {
            throw new IllegalArgumentException
                    ("maxWaitTime must be greater than 0: "+maxWaitTimeInMillis);
        }

        this.maxWaitTimeInMillis = maxWaitTimeInMillis;
    } // private constructor

    /** Returns the current maxWaitTime value
     */
    public synchronized long getMaxWaitTimeInMillis()
    {
        return maxWaitTimeInMillis;
    }

    /** Sets the maxWaitTime value
     *
     * @param maxWaitTimeInMillis maximum amount of time that a tx will wait to
     * acquire a lock (whether slock or xlock) before the database throws a
     * LockAbortException.  Must be greater than 0, and is specified in ms.
     * @see edu.yu.dbimpl.tx.concurrency.ConcurrencyMgrBase#sLock
     * @see edu.yu.dbimpl.tx.concurrency.ConcurrencyMgrBase#xLock
     */
    public synchronized void setMaxTimeInMillis(final long maxWaitTimeInMillis) {
        if (maxWaitTimeInMillis < 1) {
            throw new IllegalArgumentException
                    ("maxWaitTime must be greater than 0: "+maxWaitTimeInMillis);
        }

        this.maxWaitTimeInMillis = maxWaitTimeInMillis;
    }

    private long maxWaitTimeInMillis;
} // enum class