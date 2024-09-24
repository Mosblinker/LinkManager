/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Milo Steier
 */
public class DbxWebAuthWrapper {
    /**
     * 
     */
    private final DbxWebAuth webAuth;
    /**
     * 
     */
    private final DbxPKCEWebAuth webAuthPKCE;
    /**
     * 
     * @param webAuth
     * @param webAuthPKCE 
     */
    protected DbxWebAuthWrapper(DbxWebAuth webAuth, DbxPKCEWebAuth webAuthPKCE){
        if (webAuth == null && webAuthPKCE == null)
            throw new NullPointerException();
        this.webAuth = webAuth;
        this.webAuthPKCE = webAuthPKCE;
    }
    /**
     * 
     * @param webAuth 
     */
    public DbxWebAuthWrapper(DbxWebAuth webAuth){
        this(webAuth,null);
    }
    /**
     * 
     * @param webAuth 
     */
    public DbxWebAuthWrapper(DbxPKCEWebAuth webAuth){
        this(null,webAuth);
    }
    /**
     * 
     * @param requestConfig
     * @param appInfo
     * @param usePKCE 
     */
    public DbxWebAuthWrapper(DbxRequestConfig requestConfig,DbxAppInfo appInfo, 
            boolean usePKCE){
        Objects.requireNonNull(requestConfig, "Dropbox Request Config cannot be null");
        Objects.requireNonNull(appInfo, "Dropbox App Info cannot be null");
        if (!usePKCE && !appInfo.hasSecret())
            throw new IllegalArgumentException("Dropbox App Info does not have an app secret.");
        else if (usePKCE && appInfo.hasSecret())
            appInfo = new DbxAppInfo(appInfo.getKey());
        if (usePKCE){
            webAuthPKCE = new DbxPKCEWebAuth(requestConfig,appInfo);
            webAuth = null;
        } else {
            webAuth = new DbxWebAuth(requestConfig,appInfo);
            webAuthPKCE = null;
        }
    }
    /**
     * 
     * @param requestConfig
     * @param appInfo 
     */
    public DbxWebAuthWrapper(DbxRequestConfig requestConfig,DbxAppInfo appInfo){
        Objects.requireNonNull(requestConfig, "Dropbox Request Config cannot be null");
        Objects.requireNonNull(appInfo, "Dropbox App Info cannot be null");
        if (appInfo.hasSecret()){
            webAuth = new DbxWebAuth(requestConfig,appInfo);
            webAuthPKCE = null;
        } else {
            webAuthPKCE = new DbxPKCEWebAuth(requestConfig,appInfo);
            webAuth = null;
        }
    }
    /**
     * 
     * @return 
     */
    public DbxWebAuth getWebAuth(){
        return webAuth;
    }
    /**
     * 
     * @return 
     */
    public DbxPKCEWebAuth getPKCEWebAuth(){
        return webAuthPKCE;
    }
    /**
     * 
     * @return 
     */
    public boolean usesPKCE(){
        return webAuthPKCE != null;
    }
    /**
     * This starts authorization and returns an "authorization URL" on the 
     * Dropbox website that let the user grant your app access to their Dropbox 
     * account. <p>
     * 
     * If a redirect URI was specified ({@link 
     * DbxWebAuth.Request.Builder#withRedirectUri(java.lang.String, 
     * com.dropbox.core.DbxSessionStore)}), then users will be redirected to the 
     * redirect URI after completing the authorization flow. The redirect URI 
     * should bring user back to your app on end device. Make sure to call 
     * {@link #finishFromRedirect(java.lang.String, 
     * com.dropbox.core.DbxSessionStore, java.util.Map)} with the query 
     * parameters received from the redirect. <p>
     * 
     * If no redirect URI was specified ({@link 
     * DbxWebAuth.Request.Builder#withNoRedirect()}), then users who grant 
     * access will be shown an "authorization code". The user must copy/paste 
     * the authorization code back into your app, at which point you can call 
     * {@link finishFromCode(String)} with to get an access token.
     * 
     * @param request OAuth 2.0 web-based authorization flow request 
     * configuration.
     * @return Authorization URL of website user can use to authorize your app.
     * @throws IllegalStateException If this does not use PKCE and either the 
     * {@link DbxWebAuth} instance that this was created was created using the 
     * deprecated {@link DbxWebAuth#DbxWebAuth(DbxRequestConfig, DbxAppInfo, 
     * String, DbxSessionStore)} constructor or was created with a {@link 
     * DbxAppInfo} without an app secret.
     */
    public String authorize(DbxWebAuth.Request request){
            // If this is using the PKCE web auth
        if (usesPKCE())
            return getPKCEWebAuth().authorize(request);
        else
            return getWebAuth().authorize(request);
    }
    /**
     * Call this after the user has visited the authorizaton URL and copy/pasted 
     * the authorization code that Dropbox gave them.
     * 
     * @param code The authorization code shown to the user when they clicked 
     * "Allow" on the authorization, page on the Dropbox website. This cannot be 
     * null.
     * @return
     * @throws DbxException If an error occurs communicating with Dropbox, or if 
     * this is using PKCE and this is not the same instance that was used to 
     * generate the authorization URL.
     */
    public DbxAuthFinish finishFromCode(String code) throws DbxException{
            // If this is using the PKCE web auth
        if (usesPKCE())
            return getPKCEWebAuth().finishFromCode(code);
        else
            return getWebAuth().finishFromCode(code);
    }
    /**
     * Call this after the user has visited the authorizaton URL and Dropbox has 
     * redirected them back to you at the redirect URI.
     * @param redirectUri The original redirect URI used by {@link 
     * authorize(DbxWebAuth.Request)}, never null.
     * @param sessionStore Session store used by {@link 
     * authorize(DbxWebAuth.Request)} to store CSRF tokens, never null.
     * @param params The query parameters on the GET request to your redirect 
     * URI, never null.
     * @return
     * @throws DbxException If an error occurs communicating with Dropbox, or if 
     * this is using PKCE and this is not the same instance that was used to 
     * generate the authorization URL.
     * @throws DbxWebAuth.BadRequestException If the redirect request is missing 
     * required query parameters, contains duplicate parameters, or includes 
     * mutually exclusive parameters (e.g. {@code "error"} and {@code "code"}).
     * @throws DbxWebAuth.BadStateException If the CSRF token retrieved from 
     * {@code sessionStore} is null or malformed.
     * @throws DbxWebAuth.CsrfException If the CSRF token passed in {@code 
     * params} does not match the CSRF token from {@code sessionStore}. This 
     * implies the redirect request may be forged.
     * @throws DbxWebAuth.NotApprovedException If the user chose to deny the 
     * authorization request.
     * @throws DbxWebAuth.ProviderException If an OAuth2 error response besides 
     * {@code "access_denied"} is set.
     */
    public DbxAuthFinish finishFromRedirect(String redirectUri, 
            DbxSessionStore sessionStore, Map<String, String[]> params) throws
            DbxException, DbxWebAuth.BadRequestException, 
            DbxWebAuth.BadStateException, DbxWebAuth.CsrfException, 
            DbxWebAuth.NotApprovedException, DbxWebAuth.ProviderException{
            // If this is using the PKCE web auth
        if (usesPKCE())
            return getPKCEWebAuth().finishFromRedirect(redirectUri,sessionStore, 
                    params);
        else
            return getWebAuth().finishFromRedirect(redirectUri, sessionStore, 
                    params);
    }
}
