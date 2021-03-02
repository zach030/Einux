package os.process;

// 页表
public class PageTableEntry {
    int virtualPageNo;       //逻辑页号
    int physicPageNo;        //页框号
    int diskBlockNo;         //外存块号
    boolean isValid;         //是否有效
    boolean isModify;        //是否修改
}
