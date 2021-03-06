package os.process;

import hardware.memory.Page;

import java.util.ArrayList;

public class StackSeg {
    ArrayList<Page> pages;
    int pageNums;
    int logicalPageNo;
    int segNo;

    StackSeg(int pageStart) {
        this.pageNums = 1;
        this.segNo = PCB.STACK_SEG_NO;
        pages = new ArrayList<>(pageNums);
        this.logicalPageNo = pageStart;
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
