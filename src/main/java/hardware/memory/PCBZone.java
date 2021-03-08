package hardware.memory;

public class PCBZone implements MemoryZone {
    int index;
    int size;
    Page[] pages;

    // 一个pcb占一个页
    PCBZone() {
        index = Memory.PCB_ZONE_START;
        pages = new Page[Memory.PCB_ZONE_SIZE];
        size = Memory.PCB_ZONE_SIZE;
        initPages();
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