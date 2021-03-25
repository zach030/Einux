package os.job;

import os.process.Instruction;

import java.util.ArrayList;

// 作业抽象类
// todo 解析c代码，分解出指令
public class Program {
    String code;
    ArrayList<Instruction> instructions;

    // todo 编译代码
    public void compile() {

    }

    // 产生作业
    public JCB genJCB() {
        JCB jcb = new JCB();
        jcb.setInstructions(instructions);
        return jcb;
    }

    public void readCode() {
        //todo 读入代码文件
        this.code = "printf('hello world');";
    }
}
