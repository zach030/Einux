package hardware.mm;

import hardware.Page;

public class KernalZone implements MemoryZone {
    int index;
    int size;
    Page page;

    KernalZone() {
        size = Memory.OS_KERNAL_SIZE;
        index = Memory.OS_KERNAL_START;
        page = new Page();
    }

    @Override
    public void write(int pageNo, int offset, short data) {
        page.write(offset, data);
    }

    @Override
    public short read(int pageNo, int offset) {
        return page.read(offset);
    }

    @Override
    public void replacePage(Page page) {
        this.page = page;
    }

    @Override
    public int getRelativePageNo(int pageNo) {
        return pageNo - index;
    }

    @Override
    public void clearZone() {
        page.clearPage();
    }
}
