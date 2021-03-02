package disk;

// 通用磁盘接口
public interface BootDisk {
    // 设置磁盘helper
    void setDiskHelper(int c, int t, int s);

    // 创建磁盘
    void createDisk();

    // 加载全部磁盘数据
    void loadDisk();

    // 初始化全部磁盘
    void initDisk();

    // 获得block
    RealBlock getBlock(int index);

    // 块是否为空
    boolean isBlockEmpty(int index);

    // 物理地址读数据
    short readData(int addr);

    // 根据物理地址向磁盘写数据
    void writeData(int addr, short data);
}
