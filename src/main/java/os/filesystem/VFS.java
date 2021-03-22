package os.filesystem;

public interface VFS {
    // 创建文件
    void createFile();
    // 打开文件
    void openFile();
    // 读取文件
    void readFile();
    // 删除文件
    void deleteFile();
}
