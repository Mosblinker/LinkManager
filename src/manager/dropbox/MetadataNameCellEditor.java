/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.v2.files.*;
import java.awt.Color;
import java.awt.Component;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.tree.DefaultMutableTreeNode;

/**
 *
 * @author Mosblinker
 */
public class MetadataNameCellEditor extends DefaultCellEditor{
    
    private static final Border FIELD_LINE_BORDER = BorderFactory.createLineBorder(Color.BLACK,1);
    
    protected Metadata lastMetadata = null;
    
    private Border fieldBorder;
    
    public MetadataNameCellEditor() {
        super(new JTextField());
        fieldBorder = editorComponent.getBorder();
    }
    /**
     * 
     * @param value
     * @return 
     */
    protected Object getValue(Object value){
        lastMetadata = null;
        if (value instanceof DefaultMutableTreeNode){
            value = ((DefaultMutableTreeNode)value).getUserObject();
        }
        if (value instanceof Metadata){
            lastMetadata = ((Metadata)value);
            return lastMetadata.getName();
        }
        return value;
    }
    @Override
    public Component getTreeCellEditorComponent(JTree tree, Object value, 
            boolean isSelected, boolean expanded, boolean leaf, int row){
        editorComponent.setBorder(fieldBorder);
        return super.getTreeCellEditorComponent(tree, getValue(value), 
                isSelected, expanded, leaf, row);
    }
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, 
            boolean isSelected, int row, int column){
        editorComponent.setBorder(FIELD_LINE_BORDER);
        return super.getTableCellEditorComponent(table, getValue(value), 
                isSelected, row, column);
    }
    @Override
    public Object getCellEditorValue(){
        Object value = super.getCellEditorValue();
        if (lastMetadata == null || lastMetadata instanceof DeletedMetadata)
            return value;
        return new RenamedMetadata(lastMetadata,(value!=null)?value.toString():null); 
    }
}
