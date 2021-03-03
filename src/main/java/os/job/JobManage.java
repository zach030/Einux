package os.job;

import os.filesystem.Block;
import os.process.Instruction;
import os.storage.StorageManage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

// 作业管理：负责作业的创建、放入外存
public class JobManage {
    public static JobManage jm = new JobManage();

    private int count = 0;
    private boolean hasNewJob = false;
    public ArrayList<JCB> backJCBS = new ArrayList<>(); //后备作业队列（不在磁盘中）
    public static final String jobInstructionPath = "D:/AllProjects/Java/Simulation-Implementation-Of-Linux-System/test/input/";
    public static final String inputJobPath = "D:/AllProjects/Java/Simulation-Implementation-Of-Linux-System/test/input/19318123-jobs-input.txt";
    public static File jobFile = new File(inputJobPath);

    JobManage() {
        JobRequest jobRequest = new JobRequest();
        //LoadJobFromFile(jobFile);
        //jobRequest.start();
    }

    // 检查作业请求线程
    class JobRequest extends Thread {
        public void run() {
            while (true) {
                try {
                    sleep(1000);
                    if (hasNewJob) {
                        //allJCBS = backJCBS;
//                        backJCBS = null;
//                        for (JCB jcb : allJCBS) {
//                            //TODO 放入磁盘
//                            jcb.saveToDisk();
//                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean isBackJobsEmpty() {
        return backJCBS.isEmpty();
    }

    public JCB getFirstJCB() {
        return backJCBS.get(0);
    }

    public void LoadJobFromFile(File file) {
        try {
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String currentScanLine = "";
            while ((currentScanLine = bufferedReader.readLine()) != null) {
                JCB jcb = new JCB();
                String[] jobInfoStr = currentScanLine.split(",");
                jcb.SetJobByFileLine(jobInfoStr);
                backJCBS.add(jcb);
                count++;
                ReadJobInstructions(jobInfoStr[0], jcb);
                jcb.calcJobPageNum();
                jcb.saveJobToDiskJCBZone();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 读取job的指令
    void ReadJobInstructions(String filename, JCB JCB) {
        try {
            FileReader fr = new FileReader(jobInstructionPath + "/" + filename + ".txt");
            BufferedReader br = new BufferedReader(fr);
            String line;
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split(",");
                JCB.SetJobInstructions(tmp);
            }
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void createNewJob() {
        // 当前jobId
        count++;
        JCB jcb = new JCB(count);
        // 随机生成指令
        generateRandomInstruction(jcb);
        // 加入后备队列
        backJCBS.add(jcb);
        hasNewJob = true;
        // 将jcb写入磁盘
        jcb.saveJobToDiskJCBZone();
    }

    void generateRandomInstruction(JCB jcb) {
        Random rand = new Random();
        int num = jcb.getJobInstructionNum();
        int arg;
        byte[] data = new byte[0];
        for (int i = 0; i < num; i++) {
            int type = rand.nextInt(5);
            switch (type) {
                case 1:
                    arg = rand.nextInt();
                    data = intToByteArray(rand.nextInt(10));
                    break;
                case 2:
                    arg = rand.nextInt(num);
                    break;
                case 5:
                case 6:
                    arg = rand.nextInt(2) + 1;
                    break;
                default:
                    arg = 0;
            }
            Instruction instruction = new Instruction(i, type, arg, data);
            jcb.instructions.add(instruction);
        }
        // calc pages job num
        jcb.calcJobPageNum();
    }


    byte[] intToByteArray(int num) {
        byte[] b = new byte[4];
        b[0] = (byte) (num & 0xff);
        b[1] = (byte) (num >> 8 & 0xff);
        b[2] = (byte) (num >> 16 & 0xff);
        b[3] = (byte) (num >> 24 & 0xff);
        return b;
    }

}
