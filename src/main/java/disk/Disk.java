package disk;

import hardware.Page;

//硬盘:外存
public class Disk implements BootDisk {
    public static BootDisk disk = new Disk();

    DiskHelper diskHelper;      // 驱动读写

    static final int OTHER_BLOCK_NUM = 20479;

    public Disk() {
        setDiskHelper(DevConfig.CYLINDER_NUM, DevConfig.TRACK_NUM, DevConfig.SECTOR_NUM);
    }

    // set disk helper
    @Override
    public void setDiskHelper(int c, int t, int s) {
        diskHelper = new DiskHelper(c, t, s);
    }

    // 创建磁盘
    @Override
    public void createDisk() {
        diskHelper.createVirtualDisk();
    }

    // 初始化磁盘数据
    @Override
    public void initDisk() {
        diskHelper.initBlock(DevConfig.BOOT_BLOCK_INDEX, DevConfig.END_FLAG);
        diskHelper.initBlock(1, OTHER_BLOCK_NUM);
    }

    // 加载全部磁盘数据
    @Override
    public void loadDisk() {
        diskHelper.loadBlockEntries();
    }

    @Override
    public RealBlock getBlock(int index) {
        return diskHelper.getBlock(index);
    }

    @Override
    public boolean isBlockEmpty(int index) {
        return getBlock(index).checkBlockFlag();
    }

    // 读数据：块号 + 块内偏移
    @Override
    public short readData(int addr) {
        return diskHelper.readBlock(addr >> 9, addr & 0X01FF);
    }

    @Override
    public void writeData(int addr, short data) {
        diskHelper.updateBlock(addr >> 9, addr & 0X01FF, data);
    }

}
