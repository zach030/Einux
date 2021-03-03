package os.filesystem;

import java.util.ArrayList;

public class DataZone implements BlockZone {
    int startBlockNo;
    int zoneSize;
    ArrayList<Block> blocks;

    DataZone(int start, int size) {
        this.startBlockNo = start;
        this.zoneSize = size;
        blocks = new ArrayList<>(size);
        initZoneBlocks();
    }

    @Override
    public void writeBlock(Block block) {
        this.blocks.set(getRelativeBlockNo(block.getBlockNo()), block);
        this.blocks.get(getRelativeBlockNo(block.getBlockNo())).syncBlock();
    }

    @Override
    public void write(int blockNo, int offset, short data) {
        this.blocks.get(getRelativeBlockNo(blockNo)).write(offset, data);
        this.blocks.get(getRelativeBlockNo(blockNo)).syncBlock();
    }

    @Override
    public short read(int blockNo, int offset) {
        return this.blocks.get(getRelativeBlockNo(blockNo)).read(offset);
    }

    @Override
    public int getRelativeBlockNo(int blockNo) {
        return blockNo - startBlockNo;
    }

    public void initZoneBlocks() {
        for (int i = 0; i < zoneSize; i++) {
            // 物理块号
            Block block = new Block(i + startBlockNo);
            blocks.add(block);
        }
    }

}
