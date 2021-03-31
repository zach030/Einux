package os.process;

import hardware.CPU;
import hardware.Clock;
import hardware.memory.Page;
import hardware.memory.Memory;
import os.job.JCB;
import os.storage.StorageManager;
import ui.PlatForm;
import utils.Log;

import java.util.ArrayList;
import java.util.Arrays;

public class PCB {
    //----------进程基本信息--------------------------
    private int ID;         //进程ID
    private Status status;     //进程状态
    private int priority;   //进程优先级
    private int policy;     //进程的调度策略
    private int IR;         //正在执行的指令类型
    private int PC;         //下一条执行的指令编号
    private int instructionsNum;//指令数
    private int timeSlice;  //当前时间片
    private int InTimes, EndTimes, RunTimes, TurnTimes; // 进程创建时间,进程结束时间,进程运行时间,进程周转时间
    static final int TIME_SLICE = 200;            //时间片
    private Instruction instruction;

    //---------------进程段信息-------------------------------
    private DataSeg dataSeg;                    //数据段
    private CodeSeg codeSeg;                    //代码段
    private StackSeg stackSeg;                  //堆栈段

    //--------------进程页信息--------------------------------
    private short internPageTableBaseAddr;             //内存页表基址
    private PageTableEntry[] internalPageTable;      //内页表
    private int pageNums;                            //进程所占页数

    //----------进程分页信息：pcb基础页 + 数据段 + 代码段 + 栈段-------
    ArrayList<Page> pcbHasPages = new ArrayList<>();
    private int pcbFramePageNo;                      //pcb信息在内存中的页框号
    public static final int PCB_LOGICAL_PAGE_NO = 0; //pcb信息逻辑页号为0
    private int dataLogicalPageNo = 1;               //数据段的起始逻辑页号
    private int dataSegPageNums;                     //数据段所占页数
    private int codeLogicalPageNo;                   //代码段起始逻辑页号
    private int codeSegPageNums;                     //代码段所占页数
    private int stackLogicalPageNo;                  //栈段起始逻辑页号
    private int stackSegPageNums;                    //栈段所占页数
    private int bufferNo;                            //阻塞队列排位

    //-------- 进程调度策略--------------------------
    public static final int SCHED_NORMAL = 0; // 按照优先级进行调度
    public static final int SCHED_FIFO = 1;   // 先进先出的调度算法
    public static final int SCHED_RR = 2;     // 时间片轮转的调度算法

    //-------进程状态--------------------------------
    public enum Status {
        READY, RUNNING, BLOCK, SUSPEND, END
    }

    static final int NOT_END = -1;     // 进程未运行完
    static final int NOT_RUN = 0;      // 未运行

    //--------进程段号-------------------------------
    static final int DATA_SEG_NO = 1;  //数据段段号
    static final int CODE_SEG_NO = 2;  //代码段段号
    static final int STACK_SEG_NO = 3; //栈段段号
    //---------用户打开文件表-----------------------
    public static int USER_MAX_FILE_NUM = 20;
    int[] userOpenFileTable = new int[]{USER_MAX_FILE_NUM};

    public PCB() {
        this.setTimeSlice();
        initUserOpenFileTable();
    }

    public PCB(int id) {
        this.setID(id);
        this.setTimeSlice();
    }

    //-----------------进程原语-----------------------
    // 创建进程原语
    public void createProcess(JCB jcb) {
        this.setID(jcb.getJobID());
        this.setPriority(jcb.getJobPriority());
        this.setInTimes(Clock.clock.getCurrentTime());
        this.setEndTimes(NOT_END);
        this.setRunTimes(NOT_RUN);
        this.setStatus(Status.READY);
        this.setPolicy(SCHED_RR);
        this.setPC(0);
        this.setTimeSlice();
        this.instructionsNum = jcb.getJobInstructionNum();
        initDataSegment(jcb);
        initCodeSegment(jcb);
        initStackSegment();
        pageNums = dataSeg.pageNums + codeSeg.pageNums + stackSeg.pageNums + 1;
        initPageTable(jcb.getDiskBlockNo());
    }

