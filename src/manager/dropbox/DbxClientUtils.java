/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.oauth.*;
import com.dropbox.core.v2.DbxClientV2;

/**
 *
 * @author Mosblinker
 */
public class DbxClientUtils {
    /**
     * 
     */
    private final DropboxLinkUtils dbxUtils;
    /**
     * 
     */
    private DbxRequestConfig dbxConfig;
    /**
     * 
     */
    private DbxCredential cred = null;
    /**
     * 
     */
    private DbxClientV2 client = null;
    /**
     * 
     * @param dbxUtils 
     * @param dbxConfig 
     */
    public DbxClientUtils(DropboxLinkUtils dbxUtils, DbxRequestConfig dbxConfig) {
        if (dbxUtils == null)
            throw new NullPointerException();
        this.dbxUtils = dbxUtils;
        this.dbxConfig = dbxConfig;
    }
    /**
     * 
     * @param dbxUtils 
     */
    public DbxClientUtils(DropboxLinkUtils dbxUtils){
        this(dbxUtils,null);
    }
    /**
     * 
     * @return 
     */
    public DropboxLinkUtils getDropboxUtils(){
        return dbxUtils;
    }
    /**
     * 
     * @return 
     */
    public DbxRequestConfig getRequestConfig(){
        if (dbxConfig == null)
            dbxConfig = dbxUtils.createRequest();
        return dbxConfig;
    }
    /**
     * 
     * @param dbxConfig 
     * @return  
     */
    public DbxClientUtils setRequestConfig(DbxRequestConfig dbxConfig){
        if (dbxConfig == null)
            throw new NullPointerException();
        if (client != null)
            throw new IllegalStateException("Dbx Client has been initialized");
        this.dbxConfig = dbxConfig;
        return this;
    }
    /**
     * 
     * @return 
     */
    public DbxCredential getCredentials(){
        if (cred == null)
            cred = dbxUtils.getCredentials();
        return cred;
    }
    /**
     * 
     * @return 
     */
    public DbxClientV2 getClient(){
        if (client == null)
            client = new DbxClientV2(getRequestConfig(),getCredentials());
        return client;
    }
    /**
     * 
     * @return
     * @throws DbxException 
     */
    public DbxClientUtils refreshCredentials() throws DbxException{
        dbxUtils.refreshCredentials(getClient(), getCredentials());
        return this;
    }
    /**
     * 
     * @return
     * @throws DbxException 
     */
    public DbxClientV2 getClientWithRefresh() throws DbxException{
        return refreshCredentials().getClient();
    }
}
