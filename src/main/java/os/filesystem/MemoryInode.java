package os.filesystem;

import os.device.BufferHead;

import java.util.ArrayList;

public class MemoryInode extends DiskInode {
    public int devNo;            // 设备编号
    public int referenceCount;      // 引用计数
    public ArrayList<BufferHead> bufferHeads = new ArrayList<>();

    public MemoryInode(int inodeNo, int blockNo) {
        super(inodeNo, blockNo);
    }

    // todo 查找目录项，把每个目录项加入内存
    public int findEntry(String name) {
        for (Directory d : this.dirEntry) {
            if (d.name.equals(name)) {
                return d.inodeNo;
            }
        }
        return -1;
    }

}
