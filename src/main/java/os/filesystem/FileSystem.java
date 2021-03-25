package os.filesystem;

import disk.DevConfig;
import hardware.memory.Memory;
import utils.SysConst;

// todo 将文件系统与磁盘拆分
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

    public static final int MAX_INODE_NUM = INODE_ZONE_SIZE * DevConfig.BLOCK_SIZE / Inode.INODE_SIZE;

    Inode root;   // 根目录
    Inode pwd;    // 当前目录
    Inode parent; // 父目录

    // 内存活动inode
    Inode[] activeINodeList = new Inode[Memory.memory.getBufferPool().getActiveInodeNum()];          // 内存缓冲区存放的活动inode
    // 磁盘inode
    Inode[] diskInodeList = new Inode[MAX_INODE_NUM];
    // 系统打开文件表
    SysFile[] sysOpenFileTable = new SysFile[Memory.memory.getBufferPool().getActiveInodeNum()];

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

        initInodeList();
        initRootDir();
        initSysFileOpenTable();
    }

    //----------初始化文件系统---------------
    void initInodeList() {
        for (int i = 0; i < activeINodeList.length; i++) {
            activeINodeList[i] = new Inode();
        }
        for (int i = 0; i < diskInodeList.length; i++) {
            diskInodeList[i] = new Inode();
        }
    }

    void initRootDir() {
        root = allocInode(SysConst.DEFAULT_DISK);
        root.setFileType(Inode.FileType.DIR);
        root.setAuthority(Inode.Authority.READ, Inode.Authority.WRITE, Inode.Authority.EXEC);
        pwd = root;
    }

    void initSysFileOpenTable() {
        for (SysFile sysFile : sysOpenFileTable) {
            sysFile.count = 0;
        }
    }

    void initSuperBlock() {
        superBlock = new SuperBlock(SUPER_BLOCK_INDEX);
        //todo 应该从磁盘中读
        superBlock.setAvailableInodeNum(MAX_INODE_NUM);   // 初始化可用inode数目
        superBlock.setBlockNum(DATA_ZONE_SIZE);           // 设置数据块大小
        superBlock.setAvailableBlockNum(DATA_ZONE_SIZE);  // 设置可用磁盘块数目
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

    //--------------------文件系统API------------------
    public Inode nameI(String path,int mode) {
        //0 根目录
        if (path.equals("/")) {
            return getInodeByNo(SysConst.DEFAULT_DISK, 0);
        }
        return null;
    }

    public Inode getInodeByNo(int devNo, int inodeNo) {
        Inode newInode = null;
        for (int i = 0; i < activeINodeList.length; i++) {
            // 如果找到，返回内存活动inode
            if (activeINodeList[i].devNo == devNo && activeINodeList[i].inodeNo == inodeNo) {
                activeINodeList[i].referCnt++;
                return activeINodeList[i];
            }
            //
            if (newInode == null && activeINodeList[i].referCnt == 0) {
                newInode = activeINodeList[i];
            }
        }
        return null;
    }

    // 分配inode，即分配块设备中inode区域的未分配inode以及活动inode表里的inode
    public Inode allocInode(int devNo) {
        if (superBlock.availableInodeNum > 0) {
            int freeInodeNo = superBlock.getFreeInode();
            if (freeInodeNo == -1) {
                // todo not-found
            }
            return getInodeByNo(devNo, freeInodeNo);
        }
        return null;
    }

    // 为inode分配系统打开文件表
    public int allocSysFileOpenTable(Inode inode) {
        for (int i = 0; i < sysOpenFileTable.length; i++) {
            if (sysOpenFileTable[i].count == 0) {
                sysOpenFileTable[i].setInode(inode);
                return i;
            }
        }
        return -1;
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
