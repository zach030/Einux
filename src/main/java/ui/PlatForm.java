/*
 * Created by JFormDesigner on Wed Mar 31 01:16:13 CST 2021
 */

package ui;

import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.table.*;

import disk.DevConfig;
import disk.DiskHelper;
import hardware.CPU;
import hardware.disk.SuperBlock;
import hardware.memory.Memory;
import hardware.memory.Page;
import os.Controller;
import os.device.DeviceManager;
import os.filesystem.FileSystem;
import os.filesystem.SysFile;
import os.job.JobManage;
import os.process.DeadLock;
import os.process.Instruction;
import os.process.PageTableEntry;
import os.process.ProcessManager;
import os.storage.StorageManager;
import utils.TextAreaLogAppender;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * @author zach
 */
public class PlatForm extends JFrame {
    public static PlatForm platForm = new PlatForm();

    public static void main(String[] args) {
        platForm.setVisible(true);
    }

    public PlatForm() {
        initComponents();
        initUI();
    }

    private void initUI() {
        // 设置bitmap标识
        for (int i = 0; i < 64; i++) {
            JButton button = (JButton) bitmap.getComponent(i);
            button.setText(String.valueOf(i));
        }
        // 设置磁盘信息
        cNum.setText(String.valueOf(DevConfig.CYLINDER_NUM));
        hNum.setText(String.valueOf(DevConfig.TRACK_NUM));
        sNum.setText(String.valueOf(DevConfig.SECTOR_NUM));
        // 设置空闲缓冲区
        for (int i = 0; i < 16; i++) {
            JLabel label = (JLabel) freeBh.getComponent(i);
            label.setText(String.valueOf(i));
        }
    }

    private void startActionPerformed(ActionEvent e) {
        dialog1.setVisible(true);
    }

    private void pauseActionPerformed(ActionEvent e) {
        Controller.controller.Pause();
    }

    private void resumeActionPerformed(ActionEvent e) {
        Controller.controller.Resume();
    }

    private void stopActionPerformed(ActionEvent e) {
        Controller.controller.Stop();
    }

    /**
     * @description: 刷新时钟
     * @author: zach
     **/
    synchronized public void refreshSystemTime() {
        systemTime.setText(String.valueOf(CPU.cpu.clock.getCurrentTime()));
        systemTime.setForeground(Color.RED);
    }

    /**
     * @description: 刷新当前运行进程
     * @author: zach
     **/
    synchronized public void refreshCurrentPCB() {
        String text = "null";
        if (CPU.cpu.isRunning()) {
            text = String.valueOf(CPU.cpu.getCurrent().getID());
            refreshCPUInfo();
            refreshPCBInfo();
        } else if (ProcessManager.pm.requesterManager.isAllFinished()){
            text = "null";
        }
        currentPCB.setText(text);
        currentPCB.setForeground(Color.RED);
    }

    public void showFinish(){
        JOptionPane.showMessageDialog(null,"已全部运行结束!");
    }

    /**
     * @description: 刷新cpu信息
     * @author: zach
     **/
    synchronized public void refreshCPUInfo() {
        String stringBuilder = String.format("当前状态:%s\n", CPU.cpu.getStatus().name()) +
                String.format("当前进程:%d\n", CPU.cpu.getCurrent().getID()) +
                String.format("IR寄存器:%d\n", CPU.cpu.getIR()) +
                String.format("PC寄存器:%d\n", CPU.cpu.getPC());
        cpuRegInfo.setText(stringBuilder);
    }

    /**
     * @description: 刷新进程信息
     * @author: zach
     **/
    synchronized public void refreshPCBInfo() {
        String stringBuilder = String.format("当前进程:%d\n", CPU.cpu.getCurrent().getID()) +
                String.format("进程状态:%s\n", CPU.cpu.getCurrent().getStatus().name()) +
                String.format("PC寄存器:%d\n", CPU.cpu.getCurrent().getPC()) +
                String.format("IR寄存器:%d\n", CPU.cpu.getCurrent().getIR()) +
                String.format("时间片:%d\n", CPU.cpu.getCurrent().getTimeSlice());
        pcbRegInfo.setText(stringBuilder);
    }

    /**
     * @description: 刷新指令信息
     * @author: zach
     **/
    synchronized public void refreshIRInfo() {
        Instruction instruction = CPU.cpu.getInstruction();
        String stringBuilder = String.format("当前指令:%d\n", instruction.getId()) +
                String.format("指令类型:%s\n", instruction.getIRType()) +
                String.format("指令参数:%d\n", instruction.getArg()) +
                String.format("指令数据:%d\n", instruction.getData());
        instructionInfo.setText(stringBuilder);
    }

    /**
     * @description: 刷新当前指令
     * @author: zach
     **/
    synchronized public void refreshCurrentPC() {
        String pc = "";
        if (CPU.cpu.getCurrent() != null) {
            pc = String.valueOf(CPU.cpu.getPC());
        } else {
            pc = "null";
        }
        currentPC.setText(pc);
        currentPC.setForeground(Color.RED);
    }

    /**
     * @description: 刷新内存位示图
     * @author: zach
     **/
    synchronized public void refreshMemoryBitMap() {
        boolean[] bit = StorageManager.sm.bitMapManager.getMemoryAllPageBitmap();
        for (int i = 0; i < bit.length; i++) {
            JButton button = (JButton) bitmap.getComponent(i);
            if (!bit[i]) {
                button.setBackground(Color.GREEN);
            } else {
                button.setBackground(Color.pink);
            }
        }
    }

    /**
     * @description: 更新系统日志
     * @author: zach
     **/
    synchronized public void refreshSystemLog(String info) {
        interruptInfo2.append(info);
    }

    /**
     * @description: 更新中断信息
     * @author: zach
     **/
    synchronized public void refreshInterruptInfo(String info) {
        interruptInfo.append(info);
    }

    /**
     * @description: 更新缓冲区分配日志
     * @author: zach
     **/
    synchronized public void refreshBufferLog(String info) {
        devLog.append(info);
    }

    /**
     * @description: 刷新进程列表
     * @author: zach
     **/
    synchronized public void refreshProcessQueue() {
        textArea5.setText(ProcessManager.pm.queueManager.displayReadyQueue());
        textArea6.setText(ProcessManager.pm.queueManager.displayResourceBlockQueue());
        textArea7.setText(ProcessManager.pm.queueManager.displayBufferBlockQueue());
        textArea8.setText(ProcessManager.pm.queueManager.displaySuspendQueue());
        textArea9.setText(ProcessManager.pm.queueManager.displayFinishQueue());
    }

    /**
     * @description: 刷新打开文件表
     * @author: zach
     **/
    synchronized public void refreshOpenFileTable() {
        int[] userOpen = CPU.cpu.getCurrent().getUserOpenFileTable();
        Vector<Vector> data = new Vector<>();
        DefaultTableModel dm = (DefaultTableModel) userOpenFile.getModel();
        for (int i = 0; i < userOpen.length; i++) {
            Vector row = new Vector();
            for (int col = 0; col < 2; col++) {
                row.add(i);
                row.add(userOpen[i]);
            }
            data.add(row);
        }
        Vector<String> columnName = new Vector<>();
        columnName.add("用户打开文件描述符");
        columnName.add("系统打开文件描述符");
        dm.setDataVector(data, columnName);
        userOpenFile.setPreferredSize(userOpenFile.getSize());
        userOpenFile.setModel(dm);

        SysFile[] sysOpen = FileSystem.fs.getSysOpenFileManager().getSysOpenFileTable();
        Vector<Vector> sysData = new Vector();
        DefaultTableModel dm1 = (DefaultTableModel) sysOpenFile.getModel();
        for (int i = 0; i < sysOpen.length; i++) {
            Vector row = new Vector();
            for (int col = 0; col < 4; col++) {
                row.add(i);
                row.add(sysOpen[i].count);
                row.add(sysOpen[i].offset);
                row.add(sysOpen[i].inodeNo);
            }
            sysData.add(row);
        }
        Vector<String> columnName1 = new Vector<>();
        columnName1.add("系统打开文件描述符");
        columnName1.add("文件引用数");
        columnName1.add("文件偏移");
        columnName1.add("文件inode号");
        dm1.setDataVector(sysData, columnName1);
        sysOpenFile.setPreferredSize(sysOpenFile.getSize());
        sysOpenFile.setModel(dm1);
    }

