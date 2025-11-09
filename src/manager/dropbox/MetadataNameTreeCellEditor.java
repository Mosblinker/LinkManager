/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import java.awt.Component;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.tree.*;
import manager.LinkManager;

/**
 *
 * @author Mosblinker
 */
public class MetadataNameTreeCellEditor extends DefaultTreeCellEditor{
    /**
     * This is the Dropbox client being used currently.
     */
    private DbxClientV2 dbxClient = null;
    
    protected Metadata lastMetadata = null;
    
    public MetadataNameTreeCellEditor(JTree tree, DefaultTreeCellRenderer renderer) {
        super(tree, renderer);
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
    /**
     * 
     * @param name
     */
    protected void giveErrorFeedback(String name){
        UIManager.getLookAndFeel().provideErrorFeedback(tree);
        JOptionPane.showMessageDialog(tree, "Cannot rename "+name+
                ": A file with the name you specified already exists. "
                        + "Specify a different file name.", 
                "Error Renaming File of Folder", JOptionPane.ERROR_MESSAGE);
    }
    @Override
    public Object getCellEditorValue(){
        Object value = super.getCellEditorValue();
        if (lastMetadata == null || lastMetadata instanceof DeletedMetadata || 
                getDropboxClient() == null)
            return value;
        if (value != null && !value.toString().isBlank()){
            if (value.toString().equals(lastMetadata.getName()))
                return lastMetadata;
            try{
                return DropboxUtilities.rename(getDropboxClient(), lastMetadata, value.toString());
            } catch (RelocationErrorException ex){
            } catch (DbxException ex){
                LinkManager.getLogger().log(Level.WARNING, "Failed to rename file in Dropbox", ex);
            }
        }
        giveErrorFeedback(lastMetadata.getName());
        return lastMetadata;
    }
}
