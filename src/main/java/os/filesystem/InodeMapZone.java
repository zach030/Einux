package os.filesystem;

public class InodeMapZone implements BlockZone {
    int blockNo;
    Block block;

    InodeMapZone(int blockNo) {
        this.blockNo = blockNo;
    }

    @Override
    public void writeBlock(Block block) {

    }

    @Override
    public void write(int blockNo, int offset, short data) {
        block.write(offset,data);
    }

    @Override
    public short read(int blockNo, int offset) {
        return block.read(offset);
    }

    @Override
    public int getRelativeBlockNo(int blockNo) {
        return blockNo - this.blockNo;
    }

}
