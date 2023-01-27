package edu.yu.dbimpl.query;

/** Specifies the public API for the SelectScan implementation by requiring all
 * implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * The ProductScan class implements the "product" relational algebra operation
 * using a pipelining approach in which all methods EXCEPT next() are delegated
 * "as is" to the underlying scans.
 *
 * @author Avraham Leff
 */

public abstract class ProductScanBase implements Scan {

    /** Constructor.
     *
     * Create a product scan having the two underlying scans.
     *
     * @param s1 the left-hand-side scan
     * @param s2 the right-hand-side scan
     */
    public ProductScanBase(Scan s1, Scan s2) {
        // fill me in with your implementation
    }

}