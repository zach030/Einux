package os.job;

import org.junit.jupiter.api.Test;
import os.process.Instruction;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

class JobManageTest {

    @Test
    void loadJobFromFile() {
        //Disk.disk.loadDisk();
        JobManage jobManage = new JobManage();
        jobManage.LoadJobFromFile(JobManage.jobFile);

    }

    @Test
    void createNewJob() {
        //JobManage.jm.createNewJob();
        JCB jcb = new JCB(10, 58);
        JobManage.jm.generateRandomInstruction(jcb);
        String fileName = "D:/AllProjects/Java/Einux/test/input/10.txt";

        Path path = Paths.get(fileName);
        // 使用newBufferedWriter创建文件并写文件
        // 这里使用了try-with-resources方法来关闭流，不用手动关闭
        try (BufferedWriter writer =
                     Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.APPEND)) {
            for (int i = 0; i < jcb.getJobInstructionNum(); i++) {
                Instruction instruction = jcb.instructions.get(i);
                writer.write(String.format("%d,%d,%d,%d\n", instruction.getId(), instruction.getType(), instruction.getArg(), instruction.getData()));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}