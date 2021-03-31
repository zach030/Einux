package os.filesystem;

import hardware.disk.*;
import hardware.memory.Memory;
import os.device.BufferHead;
import os.device.DeviceManager;
import utils.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 文件系统中三个标识：inodeNo, sysFd, fd
 * inodeNo：inode的物理编号，是diskInodeList中的下标
 * sysFd:   inode在系统打开文件表中的下标
 * fd:      inode在用户打开文件表中的下标
 * @author: zach
 **/
public class FileSystem implements VFS {
    public static FileSystem fs = new FileSystem();

    //--------------成员-------------------
    static disk.BootDisk currentBootDisk;
    HashMap<Integer, disk.BootDisk> bootDiskMap = new HashMap<>();

    public static class FileTree {
        int parent;
        int son;

        FileTree(int parent, int son) {
            this.parent = parent;
            this.son = son;
        }
    }

    HashMap<FileTree, String> allFileMap = new HashMap<>();
    public BootDiskManager bootDiskManager;
    public InodeManager inodeManager;
    public SysOpenFileManager sysOpenFileManager;

    //--------------文件系统常量------------------------
    public static final int ROOT_INODE_NO = 0;
    public static final int HOME_INODE_NO = 1;
    public static final int DEV_INODE_NO = 2;
    public static final int ETC_INODE_NO = 3;
    public static final int MEM_INODE_MAX_NUM = 32;

    //--------------基本inode--------------------
    MemoryInode root;   // 根目录
    MemoryInode pwd;    // 当前目录

    /**
     * @description: 管理当前系统的启动盘
     * @author: zach
     **/
    class BootDiskManager {
        /**
         * @description: 搜索系统当前挂载的启动盘
         * @author: zach
         **/
        public disk.BootDisk getBootDisk(int devNo) {
            for (Map.Entry<Integer, disk.BootDisk> map : bootDiskMap.entrySet()) {
                if (map.getKey().equals(devNo)) {
                    return map.getValue();
                }
            }
            return null;
        }
    }

    /**
     * @description: 管理系统中的inode
     * @author: zach
     **/
    public static class InodeManager {
        public InodeManager() {
            loadDiskInodeList();
        }

        // 存放当前内存活动inode列表, 容量是内存缓冲区中可以存放最多的inode数目256个
        private ArrayList<MemoryInode> activeInodeList = new ArrayList<>();
        // 存放磁盘中所有的inode（与超级块中的位示图保持一致）
        private DiskInode[] diskInodeList = new DiskInode[BootDisk.DISK_MAX_INODE_NUM];

        /**
         * @description: 从磁盘加载全部inode
         * @author: zach
         **/
        private void loadDiskInodeList() {
            //todo 初始化root,home,dev,etc
            DiskInode root = new DiskInode(ROOT_INODE_NO, BootDisk.DATA_ZONE_INDEX);
            joinDiskInodeList(root);
        }

        /**
         * @description: 将inode加入内存活动inode列表
         * @author: zach
         **/
        public void joinActiveInodeList(MemoryInode inode) {
            this.activeInodeList.add(inode);
        }

        /**
         * @description: lru淘汰最久未用内存活动inode
         * @author: zach
         **/
        public void freeOneActiveInode() {
            int oldest = this.activeInodeList.get(0).lastUpdateTime;
            int index = 0;
            for (int i = 0; i < this.activeInodeList.size(); i++) {
                if (this.activeInodeList.get(i).lastUpdateTime < oldest) {
                    oldest = this.activeInodeList.get(i).lastUpdateTime;
                    index = i;
                }
            }
            // 放回磁盘
            MemoryInode failInode = activeInodeList.get(index);
            this.inodeInDisk(failInode);
            this.activeInodeList.remove(index);
            Log.Info("淘汰内存inode", String.format("已淘汰内存inode:%d", index));
        }

        /**
         * @description: 加入磁盘inode列表
         * @author: zach
         **/
        public void joinDiskInodeList(DiskInode diskInode) {
            this.diskInodeList[diskInode.inodeNo] = diskInode;
            // todo 将inode写入磁盘
            diskInode.syncToDisk();
        }

        /**
         * @description: 判断内存活动inode区是否已满
         * @author: zach
         **/
        public boolean isActiveInodeFull() {
            return this.activeInodeList.size() == MEM_INODE_MAX_NUM;
        }

        /**
         * @description: 根据inodeNo查找内存中的活动inode
         * @author: zach
         **/
        public MemoryInode getActiveInodeByNo(int devNo, int inodeNo) {
            for (MemoryInode memoryInode : this.activeInodeList) {
                // 先查内存活动inode，如果找到，返回inode
                if (memoryInode.devNo == devNo && memoryInode.inodeNo == inodeNo) {
                    memoryInode.referenceCount++;
                    return memoryInode;
                }
            }
            return null;
        }

