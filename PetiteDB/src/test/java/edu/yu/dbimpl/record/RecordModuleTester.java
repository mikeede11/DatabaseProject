package edu.yu.dbimpl.record;

import edu.yu.dbimpl.buffer.BufferMgr;
import edu.yu.dbimpl.buffer.BufferMgrBase;
import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.log.LogMgr;
import edu.yu.dbimpl.log.LogMgrBase;
import edu.yu.dbimpl.tx.Transaction;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class RecordModuleTester {
    private final int BLOCK_SIZE = 4096;

    FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);

    @Test
    public void generalRecordModuleTest(){
        LogMgrBase lm = new LogMgr(manager, "logFile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 5, 5000);
        //define simple schema an int and a string (keep track of field names)
        SchemaBase schema = new Schema();
        String studentFieldName = "Student Name";
        String  studentIDFieldName = "YUID";
        schema.addStringField(studentFieldName, 20);//my name is like 15 Michael Edelman
        schema.addIntField(studentIDFieldName);
        //construct a layout object from that schema
        LayoutBase layout = new Layout(schema);
        //create a tx
        Transaction tx = new Transaction(manager, lm, bm);
        tx.append("testFile.txt");
        BlockIdBase blk = new BlockId("testFile.txt", 0);
        //with all those pieces create a recordPage
        RecordPageBase recPage = new RecordPage(tx, blk, layout);
        //insert a record at the start
        recPage.insertAfter(-1);
        //find the record (next)
        int slot = recPage.nextAfter(-1);
        //with that slot # set the field names
        String expectedStudentNameValue = "Michael Edelman";
        int expectedIDValue = 800508416;
        recPage.setInt(slot, studentIDFieldName, expectedIDValue);
        recPage.setString(slot, studentFieldName, expectedStudentNameValue);
        //with that slot # get the field names
        String actualNameVal = recPage.getString(slot, studentFieldName);
        int actualIDVal = recPage.getInt(slot, studentIDFieldName);
        //assert they ere as expected
        Assert.assertEquals(expectedIDValue, actualIDVal);
        Assert.assertEquals(expectedStudentNameValue, actualNameVal);
        //delete the record
        recPage.delete(slot);
        //than try to get the rec/slot # with next()
        slot = recPage.nextAfter(-1);
        //make sure its -1
        Assert.assertEquals(-1, slot);
    }

    //tests needed to test iteration, setting in the middle of a block, when theres no room in the block
}
