package edu.yu.dbimpl.metadata;

import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.record.*;
import edu.yu.dbimpl.tx.TxBase;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Types;
import java.util.List;
import java.util.logging.Logger;
import java.util.spi.ToolProvider;

public class TableMgr extends TableMgrBase {
    private TableScanBase tableTableScanner;
    private TableScanBase fieldTableScanner;
    private TxBase tx;
    private Logger metadataLogger;
    private final String NUMBER_OF_FIELDS = "NUMBER_OF_FIELDS";
    private final  String START_BLOCK = "START_BLOCK";
    private final String START_SLOT = "START_SLOT";
    private final String FIELD_NAME = "FIELD_NAME";
    private final String FIELD_TYPE = "FIELD_TYPE";
    private final String FIELD_LENGTH = "FIELD_LENGTH";//If int we do nit need to set

    /**
     * Constructor: create a new catalog manager.
     *
     * @param isNew true iff this is the first time that the database is being
     *              created (for this file system root): implicitly requests that the TableMgr
     *              creates the two meta-data catalog tables.
     * @param tx    supplies the transactional scope for database operations used in
     */
    public TableMgr(boolean isNew, TxBase tx) {
        super(isNew, tx);
        this.tx = tx;//create schema for TABLE METADATA
        this.metadataLogger = Logger.getLogger(FileMgr.class.getName());
        SchemaBase tblSchema = new Schema();
        tblSchema.addStringField(TABLE_NAME, MAX_LENGTH_PER_NAME);//this will be a table that basically contains a list of table names
        tblSchema.addIntField(NUMBER_OF_FIELDS);//this will be a table that basically contains a list of table names
        tblSchema.addIntField(START_BLOCK);
        tblSchema.addIntField(START_SLOT);//this will be a table that basically contains a list of table names
        //CREATE THE SCHEMA FOR FIELD METADATA
        SchemaBase fldSchema = new Schema();
        fldSchema.addStringField(FIELD_NAME, MAX_LENGTH_PER_NAME);
        fldSchema.addIntField(FIELD_TYPE);
        fldSchema.addIntField(FIELD_LENGTH);
        if(isNew) {
            tx.append(TABLE_META_DATA_TABLE);
            tx.append(FIELD_META_DATA_TABLE);

            //CREATE THE TABLE METADATA
            tableTableScanner = new TableScan(tx, TABLE_META_DATA_TABLE, new Layout(tblSchema));
            fieldTableScanner = new TableScan(tx, FIELD_META_DATA_TABLE, new Layout(fldSchema));
            createTable(TABLE_META_DATA_TABLE, tblSchema, tx);
            createTable(FIELD_META_DATA_TABLE, fldSchema, tx);
        }
        else {
            //otherwise if its not new ( already there)
            tableTableScanner = new TableScan(tx, TABLE_META_DATA_TABLE, new Layout(tblSchema));
            fieldTableScanner = new TableScan(tx, FIELD_META_DATA_TABLE, new Layout(fldSchema));
        }
    }

    @Override
    public LayoutBase getLayout(String tblname, TxBase tx) {
        //check if table is in table meta data
        metadataLogger.info("TblMgr getLayout() of "+ tblname);
        tableTableScanner.beforeFirst();
        int numFields = -1;
        int firstBlk = -1;
        int firstSlot = -1;
        while(tableTableScanner.next()) {
            if(tableTableScanner.getString(TABLE_NAME).equals(tblname)) {
                numFields = tableTableScanner.getInt(NUMBER_OF_FIELDS);
                firstBlk = tableTableScanner.getInt(START_BLOCK);
                firstSlot = tableTableScanner.getInt(START_SLOT);
                tableTableScanner.beforeFirst();
                break;
            }
        }
        //if yes find
        if(numFields != -1){
            int fieldsGot = 0;
            SchemaBase schema = new Schema();
            fieldTableScanner.moveToRid(new RID(firstBlk, firstSlot));

            do {
                if(fieldTableScanner.getInt(FIELD_TYPE) == 1) {
                    schema.addStringField(fieldTableScanner.getString(FIELD_NAME), fieldTableScanner.getInt(FIELD_LENGTH));
                }
                else{
                    //must be aN INT
                    schema.addIntField(fieldTableScanner.getString(FIELD_NAME));
                }
                fieldsGot++;
            }while(fieldsGot < numFields && fieldTableScanner.next());
            fieldTableScanner.beforeFirst();
            metadataLogger.info("TblMgr returning layout for table " + tblname + " in getLayout()");
            return new Layout(schema);

        }
        else {
            metadataLogger.info("TblMgr table " + tblname + " not found in getLayout()");
            return null;
        }
    }

    //other design woul be to store field names and their offsets and tblnames and add their sklotsize. just get rid of len and change type to offset
    @Override
    public void createTable(String tblname, SchemaBase schema, TxBase tx) {
        //if table name doesnt already exist
        if (tx.size(tblname) == 0) {
            tx.append(tblname);
        }//TODO UH OH WHAT ABOUT DUPLICATE TABLES
            //insert into tabledata with table scanner
        metadataLogger.info("TblMgr creating table " + tblname);
        tableTableScanner.insert();
        tableTableScanner.setString(TABLE_NAME, tblname);
        List<String> fields = schema.fields();
        tableTableScanner.setInt(NUMBER_OF_FIELDS, fields.size());//The table name, num of fields, first block, first slot
        for (int i = 0; i < fields.size(); i++) {
            fieldTableScanner.insert();//we do this so we can get the proper start values to fill in below
            if (i == 0) {
                //puts the starting position (block, slot)
                tableTableScanner.setInt(START_BLOCK, fieldTableScanner.getRid().blockNumber());
                tableTableScanner.setInt(START_SLOT, fieldTableScanner.getRid().slot());
            }
            fieldTableScanner.setString(FIELD_NAME, fields.get(i));
            //1 - string 0 - int
            if (schema.type(fields.get(i)) == Types.VARCHAR) {
                fieldTableScanner.setInt(FIELD_TYPE, 1);
                fieldTableScanner.setInt(FIELD_LENGTH, schema.length(fields.get(i)));
            } else {
                fieldTableScanner.setInt(FIELD_TYPE, 0);
            }
        }
        //when we insert into a table it looks at the next record from current record and looks for next available 0 record spot
        //after insert the  pointer is pointed to the record just designated for insertion
        //so when you start to put in field values you can - right after insert - store the value of the current RID into
        // the table meta data to know where this tables field data starts in the field meta data table (loop insert and add each field, but for the first field make sure to mark down blknum and slot num)
    }
}
