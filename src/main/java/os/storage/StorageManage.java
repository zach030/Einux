package os.storage;

import hardware.CPU;
import hardware.mm.Memory;
import os.filesystem.FileSystem;
import os.process.PCB;
import utils.SysConst;

import java.util.Arrays;

public class StorageManage {
    public static StorageManage sm = new StorageManage();

    public StorageManage() {
        Arrays.fill(sysPageTableUsed, false);
        Arrays.fill(memoryUsageBitmap, false);
        Arrays.fill(swapAreaUsageBitmap, false);
    }

    //-------------------------存储位示图管理------------------------
    // 系统页表使用情况
    boolean[] sysPageTableUsed = new boolean[SysConst.PAGE_FRAME_SIZE * Memory.PAGE_TABLE_SIZE / Memory.PAGE_TABLE_ENTRY_SIZE];
    // 内存页框位示图
    boolean[] memoryUsageBitmap = new boolean[SysConst.PAGE_NUM];
    // 交换区使用情况位示图
    boolean[] swapAreaUsageBitmap = new boolean[FileSystem.SWAP_BLOCK_RANGE[1] - FileSystem.SWAP_BLOCK_RANGE[0] + 1];

    //----------------------修改位示图---------------
    // 修改内存页框位示图
    synchronized public void modifyMemoryPageBitMap(int pageFrameNo) {
        this.memoryUsageBitmap[pageFrameNo] = true;
    }

    // 修改系统页表位示图
    synchronized public void modifySysPageTableBitMap(int logicalPageNo) {
        this.sysPageTableUsed[logicalPageNo] = true;
    }

    // 修改交换区位示图
    synchronized public void modifySwapAreaUsageBitMap(int blockNo) {
        this.swapAreaUsageBitmap[blockNo] = true;
    }

    //----------------------存储区判断-----------------------
    // 内存的pcb池是否已满
    public boolean isPCBPoolZoneHasEmpty() {
        return Memory.memory.isPCBPoolFull();
    }

    // 磁盘的交换区是否有足够空间
    public boolean isSwapAreaEnough(int pageNum) {
        return getFreePageNumsInSwapArea() >= pageNum;
    }

    //----------------------内存空间处理---------------------
    // 给pcb分配页表
    public void distributedPCBPageTable(PCB pcb) {
        int count = 0, base;
        int pagesNum = pcb.getPageNums();
        for (base = 0; base < sysPageTableUsed.length - pagesNum; base++) {
            count = 0;
            if (!sysPageTableUsed[base]) {
                for (int j = 0; j < pagesNum; j++) {
                    if (!sysPageTableUsed[base + j])
                        count++;
                }
                if (count == pagesNum)
                    break;
                else
                    base += pagesNum - 1;
            }
        }
        pcb.setPageTableBaseAddr(base * Memory.PAGE_TABLE_ENTRY_SIZE);
        for (int j = 0; j < count; j++) {
            modifySysPageTableBitMap(base+j);
        }
    }

    // 获取内存空闲页数
    public int getFreePageNumInMemory() {
        int count = 0;
        for (boolean b : memoryUsageBitmap) {
            if (!b) {
                count++;
            }
        }
        return count;
    }

    // 内存PCB池中申请空闲页
    public int applyPageFromPCBPool() {
        return Memory.memory.getPcbPoolZone().allocNewPos();
    }

    //----------------------磁盘空间处理-----------------------
    // 获取磁盘交换区内空闲的盘块数
    public int getFreePageNumsInSwapArea() {
        int count = 0;
        // 交换区在外存20224-20479块，在所有页面为1984~2112页
        for (boolean b : swapAreaUsageBitmap) {
            if (!b) {
                count++;
            }
        }
        return count;
    }

    // 为进程分配磁盘交换区可用区，建立外页表
    public void applyVirtualMemory(PCB pcb) {
        int needPageFrameNum = pcb.getPageNums();
        int count = 0;
        for (int i = 0; i < swapAreaUsageBitmap.length; i++) {

        }
    }

    // 处理缺页中断
    public void doPageFault(PCB pcb, int virtualPageNo) {
        System.out.println("[PAGE FAULT] 开始处理缺页中断");
        // cpu 进行保护现场
        CPU.cpu.Protect();
        pcb.setStatus(PCB.TASK_SUSPEND);

    }

    // 访问内存
    public int visitMemory(int physicalAddr) {
        return Memory.memory.readData((short) physicalAddr);
    }

}
