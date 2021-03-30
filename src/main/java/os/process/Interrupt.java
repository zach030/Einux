package os.process;

import hardware.CPU;
import hardware.disk.Block;
import hardware.memory.Memory;
import hardware.memory.Page;
import os.filesystem.FileSystem;
import os.storage.StorageManager;
import os.storage.Transfer;
import os.systemcall.SystemCall;
import utils.Log;

public class Interrupt {
    public static Interrupt interrupt = new Interrupt();
    // 中断向量表
    public static final int PAGE_FAULT = 0;
    public static final int SYSTEM_CALL_OPEN = 1;

    /**
     * @description: 中断处理程序
     * @author: zach
     **/
    public void doInterrupt() {
        int interrupt = CPU.cpu.getInterrupt();
        switch (interrupt) {
            case PAGE_FAULT:
                doPageFault();
                break;
            case SYSTEM_CALL_OPEN:
                doOpenFileSystemCall();
                break;
        }
    }

    /**
     * @description: 缺页中断：进程挂起，查进程页表，查到外存物理块号，内存pcb数据区分配空闲页
     * 将物理块的数据写入内存空闲区，唤醒进程，设置进程状态
     * @author: zach
     **/
    public void doPageFault() {
        //当前运行进程，当前指令逻辑地址
        PCB pcb = CPU.cpu.getCurrent();
        int virtualPageNo = CPU.cpu.getCr2();
        // cpu 进行保护现场
        CPU.cpu.Protect();
        pcb.setStatus(PCB.TASK_SUSPEND);
        // 查页表得到外存磁盘号，进行调页
        int blockNo = pcb.searchPageTable(virtualPageNo);
        int pageFrameNo = StorageManager.sm.allotManager.allotEmptyPCBDataPage();
        // 查看内存是否有空闲页框，分配一个
        if (pageFrameNo == StorageManager.NOT_ENOUGH) {
            int frameNo = StorageManager.sm.memoryManager.lruGetHeadPageNum();
            //淘汰页面,如果没有则使用淘汰算法淘汰一个，将淘汰页写回磁盘
            Log.Info("", String.format("当前内存无空闲页框，经过LRU算法，淘汰最久未使用的页框:%d", frameNo));
            // 此进程将占用此页
            pageFrameNo = frameNo;
        }
        // 将块号blockNo的数据写入pageFrameNo的页内
        // 根据块号查到物理块
        Block block = FileSystem.getCurrentBootDisk().getBlockInDisk(blockNo);
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
        Log.Info("缺页中断", "已成功完成请求调页，结束缺页中断");
    }

    /**
     * @description: 执行打开文件系统调用
     * @author: zach
     **/
    public void doOpenFileSystemCall() {
        PCB pcb = CPU.cpu.getCurrent();
        // cpu 进行保护现场
        CPU.cpu.Protect();
        // 设置挂起
        pcb.setStatus(PCB.TASK_SUSPEND);
        // 系统调用寄存器出栈
        String path = CPU.cpu.getSystemCallReg();
        // 执行系统调用
        int fd = SystemCall.systemCall.fileSystemCall.open(path, SystemCall.WRITE_ONLY);
        // pcb就绪态
        pcb.setStatus(PCB.TASK_READY);
        // cpu恢复现场
        CPU.cpu.Recovery(pcb);
        // 设置pcb运行态
        pcb.setStatus(PCB.TASK_RUNNING);
        Log.Info("打开文件", String.format("进程:%d,执行打开文件系统调用,打开文件:%s,返回文件描述符为:%d",
                pcb.getID(), path, fd));
    }
}
