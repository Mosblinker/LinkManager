/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v2.*;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.users.*;
import icons.DefaultPfpIcon;
import java.awt.Color;
import java.awt.MediaTracker;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import manager.*;

/**
 * A utility library used with Dropbox stuff.
 * @author Mosblinker
 */
public class DropboxUtilities {
    /**
     * This is the minimum chunk size that can be used when uploading a decently 
     * large file. This is set to 4 MiB.
     */
    public static final long MINIMUM_CHUNK_SIZE = 0x400000;
    /**
     * This is the maximum chunk size that can be used when uploading a decently 
     * large file. This is set to 148 MiB.
     */
    public static final long MAXIMUM_CHUNK_SIZE = 0x9400000;
    /**
     * This is the default chunk size to use when uploading a decently large 
     * file. This is set to 8 MiB.
     */
    public static final long DEFAULT_CHUNK_SIZE = 0x800000;
    /**
     * This is the amount of times the program will try to upload a file to 
     * Dropbox when the file is being uploaded in chunks before giving up.
     */
    public static final int CHUNKED_UPLOAD_MAX_ATTEMPTS = 10;
    /**
     * This class cannot be constructed.
     */
    private DropboxUtilities() {}
    /**
     * 
     * @param path
     * @return 
     */
    public static String formatDropboxPath(String path){
            // If the path does not start with a slash
        if (!path.startsWith("/"))
                // Add a slash to the start of the file name
            return "/"+path;
        return path.trim();
    }
    /**
     * 
     * @param account
     * @param userName
     * @return 
     */
    public static Icon getProfilePicture(FullAccount account, String userName){
            // Get the URL for the user's profile picture
        String pfpUrl = account.getProfilePhotoUrl();
            // This will get the image icon with the user's profile picture
        ImageIcon pfpIcon = null;
            // If the user has a profile picture set.
        if (pfpUrl != null){
            try{    // Load the profile picture
                pfpIcon = new ImageIcon(new URL(pfpUrl), 
                            // Set the description to state that it's the user's 
                            // profile picture
                        userName+"'s Profile Picture");
            } catch (MalformedURLException ex){ 
                LinkManager.getLogger().log(Level.INFO, 
                        "Dropbox user PFP URL is malformed", ex);
            }
        }   // If the user did not have a profile picture set or the profile 
            // picture failed to load
        if (pfpIcon == null || (pfpIcon.getImageLoadStatus() 
                & (MediaTracker.ABORTED | MediaTracker.ERRORED)) != 0)
                // Return a default profile picture with a background color
                // dependent on the hash code of the user's unique user ID.
            return new DefaultPfpIcon(new Color(account.getAccountId().hashCode()));
        return pfpIcon;
    }
    /**
     * 
     * @param account
     * @return 
     */
    public static Icon getProfilePicture(FullAccount account){
        return getProfilePicture(account,account.getName().getDisplayName());
    }
    /**
     * 
     * @param allocation
     * @return 
     */
    public static long getAllocatedSpace(SpaceAllocation allocation){
            // If the user's account is part of a team
        if (allocation.isTeam())
                // Return the amount of space allocated to the team
            return allocation.getTeamValue().getAllocated();
        else    // Return the amount of space allocated to the user alone
            return allocation.getIndividualValue().getAllocated();
    }
    /**
     * 
     * @param usage
     * @return 
     */
    public static long getAllocatedSpace(SpaceUsage usage){
        return getAllocatedSpace(usage.getAllocation());
    }
    /**
     * 
     * @param ex
     * @return 
     */
    public static boolean fileNotFound(GetMetadataErrorException ex){
            // If the error is related to the path and it is because the path is 
            // not found, then the file doesn't exist
        return ex.errorValue.isPath() && ex.errorValue.getPathValue().isNotFound();
    }
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
                // If the file was not found
            if (fileNotFound(ex))
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
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @param l
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata download(File file, String path, 
            DbxUserFilesRequests dbxFiles, ProgressListener l) 
            throws IOException, DbxException{
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file));
                DbxDownloader<FileMetadata> dbxDown = dbxFiles.downloadBuilder(path).start()){
                // This is the metadata of the file that will be downloaded
            FileMetadata metadata;
                // If no progress listener was provided
            if (l == null)
                    // Download the file from Dropbox
                metadata = dbxDown.download(out);
            else
                    // Download the file from Dropbox
                metadata = dbxDown.download(out, l);
                // Set the last modified time of the file from the server
            file.setLastModified(metadata.getServerModified().getTime());
            return metadata;
        }
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata download(File file, String path, 
            DbxUserFilesRequests dbxFiles) throws IOException, DbxException{
        return download(file,path,dbxFiles,null);
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @param chunkSize
     * @param overwrite
     * @param l
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata upload(File file, String path, 
            DbxUserFilesRequests dbxFiles, long chunkSize, boolean overwrite, 
            ProgressListener l) throws IOException, DbxException{
        LinkManager.getLogger().entering("DropboxUtilities", "upload",
                new Object[]{file,path,dbxFiles,chunkSize,overwrite,l});
            // If the chunk size is smaller than the minimum chunk size
        if (chunkSize < MINIMUM_CHUNK_SIZE)
            throw new IllegalArgumentException("Chunk size is too small ("+
                    chunkSize+" < "+MINIMUM_CHUNK_SIZE+")");
            // If the chunk size is larger than the maximum chunk size
        else if (chunkSize > MAXIMUM_CHUNK_SIZE){
            throw new IllegalArgumentException("Chunk size is too large ("+
                    chunkSize+" > "+MAXIMUM_CHUNK_SIZE+")");
        }   // Get the write mode to use for uploading the file. If the file is 
            // to be overwritten if it exists, overwrite it. Otherwise, don't 
            // overwrite it if it exists
        WriteMode mode = (overwrite)?WriteMode.OVERWRITE:WriteMode.ADD;
            // This is the size of the file to upload
        long fileSize = file.length();
            // If we can get away with not breaking up the file into chunks
        if (fileSize < chunkSize){
                // Create an input stream to load the file 
            try (InputStream in = new BufferedInputStream(new FileInputStream(file))){
                    // Create an upload builder to upload the file with the 
                    // given mode
                UploadBuilder uploader = dbxFiles.uploadBuilder(path)
                        .withMode(mode)
                            // Set the client modified date to the file's last 
                            // modified date
                        .withClientModified(new Date(file.lastModified()));
                FileMetadata metadata;
                    // If the progress listener is null
                if (l == null)
                        // Upload the file to Dropbox
                    metadata = uploader.uploadAndFinish(in);
                else
                        // Upload the file to Dropbox
                    metadata = uploader.uploadAndFinish(in, l);
                LinkManager.getLogger().exiting("DropboxUtilities", "upload", metadata);
                return metadata;
            }
        } else {    // Code is heavily based off the upload file example for Dropbox
                // The amount of bytes that have been uploaded so far
            long uploaded = 0;
                // The session ID for the upload session, used to resume the 
                // session if we unexpectedly fail to upload
            String sessionID = null;
                // A progress listener that adjusts the bytes written to reflect 
                // the total amount of bytes written so far
            ProgressListener listener = new ProgressListener(){
                    // The amount of bytes in the chunks that have been uploaded
                    // so far
                long uploaded = 0;
                @Override
                public void onProgress(long bytesWritten){
                        // If the given listener is not null
                    if (l != null)
                            // Inform it of the total amount of bytes written
                        l.onProgress(bytesWritten+uploaded);
                        // If a chunk has been completed
                    if (bytesWritten == chunkSize)
                        uploaded += chunkSize;
                }
            };  // This is the most recent exception that was thrown
            DbxException dbxEx = null;
                // This is the file metadata to return upon a successful upload
            FileMetadata metadata = null;
                // A for loop to retry uploading the file if and when we've 
                // failed at some point
            for (int i = 0; i < CHUNKED_UPLOAD_MAX_ATTEMPTS && metadata == null; 
                    i++){
                LinkManager.getLogger().log(Level.FINER, "Uploading attempt {0}", i);
                dbxEx = null;
                    // Create an input stream to load the file 
                try (InputStream in = new BufferedInputStream(new FileInputStream(file))){
                        // Skip the bytes we've already read
                    in.skip(uploaded);
                        // If we are at the start of the upload and the session 
                        // hasn't begun yet
                    if (sessionID == null){
                            // Upload the first chunk and get the session ID
                        sessionID = dbxFiles.uploadSessionStart()
                                .uploadAndFinish(in, chunkSize, listener)
                                .getSessionId();
                        uploaded += chunkSize;
                    }   // A cursor that represents how far we are in the upload
                    UploadSessionCursor cursor = new UploadSessionCursor(sessionID, uploaded);
                        // While there are still chunks to upload before we 
                        // reach the last chunk
                    while (fileSize - uploaded > chunkSize){
                            // Upload the next chunk
                        dbxFiles.uploadSessionAppendV2(cursor)
                                .uploadAndFinish(in, chunkSize, listener);
                        uploaded += chunkSize;
                            // Update the cursor
                        cursor = new UploadSessionCursor(sessionID, uploaded);
                    }   // Create a commit info for the final part of the upload 
                        // with the file's path and the mode
                    CommitInfo info = CommitInfo.newBuilder(path)
                            .withMode(mode)
                                // Set the client modified date to the file's 
                                // last modified date
                            .withClientModified(new Date(file.lastModified()))
                            .build();
                        // Upload the final part of the file to Dropbox
                    metadata = dbxFiles.uploadSessionFinish(cursor, info)
                            .uploadAndFinish(in, fileSize - uploaded, listener);
                } catch (RetryException ex){
                    LinkManager.getLogger().log(Level.INFO, "Uploading attempt "+i+" failed", ex);
                        // This is thrown when the program wants us to back off 
                        // for a bit
                    dbxEx = ex;
                    try{    // Wait the amount of time we've been told to before 
                            // retrying, plus an additional millisecond for good 
                        Thread.sleep(ex.getBackoffMillis()+1);  // measure
                    } catch (InterruptedException ex1){ }
                } catch (NetworkIOException ex){
                    LinkManager.getLogger().log(Level.INFO, "Uploading attempt "+i+" failed", ex);
                    LinkManager.getLogger().log(Level.INFO, "Network error encountered", ex.getCause());
                        // If the previous error was also a network issue with 
                        // Dropbox
                    if (dbxEx instanceof NetworkIOException){
                        try{    // Wait a second just in case it was a timeout
                        Thread.sleep(1000);
                        } catch (InterruptedException ex1){ }
                    }
                    dbxEx = ex;
                } catch (UploadSessionFinishErrorException ex){
                    LinkManager.getLogger().log(Level.INFO, "Uploading attempt "+i+" failed", ex);
                        // If the offset into the stream doesn't match the 
                        // amount we've uploaded
                    if (ex.errorValue.isLookupFailed() && 
                            ex.errorValue.getLookupFailedValue().isIncorrectOffset()){
                        dbxEx = ex;
                            // Correct the offset loaded so far
                        uploaded = ex.errorValue.getLookupFailedValue().
                                getIncorrectOffsetValue().
                                getCorrectOffset();
                    } else
                        throw ex;
                }
            }   // If the file metadata is null (indicates a failed upload) and 
                // we have a Dropbox error to forward
            if (metadata == null && dbxEx != null)
                throw dbxEx;
            LinkManager.getLogger().exiting("DropboxUtilities", "upload", metadata);
            return metadata;
        }
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @param chunkSize
     * @param overwrite
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata upload(File file, String path, 
            DbxUserFilesRequests dbxFiles, long chunkSize, boolean overwrite) 
            throws IOException, DbxException{
        return upload(file,path,dbxFiles,chunkSize,overwrite,null);
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @param chunkSize
     * @param l
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata upload(File file, String path, 
            DbxUserFilesRequests dbxFiles, long chunkSize, ProgressListener l) 
            throws IOException, DbxException{
        return upload(file,path,dbxFiles,chunkSize,false,l);
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @param chunkSize
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata upload(File file, String path, 
            DbxUserFilesRequests dbxFiles, long chunkSize) throws IOException, 
            DbxException{
        return upload(file,path,dbxFiles,chunkSize,null);
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @param overwrite
     * @param l
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata upload(File file, String path, 
            DbxUserFilesRequests dbxFiles, boolean overwrite,ProgressListener l) 
            throws IOException, DbxException{
        return upload(file,path,dbxFiles,DEFAULT_CHUNK_SIZE,overwrite,l);
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @param overwrite
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata upload(File file, String path, 
            DbxUserFilesRequests dbxFiles,boolean overwrite) throws IOException, 
            DbxException{
        return upload(file,path,dbxFiles,overwrite,null);
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @param l
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata upload(File file, String path, 
            DbxUserFilesRequests dbxFiles, ProgressListener l) 
            throws IOException, DbxException{
        return upload(file,path,dbxFiles,DEFAULT_CHUNK_SIZE,l);
    }
    /**
     * 
     * @param file
     * @param path
     * @param dbxFiles
     * @return
     * @throws IOException
     * @throws DbxException 
     */
    public static FileMetadata upload(File file, String path, 
            DbxUserFilesRequests dbxFiles) throws IOException, DbxException{
        return upload(file,path,dbxFiles,null);
    }
    /**
     * 
     * @param fileSize
     * @param l
     * @return 
     */
    public static ProgressListener setUpProgressListener(long fileSize, 
            ProgressObserver l){
            // Set the progress to be zero
        l.setValue(0);
            // Get the value needed to divide the file length to get it back 
            // into the range of integers
        double div = LinkManagerUtilities.getFileSizeDivider(fileSize);
            // Set the progress maximum to the file length divided by the 
            // divisor
        l.setMaximum((int)Math.ceil(fileSize / div));
            // Create and return a progress listener that will update the 
            // progress bar to reflect the bytes that have been written so far
        return (long bytesWritten) -> {
                // Update the progress with the amount of bytes written
            l.setValue((int)Math.ceil(bytesWritten / div));
        };
    }
    /**
     * 
     * @param client
     * @param path
     * @param list
     * @return
     * @throws DbxException 
     */
    public static List<Metadata> listFolder(DbxClientV2 client, String path, 
            List<Metadata> list) throws DbxException{
        LinkManager.getLogger().entering("DropboxUtilities", "listFolder",
                new Object[]{client,path,list});
        if (list == null)
            list = new ArrayList<>();
            //  Get the files and folder metadata from the Dropbox directory
        ListFolderResult results = client.files().listFolder((path!=null)?path:"");
        for (Metadata metadata : results.getEntries()){
            list.add(metadata);
        }
        while (results.getHasMore()){
            results = client.files().listFolderContinue(results.getCursor());
            for (Metadata metadata : results.getEntries()){
                list.add(metadata);
            }
        }
        LinkManager.getLogger().exiting("DropboxUtilities", "listFolder", list);
        return list;
    }
    /**
     * 
     * @param client
     * @param path
     * @return
     * @throws DbxException 
     */
    public static List<Metadata> listFolder(DbxClientV2 client, String path) throws DbxException{
        return listFolder(client,path,new ArrayList<>());
    }
    /**
     * 
     * @param client
     * @param metadata
     * @param includeDeleted
     * @return
     * @throws DbxException 
     */
    public static DefaultMutableTreeNode listFolderTree(DbxClientV2 client, 
            Metadata metadata, boolean includeDeleted) throws DbxException{
        LinkManager.getLogger().entering("DropboxUtilities", "listFolderTree",
                new Object[]{client,metadata,includeDeleted});
        Objects.requireNonNull(metadata);
        if (metadata instanceof DeletedMetadata && !includeDeleted){
            LinkManager.getLogger().exiting("DropboxUtilities", 
                    "listFolderTree", null);
            return null;
        }
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(metadata);
        if (metadata instanceof FolderMetadata)
            traverseFolderTree(client,metadata.getPathLower(),includeDeleted,node);
        LinkManager.getLogger().exiting("DropboxUtilities", 
                "listFolderTree", node);
        return node;
    }
    /**
     * 
     * @param client
     * @param metadata
     * @return
     * @throws DbxException 
     */
    public static DefaultMutableTreeNode listFolderTree(DbxClientV2 client, 
            Metadata metadata) throws DbxException{
        return listFolderTree(client,metadata,false);
    }
    /**
     * 
     * @param client
     * @param path
     * @param includeDeleted
     * @return
     * @throws DbxException 
     */
    public static DefaultMutableTreeNode listFolderTree(DbxClientV2 client, 
            String path, boolean includeDeleted) throws DbxException{
        LinkManager.getLogger().entering("DropboxUtilities", "listFolderTree",
                new Object[]{client,path,includeDeleted});
        DefaultMutableTreeNode root;
        if (path == null || path.isEmpty() || path.equals("/")){
            root = new DefaultMutableTreeNode();
            traverseFolderTree(client,"",includeDeleted,root);
        } else {
            root = listFolderTree(client,client.files().getMetadata(path),
                    includeDeleted);
        }
        LinkManager.getLogger().exiting("DropboxUtilities", "listFolderTree", root);
        return root;
    }
    /**
     * 
     * @param client
     * @param path
     * @return
     * @throws DbxException 
     */
    public static DefaultMutableTreeNode listFolderTree(DbxClientV2 client, 
            String path) throws DbxException{
        return listFolderTree(client,path,false);
    }
    /**
     * 
     * @param client
     * @param path
     * @param includeDeleted
     * @param root
     * @return
     * @throws DbxException 
     */
    private static void traverseFolderTree(DbxClientV2 client, 
            String path, boolean includeDeleted, DefaultMutableTreeNode root) 
            throws DbxException{
        LinkManager.getLogger().entering("DropboxUtilities", "traverseFolderTree",
                new Object[]{client,path,includeDeleted,root});
            //  Get the files and folder metadata from the Dropbox directory
        ListFolderResult results = client.files().listFolder((path!=null)?path:"");
        for (Metadata metadata : results.getEntries()){
            if (metadata == null)
                continue;
            DefaultMutableTreeNode node = listFolderTree(client,metadata,
                    includeDeleted);
            if (node != null)
                root.add(node);
        }
        while (results.getHasMore()){
            results = client.files().listFolderContinue(results.getCursor());
            for (Metadata metadata : results.getEntries()){
                if (metadata == null)
                    continue;
                DefaultMutableTreeNode node = listFolderTree(client,metadata,
                        includeDeleted);
                if (node != null)
                    root.add(node);
            }
        }
        LinkManager.getLogger().exiting("DropboxUtilities", "traverseFolderTree");
    }
}