    // 撤销进程
    public void cancelProcess() {
        setStatus(Status.END);
        setEndTimes(CPU.cpu.clock.getCurrentTime());
        StorageManager.sm.releaseManager.releasePCBData(this);      // 释放此进程占用的数据区
        StorageManager.sm.releaseManager.releasePCBPageTable(this); // 释放进程页表
        StorageManager.sm.releaseManager.releasePCBPoolPage(this);  // 修改内存pcbPool数据
        DeadLock.deadLock.releasePCBResource(this);                 // 释放此进程占有的资源
    }

    // 阻塞进程
    public void blockProcess() {
        setStatus(Status.BLOCK);
    }

    // 唤醒进程
    public void wakeUpProcess() {
        setStatus(Status.READY);
    }

    // 挂起进程
    public void suspendProcess() {
        setStatus(Status.SUSPEND);
    }

    //----------------初始化进程各段------------------------
    void initDataSegment(JCB jcb) {
        dataSeg = new DataSeg(jcb.getData(), jcb.getDataSegPages());
        this.dataSegPageNums = dataSeg.getPageNums();
    }

    void initCodeSegment(JCB jcb) {
        this.codeLogicalPageNo = dataLogicalPageNo + dataSegPageNums;
        codeSeg = new CodeSeg(jcb.getInstructions(), codeLogicalPageNo, jcb.getCodeSegPages());
        this.codeSegPageNums = codeSeg.getPageNums();
    }

    void initStackSegment() {
        this.stackLogicalPageNo = codeLogicalPageNo + codeSegPageNums;
        stackSeg = new StackSeg(stackLogicalPageNo);
        this.stackSegPageNums = stackSeg.getPageNums();
    }

    //----------------------初始化进程页表表----------------
    // 初始化进程页表
    void initPageTable(int[] blockNo) {
        internalPageTable = new PageTableEntry[pageNums];
        for (int i = 0; i < pageNums; i++) {
            PageTableEntry pageTableEntry = new PageTableEntry();
            pageTableEntry.setVirtualPageNo(i);
            pageTableEntry.setPhysicPageNo(-1);
            pageTableEntry.setValid(false);
            pageTableEntry.setModify(false);
            pageTableEntry.setDiskBlockNo(blockNo[i]);
            internalPageTable[i] = pageTableEntry;
        }
    }

    // 写进程的内页表项
    public void writePageTableEntry(int virtualNo, Page page) {
        internalPageTable[virtualNo].setVirtualPageNo(page.getLogicalNo());
        internalPageTable[virtualNo].setValid(true);
        internalPageTable[virtualNo].setDiskBlockNo(page.getBlockNo());
        internalPageTable[virtualNo].setModify(page.isModify());
        internalPageTable[virtualNo].setPhysicPageNo(page.getFrameNo());
        int pte = internalPageTable[virtualNo].pteDataToWord();
        // 将页表项写入内存
        Memory.memory.writeWordData((short) (internPageTableBaseAddr + virtualNo * Memory.PAGE_TABLE_ENTRY_SIZE), pte);
        PlatForm.platForm.refreshPageTable();
    }

    // 查找页表
    public int searchPageTable(int pageNo) {
        for (PageTableEntry pageTableEntry : internalPageTable) {
            if (pageTableEntry.getVirtualPageNo() == pageNo) {
                Log.Info("查页表", String.format("正在为进程:%d，逻辑页号:%d，查询页框号", this.getID(), pageNo));
                return pageTableEntry.getDiskBlockNo();
            }
        }
        return -1;
    }

    //-------------------------内外存写数据------------------
    // 写入内存pcb池中
    public void writePCBPage() {
        // 新建pcb基础信息页
        Page page = new Page(PCB_LOGICAL_PAGE_NO, pcbFramePageNo, 0, true);
        page.write(0, (short) getID());        // 写入pcb的id
        page.write(2, (short) getPriority());    // 写入pcb的优先级
        page.write(4, (short) getInTimes());     // 写入pcb进入时间
        page.write(6, (short) getPolicy());      // 写入进程调度策略
        page.write(8, (short) getIR());          // 写入当前运行的指令
        page.write(10, (short) getPC());         // 写入当前运行指令编号
        // 将此页信息进行同步
        page.syncPage();
        // 将pcb基础信息页加入列表
        this.pcbHasPages.add(page);
    }

