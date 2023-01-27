package edu.yu.dbimpl.record;

import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.query.Constant;
import edu.yu.dbimpl.tx.Transaction;
import edu.yu.dbimpl.tx.TxBase;

import java.sql.Types;
import java.util.logging.Logger;

public class TableScan extends TableScanBase  {
    private TxBase tx;
    private String table;
    private SchemaBase schema;
    private LayoutBase layout;
    private RecordPage currentRecBlock;
    private int currentRecord;
    private BlockIdBase blk;
    private boolean closed;
    private Logger tblScanLogger;
    /**
     * Constructor
     *
     * @param tx      Defines the transactional scope under which the scan operations
     *                will take place
     * @param tblname Names of the table over which the scan will be performed
     * @param layout  Defines the logical and physical schema of the
     */
    public TableScan(TxBase tx, String tblname, LayoutBase layout) {
        super(tx, tblname, layout);
        this.tx = tx;
        this.table = tblname;
        this.layout = layout;
        this.schema = layout.schema();
        this.blk = new BlockId(tblname, 0);//NOTE THIS BLK IS TREATED AS THE HEADER BLOCK
        this.currentRecBlock = new RecordPage(tx, blk, layout);
        this.tblScanLogger = Logger.getLogger(FileMgr.class.getName());
        if(tx.size(tblname ) == 0){
            tx.append(tblname);
        }
        //we only touch the header when somethings been deleted. from a file perspective the oth slot of the 0th block (the absolute 0th record) is reserved for the header.
        //therefore we will always start our current record at slot -1 to communicate to other methods where we are.
        this.currentRecord = -1;//because we need to reserve the first
        // I SUGGEST checking in use flag for header in constructor if 1 - it was already set else insert that record and set vals to zero
        tx.pin(blk);
        int headerFlag = tx.getInt(blk, 0);
        tx.unpin(blk);
        if(headerFlag != 1){
            currentRecBlock.insertAfter(-1);//these methods unpin the blk so need to repin
            tx.pin(blk);
            tx.setInt(blk, Integer.BYTES, -1, true);//set vals to -1 to signal none in free list.
            tx.setInt(blk, Integer.BYTES + Integer.BYTES, -1, true);
            tx.unpin(blk);
            tblScanLogger.info("TS Set up header for this file since there was none");
        }



    }
    // TODO DOESNT THIS ALSO HAVE TO IMPLEMENT SCAN METHODS?
    @Override
    public void setVal(String fldname, Constant val) {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){
            throw new IllegalStateException("pointer is in undefined state - you can try to invoke methods like next() if you are before the first record. or maybe there are no records in the first place try insert(). Or It could be you are in an invalid block due to next() reaching the end. invoke beforeFirst or moveToRID() to fix");
        }
        tblScanLogger.info("TS setVal()");
        if(schema.type(fldname) == Types.INTEGER){
            setInt(fldname, val.asInt());
        }
        else if(schema.type(fldname) == Types.VARCHAR){
            setString(fldname, val.asString());
        }
    }

    @Override
    public void setInt(String fldname, int val) {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){
            throw new IllegalStateException("pointer is in undefined state - you can try to invoke methods like next() if you are before the first record. or maybe there are no records in the first place try insert(). Or It could be you are in an invalid block due to next() reaching the end. invoke beforeFirst or moveToRID() to fix");
        }
        tblScanLogger.info("TS setInt() " + val + " for field \"" + fldname + "\"");
        this.currentRecBlock.setInt(currentRecord, fldname, val);
    }

    @Override
    public void setString(String fldname, String val) {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){
            throw new IllegalStateException("pointer is in undefined state - you can try to invoke methods like next() if you are before the first record. or maybe there are no records in the first place try insert(). Or It could be you are in an invalid block due to next() reaching the end. invoke beforeFirst or moveToRID() to fix");
        }
        tblScanLogger.info("TS setString() " + val + " for field \"" + fldname + "\"");
        this.currentRecBlock.setString(currentRecord, fldname, val);

    }

    //this method changes the currentRec
    @Override
    public void insert() {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        //check header - if value other than zero get slot from freelist
        RID recToInsert = getSlotFromFreeList();
        if(recToInsert != null){//that means theres a spot on the freelist
            tblScanLogger.info("TS insert() from a spot on the freelist");
            if(currentRecBlock.block().number() != recToInsert.blockNumber()) {
                currentRecBlock = new RecordPage(tx, new BlockId(table, recToInsert.blockNumber()), layout);
            }
            currentRecord = recToInsert.slot();
            currentRecBlock.insertAfter(currentRecord);// we know this is available by definition if its on the freelist
        }
        else{//if you cant get a spot from free list just try to put it at the next available slot after current slot - either within same block or next
//            int fileSize = tx.size(table);
//            currentRecBlock = new RecordPage(tx, new BlockId(table, fileSize - 1), layout);
//            if(currentRecBlock.block().equals(blk) && currentRecord == -1){//if our position is 0,0
//                currentRecord = 1;
//            }
            tblScanLogger.info("TS insert() at next open slot");
            currentRecord = currentRecBlock.insertAfter(currentRecord + 1);//even if -1 since header is set it will skip 0 slot

            if(currentRecord == -1){//if we were unable to insert try to append, format, and put it there - use -1 since new block
                tblScanLogger.info("TS hmm it seems theres no spots in this block lets append a new block");
                //append/unpin/commit
                currentRecBlock = new RecordPage(tx, tx.append(table), layout);//get a record page for the new block
                currentRecBlock.format();
                if(currentRecBlock.block().equals(blk) && currentRecord == -1){//special case skip header
                    currentRecord = 1;
                }
                currentRecord = currentRecBlock.insertAfter(currentRecord);
            }
        }
        //else get next spot
        ;//TODO upon insertion you should change or b4 you need to set the prev guy blocknum and slotnum to zero
    }

    /** This method will deal directly with the tx because a recPage only speaks field names
     * and the blockNum and slotNum Integers we are looking at in the free list are not under
     * field names because of design decisions - we just internally are aware that they are the
     * 2nd integer and 3rd integer offset respectively for each slot **/
    private RID getSlotFromFreeList(){
        BlockIdBase headerBlk = blk;
        BlockIdBase otherBlk = headerBlk;
        tx.pin(headerBlk);
        int blockNumOfFreeSlot = tx.getInt(headerBlk, Integer.BYTES);
        int slotNumOfFreeSlot = tx.getInt(headerBlk, Integer.BYTES + Integer.BYTES);
        if(blockNumOfFreeSlot != -1){
            if(headerBlk.number() != blockNumOfFreeSlot){
                otherBlk = new BlockId(table, blockNumOfFreeSlot);
                tx.pin(otherBlk);
            }
            int otherBlkNum = tx.getInt(otherBlk, (layout.slotSize() * slotNumOfFreeSlot) + Integer.BYTES);
            int otherSlotNum = tx.getInt(otherBlk, (layout.slotSize() * slotNumOfFreeSlot) + Integer.BYTES + Integer.BYTES);
            //whether the slot that we are giving away is the end (-1) or not we should set the vals to 0
            tx.setInt(otherBlk, (layout.slotSize() * slotNumOfFreeSlot) + Integer.BYTES, 0, true);
            tx.setInt(otherBlk, (layout.slotSize() * slotNumOfFreeSlot) + Integer.BYTES + Integer.BYTES, 0, true);
            //ok now just put the vals of the next available slot (or -1) in the header
            tx.setInt(headerBlk, Integer.BYTES, otherBlkNum, true);
            tx.setInt(headerBlk, Integer.BYTES + Integer.BYTES, otherSlotNum, true);
            tx.unpin(headerBlk);
            if(!headerBlk.equals(otherBlk)) {
                tx.unpin(otherBlk);
            }
            return new RID(blockNumOfFreeSlot, slotNumOfFreeSlot);
        }
        else{
            tx.unpin(headerBlk);
            return null;
        }
        //and when you put it in from free list make sure to A) if header does not have value of -1
        // save the blk and slot its pointing to and then get the blk and slot from that record and put it in the header.
        // if the block itself had a value of lrdf (-1) then make sure to set that to regular (0) before
        //put that slots info in the header


    }

    @Override
    public void delete() {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){
            throw new IllegalStateException("pointer is in undefined state - you can try to invoke methods like next() if you are before the first record. or maybe there are no records in the first place try insert(). Or It could be you are in an invalid block due to next() reaching the end. invoke beforeFirst or moveToRID() to fix");
        }
        BlockIdBase curBlock = currentRecBlock.block();
        int blockNum = curBlock.number();
        int slotNum = currentRecord;//tx.getInt(  curBlock,(layout.slotSize() * currentRecord) + Integer.BYTES + Integer.BYTES);
        tblScanLogger.info("TS delete() current record " + slotNum + " in block " + curBlock);
        tx.pin(curBlock);
        //set this record to have the lrdf symbol
        tx.setInt(curBlock, (currentRecord * layout.slotSize()) + Integer.BYTES, -1, true);//-1 is lrdf
        tx.unpin(curBlock);
        currentRecBlock.delete(currentRecord);
        putAtEndOfFreeList(blockNum, currentRecord);// (and also remove lrdf symbol and move it - yeah does this by default)

        //make sure to initialize correctly
        //thats it I believe this is the entire specification
        //also implement close()
        //jeez that was tricky
    }

    private void putAtEndOfFreeList(int blkNum, int slotNum){
        BlockIdBase currentBlk = blk;//start at head
        tx.pin(currentBlk);//pin incase not already pinned
        //tblName is file name
        int currentSlotNum = tx.getInt(currentBlk, Integer.BYTES * 2);//check slotNum
        int previousSlotNum = currentSlotNum;
        int blockNum = tx.getInt(currentBlk, Integer.BYTES);//check blkNum
        int prevBlkNum = blockNum;

        //ok so i feel the most recently deleted file should have its values set to -1 to signal it is the lrdf - which means its the end of the chain
        while(blockNum != -1){//while we didnt get to the last del slot in FL
            if(currentBlk.number() != blockNum) {//if the blk we got on last iteration is different than our current one - update it.
                tx.unpin(currentBlk);//unpin old block we are done with it.
                currentBlk = new BlockId(table, blockNum);//update curBlk
                tx.pin(currentBlk);//pin it
            }
            prevBlkNum = blockNum;//before we get values to check out new rec save the old vals.
            blockNum = tx.getInt(currentBlk, (layout.slotSize() * currentSlotNum) + Integer.BYTES);//getting block val
            previousSlotNum = currentSlotNum;
            currentSlotNum = tx.getInt(currentBlk, (layout.slotSize() * currentSlotNum) + (Integer.BYTES * 2));//slot val
        }
        //if we are here it means we got to end of list in which case blkNum should be -1 and prevBlk and prev Slot will be the record that should put the arguments in as their info
        //if prevBlkNum == -1 that means that the list was empty and all we need to do is put the arg values as blkNum and slotNum in header.
        if(prevBlkNum == -1){
            //already pinned from above
            tx.setInt(currentBlk, Integer.BYTES, blkNum, true);
            tx.setInt(currentBlk,Integer.BYTES + Integer.BYTES, slotNum, true);
        }else{
            BlockIdBase prevBlk = new BlockId(table, prevBlkNum);
            tx.setInt(prevBlk, (previousSlotNum * layout.slotSize()) + Integer.BYTES, blkNum, true);
            tx.setInt(prevBlk, (previousSlotNum * layout.slotSize()) + Integer.BYTES + Integer.BYTES, slotNum, true);
        }

        //otherwise if prevBlkNum is an actual value then put the arg values in the prevBlk and prevSlot record which also btw gets rid of the -1 in that slot which would signal its is the lrdf - but its not now rather the one it points to now which was set in delete()
        tx.unpin(currentBlk);



    }

    @Override
    public RID getRid() {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){
            throw new IllegalStateException("pointer is in undefined state - you can try to invoke methods like next() if you are before the first record. or maybe there are no records in the first place try insert(). Or It could be you are in an invalid block due to next() reaching the end. invoke beforeFirst or moveToRID() to fix");
        }
        return new RID(currentRecBlock.block().number(), currentRecord);
    }

    //this method changes the currentRec
    @Override
    public void moveToRid(RID rid) {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(rid.blockNumber() != currentRecBlock.block().number()){
            currentRecBlock = new RecordPage(tx, new BlockId(table, rid.blockNumber()), layout);
        }
        currentRecord = rid.slot();
        tblScanLogger.info("TS moveToRid() - currentRecord at " + currentRecord);
    }

    //this method changes the currentRec
    @Override
    public void beforeFirst() {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        //NOTE the first record may not be in the first block if client deleted all of records in 1st block
        //in this case we need to make sure we are at right - eh well it just says it need to be beforeFirst such
        //that a call to next will return true yeah theres another record - so if we make it that next traverses blocks in file
        //this will happen regardless
        //we can initially
        if(!blk.equals(currentRecBlock.block())){//if we are not at the header block
            currentRecBlock = new RecordPage(tx, blk, layout);//put us at the header block
        }
        currentRecord = -1;//will signal to next how to navigate. why cant we just set it at 0,1 - b/c then we'll never go anywhere but 0,1
        //yeah -1 is good place because then we know exactly our situation when next() is called and it can give us the first record
        //which is a bit different + 1 due to header
        tblScanLogger.info("TS beforeFirst() - currentRecord at -1");
    }


    //this method changes the currentRec
    @Override
    public boolean next() {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){//oh we must be in block 0 before first - ok lets set curRec to 1 so that we do not process the header rec
            currentRecord = 1;
        }
        else{
            currentRecord += 1;//to go and consider the next record
        }
