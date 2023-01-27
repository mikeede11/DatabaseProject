package edu.yu.dbimpl.query;

/** A Scan represents the output of a relational algebra query.  The elegance
 * of the relational model implies that a "database table" can be represented
 * by a Scan instance since the output of a query is itself a relation (and a
 * "table" is a relation).
 *
 * A Scan can be usefully thought of as a "read-only" Iterator (over the set of
 * records returned by the query).
 *
 * Students MAY NOT change this interface IN ANY WAY!
 */
public interface Scan {

    /** Positions the scan before its first record. A subsequent call to next()
     * will return the first record.
     */
    public void beforeFirst();

    /** Moves the scan to the next record.
     *
     * @return false if there is no next record
     */
    public boolean next();

    /** Returns the value of the specified integer field in the current record.
     *
     * @param fldname the name of the field
     * @return the field's integer value in the current record
     */
    public int getInt(String fldname);

    /** Returns the value of the specified string field in the current record.
     *
     * @param fldname the name of the field
     * @return the field's string value in the current record
     */
    public String getString(String fldname);

    /** Returns the value of the specified field in the current record.  The
     * value is expressed as a Constant.
     *
     * @param fldname the name of the field
     * @return the value of that field, expressed as a Constant.
     */
    public Constant getVal(String fldname);

    /** Returns true iff the scan has the specified field.
     *
     * @param fldname the name of the field
     * @return true iff the scan has that field, false otherwise
     */
    public boolean hasField(String fldname);

    /** Terminate the scan processing (and automatically also close all
     * underlying scans, if any).
     */
    public void close();
}