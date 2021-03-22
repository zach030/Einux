package os.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeadLockTest {

    @Test
    void applyResource() {
        DeadLock.deadLock.applyResource(new PCB(1),0,1);
        DeadLock.deadLock.applyResource(new PCB(2),1,3);
        DeadLock.deadLock.applyResource(new PCB(3),2,5);
        DeadLock.deadLock.applyResource(new PCB(4),2,3);
        DeadLock.deadLock.applyResource(new PCB(5),1,3);
    }
}