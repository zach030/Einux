package os.filesystem;

public class SysFile {
    public int flag;
    public int count;
    public int offset;
    public int inodeNo;

    public void setInode(int inodeNo){
        this.inodeNo=inodeNo;
        this.count++;
    }

    public void init() {
        this.count = 0;
    }
}
