package os.systemcall;

import os.filesystem.FileSystem;
import os.filesystem.Inode;


public class SystemCall {
    // 生成目录
    public boolean genDir(String path, int mode, int devNo){
        Inode inode = FileSystem
        return true;
    }
    // 创建文件
    public void create(String path, int mode){

    }
    // 删除文件
    public void unlink(String path){

    }
    // 打开文件
    public void open(String path, int mode){

    }
    // 关闭文件
    public void close(int fd){

    }
}
