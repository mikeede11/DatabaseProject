package edu.yu.dbimpl.query;

import edu.yu.dbimpl.file.FileMgr;

import java.util.logging.Logger;

public class ProductScan extends ProductScanBase {
    private Scan scan1;
    private Scan scan2;
    private Logger psLogger;
    /**
     * Constructor.
     * <p>
     * Create a product scan having the two underlying scans.
     *
     * @param s1 the left-hand-side scan
     * @param s2 the right-hand-side scan
     */
    public ProductScan(Scan s1, Scan s2) {
        super(s1, s2);
        this.scan1 = s1;
        this.scan2 = s2;
        this.psLogger = Logger.getLogger(FileMgr.class.getName());
        psLogger.info("ProductScan created");
    }

    @Override
    public void beforeFirst() {
        //llhs is AT first
        psLogger.info("ProductScan beforeFirst() began execution");
        scan1.beforeFirst();
        scan1.next();
        scan2.beforeFirst();

//        scan1.beforeFirst();
//        scan2.beforeFirst();
//        scan2.next();
    }

    @Override
    public boolean next() {
        //next scan2 if false then next scan 1 and beforeFirst scan 2
        psLogger.info("ProductScan next() called");
        if(!scan2.next()){//IF THIS RETURNS TRUE AS IT SHOULD THEN SCAN1 WILL NEVER BE CALLED AND CURRENT REC WILL BE -1
            psLogger.info("ProductScan got to end of second table. adjusting pointers accordingly if we are not at the end of the first table");
            scan2.beforeFirst();
            scan2.next();
            return scan1.next();
        }
        return true;
    }

    @Override
    public int getInt(String fldname) {
        if(scan1.hasField(fldname)){
            return scan1.getInt(fldname);
        }
        else{
            return scan2.getInt(fldname);
        }
    }

    @Override
    public String getString(String fldname) {
        if(scan1.hasField(fldname)){
            return scan1.getString(fldname);
        }
        else{
            return scan2.getString(fldname);
        }
    }

    @Override
    public Constant getVal(String fldname) {
        if(scan1.hasField(fldname)){
            return scan1.getVal(fldname);
        }
        else{
            return scan2.getVal(fldname);
        }
    }

    @Override
    public boolean hasField(String fldname) {
        //IF ITS IN EITHER OF THE SCANS
        return scan1.hasField(fldname) || scan2.hasField(fldname);
    }

    @Override
    public void close() {
        scan1.close();
        scan2.close();

    }
}
