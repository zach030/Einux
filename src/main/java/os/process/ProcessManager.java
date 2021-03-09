package os.process;

import hardware.memory.Memory;
import hardware.memory.Page;
import os.filesystem.Block;
import os.filesystem.FileSystem;
import os.job.JCB;
import os.storage.StorageManager;
import os.storage.Transfer;
import utils.Log;

import java.util.ArrayList;

// 进程管理:三级调度，PCB的状态切换，死锁检测
public class ProcessManager {
    public static ProcessManager pm = new ProcessManager();

    //-------------常量定义------------------------
    static final int PCB_MAX_NUM = 13;   //PCB最大数量，与内存中PCB池的大小一样，一个pcb信息占一个页的大小

    //---------------成员----------------------------
    // 进程队列管理
    public QueueManager queueManager;
    // 请求管理
    public RequesterManager requesterManager;
    // 进程操作
    public ProcessOperator processOperator;

    public ProcessManager() {
        queueManager = new QueueManager();
        requesterManager = new RequesterManager();
        processOperator = new ProcessOperator();
    }

    static public class QueueManager {
        QueueManager() {
            this.allPCB = new ArrayList<>();
            this.readyQueue = new ArrayList<>();
            this.blockQueue = new ArrayList<>();
            this.suspendQueue = new ArrayList<>();
            this.finishQueue = new ArrayList<>();
            this.bufferBlockQueue = new ArrayList<>();
            this.resourceBlockQueue = new ArrayList<>();
            this.DMABlockQueue = new ArrayList<>();
        }

        //----------进程调度的PCB队列-------------------
        ArrayList<PCB> allPCB;                      //全部的PCB
        ArrayList<PCB> readyQueue;                  //pcb就绪队列
        ArrayList<PCB> blockQueue;                  //pcb阻塞队列
        ArrayList<PCB> suspendQueue;                //pcb挂起队列
        ArrayList<PCB> finishQueue;                 //pcb完成队列
        ArrayList<ArrayList<PCB>> bufferBlockQueue; //设备缓冲区阻塞队列
        ArrayList<ArrayList<PCB>> resourceBlockQueue;//资源阻塞队列
        ArrayList<PCB> DMABlockQueue;               //DMA阻塞队列

        //--------------展示进程队列信息----------------------
        public synchronized void DisplayAllPCBQueue() {
            Log.Info("进程队列信息", "当前就绪队列:");
            displayReadyQueue();
            Log.Info("进程队列信息", "当前阻塞队列:");
            displayBlockQueue();
            Log.Info("进程队列信息", "当前挂起队列:");
            displaySuspendQueue();
            Log.Info("进程队列信息", "当前完成队列:");
            displayFinishQueue();
        }

        synchronized void displayReadyQueue() {
            if (!this.readyQueue.isEmpty()) {
                for (PCB pcb : readyQueue) {
                    System.out.print("进程：" + pcb.getID() + "\t");
                }
            } else {
                System.out.print("就绪队列为空");
            }
            System.out.println();
        }

        synchronized void displayBlockQueue() {
            if (!this.blockQueue.isEmpty()) {
                for (PCB pcb : blockQueue) {
                    System.out.print("进程：" + pcb.getID() + "\t");
                }
            } else {
                System.out.print("阻塞队列为空");
            }
            System.out.println();
        }

        synchronized void displaySuspendQueue() {
            if (!this.suspendQueue.isEmpty()) {
                for (PCB pcb : suspendQueue) {
                    System.out.print("进程：" + pcb.getID() + "\t");
                }
            } else {
                System.out.print("挂起队列为空");
            }
            System.out.println();
        }

        synchronized void displayFinishQueue() {
            if (!this.finishQueue.isEmpty()) {
                for (PCB pcb : finishQueue) {
                    System.out.print("进程：" + pcb.getID() + "\t");
                }
            } else {
                System.out.print("已完成队列为空");
            }
            System.out.println();
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

        // 加入资源阻塞队列
        synchronized public void joinResourceBlockQueue(PCB pcb, int resource) {
            this.resourceBlockQueue.get(resource).add(pcb);
        }

        // 加入缓冲区阻塞队列
        synchronized public void joinBufferBlockQueue(PCB pcb, int devNo) {
            this.bufferBlockQueue.get(devNo).add(pcb);
        }

        // 获取就绪队列队头
        synchronized public PCB getFromReadyQueue() {
            PCB pcb = this.readyQueue.get(0);
            this.readyQueue.remove(pcb);
            return pcb;
        }

        // 获取挂起队列对头
        synchronized public PCB getFromSuspendQueue() {
            PCB pcb = this.suspendQueue.get(0);
            this.suspendQueue.remove(pcb);
            return pcb;
        }

        // 获取阻塞队列队头
        synchronized public PCB getFromBlockQueue() {
            PCB pcb = this.blockQueue.get(0);
            this.blockQueue.remove(pcb);
            return pcb;
        }

        // 移除被撤销的PCB
        synchronized public void removeFromAllQueue(PCB pcb) {
            this.readyQueue.remove(pcb);
            this.suspendQueue.remove(pcb);
            this.blockQueue.remove(pcb);
        }
    }

