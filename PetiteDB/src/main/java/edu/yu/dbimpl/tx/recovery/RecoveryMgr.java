package edu.yu.dbimpl.tx.recovery;

import edu.yu.dbimpl.buffer.BufferBase;
import edu.yu.dbimpl.buffer.BufferMgrBase;
import edu.yu.dbimpl.file.BlockId;
import edu.yu.dbimpl.file.BlockIdBase;
import edu.yu.dbimpl.file.FileMgr;
import edu.yu.dbimpl.log.LogMgrBase;
import edu.yu.dbimpl.tx.TxBase;

import java.util.*;
import java.util.logging.Logger;

public class RecoveryMgr extends RecoveryMgrBase {
    private TxBase tx;
    private LogMgrBase lm;
    private BufferMgrBase bm;
    private Logger recoveryLogger;
    /**
     * Create a recovery manager for the specified transaction.
     *
     * @param tx        the transaction associated with this recovery manager instance
     * @param logMgr    the singleton logMgr for the DBMS
     * @param bufferMgr the singleton bufferMgr for the DBMS
     */
    public RecoveryMgr(TxBase tx, LogMgrBase logMgr, BufferMgrBase bufferMgr) {
        super(tx, logMgr, bufferMgr);
        this.tx = tx;
        this.lm = logMgr;
        this.bm = bufferMgr;
        this.recoveryLogger = Logger.getLogger(FileMgr.class.getName());
        //TODO Log a TX START - since the RMgr is constructed when its tx is we - just to be consistent with logging style
        String txType = "START";
        String log = txType + "," + tx.txnum();
        lm.append(log.getBytes());//FIXME I DONT THINK WE NEED TO FLUSH A START MESSAGE - B/C

    }

    //TODO SERIALIZATION : JUST MAKE A STRING AND MAKE IT INTO BYTES WHEN YOU POST IT.
    // BUT THEN GET IT AS A STRING AND SPLICE IT WITH SAY COMMAS. THE RM CREATES THE LOGS AND USES THEM(JUST RECOVER()?)
    @Override
    public void commit() {
        String txType = "COMMIT";
        String log = txType + "," + tx.txnum();
        //TODO LOCK AQUIRE

        bm.flushAll(tx.txnum()); //this will flush all the buffers that were modified in this tx to disc
        // - so logically this tx is complete and on disc
        //At this point if there would be a shutdown our recover() would just undo these actions b/c no commit log
        int lsn = lm.append(log.getBytes());
        lm.flush(lsn);
        //now there will be a commit log on disc and so the tx is totally persisted

        //simply post a commit log to logfile and flush
        //concommitant stuff???

    }

    @Override
    public void rollback() {
        Iterator<byte[]> logs = lm.iterator();
        while(logs.hasNext()) {
            byte[] currentLogRec = logs.next();
            String[] logData = updateLogVals(currentLogRec);
            String logType = actionType(currentLogRec);
            if(Integer.parseInt(logData[1]) == tx.txnum()){

                if(logType.equals("START")){
                    break;//we are done rollingback this tx
                }
                if(logType.equals("COMMIT") || logType.equals("ROLLBACK")){
                    continue;
                }
                //note at this point the only log records we should see are logs for this tx are SET INT and SET STRING LOGS
                BufferBase buf = bm.pin(new BlockId(logData[2], Integer.parseInt(logData[3])));
                if(logType.equals("SETINT")) {
                    buf.contents().setInt(Integer.parseInt(logData[4]), Integer.parseInt(logData[5]));
                    //flush no?
                }
                else if(logType.equals("SETSTRING")){
                    buf.contents().setString(Integer.parseInt(logData[4]), logData[5]);
                }
            }
        }
        bm.flushAll(tx.txnum());//yes this is advisable b/c what if sys. shutdown and we undid stuff that wasnt actually undone (on disc)
        //and then we flushed a rollback rec to log. then upon recovery the sys will not undo this tx-i actions b/c of the rollback log. but they should be undone!
        String txType = "ROLLBACK";
        String log = txType + "," + tx.txnum();
        //TODO LOCK AQUIRE
        int lsn = lm.append(log.getBytes());
        lm.flush(lsn);
        //simply post a ROLLBACK log to logfile and flush
        //concommitant stuff???
    }

