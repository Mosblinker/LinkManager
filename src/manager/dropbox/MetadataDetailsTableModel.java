/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author Mosblinker
 */
public class MetadataDetailsTableModel extends AbstractTableModel{
    /**
     * 
     */
    public static final String[] COLUMN_NAMES = {
        "Name",
        "Size",
        "Type",
        "Client Modified",
        "Server Modified"
    };
    /**
     * 
     */
    public static final Class<?>[] COLUMN_CLASSES = {
        Metadata.class,
        Long.class,
        String.class,
        Date.class,
        Date.class
    };
    
    private List<Metadata> data = new ArrayList<>();
    
    private List<Metadata> listView = new RowList();
    
    private DbxClientV2 dbxClient = null;
    
    private JTable table;
    
    public MetadataDetailsTableModel(JTable table){
        this.table = table;
    }
    
    public MetadataDetailsTableModel(JTable table,Collection<? extends Metadata> data){
        this(table);
        this.data.addAll(data);
    }
    
    public MetadataDetailsTableModel(JTable table, Metadata[] data){
        this(table,Arrays.asList(data));
    }
    
    public MetadataDetailsTableModel(){
        this((JTable)null);
    }
    
    public MetadataDetailsTableModel(Collection<? extends Metadata> data){
        this(null,data);
    }
    
    public MetadataDetailsTableModel(Metadata[] data){
        this(null,data);
    }
    
    public JTable getTable(){
        return table;
    }
    
    public void setTable(JTable table){
        this.table = table;
    }
    /**
     * 
     * @return 
     */
    public DbxClientV2 getDropboxClient(){
        return dbxClient;
    }
    /**
     * 
     * @param client 
     */
    public void setDropboxClient(DbxClientV2 client){
        dbxClient = client;
    }
    
