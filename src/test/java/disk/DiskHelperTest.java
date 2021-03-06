package disk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiskHelperTest {

    @Test
    void updateFullBlock() {
//        byte[] data = new byte[]{0x1F,0x12,0x03};
//        System.out.println(Integer.toHexString(data[1]));
//        System.out.println(Integer.toHexString(data[2]).length());
//        Disk.disk.createDisk();
        String num = Integer.toHexString(((byte) ( (2 & 0X00FF) & 0XFF) & 0x000000FF) | 0xFFFFFF00).substring(6);
        System.out.println(num);
    }
}