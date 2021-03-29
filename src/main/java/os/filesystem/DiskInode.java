package os.filesystem;

import java.util.ArrayList;

public class DiskInode {
    public int inodeNo;             // inode编号
    public int userID;              // 用户ID;
    public int groupID;             // 用户组ID
    public int hardLinkNum;         // 硬连接数，即有多少文件名指向这个inode
    public int fileSize;            // 文件大小
    public int flag;                // 标志位
    public int mode;                // 文件访问权限
    public int createdTime;         // 创建时间
    public int lastUpdateTime;      // 最后更新时间
    public int authority;           // 文件权限
    public boolean access;
    public FileType fileType;        // 文件类型
    public ArrayList<Integer> blockNoList;   // 使用的存储区域的块编号，​表示某个文件使用着存储区域的哪一个块。
    public ArrayList<Directory> dirEntry; // 目录项

    public DiskInode(int inodeNo){
        this.inodeNo = inodeNo;
        dirEntry = new ArrayList<>();
        blockNoList = new ArrayList<>();
    }
    public enum FileType {
        REG, DIR, CHAR, BLK, FIFO, LINK, SOCKET
    }

    public enum Authority {
        READ, WRITE, EXEC
    }

    public void setAuthority(Inode.Authority... authority) {
        int auth = 0;
        for (Inode.Authority a : authority) {
            switch (a) {
                case READ:
                    auth += 4;
                    break;
                case WRITE:
                    auth += 2;
                    break;
                case EXEC:
                    auth += 1;
                    break;
            }
        }
        this.authority = auth;
    }

    public boolean canRead() {
        return this.authority == 4 || this.authority == 5 || this.authority == 6 || this.authority == 7;
    }

    public boolean canWrite() {
        //todo 可写就一定可读? right?
        return this.authority == 6 || this.authority == 7;
    }

    public boolean canExec() {
        return this.authority == 1 || this.authority == 7 || this.authority == 6 || this.authority == 3;
    }

    public boolean canAccess() {
        return access;
    }

    /**
     * @description: 将inode写入磁盘
     * @author: zach
     **/
    //todo 将inode写入磁盘
    public void syncToDisk() {

    }
    /**
        * @description: 将inode写入内存
        * @author: zach
     **/
    public void syncToMemory(){

    }

    public int getAuthority() {
        return authority;
    }

    public void setAuthority(int authority) {
        this.authority = authority;
    }

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }
    /**
        * @description: 添加目录项
        * @author: zach
     **/
    public void addDirEntry(String name, int inodeNo){
        Directory d = new Directory(name,inodeNo);
        this.dirEntry.add(d);
    }
}
