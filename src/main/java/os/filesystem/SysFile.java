package os.filesystem;

public class SysFile {
    public int flag;
    public int count;
    public int offset;
    public Inode inode;

    public void setInode(Inode inode) {
        this.count++;
        this.inode = inode;
    }
}
