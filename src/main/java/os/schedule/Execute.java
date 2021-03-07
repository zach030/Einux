package os.schedule;

import hardware.CPU;
import hardware.MMU;
import hardware.memory.Memory;
import os.process.Instruction;
import os.process.ProcessManager;
import os.storage.StorageManager;
import utils.Log;
import utils.SysConst;

public class Execute {
    public static Execute execute = new Execute();
    static final String period = "执行指令";

    public void ExecuteInstruction(Instruction instruction) {
        switch (instruction.getType()) {
            case 0:
                Log.Info(period, "执行系统调用--创建文件");
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
        // 3. 阻塞进程
        ProcessManager.pm.processOperator.blockPCB(CPU.cpu.getCurrent());
        // 4. cpu保护现场
        CPU.cpu.Protect();
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
        Log.Info(period, String.format("读出内存逻辑地址:%d,物理地址:%d,数据为:%d", logicalAddr, physicAddr, data));
    }

    // 跳转指令
    void execJumpInstruction(Instruction instruction) {
        // 1. cpu用户态
        CPU.cpu.setState(CPU.USER_STATE);
        // 2. 获取待跳转的指令id
        int targetInstructionID = instruction.getArg();
        // 2. 设置cpu的pc指针
        CPU.cpu.setPC(targetInstructionID);
    }

    // 请求输入
    void execInputInstruction(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu用户态
        CPU.cpu.setState(CPU.USER_STATE);
        //todo 如果无资源，则阻塞
    }

    // 请求输出
    void execOutputInstruction(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu核心态
        CPU.cpu.setState(CPU.KERNAL_STATE);
        //todo 如果无资源，则阻塞
    }

    // 请求资源
    void execApplyResource(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu核心态
        CPU.cpu.setState(CPU.KERNAL_STATE);
        //todo 如果无资源则阻塞
    }

    // 释放资源
    void execReleaseResource(Instruction instruction) {
        // 1. pc指针自增
        CPU.cpu.autoAddPC();
        // 2. cpu核心态
        CPU.cpu.setState(CPU.KERNAL_STATE);
        //todo 释放资源后，被阻塞的进程可加入就绪队列
    }
}
