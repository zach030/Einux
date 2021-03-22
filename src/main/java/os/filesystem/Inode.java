package os.filesystem;

public class Inode {
    public static final int INODE_SIZE = 128;
    // inode大小 128B, inode区有64块，每块512B，最大存放256个inode
    int fileSize;        // 文件大小
    int createTime;      // 创建时间
    int fileAuthority;   // 文件权限
    int blockIndex;      // 所在数据块
    int filenameLength;  // 文件名大小
    int fileType;        // 文件类型
}
