/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.renderer;

import java.text.*;
import java.util.*;
import javax.swing.*;
import javax.swing.table.*;

/**
 *
 * @author Mosblinker
 */
public class DateTableCellRenderer extends DefaultTableCellRenderer {
    /**
     * 
     */
    private final DateFormat format;
    /**
     * 
     * @param format 
     */
    public DateTableCellRenderer(DateFormat format){
        this.format = Objects.requireNonNull(format);
    }
    /**
     * 
     * @return 
     */
    public DateFormat getDateFormat(){
        return format;
    }
    @Override
    public java.awt.Component getTableCellRendererComponent(JTable table, 
            Object value, boolean isSelected, boolean hasFocus, int row, 
            int column) {
            // If the value is a date
        if (value instanceof Date)
                // Format the date
            value = format.format((Date) value);
        return super.getTableCellRendererComponent(table, value, isSelected, 
                hasFocus, row, column);
    }
}
