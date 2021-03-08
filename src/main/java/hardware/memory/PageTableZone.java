package hardware.memory;

import os.process.PageTableEntry;

public class PageTableZone implements MemoryZone {
    int index;
    int size;
    Page[] pages;                       // 页表所占的页
    PageTableEntry[] pageTableEntries;  // 全部的页表项

    PageTableZone() {
        index = Memory.PAGE_TABLE_START;
        pages = new Page[Memory.PAGE_TABLE_SIZE];
        size = Memory.PAGE_TABLE_SIZE;
        initPages();

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
    public void initPages() {
        for (int i = 0; i < size; i++) {
            Page page = new Page();
            this.pages[i] = page;
        }
    }
}
