package hardware.memory;

import org.junit.jupiter.api.Test;

class MemoryTest {

    @Test
    void writeWordData() {
        Memory.memory.writeData((short)1700,(short) 12);
        //Memory.memory.writeWordData((short) 512,72670152);
    }
}