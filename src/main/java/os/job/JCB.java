package os.job;

import hardware.Clock;
import os.process.Instruction;
import hardware.Clock;
import os.process.Instruction;
import utils.SysConst;

import java.util.ArrayList;
import java.util.Random;

public class JCB {
    private int jobID;                //作业ID
    private int jobPriority;          //作业/进程的优先级
    private int jobInTime;            //作业进入时间
    private int jobInstructionNum;    //作业包含的指令数目
    private int jobPagesNum;          //作业所占用的页面数目
    private int diskBlockNo;          //所在磁盘块号
    private byte[] data;              //作业所带数据
    ArrayList<Instruction> instructions;

    // random create
    JCB(int jobID) {
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
            codeSize += this.instructions.get(i).getSize();
        }
        // code segment pages
        int codePages = codeSize / SysConst.PAGE_FRAME_SIZE + 1;
        // data segment pages
        int dataPages = data.length / SysConst.PAGE_FRAME_SIZE + 1;
        // stack segment pages: default one page
        int stackPages = 1;
        int metaDataPages = 1;
        this.jobPagesNum = codePages + dataPages + stackPages + metaDataPages;
    }

    public void saveToDisk(){
        //TODO 开始需要先将jcb写入磁盘jcb区
        //  新建一个页，用来存job信息
        //  从外磁盘分配块----》blockno
        //  jcb设置外存盘块号
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
