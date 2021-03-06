package disk;

import java.util.HashMap;
import java.util.Map;

public class DevConfig {
    //--------disk---------------------
    // 磁盘块数
    public static final int DISK_BLOCK_NUM = 10 * 32 * 64;
    // ------各分区盘块初始下标
    public static final int BOOT_BLOCK_INDEX = 0;
    public static final int SUPER_BLOCK_INDEX = 1;
    public static final int INODE_ZONE_INDEX = 2;
    public static final int DATA_ZONE_INDEX = 66;
    public static final int JCB_ZONE_INDEX = 19966;
    public static final int SWAP_ZONE_INDEX = 20224;
    // 初始化磁盘map
    public final static Map<Integer, String> INIT_BLOCK_MAP = new HashMap<>() {
        {
            put(BOOT_BLOCK_INDEX, BIOS_BLOCK);
            put(SUPER_BLOCK_INDEX, SUPER_BLOCK);
            put(INODE_ZONE_INDEX, NORMAL_BLOCK);
            put(DATA_ZONE_INDEX, NORMAL_BLOCK);
            put(JCB_ZONE_INDEX, NORMAL_BLOCK);
            put(SWAP_ZONE_INDEX, NORMAL_BLOCK);
        }
    };
    // 每个块里有多少inode
    public static final int INODES_PER_BLOCK = 2;
    // 柱面数目
    public static final int CYLINDER_NUM = 10;
    // 磁道数目
    public static final int TRACK_NUM = 32;
    // 扇区数目
    public static final int SECTOR_NUM = 64;
    // 物理块大小
    public static final int BLOCK_SIZE = 512;
    // end flag
    public static final int END_FLAG = -1;

    // BIOS块数据
    public static final String BIOS_BLOCK =
            "B8 30 00 BA C0 00 01 D0 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 55 AA\n";

    // 超级块数据
    // TODO 超级块数据需要设计，放哪些数据
    public static final String SUPER_BLOCK =
            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n";
    // 普通块数据
    public static final String NORMAL_BLOCK =
            "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n" +
                    "00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00\n";
}
