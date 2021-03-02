package hardware.line;

public class AddrLine implements Line {
    public static Line addrLine = new AddrLine();
    // addr-in-line
    private short addr = 0;

    @Override
    public void reset() {
        this.addr = 0;
    }

    @Override
    public void storeData(short addr) {
        this.addr = addr;
    }

    @Override
    public short getData() {
        short tmp = this.addr;
        this.reset();
        return tmp;
    }
}
