package edu.yu.dbimpl.query;

/** Specifies the public API for the SelectScan implementation by requiring all
 * implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * The SelectScan class implements the "select" relational algebra operation
 * using a pipelining approach in which all methods EXCEPT next() are delegated
 * "as is" to the underlying scan.
 *
 * @author Avraham Leff
 */

import edu.yu.dbimpl.record.*;

public abstract class SelectScanBase implements UpdateScan {

    /** Constructor

     * Create a select scan having the specified underlying
     * scan and predicate.
     *
     * @param scan the scan representing the input relation
     * @param predicate the selection predicate that will be applied to filter
     * the input relation
     */
    public SelectScanBase(Scan scan, Predicate predicate) {
        // fill me in with your implementation!
    }
}