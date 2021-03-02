package hardware;

import hardware.mm.Memory;
import org.junit.jupiter.api.Test;

class MemoryTest {
    Memory memory = new Memory();
    @Test
    void clearMemory() {
    }

    @Test
    void writeData() {
        // pa:011110 011111000   page:30   offset:248
        memory.writeData((short)(15608),(short) (20));

        short data = memory.readData((short)(15608));
        System.out.println(data);
    }

    @Test
    void readData() {

    }
}