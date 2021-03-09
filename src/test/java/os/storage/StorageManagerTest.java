package os.storage;

import org.junit.jupiter.api.Test;
import os.job.JCB;
import os.process.Instruction;
import os.process.PCB;
import os.process.ProcessManager;

import java.util.ArrayList;

class StorageManagerTest {
    JCB jcb = new JCB();
    @Test
    void getFreePageNumsInSwapArea() {
        //2, 0, 3, 0, 1, 3, 1, 2, 0, 1
        StorageManager.sm.allotManager.lruCache.put(1,2);
        StorageManager.sm.allotManager.lruCache.put(2,0);
        StorageManager.sm.allotManager.lruCache.put(3,3);
        StorageManager.sm.allotManager.lruCache.put(4,0);
        StorageManager.sm.allotManager.lruCache.put(5,1);
        StorageManager.sm.allotManager.lruCache.put(6,3);
        StorageManager.sm.allotManager.lruCache.get(2);
        StorageManager.sm.allotManager.lruCache.get(0);
        StorageManager.sm.allotManager.lruCache.get(3);
        StorageManager.sm.allotManager.lruCache.get(1);
    }

    @Test
    void distributedPCBPageTable() {
        short[] data = {0x01, 0x01, 0x02, 0x04, 0x71, 0x2b, 0x5f, 0x30};
        jcb.setData(data);
        ArrayList<Instruction> instructions = new ArrayList<>(){};
        instructions.add(new Instruction(1,0,3,data[0]));
        jcb.setInstructions(instructions);
        PCB pcb = ProcessManager.pm.processOperator.createPCB(jcb);
        StorageManager.sm.allotManager.allotPCBPageTable(pcb);
    }
}