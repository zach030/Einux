package disk;

import static org.junit.jupiter.api.Assertions.*;

class DiskTest {

    @org.junit.jupiter.api.Test
    void readData() {
        BootDisk bootDisk = new Disk();
        bootDisk.setDiskHelper(10, 32, 64);
        bootDisk.loadDisk();
        // addr: 15位块号 + 9位块内偏移
        bootDisk.writeData(10, (short) 0X08);
        System.out.println(bootDisk.readData(10));
    }

    @org.junit.jupiter.api.Test
    void writeData() {
        int addr = 10;
        int blockNo = addr >> 9;
        int offset = addr & 0X01FF;
        System.out.println("data is:"+String.valueOf((short)0XF8));
        int row = offset / 15;    //33
        int column = offset % 15; //5
        int rowStart = row * 49;  //1617
        int columnStart = (column - 1) * 3; //12
        System.out.println("no is:" + blockNo + ", offset is:" + offset + ", seek is:" + (rowStart + columnStart));
    }
}