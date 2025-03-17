/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.renderer;

import java.awt.*;
import javax.swing.*;
import javax.swing.tree.*;

/**
 *
 * @author Mosblinker
 */
public class ChildCountTreeCellRenderer extends DefaultTreeCellRenderer{
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus){
            // If the value is a TreeNode
        if (value instanceof TreeNode){
                // Get the value as a TreeNode
            TreeNode node = (TreeNode) value;
                // This is the user object for the node
            Object obj = null;
                // If the node is a DefaultMutableTreeNode
            if (node instanceof DefaultMutableTreeNode)
                    // Get the user object of the node
                obj = ((DefaultMutableTreeNode)node).getUserObject();
            else    // Get the node as a String
                obj = node.toString();
                // If the node's user object is a String and the node allows 
                // children
            if (obj instanceof String && node.getAllowsChildren()){
                    // Get the amount of child nodes
                int children = node.getChildCount();
                    // Get the node's user object and append the child count
                value = obj + " ["+children+
                            // If there is only one child, do not show an "s" at 
                            // the end of the word
                        " Item"+((children!=1)?"s":"")+"]";
            }
        }
        return super.getTreeCellRendererComponent(tree, value, sel, 
                expanded, leaf, row, hasFocus);
    }
}
