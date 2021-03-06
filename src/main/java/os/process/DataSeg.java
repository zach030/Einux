package os.process;

import hardware.memory.Page;

import java.util.ArrayList;

// 数据段
public class DataSeg {
    ArrayList<Page> pages;
    int pageNums;
    int logicalPageNo;
    int segNo;
    short[] data;

    DataSeg(short[] data, int pageNums) {
        this.segNo = PCB.DATA_SEG_NO;
        this.data = data;
        this.logicalPageNo = 1;
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
