/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.v2.*;
import com.dropbox.core.v2.files.*;

/**
 * A utility library used with Dropbox stuff.
 * @author Mosblinker
 */
public class DropboxUtilities {
    /**
     * This class cannot be constructed.
     */
    private DropboxUtilities() {}
    /**
     * 
     * @param path
     * @param dbxFiles
     * @return
     * @throws DbxException 
     */
    public static boolean exists(String path, DbxUserFilesRequests dbxFiles) 
            throws DbxException{
        try{    // Check if the file exists
            dbxFiles.getMetadataBuilder(path).start();
            return true;
        } catch (GetMetadataErrorException ex){
                // If the error is related to the path and it is because the 
                // path is not found, then the file doesn't exist
            if (ex.errorValue.isPath() && ex.errorValue.getPathValue().isNotFound())
                return false;
            throw ex;
        }
    }
    /**
     * 
     * @param path
     * @param client
     * @return
     * @throws DbxException 
     */
    public static boolean exists(String path, DbxClientV2 client) 
            throws DbxException{
        return exists(path,client.files());
    }
    /**
     * 
     * @param path
     * @param dbxFiles
     * @return
     * @throws IllegalArgumentException
     * @throws DeleteErrorException
     * @throws DbxException 
     */
    public static DeleteResult deleteIfExists(String path, 
            DbxUserFilesRequests dbxFiles) throws DbxException{
        try{    // Try to delete the file
            return dbxFiles.deleteV2(path);
        } catch (DeleteErrorException ex){
                // If the reason why it failed to delete is because the file 
                // doesn't exist
            if (ex.errorValue.isPathLookup() && 
                    ex.errorValue.getPathLookupValue().isNotFound())
                    // Just return null
                return null;
                // Forward the exception
            throw ex;
        }
    }
}
