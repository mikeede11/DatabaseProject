package edu.yu.dbimpl.file;

import java.io.File;

public class Driver {
    public static void main(String[] args) {
//microtest
        System.out.println(8 - 5);
//
//        final int BLOCK_SIZE = 4096;
////        System.out.println(System.getProperty("user.dir")  + File.separator);//Petite DB/
////        //System.out.println(Integer.BYTES);
////        final String dirName = "temp_appendAndLengthTest_ProfessorFileModuleTest";//+testFile;
////        final File dbDirectory = new File(dirName);
////        final int blockSize = 400;
////
//////        logger.info("Creating a FileMgr");
////        final FileMgrBase fm = new FileMgr(dbDirectory, blockSize);
////        System.out.println(fm.length(" that doesnt exist"));
////        System.out.println(fm.length("testfile"));
//        FileMgr manager = new FileMgr(new File("dbDirectory"), BLOCK_SIZE);
////        //System.out.println(manager.length("testfile1"));
//        PageBase p = new Page(manager.blockSize());
//        String s = "abcdefghijklm";
//        int i = 345;
//        p.setString(88, s);
//        p.setInt(105, i);
//        BlockId blk = new BlockId("fileOne.txt", 0);
//        manager.write(blk, p);
//        PageBase p2 = new Page(manager.blockSize());
//        manager.read(blk, p2);
//        System.out.println(p2.getString(88));
//        System.out.println(p2.getInt(105));
//
//        System.out.println(p2.getInt(105) == i);
//        System.out.println(s.equals(p2.getString(88)));
//        String input = "0000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";
//        p.setBytes(0, input.getBytes());
//        manager.write(new BlockId("testFile5.txt", 0), p);
//        String result = p.getString(0);
//        assert input.equals(result);
//        System.out.println(input);
//        System.out.println(result);

        //PageConcurrencyTest a page of 2000+ size that stores strings of length 200 consecutively with int 4s when it gets the int it doesn't work
        //Email would be a good idea
        //test did a getString()
//        p.setBytes(0, new byte[]{0,0,0,1,8,9,0,3,0,0,6,0,0,0,8,0,0,0,0,0,0,7,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,3});
//        byte[] b = p.getBytes(0);
//        for(byte i: b){
//            System.out.print(i);
//        }
        //makes dbDirectory folder as expected and where expected
        //doesnt make additional ones if already there
        //deletes directories and I assume files with temp prefix
//        Page p = new Page(manager.blockSize());
//        String s = "This text is the first test to see if the data is really written to disk and hopefully also read from disk";
//        p.setString(0,s);
//        BlockId blk = new BlockId("firstTxtFile.txt", 0);
//        manager.write(blk, p);
//        manager.read(blk, p);
//        System.out.println(p.getString(0));


    }
}
