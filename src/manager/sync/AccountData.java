/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.sync;

import javax.swing.Icon;

/**
 *
 * @author Mosblinker
 */
public interface AccountData {
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
    public DatabaseSyncMode getSyncMode();
}
