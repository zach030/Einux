package hardware.mm;

// interface of all memory zone
public interface MemoryZone {
    void write(int pageNo, int offset, short data);
    short read(int pageNo, int offset);
    int getRelativePageNo(int pageNo);
    void clearZone();
}
