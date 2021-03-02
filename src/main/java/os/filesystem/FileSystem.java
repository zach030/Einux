package os.filesystem;

public class FileSystem {
    public static FileSystem fs = new FileSystem();

    public static final int INODE_NUM = 64;                     // inode节点数
    public static final int BLOCK_NUM = 19898;                  // 数据块总块数
    public static final int SUPER_BLOCK_INDEX = 1;              // 超级块在磁盘中的下标
    public static final int INODE_MAP_INDEX = 2;                // Inode map在磁盘中的下标
    public static final int BLOCK_MAP_INDEX = 3;                // block map在磁盘中的下标
    public static final int[] INODE_BLOCK_RANGE = {4, 67};      // inode区范围
    public static final int[] DATA_BLOCK_RANGE = {68, 19965};   // 数据区范围
    public static final int[] JCB_BLOCK_RANGE = {19966, 20223}; // jcb存储区范围
    public static final int[] SWAP_BLOCK_RANGE = {20224, 20479};// 交换区范围
}
