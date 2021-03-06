package hardware.memory;

// interface of all memory zone
public interface MemoryZone {
    void write(int pageNo, int offset, short data);

    short read(int pageNo, int offset);

    void replacePage(Page page);

    int getRelativePageNo(int pageNo);

    void clearZone();

    void initPages();
}
