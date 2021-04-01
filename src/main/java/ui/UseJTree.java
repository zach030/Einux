package ui;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class UseJTree extends JFrame {
    public UseJTree(){
        super("JTree 使用举例");
        setSize(400,300);
        addWindowListener(new WindowAdapter(){
            public void windowClosing(WindowEvent windowevent){
                Window window = windowevent.getWindow();
                window.dispose();
                System.exit(0);
            }
        });
    }
    JTree tree;
    DefaultTreeModel treeModel;
    public void init(){

        DefaultMutableTreeNode root = new DefaultMutableTreeNode("管理");
        //一级子节点
        DefaultMutableTreeNode subroot_1_1 = new DefaultMutableTreeNode("人员管理");
        DefaultMutableTreeNode subroot_2_1 = new DefaultMutableTreeNode("财务管理");
        //二级子节点
        DefaultMutableTreeNode subroot_3_1 = new DefaultMutableTreeNode("市场部");
        DefaultMutableTreeNode subroot_3_2 = new DefaultMutableTreeNode("项目工程部");

        DefaultMutableTreeNode  leaf1 = new DefaultMutableTreeNode ("张小军");
        DefaultMutableTreeNode  leaf2 =new DefaultMutableTreeNode ("李米");

        //二级子节点
        DefaultMutableTreeNode subroot_4_1 = new  DefaultMutableTreeNode("月末报表");
        DefaultMutableTreeNode subroot_4_2 = new  DefaultMutableTreeNode("工资管理");
        DefaultMutableTreeNode leaf3 = new DefaultMutableTreeNode("王津");
        DefaultMutableTreeNode leaf4 = new DefaultMutableTreeNode("王兵");
        treeModel = new DefaultTreeModel(root);


        //构建一颗树
        treeModel.insertNodeInto(subroot_1_1,root,0);
        treeModel.insertNodeInto(subroot_2_1,root,1);

        treeModel.insertNodeInto(subroot_3_1,subroot_1_1,0);
        treeModel.insertNodeInto(subroot_3_2,subroot_1_1,1);

        treeModel.insertNodeInto(leaf1,subroot_3_2,0);
        treeModel.insertNodeInto(leaf2,subroot_3_1,0);

        treeModel.insertNodeInto(subroot_4_1,subroot_2_1,0);
        treeModel.insertNodeInto(subroot_4_2,subroot_2_1,1);

        treeModel.insertNodeInto(leaf3,subroot_4_2,0);
        treeModel.insertNodeInto(leaf4,subroot_4_2,1);
        tree = new JTree(treeModel);
        getContentPane().add(tree,BorderLayout.CENTER);

    }
    public static void main(String[] args) {
        UseJTree jtree = new UseJTree();
        jtree.init();
        jtree.setVisible(true);
    }

}
