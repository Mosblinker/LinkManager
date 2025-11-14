/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import java.awt.Component;
import java.util.Objects;
import java.util.logging.Level;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import manager.LinkManager;

/**
 * This is an object representing a renamed metadata
 * @author Mosblinker
 */
public class RenamedMetadata {
    
    private Metadata metadata;
        
    private String name;

    protected RenamedMetadata(Metadata metadata, String name){
        this.metadata = Objects.requireNonNull(metadata);
        this.name = name;
    }

    public Metadata getMetadata(){
        return metadata;
    }

    public String getNewName(){
        return name;
    }
    @Override
    public String toString(){
        return name;
    }
    /**
     * 
     * @param client
     * @return 
     * @throws com.dropbox.core.DbxException 
     */
    public Metadata rename(DbxClientV2 client) throws DbxException{
        if (getNewName() == null || getNewName().isBlank())
            return null;
        if (getNewName().equals(getMetadata().getName()))
            return getMetadata();
        return DropboxUtilities.rename(client, getMetadata(), getNewName());
    }
    /**
     * 
     * @param client
     * @param parent
     * @return 
     */
    public Metadata renameWithError(DbxClientV2 client, Component parent){
        try{
            Metadata temp = rename(client);
            if (temp != null)
                return temp;
        } catch (RelocationErrorException ex){
            // TODO: Add different error prompts for the different types of errors
            
            // Issue thrown when there's a conflict with the name: {".tag":"to","to":{".tag":"conflict","conflict":"folder"}}
            LinkManager.getLogger().log(Level.WARNING, "Failed to rename file in Dropbox", ex);
        } catch (DbxException ex){
            LinkManager.getLogger().log(Level.WARNING, "Failed to rename file in Dropbox", ex);
        }
        giveRenameErrorFeedback(parent);
        return getMetadata();
    }
    /**
     * 
     * @param parent 
     */
    public void giveRenameErrorFeedback(Component parent){
        giveRenameErrorFeedback(parent,getMetadata().getName());
    }
    /**
     * 
     * @param parent
     * @param name
     */
    public static void giveRenameErrorFeedback(Component parent, String name){
        UIManager.getLookAndFeel().provideErrorFeedback(parent);
        JOptionPane.showMessageDialog(parent, "Cannot rename "+name+
                ": A file with the name you specified already exists. "
                        + "Specify a different file name.", 
                "Error Renaming File of Folder", JOptionPane.ERROR_MESSAGE);
    }
}
