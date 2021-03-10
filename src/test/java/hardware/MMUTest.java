package hardware;

import hardware.memory.Page;
import org.junit.jupiter.api.Test;
import os.job.JCB;
import os.process.PCB;

class MMUTest {

    @Test
    void searchPageTable() {
        JCB jcb = new JCB();
        PCB pcb = new PCB();
        Page page = new Page();
        page.setLogicalNo(2);
        page.setFrameNo(10);
        page.setBlockNo(19900);
        page.setModify(true);
        pcb.createProcess(jcb);
        pcb.writePageTableEntry(0, page);
        int frameNo = MMU.mmu.searchPageTable(0);
        System.out.println(frameNo);
    }
}