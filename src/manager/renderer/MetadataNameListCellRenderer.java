/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.renderer;

import com.dropbox.core.v2.files.*;
import java.awt.Component;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JList;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import manager.icons.DropboxIcon;

/**
 *
 * @author Mosblinker
 */
public class MetadataNameListCellRenderer extends DefaultListCellRenderer{
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
    public MetadataNameListCellRenderer(){
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
        if (metadata == null)
            return DROPBOX_ICON;
        return null;
    }
    /**
     * 
     * @param value
     * @return 
     */
    protected Object getValue(Metadata value){
        return (value!=null)?value.getName():null;
    }
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
        Metadata metadata = null;
        if (value instanceof Metadata){
            metadata = (Metadata)value;
            value = getValue(metadata);
        } else if (value == null)
            value = getValue(null);
        Component comp = super.getListCellRendererComponent(list, value, index, 
                isSelected, cellHasFocus);
        setIcon(getIconForMetadata(metadata));
        return comp;
    }
    
}
