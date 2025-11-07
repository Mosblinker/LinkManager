/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.security.*;
import java.util.*;
import java.util.logging.Level;
import java.util.prefs.*;
import javax.crypto.*;
import javax.crypto.spec.*;
import static manager.DatabaseSyncMode.DROPBOX;
import manager.config.*;
import manager.dropbox.DropboxLinkUtils;
import manager.links.*;
import manager.security.*;
import org.sqlite.SQLiteConfig;
import utils.SwingExtendedUtilities;

/**
 * This is a class that is used to store and manage the configuration for 
 * LinkManager. All properties not stored in SQLiteConfig are stored internally 
 * as Strings.
 * @author Milo Steier
 */
public class LinkManagerConfig extends AbstractLinksListSettings{
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
    
    public static final String HIDDEN_FILES_ARE_SHOWN_KEY = "HiddenFilesAreShown";
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
     * 
     */
    public static final String VISIBLE_RECTANGLE_FOR_LIST_KEY = 
            "VisibleRect";
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
     * This is the configuration key for the UUID for the user of the program.
     */
    public static final String USER_ID_KEY = "UserID";
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
     * This is an array that contains the suffixes for the keys in properties 
     * that deal with list stuff.
     */
    private static final String[] LIST_PROPERTY_KEY_SUFFIXES = {
        LIST_TYPE_PROPERTY_KEY_SUFFIX,
        LIST_ID_PROPERTY_KEY_SUFFIX
    };
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
        LAST_VISIBLE_INDEX_FOR_LIST_KEY,
        VISIBLE_RECTANGLE_FOR_LIST_KEY
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
     * 
     */
    public static final String CHECK_FOR_UPDATES_AT_START_KEY = "CheckForUpdatesAtStartup";
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
     * This is the preference node that stores the settings and tokens for 
     * Dropbox.
     */
    protected ConfigPreferences dropboxNode = null;
    /**
     * This is used to handle the list type preference nodes.
     */
    protected final ListConfigNodeParent listTypeNodes;
    /**
     * This is used to handle the listID preference nodes.
     */
    protected final ListConfigNodeParent listIDNodes;
    /**
     * This is the ID for the program.
     */
    private UUID programID = null;
    /**
     * This is a utilities object for encrypting and decrypting values.
     */
    protected CipherUtils cipherUtils = null;
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
        listTypeNodes = new ListConfigNodeParent(){
            @Override
            public String getParentPath() {
                return LIST_TYPE_PREFERENCE_NODE_NAME;
            }
        };
        listIDNodes = new ListConfigNodeParent(){
            @Override
            public String getParentPath() {
                return LIST_ID_PREFERENCE_NODE_NAME;
            }
        };;
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
        this.cipherUtils = new CipherUtils(linkConfig.cipherUtils);
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
            dropboxNode = getLocalChild(DROPBOX_PREFERENCE_NODE_NAME);
        return dropboxNode;
    }
    /**
     * 
     * @param type The list type
     * @return 
     */
    public ConfigPreferences getListTypePreferences(int type){
        return listTypeNodes.getNode(type);
    }
    /**
     * 
     * @return 
     */
    public Set<Integer> getListTypes(){
        return Collections.unmodifiableSet(listTypeNodes.getKeys());
    }
    /**
     * 
     * @param listID The list ID
     * @return 
     */
    public ConfigPreferences getListPreferences(int listID){
        return listIDNodes.getNode(listID);
    }
    @Override
    public Set<Integer> getListIDs(){
        return Collections.unmodifiableSet(listIDNodes.getKeys());
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
     * 
     * @param path
     * @param defaults
     * @return 
     */
    protected ConfigPreferences getLocalChild(String path, Properties defaults){
        return getPreferences().node(path, defaults);
    }
    /**
     * 
     * @param path
     * @return 
     */
    protected ConfigPreferences getLocalChild(String path){
        return getLocalChild(path,null);
    }
    /**
     * 
     */
    protected void updatePreferences(){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "updatePreferences");
        try{    // Get the names of the child nodes in the parent preference 
            String[] childNodes = getPreferences().childrenNames();     // node
                // Go through the names of the child nodes
            for (String child : childNodes){
                    // If the child's name is not null
                if (child != null){
                        // Get the index of the equals sign in the child's name
                    int index = child.indexOf("=");
                        // If the child has an equals sign in its name
                    if (index >= 0){
                        try{    // Parse the number at the end of the child's 
                                // name
                            int value = Integer.parseInt(child.substring(index+1));
                                // Get the prefix for the child name
                            String prefix = child.substring(0,index);
                                // This is the parent object for the node type
                            ListConfigNodeParent parent;
                                // Determine how to treat this node
                            switch(prefix){
                                    // If it's an old list type node
                                case(LIST_TYPE_PREFERENCE_NODE_NAME):
                                    parent = listTypeNodes;
                                    break;
                                    // If it's an old listID node
                                case(LIST_ID_PREFERENCE_NODE_NAME):
                                    parent = listIDNodes;
                                    break;
                                default:
                                    continue;
                            }   // Get the original node
                            Preferences oldNode = getLocalChild(child);
                                // Get the new node
                            ConfigPreferences node = parent.getNode(value);
                            try{    // Go through the keys in the old node
                                for (String key : oldNode.keys()){
                                        // If the new node doesn't have a value 
                                        // set for the current key
                                    if (!node.isKeySet(key))
                                            // Put the value from the old node 
                                            // into the new node
                                        node.put(key, oldNode.get(key, ""));
                                }
                            } catch (BackingStoreException ex) {
                                LinkManager.getLogger().log(Level.WARNING, 
                                        "Failed to get keys for a node", ex);
                            }
                                // Remove the old node
                            removeNode(oldNode);
                        } catch (NumberFormatException | 
                                IllegalStateException ex) {
                            LinkManager.getLogger().log(Level.WARNING,
                                    "Unable to get old node", ex);
                        }
                    }
                }
            }
        } catch (BackingStoreException ex) {
            LinkManager.getLogger().log(Level.WARNING, 
                    "Backing store exception thrown", ex);
        }
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "updatePreferences");
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
            // Get the parent node for the list type preference nodes and clear 
            // the node cache
        listTypeNodes.setParentNode();
            // Get the parent node for the listID preference nodes and clear the 
            // node cache
        listIDNodes.setParentNode();
            // Reset the Dropbox node to null
        dropboxNode = null;
            // Update the values in the preference nodes
        updatePreferences();
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
    public CipherUtils getCipher(){
        return cipherUtils;
    }
    /**
     * 
     * @param utils 
     */
    public void setCipher(CipherUtils utils){
        this.cipherUtils = utils;
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
     * @param userUtils
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    protected void loadEncryptionKey(CipherUtils userUtils) throws 
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
                // Generate the encryption key for the cipher
            getCipher().generateEncryptionKey();
                // Encrypt the encryption key and store it
            setRawEncryptionKey(userUtils.encryptByteArray(
                    getCipher().getEncryptionKey()));
                // Set the Dropbox access token, which should encrypt it now
            setDropboxAccessToken(accessToken);
                // Set the Dropbox refresh token, which should encrypt it now
            setDropboxRefreshToken(refreshToken);
        } else {    // Decrypt the encryption key
            localKey = userUtils.decryptByteArray(localKey);
                // Set the encryption key for the cipher
            getCipher().setEncryptionKey(localKey);
        }
    }
    /**
     * 
     * @param userUtils
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    public void initEncryption(CipherUtils userUtils) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, 
            InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, BadPaddingException{
            // Set the CipherUtils to use a copy of the user CipherUtils
        setCipher(new CipherUtils(userUtils));
            // If the user CipherUtils did not have a key generator
        if (getCipher().getKeyGenerator() == null)
            getCipher().setKeyGenerator();
            // Initialize the encryption
        loadEncryptionKey(userUtils);
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
    public void initEncryption(SecretKey key, IvParameterSpec iv) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, 
            InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, BadPaddingException{
            // Initialize the encryption using the given key and IV parameters
        loadEncryptionKey(new CipherUtils(getCipher()).setKey(key)
                .setIV(iv));
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
    public void initEncryption(byte[] encryptKey) throws 
            NoSuchAlgorithmException, NoSuchPaddingException, 
            InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, BadPaddingException{
            // Initialize the encryption using the given encryption key
        loadEncryptionKey(new CipherUtils(getCipher())
                .setEncryptionKey(encryptKey));
    }
    /**
     * 
     */
    public void resetEncryption(){
            // If the CipherUtils object is not null
        if (getCipher() != null)
                // Clear the encryption key
            getCipher().clearEncryptionKey();
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
        return getCipher() != null && getCipher().isEncryptionKeySet();
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
            return getCipher().encryptByteArray(value);
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
            return getCipher().decryptByteArray(value);
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
        b = cProp.getBooleanProperty(HIDDEN_FILES_ARE_SHOWN_KEY);
        if (b != null)
            setHiddenFilesAreShown(b);
            // Get the value for the Dropbox database file path from the 
            // properties
        str = cProp.getProperty(DROPBOX_PROPERTY_KEY_PREFIX+DATABASE_FILE_PATH_KEY);    
            // If the properties has the Dropbox database file path
        if (str != null)
                // Set the Dropbox database file path from the properties
            setDropboxDatabaseFileName(str);
        b = cProp.getBooleanProperty(CHECK_FOR_UPDATES_AT_START_KEY);
        if (b != null)
            setCheckForUpdateAtStartup(b);
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
            // This maps the listIDs to the visible rectangle for that list
        Map<Integer,Rectangle> visRectMap = new HashMap<>();
            // This gets a set of keys for the properties that deal with lists
        Set<String> listKeys = new HashSet<>(cProp.stringPropertyNames());
            // Remove any null keys and keys that aren't prefixed keys for the 
            // selection
        listKeys.removeIf((String t) -> {
            return t == null || !isPrefixedListKey(t);
        });
            // Go through the property keys that deal with lists
        for (String key : listKeys){
                // The prefix for the current key
            String keyPrefix = null;
                // The suffix for the property version of the key
            String keySuffix = null;
                // Go through the possible list property key suffixes
            for (String suffix : LIST_PROPERTY_KEY_SUFFIXES){
                    // Get the index of the start of the current suffix if 
                    // present in the current key
                int index = key.lastIndexOf(suffix);
                    // If the suffix is present in the current key
                if (index >= 0){
                        // This is the suffix for the key
                    keySuffix = suffix;
                        // Get the prefix for the key
                    keyPrefix = key.substring(0, index);
                    break;
                }
            }   // If this key does not have a matching key prefix or suffix
            if (keySuffix == null || keyPrefix == null)
                continue;   // Skip this key
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
                        break;
                        // If this is the visible rectangle index key
                    case(VISIBLE_RECTANGLE_FOR_LIST_KEY):
                        visRectMap.put(type, cProp.getRectangleProperty(key));
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
            // Remove all null values from the visible rectangles
        visRectMap.values().removeIf((Rectangle t) -> t == null);
            // Add all the values for the selected links in the lists
        getSelectedLinkMap().putAll(selMap);
            // Add all the values for the selected links are visible in the lists
        getSelectedLinkVisibleMap().putAll(selVisMap);
            // Add all the values for the first visible indexes in the lists
        getFirstVisibleIndexMap().putAll(firstVisMap);
            // Add all the values for the last visible indexes in the lists
        getLastVisibleIndexMap().putAll(lastVisMap);
            // Add all the values for the current tab listIDs 
        getCurrentTabListIDMap().putAll(selListIDMap);
            // Add all the values for the current tab indexes
        getCurrentTabIndexMap().putAll(selListMap);
            // Add all the values for the visible rectangles for the lists
        getVisibleRectMap().putAll(visRectMap);
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
            addListDataToProperties(getSelectedLinkVisibleMap(),
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
                // Add the visible rectangle data to the properties
            addListDataToProperties(getVisibleRectMap(),
                    VISIBLE_RECTANGLE_FOR_LIST_KEY+LIST_ID_PROPERTY_KEY_SUFFIX,
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
     * @param value 
     */
    public void setHiddenFilesAreShown(Boolean value){
        getPreferences().putObject(HIDDEN_FILES_ARE_SHOWN_KEY, value);
    }
    /**
     * 
     * @return 
     */
    public boolean getHiddenFilesAreShown(){
        return getPreferences().getBoolean(HIDDEN_FILES_ARE_SHOWN_KEY,true);
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
        if (dim != null && comp != null)
                // Set the size of the component
            SwingExtendedUtilities.setComponentSize(comp, dim);
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
            }   // Set the bounds for the component
            SwingExtendedUtilities.setComponentBounds(comp, rect);
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
            currTabIDMap = new ListConfigDataMapImpl<>(){
                @Override
                protected Integer getValue(int key) {
                    return getCurrentTabListID(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setCurrentTabListID(key,value);
                }
                @Override
                protected ListConfigNodeParent getNodes() {
                    return listTypeNodes;
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
            currTabIndexMap = new ListConfigDataMapImpl<>(){
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
                protected ListConfigNodeParent getNodes() {
                    return listTypeNodes;
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
    @Override
    public void setSelectedLink(int listID, String value){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "setSelectedLink", new Object[]{listID,value});
        getListPreferences(listID).put(SELECTED_LINK_FOR_LIST_KEY, value);
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "setSelectedLink");
    }
    @Override
    public String getSelectedLink(int listID){
        return getListPreferences(listID).get(SELECTED_LINK_FOR_LIST_KEY, null);
    }
    @Override
    public void setSelectedLinkVisible(int listID, Boolean value){
        getListPreferences(listID).putObject(
                SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY, value);
    }
    @Override
    public Boolean isSelectedLinkVisible(int listID){
        if (getListPreferences(listID).containsKey(
                            SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY))
            return getListPreferences(listID).getBoolean(
                    SELECTED_LINK_IS_VISIBLE_FOR_LIST_KEY, false);
        return null;
    }
    @Override
    public void setFirstVisibleIndex(int listID, Integer value){
        getListPreferences(listID).putObject(FIRST_VISIBLE_INDEX_FOR_LIST_KEY,
                value);
    }
    @Override
    public Integer getFirstVisibleIndex(int listID){
        return getIntegerPreference(getListPreferences(listID), 
                FIRST_VISIBLE_INDEX_FOR_LIST_KEY);
    }
    @Override
    public void setLastVisibleIndex(int listID, Integer value){
        getListPreferences(listID).putObject(LAST_VISIBLE_INDEX_FOR_LIST_KEY,
                value);
    }
    @Override
    public Integer getLastVisibleIndex(int listID){
        return getIntegerPreference(getListPreferences(listID), 
                LAST_VISIBLE_INDEX_FOR_LIST_KEY);
    }
    @Override
    public void setVisibleRect(int listID, Rectangle value){
        getListPreferences(listID).putObject(VISIBLE_RECTANGLE_FOR_LIST_KEY, 
                value);
    }
    @Override
    public Rectangle getVisibleRect(int listID, Rectangle defaultValue){
        return getListPreferences(listID).getRectangle(
                VISIBLE_RECTANGLE_FOR_LIST_KEY, defaultValue);
    }
    @Override
    public boolean removeListSettings(int listID){
        return listIDNodes.removeNode(listID);
    }
    /**
     * This returns the user ID set for this configuration.
     * @return The user ID.
     */
    public UUID getUserID(){
        return getSharedPreferences().getUUID(USER_ID_KEY, null);
    }
    /**
     * This sets the user ID for this configuration.
     * @param id The new user ID.
     * @throws NullPointerException If the user ID is null.
     */
    public void setUserID(UUID id){
            // Check if the user ID is null
        Objects.requireNonNull(id, "User ID cannot be null");
        getSharedPreferences().putObject(USER_ID_KEY, id);
    }
    /**
     * This sets the user ID to be a random {@code UUID}.
     * @return The {@code UUID} used as the user ID.
     * @see UUID#randomUUID() 
     */
    public UUID setRandomUserID(){
            // Generate a random UUID
        UUID id = UUID.randomUUID();
            // Set the user ID to the generated UUID
        setUserID(id);
        return id;
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
                LinkManager.getLogger().log(Level.WARNING, 
                        "Failed to encrypt value", ex);
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
                LinkManager.getLogger().log(Level.WARNING, 
                        "Failed to decrypt value", ex);
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
     * @param mode
     * @return 
     */
    public String getDatabaseFileSyncPath(DatabaseSyncMode mode){
        if (mode != null){
            switch(mode){
                case DROPBOX:
                    return getDropboxDatabaseFileName();
            }
        }
        return null;
    }
    /**
     * 
     * @param mode
     * @param value 
     */
    public void setDatabaseFileSyncPath(DatabaseSyncMode mode, String value){
        if (mode != null){
            switch(mode){
                case DROPBOX:
                    setDropboxDatabaseFileName(value);
            }
        }
    }
    /**
     * 
     * @param defaultValue
     * @return 
     */
    public boolean getCheckForUpdateAtStartup(boolean defaultValue){
        return getPreferences().getBoolean(CHECK_FOR_UPDATES_AT_START_KEY, defaultValue);
    }
    /**
     * 
     * @return 
     */
    public boolean getCheckForUpdateAtStartup(){
        return getCheckForUpdateAtStartup(true);
    }
    /**
     * 
     * @param value 
     */
    public void setCheckForUpdateAtStartup(boolean value){
        getPreferences().putBoolean(CHECK_FOR_UPDATES_AT_START_KEY, value);
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
    private abstract class ListConfigDataMapImpl<V> extends ListConfigDataMap<V>{
        /**
         * 
         * @return 
         */
        protected abstract ListConfigNodeParent getNodes();
        @Override
        protected Set<Integer> getKeys(){
            return removeUnusedKeys(getNodes().getKeys());
        }
    }
    /**
     * This is a class that handles dealing with nodes used for list 
     * configuration data.
     */
    protected abstract class ListConfigNodeParent{
        /**
         * This is the parent preference node for the the list configuration 
         * data preference nodes.
         */
        protected ConfigPreferences parentNode = null;
        /**
         * This is a map that caches the list configuration data preference 
         * nodes.
         */
        protected Map<Integer, ConfigPreferences> nodeMap = new HashMap<>();
        /**
         * 
         * @return 
         */
        public Map<Integer, ConfigPreferences> getNodeCache(){
            return nodeMap;
        }
        /**
         * 
         * @return 
         */
        public ConfigPreferences getParentNode(){
            return parentNode;
        }
        /**
         * 
         * @param node 
         */
        public void setParentNode(ConfigPreferences node){
            this.parentNode = node;
                // Clear the listID preference node cache
            nodeMap.clear();
        }
        /**
         * 
         * @param path 
         */
        public void setParentNode(String path){
            setParentNode(getLocalChild(path));
        }
        /**
         * 
         */
        public void setParentNode(){
            setParentNode(getParentPath());
        }
        /**
         * 
         * @return 
         */
        public abstract String getParentPath();
        /**
         * 
         * @param key
         * @return 
         */
        protected ConfigPreferences node(int key){
            return getParentNode().node(Integer.toString(key));
        }
        /**
         * 
         * @param key
         * @return 
         */
        public boolean nodeExists(int key){
            return LinkManagerConfig.this.nodeExists(getParentNode(),
                    Integer.toString(key));
        }
        /**
         * 
         * @param key
         * @return 
         */
        public ConfigPreferences getNode(int key){
                // Check the preference node cache for the node
            ConfigPreferences node = nodeMap.get(key);
                // If the cache does not have the preference node
            if (node == null){
                    // Get the node
                node = node(key);
                    // Cache the node
                nodeMap.put(key, node);
            }
            return node;
        }
        /**
         * 
         * @param key
         * @return 
         */
        public boolean removeNode(int key){
                // Remove the node from the cache if there is one
            Preferences node = nodeMap.remove(key);
                // If there is no node cached for the key
            if (node == null){
                    // If there is a node for the list with the given listID
                if (nodeExists(key))
                        // Get that node
                    node = node(key);
            }   // If there is a list preference node for the given key
            if (node != null)
                    // Remove the node
                LinkManagerConfig.this.removeNode(node);
            return node != null;
        }
        /**
         * 
         * @return 
         */
        public Set<Integer> getKeys(){
                // This will get the keys in this map
            Set<Integer> keys = new TreeSet<>();
            try{    // Get the names of the child nodes in the parent preference 
                String[] childNodes = getParentNode().childrenNames(); // node
                    // Go through the names of the child nodes
                for (String child : childNodes){
                        // If the child's name is not null
                    if (child != null){
                        try{    // Parse the number
                            keys.add(Integer.valueOf(child));
                        } catch (NumberFormatException ex) {}
                    }
                }
            } catch (BackingStoreException ex) {}
            return keys;
        }
    }
}
