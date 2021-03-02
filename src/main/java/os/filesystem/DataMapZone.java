package os.filesystem;

public class DataMapZone implements BlockZone {
    int blockNo;
    Block block;

    DataMapZone(int blockNo) {
        this.blockNo = blockNo;
    }

    @Override
    public void writeBlock(Block block) {

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

}
