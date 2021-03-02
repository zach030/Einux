package os.filesystem;

import hardware.Page;

import java.util.ArrayList;

public class InodeZone implements BlockZone {
    int startBlockNo;
    int zoneSize;
    ArrayList<Block> blocks;

    InodeZone(int start, int size) {
        this.startBlockNo = start;
        this.zoneSize = size;
        blocks = new ArrayList<>(size);
    }

    @Override
    public void writeBlock(Block block) {

    }

    @Override
    public void write(int blockNo, int offset, short data) {
        this.blocks.get(getRelativeBlockNo(blockNo)).write(offset, data);
    }

    @Override
    public short read(int blockNo, int offset) {
        return this.blocks.get(getRelativeBlockNo(blockNo)).read(offset);
    }

    @Override
    public int getRelativeBlockNo(int blockNo) {
        return blockNo - startBlockNo;
    }

}
