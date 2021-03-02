package disk;

public interface RealBlock {
    // 磁盘物理块的初始化方法
    void initDiskBlock();

    // 读磁盘：块内偏移
    short readBlock(int offset);

    // 写磁盘：块内偏移 + 数据
    void writeBlock(int offset, short content);

    // 判断盘块标志位
    boolean checkBlockFlag();
}
