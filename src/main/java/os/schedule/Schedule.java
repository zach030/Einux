package os.schedule;

import hardware.CPU;
import hardware.MMU;
import os.filesystem.FileSystem;
import os.job.JCB;
import os.job.JobManage;
import os.process.Instruction;
import os.process.Interrupt;
import os.process.PCB;
import os.process.ProcessManager;
import os.storage.StorageManager;
import ui.PlatForm;
import utils.Log;

public class Schedule extends Thread {
    public static Schedule schedule = new Schedule();

    public static boolean systemStatus = false;
    // 内存缺页警示值
    public static final int MEMORY_LACK_WARN = 8;
    //------------------日志常量------------------
    public static final String systemRun = "系统运行";
    public static final String schedulePeriod = "系统调度";
    public static final String highLevelSchedule = "高级调度";
    public static final String midLevelSchedule = "中级调度";
    public static final String lowLevelSchedule = "低级调度";
    public static final String memoryDetect = "内存空间检测";
    public static final String memoryOperate = "内存读写操作";
    public static final String diskDetect = "磁盘空间检测";
    public static final String diskOperate = "磁盘读写操作";
    //----------------调度标志位------------------------
    public static boolean needHighLevelScheduling = false;   // 是否需要高级调度
    public static boolean needMediumLevelScheduling = false; // 是否需要中级调度

    // 高级调度，从后备队列选作业进入内存并创建进程
    public void HighLevelScheduling() {
        // 后备队列不为空且内存pcb池不满
        Log.Debug(highLevelSchedule, "正在检测后备队列是否为空，内存pcb池是否有足够空间");
        if (!JobManage.jm.isBackJobsEmpty() && !ProcessManager.pm.requesterManager.isPCBPoolFull()) {
            // 从后备队列取一个job
            JCB jcb = JobManage.jm.getFirstJCB();
            Log.Info(highLevelSchedule, "取出作业,作业id为:" + jcb.getJobID());
            // PCB池空间足够
            Log.Debug(memoryDetect, "正在检测内存pcb池是否有空闲空间");
            if (StorageManager.sm.requesterManager.isPCBPoolZoneHasEmpty()) {
                // 判断虚存空间足够
                Log.Debug(diskDetect, "正在检测磁盘交换区是否有空闲空间，当前作业需要物理块数:" + jcb.getJobPagesNum());
                if (StorageManager.sm.requesterManager.isSwapAreaEnough(jcb.getJobPagesNum())) {
                    // 将作业数据保存到磁盘交换区
                    StorageManager.sm.diskManager.saveToSwapZone(jcb);
                    Log.Info(diskOperate, "成功将当前作业的数据保存到磁盘交换区");
                    // 将JCB转换为PCB，创建进程
                    PCB pcb = ProcessManager.pm.processOperator.createPCB(jcb);
                    Log.Info(highLevelSchedule, "成功由当前作业新建进程pcb，id为" + pcb.getID());
                    // 分配内存系统页表
                    StorageManager.sm.allotManager.allotPCBPageTable(pcb);
                    Log.Info(memoryOperate, "成功为进程:" + pcb.getID() + "分配页表");
                    // 将进程写入pcb池中
                    ProcessManager.pm.processOperator.addPCBToPCBPool(pcb);
                    Log.Info(memoryOperate, "成功将进程pcb信息页写入内存pcb池");
                    // 将该进程加入到就绪队列
                    ProcessManager.pm.queueManager.joinReadQueue(pcb);
                    Log.Info(memoryOperate, "成功将当前进程:" + pcb.getID() + "加入就绪队列");
                    Log.Info(memoryOperate, "成功创建PCB，ID=" + pcb.getID());
                } else {
                    Log.Error(highLevelSchedule, "虚存空间不足！---高级调度退出");
                }
            } else {
                Log.Error(highLevelSchedule, "PCB池空间不足！---高级调度退出！");
            }
        } else {
            Log.Error(highLevelSchedule, "后备队列为空！---高级调度退出！");
        }
    }

