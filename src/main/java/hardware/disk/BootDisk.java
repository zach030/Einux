package hardware.disk;

import disk.DevConfig;
import disk.DiskHelper;
import disk.RealBlock;
import os.filesystem.DiskInode;
import utils.Log;

public class BootDisk implements disk.BootDisk {
    public static BootDisk bootDisk = new BootDisk();
    //-------------磁盘分区信息-------------------------------
    public static final int DEVICE_NO = 0;
    public static final int BLOCK_NUM = 19898;                  // 数据块总块数
    public static final int SUPER_BLOCK_INDEX = 1;              // 超级块在磁盘中的下标
    public static final int INODE_MAP_INDEX = 2;                // Inode map在磁盘中的下标
    public static final int DATA_MAP_INDEX = 3;                 // block map在磁盘中的下标
    public static final int INODE_ZONE_INDEX = 4;               // inode区在磁盘的下标
    public static final int INODE_ZONE_SIZE = 64;               // inode节点数
    public static final int DATA_ZONE_INDEX = 68;               // 数据区起始下标
    public static final int DATA_ZONE_SIZE = 19898;             // 数据区大小
    public static final int JCB_ZONE_INDEX = 19966;             // jcb区下标
    public static final int JCB_ZONE_SIZE = 256;                // jcb区大小
    public static final int SWAP_ZONE_INDEX = 20224;            // 交换区下标
    public static final int SWAP_ZONE_SIZE = 256;               // 交换区大小
    public static final int DISK_MAX_INODE_NUM = INODE_ZONE_SIZE * DevConfig.BLOCK_SIZE / DiskInode.INODE_SIZE;
    static final int OTHER_BLOCK_NUM = 20479;

    //-----------------------错误常量-------------------
    public static final String INVALID_BLOCK_NO = "无效的物理块号，未找到对应磁盘分区，请重试";

    //------------------成员----------------
    DiskHelper diskHelper;              // 驱动读写helper
    private SuperBlock superBlock;      //磁盘超级块
    private InodeMapZone inodeMapZone;  //inode位示图区
    private DataMapZone dataMapZone;    //数据块位示图区
    private InodeZone inodeZone;        //inode区
    private DataZone dataZone;          //数据区
    private JCBZone jcbZone;            //jcb区
    private SwapZone swapZone;          //交换区

    public BootDisk() {

    }
    /**
        * @description: 初始化启动盘
        * @author: zach
     **/
    public void initBootDisk(){
        setDiskHelper(DevConfig.CYLINDER_NUM, DevConfig.TRACK_NUM, DevConfig.SECTOR_NUM);
        loadDisk();
        initSuperBlock();
        initInodeMapZone();
        initDataMapZone();
        initInodeZone();
        initDataZone();
        initJCBZone();
        initSwapZone();
    }
    /**
        * @description: 初始化超级块：重点inode数据
        * @author: zach
     **/
    void initSuperBlock() {
        superBlock = new SuperBlock(SUPER_BLOCK_INDEX);
        //todo 应该从磁盘中读
        superBlock.loadSuperBlockData();
    }

    void initInodeMapZone() {
        inodeMapZone = new InodeMapZone(INODE_MAP_INDEX);
    }

    void initDataMapZone() {
        dataMapZone = new DataMapZone(DATA_MAP_INDEX);
    }

    void initInodeZone() {
        inodeZone = new InodeZone(INODE_ZONE_INDEX, INODE_ZONE_SIZE);
    }

    void initDataZone() {
        dataZone = new DataZone(DATA_ZONE_INDEX, DATA_ZONE_SIZE);
    }

    void initJCBZone() {
        jcbZone = new JCBZone(JCB_ZONE_INDEX, JCB_ZONE_SIZE);
    }

    void initSwapZone() {
        swapZone = new SwapZone(SWAP_ZONE_INDEX, SWAP_ZONE_SIZE);
    }

    //-------------------------磁盘读写API-----------------------
    // 提供：根据物理地址的读写；读写整个物理块；

    /**
     * @program: Einux
     * @description: 磁盘物理地址+数据 写
     * @author: zach
     **/
    // 15块号 + 9块内偏移
    public void write(int addr, short data) {
        int block = (addr >> 9) & 0X07FFF;
        int offset = (addr & 0X01FF);
        BlockZone blockZone = switchZone(block);
        if (blockZone == null) {
            Log.Error("磁盘读写", INVALID_BLOCK_NO);
        }
        blockZone.write(block, offset, data);
    }

