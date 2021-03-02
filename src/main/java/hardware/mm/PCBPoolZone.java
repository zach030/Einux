package hardware.mm;

import hardware.Page;

import java.util.Arrays;

public class PCBPoolZone implements MemoryZone {

    int freeZoneSize = Memory.PCB_POOL_SIZE;
    int index;
    int size;
    Page[] pages;
    // pcb池分配位示图
    boolean[] useBitMap = new boolean[Memory.PCB_POOL_SIZE];

    PCBPoolZone() {
        index = Memory.PCB_POOL_START;
        pages = new Page[Memory.PCB_POOL_SIZE];
        Arrays.fill(pages, new Page());
        size = Memory.PCB_POOL_SIZE;
        Arrays.fill(useBitMap, false);
    }

    public int getFreePCBZone() {
        return freeZoneSize;
    }

    @Override
    public void write(int pageNo, int offset, short data) {
        pages[pageNo].write(offset, data);
    }

    @Override
    public short read(int pageNo, int offset) {
        return pages[pageNo].read(offset);
    }

    @Override
    public void replacePage(Page page) {
        this.pages[getRelativePageNo(page.getFrameNo())] = page;
    }

    // 区域内相对页框号
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

    // 分配空闲的pcb pool
    public int allocNewPos() {
        for (int i = 0; i < useBitMap.length; i++) {
            // 遍历到第一个未用的位，就返回
            if (!useBitMap[i]) {
                // 返回物理页框号
                return i + Memory.PCB_POOL_SIZE;
            }
        }
        return -1;
    }
}
