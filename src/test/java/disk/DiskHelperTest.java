package disk;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DiskHelperTest {

    @Test
    void updateFullBlock() {
        byte[] data = new byte[]{0x1F,0x12,0x03};
        System.out.println(Integer.toHexString(data[1]));
        System.out.println(Integer.toHexString(data[2]).length());
    }
}