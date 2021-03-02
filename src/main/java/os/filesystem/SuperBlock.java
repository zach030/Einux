package os.filesystem;

public class SuperBlock implements BlockZone{
    int blockNo;
    Block block;

    SuperBlock(int blockNo){
        this.blockNo = blockNo;
    }
    @Override
    public void writeBlock(Block block) {

    }

    @Override
    public void write(int offset, short data) {

    }

    @Override
    public short read(int offset) {
        return 0;
    }

}
