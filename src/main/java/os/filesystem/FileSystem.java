package os.filesystem;

public class FileSystem implements VFS {
    public static FileSystem fs = new FileSystem();

    //-------------磁盘分区信息-------------------------------
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

    SuperBlock superBlock;      //磁盘超级块
    InodeMapZone inodeMapZone;  //inode位示图区
    DataMapZone dataMapZone;    //数据块位示图区
    InodeZone inodeZone;        //inode区
    DataZone dataZone;          //数据区
    JCBZone jcbZone;            //jcb区
    SwapZone swapZone;          //交换区

    public FileSystem() {
        initSuperBlock();
        initInodeMapZone();
        initDataMapZone();
        initInodeZone();
        initDataZone();
        initJCBZone();
        initSwapZone();
    }

    //----------初始化磁盘各分区---------------
    void initSuperBlock() {
        superBlock = new SuperBlock(SUPER_BLOCK_INDEX);
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

    //---------------------磁盘的读写API---------------
    // 15块号 + 9块内偏移
    public void write(int addr, short data) {
        int block = (addr >> 9) & 0X07FFF;
        int offset = (addr & 0X01FF);
        BlockZone blockZone = switchZone(block);
        blockZone.write(block, offset, data);
    }

    public short read(int addr) {
        int block = (addr >> 9) & 0X07FFF;
        int offset = (addr & 0X01FF);
        BlockZone blockZone = switchZone(block);
        return blockZone.read(block, offset);
    }

    public void writeBlock(Block block) {
        int blockNo = block.getBlockNo();
        BlockZone blockZone = switchZone(blockNo);
        blockZone.writeBlock(block);
    }

    // 获取磁盘内的物理块
    public Block getBlockInDisk(int blockNo) {
        BlockZone blockZone = switchZone(blockNo);
        return blockZone.getBlock(blockNo);
    }

    BlockZone switchZone(int block) {
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

}
