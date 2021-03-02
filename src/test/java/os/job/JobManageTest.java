package os.job;

import org.junit.jupiter.api.Test;

class JobManageTest {

    @Test
    void loadJobFromFile() throws InterruptedException {
        JobManage jobManage = new JobManage();
        Thread.sleep(3000);
        jobManage.createNewJob();

    }
}