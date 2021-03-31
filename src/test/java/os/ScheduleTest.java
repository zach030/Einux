package os;

import org.junit.jupiter.api.Test;
import os.schedule.Schedule;

class ScheduleTest {
    @Test
    void test() {
        byte[] data = new byte[]{1,2,3};
        String num = Integer.toHexString((data[2] & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase();
        System.out.println(num);
    }
}