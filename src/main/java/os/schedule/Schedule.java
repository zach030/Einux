package os.schedule;

import hardware.CPU;
import hardware.MMU;
import os.job.JCB;
import os.job.JobManage;
import os.process.PCB;
import os.process.ProcessManage;
import os.storage.StorageManage;

public class Schedule {
    public static Schedule schedule = new Schedule();

    // 内存缺页警示值
    public static final int MEMORY_LACK_WARN = 8;
    public static boolean needHighLevelScheduling = true;
    public static boolean needMediumLevelScheduling = true;

    // 高级调度，从后备队列选作业进入内存并创建进程
    public void HighLevelScheduling() {
        if (!JobManage.jm.isBackJobsEmpty() && !ProcessManage.pm.isPCBPoolFull()) { // 后备队列不为空并且系统可加入更多进程
            JCB jcb = JobManage.jm.getFirstJCB(); // 从后备队列取一个job
            if (StorageManage.sm.isPCBPoolZoneHasEmpty()) { // PCB池空间足够
                // 判断虚存空间足够
                if (StorageManage.sm.isSwapAreaEnough(jcb.getJobPagesNum())) {
                    PCB pcb = ProcessManage.pm.createPCB(jcb);  // 将JCB转换为PCB，创建进程
                    StorageManage.sm.allocPCBPageTable(pcb);    // 分配内存系统页表
                    StorageManage.sm.saveToSwapZone(jcb);       // 将进程数据保存到磁盘交换区
                    ProcessManage.pm.addPCBToPCBPool(pcb);      //将进程写入pcb池中
                    ProcessManage.pm.joinReadQueue(pcb);        // 将该进程加入到就绪队列
                    System.out.println("[INFO]-----成功创建PCB，ID=" + pcb.getID());
                } else {
                    System.out.println("[INFO]---虚存空间不足！---高级调度退出！");
                }
            } else {
                System.out.println("[INFO]---PCB池空间不足！---高级调度退出！");
            }
        } else {
            System.out.println("[INFO]---后备队列为空！---高级调度退出！");
        }
    }

    // 中级调度，又称平衡调度，根据内存资源情况决定内存要容纳的进程数
    public void MediumLevelScheduling() {
        System.out.printf("[TIME]---系统时间:%d ---开始中级调度！", CPU.cpu.clock.getCurrentTime());
        int pageFrameNum = StorageManage.sm.getFreePageNumInMemory();
        System.out.println("[INFO]---内存当前可用页框数:" + pageFrameNum);
        // 如果内存可用页框小于8，看做内存资源不足，把最近未访问的进程挂起调出内存
        if (pageFrameNum < MEMORY_LACK_WARN) {
            // todo 将不可运行的进程挂起
//            PCB pcb = ProcessManage.pm.getNotUsedPCB(); // 找到一个不能运行的进程
//            if (pcb == null) // 找不到直接返回
//                return;
//            //ProcessManage.m.PullProcessOutOfMemory(pcb); // 将此进程关联的代码段和数据段调出内存
//            ProcessManage.pm.joinSuspendQueue(pcb); // 挂起进程
//            System.out.println("[INFO]---内存资源不足！ ---挂起进程:" + pcb.getID());
        } else { //todo 内存资源足够，将挂起的进程从外存重新调回内存
            if (ProcessManage.pm.isSuspendQueueEmpty()) {
                System.out.println("[INFO]---内存资源足够！ ---挂起队列为空！ ---退出中级调度！");
                return;
            }
            PCB pcb = ProcessManage.pm.getFromSuspendQueue(); // 取出挂起队列第一个进程
            //ProcessManage.m.BringProcessToMemory(pcb); // 将代码段、数据段重新调入内存
            ProcessManage.pm.joinReadQueue(pcb); // 加入到就绪队列
            System.out.printf("[MEMORY]---内存资源足够！ ---将进程%d调回内存", pcb.getID());
        }
    }

    // 低级调度，选择一个进程运行
    public boolean LowLevelScheduling() {
        System.out.printf("[TIME] 系统时间:%d ---开始低级调度！", CPU.cpu.clock.getCurrentTime());
        // 查看就绪队列是否有进程可调度
        if (ProcessManage.pm.isReadyQueueEmpty()) { // 就绪队列为空，直接返回
            System.out.println("[INFO]------就绪队列为空！ ---结束低级调度！");
            return false;
        }
        // 就绪队列不为空
        PCB pcb = ProcessManage.pm.getFromReadyQueue(); // 从就绪队列取一个进程
        CPU.cpu.Recovery(pcb); // 恢复CPU现场
        CPU.cpu.getCurrent().setStatus(PCB.TASK_RUNNING); // 设为运行态
        CPU.cpu.mmu.initMMU(CPU.cpu.getCurrent().getInternPageTableBaseAddr(), CPU.cpu.getCurrent().getPageNums());// 将进程页表基地址装入MMU
        return true;
    }

