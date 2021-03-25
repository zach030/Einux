package os.filesystem;

import hardware.CPU;
import utils.SysConst;

import java.util.ArrayList;

public class Inode {
    public static final int INODE_SIZE = 32;

    public enum FileType {
        REG, DIR, CHAR, BLK, FIFO, LINK, SOCKET
    }

    public enum Authority {
        READ, WRITE, EXEC
    }

    // inode大小 32B, inode区有64块，每块512B，最大存放1024个inode
    public Inode() {
        this.directories = new ArrayList<>();
        this.dataBlocks = new ArrayList<>();
        this.hardLink = 1;
        this.devNo = SysConst.DEFAULT_DISK;
        this.createTime = CPU.cpu.clock.getCurrentTime();
    }

    int inodeNo;
    int devNo;
    ArrayList<Integer> dataBlocks;
    ArrayList<Directory> directories;   // 目录项
    int referCnt;                       // 引用数
    int hardLink;                       // 硬链接
    int fileSize;                       // 文件大小
    int createTime;                     // 创建时间
    int updateTime;                     // 更新时间
    int authority;       // 文件权限
    int blockIndex;      // 所在数据块
    FileType fileType;        // 文件类型

    public FileType getFileType() {
        return fileType;
    }

    public void setFileType(FileType fileType) {
        this.fileType = fileType;
    }

    public void setAuthority(Authority... authority) {
        int auth = 0;
        for (Authority a : authority) {
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

}
