package hardware.mm;

import hardware.Page;

import java.util.Arrays;

public class PCBZone implements MemoryZone {
    int index;
    int size;
    Page[] pages;

    // 一个pcb占一个页
    PCBZone() {
        index = Memory.PCB_ZONE_START;
        pages = new Page[Memory.PCB_ZONE_SIZE];
        Arrays.fill(pages, new Page());
        size = Memory.PCB_ZONE_SIZE;
    }

    @Override
    public void write(int pageNo, int offset, short data) {
        this.pages[pageNo].write(offset, data);
    }

    @Override
    public short read(int pageNo, int offset) {
        return pages[pageNo].read(offset);
    }

    @Override
    public void replacePage(Page page) {
        this.pages[getRelativePageNo(page.getFrameNo())] = page;
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
}