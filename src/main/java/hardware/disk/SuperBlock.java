package hardware.disk;

import java.util.ArrayList;
import java.util.Arrays;

public class SuperBlock implements BlockZone {
    int blockNo;
    Block block;

    ArrayList<Integer> freeInode;   // 空闲inode列表
    ArrayList<Integer> freeDataBlock; // 空闲数据块
    //todo 需要移到inode map盘吗?
    boolean[] inodeBitMap;
    boolean[] dataBitMap;
    int availableInodeNum;                            // 可用inode数
    int blockNum;            // 总块数
    int availableBlockNum;   // 可用块数

    SuperBlock(int blockNo) {
        this.blockNum = BootDisk.DATA_ZONE_SIZE;
        this.blockNo = blockNo;
        inodeBitMap = new boolean[BootDisk.DISK_MAX_INODE_NUM];
        dataBitMap = new boolean[BootDisk.DATA_ZONE_SIZE];
        initZoneBlocks();
    }

    public void writeBlock(Block block) {
        this.block = block;
        // 同步数据到磁盘
        this.block.syncBlock();
    }

    @Override
    public void write(int blockNo, int offset, short data) {
        this.block.write(offset, data);
        this.block.syncBlock();
    }

    @Override
    public short read(int blockNo, int offset) {
        return this.block.read(offset);
    }

    @Override
    public int getRelativeBlockNo(int blockNo) {
        return 0;
    }

    @Override
    public Block getBlock(int blockNo) {
        return block;
    }

    public void initZoneBlocks() {
        this.block = new Block(blockNo);
    }

    synchronized public void modifyInodeBitMap(int no, boolean status) {
        this.inodeBitMap[no] = status;
    }

    synchronized public void modifyDataBlockBitMap(int no, boolean status) {
        this.dataBitMap[no] = status;
    }

    //todo 获取磁盘空闲inode
    public int getFreeInode() {
        if (availableInodeNum > 0) {
            // 从空闲inode链表中取头一个
            int nodeNo = freeInode.remove(0);
            // 分配磁盘inode
            return allocInode(nodeNo);
        }
        return -1;
    }

    /**
        * @description: 增加空闲inode
        * @author: zach
     **/
    public void addFreeInode(int inodeNo) {
        modifyInodeBitMap(inodeNo, false);
        this.availableInodeNum++;
        freeInode.add(inodeNo);
    }

    // 分配磁盘inode的原子操作
    public int allocInode(int no) {
        modifyInodeBitMap(no, true);
        this.availableInodeNum--;
        return no;
    }

    public int getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }

    public Block getBlock() {
        return block;
    }

    public void setBlock(Block block) {
        this.block = block;
    }

    public int getAvailableInodeNum() {
        return availableInodeNum;
    }

    //todo 全部改为从磁盘读
    public void loadSuperBlockData() {
        this.setAvailableBlockNum(BootDisk.DATA_ZONE_SIZE);
        this.setAvailableInodeNum(BootDisk.DISK_MAX_INODE_NUM);
    }

    public void setAvailableInodeNum(int availableInodeNum) {
        // 设置位示图
        Arrays.fill(inodeBitMap, false);
        // 设置空闲inode链表
        freeInode = new ArrayList<>(availableInodeNum);
        for (int i = 0; i < availableInodeNum; i++) {
            freeInode.add(i);
        }
        this.availableInodeNum = availableInodeNum;
    }

    public int getBlockNum() {
        return blockNum;
    }

    public void setBlockNum(int blockNum) {
        this.blockNum = blockNum;
    }

    public int getAvailableBlockNum() {
        return availableBlockNum;
    }

    /**
     * @description: 分配磁盘数据块
     * @author: zach
     **/
    public int getFreeDataBlock() {
        if (availableBlockNum > 0) {
            // 从空闲数据块链表中取头一个
            int blockNo = freeDataBlock.remove(0);
            // 分配磁盘数据块
            return allocDataBlock(blockNo);
        }
        return -1;
    }

    // 分配磁盘inode的原子操作
    public int allocDataBlock(int no) {
        modifyDataBlockBitMap(no, true);
        this.availableBlockNum--;
        return no;
    }

    public void setAvailableBlockNum(int availableBlockNum) {
        Arrays.fill(dataBitMap, false);
        this.availableBlockNum = availableBlockNum;
        freeDataBlock = new ArrayList<>(availableBlockNum);
        for (int i = 0; i < availableBlockNum; i++) {
            freeDataBlock.add(i + BootDisk.DATA_ZONE_INDEX);
        }
    }
}