    // 中级调度，根据内存的使用情况，对进程进行换入换出
    public void MidLevelScheduling() {
        int pageFrameNum = StorageManager.sm.requesterManager.getFreePageNumInMemory();
        Log.Info(midLevelSchedule, "内存当前可用页框数为:" + pageFrameNum);
        // 如果内存可用页框小于8，看做内存资源不足，把最近未访问的进程挂起调出内存
        if (pageFrameNum < MEMORY_LACK_WARN) {
            // todo 将不可运行的进程挂起
            // 0. 找到一个不能运行的进程
            PCB pcb = ProcessManager.pm.processOperator.chooseNotUsedPCB();
            if (pcb == null) // 找不到直接返回
                return;
            // 1. 将此进程数据转至磁盘
            ProcessManager.pm.processOperator.turnPCBToDisk(pcb);
            // 2. 将此进程加入挂起队列
            ProcessManager.pm.queueManager.joinSuspendQueue(pcb);
            System.out.println("[INFO]---内存资源不足！ ---挂起进程:" + pcb.getID());
        } else {
            // 0. 判断挂起队列是否空
            if (ProcessManager.pm.requesterManager.isSuspendQueueEmpty()) {
                Log.Info(midLevelSchedule, "当前内存资源足够，挂起队列为空，退出中级调度");
                return;
            }
            // 1. 取出挂起队列第一个进程
            PCB pcb = ProcessManager.pm.queueManager.getFromSuspendQueue();
            // 2. 将代码段、数据段重新调入内存
            ProcessManager.pm.processOperator.returnPCBToMemory(pcb);
            // 3. 加入到就绪队列
            ProcessManager.pm.queueManager.joinReadQueue(pcb);
            Log.Info(midLevelSchedule, "当前内存资源充足，已将进程:" + pcb.getID() + "的数据调回内存");
        }
    }

    // 低级调度，从就绪队列选择一个进程运行
    public boolean LowLevelScheduling() {
        Log.Info(lowLevelSchedule, "开始低级调度");
        // 查看就绪队列是否有进程可调度
        if (ProcessManager.pm.requesterManager.isReadyQueueEmpty()) { // 就绪队列为空，直接返回
            Log.Debug(lowLevelSchedule, "就绪队列为空，结束此次低级调度");
            return false;
        }
        // 就绪队列不为空
        PCB pcb = ProcessManager.pm.queueManager.getFromReadyQueue(); // 从就绪队列取一个进程
        CPU.cpu.Recovery(pcb); // 恢复CPU现场
        CPU.cpu.getCurrent().setStatus(PCB.Status.RUNNING); // 设为运行态
        CPU.cpu.mmu.initMMU(CPU.cpu.getCurrent().getInternPageTableBaseAddr(), CPU.cpu.getCurrent().getPageNums());// 将进程页表基地址装入MMU
        return true;
    }