//        int slot = -1;
        int fileSize = tx.size(table);
        for(; currentRecBlock.block().number() < fileSize; currentRecBlock = new RecordPage(tx, new BlockId(table, currentRecBlock.block().number() + 1), layout)){
            currentRecord = currentRecBlock.nextAfter(currentRecord);//ok prob next record not also considering current one then no progression.
            if(currentRecord != -1){
                //we found a valid rec spot and cursor is now there
                return true;
            }
        }

        return false;//if there is no valid record the current record will be -1 ewhich will be idempotent for next() and good for insert()
        //TODO I DO NOT KNOW WHAT SHOULD HAPPEN IF NO RECORD FOUND AND NEXT WAS EXECUTED - B/C CURRENTRECBLOCK WILL BE AT AN INVALID POSITION
        // SHOULD WE JUST RESET IT TO -1 OR UGH IDK IDC.
    }

    @Override
    public int getInt(String fldname) {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){
            throw new IllegalStateException("pointer is in undefined state - you can try to invoke methods like next() if you are before the first record. or maybe there are no records in the first place try insert(). Or It could be you are in an invalid block due to next() reaching the end. invoke beforeFirst or moveToRID() to fix");
        }
        return currentRecBlock.getInt(currentRecord, fldname);
    }

    @Override
    public String getString(String fldname) {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){
            throw new IllegalStateException("pointer is in undefined state - you can try to invoke methods like next() if you are before the first record. or maybe there are no records in the first place try insert(). Or It could be you are in an invalid block due to next() reaching the end. invoke beforeFirst or moveToRID() to fix");
        }
        return currentRecBlock.getString(currentRecord, fldname);
    }

    @Override
    public Constant getVal(String fldname) {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        if(currentRecord == -1){//Ah yes
            throw new IllegalStateException("pointer is in undefined state - you can try to invoke methods like next() if you are before the first record. or maybe there are no records in the first place try insert(). Or It could be you are in an invalid block due to next() reaching the end. invoke beforeFirst or moveToRID() to fix");
        }
        if(schema.type(fldname) == Types.INTEGER){
            return new Constant(getInt(fldname));
        }
        else{//must be a varchar/string
            return new Constant(getString(fldname));
        }
        //We dont prepare for case where either no fldName or unsupported type - we just assume its either int or string
    }

    @Override
    public boolean hasField(String fldname) {
        if(closed){throw new IllegalStateException("This Scan has been terminated");}
        return schema.hasField(fldname);
    }

    @Override
    public void close() {
        if(closed){throw new IllegalStateException("This Scan has already been terminated");}
        closed = true;
        //tx.commit()??? - eh we dont want to couple it that tightly

    }
}
