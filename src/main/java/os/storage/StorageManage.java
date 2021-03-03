package os.storage;

import hardware.CPU;
import hardware.mm.Memory;
import os.filesystem.Block;
import os.filesystem.FileSystem;
import os.job.JCB;
import os.process.PCB;
import utils.SysConst;

import java.util.Arrays;

public class StorageManage {
    public static StorageManage sm = new StorageManage();

    public StorageManage() {
        Arrays.fill(sysPageTableBitMap, false);
        Arrays.fill(memoryAllPageBitmap, false);
        Arrays.fill(memoryPCBPoolBitMap, false);
        Arrays.fill(memoryBufferBitMap, false);
        Arrays.fill(jcbZoneBitMap, false);
        Arrays.fill(swapZoneBitmap, false);
    }

    //-------------------------存储位示图管理------------------------
    // 内存页框位示图(64页)
    boolean[] memoryAllPageBitmap = new boolean[SysConst.PAGE_NUM];
    // 系统页表使用情况(2页,64个页表项)
    boolean[] sysPageTableBitMap = new boolean[SysConst.PAGE_FRAME_SIZE * Memory.PAGE_TABLE_SIZE / Memory.PAGE_TABLE_ENTRY_SIZE];
    // 内存pcb池使用位示图(13页)
    boolean[] memoryPCBPoolBitMap = new boolean[Memory.PCB_POOL_SIZE];
    // 内存缓冲区位示图(16页)
    boolean[] memoryBufferBitMap = new boolean[Memory.BUFFER_SIZE];
    // 交换区使用情况位示图(256块)
    boolean[] swapZoneBitmap = new boolean[FileSystem.SWAP_ZONE_SIZE];
    // 磁盘JCB区使用位示图(256块)
    boolean[] jcbZoneBitMap = new boolean[FileSystem.JCB_ZONE_SIZE];

    //----------------------修改位示图---------------
    // 修改系统页表位示图
    synchronized public void modifySysPageTableBitMap(int logicalPageNo) {
        this.sysPageTableBitMap[logicalPageNo] = true;
    }

    // 修改内存页框位示图
    synchronized public void modifyMemoryPageBitMap(int pageFrameNo) {
        this.memoryAllPageBitmap[pageFrameNo] = true;
    }

    // 修改内存pcb池位示图
    synchronized public void modifyPCBPoolAreaBitMap(int pageNo) {
        this.memoryPCBPoolBitMap[pageNo] = true;
        this.modifyMemoryPageBitMap(pageNo + Memory.PCB_POOL_START);
    }

    // 修改内存缓冲区位示图
    synchronized public void modifyBufferAreaBitMap(int pageNo) {
        this.memoryBufferBitMap[pageNo] = true;
        this.modifyMemoryPageBitMap(pageNo + Memory.BUFFER_START);
    }

    // 修改交换区位示图
    synchronized public void modifySwapAreaBitMap(int blockNo) {
        this.swapZoneBitmap[blockNo] = true;
    }

    // 修改JCB区位示图
    synchronized public void modifyJCBAreaBitMap(int blockNo) {
        this.jcbZoneBitMap[blockNo] = true;
    }

    //----------------------存储区判断-----------------------
    // 内存的pcb池是否有空闲
    public boolean isPCBPoolZoneHasEmpty() {
        for (boolean b : memoryPCBPoolBitMap) {
            if (b) {
                return true;
            }
        }
        return false;
    }

    // 磁盘的交换区是否有足够空间
    public boolean isSwapAreaEnough(int pageNum) {
        return getFreePageNumsInSwapArea() >= pageNum;
    }

    //----------------------内存请求分配页---------------------
    // 给pcb分配页表
    public void allocPCBPageTable(PCB pcb) {
        int count = 0, base;
        int pagesNum = pcb.getPageNums();
        for (base = 0; base < sysPageTableBitMap.length - pagesNum; base++) {
            count = 0;
            if (!sysPageTableBitMap[base]) {
                // 页表区找到连续的page num大小的区域存放进程的页表
                for (int j = 0; j < pagesNum; j++) {
                    if (!sysPageTableBitMap[base + j])
                        count++;
                }
                if (count == pagesNum)
                    break;
                else
                    base += pagesNum - 1;
            }
        }
        pcb.setInternPageTableBaseAddr(base * Memory.PAGE_TABLE_ENTRY_SIZE);
        for (int j = 0; j < count; j++) {
            modifySysPageTableBitMap(base + j);
        }
    }

    // 内存PCB池中申请空闲页
    public void allocEmptyPagePCBPool(PCB pcb) {
        for (int i = 0; i < memoryPCBPoolBitMap.length; i++) {
            if (memoryPCBPoolBitMap[i]) {
                modifyPCBPoolAreaBitMap(i);
                pcb.setPcbFramePageNo(i + Memory.PCB_POOL_START);
                return;
            }
        }
    }

    //---------------------------获取内存各分区空闲数-------------------
    // 获取内存空闲页数
    public int getFreePageNumInMemory() {
        int count = 0;
        for (boolean b : memoryAllPageBitmap) {
            if (!b) {
                count++;
            }
        }
        return count;
    }

    //----------------------磁盘空间处理-----------------------
    // 获取磁盘交换区内空闲的盘块数
    public int getFreePageNumsInSwapArea() {
        int count = 0;
        // 交换区在外存20224-20479块，在所有页面为1984~2112页
        for (boolean b : swapZoneBitmap) {
            if (!b) {
                count++;
            }
        }
        return count;
    }

    // 将作业的全部数据写入交换区
    public void saveToSwapZone(JCB jcb) {
        jcb.saveJobBlockToSwapZone();
    }

    // 处理缺页中断
    public void doPageFault(PCB pcb, int virtualPageNo) {
        System.out.println("[PAGE FAULT] 开始处理缺页中断");
        // cpu 进行保护现场
        CPU.cpu.Protect();
        pcb.setStatus(PCB.TASK_SUSPEND);

    }

    //-----------------------------从磁盘中请求分配空闲块-------------------
    // 从磁盘JCB区分配出空闲的JCB块
    public Block allocEmptyJCBBlock() {
        int blockNo = 0;
        for (int i = 0; i < jcbZoneBitMap.length; i++) {
            if (!jcbZoneBitMap[i]) {
                blockNo = i;
                modifyJCBAreaBitMap(i);
                break;
            }
        }
        return new Block(blockNo + FileSystem.JCB_ZONE_INDEX);
    }

    // 从磁盘交换区分配出空闲的交换块
    public Block allocEmptySwapBlock() {
        int blockNo = 0;
        for (int i = 0; i < swapZoneBitmap.length; i++) {
            if (!swapZoneBitmap[i]) {
                blockNo = i;
                modifySwapAreaBitMap(i);
                break;
            }
        }
        return new Block(blockNo + FileSystem.SWAP_ZONE_INDEX);
    }

    // 向磁盘内写入一块
    public void writeBlockToDisk(Block block) {
        FileSystem.fs.writeBlock(block);
    }

    //---------------------------内存处理-------------------
    // 访问内存
    public int visitMemory(int physicalAddr) {
        return Memory.memory.readData((short) physicalAddr);
    }

}
