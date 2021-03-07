package os.device;

public class BufferHead {

    private int devNo;           // 设备编号
    private int blockNo;         // 外存块编号
    private int bufferNo;        // 缓冲区编号
    private int memoryBlockNo;   //​内存块号
    public int flag;             // 标志位

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

    public int getMemoryBlockNo() {
        return memoryBlockNo;
    }

    public void setMemoryBlockNo(int memoryBlockNo) {
        this.memoryBlockNo = memoryBlockNo;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }
}
