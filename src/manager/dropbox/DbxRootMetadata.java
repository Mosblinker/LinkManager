/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.v2.files.*;

/**
 * This is a metadata object that represents the root folder for Dropbox.
 * @author Mosblinker
 */
public final class DbxRootMetadata extends Metadata{
    /**
     * 
     */
    public DbxRootMetadata() {
        super("","/","/",null,null);
    }
}
