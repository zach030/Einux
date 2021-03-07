package os.process;

// 页表
public class PageTableEntry {
    private int virtualPageNo;       //逻辑页号
    private int physicPageNo;        //页框号
    private int diskBlockNo;         //外存块号
    private boolean isValid;         //是否有效
    private boolean isModify;        //是否修改

    public int pteDataToWord() {
        int data = 0;
        // 7 逻辑页号  6 物理页框号  15 物理块号  1 有效位   1 修改位   2 未用
        data |= this.getVirtualPageNo() << 25 & 0XFE000000;
        data |= this.getPhysicPageNo() << 19 & 0X01F80000;
        data |= this.getDiskBlockNo() << 4 & 0X0007FFF0;
        int valid = this.isValid() ? 1 : 0;
        data |= valid << 3 & 0X00000008;
        int modify = this.isModify() ? 1 : 0;
        data |= modify << 2 & 0X00000004;
        return data;
    }

    public int getVirtualPageNo() {
        return virtualPageNo;
    }

    public void setVirtualPageNo(int virtualPageNo) {
        this.virtualPageNo = virtualPageNo;
    }

    public int getPhysicPageNo() {
        return physicPageNo;
    }

    public void setPhysicPageNo(int physicPageNo) {
        this.physicPageNo = physicPageNo;
    }

    public int getDiskBlockNo() {
        return diskBlockNo;
    }

    public void setDiskBlockNo(int diskBlockNo) {
        this.diskBlockNo = diskBlockNo;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setValid(boolean valid) {
        isValid = valid;
    }

    public boolean isModify() {
        return isModify;
    }

    public void setModify(boolean modify) {
        isModify = modify;
    }
}
