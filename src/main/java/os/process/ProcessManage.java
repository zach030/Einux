package os.process;

import disk.DevConfig;
import os.filesystem.FileSystem;
import os.job.JCB;
import os.storage.StorageManage;

import java.util.ArrayList;

// 进程管理:三级调度，PCB的状态切换，死锁检测
public class ProcessManage {
    public static ProcessManage pm = new ProcessManage();

    //----------进程调度的PCB队列-------------------
    ArrayList<PCB> allPCB;         //全部的PCB
    ArrayList<PCB> readyQueue;     //pcb就绪队列
    ArrayList<PCB> blockQueue;     //pcb阻塞队列
    ArrayList<PCB> suspendQueue;   //pcb挂起队列
    ArrayList<PCB> finishQueue;    //pcb完成队列

    static final int PCB_MAX_NUM = 13;   //PCB最大数量，与内存中PCB池的大小一样，一个pcb信息占一个页的大小

    public ProcessManage() {
        this.allPCB = new ArrayList<>();
        this.readyQueue = new ArrayList<>();
        this.blockQueue = new ArrayList<>();
        this.suspendQueue = new ArrayList<>();
        this.finishQueue = new ArrayList<>();
    }

    //---------------进程调度判断操作------------------
    // 判断当前pcb是否已达上限
    public boolean isPCBPoolFull() {
        return allPCB.size() >= PCB_MAX_NUM;
    }

    // 判断就绪队列是否已空
    public boolean isReadyQueueEmpty() {
        return this.readyQueue.isEmpty();
    }

    // 判断阻塞队列是否已空
    public boolean isBlockQueueEmpty() {
        return this.blockQueue.isEmpty();
    }

    // 判断挂起队列是否已空
    public boolean isSuspendQueueEmpty() {
        return this.suspendQueue.isEmpty();
    }

    // 判断进程调度是否已结束
    public boolean isAllFinished() {
        return allPCB.size() == finishQueue.size() && isReadyQueueEmpty() && isBlockQueueEmpty() && isSuspendQueueEmpty();
    }

    //---------------进程原语------------------------
    // 创建进程
    public PCB createPCB(JCB jcb) {
        PCB pcb = new PCB();
        pcb.createProcess(jcb);
        return pcb;
    }

    // 撤销进程
    public void cancelPCB(PCB pcb) {

    }

    //-----------------------------内存操作-----------------------
    // 将pcb加入内存的pcb池
    public void addPCBToPCBPool(PCB pcb) {
        //1、向内存的pcb池请求分配页
        int pageFrame = StorageManage.sm.applyPageFromPCBPool();
        //2、设置pcb在内存中的页号
        pcb.setPcbFramePageNo(pageFrame);
        //2、将pcb的数据写入该页
        pcb.writePCBPage();
        //3、修改内存中页框位示图
        StorageManage.sm.modifyMemoryPageBitMap(pageFrame);
    }

    //----------------------------磁盘操作--------------------
    public void writePCBExternalPageTableToDisk(PCB pcb) {
        // 0=>进程号 2=>逻辑页号 4=>物理块号 一个页表项6个字节
//        int startAddress = FileSystem.EXTERNAL_PAGE_TABLE_INDEX * DevConfig.BLOCK_SIZE;
//        int i = startAddress;
//        for (; i < startAddress + FileSystem.EXTERNAL_PAGE_TABLE_SIZE; i += 6) {
//            if (ExternalMemory.mem.ReadData(i) == 0) {
//                int count = 0, k = i;
//                for (int j = 0; j < pcb.GetPageFramesNum(); j++, k += 6) {
//                    if (ExternalMemory.mem.ReadData(k) == 0)
//                        count++;
//                }
//                if (count == pcb.GetPageFramesNum())
//                    break;
//                i = k;
//            }
//        }
//        pcb.SetExternalPageTableBaseAddress(i);
//        for (int j = 0; j < pcb.GetPageFramesNum(); j++, i += 6) {
//            ExternalMemory.mem.WriteData(i, pcb.GetProID());
//            ExternalMemory.mem.WriteData(i + 2, (short) externalPageTable[j, 0]);
//            ExternalMemory.mem.WriteData(i + 4, (short) externalPageTable[j, 1]);
//        }
    }

    //--------------展示进程队列信息----------------------
    public synchronized void DisplayAllPCBQueue() {
        System.out.println("[INFO]----当前就绪队列：");
        displayReadyQueue();
        System.out.println("[INFO]----当前阻塞队列：");
        displayBlockQueue();
        System.out.println("[INFO]----当前挂起队列：");
        displaySuspendQueue();
        System.out.println("[INFO]----当前完成队列：");
        displayFinishQueue();
    }

    synchronized void displayReadyQueue() {
        if (!this.readyQueue.isEmpty()) {
            for (PCB pcb : readyQueue) {
                System.out.print("进程：" + pcb.getID() + "\t");
            }
        } else {
            System.out.println("就绪队列为空");
        }
    }

    synchronized void displayBlockQueue() {
        if (!this.blockQueue.isEmpty()) {
            for (PCB pcb : blockQueue) {
                System.out.print("进程：" + pcb.getID() + "\t");
            }
        } else {
            System.out.println("阻塞队列为空");
        }
    }

    synchronized void displaySuspendQueue() {
        if (!this.suspendQueue.isEmpty()) {
            for (PCB pcb : suspendQueue) {
                System.out.print("进程：" + pcb.getID() + "\t");
            }
        } else {
            System.out.println("挂起队列为空");
        }
    }

    synchronized void displayFinishQueue() {
        if (!this.finishQueue.isEmpty()) {
            for (PCB pcb : finishQueue) {
                System.out.print("进程：" + pcb.getID() + "\t");
            }
        } else {
            System.out.println("已完成队列为空");
        }
    }

    //-----------------------进程调度队列基本操作--------------
    // 加入就绪队列
    synchronized public void joinReadQueue(PCB pcb) {
        this.readyQueue.add(pcb);
    }

    // 加入挂起队列
    synchronized public void joinSuspendQueue(PCB pcb) {
        this.suspendQueue.add(pcb);
    }

    // 加入阻塞队列
    synchronized public void joinBlockQueue(PCB pcb) {
        this.blockQueue.add(pcb);
    }

    // 加入已完成队列
    synchronized public void joinFinishedQueue(PCB pcb) {
        this.finishQueue.add(pcb);
    }

    // 获取就绪队列队头
    synchronized public PCB getFromReadyQueue() {
        return this.readyQueue.get(0);
    }

    // 获取挂起队列对头
    synchronized public PCB getFromSuspendQueue() {
        return this.suspendQueue.get(0);
    }

    // 获取阻塞队列队头
    synchronized public PCB getFromBlockQueue() {
        return this.blockQueue.get(0);
    }

    // 获取一个不适用的PCB
    synchronized public PCB getNotUsedPCB() {
        return null;
    }

}
