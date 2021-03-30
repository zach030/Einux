package os.device;

import hardware.CPU;
import hardware.memory.Memory;
import os.process.PCB;
import os.process.ProcessManager;
import os.storage.StorageManager;
import utils.Log;

import java.util.ArrayList;

public class DeviceManager {
    public static DeviceManager dm = new DeviceManager();

    // 磁盘写入缓冲区(writeDevToBuffer)
    // 缓冲区数据写入磁盘(writeBufferToDev)
    //----------常量-----------------
    public static final String bufferOp = "缓冲区处理";
    public static final int LACK_FREE_BUFFER = -1;
    public static final int BH_EMPTY = 0;    // 空闲缓冲块
    public static final int BH_BUSY = 1;     // 表示缓冲区正使用，无第三方申请
    public static final int BH_UPDATE = 4;   // 缓冲区数据与磁盘保持一致

    //----------成员-----------------
    public BufferQueueManager bufferQueueManager;
    public BufferOperator bufferOperator;
    public BufferHead[] bufferHeads;    // 全部的缓冲区块

    public DeviceManager() {
        bufferHeads = new BufferHead[Memory.BUFFER_SIZE];
        bufferQueueManager = new BufferQueueManager();
        bufferOperator = new BufferOperator();
    }

    public static class BufferQueueManager {
        ArrayList<BufferHead> freeQueue = new ArrayList<>();  //空闲缓冲区
        // 一个设备对应一个list
        ArrayList<ArrayList<BufferHead>> hashQueue = new ArrayList<>();  // 已分配缓冲区

        /**
         * @description: 将缓冲区头部加入空闲队列
         * @author: zach
         **/
        synchronized public void joinFreeQueue(BufferHead bufferHead) {
            freeQueue.add(bufferHead);
        }

        synchronized boolean isFreeQueueEmpty() {
            return freeQueue.isEmpty();
        }

        /**
         * @description: 某设备获得缓冲区头部后, 加入此设备的缓冲区散列队列
         * @author: zach
         **/
        synchronized public void joinAllottedQueue(int devNo, BufferHead bh) {
            hashQueue.get(devNo).add(bh);
        }

        /**
         * @description: 获取此设备的缓冲区散列队列
         * @author: zach
         **/
        synchronized public ArrayList<BufferHead> getDevAllottedBhQueue(int devNo) {
            return hashQueue.get(devNo);
        }

        /**
         * @description: 申请一个空闲缓冲区
         * @author: zach
         **/
        synchronized public BufferHead getFreeBh() {
            return freeQueue.remove(0);
        }
    }

    public class BufferOperator {
        BufferOperator() {
            initBuffer();
        }

        /**
         * @description: 初始化缓冲区, 设置所有缓冲区都未被占用
         * @author: zach
         **/
        public void initBuffer() {
            for (int i = 0; i < bufferHeads.length; i++) {
                BufferHead bh = new BufferHead();
                bh.setBlockNo(-1);
                bh.setBufferNo(i);
                bh.setDevNo(-1);
                bh.setFrameNo(Memory.BUFFER_START + i);
                bh.setFlag(BH_EMPTY);
                bufferHeads[i] = bh;
                bufferQueueManager.joinFreeQueue(bh);
            }
            bufferQueueManager.hashQueue.add(new ArrayList<>());
        }

        /**
         * @description: 修改缓冲区头部位示图
         * @author: zach
         **/
        public synchronized void modifyStorageBitMap(int bufferNo, boolean status) {
            StorageManager.sm.bitMapManager.modifyBufferAreaBitMap(bufferNo, status);
        }

        /**
         * @description: 给设备分配一个缓冲区头部
         * @author: zach
         **/
//        public BufferHead getRandomBuffer(int devNo) {
//            // 0. 判断空闲缓冲队列是否为空
//            if (bufferQueueManager.freeQueue.isEmpty()) {
//                // 0.1 空闲缓冲区不足，进程被阻塞
//                ProcessManager.pm.processOperator.blockPCB(CPU.cpu.getCurrent(), devNo, LACK_FREE_BUFFER);
//                return null;
//            }
//            // 1. 从空闲队列获取队头
//            BufferHead bh = bufferQueueManager.getFreeBh();
//            // 2. 设置此缓冲区正在使用
//            bh.setFlag(BH_BUSY);
//            // 3. 修改内存位示图
//            modifyStorageBitMap(bh.getBufferNo(), true);
//            // 4. 插入到已分配缓冲区
//            bufferQueueManager.joinAllottedQueue(devNo, bh);
//            return bh;
//        }

