package disk;

public interface RealBlock {
    // 磁盘物理块的初始化方法
    void initDiskBlock();

    byte[] getAllData();
    // 读磁盘：块内偏移
    short read(int offset);

    // 写磁盘：块内偏移 + 数据
    void write(int offset, short content);

    // 写整个块
    void writeBlock(byte[] data);

    // 判断盘块标志位
    boolean checkBlockFlag();
}
