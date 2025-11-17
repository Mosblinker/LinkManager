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
     * @throws java.lang.Exception 
     */
    public void setAccountName(String name) throws Exception;
    /**
     * 
     * @param icon 
     * @throws java.lang.Exception 
     */
    public void setProfilePictureIcon(Icon icon) throws Exception;
    /**
     * 
     * @param value 
     * @throws java.lang.Exception 
     */
    public void setSpaceUsed(Long value) throws Exception;
    /**
     * 
     * @param value 
     * @throws java.lang.Exception 
     */
    public void setAllocatedSpace(Long value) throws Exception;
}
