package edu.yu.dbimpl.record;

import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.tx.TxBase;

import java.sql.Types;

public class RecordPage extends RecordPageBase {
    private TxBase tx;
    private BlockIdBase blk;
    private LayoutBase layout;
    private SchemaBase schema;
    /**
     * Constructor
     *
     * @param tx     Defines the transaction scope in which operations on the block
     *               will take place
     * @param blk    The block in which the record is stored
     * @param layout Holds the physical and logical record schema
     */
    public RecordPage(TxBase tx, BlockIdBase blk, LayoutBase layout) {
        super(tx, blk, layout);
        this.tx = tx;
        this.blk = blk;
        this.layout = layout;
        this.schema = layout.schema();


        //NOTE WE ASSUME THE TX DOES NOT PIN THE BLOCK BEFORE HAND EITHER WAY BETTER SAFE THAN SORRY
    }

    //TODO FOR GET METHODS WHERE THE RECORD IS DELETED WHAT DO WE RETURN??? THIS WAS NOT SETTLED ON PIAZZA

    @Override
    public int getInt(int slot, String fldname) {
        if(!schema.hasField(fldname) || schema.type(fldname) != Types.INTEGER){
            throw new IllegalArgumentException("This field doesnt exist or this is not an integer field!");
        }
        tx.pin(blk);
        int val = tx.getInt(blk, (layout.slotSize() * slot) + layout.offset(fldname));
        tx.unpin(blk);
        return val;

    }

    @Override
    public String getString(int slot, String fldname) {
        if(!schema.hasField(fldname) || schema.type(fldname) != Types.VARCHAR){
            throw new IllegalArgumentException("This field doesnt exist or this is not an String field!");
        }

        tx.pin(blk);
        String val = tx.getString(blk, (layout.slotSize() * slot) + layout.offset(fldname));
        tx.unpin(blk);
        return val;
    }

    @Override
    public void setInt(int slot, String fldname, int val) {
        tx.pin(blk);
        if(tx.getInt(blk, (layout.slotSize() * slot)) == 1) {
            tx.setInt(blk, (layout.slotSize() * slot) + layout.offset(fldname), val, true);
        }
        tx.unpin(blk);
    }

    @Override
    public void setString(int slot, String fldname, String val) {
        tx.pin(blk);
        //TODO DO WE NEED TO PAD IN VAL.LENGTH < MAXLENGTH?
        if(tx.getInt(blk, (layout.slotSize() * slot)) == 1) {
            tx.setString(blk, (layout.slotSize() * slot) + layout.offset(fldname), val, true);
        }
        tx.unpin(blk);
    }

    @Override
    public void delete(int slot) {
        tx.pin(blk);
        tx.setInt(blk, layout.slotSize() * slot, 0, true);//TODO LOG THIS OR NOT?
        tx.unpin(blk);
    }

    @Override
    public void format() {
        //literally set every int, string , and use bit to zero/empty in this blk
        int slotsThatCanFitInABlock = (tx.blockSize()/layout.slotSize());
        int startOffset = 0;
        int currentOffset = 0;
        tx.pin(blk);
        for (int slot = 0; slot < slotsThatCanFitInABlock; slot++) {
            startOffset = slot * layout.slotSize();
            tx.setInt(blk, startOffset, 0, false);//set in use Integer to 0 = not in use.
            for (String field: schema.fields()) {
                currentOffset = startOffset + layout.offset(field);
                if(schema.type(field) == Types.INTEGER){
                    tx.setInt(blk, currentOffset, 0, false);
                }
                else if(schema.type(field) == Types.VARCHAR){
                    tx.setString(blk, currentOffset, "", false);
                }
            }
        }
        tx.unpin(blk);
    }

    @Override
    public int nextAfter(int slot) {
        int slotIndex = slot == -1 ? 0 : slot;
        int nextInUseSlot = -1;
        //TODO HOW ARE SLOTS INDEXED!!!???
        int slotsThatCanFitInABlock = tx.blockSize() / layout.slotSize();
        tx.pin(blk);
        for (; slotIndex < slotsThatCanFitInABlock; slotIndex++) {
            if(tx.getInt(blk, slotIndex * layout.slotSize()) == 1){
                nextInUseSlot = slotIndex;//IT IS SLOT NUMBER NOT OFFSET
                break;
            }
        }
        tx.unpin(blk);
        return nextInUseSlot;
    }

    @Override
    public int insertAfter(int slot) {
        int slotIndex = slot == -1 ? 0 : slot;
        int nextAvailableSlot = -1;
        //TODO HOW ARE SLOTS INDEXED!!!??? A - pretty positive 0 indexing
        int slotsThatCanFitInABlock = tx.blockSize() / layout.slotSize();
        tx.pin(blk);
        for (; slotIndex < slotsThatCanFitInABlock; slotIndex++) {
            if(tx.getInt(blk, slotIndex * layout.slotSize()) == 0){
                nextAvailableSlot = slotIndex;//IT IS SLOT NUMBER NOT OFFSET
                tx.setInt(blk, slotIndex * layout.slotSize(), 1, true);//OKTOLOG???//SETS TO IN USE
                break;
            }
        }
        tx.unpin(blk);
        return nextAvailableSlot;
    }

    @Override
    public BlockIdBase block() {
        return blk;
    }
}
