package os.filesystem;

import org.junit.jupiter.api.Test;

class FileSystemTest {

    @Test
    void nameI() {
        MemoryInode memoryInode = FileSystem.fs.getInodeWithPath("/home/zach/go",1);
        System.out.println(memoryInode.inodeNo);
    }
}