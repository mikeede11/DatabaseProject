package edu.yu.dbimpl.file;

import org.junit.*;

import java.io.File;

import static org.junit.Assert.*;

public class FileModuleTester {
    private final int BLOCK_SIZE = 4096;

    FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);

    @Test
    public void lengthTest(){

        assertEquals(0, manager.length("testFile.txt"));
    }

    @Test
    public void concurrencyFileMgrReadTest(){
        Page p0 = new Page(manager.blockSize());
        Page p1 = new Page(manager.blockSize());
        Page p2 = new Page(manager.blockSize());
        Page p3 = new Page(manager.blockSize());
        Page p4 = new Page(manager.blockSize());
        Page p5 = new Page(manager.blockSize());
        Page p6 = new Page(manager.blockSize());
        Page p7 = new Page(manager.blockSize());
        Page p8 = new Page(manager.blockSize());
        Page p9 = new Page(manager.blockSize());
        String s0 ="0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        String s1 = "1111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        String s2 = "2222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222";
        String s3 = "3333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333";
        String s4 = "4444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444";
        String s5 = "5555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555";
        String s6 = "6666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666";
        String s7 = "7777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777";
        String s8 = "8888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888";
        String s9 = "9999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999";
        p0.setString(0,s0);
        p1.setString(0,s1);
        p2.setString(0,s2);
        p3.setString(0,s3);
        p4.setString(0,s4);
        p5.setString(0,s5);
        p6.setString(0,s6);
        p7.setString(0,s7);
        p8.setString(0,s8);
        p9.setString(0,s9);
        manager.write(new BlockId("dataFile.txt", 0), p0);
        manager.write(new BlockId("dataFile.txt", 1), p1);
        manager.write(new BlockId("dataFile.txt", 2), p2);
        manager.write(new BlockId("dataFile.txt", 3), p3);
        manager.write(new BlockId("dataFile.txt", 4), p4);
        manager.write(new BlockId("dataFile.txt", 5), p5);
        manager.write(new BlockId("dataFile.txt", 6), p6);
        manager.write(new BlockId("dataFile.txt", 7), p7);
        manager.write(new BlockId("dataFile.txt", 8), p8);
        manager.write(new BlockId("dataFile.txt", 9), p9);
        Page pResult = new Page(manager.blockSize());
        manager.read(new BlockId("dataFile.txt", 0), pResult);
        assertEquals(s0, pResult.getString(0));
        manager.read(new BlockId("dataFile.txt", 1), pResult);
        assertEquals(s1, pResult.getString(0));

        manager.read(new BlockId("dataFile.txt", 2), pResult);
        assertEquals(s2, pResult.getString(0));
        manager.read(new BlockId("dataFile.txt", 3), pResult);
        assertEquals(s3, pResult.getString(0));
        manager.read(new BlockId("dataFile.txt", 4), pResult);
        assertEquals(s4, pResult.getString(0));
        manager.read(new BlockId("dataFile.txt", 5), pResult);
        assertEquals(s5, pResult.getString(0));
        manager.read(new BlockId("dataFile.txt", 6), pResult);
        assertEquals(s6, pResult.getString(0));
        manager.read(new BlockId("dataFile.txt", 7), pResult);
        assertEquals(s7, pResult.getString(0));
        manager.read(new BlockId("dataFile.txt", 8), pResult);
        assertEquals(s8, pResult.getString(0));
        manager.read(new BlockId("dataFile.txt", 9), pResult);
        assertEquals(s9, pResult.getString(0));
    }

    @Test
    public void pageConcurrencyTest(){
        String s0 = "00000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
        String s1 = "11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";
        String s2 = "22222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222222";
        String s3 = "33333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333333";
        String s4 = "44444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444444";
        String s5 = "55555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555555";
        String s6 = "66666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666666";
        String s7 = "77777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777777";
        String s8 = "88888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888888";
        String s9 = "99999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999999";
        Page p = new Page(manager.blockSize());
        p.setString(0, s0);
        p.setString(204, s1);
        p.setString(408, s2);
        p.setString(612, s3);
        p.setString(816, s4);
        p.setString(1020, s5);
        p.setString(1224, s6);
        p.setString(1428, s7);
        p.setString(1632, s8);
        p.setString(1836, s9);
        assertEquals(s0,p.getString(0));
        assertEquals(s1,p.getString(204));
        assertEquals(s2,p.getString(408));
        assertEquals(s3,p.getString(612));
        assertEquals(s4,p.getString(816));
        assertEquals(s5,p.getString(1020));
        assertEquals(s6,p.getString(1224));
        assertEquals(s7,p.getString(1428));
        assertEquals(s8,p.getString(1632));
        assertEquals(s9,p.getString(1836));




    }

    @Test
    public void microTest(){
        PageBase p = new Page(manager.blockSize());
        String s = "abcdefghijklm";
        int i = 345;
        p.setString(88, s);
        p.setInt(105, i);
        BlockId blk = new BlockId("fileOne.txt", 0);
        manager.write(blk, p);
        PageBase p2 = new Page(manager.blockSize());
        manager.read(blk, p2);
        assertEquals(s, p2.getString(88));
        assertEquals(i, p2.getInt(105));
    }

    @Test
    public void performanceTest(){
        int offset = 0;
        String s = "";
        int blkNum = 0;
        PageBase p = new Page(manager.blockSize());
        for(int i = 0; i < 174806; i++){
            s = Integer.toString(i);
            if ((manager.blockSize() - offset) < s.length() + Integer.BYTES) {
                manager.write(new BlockId("performanceFile.txt", blkNum++), p);
                offset = 0;
                p = new Page(manager.blockSize());
            }
            p.setString(offset, s);
            offset += s.length() + Integer.BYTES;
        }

        assertTrue(true);
    }


}