    // 运行进程
    public void RunProcess() {
        // 0.系统时间自增
        CPU.cpu.clock.systemTimeSelfAdd();
        // 如果CPU空闲,需要低级调度
        if (!CPU.cpu.isRunning()) {
            Log.Info(schedulePeriod, "当前CPU空闲");
            boolean success = LowLevelScheduling(); // 进行低级调度
            if (success) {
                Log.Info(systemRun, "开始运行进程:" + CPU.cpu.getCurrent().getID());
            } else
                return;
        }
        PlatForm.platForm.refreshCurrentPCB();
        // 如果进程运行完毕，取消进程
        if (CPU.cpu.isCurrentPCBEnd()) {
            Log.Info(systemRun, "进程:" + CPU.cpu.getCurrent().getID() + ",运行结束");
            CPU.cpu.Protect(); // CPU保护现场
            ProcessManager.pm.processOperator.cancelPCB(CPU.cpu.getCurrent()); // 撤销进程;
            return;
        }
        // 如果所有进程运行完毕
        if (ProcessManager.pm.requesterManager.isAllFinished()) {
            Log.Info(schedulePeriod, "当前进程已全部运行结束");
            return;
        }

        Log.Info(systemRun, "正在运行进程:" + CPU.cpu.getCurrent().getID());
        // 1.获取当前指令的逻辑地址
        int instructionLogicalAddr = CPU.cpu.getCurrentIRAddr();
        // 2.通过MMU将逻辑地址转为物理地址
        int physicalAddress = CPU.cpu.mmu.ResolveLogicalAddress((short) instructionLogicalAddr); // 当前指令的物理地址
        if (physicalAddress == MMU.NOT_FOUND_ERROR) {
            // 2.1. 如果发生缺页，获取当前指令所在的页
            int pageNo = CPU.cpu.getCurrentIRPageNum();
            Log.Info("缺页中断", "查询到指令:" + CPU.cpu.getPC() + ",逻辑页号是：" + pageNo);
            // 保存缺页中断所需逻辑页号
            CPU.cpu.setCr2(pageNo);
            // 设置中断向量值
            CPU.cpu.setInterrupt(Interrupt.PAGE_FAULT);
            // 运行中断服务例程
            Interrupt.interrupt.doInterrupt();
            // 2.3. 重新获得当前指令的物理地址
            physicalAddress = CPU.cpu.mmu.ResolveLogicalAddress((short) instructionLogicalAddr);
        }
        // 3.从内存取出这条指令
        Log.Info("访问内存取指令", String.format("当前进程正在访问内存，指令的物理地址:%d", physicalAddress));
        int instructionData = StorageManager.sm.memoryManager.visitMemory(physicalAddress);
        Instruction instruction = ProcessManager.pm.processOperator.getInstructionByData(instructionData);
        Log.Info("访问内存取指令", String.format("从内存:%d，取出指令:%d", physicalAddress, instruction.getId()));
        CPU.cpu.setInstruction(instruction);
        PlatForm.platForm.refreshIRInfo();
        // 4.进程运行时间增加
        CPU.cpu.getCurrent().addRunTime(100);
        // 5. 进程时间片减少
        CPU.cpu.getCurrent().subTimeSlice();
        // 6. 执行指令
        CPU.cpu.setIR(instruction.getType());
        Log.Info(systemRun, String.format("当前运行进程:%d, 运行时间:%d, 指令数:%d, 优先级:%d, 时间片余额:%d", CPU.cpu.getCurrent().getID(), CPU.cpu.getCurrent().getRunTimes(), CPU.cpu.getCurrent().getInstructionsNum(), CPU.cpu.getCurrent().getPriority(), CPU.cpu.getCurrent().getTimeSlice()));
        Log.Info(systemRun, String.format("当前执行第:%d条指令，指令类型是:%d, 指令的逻辑地址是:%d, 物理地址是:%d", instruction.getId(), instruction.getType(), instructionLogicalAddr, physicalAddress));
        Execute.execute.ExecuteInstruction(instruction);
        PlatForm.platForm.refreshCurrentPC();
        // 如果cpu此时空闲，则需要重新调度（进程被阻塞了）
        if (!CPU.cpu.isRunning()) {
            Log.Debug(lowLevelSchedule, "当前进程被阻塞，需要重新调度");
            return;
        }

        // 7. 判断进程时间片是否用完
        if (CPU.cpu.getCurrent().isRunOutOfTimeSlice()) {
            Log.Info(systemRun, String.format("当前进程:%d, 时间片已用完", CPU.cpu.getCurrent().getID()));
            // 7.1 CPU保护现场
            CPU.cpu.Protect();
            // 7.2 将此进程重新放入就绪队列
            ProcessManager.pm.queueManager.joinReadQueue(CPU.cpu.getCurrent());
        }
    }


    // 调度线程开始
    public void init() {
        FileSystem.fs.start();
        // 0.开启系统计时器
        CPU.cpu.clock.start();
        // 1.开启高级、中级调度检测线程
        Detector.detector.StartDetector();
        JobManage.jm.LoadJobFromFile(JobManage.jm.chooseFile);
    }

    private final Object lock = new Object();
    private boolean pause = false;

    /**
     * @description: 暂停线程
     * @author: zach
     **/
    public void pauseThread() {
        pause = true;
    }

    /**
     * @description: 继续线程
     * @author: zach
     **/
    public void resumeThread() {
        pause = false;
        synchronized (lock) {
            lock.notify();
        }
    }

    /**
     * @description: 等待锁
     * @author: zach
     **/
    void onPause() {
        synchronized (lock) {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @description: 终止程序
     * @author: zach
     **/
    public void Stop() {

    }

    @Override
    public void run() {
        while (true) {
            while (pause) {
                onPause();
            }
            try {
                sleep(1000);
                // 如果发生时钟中断，开启调度
                if (CPU.cpu.clock.GetIfInterrupt()) {
                    // 高级调度
                    if (needHighLevelScheduling) {
                        // 由后备队列检测线程设置是否需要高级调度
                        Log.Info(schedulePeriod, "正在进行高级调度");
                        HighLevelScheduling();
                        needHighLevelScheduling = false;
                    }
                    // 中级调度
                    if (needMediumLevelScheduling) {
                        Log.Info(schedulePeriod, "正在进行中级调度");
                        MidLevelScheduling();
                        needMediumLevelScheduling = false;
                    }
                    RunProcess(); // 运行进程
                    CPU.cpu.clock.ResetIfInterrupt(); // 恢复中断
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