    @Override
    public int getRowCount() {
        return data.size();
    }
    @Override
    public int getColumnCount() {
        return COLUMN_NAMES.length;
    }
    /**
     * 
     * @param metadata
     * @return 
     */
    protected String getFileType(FileMetadata metadata){
        return "File";
    }
    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Metadata metadata = data.get(rowIndex);
        Objects.checkIndex(columnIndex, COLUMN_NAMES.length);
        if (metadata != null){
            switch(columnIndex){
                case(0):
                    return metadata;
                case(1):
                    if (metadata instanceof FileMetadata)
                        return ((FileMetadata)metadata).getSize();
                    return null;
                case(2):
                    if (metadata instanceof FolderMetadata || 
                            metadata instanceof DbxRootMetadata)
                        return "File folder";
                    else if (metadata instanceof DeletedMetadata)
                        return "Deleted";
                    else if (metadata instanceof FileMetadata){
                        return getFileType((FileMetadata)metadata);
                    } else 
                        return null;
                case(3):
                    if (metadata instanceof FileMetadata)
                        return ((FileMetadata)metadata).getClientModified();
                case(4):
                    if (metadata instanceof FileMetadata)
                        return ((FileMetadata)metadata).getServerModified();
            }
        }
        return null;
    }
    /**
     * 
     * @param column
     * @return 
     */
    @Override
    public String getColumnName(int column){
        if (column < 0 || column >= COLUMN_NAMES.length)
            return "";
        return COLUMN_NAMES[column];
    }
    @Override
    public int findColumn(String columnName){
        for (int i = 0; i < COLUMN_NAMES.length; i++){
            if (COLUMN_NAMES[i].equals(columnName))
                return i;
        }
        return -1;
    }
    /**
     * 
     * @param columnIndex
     * @return 
     */
    @Override
    public Class<?> getColumnClass(int columnIndex){
        if (columnIndex < 0 || columnIndex >= COLUMN_CLASSES.length)
            return Object.class;
        return COLUMN_CLASSES[columnIndex];
    }
    /**
     * 
     * @param rowIndex
     * @param columnIndex
     * @return 
     */
    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex){
            // Only the name column is editable
        if (columnIndex != 0 || rowIndex < 0 || rowIndex >= getRowCount() || 
                getDropboxClient() == null)
            return false;
        Metadata metadata = data.get(rowIndex);
        return metadata instanceof FileMetadata || metadata instanceof FolderMetadata;
    }
    /**
     * 
     * @param renamedMetadata
     * @return 
     */
    protected Metadata rename(RenamedMetadata renamedMetadata){
        return renamedMetadata.renameWithError(getDropboxClient(), getTable());
    }
    /**
     * 
     * @param aValue
     * @param row
     * @param column
     */
    @Override
    public void setValueAt(Object aValue, int row, int column){
        Objects.checkIndex(row, getRowCount());
        Objects.checkIndex(column, getColumnCount());
        Objects.requireNonNull(aValue);
        if (!isCellEditable(row,column))
            return;
        if (aValue instanceof String){
            Metadata old = data.get(row);
            String name = (String)aValue;
            if (old instanceof FileMetadata){
                FileMetadata file = (FileMetadata)old;
                aValue = FileMetadata.newBuilder(name, file.getId(), 
                        file.getClientModified(), file.getServerModified(), 
                        file.getRev(), file.getSize())
                        .withPathDisplay(file.getPathDisplay())
                        .withPathLower(file.getPathLower());
            } else if (old instanceof FolderMetadata){
                FolderMetadata folder = (FolderMetadata)old;
                aValue = FolderMetadata.newBuilder(name, folder.getId())
                        .withPathDisplay(folder.getPathDisplay())
                        .withPathLower(folder.getPathLower());
            }
        } else if (aValue instanceof RenamedMetadata){
            aValue = rename((RenamedMetadata)aValue);
        }
        if (aValue instanceof Metadata || aValue == null){
            if (!Objects.equals(data.get(row), aValue)){
                data.set(row, (Metadata)aValue);
                fireTableCellUpdated(row, column);
            }
        } else
            throw new ClassCastException();
    }
    /**
     * 
     * @param index
     * @return 
     */
    public Metadata getRow(int index){
        return data.get(index);
    }
    /**
     * 
     * @param index
     * @param metadata 
     */
    public void insertRow(int index, Metadata metadata){
        data.add(index, metadata);
        fireTableRowsInserted(index,index);
    }
    /**
     * 
     * @param metadata 
     */
    public void addRow(Metadata metadata){
        insertRow(getRowCount(),metadata);
    }
    /**
     * 
     * @param index
     * @return 
     */
    public Metadata removeRow(int index){
        Metadata value = data.remove(index);
        fireTableRowsDeleted(index,index);
        return value;
    }
    /**
     * 
     * @param metadata
     * @return 
     */
    public int indexOfRow(Metadata metadata){
        return data.indexOf(metadata);
    }
    /**
     * 
     * @param index
     * @param metadata
     * @return 
     */
    public Metadata setRow(int index, Metadata metadata){
        Metadata old = data.set(index, metadata);
        if (!Objects.equals(old, metadata))
            fireTableRowsUpdated(index,index);
        return old;
    }
    /**
     * 
     * @param fromIndex
     * @param toIndex 
     */
    public void removeRows(int fromIndex, int toIndex){
        Objects.checkFromToIndex(fromIndex, toIndex, getRowCount());
        if (fromIndex == toIndex)
            return;
        if (fromIndex == 0 && toIndex == getRowCount())
            data.clear();
        else{
            for (int i = toIndex-1; i >= fromIndex; i--){
                data.remove(i);
            }
        }
        fireTableRowsDeleted(fromIndex,toIndex-1);
    }
    /**
     * 
     * @return 
     */
    public List<Metadata> getMetadataList(){
        return listView;
    }
    /**
     * 
     */
    protected class RowList extends AbstractList<Metadata>{
        @Override
        public Metadata get(int index) {
            return getRow(index);
        }
        @Override
        public int size() {
            return getRowCount();
        }
        @Override
        public boolean contains(Object o) {
            return data.contains(o);
        }
        @Override
        public Object[] toArray() {
            return data.toArray();
        }
        @Override
        public boolean remove(Object o) {
            int index = indexOf(o);
            if (index < 0)
                return false;
            remove(index);
            return true;
        }
        @Override
        public boolean containsAll(Collection<?> c) {
            return data.containsAll(c);
        }
        @Override
        public boolean addAll(Collection<? extends Metadata> c) {
            return addAll(size(),c);
        }
        @Override
        public boolean addAll(int index, Collection<? extends Metadata> c) {
            boolean modified = data.addAll(index, c);
            if (modified)
                fireTableRowsInserted(index,index+c.size()-1);
            return modified;
        }
        /**
         * 
         * @param fromIndex
         * @param toIndex 
         */
        @Override
        protected void removeRange(int fromIndex, int toIndex){
            removeRows(fromIndex,toIndex);
        }
        @Override
        public Metadata set(int index, Metadata element) {
            return setRow(index,element);
        }
        @Override
        public void add(int index, Metadata element) {
            insertRow(index,element);
        }
        @Override
        public Metadata remove(int index) {
            return removeRow(index);
        }
        @Override
        public int indexOf(Object o) {
            return data.indexOf(o);
        }
        @Override
        public int lastIndexOf(Object o) {
            return data.lastIndexOf(o);
        }
        @Override
        public void replaceAll(UnaryOperator<Metadata> operator){
            data.replaceAll(operator);
            fireTableRowsUpdated(0,size()-1);
        }
        @Override
        public void sort(Comparator<? super Metadata> c){
            data.sort(c);
            fireTableRowsUpdated(0,size()-1);
        }
    }
}
