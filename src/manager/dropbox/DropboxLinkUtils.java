/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.oauth.*;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;

/**
 *
 * @author Milo Steier
 */
public abstract class DropboxLinkUtils {
    
    public DropboxLinkUtils(){
        
    }
    
    
    
    /**
     * 
     * @return 
     */
    public abstract String getAppKey();
    /**
     * 
     * @return 
     */
    public abstract String getSecretKey();
    /**
     * 
     * @return 
     */
    public boolean hasSecretKey(){
        return getSecretKey() != null;
    }
    /**
     * 
     * @return 
     */
    public abstract boolean usesPKCE();
    /**
     * 
     * @return 
     */
    public abstract Collection<String> getPermissionScope();
    /**
     * 
     * @return 
     */
    public abstract String getClientID();
    
    /**
     * 
     * @param includeSecret
     * @return 
     */
    public DbxAppInfo getAppInfo(boolean includeSecret){
        if (includeSecret && !hasSecretKey())
            throw new IllegalStateException("This does not have a secret");
        if (includeSecret)
            return new DbxAppInfo(getAppKey(),getSecretKey());
        else
            return new DbxAppInfo(getAppKey());
    }
    /**
     * 
     * @return 
     */
    public DbxAppInfo getAppInfo(){
        return DropboxLinkUtils.this.getAppInfo(hasSecretKey() && !usesPKCE());
    }
    /**
     * 
     * @param scope
     * @return 
     */
    public DbxWebAuth.Request createWebAuthRequest(Collection<String> scope){
        DbxWebAuth.Request.Builder builder = DbxWebAuth.newRequestBuilder()
                .withNoRedirect()
                .withTokenAccessType(TokenAccessType.OFFLINE);
        if (scope != null)
            builder = builder.withScope(scope);
        return builder.build();
    }
    /**
     * 
     * @return 
     */
    public DbxWebAuth.Request createWebAuthRequest(){
        return createWebAuthRequest(getPermissionScope());
    }
    /**
     * 
     * @return 
     */
    public abstract String getAccessToken();
    /**
     * 
     * @param token 
     */
    public abstract void setAccessToken(String token);
    /**
     * 
     * @return 
     */
    public abstract String getRefreshToken();
    /**
     * 
     * @param token 
     */
    public abstract void setRefreshToken(String token);
    /**
     * 
     * @return 
     */
    public abstract Long getTokenExpiresAt();
    /**
     * 
     * @return 
     */
    public Date getTokenExpiresAtDate(){
        Long time = getTokenExpiresAt();
        return (time != null) ? new Date(time) : null;
    }
    /**
     * 
     * @param time 
     */
    public abstract void setTokenExpiresAt(Long time);
    /**
     * 
     * @param time 
     */
    public void setTokenExpiresAt(Date time){
        setTokenExpiresAt((time != null) ? time.getTime() : null);
    }
    /**
     * 
     * @param accessToken
     * @param expiresAt
     * @param refreshToken 
     */
    public void setCredentials(String accessToken, Long expiresAt, 
            String refreshToken){
        setAccessToken(accessToken);
        setRefreshToken(refreshToken);
        setTokenExpiresAt(expiresAt);
    }
    /**
     * 
     * @param authFinish 
     */
    public void setCredentials(DbxAuthFinish authFinish){
        setCredentials(authFinish.getAccessToken(),
                authFinish.getExpiresAt(),
                authFinish.getRefreshToken());
    }
    /**
     * 
     * @param credentials 
     */
    public void setCredentials(DbxCredential credentials){
        setCredentials(credentials.getAccessToken(),
                credentials.getExpiresAt(),
                credentials.getRefreshToken());
    }
    /**
     * 
     * @param refresh 
     */
    public void refreshCredentials(DbxRefreshResult refresh){
        setAccessToken(refresh.getAccessToken());
        setTokenExpiresAt(refresh.getExpiresAt());
    }
    /**
     * 
     */
    public void clearCredentials(){
        setCredentials(null,null,null);
    }
    /**
     * 
     * @param usePKCE
     * @return 
     */
    public DbxCredential getCredentials(boolean usePKCE){
        String accessToken = getAccessToken();
        if (accessToken == null)
            return null;
        String refreshToken = getRefreshToken();
        if (refreshToken == null)
                // This does not support refreshing the token
            return new DbxCredential(accessToken);
        Long expiresAt = getTokenExpiresAt();
        return (usePKCE) ? 
                new DbxCredential(accessToken,expiresAt,refreshToken,getAppKey()) : 
                new DbxCredential(accessToken,expiresAt,refreshToken,getAppKey(),
                        getSecretKey());
    }
    /**
     * 
     * @return 
     */
    public DbxCredential getCredentials(){
        return DropboxLinkUtils.this.getCredentials(usesPKCE() || !hasSecretKey());
    }
    /**
     * 
     * @return 
     */
    public DbxRequestConfig.Builder createRequestBuilder(){
        return DbxRequestConfig.newBuilder(getClientID());
    }
    /**
     * 
     * @return 
     */
    public DbxRequestConfig createRequest(){
        return createRequestBuilder().build();
    }
    
    /**
     * 
     * @return 
     */
    protected String paramString(){
        return "appKey="+Objects.toString(getAppKey(), "")+
                ",secretKey="+Objects.toString(getSecretKey(), "")+
                ",usesPKCE="+usesPKCE()+
                ",permissionScope="+Objects.toString(getPermissionScope(), "")+
                ",clientID="+Objects.toString(getClientID(),"")+
                ",accessToken="+Objects.toString(getAccessToken(),"")+
                ",refreshToken="+Objects.toString(getRefreshToken(),"")+
                ",tokenExpiresAt="+Objects.toString(getTokenExpiresAt(),"")+
                " ("+Objects.toString(getTokenExpiresAtDate(),"")+")";
    }
    
    @Override
    public String toString(){
        return getClass().getName()+"["+paramString()+"]";
    }
}