    /**
     * @description: 追加日志
     * @author: zach
     **/
    private void logAppender() {
        try {
            new TextAreaLogAppender(interruptInfo2, interrupt2).start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "重定向错误");
        }
    }

    /**
     * @description: 刷新设备管理 缓冲区
     * @author: zach
     **/
    synchronized private void refreshFreeBuf() {
        boolean[] bitmap = DeviceManager.dm.bufferQueueManager.getFreeBitMap();
        for (int i = 0; i < 16; i++) {
            JLabel label = (JLabel) freeBh.getComponent(i);
            if (!bitmap[i]) {
                label.setBackground(Color.GREEN);
            } else {
                label.setBackground(Color.pink);
            }
        }
    }

    /**
     * @description: 刷新进程页表
     * @author: zach
     **/
    synchronized public void refreshPageTable() {
        PageTableEntry[] pageTableEntries = CPU.cpu.getCurrent().getInternalPageTable();

        Vector data = new Vector();
        DefaultTableModel defaultTableModel = (DefaultTableModel) pageTable.getModel();
        for (int i = 0; i < pageTableEntries.length; i++) {
            Vector rowData = new Vector();
            for (int col = 0; col < 5; col++) {
                rowData.add(pageTableEntries[i].getVirtualPageNo());
                rowData.add(pageTableEntries[i].getPhysicPageNo());
                rowData.add(pageTableEntries[i].getDiskBlockNo());
                rowData.add(pageTableEntries[i].isModify());
                rowData.add(pageTableEntries[i].isValid());
            }
            data.add(rowData);
        }
        Vector<String> columnName = new Vector<>();
        columnName.add("逻辑页号");
        columnName.add("物理框号");
        columnName.add("外存块号");
        columnName.add("修改位");
        columnName.add("有效位");
        defaultTableModel.setDataVector(data, columnName);
        //重新setModel前设置一下table的size，不然会使用默认size
        pageTable.setPreferredSize(pageTable.getSize());
        pageTable.setModel(defaultTableModel);
    }

    private int getFrameNo(String page) {
        page = page.replaceAll("m", "");
        return Integer.parseInt(page);
    }

    synchronized private void showMemoryPage(int frameNo) {
        Page p = Memory.memory.readPage(frameNo);
        byte[] data = p.getData();
        for (int i = 0; i < data.length; i++) {
            JLabel label = (JLabel) panel1.getComponent(i);
            String num = Integer.toHexString((data[i] & 0x000000FF) | 0xFFFFFF00).substring(6).toUpperCase();
            label.setText(num);

        }
        memoryView.setVisible(true);
    }

    private void m0ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m1ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m2ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m3ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m4ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m5ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m6ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m7ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m8ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m9ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m10ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m11ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m12ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m13ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m14ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m15ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m16ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m17ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m18ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m19ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m20ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m21ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m22ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m23ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m24ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m25ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m26ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m27ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m28ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m29ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m30ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m31ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m32ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m33ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m34ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m35ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m36ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m37ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m38ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m39ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m40ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m41ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m42ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m43ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m44ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m45ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m46ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m47ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m48ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m49ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m50ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m51ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m52ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m53ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m54ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m55ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m56ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m57ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m58ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m59ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m60ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m61ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m62ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void m63ActionPerformed(ActionEvent e) {
        showMemoryPage(getFrameNo(e.getActionCommand()));
    }

    private void viewDiskActionPerformed(ActionEvent e) {
        try {
            java.awt.Desktop.getDesktop().open(new File(DiskHelper.rootDir));
        } catch (Exception e1) {
            e1.printStackTrace();
        }

    }

    synchronized private void refreshFileTree() {
        DefaultMutableTreeNode root;
        HashMap<FileSystem.FileTree, String> fileTree = FileSystem.fs.getAllFileMap();
        for (Map.Entry<FileSystem.FileTree, String> entry : fileTree.entrySet()) {
            if (entry.getValue().equals("/")) root = new DefaultMutableTreeNode("/");

        }

    }

    /**
     * @description: 刷新磁盘基础信息
     * @author: zach
     **/
    private void refreshDiskInfo() {
        SuperBlock superBlock = FileSystem.getCurrentBootDisk().getSuperBlock();
        String content = String.format("当前磁盘可用inode数:%d\n当前磁盘空闲数据块数:%d\n", superBlock.getAvailableInodeNum(),
                superBlock.getAvailableBlockNum());
        diskInfo.setText(content);
    }

    /**
     * @description: 刷新资源表
     * @author: zach
     **/
    private void refreshResourceAvailable() {
        int[] available = DeadLock.banker.getAvailable();

        Vector data = new Vector();
        DefaultTableModel defaultTableModel = (DefaultTableModel) resourceTable.getModel();
        for (int i = 0; i < available.length; i++) {
            Vector rowData = new Vector();
            for (int col = 0; col < 2; col++) {
                rowData.add(DeadLock.ResourceType.values()[i]);
                rowData.add(available[i]);
            }
            data.add(rowData);
        }
        Vector<String> columnName = new Vector<>();
        columnName.add("资源类型");
        columnName.add("可用值");
        defaultTableModel.setDataVector(data, columnName);
        //重新setModel前设置一下table的size，不然会使用默认size
        resourceTable.setPreferredSize(resourceTable.getSize());
        resourceTable.setModel(defaultTableModel);
    }

    /**
     * @description: 刷新资源阻塞队列
     * @author: zach
     **/
    public void refreshResourceBlockQueue(String info1, String info2, String info3) {
        keyBlock.setText(info1);
        screenBlock.setText(info2);
        otherBlock.setText(info3);
    }

    private void choosediskActionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);//是否允许多选
        int result = fc.showOpenDialog(new JPanel());
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            DiskHelper.setRootDir(file.getPath());
        }
        System.out.println(DiskHelper.rootDir);
    }

    private void choosejobActionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));
        fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        fc.setMultiSelectionEnabled(false);//是否允许多选
        int result = fc.showOpenDialog(new JPanel());
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            JobManage.jm.setChooseFile(file);
        }
    }

    private void button2ActionPerformed(ActionEvent e) {
        wait.setText("正在加载磁盘，请等待.....");
        Controller.controller.Start();
        new UpdateConsole().start();
        //logAppender();
        dialog1.setVisible(false);
    }

    private void button1ActionPerformed(ActionEvent e) {
        JobManage.jm.createNewJob();
    }

    class UpdateConsole extends Thread {
        public void run() {
            while (true) {
                try {
                    refreshMemoryBitMap();
                    refreshFreeBuf();
                    refreshDiskInfo();
                    sleep(1000);
                    refreshProcessQueue();
                    refreshResourceAvailable();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        mainTabPanel = new JTabbedPane();
        console = new JPanel();
        pageTablePanel = new JScrollPane();
        pageTable = new JTable();
        consoletoolBar1 = new JToolBar();
        start = new JButton();
        button1 = new JButton();
        pause = new JButton();
        resume = new JButton();
        stop = new JButton();
        systemTime = new JTextField();
        label1 = new JLabel();
        label2 = new JLabel();
        currentPCB = new JTextField();
        label3 = new JLabel();
        currentPC = new JTextField();
        baseTabPanel = new JTabbedPane();
        panel6 = new JPanel();
        scrollPane2 = new JScrollPane();
        cpuRegInfo = new JTextArea();
        panel7 = new JPanel();
        scrollPane3 = new JScrollPane();
        pcbRegInfo = new JTextArea();
        panel8 = new JPanel();
        scrollPane4 = new JScrollPane();
        instructionInfo = new JTextArea();
        interrupt = new JScrollPane();
        interruptInfo = new JTextArea();
        interrupt2 = new JScrollPane();
        interruptInfo2 = new JTextArea();
        memory = new JPanel();
        bitmap = new JPanel();
        m0 = new JButton();
        m1 = new JButton();
        m2 = new JButton();
        m3 = new JButton();
        m4 = new JButton();
        m5 = new JButton();
        m6 = new JButton();
        m7 = new JButton();
        m8 = new JButton();
        m9 = new JButton();
        m10 = new JButton();
        m11 = new JButton();
        m12 = new JButton();
        m13 = new JButton();
        m14 = new JButton();
        m15 = new JButton();
        m16 = new JButton();
        m17 = new JButton();
        m18 = new JButton();
        m19 = new JButton();
        m20 = new JButton();
        m21 = new JButton();
        m22 = new JButton();
        m23 = new JButton();
        m24 = new JButton();
        m25 = new JButton();
        m26 = new JButton();
        m27 = new JButton();
        m28 = new JButton();
        m29 = new JButton();
        m30 = new JButton();
        m31 = new JButton();
        m32 = new JButton();
        m33 = new JButton();
        m34 = new JButton();
        m35 = new JButton();
        m36 = new JButton();
        m37 = new JButton();
        m38 = new JButton();
        m39 = new JButton();
        m40 = new JButton();
        m41 = new JButton();
        m42 = new JButton();
        m43 = new JButton();
        m44 = new JButton();
        m45 = new JButton();
        m46 = new JButton();
        m47 = new JButton();
        m48 = new JButton();
        m49 = new JButton();
        m50 = new JButton();
        m51 = new JButton();
        m52 = new JButton();
        m53 = new JButton();
        m54 = new JButton();
        m55 = new JButton();
        m56 = new JButton();
        m57 = new JButton();
        m58 = new JButton();
        m59 = new JButton();
        m60 = new JButton();
        m61 = new JButton();
        m62 = new JButton();
        m63 = new JButton();
        process = new JPanel();
        tabbedPane3 = new JTabbedPane();
        readyQ = new JPanel();
        scrollPane6 = new JScrollPane();
        textArea5 = new JTextArea();
        resourceBQ = new JPanel();
        textArea6 = new JTextArea();
        bufferBQ = new JPanel();
        textArea7 = new JTextArea();
        suspendBQ = new JPanel();
        textArea8 = new JTextArea();
        finifshQ = new JPanel();
        textArea9 = new JTextArea();
        disk = new JPanel();
        viewDisk = new JButton();
        scrollPane7 = new JScrollPane();
        diskInfo = new JTextArea();
        label4 = new JLabel();
        label5 = new JLabel();
        cNum = new JTextField();
        label6 = new JLabel();
        hNum = new JTextField();
        label7 = new JLabel();
        sNum = new JTextField();
        filesystem = new JPanel();
        scrollPane8 = new JScrollPane();
        nodeTree = new JTree();
        scrollPane9 = new JScrollPane();
        userOpenFile = new JTable();
        scrollPane10 = new JScrollPane();
        sysOpenFile = new JTable();
        device = new JPanel();
        label520 = new JLabel();
        freeBh = new JPanel();
        label521 = new JLabel();
        label522 = new JLabel();
        label523 = new JLabel();
        label524 = new JLabel();
        label525 = new JLabel();
        label526 = new JLabel();
        label527 = new JLabel();
        label528 = new JLabel();
        label529 = new JLabel();
        label530 = new JLabel();
        label531 = new JLabel();
        label532 = new JLabel();
        label533 = new JLabel();
        label534 = new JLabel();
        label535 = new JLabel();
        label536 = new JLabel();
        devPane = new JScrollPane();
        devLog = new JTextArea();
        resource = new JPanel();
        scrollPane1 = new JScrollPane();
        resourceTable = new JTable();
        label537 = new JLabel();
        scrollPane5 = new JScrollPane();
        keyBlock = new JTextArea();
        label538 = new JLabel();
        scrollPane11 = new JScrollPane();
        screenBlock = new JTextArea();
        label539 = new JLabel();
        scrollPane12 = new JScrollPane();
        otherBlock = new JTextArea();
        label540 = new JLabel();
        memoryView = new JFrame();
        panel1 = new JPanel();
        label8 = new JLabel();
        label9 = new JLabel();
        label10 = new JLabel();
        label11 = new JLabel();
        label12 = new JLabel();
        label13 = new JLabel();
        label14 = new JLabel();
        label15 = new JLabel();
        label16 = new JLabel();
        label17 = new JLabel();
        label18 = new JLabel();
        label19 = new JLabel();
        label20 = new JLabel();
        label21 = new JLabel();
        label22 = new JLabel();
        label23 = new JLabel();
        label24 = new JLabel();
        label25 = new JLabel();
        label26 = new JLabel();
        label27 = new JLabel();
        label28 = new JLabel();
        label29 = new JLabel();
        label30 = new JLabel();
        label31 = new JLabel();
        label32 = new JLabel();
        label33 = new JLabel();
        label34 = new JLabel();
        label35 = new JLabel();
        label36 = new JLabel();
        label37 = new JLabel();
        label38 = new JLabel();
        label39 = new JLabel();
        label40 = new JLabel();
        label41 = new JLabel();
        label42 = new JLabel();
        label43 = new JLabel();
        label44 = new JLabel();
        label45 = new JLabel();
        label46 = new JLabel();
        label47 = new JLabel();
        label48 = new JLabel();
        label49 = new JLabel();
        label50 = new JLabel();
        label51 = new JLabel();
        label52 = new JLabel();
        label53 = new JLabel();
        label54 = new JLabel();
        label55 = new JLabel();
        label56 = new JLabel();
        label57 = new JLabel();
        label58 = new JLabel();
        label59 = new JLabel();
        label60 = new JLabel();
        label61 = new JLabel();
        label62 = new JLabel();
        label63 = new JLabel();
        label64 = new JLabel();
        label65 = new JLabel();
        label66 = new JLabel();
        label67 = new JLabel();
        label68 = new JLabel();
        label69 = new JLabel();
        label70 = new JLabel();
        label71 = new JLabel();
        label72 = new JLabel();
        label73 = new JLabel();
        label74 = new JLabel();
        label75 = new JLabel();
        label76 = new JLabel();
        label77 = new JLabel();
        label78 = new JLabel();
        label79 = new JLabel();
        label80 = new JLabel();
        label81 = new JLabel();
        label82 = new JLabel();
        label83 = new JLabel();
        label84 = new JLabel();
        label85 = new JLabel();
        label86 = new JLabel();
        label87 = new JLabel();
        label88 = new JLabel();
        label89 = new JLabel();
        label90 = new JLabel();
        label91 = new JLabel();
        label92 = new JLabel();
        label93 = new JLabel();
        label94 = new JLabel();
        label95 = new JLabel();
        label96 = new JLabel();
        label97 = new JLabel();
        label98 = new JLabel();
        label99 = new JLabel();
        label100 = new JLabel();
        label101 = new JLabel();
        label102 = new JLabel();
        label103 = new JLabel();
        label104 = new JLabel();
        label105 = new JLabel();
        label106 = new JLabel();
        label107 = new JLabel();
        label108 = new JLabel();
        label109 = new JLabel();
        label110 = new JLabel();
        label111 = new JLabel();
        label112 = new JLabel();
        label113 = new JLabel();
        label114 = new JLabel();
        label115 = new JLabel();
        label116 = new JLabel();
        label117 = new JLabel();
        label118 = new JLabel();
        label119 = new JLabel();
        label120 = new JLabel();
        label121 = new JLabel();
        label122 = new JLabel();
        label123 = new JLabel();
        label124 = new JLabel();
        label125 = new JLabel();
        label126 = new JLabel();
        label127 = new JLabel();
        label128 = new JLabel();
        label129 = new JLabel();
        label130 = new JLabel();
        label131 = new JLabel();
        label132 = new JLabel();
        label133 = new JLabel();
        label134 = new JLabel();
        label135 = new JLabel();
        label136 = new JLabel();
        label137 = new JLabel();
        label138 = new JLabel();
        label139 = new JLabel();
        label140 = new JLabel();
        label141 = new JLabel();
        label142 = new JLabel();
        label143 = new JLabel();
        label144 = new JLabel();
        label145 = new JLabel();
        label146 = new JLabel();
        label147 = new JLabel();
        label148 = new JLabel();
        label149 = new JLabel();
        label150 = new JLabel();
        label151 = new JLabel();
        label152 = new JLabel();
        label153 = new JLabel();
        label154 = new JLabel();
        label155 = new JLabel();
        label156 = new JLabel();
        label157 = new JLabel();
        label158 = new JLabel();
        label159 = new JLabel();
        label160 = new JLabel();
        label161 = new JLabel();
        label162 = new JLabel();
        label163 = new JLabel();
        label164 = new JLabel();
        label165 = new JLabel();
        label166 = new JLabel();
        label167 = new JLabel();
        label168 = new JLabel();
        label169 = new JLabel();
        label170 = new JLabel();
        label171 = new JLabel();
        label172 = new JLabel();
        label173 = new JLabel();
        label174 = new JLabel();
        label175 = new JLabel();
        label176 = new JLabel();
        label177 = new JLabel();
        label178 = new JLabel();
        label179 = new JLabel();
        label180 = new JLabel();
        label181 = new JLabel();
        label182 = new JLabel();
        label183 = new JLabel();
        label184 = new JLabel();
        label185 = new JLabel();
        label186 = new JLabel();
        label187 = new JLabel();
        label188 = new JLabel();
        label189 = new JLabel();
        label190 = new JLabel();
        label191 = new JLabel();
        label192 = new JLabel();
        label193 = new JLabel();
        label194 = new JLabel();
        label195 = new JLabel();
        label196 = new JLabel();
        label197 = new JLabel();
        label198 = new JLabel();
        label199 = new JLabel();
        label200 = new JLabel();
        label201 = new JLabel();
        label202 = new JLabel();
        label203 = new JLabel();
        label204 = new JLabel();
        label205 = new JLabel();
        label206 = new JLabel();
        label207 = new JLabel();
        label208 = new JLabel();
        label209 = new JLabel();
        label210 = new JLabel();
        label211 = new JLabel();
        label212 = new JLabel();
        label213 = new JLabel();
        label214 = new JLabel();
        label215 = new JLabel();
        label216 = new JLabel();
        label217 = new JLabel();
        label218 = new JLabel();
        label219 = new JLabel();
        label220 = new JLabel();
        label221 = new JLabel();
        label222 = new JLabel();
        label223 = new JLabel();
        label224 = new JLabel();
        label225 = new JLabel();
        label226 = new JLabel();
        label227 = new JLabel();
        label228 = new JLabel();
        label229 = new JLabel();
        label230 = new JLabel();
        label231 = new JLabel();
        label232 = new JLabel();
        label233 = new JLabel();
        label234 = new JLabel();
        label235 = new JLabel();
        label236 = new JLabel();
        label237 = new JLabel();
        label238 = new JLabel();
        label239 = new JLabel();
        label240 = new JLabel();
        label241 = new JLabel();
        label242 = new JLabel();
        label243 = new JLabel();
        label244 = new JLabel();
        label245 = new JLabel();
        label246 = new JLabel();
        label247 = new JLabel();
        label248 = new JLabel();
        label249 = new JLabel();
        label250 = new JLabel();
        label251 = new JLabel();
        label252 = new JLabel();
        label253 = new JLabel();
        label254 = new JLabel();
        label255 = new JLabel();
        label256 = new JLabel();
        label257 = new JLabel();
        label258 = new JLabel();
        label259 = new JLabel();
        label260 = new JLabel();
        label261 = new JLabel();
        label262 = new JLabel();
        label263 = new JLabel();
        label264 = new JLabel();
        label265 = new JLabel();
        label266 = new JLabel();
        label267 = new JLabel();
        label268 = new JLabel();
        label269 = new JLabel();
        label270 = new JLabel();
        label271 = new JLabel();
        label272 = new JLabel();
        label273 = new JLabel();
        label274 = new JLabel();
        label275 = new JLabel();
        label276 = new JLabel();
        label277 = new JLabel();
        label278 = new JLabel();
        label279 = new JLabel();
        label280 = new JLabel();
        label281 = new JLabel();
        label282 = new JLabel();
        label283 = new JLabel();
        label284 = new JLabel();
        label285 = new JLabel();
        label286 = new JLabel();
        label287 = new JLabel();
        label288 = new JLabel();
        label289 = new JLabel();
        label290 = new JLabel();
        label291 = new JLabel();
        label292 = new JLabel();
        label293 = new JLabel();
        label294 = new JLabel();
        label295 = new JLabel();
        label296 = new JLabel();
        label297 = new JLabel();
        label298 = new JLabel();
        label299 = new JLabel();
        label300 = new JLabel();
        label301 = new JLabel();
        label302 = new JLabel();
        label303 = new JLabel();
        label304 = new JLabel();
        label305 = new JLabel();
        label306 = new JLabel();
        label307 = new JLabel();
        label308 = new JLabel();
        label309 = new JLabel();
        label310 = new JLabel();
        label311 = new JLabel();
        label312 = new JLabel();
        label313 = new JLabel();
        label314 = new JLabel();
        label315 = new JLabel();
        label316 = new JLabel();
        label317 = new JLabel();
        label318 = new JLabel();
        label319 = new JLabel();
        label320 = new JLabel();
        label321 = new JLabel();
        label322 = new JLabel();
        label323 = new JLabel();
        label324 = new JLabel();
        label325 = new JLabel();
        label326 = new JLabel();
        label327 = new JLabel();
        label328 = new JLabel();
        label329 = new JLabel();
        label330 = new JLabel();
        label331 = new JLabel();
        label332 = new JLabel();
        label333 = new JLabel();
        label334 = new JLabel();
        label335 = new JLabel();
        label336 = new JLabel();
        label337 = new JLabel();
        label338 = new JLabel();
        label339 = new JLabel();
        label340 = new JLabel();
        label341 = new JLabel();
        label342 = new JLabel();
        label343 = new JLabel();
        label344 = new JLabel();
        label345 = new JLabel();
        label346 = new JLabel();
        label347 = new JLabel();
        label348 = new JLabel();
        label349 = new JLabel();
        label350 = new JLabel();
        label351 = new JLabel();
        label352 = new JLabel();
        label353 = new JLabel();
        label354 = new JLabel();
        label355 = new JLabel();
        label356 = new JLabel();
        label357 = new JLabel();
        label358 = new JLabel();
        label359 = new JLabel();
        label360 = new JLabel();
        label361 = new JLabel();
        label362 = new JLabel();
        label363 = new JLabel();
        label364 = new JLabel();
        label365 = new JLabel();
        label366 = new JLabel();
        label367 = new JLabel();
        label368 = new JLabel();
        label369 = new JLabel();
        label370 = new JLabel();
        label371 = new JLabel();
        label372 = new JLabel();
        label373 = new JLabel();
        label374 = new JLabel();
        label375 = new JLabel();
        label376 = new JLabel();
        label377 = new JLabel();
        label378 = new JLabel();
        label379 = new JLabel();
        label380 = new JLabel();
        label381 = new JLabel();
        label382 = new JLabel();
        label383 = new JLabel();
        label384 = new JLabel();
        label385 = new JLabel();
        label386 = new JLabel();
        label387 = new JLabel();
        label388 = new JLabel();
        label389 = new JLabel();
        label390 = new JLabel();
        label391 = new JLabel();
        label392 = new JLabel();
        label393 = new JLabel();
        label394 = new JLabel();
        label395 = new JLabel();
        label396 = new JLabel();
        label397 = new JLabel();
        label398 = new JLabel();
        label399 = new JLabel();
        label400 = new JLabel();
        label401 = new JLabel();
        label402 = new JLabel();
        label403 = new JLabel();
        label404 = new JLabel();
        label405 = new JLabel();
        label406 = new JLabel();
        label407 = new JLabel();
        label408 = new JLabel();
        label409 = new JLabel();
        label410 = new JLabel();
        label411 = new JLabel();
        label412 = new JLabel();
        label413 = new JLabel();
        label414 = new JLabel();
        label415 = new JLabel();
        label416 = new JLabel();
        label417 = new JLabel();
        label418 = new JLabel();
        label419 = new JLabel();
        label420 = new JLabel();
        label421 = new JLabel();
        label422 = new JLabel();
        label423 = new JLabel();
        label424 = new JLabel();
        label425 = new JLabel();
        label426 = new JLabel();
        label427 = new JLabel();
        label428 = new JLabel();
        label429 = new JLabel();
        label430 = new JLabel();
        label431 = new JLabel();
        label432 = new JLabel();
        label433 = new JLabel();
        label434 = new JLabel();
        label435 = new JLabel();
        label436 = new JLabel();
        label437 = new JLabel();
        label438 = new JLabel();
        label439 = new JLabel();
        label440 = new JLabel();
        label441 = new JLabel();
        label442 = new JLabel();
        label443 = new JLabel();
        label444 = new JLabel();
        label445 = new JLabel();
        label446 = new JLabel();
        label447 = new JLabel();
        label448 = new JLabel();
        label449 = new JLabel();
        label450 = new JLabel();
        label451 = new JLabel();
        label452 = new JLabel();
        label453 = new JLabel();
        label454 = new JLabel();
        label455 = new JLabel();
        label456 = new JLabel();
        label457 = new JLabel();
        label458 = new JLabel();
        label459 = new JLabel();
        label460 = new JLabel();
        label461 = new JLabel();
        label462 = new JLabel();
        label463 = new JLabel();
        label464 = new JLabel();
        label465 = new JLabel();
        label466 = new JLabel();
        label467 = new JLabel();
        label468 = new JLabel();
        label469 = new JLabel();
        label470 = new JLabel();
        label471 = new JLabel();
        label472 = new JLabel();
        label473 = new JLabel();
        label474 = new JLabel();
        label475 = new JLabel();
        label476 = new JLabel();
        label477 = new JLabel();
        label478 = new JLabel();
        label479 = new JLabel();
        label480 = new JLabel();
        label481 = new JLabel();
        label482 = new JLabel();
        label483 = new JLabel();
        label484 = new JLabel();
        label485 = new JLabel();
        label486 = new JLabel();
        label487 = new JLabel();
        label488 = new JLabel();
        label489 = new JLabel();
        label490 = new JLabel();
        label491 = new JLabel();
        label492 = new JLabel();
        label493 = new JLabel();
        label494 = new JLabel();
        label495 = new JLabel();
        label496 = new JLabel();
        label497 = new JLabel();
        label498 = new JLabel();
        label499 = new JLabel();
        label500 = new JLabel();
        label501 = new JLabel();
        label502 = new JLabel();
        label503 = new JLabel();
        label504 = new JLabel();
        label505 = new JLabel();
        label506 = new JLabel();
        label507 = new JLabel();
        label508 = new JLabel();
        label509 = new JLabel();
        label510 = new JLabel();
        label511 = new JLabel();
        label512 = new JLabel();
        label513 = new JLabel();
        label514 = new JLabel();
        label515 = new JLabel();
        label516 = new JLabel();
        label517 = new JLabel();
        label518 = new JLabel();
        label519 = new JLabel();
        dialog1 = new JDialog();
        choosedisk = new JButton();
        choosejob = new JButton();
        button2 = new JButton();
        progressBar1 = new JProgressBar();

        //======== this ========
        setTitle("Einux");
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== mainTabPanel ========
        {
            mainTabPanel.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));

            //======== console ========
            {
                console.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing.
                border.EmptyBorder(0,0,0,0), "JF\u006frmDesi\u0067ner Ev\u0061luatio\u006e",javax.swing.border.TitledBorder.CENTER
                ,javax.swing.border.TitledBorder.BOTTOM,new java.awt.Font("Dialo\u0067",java.awt.Font
                .BOLD,12),java.awt.Color.red),console. getBorder()));console. addPropertyChangeListener(
                new java.beans.PropertyChangeListener(){@Override public void propertyChange(java.beans.PropertyChangeEvent e){if("borde\u0072"
                .equals(e.getPropertyName()))throw new RuntimeException();}});
                console.setLayout(null);

                //======== pageTablePanel ========
                {

                    //---- pageTable ----
                    pageTable.setModel(new DefaultTableModel(
                        new Object[][] {
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {"", "", "", "", ""},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                            {null, null, null, null, null},
                        },
                        new String[] {
                            "\u903b\u8f91\u9875\u53f7", "\u9875\u6846\u53f7", "\u5916\u5b58\u5757\u53f7", "\u4fee\u6539\u4f4d", "\u6709\u6548\u4f4d"
                        }
                    ));
                    pageTable.setPreferredScrollableViewportSize(new Dimension(400, 400));
                    pageTable.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    pageTablePanel.setViewportView(pageTable);
                }
                console.add(pageTablePanel);
                pageTablePanel.setBounds(0, 230, pageTablePanel.getPreferredSize().width, 193);

                //======== consoletoolBar1 ========
                {

                    //---- start ----
                    start.setText("\u5f00\u673a");
                    start.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    start.addActionListener(e -> startActionPerformed(e));
                    consoletoolBar1.add(start);

                    //---- button1 ----
                    button1.setText("\u65b0\u4f5c\u4e1a");
                    button1.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    button1.addActionListener(e -> button1ActionPerformed(e));
                    consoletoolBar1.add(button1);

                    //---- pause ----
                    pause.setText("\u6682\u505c");
                    pause.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    pause.addActionListener(e -> pauseActionPerformed(e));
                    consoletoolBar1.add(pause);

                    //---- resume ----
                    resume.setText("\u7ee7\u7eed");
                    resume.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    resume.addActionListener(e -> resumeActionPerformed(e));
                    consoletoolBar1.add(resume);

                    //---- stop ----
                    stop.setText("\u7ed3\u675f");
                    stop.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    stop.addActionListener(e -> stopActionPerformed(e));
                    consoletoolBar1.add(stop);
                }
                console.add(consoletoolBar1);
                consoletoolBar1.setBounds(new Rectangle(new Point(265, 0), consoletoolBar1.getPreferredSize()));

                //---- systemTime ----
                systemTime.setPreferredSize(new Dimension(100, 38));
                systemTime.setEditable(false);
                systemTime.setHorizontalAlignment(SwingConstants.CENTER);
                console.add(systemTime);
                systemTime.setBounds(new Rectangle(new Point(100, 60), systemTime.getPreferredSize()));

                //---- label1 ----
                label1.setText("\u7cfb\u7edf\u65f6\u95f4");
                label1.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                console.add(label1);
                label1.setBounds(25, 60, 70, 30);

                //---- label2 ----
                label2.setText("\u5f53\u524d\u8fdb\u7a0b");
                label2.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                console.add(label2);
                label2.setBounds(new Rectangle(new Point(25, 115), label2.getPreferredSize()));

                //---- currentPCB ----
                currentPCB.setPreferredSize(new Dimension(100, 38));
                currentPCB.setEditable(false);
                currentPCB.setHorizontalAlignment(SwingConstants.CENTER);
                console.add(currentPCB);
                currentPCB.setBounds(new Rectangle(new Point(100, 110), currentPCB.getPreferredSize()));

                //---- label3 ----
                label3.setText("\u5f53\u524d\u6307\u4ee4");
                label3.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                console.add(label3);
                label3.setBounds(new Rectangle(new Point(25, 165), label3.getPreferredSize()));

                //---- currentPC ----
                currentPC.setPreferredSize(new Dimension(100, 38));
                currentPC.setEditable(false);
                currentPC.setHorizontalAlignment(SwingConstants.CENTER);
                console.add(currentPC);
                currentPC.setBounds(new Rectangle(new Point(100, 160), currentPC.getPreferredSize()));

                //======== baseTabPanel ========
                {
                    baseTabPanel.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));

                    //======== panel6 ========
                    {
                        panel6.setLayout(null);

                        //======== scrollPane2 ========
                        {

                            //---- cpuRegInfo ----
                            cpuRegInfo.setPreferredSize(new Dimension(100, 22));
                            scrollPane2.setViewportView(cpuRegInfo);
                        }
                        panel6.add(scrollPane2);
                        scrollPane2.setBounds(0, 0, 330, 90);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < panel6.getComponentCount(); i++) {
                                Rectangle bounds = panel6.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel6.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel6.setMinimumSize(preferredSize);
                            panel6.setPreferredSize(preferredSize);
                        }
                    }
                    baseTabPanel.addTab("CPU\u4fe1\u606f", panel6);

                    //======== panel7 ========
                    {
                        panel7.setLayout(null);

                        //======== scrollPane3 ========
                        {

                            //---- pcbRegInfo ----
                            pcbRegInfo.setPreferredSize(new Dimension(100, 22));
                            scrollPane3.setViewportView(pcbRegInfo);
                        }
                        panel7.add(scrollPane3);
                        scrollPane3.setBounds(0, 0, 330, 90);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < panel7.getComponentCount(); i++) {
                                Rectangle bounds = panel7.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel7.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel7.setMinimumSize(preferredSize);
                            panel7.setPreferredSize(preferredSize);
                        }
                    }
                    baseTabPanel.addTab("\u8fdb\u7a0b\u4fe1\u606f", panel7);

                    //======== panel8 ========
                    {
                        panel8.setLayout(null);

                        //======== scrollPane4 ========
                        {

                            //---- instructionInfo ----
                            instructionInfo.setPreferredSize(new Dimension(100, 22));
                            scrollPane4.setViewportView(instructionInfo);
                        }
                        panel8.add(scrollPane4);
                        scrollPane4.setBounds(0, 0, 330, 90);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < panel8.getComponentCount(); i++) {
                                Rectangle bounds = panel8.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel8.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel8.setMinimumSize(preferredSize);
                            panel8.setPreferredSize(preferredSize);
                        }
                    }
                    baseTabPanel.addTab("\u6307\u4ee4\u4fe1\u606f", panel8);
                }
                console.add(baseTabPanel);
                baseTabPanel.setBounds(215, 65, 330, 130);

                //======== interrupt ========
                {
                    interrupt.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    interrupt.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                    //---- interruptInfo ----
                    interruptInfo.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
                    interruptInfo.setForeground(Color.red);
                    interruptInfo.setEditable(false);
                    interrupt.setViewportView(interruptInfo);
                }
                console.add(interrupt);
                interrupt.setBounds(560, 65, 405, 130);

                //======== interrupt2 ========
                {
                    interrupt2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    interrupt2.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                    //---- interruptInfo2 ----
                    interruptInfo2.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
                    interruptInfo2.setForeground(Color.black);
                    interruptInfo2.setEditable(false);
                    interrupt2.setViewportView(interruptInfo2);
                }
                console.add(interrupt2);
                interrupt2.setBounds(450, 230, 515, 185);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < console.getComponentCount(); i++) {
                        Rectangle bounds = console.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = console.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    console.setMinimumSize(preferredSize);
                    console.setPreferredSize(preferredSize);
                }
            }
            mainTabPanel.addTab("\u63a7\u5236\u53f0", console);

            //======== memory ========
            {
                memory.setLayout(new BorderLayout());

                //======== bitmap ========
                {
                    bitmap.setLayout(new GridLayout(4, 16));

                    //---- m0 ----
                    m0.setText("0");
                    m0.addActionListener(e -> {
			m0ActionPerformed(e);
			m0ActionPerformed(e);
		});
                    bitmap.add(m0);

                    //---- m1 ----
                    m1.setText("0");
                    m1.addActionListener(e -> m1ActionPerformed(e));
                    bitmap.add(m1);

                    //---- m2 ----
                    m2.setText("0");
                    m2.addActionListener(e -> m2ActionPerformed(e));
                    bitmap.add(m2);

                    //---- m3 ----
                    m3.setText("0");
                    m3.addActionListener(e -> m3ActionPerformed(e));
                    bitmap.add(m3);

                    //---- m4 ----
                    m4.setText("0");
                    m4.addActionListener(e -> m4ActionPerformed(e));
                    bitmap.add(m4);

                    //---- m5 ----
                    m5.setText("0");
                    m5.addActionListener(e -> m5ActionPerformed(e));
                    bitmap.add(m5);

                    //---- m6 ----
                    m6.setText("0");
                    m6.addActionListener(e -> m6ActionPerformed(e));
                    bitmap.add(m6);

                    //---- m7 ----
                    m7.setText("0");
                    m7.addActionListener(e -> m7ActionPerformed(e));
                    bitmap.add(m7);

                    //---- m8 ----
                    m8.setText("0");
                    m8.addActionListener(e -> m8ActionPerformed(e));
                    bitmap.add(m8);

                    //---- m9 ----
                    m9.setText("0");
                    m9.addActionListener(e -> m9ActionPerformed(e));
                    bitmap.add(m9);

                    //---- m10 ----
                    m10.setText("0");
                    m10.addActionListener(e -> m10ActionPerformed(e));
                    bitmap.add(m10);

                    //---- m11 ----
                    m11.setText("0");
                    m11.addActionListener(e -> m11ActionPerformed(e));
                    bitmap.add(m11);

                    //---- m12 ----
                    m12.setText("0");
                    m12.addActionListener(e -> m12ActionPerformed(e));
                    bitmap.add(m12);

                    //---- m13 ----
                    m13.setText("0");
                    m13.addActionListener(e -> m13ActionPerformed(e));
                    bitmap.add(m13);

                    //---- m14 ----
                    m14.setText("0");
                    m14.addActionListener(e -> m14ActionPerformed(e));
                    bitmap.add(m14);

                    //---- m15 ----
                    m15.setText("0");
                    m15.addActionListener(e -> m15ActionPerformed(e));
                    bitmap.add(m15);

                    //---- m16 ----
                    m16.setText("0");
                    m16.addActionListener(e -> m16ActionPerformed(e));
                    bitmap.add(m16);

                    //---- m17 ----
                    m17.setText("0");
                    m17.addActionListener(e -> m17ActionPerformed(e));
                    bitmap.add(m17);

                    //---- m18 ----
                    m18.setText("0");
                    m18.addActionListener(e -> m18ActionPerformed(e));
                    bitmap.add(m18);

                    //---- m19 ----
                    m19.setText("0");
                    m19.addActionListener(e -> m19ActionPerformed(e));
                    bitmap.add(m19);

                    //---- m20 ----
                    m20.setText("0");
                    m20.addActionListener(e -> m20ActionPerformed(e));
                    bitmap.add(m20);

                    //---- m21 ----
                    m21.setText("0");
                    m21.addActionListener(e -> m21ActionPerformed(e));
                    bitmap.add(m21);

                    //---- m22 ----
                    m22.setText("0");
                    m22.addActionListener(e -> m22ActionPerformed(e));
                    bitmap.add(m22);

                    //---- m23 ----
                    m23.setText("0");
                    m23.addActionListener(e -> m23ActionPerformed(e));
                    bitmap.add(m23);

                    //---- m24 ----
                    m24.setText("0");
                    m24.addActionListener(e -> m24ActionPerformed(e));
                    bitmap.add(m24);

                    //---- m25 ----
                    m25.setText("0");
                    m25.addActionListener(e -> m25ActionPerformed(e));
                    bitmap.add(m25);

                    //---- m26 ----
                    m26.setText("0");
                    m26.addActionListener(e -> m26ActionPerformed(e));
                    bitmap.add(m26);

                    //---- m27 ----
                    m27.setText("0");
                    m27.addActionListener(e -> m27ActionPerformed(e));
                    bitmap.add(m27);

                    //---- m28 ----
                    m28.setText("0");
                    m28.addActionListener(e -> m28ActionPerformed(e));
                    bitmap.add(m28);

                    //---- m29 ----
                    m29.setText("0");
                    m29.addActionListener(e -> m29ActionPerformed(e));
                    bitmap.add(m29);

                    //---- m30 ----
                    m30.setText("0");
                    m30.addActionListener(e -> m30ActionPerformed(e));
                    bitmap.add(m30);

                    //---- m31 ----
                    m31.setText("0");
                    m31.addActionListener(e -> m31ActionPerformed(e));
                    bitmap.add(m31);

                    //---- m32 ----
                    m32.setText("0");
                    m32.addActionListener(e -> m32ActionPerformed(e));
                    bitmap.add(m32);

                    //---- m33 ----
                    m33.setText("0");
                    m33.addActionListener(e -> m33ActionPerformed(e));
                    bitmap.add(m33);

                    //---- m34 ----
                    m34.setText("0");
                    m34.addActionListener(e -> m34ActionPerformed(e));
                    bitmap.add(m34);

                    //---- m35 ----
                    m35.setText("0");
                    m35.addActionListener(e -> m35ActionPerformed(e));
                    bitmap.add(m35);

                    //---- m36 ----
                    m36.setText("0");
                    m36.addActionListener(e -> m36ActionPerformed(e));
                    bitmap.add(m36);

                    //---- m37 ----
                    m37.setText("0");
                    m37.addActionListener(e -> m37ActionPerformed(e));
                    bitmap.add(m37);

                    //---- m38 ----
                    m38.setText("0");
                    m38.addActionListener(e -> m38ActionPerformed(e));
                    bitmap.add(m38);

                    //---- m39 ----
                    m39.setText("0");
                    m39.addActionListener(e -> m39ActionPerformed(e));
                    bitmap.add(m39);

                    //---- m40 ----
                    m40.setText("0");
                    m40.addActionListener(e -> m40ActionPerformed(e));
                    bitmap.add(m40);

                    //---- m41 ----
                    m41.setText("0");
                    m41.addActionListener(e -> m41ActionPerformed(e));
                    bitmap.add(m41);

                    //---- m42 ----
                    m42.setText("0");
                    m42.addActionListener(e -> m42ActionPerformed(e));
                    bitmap.add(m42);

                    //---- m43 ----
                    m43.setText("0");
                    m43.addActionListener(e -> m43ActionPerformed(e));
                    bitmap.add(m43);

                    //---- m44 ----
                    m44.setText("0");
                    m44.addActionListener(e -> m44ActionPerformed(e));
                    bitmap.add(m44);

                    //---- m45 ----
                    m45.setText("0");
                    m45.addActionListener(e -> m45ActionPerformed(e));
                    bitmap.add(m45);

                    //---- m46 ----
                    m46.setText("0");
                    m46.addActionListener(e -> m46ActionPerformed(e));
                    bitmap.add(m46);

                    //---- m47 ----
                    m47.setText("0");
                    m47.addActionListener(e -> m47ActionPerformed(e));
                    bitmap.add(m47);

                    //---- m48 ----
                    m48.setText("0");
                    m48.addActionListener(e -> m48ActionPerformed(e));
                    bitmap.add(m48);

                    //---- m49 ----
                    m49.setText("0");
                    m49.addActionListener(e -> m49ActionPerformed(e));
                    bitmap.add(m49);

                    //---- m50 ----
                    m50.setText("0");
                    m50.addActionListener(e -> m50ActionPerformed(e));
                    bitmap.add(m50);

                    //---- m51 ----
                    m51.setText("0");
                    m51.addActionListener(e -> m51ActionPerformed(e));
                    bitmap.add(m51);

                    //---- m52 ----
                    m52.setText("0");
                    m52.addActionListener(e -> m52ActionPerformed(e));
                    bitmap.add(m52);

                    //---- m53 ----
                    m53.setText("0");
                    m53.addActionListener(e -> m53ActionPerformed(e));
                    bitmap.add(m53);

                    //---- m54 ----
                    m54.setText("0");
                    m54.addActionListener(e -> m54ActionPerformed(e));
                    bitmap.add(m54);

                    //---- m55 ----
                    m55.setText("0");
                    m55.addActionListener(e -> m55ActionPerformed(e));
                    bitmap.add(m55);

                    //---- m56 ----
                    m56.setText("0");
                    m56.addActionListener(e -> m56ActionPerformed(e));
                    bitmap.add(m56);

                    //---- m57 ----
                    m57.setText("0");
                    m57.addActionListener(e -> m57ActionPerformed(e));
                    bitmap.add(m57);

                    //---- m58 ----
                    m58.setText("0");
                    m58.addActionListener(e -> m58ActionPerformed(e));
                    bitmap.add(m58);

                    //---- m59 ----
                    m59.setText("0");
                    m59.addActionListener(e -> m59ActionPerformed(e));
                    bitmap.add(m59);

                    //---- m60 ----
                    m60.setText("0");
                    m60.addActionListener(e -> m60ActionPerformed(e));
                    bitmap.add(m60);

                    //---- m61 ----
                    m61.setText("0");
                    m61.addActionListener(e -> m61ActionPerformed(e));
                    bitmap.add(m61);

                    //---- m62 ----
                    m62.setText("0");
                    m62.addActionListener(e -> m62ActionPerformed(e));
                    bitmap.add(m62);

                    //---- m63 ----
                    m63.setText("0");
                    m63.addActionListener(e -> m63ActionPerformed(e));
                    bitmap.add(m63);
                }
                memory.add(bitmap, BorderLayout.CENTER);
            }
            mainTabPanel.addTab("\u5185\u5b58\u7ba1\u7406", memory);

            //======== process ========
            {
                process.setLayout(null);

                //======== tabbedPane3 ========
                {
                    tabbedPane3.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));

                    //======== readyQ ========
                    {
                        readyQ.setLayout(null);

                        //======== scrollPane6 ========
                        {

                            //---- textArea5 ----
                            textArea5.setMinimumSize(new Dimension(100, 22));
                            textArea5.setPreferredSize(new Dimension(100, 22));
                            scrollPane6.setViewportView(textArea5);
                        }
                        readyQ.add(scrollPane6);
                        scrollPane6.setBounds(0, 0, 810, 205);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < readyQ.getComponentCount(); i++) {
                                Rectangle bounds = readyQ.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = readyQ.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            readyQ.setMinimumSize(preferredSize);
                            readyQ.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u5c31\u7eea\u961f\u5217", readyQ);

                    //======== resourceBQ ========
                    {
                        resourceBQ.setLayout(null);

                        //---- textArea6 ----
                        textArea6.setMinimumSize(new Dimension(100, 22));
                        textArea6.setPreferredSize(new Dimension(100, 22));
                        resourceBQ.add(textArea6);
                        textArea6.setBounds(0, 0, 808, 203);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < resourceBQ.getComponentCount(); i++) {
                                Rectangle bounds = resourceBQ.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = resourceBQ.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            resourceBQ.setMinimumSize(preferredSize);
                            resourceBQ.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u8d44\u6e90\u963b\u585e\u961f\u5217", resourceBQ);

                    //======== bufferBQ ========
                    {
                        bufferBQ.setLayout(null);

                        //---- textArea7 ----
                        textArea7.setMinimumSize(new Dimension(100, 22));
                        textArea7.setPreferredSize(new Dimension(100, 22));
                        bufferBQ.add(textArea7);
                        textArea7.setBounds(0, 0, 808, 203);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < bufferBQ.getComponentCount(); i++) {
                                Rectangle bounds = bufferBQ.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = bufferBQ.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            bufferBQ.setMinimumSize(preferredSize);
                            bufferBQ.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u7f13\u51b2\u533a\u963b\u585e\u961f\u5217", bufferBQ);

                    //======== suspendBQ ========
                    {
                        suspendBQ.setLayout(null);

                        //---- textArea8 ----
                        textArea8.setMinimumSize(new Dimension(100, 22));
                        textArea8.setPreferredSize(new Dimension(100, 22));
                        suspendBQ.add(textArea8);
                        textArea8.setBounds(0, 0, 808, 203);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < suspendBQ.getComponentCount(); i++) {
                                Rectangle bounds = suspendBQ.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = suspendBQ.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            suspendBQ.setMinimumSize(preferredSize);
                            suspendBQ.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u6302\u8d77\u961f\u5217", suspendBQ);

                    //======== finifshQ ========
                    {
                        finifshQ.setLayout(null);

                        //---- textArea9 ----
                        textArea9.setMinimumSize(new Dimension(100, 22));
                        textArea9.setPreferredSize(new Dimension(100, 22));
                        finifshQ.add(textArea9);
                        textArea9.setBounds(0, 0, 808, 203);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for(int i = 0; i < finifshQ.getComponentCount(); i++) {
                                Rectangle bounds = finifshQ.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = finifshQ.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            finifshQ.setMinimumSize(preferredSize);
                            finifshQ.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u5df2\u5b8c\u6210\u961f\u5217", finifshQ);
                }
                process.add(tabbedPane3);
                tabbedPane3.setBounds(65, 0, 810, 245);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < process.getComponentCount(); i++) {
                        Rectangle bounds = process.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = process.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    process.setMinimumSize(preferredSize);
                    process.setPreferredSize(preferredSize);
                }
            }
            mainTabPanel.addTab("\u8fdb\u7a0b\u7ba1\u7406", process);

            //======== disk ========
            {
                disk.setLayout(null);

                //---- viewDisk ----
                viewDisk.setText("\u67e5\u770b\u78c1\u76d8");
                viewDisk.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                viewDisk.addActionListener(e -> viewDiskActionPerformed(e));
                disk.add(viewDisk);
                viewDisk.setBounds(new Rectangle(new Point(410, 25), viewDisk.getPreferredSize()));

                //======== scrollPane7 ========
                {

                    //---- diskInfo ----
                    diskInfo.setPreferredSize(new Dimension(100, 22));
                    diskInfo.setEditable(false);
                    diskInfo.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    scrollPane7.setViewportView(diskInfo);
                }
                disk.add(scrollPane7);
                scrollPane7.setBounds(450, 130, 465, 205);

                //---- label4 ----
                label4.setText("\u78c1\u76d8\u4fe1\u606f");
                label4.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                disk.add(label4);
                label4.setBounds(new Rectangle(new Point(650, 95), label4.getPreferredSize()));

                //---- label5 ----
                label5.setText("\u67f1\u9762\u6570");
                label5.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                disk.add(label5);
                label5.setBounds(95, 120, 55, 30);

                //---- cNum ----
                cNum.setPreferredSize(new Dimension(100, 38));
                cNum.setEditable(false);
                cNum.setHorizontalAlignment(SwingConstants.CENTER);
                disk.add(cNum);
                cNum.setBounds(new Rectangle(new Point(185, 120), cNum.getPreferredSize()));

                //---- label6 ----
                label6.setText("\u78c1\u9053\u6570");
                label6.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                disk.add(label6);
                label6.setBounds(new Rectangle(new Point(95, 195), label6.getPreferredSize()));

                //---- hNum ----
                hNum.setPreferredSize(new Dimension(100, 38));
                hNum.setEditable(false);
                hNum.setHorizontalAlignment(SwingConstants.CENTER);
                disk.add(hNum);
                hNum.setBounds(new Rectangle(new Point(185, 190), hNum.getPreferredSize()));

                //---- label7 ----
                label7.setText("\u6247\u533a\u6570");
                label7.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                disk.add(label7);
                label7.setBounds(new Rectangle(new Point(95, 270), label7.getPreferredSize()));

                //---- sNum ----
                sNum.setPreferredSize(new Dimension(100, 38));
                sNum.setEditable(false);
                sNum.setHorizontalAlignment(SwingConstants.CENTER);
                disk.add(sNum);
                sNum.setBounds(new Rectangle(new Point(185, 265), sNum.getPreferredSize()));

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < disk.getComponentCount(); i++) {
                        Rectangle bounds = disk.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = disk.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    disk.setMinimumSize(preferredSize);
                    disk.setPreferredSize(preferredSize);
                }
            }
            mainTabPanel.addTab("\u78c1\u76d8\u7ba1\u7406", disk);

            //======== filesystem ========
            {
                filesystem.setLayout(null);

                //======== scrollPane8 ========
                {

                    //---- nodeTree ----
                    nodeTree.setModel(new DefaultTreeModel(
                        new DefaultMutableTreeNode("root") {
                            {
                                DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("home");
                                    node1.add(new DefaultMutableTreeNode("zach"));
                                add(node1);
                                node1 = new DefaultMutableTreeNode("dev");
                                    node1.add(new DefaultMutableTreeNode("block"));
                                    node1.add(new DefaultMutableTreeNode("tty"));
                                add(node1);
                                node1 = new DefaultMutableTreeNode("etc");
                                    node1.add(new DefaultMutableTreeNode("ssh"));
                                add(node1);
                            }
                        }));
                    nodeTree.setEditable(true);
                    scrollPane8.setViewportView(nodeTree);
                }
                filesystem.add(scrollPane8);
                scrollPane8.setBounds(0, 0, 221, 425);

                //======== scrollPane9 ========
                {

                    //---- userOpenFile ----
                    userOpenFile.setModel(new DefaultTableModel(
                        new Object[][] {
                            {null, null},
                            {null, null},
                            {null, null},
                            {"", ""},
                            {null, null},
                            {null, null},
                            {null, null},
                            {null, null},
                            {null, null},
                            {null, null},
                        },
                        new String[] {
                            "\u7528\u6237\u6253\u5f00\u6587\u4ef6\u63cf\u8ff0\u7b26", "\u7cfb\u7edf\u6253\u5f00\u6587\u4ef6\u63cf\u8ff0\u7b26"
                        }
                    ));
                    userOpenFile.setPreferredScrollableViewportSize(new Dimension(300, 400));
                    userOpenFile.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    userOpenFile.setPreferredSize(new Dimension(100, 250));
                    scrollPane9.setViewportView(userOpenFile);
                }
                filesystem.add(scrollPane9);
                scrollPane9.setBounds(220, 0, 305, 425);

                //======== scrollPane10 ========
                {

                    //---- sysOpenFile ----
                    sysOpenFile.setModel(new DefaultTableModel(
                        new Object[][] {
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null},
                            {"", "", "", ""},
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null},
                            {null, null, null, null},
                        },
                        new String[] {
                            "\u7cfb\u7edf\u6253\u5f00\u6587\u4ef6\u63cf\u8ff0\u7b26", "\u6587\u4ef6\u5f15\u7528\u6570", "\u6587\u4ef6\u504f\u79fb", "inode\u53f7"
                        }
                    ));
                    sysOpenFile.setPreferredScrollableViewportSize(new Dimension(400, 400));
                    sysOpenFile.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                    scrollPane10.setViewportView(sysOpenFile);
                }
                filesystem.add(scrollPane10);
                scrollPane10.setBounds(525, 0, 460, 425);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < filesystem.getComponentCount(); i++) {
                        Rectangle bounds = filesystem.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = filesystem.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    filesystem.setMinimumSize(preferredSize);
                    filesystem.setPreferredSize(preferredSize);
                }
            }
            mainTabPanel.addTab("\u6587\u4ef6\u7cfb\u7edf", filesystem);

            //======== device ========
            {
                device.setLayout(null);

                //---- label520 ----
                label520.setText("\u7a7a\u95f2\u7f13\u51b2\u533a\u961f\u5217");
                label520.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                device.add(label520);
                label520.setBounds(new Rectangle(new Point(65, 65), label520.getPreferredSize()));

                //======== freeBh ========
                {
                    freeBh.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
                    freeBh.setPreferredSize(new Dimension(100, 100));
                    freeBh.setLayout(new GridLayout(1, 16));

                    //---- label521 ----
                    label521.setText("text");
                    label521.setBorder(new LineBorder(Color.black, 1, true));
                    label521.setHorizontalAlignment(SwingConstants.CENTER);
                    label521.setOpaque(true);
                    freeBh.add(label521);

                    //---- label522 ----
                    label522.setText("text");
                    label522.setBorder(new LineBorder(Color.black, 1, true));
                    label522.setHorizontalAlignment(SwingConstants.CENTER);
                    label522.setOpaque(true);
                    freeBh.add(label522);

                    //---- label523 ----
                    label523.setText("text");
                    label523.setBorder(new LineBorder(Color.black, 1, true));
                    label523.setHorizontalAlignment(SwingConstants.CENTER);
                    label523.setOpaque(true);
                    freeBh.add(label523);

                    //---- label524 ----
                    label524.setText("text");
                    label524.setBorder(new LineBorder(Color.black, 1, true));
                    label524.setHorizontalAlignment(SwingConstants.CENTER);
                    label524.setOpaque(true);
                    freeBh.add(label524);

                    //---- label525 ----
                    label525.setText("text");
                    label525.setBorder(new LineBorder(Color.black, 1, true));
                    label525.setHorizontalAlignment(SwingConstants.CENTER);
                    label525.setOpaque(true);
                    freeBh.add(label525);

                    //---- label526 ----
                    label526.setText("text");
                    label526.setBorder(new LineBorder(Color.black, 1, true));
                    label526.setHorizontalAlignment(SwingConstants.CENTER);
                    label526.setOpaque(true);
                    freeBh.add(label526);

                    //---- label527 ----
                    label527.setText("text");
                    label527.setBorder(new LineBorder(Color.black, 1, true));
                    label527.setHorizontalAlignment(SwingConstants.CENTER);
                    label527.setOpaque(true);
                    freeBh.add(label527);

                    //---- label528 ----
                    label528.setText("text");
                    label528.setBorder(new LineBorder(Color.black, 1, true));
                    label528.setHorizontalAlignment(SwingConstants.CENTER);
                    label528.setOpaque(true);
                    freeBh.add(label528);

                    //---- label529 ----
                    label529.setText("text");
                    label529.setBorder(new LineBorder(Color.black, 1, true));
                    label529.setHorizontalAlignment(SwingConstants.CENTER);
                    label529.setOpaque(true);
                    freeBh.add(label529);

                    //---- label530 ----
                    label530.setText("text");
                    label530.setBorder(new LineBorder(Color.black, 1, true));
                    label530.setHorizontalAlignment(SwingConstants.CENTER);
                    label530.setOpaque(true);
                    freeBh.add(label530);

                    //---- label531 ----
                    label531.setText("text");
                    label531.setBorder(new LineBorder(Color.black, 1, true));
                    label531.setHorizontalAlignment(SwingConstants.CENTER);
                    label531.setOpaque(true);
                    freeBh.add(label531);

                    //---- label532 ----
                    label532.setText("text");
                    label532.setBorder(new LineBorder(Color.black, 1, true));
                    label532.setHorizontalAlignment(SwingConstants.CENTER);
                    label532.setOpaque(true);
                    freeBh.add(label532);

                    //---- label533 ----
                    label533.setText("text");
                    label533.setBorder(new LineBorder(Color.black, 1, true));
                    label533.setHorizontalAlignment(SwingConstants.CENTER);
                    label533.setOpaque(true);
                    freeBh.add(label533);

                    //---- label534 ----
                    label534.setText("text");
                    label534.setBorder(new LineBorder(Color.black, 1, true));
                    label534.setHorizontalAlignment(SwingConstants.CENTER);
                    label534.setOpaque(true);
                    freeBh.add(label534);

                    //---- label535 ----
                    label535.setText("text");
                    label535.setBorder(new LineBorder(Color.black, 1, true));
                    label535.setHorizontalAlignment(SwingConstants.CENTER);
                    label535.setOpaque(true);
                    freeBh.add(label535);

                    //---- label536 ----
                    label536.setText("text");
                    label536.setBorder(new LineBorder(Color.black, 1, true));
                    label536.setHorizontalAlignment(SwingConstants.CENTER);
                    label536.setOpaque(true);
                    freeBh.add(label536);
                }
                device.add(freeBh);
                freeBh.setBounds(205, 50, 675, 55);

                //======== devPane ========
                {
                    devPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    devPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

                    //---- devLog ----
                    devLog.setFont(new Font("\u9ed1\u4f53", Font.PLAIN, 14));
                    devLog.setForeground(Color.black);
                    devLog.setEditable(false);
                    devPane.setViewportView(devLog);
                }
                device.add(devPane);
                devPane.setBounds(70, 140, 810, 225);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < device.getComponentCount(); i++) {
                        Rectangle bounds = device.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = device.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    device.setMinimumSize(preferredSize);
                    device.setPreferredSize(preferredSize);
                }
            }
            mainTabPanel.addTab("\u8bbe\u5907\u7ba1\u7406", device);

            //======== resource ========
            {
                resource.setLayout(null);

                //======== scrollPane1 ========
                {

                    //---- resourceTable ----
                    resourceTable.setModel(new DefaultTableModel(
                        new Object[][] {
                            {null, null},
                            {null, null},
                            {null, null},
                        },
                        new String[] {
                            "\u8d44\u6e90\u7c7b\u578b", "\u53ef\u7528\u503c"
                        }
                    ));
                    scrollPane1.setViewportView(resourceTable);
                }
                resource.add(scrollPane1);
                scrollPane1.setBounds(0, 0, scrollPane1.getPreferredSize().width, 425);

                //---- label537 ----
                label537.setText("KEYBOARD");
                label537.setHorizontalAlignment(SwingConstants.CENTER);
                label537.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                resource.add(label537);
                label537.setBounds(500, 85, 90, 35);

                //======== scrollPane5 ========
                {

                    //---- keyBlock ----
                    keyBlock.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 14));
                    keyBlock.setEditable(false);
                    scrollPane5.setViewportView(keyBlock);
                }
                resource.add(scrollPane5);
                scrollPane5.setBounds(505, 125, 85, 240);

                //---- label538 ----
                label538.setText("SCREEN");
                label538.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                label538.setHorizontalAlignment(SwingConstants.CENTER);
                resource.add(label538);
                label538.setBounds(660, 90, 80, 20);

                //======== scrollPane11 ========
                {

                    //---- screenBlock ----
                    screenBlock.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 14));
                    screenBlock.setEditable(false);
                    scrollPane11.setViewportView(screenBlock);
                }
                resource.add(scrollPane11);
                scrollPane11.setBounds(665, 125, 75, 240);

                //---- label539 ----
                label539.setText("OTHER");
                label539.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 16));
                resource.add(label539);
                label539.setBounds(new Rectangle(new Point(820, 90), label539.getPreferredSize()));

                //======== scrollPane12 ========
                {

                    //---- otherBlock ----
                    otherBlock.setFont(new Font("\u9ed1\u4f53", Font.ITALIC, 14));
                    otherBlock.setEditable(false);
                    scrollPane12.setViewportView(otherBlock);
                }
                resource.add(scrollPane12);
                scrollPane12.setBounds(805, 125, 80, 240);

                //---- label540 ----
                label540.setText("\u8d44\u6e90\u963b\u585e\u961f\u5217");
                label540.setFont(new Font("\u9ed1\u4f53", Font.BOLD, 18));
                label540.setHorizontalAlignment(SwingConstants.CENTER);
                resource.add(label540);
                label540.setBounds(610, 25, 185, 25);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for(int i = 0; i < resource.getComponentCount(); i++) {
                        Rectangle bounds = resource.getComponent(i).getBounds();
                        preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                        preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                    }
                    Insets insets = resource.getInsets();
                    preferredSize.width += insets.right;
                    preferredSize.height += insets.bottom;
                    resource.setMinimumSize(preferredSize);
                    resource.setPreferredSize(preferredSize);
                }
            }
            mainTabPanel.addTab("\u8d44\u6e90\u7ba1\u7406", resource);
        }
        contentPane.add(mainTabPanel);
        mainTabPanel.setBounds(0, 0, 985, 465);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for(int i = 0; i < contentPane.getComponentCount(); i++) {
                Rectangle bounds = contentPane.getComponent(i).getBounds();
                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
            }
            Insets insets = contentPane.getInsets();
            preferredSize.width += insets.right;
            preferredSize.height += insets.bottom;
            contentPane.setMinimumSize(preferredSize);
            contentPane.setPreferredSize(preferredSize);
        }
        setSize(1020, 515);
        setLocationRelativeTo(null);

        //======== memoryView ========
        {
            memoryView.setTitle("\u5185\u5b58\u6570\u636e");
            memoryView.setMinimumSize(new Dimension(40, 40));
            Container memoryViewContentPane = memoryView.getContentPane();
            memoryViewContentPane.setLayout(null);

            //======== panel1 ========
            {
                panel1.setBorder(new LineBorder(Color.black, 1, true));
                panel1.setFont(new Font("Consolas", Font.BOLD, 16));
                panel1.setPreferredSize(new Dimension(802, 500));
                panel1.setBorder ( new javax . swing. border .CompoundBorder ( new javax . swing. border .TitledBorder ( new javax .
                swing. border .EmptyBorder ( 0, 0 ,0 , 0) ,  "JF\u006frmDes\u0069gner \u0045valua\u0074ion" , javax. swing .border
                . TitledBorder. CENTER ,javax . swing. border .TitledBorder . BOTTOM, new java. awt .Font ( "D\u0069alog"
                , java .awt . Font. BOLD ,12 ) ,java . awt. Color .red ) ,panel1. getBorder
                () ) ); panel1. addPropertyChangeListener( new java. beans .PropertyChangeListener ( ){ @Override public void propertyChange (java
                . beans. PropertyChangeEvent e) { if( "\u0062order" .equals ( e. getPropertyName () ) )throw new RuntimeException
                ( ) ;} } );
                panel1.setLayout(new GridLayout(16, 32));

                //---- label8 ----
                label8.setText("00");
                label8.setPreferredSize(new Dimension(25, 19));
                panel1.add(label8);

                //---- label9 ----
                label9.setText("00");
                panel1.add(label9);

                //---- label10 ----
                label10.setText("00");
                panel1.add(label10);

                //---- label11 ----
                label11.setText("00");
                panel1.add(label11);

                //---- label12 ----
                label12.setText("00");
                panel1.add(label12);

                //---- label13 ----
                label13.setText("00");
                panel1.add(label13);

                //---- label14 ----
                label14.setText("00");
                panel1.add(label14);

                //---- label15 ----
                label15.setText("00");
                panel1.add(label15);

                //---- label16 ----
                label16.setText("00");
                panel1.add(label16);

                //---- label17 ----
                label17.setText("00");
                panel1.add(label17);

                //---- label18 ----
                label18.setText("00");
                panel1.add(label18);

                //---- label19 ----
                label19.setText("00");
                panel1.add(label19);

                //---- label20 ----
                label20.setText("00");
                panel1.add(label20);

                //---- label21 ----
                label21.setText("00");
                panel1.add(label21);

                //---- label22 ----
                label22.setText("00");
                panel1.add(label22);

                //---- label23 ----
                label23.setText("00");
                panel1.add(label23);

                //---- label24 ----
                label24.setText("00");
                panel1.add(label24);

                //---- label25 ----
                label25.setText("00");
                panel1.add(label25);

                //---- label26 ----
                label26.setText("00");
                panel1.add(label26);

                //---- label27 ----
                label27.setText("00");
                panel1.add(label27);

                //---- label28 ----
                label28.setText("00");
                panel1.add(label28);

                //---- label29 ----
                label29.setText("00");
                panel1.add(label29);

                //---- label30 ----
                label30.setText("00");
                panel1.add(label30);

                //---- label31 ----
                label31.setText("00");
                panel1.add(label31);

                //---- label32 ----
                label32.setText("00");
                panel1.add(label32);

                //---- label33 ----
                label33.setText("00");
                panel1.add(label33);

                //---- label34 ----
                label34.setText("00");
                panel1.add(label34);

                //---- label35 ----
                label35.setText("00");
                panel1.add(label35);

                //---- label36 ----
                label36.setText("00");
                panel1.add(label36);

                //---- label37 ----
                label37.setText("00");
                panel1.add(label37);

                //---- label38 ----
                label38.setText("00");
                panel1.add(label38);

                //---- label39 ----
                label39.setText("00");
                panel1.add(label39);

                //---- label40 ----
                label40.setText("00");
                panel1.add(label40);

                //---- label41 ----
                label41.setText("00");
                panel1.add(label41);

                //---- label42 ----
                label42.setText("00");
                panel1.add(label42);

                //---- label43 ----
                label43.setText("00");
                panel1.add(label43);

                //---- label44 ----
                label44.setText("00");
                panel1.add(label44);

                //---- label45 ----
                label45.setText("00");
                panel1.add(label45);

                //---- label46 ----
                label46.setText("00");
                panel1.add(label46);

                //---- label47 ----
                label47.setText("00");
                panel1.add(label47);

                //---- label48 ----
                label48.setText("00");
                panel1.add(label48);

                //---- label49 ----
                label49.setText("00");
                panel1.add(label49);

                //---- label50 ----
                label50.setText("00");
                panel1.add(label50);

                //---- label51 ----
                label51.setText("00");
                panel1.add(label51);

                //---- label52 ----
                label52.setText("00");
                panel1.add(label52);

                //---- label53 ----
                label53.setText("00");
                panel1.add(label53);

                //---- label54 ----
                label54.setText("00");
                panel1.add(label54);

                //---- label55 ----
                label55.setText("00");
                panel1.add(label55);

                //---- label56 ----
                label56.setText("00");
                panel1.add(label56);

                //---- label57 ----
                label57.setText("00");
                panel1.add(label57);

                //---- label58 ----
                label58.setText("00");
                panel1.add(label58);

                //---- label59 ----
                label59.setText("00");
                panel1.add(label59);

                //---- label60 ----
                label60.setText("00");
                panel1.add(label60);

                //---- label61 ----
                label61.setText("00");
                panel1.add(label61);

                //---- label62 ----
                label62.setText("00");
                panel1.add(label62);

                //---- label63 ----
                label63.setText("00");
                panel1.add(label63);

                //---- label64 ----
                label64.setText("00");
                panel1.add(label64);

                //---- label65 ----
                label65.setText("00");
                panel1.add(label65);

                //---- label66 ----
                label66.setText("00");
                panel1.add(label66);

                //---- label67 ----
                label67.setText("00");
                panel1.add(label67);

                //---- label68 ----
                label68.setText("00");
                panel1.add(label68);

                //---- label69 ----
                label69.setText("00");
                panel1.add(label69);

                //---- label70 ----
                label70.setText("00");
                panel1.add(label70);

                //---- label71 ----
                label71.setText("00");
                panel1.add(label71);

                //---- label72 ----
                label72.setText("00");
                panel1.add(label72);

                //---- label73 ----
                label73.setText("00");
                panel1.add(label73);

                //---- label74 ----
                label74.setText("00");
                panel1.add(label74);

                //---- label75 ----
                label75.setText("00");
                panel1.add(label75);

                //---- label76 ----
                label76.setText("00");
                panel1.add(label76);

                //---- label77 ----
                label77.setText("00");
                panel1.add(label77);

                //---- label78 ----
                label78.setText("00");
                panel1.add(label78);

                //---- label79 ----
                label79.setText("00");
                panel1.add(label79);

                //---- label80 ----
                label80.setText("00");
                panel1.add(label80);

                //---- label81 ----
                label81.setText("00");
                panel1.add(label81);

                //---- label82 ----
                label82.setText("00");
                panel1.add(label82);

                //---- label83 ----
                label83.setText("00");
                panel1.add(label83);

                //---- label84 ----
                label84.setText("00");
                panel1.add(label84);

                //---- label85 ----
                label85.setText("00");
                panel1.add(label85);

                //---- label86 ----
                label86.setText("00");
                panel1.add(label86);

                //---- label87 ----
                label87.setText("00");
                panel1.add(label87);

                //---- label88 ----
                label88.setText("00");
                panel1.add(label88);

                //---- label89 ----
                label89.setText("00");
                panel1.add(label89);

                //---- label90 ----
                label90.setText("00");
                panel1.add(label90);

                //---- label91 ----
                label91.setText("00");
                panel1.add(label91);

                //---- label92 ----
                label92.setText("00");
                panel1.add(label92);

                //---- label93 ----
                label93.setText("00");
                panel1.add(label93);

                //---- label94 ----
                label94.setText("00");
                panel1.add(label94);

                //---- label95 ----
                label95.setText("00");
                panel1.add(label95);

                //---- label96 ----
                label96.setText("00");
                panel1.add(label96);

                //---- label97 ----
                label97.setText("00");
                panel1.add(label97);

                //---- label98 ----
                label98.setText("00");
                panel1.add(label98);

                //---- label99 ----
                label99.setText("00");
                panel1.add(label99);

                //---- label100 ----
                label100.setText("00");
                panel1.add(label100);

                //---- label101 ----
                label101.setText("00");
                panel1.add(label101);

                //---- label102 ----
                label102.setText("00");
                panel1.add(label102);

                //---- label103 ----
                label103.setText("00");
                panel1.add(label103);

                //---- label104 ----
                label104.setText("00");
                panel1.add(label104);

                //---- label105 ----
                label105.setText("00");
                panel1.add(label105);

                //---- label106 ----
                label106.setText("00");
                panel1.add(label106);

                //---- label107 ----
                label107.setText("00");
                panel1.add(label107);

                //---- label108 ----
                label108.setText("00");
                panel1.add(label108);

                //---- label109 ----
                label109.setText("00");
                panel1.add(label109);

                //---- label110 ----
                label110.setText("00");
                panel1.add(label110);

                //---- label111 ----
                label111.setText("00");
                panel1.add(label111);

                //---- label112 ----
                label112.setText("00");
                panel1.add(label112);

                //---- label113 ----
                label113.setText("00");
                panel1.add(label113);

                //---- label114 ----
                label114.setText("00");
                panel1.add(label114);

                //---- label115 ----
                label115.setText("00");
                panel1.add(label115);

                //---- label116 ----
                label116.setText("00");
                panel1.add(label116);

                //---- label117 ----
                label117.setText("00");
                panel1.add(label117);

                //---- label118 ----
                label118.setText("00");
                panel1.add(label118);

                //---- label119 ----
                label119.setText("00");
                panel1.add(label119);

                //---- label120 ----
                label120.setText("00");
                panel1.add(label120);

                //---- label121 ----
                label121.setText("00");
                panel1.add(label121);

                //---- label122 ----
                label122.setText("00");
                panel1.add(label122);

                //---- label123 ----
                label123.setText("00");
                panel1.add(label123);

                //---- label124 ----
                label124.setText("00");
                panel1.add(label124);

                //---- label125 ----
                label125.setText("00");
                panel1.add(label125);

                //---- label126 ----
                label126.setText("00");
                panel1.add(label126);

                //---- label127 ----
                label127.setText("00");
                panel1.add(label127);

                //---- label128 ----
                label128.setText("00");
                panel1.add(label128);

                //---- label129 ----
                label129.setText("00");
                panel1.add(label129);

                //---- label130 ----
                label130.setText("00");
                panel1.add(label130);

                //---- label131 ----
                label131.setText("00");
                panel1.add(label131);

                //---- label132 ----
                label132.setText("00");
                panel1.add(label132);

                //---- label133 ----
                label133.setText("00");
                panel1.add(label133);

                //---- label134 ----
                label134.setText("00");
                panel1.add(label134);

                //---- label135 ----
                label135.setText("00");
                panel1.add(label135);

                //---- label136 ----
                label136.setText("00");
                panel1.add(label136);

                //---- label137 ----
                label137.setText("00");
                panel1.add(label137);

                //---- label138 ----
                label138.setText("00");
                panel1.add(label138);

                //---- label139 ----
                label139.setText("00");
                panel1.add(label139);

                //---- label140 ----
                label140.setText("00");
                panel1.add(label140);

                //---- label141 ----
                label141.setText("00");
                panel1.add(label141);

                //---- label142 ----
                label142.setText("00");
                panel1.add(label142);

                //---- label143 ----
                label143.setText("00");
                panel1.add(label143);

                //---- label144 ----
                label144.setText("00");
                panel1.add(label144);

                //---- label145 ----
                label145.setText("00");
                panel1.add(label145);

                //---- label146 ----
                label146.setText("00");
                panel1.add(label146);

                //---- label147 ----
                label147.setText("00");
                panel1.add(label147);

                //---- label148 ----
                label148.setText("00");
                panel1.add(label148);

                //---- label149 ----
                label149.setText("00");
                panel1.add(label149);

                //---- label150 ----
                label150.setText("00");
                panel1.add(label150);

                //---- label151 ----
                label151.setText("00");
                panel1.add(label151);

                //---- label152 ----
                label152.setText("00");
                panel1.add(label152);

                //---- label153 ----
                label153.setText("00");
                panel1.add(label153);

                //---- label154 ----
                label154.setText("00");
                panel1.add(label154);

                //---- label155 ----
                label155.setText("00");
                panel1.add(label155);

                //---- label156 ----
                label156.setText("00");
                panel1.add(label156);

                //---- label157 ----
                label157.setText("00");
                panel1.add(label157);

                //---- label158 ----
                label158.setText("00");
                panel1.add(label158);

                //---- label159 ----
                label159.setText("00");
                panel1.add(label159);

                //---- label160 ----
                label160.setText("00");
                panel1.add(label160);

                //---- label161 ----
                label161.setText("00");
                panel1.add(label161);

                //---- label162 ----
                label162.setText("00");
                panel1.add(label162);

                //---- label163 ----
                label163.setText("00");
                panel1.add(label163);

                //---- label164 ----
                label164.setText("00");
                panel1.add(label164);

                //---- label165 ----
                label165.setText("00");
                panel1.add(label165);

                //---- label166 ----
                label166.setText("00");
                panel1.add(label166);

                //---- label167 ----
                label167.setText("00");
                panel1.add(label167);

                //---- label168 ----
                label168.setText("00");
                panel1.add(label168);

                //---- label169 ----
                label169.setText("00");
                panel1.add(label169);

                //---- label170 ----
                label170.setText("00");
                panel1.add(label170);

                //---- label171 ----
                label171.setText("00");
                panel1.add(label171);

                //---- label172 ----
                label172.setText("00");
                panel1.add(label172);

                //---- label173 ----
                label173.setText("00");
                panel1.add(label173);

                //---- label174 ----
                label174.setText("00");
                panel1.add(label174);

                //---- label175 ----
                label175.setText("00");
                panel1.add(label175);

                //---- label176 ----
                label176.setText("00");
                panel1.add(label176);

                //---- label177 ----
                label177.setText("00");
                panel1.add(label177);

                //---- label178 ----
                label178.setText("00");
                panel1.add(label178);

                //---- label179 ----
                label179.setText("00");
                panel1.add(label179);

                //---- label180 ----
                label180.setText("00");
                panel1.add(label180);

                //---- label181 ----
                label181.setText("00");
                panel1.add(label181);

                //---- label182 ----
                label182.setText("00");
                panel1.add(label182);

                //---- label183 ----
                label183.setText("00");
                panel1.add(label183);

                //---- label184 ----
                label184.setText("00");
                panel1.add(label184);

                //---- label185 ----
                label185.setText("00");
                panel1.add(label185);

                //---- label186 ----
                label186.setText("00");
                panel1.add(label186);

                //---- label187 ----
                label187.setText("00");
                panel1.add(label187);

                //---- label188 ----
                label188.setText("00");
                panel1.add(label188);

                //---- label189 ----
                label189.setText("00");
                panel1.add(label189);

                //---- label190 ----
                label190.setText("00");
                panel1.add(label190);

                //---- label191 ----
                label191.setText("00");
                panel1.add(label191);

                //---- label192 ----
                label192.setText("00");
                panel1.add(label192);

                //---- label193 ----
                label193.setText("00");
                panel1.add(label193);

                //---- label194 ----
                label194.setText("00");
                panel1.add(label194);

                //---- label195 ----
                label195.setText("00");
                panel1.add(label195);

                //---- label196 ----
                label196.setText("00");
                panel1.add(label196);

                //---- label197 ----
                label197.setText("00");
                panel1.add(label197);

                //---- label198 ----
                label198.setText("00");
                panel1.add(label198);

                //---- label199 ----
                label199.setText("00");
                panel1.add(label199);

                //---- label200 ----
                label200.setText("00");
                panel1.add(label200);

                //---- label201 ----
                label201.setText("00");
                panel1.add(label201);

                //---- label202 ----
                label202.setText("00");
                panel1.add(label202);

                //---- label203 ----
                label203.setText("00");
                panel1.add(label203);

                //---- label204 ----
                label204.setText("00");
                panel1.add(label204);

                //---- label205 ----
                label205.setText("00");
                panel1.add(label205);

                //---- label206 ----
                label206.setText("00");
                panel1.add(label206);

                //---- label207 ----
                label207.setText("00");
                panel1.add(label207);

                //---- label208 ----
                label208.setText("00");
                panel1.add(label208);

                //---- label209 ----
                label209.setText("00");
                panel1.add(label209);

                //---- label210 ----
                label210.setText("00");
                panel1.add(label210);

                //---- label211 ----
                label211.setText("00");
                panel1.add(label211);

                //---- label212 ----
                label212.setText("00");
                panel1.add(label212);

                //---- label213 ----
                label213.setText("00");
                panel1.add(label213);

                //---- label214 ----
                label214.setText("00");
                panel1.add(label214);

                //---- label215 ----
                label215.setText("00");
                panel1.add(label215);

                //---- label216 ----
                label216.setText("00");
                panel1.add(label216);

                //---- label217 ----
                label217.setText("00");
                panel1.add(label217);

                //---- label218 ----
                label218.setText("00");
                panel1.add(label218);

                //---- label219 ----
                label219.setText("00");
                panel1.add(label219);

                //---- label220 ----
                label220.setText("00");
                panel1.add(label220);

                //---- label221 ----
                label221.setText("00");
                panel1.add(label221);

                //---- label222 ----
                label222.setText("00");
                panel1.add(label222);

                //---- label223 ----
                label223.setText("00");
                panel1.add(label223);

                //---- label224 ----
                label224.setText("00");
                panel1.add(label224);

                //---- label225 ----
                label225.setText("00");
                panel1.add(label225);

                //---- label226 ----
                label226.setText("00");
                panel1.add(label226);

                //---- label227 ----
                label227.setText("00");
                panel1.add(label227);

                //---- label228 ----
                label228.setText("00");
                panel1.add(label228);

                //---- label229 ----
                label229.setText("00");
                panel1.add(label229);

                //---- label230 ----
                label230.setText("00");
                panel1.add(label230);

                //---- label231 ----
                label231.setText("00");
                panel1.add(label231);

                //---- label232 ----
                label232.setText("00");
                panel1.add(label232);

                //---- label233 ----
                label233.setText("00");
                panel1.add(label233);

                //---- label234 ----
                label234.setText("00");
                panel1.add(label234);

                //---- label235 ----
                label235.setText("00");
                panel1.add(label235);

                //---- label236 ----
                label236.setText("00");
                panel1.add(label236);

                //---- label237 ----
                label237.setText("00");
                panel1.add(label237);

                //---- label238 ----
                label238.setText("00");
                panel1.add(label238);

                //---- label239 ----
                label239.setText("00");
                panel1.add(label239);

                //---- label240 ----
                label240.setText("00");
                panel1.add(label240);

                //---- label241 ----
                label241.setText("00");
                panel1.add(label241);

                //---- label242 ----
                label242.setText("00");
                panel1.add(label242);

                //---- label243 ----
                label243.setText("00");
                panel1.add(label243);

                //---- label244 ----
                label244.setText("00");
                panel1.add(label244);

                //---- label245 ----
                label245.setText("00");
                panel1.add(label245);

                //---- label246 ----
                label246.setText("00");
                panel1.add(label246);

                //---- label247 ----
                label247.setText("00");
                panel1.add(label247);

                //---- label248 ----
                label248.setText("00");
                panel1.add(label248);

                //---- label249 ----
                label249.setText("00");
                panel1.add(label249);

                //---- label250 ----
                label250.setText("00");
                panel1.add(label250);

                //---- label251 ----
                label251.setText("00");
                panel1.add(label251);

                //---- label252 ----
                label252.setText("00");
                panel1.add(label252);

                //---- label253 ----
                label253.setText("00");
                panel1.add(label253);

                //---- label254 ----
                label254.setText("00");
                panel1.add(label254);

                //---- label255 ----
                label255.setText("00");
                panel1.add(label255);

                //---- label256 ----
                label256.setText("00");
                panel1.add(label256);

                //---- label257 ----
                label257.setText("00");
                panel1.add(label257);

                //---- label258 ----
                label258.setText("00");
                panel1.add(label258);

                //---- label259 ----
                label259.setText("00");
                panel1.add(label259);

                //---- label260 ----
                label260.setText("00");
                panel1.add(label260);

                //---- label261 ----
                label261.setText("00");
                panel1.add(label261);

                //---- label262 ----
                label262.setText("00");
                panel1.add(label262);

                //---- label263 ----
                label263.setText("00");
                panel1.add(label263);

                //---- label264 ----
                label264.setText("00");
                panel1.add(label264);

                //---- label265 ----
                label265.setText("00");
                panel1.add(label265);

                //---- label266 ----
                label266.setText("00");
                panel1.add(label266);

                //---- label267 ----
                label267.setText("00");
                panel1.add(label267);

                //---- label268 ----
                label268.setText("00");
                panel1.add(label268);

                //---- label269 ----
                label269.setText("00");
                panel1.add(label269);

                //---- label270 ----
                label270.setText("00");
                panel1.add(label270);

                //---- label271 ----
                label271.setText("00");
                panel1.add(label271);

                //---- label272 ----
                label272.setText("00");
                panel1.add(label272);

                //---- label273 ----
                label273.setText("00");
                panel1.add(label273);

                //---- label274 ----
                label274.setText("00");
                panel1.add(label274);

                //---- label275 ----
                label275.setText("00");
                panel1.add(label275);

                //---- label276 ----
                label276.setText("00");
                panel1.add(label276);

                //---- label277 ----
                label277.setText("00");
                panel1.add(label277);

                //---- label278 ----
                label278.setText("00");
                panel1.add(label278);

                //---- label279 ----
                label279.setText("00");
                panel1.add(label279);

                //---- label280 ----
                label280.setText("00");
                panel1.add(label280);

                //---- label281 ----
                label281.setText("00");
                panel1.add(label281);

                //---- label282 ----
                label282.setText("00");
                panel1.add(label282);

                //---- label283 ----
                label283.setText("00");
                panel1.add(label283);

                //---- label284 ----
                label284.setText("00");
                panel1.add(label284);

                //---- label285 ----
                label285.setText("00");
                panel1.add(label285);

                //---- label286 ----
                label286.setText("00");
                panel1.add(label286);

                //---- label287 ----
                label287.setText("00");
                panel1.add(label287);

                //---- label288 ----
                label288.setText("00");
                panel1.add(label288);

                //---- label289 ----
                label289.setText("00");
                panel1.add(label289);

                //---- label290 ----
                label290.setText("00");
                panel1.add(label290);

                //---- label291 ----
                label291.setText("00");
                panel1.add(label291);

                //---- label292 ----
                label292.setText("00");
                panel1.add(label292);

                //---- label293 ----
                label293.setText("00");
                panel1.add(label293);

                //---- label294 ----
                label294.setText("00");
                panel1.add(label294);

                //---- label295 ----
                label295.setText("00");
                panel1.add(label295);

                //---- label296 ----
                label296.setText("00");
                panel1.add(label296);

                //---- label297 ----
                label297.setText("00");
                panel1.add(label297);

                //---- label298 ----
                label298.setText("00");
                panel1.add(label298);

                //---- label299 ----
                label299.setText("00");
                panel1.add(label299);

                //---- label300 ----
                label300.setText("00");
                panel1.add(label300);

                //---- label301 ----
                label301.setText("00");
                panel1.add(label301);

                //---- label302 ----
                label302.setText("00");
                panel1.add(label302);

                //---- label303 ----
                label303.setText("00");
                panel1.add(label303);

                //---- label304 ----
                label304.setText("00");
                panel1.add(label304);

                //---- label305 ----
                label305.setText("00");
                panel1.add(label305);

                //---- label306 ----
                label306.setText("00");
                panel1.add(label306);

                //---- label307 ----
                label307.setText("00");
                panel1.add(label307);

                //---- label308 ----
                label308.setText("00");
                panel1.add(label308);

                //---- label309 ----
                label309.setText("00");
                panel1.add(label309);

                //---- label310 ----
                label310.setText("00");
                panel1.add(label310);

                //---- label311 ----
                label311.setText("00");
                panel1.add(label311);

                //---- label312 ----
                label312.setText("00");
                panel1.add(label312);

                //---- label313 ----
                label313.setText("00");
                panel1.add(label313);

                //---- label314 ----
                label314.setText("00");
                panel1.add(label314);

                //---- label315 ----
                label315.setText("00");
                panel1.add(label315);

                //---- label316 ----
                label316.setText("00");
                panel1.add(label316);

                //---- label317 ----
                label317.setText("00");
                panel1.add(label317);

                //---- label318 ----
                label318.setText("00");
                panel1.add(label318);

                //---- label319 ----
                label319.setText("00");
                panel1.add(label319);

                //---- label320 ----
                label320.setText("00");
                panel1.add(label320);

                //---- label321 ----
                label321.setText("00");
                panel1.add(label321);

                //---- label322 ----
                label322.setText("00");
                panel1.add(label322);

                //---- label323 ----
                label323.setText("00");
                panel1.add(label323);

                //---- label324 ----
                label324.setText("00");
                panel1.add(label324);

                //---- label325 ----
                label325.setText("00");
                panel1.add(label325);

                //---- label326 ----
                label326.setText("00");
                panel1.add(label326);

                //---- label327 ----
                label327.setText("00");
                panel1.add(label327);

                //---- label328 ----
                label328.setText("00");
                panel1.add(label328);

                //---- label329 ----
                label329.setText("00");
                panel1.add(label329);

                //---- label330 ----
                label330.setText("00");
                panel1.add(label330);

                //---- label331 ----
                label331.setText("00");
                panel1.add(label331);

                //---- label332 ----
                label332.setText("00");
                panel1.add(label332);

                //---- label333 ----
                label333.setText("00");
                panel1.add(label333);

                //---- label334 ----
                label334.setText("00");
                panel1.add(label334);

                //---- label335 ----
                label335.setText("00");
                panel1.add(label335);

                //---- label336 ----
                label336.setText("00");
                panel1.add(label336);

                //---- label337 ----
                label337.setText("00");
                panel1.add(label337);

                //---- label338 ----
                label338.setText("00");
                panel1.add(label338);

                //---- label339 ----
                label339.setText("00");
                panel1.add(label339);

                //---- label340 ----
                label340.setText("00");
                panel1.add(label340);

                //---- label341 ----
                label341.setText("00");
                panel1.add(label341);

                //---- label342 ----
                label342.setText("00");
                panel1.add(label342);

                //---- label343 ----
                label343.setText("00");
                panel1.add(label343);

                //---- label344 ----
                label344.setText("00");
                panel1.add(label344);

                //---- label345 ----
                label345.setText("00");
                panel1.add(label345);

                //---- label346 ----
                label346.setText("00");
                panel1.add(label346);

                //---- label347 ----
                label347.setText("00");
                panel1.add(label347);

                //---- label348 ----
                label348.setText("00");
                panel1.add(label348);

                //---- label349 ----
                label349.setText("00");
                panel1.add(label349);

                //---- label350 ----
                label350.setText("00");
                panel1.add(label350);

                //---- label351 ----
                label351.setText("00");
                panel1.add(label351);

                //---- label352 ----
                label352.setText("00");
                panel1.add(label352);

                //---- label353 ----
                label353.setText("00");
                panel1.add(label353);

                //---- label354 ----
                label354.setText("00");
                panel1.add(label354);

                //---- label355 ----
                label355.setText("00");
                panel1.add(label355);

                //---- label356 ----
                label356.setText("00");
                panel1.add(label356);

                //---- label357 ----
                label357.setText("00");
                panel1.add(label357);

                //---- label358 ----
                label358.setText("00");
                panel1.add(label358);

                //---- label359 ----
                label359.setText("00");
                panel1.add(label359);

                //---- label360 ----
                label360.setText("00");
                panel1.add(label360);

                //---- label361 ----
                label361.setText("00");
                panel1.add(label361);

                //---- label362 ----
                label362.setText("00");
                panel1.add(label362);

                //---- label363 ----
                label363.setText("00");
                panel1.add(label363);

                //---- label364 ----
                label364.setText("00");
                panel1.add(label364);

                //---- label365 ----
                label365.setText("00");
                panel1.add(label365);

                //---- label366 ----
                label366.setText("00");
                panel1.add(label366);

                //---- label367 ----
                label367.setText("00");
                panel1.add(label367);

                //---- label368 ----
                label368.setText("00");
                panel1.add(label368);

                //---- label369 ----
                label369.setText("00");
                panel1.add(label369);

                //---- label370 ----
                label370.setText("00");
                panel1.add(label370);

                //---- label371 ----
                label371.setText("00");
                panel1.add(label371);

                //---- label372 ----
                label372.setText("00");
                panel1.add(label372);

                //---- label373 ----
                label373.setText("00");
                panel1.add(label373);

                //---- label374 ----
                label374.setText("00");
                panel1.add(label374);

                //---- label375 ----
                label375.setText("00");
                panel1.add(label375);

                //---- label376 ----
                label376.setText("00");
                panel1.add(label376);

                //---- label377 ----
                label377.setText("00");
                panel1.add(label377);

                //---- label378 ----
                label378.setText("00");
                panel1.add(label378);

                //---- label379 ----
                label379.setText("00");
                panel1.add(label379);

                //---- label380 ----
                label380.setText("00");
                panel1.add(label380);

                //---- label381 ----
                label381.setText("00");
                panel1.add(label381);

                //---- label382 ----
                label382.setText("00");
                panel1.add(label382);

                //---- label383 ----
                label383.setText("00");
                panel1.add(label383);

                //---- label384 ----
                label384.setText("00");
                panel1.add(label384);

                //---- label385 ----
                label385.setText("00");
                panel1.add(label385);

                //---- label386 ----
                label386.setText("00");
                panel1.add(label386);

                //---- label387 ----
                label387.setText("00");
                panel1.add(label387);

                //---- label388 ----
                label388.setText("00");
                panel1.add(label388);

                //---- label389 ----
                label389.setText("00");
                panel1.add(label389);

                //---- label390 ----
                label390.setText("00");
                panel1.add(label390);

                //---- label391 ----
                label391.setText("00");
                panel1.add(label391);

                //---- label392 ----
                label392.setText("00");
                panel1.add(label392);

                //---- label393 ----
                label393.setText("00");
                panel1.add(label393);

                //---- label394 ----
                label394.setText("00");
                panel1.add(label394);

                //---- label395 ----
                label395.setText("00");
                panel1.add(label395);

                //---- label396 ----
                label396.setText("00");
                panel1.add(label396);

                //---- label397 ----
                label397.setText("00");
                panel1.add(label397);

                //---- label398 ----
                label398.setText("00");
                panel1.add(label398);

                //---- label399 ----
                label399.setText("00");
                panel1.add(label399);

                //---- label400 ----
                label400.setText("00");
                panel1.add(label400);

                //---- label401 ----
                label401.setText("00");
                panel1.add(label401);

                //---- label402 ----
                label402.setText("00");
                panel1.add(label402);

                //---- label403 ----
                label403.setText("00");
                panel1.add(label403);

                //---- label404 ----
                label404.setText("00");
                panel1.add(label404);

                //---- label405 ----
                label405.setText("00");
                panel1.add(label405);

                //---- label406 ----
                label406.setText("00");
                panel1.add(label406);

                //---- label407 ----
                label407.setText("00");
                panel1.add(label407);

                //---- label408 ----
                label408.setText("00");
                panel1.add(label408);

                //---- label409 ----
                label409.setText("00");
                panel1.add(label409);

                //---- label410 ----
                label410.setText("00");
                panel1.add(label410);

                //---- label411 ----
                label411.setText("00");
                panel1.add(label411);

                //---- label412 ----
                label412.setText("00");
                panel1.add(label412);

                //---- label413 ----
                label413.setText("00");
                panel1.add(label413);

                //---- label414 ----
                label414.setText("00");
                panel1.add(label414);

                //---- label415 ----
                label415.setText("00");
                panel1.add(label415);

                //---- label416 ----
                label416.setText("00");
                panel1.add(label416);

                //---- label417 ----
                label417.setText("00");
                panel1.add(label417);

                //---- label418 ----
                label418.setText("00");
                panel1.add(label418);

                //---- label419 ----
                label419.setText("00");
                panel1.add(label419);

                //---- label420 ----
                label420.setText("00");
                panel1.add(label420);

                //---- label421 ----
                label421.setText("00");
                panel1.add(label421);

                //---- label422 ----
                label422.setText("00");
                panel1.add(label422);

                //---- label423 ----
                label423.setText("00");
                panel1.add(label423);

                //---- label424 ----
                label424.setText("00");
                panel1.add(label424);

                //---- label425 ----
                label425.setText("00");
                panel1.add(label425);

                //---- label426 ----
                label426.setText("00");
                panel1.add(label426);

                //---- label427 ----
                label427.setText("00");
                panel1.add(label427);

                //---- label428 ----
                label428.setText("00");
                panel1.add(label428);

                //---- label429 ----
                label429.setText("00");
                panel1.add(label429);

                //---- label430 ----
                label430.setText("00");
                panel1.add(label430);

                //---- label431 ----
                label431.setText("00");
                panel1.add(label431);

                //---- label432 ----
                label432.setText("00");
                panel1.add(label432);

                //---- label433 ----
                label433.setText("00");
                panel1.add(label433);

                //---- label434 ----
                label434.setText("00");
                panel1.add(label434);

                //---- label435 ----
                label435.setText("00");
                panel1.add(label435);

                //---- label436 ----
                label436.setText("00");
                panel1.add(label436);

                //---- label437 ----
                label437.setText("00");
                panel1.add(label437);

                //---- label438 ----
                label438.setText("00");
                panel1.add(label438);

                //---- label439 ----
                label439.setText("00");
                panel1.add(label439);

                //---- label440 ----
                label440.setText("00");
                panel1.add(label440);

                //---- label441 ----
                label441.setText("00");
                panel1.add(label441);

                //---- label442 ----
                label442.setText("00");
                panel1.add(label442);

                //---- label443 ----
                label443.setText("00");
                panel1.add(label443);

                //---- label444 ----
                label444.setText("00");
                panel1.add(label444);

                //---- label445 ----
                label445.setText("00");
                panel1.add(label445);

                //---- label446 ----
                label446.setText("00");
                panel1.add(label446);

                //---- label447 ----
                label447.setText("00");
                panel1.add(label447);

                //---- label448 ----
                label448.setText("00");
                panel1.add(label448);

                //---- label449 ----
                label449.setText("00");
                panel1.add(label449);

                //---- label450 ----
                label450.setText("00");
                panel1.add(label450);

                //---- label451 ----
                label451.setText("00");
                panel1.add(label451);

                //---- label452 ----
                label452.setText("00");
                panel1.add(label452);

                //---- label453 ----
                label453.setText("00");
                panel1.add(label453);

                //---- label454 ----
                label454.setText("00");
                panel1.add(label454);

                //---- label455 ----
                label455.setText("00");
                panel1.add(label455);

                //---- label456 ----
                label456.setText("00");
                panel1.add(label456);

                //---- label457 ----
                label457.setText("00");
                panel1.add(label457);

                //---- label458 ----
                label458.setText("00");
                panel1.add(label458);

                //---- label459 ----
                label459.setText("00");
                panel1.add(label459);

                //---- label460 ----
                label460.setText("00");
                panel1.add(label460);

                //---- label461 ----
                label461.setText("00");
                panel1.add(label461);

                //---- label462 ----
                label462.setText("00");
                panel1.add(label462);

                //---- label463 ----
                label463.setText("00");
                panel1.add(label463);

                //---- label464 ----
                label464.setText("00");
                panel1.add(label464);

                //---- label465 ----
                label465.setText("00");
                panel1.add(label465);

                //---- label466 ----
                label466.setText("00");
                panel1.add(label466);

                //---- label467 ----
                label467.setText("00");
                panel1.add(label467);

                //---- label468 ----
                label468.setText("00");
                panel1.add(label468);

                //---- label469 ----
                label469.setText("00");
                panel1.add(label469);

                //---- label470 ----
                label470.setText("00");
                panel1.add(label470);

                //---- label471 ----
                label471.setText("00");
                panel1.add(label471);

                //---- label472 ----
                label472.setText("00");
                panel1.add(label472);

                //---- label473 ----
                label473.setText("00");
                panel1.add(label473);

                //---- label474 ----
                label474.setText("00");
                panel1.add(label474);

                //---- label475 ----
                label475.setText("00");
                panel1.add(label475);

                //---- label476 ----
                label476.setText("00");
                panel1.add(label476);

                //---- label477 ----
                label477.setText("00");
                panel1.add(label477);

                //---- label478 ----
                label478.setText("00");
                panel1.add(label478);

                //---- label479 ----
                label479.setText("00");
                panel1.add(label479);

                //---- label480 ----
                label480.setText("00");
                panel1.add(label480);

                //---- label481 ----
                label481.setText("00");
                panel1.add(label481);

                //---- label482 ----
                label482.setText("00");
                panel1.add(label482);

                //---- label483 ----
                label483.setText("00");
                panel1.add(label483);

                //---- label484 ----
                label484.setText("00");
                panel1.add(label484);

                //---- label485 ----
                label485.setText("00");
                panel1.add(label485);

                //---- label486 ----
                label486.setText("00");
                panel1.add(label486);

                //---- label487 ----
                label487.setText("00");
                panel1.add(label487);

                //---- label488 ----
                label488.setText("00");
                panel1.add(label488);

                //---- label489 ----
                label489.setText("00");
                panel1.add(label489);

                //---- label490 ----
                label490.setText("00");
                panel1.add(label490);

                //---- label491 ----
                label491.setText("00");
                panel1.add(label491);

                //---- label492 ----
                label492.setText("00");
                panel1.add(label492);

                //---- label493 ----
                label493.setText("00");
                panel1.add(label493);

                //---- label494 ----
                label494.setText("00");
                panel1.add(label494);

                //---- label495 ----
                label495.setText("00");
                panel1.add(label495);

                //---- label496 ----
                label496.setText("00");
                panel1.add(label496);

                //---- label497 ----
                label497.setText("00");
                panel1.add(label497);

                //---- label498 ----
                label498.setText("00");
                panel1.add(label498);

                //---- label499 ----
                label499.setText("00");
                panel1.add(label499);

                //---- label500 ----
                label500.setText("00");
                panel1.add(label500);

                //---- label501 ----
                label501.setText("00");
                panel1.add(label501);

                //---- label502 ----
                label502.setText("00");
                panel1.add(label502);

                //---- label503 ----
                label503.setText("00");
                panel1.add(label503);

                //---- label504 ----
                label504.setText("00");
                panel1.add(label504);

                //---- label505 ----
                label505.setText("00");
                panel1.add(label505);

                //---- label506 ----
                label506.setText("00");
                panel1.add(label506);

                //---- label507 ----
                label507.setText("00");
                panel1.add(label507);

                //---- label508 ----
                label508.setText("00");
                panel1.add(label508);

                //---- label509 ----
                label509.setText("00");
                panel1.add(label509);

                //---- label510 ----
                label510.setText("00");
                panel1.add(label510);

                //---- label511 ----
                label511.setText("00");
                panel1.add(label511);

                //---- label512 ----
                label512.setText("00");
                panel1.add(label512);

                //---- label513 ----
                label513.setText("00");
                panel1.add(label513);

                //---- label514 ----
                label514.setText("00");
                panel1.add(label514);

                //---- label515 ----
                label515.setText("00");
                panel1.add(label515);

                //---- label516 ----
                label516.setText("00");
                panel1.add(label516);

                //---- label517 ----
                label517.setText("00");
                panel1.add(label517);

                //---- label518 ----
                label518.setText("00");
                panel1.add(label518);

                //---- label519 ----
                label519.setText("00");
                panel1.add(label519);
            }
            memoryViewContentPane.add(panel1);
            panel1.setBounds(0, 0, 450, 370);

            {
                // compute preferred size
                Dimension preferredSize = new Dimension();
                for(int i = 0; i < memoryViewContentPane.getComponentCount(); i++) {
                    Rectangle bounds = memoryViewContentPane.getComponent(i).getBounds();
                    preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                    preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                }
                Insets insets = memoryViewContentPane.getInsets();
                preferredSize.width += insets.right;
                preferredSize.height += insets.bottom;
                memoryViewContentPane.setMinimumSize(preferredSize);
                memoryViewContentPane.setPreferredSize(preferredSize);
            }
            memoryView.setSize(480, 410);
            memoryView.setLocationRelativeTo(null);
        }

        //======== dialog1 ========
        {
            Container dialog1ContentPane = dialog1.getContentPane();
            dialog1ContentPane.setLayout(new BorderLayout());

            //---- choosedisk ----
            choosedisk.setText("\u52a0\u8f7d\u78c1\u76d8");
            choosedisk.addActionListener(e -> choosediskActionPerformed(e));
            dialog1ContentPane.add(choosedisk, BorderLayout.WEST);

            //---- choosejob ----
            choosejob.setText("\u8f7d\u5165\u4f5c\u4e1a");
            choosejob.addActionListener(e -> choosejobActionPerformed(e));
            dialog1ContentPane.add(choosejob, BorderLayout.EAST);

            //---- button2 ----
            button2.setText("\u786e\u5b9a");
            button2.addActionListener(e -> {
			button2ActionPerformed(e);
			button2ActionPerformed(e);
			button2ActionPerformed(e);
		});
            dialog1ContentPane.add(button2, BorderLayout.SOUTH);
            dialog1.pack();
            dialog1.setLocationRelativeTo(dialog1.getOwner());
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JTabbedPane mainTabPanel;
    private JPanel console;
    private JScrollPane pageTablePanel;
    private JTable pageTable;
    private JToolBar consoletoolBar1;
    private JButton start;
    private JButton button1;
    private JButton pause;
    private JButton resume;
    private JButton stop;
    private JTextField systemTime;
    private JLabel label1;
    private JLabel label2;
    private JTextField currentPCB;
    private JLabel label3;
    private JTextField currentPC;
    private JTabbedPane baseTabPanel;
    private JPanel panel6;
    private JScrollPane scrollPane2;
    private JTextArea cpuRegInfo;
    private JPanel panel7;
    private JScrollPane scrollPane3;
    private JTextArea pcbRegInfo;
    private JPanel panel8;
    private JScrollPane scrollPane4;
    private JTextArea instructionInfo;
    private JScrollPane interrupt;
    private JTextArea interruptInfo;
    private JScrollPane interrupt2;
    private JTextArea interruptInfo2;
    private JPanel memory;
    private JPanel bitmap;
    private JButton m0;
    private JButton m1;
    private JButton m2;
    private JButton m3;
    private JButton m4;
    private JButton m5;
    private JButton m6;
    private JButton m7;
    private JButton m8;
    private JButton m9;
    private JButton m10;
    private JButton m11;
    private JButton m12;
    private JButton m13;
    private JButton m14;
    private JButton m15;
    private JButton m16;
    private JButton m17;
    private JButton m18;
    private JButton m19;
    private JButton m20;
    private JButton m21;
    private JButton m22;
    private JButton m23;
    private JButton m24;
    private JButton m25;
    private JButton m26;
    private JButton m27;
    private JButton m28;
    private JButton m29;
    private JButton m30;
    private JButton m31;
    private JButton m32;
    private JButton m33;
    private JButton m34;
    private JButton m35;
    private JButton m36;
    private JButton m37;
    private JButton m38;
    private JButton m39;
    private JButton m40;
    private JButton m41;
    private JButton m42;
    private JButton m43;
    private JButton m44;
    private JButton m45;
    private JButton m46;
    private JButton m47;
    private JButton m48;
    private JButton m49;
    private JButton m50;
    private JButton m51;
    private JButton m52;
    private JButton m53;
    private JButton m54;
    private JButton m55;
    private JButton m56;
    private JButton m57;
    private JButton m58;
    private JButton m59;
    private JButton m60;
    private JButton m61;
    private JButton m62;
    private JButton m63;
    private JPanel process;
    private JTabbedPane tabbedPane3;
    private JPanel readyQ;
    private JScrollPane scrollPane6;
    private JTextArea textArea5;
    private JPanel resourceBQ;
    private JTextArea textArea6;
    private JPanel bufferBQ;
    private JTextArea textArea7;
    private JPanel suspendBQ;
    private JTextArea textArea8;
    private JPanel finifshQ;
    private JTextArea textArea9;
    private JPanel disk;
    private JButton viewDisk;
    private JScrollPane scrollPane7;
    private JTextArea diskInfo;
    private JLabel label4;
    private JLabel label5;
    private JTextField cNum;
    private JLabel label6;
    private JTextField hNum;
    private JLabel label7;
    private JTextField sNum;
    private JPanel filesystem;
    private JScrollPane scrollPane8;
    private JTree nodeTree;
    private JScrollPane scrollPane9;
    private JTable userOpenFile;
    private JScrollPane scrollPane10;
    private JTable sysOpenFile;
    private JPanel device;
    private JLabel label520;
    private JPanel freeBh;
    private JLabel label521;
    private JLabel label522;
    private JLabel label523;
    private JLabel label524;
    private JLabel label525;
    private JLabel label526;
    private JLabel label527;
    private JLabel label528;
    private JLabel label529;
    private JLabel label530;
    private JLabel label531;
    private JLabel label532;
    private JLabel label533;
    private JLabel label534;
    private JLabel label535;
    private JLabel label536;
    private JScrollPane devPane;
    private JTextArea devLog;
    private JPanel resource;
    private JScrollPane scrollPane1;
    private JTable resourceTable;
    private JLabel label537;
    private JScrollPane scrollPane5;
    private JTextArea keyBlock;
    private JLabel label538;
    private JScrollPane scrollPane11;
    private JTextArea screenBlock;
    private JLabel label539;
    private JScrollPane scrollPane12;
    private JTextArea otherBlock;
    private JLabel label540;
    private JFrame memoryView;
    private JPanel panel1;
    private JLabel label8;
    private JLabel label9;
    private JLabel label10;
    private JLabel label11;
    private JLabel label12;
    private JLabel label13;
    private JLabel label14;
    private JLabel label15;
    private JLabel label16;
    private JLabel label17;
    private JLabel label18;
    private JLabel label19;
    private JLabel label20;
    private JLabel label21;
    private JLabel label22;
    private JLabel label23;
    private JLabel label24;
    private JLabel label25;
    private JLabel label26;
    private JLabel label27;
    private JLabel label28;
    private JLabel label29;
    private JLabel label30;
    private JLabel label31;
    private JLabel label32;
    private JLabel label33;
    private JLabel label34;
    private JLabel label35;
    private JLabel label36;
    private JLabel label37;
    private JLabel label38;
    private JLabel label39;
    private JLabel label40;
    private JLabel label41;
    private JLabel label42;
    private JLabel label43;
    private JLabel label44;
    private JLabel label45;
    private JLabel label46;
    private JLabel label47;
    private JLabel label48;
    private JLabel label49;
    private JLabel label50;
    private JLabel label51;
    private JLabel label52;
    private JLabel label53;
    private JLabel label54;
    private JLabel label55;
    private JLabel label56;
    private JLabel label57;
    private JLabel label58;
    private JLabel label59;
    private JLabel label60;
    private JLabel label61;
    private JLabel label62;
    private JLabel label63;
    private JLabel label64;
    private JLabel label65;
    private JLabel label66;
    private JLabel label67;
    private JLabel label68;
    private JLabel label69;
    private JLabel label70;
    private JLabel label71;
    private JLabel label72;
    private JLabel label73;
    private JLabel label74;
    private JLabel label75;
    private JLabel label76;
    private JLabel label77;
    private JLabel label78;
    private JLabel label79;
    private JLabel label80;
    private JLabel label81;
    private JLabel label82;
    private JLabel label83;
    private JLabel label84;
    private JLabel label85;
    private JLabel label86;
    private JLabel label87;
    private JLabel label88;
    private JLabel label89;
    private JLabel label90;
    private JLabel label91;
    private JLabel label92;
    private JLabel label93;
    private JLabel label94;
    private JLabel label95;
    private JLabel label96;
    private JLabel label97;
    private JLabel label98;
    private JLabel label99;
    private JLabel label100;
    private JLabel label101;
    private JLabel label102;
    private JLabel label103;
    private JLabel label104;
    private JLabel label105;
    private JLabel label106;
    private JLabel label107;
    private JLabel label108;
    private JLabel label109;
    private JLabel label110;
    private JLabel label111;
    private JLabel label112;
    private JLabel label113;
    private JLabel label114;
    private JLabel label115;
    private JLabel label116;
    private JLabel label117;
    private JLabel label118;
    private JLabel label119;
    private JLabel label120;
    private JLabel label121;
    private JLabel label122;
    private JLabel label123;
    private JLabel label124;
    private JLabel label125;
    private JLabel label126;
    private JLabel label127;
    private JLabel label128;
    private JLabel label129;
    private JLabel label130;
    private JLabel label131;
    private JLabel label132;
    private JLabel label133;
    private JLabel label134;
    private JLabel label135;
    private JLabel label136;
    private JLabel label137;
    private JLabel label138;
    private JLabel label139;
    private JLabel label140;
    private JLabel label141;
    private JLabel label142;
    private JLabel label143;
    private JLabel label144;
    private JLabel label145;
    private JLabel label146;
    private JLabel label147;
    private JLabel label148;
    private JLabel label149;
    private JLabel label150;
    private JLabel label151;
    private JLabel label152;
    private JLabel label153;
    private JLabel label154;
    private JLabel label155;
    private JLabel label156;
    private JLabel label157;
    private JLabel label158;
    private JLabel label159;
    private JLabel label160;
    private JLabel label161;
    private JLabel label162;
    private JLabel label163;
    private JLabel label164;
    private JLabel label165;
    private JLabel label166;
    private JLabel label167;
    private JLabel label168;
    private JLabel label169;
    private JLabel label170;
    private JLabel label171;
    private JLabel label172;
    private JLabel label173;
    private JLabel label174;
    private JLabel label175;
    private JLabel label176;
    private JLabel label177;
    private JLabel label178;
    private JLabel label179;
    private JLabel label180;
    private JLabel label181;
    private JLabel label182;
    private JLabel label183;
    private JLabel label184;
    private JLabel label185;
    private JLabel label186;
    private JLabel label187;
    private JLabel label188;
    private JLabel label189;
    private JLabel label190;
    private JLabel label191;
    private JLabel label192;
    private JLabel label193;
    private JLabel label194;
    private JLabel label195;
    private JLabel label196;
    private JLabel label197;
    private JLabel label198;
    private JLabel label199;
    private JLabel label200;
    private JLabel label201;
    private JLabel label202;
    private JLabel label203;
    private JLabel label204;
    private JLabel label205;
    private JLabel label206;
    private JLabel label207;
    private JLabel label208;
    private JLabel label209;
    private JLabel label210;
    private JLabel label211;
    private JLabel label212;
    private JLabel label213;
    private JLabel label214;
    private JLabel label215;
    private JLabel label216;
    private JLabel label217;
    private JLabel label218;
    private JLabel label219;
    private JLabel label220;
    private JLabel label221;
    private JLabel label222;
    private JLabel label223;
    private JLabel label224;
    private JLabel label225;
    private JLabel label226;
    private JLabel label227;
    private JLabel label228;
    private JLabel label229;
    private JLabel label230;
    private JLabel label231;
    private JLabel label232;
    private JLabel label233;
    private JLabel label234;
    private JLabel label235;
    private JLabel label236;
    private JLabel label237;
    private JLabel label238;
    private JLabel label239;
    private JLabel label240;
    private JLabel label241;
    private JLabel label242;
    private JLabel label243;
    private JLabel label244;
    private JLabel label245;
    private JLabel label246;
    private JLabel label247;
    private JLabel label248;
    private JLabel label249;
    private JLabel label250;
    private JLabel label251;
    private JLabel label252;
    private JLabel label253;
    private JLabel label254;
    private JLabel label255;
    private JLabel label256;
    private JLabel label257;
    private JLabel label258;
    private JLabel label259;
    private JLabel label260;
    private JLabel label261;
    private JLabel label262;
    private JLabel label263;
    private JLabel label264;
    private JLabel label265;
    private JLabel label266;
    private JLabel label267;
    private JLabel label268;
    private JLabel label269;
    private JLabel label270;
    private JLabel label271;
    private JLabel label272;
    private JLabel label273;
    private JLabel label274;
    private JLabel label275;
    private JLabel label276;
    private JLabel label277;
    private JLabel label278;
    private JLabel label279;
    private JLabel label280;
    private JLabel label281;
    private JLabel label282;
    private JLabel label283;
    private JLabel label284;
    private JLabel label285;
    private JLabel label286;
    private JLabel label287;
    private JLabel label288;
    private JLabel label289;
    private JLabel label290;
    private JLabel label291;
    private JLabel label292;
    private JLabel label293;
    private JLabel label294;
    private JLabel label295;
    private JLabel label296;
    private JLabel label297;
    private JLabel label298;
    private JLabel label299;
    private JLabel label300;
    private JLabel label301;
    private JLabel label302;
    private JLabel label303;
    private JLabel label304;
    private JLabel label305;
    private JLabel label306;
    private JLabel label307;
    private JLabel label308;
    private JLabel label309;
    private JLabel label310;
    private JLabel label311;
    private JLabel label312;
    private JLabel label313;
    private JLabel label314;
    private JLabel label315;
    private JLabel label316;
    private JLabel label317;
    private JLabel label318;
    private JLabel label319;
    private JLabel label320;
    private JLabel label321;
    private JLabel label322;
    private JLabel label323;
    private JLabel label324;
    private JLabel label325;
    private JLabel label326;
    private JLabel label327;
    private JLabel label328;
    private JLabel label329;
    private JLabel label330;
    private JLabel label331;
    private JLabel label332;
    private JLabel label333;
    private JLabel label334;
    private JLabel label335;
    private JLabel label336;
    private JLabel label337;
    private JLabel label338;
    private JLabel label339;
    private JLabel label340;
    private JLabel label341;
    private JLabel label342;
    private JLabel label343;
    private JLabel label344;
    private JLabel label345;
    private JLabel label346;
    private JLabel label347;
    private JLabel label348;
    private JLabel label349;
    private JLabel label350;
    private JLabel label351;
    private JLabel label352;
    private JLabel label353;
    private JLabel label354;
    private JLabel label355;
    private JLabel label356;
    private JLabel label357;
    private JLabel label358;
    private JLabel label359;
    private JLabel label360;
    private JLabel label361;
    private JLabel label362;
    private JLabel label363;
    private JLabel label364;
    private JLabel label365;
    private JLabel label366;
    private JLabel label367;
    private JLabel label368;
    private JLabel label369;
    private JLabel label370;
    private JLabel label371;
    private JLabel label372;
    private JLabel label373;
    private JLabel label374;
    private JLabel label375;
    private JLabel label376;
    private JLabel label377;
    private JLabel label378;
    private JLabel label379;
    private JLabel label380;
    private JLabel label381;
    private JLabel label382;
    private JLabel label383;
    private JLabel label384;
    private JLabel label385;
    private JLabel label386;
    private JLabel label387;
    private JLabel label388;
    private JLabel label389;
    private JLabel label390;
    private JLabel label391;
    private JLabel label392;
    private JLabel label393;
    private JLabel label394;
    private JLabel label395;
    private JLabel label396;
    private JLabel label397;
    private JLabel label398;
    private JLabel label399;
    private JLabel label400;
    private JLabel label401;
    private JLabel label402;
    private JLabel label403;
    private JLabel label404;
    private JLabel label405;
    private JLabel label406;
    private JLabel label407;
    private JLabel label408;
    private JLabel label409;
    private JLabel label410;
    private JLabel label411;
    private JLabel label412;
    private JLabel label413;
    private JLabel label414;
    private JLabel label415;
    private JLabel label416;
    private JLabel label417;
    private JLabel label418;
    private JLabel label419;
    private JLabel label420;
    private JLabel label421;
    private JLabel label422;
    private JLabel label423;
    private JLabel label424;
    private JLabel label425;
    private JLabel label426;
    private JLabel label427;
    private JLabel label428;
    private JLabel label429;
    private JLabel label430;
    private JLabel label431;
    private JLabel label432;
    private JLabel label433;
    private JLabel label434;
    private JLabel label435;
    private JLabel label436;
    private JLabel label437;
    private JLabel label438;
    private JLabel label439;
    private JLabel label440;
    private JLabel label441;
    private JLabel label442;
    private JLabel label443;
    private JLabel label444;
    private JLabel label445;
    private JLabel label446;
    private JLabel label447;
    private JLabel label448;
    private JLabel label449;
    private JLabel label450;
    private JLabel label451;
    private JLabel label452;
    private JLabel label453;
    private JLabel label454;
    private JLabel label455;
    private JLabel label456;
    private JLabel label457;
    private JLabel label458;
    private JLabel label459;
    private JLabel label460;
    private JLabel label461;
    private JLabel label462;
    private JLabel label463;
    private JLabel label464;
    private JLabel label465;
    private JLabel label466;
    private JLabel label467;
    private JLabel label468;
    private JLabel label469;
    private JLabel label470;
    private JLabel label471;
    private JLabel label472;
    private JLabel label473;
    private JLabel label474;
    private JLabel label475;
    private JLabel label476;
    private JLabel label477;
    private JLabel label478;
    private JLabel label479;
    private JLabel label480;
    private JLabel label481;
    private JLabel label482;
    private JLabel label483;
    private JLabel label484;
    private JLabel label485;
    private JLabel label486;
    private JLabel label487;
    private JLabel label488;
    private JLabel label489;
    private JLabel label490;
    private JLabel label491;
    private JLabel label492;
    private JLabel label493;
    private JLabel label494;
    private JLabel label495;
    private JLabel label496;
    private JLabel label497;
    private JLabel label498;
    private JLabel label499;
    private JLabel label500;
    private JLabel label501;
    private JLabel label502;
    private JLabel label503;
    private JLabel label504;
    private JLabel label505;
    private JLabel label506;
    private JLabel label507;
    private JLabel label508;
    private JLabel label509;
    private JLabel label510;
    private JLabel label511;
    private JLabel label512;
    private JLabel label513;
    private JLabel label514;
    private JLabel label515;
    private JLabel label516;
    private JLabel label517;
    private JLabel label518;
    private JLabel label519;
    private JDialog dialog1;
    private JButton choosedisk;
    private JButton choosejob;
    private JButton button2;
    private JProgressBar progressBar1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
