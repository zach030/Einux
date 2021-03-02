package os.process;

import utils.SysConst;

import java.security.PublicKey;
import java.util.HashMap;

// 指令
public class Instruction {
    int id;     //指令id
    int type;   //指令类型
    int arg;    //指令携带参数
    byte[] data;//携带数据
    int size;
    public static final int INSTRUCTION_SIZE = 8;  //指令大小默认8字节
    public static final int ONE_PAGE_HAS_INSTRUCTION_NUM = SysConst.PAGE_FRAME_SIZE / INSTRUCTION_SIZE;
    public static final HashMap<Integer, String> instructionsMap = new HashMap<>() {
        {
            put(0, "System call");        // 系统调用指令  0                0
            put(1, "visit memory");       // 写内存       内存逻辑地址    数据
            put(2, "jump");               // 跳转        指令id            0
            put(3, "input");              // 输入        申请DMA           0
            put(4, "output");             // 输出        申请DMA           0
            put(5, "request resource");   // 申请资源    资源类型：1，2，3  0
            put(6, "release resource");   // 释放资源    资源类型；1，2，3  0
        }
    };

    public Instruction(int id, int type, int arg, byte[] data) {
        this.id = id;
        this.type = type;
        this.arg = arg;
        this.data = data;
        this.size = INSTRUCTION_SIZE;
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getArg() {
        return arg;
    }

    public void setArg(int arg) {
        this.arg = arg;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public static HashMap<Integer, String> getInstructionsMap() {
        return instructionsMap;
    }
}
