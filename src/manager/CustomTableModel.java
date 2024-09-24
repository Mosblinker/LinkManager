/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.util.*;
import javax.swing.table.*;

/**
 * This is a custom table model that allows the assigning of classes to columns 
 * and the cells are not editable by default.
 * @author Milo Steier
 */
public class CustomTableModel extends DefaultTableModel {
    /**
     * The map that maps column indexes to the column's class.
     */
    private final Map<Integer,Class<?>> colClasses = new HashMap<>();
    /**
     * This constructs a CustomTableModel with as many columns as there are 
     * elements in {@code columnNames} and a row count of zero. The names of 
     * each column will be taken from the {@code columnNames} array.
     * @param columnNames An array containing the names of the new columns. If 
     * this is null or empty, then the model has no columns.
     */
    public CustomTableModel(String... columnNames){
        super(columnNames,0);
    }
    /**
     * This constructs an empty CustomTableModel with no rows or columns.
     */
    public CustomTableModel(){
        super();
    }
    /**
     * This returns the most specific superclass for all the cell values in the 
     * column at the given index. The column class is set via the {@link 
     * #setColumnClass setColumnClass} method. If the column class is set to a 
     * non-null class, then that is what will be returned. Otherwise, this will 
     * return {@code Object.class}.
     * @param columnIndex {@inheritDoc }
     * @return The class set for the given column, or {@code Object.class} if 
     * no class is specified for that column.
     * @see #setColumnClass
     * @see #isColumnClassSet 
     */
    @Override
    public Class<?> getColumnClass(int columnIndex) {
            // If a non-null column class is set for the column, return it. 
            // Otherwise, return Object.class
        return Objects.requireNonNullElse(colClasses.getOrDefault(columnIndex, 
                super.getColumnClass(columnIndex)),Object.class);
    }
    /**
     * This sets the most specific superclass for all the cell values in the 
     * column at the given index. If {@code columnClass} is null, then the 
     * column class will be cleared ({@code getColumnClass(columnIndex)} will 
     * return {@code Object.class}). Otherwise, subsequent calls to {@code 
     * getColumnClass(columnIndex)} will return {@code columnClass}.
     * @param columnIndex The column whose class is to be set.
     * @param columnClass The class for the column at {@code columnIndex}, or 
     * null.
     * @see #getColumnClass 
     * @see #isColumnClassSet 
     */
    public void setColumnClass(int columnIndex, Class<?> columnClass){
            // If the column class is null
        if (columnClass == null)
            colClasses.remove(columnIndex);
        else
            colClasses.put(columnIndex, columnClass);
    }
    /**
     * This returns whether the most specific superclass for all the cell values 
     * in the column at the given index has been set to a non-null value.
     * @param columnIndex The column being queried.
     * @return Whether a class has been specified for the given column.
     * @see #getColumnClass 
     * @see #setColumnClass 
     */
    public boolean isColumnClassSet(int columnIndex){
        return colClasses.get(columnIndex) != null;
    }
    /**
     * This returns whether the cell at the given row and column is editable.
     * @param row {@inheritDoc }
     * @param column {@inheritDoc }
     * @return {@code false}
     * @see #setValueAt 
     */
    @Override
    public boolean isCellEditable(int row, int column){
        return false;
    }
}
