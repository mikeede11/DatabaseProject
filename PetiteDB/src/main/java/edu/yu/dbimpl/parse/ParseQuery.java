package edu.yu.dbimpl.parse;

import edu.yu.dbimpl.query.Predicate;

import java.util.Collection;
import java.util.List;

public class ParseQuery extends ParseQueryBase {
    List<String> fields;
    Collection<String> tables;
    Predicate predicate;
    /**
     * Constructor: saves the parameters.
     *
     * @param fields    the fields specified in the select statement
     * @param tables    the tables specified in the select statement
     * @param predicate the predicate specified in the select statement
     */
    public ParseQuery(List<String> fields, Collection<String> tables, Predicate predicate) {
        super(fields, tables, predicate);
        this.fields = fields;
        this.tables = tables;
        this.predicate = predicate;
    }

    @Override
    public List<String> fields() {
        return fields;
    }

    @Override
    public Collection<String> tables() {
        return tables;
    }

    @Override
    public Predicate predicate() {
        return predicate;
    }
}