        /**
         * @description: 获取此设备的指定块号对应的缓冲区头部
         * @author: zach
         **/
        private BufferHead getBuffer(int devNo, int blockNo) {
            // 0. 从散列队列里取
            // 0.1 获取此设备的缓冲队列
            ArrayList<BufferHead> devAllottedBhQueue = bufferQueueManager.getDevAllottedBhQueue(devNo);
            // 遍历散列队列，找到缓冲区头部信息和deviceNo、blockNo相等的缓冲区头部
            for (BufferHead bh : devAllottedBhQueue) {
                // 找到队列中，该磁盘块对应的bh
                if (bh.getBlockNo() == blockNo) {
                    // 若该bh正在被其他进程使用
                    if (bh.getFlag() == BH_BUSY) {
                        Log.Info(bufferOp, String.format("缓冲区:%d,正在被进程:%d使用", bh.getBufferNo(), CPU.cpu.getCurrent().getID()));
                        // 阻塞此进程
                        bh.blockPCBToThis(CPU.cpu.getCurrent());
                        ProcessManager.pm.processOperator.blockPCB(CPU.cpu.getCurrent(),devNo,bh.getBufferNo());
                        return null;
                    }
                    // 如果缓冲区未被占用
                    bh.setFlag(BH_BUSY);
                    // 修改缓冲区位示图
                    modifyStorageBitMap(bh.getBufferNo(), true);
                    Log.Info(bufferOp, String.format("缓冲区散列队列中空闲区:%d,已获得申请，对应内存块号:%d,磁盘块号:%d", bh.getBufferNo(), bh.getFrameNo(), bh.getBlockNo()));
                    return bh;
                }
            }
            // 1. 若散列队列没有，申请新的缓冲区
            return allotBufferHead(devNo, blockNo);
        }

        /**
         * @description: 为此设备指定块号分配一个内存缓冲区头部
         * @author: zach
         **/
        private BufferHead allotBufferHead(int devNo, int blockNo) {
            // 0. 判断空闲队列是否有空
            if (bufferQueueManager.isFreeQueueEmpty()) {
                // 0.1 因无空闲缓冲区而阻塞
                ProcessManager.pm.processOperator.blockPCB(CPU.cpu.getCurrent(), devNo, LACK_FREE_BUFFER);
                return null;
            }
            // 1. 从空闲队列获取bh
            BufferHead freeBh = bufferQueueManager.getFreeBh();
            // 2. 设置bh信息
            freeBh.setBlockNo(blockNo);
            freeBh.setDevNo(devNo);
            freeBh.setFlag(BH_BUSY);
            // 3.占用此bh
            modifyStorageBitMap(freeBh.getBufferNo(), true);
            bufferQueueManager.joinAllottedQueue(devNo, freeBh);
            Log.Info(bufferOp, String.format("从缓冲空闲队列中取出:%d,对应内存块号:%d,对应外存块号:%d", freeBh.getBufferNo(), freeBh.getFrameNo(), freeBh.getBlockNo()));
            return freeBh;
        }

        /**
         * @description: 释放缓冲区头部
         * @author: zach
         **/
        public void freeBuffer(BufferHead bh) {
            // 0. 清除bh的标志位
            bh.setFlag(BH_EMPTY);
            // 1. 从已释放的缓冲区中选出来，写入空闲队列
            Log.Info(bufferOp, String.format("释放缓冲区:%d,内存块号:%d,外设块号:%d", bh.getBufferNo(), bh.getFrameNo(), bh.getBlockNo()));
            // 2. 设置内存缓冲区位图
            modifyStorageBitMap(bh.getBufferNo(), false);
            // 3. 加入空闲队列
            bufferQueueManager.joinFreeQueue(bh);
            for (PCB pcb : bh.wakePCBToThis()) {
                ProcessManager.pm.processOperator.wakePCB(pcb);
            }
        }

        /**
         * @description: 将磁盘块内的信息写到对应的缓冲区中
         * @author: zach
         **/
        public BufferHead writeDevToBuffer(int devNo, int blockNo) {
            BufferHead bh = getBuffer(devNo, blockNo);
            // 如果缓冲区数据最新
            if (bh == null) {
                Log.Error("写磁盘失败", String.format("将缓冲区头部数据写入磁盘失败,写入磁盘块号:%d", blockNo));
                return null;
            }
            // 缓冲区数据与磁盘一致,不用写
            if (bh.getFlag() == BH_UPDATE) {
                Log.Info("磁盘写入缓冲区", String.format("磁盘块:%d,与缓冲区:%d,数据一致,无需写回", blockNo, bh.getFrameNo()));
                return bh;
            }
            // 不是最新，需要将磁盘的数据写入缓冲区
            StorageManager.sm.memoryManager.writeDiskToBuffer(blockNo, bh.getBufferNo(), bh.getFrameNo());
            bh.setFlag(BH_UPDATE);
            return bh;
        }

        /**
         * @description: 将缓冲区中的信息写回对应的磁盘物理块
         * @author: zach
         **/
        public void writeBufferToDev(BufferHead bh) {
            // 将缓冲区数据写入设备
            if (bh.getFlag() == BH_UPDATE) {
                Log.Info("缓冲区写入磁盘", String.format("磁盘块:%d,与缓冲区:%d,数据一致,无需写回", bh.getBlockNo(), bh.getFrameNo()));
                return;
            }
            StorageManager.sm.diskManager.writeBufferToDisk(bh.getBlockNo(), bh.getFrameNo());
            // 清空缓冲区
            freeBuffer(bh);
        }
    }
}
