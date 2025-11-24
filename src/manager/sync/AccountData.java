/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.sync;

import java.beans.PropertyChangeListener;
import javax.swing.Icon;

/**
 *
 * @author Mosblinker
 */
public interface AccountData {
    /**
     * 
     */
    public static final String PROFILE_PICTURE_ICON_PROPERTY_CHANGED = 
            "ProfileIconPropertyChanged";
    /**
     * 
     */
    public static final String ACCOUNT_NAME_PROPERTY_CHANGED = 
            "AccountNamePropertyChanged";
    /**
     * 
     */
    public static final String SPACE_USED_PROPERTY_CHANGED = 
            "SpaceUsedPropertyChanged";
    /**
     * 
     */
    public static final String ALLOCATED_SPACE_PROPERTY_CHANGED = 
            "AllocatedSpacePropertyChanged";
    /**
     * 
     * @return 
     */
    public String getAccountName();
    /**
     * 
     * @return 
     */
    public Icon getProfilePictureIcon();
    /**
     * 
     * @return 
     */
    public Long getSpaceUsed();
    /**
     * 
     * @return 
     */
    public Long getAllocatedSpace();
    /**
     * 
     * @return 
     */
    public default Long getSpaceFree(){
        if (getAllocatedSpace() == null)
            return null;
        if (getSpaceUsed() == null)
            return getAllocatedSpace();
        return getAllocatedSpace() - getSpaceUsed();
    }
    /**
     * 
     * @return 
     */
    public SyncLocation getSyncMode();
    /**
     * 
     * @param l 
     */
    public void addPropertyChangeListener(PropertyChangeListener l);
    /**
     * 
     * @param propertyName
     * @param l 
     */
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l);
    /**
     * 
     * @param l 
     */
    public void removePropertyChangeListener(PropertyChangeListener l);
    /**
     * 
     * @param propertyName
     * @param l 
     */
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l);
    /**
     * 
     * @return 
     */
    public PropertyChangeListener[] getPropertyChangeListeners();
    /**
     * 
     * @param propertyName
     * @return 
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName);
}
