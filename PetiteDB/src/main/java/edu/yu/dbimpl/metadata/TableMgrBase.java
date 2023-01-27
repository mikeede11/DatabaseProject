package edu.yu.dbimpl.metadata;

/** Specifies the public API for the TableMgr implementation by requiring all
 * TableMgr implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * TableMgrBase constrains the TableMgr design by implicitly assuming an
 * implementation consisting of one table that stores meta-data about table
 * names, and another tables that stores meta-data about field names.  Also:
 * the names of these tables and their field names is specified by constants.
 * In general, PetiteDB tries to give implementations more freedom, but hard to
 * see how to define a testable interface that doesn't use Java's reflection
 * APIs to do the job.
 *
 * A TableMgr stores the meta-data about tables created by users, thus creating
 * and managing a catalog.  Implementations MUST SUPPORT client ability to
 * TableScan iterate over the TABLE_META_DATA_TABLE and FIELD_META_DATA_TABLE
 * catalog tables.  The TABLE_META_DATA_TABLE MUST STORE table name information
 * in a TABLE_NAME field.  Implementations NEED NOT support field names and
 * tables names that are larger than MAX_LENGTH_PER_NAME.
 */

import edu.yu.dbimpl.tx.TxBase;
import edu.yu.dbimpl.record.LayoutBase;
import edu.yu.dbimpl.record.SchemaBase;

public abstract class TableMgrBase {
    /** The max characters a tablename or fieldname can have.
     */
    public static final int MAX_LENGTH_PER_NAME = 16;

    /** Constants that define the tables and table fields used by the TableMgr
     */
    public static final String TABLE_META_DATA_TABLE = "tblcat";
    public static final String FIELD_META_DATA_TABLE = "fldcat";
    public static final String TABLE_NAME = "tblname";

    /** Constructor: create a new catalog manager.
     *
     * @param isNew true iff this is the first time that the database is being
     * created (for this file system root): implicitly requests that the TableMgr
     * creates the two meta-data catalog tables.
     * @param tx supplies the transactional scope for database operations used in
     * the constructor implementation.
     */
    public TableMgrBase(boolean isNew, TxBase tx) {
        // fill me in in the implementation class!
    }

    /** Retrieves the layout of the specified table.  If the table is not in the
     * catalog, return null.
     *
     * @param tblname the name of the table whose meta-data is being requested
     * @param tx supplies the transactional scope for the method's implementation.
     * @return the meta-data for the specified table, null if no such table.
     */
    public abstract LayoutBase getLayout(String tblname, TxBase tx);

    /** Supplies the meta-data that should be persisted to the system catalog
     * about a newly created database table.
     *
     * @param tblname the name of the new table
     * @param schema the table's schema
     * @param tx supplies the transactional scope for the method's implementation
     */
    public abstract void createTable(String tblname, SchemaBase schema, TxBase tx);
}