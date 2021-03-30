/*
 * Created by JFormDesigner on Fri Mar 05 22:43:56 CST 2021
 */

package ui;

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 * @author zach
 */
public class Console extends JFrame {
    public static void main(String[] args){
        new Console().setVisible(true);
    }
    public Console() {
        initComponents();
    }

    private void progressBar2ComponentShown(ComponentEvent e) {

        // TODO add your code here
    }

    private void button1MouseClicked(MouseEvent e) {
        // TODO add your code here
    }

    private void button1ActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void consoleItemStateChanged(ItemEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - unknown
        ResourceBundle bundle = ResourceBundle.getBundle("ui.setting");
        frame1 = new JFrame();
        系统菜单 = new JMenuBar();
        console = new JMenuItem();
        menuItem2 = new JMenuItem();
        memory = new JMenuItem();
        menuItem1 = new JMenuItem();

        //======== frame1 ========
        {
            frame1.setBackground(new Color(102, 102, 102));
            frame1.setTitle("Einux");
            Container frame1ContentPane = frame1.getContentPane();
            frame1ContentPane.setLayout(new BorderLayout());

            //======== 系统菜单 ========
            {

                //---- console ----
                console.setText("\u63a7\u5236\u53f0");
                console.addItemListener(e -> consoleItemStateChanged(e));
                系统菜单.add(console);

                //---- menuItem2 ----
                menuItem2.setText(bundle.getString("menuItem2.text"));
                系统菜单.add(menuItem2);

                //---- memory ----
                memory.setText("\u5185\u5b58\u7ba1\u7406");
                系统菜单.add(memory);

                //---- menuItem1 ----
                menuItem1.setText("\u78c1\u76d8\u7ba1\u7406");
                系统菜单.add(menuItem1);
            }
            frame1ContentPane.add(系统菜单, BorderLayout.NORTH);
            frame1.pack();
            frame1.setLocationRelativeTo(null);
        }
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - unknown
    private JFrame frame1;
    private JMenuBar 系统菜单;
    private JMenuItem console;
    private JMenuItem menuItem2;
    private JMenuItem memory;
    private JMenuItem menuItem1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
