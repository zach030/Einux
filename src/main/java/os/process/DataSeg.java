package os.process;

import hardware.Page;
import utils.SysConst;

import java.util.ArrayList;
import java.util.Arrays;

// 数据段
public class DataSeg {
    ArrayList<Page> pages;
    int pageNums;
    int logicalPageNo;
    int segNo;
    byte[] data;

    DataSeg(byte[] data) {
        this.segNo = PCB.DATA_SEG_NO;
        this.data = data;
        this.logicalPageNo = 1;
        int dataLength = 0;
        if (data != null) {
            dataLength = data.length;
        }
        this.pageNums = dataLength / SysConst.PAGE_FRAME_SIZE + 1;
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
