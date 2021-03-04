package hardware;

import hardware.memory.Page;
import org.junit.jupiter.api.Test;
import os.process.PCB;

class MMUTest {

    @Test
    void searchPageTable() {
        PCB pcb = new PCB();
        Page page = new Page();
        page.setLogicalNo(2);
        page.setFrameNo(10);
        page.setBlockNo(19900);
        page.setModify(true);
        pcb.writePageTableEntry(0, page);
        int frameNo = MMU.mmu.searchPageTable(0);
        System.out.println(frameNo);
    }
}