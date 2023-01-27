package edu.yu.dbimpl.file;

public class BlockId extends BlockIdBase {
    private String fileName;
    private int blockNum;
    public BlockId(String filename, int blknum) {
        super(filename, blknum);
        this.blockNum = blknum;
        this.fileName = filename;
    }

    @Override
    public String fileName() {
        return fileName;
    }

    @Override
    public int number() {
        return blockNum;
    }

    @Override
    public boolean equals(Object other){
        if(other instanceof BlockId) {
            BlockIdBase blk = (BlockIdBase) other;
            if (this.fileName.equals(blk.fileName()) && this.number() == blk.number()) {
                return true;
            } else {
                return false;
            }
        }
        else{
            return false;
        }
    }

    @Override
    public String toString(){
        return fileName + "Block # " + blockNum;
    }
}
