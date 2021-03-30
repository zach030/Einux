package os.systemcall;

import hardware.CPU;
import org.junit.jupiter.api.Test;
import os.process.PCB;

class SystemCallTest {
    @Test
    void open() {
        PCB p1 = new PCB();
        p1.setID(1);
        CPU.cpu.setCurrent(p1);
        int fd1 = SystemCall.systemCall.fileSystemCall.open("/home/zach", 0);
        int fd2 = SystemCall.systemCall.fileSystemCall.open("/home/zach", 0);
        System.out.println(fd1+","+fd2);
    }
}