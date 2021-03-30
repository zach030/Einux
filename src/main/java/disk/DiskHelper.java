package disk;

import java.io.*;
import java.util.ArrayList;

// help to operate simulate disk like .txt
public class DiskHelper {

    // record path:block-entry
    // index of the list is Logical Block Address(LBA)
    ArrayList<RealBlockEntry> blockEntries = new ArrayList<>();

    public DiskHelper(int c, int h, int s) {
        this.cylinder = c;
        this.header = h;
        this.sector = s;
    }

    // simulate a sector, min storage unit
    class RealBlockEntry extends File implements RealBlock {
        // block position: cylinder, track, sector
        int no;
        String path;
        String content;
        int c, h, s;
        byte[] data = new byte[DevConfig.BLOCK_SIZE];
        boolean empty;

        RealBlockEntry(int no, String path, int c, int h, int s) {
            super(path);
            this.no = no;
            this.path = path;
            this.c = c;
            this.h = h;
            this.s = s;
            empty = true;
        }

        //TODO 对读出的数据 提供定制接口，指定位置读/写
        void loadBlockContent() {
            File file = this.getAbsoluteFile();
            InputStreamReader read = null;
            StringBuilder content = new StringBuilder();
            try {
                read = new InputStreamReader(
                        new FileInputStream(file));
                BufferedReader bufferedReader = new BufferedReader(read);
                String lineTxt;
                while ((lineTxt = bufferedReader.readLine()) != null) {
                    content.append(lineTxt).append(" ");
                }
                read.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.content = content.toString();
            loadBlockByteData();
        }

        void loadBlockByteData() {
            System.out.println("now is block:" + this.no + ",path is:" + this.path);
            String[] strData = this.content.split(" ");
            for (int i = 0; i < strData.length; i++) {
                //System.out.println(i + " : " + strData[i]);
                this.data[i] = (byte) (Integer.parseInt(strData[i], 16));
            }
        }

        @Override
        public void initDiskBlock() {

        }

        @Override
        public byte[] getAllData() {
            return data;
        }

        @Override
        public short read(int offset) {
            return (short) ((data[offset] & 0xFF) | (data[offset + 1] << 8));
        }

        @Override
        public void write(int offset, short content) {
            updateBlock(this.no, offset, content);
        }

        @Override
        public void writeBlock(byte[] data) {
            this.data = data;
            updateFullBlock(this.no);
        }

        @Override
        public boolean checkBlockFlag() {
            return empty;
        }
    }

    // root dir for simulate disk
    public static String rootDir = "D:/AllProjects/Java/Simulation-Implementation-Of-Linux-System/disk";
    // simulate disk prefix
    public static final String cylinderPrefix = "cylinder";
    public static final String trackPrefix = "track";
    public static final String sectorPrefix = "sector";
    // num of cylinder
    private int cylinder;
    // num of header
    private int header;
    // num of sector
    private int sector;

    // create virtual disk
    public void createVirtualDisk() {
        int count = 0;
        for (int i = 0; i < cylinder; i++) {
            String cPath = createCylinder(i);
            for (int j = 0; j < header; j++) {
                String tPath = createTracker(cPath, j);
                for (int k = 0; k < sector; k++) {
                    String sPath = createSector(tPath, k);
                    // TODO store sector entry
                    RealBlockEntry blockEntry = new RealBlockEntry(count, sPath, i, j, k);
                    blockEntries.add(blockEntry);
                    count++;
                }
            }
        }
    }

    public RealBlock getBlock(int index) {
        return blockEntries.get(index);
    }

    // TODO: traverse directory to set block entry map
    public void loadBlockEntries() {
        int count = 0;
        for (int i = 0; i < cylinder; i++) {
            String cPath = loadCylinder(i);
            for (int j = 0; j < header; j++) {
                String tPath = loadTracker(cPath, j);
                for (int k = 0; k < sector; k++) {
                    String sPath = loadSector(tPath, k);
                    RealBlockEntry blockEntry = new RealBlockEntry(count, sPath, i, j, k);
                    blockEntries.add(blockEntry);
                    count++;
                }
            }
        }
        for (RealBlockEntry realBlockEntry : blockEntries) {
            realBlockEntry.loadBlockContent();
        }
    }

