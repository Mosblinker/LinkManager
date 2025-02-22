/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.awt.Component;
import java.awt.Dimension;
import java.util.*;
import java.util.prefs.*;
import manager.config.*;
import org.sqlite.SQLiteConfig;

/**
 * This is a class that is used to store and manage the configuration for 
 * LinkManager. All properties not stored in SQLiteConfig are stored internally 
 * as Strings.
 * @author Milo Steier
 */
public class LinkManagerConfig {
    /**
     * This is the configuration key for the progress display setting.
     */
    public static final String PROGRESS_DISPLAY_KEY = "DisplayProgress";
    /**
     * This is the configuration key for the always on top setting.
     */
    public static final String ALWAYS_ON_TOP_KEY = "AlwaysOnTop";
    /**
     * This is the configuration key for the blank lines setting.
     */
    public static final String BLANK_LINES_KEY = "AddBlankLines";
    /**
     * This is the configuration key for the setting that enables link 
     * operations.
     */
    public static final String ENABLE_LINK_OPS_KEY = "EnableLinkOperations";
    /**
     * This is the configuration key for the setting that enables link 
     * operations for hidden lists.
     */
    public static final String ENABLE_HIDDEN_LINK_OPS_KEY = 
            "EnableHiddenLinkOperations";
    /**
     * This is the configuration key for the database file path.
     */
    public static final String DATABASE_FILE_PATH_KEY = "DatabaseFilePath";
    /**
     * This is the configuration key for how to handle changing where the 
     * database file is located.
     */
    public static final String DATABASE_FILE_CHANGE_OPERATION_KEY = 
            "DatabaseFileChangeOperation";
    /**
     * This is the configuration key for the autosave frequency setting.
     */
    public static final String AUTOSAVE_FREQUENCY_KEY = 
            "AutosaveFrequencyIndex";
    /**
     * This is the configuration key for the auto-hide wait duration setting.
     */
    public static final String AUTO_HIDE_WAIT_DURATION_KEY = 
            "AutoHideWaitDurationIndex";
    /**
     * This is the configuration key for the setting that determines if the 
     * search factors in capitalization.
     */
    public static final String SEARCH_MATCH_CASE_KEY = "MatchCase";
    /**
     * This is the configuration key for the setting that determines if the 
     * search factors in white spaces.
     */
    public static final String SEARCH_MATCH_SPACES_KEY = "MatchWhiteSpaces";
    /**
     * This is the configuration key for the setting that determines if the 
     * search wraps around when it reaches the end of the list.
     */
    public static final String SEARCH_WRAP_AROUND_KEY = "SearchWrapAround";
    /**
     * This is the configuration key for the text to search for.
     */
    public static final String SEARCH_TEXT_KEY = "SearchText";
    /**
     * This is the configuration key for the text in the link text field. This 
     * is only loaded when the program first starts, and does not get set when 
     * the user loads a configuration file.
     */
    public static final String ENTERED_LINK_TEXT_KEY = "EnteredLink";
    /**
     * This is the configuration key for whether the exception and error codes 
     * will be included in any error popups related to the database when not in 
     * debug mode. The exception and error code will be shown regardless of this 
     * setting when in debug mode.
     */
    public static final String SHOW_DETAILED_DATABASE_ERRORS = 
            "ShowDetailedDatabaseErrors";
    /**
     * This is the configuration key for whether lists set to hidden should be 
     * made visible or not.
     */
    public static final String HIDDEN_LISTS_ARE_SHOWN_KEY = 
            "HiddenListsAreShown";
    /**
     * This is the configuration key for whether outdated lists should be 
     * overwritten when saving the lists to the database. {@code 0} for no, 
     * {@code 1} for yes, and {@code 2} for ask before saving.
     * 
     * @todo Implement this feature.
     */
    public static final String REPLACE_OUTDATED_LISTS_KEY = 
            "ReplaceOutdatedLists";
    
    public static final String SYNC_DATABASE_KEY = "SyncDatabase";
    
    /**
     * This is the suffix for the configuration keys for the size of a 
     * component.
     */
    public static final String COMPONENT_SIZE_KEY_SUFFIX = "Size";
    /**
     * This is the suffix for the configuration keys for the position of a 
     * component.
     */
    public static final String COMPONENT_POSITION_KEY_SUFFIX = "Position";
    /**
     * This is the suffix for the configuration keys for the bounds of a 
     * component.
     */
    public static final String COMPONENT_BOUNDS_KEY_SUFFIX = "Bounds";
    
    /**
     * This is the old suffix for configuration keys for the width component of 
     * a dimension.
     */
    protected static final String WIDTH_KEY_SUFFIX = "Width";
    /**
     * This is the old suffix for configuration keys for the height component of 
     * a dimension.
     */
    protected static final String HEIGHT_KEY_SUFFIX = "Height";
    
