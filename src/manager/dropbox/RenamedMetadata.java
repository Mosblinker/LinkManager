/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.Metadata;

/**
 * This is an object representing a renamed metadata
 * @author Mosblinker
 */
public class RenamedMetadata {
    
    private Metadata metadata;
        
    private String name;

    protected RenamedMetadata(Metadata metadata, String name){
        this.metadata = metadata;
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
}
