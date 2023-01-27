package edu.yu.dbimpl.record;

/**  Specifies the public API for the Schema implementation by requiring all
 * Schema implementations to extend this base class.
 *
 * Students MAY NOT modify this class in any way, they must suppport EXACTLY
 * the constructor signatures specified in the base class (and NO OTHER
 * signatures).
 *
 * A Schema represents the "logical" record schema of a table.  A schema
 * contains the name and type of each field of the table, as well as the length
 * of each varchar field.  Schemas have no knowledge of offsets within the
 * record.
 *
 * NOTE: Strings MUST BE typed as java.sql.Types.VARCHAR
 *
 * NOTE: Schema is conceptually a "value class", with all implications
 * concomitant thereto.
 */

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;



public abstract class SchemaBase {

    /** No-arg constructor.
     */
    public SchemaBase() {
        // fill me in in your implementation class!
    }

    /** Adds a field to the schema having a specified name, type, and length.
     * Specifying "length" is only relevant for "String" type, and is ignored
     * for "int" type because (aside for varchar), the length of a schema's
     * field is implementation dependent.
     *
     * @param fldname the name of the field
     * @param type the type of the field, using the constants in {@link
     * java.sql.Types}
     * @param length the logical (in contrast to physical) length of a string
     * field.
     */
    public abstract void addField(String fldname, int type, int length);

    /** Adds an integer field to the schema.
     *
     * @param fldname the name of the field
     */
    public abstract void addIntField(String fldname);

    /** Adds a string field to the schema.  The length is the logical length of
     * the field.  For example, if the field is defined as varchar(8), then its
     * length is 8.
     *
     * @param fldname the name of the field
     * @param length the number of chars in the varchar definition
     */
    public abstract void addStringField(String fldname, int length);

    /** Adds a field to the schema, retrieving "by name" its type and length
     * information from the specified schema
     *
     * @param fldname the name of the field
     * @param sch the other schema
     */
    public abstract void add(String fldname, SchemaBase sch);


    /** Adds all fields from the specified schema to the current schema.
     *
     * @param sch the other schema
     */
    public abstract void addAll(SchemaBase sch);

    /** Returns the field names in this schema.
     *
     * @return the collection of the schema's field names
     */
    public abstract List<String> fields();

    /** Returns true iff the specified field is in the schema
     *
     * @param fldname the name of the field
     * @return true iff the field is in the schema
     */
    public abstract boolean hasField(String fldname);

    /** Returns the type of the specified field, using the
     * constants in {@link java.sql.Types}.
     *
     * @param fldname the name of the field
     * @return the integer type of the field
     */
    public abstract int type(String fldname);

    /** Returns the logical length of the specified field.  If the field is not a
     * string field, then the return value is undefined.
     *
     * @fixme should possibly be "throw an exception" rather than "return
     * undefined".
     *
     * @param fldname the name of the field
     * @return the logical length of the field
     */
    public abstract int length(String fldname);
} // class