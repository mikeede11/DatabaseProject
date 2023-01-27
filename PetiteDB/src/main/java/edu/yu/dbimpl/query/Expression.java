package edu.yu.dbimpl.query;

/** A class representing an SQL expression (see the lecture that defines the
 * current restrictions on what must be supported).  The class is immutable by
 * design.
 *
 * Students MAY NOT modify this class in any way!
 */

import edu.yu.dbimpl.record.*;

public class Expression {

    /** Constructor which takes a Constant.
     */
    public Expression(final Constant val) {
        this.val = val;
    }

    /** Constructor which takes a String that names a single field (column).
     */
    public Expression(final String fldname) {
        this.fldname = fldname;
    }

    /** Evaluate the expression by extracting the value corresponding to the
     * field name from the current record of the specified scan.
     *
     * @param scan the scan, assumed to be positioned on a valid record.
     * @return the "Constant" supplied by the constructior if a "constant"
     * expression, the value of the scan's current "record.fieldName" if a "field
     * name" expression
     */
    public Constant evaluate(Scan scan) {
        return (val != null) ? val : scan.getVal(fldname);
    }

    /** Return true iff the expression references a field name, false otherwise.
     *
     * @return true iff the expression denotes a field name.
     */
    public boolean isFieldName() {
        return fldname != null;
    }

    /** If a "Constant" expression, returns the constant, else returns null.
     *
     * @return the expression as a constant or null, as appropriate
     */
    public Constant asConstant() {
        return val;
    }

    /** If a "Field Name" expression, returns the field name, else returns null.
     *
     * @return the expression as a field name or null, as appropriate.
     */
    public String asFieldName() {
        return fldname;
    }

    /** Determine if all of the fields mentioned in this expression are
     * contained in the specified schema.
     *
     * @param schema the schema
     * @return if a "constant" expression, returns true; if a "field name"
     * expression, returns true iff the field name is in the schema
     */
    public boolean appliesTo(SchemaBase schema) {
        return (val != null) ? true : schema.hasField(fldname);
    }

    @Override
    public boolean equals(Object obj) {
        // semantics for Expression identity depend on whether it's encapsulating a
        // field name or a value
        if (obj == this) {
            return true;
        }

        // "null instanceof [type]" also returns false
        if (!(obj instanceof Expression)) {
            return false;
        }

        final Expression that = (Expression) obj;
        if (this.isFieldName() != that.isFieldName()) {
            return false;
        }

        if (this.isFieldName()) {
            return this.asFieldName().equals(that.asFieldName());
        }
        else {                      // a constant
            return this.asConstant().equals(that.asConstant());
        }
    } // equals

    @Override
    public int hashCode() {
        if (isFieldName()) {
            return asFieldName().hashCode();
        }
        else {
            return asConstant().hashCode();
        }
    }


    @Override
    public String toString() {
        return (val != null) ? val.toString() : fldname;
    }

    private Constant val = null;
    private String fldname = null;
}