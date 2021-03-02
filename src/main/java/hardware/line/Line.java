package hardware.line;

public interface Line {
    // reset data in line
    void reset();
    // store data to line
    void storeData(short data);
    // get data from line
    short getData();
}
