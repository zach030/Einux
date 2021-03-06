package os.schedule;

import hardware.CPU;
import os.process.Instruction;

public class Execute {
    public static Execute execute = new Execute();

    public void ExecuteInstruction(Instruction instruction) {
        switch (instruction.getType()) {
            case 0:
                System.out.println("执行系统调用");
                execSystemCallInstruction();
                break;
            case 1:
                System.out.println("执行写内存指令");
                execWriteMemoryInstruction();
                break;
            case 2:
                System.out.println("执行跳转指令");
                execJumpInstruction();
                break;
            case 3:
                System.out.println("执行输入指令");
                execInputInstruction();
                break;
            case 4:
                System.out.println("执行输出指令");
                execOutputInstruction();
                break;
            case 5:
                System.out.println("执行申请资源类型");
                execApplyResource();
                break;
            case 6:
                System.out.println("执行释放资源类型");
                execReleaseResource();
                break;
        }
    }

    void execSystemCallInstruction() {
        CPU.cpu.autoAddPC();
    }

    void execWriteMemoryInstruction() {
        CPU.cpu.autoAddPC();
    }

    void execJumpInstruction() {
        CPU.cpu.autoAddPC();
    }

    void execInputInstruction() {
        CPU.cpu.autoAddPC();
    }

    void execOutputInstruction() {
        CPU.cpu.autoAddPC();
    }

    void execApplyResource() {
        CPU.cpu.autoAddPC();
    }

    void execReleaseResource() {
        CPU.cpu.autoAddPC();
    }
}
