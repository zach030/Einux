package os;

import disk.Disk;
import org.junit.jupiter.api.Test;
import os.schedule.Schedule;

class ScheduleTest {
    @Test
    void run() {
        Disk.disk.loadDisk();
        Schedule.schedule.Run();
    }
}