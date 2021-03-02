package hardware;

import java.util.ArrayList;

public class TLB {
    public static TLB tlb = new TLB();


    ArrayList<TLBEntry> tlbEntries = new ArrayList<>();  //快表

    class TLBEntry {
        int virtualPageNo; //虚拟页号
        int physicPageNo;  //物理页号

        TLBEntry(int vp, int pp) {
            this.virtualPageNo = vp;
            this.physicPageNo = pp;
        }
    }

    // 刷新TLB
    public void flushTLB() {
        tlbEntries = new ArrayList<>();
    }

    int searchTLB(int virtualPageNo) {
        return MMU.NOT_FOUND_ERROR;
    }

    synchronized void addTLB(int virtualPageNo, int physicPageNo) {
        this.tlbEntries.add(new TLBEntry(virtualPageNo, physicPageNo));
    }
}
