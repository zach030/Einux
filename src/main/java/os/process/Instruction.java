package os.process;

import utils.SysConst;

import java.util.HashMap;

// 指令
public class Instruction {
    int id;     //指令id
    int type;   //指令类型
    int arg;    //指令携带参数
    short data;//携带数据
    int size;
    public static final int INSTRUCTION_SIZE = 4;  //指令大小默认4字节
    public static final int ONE_PAGE_HAS_INSTRUCTION_NUM = SysConst.PAGE_FRAME_SIZE / INSTRUCTION_SIZE;
    public static final HashMap<Integer, String> instructionsMap = new HashMap<>() {
        {
            // type                                       arg           data
            put(0, "System call");        // 打开文件    文件路径           0
            put(1, "write memory");       // 写内存      内存逻辑地址    数据
            put(2, "read memory");        // 读内存      内存逻辑地址       0
            put(3, "jump");               // 跳转        指令id            0
            put(4, "input");              // 输入        申请keyboard      0
            put(5, "output");             // 输出        申请screen        0
            put(6, "request resource");   // 申请资源    申请other         0
            put(7, "release resource");   // 释放资源    释放other         x
        }
    };

    public Instruction(int id, int type, int arg, short data) {
        this.id = id;
        this.type = type;
        this.arg = arg;
        this.data = data;
        this.size = INSTRUCTION_SIZE;
    }

    public Instruction(int id) {
        this.id = id;
    }

    public Instruction() {

    }

    public int simplifyInstructionData() {
        int data = 0;
        // 32bit: id 8位，type 3位，arg 5位，data 16位
        data |= this.id << 24 & 0XFF000000;
        data |= this.type << 21 & 0X00E00000;
        data |= this.arg << 16 & 0X001F0000;
        data |= this.data & 0X0000FFFF;
        return data;
    }

    public String getIRType() {
        switch (type) {
            case 0:
                return "系统调用-打开文件";
            case 1:
                return "写内存";
            case 2:
                return "读内存";
            case 3:
                return "跳转指令";
            case 4:
                return "请求输入";
            case 5:
                return "请求输出";
            case 6:
                return "申请资源";
            case 7:
                return "释放资源";
        }
        return "";
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

    public short getData() {
        return data;
    }

    public void setData(short data) {
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
