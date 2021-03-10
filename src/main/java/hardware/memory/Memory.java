package hardware.memory;

import utils.SysConst;

public class Memory {
    public static Memory memory = new Memory();

    //----------内存分区情况---------------------------------------------------
    public static final int MEMORY_SIZE = SysConst.PAGE_FRAME_SIZE * SysConst.PAGE_NUM; // 内存32KB
    public static final int SYSTEM_ZONE_SIZE = 16 * SysConst.PAGE_FRAME_SIZE;            // 系统区 0~15
    public static final int USER_ZONE_SIZE = 32 * SysConst.PAGE_FRAME_SIZE;              // 用户区 16~47
    public static final int BUFFER_ZONE_SIZE = 16 * SysConst.PAGE_FRAME_SIZE;            // 缓冲区 48~63
    //---------各分区范围及大小-------------------------------------------------------
    public static final int OS_KERNAL_START = 0;                    // 操作系统内核起始
    public static final int OS_KERNAL_SIZE = 1;                     // 操作系统内核大小

    public static final int PAGE_TABLE_START = 1;                   // 系统区页表起始
    public static final int PAGE_TABLE_SIZE = 2;                    // 系统区页表大小

    public static final int PCB_POOL_START = 3;                     // PCB池起始页号
    public static final int PCB_POOL_SIZE = 13;                     // 系统区PCB池大小

    public static final int PCB_ZONE_START = 16;                    // PCB区起始页号
    public static final int PCB_ZONE_SIZE = 32;                     // PCB区大小

    public static final int BUFFER_START = 48;                      // 缓冲区起始块号
    public static final int BUFFER_SIZE = 16;                       // 缓冲区大小

    public static final int PAGE_TABLE_ENTRY_SIZE = 32;             // 页表项大小
    public static final int ONE_PAGE_HAS_TABLE_ENTRY = SysConst.PAGE_FRAME_SIZE / PAGE_TABLE_ENTRY_SIZE; // 一个页有的页表项数


    KernalZone kernalZone;           // os 内核区
    PageTableZone pageTableZone;     // 页表区
    PCBPoolZone pcbPoolZone;         // pcb池区
    PCBZone pcbZone;                 // pcb区
    BufferPool bufferPool;           // 缓冲区

    public Memory() {
        kernalZone = new KernalZone();
        pageTableZone = new PageTableZone();
        pcbPoolZone = new PCBPoolZone();
        pcbZone = new PCBZone();
        bufferPool = new BufferPool();
    }

    public void clearMemory() {
        kernalZone.clearZone();
        pageTableZone.clearZone();
        pcbPoolZone.clearZone();
        pcbZone.clearZone();
        bufferPool.clearZone();
    }

    public void writePage(Page page) {
        // 根据页框号，对内存中的页进行替换
        int pageFrameNo = page.getFrameNo();
        MemoryZone memoryZone = switchZone(pageFrameNo);
        memoryZone.replacePage(page);
    }

    public Page readPage(int frameNo) {
        MemoryZone memoryZone = switchZone(frameNo);
        return memoryZone.getPage(frameNo);
    }

    // 入参是物理地址：页框号(6 bit)+页内偏移(9 bit)
    public void writeData(short addr, short data) {
        // 将数据写入指定内存地址
        int page = (addr >> 9) & 0X003F;
        int offset = (addr & 0X01FF);
        MemoryZone memoryZone = switchZone(page);
        memoryZone.write(memoryZone.getRelativePageNo(page), offset, data);
    }

    // 清除内存该位的数据
    public void clearData(short addr) {
        int page = (addr >> 9) & 0X003F;
        int offset = (addr & 0X01FF);
        MemoryZone memoryZone = switchZone(page);
        memoryZone.write(memoryZone.getRelativePageNo(page), offset, (short) 0);
    }

    // 写32位大小的数据
    public void writeWordData(short addr, int data) {
        int page = (addr >> 9) & 0X003F;
        int offset = (addr & 0X01FF);
        MemoryZone memoryZone = switchZone(page);
        short lowData = (short) (data & 0X0000FFFF);
        short highData = (short) (data >> 16 & 0X0000FFFF);
        memoryZone.write(memoryZone.getRelativePageNo(page), offset, lowData);
        //todo 会不会跨页
        memoryZone.write(memoryZone.getRelativePageNo(page), offset + 2, highData);
    }

    // 入参是物理地址 15位:(6位页框号，9位页内偏移)
    public short readData(short addr) {
        // 物理地址分为页框号+页内偏移
        int page = (addr >> 9) & 0X003F;
        int offset = (addr & 0X01FF);
        // 通过页框号，找到对应哪个zone的页，再操作
        MemoryZone memoryZone = switchZone(page);
        return memoryZone.read(memoryZone.getRelativePageNo(page), offset);
    }

    public int readWordData(short addr) {
        // 物理地址分为页框号+页内偏移
        int page = (addr >> 9) & 0X003F;
        int offset = (addr & 0X01FF);
        // 通过页框号，找到对应哪个zone的页，再操作
        MemoryZone memoryZone = switchZone(page);
        short lowData = memoryZone.read(memoryZone.getRelativePageNo(page), offset);
        short highData = memoryZone.read(memoryZone.getRelativePageNo(page), offset + 2);
        return (((int) lowData) & 0X0000FFFF) | (((int) highData << 16) & 0XFFFF0000);
    }

    MemoryZone switchZone(int page) {
        if (page == 0) {
            return kernalZone;
        }
        if (page >= 1 && page <= 2) {
            return pageTableZone;
        }
        if (page >= 3 && page <= 15) {
            return pcbPoolZone;
        }
        if (page >= 16 && page <= 47) {
            return pcbZone;
        }
        if (page >= 48 && page <= 63) {
            return bufferPool;
        }
        return null;
    }

    // 获取内存中pcb池的空闲数量
    public int getPcbPoolSize() {
        return this.pcbPoolZone.freeZoneSize;
    }

    // 判断内存中的pcb池是否已经满
    public boolean isPCBPoolHasEmpty() {
        return this.pcbPoolZone.getFreePCBZone() > 0;
    }

    // 从PCB池中分配一个空闲PCB
    public void distributedPCBPool() {

    }

    public BufferPool getBufferPool() {
        return bufferPool;
    }

    public void setBufferPool(BufferPool bufferPool) {
        this.bufferPool = bufferPool;
    }

    public KernalZone getKernalZone() {
        return kernalZone;
    }

    public void setKernalZone(KernalZone kernalZone) {
        this.kernalZone = kernalZone;
    }

    public PageTableZone getPageTableZone() {
        return pageTableZone;
    }

    public void setPageTableZone(PageTableZone pageTableZone) {
        this.pageTableZone = pageTableZone;
    }

    public PCBPoolZone getPcbPoolZone() {
        return pcbPoolZone;
    }

    public void setPcbPoolZone(PCBPoolZone pcbPoolZone) {
        this.pcbPoolZone = pcbPoolZone;
    }

    public PCBZone getPcbZone() {
        return pcbZone;
    }

    public void setPcbZone(PCBZone pcbZone) {
        this.pcbZone = pcbZone;
    }
}
