package hardware;

import hardware.memory.Memory;
import utils.SysConst;

public class MMU {
    public static MMU mmu = new MMU();
    public TLB tlb = TLB.tlb;
    int pageTableBaseAddr;        //页表基址寄存器
    int pageNums;                 //需要页数
    public static final int NOT_FOUND_ERROR = -1;

    public int ResolveLogicalAddress(short VA) {
        // 高7位页号，低9位页内偏移
        //1、将地址分解为逻辑页号与页内偏移
        int pageNo = (VA >> 9) & 0X007F;
        int offset = VA & 0X01FF;
        System.out.println("[MMU]-----将逻辑地址:" + VA + ",分解为:页号" + pageNo + ",页内偏移:" + offset);
        System.out.println("[TLB]-----开始搜索快表...");
        //2、用逻辑页号查询快表
        int pageFrameNo = tlb.searchTLB(pageNo);
        if (pageFrameNo == NOT_FOUND_ERROR) {
            //2.1 查不到去查页表
            System.out.println("[TLB]------快表未命中，开始查询页表...");
            pageFrameNo = searchPageTable(pageNo);
            if (pageFrameNo == NOT_FOUND_ERROR) {
                System.out.println("[MMU]-----页表未命中.....");
                return NOT_FOUND_ERROR;
            } else {
                System.out.println("[MMU]-----页表命中，查询出页框号为:" + pageFrameNo);
                //2.2 查到页框号，写入快表，并返回
                tlb.addTLB(pageNo, pageFrameNo);
                return pageFrameNo * SysConst.PAGE_FRAME_SIZE + offset;
            }
        } else {
            //3、查到了，返回物理地址
            System.out.println("[TLB]-----快表命中，查询出页框号为:" + pageFrameNo);
            return pageFrameNo * SysConst.PAGE_FRAME_SIZE + offset;
        }
    }

    // 查询进程的页表
    int searchPageTable(int virtualPageNum) {
        for (int i = 0, j = 0; i < pageNums; i++, j += Memory.PAGE_TABLE_ENTRY_SIZE) {
            //todo 用地址查询页表项有问题 !!!!!
            int pte = Memory.memory.readWordData((short) (pageTableBaseAddr + j));
            // 取得此页表项对应的逻辑页号
            int virtualNo = pte >> 25 & 0X0000007F;
            if (virtualNo == virtualPageNum) {
                // 返回此页表项对应的页框号
                return pte >> 19 & 0X0000003F;
            }
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
