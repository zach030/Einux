package os.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DeadLockTest {

    @Test
    void applyResource() {
        DeadLock.deadLock.applyResource(new PCB(1), DeadLock.ResourceType.KEYBOARD, 1);
        DeadLock.deadLock.applyResource(new PCB(2), DeadLock.ResourceType.OTHER, 3);
        DeadLock.deadLock.applyResource(new PCB(3), DeadLock.ResourceType.SCREEN, 1);
        DeadLock.deadLock.applyResource(new PCB(4), DeadLock.ResourceType.OTHER, 2);
        DeadLock.deadLock.applyResource(new PCB(5), DeadLock.ResourceType.OTHER, 2);
    }
}