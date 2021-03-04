package os.process;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PageTableEntryTest {

    @Test
    void pteDataToWord() {
        PageTableEntry pte = new PageTableEntry();
        // 7 逻辑页号  6 物理页框号  15 物理块号  1 有效位   1 修改位   2 未用
        pte.setVirtualPageNo(2);
        pte.setPhysicPageNo(10);
        pte.setDiskBlockNo(19900);
        pte.setValid(true);
        pte.setModify(false);
        System.out.println(pte.pteDataToWord());
    }
}