        /**
         * @description: 根据inodeNo查找磁盘中的inode
         * @author: zach
         **/
        public DiskInode getDiskInodeByNo(int inodeNo) {
            return this.diskInodeList[inodeNo];
        }

        /**
         * @description: 将inode从内存中移除，放回磁盘
         * @author: zach
         **/
        public void inodeInDisk(MemoryInode memoryInode) {

        }

        /**
         * @description: 将磁盘中的inode加入内存
         * @author: zach
         **/
        public void inodeInMemory(MemoryInode memoryInode) {
            if (this.isActiveInodeFull()) {
                Log.Error("内存inode区已满", "当前内存inode区已满,需要释放inode");
                this.freeOneActiveInode();
            }
            Log.Info("加入内存活动inode列表", String.format("正在将inode:%d，加入内存活动列表", memoryInode.inodeNo));
            this.joinActiveInodeList(memoryInode);
            //todo 写入内存
            for (int blockNo : memoryInode.blockNoList) {
                BufferHead bh = DeviceManager.dm.bufferOperator.writeDevToBuffer(currentBootDisk.getBootDiskNo(), blockNo);
                if (bh == null) {
                    Log.Error("将inode写入内存缓冲区", String.format("将inode:%d,写入内存缓冲区失败，无可用缓冲区，进程已被阻塞", memoryInode.inodeNo));
                    return;
                }
                Log.Info("写入内存缓冲区", String.format("正在将BufferHeader对应的磁盘号:%d,写入内存缓冲区页号:%d,物理页框号:%d内",
                        bh.getBlockNo(), bh.getBufferNo(), bh.getFrameNo()));
                memoryInode.syncToMemory(bh);
                //todo 何时释放缓冲区？这里直接释放对吗？仿真文件数据被读取了
                DeviceManager.dm.bufferOperator.freeBuffer(bh);
            }
        }
    }

    /**
     * @description: 管理当前系统的打开文件表
     * @author: zach
     **/
    public class SysOpenFileManager {
        SysFile[] sysOpenFileTable = new SysFile[Memory.memory.getBufferPool().getActiveInodeNum()];

        public SysOpenFileManager() {
            initSysFileOpenTable();
        }

        /**
         * @description: 初始化系统打开文件表
         * @author: zach
         **/
        void initSysFileOpenTable() {
            for (int i = 0; i < sysOpenFileTable.length; i++) {
                SysFile sysFile = new SysFile();
                sysFile.init();
                sysOpenFileTable[i] = sysFile;
            }
        }

        public SysFile[] getSysOpenFileTable() {
            return sysOpenFileTable;
        }

        /**
         * @description: 分配系统打开文件表项
         * @author: zach
         **/
        public int allocSysFileOpenTable(int inodeNo) {
            for (int i = 0; i < sysOpenFileTable.length; i++) {
                if (sysOpenFileTable[i].count == 0) {
                    Log.Info("正在分配系统打开文件表", String.format("为inode:%d，寻找到系统打开文件表下标:%d", inodeNo, i));
                    sysOpenFileTable[i].setInode(inodeNo);
                    return i;
                }
            }
            return -1;
        }

        /**
         * @description: 根据inodeNo查找系统打开文件表，返回下标sysFd
         * @author: zach
         **/
        public int getSysFdByInodeNo(int inodeNo) {
            for (int i = 0; i < sysOpenFileTable.length; i++) {
                if (sysOpenFileTable[i].inodeNo == inodeNo) {
                    return i;
                }
            }
            return -1;
        }

        /**
         * @description: 根据系统打开文件表下标, 返回打开文件sysFile
         * @author: zach
         **/
        public SysFile getSysFileBySysFd(int sysFd) {
            return sysOpenFileTable[sysFd];
        }

        /**
         * @description: 减少对系统打开文件表中一个文件的引用计数
         * @author: zach
         **/
        public void freeOneOpenFileCount(int sysFd) {
            this.sysOpenFileTable[sysFd].count--;
        }

        /**
         * @description: 根据系统打开文件表下标，返回inode
         * @author: zach
         **/
        public MemoryInode getMemoryInodeBySysFd(int sysFd) {
            int inodeNo = sysOpenFileTable[sysFd].inodeNo;
            sysOpenFileTable[sysFd].count++;
            return inodeManager.getActiveInodeByNo(currentBootDisk.getBootDiskNo(), inodeNo);
        }
    }

