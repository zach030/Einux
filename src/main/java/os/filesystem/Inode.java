package os.filesystem;

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
    }

    int inodeNo;
    int devNo;
    ArrayList<Integer> dataBlocks;
    ArrayList<Directory> directories;   // 目录项
    int referCnt;                       // 引用数
    int fileSize;                       // 文件大小
    int createTime;                     // 创建时间
    int updateTime;                     // 更新时间
    int authority;       // 文件权限
    int blockIndex;      // 所在数据块
    int filenameLength;  // 文件名大小
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
}