    /**
     * This is the start of the path for the preference node used to store the 
     * configuration data for an instance of the program.
     */
    private static final String LOCAL_PREFERENCE_NODE_PATH = "local";
    /**
     * This is the start of the path for the preference node used to store 
     * sensitive data for the program.
     */
    private static final String PRIVATE_PREFERENCE_NODE_PATH = "private";
    /**
     * This is the preference node containing all the preferences for 
     * LinkManager. This is the parent preference node for all other nodes, and 
     * any settings stored in this node are shared between all instances of 
     * LinkManager.
     */
    private ConfigPreferences programNode;
    /**
     * This is the preference node containing the configuration for this 
     * instance of LinkManager and any that share this instance's ID.
     */
    private ConfigPreferences localNode = null;
    /**
     * This is a properties map that stores the defaults for {@code localNode}.
     */
    private final ConfigProperties localDefaults;
    /**
     * This is the preference node containing the sensitive data for this 
     * instance of LinkManager and any that share this instance's ID.
     */
    private ConfigPreferences privateNode = null;
    /**
     * This is a properties map that stores the defaults for {@code privateNode}.
     */
    private final ConfigProperties privateDefaults;
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
     * This is a map used to map components to the names of the component in 
     * the settings.
     */
    private final Map<Component, String> compNameMap;
    /**
     * This is the ID for the program.
     */
    private UUID programID = null;
    /**
     * 
     * @param sqlProp
     * @param node 
     */
    private LinkManagerConfig(Properties sqlProp, ConfigPreferences node){
        defaultConfig = new Properties();
        config = new Properties(defaultConfig);
        defaultPrivateConfig = new Properties();
        privateConfig = new Properties(defaultPrivateConfig);
        compNameMap = new HashMap<>();
            // If the given SQLite config properties is not null
        if(sqlProp != null)
            sqlConfig = new SQLiteConfig(sqlProp);
        else
            sqlConfig = new SQLiteConfig();
        programNode = node;
        localDefaults = new ConfigProperties();
        privateDefaults = new ConfigProperties();
    }
    /**
     * 
     * @param node 
     */
    public LinkManagerConfig(Preferences node){
        this(null, new ConfigPreferences(node, new ConfigProperties()));
    }
    /**
     * 
     * @param linkConfig 
     */
    public LinkManagerConfig(LinkManagerConfig linkConfig){
        this(linkConfig.sqlConfig.toProperties(), linkConfig.programNode);
        this.defaultConfig.putAll(linkConfig.defaultConfig);
        this.defaultPrivateConfig.putAll(linkConfig.defaultPrivateConfig);
        this.config.putAll(linkConfig.config);
        this.privateConfig.putAll(linkConfig.privateConfig);
        this.compNameMap.putAll(linkConfig.compNameMap);
        this.localDefaults.addProperties(linkConfig.localDefaults);
        this.privateDefaults.addProperties(linkConfig.privateDefaults);
        LinkManagerConfig.this.setProgramID(linkConfig.programID);
    }
    /**
     * This returns the preference node used to store the shared configuration 
     * data for all instances of LinkManager.
     * @return The shared configuration preference node.
     * @see #getSharedDefaults() 
     */
    public ConfigPreferences getSharedPreferences(){
        return programNode;
    }
    /**
     * This returns the properties map that stores the default values for the 
     * {@link #getSharedPreferences() shared preference node}.
     * @return The properties map with the defaults for the shared preference 
     * node.
     * @see #getSharedPreferences() 
     */
    public ConfigProperties getSharedDefaults(){
        return (ConfigProperties) getSharedPreferences().getDefaults();
    }
    /**
     * This returns the preference node used to store the configuration for 
     * LinkManager.
     * @return The local preference node.
     * @see #getDefaults() 
     */
    public ConfigPreferences getPreferences(){
        return localNode;
    }
    /**
     * This returns the properties map that stores the default values for the 
     * {@link #getPreferences preference node}.
     * @return The properties map with the defaults for the local preference 
     * node.
     * @see #getPreferences() 
     */
    public ConfigProperties getDefaults(){
        return localDefaults;
    }
    /**
     * This returns the preference node used to store the sensitive data for 
     * LinkManager.
     * @return The local preference node for private data.
     * @see #getPrivateDefaults() 
     */
    public ConfigPreferences getPrivatePreferences(){
        return privateNode;
    }
    /**
     * This returns the properties map that stores the default values for the 
     * {@link #getPrivatePreferences() sensitive data preference node}.
     * @return The properties map with the defaults for the preference node for 
     * private data.
     * @see #getPrivatePreferences() 
     */
    public ConfigProperties getPrivateDefaults(){
        return privateDefaults;
    }
    /**
     * This gets a preference node relative to the program preference node with 
     * the given defaults. This is equivalent to the following: 
     * 
     * <pre> {@code
     * Preferences node = getSharedPreferences().node(pathName);
     * return new ConfigPreferences(node, defaults);
     * }</pre>
     * 
     * @param pathName The path name for the preference node to return.
     * @param defaults The defaults for the preferences.
     * @return A ConfigPreferences node from the shared preference node.
     * @throws IllegalArgumentException If the path name is invalid (i.e. it 
     * contains multiple consecutive slash characters or it ends with a slash 
     * character and is more than one character long).
     * @throws NullPointerException If the path name is null.
     * @throws IllegalStateException If the program node (or an ancestor) has 
     * been removed with the {@link ConfigPreferences#removeNode() removeNode()} 
     * method.
     * @see #getSharedPreferences() 
     * @see ConfigPreferences#node(String) 
     * @see ConfigPreferences
     * @see #getProgramIDNode(String, Properties) 
     */
    protected ConfigPreferences getNode(String pathName, Properties defaults){
        return new ConfigPreferences(programNode.node(pathName),defaults);
    }
    /**
     * This returns a preference node that is relative to the program preference 
     * node, and using the {@link #getProgramID() program ID} as the name of the 
     * node. This is roughly equivalent to calling {@link #getNode(String, 
     * Properties) getNode}{@code (path+"/"+getProgramID().toString(), 
     * defaults)}.
     * @param path The path for the node. The path will end with the program ID.
     * @param defaults The defaults for the preferences.
     * @return A ConfigPreferences node from the shared preference node and with 
     * the program ID as the name.
     * @throws IllegalArgumentException If the path name is invalid (i.e. it 
     * contains multiple consecutive slash characters).
     * @throws IllegalStateException If the program node (or an ancestor) has 
     * been removed with the {@link ConfigPreferences#removeNode() removeNode()} 
     * method.
     * @see #getNode(String, Properties) 
     * @see #getSharedPreferences() 
     * @see #getProgramID() 
     */
    protected ConfigPreferences getProgramIDNode(String path, Properties defaults){
            // If the path is null
        if (path == null)
            path = "";
            // If the path is not empty and does not end with a slash
        if (!path.isEmpty() && path.charAt(path.length()-1) != '/')
            path += "/";
        return getNode(path+programID.toString(), defaults);
    }
    /**
     * This returns the program ID set for this configuration.
     * @return The program ID.
     */
    public UUID getProgramID(){
        return programID;
    }
    /**
     * This sets the program ID for this configuration. This will also set the 
     * {@link #getPreferences() local} and {@link #getPrivatePreferences() 
     * private preference nodes}.
     * @param id The new program ID.
     * @throws NullPointerException If the program ID is null.
     * @see #getProgramID() 
     * @see #setRandomProgramID() 
     * @see #getPreferences()
     * @see #getPrivatePreferences()
     */
    public void setProgramID(UUID id){
            // Check if the program ID is null
        Objects.requireNonNull(id, "Program ID cannot be null");
            // If the program ID would not change
        if (id.equals(programID))
            return;
        programID = id;
            // Set the local preference node
        localNode = getProgramIDNode(LOCAL_PREFERENCE_NODE_PATH,localDefaults);
            // Set the private preference node
        privateNode = getProgramIDNode(PRIVATE_PREFERENCE_NODE_PATH,privateDefaults);
    }
    /**
     * This sets the program ID to be a random {@code UUID}.
     * @return The {@code UUID} used as the program ID.
     * @see UUID#randomUUID() 
     * @see #getProgramID() 
     * @see #setProgramID(UUID) 
     * @see #getPreferences()
     * @see #getPrivatePreferences()
     */
    public UUID setRandomProgramID(){
            // Generate a random UUID
        UUID id = UUID.randomUUID();
            // Set the program ID to the generated UUID
        setProgramID(id);
        return id;
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
     * This returns a map used to map components in LinkManager to the names of 
     * the component in the settings that relate to that component.
     * @return A map that maps components to the names for those components.
     */
    public Map<Component, String> getComponentNames(){
        return compNameMap;
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
            // If the value is null
        if (value == null)
                // Remove it from the configuration and return its value
            return removeConfigProperty(key,config);
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
            if (config.containsKey(key) || !valueStr.equals(defValue)){
                    // Set the value in the config and get its old value
                Object oldValue = config.setProperty(key, valueStr);
                    // If the old value is null, return null. Otherwise, return 
                    // the value as a string
                return (oldValue == null) ? null : oldValue.toString();
            } else  // Return the default value
                return defValue;
        }
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
    /**
     * 
     * @param map
     * @param config
     * @param defaultConfig 
     */
    protected synchronized void addConfigProperties(Map<?, ?> map, 
            Properties config, Properties defaultConfig){
            // Go through the given map's entries
        for (Map.Entry<?, ?> entry : map.entrySet()){
                // Set the property
            setConfigProperty(Objects.requireNonNull(entry.getKey()).toString(),
                    entry.getValue(),config,defaultConfig);
        }
    }
    /**
     * 
     * @param map 
     */
    public synchronized void addProperties(Map<?, ?> map){
        addConfigProperties(map, getProperties(), getDefaultProperties());
    }
    /**
     * 
     * @param map 
     */
    public synchronized void addDefaultProperties(Map<?, ?> map){
        addConfigProperties(map, getDefaultProperties(), null);
    }
    /**
     * 
     * @param map 
     */
    public synchronized void addPrivateProperties(Map<?, ?> map){
        addConfigProperties(map, getPrivateProperties(), 
                getDefaultPrivateProperties());
    }
    /**
     * 
     * @param map 
     */
    public synchronized void addDefaultPrivateProperties(Map<?, ?> map){
        addConfigProperties(map, getDefaultPrivateProperties(), null);
    }
    /**
     * 
     * @param key
     * @param config
     * @return 
     */
    protected synchronized String removeConfigProperty(String key, Properties config){
            // Remove the value from the config
        Object value = config.remove(key);
            // If the value is not null, return it as a String. Otherwise, 
        return (value != null) ? value.toString() : null;   // return null
    }
    /**
     * 
     * @param key
     * @return 
     */
    public synchronized String removeProperty(String key){
        return removeConfigProperty(key, getProperties());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public synchronized String removeDefaultProperty(String key){
        return removeConfigProperty(key, getDefaultProperties());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public synchronized String removePrivateProperty(String key){
        return removeConfigProperty(key, getPrivateProperties());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public synchronized String removeDefaultPrivate(String key){
        return removeConfigProperty(key, getDefaultPrivateProperties());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public boolean getBooleanProperty(String key){
        return Boolean.parseBoolean(getProperty(key));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public boolean getBooleanProperty(String key, boolean defaultValue){
        String value = getProperty(key);
        if (value == null)
            return defaultValue;
        return Boolean.parseBoolean(value);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public boolean getPrivateBooleanProperty(String key){
        return Boolean.parseBoolean(getPrivateProperty(key));
    }
    /**
     * 
     * @param value
     * @return 
     */
    private Integer getConfigIntProperty(String value){
            // If the value is null
        if (value == null)
            return null;
        try{    // Parse the value of the property
            return Integer.valueOf(value);
        } catch (NumberFormatException ex){
            return null;
        }
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Integer getIntProperty(String key){
        return getConfigIntProperty(getProperty(key));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public int getIntProperty(String key, int defaultValue){
            // Get the value for the property
        Integer value = getIntProperty(key);
            // If it's not null, return it. Otherwise, return the given integer
        return (value != null) ? value : defaultValue;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Integer getPrivateIntProperty(String key){
        return getConfigIntProperty(getPrivateProperty(key));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public int getPrivateIntProperty(String key, int defaultValue){
            // Get the value for the property
        Integer value = getPrivateIntProperty(key);
            // If it's not null, return it. Otherwise, return the given integer
        return (value != null) ? value : defaultValue;
    }
    /**
     * 
     * @param value
     * @return 
     */
    private Long getConfigLongProperty(String value){
            // If the value is null
        if (value == null)
            return null;
        try{    // Parse the value of the property
            return Long.valueOf(value);
        } catch (NumberFormatException ex){
            return null;
        }
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Long getLongProperty(String key){
        return getConfigLongProperty(getProperty(key));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public long getLongProperty(String key, long defaultValue){
            // Get the value for the property
        Long value = getLongProperty(key);
            // If it's not null, return it. Otherwise, return the given long 
        return (value != null) ? value : defaultValue;  // value 
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Long getPrivateLongProperty(String key){
        return getConfigLongProperty(getPrivateProperty(key));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public long getPrivateLongProperty(String key, long defaultValue){
            // Get the value for the property
        Long value = getPrivateLongProperty(key);
            // If it's not null, return it. Otherwise, return the given long 
        return (value != null) ? value : defaultValue;  // value
    }
    /**
     * 
     * @param value
     * @return 
     */
    private Double getConfigDoubleProperty(String value){
            // If the value is null
        if (value == null)
            return null;
        try{    // Parse the value of the property
            return Double.valueOf(value);
        } catch (NumberFormatException ex){
            return null;
        }
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Double getDoubleProperty(String key){
        return getConfigDoubleProperty(getProperty(key));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public double getDoubleProperty(String key, double defaultValue){
            // Get the value for the property
        Double value = getDoubleProperty(key);
            // If it's not null, return it. Otherwise, return the given double  
        return (value != null) ? value : defaultValue;  // value 
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Double getPrivateDoubleProperty(String key){
        return getConfigDoubleProperty(getPrivateProperty(key));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public double getPrivateDoubleProperty(String key, double defaultValue){
            // Get the value for the property
        Double value = getPrivateDoubleProperty(key);
            // If it's not null, return it. Otherwise, return the given double 
        return (value != null) ? value : defaultValue;  // value
    }
    /**
     * 
     * @param key
     * @param config
     * @return 
     */
    protected Dimension getConfigSizeProperty(String key, Properties config){
            // Get the width, as a String
        String widthStr = config.getProperty(key+WIDTH_KEY_SUFFIX);
            // Get the height as a String
        String heightStr = config.getProperty(key+HEIGHT_KEY_SUFFIX);
            // If either the width or height are null
        if (widthStr == null || heightStr == null)
            return null;
        try{    // Parse the width and height and return them as a Dimension.
            return new Dimension(Integer.parseInt(widthStr),
                    Integer.parseInt(heightStr));
        } catch (NumberFormatException ex){
            return null;
        }
    }
    /**
     * 
     * @param key
     * @param dim
     * @param config 
     * @param defaultConfig 
     */
    protected void setConfigSizeProperty(String key, Dimension dim, 
            Properties config, Properties defaultConfig){
            // Set the width value if it is not null. Otherwise, use null
        setConfigProperty(key+WIDTH_KEY_SUFFIX,(dim!=null)?dim.width:null
                ,config,defaultConfig);
            // Set the height value if it is not null. Otherwise, use null
        setConfigProperty(key+HEIGHT_KEY_SUFFIX,(dim!=null)?dim.height:null
                ,config,defaultConfig);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Dimension getSizeProperty(String key){
        return getConfigSizeProperty(key, getProperties());
    }
    /**
     * 
     * @param key
     * @param dim
     * @return 
     */
    public Dimension getSizeProperty(String key, Dimension dim){
            // Get the dimension from the property
        Dimension temp = getSizeProperty(key);
            // If the property is not null, return it. Otherwise, return the 
        return (temp != null) ? temp : dim;     // given dimension
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension getSizeProperty(Component comp){
        return getSizeProperty(getComponentNames().get(comp));
    }
    /**
     * 
     * @param comp
     * @param dim
     * @return 
     */
    public Dimension getSizeProperty(Component comp, Dimension dim){
        return getSizeProperty(getComponentNames().get(comp),dim);
    }
    /**
     * 
     * @param key
     * @param dim 
     */
    public void setSizeProperty(String key, Dimension dim){
        setConfigSizeProperty(key,dim,getProperties(),getDefaultProperties());
    }
    /**
     * 
     * @param comp
     * @param dim 
     */
    public void setSizeProperty(Component comp, Dimension dim){
        setSizeProperty(getComponentNames().get(comp),dim);
    }
    /**
     * 
     * @param comp 
     */
    public void setSizeProperty(Component comp){
        setSizeProperty(comp,comp.getSize());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Dimension getDefaultSizeProperty(String key){
        return getConfigSizeProperty(key, getDefaultProperties());
    }
    /**
     * 
     * @param key
     * @param dim
     * @return 
     */
    public Dimension getDefaultSizeProperty(String key, Dimension dim){
            // Get the dimension from the property
        Dimension temp = getDefaultSizeProperty(key);
            // If the property is not null, return it. Otherwise, return the 
        return (temp != null) ? temp : dim;     // given dimension
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension getDefaultSizeProperty(Component comp){
        return getDefaultSizeProperty(getComponentNames().get(comp));
    }
    /**
     * 
     * @param comp
     * @param dim
     * @return 
     */
    public Dimension getDefaultSizeProperty(Component comp, Dimension dim){
        return getDefaultSizeProperty(getComponentNames().get(comp),dim);
    }
    /**
     * 
     * @param key
     * @param dim 
     */
    public void setDefaultSizeProperty(String key, Dimension dim){
        setConfigSizeProperty(key,dim,getDefaultProperties(),null);
    }
    /**
     * 
     * @param comp
     * @param dim 
     */
    public void setDefaultSizeProperty(Component comp, Dimension dim){
        setDefaultSizeProperty(getComponentNames().get(comp),dim);
    }
    /**
     * 
     * @param comp 
     */
    public void setDefaultSizeProperty(Component comp){
        setDefaultSizeProperty(comp,comp.getPreferredSize());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Dimension getPrivateSizeProperty(String key){
        return getConfigSizeProperty(key, getPrivateProperties());
    }
    /**
     * 
     * @param key
     * @param dim
     * @return 
     */
    public Dimension getPrivateSizeProperty(String key, Dimension dim){
            // Get the dimension from the property
        Dimension temp = getPrivateSizeProperty(key);
            // If the property is not null, return it. Otherwise, return the 
        return (temp != null) ? temp : dim;     // given dimension
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension getPrivateSizeProperty(Component comp){
        return getPrivateSizeProperty(getComponentNames().get(comp));
    }
    /**
     * 
     * @param comp
     * @param dim
     * @return 
     */
    public Dimension getPrivateSizeProperty(Component comp, Dimension dim){
        return getPrivateSizeProperty(getComponentNames().get(comp),dim);
    }
    /**
     * 
     * @param key
     * @param dim 
     */
    public void setPrivateSizeProperty(String key, Dimension dim){
        setConfigSizeProperty(key,dim,getPrivateProperties(),getDefaultPrivateProperties());
    }
    /**
     * 
     * @param comp
     * @param dim 
     */
    public void setPrivateSizeProperty(Component comp, Dimension dim){
        setPrivateSizeProperty(getComponentNames().get(comp),dim);
    }
    /**
     * 
     * @param comp 
     */
    public void setPrivateSizeProperty(Component comp){
        setPrivateSizeProperty(comp,comp.getSize());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Dimension getDefaultPrivateSizeProperty(String key){
        return getConfigSizeProperty(key, getDefaultPrivateProperties());
    }
    /**
     * 
     * @param key
     * @param dim
     * @return 
     */
    public Dimension getDefaultPrivateSizeProperty(String key, Dimension dim){
            // Get the dimension from the property
        Dimension temp = getDefaultPrivateSizeProperty(key);
            // If the property is not null, return it. Otherwise, return the 
        return (temp != null) ? temp : dim;     // given dimension
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension getDefaultPrivateSizeProperty(Component comp){
        return getDefaultPrivateSizeProperty(getComponentNames().get(comp));
    }
    /**
     * 
     * @param comp
     * @param dim
     * @return 
     */
    public Dimension getDefaultPrivateSizeProperty(Component comp, Dimension dim){
        return getDefaultPrivateSizeProperty(getComponentNames().get(comp),dim);
    }
    /**
     * 
     * @param key
     * @param dim 
     */
    public void setDefaultPrivateSizeProperty(String key, Dimension dim){
        setConfigSizeProperty(key,dim,getDefaultPrivateProperties(),null);
    }
    /**
     * 
     * @param comp
     * @param dim 
     */
    public void setDefaultPrivateSizeProperty(Component comp, Dimension dim){
        setDefaultPrivateSizeProperty(getComponentNames().get(comp),dim);
    }
    /**
     * 
     * @param comp 
     */
    public void setDefaultPrivateSizeProperty(Component comp){
        setDefaultPrivateSizeProperty(comp,comp.getPreferredSize());
    }
    /**
     * 
     * @param value
     * @return 
     */
    protected String formatFilePath(String value){
            // If the value is not null and not blank
        if (value != null && !value.isBlank())
            return value.trim();
        return null;
    }
    /**
     * 
     * @param key
     * @param config
     * @param defaultConfig
     * @return 
     */
    protected String getConfigFilePathProperty(String key, Properties config, 
            Properties defaultConfig){
            // Get the value of the property from the config map and format it
        String value = formatFilePath(config.getProperty(key));
            // If the value is not null
        if (value != null)
            return value;
            // If there was default config map provided
        if (defaultConfig != null){
                // Get the value from the default config map and format it
            value = formatFilePath(defaultConfig.getProperty(key));
                // If the value is not null
            if (value != null)
                return value;
        }
        return null;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public String getFilePathProperty(String key){
        return getConfigFilePathProperty(key,getProperties(),getDefaultProperties());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public String getPrivateFilePathProperty(String key){
        return getConfigFilePathProperty(key,getPrivateProperties(),
                getDefaultPrivateProperties());
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public String setFilePathProperty(String key, String value){
        return setProperty(key,formatFilePath(value));
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public String setPrivateFilePathProperty(String key, String value){
        return setPrivateProperty(key,formatFilePath(value));
    }
    
    
    
    /**
     * 
     * @param key
     * @param value
     * @param node 
     */
    protected void setFilePathPreference(String key, String value, 
            ConfigPreferences node){
            // Format the file path and set it
        node.put(key,formatFilePath(value));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @param node
     * @return 
     */
    protected String getFilePathPreference(String key, String defaultValue, 
            ConfigPreferences node){
            // Get the value of the property from the node and format it
        String value = formatFilePath(node.get(key, null));
            // If the value is not null
        if (value != null)
            return value;
            // If there is a default property list for the node
        if (node.getDefaults() != null){
                // Get the value from the default property list and format it
            value = formatFilePath(node.getDefaults().getProperty(key));
                // If the value is not null
            if (value != null)
                return value;
        }
        return formatFilePath(defaultValue);
    }
    /**
     * 
     * @param key
     * @param value
     * @param prop 
     */
    protected void setProperty(String key, Object value, Properties prop){
            // If the value is null
        if (value == null)
                // Remove the key from the properties
            prop.remove(key);
        else    // Set the value as a String
            prop.setProperty(key, value.toString());
    }
    /**
     * 
     * @param key
     * @param value
     * @param prop 
     */
    protected void setFilePathProperty(String key,String value,
            ConfigProperties prop){
            // Format the file path and set it
        prop.setProperty(key,formatFilePath(value));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @param prop
     * @param defaults
     * @return 
     */
    protected String getFilePathProperty(String key, String defaultValue, 
            Properties prop, Properties defaults){
            // Get the value of the property from the properties and format it
        String value = formatFilePath(prop.getProperty(key));
            // If the value is not null
        if (value != null)
            return value;
            // If there is a default property list for the node
        if (defaults != null){
                // Get the value from the default property list and format it
            value = formatFilePath(defaults.getProperty(key));
                // If the value is not null
            if (value != null)
                return value;
        }
        return formatFilePath(defaultValue);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @param prop
     * @return 
     */
    protected String getFilePathProperty(String key, String defaultValue, 
            Properties prop){
        return getFilePathProperty(key,defaultValue,prop,null);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    protected String getComponentName(Component comp){
        return getComponentNames().getOrDefault(comp, comp.getName());
    }
    /**
     * 
     * @param prop 
     */
    public void importProperties(Properties prop){
            // Make sure the Properties object is not null
        Objects.requireNonNull(prop);
            // This will get a ConfigProperties version of the given Properties 
        ConfigProperties cProp;     // object
            // If the given Properties object is already a ConfigProperties
        if (prop instanceof ConfigProperties)
            cProp = (ConfigProperties) prop;
        else    // Create a new ConfigProperties with the given Properties 
                // object as its defaults. This should be okay since we won't be 
                // writing to it, only reading from it.
            cProp = new ConfigProperties(prop);
        
            // TODO: Remove this once the config properties map is removed or 
            // repurposed.
            // Add all the properties to the config properties map
        config.putAll(prop);
        
            // Get the value for the database file path from the properties
        String str = cProp.getProperty(DATABASE_FILE_PATH_KEY);
            // If the properties has the database file path
        if (str != null)
                // Set the database file path from the properties
            setDatabaseFileName(str);
            // Get the value for the progress display from the properties
        Integer i = cProp.getIntProperty(PROGRESS_DISPLAY_KEY);
            // If the properties has the progress display settings
        if (i != null)
                // Set the progress display settings from the properties
            setProgressDisplaySetting(i);
            // Get the value for the always on top setting from the properties
        Boolean b = cProp.getBooleanProperty(ALWAYS_ON_TOP_KEY);
            // If the properties has the always on top setting
        if (b != null)
                // Set the always on top settings from the properties
            setAlwaysOnTop(b);
            // Get the value for the add blank lines setting from the properties
        b = cProp.getBooleanProperty(BLANK_LINES_KEY);
            // If the properties has the add blank lines setting
        if (b != null)
                // Set the add blank lines settings from the properties
            setAddBlankLines(b);
            // Get the value for the link operations enabled from the properties
        b = cProp.getBooleanProperty(ENABLE_LINK_OPS_KEY);
            // If the properties has the link operations enabled value
        if (b != null)
                // Set the link operations enabled from the properties
            setLinkOperationsEnabled(b);
            // Get the value for the hidden link operations enabled from the 
            // properties
        b = cProp.getBooleanProperty(ENABLE_HIDDEN_LINK_OPS_KEY);
            // If the properties has the hidden link operations enabled value
        if (b != null)
                // Set the hidden link operations enabled from the properties
            setHiddenLinkOperationsEnabled(b);
            // Get the value for the database file change operation from the 
            // properties
        i = cProp.getIntProperty(DATABASE_FILE_CHANGE_OPERATION_KEY);
            // If the properties has the database file change operation
        if (i != null)
                // Set the database file change operation from the properties
            setDatabaseFileChangeOperation(i);
            // Get the value for the autosave frequency index from the 
            // properties
        i = cProp.getIntProperty(AUTOSAVE_FREQUENCY_KEY);
            // If the properties has the autosave frequency index
        if (i != null)
                // Set the autosave frequency index from the properties
            setAutosaveFrequencyIndex(i);
            // Get the value for the auto-hide wait duration index from the 
            // properties
        i = cProp.getIntProperty(AUTO_HIDE_WAIT_DURATION_KEY);
            // If the properties has the auto-hide wait duration index
        if (i != null)
                // Set the auto-hide wait duration index from the properties
            setAutoHideWaitDurationIndex(i);
            // Get the value for the search match case setting from the 
            // properties
        b = cProp.getBooleanProperty(SEARCH_MATCH_CASE_KEY);
            // If the properties has the search match case setting
        if (b != null)
                // Set the search match case setting from the properties
            setSearchMatchCase(b);
            // Get the value for the search match spaces setting from the 
            // properties
        b = cProp.getBooleanProperty(SEARCH_MATCH_SPACES_KEY);
            // If the properties has the search match spaces setting
        if (b != null)
                // Set the search match spaces setting from the properties
            setSearchMatchSpaces(b);
            // Get the value for the search wrap around setting from the 
            // properties
        b = cProp.getBooleanProperty(SEARCH_WRAP_AROUND_KEY);
            // If the properties has the search wrap around setting
        if (b != null)
                // Set the search wrap around setting from the properties
            setSearchWrapAround(b);
            // Get the value for the search text from the properties
        str = cProp.getProperty(SEARCH_TEXT_KEY);
            // If the properties has the search text
        if (str != null)
                // Set the search text from the properties
            setSearchText(str);
            // Get the value for the entered link text from the properties
        str = cProp.getProperty(ENTERED_LINK_TEXT_KEY);
            // If the properties has the entered link text
        if (str != null)
                // Set the entrered link text from the properties
            setEnteredLinkText(str);
            // Get the value for the hidden lists are shown setting from the 
            // properties
        b = cProp.getBooleanProperty(HIDDEN_LISTS_ARE_SHOWN_KEY);
            // If the properties has the hidden lists are shown setting
        if (b != null)
                // Set the hidden lists are shown setting from the properties
            setHiddenListsAreShown(b);
            // Get the value for the database error details are shown setting 
            // from the properties
        b = cProp.getBooleanProperty(SHOW_DETAILED_DATABASE_ERRORS);
            // If the properties has the database error details are shown value
        if (b != null)
                // Set whether database error details are shown from the 
            setDatabaseErrorDetailsAreShown(b);     // properties
            // Get the value for the database sync setting from the properties
        b = cProp.getBooleanProperty(SYNC_DATABASE_KEY);
            // If the properties has the database sync setting
        if (b != null)
                // Set whether database will sync from the properties
            setDatabaseWillSync(b);
            // Go through the entries in the component name map
        for (Map.Entry<Component,String> entry:getComponentNames().entrySet()){
                // Get the dimension for the component from the properties
            Dimension dim = cProp.getDimensionProperty(
                    entry.getValue()+COMPONENT_SIZE_KEY_SUFFIX);
                // If the properties has a size for the component
            if (dim != null)
                    // Set the component's size from the properties
                setComponentSize(entry.getKey(),dim);
            
                // TODO: Remove this once the config properties map is removed or 
                // repurposed.
                // Remove the component size, since that's in the preference node
            config.remove(entry.getValue()+COMPONENT_SIZE_KEY_SUFFIX);
        }
        
            // TODO: Remove this once the config properties map is removed or 
            // repurposed.
            // Remove the database file path, since that's in the preference node
        config.remove(DATABASE_FILE_PATH_KEY);
            // Remove the progress display, since that's in the preference node
        config.remove(PROGRESS_DISPLAY_KEY);
            // Remove the always on top value, since that's in the preference node
        config.remove(ALWAYS_ON_TOP_KEY);
            // Remove the add blank lines value, since that's in the preference node
        config.remove(BLANK_LINES_KEY);
            // Remove the link ops enabled value, since that's in the preference node
        config.remove(ENABLE_LINK_OPS_KEY);
            // Remove the hidden link ops enabled value, since that's in the preference node
        config.remove(ENABLE_HIDDEN_LINK_OPS_KEY);
            // Remove the database file change operation value, since that's in the preference node
        config.remove(DATABASE_FILE_CHANGE_OPERATION_KEY);
            // Remove the autosave frequency index, since that's in the preference node
        config.remove(AUTOSAVE_FREQUENCY_KEY);
            // Remove the auto-hide wait duration index, since that's in the preference node
        config.remove(AUTO_HIDE_WAIT_DURATION_KEY);
            // Remove the search match case value, since that's in the preference node
        config.remove(SEARCH_MATCH_CASE_KEY);
            // Remove the search match spaces value, since that's in the preference node
        config.remove(SEARCH_MATCH_SPACES_KEY);
            // Remove the search wrap around value, since that's in the preference node
        config.remove(SEARCH_WRAP_AROUND_KEY);
            // Remove the search text, since that's in the preference node
        config.remove(SEARCH_TEXT_KEY);
            // Remove the entered link text, since that's in the preference node
        config.remove(ENTERED_LINK_TEXT_KEY);
            // Remove the hidden lists are shown value, since that's in the preference node
        config.remove(HIDDEN_LISTS_ARE_SHOWN_KEY);
            // Remove the database error details are shown value, since that's in the preference node
        config.remove(SHOW_DETAILED_DATABASE_ERRORS);
            // Remove the database sync value, since that's in the preference node
        config.remove(SYNC_DATABASE_KEY);
    }
    /**
     * 
     * @param fileName 
     */
    public void setDefaultDatabaseFileName(String fileName){
        setFilePathProperty(DATABASE_FILE_PATH_KEY,fileName,getDefaults());
    }
    /**
     * 
     * @return 
     */
    public String getDefaultDatabaseFileName(){
        return getFilePathProperty(DATABASE_FILE_PATH_KEY,null,getDefaults());
    }
    /**
     * 
     * @param fileName 
     */
    public void setDatabaseFileName(String fileName){
        setFilePathPreference(DATABASE_FILE_PATH_KEY,fileName,getPreferences());
    }
    /**
     * 
     * @return 
     */
    public String getDatabaseFileName(){
        return getFilePathPreference(DATABASE_FILE_PATH_KEY, 
                LinkManager.LINK_DATABASE_FILE, getPreferences());
    }
    /**
     * 
     * @param value 
     */
    public void setProgressDisplaySetting(Integer value){
        getPreferences().putObject(PROGRESS_DISPLAY_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getProgressDisplaySetting(int defaultValue){
        return getPreferences().getInt(PROGRESS_DISPLAY_KEY, defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setAlwaysOnTop(Boolean value){
        getPreferences().putObject(ALWAYS_ON_TOP_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean isAlwaysOnTop(boolean defaultValue){
        return getPreferences().getBoolean(ALWAYS_ON_TOP_KEY, defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setAddBlankLines(Boolean value){
        getPreferences().putObject(BLANK_LINES_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean getAddBlankLines(boolean defaultValue){
        return getPreferences().getBoolean(BLANK_LINES_KEY, defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setLinkOperationsEnabled(Boolean value){
        getPreferences().putObject(ENABLE_LINK_OPS_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean isLinkOperationsEnabled(boolean defaultValue){
        return getPreferences().getBoolean(ENABLE_LINK_OPS_KEY, defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setHiddenLinkOperationsEnabled(Boolean value){
        getPreferences().putObject(ENABLE_HIDDEN_LINK_OPS_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean isHiddenLinkOperationsEnabled(boolean defaultValue){
        return getPreferences().getBoolean(ENABLE_HIDDEN_LINK_OPS_KEY, 
                defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setDatabaseFileChangeOperation(Integer value){
        getPreferences().putObject(DATABASE_FILE_CHANGE_OPERATION_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getDatabaseFileChangeOperation(int defaultValue){
        return getPreferences().getInt(DATABASE_FILE_CHANGE_OPERATION_KEY, 
                defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setAutosaveFrequencyIndex(Integer value){
        getPreferences().putObject(AUTOSAVE_FREQUENCY_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getAutosaveFrequencyIndex(int defaultValue){
        return getPreferences().getInt(AUTOSAVE_FREQUENCY_KEY, defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setAutoHideWaitDurationIndex(Integer value){
        getPreferences().putObject(AUTO_HIDE_WAIT_DURATION_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getAutoHideWaitDurationIndex(int defaultValue){
        return getPreferences().getInt(AUTO_HIDE_WAIT_DURATION_KEY, defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setSearchMatchCase(Boolean value){
        getPreferences().putObject(SEARCH_MATCH_CASE_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean getSearchMatchCase(boolean defaultValue){
        return getPreferences().getBoolean(SEARCH_MATCH_CASE_KEY,defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setSearchMatchSpaces(Boolean value){
        getPreferences().putObject(SEARCH_MATCH_SPACES_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean getSearchMatchSpaces(boolean defaultValue){
        return getPreferences().getBoolean(SEARCH_MATCH_SPACES_KEY,defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setSearchWrapAround(Boolean value){
        getPreferences().putObject(SEARCH_WRAP_AROUND_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean getSearchWrapAround(boolean defaultValue){
        return getPreferences().getBoolean(SEARCH_WRAP_AROUND_KEY,defaultValue);
    }
    /**
     * 
     * @param text 
     */
    public void setSearchText(String text){
            // If the search text is null or empty, use null for it
        if (text == null || text.isEmpty())
            text = null;
            // Store the search text in the preference node
        getPreferences().put(SEARCH_TEXT_KEY, text);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public String getSearchText(String defaultValue){
        return getPreferences().get(SEARCH_TEXT_KEY,defaultValue);
    }
    /**
     * 
     * @return 
     */
    public String getSearchText(){
        return getSearchText(null);
    }
    /**
     * 
     * @param text 
     */
    public void setEnteredLinkText(String text){
            // If the entered link text is null or blank, use null for it
        if (text == null || text.isBlank())
            text = null;
        getPreferences().put(ENTERED_LINK_TEXT_KEY, text);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public String getEnteredLinkText(String defaultValue){
        return getPreferences().get(ENTERED_LINK_TEXT_KEY,defaultValue);
    }
    /**
     * 
     * @return 
     */
    public String getEnteredLinkText(){
        return getEnteredLinkText(null);
    }
    /**
     * 
     * @param value 
     */
    public void setHiddenListsAreShown(Boolean value){
        getPreferences().putObject(HIDDEN_LISTS_ARE_SHOWN_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean getHiddenListsAreShown(boolean defaultValue){
        return getPreferences().getBoolean(HIDDEN_LISTS_ARE_SHOWN_KEY,
                defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setDatabaseErrorDetailsAreShown(Boolean value){
        getPreferences().putObject(SHOW_DETAILED_DATABASE_ERRORS, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean getDatabaseErrorDetailsAreShown(boolean defaultValue){
        return getPreferences().getBoolean(SHOW_DETAILED_DATABASE_ERRORS,
                defaultValue);
    }
    /**
     * 
     * @param value 
     */
    public void setDatabaseWillSync(Boolean value){
        getPreferences().putObject(SYNC_DATABASE_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean getDatabaseWillSync(boolean defaultValue){
        return getPreferences().getBoolean(SYNC_DATABASE_KEY,defaultValue);
    }
    /**
     * 
     * @param comp
     * @param value 
     */
    public void setDefaultComponentSize(Component comp, Dimension value){
        getDefaults().setProperty(
                getComponentName(comp)+COMPONENT_SIZE_KEY_SUFFIX, value);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension getDefaultComponentSize(Component comp){
        return getDefaults().getDimensionProperty(
                getComponentName(comp)+COMPONENT_SIZE_KEY_SUFFIX);
    }
    /**
     * 
     * @param comp
     * @param value 
     */
    public void setComponentSize(Component comp, Dimension value){
        getPreferences().putDimension(
                getComponentName(comp)+COMPONENT_SIZE_KEY_SUFFIX, value);
    }
    /**
     * 
     * @param comp 
     */
    public void setComponentSize(Component comp){
        setComponentSize(comp,comp.getSize());
    }
    /**
     * 
     * @param comp
     * @param defaultValue
     * @return 
     */
    public Dimension getComponentSize(Component comp, Dimension defaultValue){
        return getPreferences().getDimension(
                getComponentName(comp)+COMPONENT_SIZE_KEY_SUFFIX, defaultValue);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension getComponentSize(Component comp){
        return getComponentSize(comp,null);
    }
}
