package hardware.mm;

import hardware.Page;
import hardware.mm.Memory;
import hardware.mm.MemoryZone;
import utils.SysConst;
import java.util.LinkedList;

// 内存缓冲区：管理
public class BufferPool implements MemoryZone {
    int index;
    int size;
    Page[] pages;
    //ActiveINodeZone activeINodeZone;
    int activeInodeZonePages = 2;            // 分配缓冲区的前两页给内存的活动inode
    int activeInodeSize = 32;                // 活动inode大小 32B
    int activeInodeNum = 2 * SysConst.PAGE_FRAME_SIZE / activeInodeSize; //总共的活动inode数

    public BufferPool() {
        index = Memory.BUFFER_START_NO;
        size = Memory.BUFFER_SIZE;
        pages = new Page[size];
        //activeINodeZone = new ActiveINodeZone(activeInodeZonePages);
    }

    public int getActiveInodeNum() {
        return activeInodeNum;
    }

    public void setActiveInodeNum(int activeInodeNum) {
        this.activeInodeNum = activeInodeNum;
    }

//    class ActiveINodeZone {
//        Page[] pages;
//        ArrayList<DiskInode> memoryDiskInodes = new ArrayList<>(activeInodeNum);
//
//        ActiveINodeZone(int size) {
//            this.pages = new Page[size];
//        }
//
//        public ArrayList<DiskInode> getMemoryDiskInodes() {
//            return memoryDiskInodes;
//        }
//
//        public void setMemoryDiskInodes(ArrayList<DiskInode> memoryDiskInodes) {
//            this.memoryDiskInodes = memoryDiskInodes;
//        }
//    }

//    public ActiveINodeZone getActiveINodeZone() {
//        return activeINodeZone;
//    }
//
//    public void setActiveINodeZone(ActiveINodeZone activeINodeZone) {
//        this.activeINodeZone = activeINodeZone;
//    }

    @Override
    public void write(int pageNo, int offset, short data) {

    }

    @Override
    public short read(int pageNo, int offset) {
        return (byte) pages[pageNo].read(offset);
    }

    @Override
    public int getRelativePageNo(int pageNo) {
        return pageNo - index;
    }

    @Override
    public void clearZone() {
        for (Page page : pages) {
            page.clearPage();
        }
    }

    // 输入：输入到进程工作空间；输出：输出到磁盘
    // 进程和磁盘都对缓冲区有读写操作
    // 进程：请求输入数据：1、获取空缓冲块；2、磁盘向空缓冲块写数据；3、挂上输入缓冲队列
    //       请求获得输入数据：1、输入队列对头出队；2、放进程工作区，进程读数据；3、空缓冲块返回空闲队列
    //       请求放入数据：1、获取空缓冲块；2、进程向里面写数据；3、挂到输出缓冲队列队尾
    //       请求输出数据到磁盘：1、输出队列对头出队；2、将数据写入磁盘；3、空闲缓冲块挂到空缓冲队尾
    // example：A进程要用户输入数据：1、请求输入数据；2、请求获得输入数据；3、A进程做处理；4、请求放入数据；5、输出数据到磁盘

    // 缓冲块
    class BufBlock {
        int type;// hin:收容磁盘输入，sout：提取向磁盘的输出,sin：提取向用户进程的输入，hout：收容用户进程的输出
    }

    // 空缓冲块队列
    LinkedList<BufBlock> emptyBufBlockList = new LinkedList<>();
    // 输入缓冲块队列
    LinkedList<BufBlock> inputBufBlockList = new LinkedList<>();
    // 输出缓冲块队列
    LinkedList<BufBlock> outputBufBlockList = new LinkedList<>();

    // 获取空闲缓冲区快
    public BufBlock getEmptyBlock() {
        return emptyBufBlockList.getFirst();
    }

    // 当磁盘将数据写入bufblock后，缓冲池将其挂到输入缓冲队列上
    public void addToInputBufBlock(BufBlock bufBlock) {
        inputBufBlockList.add(bufBlock);
    }

    // 从输入队列中获取队头缓冲块
    public BufBlock getInputBufBlock() {
        return inputBufBlockList.getFirst();
    }

    // 取出数据后，返回至空闲队列队尾
    public void addToEmptyBufBlock(BufBlock bufBlock) {
        emptyBufBlockList.add(bufBlock);
    }

    // 挂到输出缓冲队列队尾
    public void addToOutputBufBlock(BufBlock bufBlock) {
        outputBufBlockList.add(bufBlock);
    }

    public BufBlock getOutputBufBlock() {
        return outputBufBlockList.getFirst();
    }

}
