/*
 * Created by JFormDesigner on Wed Mar 31 01:16:13 CST 2021
 */

package ui;

import disk.DiskHelper;
import os.job.JobManage;
import os.schedule.Schedule;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.swing.*;
import javax.swing.tree.*;

/**
 * @author unknown
 */
public class PlatForm extends JFrame {
    public static void main(String[] args) {
        new PlatForm().setVisible(true);
    }

    public PlatForm() {
        initComponents();
    }

    private void loadDiskActionPerformed(ActionEvent e) {
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(new File("."));
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setMultiSelectionEnabled(false);//是否允许多选
        int result = fc.showOpenDialog(new JPanel());
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            DiskHelper.rootDir = file.getPath();
        }
        System.out.println(DiskHelper.rootDir);
    }

    private void loadJobActionPerformed(ActionEvent e) {
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

    private void startActionPerformed(ActionEvent e) {
        Schedule.schedule.init();
        synchronized (Schedule.systemStatus) {
            Schedule.systemStatus.notify();
        }
    }

    private void pauseActionPerformed(ActionEvent e) {
    }

    private void resumeActionPerformed(ActionEvent e) {
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        mainTabPanel = new JTabbedPane();
        console = new JPanel();
        pageTablePanel = new JScrollPane();
        pageTable = new JTable();
        consoletoolBar1 = new JToolBar();
        loadDisk = new JButton();
        loadJob = new JButton();
        start = new JButton();
        pause = new JButton();
        resume = new JButton();
        systemTime = new JTextField();
        label1 = new JLabel();
        label2 = new JLabel();
        currentPCB = new JTextField();
        label3 = new JLabel();
        currentIR = new JTextField();
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
        scrollPane5 = new JScrollPane();
        systemLog = new JTextArea();
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
        panel9 = new JPanel();
        scrollPane6 = new JScrollPane();
        textArea5 = new JTextArea();
        panel10 = new JPanel();
        textArea6 = new JTextArea();
        panel11 = new JPanel();
        textArea7 = new JTextArea();
        panel12 = new JPanel();
        textArea8 = new JTextArea();
        panel13 = new JPanel();
        textArea9 = new JTextArea();
        disk = new JPanel();
        button70 = new JButton();
        scrollPane7 = new JScrollPane();
        textArea10 = new JTextArea();
        label4 = new JLabel();
        label5 = new JLabel();
        textField4 = new JTextField();
        label6 = new JLabel();
        textField5 = new JTextField();
        label7 = new JLabel();
        textField6 = new JTextField();
        filesystem = new JPanel();
        scrollPane8 = new JScrollPane();
        tree1 = new JTree();
        scrollPane9 = new JScrollPane();
        table2 = new JTable();
        scrollPane10 = new JScrollPane();
        table3 = new JTable();

        //======== this ========
        setTitle("Einux");
        Container contentPane = getContentPane();
        contentPane.setLayout(null);

        //======== mainTabPanel ========
        {

            //======== console ========
            {
                console.setBorder(new javax.swing.border.CompoundBorder(new javax.swing.border.TitledBorder(new javax.swing
                        .border.EmptyBorder(0, 0, 0, 0), "JF\u006frmD\u0065sig\u006eer \u0045val\u0075ati\u006fn", javax.swing.border.TitledBorder
                        .CENTER, javax.swing.border.TitledBorder.BOTTOM, new java.awt.Font("Dia\u006cog", java.
                        awt.Font.BOLD, 12), java.awt.Color.red), console.getBorder()))
                ;
                console.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
                    @Override
                    public void propertyChange(java.beans.PropertyChangeEvent e
                    ) {
                        if ("\u0062ord\u0065r".equals(e.getPropertyName())) throw new RuntimeException();
                    }
                })
                ;
                console.setLayout(null);

                //======== pageTablePanel ========
                {
                    pageTablePanel.setViewportView(pageTable);
                }
                console.add(pageTablePanel);
                pageTablePanel.setBounds(0, 230, pageTablePanel.getPreferredSize().width, 193);

                //======== consoletoolBar1 ========
                {

                    //---- loadDisk ----
                    loadDisk.setText("\u52a0\u8f7d\u78c1\u76d8");
                    loadDisk.addActionListener(e -> loadDiskActionPerformed(e));
                    consoletoolBar1.add(loadDisk);

                    //---- loadJob ----
                    loadJob.setText("\u8f7d\u5165\u4f5c\u4e1a");
                    loadJob.addActionListener(e -> loadJobActionPerformed(e));
                    consoletoolBar1.add(loadJob);

                    //---- start ----
                    start.setText("\u5f00\u673a");
                    start.addActionListener(e -> startActionPerformed(e));
                    consoletoolBar1.add(start);

                    //---- pause ----
                    pause.setText("\u6682\u505c");
                    pause.addActionListener(e -> pauseActionPerformed(e));
                    consoletoolBar1.add(pause);

                    //---- resume ----
                    resume.setText("\u7ee7\u7eed");
                    resume.addActionListener(e -> resumeActionPerformed(e));
                    consoletoolBar1.add(resume);
                }
                console.add(consoletoolBar1);
                consoletoolBar1.setBounds(new Rectangle(new Point(265, 0), consoletoolBar1.getPreferredSize()));

                //---- systemTime ----
                systemTime.setPreferredSize(new Dimension(100, 38));
                console.add(systemTime);
                systemTime.setBounds(new Rectangle(new Point(115, 60), systemTime.getPreferredSize()));

                //---- label1 ----
                label1.setText("\u7cfb\u7edf\u65f6\u95f4");
                console.add(label1);
                label1.setBounds(40, 60, 70, 30);

                //---- label2 ----
                label2.setText("\u5f53\u524d\u8fdb\u7a0b");
                console.add(label2);
                label2.setBounds(new Rectangle(new Point(40, 115), label2.getPreferredSize()));

                //---- currentPCB ----
                currentPCB.setPreferredSize(new Dimension(100, 38));
                console.add(currentPCB);
                currentPCB.setBounds(new Rectangle(new Point(115, 110), currentPCB.getPreferredSize()));

                //---- label3 ----
                label3.setText("\u5f53\u524d\u6307\u4ee4");
                console.add(label3);
                label3.setBounds(new Rectangle(new Point(40, 165), label3.getPreferredSize()));

                //---- currentIR ----
                currentIR.setPreferredSize(new Dimension(100, 38));
                console.add(currentIR);
                currentIR.setBounds(new Rectangle(new Point(115, 160), currentIR.getPreferredSize()));

                //======== baseTabPanel ========
                {

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
                        scrollPane2.setBounds(0, 0, 305, 95);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel6.getComponentCount(); i++) {
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
                        scrollPane3.setBounds(0, 0, 310, 90);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel7.getComponentCount(); i++) {
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
                        scrollPane4.setBounds(0, 0, 310, 90);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel8.getComponentCount(); i++) {
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
                baseTabPanel.setBounds(315, 65, 310, 130);

                //======== scrollPane5 ========
                {

                    //---- systemLog ----
                    systemLog.setPreferredSize(new Dimension(100, 22));
                    scrollPane5.setViewportView(systemLog);
                }
                console.add(scrollPane5);
                scrollPane5.setBounds(450, 230, 535, 195);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < console.getComponentCount(); i++) {
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
                    bitmap.add(m0);

                    //---- m1 ----
                    m1.setText("0");
                    bitmap.add(m1);

                    //---- m2 ----
                    m2.setText("0");
                    bitmap.add(m2);

                    //---- m3 ----
                    m3.setText("0");
                    bitmap.add(m3);

                    //---- m4 ----
                    m4.setText("0");
                    bitmap.add(m4);

                    //---- m5 ----
                    m5.setText("0");
                    bitmap.add(m5);

                    //---- m6 ----
                    m6.setText("0");
                    bitmap.add(m6);

                    //---- m7 ----
                    m7.setText("0");
                    bitmap.add(m7);

                    //---- m8 ----
                    m8.setText("0");
                    bitmap.add(m8);

                    //---- m9 ----
                    m9.setText("0");
                    bitmap.add(m9);

                    //---- m10 ----
                    m10.setText("0");
                    bitmap.add(m10);

                    //---- m11 ----
                    m11.setText("0");
                    bitmap.add(m11);

                    //---- m12 ----
                    m12.setText("0");
                    bitmap.add(m12);

                    //---- m13 ----
                    m13.setText("0");
                    bitmap.add(m13);

                    //---- m14 ----
                    m14.setText("0");
                    bitmap.add(m14);

                    //---- m15 ----
                    m15.setText("0");
                    bitmap.add(m15);

                    //---- m16 ----
                    m16.setText("0");
                    bitmap.add(m16);

                    //---- m17 ----
                    m17.setText("0");
                    bitmap.add(m17);

                    //---- m18 ----
                    m18.setText("0");
                    bitmap.add(m18);

                    //---- m19 ----
                    m19.setText("0");
                    bitmap.add(m19);

                    //---- m20 ----
                    m20.setText("0");
                    bitmap.add(m20);

                    //---- m21 ----
                    m21.setText("0");
                    bitmap.add(m21);

                    //---- m22 ----
                    m22.setText("0");
                    bitmap.add(m22);

                    //---- m23 ----
                    m23.setText("0");
                    bitmap.add(m23);

                    //---- m24 ----
                    m24.setText("0");
                    bitmap.add(m24);

                    //---- m25 ----
                    m25.setText("0");
                    bitmap.add(m25);

                    //---- m26 ----
                    m26.setText("0");
                    bitmap.add(m26);

                    //---- m27 ----
                    m27.setText("0");
                    bitmap.add(m27);

                    //---- m28 ----
                    m28.setText("0");
                    bitmap.add(m28);

                    //---- m29 ----
                    m29.setText("0");
                    bitmap.add(m29);

                    //---- m30 ----
                    m30.setText("0");
                    bitmap.add(m30);

                    //---- m31 ----
                    m31.setText("0");
                    bitmap.add(m31);

                    //---- m32 ----
                    m32.setText("0");
                    bitmap.add(m32);

                    //---- m33 ----
                    m33.setText("0");
                    bitmap.add(m33);

                    //---- m34 ----
                    m34.setText("0");
                    bitmap.add(m34);

                    //---- m35 ----
                    m35.setText("0");
                    bitmap.add(m35);

                    //---- m36 ----
                    m36.setText("0");
                    bitmap.add(m36);

                    //---- m37 ----
                    m37.setText("0");
                    bitmap.add(m37);

                    //---- m38 ----
                    m38.setText("0");
                    bitmap.add(m38);

                    //---- m39 ----
                    m39.setText("0");
                    bitmap.add(m39);

                    //---- m40 ----
                    m40.setText("0");
                    bitmap.add(m40);

                    //---- m41 ----
                    m41.setText("0");
                    bitmap.add(m41);

                    //---- m42 ----
                    m42.setText("0");
                    bitmap.add(m42);

                    //---- m43 ----
                    m43.setText("0");
                    bitmap.add(m43);

                    //---- m44 ----
                    m44.setText("0");
                    bitmap.add(m44);

                    //---- m45 ----
                    m45.setText("0");
                    bitmap.add(m45);

                    //---- m46 ----
                    m46.setText("0");
                    bitmap.add(m46);

                    //---- m47 ----
                    m47.setText("0");
                    bitmap.add(m47);

                    //---- m48 ----
                    m48.setText("0");
                    bitmap.add(m48);

                    //---- m49 ----
                    m49.setText("0");
                    bitmap.add(m49);

                    //---- m50 ----
                    m50.setText("0");
                    bitmap.add(m50);

                    //---- m51 ----
                    m51.setText("0");
                    bitmap.add(m51);

                    //---- m52 ----
                    m52.setText("0");
                    bitmap.add(m52);

                    //---- m53 ----
                    m53.setText("0");
                    bitmap.add(m53);

                    //---- m54 ----
                    m54.setText("0");
                    bitmap.add(m54);

                    //---- m55 ----
                    m55.setText("0");
                    bitmap.add(m55);

                    //---- m56 ----
                    m56.setText("0");
                    bitmap.add(m56);

                    //---- m57 ----
                    m57.setText("0");
                    bitmap.add(m57);

                    //---- m58 ----
                    m58.setText("0");
                    bitmap.add(m58);

                    //---- m59 ----
                    m59.setText("0");
                    bitmap.add(m59);

                    //---- m60 ----
                    m60.setText("0");
                    bitmap.add(m60);

                    //---- m61 ----
                    m61.setText("0");
                    bitmap.add(m61);

                    //---- m62 ----
                    m62.setText("0");
                    bitmap.add(m62);

                    //---- m63 ----
                    m63.setText("0");
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

                    //======== panel9 ========
                    {
                        panel9.setLayout(null);

                        //======== scrollPane6 ========
                        {

                            //---- textArea5 ----
                            textArea5.setMinimumSize(new Dimension(100, 22));
                            textArea5.setPreferredSize(new Dimension(100, 22));
                            scrollPane6.setViewportView(textArea5);
                        }
                        panel9.add(scrollPane6);
                        scrollPane6.setBounds(0, 0, 810, 205);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel9.getComponentCount(); i++) {
                                Rectangle bounds = panel9.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel9.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel9.setMinimumSize(preferredSize);
                            panel9.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u5c31\u7eea\u961f\u5217", panel9);

                    //======== panel10 ========
                    {
                        panel10.setLayout(null);

                        //---- textArea6 ----
                        textArea6.setMinimumSize(new Dimension(100, 22));
                        textArea6.setPreferredSize(new Dimension(100, 22));
                        panel10.add(textArea6);
                        textArea6.setBounds(0, 0, 808, 203);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel10.getComponentCount(); i++) {
                                Rectangle bounds = panel10.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel10.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel10.setMinimumSize(preferredSize);
                            panel10.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u8d44\u6e90\u963b\u585e\u961f\u5217", panel10);

                    //======== panel11 ========
                    {
                        panel11.setLayout(null);

                        //---- textArea7 ----
                        textArea7.setMinimumSize(new Dimension(100, 22));
                        textArea7.setPreferredSize(new Dimension(100, 22));
                        panel11.add(textArea7);
                        textArea7.setBounds(0, 0, 808, 203);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel11.getComponentCount(); i++) {
                                Rectangle bounds = panel11.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel11.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel11.setMinimumSize(preferredSize);
                            panel11.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u7f13\u51b2\u533a\u963b\u585e\u961f\u5217", panel11);

                    //======== panel12 ========
                    {
                        panel12.setLayout(null);

                        //---- textArea8 ----
                        textArea8.setMinimumSize(new Dimension(100, 22));
                        textArea8.setPreferredSize(new Dimension(100, 22));
                        panel12.add(textArea8);
                        textArea8.setBounds(0, 0, 808, 203);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel12.getComponentCount(); i++) {
                                Rectangle bounds = panel12.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel12.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel12.setMinimumSize(preferredSize);
                            panel12.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u6302\u8d77\u961f\u5217", panel12);

                    //======== panel13 ========
                    {
                        panel13.setLayout(null);

                        //---- textArea9 ----
                        textArea9.setMinimumSize(new Dimension(100, 22));
                        textArea9.setPreferredSize(new Dimension(100, 22));
                        panel13.add(textArea9);
                        textArea9.setBounds(0, 0, 808, 203);

                        {
                            // compute preferred size
                            Dimension preferredSize = new Dimension();
                            for (int i = 0; i < panel13.getComponentCount(); i++) {
                                Rectangle bounds = panel13.getComponent(i).getBounds();
                                preferredSize.width = Math.max(bounds.x + bounds.width, preferredSize.width);
                                preferredSize.height = Math.max(bounds.y + bounds.height, preferredSize.height);
                            }
                            Insets insets = panel13.getInsets();
                            preferredSize.width += insets.right;
                            preferredSize.height += insets.bottom;
                            panel13.setMinimumSize(preferredSize);
                            panel13.setPreferredSize(preferredSize);
                        }
                    }
                    tabbedPane3.addTab("\u5df2\u5b8c\u6210\u961f\u5217", panel13);
                }
                process.add(tabbedPane3);
                tabbedPane3.setBounds(65, 0, 810, 245);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < process.getComponentCount(); i++) {
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

                //---- button70 ----
                button70.setText("\u67e5\u770b\u78c1\u76d8");
                disk.add(button70);
                button70.setBounds(new Rectangle(new Point(410, 25), button70.getPreferredSize()));

                //======== scrollPane7 ========
                {

                    //---- textArea10 ----
                    textArea10.setPreferredSize(new Dimension(100, 22));
                    scrollPane7.setViewportView(textArea10);
                }
                disk.add(scrollPane7);
                scrollPane7.setBounds(450, 130, 465, 205);

                //---- label4 ----
                label4.setText("\u78c1\u76d8\u4fe1\u606f");
                disk.add(label4);
                label4.setBounds(new Rectangle(new Point(650, 95), label4.getPreferredSize()));

                //---- label5 ----
                label5.setText("\u67f1\u9762\u6570");
                disk.add(label5);
                label5.setBounds(95, 120, 55, 30);

                //---- textField4 ----
                textField4.setPreferredSize(new Dimension(100, 38));
                disk.add(textField4);
                textField4.setBounds(new Rectangle(new Point(185, 120), textField4.getPreferredSize()));

                //---- label6 ----
                label6.setText("\u78c1\u9053\u6570");
                disk.add(label6);
                label6.setBounds(new Rectangle(new Point(95, 195), label6.getPreferredSize()));

                //---- textField5 ----
                textField5.setPreferredSize(new Dimension(100, 38));
                disk.add(textField5);
                textField5.setBounds(new Rectangle(new Point(185, 190), textField5.getPreferredSize()));

                //---- label7 ----
                label7.setText("\u6247\u533a\u6570");
                disk.add(label7);
                label7.setBounds(new Rectangle(new Point(95, 270), label7.getPreferredSize()));

                //---- textField6 ----
                textField6.setPreferredSize(new Dimension(100, 38));
                disk.add(textField6);
                textField6.setBounds(new Rectangle(new Point(185, 265), textField6.getPreferredSize()));

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < disk.getComponentCount(); i++) {
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

                    //---- tree1 ----
                    tree1.setModel(new DefaultTreeModel(
                            new DefaultMutableTreeNode("(root)") {
                                {
                                    add(new DefaultMutableTreeNode("."));
                                    DefaultMutableTreeNode node1 = new DefaultMutableTreeNode("home");
                                    node1.add(new DefaultMutableTreeNode("zach"));
                                    add(node1);
                                    node1 = new DefaultMutableTreeNode("dev");
                                    node1.add(new DefaultMutableTreeNode("block"));
                                    add(node1);
                                    node1 = new DefaultMutableTreeNode("etc");
                                    node1.add(new DefaultMutableTreeNode("ssh"));
                                    add(node1);
                                }
                            }));
                    scrollPane8.setViewportView(tree1);
                }
                filesystem.add(scrollPane8);
                scrollPane8.setBounds(0, 0, 221, 425);

                //======== scrollPane9 ========
                {
                    scrollPane9.setViewportView(table2);
                }
                filesystem.add(scrollPane9);
                scrollPane9.setBounds(220, 0, 370, 425);

                //======== scrollPane10 ========
                {
                    scrollPane10.setViewportView(table3);
                }
                filesystem.add(scrollPane10);
                scrollPane10.setBounds(590, 0, 395, 425);

                {
                    // compute preferred size
                    Dimension preferredSize = new Dimension();
                    for (int i = 0; i < filesystem.getComponentCount(); i++) {
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
        }
        contentPane.add(mainTabPanel);
        mainTabPanel.setBounds(0, 0, 985, 465);

        {
            // compute preferred size
            Dimension preferredSize = new Dimension();
            for (int i = 0; i < contentPane.getComponentCount(); i++) {
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
        setSize(985, 505);
        setLocationRelativeTo(getOwner());
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JTabbedPane mainTabPanel;
    private JPanel console;
    private JScrollPane pageTablePanel;
    private JTable pageTable;
    private JToolBar consoletoolBar1;
    private JButton loadDisk;
    private JButton loadJob;
    private JButton start;
    private JButton pause;
    private JButton resume;
    private JTextField systemTime;
    private JLabel label1;
    private JLabel label2;
    private JTextField currentPCB;
    private JLabel label3;
    private JTextField currentIR;
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
    private JScrollPane scrollPane5;
    private JTextArea systemLog;
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
    private JPanel panel9;
    private JScrollPane scrollPane6;
    private JTextArea textArea5;
    private JPanel panel10;
    private JTextArea textArea6;
    private JPanel panel11;
    private JTextArea textArea7;
    private JPanel panel12;
    private JTextArea textArea8;
    private JPanel panel13;
    private JTextArea textArea9;
    private JPanel disk;
    private JButton button70;
    private JScrollPane scrollPane7;
    private JTextArea textArea10;
    private JLabel label4;
    private JLabel label5;
    private JTextField textField4;
    private JLabel label6;
    private JTextField textField5;
    private JLabel label7;
    private JTextField textField6;
    private JPanel filesystem;
    private JScrollPane scrollPane8;
    private JTree tree1;
    private JScrollPane scrollPane9;
    private JTable table2;
    private JScrollPane scrollPane10;
    private JTable table3;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
