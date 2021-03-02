package os.filesystem;

public interface BlockZone {
    void writeBlock(Block block);

    void write(int blockNo,int offset, short data);

    short read(int blockNo,int offset);

    int getRelativeBlockNo(int blockNo);
}
