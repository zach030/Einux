package hardware;

import utils.SysConst;

// page : base operation for nno matter memory or disk
public class Page {
    private int no;             //逻辑页号
    private int frameNo;        //物理页框号
    private int blockNo;        //物理块号
    private boolean stay;       //驻留内存(状态位)
    private boolean modify;     //是否修改(修改位)
    byte[] pageData = new byte[SysConst.PAGE_FRAME_SIZE]; // 页数据

    public Page() {
        this.stay = true;
    }

    int R; //引用位，被访问则置1

    // clock-mmu refer page
    public void Refer() {
        this.R = SysConst.PAGE_REFER;
    }

    // clock-mmu clear page
    public void Clear() {
        this.R = SysConst.PAGE_CLEAR;
    }

    //write page
    public void write(int offset, short data) {
        // 低八位
        pageData[offset] = (byte) (data & 0XFF);
        // 高八位
        pageData[offset + 1] = (byte) (data >> 8);
    }

    //read page
    public short read(int offset) {
        return (short) ((pageData[offset] & 0xFF)| (pageData[offset+1] << 8));
    }

    public void clearPage(){
        this.pageData = new byte[SysConst.PAGE_FRAME_SIZE];
    }

    // synchronization to disk ( virtual address + data)
    public void syncPageData(int addr, short data) {
        // TODO 将逻辑地址转为物理地址
        //    判读页在磁盘or内存
        //    根据情况进行修改页数据
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public int getFrameNo() {
        return frameNo;
    }

    public void setFrameNo(int frameNo) {
        this.frameNo = frameNo;
    }

    public int getBlockNo() {
        return blockNo;
    }

    public void setBlockNo(int blockNo) {
        this.blockNo = blockNo;
    }

    public boolean isStay() {
        return stay;
    }

    public void setStay(boolean stay) {
        this.stay = stay;
    }

    public boolean isModify() {
        return modify;
    }

    public void setModify(boolean modify) {
        this.modify = modify;
    }

    public byte[] getData() {
        return pageData;
    }

    public void setData(byte[] data) {
        this.pageData = data;
    }

    public int getR() {
        return R;
    }

    public void setR(int r) {
        R = r;
    }
}
