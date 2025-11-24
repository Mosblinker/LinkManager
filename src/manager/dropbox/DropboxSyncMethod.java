/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.*;
import manager.*;
import manager.sync.*;

/**
 *
 * @author Mosblinker
 */
public abstract class DropboxSyncMethod implements SyncMethod<FileMetadata, FileMetadata, DropboxAccountData>{
    /**
     * 
     * @return 
     */
    public abstract DropboxLinkUtils getDropboxLinkUtils();
    /**
     * 
     * @return 
     */
    public abstract long getChunkSize();
    /**
     * 
     * @return 
     * @throws DbxException
     */
    public DbxClientV2 getClient() throws DbxException{
        DropboxLinkUtils dbxUtils = getDropboxLinkUtils();
        if (dbxUtils != null)
                // Get a client to communicate with Dropbox, refreshing the 
                // Dropbox credentials if necessary
            return dbxUtils.createClientUtils().getClientWithRefresh();
        else
            throw new IllegalStateException();
    }
    @Override
    public SyncMode getSyncMode() {
        return SyncMode.DROPBOX;
    }
    @Override
    public DropboxAccountData getAccountData() throws DbxException {
        LinkManager.getLogger().entering(this.getClass().getName(), "getAccountData");
        DropboxAccountData data = new DropboxAccountData(getClient());
        LinkManager.getLogger().exiting(this.getClass().getName(), "getAccountData", data);
        return data;
    }
    @Override
    public boolean isUsable() {
        return getDropboxLinkUtils() != null;
    }
    @Override
    public boolean isLoggedIn() {
        return isUsable() && getDropboxLinkUtils().getAccessToken() != null;
    }
    @Override
    public FileMetadata download(File file, String path, ProgressObserver l) throws DbxException, IOException {
        LinkManager.getLogger().entering(this.getClass().getName(), "download", 
                new Object[]{file, path, l});
            // Get a client to communicate with Dropbox, refreshing the Dropbox 
            // credentials if necessary
        DbxClientV2 client = getClient();
            // Get the file namespace for Dropbox
        DbxUserFilesRequests dbxFiles = client.files();
            // This gets the size of the file to be downloaded
        Long size = null;
        try{    // Get the metadata for the file
            Metadata metadata = dbxFiles.getMetadataBuilder(path).start();
                // If the metadata is actually file metadata
            if (metadata instanceof FileMetadata){
                    // Get the size of the file
                size = ((FileMetadata) metadata).getSize();
            }
        } catch (GetMetadataErrorException ex){
            LinkManager.getLogger().log(Level.WARNING,"Failed to download from Dropbox",ex);
                // If the error because the file doesn't exist
            if (DropboxUtilities.fileNotFound(ex)){
                LinkManager.getLogger().exiting(this.getClass().getName(), 
                        "download", null);
                return null;
            } else 
                throw ex;
        }   // This is the progress listener to use to listen to how many bytes 
            // have been downloaded so far.
        ProgressListener listener = null;
            // If the file size was loaded
        if (size != null){
                // Setup the progress bar and get the progress listener
            listener = DropboxUtilities.setUpProgressListener(size, l);
                // Set the progress bar to not be indeterminate
            l.setIndeterminate(false);
        }   // Download the file from Dropbox
        FileMetadata data = DropboxUtilities.download(file, path, dbxFiles, listener);
        LinkManager.getLogger().exiting(this.getClass().getName(), "download", data);
        return data;
    }
    @Override
    public FileMetadata upload(File file, String path, ProgressObserver l) throws DbxException, IOException {
        LinkManager.getLogger().entering(this.getClass().getName(), "upload", 
                new Object[]{file, path, l});
            // Get a client to communicate with Dropbox, refreshing the Dropbox 
            // credentials if necessary
        DbxClientV2 client = getClient();
            // Setup the progress bar and get the progress listener used to 
            // update the progress bar to reflect the bytes that have been 
            // uploaded so far.
        ProgressListener listener = DropboxUtilities.setUpProgressListener(
                file.length(), l);
            // Set the progress bar to not be indeterminate
        l.setIndeterminate(false);
            // Upload the file to Dropbox, using the set chunk size and 
            // overwriting the file if it already exists
        FileMetadata data = DropboxUtilities.upload(file, path, client.files(), 
                getChunkSize(), true, listener);
        LinkManager.getLogger().exiting(this.getClass().getName(), "upload", data);
        return data;
    }
}
