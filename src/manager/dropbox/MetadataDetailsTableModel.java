/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import components.AbstractListModelList;
import components.ArrayListModel;
import java.util.*;
import javax.swing.JTable;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
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
    
    private AbstractListModelList<Metadata> data;
    
    private DbxClientV2 dbxClient = null;
    
    private JTable table;
    
    public MetadataDetailsTableModel(JTable table, AbstractListModelList<Metadata> data){
        this.table = table;
        this.data = Objects.requireNonNull(data);
        this.data.addListDataListener(new Handler());
    }
    
    public MetadataDetailsTableModel(JTable table){
        this(table,new ArrayListModel<>());
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
                    if (metadata instanceof FolderMetadata)
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
        return data.remove(index);
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
        return data.set(index, metadata);
    }
    /**
     * 
     * @param fromIndex
     * @param toIndex 
     */
    public void removeRows(int fromIndex, int toIndex){
        if (fromIndex <= 0 && toIndex >= data.size())
            data.clear();
        else
            data.subList(fromIndex, toIndex).clear();
    }
    /**
     * 
     * @return 
     */
    public List<Metadata> getMetadataList(){
        return data;
    }
    /**
     * 
     */
    private class Handler implements ListDataListener{
        @Override
        public void intervalAdded(ListDataEvent evt) {
            fireTableRowsInserted(evt.getIndex0(),evt.getIndex1());
        }
        @Override
        public void intervalRemoved(ListDataEvent evt) {
            fireTableRowsDeleted(evt.getIndex0(),evt.getIndex1());
        }
        @Override
        public void contentsChanged(ListDataEvent evt) {
            if (evt.getIndex0() >= 0 && evt.getIndex1() >= 0){
                if (evt.getIndex0() == 0 && evt.getIndex1() >= data.size())
                    fireTableDataChanged();
                else
                    fireTableRowsUpdated(evt.getIndex0(),evt.getIndex1());
            }
        }
    }
}
