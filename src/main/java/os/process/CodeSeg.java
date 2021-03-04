package os.process;

import hardware.memory.Page;

import java.util.ArrayList;

public class CodeSeg {
    ArrayList<Page> pages;
    int pageNums;
    int logicalPageNo;
    int segNo;
    int instructionNum;              // 进程包含的指令数目
    ArrayList<Instruction> instructions;      // 指令集

    CodeSeg(ArrayList<Instruction> instructions, int pageStart, int pageNums) {
        this.segNo = PCB.CODE_SEG_NO;
        this.instructions = instructions;
        this.instructionNum = instructions.size();
        this.logicalPageNo = pageStart;
        this.pageNums = pageNums;
        pages = new ArrayList<>(pageNums);
    }

    public ArrayList<Page> getPages() {
        return pages;
    }

    public void setPages(ArrayList<Page> pages) {
        this.pages = pages;
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