    /**
     * @description: 文件系统初始化工作：
     * 加载启动盘
     * 初始化inode管理与打开文件表管理
     * 初始化root节点
     * @author: zach
     **/
    public FileSystem() {
        // 挂载启动盘
        BootDisk bootDisk = BootDisk.bootDisk;
        bootDiskMap.put(BootDisk.DEVICE_NO, bootDisk);
        Log.Info("启动程序", String.format("检测设备，发现块号为:%d的启动盘，将其挂载到系统", BootDisk.DEVICE_NO));
        bootDiskManager = new BootDiskManager();
        bootLoader();
    }

    /**
     * @description: 启动盘启动程序
     * @author: zach
     **/
    public void bootLoader() {
        // 获取当前使用的磁盘
        currentBootDisk = bootDiskManager.getBootDisk(BootDisk.DEVICE_NO);
        Log.Info("挂载磁盘", String.format("检测到当前系统存在设备号为:%d的启动盘，正在装载...", BootDisk.DEVICE_NO));
        // 初始化文件系统
        Log.Info("加载磁盘数据", String.format("正在加载设备号：%d的磁盘数据", BootDisk.DEVICE_NO));
        currentBootDisk.initBootDisk();
        initFileSystem();
    }

    public void start() {
        Log.Info("文件系统启动", "系统已启动，正在初始化文件系统");
    }

    //----------初始化文件系统---------------
    private void initFileSystem() {
        inodeManager = new InodeManager();
        sysOpenFileManager = new SysOpenFileManager();
        initRootDir();
    }

    /**
     * @description: 初始化root节点，root节点应该文件系统初始化的时候就建好
     * @author: zach
     **/
    void initRootDir() {
        Log.Info("文件系统初始化", "正在初始化根节点root");
        root = allocInode(getFreeInodeNo(), getFreeDataBlockNo());
        // 设置root属性
        root.setFileType(DiskInode.FileType.DIR);
        root.setAuthority(MemoryInode.Authority.READ, MemoryInode.Authority.WRITE, MemoryInode.Authority.EXEC);
        pwd = root;
        allFileMap.put(new FileTree(-1, ROOT_INODE_NO), "/");
        initBaseDir();
    }

    /**
     * @description: 初始化基本的文件
     * @author: zach
     **/
    void initBaseDir() {
        Log.Info("文件系统初始化", "正在初始化home文件");
        MemoryInode home = addNewFileToDir(root, "home");
        Log.Info("文件系统初始化", "正在初始化dev文件");
        MemoryInode dev = addNewFileToDir(root, "dev");
        Log.Info("文件系统初始化", "正在初始化etc文件");
        MemoryInode etc = addNewFileToDir(root, "etc");
        addNewFileToDir(home, "zach");
        addNewFileToDir(dev, "block");
        addNewFileToDir(dev, "tty");
        addNewFileToDir(etc, "ssh");
    }

    //--------------------文件系统API------------------
    // 先是从文件全名映射到 inode number ( map<string, int32_t>)，再从 inode number 映射到文件内容(map<int32_t, string>)

    /**
     * @description: 根据文件路径得到文件inode
     * 1.查找父级目录 dirNameI()
     * 2.父级目录搜索 findEntry()
     * 2.1 如果不存在，申请一个inode
     * 2.2 调用addEntry()，加入目录
     * 3.返回文件inodeNo
     * 4.遍历
     * @author: zach
     **/
    public MemoryInode getInodeWithPath(String path, int mode) {
        // 第一步是dir_namei（），找到文件所在的目录的inode，
        // 第二步是find_entry（），即在dir目录中，找到文件对应的目录项，并返回目录项的指针de，
        // 如果要找的文件对应的目录项不存在，就申请一个节点，
        // 并通过add_entry()将节点对应的res_dir目录项加入目录dir
        // iget（）
        MemoryInode node = getFileParentDir(path);
        if (node == root) {
            return root;
        }
        int sysFd = -1;
        if (node != null) {
            sysFd = node.findEntry(getFileNameWithPath(path));
        }
        if (sysFd == -1) {
            //todo 文件还不存在,创建一个新的文件
            MemoryInode memoryInode = addNewFileToDir(node, getFileNameWithPath(path));
            sysFd = node.findEntry(getFileNameWithPath(path));
        }
        return sysOpenFileManager.getMemoryInodeBySysFd(sysFd);
    }

    /**
     * @description: 返回内存活动inode
     * @author: zach
     **/
    public MemoryInode getInode(String path) {
        MemoryInode memoryInode = getFileParentDir(path);
        int sysFd = memoryInode.findEntry(getFileNameWithPath(path));
        return sysOpenFileManager.getMemoryInodeBySysFd(sysFd);
    }

