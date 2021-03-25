package os.filesystem;

import disk.BootDisk;
import disk.Disk;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SysFileSystemTest {

    @Test
    void write() {
    }

    @Test
    void read() {
        Disk.disk.loadDisk();
    }

    @Test
    void writeBlock() {
        Disk.disk.loadDisk();
        Block block = new Block(10);
        block.setData(new byte[]{0X01, 0X02, 0X03});
        FileSystem.fs.writeBlock(block);
    }
}