    /**
     * @program: Einux
     * @description: 磁盘物理地址+数据 读
     * @author: zach
     **/
    public short read(int addr) {
        int block = (addr >> 9) & 0X07FFF;
        int offset = (addr & 0X01FF);
        BlockZone blockZone = switchZone(block);
        if (blockZone == null) {
            Log.Error("磁盘读写", INVALID_BLOCK_NO);
            return 0;
        }
        return blockZone.read(block, offset);
    }

    /**
     * @program: ${PROJECT_NAME}
     * @description: 写物理块整块
     * @author: zach
     **/
    public void writeBlock(Block block) {
        int blockNo = block.getBlockNo();
        BlockZone blockZone = switchZone(blockNo);
        if (blockZone == null) {
            Log.Error("磁盘读写", INVALID_BLOCK_NO);
            return;
        }
        blockZone.writeBlock(block);
    }

    /**
     * @program: ${PROJECT_NAME}
     * @description: 获取磁盘中的物理块
     * @author: zach
     **/
    // 获取磁盘内的物理块
    public Block getBlockInDisk(int blockNo) {
        BlockZone blockZone = switchZone(blockNo);
        if (blockZone == null) {
            Log.Error("磁盘读写", INVALID_BLOCK_NO);
            return null;
        }
        return blockZone.getBlock(blockNo);
    }

    /**
     * @program: ${PROJECT_NAME}
     * @description: 根据块号找到所在的磁盘分区
     * @author: zach
     **/
    private BlockZone switchZone(int block) {
        if (block == 1) {
            return superBlock;
        }
        if (block == 2) {
            return inodeMapZone;
        }
        if (block == 3) {
            return dataMapZone;
        }
        if (block >= 4 && block <= 67) {
            return inodeZone;
        }
        if (block >= 68 && block <= 19965) {
            return dataZone;
        }
        if (block >= 19966 && block <= 20223) {
            return jcbZone;
        }
        if (block >= 20224 && block <= 20479) {
            return swapZone;
        } else {
            return null;
        }
    }
    //-----------------磁盘启动程序----------
    /**
     * @description: 设置磁盘引导程序
     * @author: zach
     **/
    @Override
    public void setDiskHelper(int c, int t, int s) {
        diskHelper = new DiskHelper(c, t, s);
    }

    /**
     * @description: 创建虚拟磁盘
     * @author: zach
     **/
    @Override
    public void createDisk() {
        diskHelper.createVirtualDisk();
    }

    /**
     * @description: 格式化磁盘
     * @author: zach
     **/
    @Override
    public void initDisk() {
        diskHelper.initBlock(DevConfig.BOOT_BLOCK_INDEX, DevConfig.END_FLAG);
        diskHelper.initBlock(1, OTHER_BLOCK_NUM);
    }

    /**
        * @description: 开机启动加载磁盘全部数据
        * @author: zach
     **/
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


    public SuperBlock getSuperBlock() {
        return superBlock;
    }

    @Override
    public int getBootDiskNo() {
        return DEVICE_NO;
    }

    public void setSuperBlock(SuperBlock superBlock) {
        this.superBlock = superBlock;
    }

    public InodeMapZone getInodeMapZone() {
        return inodeMapZone;
    }

    public void setInodeMapZone(InodeMapZone inodeMapZone) {
        this.inodeMapZone = inodeMapZone;
    }

    public DataMapZone getDataMapZone() {
        return dataMapZone;
    }

    public void setDataMapZone(DataMapZone dataMapZone) {
        this.dataMapZone = dataMapZone;
    }

    public InodeZone getInodeZone() {
        return inodeZone;
    }

    public void setInodeZone(InodeZone inodeZone) {
        this.inodeZone = inodeZone;
    }

    public DataZone getDataZone() {
        return dataZone;
    }

    public void setDataZone(DataZone dataZone) {
        this.dataZone = dataZone;
    }

    public JCBZone getJcbZone() {
        return jcbZone;
    }

    public void setJcbZone(JCBZone jcbZone) {
        this.jcbZone = jcbZone;
    }

    public SwapZone getSwapZone() {
        return swapZone;
    }

    public void setSwapZone(SwapZone swapZone) {
        this.swapZone = swapZone;
    }
}
