package edu.yu.dbimpl.query;

import edu.yu.dbimpl.file.FileMgr;

import java.util.List;
import java.util.logging.Logger;

public class ProjectScan extends ProjectScanBase {
    private Scan scan;
    private List<String> fields;
    private Logger projLogger;
    /**
     * Constructor.
     * <p>
     * Create a project scan having the specified underlying scan and field
     * list of columns to keep (all other field names will be dropped).
     *
     * @param scan   representing the input relation
     * @param fields list of field names to keep
     */
    public ProjectScan(Scan scan, List<String> fields) {
        super(scan, fields);
        this.scan = scan;
        this.fields = fields;
        this.projLogger = Logger.getLogger(FileMgr.class.getName());
        projLogger.info("ProjectScan created");
    }
//NOTE: QUESTION @110 ON PIAZZA TELLS ME TO THROW A RUNTIMEEXCEPTION WHENEVER A FIELDNAME IS CALLED THAT IS NON-EXISTENT -
    //IM GUESSING THIS MEANS A FIELD NAME THAT IS NOT IN THE LIST OF FIELDS. HOWEVER I DO NOT ASSUME THIS MEANS FIELDS THAT
// DONT EXIST AT ALL B/C THAT IS DEALT WITH BY THE HASFIELD()
    @Override
    public void beforeFirst() {
        scan.beforeFirst();
    }

    @Override
    public boolean next() {
        projLogger.info("ProjectScan next() called");
        return scan.next();
    }

    @Override
    public int getInt(String fldname) {
        if(!fields.contains(fldname)){throw new RuntimeException();}
        return scan.getInt(fldname);
    }

    @Override
    public String getString(String fldname) {
        if(!fields.contains(fldname)){throw new RuntimeException();}
        return scan.getString(fldname);
    }

    @Override
    public Constant getVal(String fldname) {
        if(!fields.contains(fldname)){throw new RuntimeException();}
        return scan.getVal(fldname);
    }

    @Override
    public boolean hasField(String fldname) {
        if(!fields.contains(fldname)){throw new RuntimeException();}
        return scan.hasField(fldname);
    }

    @Override
    public void close() {
        scan.close();
    }
}
