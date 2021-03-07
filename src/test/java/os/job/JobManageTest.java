package os.job;

import disk.Disk;
import org.junit.jupiter.api.Test;

class JobManageTest {

    @Test
    void loadJobFromFile(){
        //Disk.disk.loadDisk();
        JobManage jobManage = new JobManage();
        jobManage.LoadJobFromFile(JobManage.jobFile);

    }

    @Test
    void createNewJob() {
        //JobManage.jm.createNewJob();
        JCB jcb = new JCB(4,8);
        JobManage.jm.generateRandomInstruction(jcb);
    }
}