package edu.yu.dbimpl.file;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.logging.*;
import edu.yu.dbimpl.MyFormatter;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FileMgr extends FileMgrBase {
    private int blocksize;
    private File dbDirectoryPath;
    public static Logger fmLogger;
    private ConsoleHandler ch;

    /**
     * The constructor is responsible for removing temporary files that may have
     * been created in previous invocations of the DBMS.  By convention, such
     * files are denoted by starting with the string "temp".
     *
     * @param dbDirectory specifies the location of the root database directory
     *                    in which files will be created
     * @param blocksize
     */
    public FileMgr(File dbDirectory, int blocksize) {
        super(dbDirectory, blocksize);
        fmLogger = Logger.getLogger(FileMgr.class.getName());
        LogManager.getLogManager().reset();
//        fmLogger.setLevel(Level.SEVERE);

        // this.fmLogger1 = Logger.getLogger("FileMGR1_Logger");
        this.ch = new ConsoleHandler();
        this.ch.setLevel(Level.INFO);
       ch.setFormatter(new MyFormatter());
        fmLogger.addHandler(ch);
        //assign IV
//        fmLogger.info("FM constructing a FileMgr with blocksize " + blocksize);
        this.blocksize = blocksize;
        this.dbDirectoryPath = new File(System.getProperty("user.dir") + File.separator + dbDirectory.getName());
//        fmLogger.info("FM DB directory path is: " + this.dbDirectoryPath);
        //if folder with name dbDirectory (isNew()) does not exist create one
        //create a folder with the name dbDirectory at the location System.getProperty("user.dir")
        //else if does you may have to delete tmp* files? ohhh yes ok
//        fmLogger.info("FM checking if DB directory path exists...");
        if (isNew()) {
//            fmLogger.info("FM it does not so well try to create a new one...");
            try {
                Path path = Paths.get(dbDirectoryPath.getAbsolutePath());//assuming  that System.getProperty("user.dir")
                //java.nio.file.Files;
                Files.createDirectory(path);
//                fmLogger.info("FM Directory is created!");
            } catch (IOException e) {
                fmLogger.info("FM Failed to create directory!" + e.getMessage());
            }
        } else {
//            fmLogger.info("FM it does...delete all files that start with \"tmp\"");
            for (File f : dbDirectoryPath.listFiles()) {//What if no files in that dir?
                if (f.getName().startsWith("temp")) {
                    f.delete();
                }
            }
        }
        fmLogger.info("FM successfully constructed fileMgr");
    }
    //db is one big bytebuffer? or there are files that exist. you write to them and read to them with a byte buffer and allocate memory for them with the allocate and allocat direct methods
    //each file is written to and read from a block at at a time. each block contains the logical number it occupies in the blocks that
    //make up this file. so for example if im reading block two from file hello.txt I open up bytebuffer to read from filetwo.txt at an offset of2*block size and ending at an offset of 3 * block size
    //put it in the page which the page will be able to process that block accordingly and walla.
    //each file is an array of bytes (actually some discontiguous sectors but the OS makes itas if its a list of blocks). I do not need to keep track of blocks for a file.
    //so whats the problem - I dont know what it means to append/allocate. ok so yeah if figured it out
    //there are blocks and then there are values. variable size values have an int before them designating length. when page reads those it processes accordingly
    //we read and write to blocks from specific offsets of files we can do this with the write function in file channel i believe or the put method in byte buffer etc,
    //as for blocks though we need to allocate and "write" blocks to files (my vals may just be zeroes) - this does not write to a file but more reserves space for the file.
    //I dont believe I need any meta information - actuall I might to tell the read or what ever to not read that info its not empty space - its NO space - not meant to be interpreted. eh no actually its fine because
    //we put that info into the value. ok so lets go.

    //have map from filename to offset
    //
//BLOCKSIZE = PAGE SIZE!!!!
    //CLIENTS ITERACT WITH PAGES , PAGES INTERACT WITH BLOCKS
    //are blocks totally ordered or ordered by file? ANS - ordered by file each blockId has filename and then the (logical sequential) number of that block for that file
    //so a client gives a page and the specific logical block client want read/written. a page will be able to read/store that block
    //when reads its needs to exclude the meta file info like byte[] length and strignlength - that is doable
    @Override
    public void read(BlockIdBase blk, PageBase p) {//Is this saying I want you to read this block of this page?
        //RandomAccessFile reader = null;
//        fmLogger.log(Level.INFO,"FM read() trying to access block file " + blk.fileName() + " block number " + blk.number() + " ...");
        try (
                RandomAccessFile reader = new RandomAccessFile(filePath(blk.fileName()), "r");
                FileChannel channel = reader.getChannel();
        ) {
//            fmLogger.info("FM succesfully accessed file " + blk.fileName());
            ByteBuffer buff = ByteBuffer.allocate(blocksize);
            int noOfBytesRead = channel.read(buff, blk.number() * blocksize);
            p.setBytes(-1, buff.array());
//            fmLogger.info("FM read bytes into buffer and then into page. The bytes in page as a string are " + p.getString(0));
        } catch (IOException e) {
            fmLogger.info("FM Could not open file " + blk.fileName());
            e.printStackTrace();
        }
        //read block bytes from file WRITE them into the page. so now user can read by calling page.getXXX
        //or is it saying I want you to read this block and load it into this page
        //considering block = page then one would go without saying . so prob read this block into this page...how do these block ids work?
        //blocks go by file not by page I think page is just the main me holder
        //read block size
    }

    //maybe the page set methods put it in but the read methods take it out - how do you write to disk and add the meta info wwhich is read from a page and
    //is just generically bytes[] (you cant assume anything else!)while at the same time reading from a page without the meta info.
    // we need to read from a page and have the
    //meta info there to properly write, but when we read from a page we dont want the meta info there
    //possible solution is that the bytes of a page contains the meta and data but when we call get getInt it simply just gets
    //an Int at whatever offset (this is obviously on the user to know it is an int they are dealing with
    //Cwhen we call getBytes() we just generically return the bytes no cutting off info we just return the byutes in the block (return meta and data)
    //when we call getStreing() we look at the info and return just the string
    //when we call setInt() just set the int
    //when we call setBytes() actually does deal with inserting the metadata
    //when we call set string i believe that too would insert the metadata
    //when it writes it needs to put add that info if byte[] or string how does it know?
    @Override
    public void write(BlockIdBase blk, PageBase p) {
//        fmLogger.info("FM write() checking if file " + blk.fileName()  + " exists...");
        if (!Files.exists(Paths.get(filePath(blk.fileName())))) {//TODO All blkfile name should be prepended with actual path
//            fmLogger.info("FM It does not so we will create with append()");
            append(blk.fileName());
        }
//        fmLogger.info("FM write() trying to access block file ...");

        try (
                RandomAccessFile writer = new RandomAccessFile(filePath(blk.fileName()), "rw");
                FileChannel channel = writer.getChannel();
        ) {
//            fmLogger.info("FM accessed now writing...");
            int noOfBytesRead = channel.write(ByteBuffer.wrap(p.getBytes(-1)), blk.number() * blocksize);
//            fmLogger.info("FM write() wrote");
        } catch (IOException e) {
            fmLogger.info("FM Could not open file " + blk.fileName());
            e.printStackTrace();
        }
    }

    //also creates new files by allocating space for that file (a block)
    @Override
    public BlockIdBase append(String filename) {// THIS METHOD SIMPLY ALLOCATES ANOTHER BLOCK OF SPACE FOR A FILE
        int blockIdNum = -1;
        fmLogger.info("FM append() checking if file " + filename + " exists...");

        if (!Files.exists(Paths.get(filePath(filename)))) {
            fmLogger.info("FM it does not...create a new one");
            try {
                Files.createFile(Paths.get(filePath(filename)));
            } catch (IOException e) {
                fmLogger.info("FM failed to create!!!");
                e.printStackTrace();
            }
        }
        try (
                RandomAccessFile raf = new RandomAccessFile(filePath(filename), "rw");
        ) {
            raf.seek(raf.length());
            byte[] space = new byte[blocksize];
            raf.write(space);
            blockIdNum = (int) ((raf.length() / blocksize) - 1);//zero indexing
            fmLogger.info("FM append() wrote an empty block and is returning block# "+ blockIdNum);
        } catch (IOException e) {
            fmLogger.info("FM failed to access or write to file");
            e.printStackTrace();
        }
        return new BlockId(filename, blockIdNum);
    }

    @Override
    public int length(String filename) {//# of blocks that are used to store the file
        int length = 0;
//        fmLogger.info("FM length() trying to access file " + filename + "...");
        try (
                RandomAccessFile raf = new RandomAccessFile(filePath(filename), "rw");
        ) {
            length = (int) (raf.length() / blocksize);
            fmLogger.info("FM the length of this file is  " + length);
        } catch (IOException e) {
            fmLogger.info("failed to access file");
            e.printStackTrace();
        }
        return length;
    }

    @Override
    public boolean isNew() {
        return !dbDirectoryPath.isDirectory();
    }

    @Override
    public int blockSize() {
        return blocksize;
    }

    private String filePath(String fileName) {
        return dbDirectoryPath.getAbsolutePath() + File.separator + fileName;
    }

}