    // 运行进程
    public void RunProcess() {
        System.out.println("[TIME]-----系统时间:" + CPU.cpu.clock.getCurrentTime() + "，系统正在运行...");
        ProcessManage.pm.DisplayAllPCBQueue();

        // 高级调度
        if (needHighLevelScheduling) { // 由后备队列检测线程设置是否需要高级调度
            System.out.println("[SCHEDULE]------正在进行高级调度.......");
            HighLevelScheduling();// 进行高级调度
            needHighLevelScheduling = false;
        }

        // 中级调度
        if (needMediumLevelScheduling) { // 是否需要中级调度
            System.out.println("[SCHEDULE]------正在进行中级调度.......");
            MediumLevelScheduling(); // 进行中级调度
            needMediumLevelScheduling = false;
        }

        // 如果所有进程运行完毕
        if (ProcessManage.pm.isAllFinished()) {
            System.out.println("[INFO]------当前所有进程都已运行完毕");
            return;
        }

        // 如果CPU空闲
        if (!CPU.cpu.isRunning()) {
            System.out.println("[INFO]------当前CPU空闲...");
            boolean success = LowLevelScheduling(); // 进行低级调度
            if (success) {
                System.out.println("[RUNNING]----开始运行进程:" + CPU.cpu.getCurrent().getID());
            } else
                return;
        }

        // 如果进程运行完毕
        if (CPU.cpu.getPC() >= CPU.cpu.getCurrent().getInstructionsNum()) {
            System.out.println("[INFO]------进程:" + CPU.cpu.getCurrent().getID() + ",运行结束.....");
            CPU.cpu.Protect(); // CPU保护现场
            ProcessManage.pm.cancelPCB(CPU.cpu.getCurrent()); // 撤销进程;
            return;
        }
        System.out.println("[RUNNING]------正在运行进程:" + CPU.cpu.getCurrent().getID());
        // 开始运行指令，先获取指令的逻辑地址，通过MMU转成物理地址
        int instructionLogicalAddr = CPU.cpu.getCurrentIRAddr(); // 当前指令的逻辑地址
        //todo 执行指令，怎么做缺页中断！！！
        int physicalAddress = CPU.cpu.mmu.ResolveLogicalAddress((short) instructionLogicalAddr); // 当前指令的物理地址
        if (physicalAddress == MMU.NOT_FOUND_ERROR) {  // 发生缺页
            int pageNo = CPU.cpu.getCurrentIRPageNum(); // 获取当前指令所在的页
            System.out.println("[PAGE FAULT]-----缺页中断，查询到指令:" + CPU.cpu.getPC() + ",逻辑页号:" + pageNo);
            StorageManage.sm.doPageFault(CPU.cpu.getCurrent(), pageNo); // 处理缺页中断
            physicalAddress = CPU.cpu.mmu.ResolveLogicalAddress((short) instructionLogicalAddr); // 当前指令的物理地址
        }
        //CPU.cpu.GetPCB().DisplayPageTable(); // 显示下页表
        // 没有发生缺中断，则从内存取出指令
        int IR = StorageManage.sm.visitMemory(physicalAddress);
        CPU.cpu.setIR(IR);
        System.out.printf("[RUNNING]---当前运行进程[%d] ---运行时间:{%d} ---指令数:{%d} ---优先级:{%d} ---时间片余额:{%d}", CPU.cpu.getCurrent().getID(), CPU.cpu.getCurrent().getRunTimes(), CPU.cpu.getCurrent().getInstructionsNum(), CPU.cpu.getCurrent().getPriority(), CPU.cpu.getCurrent().getTimeSlice());
        System.out.printf("[RUNNING]---当前执行第%d条指令 ---指令类型:{%d} ---逻辑地址:0x{%s} ---物理地址:0x{%s}", CPU.cpu.getPC() + 1, IR, String.format("%02X", instructionLogicalAddr), String.format("%02X", physicalAddress));
        //CPU.cpu.getCurrent().SetRunTimeAdd(100); // 运行时间增加
        Execute.execute.ExecuteInstruction(IR); // 执行指令

        if (!CPU.cpu.isRunning()) {
            // 如果cpu空闲
            return;
        }

        // 进程没运行完
//        CPU.cpu.GetPCB().SetTimeSliceSubtract(); // 时间片减少
//        if (CPU.cpu.GetPCB().GetTimeSlice() <= 0) // 时间片用完
//        {
//            lOutput += "\n---时间片用完！";
//            CPU.cpu.ProtectSpot(); // CPU保护现场
//            ProcessManage.m.JoinReadyQueue(CPU.cpu.GetPCB()); // 进程加入到就绪队列
//        }
//        Console.WriteLine(lOutput);
//        ProcessManage.m.LogToFile(lOutput);
//        MainForm.f.RefreshProcessInfoUI(lOutput);
    }

    // 调度线程开始
    public void Run() {
        CPU.cpu.clock.start(); // 开启计时器
        JobManage.jm.LoadJobFromFile(JobManage.jobFile);
        while (true) {
            if (CPU.cpu.clock.GetIfInterrupt()) // 一次中断
            {
                RunProcess(); // 运行进程
                CPU.cpu.clock.ResetIfInterrupt(); // 恢复中断
            }
        }
    }
}
