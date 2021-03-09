package os.storage;

import hardware.CPU;
import hardware.memory.Page;
import hardware.memory.Memory;
import os.filesystem.Block;
import os.filesystem.FileSystem;
import os.job.JCB;
import os.process.PCB;
import os.process.ProcessManager;
import utils.Log;
import utils.SysConst;

import java.util.*;

public class StorageManager {
    public static StorageManager sm = new StorageManager();
    //----------------常量-------------------------
    // 空间不足
    public static final int NOT_ENOUGH = -1;
    public static final String allot = "分配内存页";

    //---------------成员--------------------------
    // 位示图管理
    public BitMapManager bitMapManager;
    // 请求管理(处理判断空间是否足够，获取空闲数等等操作)
    public RequesterManager requesterManager;
    // 资源分配管理(写数据)
    public AllotManager allotManager;
    // 资源释放管理(进程结束，释放数据)
    public ReleaseManager releaseManager;
    // 内存读写
    public MemoryManager memoryManager;
    // 磁盘读写
    public DiskManager diskManager;

    public StorageManager() {
        bitMapManager = new BitMapManager();
        requesterManager = new RequesterManager();
        allotManager = new AllotManager();
        releaseManager = new ReleaseManager();
        memoryManager = new MemoryManager();
        diskManager = new DiskManager();
    }

    public static class BitMapManager {
        BitMapManager() {
            Arrays.fill(sysPageTableBitMap, false);
            Arrays.fill(memoryAllPageBitmap, false);
            Arrays.fill(memoryPCBPoolBitMap, false);
            Arrays.fill(memoryPCBDataBitMap, false);
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
        // 内存pcb数据区使用位示图
        boolean[] memoryPCBDataBitMap = new boolean[Memory.PCB_ZONE_SIZE];
        // 内存缓冲区位示图(16页)
        boolean[] memoryBufferBitMap = new boolean[Memory.BUFFER_SIZE];
        // 交换区使用情况位示图(256块)
        boolean[] swapZoneBitmap = new boolean[FileSystem.SWAP_ZONE_SIZE];
        // 磁盘JCB区使用位示图(256块)
        boolean[] jcbZoneBitMap = new boolean[FileSystem.JCB_ZONE_SIZE];

        // 管理位示图
        //----------------------修改位示图---------------
        // 修改系统页表位示图
        synchronized public void modifySysPageTableBitMap(int logicalPageNo, boolean status) {
            this.sysPageTableBitMap[logicalPageNo] = status;
        }

        // 修改内存页框位示图
        synchronized public void modifyMemoryPageBitMap(int pageFrameNo, boolean status) {
            this.memoryAllPageBitmap[pageFrameNo] = status;
        }

        // 修改内存pcb池位示图
        synchronized public void modifyPCBPoolAreaBitMap(int pageNo, boolean status) {
            this.memoryPCBPoolBitMap[pageNo] = true;
            this.modifyMemoryPageBitMap(pageNo + Memory.PCB_POOL_START, status);
        }

        // 修改内存pcb池位示图
        synchronized public void modifyPCBDataAreaBitMap(int pageNo, boolean status) {
            this.memoryPCBDataBitMap[pageNo] = true;
            this.modifyMemoryPageBitMap(pageNo + Memory.PCB_ZONE_START, status);
        }

        // 修改内存缓冲区位示图
        synchronized public void modifyBufferAreaBitMap(int pageNo, boolean status) {
            this.memoryBufferBitMap[pageNo] = status;
            this.modifyMemoryPageBitMap(pageNo + Memory.BUFFER_START, status);
        }

        // 修改磁盘交换区位示图
        synchronized public void modifySwapAreaBitMap(int blockNo, boolean status) {
            this.swapZoneBitmap[blockNo] = status;
        }

        // 修改JCB区位示图
        synchronized public void modifyJCBAreaBitMap(int blockNo, boolean status) {
            this.jcbZoneBitMap[blockNo] = status;
        }
    }

    public class AllotManager {

