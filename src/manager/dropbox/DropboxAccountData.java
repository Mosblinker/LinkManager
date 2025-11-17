/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.users.*;
import java.util.Objects;
import javax.swing.Icon;
import manager.sync.*;

/**
 *
 * @author Mosblinker
 */
public class DropboxAccountData extends AbstractAccountData{
    /**
     * 
     */
    protected DbxUserUsersRequests users;
    /**
     * The account details for the user
     */
    private FullAccount account = null;
    /**
     * The space usage for the user
     */
    private SpaceUsage spaceUsage = null;
    /**
     * 
     * @param users 
     */
    public DropboxAccountData(DbxUserUsersRequests users){
        this.users = Objects.requireNonNull(users);
    }
    /**
     * 
     * @param client 
     */
    public DropboxAccountData(DbxClientV2 client){
        this(client.users());
    }
    /**
     * 
     * @return 
     */
    public DbxUserUsersRequests getUserRequest(){
        return users;
    }
    /**
     * Get the account details for the user
     * @return 
     * @throws com.dropbox.core.DbxException 
     */
    protected FullAccount getAccount() throws DbxException{
        if (account == null)
                // Get the account details for the user
            account = getUserRequest().getCurrentAccount();
        return account;
    }
    /**
     * Get the space usage for the user
     * @return
     * @throws DbxException 
     */
    protected SpaceUsage getSpaceUsage() throws DbxException{
        if (spaceUsage == null)
                // Get the space usage for the user
            spaceUsage = users.getSpaceUsage();
        return spaceUsage;
    }
    @Override
    public String getAccountName() throws DbxException {
        return getAccount().getName().getDisplayName();
    }
    @Override
    public Icon getProfilePictureIcon() throws DbxException {
        return DropboxUtilities.getProfilePicture(account, getAccountName());
    }
    @Override
    public Long getSpaceUsed() throws DbxException {
        return getSpaceUsage().getUsed();
    }
    @Override
    public Long getAllocatedSpace() throws DbxException {
        return DropboxUtilities.getAllocatedSpace(getSpaceUsage());
    }
    @Override
    public DatabaseSyncMode getSyncMode() {
        return DatabaseSyncMode.DROPBOX;
    }
}
