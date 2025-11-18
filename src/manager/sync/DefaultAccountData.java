/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.sync;

import java.util.Objects;
import javax.swing.Icon;

/**
 *
 * @author Mosblinker
 */
public class DefaultAccountData extends AbstractAccountData implements MutableAccountData{
    /**
     * 
     */
    public static final String SYNC_MODE_PROPERTY_CHANGED = 
            "SyncModePropertyChanged";
    /**
     * The account's user name.
     */
    private String accountName;
    /**
     * The account's profile picture.
     */
    private Icon pfpIcon;
    /**
     * The amount of space that the user has used in their account.
     */
    private Long spaceUsed;
    /**
     * The amount of space allocated to the user's account.
     */
    private Long allocatedSpace;
    /**
     * 
     */
    private DatabaseSyncMode syncMode;
    /**
     * 
     * @param syncMode
     * @param accountName
     * @param pfpIcon
     * @param spaceUsed
     * @param allocatedSpace 
     */
    public DefaultAccountData(DatabaseSyncMode syncMode, String accountName, 
            Icon pfpIcon, Long spaceUsed, Long allocatedSpace){
        this.syncMode = syncMode;
        this.accountName = accountName;
        this.pfpIcon = pfpIcon;
        this.spaceUsed = spaceUsed;
        this.allocatedSpace = allocatedSpace;
    }
    /**
     * 
     * @param syncMode 
     */
    public DefaultAccountData(DatabaseSyncMode syncMode){
        this(syncMode,null,null,null,null);
    }
    /**
     * 
     */
    public DefaultAccountData(){
        this((DatabaseSyncMode)null);
    }
    /**
     * 
     * @param data 
     */
    public DefaultAccountData(AccountData data){
        this(data.getSyncMode(),data.getAccountName(),data.getProfilePictureIcon(),
                data.getSpaceUsed(),data.getAllocatedSpace());
    }
    @Override
    public String getAccountName() {
        return accountName;
    }
    @Override
    public Icon getProfilePictureIcon() {
        return pfpIcon;
    }
    @Override
    public Long getSpaceUsed() {
        return spaceUsed;
    }
    @Override
    public Long getAllocatedSpace() {
        return allocatedSpace;
    }
    @Override
    public DatabaseSyncMode getSyncMode() {
        return syncMode;
    }
    @Override
    public void setAccountName(String name) {
        if (!Objects.equals(name, accountName)){
            String old = accountName;
            accountName = name;
            firePropertyChange(ACCOUNT_NAME_PROPERTY_CHANGED,old,name);
        }
    }
    @Override
    public void setProfilePictureIcon(Icon icon) {
        if (!Objects.equals(icon, pfpIcon)){
            Icon old = pfpIcon;
            pfpIcon = icon;
            firePropertyChange(PROFILE_PICTURE_ICON_PROPERTY_CHANGED,old,icon);
        }
    }
    @Override
    public void setSpaceUsed(Long value) {
        if (!Objects.equals(value, spaceUsed)){
            Long old = spaceUsed;
            spaceUsed = value;
            firePropertyChange(SPACE_USED_PROPERTY_CHANGED,old,value);
        }
    }
    @Override
    public void setAllocatedSpace(Long value) {
        if (!Objects.equals(value, allocatedSpace)){
            Long old = allocatedSpace;
            allocatedSpace = value;
            firePropertyChange(ALLOCATED_SPACE_PROPERTY_CHANGED,old,value);
        }
    }
    /**
     * 
     * @param mode 
     */
    public void setSyncMode(DatabaseSyncMode mode){
        if (!Objects.equals(syncMode, mode)){
            DatabaseSyncMode old = syncMode;
            syncMode = mode;
            firePropertyChange(SYNC_MODE_PROPERTY_CHANGED,old,mode);
        }
    }
}
