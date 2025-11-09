/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.v2.files.*;
import java.awt.Color;
import java.awt.Component;
import javax.swing.Icon;
import javax.swing.JTree;
import javax.swing.tree.*;
import manager.icons.DropboxIcon;

/**
 *
 * @author Mosblinker
 */
public class MetadataTreeCellRenderer extends DefaultTreeCellRenderer {
    
    private static final Icon DROPBOX_ICON = new DropboxIcon(16);
    
    private static final Icon DISABLED_DROPBOX_ICON = new DropboxIcon(16,Color.GRAY);
    
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value,
            boolean sel, boolean expanded, boolean leaf, int row, 
            boolean hasFocus) {
        boolean isRoot = false;
        if (value instanceof DefaultMutableTreeNode){
            value = ((DefaultMutableTreeNode)value).getUserObject();
            if (value instanceof DbxRootMetadata){
                String name = ((Metadata)value).getName();
                if (!name.isBlank())
                    value = name;
                else
                    value = "Dropbox";
                isRoot = true;
            }
            else if (value instanceof Metadata){
                value = ((Metadata)value).getName();
            }
        }
        Component comp = super.getTreeCellRendererComponent(tree, value, sel, 
                expanded, leaf, row, hasFocus);
        if (isRoot){
            setIcon(DROPBOX_ICON);
            setDisabledIcon(DISABLED_DROPBOX_ICON);
        }
        
        return comp;
    }
}
