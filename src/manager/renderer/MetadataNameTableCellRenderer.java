/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.renderer;

import com.dropbox.core.v2.files.*;
import javax.swing.Icon;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import manager.dropbox.DbxRootMetadata;
import manager.icons.DropboxIcon;

/**
 *
 * @author Mosblinker
 */
public class MetadataNameTableCellRenderer extends DefaultTableCellRenderer{
    /**
     * 
     */
    private static final Icon DROPBOX_ICON = new DropboxIcon(16);
    /**
     * 
     */
    private Icon folderIcon = null;
    /**
     * 
     */
    private Icon defaultFolderIcon;
    /**
     * 
     */
    private Icon fileIcon = null;
    /**
     * 
     */
    private Icon defaultFileIcon;
    /**
     * 
     */
    public MetadataNameTableCellRenderer(){
        UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
        defaultFolderIcon = uiDefaults.getIcon("FileChooser.directoryIcon");
        defaultFileIcon = uiDefaults.getIcon("FileChooser.fileIcon");
    }
    /**
     * 
     * @return 
     */
    public Icon getDefaultFolderIcon(){
        return defaultFolderIcon;
    }
    /**
     * 
     * @param icon 
     */
    public void setFolderIcon(Icon icon){
        folderIcon = icon;
    }
    /**
     * 
     * @return 
     */
    public Icon getFolderIcon(){
        if (folderIcon == null)
            return getDefaultFolderIcon();
        return folderIcon;
    }
    /**
     * 
     * @return 
     */
    public Icon getDefaultFileIcon(){
        return defaultFileIcon;
    }
    /**
     * 
     * @param icon 
     */
    public void setFileIcon(Icon icon){
        fileIcon = icon;
    }
    /**
     * 
     * @return 
     */
    public Icon getFileIcon(){
        if (fileIcon == null)
            return getDefaultFileIcon();
        return fileIcon;
    }
    /**
     * 
     * @param metadata
     * @return 
     */
    protected Icon getIconForMetadata(Metadata metadata){
        if (metadata instanceof FileMetadata)
            return getFileIcon();
        if (metadata instanceof FolderMetadata)
            return getFolderIcon();
        if (metadata instanceof DbxRootMetadata || metadata == null)
            return DROPBOX_ICON;
        return null;
    }
    @Override
    protected void setValue(Object value){
        Icon icon = null;
        if (value instanceof Metadata){
            Metadata metadata = (Metadata)value;
            icon = getIconForMetadata(metadata);
            super.setValue(metadata.getName());
        } else{
            super.setValue(value);
        }
        setIcon(icon);
    }
}
