package hardware.memory;

// interface of all memory zone
public interface MemoryZone {
    void write(int pageNo, int offset, short data);

    short read(int pageNo, int offset);

    void replacePage(Page page);

    Page getPage(int pageNo);

    int getRelativePageNo(int pageNo);

    void clearZone();

    void clearPage(int pageNo);

    void initPages();
}
