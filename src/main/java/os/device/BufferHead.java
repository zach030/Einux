package os.device;

import hardware.memory.Memory;
import os.process.PCB;

import java.util.ArrayList;

public class BufferHead {

    private int devNo;           // 设备编号
    private int blockNo;         // 外存块编号
    private int bufferNo;        // 缓冲区编号
    private int frameNo;   //​内存块号
    public int flag;             // 标志位
    ArrayList<PCB> blockWaitBufferQueue = new ArrayList<>(); // 被此块阻塞的进程队列

    public int getDevNo() {
        return devNo;
    }

    public void setDevNo(int devNo) {
        this.devNo = devNo;
    }

    public int getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }

    public int getBufferNo() {
        return bufferNo;
    }

    public void setBufferNo(int bufferNo) {
        this.bufferNo = bufferNo;
    }

    public int getFrameNo() {
        return frameNo;
    }

    public void setFrameNo(int frameNo) {
        this.frameNo = frameNo;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public ArrayList<PCB> getBlockWaitBufferQueue() {
        return blockWaitBufferQueue;
    }

    /**
        * @description: 将因争夺此缓冲区而阻塞的进程加入队列
        * @author: zach
     **/
    synchronized public void blockPCBToThis(PCB pcb) {
        pcb.setBufferNo(this.getBufferNo());
        this.blockWaitBufferQueue.add(pcb);
    }

    /**
        * @description: 当此缓冲区头部释放时,返回被阻塞的进程列表
        * @author: zach
     **/
    synchronized public ArrayList<PCB> wakePCBToThis() {
        ArrayList<PCB> allBlockPCB = this.blockWaitBufferQueue;
        this.blockWaitBufferQueue.clear();
        return allBlockPCB;
    }
}
