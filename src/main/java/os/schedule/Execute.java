package os.schedule;

import hardware.CPU;
import hardware.MMU;
import hardware.memory.Memory;
import os.process.DeadLock;
import os.process.Instruction;
import os.process.ProcessManager;
import os.storage.StorageManager;
import os.systemcall.SystemCall;
import utils.Log;
import utils.SysConst;

public class Execute {
    public static Execute execute = new Execute();
    static final String period = "执行指令";

    public void ExecuteInstruction(Instruction instruction) {
        switch (instruction.getType()) {
            case 0:
                Log.Info(period, "执行系统调用--打开文件");
                execSystemCallInstruction(instruction);
                break;
            case 1:
                Log.Info(period, "执行写内存指令");
                execWriteMemoryInstruction(instruction);
                break;
            case 2:
                Log.Info(period, "执行读内存指令");
                execReadMemoryInstruction(instruction);
                break;
            case 3:
                Log.Info(period, "执行跳转指令");
                execJumpInstruction(instruction);
                break;
            case 4:
                Log.Info(period, "执行输入指令");
                execInputInstruction(instruction);
                break;
            case 5:
                Log.Info(period, "执行输出指令");
                execOutputInstruction(instruction);
                break;
            case 6:
                Log.Info(period, "执行申请资源类型");
                execApplyResource(instruction);
                break;
            case 7:
                Log.Info(period, "执行释放资源类型");
                execReleaseResource(instruction);
                break;
        }
    }

    //------------------指令的执行--------------------
    // 系统调用---创建文件
    void execSystemCallInstruction(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu进入内核态
        CPU.cpu.setState(CPU.KERNAL_STATE);
        // 3. cpu保护现场
        CPU.cpu.Protect();
        // 4. 阻塞进程
        ProcessManager.pm.processOperator.blockPCB(CPU.cpu.getCurrent());
        // 分解出文件路径
        int fileName = instruction.getArg();
        String path = "/home/zach/" + fileName + ".txt";
        // 系统调用打开文件
        int fd = SystemCall.systemCall.fileSystemCall.open(path, SystemCall.WRITE_ONLY);
        Log.Info("打开文件", String.format("进程:%d,执行打开文件系统调用,打开文件:%s,返回文件描述符为:%d",
                CPU.cpu.getCurrent().getID(), path, fd));
    }

    // 写内存指令
    void execWriteMemoryInstruction(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu用户态
        CPU.cpu.setState(CPU.USER_STATE);
        // 3. 获取写内存的逻辑地址
        int logicalAddr = instruction.getArg();
        // 4. 转换为物理地址
        int physicAddr = CPU.cpu.mmu.ResolveLogicalAddress((short) logicalAddr);
        // 5. 判断是否发生缺页
        if (physicAddr == MMU.NOT_FOUND_ERROR) {
            // 5.1 获取逻辑页号
            int logicalPageNo = logicalAddr / SysConst.PAGE_FRAME_SIZE;
            // 5.2 进行缺页中断
            StorageManager.sm.memoryManager.doPageFault(CPU.cpu.getCurrent(), logicalPageNo);
            // 5.3 查询到物理地址
            physicAddr = CPU.cpu.mmu.ResolveLogicalAddress((short) logicalAddr);
        }
        // 6. 获取写入内存的数据
        short data = instruction.getData();
        // 7. 向内存写数据
        Memory.memory.writeData((short) physicAddr, data);
        Log.Info("写内存指令", String.format("进程:%d,指令:%d,向内存物理地址:%d,写入数据:%d",
                CPU.cpu.getCurrent().getID(), instruction.getId(), physicAddr, data));
    }

    // 读内存指令
    void execReadMemoryInstruction(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu用户态
        CPU.cpu.setState(CPU.USER_STATE);
        // 3. 获取写内存的逻辑地址
        int logicalAddr = instruction.getArg();
        // 4. 转换为物理地址
        int physicAddr = CPU.cpu.mmu.ResolveLogicalAddress((short) logicalAddr);
        // 5. 判断是否发生缺页
        if (physicAddr == MMU.NOT_FOUND_ERROR) {
            // 5.1 获取逻辑页号
            int logicalPageNo = logicalAddr / SysConst.PAGE_FRAME_SIZE;
            // 5.2 进行缺页中断
            StorageManager.sm.memoryManager.doPageFault(CPU.cpu.getCurrent(), logicalPageNo);
            // 5.3 查询到物理地址
            physicAddr = CPU.cpu.mmu.ResolveLogicalAddress((short) logicalAddr);
        }
        // 6. 读内存数据
        short data = Memory.memory.readData((short) physicAddr);
        Log.Info("读内存指令", String.format("读出内存逻辑地址:%d,物理地址:%d,数据为:%d", logicalAddr, physicAddr, data));
    }

    // 跳转指令
    void execJumpInstruction(Instruction instruction) {
        // 1. cpu用户态
        CPU.cpu.setState(CPU.USER_STATE);
        // 2. 获取待跳转的指令id
        int targetInstructionID = instruction.getArg();
        // 2. 设置cpu的pc指针
        CPU.cpu.setPC(targetInstructionID);
        Log.Info("执行跳转指令", String.format("进程:%d,执行指令:%d,跳转到指令:%d",
                CPU.cpu.getCurrent().getID(), instruction.getId(), targetInstructionID));
    }

    // 请求输入
    void execInputInstruction(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu用户态
        CPU.cpu.setState(CPU.USER_STATE);
        int resource = instruction.getArg();
        Log.Info("执行请求输入指令", String.format("进程:%d,执行指令:%d,请求资源:%s", CPU.cpu.getCurrent().getID(), instruction.getId(), DeadLock.ResourceType.values()[resource].name()));
        DeadLock.deadLock.applyResource(CPU.cpu.getCurrent(), DeadLock.ResourceType.values()[resource], 1);
    }

    // 请求输出
    void execOutputInstruction(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu核心态
        CPU.cpu.setState(CPU.KERNAL_STATE);
        int resource = instruction.getArg();
        Log.Info("执行请求输出指令", String.format("进程:%d,执行指令:%d,请求资源:%s", CPU.cpu.getCurrent().getID(), instruction.getId(), DeadLock.ResourceType.values()[resource].name()));
        DeadLock.deadLock.applyResource(CPU.cpu.getCurrent(), DeadLock.ResourceType.values()[resource], 1);
    }

    // 请求资源
    void execApplyResource(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu核心态
        CPU.cpu.setState(CPU.KERNAL_STATE);
        // 申请资源
        int resource = instruction.getArg();
        int num = instruction.getData();
        Log.Info("执行请求资源指令", String.format("进程:%d,执行指令:%d,请求资源:%d，个数:%d", CPU.cpu.getCurrent().getID(), instruction.getId(), resource, num));
        DeadLock.deadLock.applyResource(CPU.cpu.getCurrent(), DeadLock.ResourceType.values()[resource], num);
    }

    // 释放资源
    void execReleaseResource(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu核心态
        CPU.cpu.setState(CPU.KERNAL_STATE);
        int resource = instruction.getArg();
        int num = instruction.getData();
        Log.Info("执行释放资源指令", String.format("进程:%d,执行指令:%d,请求资源:%d，个数:%d", CPU.cpu.getCurrent().getID(), instruction.getId(), resource, num));
        DeadLock.deadLock.releaseResource(CPU.cpu.getCurrent(), DeadLock.ResourceType.values()[resource], num);
    }
}
