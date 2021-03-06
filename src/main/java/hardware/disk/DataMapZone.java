package hardware.disk;

import os.filesystem.FileSystem;

public class DataMapZone implements BlockZone {
    int blockNo;
    Block block;
    boolean[] blockBitMap = new boolean[BootDisk.BLOCK_NUM];
    DataMapZone(int blockNo) {
        this.blockNo = blockNo;
        initZoneBlocks();
    }


    @Override
    public void writeBlock(Block block) {
        this.block = block;
        this.block.syncBlock();
    }

    @Override
    public void write(int blockNo, int offset, short data) {
        this.block.write(offset, data);
    }

    @Override
    public short read(int blockNo, int offset) {
        return this.block.read(offset);
    }

    @Override
    public int getRelativeBlockNo(int blockNo) {
        return blockNo - this.blockNo;
    }

    @Override
    public Block getBlock(int blockNo) {
        return block;
    }

    public void initZoneBlocks() {
        this.block = new Block(blockNo);
    }
}
