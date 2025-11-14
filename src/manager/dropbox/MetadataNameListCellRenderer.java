/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.v2.files.Metadata;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author Mosblinker
 */
public class MetadataNameListCellRenderer extends DefaultListCellRenderer{
    /**
     * 
     * @param list
     * @param value
     * @param index
     * @param isSelected
     * @param cellHasFocus
     * @return 
     */
    @Override
    public Component getListCellRendererComponent(JList list,
            Object value,int index,boolean isSelected,boolean cellHasFocus){
        if (value instanceof Metadata){
            value = ((Metadata)value).getName();
        }
        return super.getListCellRendererComponent(list, value, index, 
                isSelected, cellHasFocus);
    }
}
