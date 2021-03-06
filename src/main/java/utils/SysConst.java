package utils;

public class SysConst {

    //-----中断号-----------
    public static final int BIOS_13 = 13;
    //------init进程信息------------
    public static final int INIT_PRIO = 0;
    public static final int INIT_PID = 0;
    // 根目录inode编号
    public static final int ROOT_INODE_INDEX = 1;
    //-------page----------------------
    // 页帧大小
    public static final int PAGE_FRAME_SIZE = 512;
    // 页数目
    public static final int PAGE_NUM = 64;
    // 引用位--引用
    public static final int PAGE_REFER = 1;
    // 引用位--清除
    public static final int PAGE_CLEAR = 0;
    //-------tlb------------------------
    // TLB命中
    public static final int TLB_HIT = 1;
    // TLB未命中
    public static final int TLB_MISS = 0;
    //------读写权限--------------
    // 只读
    public static final int R = 0;
    // 可读可写
    public static final int RW = 1;

    //---------- 文件系统超级块数据------
    // inode位示图大小
    public static final int INODE_MAP_SIZE = 10;
    // bitmap位示图大小
    public static final int BIT_MAP_SIZE = 10;
    // -------磁盘设备号----------------
    public static final int DEFAULT_DISK = 1;
    //TODO 可添加其他启动盘

    // ----仿真汇编指令------
    public static final String BIOS_ASM_CODE = "jmp far f000:e05b";

    // -----root用户信息
    public static final int ROOT_ID = 1;
    public static final int ROOT_GROUP_ID =1;
}
