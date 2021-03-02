package os.filesystem;

import disk.DevConfig;
import disk.Disk;

// 磁盘管理单元 ：块
public class Block {
    int blockNo;    //物理块号
    boolean modify; //是否已修改
    byte[] data = new byte[DevConfig.BLOCK_SIZE];

    public Block() {
        this.modify = false;
        loadBlockData();
    }

    // 从磁盘加载数据
    void loadBlockData() {
        this.data = Disk.disk.getBlock(blockNo).getAllData();
    }

    public void write(int offset, short data) {
        // 低八位
        this.data[offset] = (byte) (data & 0XFF);
        // 高八位
        this.data[offset + 1] = (byte) (data >> 8);
        // 同步写到磁盘
        syncBlock();
    }

    public short read(int offset) {
        return (short) ((data[offset] & 0xFF) | (data[offset + 1] << 8));
    }

    // 同步到磁盘real block
    public void syncBlock() {
        Disk.disk.getBlock(this.blockNo).writeBlock(this.data);
    }
}
