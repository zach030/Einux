package os.systemcall;

import hardware.CPU;
import os.device.BufferHead;
import os.device.DeviceManager;
import os.filesystem.DiskInode;
import os.filesystem.FileSystem;
import os.filesystem.MemoryInode;
import utils.Log;


public class SystemCall {
    public static SystemCall systemCall = new SystemCall();

    // 打开文件方式
    public static final int READ_ONLY = 1;
    public static final int WRITE_ONLY = 2;

    // 文件系统调用
    public FileSystemCall fileSystemCall = new FileSystemCall();

    public static class FileSystemCall {
        /**
         * @description: 创建文件
         * @author: zach
         **/
        public int create(String path, int mode) {
            MemoryInode memoryInode = FileSystem.fs.getInodeWithPath(path, mode);
            return FileSystem.fs.sysOpenFileManager.getSysFdByInodeNo(memoryInode.inodeNo);
        }

        /**
         * @description: 建立硬链接
         * @author: zach
         **/
        public boolean link(String linkPath, String filePath) {
            MemoryInode linkInode = FileSystem.fs.getInodeWithPath(linkPath, WRITE_ONLY);
            if (linkInode.fileType == DiskInode.FileType.DIR) {
                Log.Error("链接文件", String.format("链接文件:%s,失败,此文件是目录", linkPath));
                return false;
            }
            // 判断新文件是否已存在,failed
            MemoryInode newInode = FileSystem.fs.getInode(filePath);
            if (newInode != null) {
                Log.Error("链接文件", String.format("链接文件:%s,失败,此文件已存在", filePath));
                return false;
            }

            return true;
        }

        /**
         * @description: 删除文件系统调用
         * @author: zach
         **/
        public void unlink(String path) {

        }

        /**
         * @description: 进程打开文件系统调用
         * @author: zach
         **/
        public int open(String path, int mode) {
            MemoryInode inode = FileSystem.fs.getInodeWithPath(path, mode);
            if (!inode.canExec()) {
                // 文件不存在或者不允许存取
                Log.Error("打开文件失败", String.format("打开文件:%s，失败，无权限", path));
                return -1;
            }
            int sysFd = FileSystem.fs.getSysOpenFileManager().getSysFdByInodeNo(inode.inodeNo);
            Log.Info("打开文件", String.format("系统打开文件表下标为:%d", sysFd));
            for (BufferHead bh : inode.bufferHeads) {
                DeviceManager.dm.bufferOperator.writeBufferToDev(bh);
            }
            return CPU.cpu.getCurrent().addUserOpenFileTable(sysFd);
        }

        /**
         * @description: 进程关闭文件系统调用
         * @author: zach
         **/
        public void close(int fd) {
            // 根据用户打开文件id，找到系统打开文件表项
            int sysFd = CPU.cpu.getCurrent().getUserOpenFileTable(fd);
            // 修改用户打开文件表
            CPU.cpu.getCurrent().releaseUserOpenFileTable(fd);
            // 将此文件的引用值减一
            FileSystem.fs.sysOpenFileManager.freeOneOpenFileCount(sysFd);

        }

        /**
         * @description: 读文件系统调用
         * @author: zach
         **/
        public void read(int fd, int buffer, int count) {

        }

        /**
         * @description: 写文件系统调用
         * @author: zach
         **/
        public void write() {

        }

        /**
         * @description: 修改文件偏移
         * @author: zach
         **/
        public void lSeek(int fd, int offset, int whence) {
            // fd-->sysFd
            int sysFd = CPU.cpu.getCurrent().getUserOpenFileTable(fd);
            if (whence == 0)
                FileSystem.fs.sysOpenFileManager.getSysFileBySysFd(sysFd).offset = offset;
            else {
                FileSystem.fs.sysOpenFileManager.getSysFileBySysFd(sysFd).offset += offset;
            }
        }

        /**
         * @description: 修改文件权限
         * @author: zach
         **/
        public void chmod(String path, int mode) {
            MemoryInode memoryInode = FileSystem.fs.getInode(path);
            if (memoryInode == null) {
                Log.Error("修改文件权限失败", String.format("修改文件的权限失败,文件:%s不存在", path));
                return;
            }
            DiskInode diskInode = FileSystem.fs.inodeManager.getDiskInodeByNo(memoryInode.inodeNo);
            diskInode.setMode(mode);
        }
    }

}
