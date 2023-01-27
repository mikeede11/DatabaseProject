package edu.yu.dbimpl.query;

/** A "value class" encapsulating a single database value.
 *
 * As a value class, Constant overrides the necessary base class methods
 * appropriately.
 *
 * Students MAY NOT modify this class in any way!
 */

public class Constant implements Comparable<Constant> {
    private Integer ival = null;
    private String  sval = null;

    public Constant(Integer ival) {
        this.ival = ival;
    }

    public Constant(String sval) {
        this.sval = sval;
    }

    public int asInt() {
        return ival;
    }

    public String asString() {
        return sval;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        // "null instanceof [type]" also returns false
        if (!(obj instanceof Constant)) {
            return false;
        }

        final Constant c = (Constant) obj;
        return (ival != null) ? ival.equals(c.ival) : sval.equals(c.sval);
    }

    @Override
    public int compareTo(Constant c) {
        return (ival != null) ? ival.compareTo(c.ival) : sval.compareTo(c.sval);
    }

    @Override
    public int hashCode() {
        return (ival != null) ? ival.hashCode() : sval.hashCode();
    }

    @Override
    public String toString() {
        return (ival != null) ? "Integer: "+ival.toString()
                : "String: "+sval.toString();
    }
}