/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.util.Properties;

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
    
    public LinkManagerConfiguration(){
        defaultConfig = new Properties();
        config = new Properties(defaultConfig);
        defaultPrivateConfig = new Properties();
        privateConfig = new Properties(defaultPrivateConfig);
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
}