    // create cylinder
    String createCylinder(int index) {
        String cPath = rootDir + "/" + cylinderPrefix + "_" + index;
        createDir(cPath);
        return cPath;
    }

    String loadCylinder(int index) {
        return rootDir + "/" + cylinderPrefix + "_" + index;
    }

    // create tracker
    String createTracker(String cPath, int index) {
        String tPath = cPath + "/" + trackPrefix + "_" + index;
        createDir(tPath);
        return tPath;
    }

    String loadTracker(String cPath, int index) {
        return cPath + "/" + trackPrefix + "_" + index;
    }

    // create sector
    String createSector(String tPath, int index) {
        String filename = sectorPrefix + "_" + index + ".txt";
        createFile(tPath, filename);
        return tPath + "/" + filename;
    }

    String loadSector(String tPath, int index) {
        return tPath + "/" + sectorPrefix + "_" + index + ".txt";
    }

    // init block
    public void initBlock(int start, int end) {
        // init single block
        if (end == DevConfig.END_FLAG) {
            writeBlock(start, 0, false);
            return;
        }
        // init multiply block
        for (int i = start; i < end; i++) {
            writeBlock(i, start, true);
        }
    }

    void writeBlock(int index, int repeatIndex, boolean repeat) {
        RealBlockEntry block = blockEntries.get(index);
        try {
            FileWriter fw = new FileWriter(block.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            String content = "";
            if (repeat) {
                content = DevConfig.INIT_BLOCK_MAP.get(repeatIndex);
            } else {
                content = DevConfig.INIT_BLOCK_MAP.get(index);
            }
            bw.write(content);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void updateFullBlock(int index) {
        RealBlockEntry realBlockEntry = blockEntries.get(index);
        StringBuilder content = new StringBuilder();
        byte[] data = realBlockEntry.data;
        for (int i = 0; i < data.length; i++) {
            if (i % 16 == 0 && i != 0) {
                content.append("\n");
            }
            String num = Integer.toHexString((data[i] & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase();
            content.append(num);
            if (i % 16 != 15) {
                content.append(" ");
            }
        }
        try {
            FileWriter fw = new FileWriter(realBlockEntry.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(content.toString());
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateBlock(int index, int offset, short data) {
        int row = offset / 15;
        int column = offset % 15;
        int rowStart = row * 49;
        int columnStart = (column - 1) * 3;
        RealBlockEntry block = blockEntries.get(index);
        try {
            RandomAccessFile raf = new RandomAccessFile(block.getAbsoluteFile(), "rw");
            raf.seek(rowStart + columnStart);
            raf.write(String.valueOf(data).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public short readBlock(int index, int offset) {
        int row = offset / 15;
        int column = offset % 15;
        int rowStart = row * 49;
        int columnStart = (column - 1) * 3;
        byte[] data = new byte[2];
        RealBlockEntry block = blockEntries.get(index);
        try {
            RandomAccessFile raf = new RandomAccessFile(block.getAbsoluteFile(), "rw");
            raf.seek(rowStart + columnStart);
            raf.seek(rowStart + columnStart);
            raf.read(data, 0, 2);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Short.parseShort(new String(data));
    }

    public static byte[] short2byte(short s) {
        byte[] b = new byte[2];
        for (int i = 0; i < 2; i++) {
            int offset = 16 - (i + 1) * 8; //因为byte占4个字节，所以要计算偏移量
            b[i] = (byte) ((s >> offset) & 0xff); //把16位分为2个8位进行分别存储
        }
        return b;
    }

    // create dir
    void createDir(String path) {
        File dir = new File(path);
        if (!dir.exists())
            dir.mkdir();
    }

    // create file
    void createFile(String path, String filename) {
        File file = new File(path + "/" + filename);
        try {
            if (!file.exists())
                file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setRootDir(String rootDir) {
        DiskHelper.rootDir = rootDir;
    }
}
