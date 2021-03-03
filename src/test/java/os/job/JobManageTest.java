package os.job;

import disk.Disk;
import org.junit.jupiter.api.Test;

class JobManageTest {

    @Test
    void loadJobFromFile(){
        Disk.disk.loadDisk();
        JobManage jobManage = new JobManage();
        jobManage.LoadJobFromFile(JobManage.jobFile);
    }
}