/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.awt.Component;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import org.sqlite.SQLiteConfig;

/**
 * This is a class that is used to store and manage the configuration for 
 * LinkManager.
 * @author Milo Steier
 */
public class LinkManagerConfiguration {
    /**
     * This is a properties map that stores the configuration for LinkManager.
     */
    private final Properties config;
    /**
     * This is a properties map that stores the default configuration for 
     * LinkManager, and which serves as the default properties map for {@code 
     * config}.
     */
    private final Properties defaultConfig;
    /**
     * This is a properties map that stores the private configuration data for 
     * LinkManager. This is used to store things like passwords and such.
     */
    private final Properties privateConfig;
    /**
     * This is a properties map that stores the default configuration for 
     * private configuration for LinkManager, and which serves as the default 
     * properties map for {@code privateConfig}.
     */
    private final Properties defaultPrivateConfig;
    /**
     * This is the SQLite configuration to use for the database.
     */
    private final SQLiteConfig sqlConfig;
    /**
     * This is a map used to map components to the prefixes for keys for 
     * settings that relate to that component.
     */
    private final Map<Component, String> compKeyMap;
//    /**
//     * This is the preference node containing the shared configuration for 
//     * LinkManager.
//     */
//    private Preferences prefConfig = null;
    
    public LinkManagerConfiguration(){
        defaultConfig = new Properties();
        config = new Properties(defaultConfig);
        defaultPrivateConfig = new Properties();
        privateConfig = new Properties(defaultPrivateConfig);
        sqlConfig = new SQLiteConfig();
        compKeyMap = new HashMap<>();
    }
    /**
     * This returns the properties map that stores the configuration for 
     * LinkManager.
     * @return The properties map.
     */
    public Properties getProperties(){
        return config;
    }
    /**
     * This returns the properties map that stores the default configuration for 
     * LinkManager. This serves as the defaults for {@link #getProperties}.
     * @return The default properties map.
     */
    public Properties getDefaultProperties(){
        return defaultConfig;
    }
    /**
     * This returns the properties map that stores the private configuration 
     * data for LinkManager. This is used to store things like passwords.
     * @return The properties map for private data.
     */
    public Properties getPrivateProperties(){
        return privateConfig;
    }
    /**
     * This returns the properties map that stores the default configuration for 
     * private configuration for LinkManager. This serves as the defaults for 
     * {@link #getPrivateProperties}, which is used to store things like 
     * passwords.
     * @return The default properties map for private data.
     */
    public Properties getDefaultPrivateProperties(){
        return defaultPrivateConfig;
    }
    /**
     * This returns the SQLite configuration for the database used by 
     * LinkManager.
     * @return The SQLite configuration.
     */
    public SQLiteConfig getSQLiteConfig(){
        return sqlConfig;
    }
    /**
     * This returns a map used to map components in LinkManager to the prefixes 
     * for properties that relate to that component. For example, the properties 
     * for which these are prefixes for may be for the size of a component or 
     * the selected file for a file chooser.
     * @return A map that maps components to the prefixes for their respective 
     * properties.
     */
    public Map<Component, String> getComponentPrefixMap(){
        return compKeyMap;
    }
}
