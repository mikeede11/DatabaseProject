package edu.yu.dbimpl.record;

import edu.yu.dbimpl.buffer.BufferMgr;
import edu.yu.dbimpl.buffer.BufferMgrBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.log.LogMgr;
import edu.yu.dbimpl.log.LogMgrBase;
import edu.yu.dbimpl.tx.Transaction;
import edu.yu.dbimpl.tx.TxBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class TableScanTester {
    private final int BLOCK_SIZE = 4096;

    FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);

    @Test
    public void basicTableScanTest(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 10, 3000);
        SchemaBase schema = new Schema();
        String studentFieldName = "Student Name";
        String  studentIDFieldName = "YUID";
        schema.addStringField(studentFieldName, 20);//my name is like 15 Michael Edelman
        schema.addIntField(studentIDFieldName);
        //construct a layout object from that schema
        LayoutBase layout = new Layout(schema);
        TxBase tx = new Transaction(manager, lm, bm);
        String table = "StudentTable.txt";
        tx.append(table);
        TableScanBase tblScan = new TableScan(tx, table, layout);
        for (int i = 0; i < 5; i++) {
            tblScan.insert();//insert 5 records
        }
        tblScan.beforeFirst();
        String[] students = {"Michael Edelman", "Shai Vadnai", "Zach Hamburger", "Nathaniel Silverman", "Moshe Berk"};
        int[] YUIDs = {800508416, 800508417, 800508418, 800508419, 800508420};
        int index = 0;
        while(tblScan.next()){
            tblScan.setString(studentFieldName, students[index]);
            tblScan.setInt(studentIDFieldName, YUIDs[index]);
            index++;
        }
        index = 0;
        tblScan.beforeFirst();
        while(tblScan.next()){
            Assert.assertEquals(students[index], tblScan.getString(studentFieldName));
            Assert.assertEquals(YUIDs[index], tblScan.getInt(studentIDFieldName));
            index++;

        }
        RID testRecord = new RID(0, 3);
        tblScan.moveToRid(testRecord);
        System.out.println("OVER HERE" + tblScan.getRid().slot());

        tblScan.delete();
        System.out.println("OVER HERE" + tblScan.getRid().slot());
        tblScan.insert();//shouldnt we now be at that record?//OH YEAH THATS GOOD
        System.out.println("OVER HERE" + tblScan.getRid().slot());

        tblScan.setString(studentFieldName, "Nathan Edelman");
        tblScan.setInt(studentIDFieldName, 312307859);
        System.out.println("OVER HERE" + tblScan.getRid().slot()); //3

        Assert.assertEquals("Nathan Edelman", tblScan.getString(studentFieldName));
        Assert.assertEquals(312307859, tblScan.getInt(studentIDFieldName));
        tblScan.moveToRid(testRecord);
        Assert.assertEquals("Nathan Edelman", tblScan.getString(studentFieldName));
        Assert.assertEquals(312307859, tblScan.getInt(studentIDFieldName));
        tblScan.setString(studentFieldName, "Zach Hamburger");
        tblScan.setInt(studentIDFieldName, 800508418);
        tblScan.beforeFirst();
        index = 0;
        while(tblScan.next()){
            Assert.assertEquals(students[index], tblScan.getString(studentFieldName));
            Assert.assertEquals(YUIDs[index], tblScan.getInt(studentIDFieldName));
            index++;
        }

        //test delete and then free list function - good

        tblScan.close();
        tx.commit();
        //delete
        //getRID
        //moveToRID
        //hasfield


    }

    @Test
    public void testAcrossBlocksFunction(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 10, 3000);
        SchemaBase schema = new Schema();
        String studentFieldName = "Student Name";
        String  studentIDFieldName = "YUID";
        schema.addStringField(studentFieldName, 20);//my name is like 15 Michael Edelman
        schema.addIntField(studentIDFieldName);
        //construct a layout object from that schema
        LayoutBase layout = new Layout(schema);
        TxBase tx = new Transaction(manager, lm, bm);
        String table = "StudentTable1.txt";
        tx.append(table);
        TableScanBase tblScan = new TableScan(tx, table, layout);
        for (int i = 0; i < 4000; i++) {
            tblScan.insert();//insert 4000 records - across multiple blocks
        }
        tblScan.beforeFirst();
        String[] students = {"Michael Edelman", "Shai Vadnai", "Zach Hamburger", "Nathaniel Silverman", "Moshe Berk"};
        int[] YUIDs = {800508416, 800508417, 800508418, 800508419, 800508420};
        int index = 0;
        while(tblScan.next()){
            if(index == 5){
                index = 0;
            }
            tblScan.setString(studentFieldName, students[index]);
            tblScan.setInt(studentIDFieldName, YUIDs[index]);
            index++;
        }
        tblScan.beforeFirst();
        while(tblScan.next()){
            if(index == 5){
                index = 0;
            }
            Assert.assertEquals(students[index], tblScan.getString(studentFieldName));
            Assert.assertEquals(YUIDs[index], tblScan.getInt(studentIDFieldName));
            index++;

        }

        tblScan.close();
        tx.commit();
    }
}
