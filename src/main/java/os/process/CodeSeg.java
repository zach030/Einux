package os.process;

import hardware.Page;
import utils.SysConst;

import java.util.ArrayList;
import java.util.Arrays;

public class CodeSeg {
    Page[] pages;
    int pageNums;
    int logicalPageNo;
    int segNo;
    int instructionNum;              // 进程包含的指令数目
    ArrayList<Instruction> instructions;      // 指令集

    CodeSeg(ArrayList<Instruction> instructions, int pageStart) {
        this.segNo = PCB.CODE_SEG_NO;
        this.instructions = instructions;
        this.instructionNum = instructions.size();
        int segSize = 0;
        for (int i = 0; i < instructionNum; i++) {
            segSize += instructions.get(i).size;
        }
        this.logicalPageNo = pageStart;
        this.pageNums = segSize / SysConst.PAGE_FRAME_SIZE + 1;
        pages = new Page[pageNums];
        Arrays.fill(pages,new Page());
    }

    public int getPageNums() {
        return pageNums;
    }

    public void setPageNums(int pageNums) {
        this.pageNums = pageNums;
    }

    public int getLogicalPageNo() {
        return logicalPageNo;
    }

    public void setLogicalPageNo(int logicalPageNo) {
        this.logicalPageNo = logicalPageNo;
    }
}
