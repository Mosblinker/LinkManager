/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.renderer;

import java.io.File;
import java.util.Objects;
import javax.swing.table.DefaultTableCellRenderer;
import measure.format.binary.ByteUnitFormat;

/**
 *
 * @author Mosblinker
 */
public class FileSizeTableCellRenderer extends DefaultTableCellRenderer{
    /**
     * This is used to format file sizes when displaying the size of a file.
     */
    private ByteUnitFormat formatter;
    /**
     * 
     * @param formatter 
     */
    public FileSizeTableCellRenderer(ByteUnitFormat formatter){
        this.formatter = Objects.requireNonNull(formatter);
    }
    /**
     * 
     */
    public FileSizeTableCellRenderer(){
        this(new ByteUnitFormat(true));
    }
    /**
     * 
     * @return 
     */
    public ByteUnitFormat getByteFormat(){
        return formatter;
    }
    /**
     * 
     * @param value 
     */
    @Override
    protected void setValue(Object value){
        if (value instanceof Number)
            super.setValue(formatter.format((Number)value));
        else if (value instanceof File)
            super.setValue(formatter.format((File)value));
        else
            super.setValue(value);
    }
}
