/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.renderer;

import java.awt.Component;
import java.util.Objects;
import javax.swing.*;
import manager.links.*;

/**
 *
 * @author Milo Steier
 */
public class LinksListCellRenderer extends DefaultListCellRenderer implements LinksListNameProvider{
    
    public static final String NULL_LIST_NAME_PROPERTY_CHANGED = "nullListName";
    
    private String nullName = "Current List";
    
    public String getNullListName(){
        return nullName;
    }
    
    public void setNullListName(String name){
        if (Objects.equals(nullName, name))
            return;
        String old = nullName;
        nullName = name;
        firePropertyChange(NULL_LIST_NAME_PROPERTY_CHANGED,old,nullName);
    }
    
    @Override
    public String getListName(LinksListPanel panel){
        return (panel != null) ? panel.getListName() : getNullListName();
    }
    
    public String getListName(LinksListModel model){
        return (model != null) ? model.getListName() : getNullListName();
    }
    
    @Override
    public Component getListCellRendererComponent(JList list, Object value, 
            int index, boolean isSelected, boolean cellHasFocus){
        if (value == null || value instanceof LinksListModel)
            value = getListName((LinksListModel)value);
        else if (value instanceof LinksListPanel)
            value = getListName((LinksListPanel)value);
        return super.getListCellRendererComponent(list, value, index, 
                isSelected, cellHasFocus);
    }
}