        class PageGroup {
            int startFrameNo;
            int endFrameNo;
            int usedFrameNum;
            boolean free;

            PageGroup(int startFrameNo, int endFrameNo) {
                this.startFrameNo = startFrameNo;
                this.endFrameNo = endFrameNo;
                this.usedFrameNum = 0;
                this.free = true;
            }

            public int getStartFrameNo() {
                return startFrameNo;
            }

            public void setStartFrameNo(int startFrameNo) {
                this.startFrameNo = startFrameNo;
            }

            public int getEndFrameNo() {
                return endFrameNo;
            }

            public void setEndFrameNo(int endFrameNo) {
                this.endFrameNo = endFrameNo;
            }

            public int getUsedFrameNum() {
                return usedFrameNum;
            }

            public void setUsedFrameNum(int usedFrameNum) {
                this.usedFrameNum = usedFrameNum;
            }

            public boolean isFree() {
                return free;
            }

            public void setFree(boolean free) {
                this.free = free;
            }
        }

        List<List<PageGroup>> freeGroups = new LinkedList<List<PageGroup>>();

        AllotManager(){
            initFreeGroupList();
        }

        // 初始化空闲链表
        private void initFreeGroupList(){

        }
        // 给pcb分配页表bit
        public void allotPCBPageTable(PCB pcb) {
            int count = 0, base;
            int pagesNum = pcb.getPageNums();
            for (base = 0; base < bitMapManager.sysPageTableBitMap.length - pagesNum; base++) {
                count = 0;
                if (!bitMapManager.sysPageTableBitMap[base]) {
                    // 页表区找到连续的page num大小的区域存放进程的页表
                    for (int j = 0; j < pagesNum; j++) {
                        if (!bitMapManager.sysPageTableBitMap[base + j])
                            count++;
                    }
                    if (count == pagesNum)
                        break;
                    else
                        base += pagesNum - 1;
                }
            }
            pcb.setInternPageTableBaseAddr((short) (base * Memory.PAGE_TABLE_ENTRY_SIZE + Memory.PAGE_TABLE_START * SysConst.PAGE_FRAME_SIZE));
            for (int j = 0; j < count; j++) {
                bitMapManager.modifySysPageTableBitMap(base + j, true);
            }
        }

        // 内存PCB池中申请空闲页bit
        public void allotEmptyPagePCBPool(PCB pcb) {
            for (int i = 0; i < bitMapManager.memoryPCBPoolBitMap.length; i++) {
                if (!bitMapManager.memoryPCBPoolBitMap[i]) {
                    bitMapManager.modifyPCBPoolAreaBitMap(i, true);
                    pcb.setPcbFramePageNo(i + Memory.PCB_POOL_START);
                    return;
                }
            }
        }

        // 内存PCB数据区申请页bit
        public int allotEmptyPCBDataPage() {
            for (int i = 0; i < bitMapManager.memoryPCBDataBitMap.length; i++) {
                if (!bitMapManager.memoryPCBDataBitMap[i]) {
                    bitMapManager.modifyPCBDataAreaBitMap(i, true);
                    return i + Memory.PCB_ZONE_START;
                }
            }
            return NOT_ENOUGH;
        }

        // 从磁盘JCB区分配出空闲的JCB块bit
        public Block allotEmptyJCBBlock() {
            int blockNo = 0;
            for (int i = 0; i < bitMapManager.jcbZoneBitMap.length; i++) {
                if (!bitMapManager.jcbZoneBitMap[i]) {
                    blockNo = i;
                    bitMapManager.modifyJCBAreaBitMap(i, true);
                    break;
                }
            }
            return new Block(blockNo + FileSystem.JCB_ZONE_INDEX);
        }

        // 从磁盘交换区分配出空闲的交换块bit
        public Block allotEmptySwapBlock() {
            int blockNo = 0;
            for (int i = 0; i < bitMapManager.swapZoneBitmap.length; i++) {
                if (!bitMapManager.swapZoneBitmap[i]) {
                    blockNo = i;
                    bitMapManager.modifySwapAreaBitMap(i, true);
                    break;
                }
            }
            return new Block(blockNo + FileSystem.SWAP_ZONE_INDEX);
        }

    }

