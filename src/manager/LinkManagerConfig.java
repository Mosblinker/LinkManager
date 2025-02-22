/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.*;
import java.util.prefs.*;
import manager.config.*;
import manager.dropbox.DropboxLinkUtils;
import manager.links.LinksListTabsPanel;
import manager.security.Obfuscator;
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
     * This is the configuration key for the encrypted access token for the 
     * Dropbox account to use to access the database file if the database file 
     * is stored in a Dropbox account. 
     */
    public static final String DROPBOX_ACCESS_TOKEN_KEY = "AccessToken";
    /**
     * This is the configuration key for the encrypted refresh token for the 
     * Dropbox account to use to access the database file if the database file 
     * is stored in a Dropbox account.
     */
    public static final String DROPBOX_REFRESH_TOKEN_KEY = "RefreshToken";
    /**
     * This is the configuration key for the expiration time for the Dropbox 
     * access token for the Dropbox account used to access the database file if 
     * the database file is stored in a Dropbox account.
     */
    public static final String DROPBOX_TOKEN_EXPIRATION_KEY = "TokenExpiresAt";
    /**
     * This is the configuration key for the listID of the currently selected 
     * list if a list with a listID is selected.
     */
    public static final String CURRENT_TAB_LIST_ID_KEY = "CurrentTabListID";
    /**
     * This is the configuration key for the index of the currently selected 
     * tab if a tab is selected. This value is used as a fallback value to be 
     * used when the value for {@link CURRENT_TAB_LIST_ID_KEY} is unavailable 
     * either due to the currently selected list not having a listID, no lists 
     * have the selected listID, or the current tab is not a list. The value for 
     * {@code CURRENT_TAB_LIST_ID_KEY} takes priority over this value due to 
     * the listIDs staying more or less constant for any given list saved to or 
     * loaded from the database, whereas the index for any given list may vary 
     * between instances of the program.
     */
    public static final String CURRENT_TAB_INDEX_KEY = "CurrentTabIndex";
    /**
     * This is the configuration key for the currently selected link in a list 
     * with a listID.
     */
    public static final String SELECTED_LINK_FOR_LIST_KEY = 
            "SelectedLink";
    /**
     * This is the configuration key for whether the currently selected link in 
     * a list is visible. (i.e. whether this should scroll to the selected link 
     * when the program is first loading) with a listID.
     */
    public static final String SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY = 
            "SelectedLinkIsVisible";
    
    public static final String FIRST_VISIBLE_INDEX_FOR_LIST_KEY = 
            "FirstVisibleIndex";
    
    public static final String LAST_VISIBLE_INDEX_FOR_LIST_KEY = 
            "LastVisibleIndex";
    /**
     * This is the suffix for the configuration keys for the size of a 
     * component.
     */
    public static final String COMPONENT_SIZE_KEY_SUFFIX = "Size";
    /**
     * This is the suffix for the configuration keys for the location of a 
     * component.
     */
    public static final String COMPONENT_LOCATION_KEY_SUFFIX = "Location";
    /**
     * This is the suffix for the configuration keys for the bounds of a 
     * component.
     */
    public static final String COMPONENT_BOUNDS_KEY_SUFFIX = "Bounds";
    /**
     * This is the suffix for the configuration keys for the window state of a 
     * frame.
     * @todo Implement the storing of the window state.
     */
    public static final String FRAME_WINDOW_STATE_KEY_SUFFIX = "WindowState";
    /**
     * This is the prefix for keys relating to Dropbox settings when importing 
     * or exporting the settings.
     */
    public static final String DROPBOX_PROPERTY_KEY_PREFIX = "Dropbox";
    /**
     * This is the suffix for the keys relating to list types when importing or 
     * exporting the settings. This is suppose to have a list type appended to 
     * the end. The list type is appended to the end of this to get the 
     * properties key specific for that list type.
     */
    public static final String LIST_TYPE_PROPERTY_KEY_SUFFIX = "ForType";
    /**
     * This is the suffix for the keys relating to lists when importing or 
     * exporting the settings. This is suppose to have a list ID appended to 
     * the end. The list's listID is appended to the end of this to get the 
     * properties key specific for that list.
     */
    public static final String LIST_ID_PROPERTY_KEY_SUFFIX = "ForList";
    /**
     * This is an array that contains the prefixes for the keys in properties 
     * that deal with list types.
     */
    private static final String[] LIST_TYPE_PROPERTY_KEY_PREFIXES = {
        CURRENT_TAB_LIST_ID_KEY,
        CURRENT_TAB_INDEX_KEY
    };
    /**
     * This is an array that contains the prefixes for the keys in properties 
     * that deal with list IDs.
     */
    private static final String[] LIST_ID_PROPERTY_KEY_PREFIXES = {
        SELECTED_LINK_FOR_LIST_KEY,
        SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY,
        FIRST_VISIBLE_INDEX_FOR_LIST_KEY,
        LAST_VISIBLE_INDEX_FOR_LIST_KEY
    };
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
     * This is the name of the preference nodes used to store the data for 
     * Dropbox.
     */
    public static final String DROPBOX_PREFERENCE_NODE_NAME = "dropbox";
    /**
     * This is the prefix for the name of the preference nodes used to store 
     * the settings relating to a specific type of list. The list type is 
     * appended to the end of this to get the preference node specific for that 
     * list type.
     */
    public static final String LIST_TYPE_PREFERENCE_NODE_NAME_PREFIX="listType=";
    /**
     * This is the prefix for the name of the preference nodes used to store 
     * the settings relating to a specific list. The list's listID is appended 
     * to the end of this to get the preference node specific for that list.
     */
    public static final String LIST_ID_PREFERENCE_NODE_NAME_PREFIX = "listID=";
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
     * This is a properties map that stores the configuration for LinkManager.
     */
    private final ConfigProperties config;
    /**
     * This is a properties map that stores the default configuration for 
     * LinkManager, and which serves as the default properties map for {@code 
     * config}.
     */
    private final Properties defaultConfig;
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
     * This is a map view of the current tab listIDs. This is initially null 
     * and is initialized when first used.
     */
    private Map<Integer, Integer> currTabIDMap = null;
    /**
     * This is a map view of the current tab list indexes. This is initially 
     * null and is initialized when first used.
     */
    private Map<Integer, Integer> currTabIndexMap = null;
    /**
     * This is a map view of the selected links for the lists. This is initially 
     * null and is initialized when first used.
     */
    private Map<Integer, String> selLinkMap = null;
    
    /**
     * This is the ID for the program.
     */
    private UUID programID = null;
    /**
     * This is the Obfuscator object used to encrypt and decrypt sensitive data.
     */
    protected final Obfuscator obfuscator;
    /**
     * 
     * @param sqlProp
     * @param node 
     * @param obfuscator
     */
    private LinkManagerConfig(Properties sqlProp, ConfigPreferences node,
            Obfuscator obfuscator){
        defaultConfig = new Properties();
        config = new ConfigProperties(defaultConfig);
        compNameMap = new HashMap<>();
            // If the given SQLite config properties is not null
        if(sqlProp != null)
            sqlConfig = new SQLiteConfig(sqlProp);
        else
            sqlConfig = new SQLiteConfig();
        programNode = node;
        localDefaults = new ConfigProperties();
        this.obfuscator = obfuscator;
    }
    /**
     * 
     * @param node 
     * @param obfuscator 
     */
    public LinkManagerConfig(Preferences node, Obfuscator obfuscator){
        this(null, new ConfigPreferences(node, new ConfigProperties()), 
                obfuscator);
    }
    /**
     * 
     * @param node 
     */
    public LinkManagerConfig(Preferences node){
        this(node,null);
    }
    /**
     * 
     * @param linkConfig 
     */
    public LinkManagerConfig(LinkManagerConfig linkConfig){
        this(linkConfig.sqlConfig.toProperties(), linkConfig.programNode,
                linkConfig.obfuscator);
        this.defaultConfig.putAll(linkConfig.defaultConfig);
        this.config.putAll(linkConfig.config);
        this.compNameMap.putAll(linkConfig.compNameMap);
        this.localDefaults.addProperties(linkConfig.localDefaults);
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
     * This returns the preference node used to store the Dropbox settings.
     * @return 
     */
    public ConfigPreferences getDropboxPreferences(){
        return getPreferences().node(DROPBOX_PREFERENCE_NODE_NAME);
    }
    /**
     * This returns the preference node used to store the Dropbox access tokens.
     * @return 
     */
    public ConfigPreferences getPrivateDropboxPreferences(){
        return getPrivatePreferences().node(DROPBOX_PREFERENCE_NODE_NAME);
    }
    /**
     * 
     * @param type The list type
     * @return 
     */
    public ConfigPreferences getListTypePreferences(int type){
        return getPreferences().node(LIST_TYPE_PREFERENCE_NODE_NAME_PREFIX+type);
    }
    /**
     * 
     * @param listID The list ID
     * @return 
     */
    public ConfigPreferences getListPreferences(int listID){
        return getPreferences().node(LIST_ID_PREFERENCE_NODE_NAME_PREFIX+listID);
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
        return programNode.node(pathName,defaults);
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
        if (!path.isEmpty() && !path.endsWith("/"))
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
        localNode = getProgramIDNode(LOCAL_PREFERENCE_NODE_PATH,getDefaults());
            // Set the private preference node
        privateNode = getProgramIDNode(PRIVATE_PREFERENCE_NODE_PATH,null);
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
    public ConfigProperties getProperties(){
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
        if (value == null){
                // Remove it from the configuration and get its value
            Object old = config.remove(key);
                // If it's not null, return it as a string. Otherwise, return null
            return (old != null) ? old.toString() : null;
        }else{   // Get the value as a String
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
    public String getPropertyDefault(String key){
        return getDefaultProperties().getProperty(key);
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
     * @param parent
     * @param path
     * @return 
     */
    private boolean nodeExists(Preferences parent, String path){
        try{
            return parent.nodeExists(path);
        } catch (BackingStoreException | IllegalStateException ex) { }
        return false;
    }
    /**
     * 
     * @param node 
     */
    private void removeNode(Preferences node){
        try {
            node.removeNode();
        } catch (BackingStoreException | IllegalStateException ex) { }
    }
    /**
     * 
     * @param key
     * @return 
     */
    private boolean isPrefixedListKey(String key){
            // Go through the list of list type property prefixes
        for (String prefix : LIST_TYPE_PROPERTY_KEY_PREFIXES){
                // If the key matches the current prefix with the list type 
                // property key suffix
            if (key.startsWith(prefix+LIST_TYPE_PROPERTY_KEY_SUFFIX))
                return true;
        }   // Go through the list of list ID property prefixes
        for (String prefix : LIST_ID_PROPERTY_KEY_PREFIXES){
                // If the key matches the current prefix with the list ID 
                // property key suffix
            if (key.startsWith(prefix+LIST_ID_PROPERTY_KEY_SUFFIX))
                return true;
        }
        return false;
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
            // Get the value for the Dropbox database file path from the 
            // properties
        str = cProp.getProperty(DROPBOX_PROPERTY_KEY_PREFIX+DATABASE_FILE_PATH_KEY);    
            // If the properties has the Dropbox database file path
        if (str != null)
                // Set the Dropbox database file path from the properties
            setDropboxDatabaseFileName(str);
            // Go through the entries in the component name map
        for (Map.Entry<Component,String> entry:getComponentNames().entrySet()){
                // Get the dimension for the component from the properties
            Dimension dim = cProp.getDimensionProperty(entry.getValue()+
                    COMPONENT_SIZE_KEY_SUFFIX);
                // If the properties has a size for the component
            if (dim != null)
                    // Set the component's size from the properties
                setComponentSize(entry.getKey(),dim);
                // Get the location for the component from the properties
            Point point = cProp.getPointProperty(entry.getValue()+
                    COMPONENT_LOCATION_KEY_SUFFIX);
                // If the properties has a location for the component
            if (point != null)
                    // Set the component's location from the properties
                setComponentLocation(entry.getKey(),point);
                // Get the bounds for the component from the properties
            Rectangle rect = cProp.getRectangleProperty(entry.getValue()+
                    COMPONENT_BOUNDS_KEY_SUFFIX);
                // If the properties has bounds for the component
            if (rect != null)
                    // Set the component's bounds from the properties
                setComponentBounds(entry.getKey(),rect);
            
                // TODO: Remove this once the config properties map is removed or 
                // repurposed.
                // Remove the component size, since that's in the preference node
            config.remove(entry.getValue()+COMPONENT_SIZE_KEY_SUFFIX);
                // Remove the component location, since that's in the preference node
            config.remove(entry.getValue()+COMPONENT_LOCATION_KEY_SUFFIX);
                // Remove the component bounds, since that's in the preference node
            config.remove(entry.getValue()+COMPONENT_BOUNDS_KEY_SUFFIX);
        }   // This maps listIDs to the selected link for that list
        Map<Integer,String> selMap = new HashMap<>();
            // This maps the listIDs to whether the selected link is visible for 
            // This maps the list types to the listID of the selected list for 
            // that list type
        Map<Integer,Integer> selListIDMap = new HashMap<>();
            // This maps the list types to the selected index of the tab for 
            // that list type
        Map<Integer,Integer> selListMap = new HashMap<>();
            // This gets a set of keys for the properties that deal with lists
        Set<String> listKeys = new HashSet<>(cProp.stringPropertyNames());
            // Remove any null keys and keys that aren't prefixed keys for the 
            // selection
        listKeys.removeIf((String t) -> {
            return t == null || !isPrefixedListKey(t);
        });
            // Go through the property keys that deal with lists
        for (String key : listKeys){
            String keyPrefix;   // Get the prefix for the current key
                // The suffix for the property version of the key
            String keySuffix = LIST_ID_PROPERTY_KEY_SUFFIX;   
                // If the key starts with the selected link key
            if (key.startsWith(SELECTED_LINK_FOR_LIST_KEY))
                keyPrefix = SELECTED_LINK_FOR_LIST_KEY;
                // If the key starts with the current tab listID key
            else if (key.startsWith(CURRENT_TAB_LIST_ID_KEY)){
                keyPrefix = CURRENT_TAB_LIST_ID_KEY;
                keySuffix = LIST_TYPE_PROPERTY_KEY_SUFFIX;
            }   // If the key starts with the current tab index key
            else if (key.startsWith(CURRENT_TAB_INDEX_KEY)){
                keyPrefix = CURRENT_TAB_INDEX_KEY;
                keySuffix = LIST_TYPE_PROPERTY_KEY_SUFFIX;
            } else  // Skip this key
                continue;
            try{    // Get the list or tabs panel that this key is for
                int type = Integer.parseInt(key.substring(keyPrefix.length()+
                        keySuffix.length()));
                    // Determine which key this is based off the prefix
                switch(keyPrefix){
                        // If this is the selected link key
                    case(SELECTED_LINK_FOR_LIST_KEY):
                        selMap.put(type, cProp.getProperty(key));
                        break;
                        // If this is the current tab listID key
                    case(CURRENT_TAB_LIST_ID_KEY):
                        selListIDMap.put(type, cProp.getIntProperty(key));
                        break;
                        // If this is the current tab index key
                    case(CURRENT_TAB_INDEX_KEY):
                        selListMap.put(type, cProp.getIntProperty(key));
                }
            } catch(NumberFormatException ex){ }
            
                // TODO: Remove this once the config properties map is removed or 
                // repurposed.
                // Remove this key since it'll soon be in the preference node
            config.remove(key);
        }   // Remove all null values from the selected links
        selMap.values().removeIf((String t) -> t == null);
            // Remove all null values from the current tab listIDs
        selListIDMap.values().removeIf((Integer t) -> t == null);
            // Remove all null values from the current tab indexes
        selListMap.values().removeIf((Integer t) -> t == null);
            // Add all the values for the selected links in the lists
        getSelectedLinkMap().putAll(selMap);
        
            // Add all the values for the current tab listIDs 
        getCurrentTabListIDMap().putAll(selListIDMap);
            // Add all the values for the current tab indexes
        getCurrentTabIndexMap().putAll(selListMap);
        
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
            // Remove the dropbox database file path, since that's in the preference node
        config.remove(DROPBOX_PROPERTY_KEY_PREFIX+DATABASE_FILE_PATH_KEY);
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
     * @param width
     * @param height 
     */
    public void setDefaultComponentSize(Component comp, int width, int height){
        getDefaults().setDimensionProperty(getComponentName(comp)+
                COMPONENT_SIZE_KEY_SUFFIX, width,height);
    }
    /**
     * 
     * @param comp
     * @param value 
     */
    public void setDefaultComponentSize(Component comp, Dimension value){
        getDefaults().setDimensionProperty(getComponentName(comp)+
                COMPONENT_SIZE_KEY_SUFFIX, value);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension getDefaultComponentSize(Component comp){
        return getDefaults().getDimensionProperty(getComponentName(comp)+
                COMPONENT_SIZE_KEY_SUFFIX);
    }
    /**
     * 
     * @param comp
     * @param width
     * @param height 
     */
    public void setComponentSize(Component comp, int width, int height){
        getPreferences().putDimension(getComponentName(comp)+
                COMPONENT_SIZE_KEY_SUFFIX, width,height);
    }
    /**
     * 
     * @param comp
     * @param value 
     */
    public void setComponentSize(Component comp, Dimension value){
        getPreferences().putDimension(getComponentName(comp)+
                COMPONENT_SIZE_KEY_SUFFIX, value);
    }
    /**
     * 
     * @param comp 
     */
    public void setComponentSize(Component comp){
        setComponentSize(comp,comp.getWidth(),comp.getHeight());
    }
    /**
     * 
     * @param comp
     * @param defaultValue
     * @return 
     */
    public Dimension getComponentSize(Component comp, Dimension defaultValue){
        return getPreferences().getDimension(getComponentName(comp)+
                COMPONENT_SIZE_KEY_SUFFIX, defaultValue);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension getComponentSize(Component comp){
        return getComponentSize(comp,null);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public boolean isComponentSizeSet(Component comp){
        return getPreferences().isKeySet(getComponentName(comp)+
                COMPONENT_SIZE_KEY_SUFFIX);
    }
    /**
     * 
     * @param comp
     * @param x
     * @param y 
     */
    public void setDefaultComponentLocation(Component comp, int x, int y){
        getDefaults().setPointProperty(getComponentName(comp)+
                COMPONENT_LOCATION_KEY_SUFFIX, x, y);
    }
    /**
     * 
     * @param comp
     * @param value 
     */
    public void setDefaultComponentLocation(Component comp, Point value){
        getDefaults().setPointProperty(getComponentName(comp)+
                COMPONENT_LOCATION_KEY_SUFFIX, value);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Point getDefaultComponentLocation(Component comp){
        return getDefaults().getPointProperty(getComponentName(comp)+
                COMPONENT_LOCATION_KEY_SUFFIX);
    }
    /**
     * 
     * @param comp
     * @param x
     * @param y 
     */
    public void setComponentLocation(Component comp, int x, int y){
        getPreferences().putPoint(getComponentName(comp)+
                COMPONENT_LOCATION_KEY_SUFFIX, x, y);
    }
    /**
     * 
     * @param comp
     * @param value 
     */
    public void setComponentLocation(Component comp, Point value){
        getPreferences().putPoint(getComponentName(comp)+
                COMPONENT_LOCATION_KEY_SUFFIX, value);
    }
    /**
     * 
     * @param comp 
     */
    public void setComponentLocation(Component comp){
        setComponentLocation(comp,comp.getX(),comp.getY());
    }
    /**
     * 
     * @param comp
     * @param defaultValue
     * @return 
     */
    public Point getComponentLocation(Component comp, Point defaultValue){
        return getPreferences().getPoint(getComponentName(comp)+
                COMPONENT_LOCATION_KEY_SUFFIX, defaultValue);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Point getComponentLocation(Component comp){
        return getComponentLocation(comp,null);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public boolean isComponentLocationSet(Component comp){
        return getPreferences().isKeySet(getComponentName(comp)+
                COMPONENT_LOCATION_KEY_SUFFIX);
    }
    /**
     * 
     * @param comp
     * @param x
     * @param y
     * @param width
     * @param height 
     */
    public void setDefaultComponentBounds(Component comp,int x,int y,int width,
            int height){
        getDefaults().setRectangleProperty(getComponentName(comp)+
                COMPONENT_BOUNDS_KEY_SUFFIX,x,y,width,height);
    }
    /**
     * 
     * @param comp
     * @param width
     * @param height 
     */
    public void setDefaultComponentBounds(Component comp,int width,int height){
        setDefaultComponentBounds(comp,0,0,width,height);
    }
    /**
     * 
     * @param comp
     * @param value 
     */
    public void setDefaultComponentBounds(Component comp, Rectangle value){
        getDefaults().setRectangleProperty(getComponentName(comp)+
                COMPONENT_BOUNDS_KEY_SUFFIX, value);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Rectangle getDefaultComponentBounds(Component comp){
        return getDefaults().getRectangleProperty(getComponentName(comp)+
                COMPONENT_BOUNDS_KEY_SUFFIX);
    }
    /**
     * 
     * @param comp
     * @param x
     * @param y
     * @param width
     * @param height 
     */
    public void setComponentBounds(Component comp,int x,int y,int width,
            int height){
        getPreferences().putRectangle(getComponentName(comp)+
                COMPONENT_BOUNDS_KEY_SUFFIX,x,y,width,height);
    }
    /**
     * 
     * @param comp
     * @param width
     * @param height 
     */
    public void setComponentBounds(Component comp,int width,int height){
        setComponentBounds(comp,0,0,width,height);
    }
    /**
     * 
     * @param comp
     * @param value 
     */
    public void setComponentBounds(Component comp, Rectangle value){
        getPreferences().putRectangle(getComponentName(comp)+
                COMPONENT_BOUNDS_KEY_SUFFIX, value);
    }
    /**
     * 
     * @param comp 
     */
    public void setComponentBounds(Component comp){
        setComponentBounds(comp,comp.getX(),comp.getY(),
                comp.getWidth(),comp.getHeight());
    }
    /**
     * 
     * @param comp
     * @param defaultValue
     * @return 
     */
    public Rectangle getComponentBounds(Component comp, Rectangle defaultValue){
        return getPreferences().getRectangle(getComponentName(comp)+
                COMPONENT_BOUNDS_KEY_SUFFIX, defaultValue);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Rectangle getComponentBounds(Component comp){
        return getComponentBounds(comp,null);
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public boolean isComponentBoundsSet(Component comp){
        return getPreferences().isKeySet(getComponentName(comp)+
                COMPONENT_BOUNDS_KEY_SUFFIX);
    }
    /**
     * 
     * @param listType
     * @param key
     * @return 
     */
    private Integer getCurrentTabValue(int listType, String key){
            // Get the preference node for the list type
        ConfigPreferences node = getListTypePreferences(listType);
            // Get whether the node contains the key
        if (node.containsKey(key))
                // Get the value for the given key
            return node.getInt(key, 0);
        return null;
    }
    /**
     * 
     * @param listType
     * @param listID 
     */
    public void setCurrentTabListID(int listType, Integer listID){
        getListTypePreferences(listType).putObject(CURRENT_TAB_LIST_ID_KEY, 
                listID);
    }
    /**
     * 
     * @param listType
     * @return 
     */
    public Integer getCurrentTabListID(int listType){
        return getCurrentTabValue(listType, CURRENT_TAB_LIST_ID_KEY);
    }
    /**
     * 
     * @return 
     */
    public Map<Integer, Integer> getCurrentTabListIDMap(){
        if (currTabIDMap == null){
            currTabIDMap = new ListConfigDataMap<>(){
                @Override
                protected Integer getValue(int key) {
                    return getCurrentTabListID(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setCurrentTabListID(key,value);
                }
                @Override
                protected String getPrefixForNodes() {
                    return LIST_TYPE_PREFERENCE_NODE_NAME_PREFIX;
                }
            };
        }
        return currTabIDMap;
    }
    /**
     * 
     * @param listType
     * @param index 
     */
    public void setCurrentTabIndex(int listType, Integer index){
        getListTypePreferences(listType).putObject(CURRENT_TAB_INDEX_KEY,index);
    }
    /**
     * 
     * @param listType
     * @return 
     */
    public Integer getCurrentTabIndex(int listType){
        return getCurrentTabValue(listType, CURRENT_TAB_INDEX_KEY);
    }
    /**
     * 
     * @return 
     */
    public Map<Integer, Integer> getCurrentTabIndexMap(){
        if (currTabIndexMap == null){
            currTabIndexMap = new ListConfigDataMap<>(){
                @Override
                protected Integer getValue(int key) {
                    return getCurrentTabIndex(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setCurrentTabIndex(key,value);
                }
                @Override
                protected String getPrefixForNodes() {
                    return LIST_TYPE_PREFERENCE_NODE_NAME_PREFIX;
                }
            };
        }
        return currTabIndexMap;
    }
    /**
     * 
     * @param listType
     * @param tabsPanel 
     */
    public void setCurrentTab(int listType, LinksListTabsPanel tabsPanel){
        setCurrentTabListID(listType,tabsPanel.getSelectedListID());
            // If the tabs panel has nothing selected, set the index to null.
            // Otherwise, set it to the selected index
        setCurrentTabIndex(listType, (tabsPanel.isSelectionEmpty()) ? null :
                tabsPanel.getSelectedIndex());
    }
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setSelectedLink(int listID, String value){
        getListPreferences(listID).put(SELECTED_LINK_FOR_LIST_KEY, value);
    }
    /**
     * 
     * @param listID
     * @return 
     */
    public String getSelectedLink(int listID){
        return getListPreferences(listID).get(SELECTED_LINK_FOR_LIST_KEY, null);
    }
    /**
     * 
     * @return 
     */
    public Map<Integer,String> getSelectedLinkMap(){
        if (selLinkMap == null){
            selLinkMap = new ListConfigDataMap<>(){
                @Override
                protected String getValue(int key) {
                    return getSelectedLink(key);
                }
                @Override
                protected void putValue(int key, String value) {
                    setSelectedLink(key,value);
                }
                @Override
                protected String getPrefixForNodes() {
                    return LIST_ID_PREFERENCE_NODE_NAME_PREFIX;
                }
            };
        }
        return selLinkMap;
    }
    /**
     * 
     * @param value 
     */
    public void setDropboxDatabaseFileName(String value){
        setFilePathPreference(DATABASE_FILE_PATH_KEY,value,
                getDropboxPreferences());
    }
    /**
     * 
     * @return 
     */
    public String getDropboxDatabaseFileName(){
        return getFilePathPreference(DATABASE_FILE_PATH_KEY,
                LinkManager.LINK_DATABASE_FILE,getDropboxPreferences());
    }
    /**
     * 
     * @todo Add encryption of the Dropbox tokens.
     * 
     * @param key
     * @param token 
     */
    private void setDropboxToken(String key, String token){
            // Get the private Dropbox preference node and put the token in it
        getPrivateDropboxPreferences().put(key, token);
    }
    /**
     * 
     * @todo Add decryption of the Dropbox tokens.
     * 
     * @param key
     * @return 
     */
    private String getDropboxToken(String key){
        return getPrivateDropboxPreferences().get(key, null);
    }
    /**
     * 
     * @param token 
     */
    public void setDropboxAccessToken(String token){
        setDropboxToken(DROPBOX_ACCESS_TOKEN_KEY,token);
    }
    /**
     * 
     * @return 
     */
    public String getDropboxAccessToken(){
        return getDropboxToken(DROPBOX_ACCESS_TOKEN_KEY);
    }
    /**
     * 
     * @param token 
     */
    public void setDropboxRefreshToken(String token){
        setDropboxToken(DROPBOX_REFRESH_TOKEN_KEY,token);
    }
    /**
     * 
     * @return 
     */
    public String getDropboxRefreshToken(){
        return getDropboxToken(DROPBOX_REFRESH_TOKEN_KEY);
    }
    /**
     * 
     * @param time 
     */
    public void setDropboxTokenExpiresAt(Long time){
            // Set the value for the time
        getPrivateDropboxPreferences().putObject(DROPBOX_TOKEN_EXPIRATION_KEY,
                time);
    }
    /**
     * 
     * @return 
     */
    public Long getDropboxTokenExpiresAt(){
            // Get the private Dropbox preference node
        ConfigPreferences node = getPrivateDropboxPreferences();
            // Get whether the node contains the dropbox token expire time
        if (node.containsKey(DROPBOX_TOKEN_EXPIRATION_KEY))
            return node.getLong(DROPBOX_TOKEN_EXPIRATION_KEY, 0);
        return null;
    }
    /**
     * 
     */
    public void clearDropboxToken(){
            // If the private Dropbox node exists
        if (nodeExists(getPrivatePreferences(),DROPBOX_PREFERENCE_NODE_NAME))
                // Remove it
            removeNode(getPrivateDropboxPreferences());
    }
    /**
     * 
     */
    public abstract class DropboxLinkUtilsConfig extends DropboxLinkUtils{
        @Override
        public String getAccessToken() {
            return getDropboxAccessToken();
        }
        @Override
        public void setAccessToken(String token) {
            setDropboxAccessToken(token);
        }
        @Override
        public String getRefreshToken() {
            return getDropboxRefreshToken();
        }
        @Override
        public void setRefreshToken(String token) {
            setDropboxRefreshToken(token);
        }
        @Override
        public Long getTokenExpiresAt() {
            return getDropboxTokenExpiresAt();
        }
        @Override
        public void setTokenExpiresAt(Long time) {
            setDropboxTokenExpiresAt(time);
        }
        @Override
        public void clearCredentials(){
            super.clearCredentials();
            clearDropboxToken();
        }
    }
    /**
     * 
     * @param <V> 
     */
    private abstract class ListConfigDataMap<V> extends AbstractMap<Integer,V>{
        /**
         * This is an array containing the entries in this map. This is 
         * initially null and is initialized the first time it is used.
         */
        private Set<Entry<Integer, V>> entries = null;
        /**
         * This returns the value to which the specified key is mapped to, or 
         * null if this map contains no mapping for the key. This is called by 
         * the {@code get} method to get the value to return.
         * @param key The key whose associated value is to be returned.
         * @return The value to which the specified key is mapped to, or null if 
         * this map contains no mapping for the key.
         */
        protected abstract V getValue(int key);
        @Override
        public V get(Object key){
                // Require the key to not be null
            Objects.requireNonNull(key, "Key cannot be null");
                // If the key is an integer
            if (key instanceof Integer)
                    // Get the value
                return getValue((Integer)key);
            else
                throw new ClassCastException();
        }
        @Override
        public boolean containsKey(Object key){
                // If the given key is an integer
            if (key instanceof Integer)
                return getValue((Integer)key) != null;
            return false;
        }
        /**
         * Associates the given value with the given key in this map, or removes 
         * the given key if the given value is null. This is called by the 
         * {@code put} method to put the values and {@code remove} method to 
         * remove the key.
         * @param key The key with which the given value is to be associated 
         * with.
         * @param value The value to be associated with the given key, or null 
         * if the key is to be removed.
         * @throws ClassCastException If the class of the given key or value 
         * prevents it from being stored in this map.
         * @see IllegalArgumentException If some property of the given key or 
         * value prevents it from being stored in this map.
         */
        protected abstract void putValue(int key, V value);
        @Override
        public V put(Integer key, V value){
                // Require the key to not be null
            Objects.requireNonNull(key, "Key cannot be null");
                // Require the value to not be null
            Objects.requireNonNull(value, "Value cannot be null");
                // Get the old value for the key
            V old = getValue(key);
                // Put the value into the map
            putValue(key,value);
            return old;
        }
        @Override
        public V remove(Object key){
                // Require the key to not be null
            Objects.requireNonNull(key, "Key cannot be null");
                // If the key is an integer
            if (key instanceof Integer){
                    // Get the key as an integer
                Integer keyInt = (Integer) key;
                    // Get the old value for the key
                V old = getValue(keyInt);
                    // Put null for the value into the map
                putValue(keyInt,null);
                return old;
            } else
                throw new ClassCastException();
        }
        /**
         * 
         * @return 
         */
        protected abstract String getPrefixForNodes();
        /**
         * This returns a set containing the keys for this map.
         * @return The set containing the keys for this map.
         */
        protected Set<Integer> getKeys(){
                // This will get the keys in this map
            Set<Integer> keys = new TreeSet<>();
            try{    // Get the names of the child nodes in the local preference 
                String[] childNodes = getPreferences().childrenNames(); // node
                    // Go through the names of the child nodes
                for (String child : childNodes){
                        // If the child's name is not null and starts with the 
                        // prefix
                    if (child != null && child.startsWith(getPrefixForNodes())){
                        try{    // Parse the number at the end
                            int value = Integer.parseInt(child.substring(
                                    getPrefixForNodes().length()));
                                // If this map contains a non-null value for 
                            if (containsKey(value))     // that key
                                keys.add(value);
                        } catch (NumberFormatException ex) {}
                    }
                }
            } catch (BackingStoreException ex) {}
            return keys;
        }
        @Override
        public Set<Entry<Integer, V>> entrySet() {
                // If the entry set is null
            if (entries == null){
                    // Create a new AbstractSet to go through the entries
                entries = new AbstractSet<>(){
                    @Override
                    public Iterator<Entry<Integer, V>> iterator() {
                        return new Iterator<>(){
                            /**
                             * Get an iterator to go over the keys.
                             */
                            Iterator<Integer> itr = getKeys().iterator();
                            /**
                             * This is the most recent key with an entry 
                             * returned for.
                             */
                            Integer current = null;
                            @Override
                            public boolean hasNext() {
                                return itr.hasNext();
                            }
                            @Override
                            public Entry<Integer, V> next() {
                                    // Get the next value in the key iterator
                                current = itr.next();
                                return new Entry<>(){
                                    /**
                                     * The key for this entry
                                     */
                                    Integer key = current;
                                    @Override
                                    public Integer getKey() {
                                        return key;
                                    }
                                    @Override
                                    public V getValue() {
                                        return ListConfigDataMap.this.getValue(key);
                                    }
                                    @Override
                                    public V setValue(V value) {
                                        return put(key,value);
                                    }
                                };
                            }
                            @Override
                            public void remove() {
                                    // Remove the value, mostly to throw any 
                                    // exceptions
                                itr.remove();
                                    // Actually remove the value from the map
                                putValue(current,null);
                            }
                        };
                    }
                    @Override
                    public int size() {
                        return getKeys().size();
                    }
                };
            }
            return entries;
        }
    }
}
