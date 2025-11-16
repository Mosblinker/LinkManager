/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.config;

import java.util.Properties;
import manager.DatabaseSyncMode;
import manager.LinkManager;

/**
 *
 * @author Mosblinker
 */
public interface ExternalFileSettings {
    /**
     * 
     * @return 
     */
    public DatabaseSyncMode getSyncMode();
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public String getDatabaseFileName(String defaultValue);
    /**
     * 
     * @return 
     */
    public default String getDatabaseFileName(){
        return getDatabaseFileName(LinkManager.LINK_DATABASE_FILE);
    }
    /**
     * 
     * @param value 
     */
    public void setDatabaseFileName(String value);
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean isFileCompressionEnabled(boolean defaultValue);
    /**
     * 
     * @return 
     */
    public default boolean isFileCompressionEnabled(){
        return isFileCompressionEnabled(false);
    }
    /**
     * 
     * @param enabled 
     */
    public void setFileCompressionEnabled(boolean enabled);
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getFileCompressionLevel(int defaultValue);
    /**
     * 
     * @return 
     */
    public default int getFileCompressionLevel(){
        return getFileCompressionLevel(5);
    }
    /**
     * 
     * @param level 
     */
    public void setFileCompressionLevel(int level);
    /**
     * 
     * @param prop 
     */
    public void importProperties(Properties prop);
    /**
     * 
     * @param prop 
     */
    public void exportProperties(ConfigProperties prop);
}
