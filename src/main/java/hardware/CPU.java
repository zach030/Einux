package hardware;

import os.process.Instruction;
import os.process.PCB;
import utils.Log;
import utils.SysConst;

public class CPU {
    public static CPU cpu = new CPU();
    public Clock clock = Clock.clock;
    public MMU mmu = MMU.mmu;

    //------CPU模式----------
    static final int REAL_MODE = 0;     //实模式
    static final int PROTECT_MODE = 1;  //保护模式

    //-----CPU工作状态-------
    public enum Status {
        KERNAL_STATUS, USER_STATUS
    }

    public CPU() {
        this.status = Status.KERNAL_STATUS;
    }

    public static final int KERNAL_STATE = 0; //核心态
    public static final int USER_STATE = 1;   //用户态
    private int mode;    //cpu mode
    private Status status;   // 态
    private boolean running;  // 运行
    private PCB current;
    private int CS;  // code segment register
    private int SS;  // stack segment register
    private int DS;  // data segment register
    private int ES;  // extra segment register
    private int IP;  // instruction pointer register
    private int IR;  // instruction pointer register
    private int PC;  // current exec instruction position register
    private int cr0;
    private int cr1; // cr1: not used
    private int cr2; // cr2: 页故障线性地址
    private int cr3;// cr3: 页目录基址寄存器，页目录表页面是页对齐的，所以该寄存器只有高20位是有效
    // 往CR3中加载一个新值时低12位必须设置为0
    private int interrupt; // 当前中断向量值
    //todo 不太对，临时写的
    private String systemCallReg; //系统调用寄存器

    // 恢复进程
    public void Recovery(PCB pNew) {
        Log.Info("恢复现场", String.format("CPU正在恢复现场,记录新进程的IR：%d，PC:%d", pNew.getIR(), pNew.getPC()));
        current = pNew;
        current.setTimeSlice();
        setIR(current.getIR());
        setPC(current.getPC());
        setRunning(true);
    }

    // 保护现场：进程被阻塞时
    public void Protect() {
        Log.Info("保护现场", String.format("CPU正在保护现场,将IR=%d,PC=%d 寄存器压栈", IR, PC));
        current.setIR(this.IR);
        current.setPC(this.PC);
        setRunning(false);
    }

    // 获取当前指令的逻辑地址
    public int getCurrentIRAddr() {
        return this.current.getCode().getLogicalPageNo() * SysConst.PAGE_FRAME_SIZE + PC * Instruction.INSTRUCTION_SIZE;
    }

    // 获取当前指令所在页
    public int getCurrentIRPageNum() {
        return PC / Instruction.ONE_PAGE_HAS_INSTRUCTION_NUM + current.getCode().getLogicalPageNo();
    }

    public Instruction getInstruction() {
        return current.getInstruction();
    }

    public void setInstruction(Instruction instruction) {
        current.setInstruction(instruction);
    }

    // 判断当前进程是否执行结束
    public boolean isCurrentPCBEnd() {
        return this.getPC() >= this.getCurrent().getInstructionsNum();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void autoAddPC() {
        this.setPC(this.PC + 1);
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public PCB getCurrent() {
        return current;
    }

    public void setCurrent(PCB current) {
        this.current = current;
    }

    public int getIR() {
        return IR;
    }

    public void setIR(int IR) {
        this.IR = IR;
    }

    public int getPC() {
        return PC;
    }

    public void setPC(int PC) {
        this.PC = PC;
    }

    public int getCr2() {
        return cr2;
    }

    public void setCr2(int cr2) {
        this.cr2 = cr2;
    }

    public int getInterrupt() {
        return interrupt;
    }

    public void setInterrupt(int interrupt) {
        this.interrupt = interrupt;
    }

    public String getSystemCallReg() {
        return systemCallReg;
    }

    public void setSystemCallReg(String systemCallReg) {
        this.systemCallReg = systemCallReg;
    }
}
