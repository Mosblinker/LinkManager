/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.awt.Component;
import java.util.*;
import org.sqlite.SQLiteConfig;

/**
 * This is a class that is used to store and manage the configuration for 
 * LinkManager. All properties not stored in SQLiteConfig are stored internally 
 * as Strings.
 * @author Milo Steier
 */
public class LinkManagerConfig {
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
    
    private LinkManagerConfig(Properties sqlProp){
        defaultConfig = new Properties();
        config = new Properties(defaultConfig);
        defaultPrivateConfig = new Properties();
        privateConfig = new Properties(defaultPrivateConfig);
        compKeyMap = new HashMap<>();
            // If the given SQLite config properties is not null
        if(sqlProp != null)
            sqlConfig = new SQLiteConfig(sqlProp);
        else
            sqlConfig = new SQLiteConfig();
    }
    
    public LinkManagerConfig(){
        this((Properties)null);
    }
    
    public LinkManagerConfig(LinkManagerConfig linkConfig){
        this(linkConfig.sqlConfig.toProperties());
        this.defaultConfig.putAll(linkConfig.defaultConfig);
        this.defaultPrivateConfig.putAll(linkConfig.defaultPrivateConfig);
        this.config.putAll(linkConfig.config);
        this.privateConfig.putAll(linkConfig.privateConfig);
        this.compKeyMap.putAll(linkConfig.compKeyMap);
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
    /**
     * This sets the property in the given {@code config} Properties for the 
     * given key to the given value, and returning the old value. If the given 
     * value is null, then the property for the given key will be reset to its 
     * default.
     * @param key The key for the property to set (cannot be null).
     * @param value The new value for the property, or null to reset the 
     * property to its default value.
     * @param config The Properties object to set the property of (cannot be 
     * null).
     * @param defaultConfig The Properties object containing the defaults for 
     * {@code config}, or null.
     * @return The old value set for the property, or null if no value was set.
     * @throws NullPointerException If either {@code key} or {@code config} are 
     * null.
     */
    protected synchronized String setConfigProperty(String key, Object value,
            Properties config, Properties defaultConfig){
            // Check if the key is null
        Objects.requireNonNull(key, "The key for the property cannot be null");
            // This gets the old value for the property
        Object oldValue;
            // If the value is null
        if (value == null)
                // Remove it from the configuration and get its value
            oldValue = config.remove(key);
        else{   // Get the value as a String
            String valueStr = Objects.toString(value);
                // This gets the default value for the property to be set, or 
                // null if there is no default value (or no defaultConfig was 
            String defValue = null;     // provided)
                // If a default Properties map was provided
            if (defaultConfig != null)
                    // Get the default value for the property
                defValue = defaultConfig.getProperty(key);
                // If the config currently has a value set for the given key or 
                // if the given value does not match the default value (prevents 
                // needlessly setting the value to its default unless it was 
                // previously set to something else)
            if (config.containsKey(key) || !valueStr.equals(defValue))
                    // Set the value in the config and get its old value
                oldValue = config.setProperty(key, valueStr);
            else    // Return the default value
                return defValue;
        }   // If the old value is null, return null. Otherwise, return the 
            // value as a string
        return (oldValue == null) ? null : oldValue.toString();
    }
    /**
     * This sets the 
     * @param key
     * @param value
     * @return 
     */
    public synchronized String setProperty(String key, Object value){
        return setConfigProperty(key,value,getProperties(),getDefaultProperties());
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public synchronized String setPropertyDefault(String key, Object value){
        return setConfigProperty(key,value,getDefaultProperties(),null);
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public synchronized String setPrivateProperty(String key, Object value){
        return setConfigProperty(key,value,getPrivateProperties(),
                getDefaultPrivateProperties());
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public synchronized String setPrivateDefault(String key, Object value){
        return setConfigProperty(key,value,getDefaultPrivateProperties(),null);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public String getProperty(String key){
        return getProperties().getProperty(key);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public String getProperty(String key, String defaultValue){
        return getProperties().getProperty(key, defaultValue);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public String getPrivateProperty(String key){
        return getPrivateProperties().getProperty(key);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public String getPrivateProperty(String key, String defaultValue){
        return getPrivateProperties().getProperty(key, defaultValue);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public String getPropertyDefault(String key){
        return getDefaultProperties().getProperty(key);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public String getPrivateDefault(String key){
        return getDefaultPrivateProperties().getProperty(key);
    }
    
}
