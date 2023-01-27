package edu.yu.dbimpl.query;

import edu.yu.dbimpl.buffer.BufferMgr;
import edu.yu.dbimpl.buffer.BufferMgrBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.log.LogMgr;
import edu.yu.dbimpl.log.LogMgrBase;
import edu.yu.dbimpl.metadata.TableMgr;
import edu.yu.dbimpl.metadata.TableMgrBase;
import edu.yu.dbimpl.record.*;
import edu.yu.dbimpl.tx.Transaction;
import edu.yu.dbimpl.tx.TxBase;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public class QueryProcessingModuleTester {
    private final int BLOCK_SIZE = 400;
    private final int NUMBER_OF_TABLES = 300;//+ 2 for the metadata ones
    FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);

    @Rule
    public ExpectedException exceptionRule = ExpectedException.none();

    @Test
    public void testBasicTest(){
        LogMgrBase lm = new LogMgr(manager, "temp_logfile.txt");
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

        String table = "temp_StdTable";
        TableScanBase tblScan = null;

        tx.append(table);
        tblMgr.createTable(table, schema, tx);
        tblScan = new TableScan(tx, table, layout);//tblscan
        for (int j = 0; j < 10; j++) {
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
        Expression lhs1 = new Expression(studentIDFieldName);
        Constant c = new Constant(YUIDs[2]);
        Expression rhs2 = new Expression(c);
        Term t = new Term(lhs1, rhs2);
        Predicate p = new Predicate(t);

        SelectScanBase ssb = new SelectScan(tblScan, p);
        ssb.beforeFirst();
        ssb.next();
        Assert.assertEquals(students[2], ssb.getString(studentFieldName));
        ArrayList<String> cols = new ArrayList<>();
        cols.add(studentIDFieldName);
        ProjectScanBase psb = new ProjectScan(tblScan, cols);
        psb.beforeFirst();
        psb.next();
        Assert.assertEquals(YUIDs[0], psb.getVal(studentIDFieldName).asInt());
        exceptionRule.expect(RuntimeException.class);
        psb.getVal(studentFieldName);
        ProjectScanBase psb2 = new ProjectScan(ssb, cols);
        psb2.beforeFirst();
        psb2.next();
        Assert.assertEquals(YUIDs[4], psb2.getVal(studentIDFieldName).asInt());
        exceptionRule.expect(RuntimeException.class);
        psb2.getVal(studentFieldName);

//        ProductScanBase prodScan = new ProductScan()
        //make second table

        String table2 = "temp_BooksTable";
        TableScanBase tblScan2 = null;
        SchemaBase schema2 = new Schema();
        schema2.addStringField("BookTitle", 30);
        schema2.addIntField("BookPrice");
        schema2.addStringField("BookGenre", 30);
        LayoutBase layout2 = new Layout(schema2);
        tx.append(table2);
        tblMgr.createTable(table2, schema2, tx);
        tblScan2 = new TableScan(tx, table, layout2);//tblscan
        for (int j = 0; j < 10; j++) {
            tblScan2.insert();//insert 5 records
        }
        tblScan2.beforeFirst();
        String[] books = {"PersonalFinanceForDummies", "Algorithms", "IntroductionToTheTalmud", "HilchotShabbos", "JewsOfSpain"};
        int[] prices = {5, 30, 25, 15, 20};
        String[] genre = {"buisness", "CS", "Religion/History", "Religion", "History"};
        int index2 = 0;
        while (tblScan2.next() && index2 < 5) {
            tblScan2.setString("BookTitle", books[index2]);
            tblScan2.setInt("BookPrice", prices[index2]);
            tblScan2.setString("BookGenre", genre[index2]);
            index2++;
        }

        ProductScanBase prodScan = new ProductScan(tblScan, tblScan2);
        prodScan.beforeFirst();
        prodScan.next();
        prodScan.next();
        prodScan.next();
        Assert.assertEquals(books[3], prodScan.getString("BookTitle"));
    }

    /**NOTE: THIS TEST TAKES ~60 SECONDS TO COMPLETE**/
    @Test
    public void selectProductEmptyResultSet(){
        LogMgrBase lm = new LogMgr(manager, "temp_logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 8, 500);
        SchemaBase schema = new Schema();
        schema.addIntField("A");//my name is like 15 Michael Edelman
        schema.addStringField("B", 9);
        //construct a layout object from that schema
        LayoutBase layout = new Layout(schema);
        TxBase tx = new Transaction(manager, lm, bm);

        TableMgrBase tblMgr = new TableMgr(true, tx);//tblscan twice

        String table = "temp_T1";
        TableScanBase tblScan = null;

//        tx.append(table);//yeah?
        tblMgr.createTable(table, schema, tx);
        tblScan = new TableScan(tx, table, layout);//tblscan

        for (int j = 0 ; j < 200; j++) {
            tblScan.insert();//insert 5 records
            tblScan.setInt("A", j);
            tblScan.setString("B", "bbb" + j);
        }
        SchemaBase schema2 = new Schema();
        schema2.addIntField("C");//my name is like 15 Michael Edelman
        schema2.addStringField("D", 9);
        //construct a layout object from that schema
        LayoutBase layout2 = new Layout(schema2);
//        TxBase tx2 = new Transaction(manager, lm, bm);//yeah?
        String table2 = "temp_T2";
        TableScanBase tblScan2 = null;

//        tx.append(table);//yeah?
        tblMgr.createTable(table2, schema2, tx);//tx?
        tblScan2 = new TableScan(tx, table2, layout2);//tblscan

        for (int j = 200; j < 400; j++) {
            tblScan2.insert();//insert 5 records
            tblScan2.setInt("C", j);
            tblScan2.setString("D", "ddd" + j);
        }

        ProductScanBase ps = new ProductScan(tblScan, tblScan2);
        SelectScanBase ss = new SelectScan(ps, new Predicate(new Term(new Expression("A"), new Expression("C"))));
        ArrayList<Integer> resultSet = new ArrayList<>();
        ss.beforeFirst();
        while(ss.next()){
            resultSet.add(ss.getInt("A"));
        }
        Assert.assertEquals(0, resultSet.size());


    }

    /**NOTE: THIS TEST TAKES ~60 SECONDS TO COMPLETE.
     * ALSO MAKE SURE THAT THE DIRECTORY OF THE TABLES
     * THAT ARE USED IN HERE is empty OR IT WILL MESS UP TEST RESULTS**/
    @Test
    public void selectProductTest(){
        LogMgrBase lm = new LogMgr(manager, "temp_logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 8, 500);
        SchemaBase schema = new Schema();
        schema.addIntField("A");//my name is like 15 Michael Edelman
        schema.addStringField("B", 9);
        //construct a layout object from that schema
        LayoutBase layout = new Layout(schema);
        TxBase tx = new Transaction(manager, lm, bm);

        TableMgrBase tblMgr = new TableMgr(true, tx);//tblscan twice

        String table = "temp_T1";
        TableScanBase tblScan = null;

//        tx.append(table);//yeah?
        tblMgr.createTable(table, schema, tx);
        tblScan = new TableScan(tx, table, layout);//tblscan

        for (int j = 0 ; j < 20; j++) {
            tblScan.insert();//insert 5 records
            tblScan.setInt("A", j);
            tblScan.setString("B", "bbb" + j);
        }
        SchemaBase schema2 = new Schema();
        schema2.addIntField("C");//my name is like 15 Michael Edelman
        schema2.addStringField("D", 9);
        //construct a layout object from that schema
        LayoutBase layout2 = new Layout(schema2);
//        TxBase tx2 = new Transaction(manager, lm, bm);//yeah?
        String table2 = "temp_T2";
        TableScanBase tblScan2 = null;

//        tx.append(table);//yeah?
        tblMgr.createTable(table2, schema2, tx);//tx?
        tblScan2 = new TableScan(tx, table2, layout2);//tblscan

        for (int j = 20; j < 40; j++) {
            tblScan2.insert();//insert 5 records
            tblScan2.setInt("C", j);
            tblScan2.setString("D", "ddd" + j);
        }

        ProductScanBase ps = new ProductScan(tblScan, tblScan2);
        SelectScanBase ss = new SelectScan(ps, new Predicate(new Term(new Expression("A"), new Expression("C"))));
        ProjectScanBase projs = new ProjectScan(ss, Arrays.asList("B","D"));
        ArrayList<String> resultSet = new ArrayList<>();
        ss.beforeFirst();
        while(projs.next()){
            resultSet.add(ss.getString("B"));
        }
        Assert.assertEquals(0, resultSet.size());


    }

    @Test
    public void selectProjectTest(){
        LogMgrBase lm = new LogMgr(manager, "temp_logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 8, 500);
        SchemaBase schema = new Schema();
        schema.addIntField("A");//my name is like 15 Michael Edelman
        schema.addStringField("B", 9);
        //construct a layout object from that schema
        LayoutBase layout = new Layout(schema);
        TxBase tx = new Transaction(manager, lm, bm);

        TableMgrBase tblMgr = new TableMgr(true, tx);//tblscan twice

        String table = "temp_T1";
        TableScanBase tblScan = null;

//        tx.append(table);//yeah?
        tblMgr.createTable(table, schema, tx);
        tblScan = new TableScan(tx, table, layout);//tblscan

        for (int j = 0 ; j < 200; j++) {
            tblScan.insert();//insert 5 records
            tblScan.setInt("A", j);
            tblScan.setString("B", "bbb" + j);
        }

        SelectScanBase ss = new SelectScan(tblScan, new Predicate(new Term(new Expression("A"), new Expression(new Constant(10)))));
        ProjectScanBase projs = new ProjectScan(ss, Arrays.asList("B"));
        ArrayList<String> resultSet = new ArrayList<>();
        projs.beforeFirst();
        while(projs.next()){
            exceptionRule.expect(RuntimeException.class);
            resultSet.add(projs.getString("A"));
            resultSet.add(projs.getString("B"));
        }
        Assert.assertEquals(1, resultSet.size());
        Assert.assertEquals("bbb10", resultSet.get(0));


    }

    @Test
    public void updateableScanTest(){
        LogMgrBase lm = new LogMgr(manager, "temp_logfile.txt");
        BufferMgrBase bm = new BufferMgr(manager, lm, 8, 500);
        SchemaBase schema = new Schema();
        schema.addIntField("A");//my name is like 15 Michael Edelman
        schema.addStringField("B", 9);
        //construct a layout object from that schema
        LayoutBase layout = new Layout(schema);
        TxBase tx = new Transaction(manager, lm, bm);

        TableMgrBase tblMgr = new TableMgr(true, tx);//tblscan twice

        String table = "temp_T1";
        TableScanBase tblScan = null;

//        tx.append(table);//yeah?
        tblMgr.createTable(table, schema, tx);
        tblScan = new TableScan(tx, table, layout);//tblscan

        for (int j = 0 ; j < 200; j++) {
            tblScan.insert();//insert 5 records
            tblScan.setInt("A", j);
            tblScan.setString("B", "bbb" + j);
        }

        SelectScanBase ss = new SelectScan(tblScan, new Predicate(new Term(new Expression("A"), new Expression(new Constant(10)))));
        ss.beforeFirst();
        while(ss.next()){
            ss.setString("B", "rec 10");
        }
        ss.beforeFirst();
        String result = "";
        while(ss.next()){
            result = ss.getString("B");
        }
        Assert.assertEquals("rec 10", result);
    }

}
