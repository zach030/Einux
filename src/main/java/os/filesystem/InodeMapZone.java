package os.filesystem;

public class InodeMapZone implements BlockZone {
    int blockNo;
    Block block;

    InodeMapZone(int blockNo) {
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
        block.write(offset, data);
        block.syncBlock();
    }

    @Override
    public short read(int blockNo, int offset) {
        return block.read(offset);
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
