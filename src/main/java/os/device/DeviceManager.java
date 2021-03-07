package os.device;

import hardware.CPU;
import hardware.memory.Memory;
import os.process.ProcessManager;

import java.util.ArrayList;

public class DeviceManager {
    public static DeviceManager dm = new DeviceManager();

    //----------常量-----------------
    public static final int BH_WRITE = 0;   // 有效
    public static final int BH_READ = 1;    // 脏

    //----------成员-----------------
    public BufferQueueManager bufferQueueManager;
    public BufferOperator bufferOperator;
    public BufferHead[] bufferHeads;    // 全部的缓冲区块

    public DeviceManager() {
        bufferQueueManager = new BufferQueueManager();
        bufferOperator = new BufferOperator();
        bufferHeads = new BufferHead[Memory.BUFFER_SIZE];
    }

    public class BufferQueueManager {
        ArrayList<BufferHead> freeQueue = new ArrayList<>();  //空闲缓冲区
        ArrayList<ArrayList<BufferHead>> allottedQueue = new ArrayList<>();  // 已分配缓冲区

        synchronized public void joinFreeQueue(BufferHead bufferHead) {
            freeQueue.add(bufferHead);
        }
    }

    public class BufferOperator {
        // 初始化缓冲区
        public void initBuffer() {
            for (int i = 0; i < bufferHeads.length; i++) {
                BufferHead bh = new BufferHead();
                bh.setBlockNo(-1);
                bh.setBufferNo(i);
                bh.setDevNo(-1);
                bh.setMemoryBlockNo(Memory.BUFFER_START + i);
                bufferHeads[i] = bh;
                bufferQueueManager.joinFreeQueue(bh);
            }
            bufferQueueManager.allottedQueue.add(new ArrayList<>());
        }

        // 获取缓冲区
        public void getBuffer() {
            // 0. 判断空闲缓冲队列是否为空
            if (bufferQueueManager.freeQueue.isEmpty()) {
                // 0.1 空闲缓冲区不足，进程被阻塞
                ProcessManager.pm.processOperator.blockPCB(CPU.cpu.getCurrent());
                return;
            }
            // 1. 从空闲队列获取队头
            BufferHead bufferHead = bufferQueueManager.freeQueue.remove(0);

        }

        // 释放缓冲区
        public void freeBuffer() {

        }

        // 磁盘读入缓冲区
        public void writeDiskToBuffer() {

        }

        // 缓冲区写入磁盘
        public void writeBufferToDisk() {

        }
    }
}
