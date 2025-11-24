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
     * The account details for the user
     */
    private FullAccount account;
    /**
     * The space usage for the user
     */
    private SpaceUsage spaceUsage;
    /**
     * 
     * @param account
     * @param spaceUsage 
     */
    public DropboxAccountData(FullAccount account, SpaceUsage spaceUsage){
        this.account = Objects.requireNonNull(account);
        this.spaceUsage = Objects.requireNonNull(spaceUsage);
    }
    /**
     * 
     * @param users 
     * @throws com.dropbox.core.DbxException 
     */
    public DropboxAccountData(DbxUserUsersRequests users) throws DbxException{
        this(users.getCurrentAccount(),users.getSpaceUsage());
    }
    /**
     * 
     * @param client 
     * @throws com.dropbox.core.DbxException 
     */
    public DropboxAccountData(DbxClientV2 client) throws DbxException{
        this(client.users());
    }
    /**
     * Get the account details for the user
     * @return 
     */
    public FullAccount getAccount(){
        return account;
    }
    /**
     * Get the space usage for the user
     * @return
     */
    public SpaceUsage getSpaceUsage(){
        return spaceUsage;
    }
    @Override
    public String getAccountName() {
        return getAccount().getName().getDisplayName();
    }
    @Override
    public Icon getProfilePictureIcon() {
        return DropboxUtilities.getProfilePicture(account, getAccountName());
    }
    @Override
    public Long getSpaceUsed() {
        return getSpaceUsage().getUsed();
    }
    @Override
    public Long getAllocatedSpace() {
        return DropboxUtilities.getAllocatedSpace(getSpaceUsage());
    }
    @Override
    public SyncMode getSyncMode() {
        return SyncMode.DROPBOX;
    }
}
