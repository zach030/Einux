package os.systemcall;

import org.junit.jupiter.api.Test;

class SystemCallTest {
    @Test
    void open() {
        int fd = SystemCall.systemCall.fileSystemCall.open("/", 0);
        System.out.println(fd);
    }
}