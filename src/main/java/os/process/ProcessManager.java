package os.process;

import hardware.CPU;
import hardware.memory.Memory;
import hardware.memory.Page;
import hardware.disk.Block;
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
            this.bufferBlockQueue.add(new ArrayList<>());
            this.resourceBlockQueue = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                this.resourceBlockQueue.add(new ArrayList<>());
            }
        }

        //----------进程调度的PCB队列-------------------
        ArrayList<PCB> allPCB;                      //全部的PCB
        ArrayList<PCB> readyQueue;                  //pcb就绪队列
        ArrayList<PCB> blockQueue;                  //pcb阻塞队列
        ArrayList<PCB> suspendQueue;                //pcb挂起队列
        ArrayList<PCB> finishQueue;                 //pcb完成队列
        //todo 展示下面两个阻塞队列信息
        // 系统调用被阻塞： 目前是文件操作后被阻塞，操作成功后即唤醒
        // 申请资源被阻塞
        // 申请缓冲区被阻塞
        ArrayList<ArrayList<PCB>> bufferBlockQueue; //设备缓冲区阻塞队列
        ArrayList<ArrayList<PCB>> resourceBlockQueue;//资源阻塞队列

        //--------------展示进程队列信息----------------------

        synchronized public String displayReadyQueue() {
            StringBuilder content = new StringBuilder();
            if (!this.readyQueue.isEmpty()) {
                for (PCB pcb : readyQueue) {
                    content.append("进程: ").append(pcb.getID()).append("\n");
                }
                Log.Info("就绪队列", content.toString());
            } else {
                content.append("就绪队列为空");
            }
            return content.toString();
        }

        synchronized public String displayResourceBlockQueue() {
            StringBuilder content = new StringBuilder();
            if (!this.resourceBlockQueue.isEmpty()) {
                ArrayList<PCB> r1 = resourceBlockQueue.get(0);
                content.append("资源类型0：");
                for (PCB pcb : r1) {
                    content.append("进程: ").append(pcb.getID()).append("\t");
                }
                content.append("\n资源类型1：");
                ArrayList<PCB> r2 = resourceBlockQueue.get(1);
                for (PCB pcb : r2) {
                    content.append("进程: ").append(pcb.getID()).append("\t");
                }
                content.append("\n资源类型2：");
                ArrayList<PCB> r3 = resourceBlockQueue.get(2);
                for (PCB pcb : r3) {
                    content.append("进程: ").append(pcb.getID()).append("\t");
                }
                Log.Info("资源阻塞队列", content.toString());
            } else {
                content.append("资源阻塞队列为空");
            }
            return content.toString();
        }

        synchronized public String displayBufferBlockQueue() {
            StringBuilder content = new StringBuilder();
            if (!this.bufferBlockQueue.isEmpty()) {
                ArrayList<PCB> current = bufferBlockQueue.get(0);
                for (PCB pcb : current) {
                    content.append("进程: ").append(pcb.getID()).append("\t");
                }
                Log.Info("缓冲区阻塞队列", content.toString());
            } else {
                content.append("缓冲区阻塞队列为空");
            }
            return content.toString();
        }

        synchronized public String displaySuspendQueue() {
            StringBuilder content = new StringBuilder();
            if (!this.suspendQueue.isEmpty()) {
                for (PCB pcb : suspendQueue) {
                    content.append("进程: ").append(pcb.getID()).append("\n");
                }
                Log.Info("挂起队列", content.toString());
            } else {
                content.append("挂起队列为空");
            }
            return content.toString();
        }

        synchronized public String displayFinishQueue() {
            StringBuilder content = new StringBuilder();
            if (!this.finishQueue.isEmpty()) {
                for (PCB pcb : finishQueue) {
                    content.append("进程: ").append(pcb.getID()).append("\n");
                }
                Log.Info("已完成队列", content.toString());
            } else {
                content.append("已完成队列为空");
            }
            return content.toString();
        }

        //-----------------------进程调度队列基本操作--------------
        synchronized public void joinAllQueue(PCB pcb) {
            this.allPCB.add(pcb);
        }

        // 加入就绪队列
        synchronized public void joinReadQueue(PCB pcb) {
            this.readyQueue.add(pcb);
        }

        // 加入挂起队列
        synchronized public void joinSuspendQueue(PCB pcb) {
            Log.Info("挂起进程", String.format("正在将进程:%d，加入系统挂起队列", pcb.getID()));
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

        // 移出资源阻塞队列
        synchronized public boolean removeFromResourceBlockQueue(PCB pcb, int resource) {
            return this.resourceBlockQueue.get(resource).remove(pcb);
        }

        // 加入缓冲区阻塞队列
        synchronized public void joinBufferBlockQueue(PCB pcb, int devNo) {
            this.bufferBlockQueue.get(devNo).add(pcb);
        }

        // 移除缓冲区阻塞队列
        synchronized public void removeFromBufferBlockQueue(PCB pcb, int devNo) {
            this.bufferBlockQueue.get(devNo).remove(pcb);
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

        synchronized public void removeFromBlockQueue(PCB pcb) {
            // 资源阻塞队列
            for (int i = 0; i < 3; i++) {
                this.removeFromResourceBlockQueue(pcb, i);
            }
            // 缓冲区阻塞队列
            this.removeFromBufferBlockQueue(pcb, FileSystem.getCurrentBootDisk().getBootDiskNo());
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
            ProcessManager.pm.queueManager.joinAllQueue(pcb);
            return pcb;
        }

        // 撤销进程
        public void cancelPCB(PCB pcb) {
            pcb.cancelProcess();
            // 将pcb从所有的队列中移除
            queueManager.removeFromAllQueue(pcb);
            // 加入完成队列
            queueManager.joinFinishedQueue(pcb);
        }

        // 唤醒进程
        public void wakePCB(PCB pcb) {
            pcb.wakeUpProcess();
            queueManager.removeFromBlockQueue(pcb);
            queueManager.joinReadQueue(pcb);
        }

        // 申请资源阻塞进程
        public void blockPCB(PCB pcb, int resource) {
            Log.Info("申请资源阻塞", String.format("进程id:%d,因申请资源:%d，被阻塞", pcb.getID(), resource));
            // 设置进程的状态为阻塞
            pcb.blockProcess();
            // cpu保护现场
            CPU.cpu.Protect();
            // 加入阻塞队列
            queueManager.joinResourceBlockQueue(pcb, resource);
        }

        // 申请缓冲区阻塞进程
        public void blockPCB(PCB pcb, int devNo, int bufferNo) {
            Log.Info("申请磁盘缓冲区阻塞", String.format("进程id:%d,设备号:%d,因申请缓冲区头部:%d，被阻塞", pcb.getID(), devNo, bufferNo));
            // 阻塞进程原语
            pcb.blockProcess();
            // 设置进程阻塞等待的缓冲区号
            pcb.setBufferNo(bufferNo);
            // cpu保护现场
            CPU.cpu.Protect();
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
            Block block = FileSystem.getCurrentBootDisk().getBlockInDisk(swapBlockNo);
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
