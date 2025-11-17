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
public interface MutableAccountData extends AccountData{
    /**
     * 
     * @param name 
     */
    public void setAccountName(String name);
    /**
     * 
     * @param icon 
     */
    public void setProfilePictureIcon(Icon icon);
    /**
     * 
     * @param value 
     */
    public void setSpaceUsed(Long value);
    /**
     * 
     * @param value 
     */
    public void setAllocatedSpace(Long value);
}
