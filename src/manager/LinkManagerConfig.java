/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.security.*;
import java.util.*;
import java.util.prefs.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import manager.config.*;
import manager.dropbox.DropboxLinkUtils;
import manager.links.*;
import manager.security.*;
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
     * This is the user and program specific encryption key for the program. 
     * This encryption key is, in it of itself, encrypted by another encryption 
     * key stored along side the program ID.
     */
    private static final String ENCRYPTION_KEY_KEY = "EncryptionKey";
    /**
     * This is the configuration key for the chunk size multiplier for the 
     * chunk size used when uploading large files.
     */
    public static final String CHUNK_SIZE_MULTIPLIER_KEY = "ChunkSizeMultiplier";
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
     * This is the name of the preference node used to store the data for 
     * Dropbox.
     */
    public static final String DROPBOX_PREFERENCE_NODE_NAME = "dropbox";
    /**
     * This is the name of the preference node used to store the preference 
     * nodes that store the settings relating to a specific type of list. The 
     * list type is used as the name of the preference node that corresponds to 
     * that list type.
     */
    public static final String LIST_TYPE_PREFERENCE_NODE_NAME = "listType";
    /**
     * This is the name of the preference node used to store the preference 
     * nodes that store the settings relating to a specific list. The listID is 
     * used as the name of the preference node that corresponds to that list.
     */
    public static final String LIST_ID_PREFERENCE_NODE_NAME = "listID";
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
     * This is a properties map that stores the configuration for LinkManager.
     */
    private final ConfigProperties config;
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
     * This is a map view of whether the selected links are visible for the 
     * lists. This is initially null and is initialized when first used.
     */
    private Map<Integer, Boolean> selLinkVisMap = null;
    /**
     * This is a map view of the first visible indexes in the lists. This is 
     * initially null and is initialized when first used.
     */
    private Map<Integer, Integer> firstVisIndexMap = null;
    /**
     * This is a map view of the last visible indexes in the lists. This is 
     * initially null and is initialized when first used.
     */
    private Map<Integer, Integer> lastVisIndexMap = null;
    /**
     * This is the preference node that stores the settings and tokens for 
     * Dropbox.
     */
    protected ConfigPreferences dropboxNode = null;
    /**
     * This is a map that caches the list type preference nodes.
     */
    protected Map<Integer, ConfigPreferences> listTypeNodeMap;
    /**
     * This is a map that caches the listID preference nodes.
     */
    protected Map<Integer, ConfigPreferences> listIDNodeMap;
    /**
     * This is the ID for the program.
     */
    private UUID programID = null;
    /**
     * This is the secret key used for the cipher.
     */
    protected SecretKey secretKey = null;
    /**
     * This is the IV Parameter used for the cipher.
     */
    protected IvParameterSpec cipherIV = null;
    /**
     * This is the SecureRandom used to generate random numbers for the cipher.
     */
    protected SecureRandom secureRand = null;
    /**
     * The key generator used to generate the secret keys.
     */
    protected KeyGenerator keyGen = null;
    /**
     * 
     * @param sqlProp
     * @param node 
     */
    private LinkManagerConfig(Properties sqlProp, ConfigPreferences node){
        config = new ConfigProperties();
        compNameMap = new HashMap<>();
            // If the given SQLite config properties is not null
        if(sqlProp != null)
            sqlConfig = new SQLiteConfig(sqlProp);
        else
            sqlConfig = new SQLiteConfig();
        programNode = node;
        localDefaults = new ConfigProperties();
        listTypeNodeMap = new HashMap<>();
        listIDNodeMap = new HashMap<>();
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
        this.config.putAll(linkConfig.config);
        this.compNameMap.putAll(linkConfig.compNameMap);
        this.localDefaults.addProperties(linkConfig.localDefaults);
        LinkManagerConfig.this.setProgramID(linkConfig.programID);
        this.secretKey = linkConfig.secretKey;
        this.cipherIV = linkConfig.cipherIV;
        this.secureRand = linkConfig.secureRand;
        this.keyGen = linkConfig.keyGen;
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
     * This returns the preference node used to store the Dropbox settings.
     * @return 
     */
    public ConfigPreferences getDropboxPreferences(){
            // If the Dropbox node is currently null
        if (dropboxNode == null)
            dropboxNode = getPreferences().node(DROPBOX_PREFERENCE_NODE_NAME);
        return dropboxNode;
    }
    /**
     * 
     * @param key
     * @param prefix
     * @param cache
     * @return 
     */
    private ConfigPreferences getListDataPreferences(int key, String prefix, 
            Map<Integer, ConfigPreferences> cache){
            // Check the preference node cache for the node
        ConfigPreferences node = cache.get(key);
            // If the cache does not have the preference node
        if (node == null){
                // Get the node
            node = getPreferences().node(prefix+"="+key);
                // Cache the node
            cache.put(key, node);
        }
        return node;
    }
    /**
     * 
     * @param type The list type
     * @return 
     */
    public ConfigPreferences getListTypePreferences(int type){
        return getListDataPreferences(type,LIST_TYPE_PREFERENCE_NODE_NAME,
                listTypeNodeMap);
    }
    /**
     * 
     * @param listID The list ID
     * @return 
     */
    public ConfigPreferences getListPreferences(int listID){
        return getListDataPreferences(listID,LIST_ID_PREFERENCE_NODE_NAME,
                listIDNodeMap);
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
     * This creates and returns the local preference node for the program using 
     * the {@link #getProgramID() program ID} as the name of the node. This is 
     * equivalent to the following: 
     * 
     * <pre> {@code
     * return getSharedPreferences().node(getProgramID().toString(), getDefaults());
     * }</pre>
     * 
     * @return The ConfigPreferences node to use to store the local settings for 
     * instances of the program with the currently set program ID.
     * @throws IllegalStateException If the program node (or an ancestor) has 
     * been removed with the {@link ConfigPreferences#removeNode() removeNode()} 
     * method.
     * @see #getSharedPreferences() 
     * @see ConfigPreferences#node(String) 
     * @see #getProgramID() 
     * @see #getDefaults() 
     * @see #getPreferences() 
     * @see #setProgramID(UUID) 
     */
    protected ConfigPreferences createPreferences(){
        return programNode.node(programID.toString(), getDefaults());
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
     * {@link #getPreferences() local preference node}.
     * @param id The new program ID.
     * @throws NullPointerException If the program ID is null.
     * @see #getProgramID() 
     * @see #setRandomProgramID() 
     * @see #getPreferences()
     */
    public void setProgramID(UUID id){
            // Check if the program ID is null
        Objects.requireNonNull(id, "Program ID cannot be null");
            // If the program ID would not change
        if (id.equals(programID))
            return;
        programID = id;
            // Set the local preference node
        localNode = createPreferences();
            // Clear the list type preference node cache
        listTypeNodeMap.clear();
            // Clear the listID preference node cache
        listIDNodeMap.clear();
            // Reset the Dropbox node to null
        dropboxNode = null;
    }
    /**
     * This sets the program ID to be a random {@code UUID}.
     * @return The {@code UUID} used as the program ID.
     * @see UUID#randomUUID() 
     * @see #getProgramID() 
     * @see #setProgramID(UUID) 
     * @see #getPreferences()
     */
    public UUID setRandomProgramID(){
            // Generate a random UUID
        UUID id = UUID.randomUUID();
            // Set the program ID to the generated UUID
        setProgramID(id);
        return id;
    }
    /**
     * 
     * @return 
     */
    public SecureRandom getSecureRandom(){
        return secureRand;
    }
    /**
     * 
     * @param rand 
     */
    public void setSecureRandom(SecureRandom rand){
        secureRand = rand;
    }
    /**
     * 
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public SecureRandom setSecureRandom() throws NoSuchAlgorithmException{
        setSecureRandom(SecureRandom.getInstanceStrong());
        return getSecureRandom();
    }
    /**
     * 
     * @return 
     */
    public KeyGenerator getKeyGenerator(){
        return keyGen;
    }
    /**
     * 
     * @param keyGen 
     */
    public void setKeyGenerator(KeyGenerator keyGen){
        this.keyGen = keyGen;
    }
    /**
     * 
     * @param rand
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public KeyGenerator setKeyGenerator(SecureRandom rand) throws NoSuchAlgorithmException{
        setKeyGenerator(CipherUtilities.getKeyGenerator(rand));
        return getKeyGenerator();
    }
    /**
     * 
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public KeyGenerator setKeyGenerator() throws NoSuchAlgorithmException{
        return setKeyGenerator(getSecureRandom());
    }
    /**
     * 
     * @param value 
     */
    protected void setRawEncryptionKey(byte[] value){
        getPreferences().putByteArray(ENCRYPTION_KEY_KEY, value);
    }
    /**
     * 
     * @return 
     */
    protected byte[] getRawEncryptionKey(){
        return getPreferences().getByteArray(ENCRYPTION_KEY_KEY, null);
    }
    /**
     * 
     * @param key
     * @param iv 
     * @throws java.security.NoSuchAlgorithmException 
     * @throws javax.crypto.NoSuchPaddingException 
     * @throws java.security.InvalidKeyException 
     * @throws java.security.InvalidAlgorithmParameterException 
     * @throws javax.crypto.IllegalBlockSizeException 
     * @throws javax.crypto.BadPaddingException 
     */
    public void initializeEncryption(SecretKey key, IvParameterSpec iv) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, 
            InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, BadPaddingException{
            // Get the encrypted encryption key
        byte[] localKey = getRawEncryptionKey();
            // If there is not an encryption key set
        if (localKey == null){
                // Get the unencrypted Dropbox access token
            String accessToken = getDropboxAccessToken();
                // Get the unencrypted Dropbox refresh token
            String refreshToken = getDropboxRefreshToken();
                // Generate the secret key
            secretKey = getKeyGenerator().generateKey();
                // Generate the IV
            cipherIV = CipherUtilities.generateIV(getSecureRandom());
                // Get the encryption key for the program
            localKey = CipherUtilities.getEncryptionKey(secretKey, cipherIV);
                // Encrypt the encryption key and store it
            setRawEncryptionKey(CipherUtilities.encryptByteArray(localKey, key,
                    iv, getSecureRandom()));
                // Set the Dropbox access token, which should encrypt it now
            setDropboxAccessToken(accessToken);
                // Set the Dropbox refresh token, which should encrypt it now
            setDropboxRefreshToken(refreshToken);
        } else {    // Decrypt the encryption key
            localKey = CipherUtilities.decryptByteArray(localKey, key, iv, 
                    getSecureRandom());
                // Extract the secret key from the encryption key
            secretKey = CipherUtilities.getSecretKeyFromEncryptionKey(localKey);
                // Extract the IV from the encryption key
            cipherIV = CipherUtilities.getIVFromEncryptionKey(localKey);
        }
    }
    /**
     * 
     * @param encryptKey
     * @throws java.security.NoSuchAlgorithmException 
     * @throws javax.crypto.NoSuchPaddingException 
     * @throws java.security.InvalidKeyException 
     * @throws java.security.InvalidAlgorithmParameterException 
     * @throws javax.crypto.IllegalBlockSizeException 
     * @throws javax.crypto.BadPaddingException 
     */
    public void initializeEncryption(byte[] encryptKey) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, 
            InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, BadPaddingException{
        initializeEncryption(
                CipherUtilities.getSecretKeyFromEncryptionKey(encryptKey),
                CipherUtilities.getIVFromEncryptionKey(encryptKey));
    }
    /**
     * 
     */
    public void resetEncryption(){
        cipherIV = null;
        secretKey = null;
            // If there was an encryption key set
        if (getRawEncryptionKey() != null)
            clearDropboxToken();
        setRawEncryptionKey(null);
    }
    /**
     * 
     * @return 
     */
    public boolean isEncryptionEnabled(){
        return secretKey != null && cipherIV != null && getSecureRandom()!=null;
    }
    /**
     * 
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    protected byte[] encryptValue(byte[] value) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, 
            InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, BadPaddingException{
            // If the encryption is enabled and the value is not null
        if (isEncryptionEnabled() && value != null)
            return CipherUtilities.encryptByteArray(value, secretKey, cipherIV, 
                    getSecureRandom());
        return value;
    }
    /**
     * 
     * @param value
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    protected byte[] decryptValue(byte[] value) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, 
            InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, BadPaddingException{
            // If the encryption is enabled and the value is not null
        if (isEncryptionEnabled() && value != null)
            return CipherUtilities.decryptByteArray(value, secretKey, cipherIV, 
                    getSecureRandom());
        return value;
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
    public String getComponentName(Component comp){
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
        }   // This maps listIDs to the selected link for that list
        Map<Integer,String> selMap = new HashMap<>();
            // This maps the listIDs to whether the selected link is visible for 
        Map<Integer,Boolean> selVisMap = new HashMap<>();   // that list
            // This maps the listIDs to the first visible index for that list
        Map<Integer,Integer> firstVisMap = new HashMap<>();
            // This maps the listIDs to the last visible index for that list
        Map<Integer,Integer> lastVisMap = new HashMap<>();
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
                // If the key starts with the selected link is visible key
            else if (key.startsWith(SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY))
                keyPrefix = SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY;
                // If the key starts with the first visible index key
            else if (key.startsWith(FIRST_VISIBLE_INDEX_FOR_LIST_KEY))
                keyPrefix = FIRST_VISIBLE_INDEX_FOR_LIST_KEY;
                // If the key starts with the last visible index key
            else if (key.startsWith(LAST_VISIBLE_INDEX_FOR_LIST_KEY))
                keyPrefix = LAST_VISIBLE_INDEX_FOR_LIST_KEY;
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
                        // If this is the selected link is visible key
                    case(SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY):
                        selVisMap.put(type, cProp.getBooleanProperty(key));
                        break;
                        // If this is the first visible index key
                    case(FIRST_VISIBLE_INDEX_FOR_LIST_KEY):
                        firstVisMap.put(type, cProp.getIntProperty(key));
                        break;
                        // If this is the last visible index key
                    case(LAST_VISIBLE_INDEX_FOR_LIST_KEY):
                        lastVisMap.put(type, cProp.getIntProperty(key));
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
        }   // Remove all null values from the selected links
        selMap.values().removeIf((String t) -> t == null);
            // Remove all the null values from whether the links are visible
        selVisMap.values().removeIf((Boolean t) -> t == null);
            // Remove all the null values from the first visible indexes
        firstVisMap.values().removeIf((Integer t) -> t == null);
            // Remove all the null values from the last visible indexes
        lastVisMap.values().removeIf((Integer t) -> t == null);
            // Remove all null values from the current tab listIDs
        selListIDMap.values().removeIf((Integer t) -> t == null);
            // Remove all null values from the current tab indexes
        selListMap.values().removeIf((Integer t) -> t == null);
            // Add all the values for the selected links in the lists
        getSelectedLinkMap().putAll(selMap);
            // Add all the values for the selected links are visible in the lists
        getSelectedLinkIsVisibleMap().putAll(selVisMap);
            // Add all the values for the first visible indexes in the lists
        getFirstVisibleIndexMap().putAll(firstVisMap);
            // Add all the values for the last visible indexes in the lists
        getLastVisibleIndexMap().putAll(lastVisMap);
            // Add all the values for the current tab listIDs 
        getCurrentTabListIDMap().putAll(selListIDMap);
            // Add all the values for the current tab indexes
        getCurrentTabIndexMap().putAll(selListMap);
    }
    /**
     * 
     * @param listData
     * @param keyPrefix
     * @param prop 
     */
    private void addListDataToProperties(Map<Integer,?> listData,String keyPrefix, 
            ConfigProperties prop){
            // Go through the entries in the given map
        for (Map.Entry<Integer, ?> entry : listData.entrySet()){
                // Add the current entry to the properties map
            prop.setProperty(keyPrefix+entry.getKey(), entry.getValue());
        }
    }
    /**
     * 
     * @return 
     */
    public ConfigProperties exportProperties(){
        try{    // This gets the preference node as a properties object
            ConfigProperties prop = getPreferences().toProperties();
                // If the Dropbox node exists
            if (nodeExists(getPreferences(),DROPBOX_PREFERENCE_NODE_NAME)){
                    // Set the value for the Dropbox database file path
                prop.setProperty(DROPBOX_PROPERTY_KEY_PREFIX+DATABASE_FILE_PATH_KEY, 
                        getDropboxDatabaseFileName());
            }   // Add the current tab listID data to the properties
            addListDataToProperties(getCurrentTabListIDMap(),
                    CURRENT_TAB_LIST_ID_KEY+LIST_TYPE_PROPERTY_KEY_SUFFIX,prop);
                // Add the current tab index data to the properties
            addListDataToProperties(getCurrentTabIndexMap(),
                    CURRENT_TAB_INDEX_KEY+LIST_TYPE_PROPERTY_KEY_SUFFIX,prop);
                // Add the selected link data to the properties
            addListDataToProperties(getSelectedLinkMap(),
                    SELECTED_LINK_FOR_LIST_KEY+LIST_ID_PROPERTY_KEY_SUFFIX,prop);
                // Add the selected link visibility data to the properties
            addListDataToProperties(getSelectedLinkIsVisibleMap(),
                    SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY+
                            LIST_ID_PROPERTY_KEY_SUFFIX,prop);
                // Add the first visible index data to the properties
            addListDataToProperties(getFirstVisibleIndexMap(),
                    FIRST_VISIBLE_INDEX_FOR_LIST_KEY+LIST_ID_PROPERTY_KEY_SUFFIX,
                    prop);
                // Add the last visible index data to the properties
            addListDataToProperties(getLastVisibleIndexMap(),
                    LAST_VISIBLE_INDEX_FOR_LIST_KEY+LIST_ID_PROPERTY_KEY_SUFFIX,
                    prop);
                // Remove the encryption key from the properties
            prop.remove(ENCRYPTION_KEY_KEY);
                // Remember to remove any sensitive or unnecessary data from the 
                // properties!
            return prop;
        } catch (BackingStoreException | IllegalStateException ex) {
            return null;
        }
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
     * @param defaultValue
     * @return 
     */
    public Dimension loadComponentSize(Component comp, Dimension defaultValue){
            // Get the size for the component
        Dimension dim = getComponentSize(comp,defaultValue);
            // If the size for the component is not null and the component is 
            // not null
        if (dim != null && comp != null){
                // Get the minimum size for the component. This will be used to 
                // ensure that the component does not go below its minimum size
            Dimension size = comp.getMinimumSize();
                // Make sure the width and height are within range
            size.width = Math.max(dim.width, size.width);
            size.height = Math.max(dim.height, size.height);
                // If the component is a window
            if (comp instanceof Window)
                    // Set the size of the window
                comp.setSize(size);
            else    // Set the preferred size of the component
                comp.setPreferredSize(size);
        }
        return dim;
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Dimension loadComponentSize(Component comp){
        return loadComponentSize(comp,null);
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
     * @param defaultValue
     * @return 
     */
    public Point loadComponentLocation(Component comp, Point defaultValue){
            // Get the location for the component
        Point point = getComponentLocation(comp,defaultValue);
            // If the location for the component is not null and the component 
            // is not null
        if (point != null && comp != null)
                // Set the location for the component
            comp.setLocation(point);
        return point;
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Point loadComponentLocation(Component comp){
        return loadComponentLocation(comp,null);
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
     * @param comp
     * @param defaultValue
     * @return 
     */
    public Rectangle loadComponentBounds(Component comp,Rectangle defaultValue){
            // Get the bounds for the component
        Rectangle rect = getComponentBounds(comp, defaultValue);
            // If the bounds for the component are not null and the component is 
            // not null
        if (rect != null && comp != null){
                // If the component is the program window
            if (comp instanceof LinkManager){
                    // If the component bounds are not set
                if (!isComponentBoundsSet(comp))
                    return rect;
            }   // Get the minimum size for the component
            Dimension min = comp.getMinimumSize();
                // Set the bounds for the component
            comp.setBounds(rect.x, rect.y, 
                        // Make sure the width is within range
                    Math.max(rect.width, min.width), 
                        // Make sure the height is within range
                    Math.max(rect.height, min.height));
        }
        return rect;
    }
    /**
     * 
     * @param comp
     * @return 
     */
    public Rectangle loadComponentBounds(Component comp){
        return loadComponentBounds(comp,null);
    }
    /**
     * 
     * @param node
     * @param key
     * @return 
     */
    private Integer getIntegerPreference(ConfigPreferences node, String key){
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
        return getIntegerPreference(getListTypePreferences(listType), 
                CURRENT_TAB_LIST_ID_KEY);
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
                    return LIST_TYPE_PREFERENCE_NODE_NAME;
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
    public int getCurrentTabIndex(int listType){
        return getListTypePreferences(listType).getInt(CURRENT_TAB_INDEX_KEY,0);
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
                         // If the node contains the current tab index key
                    if (getListTypePreferences(key).containsKey(
                            CURRENT_TAB_INDEX_KEY))
                        return getCurrentTabIndex(key);
                    return null;
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setCurrentTabIndex(key,value);
                }
                @Override
                protected String getPrefixForNodes() {
                    return LIST_TYPE_PREFERENCE_NODE_NAME;
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
            // The listID of the current tab
        Integer listID = null;
            // The index of the current tab
        Integer index = null;
            // If the given panel is not null
        if (tabsPanel != null){
            listID = tabsPanel.getSelectedListID();
                // If the tabs panel has nothing selected, use null as the 
                // index. Otherwise, use the selected index
            index = (tabsPanel.isSelectionEmpty()) ? null :
                    tabsPanel.getSelectedIndex();
        }
        setCurrentTabListID(listType,listID);
        setCurrentTabIndex(listType, index);
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
                    return LIST_ID_PREFERENCE_NODE_NAME;
                }
            };
        }
        return selLinkMap;
    }
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setSelectedLinkIsVisible(int listID, Boolean value){
        getListPreferences(listID).putObject(
                SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY, value);
    }
    /**
     * 
     * @param listID
     * @return 
     */
    public boolean getSelectedLinkIsVisible(int listID){
        return getListPreferences(listID).getBoolean(
                SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY, false);
    }
    /**
     * 
     * @return 
     */
    public Map<Integer,Boolean> getSelectedLinkIsVisibleMap(){
        if (selLinkVisMap == null){
            selLinkVisMap = new ListConfigDataMap<>(){
                @Override
                protected Boolean getValue(int key) {
                        // If the node contains the selected link is visible key
                    if (getListPreferences(key).containsKey(
                            SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY))
                        return getSelectedLinkIsVisible(key);
                    return null;
                }
                @Override
                protected void putValue(int key, Boolean value) {
                    setSelectedLinkIsVisible(key,value);
                }
                @Override
                protected String getPrefixForNodes() {
                    return LIST_ID_PREFERENCE_NODE_NAME;
                }
            };
        }
        return selLinkVisMap;
    }
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setFirstVisibleIndex(int listID, Integer value){
        getListPreferences(listID).putObject(FIRST_VISIBLE_INDEX_FOR_LIST_KEY,
                value);
    }
    /**
     * 
     * @param listID
     * @return 
     */
    public Integer getFirstVisibleIndex(int listID){
        return getIntegerPreference(getListPreferences(listID), 
                FIRST_VISIBLE_INDEX_FOR_LIST_KEY);
    }
    /**
     * 
     * @return 
     */
    public Map<Integer, Integer> getFirstVisibleIndexMap(){
        if (firstVisIndexMap == null){
            firstVisIndexMap = new ListConfigDataMap<>(){
                @Override
                protected Integer getValue(int key) {
                    return getFirstVisibleIndex(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setFirstVisibleIndex(key,value);
                }
                @Override
                protected String getPrefixForNodes() {
                    return LIST_ID_PREFERENCE_NODE_NAME;
                }
            };
        }
        return firstVisIndexMap;
    }
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setLastVisibleIndex(int listID, Integer value){
        getListPreferences(listID).putObject(LAST_VISIBLE_INDEX_FOR_LIST_KEY,
                value);
    }
    /**
     * 
     * @param listID
     * @return 
     */
    public Integer getLastVisibleIndex(int listID){
        return getIntegerPreference(getListPreferences(listID), 
                LAST_VISIBLE_INDEX_FOR_LIST_KEY);
    }
    /**
     * 
     * @return 
     */
    public Map<Integer, Integer> getLastVisibleIndexMap(){
        if (lastVisIndexMap == null){
            lastVisIndexMap = new ListConfigDataMap<>(){
                @Override
                protected Integer getValue(int key) {
                    return getLastVisibleIndex(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setLastVisibleIndex(key,value);
                }
                @Override
                protected String getPrefixForNodes() {
                    return LIST_ID_PREFERENCE_NODE_NAME;
                }
            };
        }
        return lastVisIndexMap;
    }
    /**
     * 
     * @param listID
     * @param panel 
     */
    public void setVisibleSection(int listID, LinksListPanel panel){
            // This will get the first visible index
        Integer firstVisIndex = null;
            // This will get the last visible index
        Integer lastVisIndex = null;
            // This will get whether the selected index is visible
        Boolean isSelVis = null;
            // If the panel is not null
        if (panel != null){
                // Get the first visible index for the list
            firstVisIndex = panel.getList().getFirstVisibleIndex();
                // If the first visible index is negative
            if (firstVisIndex < 0)
                firstVisIndex = null;
                // Get the last visible index for the list
            lastVisIndex = panel.getList().getLastVisibleIndex();
                // If the last visible index is negative
            if (lastVisIndex < 0)
                lastVisIndex = null;
                // If the panel's selection is not empty
            if (!panel.isSelectionEmpty())
                    // Get whether the selected indes is visible
                isSelVis = panel.isIndexVisible(panel.getSelectedIndex());
        }   // Set the first visible index for the list
        setFirstVisibleIndex(listID,firstVisIndex);
            // Set the last visible index for the list
        setLastVisibleIndex(listID,lastVisIndex);
            // Set whether the selected link is visible
        setSelectedLinkIsVisible(listID,isSelVis);
    }
    /**
     * 
     * @param panel 
     */
    public void setVisibleSection(LinksListPanel panel){
            // If the panel is not null and has a non-null listID
        if (panel != null && panel.getListID() != null)
            setVisibleSection(panel.getListID(),panel);
    }
    /**
     * 
     * @param listID 
     * @return  
     */
    public boolean removeListPreferences(int listID){
            // Remove the node from the cache if there is one
        Preferences node = listIDNodeMap.remove(listID);
            // If there is no node cached for the listID
        if (node == null){
                // If there is a node for the list with the given listID
            if (nodeExists(getPreferences(),LIST_TYPE_PREFERENCE_NODE_NAME+"="+listID))
                    // Get that node
                node = getPreferences().node(LIST_TYPE_PREFERENCE_NODE_NAME+"="+listID);
        }   // If there is a list preference node for the given listID
        if (node != null)
                // Remove the node
            removeNode(node);
        return node != null;
    }
    /**
     * 
     * @param listIDs
     * @return 
     */
    public boolean removeListPreferences(Collection<Integer> listIDs){
        boolean changed = false;
        for (Integer listID : listIDs){
            if (listID != null){
                boolean removed = removeListPreferences(listID);
                changed |= removed;
            }
        }
        return changed;
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
     * @param value 
     */
    public void setDropboxChunkSizeMultiplier(Integer value){
        getDropboxPreferences().putObject(CHUNK_SIZE_MULTIPLIER_KEY, value);
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public int getDropboxChunkSizeMultiplier(int defaultValue){
        return getDropboxPreferences().getInt(CHUNK_SIZE_MULTIPLIER_KEY, 
                defaultValue);
    }
    /**
     * 
     * @todo Add encryption of the Dropbox tokens.
     * 
     * @param key
     * @param token 
     */
    private void setDropboxToken(String key, String token){
            // This will be the value that is stored
        Object value = token;
            // If the encryption is enabled and the token is not null
        if (isEncryptionEnabled() && token != null){
            try{    // Encrypt the token
                value = encryptValue(token.getBytes());
            } catch (GeneralSecurityException ex){
//                throw new UncheckedSecurityException(ex);
            }
        }   // Get the Dropbox preference node and put the token in it
        getDropboxPreferences().putObject(key, value);
    }
    /**
     * 
     * @todo Add decryption of the Dropbox tokens.
     * 
     * @param key
     * @return 
     */
    private String getDropboxToken(String key){
            // If the encryption is enabled
        if (isEncryptionEnabled()){
            try{    // Decrypt the token
                byte[] arr = decryptValue(getDropboxPreferences().getByteArray(key, null));
                    // If there was an encrypted token
                if (arr != null)
                    return new String(arr);
            } catch (GeneralSecurityException ex){
//                throw new UncheckedSecurityException(ex);
            }
        }
        return getDropboxPreferences().get(key, null);
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
        getDropboxPreferences().putObject(DROPBOX_TOKEN_EXPIRATION_KEY,time);
    }
    /**
     * 
     * @return 
     */
    public Long getDropboxTokenExpiresAt(){
            // Get the Dropbox preference node
        ConfigPreferences node = getDropboxPreferences();
            // Get whether the node contains the dropbox token expire time
        if (node.containsKey(DROPBOX_TOKEN_EXPIRATION_KEY))
            return node.getLong(DROPBOX_TOKEN_EXPIRATION_KEY, 0);
        return null;
    }
    /**
     * 
     */
    public void clearDropboxToken(){
            // Set the Dropbox access token to null, clearing it
        setDropboxAccessToken(null);
            // Set the Dropbox refresh token to null, clearing it
        setDropboxRefreshToken(null);
            // Set the Dropbox token expiration time to null, clearing it
        setDropboxTokenExpiresAt(null);
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
                                    getPrefixForNodes().length()+1));
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
