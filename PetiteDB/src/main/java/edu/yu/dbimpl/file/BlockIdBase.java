package edu.yu.dbimpl.file;

/** Specifies the public API for the BlockId implementation by requiring all
 * BlockId implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * A BlockId object represents the physical position of a block using its file
 * name and logical block number.  A BlockId is an immutable value class:
 * implementations should consider the implications of that design.
 *
 * @author Avraham Leff
 */


public abstract class BlockIdBase {
    public BlockIdBase(String filename, int blknum) {
        // fill me in in your implementation class!
    }

    abstract public String fileName();

    abstract public int number();
}
