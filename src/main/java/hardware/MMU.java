package hardware;

import hardware.mm.Memory;
import utils.SysConst;

public class MMU {
    public static MMU mmu = new MMU();
    public TLB tlb = TLB.tlb;
    int pageTableBaseAddr;        //页表基址寄存器
    int pageNums;                 //需要页数
    public static final int NOT_FOUND_ERROR = -1;

    public int ResolveLogicalAddress(short VA) {
        // 高7位页号，低9位页内偏移
        int pageNo = (VA >> 9) & 0X007F;
        int offset = VA & 0X01FF;
        System.out.println("[MMU]-----将逻辑地址:" + VA + ",分解为:页号" + pageNo + ",页内偏移:" + offset);
        System.out.println("[TLB]-----开始搜索快表...");
        int pageFrameNo = tlb.searchTLB(pageNo);
        if (pageFrameNo == NOT_FOUND_ERROR) {
            System.out.println("[TLB]------快表未命中，开始查询页表...");
            pageFrameNo = searchPageTable(pageNo);
            if (pageFrameNo == NOT_FOUND_ERROR) {
                System.out.println("[MMU]-----页表未命中.....");
                return pageFrameNo;
            } else {
                System.out.println("[MMU]-----页表命中，查询出页框号为:" + pageFrameNo);
                tlb.addTLB(pageNo, pageFrameNo);
                return pageFrameNo * SysConst.PAGE_FRAME_SIZE + offset;
            }
        } else {
            System.out.println("[TLB]-----快表命中，查询出页框号为:" + pageFrameNo);
            return pageFrameNo * SysConst.PAGE_FRAME_SIZE + offset;
        }
    }

    // 查询进程的页表
    int searchPageTable(int virtualPageNum) {
        for (int i = 0, j = 0; i < pageNums; i++, j += 4)
        {
            int pageNum = Memory.memory.readData((short)(pageTableBaseAddr + j));
            if (pageNum == virtualPageNum)
                return Memory.memory.readData((short)(pageTableBaseAddr + j + 2));
        }
        return NOT_FOUND_ERROR;
    }

    // 进程访问时，更新MMU
    public void initMMU(int addr, int pageNum) {
        this.pageTableBaseAddr = addr;
        this.pageNums = pageNum;
        tlb.flushTLB();
    }
}
