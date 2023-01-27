package edu.yu.dbimpl.query;

import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.record.RID;
import edu.yu.dbimpl.record.TableScan;

import java.util.logging.Logger;

public class SelectScan extends SelectScanBase {
    private Scan scan;
    private Predicate pred;
    private boolean updateable;
    private Logger ssLogger;
    /**
     * Constructor
     * <p>
     * Create a select scan having the specified underlying
     * scan and predicate.
     *
     * @param scan      the scan representing the input relation
     * @param predicate the selection predicate that will be applied to filter
     */
    public SelectScan(Scan scan, Predicate predicate) {
        super(scan, predicate);
        if(scan instanceof TableScan){
            this.updateable = true;
            this.ssLogger = Logger.getLogger(FileMgr.class.getName());

        }
        this.scan = scan;
        this.pred = predicate;

    }
//MAJOR NOTE:OK VERY TRICKY ISSUE: IT SOUNDS LIKE FROM SLIDE 30 THAT EACH SET AND GET METHOD SHOULD THROW A CLASS CAST EXCEPTION
    //IF CALLED ON A
    @Override
    public void setVal(String fldname, Constant val) {//same as table scan
        if(!updateable){ throw  new ClassCastException();}
        ((TableScan)scan).setVal(fldname,val);
    }

    @Override
    public void setInt(String fldname, int val) {//same as table scan
        if(!updateable){ throw  new ClassCastException();}
        ((TableScan)scan).setInt(fldname,val);
    }

    @Override
    public void setString(String fldname, String val) {//same as table scan
        if(!updateable){ throw  new ClassCastException();}
        ((TableScan)scan).setString(fldname,val);
    }

    @Override
    public void insert() {//i think same as table scan?
        //TODO how should this work? - insert and then internally call next?
        if(!updateable){ throw  new ClassCastException();}
        ((TableScan)scan).insert();
    }

    @Override
    public void delete() {//same
        if(!updateable){ throw  new ClassCastException();}
        ((TableScan)scan).delete();
    }

    @Override
    public RID getRid() {//same
        if(!updateable){ throw  new ClassCastException();}
        return  ((TableScan)scan).getRid();
    }

    @Override
    public void moveToRid(RID rid) {//similar but throw exception if not allowed
        if(!updateable){ throw  new ClassCastException();}
        ((TableScan)scan).moveToRid(rid);
        //TODO WHAT IF WE MOVE TO ILLEGAL RID?
    }

    @Override
    public void beforeFirst() {//same as tbl
        scan.beforeFirst();
    }

    @Override
    public boolean next() {//main dif
        ssLogger.info("SelectScan next() executing");
        while(scan.next()){//SO THIS WILL RETURN TRUE EVEN WHEN the first tbl hasnt been moved into a valid pos
            if(pred.isSatisfied(scan)){//and this will execute
                ssLogger.info("SelectScan.next() - predicate was satisfied");
                return true;
            }
        }
        ssLogger.info("SelectScan.next() - predicate was NOT satisfied");
        return false;
    }
/**For the below 4 methods we are assuming that they will only be called by the client when the scan is in a legal state
 * In other words the client will not call moveToRid()  to an illegal spot and then call these methods - or the set ones as well**/
    @Override
    public int getInt(String fldname) {//same as table scan
        return scan.getInt(fldname);
    }

    @Override
    public String getString(String fldname) {//same as table scan
        return scan.getString(fldname);
    }

    @Override
    public Constant getVal(String fldname) {//same as table scan
        return scan.getVal(fldname);
    }

    @Override
    public boolean hasField(String fldname) {
        return scan.hasField(fldname);
    }

    @Override
    public void close() {
        scan.close();
    }
}
