package os.storage;

import org.junit.jupiter.api.Test;
import os.job.JCB;
import os.process.Instruction;
import os.process.PCB;
import os.process.ProcessManage;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class StorageManageTest {
    JCB jcb = new JCB();
    @Test
    void getFreePageNumsInSwapArea() {
        System.out.println("current disk swap area blocks:" + StorageManage.sm.getFreePageNumsInSwapArea());
    }

    @Test
    void distributedPCBPageTable() {
        short[] data = {0x01, 0x01, 0x02, 0x04, 0x71, 0x2b, 0x5f, 0x30};
        jcb.setData(data);
        ArrayList<Instruction> instructions = new ArrayList<>(){};
        instructions.add(new Instruction(1,0,3,data[0]));
        jcb.setInstructions(instructions);
        PCB pcb = ProcessManage.pm.createPCB(jcb);
        StorageManage.sm.allocPCBPageTable(pcb);
    }
}