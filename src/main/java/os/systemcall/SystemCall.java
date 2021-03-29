package os.systemcall;

import hardware.CPU;
import os.filesystem.FileSystem;
import os.filesystem.Inode;
import os.filesystem.MemoryInode;
import utils.Log;


public class SystemCall {
    public static SystemCall systemCall = new SystemCall();

    // 文件系统调用
    FileSystemCall fileSystemCall = new FileSystemCall();

    // todo 对文件权限欠考虑
    static class FileSystemCall {
        // 生成目录
        public boolean genDir(String path, int mode, int devNo) {
//            Inode inode = FileSystem.fs.allocInode(devNo);
            return true;
        }

        // 创建文件
        public void create(String path, int mode) {
            MemoryInode memoryInode = FileSystem.fs.nameI(path, mode);
            if (memoryInode != null) {
                // 如果文件已存在

            }
            //{
            // 取对应文件名的索引节点（算法namei）；
            // if（文件已经存在）
            // {
            //  if（不允许访问）
            //  {
            //   释放索引节点（iput）；
            //   return（错）；
            //  }
            // }
            // else //文件还不存在
            // {
            //  从文件系统中分配一个空闲索引节点（算法ialloc）；
            //  在父目录中建立新目录项：包括新文件名和新分配的索引节点号；
            // }
            // 为索引节点分配文件表项，初始化引用数；
            // if（文件在创建时已存在）
            //  释放所有文件块（算法free）；
            // 解锁（索引节点）；
            // return（用户文件描述符）；
            //}
        }

        // 删除文件
        public void unlink(String path) {

        }

        // 	fd = open(pathname,flags,modes);
        //	//pathname是文件名；
        //	//flags指示打开的类型（如读或写）；
        //	//modes给出文件的许可权（如果文件正在被建立）？？？？？？
        //	//系统调用open返回一个称为文件描述符的整数。进程用文件描述符操作文件
        public int open(String path, int mode) {
            MemoryInode inode = FileSystem.fs.nameI(path, mode);
            if(inode==null){
                //todo create file

            }
            if (!inode.canExec()) {
                // 文件不存在或者不允许存取
                Log.Error("打开文件失败",String.format("打开文件:%s，失败，无权限",path));
                return -1;
            }
            int sysFd = FileSystem.fs.getSysOpenFileManager().getSysFdByInodeNo(inode.inodeNo);
            return CPU.cpu.getCurrent().addUserOpenFileTable(sysFd);
        }

        // 关闭文件
        public void close(int fd) {
            // 根据用户打开文件id，找到系统打开文件表项
            int sysFd = CPU.cpu.getCurrent().getUserOpenFileTable(fd);
            // 修改用户打开文件表
            CPU.cpu.getCurrent().releaseUserOpenFileTable(fd);
            // 将此文件的引用值减一
            //FileSystem.fs.freeOneOpenFileCount(sysFd);
        }

        // 读文件
        public void read(int fd, int buffer, int count) {

        }
    }

}
