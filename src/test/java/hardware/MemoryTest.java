package hardware;

import hardware.memory.Memory;
import org.junit.jupiter.api.Test;

class MemoryTest {
    Memory memory = new Memory();

    @Test
    void clearMemory() {
        Memory.memory.writeWordData((short) 512, 72670152);
        int data = Memory.memory.readWordData((short) 512);
        System.out.println(data);
    }

    @Test
    void writeData() {
        // pa:011110 011111000   page:30   offset:248
        memory.writeData((short) (15608), (short) (20));

        short data = memory.readData((short) (15608));
        System.out.println(data);
    }

    @Test
    void readData() {
        int data = 72670152;
        // 0000010001010100  1101101111001000

        short lowData = (short) (data & 0X0000FFFF);
        short highData = (short) (data >> 16 & 0X0000FFFF);

        // 11001000
        // 10101000000000000000000
        int low = ((int) lowData) & 0X0000FFFF;
        int high = ((int) highData << 16) & 0XFFFF0000;
        System.out.println(low | high);
    }
}