    public class RequesterManager {
        //---------------进程调度判断操作------------------
        // 判断当前pcb是否已达上限
        public boolean isPCBPoolFull() {
            return queueManager.allPCB.size() >= PCB_MAX_NUM;
        }

        // 判断就绪队列是否已空
        public boolean isReadyQueueEmpty() {
            return queueManager.readyQueue.isEmpty();
        }

        // 判断阻塞队列是否已空
        public boolean isBlockQueueEmpty() {
            return queueManager.blockQueue.isEmpty();
        }

        // 判断挂起队列是否已空
        public boolean isSuspendQueueEmpty() {
            return queueManager.suspendQueue.isEmpty();
        }

        // 判断进程调度是否已结束
        public boolean isAllFinished() {
            return queueManager.allPCB.size() == queueManager.finishQueue.size() && isReadyQueueEmpty() && isBlockQueueEmpty() && isSuspendQueueEmpty();
        }
    }

    public class ProcessOperator {
        //---------------进程原语------------------------
        // 创建进程
        public PCB createPCB(JCB jcb) {
            PCB pcb = new PCB();
            pcb.createProcess(jcb);
            return pcb;
        }

        // 撤销进程
        public void cancelPCB(PCB pcb) {
            pcb.cancelProcess();
            // 将pcb从所有的队列中移除
            queueManager.removeFromAllQueue(pcb);
            // 加入完成队列
            queueManager.finishQueue.add(pcb);
        }

        // 唤醒进程
        public void wakePCB(PCB pcb) {
            pcb.wakeUpProcess();
            queueManager.removeFromAllQueue(pcb);
            queueManager.joinReadQueue(pcb);
        }

        // 阻塞进程
        public void blockPCB(PCB pcb) {
            // 进程阻塞原语
            pcb.blockProcess();
            // 加入阻塞队列
            queueManager.blockQueue.add(pcb);
        }

        // 申请资源阻塞进程
        public void blockPCB(PCB pcb, int resource) {
            pcb.blockProcess();
            queueManager.joinResourceBlockQueue(pcb, resource);
        }

        // 申请缓冲区阻塞进程
        public void blockPCB(PCB pcb, int devNo, int bufferNo) {
            // 阻塞进程原语
            pcb.blockProcess();
            // 设置进程阻塞等待的缓冲区号
            pcb.setBufferNo(bufferNo);
            // 加入缓冲区阻塞队列
            queueManager.joinBufferBlockQueue(pcb, devNo);
        }

        // 将pcb加入内存的pcb池
        public void addPCBToPCBPool(PCB pcb) {
            //1、向内存的pcb池请求分配页
            StorageManager.sm.allotManager.allotEmptyPagePCBPool(pcb);
            //2、将pcb的数据写入该页
            pcb.writePCBPage();
        }

        // 将pcb的数据从磁盘调回内存
        public void returnPCBToMemory(PCB pcb) {
            // 0. 根据当前执行的指令所在页面获取页号
            int pageNo = pcb.getCurrentPageNumOfIR();
            // 1. 请求内存分配空闲页框
            int pageFrame = StorageManager.sm.allotManager.allotEmptyPCBDataPage();
            // 2. 查询页表找到pcb存放在外存数据的起始块号
            int swapBlockNo = pcb.searchPageTable(pageNo);
            // 3. 获取此块数据
            Block block = FileSystem.fs.getBlockInDisk(swapBlockNo);
            // 4. 重写页表
            Page page = Transfer.transfer.transferBlockToPage(block, pageNo, pageFrame);
            pcb.writePageTableEntry(pageNo, page);
            // 5. 写回内存
            Memory.memory.writePage(page);
        }

        public void turnPCBToDisk(PCB pcb) {

        }

        public PCB chooseNotUsedPCB() {
            return null;
        }

        // 由读出的数据构成指令结构体
        public Instruction getInstructionByData(int data) {
            Instruction instruction = new Instruction();
            // 32bit: id 8位，type 3位，arg 5位，data 16位
            instruction.setId(data >> 24 & 0X000000FF);
            instruction.setType(data >> 21 & 0X00000007);
            instruction.setArg(data >> 16 & 0X0000001F);
            instruction.setData((short) ((short) data & 0X0000FFFF));
            return instruction;
        }

    }

}
