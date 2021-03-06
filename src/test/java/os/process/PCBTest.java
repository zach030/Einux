package os.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PCBTest {

    @Test
    void writePCBPage() {
        PCB pcb = new PCB();
        pcb.setPcbFramePageNo(4);
        pcb.setID(5);
        pcb.setPriority(3);
        pcb.setInTimes(10);
        pcb.writePCBPage();
    }
}