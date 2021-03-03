package os.job;

import hardware.Clock;
import os.filesystem.Block;
import os.process.Instruction;
import os.storage.StorageManage;
import utils.SysConst;

import java.util.ArrayList;
import java.util.Random;

public class JCB {
    private int jobID;                //作业ID
    private int jobPriority;          //作业/进程的优先级
    private int jobInTime;            //作业进入时间
    private int jobInstructionNum;    //作业包含的指令数目
    private int jobPagesNum;          //作业所占用的页面数目
    private int dataSegPages;         //数据段页面数
    private int codeSegPages;         //代码段页面数
    private int stackSegPages;        //栈段页面数
    private int[] diskBlockNo;        //所在磁盘块号
    private byte[] data;              //作业所带数据
    private ArrayList<Block> blocks;  //作业所在的全部物理块
    ArrayList<Instruction> instructions;

    // random create
    JCB(int jobID) {
        this.data = new byte[]{};
        this.diskBlockNo = new int[jobPagesNum];
        Random random = new Random();
        this.jobID = jobID;
        this.setJobInTime(Clock.clock.getCurrentTime());
        this.setJobPriority(random.nextInt(4) + 1);
        this.setJobInstructionNum(random.nextInt(10) + 1);
        this.instructions = new ArrayList<>();
    }

    // load from initial file
    public JCB() {
        this.setJobInTime(Clock.clock.getCurrentTime());
        this.instructions = new ArrayList<>();
    }

    // init job from txt file
    public void SetJobByFileLine(String[] jobInfo) {
        this.setJobID(Integer.parseInt(jobInfo[0]));
        this.setJobPriority(Integer.parseInt(jobInfo[1]));
        this.setJobInTime(Integer.parseInt(jobInfo[2]));
        this.setJobInstructionNum(Integer.parseInt(jobInfo[3]));
        instructions = new ArrayList<>(jobInstructionNum);
    }

    // init job instructions from file
    public void SetJobInstructions(String[] instructionInfo) {
        Instruction instruction = new Instruction(Integer.parseInt(instructionInfo[0]),
                Integer.parseInt(instructionInfo[1]), Integer.parseInt(instructionInfo[2]), instructionInfo[3].getBytes());
        this.instructions.add(instruction);
    }

    // calc job has pages num
    public void calcJobPageNum() {
        int codeSize = 0;
        for (int i = 0; i < jobInstructionNum; i++) {
            // traverse job's all instructions
            this.data = mergeBytes(this.data, instructions.get(i).getData());
            codeSize += this.instructions.get(i).getSize();
        }
        this.codeSegPages = codeSize / SysConst.PAGE_FRAME_SIZE + 1;
        int dataLength = 0;
        if (data != null) {
            dataLength = data.length;
        }
        this.dataSegPages = dataLength / SysConst.PAGE_FRAME_SIZE + 1;
        this.stackSegPages = 1;
        int jcbMetaPages = 1;
        this.jobPagesNum = this.codeSegPages + this.dataSegPages + this.stackSegPages + jcbMetaPages;
        blocks = new ArrayList<>(jobPagesNum);
    }

    // 将jcb信息保存到磁盘jcb区
    public void saveJobToDiskJCBZone() {
        Block block = StorageManage.sm.allocEmptyJCBBlock();
        block.write(0, (short) this.getJobID());
        block.write(2, (short) this.getJobPriority());
        block.write(4, (short) this.getJobInTime());
        block.write(6, (short) this.getJobPagesNum());
        block.write(8, (short) this.getJobInstructionNum());
        blocks.add(block);
        this.diskBlockNo[0] = block.getBlockNo();
        // 同步jcb到磁盘
        block.syncBlock();
    }

    // 将jcb数据保存到磁盘交换区
    public void saveJobBlockToSwapZone() {
        for (int i = 0; i < jobPagesNum; i++) {
            Block block = StorageManage.sm.allocEmptySwapBlock();
            //todo 存储jcb的数据到交换区
            // 记录jcb数据在磁盘中的物理块号
            blocks.add(block);
            this.diskBlockNo[i + 1] = block.getBlockNo();
            block.syncBlock();
        }
    }

    public static byte[] mergeBytes(byte[] data1, byte[] data2) {
        if (data1 == null) {
            return data2;
        }
        byte[] data3 = new byte[data1.length + data2.length];
        System.arraycopy(data1, 0, data3, 0, data1.length);
        System.arraycopy(data2, 0, data3, data1.length, data2.length);
        return data3;
    }

    public int[] getDiskBlockNo() {
        return diskBlockNo;
    }

    public void setDiskBlockNo(int[] diskBlockNo) {
        this.diskBlockNo = diskBlockNo;
    }

    public int getDataSegPages() {
        return dataSegPages;
    }

    public void setDataSegPages(int dataSegPages) {
        this.dataSegPages = dataSegPages;
    }

    public int getCodeSegPages() {
        return codeSegPages;
    }

    public void setCodeSegPages(int codeSegPages) {
        this.codeSegPages = codeSegPages;
    }

    public int getStackSegPages() {
        return stackSegPages;
    }

    public void setStackSegPages(int stackSegPages) {
        this.stackSegPages = stackSegPages;
    }

    public ArrayList<Instruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(ArrayList<Instruction> instructions) {
        this.instructions = instructions;
    }

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public int getJobPriority() {
        return jobPriority;
    }

    public void setJobPriority(int jobPriority) {
        this.jobPriority = jobPriority;
    }

    public int getJobInTime() {
        return jobInTime;
    }

    public void setJobInTime(int jobInTime) {
        this.jobInTime = jobInTime;
    }

    public int getJobInstructionNum() {
        return jobInstructionNum;
    }

    public void setJobInstructionNum(int jobInstructionNum) {
        this.jobInstructionNum = jobInstructionNum;
    }

    public int getJobPagesNum() {
        return jobPagesNum;
    }

    public void setJobPagesNum(int jobPagesNum) {
        this.jobPagesNum = jobPagesNum;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