    /**
     * @description: 根据文件路径找出文件名
     * @author: zach
     **/
    private String getFileNameWithPath(String path) {
        String[] files = path.split("/");
        return files[files.length - 1];
    }

    /**
     * @description: 根据文件路径找出文件所在父级目录
     * example: /home/go/main.go --> home,go ,返回go对应的inode
     * @author: zach
     **///todo bad code
    MemoryInode getFileParentDir(String path) {
        if (path.equals("/")) {
            return root;
        }
        if (path.charAt(0) == '/') {
            pwd = root;
        }
        String[] paths = path.split("/");
        for (int i = 0; i < paths.length - 2; i++) {
            if (!pwd.canAccess()) {
                Log.Error("进入文件失败", String.format("对于文件:%s，无访问权限", paths[i]));
                return null;
            }
            // 获得下一级目录名
            String fileName = paths[i + 1];
            // 通过查询目录项,找到inodeNo
            int inodeNo = pwd.findEntry(fileName);
            // 根据inodeNo查询内存活动列表,返回内存inode
            MemoryInode memoryInode = inodeManager.getActiveInodeByNo(currentBootDisk.getBootDiskNo(), inodeNo);
            if (memoryInode == null) {
                // exist in disk
                memoryInode = addNewFileToDir(pwd, fileName);
            }
            pwd = memoryInode;
        }
        return pwd;
    }

    /**
     * @description: 在当前目录下创建一个新的文件
     * 从超级块中分配空闲inodeNo
     * 加入磁盘inode列表
     * 从磁盘inode列表中找出diskInode
     * 放入内存，得到memoryInode
     * @author: zach
     **/
    public MemoryInode addNewFileToDir(MemoryInode dir, String name) {
        // 申请磁盘空闲inode
        int inodeNo = getFreeInodeNo();
        DiskInode diskInode = newDiskInode(inodeNo);
        // 得到内存活动inode
        MemoryInode memoryInode = allocInode(inodeNo, getFreeDataBlockNo());
        // 加入子目录
        dir.addDirEntry(name, inodeNo);
        // 更新统计map
        allFileMap.put(new FileTree(dir.inodeNo, inodeNo), name);
        return memoryInode;
    }

    //-------------------------分配磁盘与内存inode---------

    /**
     * @description: 已知inodeNo时, 根据inodeNo，获取磁盘inode，加入内存，分配打开文件表
     * @author: zach
     **/
    private MemoryInode allocInode(int inodeNo, int blockNo) {
        DiskInode diskInode = inodeManager.getDiskInodeByNo(inodeNo);
        MemoryInode memoryInode = new MemoryInode(diskInode.inodeNo, blockNo);
        inodeManager.inodeInMemory(memoryInode);
        sysOpenFileManager.allocSysFileOpenTable(inodeNo);
        return memoryInode;
    }

    /**
     * @description: 创建磁盘新文件
     * @author: zach
     **/
    public DiskInode newDiskInode(int inodeNo) {
        int blockNo = currentBootDisk.getSuperBlock().getFreeDataBlock();
        DiskInode diskInode = new DiskInode(inodeNo, blockNo);
        inodeManager.joinDiskInodeList(diskInode);
        return diskInode;
    }

    /**
     * @description: 获得当前系统中的空闲inodeNo
     * @author: zach
     **/
    public int getFreeInodeNo() {
        int inodeNo = currentBootDisk.getSuperBlock().getFreeInode();
        Log.Info("分配空闲inode块", String.format("当前系统正在请求分配空闲inode块:%d", inodeNo));
        return inodeNo;
    }

    /**
     * @description: 获得当前系统空闲数据块
     * @author: zach
     **/
    public int getFreeDataBlockNo() {
        int blockNo = currentBootDisk.getSuperBlock().getFreeDataBlock();
        Log.Info("分配空闲数据块", String.format("当前系统正在请求分配空闲数据块:%d", blockNo));
        return blockNo;
    }

    /**
     * @description: 释放内存inode
     * @author: zach
     **/
    public void freeInode(int inodeNo) {
        currentBootDisk.getSuperBlock().addFreeInode(inodeNo);
    }

    //--------------文件系统存储API--------------------

    public HashMap<FileTree, String> getAllFileMap() {
        return allFileMap;
    }

    public void setAllFileMap(HashMap<FileTree, String> allFileMap) {
        this.allFileMap = allFileMap;
    }

    public static disk.BootDisk getCurrentBootDisk() {
        return currentBootDisk;
    }

    public InodeManager getInodeManager() {
        return inodeManager;
    }

    public SysOpenFileManager getSysOpenFileManager() {
        return sysOpenFileManager;
    }
}

