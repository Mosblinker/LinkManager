/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.v2.files.*;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.util.EventObject;
import javax.swing.*;
import javax.swing.tree.*;

/**
 *
 * @author Mosblinker
 */
public class MetadataNameTreeCellEditor extends DefaultTreeCellEditor{
    
    protected Metadata lastMetadata = null;
    
    public MetadataNameTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
    }
    /**
     * 
     * @param event
     * @return 
     */
    @Override
    public boolean isCellEditable(EventObject event){
        if (event != null) {
            if (event.getSource() instanceof JTree) {
                JTree tree = (JTree)event.getSource();
                if (event instanceof MouseEvent) {
                    TreePath path = tree.getPathForLocation(
                            ((MouseEvent)event).getX(),((MouseEvent)event).getY());
                    if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode){
                        DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
                        if (node.getUserObject() instanceof DbxRootMetadata)
                            return false;
                    }
                }
            }
        }
        return super.isCellEditable(event);
    }
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, 
            boolean isSelected, boolean expanded, boolean leaf, int row){
        lastMetadata = null;
        if (value instanceof DefaultMutableTreeNode){
            value = ((DefaultMutableTreeNode)value).getUserObject();
            if (value instanceof Metadata){
                lastMetadata = ((Metadata)value);
                value = lastMetadata.getName();
            }
        }
        return super.getTreeCellEditorComponent(tree, value, isSelected, expanded, leaf, row);
    }
    @Override
    public Object getCellEditorValue(){
        Object value = super.getCellEditorValue();
        if (lastMetadata == null || lastMetadata instanceof DeletedMetadata)
            return value;
        return new RenamedMetadata(lastMetadata,(value!=null)?value.toString():null); 
    }
}
