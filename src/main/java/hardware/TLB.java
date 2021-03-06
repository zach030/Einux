package hardware;

import java.util.ArrayList;

public class TLB {
    public static TLB tlb = new TLB();
    static final int TLB_SIZE = 16;

    public TLB() {
    }

    ArrayList<TLBEntry> tlbEntries = new ArrayList<>(TLB_SIZE);  //快表

    static class TLBEntry {
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
        for (TLBEntry tlbEntry : tlbEntries) {
            if (tlbEntry.virtualPageNo == virtualPageNo) {
                return tlbEntry.physicPageNo;
            }
        }
        return MMU.NOT_FOUND_ERROR;
    }

    synchronized void addTLB(int virtualPageNo, int physicPageNo) {
        this.tlbEntries.add(new TLBEntry(virtualPageNo, physicPageNo));
    }
}
