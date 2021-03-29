package os.filesystem;

// 目录
public class Directory {
    String name;
    int inodeNo;

    public Directory(String name, int no) {
        this.name = name;
        this.inodeNo = no;
    }
}
