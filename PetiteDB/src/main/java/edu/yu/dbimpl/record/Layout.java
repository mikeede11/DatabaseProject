package edu.yu.dbimpl.record;

import edu.yu.dbimpl.file.Page;
import edu.yu.dbimpl.file.PageBase;

import javax.swing.*;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

public class Layout extends LayoutBase {
    private Map<String, Integer> fldnameToOffset;
    private int slotSize;
    private SchemaBase schema;

    public Layout(SchemaBase schema) {
        super(schema);
        fldnameToOffset = new HashMap<>();
        //to store the in use/empty integer at the beginning of each record, the Block # and slot # of this record
        slotSize = Integer.BYTES + Integer.BYTES + Integer.BYTES;
        //regular fields from schema
        for (String field: schema.fields()) {
            //dont worry about double fields - impossible b/c keyset from a hashmap in Schema
            fldnameToOffset.put(field, slotSize);
            if(schema.type(field) == Types.INTEGER){
                slotSize += Integer.BYTES;
            }
            else if(schema.type(field) == Types.VARCHAR){
                slotSize += PageBase.maxLength(schema.length(field));//TODO does each string specify its length???
            }
        }

        this.schema = schema;
    }

    public Layout(SchemaBase schema, Map<String,Integer> offsets, int slotsize)
    {
        super(schema);
        this.schema = schema;
        this.fldnameToOffset = offsets;
        this.slotSize = slotsize;
    }

    @Override
    public SchemaBase schema() {
        return this.schema;
    }

    @Override
    public int offset(String fldname)
    {
        if(fldnameToOffset.get(fldname) == null){ throw new IllegalArgumentException();}
        return fldnameToOffset.get(fldname);
    }

    @Override
    public int slotSize() {
        return slotSize;
    }
}
