package hardware.line;

public class DataLine implements Line {
    public static Line dataLine = new DataLine();
    // data-in-line
    private short data = 0;

    public void reset() {
        this.data = 0;
    }

    public void storeData(short data) {
        this.data = data;
    }

    public short getData() {
        short tmp = this.data;
        this.reset();
        return tmp;
    }
}
