package os.storage;

import hardware.CPU;
import hardware.mm.Memory;
import os.filesystem.FileSystem;
import os.process.PCB;
import utils.SysConst;

public class StorageManage {
    public static StorageManage sm = new StorageManage();
    // 系统页表使用情况
    boolean[] sysPageTableUsed = new boolean[SysConst.PAGE_FRAME_SIZE * Memory.PAGE_TABLE_SIZE / Memory.PAGE_TABLE_ENTRY_SIZE];
    // 内存使用情况位示图
    boolean[] memoryUsageBitmap = new boolean[SysConst.PAGE_NUM];
    // 交换区使用情况位示图
    boolean[] swapAreaUsageBitmap = new boolean[FileSystem.SWAP_BLOCK_RANGE[1] - FileSystem.SWAP_BLOCK_RANGE[0] + 1];

    public boolean isPCBPoolZoneHasEmpty() {
        return Memory.memory.isPCBPoolFull();
    }

    // 获取磁盘交换区内空闲的盘块数
    public int getFreePageNumsInSwapArea() {
        int count = 0;
        // 交换区在外存20224-20479块，在所有页面为1984~2112页
        for (int i = 0; i < swapAreaUsageBitmap.length; i++) {
            if (!swapAreaUsageBitmap[i]) {
                count++;
            }
        }
        return count;
    }

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
            sysPageTableUsed[base + j] = true;
        }
    }

    // 为进程分配磁盘交换区可用区，建立外页表
    public void applyVirtualMemory(PCB pcb) {
        int needPageFrameNum = pcb.getPageNums();
        int count = 0;
        for (int i = 0; i < swapAreaUsageBitmap.length; i++) {

        }
    }

    public int getFreePageNumInMemory(){
        int count = 0;
        for (int i = 0; i < memoryUsageBitmap.length; i++) {
            if(!memoryUsageBitmap[i]){
                count++;
            }
        }
        return count;
    }

    public boolean isSwapAreaEnough(int pageNum) {
        return getFreePageNumsInSwapArea() >= pageNum;
    }

    // 内存PCB池中申请空闲页
    public int applyPageFromPCBPool() {
        return Memory.memory.getPcbPoolZone().allocNewPos();
    }

    // 修改内存页框位示图
    public void modifyMemoryPageBitMap(int pageFrameNo) {
        this.memoryUsageBitmap[pageFrameNo] = true;
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
