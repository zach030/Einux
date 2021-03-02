package hardware.mm;

import hardware.Page;

import java.util.Arrays;

public class PageTableZone implements MemoryZone {
    int index;
    int size;
    Page[] pages;

    PageTableZone() {
        index = Memory.PAGE_TABLE_START;
        pages = new Page[Memory.PAGE_TABLE_SIZE];
        Arrays.fill(pages, new Page());
        size = Memory.PAGE_TABLE_SIZE;
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
