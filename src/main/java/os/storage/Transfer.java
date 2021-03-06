package os.storage;

import hardware.memory.Page;
import hardware.disk.Block;

//负责内存数据结构与磁盘的数据结构之间的转换
public class Transfer {
    public static Transfer transfer = new Transfer();

    public Page transferBlockToPage(Block block, int logicalNo, int frameNo) {
        Page page = new Page();
        page.setBlockNo(block.getBlockNo());
        page.setModify(false);
        page.setLogicalNo(logicalNo);
        page.setFrameNo(frameNo);
        page.setStay(true);
        page.setData(block.getData());
        return page;
    }

    public Block transferPageToBlock(Page page, int blockNo) {
        Block block = new Block();
        block.setBlockNo(blockNo);
        block.setData(page.getData());
        return block;
    }
}
