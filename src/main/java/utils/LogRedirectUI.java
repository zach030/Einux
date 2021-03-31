package utils;

import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.*;

public class LogRedirectUI {
    private final static Logger log = Log.logger;
    private JFrame frame;
    private JScrollPane scrollPane = new JScrollPane();
    private JTextArea textArea = new JTextArea();
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    new LogRedirectUI();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
    /**
     * Create the application.
     */
    public LogRedirectUI() {
        initialize();
        initLog();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setVisible(true);
        frame.setTitle("Log redirect");
        frame.setBounds(100, 100, 470, 514);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(null);


        scrollPane.setBounds(0, 114, 452, 353);
        frame.getContentPane().add(scrollPane);


        scrollPane.setViewportView(textArea);

        JButton btnButton1 = new JButton("button1");
        btnButton1.setBounds(28, 53, 113, 27);
        frame.getContentPane().add(btnButton1);

        JButton btnButton2 = new JButton("button2");
        btnButton2.setBounds(169, 53, 113, 27);
        frame.getContentPane().add(btnButton2);

        JButton btnButton3 = new JButton("button3");
        btnButton3.setBounds(310, 53, 113, 27);
        frame.getContentPane().add(btnButton3);

        // 添加监听
        btnButton1.addActionListener(event -> {
            Log.Info("xxx","xxx");
        });
        btnButton2.addActionListener(event -> {
            Log.Info("xxx","xxx");
        });
        btnButton3.addActionListener(event -> {
            Log.Info("xxx","xxx");
        });
    }
    private void initLog() {
        try {
            Thread thread;
            thread = new TextAreaLogAppender(textArea, scrollPane);
            thread.start();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null, "重定向错误");
        }
    }
}