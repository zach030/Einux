package hardware.memory;

import os.filesystem.DiskInode;
import utils.SysConst;

import java.util.LinkedList;

// 内存缓冲区：管理
public class BufferPool implements MemoryZone {
    int index;
    int size;
    Page[] pages;
    int activeInodeZonePages = 2;            // 分配缓冲区的前两页给内存的活动inode
    int activeInodeNum = 2 * SysConst.PAGE_FRAME_SIZE / DiskInode.INODE_SIZE; //总共的活动inode数

    public BufferPool() {
        index = Memory.BUFFER_START;
        size = Memory.BUFFER_SIZE;
        pages = new Page[size];
        initPages();
    }

    public int getActiveInodeNum() {
        return activeInodeNum;
    }

    public void setActiveInodeNum(int activeInodeNum) {
        this.activeInodeNum = activeInodeNum;
    }

    @Override
    public void write(int pageNo, int offset, short data) {

    }

    @Override
    public short read(int pageNo, int offset) {
        return (byte) pages[pageNo].read(offset);
    }

    @Override
    public void replacePage(Page page) {
        int pageNo = getRelativePageNo(page.getFrameNo());
        pages[pageNo] = page;
    }

    @Override
    public Page getPage(int pageNo) {
        return pages[getRelativePageNo(pageNo)];
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

    @Override
    public void clearPage(int pageNo) {
        pages[getRelativePageNo(pageNo)].clearPage();
    }

    @Override
    public void initPages() {
        for (int i = 0; i < size; i++) {
            Page page = new Page();
            this.pages[i] = page;
        }
    }
}
