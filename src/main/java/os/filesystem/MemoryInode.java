package os.filesystem;

public class MemoryInode extends DiskInode {
    public int devNo;            // 设备编号
    public int referenceCount;      // 引用计数

    public MemoryInode(int inodeNo) {
        super(inodeNo);
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
    /**
        * @description: 从内存移到磁盘
        * @author: zach
     **/
    public void freeInodeFromMemory(){

    }
}
