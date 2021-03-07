package hardware;

import os.process.Instruction;
import os.process.PCB;
import utils.SysConst;

public class CPU {
    public static CPU cpu = new CPU();
    public Clock clock = Clock.clock;
    public MMU mmu = MMU.mmu;

    //------CPU模式----------
    static final int REAL_MODE = 0;     //实模式
    static final int PROTECT_MODE = 1;  //保护模式
    //-----CPU工作状态-------
    public static final int KERNAL_STATE = 0; //核心态
    public static final int USER_STATE = 1;   //用户态
    private int mode;    //cpu mode
    private int state;   // 态
    private boolean running;  // 运行
    private PCB current;
    private int CS;  // code segment register
    private int SS;  // stack segment register
    private int DS;  // data segment register
    private int ES;  // extra segment register
    private int IP;  // instruction pointer register
    private int IR;  // instruction pointer register
    private int PC;  // current exec instruction position register
    //TODO implement cr0,32位寄存器,与int大小一致
    private int cr0;
    private int cr1; // cr1: not used
    private int cr2; // cr2: 页故障线性地址
    private int cr3;// cr3: 页目录基址寄存器，页目录表页面是页对齐的，所以该寄存器只有高20位是有效
    // 往CR3中加载一个新值时低12位必须设置为0

    // 恢复进程
    public void Recovery(PCB pNew) {
        current = pNew;
        current.setTimeSlice();
        setIR(current.getIR());
        setPC(current.getPC());
        setRunning(true);
    }

    // 保护现场：进程被阻塞时
    public void Protect() {
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

    // 判断当前进程是否执行结束
    public boolean isCurrentPCBEnd(){
        return this.getPC() >= this.getCurrent().getInstructionsNum();
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
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
}
