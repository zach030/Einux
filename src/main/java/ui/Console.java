/*
 * Created by JFormDesigner on Fri Mar 05 22:43:56 CST 2021
 */

package ui;

import disk.Disk;
import os.schedule.Schedule;

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
        Disk.disk.loadDisk();
    }

    private void button1ActionPerformed(ActionEvent e) {
        // TODO add your code here
    }

    private void initComponents() {
        // JFormDesigner - Component initialization - DO NOT MODIFY  //GEN-BEGIN:initComponents
        // Generated using JFormDesigner Evaluation license - zach
        ResourceBundle bundle = ResourceBundle.getBundle("ui.setting");
        dialogPane = new JPanel();
        contentPanel = new JPanel();
        panel1 = new JPanel();
        label1 = new JLabel();
        progressBar2 = new JProgressBar();
        buttonBar = new JPanel();
        button1 = new JButton();

        //======== this ========
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setFont(new Font(Font.DIALOG_INPUT, Font.BOLD, 12));
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        //======== dialogPane ========
        {
            dialogPane.setBorder(new EmptyBorder(12, 12, 12, 12));
            dialogPane.setBorder (new javax. swing. border. CompoundBorder( new javax .swing .border .TitledBorder (new javax
            . swing. border. EmptyBorder( 0, 0, 0, 0) , "JF\u006frmDesi\u0067ner Ev\u0061luatio\u006e", javax. swing
            . border. TitledBorder. CENTER, javax. swing. border. TitledBorder. BOTTOM, new java .awt .
            Font ("Dialo\u0067" ,java .awt .Font .BOLD ,12 ), java. awt. Color. red
            ) ,dialogPane. getBorder( )) ); dialogPane. addPropertyChangeListener (new java. beans. PropertyChangeListener( ){ @Override
            public void propertyChange (java .beans .PropertyChangeEvent e) {if ("borde\u0072" .equals (e .getPropertyName (
            ) )) throw new RuntimeException( ); }} );
            dialogPane.setLayout(new BorderLayout());

            //======== contentPanel ========
            {
                contentPanel.setLayout(new CardLayout());

                //======== panel1 ========
                {
                    panel1.setLayout(new BorderLayout());

                    //---- label1 ----
                    label1.setText(bundle.getString("label1.text"));
                    label1.setFont(new Font("Arial", Font.BOLD, 40));
                    label1.setHorizontalAlignment(SwingConstants.CENTER);
                    panel1.add(label1, BorderLayout.CENTER);

                    //---- progressBar2 ----
                    progressBar2.addComponentListener(new ComponentAdapter() {
                        @Override
                        public void componentShown(ComponentEvent e) {
                            progressBar2ComponentShown(e);
                        }
                    });
                    panel1.add(progressBar2, BorderLayout.SOUTH);
                }
                contentPanel.add(panel1, "card1");
            }
            dialogPane.add(contentPanel, BorderLayout.CENTER);

            //======== buttonBar ========
            {
                buttonBar.setBorder(new EmptyBorder(12, 0, 0, 0));
                buttonBar.setLayout(new GridBagLayout());
                ((GridBagLayout)buttonBar.getLayout()).columnWidths = new int[] {85, 243, 80};
                ((GridBagLayout)buttonBar.getLayout()).columnWeights = new double[] {0.0, 1.0, 0.0};

                //---- button1 ----
                button1.setText(bundle.getString("button1.text"));
                button1.setIcon(null);
                button1.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        button1MouseClicked(e);
                    }
                });
                button1.addActionListener(e -> button1ActionPerformed(e));
                buttonBar.add(button1, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0,
                    GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL,
                    new Insets(0, 0, 5, 5), 0, 0));
            }
            dialogPane.add(buttonBar, BorderLayout.SOUTH);
        }
        contentPane.add(dialogPane, BorderLayout.CENTER);
        setSize(820, 430);
        setLocationRelativeTo(null);
        // JFormDesigner - End of component initialization  //GEN-END:initComponents
    }

    // JFormDesigner - Variables declaration - DO NOT MODIFY  //GEN-BEGIN:variables
    // Generated using JFormDesigner Evaluation license - zach
    private JPanel dialogPane;
    private JPanel contentPanel;
    private JPanel panel1;
    private JLabel label1;
    private JProgressBar progressBar2;
    private JPanel buttonBar;
    private JButton button1;
    // JFormDesigner - End of variables declaration  //GEN-END:variables
}
