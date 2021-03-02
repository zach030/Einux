package os.process;

import hardware.Page;

import java.util.Arrays;

public class StackSeg {
    Page[] pages;
    int pageNums;
    int logicalPageNo;
    int segNo;

    StackSeg(int pageStart) {
        this.pageNums = 1;
        this.segNo = PCB.STACK_SEG_NO;
        pages = new Page[pageNums];
        Arrays.fill(pages,new Page());
        this.logicalPageNo = pageStart;
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
