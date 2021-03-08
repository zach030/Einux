package hardware.memory;

public class PCBPoolZone implements MemoryZone {

    int freeZoneSize = Memory.PCB_POOL_SIZE;
    int index;
    int size;
    Page[] pages;

    PCBPoolZone() {
        index = Memory.PCB_POOL_START;
        pages = new Page[Memory.PCB_POOL_SIZE];
        size = Memory.PCB_POOL_SIZE;
        initPages();
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

    @Override
    public Page getPage(int pageNo) {
        return pages[getRelativePageNo(pageNo)];
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

    @Override
    public void initPages() {
        for (int i = 0; i < size; i++) {
            Page page = new Page();
            this.pages[i] = page;
        }
    }
}
