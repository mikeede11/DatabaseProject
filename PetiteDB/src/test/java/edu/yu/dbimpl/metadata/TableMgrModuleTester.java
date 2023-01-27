package edu.yu.dbimpl.metadata;

import edu.yu.dbimpl.buffer.BufferMgr;
import edu.yu.dbimpl.buffer.BufferMgrBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.log.LogMgr;
import edu.yu.dbimpl.log.LogMgrBase;
import edu.yu.dbimpl.query.Scan;
import edu.yu.dbimpl.record.*;
import edu.yu.dbimpl.tx.Transaction;
import edu.yu.dbimpl.tx.TxBase;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;

public class TableMgrModuleTester {

    private final int BLOCK_SIZE = 256;
    private final int NUMBER_OF_TABLES = 300;//+ 2 for the metadata ones
    FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);

    /**These two tests work together. The first one BasicMetaDataTester() creates NUMBER_OF_TABLES tables
     * all these tables are basically identical except their names are numbered (StdTable0, StdTable1....StdTableN)
     * Each contain an int and string field - so we get to see if we can manage the different types. With the
     * TableMgr we "create" all these tables by putting them in the MetaData tables. We use a TableScan
     *  to insert a few records and test if they were inserted correctly into the actual tables. The Second test, named
     *  PersistenceTest() basically just tests if the metadata is persisted by going through literally every table in the tblcat
     *  and most fields in the fldcat. These tests are good because by adjusting BLOCK_SIZE and NUMBER_OF_TABLES we can
     *  easily test how the TableMgr and thus MetaData deal with data that fills multiple blocks.**/
    @Test
    public void basicMetaDataTester(){
        //START WITH EMPTY DB
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 10, 3000);
        SchemaBase schema = new Schema();
        String studentFieldName = "Student Name";
        String  studentIDFieldName = "YUID";
        schema.addStringField(studentFieldName, 20);//my name is like 15 Michael Edelman
        schema.addIntField(studentIDFieldName);
        //construct a layout object from that schema
        LayoutBase layout = new Layout(schema);
//        System.out.println(layout.slotSize());
//        for(int i = 0; i < schema.fields().size(); i++){
//            System.out.println(layout.offset(schema.fields().get(i)));
//        }
        TxBase tx = new Transaction(manager, lm, bm);

        TableMgrBase tblMgr = new TableMgr(true, tx);//tblscan twice

        String table = "StdTable";
        TableScanBase tblScan = null;
        for (int i = 0; i < NUMBER_OF_TABLES; i++) {
            tx.append(table + i);
            tblMgr.createTable(table + i, schema, tx);
            tblScan = new TableScan(tx, table + i, layout);//tblscan
            for (int j = 0; j < 5; j++) {
                tblScan.insert();//insert 5 records
            }
            tblScan.beforeFirst();
            String[] students = {"Michael Edelman", "Shai Vadnai", "Zach Hamburger", "Nathaniel Silverman", "Moshe Berk"};
            int[] YUIDs = {800508416, 800508417, 800508418, 800508419, 800508420};
            int index = 0;
            while (tblScan.next() && index < 5) {
                tblScan.setString(studentFieldName, students[index]);
                tblScan.setInt(studentIDFieldName, YUIDs[index]);
                index++;
            }
            index = 0;
            tblScan.beforeFirst();
            while (tblScan.next() && index < 5) {
                Assert.assertEquals(students[index], tblScan.getString(studentFieldName));
                Assert.assertEquals(YUIDs[index], tblScan.getInt(studentIDFieldName));
                index++;
            }
            tblScan.close();
        }
        tx.commit();
    }

    @Test
    public void persistenceTest(){
        LogMgrBase lm = new LogMgr(manager, "logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 10, 3000);
        TxBase tx = new Transaction(manager, lm, bm);
        String table = "StdTable0";
        TableMgrBase tblMgr = new TableMgr(false, tx);
        Scan tblScan = new TableScan(tx, "tblcat", tblMgr.getLayout("tblcat", tx));
        tblScan.next();
        Assert.assertEquals("tblcat", tblScan.getString("tblname"));
        Assert.assertEquals(4, tblScan.getInt("NUMBER_OF_FIELDS"));
        Assert.assertEquals(0, tblScan.getInt("START_BLOCK"));
        Assert.assertEquals(1, tblScan.getInt("START_SLOT"));
        tblScan.next();
        Assert.assertEquals("fldcat", tblScan.getString("tblname"));
        Assert.assertEquals(3, tblScan.getInt("NUMBER_OF_FIELDS"));
        Assert.assertEquals(0, tblScan.getInt("START_BLOCK"));
        Assert.assertEquals(5, tblScan.getInt("START_SLOT"));

        for (int i = 0; i < NUMBER_OF_TABLES; i++) {
            tblScan.next();
            Assert.assertEquals("StdTable" + i, tblScan.getString("tblname"));
            Assert.assertEquals(2, tblScan.getInt("NUMBER_OF_FIELDS"));
//            Assert.assertEquals(, tblScan.getInt("START_BLOCK"));
//            Assert.assertEquals(5, tblScan.getInt("START_SLOT"));
        }
        Scan tblScan2 = new TableScan(tx, "fldcat", tblMgr.getLayout("fldcat", tx));
        tblScan2.next();
        Assert.assertEquals("tblname", tblScan2.getString("FIELD_NAME"));
        Assert.assertEquals(1, tblScan2.getInt("FIELD_TYPE"));
        Assert.assertEquals(16, tblScan2.getInt("FIELD_LENGTH"));
        tblScan2.next();
        Assert.assertEquals("NUMBER_OF_FIELDS", tblScan2.getString("FIELD_NAME"));//wouldnt it be NUMBER OF FIELDS
        Assert.assertEquals(0, tblScan2.getInt("FIELD_TYPE"));//0
        tblScan2.next();
        Assert.assertEquals("START_BLOCK",tblScan2.getString("FIELD_NAME"));
        tblScan2.next();
        Assert.assertEquals("START_SLOT",tblScan2.getString("FIELD_NAME"));
        tblScan2.next();
        Assert.assertEquals("FIELD_NAME",tblScan2.getString("FIELD_NAME"));
        tblScan2.next();
        Assert.assertEquals("FIELD_TYPE",tblScan2.getString("FIELD_NAME"));
        tblScan2.next();
        Assert.assertEquals("FIELD_LENGTH",tblScan2.getString("FIELD_NAME"));

        for (int i = 0; i < NUMBER_OF_TABLES; i++) {
            tblScan2.next();
            Assert.assertEquals("Student Name", tblScan2.getString("FIELD_NAME"));
            tblScan2.next();
            Assert.assertEquals("YUID", tblScan2.getString("FIELD_NAME"));
        }
//        Assert.assertEquals(16, tblScan2.getInt("FIELD_LENGTH"));//no field length
        //CONFIRM THAT GETLAYOUT WORKS
        LayoutBase l = tblMgr.getLayout(table, tx);
        Assert.assertEquals(40, l.slotSize());
        Assert.assertEquals(12, l.offset(l.schema().fields().get(0)));
        Assert.assertEquals(36, l.offset(l.schema().fields().get(1)));


        //across block testing
        tblScan.close();
        tblScan2.close();
        tx.commit();
    }
}
