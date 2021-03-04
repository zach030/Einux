package os.filesystem;

public class SuperBlock implements BlockZone {
    int blockNo;
    Block block;

    SuperBlock(int blockNo) {
        this.blockNo = blockNo;
        initZoneBlocks();
    }

    public void writeBlock(Block block) {
        this.block = block;
        // 同步数据到磁盘
        this.block.syncBlock();
    }

    @Override
    public void write(int blockNo, int offset, short data) {
        this.block.write(offset, data);
        this.block.syncBlock();
    }

    @Override
    public short read(int blockNo, int offset) {
        return this.block.read(offset);
    }

    @Override
    public int getRelativeBlockNo(int blockNo) {
        return 0;
    }

    @Override
    public Block getBlock(int blockNo) {
        return block;
    }

    public void initZoneBlocks() {
        this.block = new Block(blockNo);
    }
}
