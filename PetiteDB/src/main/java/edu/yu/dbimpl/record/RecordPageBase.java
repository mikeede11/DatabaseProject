package edu.yu.dbimpl.record;

import static java.sql.Types.INTEGER;
import edu.yu.dbimpl.file.*;
import edu.yu.dbimpl.tx.TxBase;

/** Specifies the public API for the RecordPage implementation by requiring all
 * RecordPage implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * A RecordPage manages "record-slots" at block granularity.
 *
 * Design note: uses the slotted page design for fixed-length fields discussed
 * in lecture and textbook.
 *
 * Design note: can help to consider the RecordPageBase API as moving parts of
 * the TxBase API "up a level" such that clients can get/set values in terms of
 * field names rather than block locations.
 */

public abstract class RecordPageBase {

    /** Constructor
     *
     * @param tx Defines the transaction scope in which operations on the block
     * will take place
     * @param blk The block in which the record is stored
     * @param layout Holds the physical and logical record schema
     */
    public RecordPageBase(TxBase tx, BlockIdBase blk, LayoutBase layout) {
        // fill me in in in your implementation class!

    }

    /** Return the integer value stored for the specified field of a specified
     * slot.
     *
     * @param fldname the name of the field.
     * @return the integer stored in that field
     */
    public abstract int getInt(int slot, String fldname);

    /** Return the string value stored for the specified field of the specified
     * slot.
     *
     * @param fldname the name of the field.
     * @return the string stored in that field
     */
    public abstract String getString(int slot, String fldname);

    /** Stores an integer at the specified field of the specified slot.
     *
     * @param fldname the name of the field
     * @param val the integer value stored in that field
     */
    public abstract void setInt(int slot, String fldname, int val);

    /** Stores a string at the specified field of the specified slot.
     *
     * @param fldname the name of the field
     * @param val the string value stored in that field
     */
    public abstract void setString(int slot, String fldname, String val);

    /** Deletes the specified slot.
     *
     * @param slot uniquely identifies the record slot.
     */
    public abstract void delete(int slot);

    /** Initializes all record slots in the block: i.e., all integers are set to
     * 0, all strings to the empty string, and all slots to "empty".
     *
     * These operations should not be logged (from a transactional point of view)
     * because we consider the old values to be meaningless.
     */
    public abstract void format();

    /** Search the block, starting from the specified slot, for an "in-use" slot.
     *
     * @param slot uniquely identifies the record slot from which the search will
     * begin.  To search from the beginning of the block, set this parameter to -1.
     * @return Returns the location of the first "in-use" slot AFTER the
     * specified slot: if all slots are "empty", returns -1 as a sentinel value.
     */
    public abstract int nextAfter(int slot);

    /** Search the block, starting from the specified slot, for an "empty" slot.
     *
     * @param slot uniquely identifies the record slot from which the search will
     * begin.  To search from the beginning of the block, set this parameter to
     * -1.
     * @return Returns the location of the first "empty" slot AFTER the specified
     * slot AND sets the state of the slot to "in-use"; if all slots are
     * "in-use", returns -1 as a sentinel value.
     */
    public abstract int insertAfter(int slot);

    /** Returns the block associated with the RecordPageBase instance.
     *
     * @return the block
     */
    public abstract BlockIdBase block();
}