    @Override
    public void recover() {
        //NO TXS ALLOWED RIGHT NOW

        Iterator<byte[]> logs = lm.iterator();
        //1)check if there are any logs to recover (what if we didnt do anything yet)
        //2)if none then return/dont do anything - nothing to recover
        //3) if yes then begin algorithm by getting that logrec - while (the logrec is not checkpoint) undo-alg - another log? ok next log if not break
        //thats one way
        //or check if more, if not stop if yes do undo alg - if come across checkpoint - break
        ArrayList<Integer> commitedTxs = new ArrayList<>();
        //commitedTxs.add(2);
        ArrayList<Integer> rolledbackTxs = new ArrayList<>();
        Set<BufferBase> buffersUndone = new LinkedHashSet<>();
        if(logs.hasNext()) {
            byte[] currentLogRec = logs.next();
            String logType = actionType(currentLogRec);
            while (!logType.equals("CHECKPOINT")){
                //Note cant the commited and rolledback txs be in one list? if a tx is rolledback it wasnt commited and vice versa.
                if(logType.equals("COMMIT")){
                    commitedTxs.add(txNumOfLog(currentLogRec));
                }
                else if(logType.equals("ROLLBACK")){
                    rolledbackTxs.add(txNumOfLog(currentLogRec));
                }
                else if(logType.equals("SETINT") || logType.equals("SETSTRING")){
                    String[] logRecInfo = updateLogVals(currentLogRec);
                    Integer txNum = Integer.parseInt(logRecInfo[1]);
                    if(!commitedTxs.contains(txNum) && !rolledbackTxs.contains(txNum)){
                        BufferBase buf = bm.pin(new BlockId(logRecInfo[2], Integer.parseInt(logRecInfo[3])));
                        if(logType.equals("SETINT")) {
                            recoveryLogger.info("RM Undoing a setInt by setting value " + Integer.parseInt(logRecInfo[5]) + " at offset " + Integer.parseInt(logRecInfo[4]));
                            buf.contents().setInt(Integer.parseInt(logRecInfo[4]), Integer.parseInt(logRecInfo[5]));
                            //flush no?
                        }
                        else{
                            recoveryLogger.info("RM Undoing a setString by setting value \"" + logRecInfo[5] + "\" at offset " + Integer.parseInt(logRecInfo[4]));
                            buf.contents().setString(Integer.parseInt(logRecInfo[4]), logRecInfo[5]);
                            //flush no?
                        }
                        buffersUndone.add(buf);
                        //for efficiency we should do all the undo actions to all the buffers that need to be undone
                        //and for each buffer we undid stuff to we kept it in a list and then when we aredone with recovery alg
                        //we flush all those buffers - this avoids writing a buffer to disc multiple times if we had to do multiple writes
                        //for that buffer
                    }
                }

                if(logs.hasNext()) {
                    currentLogRec = logs.next();
                    logType = actionType(currentLogRec);
                    recoveryLogger.info("RM next log is of type " + logType);
                }
                else{
                    break;
                }

            }
            for(BufferBase b: buffersUndone){
                b.flush();
            }
        }

        checkpoint();
        //NOW ALLOW TXS
        //TODO READ
    }

    @Override
    public int setInt(BufferBase buff, int offset, int newval) {
        String txType = "SETINT";
        BlockIdBase blk = buff.block();
        //offset good
        //aquire a lock i would guess, read original value
        String originalVal = "" + buff.contents().getInt(offset);
        String log = txType + "," + tx.txnum() + "," + blk.fileName() + "," + blk.number() + "," + offset + "," + originalVal +  "," + newval;
        return lm.append(log.getBytes());//FIXME AFTER THE RECORD HAS BEEN WRITTEN TO THE LOG??? LSN++ POSTFIX!!!

        //simply post a SETINT log to logfile
    }

    @Override
    public int setString(BufferBase buff, int offset, String newval) {
        String txType = "SETSTRING";
        BlockIdBase blk = buff.block();
        //offset good
        //aquire a lock i would guess, read original value
        String originalVal = buff.contents().getString(offset);
        String log = txType + "," + tx.txnum() + "," + blk.fileName() + "," + blk.number() + "," + offset + "," + originalVal +  "," + newval;
        return lm.append(log.getBytes());//FIXME AFTER THE RECORD HAS BEEN WRITTEN TO THE LOG??? LSN++ POSTFIX!!!
        //simply post a SETSTRING log to logfile
    }

    private String actionType(byte[] logrec){
        String strVersion = new String(logrec);
        return strVersion.substring(0,strVersion.indexOf(","));
    }

    //HONESTLY WE DONT NEED THE <>
    // 0 - log type, 1 - txNum, 2 - file, 3 - blk#, 4 - offset, 5 - original value, 6 - new value
    private String[] updateLogVals(byte[] logrec){
        String strVersion = new String(logrec);
        return strVersion.split(",");
    }

    private int txNumOfLog(byte[] logrec){
        String strVersion = new String(logrec);
        return Integer.parseInt(strVersion.substring(strVersion.indexOf(",") + 1));
    }

    private void checkpoint(){
        String txType = "CHECKPOINT";
        String log = txType + "," + tx.txnum();
        //TODO ON STARTUP AFTER RECOVERY MAKE SURE NO TXS EXECUTE YET
        //TODO LOCK AQUIRE
        int lsn = lm.append(log.getBytes());
        lm.flush(lsn);
    }
}
