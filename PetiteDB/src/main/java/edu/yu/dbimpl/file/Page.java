package edu.yu.dbimpl.file;

import java.nio.ByteBuffer;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public class Page extends PageBase{
    private int blocksize;
    private byte[] pageData;
    private boolean log;
    private Logger pageLogger;
    private ConsoleHandler ch;
    //private ByteBuffer bb;
    public Page(int blocksize) {
        super(blocksize);
        this.pageLogger = Logger.getLogger(FileMgr.class.getName());
        //assign IV
//        pageLogger.info("FM constructing a Page");
        this.blocksize = blocksize;
        this.pageData = new byte[blocksize];

    }

    public Page(byte[] arr){
        super(arr);
        this.pageLogger = Logger.getLogger(FileMgr.class.getName());

//        pageLogger.info("FM constructing a LOG Page");

        this.log = true;
        this.pageData = new byte[arr.length];
    }

    //all set methods write to disk
    // Each Page wraps a ByteBuffer instance to store bytes -> allocate and
    //allocateDirect
    //▸ Read the class Javadoc carefully!

    // java.nio Tutorial
    //▸ Java classes worth investigating, especially for
    //performance reasons
    //▸ ByteBuffer
    //▸ FileChannel

    @Override
    public synchronized int getInt(int offset) {
        ByteBuffer bb = ByteBuffer.wrap(pageData);
        int value = bb.getInt(offset);
//        pageLogger.info("FM getInt() returned " + value + " from offset " + offset);
        return value;
    }


    @Override
    public synchronized void setInt(int offset, int n) {
        ByteBuffer bb = ByteBuffer.wrap(pageData);
        bb.putInt(offset, n);
//        pageLogger.info("FM setInt() " + n + " at offset " + offset);

    }

    @Override
    public synchronized byte[] getBytes(int offset) {
        if(offset == -1){
            return pageData;
        }
        if(log){
//            pageLogger.info("FM getbytes(" + offset  + ") returning all this pages data");
            return pageData;
        }
        //go to offset read length of arr. return array with that range
        ByteBuffer bb = ByteBuffer.wrap(pageData);
        int byteArrLength = bb.getInt(offset);//the int specifying the byte[] length should be at the start of the offset of this data we want to read
        byte[] data = new byte[byteArrLength + Integer.BYTES];
        bb.position(offset);
        bb.get(data, 0, byteArrLength + Integer.BYTES);
//        pageLogger.info("FM getBytes(" + offset + ") retrieved and returning byte[] " + byteString(data) + " with length " + (byteArrLength + Integer.BYTES));

        return data;
    }

    @Override
    public synchronized void setBytes(int offset, byte[] b) {
        ByteBuffer bb = ByteBuffer.wrap(pageData);//starts at position 0 - duh
        if(offset == -1 || log){
            bb.put(b, 0, b.length);
            return;
        }
        int byteArrLength = b.length;
        /* There are two ways to deal with ByteBuffers
        * 1) each time you put/get something you can explicitly specify the position
        * 2) you can just put/get wherever the pointer in the buffer is currently at
        * the position of the pointer is determined by the previous operations on the buffer. you can use rewind() to reset to position 0
        * we use approach 1) to have more control over our actions*/
        bb.putInt(offset, byteArrLength);//add int in beginning that specifies length
        bb.position(offset + Integer.BYTES);
        bb.put(b, 0, byteArrLength);//may be unnecessary since cursor is probably where it needs to be but its like a double check so good.
        /**/
//        pageLogger.info("FM setBytes(" + offset + ") put byte[] " + byteString(b) + " with length " + byteArrLength);
    }

    @Override
    public synchronized String getString(int offset) {
        //same as bytes but use maxLength function since sometimes char sizes can vary
        ByteBuffer bb = ByteBuffer.wrap(pageData);
        int strLength = bb.getInt(offset);//# of chars
        int maxLength = maxLength(strLength);
        byte[] data = new byte[strLength];
        bb.position(offset + Integer.BYTES);
        bb.get(data, 0, strLength);
//        pageLogger.info("FM getString() retrieved and returning string  " + (new String(data)));

        return new String(data);
    }

    @Override
    public synchronized void setString(int offset, String s) {
        //same as bytes but use maxLength function since sometimes char sizes can vary
        ByteBuffer bb = ByteBuffer.wrap(pageData);
        bb.putInt(offset, s.length());//I think this should work.
        bb.position(offset + Integer.BYTES);
        bb.put(s.getBytes(), 0, s.length());
//        pageLogger.info("FM setString() put string  " + s + " at offset " + offset);

    }

    public byte[] getAllBytes(){
        return pageData;
    }

    private String byteString(byte[] b){
        StringBuilder sb = new StringBuilder();
        for (byte x: b
             ) {
            sb.append(x + " ");
        }
        return sb.toString();
    }
}
