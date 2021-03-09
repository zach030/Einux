package os.device;

import disk.Disk;
import org.junit.jupiter.api.Test;
import utils.SysConst;

class DeviceManagerTest {
    @Test
    void getBuffer() {
        Disk.disk.loadDisk();
        // 将数据写入缓冲区
        BufferHead bh = DeviceManager.dm.bufferOperator.writeDevToBuffer(SysConst.DEFAULT_DISK, 0);
        // 将缓冲区写回磁盘
        DeviceManager.dm.bufferOperator.writeBufferToDev(bh);
    }

}