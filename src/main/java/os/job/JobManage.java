package os.job;

import os.filesystem.Block;
import os.process.Instruction;
import os.storage.StorageManage;

import java.security.MessageDigest;
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
    }

    public boolean isBackJobsEmpty() {
        return backJCBS.isEmpty();
    }

    public JCB getFirstJCB() {
        JCB jcb = backJCBS.get(0);
        backJCBS.remove(0);
        return jcb;
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
        for (int i = 0; i < num; i++) {
            short data = 0;
            // 产生指令类型随机数
            int type = rand.nextInt(8);
            Instruction instruction = new Instruction(i);
            boolean[] requireResource = new boolean[]{false, false, false};
            switch (type) {
                case 0:
                    // 创建文件，arg是文件名
                    createSystemCallIns(instruction);
                    break;
                case 1:
                    // 写内存
                    createWriteMemoryIns(instruction);
                    break;
                case 2:
                    // 读内存指令
                    createReadMemoryIns(instruction);
                    break;
                case 3:
                    // 跳转指令
                    createJumpIns(instruction, num);
                    break;
                case 4:
                    // 请求输入指令
                    createInputIns(instruction);
                    break;
                case 5:
                    // 请求输出指令
                    createOutputIns(instruction);
                    break;
                case 6:
                    // 申请资源指令
                    createRequireResourceIns(instruction);
                    break;
                case 7:
                    // 释放资源指令
                    createReleaseResourceIns(instruction, requireResource);
                    break;
                default:
                    return;
            }
            jcb.instructions.add(instruction);
        }
        // calc pages job num
        jcb.calcJobPageNum();
    }

    // 系统调用---创建文件命令
    public void createSystemCallIns(Instruction instruction) {
        instruction.setType(0);
        int fileName = new Random().nextInt(32);
        instruction.setArg(fileName);
        instruction.setData((short) 0);
        instruction.setSize(Instruction.INSTRUCTION_SIZE);
    }

    // 写内存指令
    public void createWriteMemoryIns(Instruction instruction) {
        instruction.setType(1);
        // todo 访存地址
        int addr = new Random().nextInt();
        instruction.setArg(addr);
        instruction.setData((short) new Random().nextInt(50));
        instruction.setSize(Instruction.INSTRUCTION_SIZE);
    }

    // 读内存指令
    public void createReadMemoryIns(Instruction instruction) {
        instruction.setType(2);
        // todo 访存地址
        int addr = new Random().nextInt();
        instruction.setArg(addr);
        instruction.setData((short) 0);
        instruction.setSize(Instruction.INSTRUCTION_SIZE);
    }

    // 跳转指令
    public void createJumpIns(Instruction instruction, int num) {
        instruction.setType(3);
        // todo 访存地址
        instruction.setArg((new Random().nextInt(num - instruction.getId()) + instruction.getId()));
        instruction.setData((short) 0);
        instruction.setSize(Instruction.INSTRUCTION_SIZE);
    }

    // 输入指令
    public void createInputIns(Instruction instruction) {
        instruction.setType(4);
        // todo 访存地址
        instruction.setArg((short) 0);
        instruction.setData((short) 0);
        instruction.setSize(Instruction.INSTRUCTION_SIZE);
    }

    // 输出指令
    public void createOutputIns(Instruction instruction) {
        instruction.setType(5);
        // todo 访存地址
        instruction.setArg((short) 0);
        instruction.setData((short) 0);
        instruction.setSize(Instruction.INSTRUCTION_SIZE);
    }

    // 申请资源指令
    public void createRequireResourceIns(Instruction instruction) {
        instruction.setType(6);
        // todo 访存地址
        // 资源类型1-3
        int source = new Random().nextInt(3) + 1;
        instruction.setArg((short) source);
        instruction.setData((short) 0);
        instruction.setSize(Instruction.INSTRUCTION_SIZE);
    }

    // 释放资源指令
    public void createReleaseResourceIns(Instruction instruction, boolean[] requireSource) {
        // 释放已被申请的资源
        int source = 0;
        int type = 7;
        for (int i = 0; i < requireSource.length; i++) {
            if (!requireSource[i]) {
                source = i + 1;
                break;
            }
            // 如果没有申请资源，则将此指令变为跳转指令
            type = 3;
        }
        if (type == 7) {
            instruction.setType(type);
            instruction.setArg((short) source);
        } else {
            // todo 跳转指令，跳转到下一条，执行的时候如果跳转指令已超出上限，则跳过
            instruction.setType(type);
            instruction.setArg((short) instruction.getId() + 1);
        }
        instruction.setData((short) 0);
        instruction.setSize(Instruction.INSTRUCTION_SIZE);
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
