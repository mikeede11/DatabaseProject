package edu.yu.dbimpl.query;

import edu.yu.dbimpl.record.RID;

/** Extends the Scan interface beyond "read-only" operations to include "update
 * records" operations.
 *
 * Students MAY NOT change this interface IN ANY WAY!
 */
public interface UpdateScan extends Scan {
    /** Modifies the field value of the current record.
     *
     * @param fldname the name of the field
     * @param val the new value, expressed as a Constant
     */
    public void setVal(String fldname, Constant val);

    /** Modifies the field value of the current record.
     *
     * @param fldname the name of the field
     * @param val the new integer value
     */
    public void setInt(String fldname, int val);

    /** Modifies the field value of the current record.
     *
     * @param fldname the name of the field
     * @param val the new string value
     */
    public void setString(String fldname, String val);

    /** Inserts a new record somewhere in the scan after the current record,
     * setting the current record to the new record.
     */
    public void insert();

    /** Deletes the current record from the scan.
     */
    public void delete();

    /** Returns the id of the current record.
     *
     * @return the id of the current record
     */
    public RID  getRid();

    /**  Positions the scan so that the current record has the specified id.
     *
     * @param rid the id of the desired record
     */
    public void moveToRid(RID rid);
}