    public void freePCBPage() {
        Page page = this.pcbHasPages.get(0);
        page.clearPage();
        page.syncPage();
    }

    public void initUserOpenFileTable() {
        Arrays.fill(userOpenFileTable, -1);
    }

    /**
     * @description: 加入用户打开文件表，并返回fd
     * @author: zach
     **/
    public int addUserOpenFileTable(int sysFd) {
        for (int i = 0; i < userOpenFileTable.length; i++) {
            if (userOpenFileTable[i] == sysFd) {
                return i;
            }
        }
        for (int i = 0; i < userOpenFileTable.length; i++) {
            if (userOpenFileTable[i] == -1) {
                userOpenFileTable[i] = sysFd;
                return i;
            }
        }
        return -1;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public int[] getUserOpenFileTable() {
        return userOpenFileTable;
    }

    public void setUserOpenFileTable(int[] userOpenFileTable) {
        this.userOpenFileTable = userOpenFileTable;
    }

    public int getUserOpenFileTable(int fd) {
        return userOpenFileTable[fd];
    }

    public void releaseUserOpenFileTable(int fd) {
        this.userOpenFileTable[fd] = -1;
    }

    //----------------进程时间操作-------------
    public int getCurrentPageNumOfIR() {
        return PC / 64 + codeLogicalPageNo;
    }

    public void subTimeSlice() {
        this.timeSlice -= 100;
    }

    public void addRunTime(int time) {
        this.setRunTimes(this.getRunTimes() + time);
    }

    public int getInstructionsNum() {
        return instructionsNum;
    }

    public void setInstructionsNum(int instructionsNum) {
        this.instructionsNum = instructionsNum;
    }

    public int getPcbFramePageNo() {
        return pcbFramePageNo;
    }

    public void setPcbFramePageNo(int pcbFramePageNo) {
        this.pcbFramePageNo = pcbFramePageNo;
    }

    public int getPageNums() {
        return pageNums;
    }

    public void setPageNums(int pageNums) {
        this.pageNums = pageNums;
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

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getPolicy() {
        return policy;
    }

    public void setPolicy(int policy) {
        this.policy = policy;
    }

    public int getInTimes() {
        return InTimes;
    }

    public void setInTimes(int inTimes) {
        InTimes = inTimes;
    }

    public int getEndTimes() {
        return EndTimes;
    }

    public void setEndTimes(int endTimes) {
        EndTimes = endTimes;
    }

    public int getRunTimes() {
        return RunTimes;
    }

    public void setRunTimes(int runTimes) {
        RunTimes = runTimes;
    }

    public int getTurnTimes() {
        return TurnTimes;
    }

    public void setTurnTimes(int turnTimes) {
        TurnTimes = turnTimes;
    }

    public DataSeg getData() {
        return dataSeg;
    }

    public void setData(DataSeg data) {
        this.dataSeg = data;
    }

    public CodeSeg getCode() {
        return codeSeg;
    }

    public void setCode(CodeSeg code) {
        this.codeSeg = code;
    }

    public StackSeg getStack() {
        return stackSeg;
    }

    public void setStack(StackSeg stack) {
        this.stackSeg = stack;
    }

    public static int getDataSegNo() {
        return DATA_SEG_NO;
    }

    public static int getCodeSegNo() {
        return CODE_SEG_NO;
    }

    public static int getStackSegNo() {
        return STACK_SEG_NO;
    }

    public int getTimeSlice() {
        return timeSlice;
    }

    public boolean isRunOutOfTimeSlice() {
        return timeSlice <= 0;
    }

    public void setTimeSlice() {
        this.timeSlice = TIME_SLICE;
    }

    public short getInternPageTableBaseAddr() {
        return internPageTableBaseAddr;
    }

    public int getBufferNo() {
        return bufferNo;
    }

    public void setBufferNo(int bufferNo) {
        this.bufferNo = bufferNo;
    }

    public void setInternPageTableBaseAddr(short internPageTableBaseAddr) {
        this.internPageTableBaseAddr = internPageTableBaseAddr;
    }

    public PageTableEntry[] getInternalPageTable() {
        return internalPageTable;
    }

    public void setInternalPageTable(PageTableEntry[] internalPageTable) {
        this.internalPageTable = internalPageTable;
    }
}