    public class ReleaseManager {
        // 将内存占用的内存数据区释放
        public void releasePCBData(PCB pcb) {
            syncFreeMemoryPCBData();
            syncFreePCBDataBitMap();
        }

        // 同步清除进程内存数据区
        private void syncFreeMemoryPCBData() {

        }

        // 同步清除进程内存数据区位示图
        private void syncFreePCBDataBitMap() {

        }

        // 将pcb占用的页表释放(最后清除)
        public void releasePCBPageTable(PCB pcb) {
            // 释放内存和修改内存位示图
            // pcb的页表基址是物理地址，需要转为逻辑地址
            syncFreeMemoryPageTable(pcb.getInternPageTableBaseAddr(), pcb.getPageNums());
            int pcbPageTableLogicalBase = (pcb.getInternPageTableBaseAddr() - Memory.PAGE_TABLE_START * SysConst.PAGE_FRAME_SIZE) / Memory.PAGE_TABLE_ENTRY_SIZE;
            syncFreePageTableBitMap(pcbPageTableLogicalBase, pcb.getPageNums());
        }

        // 同步清除页表所占的物理内存
        private void syncFreeMemoryPageTable(int base, int pageNums) {
            //物理起始地址为base，页表项为pageNums
            for (int pageNo = 0; pageNo < pageNums; pageNo++) {
                Memory.memory.clearData((short) (base + pageNo * Memory.PAGE_TABLE_ENTRY_SIZE));
            }
        }

        // 同步清除页表所占的内存pcb池位示图
        private void syncFreePageTableBitMap(int base, int num) {
            for (int i = 0; i < num; i++) {
                bitMapManager.modifySysPageTableBitMap(i + base, false);
            }
        }

        // 将pcb占用的pcb池页释放
        public void releasePCBPoolPage(PCB pcb) {
            pcb.freePCBPage();
            bitMapManager.modifyPCBPoolAreaBitMap(pcb.getPcbFramePageNo() - Memory.PCB_POOL_START, false);
        }

        // 释放jcb在磁盘jcb区的资源
        public void releaseJCBData() {
            //todo implement
        }

    }

    public class RequesterManager {
        // 内存的pcb池是否有空闲
        public boolean isPCBPoolZoneHasEmpty() {
            for (boolean b : bitMapManager.memoryPCBPoolBitMap) {
                if (!b) {
                    return true;
                }
            }
            return false;
        }

        // 获取内存空闲页数
        public int getFreePageNumInMemory() {
            int count = 0;
            for (boolean b : bitMapManager.memoryAllPageBitmap) {
                if (!b) {
                    count++;
                }
            }
            return count;
        }

        // 获取磁盘交换区内空闲的盘块数
        public int getFreePageNumsInSwapArea() {
            int count = 0;
            // 交换区在外存20224-20479块，在所有页面为1984~2112页
            for (boolean b : bitMapManager.swapZoneBitmap) {
                if (!b) {
                    count++;
                }
            }
            return count;
        }

        // 磁盘的交换区是否有足够空间
        public boolean isSwapAreaEnough(int pageNum) {
            return getFreePageNumsInSwapArea() >= pageNum;
        }
    }

    public class MemoryManager {
        public LRUCache lruCache;

        MemoryManager() {
            lruCache = new LRUCache();
        }

        class LRUCache {
            private int capacity;
            private HashMap<Integer, ListNode> hashmap;
            private ListNode head;
            private ListNode tail;

            private class ListNode {
                int key;
                Page val;
                ListNode prev;
                ListNode next;

                public ListNode() {
                }

                public ListNode(int key, Page val) {
                    this.key = key;
                    this.val = val;
                }
            }

            public LRUCache() {
                this.capacity = Memory.PCB_ZONE_SIZE;
                hashmap = new HashMap<>();
                head = new ListNode();
                tail = new ListNode();
                head.next = tail;
                tail.prev = head;
            }

