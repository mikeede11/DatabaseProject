package edu.yu.dbimpl.query;

/** A Term is a comparison between two Expressions (see the lecture that
 * defines the current restrictions on what must be supported).  The class is
 * immutable by design.
 *
 * Students MAY NOT modify this class in any way!
 */

import java.util.Objects;

public class Term {
    /** Constructor that creates a term that compares the supplied two
     * expressions for equality.
     *
     * @param lhs  the LHS expression
     * @param rhs  the RHS expression
     */
    public Term(final Expression lhs, final Expression rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    /** Return true iff both of the term's expressions evaluate to the same
     * constant, with respect to the specified scan, false otherwise.
     *
     * @param scan the scan
     * @return true if both expressions have the same value in the scan
     * @see Expression#evaluate
     */
    public boolean isSatisfied(final Scan scan) {
        final Constant lhsval = lhs.evaluate(scan);//1st table - which is in invalid state
        final Constant rhsval = rhs.evaluate(scan);
        return rhsval.equals(lhsval);
    }

    @Override
    public String toString() {
        return lhs.toString() + "=" + rhs.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        // "null instanceof [type]" also returns false
        if (!(obj instanceof Term)) {
            return false;
        }

        final Term that = (Term) obj;
        return this.rhs.equals(that.rhs) && this.lhs.equals(that.lhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rhs, lhs);
    }

    // package protected, safe to do so because "immutable"
    final Expression lhs, rhs;
}