            private void removeNode(ListNode node) {
                node.prev.next = node.next;
                node.next.prev = node.prev;
            }

            private void addNodeToLast(ListNode node) {
                node.prev = tail.prev;
                node.prev.next = node;
                node.next = tail;
                tail.prev = node;
            }

            private void moveNodeToLast(ListNode node) {
                removeNode(node);
                addNodeToLast(node);
            }

            public Page getPage(int key) {
                if (hashmap.containsKey(key)) {
                    ListNode node = hashmap.get(key);
                    moveNodeToLast(node);
                    return node.val;
                } else {
                    return null;
                }
            }

            public void visitPage(int key, Page page) {
                if (hashmap.containsKey(key)) {
                    ListNode node = hashmap.get(key);
                    node.val = page;
                    moveNodeToLast(node);
                    return;
                }
                if (hashmap.size() == capacity) {
                    hashmap.remove(head.next.key);
                    removeNode(head.next);
                }
                ListNode node = new ListNode(key, page);
                hashmap.put(key, node);
                addNodeToLast(node);
            }
        }

        // 读内存
        public int visitMemory(int physicalAddr) {
            int frameNo = (physicalAddr >> 9) & 0X003F;
            lruCache.visitPage(frameNo, Memory.memory.readPage(frameNo));
            return Memory.memory.readWordData((short) physicalAddr);
        }

        // 处理缺页中断
        public void doPageFault(PCB pcb, int virtualPageNo) {
            System.out.println("[PAGE FAULT] 开始处理缺页中断");
            // cpu 进行保护现场
            CPU.cpu.Protect();
            pcb.setStatus(PCB.TASK_SUSPEND);
            ProcessManager.pm.queueManager.joinSuspendQueue(pcb);
            // 查页表得到外存磁盘号，进行调页
            int blockNo = pcb.searchPageTable(virtualPageNo);
            int pageFrameNo = allotManager.allotEmptyPCBDataPage();
            // 查看内存是否有空闲页框，分配一个
            if (pageFrameNo == NOT_ENOUGH) {
                //淘汰页面,如果没有则使用淘汰算法淘汰一个，将淘汰页写回磁盘
            }
            // 将块号blockNo的数据写入pageFrameNo的页内
            // 根据块号查到物理块
            Block block = FileSystem.fs.getBlockInDisk(blockNo);
            //todo 加内存缓冲区处理，不是直接从磁盘写到内存
            // 转化为内存页
            Page page = Transfer.transfer.transferBlockToPage(block, virtualPageNo, pageFrameNo);
            // 将页写入内存指定位置
            Memory.memory.writePage(page);
            // 设置进程页表
            pcb.writePageTableEntry(virtualPageNo, page);
            // pcb就绪态
            pcb.setStatus(PCB.TASK_READY);
            // cpu恢复现场
            CPU.cpu.Recovery(pcb);
            // 设置pcb运行态
            pcb.setStatus(PCB.TASK_RUNNING);
            System.out.println("[PAGE FAULT SUCCESS]------已成功完成请求调页，结束缺页中断...");
        }

        public void writeDiskToBuffer(int blockNo, int logicalNo, int frameNo) {
            // 读出磁盘块
            Block block = FileSystem.fs.getBlockInDisk(blockNo);
            Page page = Transfer.transfer.transferBlockToPage(block, logicalNo, frameNo);
            page.syncPage();
        }
    }

    public class DiskManager {
        // 将作业的全部数据写入交换区
        public void saveToSwapZone(JCB jcb) {
            jcb.saveJobBlockToSwapZone();
        }

        // 向磁盘内写入一块
        public void writeBlockToDisk(Block block) {
            FileSystem.fs.writeBlock(block);
        }

        // 向缓冲区数据写入磁盘
        public void writeBufferToDisk(int blockNo, int frameNo) {
            // 获取内存页
            Page page = Memory.memory.readPage(frameNo);
            // 转换为物理块
            Block block = Transfer.transfer.transferPageToBlock(page, blockNo);
            // 同步修改到磁盘
            block.syncBlock();
        }
    }

}
