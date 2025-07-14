/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package manager;

import com.dropbox.core.*;
import com.dropbox.core.json.JsonReader;
import com.dropbox.core.oauth.*;
import com.dropbox.core.util.IOUtil.ProgressListener;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.*;
import com.dropbox.core.v2.users.*;
import components.*;
import components.debug.DebugCapable;
import components.disable.DisableGUIInput;
import static components.disable.DisableInput.beep;
import components.progress.JProgressDisplayMenu;
import components.text.CompoundUndoManager;
import components.text.action.commands.*;
import files.FilesExtended;
import static files.FilesExtended.generateExtensionFilter;
import files.extensions.ConfigExtensions;
import static files.extensions.TextDocumentExtensions.TEXT_FILTER;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.security.*;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import java.util.prefs.*;
import javax.crypto.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.filechooser.*;
import javax.swing.table.*;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import javax.swing.tree.*;
import manager.config.*;
import manager.database.*;
import static manager.database.LinkDatabaseConnection.*;
import manager.dropbox.*;
import manager.links.*;
import manager.painters.LinkManagerIconPainter;
import manager.renderer.*;
import manager.security.*;
import manager.timermenu.*;
import measure.format.binary.ByteUnitFormat;
import org.sqlite.*;
import org.sqlite.core.*;
import sql.*;

/**
 * This is a program to manage lists of links.
 * @author Milo Steier
 */
public class LinkManager extends JFrame implements DisableGUIInput,DebugCapable{
    /**
     * This is the name of the program.
     */
    public static final String PROGRAM_NAME = "Link Mananger";
    /**
     * This is the version of the program.
     */
    public static final String PROGRAM_VERSION = "0.11.0";
    /**
     * This is the internal name for the program.
     */
    protected static final String INTERNAL_PROGRAM_NAME = "LinkManager";
    /**
     * This is the pattern for the file handler to use for the log files of this 
     * program.
     */
    private static final String PROGRAM_LOG_PATTERN = 
            "%h/.mosblinker/logs/"+INTERNAL_PROGRAM_NAME+"-%u.%g.log";
    /**
     * This is an array containing the widths and heights for the icon images 
     * for this program. The icon images are generated on the fly.
     */
    private static final int[] ICON_SIZES = {16, 24, 32, 48, 64, 96, 128, 256, 512};
    /**
     * This is the client identifier to pass to Dropbox for this program. This 
     * contains the name of this program and the version.
     */
    private static final String DROPBOX_CLIENT_ID = INTERNAL_PROGRAM_NAME+"/"+
            PROGRAM_VERSION;
    /**
     * This is a list containing the default names for the lists of links. <p>
     * 0: Links
     */
    private static final String[] DEFAULT_LIST_NAMES = new String[]{
        "Links"
    };
    /**
     * This is the argument for specifying a program ID for the program from the 
     * arguments.
     */
    public static final String PROGRAM_ID_ARGUMENT = "-programID=";
    /**
     * This is the argument for specifying a configuration file for the program 
     * from the arguments.
     */
    public static final String CONFIG_FILE_ARGUMENT = "-config=";
    /**
     * This holds the abstract path to the default database file storing the 
     * tables containing the links.
     */
    public static final String LINK_DATABASE_FILE = "LinkManager.db";
    /**
     * This is the name of the preference node used to store the settings for 
     * this program.
     */
    private static final String PREFERENCE_NODE_NAME = "milo/link/"+INTERNAL_PROGRAM_NAME;
    /**
     * This is the name of the file used to store the configuration.
     */
    public static final String CONFIG_FILE = "LinkManager.cfg";
    /**
     * This is the name of the file used to store the dropbox API keys.
     */
    public static final String DROPBOX_API_KEY_FILE = "LinkManagerDropboxKey.json";
    /**
     * This is the header  for the general settings in the configuration 
     * file.
     */
    private static final String GENERAL_CONFIG_HEADER = "["+INTERNAL_PROGRAM_NAME+" Config]";
    /**
     * This is the configuration key for the program ID. This is used to
     * determine what preference node to use for the program.
     */
    private static final String PROGRAM_ID_KEY = "ProgramID";
    /**
     * This is the configuration key for the encryption key used to encrypt the 
     * encryption keys for all the users of the program with the same program 
     * ID.
     */
    private static final String USER_ENCRYPTION_KEY_KEY = "UserEncryptionKey";
    
    private static final String LIST_MANAGER_NAME = "ListManager";
    
    private static final String LIST_TABS_MANAGER_NAME = "ListTabsManager";
    
    private static final String ADD_LINKS_PANEL_NAME = "AddLinksPanel";
    
    private static final String COPY_OR_MOVE_LINKS_PANEL_NAME = 
            "CopyOrMoveLinksPanel";
    
    private static final String OPEN_FILE_CHOOSER_NAME = "OpenFileChooser";
    
    private static final String SAVE_FILE_CHOOSER_NAME = "SaveFileChooser";
    
    private static final String CONFIG_FILE_CHOOSER_NAME = "ConfigFileChooser";
    
    private static final String EXPORT_FILE_CHOOSER_NAME = "ExportFileChooser";
    
    private static final String DATABASE_FILE_CHOOSER_NAME = 
            "DatabaseFileChooser";
    
    private static final String DATABASE_LOCATION_DIALOG_NAME = 
            "SetDatabaseLocation";
    
    private static final String SEARCH_DIALOG_NAME = "SearchDialog";
    
    private static final String LINK_MANAGER_NAME = INTERNAL_PROGRAM_NAME;
    /**
     * This is a collection storing the required Dropbox scope for the program. 
     * If this is null, then the program does not specify the scope it requires. 
     * If not, then this set should be immutable.
     */
    private static final Collection<String> DROPBOX_SCOPE_PERMISSIONS = null;
    /**
     * This is the file extension for log files.
     */
    public static final String LOG_FILE_EXTENSION = "log";
    /**
     * This contains the file filter for log files.
     */
    public static final FileNameExtensionFilter LOG_FILE_FILTER = 
            generateExtensionFilter("Log File",LOG_FILE_EXTENSION);
    /**
     * This contains the file filter for Internet Shortcut files.
     */
    public static final FileNameExtensionFilter SHORTCUT_FILE_FILTER = 
            generateExtensionFilter("Internet Shortcuts","url");
    /**
     * The header used in an Internet Shortcut file to indicate where the actual 
     * shortcut is located.
     */
    public static final String SHORTCUT_HEADER = "[InternetShortcut]";
    /**
     * The flag used in an Internet Shortcut file to indicate the URL.
     */
    public static final String URL_FLAG = "URL=";
    /**
     * This contains the file filter for database files.
     */
    public static final FileNameExtensionFilter DATABASE_FILE_FILTER = 
            generateExtensionFilter("Data Base File","db");
    /**
     * This is the file extension for backup copy files generated by this 
     * program.
     */
    public static final String BACKUP_FILE_EXTENSION = "bak";
    /**
     * This is the action command for exiting the program.
     */
    private static final String EXIT_COMMAND = "Exit";
    /**
     * This is the action command for toggling whether hidden lists are to be 
     * shown.
     */
    private static final String HIDDEN_LISTS_TOGGLE_COMMAND="HiddenListsToggle";
    /**
     * This is the action command key for saving a list to a file.
     */
    private static final String SAVE_TO_FILE_ACTION_KEY = "SaveToFile";
    /**
     * This is the action command key for adding links to a list from a file.
     */
    private static final String ADD_FROM_FILE_ACTION_KEY = "AddFromFile";
    /**
     * This is the action command key for adding links to a list from a text 
     * area.
     */
    private static final String ADD_FROM_TEXT_AREA_ACTION_KEY = "AddFromList";
    /**
     * This is the action command key for copying links from the selected list 
     * and adding them to another list.
     */
    private static final String COPY_TO_LIST_ACTION_KEY = "CopyToList";
    /**
     * This is the action command key for moving links from the selected list 
     * and adding them to another list.
     */
    private static final String MOVE_TO_LIST_ACTION_KEY = "MoveToList";
    /**
     * This is the action command key for removing the links from a list that 
     * are shared between it and the currently selected list.
     */
    private static final String REMOVE_FROM_LIST_ACTION_KEY = "RemoveFromList";
    
    private static final String REMOVE_OTHER_LISTS_ACTION_KEY = 
            "RemoveOtherLists";
    
    private static final String REMOVE_FROM_HIDDEN_LISTS_ACTION_KEY = 
            "RemoveFromPrivateList";
    
    private static final String REMOVE_OTHER_HIDDEN_LISTS_ACTION_KEY = 
            "RemoveOtherPrivateLists";
    /**
     * This is the action command key for making a list hidden.
     */
    private static final String HIDE_LIST_ACTION_KEY = "MakeListPrivate";
    /**
     * This is the action command key for making a list read only.
     */
    private static final String MAKE_LIST_READ_ONLY_ACTION_KEY = "MakeListReadOnly";
    /**
     * This is used to enable or disable the initial loading of the database and 
     * saving the database when the program closes.
     */
    private static final boolean ENABLE_INITIAL_LOAD_AND_SAVE = true;
    /**
     * This is the flag telling the database loader that it should load all the 
     * lists instead of just the lists that have been updated.
     */
    private static final int DATABASE_LOADER_LOAD_ALL_FLAG = 0x01;
    /**
     * This is the flag telling the database loader that it should check to see 
     * if the local version of the database has any lists that are more 
     * up-to-date than the downloaded version.
     */
    private static final int DATABASE_LOADER_CHECK_LOCAL_FLAG = 0x02;
    
    protected static final SimpleDateFormat DEBUG_DATE_FORMAT = 
            new SimpleDateFormat("M/d/yyyy h:mm:ss a");
    /**
     * This is a logger used to log messages for the program.
     */
    private static final Logger linkManLogger = 
            Logger.getLogger(INTERNAL_PROGRAM_NAME+"-"+PROGRAM_VERSION);
    /**
     * This returns the logger used to log messages for this program.
     * @return The logger used to log message for the program.
     */
    public static Logger getLogger(){
        return linkManLogger;
    }
    /**
     * This returns the file used to store the configuration of the program.
     * @return The configuration file.
     */
    private File getConfigFile(){
            // If a configuration file was specified
        if (configFile != null)
            return configFile;
        return new File(LinkManagerUtilities.getProgramDirectory(),CONFIG_FILE);
    }
    /**
     * This returns the file containing the Dropbox API keys for this program.
     * @return The dropbox API key file.
     */
    private File getDropboxAPIFile(){
        return new File(LinkManagerUtilities.getProgramDirectory(),DROPBOX_API_KEY_FILE);
    }
    /**
     * 
     * @param fileName The file name for the database file
     * @return 
     */
    private File getDatabaseFile(String fileName){
            // Trim the file name
        fileName = fileName.trim();
            // Get the database file
        File file = new File(fileName);
            // If the database file is relative
        if (!file.isAbsolute()){
            return new File(LinkManagerUtilities.getWorkingDirectory(),fileName);
        }
        return file;
    }
    /**
     * This returns the file used to store the database for the program.
     * @return The database file.
     */
    private File getDatabaseFile(){
        return getDatabaseFile(config.getDatabaseFileName());
    }
    /**
     * 
     */
    private void updateDatabaseFileFields(){
            // Get the database file from the config
        dbFileNameField.setText(config.getDatabaseFileName());
    }
    /**
     * This creates and returns a connection to the database file located at 
     * the given file path.
     * @param file The file path for the database to connect to.
     * @return The connection to the database.
     * @throws SQLException If a database error occurs.
     */
    private LinkDatabaseConnection connect(String file) throws SQLException{
        return new LinkDatabaseConnection(file, config.getSQLiteConfig());
    }
    /**
     * This creates and returns a connection to the database file located at 
     * the given file.
     * @param file The file for the database to connect to.
     * @return The connection to the database.
     * @throws SQLException If a database error occurs.
     */
    private LinkDatabaseConnection connect(File file) throws SQLException{
        return connect(file.toString());
    }
    /**
     * This returns whether the program is logged in to Dropbox.
     * @return Whether the program is logged in to Dropbox.
     */
    private boolean isLoggedInToDropbox(){
            // TODO: Deal with invalid access tokens
        return loadDbxUtils() != null && dbxUtils.getAccessToken() != null;
    }
    /**
     * 
     * @return 
     */
    private static File createTempFile() throws IOException{
        return File.createTempFile("LinkManager", null);
    }
    
    private DropboxLinkUtils loadDbxUtils(){
            // Get the file containing the API keys
        File dbxKey = getDropboxAPIFile();
            // If the file is null or doesn't exist
        if (dbxKey == null || !dbxKey.exists()){
            getLogger().warning("Dropbox API file not found.");
            return null;
        }
        if (dbxUtils == null){
            DbxAppInfo appInfo;
            try{
                appInfo = DbxAppInfo.Reader.readFromFile(dbxKey);
            } catch (JsonReader.FileLoadException ex){
                getLogger().log(Level.WARNING,"Dropbox API file failed to load", 
                        ex);
                return null;
            }
            dbxUtils = config.new DropboxLinkUtilsConfig(){
                DbxAppInfo info = appInfo;
                @Override
                public String getAppKey() {
                    return info.getKey();
                }
                @Override
                public String getSecretKey() {
                    return info.getSecret();
                }
                @Override
                public Collection<String> getPermissionScope() {
                    return DROPBOX_SCOPE_PERMISSIONS;
                }
                @Override
                public String getClientID() {
                    return DROPBOX_CLIENT_ID;
                }
                @Override
                public DbxAppInfo getAppInfo(){
                    return info;
                }
            };
        }
        return dbxUtils;
    }
    /**
     * This constructs a new LinkManager with the given value determining if it 
     * is in debug mode and the given program ID.
     * @param debugMode Whether the program is in debug mode.
     * @param programID The program ID for this instance of the program. This is 
     * used to determine which settings to use, or null.
     * @param configFile The configuration file for the program to load some 
     * settings from, or null.
     */
    public LinkManager(boolean debugMode, UUID programID, File configFile) {
        this.debugMode = debugMode;
            // Generate the icons for this program
        setIconImages(LinkManagerUtilities.generateIconImages(ICON_SIZES, 
                new LinkManagerIconPainter()));
        editCommands = new HashMap<>();
        undoCommands = new HashMap<>();
        textPopupMenus = new HashMap<>();
        this.configFile = configFile;
        
            // This will get the preference node for the program
        Preferences node = null;
        try{    // Try to get the preference node used for the program
            node = Preferences.userRoot().node(PREFERENCE_NODE_NAME);
        } catch (SecurityException | IllegalStateException ex){
            getLogger().log(Level.SEVERE, "Unable to load preference node", ex);
            // TODO: Error message window
        }
        
            // Create the configuration for the program
        config = new LinkManagerConfig(node);
        try{    // Try to load the configuration file into the properties
            LinkManagerUtilities.loadProperties(getConfigFile(),config.getProperties());
        } catch (IOException ex){
            getLogger().log(Level.WARNING, "Unable to load configuration file", 
                    ex);
            // TODO: Error message window
        }
            // If no program ID was provided to the program
        if (programID == null){
                // Get the program ID as a String from the properties
            String programIDStr = config.getProperties().getProperty(PROGRAM_ID_KEY);
                // If there is a program ID set
            if (programIDStr != null){
                try{    // Try to get the program ID
                    programID = UUID.fromString(programIDStr);
                } catch (IllegalArgumentException ex){}
            }
        }   // If there is a program ID to use
        if (programID != null)
            config.setProgramID(programID);
        else{   // Set and store a random program ID
            config.getProperties().setProperty(PROGRAM_ID_KEY, 
                    config.setRandomProgramID());
        }   // Log the program ID
        getLogger().log(Level.FINER, "Using program ID {0}", programID);
        try{    // Create a CipherUtils object
            CipherUtils cipher = new CipherUtils();
                // Get the encryption key from the config file if there is one
            byte[] encryptKey = config.getProperties().
                    getByteArrayProperty(USER_ENCRYPTION_KEY_KEY);
                // If there isn't an encryption key
            if (encryptKey == null){
                    // Reset the encryption on the config just in case there was 
                    // one set previously
                config.resetEncryption();
                    // Generate a new encryption key
                cipher.generateEncryptionKey();
                    // Store the encryption key
                config.getProperties().setProperty(USER_ENCRYPTION_KEY_KEY, 
                        cipher.getEncryptionKey());
            } else {// Set the encryption key for the cipher
                cipher.setEncryptionKey(encryptKey);
            }   // Initialize the encryption on the configuration
            config.initEncryption(cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | 
                InvalidKeyException | InvalidAlgorithmParameterException | 
                IllegalBlockSizeException | BadPaddingException ex) {
            getLogger().log(Level.WARNING, "Unable to load encryption keys", ex);
            config.resetEncryption();
        }
        try{    // Try to save the properties to the configuration file
            saveConfigFile();
        } catch (IOException ex) {
            getLogger().log(Level.WARNING,"Unable to save configuration to file",
                    ex);
            // TODO: Error message window
        }
        
        loadDbxUtils();
        
        dbxChunkSizeModel = new DbxChunkSizeSpinnerModel();
        initComponents();
        
        listsTabPanels = new LinksListTabsPanel[]{
            allListsTabsPanel,
            shownListsTabsPanel
        };
        debugMenu.setVisible(debugMode);
        
        allListsTabsPanel.setListActionMapper((String t, LinksListPanel u) -> {
            return createPanelAction(t,u,allListsTabsPanel);
        });
        shownListsTabsPanel.setListActionMapper((String t, LinksListPanel u) -> {
            return createPanelAction(t,u,shownListsTabsPanel);
        });
        addListMenu("Save Links To File",SAVE_TO_FILE_ACTION_KEY,listMenu,0);
        addListMenu("Add Links From File",ADD_FROM_FILE_ACTION_KEY,listMenu,2);
        addListMenu("Add Links From List",ADD_FROM_TEXT_AREA_ACTION_KEY,listMenu,4);
        addListMenu("Copy Links From Current List",COPY_TO_LIST_ACTION_KEY,listMenu,6);
        setListMenuEnabled(COPY_TO_LIST_ACTION_KEY,false);
        addListMenu("Move Links From Current List",MOVE_TO_LIST_ACTION_KEY,listMenu,8);
        setListMenuEnabled(MOVE_TO_LIST_ACTION_KEY,false);
        addListMenu("Remove Current List From List",REMOVE_FROM_LIST_ACTION_KEY,listMenu,10);
        setListMenuEnabled(REMOVE_FROM_LIST_ACTION_KEY,false);
        allListsTabsPanel.putListActionMenus(hideListsMenu,makeListReadOnlyMenuAll);
        shownListsTabsPanel.putListActionMenus(makeListReadOnlyMenuShown);
        setListMenuEnabled(MAKE_LIST_READ_ONLY_ACTION_KEY,false);
        allListsTabsPanel.getListActionMenu(REMOVE_FROM_LIST_ACTION_KEY)
                .add(allListsTabsPanel.getOrCreateListMenuItem(null, 
                        REMOVE_OTHER_LISTS_ACTION_KEY));
        allListsTabsPanel.getListActionMenu(REMOVE_FROM_LIST_ACTION_KEY)
                .add(allListsTabsPanel.getOrCreateListMenuItem(null, 
                        REMOVE_FROM_HIDDEN_LISTS_ACTION_KEY));
        shownListsTabsPanel.getListActionMenu(REMOVE_FROM_LIST_ACTION_KEY)
                .add(shownListsTabsPanel.getOrCreateListMenuItem(null, 
                        REMOVE_OTHER_LISTS_ACTION_KEY));
        
        listAOpCombo.setRenderer(new LinksListCellRenderer());
        listBOpCombo.setRenderer(new LinksListCellRenderer());
        listCOpCombo.setRenderer(new LinksListCellRenderer());
        
        dbListTable.setDefaultRenderer(java.util.Date.class, new DateTableCellRenderer(DEBUG_DATE_FORMAT));
        dbListTable.setDefaultRenderer(Integer.class, new DefaultTableCellRenderer(){
            @Override
            public java.awt.Component getTableCellRendererComponent(
                    JTable table, Object value, boolean isSelected, 
                    boolean hasFocus, int row, int column) {
                    // If the value is an integer or null
                if (value instanceof Integer || value == null){
                        // Get the value as an Integer
                    Integer i = (Integer) value;
                        // Determine which column this is being rendered to
                    switch(column){
                        case(4):    // If this column is the flags column
                                // If the value is not null
                            if (value != null)
                                value = String.format("0x%08X", i);
                            break;
                        case(5):    // If this column is the size limit
                                // If the value is null
                            if (value == null)
                                value = "N/A";
                    }
                }
                return super.getTableCellRendererComponent(table, value, 
                        isSelected, hasFocus, row, column);
            }
        });
        dbPrefixTable.getSelectionModel().addListSelectionListener((ListSelectionEvent evt) -> {
            updatePrefixButtons();
        });
        dbTableTable.getSelectionModel().addListSelectionListener((ListSelectionEvent evt) -> {
                // If the table's selection is adjusting
            if (evt.getValueIsAdjusting())
                return;
                // Get the selected row
            int selRow = dbTableTable.getSelectedRow();
                // If there is no selected row
            if (selRow < 0)
                dbTableStructText.setText(null);
            else
                dbTableStructText.setText(Objects.toString(dbTableTable.getValueAt(selRow, 2), ""));
        });
        dbListTable.getSelectionModel().addListSelectionListener((ListSelectionEvent evt) -> {
                // If the table's selection is adjusting
            if (evt.getValueIsAdjusting())
                return;
                // Get the value in the first column of the selected row, or 
                // null if no rows are selected
            Object tempID = (dbListTable.getSelectedRow() < 0) ? null : 
                    dbListTable.getValueAt(dbListTable.getSelectedRow(), 0);
                // This will get the selected listID
            Integer listID = null;
                // If the value is an integer
            if (tempID instanceof Integer)
                    // Use it as the listID
                listID = (Integer) tempID;
                // If there is a listID selected
            if (listID != null)
                dbListIDCombo.setSelectedItem(listID);
        });
        dbCreatePrefixTree.setCellRenderer(new ChildCountTreeCellRenderer());
        
        listManipulator.addListSelectionListener(listManipSelCountPanel);
        copyOrMoveListSelector.addListSelectionListener(copyOrMoveSelCountPanel);
        
        progressBar.addChangeListener(progressDisplay);
        progressBar.addPropertyChangeListener(progressDisplay);
        
        textPopupMenus.put(linkTextField, new JPopupMenu());
        textPopupMenus.put(dbQueryPanel.getQueryTextField(), dbQueryPanel.getQueryPopupMenu());
        textPopupMenus.put(prefixField, new JPopupMenu());
        textPopupMenus.put(dbTableStructText, new JPopupMenu());
        textPopupMenus.put(dbFileField, new JPopupMenu());
        textPopupMenus.put(searchPanel.getSearchTextField(), searchPanel.getSearchPopupMenu());
        textPopupMenus.put(addLinksPanel.getTextArea(), addLinksPanel.getTextPopupMenu());
        textPopupMenus.put(dropboxSetupPanel.getAuthorizationCodeField(), 
                dropboxSetupPanel.getAuthorizationCodePopupMenu());
        textPopupMenus.put(dbxDbFileField, new JPopupMenu());
        
        pasteAndAddAction = new PasteAndAddAction(){
            @Override
            public List<String> getList() {
                return getSelectedTabsPanel().getSelectedModel();
            }
            @Override
            public JTextComponent getTextComponent() {
                return linkTextField;
            }
        };
        pasteAndAddButton.setAction(pasteAndAddAction);
        pasteAndAddButton.setText("Add From Clipboard");
        
        for (Map.Entry<JTextComponent, JPopupMenu> entry : textPopupMenus.entrySet()){
            JTextComponent comp = entry.getKey();
            TextComponentCommands textCmd = new TextComponentCommands(comp);
            editCommands.put(comp, textCmd);
            if (comp == dbTableStructText){
                textCmd.addToTextComponent();
                entry.getValue().add(textCmd.getCopyAction());
                entry.getValue().add(textCmd.getSelectAllAction());
                continue;
            }
            UndoManagerCommands undoCmd = new UndoManagerCommands(
                    new CompoundUndoManager(),false);
            undoCommands.put(comp, undoCmd);
            if (comp == linkTextField)
                LinkManagerUtilities.addToPopupMenu(entry.getValue(),undoCmd,
                        textCmd,pasteAndAddAction);
            else
                LinkManagerUtilities.addToPopupMenu(entry.getValue(),undoCmd,
                        textCmd);
            comp.setComponentPopupMenu(entry.getValue());
        }
        
        editCommands.get(linkTextField).getPasteAction().
                addPropertyChangeListener((PropertyChangeEvent evt) -> {
                // If the regular paste command has had its enable property 
            if ("enabled".equals(evt.getPropertyName()))    // changed
                updatePasteAndAddAction();
        });
        
            // The action performed when escape is pressed
        Action linkFieldCancel = new AbstractAction("LinkCancel"){
            @Override
            public void actionPerformed(ActionEvent e) {
                resetLinkField();
            }
        };
        linkTextField.getInputMap(JComponent.WHEN_FOCUSED)
                .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), 
                        linkFieldCancel.getValue(Action.NAME));
        linkTextField.getActionMap().put(linkFieldCancel.getValue(Action.NAME),
                linkFieldCancel);
        linkTextField.getInputMap(JComponent.WHEN_FOCUSED)
                .put((KeyStroke)pasteAndAddAction.getValue(Action.ACCELERATOR_KEY), 
                        pasteAndAddAction.getValue(Action.NAME));
        linkTextField.getActionMap().put(pasteAndAddAction.getValue(Action.NAME),
                pasteAndAddAction);
        
        linkTextField.getDocument().addDocumentListener(new SingleMethodDocumentListener(){
            @Override
            public void documentUpdate(DocumentEvent evt, DocumentEvent.EventType type) {
                updateNewLinkButton();
            }
        });
        dbFileField.getDocument().addDocumentListener(new SingleMethodDocumentListener(){
            @Override
            public void documentUpdate(DocumentEvent evt, DocumentEvent.EventType type) {
                updateDBLocationButtons();
            }
        });
        
        saveFC.addChoosableFileFilter(TEXT_FILTER);
        saveFC.setFileFilter(TEXT_FILTER);
        openFC.addChoosableFileFilter(SHORTCUT_FILE_FILTER);
        openFC.addChoosableFileFilter(TEXT_FILTER);
        
        searchMenu.add(searchPanel.getFindNextAction());
        searchMenu.add(searchPanel.getFindPreviousAction());
        
            // Set up the component key prefix map
        config.getComponentNames().put(listManipulator, LIST_MANAGER_NAME);
        config.getComponentNames().put(listTabsManipulator, LIST_TABS_MANAGER_NAME);
        config.getComponentNames().put(addLinksPanel, ADD_LINKS_PANEL_NAME);
        config.getComponentNames().put(copyOrMoveListSelector, 
                COPY_OR_MOVE_LINKS_PANEL_NAME);
        config.getComponentNames().put(openFC, OPEN_FILE_CHOOSER_NAME);
        config.getComponentNames().put(saveFC, SAVE_FILE_CHOOSER_NAME);
        config.getComponentNames().put(configFC, CONFIG_FILE_CHOOSER_NAME);
        config.getComponentNames().put(exportFC, EXPORT_FILE_CHOOSER_NAME);
        config.getComponentNames().put(databaseFC, DATABASE_FILE_CHOOSER_NAME);
        config.getComponentNames().put(LinkManager.this, LINK_MANAGER_NAME);
        config.getComponentNames().put(setLocationDialog, DATABASE_LOCATION_DIALOG_NAME);
        config.getComponentNames().put(searchDialog, SEARCH_DIALOG_NAME);
        
            // Initialize the defaults that are dependent on the UI
        
            // Set the default location of the search dialog
        config.setDefaultComponentLocation(searchDialog, searchDialog.getX(), 
                searchDialog.getY());
            
            // Go through the components to store their preferred sizes
        for (Component comp : config.getComponentNames().keySet()){
                // If the component is the search dialog
            if (comp == searchDialog)
                continue;
                // Get the prefered size of the current component
            Dimension size = comp.getPreferredSize();
                // If the current component is this component
            if (comp == LinkManager.this){
                    // Use the preferred size of the component as it's 
                    // default size for its bounds
                config.setDefaultComponentBounds(comp, size.width,size.height);
                // If the component is the database location dialog
            } else if (comp == setLocationDialog){
                    // Use the preferred size of the component as it's 
                    // default size for its bounds, but use its current location 
                    // as the default location
                config.setDefaultComponentBounds(comp, comp.getX(),comp.getY(),
                        size.width,size.height);
            } else {// Use the preferred size of the current component as its 
                    // default size
                config.setDefaultComponentSize(comp,size);
            }
        }
        
            // Set the SQLite config to enforce the foreign keys
        config.getSQLiteConfig().enforceForeignKeys(foreignKeysToggle.isSelected());
        
        progressObserver = new DefaultProgressObserver(progressBar){
            @Override
            public DefaultProgressObserver setValue(int value) {
                super.setValue(value);
                slowTestToggle.runSlowTest();
                return this;
            }
            @Override
            public String getText() {
                return progressDisplay.getString();
            }
            @Override
            public DefaultProgressObserver setText(String text) {
                progressDisplay.setString(text);
                return this;
            }
        };
        
        System.gc();        // Run the garbage collector
            // Configure the program from the settings
        configureProgram();
        if (ENABLE_INITIAL_LOAD_AND_SAVE){
            loadDatabase(DATABASE_LOADER_LOAD_ALL_FLAG);// | DATABASE_LOADER_CHECK_LOCAL_FLAG);
        }
    }
    /**
     * This constructs a new LinkManager with the given value determining if it 
     * is in debug mode and the given program ID.
     * @param debugMode Whether the program is in debug mode.
     * @param programID The program ID for this instance of the program. This is 
     * used to determine which settings to use, or null.
     */
    public LinkManager(boolean debugMode, UUID programID) {
        this(debugMode,programID,null);
    }
    /**
     * This constructs a new LinkManager with the given value determining if it 
     * is in debug mode and the given program ID.
     * @param debugMode Whether the program is in debug mode.
     * @param configFile The configuration file for the program to load some 
     * settings from, or null.
     */
    public LinkManager(boolean debugMode, File configFile) {
        this(debugMode,null,configFile);
    }
    /**
     * This constructs a new LinkManager with the given value determining if it 
     * is in debug mode and that will load the program ID from the 
     * configuration.
     * @param debugMode Whether the program is in debug mode.
     */
    public LinkManager(boolean debugMode){
        this(debugMode,null, null);
    }
     /**
     * This constructs a new LinkManager that is not in debug mode and with the 
     * given program ID.
     * @param programID The program ID for this instance of the program. This is 
     * used to determine which settings to use.
     */
    public LinkManager(UUID programID) {
        this(false,programID, null);
    }
    /**
     * This constructs a new LinkManager that is not in debug mode and that will 
     * load the program ID from the configuration.
     */
    public LinkManager(){
        this(false);
    }
    /**
     * 
     * @param command
     * @param panel
     * @param tabsPanel
     * @return 
     */
    private LinksListAction createPanelAction(String command, 
            LinksListPanel panel, LinksListTabsPanel tabsPanel){
            // If the given command is null
        if (command == null)
            return null;
            // If the panel is null (this is the action for the currently 
            // selected panel)
        if (panel == null){
                // Determine which action to return based off the given command
            switch(command){
                    // If this is the remove from list action
                case(REMOVE_FROM_LIST_ACTION_KEY):
                    return new RemoveFromListsAction(tabsPanel,panel);
                case(REMOVE_OTHER_LISTS_ACTION_KEY):
                    return new RemoveFromListsAction(tabsPanel,panel,false);
                case(REMOVE_FROM_HIDDEN_LISTS_ACTION_KEY):
                    return new RemoveFromListsAction(tabsPanel,panel,true,true);
                case(REMOVE_OTHER_HIDDEN_LISTS_ACTION_KEY):
                    return new RemoveFromListsAction(tabsPanel,panel,false,true);
                    // There is no copy to current list action (this action 
                    // copies from the current list)
                case(COPY_TO_LIST_ACTION_KEY):
                    // There is no move to current list action (this action 
                    // moves from the current list)
                case(MOVE_TO_LIST_ACTION_KEY):
                    // There is no hide current list action
                case(HIDE_LIST_ACTION_KEY):
                    // There is no make current list read only action
                case(MAKE_LIST_READ_ONLY_ACTION_KEY):
                    return null;
            }
        }
        switch(command){
            case(SAVE_TO_FILE_ACTION_KEY):
                return new SaveToFileAction(tabsPanel,panel);
            case(ADD_FROM_FILE_ACTION_KEY):
                return new AddFromFileAction(tabsPanel,panel);
            case(ADD_FROM_TEXT_AREA_ACTION_KEY):
                return new AddFromTextAreaAction(tabsPanel,panel);
            case(COPY_TO_LIST_ACTION_KEY):
                return new CopyOrMoveToListAction(tabsPanel,panel,false);
            case(MOVE_TO_LIST_ACTION_KEY):
                return new CopyOrMoveToListAction(tabsPanel,panel,true);
            case(REMOVE_FROM_LIST_ACTION_KEY):
                return new RemoveFromListAction(tabsPanel,panel);
            case(HIDE_LIST_ACTION_KEY):
                return new HideListAction(tabsPanel,panel);
            case(MAKE_LIST_READ_ONLY_ACTION_KEY):
                return new MakeListReadOnlyAction(tabsPanel,panel);
        }
        if (isInDebug()){
            return new LinksListTabAction(command,command,tabsPanel,panel){
                @Override
                public void actionPerformed(ActionEvent evt,LinksListPanel panel,
                        LinksListTabsPanel tabsPanel) {
                    System.out.println("ActionEvent: " + evt);
                    System.out.println("Panel: "+panel);
                    System.out.println("Tabs Panel: " + tabsPanel);
                }
            };
        }
        return null;
    }
    /**
     * 
     * @param panel 
     */
    private void setVisibleTabsPanel(LinksListTabsPanel panel){
        for (LinksListTabsPanel tabsPanel : listsTabPanels){
            tabsPanel.setListActionMenusVisible(panel == tabsPanel);
        }
        LinkManagerUtilities.setCard(tabsPanelDisplay,panel);
        updateButtons();
    }
    /**
     * This returns the LinksListTabsPanel that is currently being displayed.
     * @return 
     */
    private LinksListTabsPanel getSelectedTabsPanel(){
        return (showHiddenListsToggle.isSelected())?allListsTabsPanel:
                shownListsTabsPanel;
    }
    /**
     * 
     */
    private void updateVisibleTabsPanel(){
        if (showHiddenListsToggle.isSelected()){
            // TODO: Perhaps stop the auto-hide timer if a hidden list is edited 
            // and/or selected, and only resumes after the lists are saved or a 
            // non-hidden list is selected?
//            if (!autoHideMenu.isRunning())
                autoHideMenu.startAutoHide();
        } else
            autoHideMenu.stopAutoHide();
        setVisibleTabsPanel(getSelectedTabsPanel());
        hiddenLinkOperationToggle.setVisible(showHiddenListsToggle.isSelected());
            // Set whether hidden lists are shown in the configuration
        config.setHiddenListsAreShown(showHiddenListsToggle.isSelected());
    }
    /**
     * 
     * @return 
     */
    private LinksListPanel getSelectedList(){
        return getSelectedTabsPanel().getSelectedList();
    }
    /**
     * 
     * @param text
     * @param actionCmd
     * @param menu
     * @param index 
     */
    private void addListMenu(String text, String actionCmd, JMenu menu, int index){
            // Go through the list tabs panels
        for (LinksListTabsPanel panel : listsTabPanels){
                // Create a menu for the panel
            JMenu tabsMenu = new JMenu(text);
                // Set the menu's action command
            tabsMenu.setActionCommand(actionCmd);
                // Put the menu into the panel's list action menus
            panel.setListActionMenu(actionCmd, tabsMenu);
                // Insert the menu into the given menu at the index
            menu.insert(tabsMenu, index);
                // Increment the index for the next menu
            index++;
        }
    }
    /**
     * 
     * @param actionCmd
     * @param enabled 
     */
    private void setListMenuEnabled(String actionCmd, boolean enabled){
            // Go through the list tabs panels
        for (LinksListTabsPanel panel : listsTabPanels){
            panel.setListActionMenuEnabled(actionCmd, enabled);
        }
    }
    /**
     * 
     * @param enabled 
     */
    private void setTabsPanelEnabled(boolean enabled){
            // Go through the list tabs panels
        for (LinksListTabsPanel panel : listsTabPanels){
            panel.setEnabled(enabled);
        }
    }
    /**
     * 
     * @param enabled 
     */
    private void setTabsPanelListsEnabled(boolean enabled){
            // Go through the list tabs panels
        for (LinksListTabsPanel panel : listsTabPanels){
            panel.setListsEnabled(enabled);
        }
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        openFC = new javax.swing.JFileChooser();
        saveFC = new javax.swing.JFileChooser();
        configFC = new javax.swing.JFileChooser();
        exportFC = new javax.swing.JFileChooser();
        databaseUpdateFC = new javax.swing.JFileChooser();
        databaseFC = new javax.swing.JFileChooser();
        setLocationDialog = new javax.swing.JDialog(this);
        setLocationPanel = new javax.swing.JPanel();
        setExternalCard = new javax.swing.JPanel();
        dbxLogInButton = new javax.swing.JButton();
        setDropboxCard = new javax.swing.JPanel();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        dbxDbFileField = new javax.swing.JTextField();
        dbxDataPanel = new javax.swing.JPanel();
        dbxPfpLabel = new components.JThumbnailLabel();
        dbxAccountLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        dbxSpaceUsedLabel = new javax.swing.JLabel();
        javax.swing.JLabel jLabel5 = new javax.swing.JLabel();
        dbxSpaceFreeLabel = new javax.swing.JLabel();
        dbxLogOutButton = new javax.swing.JButton();
        javax.swing.Box.Filler filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        javax.swing.JLabel jLabel7 = new javax.swing.JLabel();
        dbxChunkSizeSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel11 = new javax.swing.JLabel();
        javax.swing.JLabel dbFileChangeLabel = new javax.swing.JLabel();
        dbFileChangeCombo = new javax.swing.JComboBox<>();
        locationControlPanel = new javax.swing.JPanel();
        setDBResetButton = new javax.swing.JButton();
        setDBAcceptButton = new javax.swing.JButton();
        setDBCancelButton = new javax.swing.JButton();
        javax.swing.JLabel dbFileLabel = new javax.swing.JLabel();
        dbFileField = new javax.swing.JTextField();
        dbFileBrowseButton = new javax.swing.JButton();
        dbFileRelativeButton = new javax.swing.JButton();
        databaseDialog = new javax.swing.JDialog(this);
        dbTabbedPane = new javax.swing.JTabbedPane();
        dbViewer = new manager.database.DatabaseTableViewer();
        dbQueryPanel = new manager.database.DatabaseQueryTestPanel();
        dbFilePanel = new javax.swing.JPanel();
        javax.swing.JLabel dbFileNameLabel = new javax.swing.JLabel();
        dbFileNameField = new javax.swing.JTextField();
        setDBFileNameButton = new javax.swing.JButton();
        resetDBFilePathButton = new javax.swing.JButton();
        dbCreateTablesButton = new javax.swing.JButton();
        foreignKeysToggle = new javax.swing.JCheckBox();
        updateDBFileCombo = new javax.swing.JComboBox<>();
        updateDBFileButton = new javax.swing.JButton();
        dbRemoveUnusedDataButton = new javax.swing.JButton();
        dbUpdateUsedPrefixesButton = new javax.swing.JButton();
        dbRemoveDuplDataButton = new javax.swing.JButton();
        dbResetIDsButton = new javax.swing.JButton();
        dbPropPanel = new javax.swing.JPanel();
        javax.swing.JLabel dbVersionTextLabel = new javax.swing.JLabel();
        dbVersionLabel = new javax.swing.JLabel();
        javax.swing.JLabel dbFileSizeTextLabel = new javax.swing.JLabel();
        dbFileSizeLabel = new javax.swing.JLabel();
        javax.swing.JLabel dbUUIDTextLabel = new javax.swing.JLabel();
        dbUUIDLabel = new javax.swing.JLabel();
        javax.swing.JLabel dbLastModTextLabel = new javax.swing.JLabel();
        dbLastModLabel = new javax.swing.JLabel();
        javax.swing.JLabel linkCountTextLabel = new javax.swing.JLabel();
        linkCountLabel = new javax.swing.JLabel();
        javax.swing.JLabel shownTotalSizeTextLabel = new javax.swing.JLabel();
        shownTotalSizeLabel = new javax.swing.JLabel();
        javax.swing.JLabel allTotalSizeTextLabel = new javax.swing.JLabel();
        allTotalSizeLabel = new javax.swing.JLabel();
        javax.swing.JLabel programIDTextLabel = new javax.swing.JLabel();
        programIDLabel = new javax.swing.JLabel();
        javax.swing.Box.Filler filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        dbUpdateLastModButton = new javax.swing.JButton();
        dbPrefixesPanel = new javax.swing.JPanel();
        dbPrefixScrollPane = new javax.swing.JScrollPane();
        dbPrefixTable = new javax.swing.JTable();
        javax.swing.JLabel jLabel9 = new javax.swing.JLabel();
        prefixField = new javax.swing.JTextField();
        addPrefixButton = new javax.swing.JButton();
        removePrefixButton = new javax.swing.JButton();
        javax.swing.JLabel jLabel14 = new javax.swing.JLabel();
        prefixThresholdSpinner = new javax.swing.JSpinner();
        javax.swing.JLabel jLabel15 = new javax.swing.JLabel();
        prefixSeparatorField = new javax.swing.JTextField();
        prefixApplyButton = new javax.swing.JButton();
        prefixCopyButton = new javax.swing.JButton();
        dbUsedPrefixesPanel = new javax.swing.JPanel();
        dbUsedPrefixScrollPane = new javax.swing.JScrollPane();
        dbUsedPrefixTable = new javax.swing.JTable();
        javax.swing.JLabel jLabel20 = new javax.swing.JLabel();
        dbUsedPrefixCombo = new javax.swing.JComboBox<>();
        javax.swing.JLabel jLabel22 = new javax.swing.JLabel();
        dbUsedPrefixSizeLabel = new javax.swing.JLabel();
        dbLinkSearchPanel = new javax.swing.JPanel();
        javax.swing.JLabel jLabel8 = new javax.swing.JLabel();
        dbSearchField = new javax.swing.JTextField();
        dbSearchPrefixCheckBox = new javax.swing.JCheckBox();
        dbSearchPrefixCombo = new javax.swing.JComboBox<>();
        dbSearchButton = new javax.swing.JButton();
        dbLinkSearchScrollPane = new javax.swing.JScrollPane();
        dbLinkSearchTable = new javax.swing.JTable();
        dbListPanel = new javax.swing.JPanel();
        dbListScrollPane = new javax.swing.JScrollPane();
        dbListTable = new javax.swing.JTable();
        javax.swing.JLabel jLabel17 = new javax.swing.JLabel();
        dbListIDCombo = new javax.swing.JComboBox<>();
        javax.swing.JLabel jLabel18 = new javax.swing.JLabel();
        dbListNameField = new javax.swing.JTextField();
        javax.swing.JLabel jLabel19 = new javax.swing.JLabel();
        dbListFlagsField = new javax.swing.JFormattedTextField();
        dbListSizeLimitToggle = new javax.swing.JCheckBox();
        dbListSizeLimitSpinner = new javax.swing.JSpinner();
        dbListEditApplyButton = new javax.swing.JButton();
        configScrollPane = new javax.swing.JScrollPane();
        configTable = new javax.swing.JTable();
        dbTablePanel = new javax.swing.JPanel();
        dbTableScrollPane = new javax.swing.JScrollPane();
        dbTableTable = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        dbTableStructText = new javax.swing.JTextArea();
        dbCreatePrefixScrollPane = new javax.swing.JScrollPane();
        dbCreatePrefixTree = new javax.swing.JTree();
        dbRefreshButton = new javax.swing.JButton();
        showSchemaToggle = new javax.swing.JCheckBox();
        backupDBButton = new javax.swing.JButton();
        printDBButton = new javax.swing.JButton();
        addLinksPanel = new manager.AddLinksFromListPanel();
        searchDialog = new javax.swing.JDialog(this);
        searchPanel = new manager.LinkSearchPanel();
        listManipSelCountPanel = new manager.SelectedItemCountPanel();
        listManipulator = new components.JListManipulator<>();
        linkEditPane = new javax.swing.JOptionPane();
        copyOrMoveSelCountPanel = new manager.SelectedItemCountPanel();
        copyOrMoveListSelector = new components.JListSelector<>();
        listTabsManipulator = new manager.links.LinksListTabsManipulator();
        listSetOpDialog = new javax.swing.JDialog(this);
        javax.swing.JLabel listAOpLabel = new javax.swing.JLabel();
        listAOpCombo = new javax.swing.JComboBox<>();
        javax.swing.JLabel listBOpLabel = new javax.swing.JLabel();
        listBOpCombo = new javax.swing.JComboBox<>();
        javax.swing.JLabel listOpLabel = new javax.swing.JLabel();
        listOperationCombo = new javax.swing.JComboBox<>();
        listSetOpCancelButton = new javax.swing.JButton();
        listSetOpApplyButton = new javax.swing.JButton();
        listCOpCombo = new javax.swing.JComboBox<>();
        javax.swing.JLabel listCOpLabel = new javax.swing.JLabel();
        dropboxSetupPanel = new manager.dropbox.DropboxSetupPanel();
        progressBar = new javax.swing.JProgressBar();
        javax.swing.JLabel newLinkLabel = new javax.swing.JLabel();
        linkTextField = new javax.swing.JTextField();
        pasteAndAddButton = new javax.swing.JButton();
        newLinkButton = new javax.swing.JButton();
        editLinkButton = new javax.swing.JButton();
        manageLinksButton = new javax.swing.JButton();
        removeLinkButton = new javax.swing.JButton();
        copyLinkButton = new javax.swing.JButton();
        openLinkButton = new javax.swing.JButton();
        tabsPanelDisplay = new javax.swing.JPanel();
        allListsTabsPanel = new manager.links.LinksListTabsPanel();
        shownListsTabsPanel = new manager.links.LinksListTabsPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        updateDatabaseItem = new javax.swing.JMenuItem();
        updateListsItem = new javax.swing.JMenuItem();
        reloadListsItem = new javax.swing.JMenuItem();
        exportListsItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        uploadDBItem = new javax.swing.JMenuItem();
        downloadDBItem = new javax.swing.JMenuItem();
        javax.swing.JPopupMenu.Separator dbConfigSeparator = new javax.swing.JPopupMenu.Separator();
        saveConfigItem = new javax.swing.JMenuItem();
        loadConfigItem = new javax.swing.JMenuItem();
        configExitSeparator = new javax.swing.JPopupMenu.Separator();
        exitButton = new javax.swing.JMenuItem();
        listMenu = new javax.swing.JMenu();
        manageListsItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        makeListReadOnlyMenuAll = new javax.swing.JMenu();
        makeListReadOnlyMenuShown = new javax.swing.JMenu();
        hideListsMenu = new javax.swing.JMenu();
        jSeparator4 = new javax.swing.JPopupMenu.Separator();
        showAllListsItem = new javax.swing.JMenuItem();
        hideAllListsItem = new javax.swing.JMenuItem();
        showHiddenListsToggle = new javax.swing.JCheckBoxMenuItem();
        searchMenu = new javax.swing.JMenu();
        searchMenuItem = new javax.swing.JMenuItem();
        optionsMenu = new javax.swing.JMenu();
        progressDisplay = new components.progress.JProgressDisplayMenu();
        alwaysOnTopToggle = new javax.swing.JCheckBoxMenuItem();
        doubleNewLinesToggle = new javax.swing.JCheckBoxMenuItem();
        linkOperationToggle = new javax.swing.JCheckBoxMenuItem();
        hiddenLinkOperationToggle = new javax.swing.JCheckBoxMenuItem();
        showDBErrorDetailsToggle = new javax.swing.JCheckBoxMenuItem();
        setDBLocationItem = new javax.swing.JMenuItem();
        syncDBToggle = new javax.swing.JCheckBoxMenuItem();
        autosaveMenu = new manager.timermenu.AutosaveMenu();
        autoHideMenu = new manager.timermenu.AutoHideMenu();
        debugMenu = new javax.swing.JMenu();
        slowTestToggle = new components.debug.SlowTestMenuItem();
        activeToggle = new javax.swing.JCheckBoxMenuItem();
        printDataItem = new javax.swing.JMenuItem();
        dbViewItem = new javax.swing.JMenuItem();
        showIDsToggle = new javax.swing.JCheckBoxMenuItem();
        printAutosaveEventsToggle = new javax.swing.JCheckBoxMenuItem();
        printAutoHideEventsToggle = new javax.swing.JCheckBoxMenuItem();
        printListPropChangeToggle = new javax.swing.JCheckBoxMenuItem();
        clearSelTabItem = new javax.swing.JMenuItem();
        clearListSelItem = new javax.swing.JMenuItem();
        listSetOpItem = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        dbxPrintButton = new javax.swing.JMenuItem();
        setDropboxTestButton = new javax.swing.JMenuItem();
        dropboxRefreshTestButton = new javax.swing.JMenuItem();

        openFC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        saveFC.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        saveFC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        configFC.setAcceptAllFileFilterUsed(false);
        configFC.setFileFilter(ConfigExtensions.CONFIG_FILTER);
        configFC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        exportFC.setDialogType(javax.swing.JFileChooser.SAVE_DIALOG);
        exportFC.setApproveButtonText("Export");
        exportFC.setDialogTitle("Export Links...");
        exportFC.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        exportFC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        databaseUpdateFC.setDialogType(javax.swing.JFileChooser.CUSTOM_DIALOG);
        databaseUpdateFC.setApproveButtonText("Update");
        databaseUpdateFC.setDialogTitle("Update Database...");
        databaseUpdateFC.setFileFilter(DATABASE_FILE_FILTER);
        databaseUpdateFC.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileChooserActionPerformed(evt);
            }
        });

        databaseFC.setApproveButtonText("Open");
        databaseFC.setDialogTitle("Set Database Location...");
        databaseFC.setFileFilter(DATABASE_FILE_FILTER);
        databaseFC.setFileSelectionMode(javax.swing.JFileChooser.FILES_AND_DIRECTORIES);

        setLocationDialog.setTitle("Set Database Location");
        setLocationDialog.setMinimumSize(new java.awt.Dimension(480, 380));
        setLocationDialog.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                setLocationDialogComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                setLocationDialogComponentResized(evt);
            }
        });

        setLocationPanel.setLayout(new java.awt.CardLayout());

        setExternalCard.setBorder(javax.swing.BorderFactory.createTitledBorder("Set Up External File"));
        setExternalCard.setName("logInCard"); // NOI18N

        dbxLogInButton.setText("Set Up Dropbox");
        dbxLogInButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbxLogInButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout setExternalCardLayout = new javax.swing.GroupLayout(setExternalCard);
        setExternalCard.setLayout(setExternalCardLayout);
        setExternalCardLayout.setHorizontalGroup(
            setExternalCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setExternalCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbxLogInButton)
                .addContainerGap(310, Short.MAX_VALUE))
        );
        setExternalCardLayout.setVerticalGroup(
            setExternalCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setExternalCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbxLogInButton)
                .addContainerGap(158, Short.MAX_VALUE))
        );

        setLocationPanel.add(setExternalCard, "logInCard");

        setDropboxCard.setBorder(javax.swing.BorderFactory.createTitledBorder("Dropbox"));
        setDropboxCard.setName("setDropbox"); // NOI18N

        jLabel2.setLabelFor(dbxDbFileField);
        jLabel2.setText("File:");

        dbxDataPanel.setLayout(new java.awt.GridBagLayout());

        dbxPfpLabel.setImageScaleMode(components.JThumbnailLabel.ALWAYS_SCALE_MAINTAIN_ASPECT_RATIO);
        dbxPfpLabel.setThumbnailBorder(javax.swing.BorderFactory.createBevelBorder(javax.swing.border.BevelBorder.RAISED));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridheight = 5;
        gridBagConstraints.ipadx = 100;
        gridBagConstraints.ipady = 100;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        dbxDataPanel.add(dbxPfpLabel, gridBagConstraints);

        dbxAccountLabel.setFont(dbxAccountLabel.getFont().deriveFont(dbxAccountLabel.getFont().getStyle() | java.awt.Font.BOLD));
        dbxAccountLabel.setText("N/A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        dbxDataPanel.add(dbxAccountLabel, gridBagConstraints);

        jLabel1.setLabelFor(dbxSpaceUsedLabel);
        jLabel1.setText("Space Used:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 12);
        dbxDataPanel.add(jLabel1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 7, 0);
        dbxDataPanel.add(dbxSpaceUsedLabel, gridBagConstraints);

        jLabel5.setLabelFor(dbxSpaceFreeLabel);
        jLabel5.setText("Space Free:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 12);
        dbxDataPanel.add(jLabel5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        dbxDataPanel.add(dbxSpaceFreeLabel, gridBagConstraints);

        dbxLogOutButton.setText("Log Out");
        dbxLogOutButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbxLogOutButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = 2;
        dbxDataPanel.add(dbxLogOutButton, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weightx = 0.75;
        dbxDataPanel.add(filler1, gridBagConstraints);

        jLabel7.setLabelFor(dbxChunkSizeSpinner);
        jLabel7.setText("Chunk Size:");

        dbxChunkSizeSpinner.setModel(dbxChunkSizeModel);
        dbxChunkSizeSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                dbxChunkSizeSpinnerStateChanged(evt);
            }
        });

        jLabel11.setText("MiB");

        javax.swing.GroupLayout setDropboxCardLayout = new javax.swing.GroupLayout(setDropboxCard);
        setDropboxCard.setLayout(setDropboxCardLayout);
        setDropboxCardLayout.setHorizontalGroup(
            setDropboxCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setDropboxCardLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setDropboxCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbxDataPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(setDropboxCardLayout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbxDbFileField, javax.swing.GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE))
                    .addGroup(setDropboxCardLayout.createSequentialGroup()
                        .addComponent(jLabel7)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbxChunkSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel11)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        setDropboxCardLayout.setVerticalGroup(
            setDropboxCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, setDropboxCardLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbxDataPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 106, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(setDropboxCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(dbxDbFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(setDropboxCardLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel7)
                    .addComponent(dbxChunkSizeSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel11))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        setLocationPanel.add(setDropboxCard, "setDropbox");

        dbFileChangeLabel.setLabelFor(dbFileChangeCombo);
        dbFileChangeLabel.setText("File Operation:");

        dbFileChangeCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Do Nothing", "Copy File", "Move File" }));
        dbFileChangeCombo.setSelectedIndex(2);
        dbFileChangeCombo.setToolTipText("What to do with the current database file.");
        dbFileChangeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbFileChangeComboActionPerformed(evt);
            }
        });

        locationControlPanel.setLayout(new java.awt.GridLayout(1, 0, 6, 0));

        setDBResetButton.setText("Reset");
        setDBResetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDBResetButtonActionPerformed(evt);
            }
        });
        locationControlPanel.add(setDBResetButton);

        setDBAcceptButton.setText("OK");
        setDBAcceptButton.setEnabled(false);
        setDBAcceptButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDBAcceptButtonActionPerformed(evt);
            }
        });
        locationControlPanel.add(setDBAcceptButton);

        setDBCancelButton.setText("Cancel");
        setDBCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDBCancelButtonActionPerformed(evt);
            }
        });
        locationControlPanel.add(setDBCancelButton);

        dbFileLabel.setLabelFor(dbFileField);
        dbFileLabel.setText("File:");

        dbFileBrowseButton.setText("Browse");
        dbFileBrowseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbFileBrowseButtonActionPerformed(evt);
            }
        });

        dbFileRelativeButton.setText("Make Relative To Program");
        dbFileRelativeButton.setToolTipText("This makes the database file's path to be relative to this program's location.");
        dbFileRelativeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbFileRelativeButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout setLocationDialogLayout = new javax.swing.GroupLayout(setLocationDialog.getContentPane());
        setLocationDialog.getContentPane().setLayout(setLocationDialogLayout);
        setLocationDialogLayout.setHorizontalGroup(
            setLocationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setLocationDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setLocationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, setLocationDialogLayout.createSequentialGroup()
                        .addComponent(dbFileChangeLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbFileChangeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(locationControlPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(setLocationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(setLocationDialogLayout.createSequentialGroup()
                        .addComponent(dbFileLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbFileField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbFileBrowseButton))
                    .addGroup(setLocationDialogLayout.createSequentialGroup()
                        .addComponent(dbFileRelativeButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        setLocationDialogLayout.setVerticalGroup(
            setLocationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(setLocationDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(setLocationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbFileLabel)
                    .addComponent(dbFileField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbFileBrowseButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbFileRelativeButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(setLocationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(setLocationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(locationControlPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, setLocationDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(dbFileChangeCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(dbFileChangeLabel)))
                .addContainerGap())
        );

        databaseDialog.setTitle("View Database");
        databaseDialog.setMinimumSize(new java.awt.Dimension(1024, 640));

        dbTabbedPane.addTab("Table View", dbViewer);

        dbQueryPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbQueryPanelActionPerformed(evt);
            }
        });
        dbTabbedPane.addTab("Query", dbQueryPanel);

        dbFileNameLabel.setLabelFor(dbFileNameField);
        dbFileNameLabel.setText("File:");

        setDBFileNameButton.setText("Set File");
        setDBFileNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDBFileNameButtonActionPerformed(evt);
            }
        });

        resetDBFilePathButton.setText("Reset Path");
        resetDBFilePathButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetDBFilePathButtonActionPerformed(evt);
            }
        });

        dbCreateTablesButton.setText("Create Tables");
        dbCreateTablesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbCreateTablesButtonActionPerformed(evt);
            }
        });

        foreignKeysToggle.setSelected(true);
        foreignKeysToggle.setText("Foreign Keys Enabled");
        foreignKeysToggle.setEnabled(false);
        foreignKeysToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                foreignKeysToggleActionPerformed(evt);
            }
        });

        updateDBFileCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "(V1.0.0+) Version Update", "(V0.6.0) Add Configuration Table", "(V0.5.0) Update Table Definitions", "(V0.4.0) Add List Size Limit Column", "(V0.3.0) Update Table Definitions (Old)", "(V0.2.0) Add Prefixes", "(V0.1.0) Add List Flags Column", "(V0.0.1) Update Column Names", "(V0.0.0) Rename Links Column" }));

        updateDBFileButton.setText("Update Database File");
        updateDBFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateDBFileButtonActionPerformed(evt);
            }
        });

        dbRemoveUnusedDataButton.setText("Remove Unused Data");
        dbRemoveUnusedDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbRemoveUnusedDataButtonActionPerformed(evt);
            }
        });

        dbUpdateUsedPrefixesButton.setText("Update Link Prefixes");
        dbUpdateUsedPrefixesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbUpdateUsedPrefixesButtonActionPerformed(evt);
            }
        });

        dbRemoveDuplDataButton.setText("Remove Duplicate Data");
        dbRemoveDuplDataButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbRemoveDuplDataButtonActionPerformed(evt);
            }
        });

        dbResetIDsButton.setText("Reset IDs");
        dbResetIDsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbResetIDsButtonActionPerformed(evt);
            }
        });

        dbPropPanel.setLayout(new java.awt.GridBagLayout());

        dbVersionTextLabel.setLabelFor(dbVersionLabel);
        dbVersionTextLabel.setText("Database Version:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        dbPropPanel.add(dbVersionTextLabel, gridBagConstraints);

        dbVersionLabel.setText("0.0.0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        dbPropPanel.add(dbVersionLabel, gridBagConstraints);

        dbFileSizeTextLabel.setLabelFor(dbFileSizeLabel);
        dbFileSizeTextLabel.setText("Database File Size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        dbPropPanel.add(dbFileSizeTextLabel, gridBagConstraints);

        dbFileSizeLabel.setText("N/A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.weightx = 0.9;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        dbPropPanel.add(dbFileSizeLabel, gridBagConstraints);

        dbUUIDTextLabel.setLabelFor(dbUUIDLabel);
        dbUUIDTextLabel.setText("Database UUID:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        dbPropPanel.add(dbUUIDTextLabel, gridBagConstraints);

        dbUUIDLabel.setText("N/A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        dbPropPanel.add(dbUUIDLabel, gridBagConstraints);

        dbLastModTextLabel.setLabelFor(dbLastModLabel);
        dbLastModTextLabel.setText("Database Last Mod:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        dbPropPanel.add(dbLastModTextLabel, gridBagConstraints);

        dbLastModLabel.setText("N/A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        dbPropPanel.add(dbLastModLabel, gridBagConstraints);

        linkCountTextLabel.setLabelFor(linkCountLabel);
        linkCountTextLabel.setText("Link Count:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        dbPropPanel.add(linkCountTextLabel, gridBagConstraints);

        linkCountLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        dbPropPanel.add(linkCountLabel, gridBagConstraints);

        shownTotalSizeTextLabel.setLabelFor(shownTotalSizeLabel);
        shownTotalSizeTextLabel.setText("Shown Lists Total Size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        dbPropPanel.add(shownTotalSizeTextLabel, gridBagConstraints);

        shownTotalSizeLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        dbPropPanel.add(shownTotalSizeLabel, gridBagConstraints);

        allTotalSizeTextLabel.setLabelFor(allTotalSizeLabel);
        allTotalSizeTextLabel.setText("All Lists Total Size:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        dbPropPanel.add(allTotalSizeTextLabel, gridBagConstraints);

        allTotalSizeLabel.setText("0");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        dbPropPanel.add(allTotalSizeLabel, gridBagConstraints);

        programIDTextLabel.setText("Program ID:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 6, 0);
        dbPropPanel.add(programIDTextLabel, gridBagConstraints);

        programIDLabel.setText("N/A");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 6, 6, 0);
        dbPropPanel.add(programIDLabel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.weighty = 0.9;
        dbPropPanel.add(filler2, gridBagConstraints);

        dbUpdateLastModButton.setText("Update Last Mod");
        dbUpdateLastModButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbUpdateLastModButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dbFilePanelLayout = new javax.swing.GroupLayout(dbFilePanel);
        dbFilePanel.setLayout(dbFilePanelLayout);
        dbFilePanelLayout.setHorizontalGroup(
            dbFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbPropPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(dbFilePanelLayout.createSequentialGroup()
                        .addComponent(updateDBFileCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(updateDBFileButton))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, dbFilePanelLayout.createSequentialGroup()
                        .addComponent(dbFileNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbFileNameField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setDBFileNameButton, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(dbFilePanelLayout.createSequentialGroup()
                        .addGroup(dbFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(dbFilePanelLayout.createSequentialGroup()
                                .addComponent(resetDBFilePathButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbCreateTablesButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbUpdateUsedPrefixesButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbUpdateLastModButton))
                            .addGroup(dbFilePanelLayout.createSequentialGroup()
                                .addComponent(foreignKeysToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(dbRemoveUnusedDataButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbRemoveDuplDataButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbResetIDsButton)))
                        .addGap(0, 33, Short.MAX_VALUE)))
                .addContainerGap())
        );
        dbFilePanelLayout.setVerticalGroup(
            dbFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbFilePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbFileNameLabel)
                    .addComponent(dbFileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setDBFileNameButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resetDBFilePathButton)
                    .addComponent(dbCreateTablesButton)
                    .addComponent(dbUpdateUsedPrefixesButton)
                    .addComponent(dbUpdateLastModButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(foreignKeysToggle)
                    .addComponent(dbRemoveUnusedDataButton)
                    .addComponent(dbRemoveDuplDataButton)
                    .addComponent(dbResetIDsButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbFilePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(updateDBFileButton)
                    .addComponent(updateDBFileCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(dbPropPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                .addContainerGap())
        );

        dbTabbedPane.addTab("Database File", dbFilePanel);

        dbPrefixTable.setAutoCreateRowSorter(true);
        dbPrefixTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {

            }
        ));
        dbPrefixTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        dbPrefixScrollPane.setViewportView(dbPrefixTable);

        jLabel9.setLabelFor(prefixField);
        jLabel9.setText("Prefix:");

        prefixField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPrefixButtonActionPerformed(evt);
            }
        });

        addPrefixButton.setText("Add Prefix");
        addPrefixButton.setEnabled(false);
        addPrefixButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addPrefixButtonActionPerformed(evt);
            }
        });

        removePrefixButton.setText("Remove");
        removePrefixButton.setEnabled(false);
        removePrefixButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removePrefixButtonActionPerformed(evt);
            }
        });

        jLabel14.setLabelFor(prefixThresholdSpinner);
        jLabel14.setText("Threshold:");

        prefixThresholdSpinner.setModel(new javax.swing.SpinnerNumberModel(100, 10, null, 1));

        jLabel15.setLabelFor(prefixSeparatorField);
        jLabel15.setText("Separators:");

        prefixApplyButton.setText("Apply");
        prefixApplyButton.setEnabled(false);
        prefixApplyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefixApplyButtonActionPerformed(evt);
            }
        });

        prefixCopyButton.setText("Copy");
        prefixCopyButton.setEnabled(false);
        prefixCopyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                prefixCopyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dbPrefixesPanelLayout = new javax.swing.GroupLayout(dbPrefixesPanel);
        dbPrefixesPanel.setLayout(dbPrefixesPanelLayout);
        dbPrefixesPanelLayout.setHorizontalGroup(
            dbPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbPrefixesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbPrefixScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                    .addGroup(dbPrefixesPanelLayout.createSequentialGroup()
                        .addComponent(jLabel9)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prefixField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(addPrefixButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removePrefixButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prefixCopyButton))
                    .addGroup(dbPrefixesPanelLayout.createSequentialGroup()
                        .addComponent(jLabel14)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prefixThresholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel15)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prefixSeparatorField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(prefixApplyButton)))
                .addContainerGap())
        );
        dbPrefixesPanelLayout.setVerticalGroup(
            dbPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbPrefixesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbPrefixScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 269, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel9)
                    .addComponent(prefixField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(addPrefixButton)
                    .addComponent(removePrefixButton)
                    .addComponent(prefixCopyButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel14)
                    .addComponent(prefixThresholdSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel15)
                    .addComponent(prefixSeparatorField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(prefixApplyButton))
                .addGap(7, 7, 7))
        );

        dbTabbedPane.addTab("Prefixes", dbPrefixesPanel);

        dbUsedPrefixTable.setAutoCreateRowSorter(true);
        dbUsedPrefixTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dbUsedPrefixScrollPane.setViewportView(dbUsedPrefixTable);

        jLabel20.setLabelFor(dbUsedPrefixCombo);
        jLabel20.setText("Search For Prefix:");

        dbUsedPrefixCombo.setMaximumRowCount(16);
        dbUsedPrefixCombo.setEnabled(false);
        dbUsedPrefixCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbUsedPrefixSearchButtonActionPerformed(evt);
            }
        });

        jLabel22.setLabelFor(dbUsedPrefixSizeLabel);
        jLabel22.setText("Size:");

        dbUsedPrefixSizeLabel.setText("0");

        javax.swing.GroupLayout dbUsedPrefixesPanelLayout = new javax.swing.GroupLayout(dbUsedPrefixesPanel);
        dbUsedPrefixesPanel.setLayout(dbUsedPrefixesPanelLayout);
        dbUsedPrefixesPanelLayout.setHorizontalGroup(
            dbUsedPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbUsedPrefixesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbUsedPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbUsedPrefixScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                    .addGroup(dbUsedPrefixesPanelLayout.createSequentialGroup()
                        .addComponent(jLabel20)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbUsedPrefixCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel22)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbUsedPrefixSizeLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 80, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        dbUsedPrefixesPanelLayout.setVerticalGroup(
            dbUsedPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbUsedPrefixesPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbUsedPrefixScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbUsedPrefixesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel20)
                    .addComponent(dbUsedPrefixCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbUsedPrefixSizeLabel)
                    .addComponent(jLabel22))
                .addContainerGap())
        );

        dbTabbedPane.addTab("Prefixes Instances", dbUsedPrefixesPanel);

        jLabel8.setLabelFor(dbSearchField);
        jLabel8.setText("Find:");

        dbSearchField.setEnabled(false);

        dbSearchPrefixCheckBox.setText("Prefix:");
        dbSearchPrefixCheckBox.setEnabled(false);
        dbSearchPrefixCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbSearchPrefixCheckBoxActionPerformed(evt);
            }
        });

        dbSearchPrefixCombo.setEnabled(false);

        dbSearchButton.setText("Search");
        dbSearchButton.setEnabled(false);
        dbSearchButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbSearchButtonActionPerformed(evt);
            }
        });

        dbLinkSearchTable.setAutoCreateRowSorter(true);
        dbLinkSearchTable.setSelectionMode(javax.swing.ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        dbLinkSearchScrollPane.setViewportView(dbLinkSearchTable);

        javax.swing.GroupLayout dbLinkSearchPanelLayout = new javax.swing.GroupLayout(dbLinkSearchPanel);
        dbLinkSearchPanel.setLayout(dbLinkSearchPanelLayout);
        dbLinkSearchPanelLayout.setHorizontalGroup(
            dbLinkSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbLinkSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbLinkSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbLinkSearchScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                    .addGroup(dbLinkSearchPanelLayout.createSequentialGroup()
                        .addGroup(dbLinkSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(dbSearchPrefixCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(dbLinkSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(dbSearchPrefixCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(dbLinkSearchPanelLayout.createSequentialGroup()
                                .addComponent(dbSearchField)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(dbSearchButton)))))
                .addContainerGap())
        );
        dbLinkSearchPanelLayout.setVerticalGroup(
            dbLinkSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbLinkSearchPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbLinkSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel8)
                    .addComponent(dbSearchField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbSearchButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbLinkSearchPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(dbSearchPrefixCheckBox)
                    .addComponent(dbSearchPrefixCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(dbLinkSearchScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 264, Short.MAX_VALUE)
                .addContainerGap())
        );

        dbTabbedPane.addTab("Link Search", dbLinkSearchPanel);

        dbListTable.setAutoCreateRowSorter(true);
        dbListTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        dbListScrollPane.setViewportView(dbListTable);

        jLabel17.setLabelFor(dbListIDCombo);
        jLabel17.setText("List ID:");

        dbListIDCombo.setMaximumRowCount(16);
        dbListIDCombo.setEnabled(false);
        dbListIDCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbListIDComboActionPerformed(evt);
            }
        });

        jLabel18.setLabelFor(dbListNameField);
        jLabel18.setText("Name:");

        jLabel19.setLabelFor(dbListFlagsField);
        jLabel19.setText("Flags:");

        dbListFlagsField.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("#0"))));

        dbListSizeLimitToggle.setText("Size Limit:");
        dbListSizeLimitToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbListSizeLimitToggleActionPerformed(evt);
            }
        });

        dbListSizeLimitSpinner.setModel(new javax.swing.SpinnerNumberModel(10000, 1, null, 1));
        dbListSizeLimitSpinner.setEnabled(false);

        dbListEditApplyButton.setText("Apply");
        dbListEditApplyButton.setEnabled(false);
        dbListEditApplyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbListEditApplyButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout dbListPanelLayout = new javax.swing.GroupLayout(dbListPanel);
        dbListPanel.setLayout(dbListPanelLayout);
        dbListPanelLayout.setHorizontalGroup(
            dbListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbListScrollPane)
                    .addGroup(dbListPanelLayout.createSequentialGroup()
                        .addComponent(jLabel17)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbListIDCombo, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel18)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbListNameField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel19)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbListFlagsField, javax.swing.GroupLayout.PREFERRED_SIZE, 85, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dbListSizeLimitToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbListSizeLimitSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbListEditApplyButton)))
                .addContainerGap())
        );
        dbListPanelLayout.setVerticalGroup(
            dbListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbListPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbListScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 299, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(dbListPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel17)
                    .addComponent(dbListIDCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel18)
                    .addComponent(dbListNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel19)
                    .addComponent(dbListFlagsField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbListSizeLimitToggle)
                    .addComponent(dbListSizeLimitSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(dbListEditApplyButton))
                .addContainerGap())
        );

        dbTabbedPane.addTab("Lists", dbListPanel);

        configTable.setAutoCreateRowSorter(true);
        configScrollPane.setViewportView(configTable);

        dbTabbedPane.addTab("Config", configScrollPane);

        dbTableTable.setAutoCreateRowSorter(true);
        dbTableTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        dbTableScrollPane.setViewportView(dbTableTable);

        dbTableStructText.setEditable(false);
        dbTableStructText.setColumns(20);
        dbTableStructText.setLineWrap(true);
        dbTableStructText.setRows(5);
        dbTableStructText.setWrapStyleWord(true);
        jScrollPane2.setViewportView(dbTableStructText);

        javax.swing.GroupLayout dbTablePanelLayout = new javax.swing.GroupLayout(dbTablePanel);
        dbTablePanel.setLayout(dbTablePanelLayout);
        dbTablePanelLayout.setHorizontalGroup(
            dbTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbTablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(dbTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                    .addComponent(jScrollPane2))
                .addContainerGap())
        );
        dbTablePanelLayout.setVerticalGroup(
            dbTablePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dbTablePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbTableScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 202, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 120, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        dbTabbedPane.addTab("DB Structure", dbTablePanel);

        dbCreatePrefixTree.setRootVisible(false);
        dbCreatePrefixTree.setShowsRootHandles(true);
        dbCreatePrefixScrollPane.setViewportView(dbCreatePrefixTree);

        dbTabbedPane.addTab("Create Prefix Test", dbCreatePrefixScrollPane);

        dbRefreshButton.setText("Refresh");
        dbRefreshButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbRefreshButtonActionPerformed(evt);
            }
        });

        showSchemaToggle.setText("Show Schema");
        showSchemaToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbRefreshButtonActionPerformed(evt);
            }
        });

        backupDBButton.setText("Make Backup");
        backupDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backupDBButtonActionPerformed(evt);
            }
        });

        printDBButton.setText("Print Data");
        printDBButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printDBButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout databaseDialogLayout = new javax.swing.GroupLayout(databaseDialog.getContentPane());
        databaseDialog.getContentPane().setLayout(databaseDialogLayout);
        databaseDialogLayout.setHorizontalGroup(
            databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(dbTabbedPane)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, databaseDialogLayout.createSequentialGroup()
                        .addComponent(showSchemaToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(printDBButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(backupDBButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(dbRefreshButton)))
                .addContainerGap())
        );
        databaseDialogLayout.setVerticalGroup(
            databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(databaseDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(dbTabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(databaseDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(dbRefreshButton)
                    .addComponent(showSchemaToggle)
                    .addComponent(backupDBButton)
                    .addComponent(printDBButton))
                .addContainerGap())
        );

        searchDialog.setTitle("Find...");
        searchDialog.setMinimumSize(new java.awt.Dimension(517, 190));
        searchDialog.setResizable(false);
        searchDialog.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                searchDialogComponentMoved(evt);
            }
        });

        searchPanel.setEnabled(false);
        searchPanel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchPanelActionPerformed(evt);
            }
        });
        searchPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                searchPanelPropertyChange(evt);
            }
        });
        searchDialog.getContentPane().add(searchPanel, java.awt.BorderLayout.CENTER);

        listManipulator.setBottomAccessory(listManipSelCountPanel);

        linkEditPane.setMessage("Enter the link to use:");
        linkEditPane.setMessageType(3);
        linkEditPane.setOptionType(2);
        linkEditPane.setWantsInput(true);
        linkEditPane.setMaximumSize(new java.awt.Dimension(32769, 120));
        linkEditPane.setMinimumSize(new java.awt.Dimension(560, 120));
        linkEditPane.setPreferredSize(new java.awt.Dimension(560, 120));

        copyOrMoveListSelector.setAcceptButtonText("Copy");
        copyOrMoveListSelector.setBottomAccessory(copyOrMoveSelCountPanel);

        listSetOpDialog.setTitle("List Set Operation Dialog");
        listSetOpDialog.setMinimumSize(new java.awt.Dimension(400, 200));

        listAOpLabel.setText("List A:");

        listAOpCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listOpComboActionPerformed(evt);
            }
        });

        listBOpLabel.setText("List B:");

        listBOpCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listOpComboActionPerformed(evt);
            }
        });

        listOpLabel.setText("Operation:");

        listOperationCombo.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "C = A ∪ B", "C = A ∩ B", "C = A ⨁ B", "C = A - B", " " }));

        listSetOpCancelButton.setText("Cancel");
        listSetOpCancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listSetOpCancelButtonActionPerformed(evt);
            }
        });

        listSetOpApplyButton.setText("Apply");
        listSetOpApplyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listSetOpApplyButtonActionPerformed(evt);
            }
        });

        listCOpCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listOpComboActionPerformed(evt);
            }
        });

        listCOpLabel.setText("List C:");

        javax.swing.GroupLayout listSetOpDialogLayout = new javax.swing.GroupLayout(listSetOpDialog.getContentPane());
        listSetOpDialog.getContentPane().setLayout(listSetOpDialogLayout);
        listSetOpDialogLayout.setHorizontalGroup(
            listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(listSetOpDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(listSetOpDialogLayout.createSequentialGroup()
                        .addGroup(listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(listAOpLabel)
                            .addComponent(listBOpLabel)
                            .addComponent(listCOpLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(listBOpCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(listAOpCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(listCOpCombo, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(listSetOpDialogLayout.createSequentialGroup()
                        .addComponent(listOpLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listOperationCombo, 0, 315, Short.MAX_VALUE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, listSetOpDialogLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(listSetOpApplyButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listSetOpCancelButton)))
                .addContainerGap())
        );
        listSetOpDialogLayout.setVerticalGroup(
            listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(listSetOpDialogLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listAOpLabel)
                    .addComponent(listAOpCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listBOpLabel)
                    .addComponent(listBOpCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listCOpLabel)
                    .addComponent(listCOpCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listOpLabel)
                    .addComponent(listOperationCombo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(listSetOpDialogLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listSetOpCancelButton)
                    .addComponent(listSetOpApplyButton, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        dropboxSetupPanel.setDialogTitle("Enter Dropbox Authorization Code");

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle(PROGRAM_NAME);
        setLocationByPlatform(true);
        setMinimumSize(new java.awt.Dimension(720, 480));
        setPreferredSize(new java.awt.Dimension(720, 480));
        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentMoved(java.awt.event.ComponentEvent evt) {
                formComponentMoved(evt);
            }
            public void componentResized(java.awt.event.ComponentEvent evt) {
                formComponentResized(evt);
            }
        });
        addWindowStateListener(new java.awt.event.WindowStateListener() {
            public void windowStateChanged(java.awt.event.WindowEvent evt) {
                formWindowStateChanged(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        newLinkLabel.setText("Edit Link:");

        linkTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLinkButtonActionPerformed(evt);
            }
        });

        pasteAndAddButton.setText("Add From Clipboard");
        pasteAndAddButton.setEnabled(false);

        newLinkButton.setText("New Link");
        newLinkButton.setEnabled(false);
        newLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLinkButtonActionPerformed(evt);
            }
        });

        editLinkButton.setText("Edit Link");
        editLinkButton.setEnabled(false);
        editLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLinkButtonActionPerformed(evt);
            }
        });

        manageLinksButton.setText("Manage Links");
        manageLinksButton.setEnabled(false);
        manageLinksButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageLinksButtonActionPerformed(evt);
            }
        });

        removeLinkButton.setText("Remove Link");
        removeLinkButton.setEnabled(false);
        removeLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeLinkButtonActionPerformed(evt);
            }
        });

        copyLinkButton.setText("Copy Link");
        copyLinkButton.setEnabled(false);
        copyLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyLinkButtonActionPerformed(evt);
            }
        });

        openLinkButton.setText("Open Link");
        openLinkButton.setEnabled(false);
        openLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openLinkButtonActionPerformed(evt);
            }
        });

        tabsPanelDisplay.setLayout(new java.awt.CardLayout());

        allListsTabsPanel.setName("allLists"); // NOI18N
        allListsTabsPanel.setShowingHiddenLists(true);
        allListsTabsPanel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                listsTabsPanelStateChanged(evt);
            }
        });
        allListsTabsPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                listsTabsPanelPropertyChange(evt);
            }
        });
        allListsTabsPanel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listsTabsPanelValueChanged(evt);
            }
        });
        tabsPanelDisplay.add(allListsTabsPanel, "allLists");

        shownListsTabsPanel.setName("shownLists"); // NOI18N
        shownListsTabsPanel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                listsTabsPanelStateChanged(evt);
            }
        });
        shownListsTabsPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                listsTabsPanelPropertyChange(evt);
            }
        });
        shownListsTabsPanel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listsTabsPanelValueChanged(evt);
            }
        });
        tabsPanelDisplay.add(shownListsTabsPanel, "shownLists");

        fileMenu.setText("File");

        updateDatabaseItem.setText("Update Database");
        updateDatabaseItem.setToolTipText("Updates the database to reflect the current lists.");
        updateDatabaseItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateDatabaseItemActionPerformed(evt);
            }
        });
        fileMenu.add(updateDatabaseItem);

        updateListsItem.setText("Refresh Lists");
        updateListsItem.setToolTipText("Reloads the unedited lists from the database");
        updateListsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                updateListsItemActionPerformed(evt);
            }
        });
        fileMenu.add(updateListsItem);

        reloadListsItem.setText("Update Lists");
        reloadListsItem.setToolTipText("Reloads all the lists from the database");
        reloadListsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reloadListsItemActionPerformed(evt);
            }
        });
        fileMenu.add(reloadListsItem);

        exportListsItem.setText("Export Lists To Files");
        exportListsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportListsItemActionPerformed(evt);
            }
        });
        fileMenu.add(exportListsItem);
        fileMenu.add(jSeparator2);

        uploadDBItem.setText("Upload Database");
        uploadDBItem.setEnabled(false);
        uploadDBItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uploadDBItemActionPerformed(evt);
            }
        });
        fileMenu.add(uploadDBItem);

        downloadDBItem.setText("Download Database");
        downloadDBItem.setEnabled(false);
        downloadDBItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                downloadDBItemActionPerformed(evt);
            }
        });
        fileMenu.add(downloadDBItem);
        fileMenu.add(dbConfigSeparator);

        saveConfigItem.setText("Save Config To File");
        saveConfigItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveConfigItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveConfigItem);

        loadConfigItem.setText("Load Config From File");
        loadConfigItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadConfigItemActionPerformed(evt);
            }
        });
        fileMenu.add(loadConfigItem);
        fileMenu.add(configExitSeparator);

        exitButton.setText("Exit");
        exitButton.setActionCommand(EXIT_COMMAND);
        exitButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitButtonActionPerformed(evt);
            }
        });
        fileMenu.add(exitButton);

        menuBar.add(fileMenu);

        listMenu.setText("Lists");

        manageListsItem.setText("Manage Lists");
        manageListsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageListsItemActionPerformed(evt);
            }
        });
        listMenu.add(manageListsItem);
        listMenu.add(jSeparator1);

        makeListReadOnlyMenuAll.setText("Set List To Read Only");
        makeListReadOnlyMenuAll.setActionCommand(MAKE_LIST_READ_ONLY_ACTION_KEY);
        makeListReadOnlyMenuAll.setEnabled(false);
        listMenu.add(makeListReadOnlyMenuAll);

        makeListReadOnlyMenuShown.setText("Set List To Read Only");
        makeListReadOnlyMenuShown.setActionCommand(MAKE_LIST_READ_ONLY_ACTION_KEY);
        makeListReadOnlyMenuShown.setEnabled(false);
        listMenu.add(makeListReadOnlyMenuShown);

        hideListsMenu.setText("Set Hidden Lists");
        hideListsMenu.setActionCommand(HIDE_LIST_ACTION_KEY);
        hideListsMenu.setEnabled(false);
        hideListsMenu.add(jSeparator4);

        showAllListsItem.setText("Show All Lists");
        showAllListsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllListsItemActionPerformed(evt);
            }
        });
        hideListsMenu.add(showAllListsItem);

        hideAllListsItem.setText("Hide All Lists");
        hideAllListsItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hideAllListsItemActionPerformed(evt);
            }
        });
        hideListsMenu.add(hideAllListsItem);

        listMenu.add(hideListsMenu);

        showHiddenListsToggle.setText("Show Hidden Lists");
        showHiddenListsToggle.setActionCommand(HIDDEN_LISTS_TOGGLE_COMMAND);
        showHiddenListsToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showHiddenListsToggleActionPerformed(evt);
            }
        });
        listMenu.add(showHiddenListsToggle);

        menuBar.add(listMenu);

        searchMenu.setText("Search");

        searchMenuItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        searchMenuItem.setText("Find...");
        searchMenuItem.setEnabled(false);
        searchMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                searchMenuItemActionPerformed(evt);
            }
        });
        searchMenu.add(searchMenuItem);

        menuBar.add(searchMenu);

        optionsMenu.setText("Options");

        progressDisplay.setProgressDisplayed(true);
        progressDisplay.setUpdateEnabled(true);
        progressDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                progressDisplayActionPerformed(evt);
            }
        });
        optionsMenu.add(progressDisplay);

        alwaysOnTopToggle.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        alwaysOnTopToggle.setText("Always On Top");
        alwaysOnTopToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                alwaysOnTopToggleActionPerformed(evt);
            }
        });
        optionsMenu.add(alwaysOnTopToggle);

        doubleNewLinesToggle.setText("Add Blank Lines In Files");
        doubleNewLinesToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                doubleNewLinesToggleActionPerformed(evt);
            }
        });
        optionsMenu.add(doubleNewLinesToggle);

        linkOperationToggle.setSelected(true);
        linkOperationToggle.setText("Enable Link Operations");
        linkOperationToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                linkOperationToggleActionPerformed(evt);
            }
        });
        optionsMenu.add(linkOperationToggle);

        hiddenLinkOperationToggle.setText("Enable Link Operations (Hidden Lists)");
        hiddenLinkOperationToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hiddenLinkOperationToggleActionPerformed(evt);
            }
        });
        optionsMenu.add(hiddenLinkOperationToggle);

        showDBErrorDetailsToggle.setSelected(true);
        showDBErrorDetailsToggle.setText("Show Database Error Details");
        showDBErrorDetailsToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showDBErrorDetailsToggleActionPerformed(evt);
            }
        });
        optionsMenu.add(showDBErrorDetailsToggle);

        setDBLocationItem.setText("Set Database Location");
        setDBLocationItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDBLocationItemActionPerformed(evt);
            }
        });
        optionsMenu.add(setDBLocationItem);

        syncDBToggle.setSelected(true);
        syncDBToggle.setText("Sync Database");
        syncDBToggle.setEnabled(false);
        syncDBToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                syncDBToggleActionPerformed(evt);
            }
        });
        optionsMenu.add(syncDBToggle);

        autosaveMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autosaveMenuActionPerformed(evt);
            }
        });
        autosaveMenu.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                autosaveMenuPropertyChange(evt);
            }
        });
        optionsMenu.add(autosaveMenu);

        autoHideMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoHideMenuActionPerformed(evt);
            }
        });
        autoHideMenu.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                autoHideMenuPropertyChange(evt);
            }
        });
        optionsMenu.add(autoHideMenu);

        menuBar.add(optionsMenu);

        debugMenu.setText("Debug");
        debugMenu.add(slowTestToggle);

        activeToggle.setSelected(true);
        activeToggle.setText("Input Enabled");
        activeToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activeToggleActionPerformed(evt);
            }
        });
        debugMenu.add(activeToggle);

        printDataItem.setText("Print Data");
        printDataItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printDataItemActionPerformed(evt);
            }
        });
        debugMenu.add(printDataItem);

        dbViewItem.setText("View Database");
        dbViewItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbViewItemActionPerformed(evt);
            }
        });
        debugMenu.add(dbViewItem);

        showIDsToggle.setText("Show IDs");
        showIDsToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showIDsToggleActionPerformed(evt);
            }
        });
        debugMenu.add(showIDsToggle);

        printAutosaveEventsToggle.setText("Print Autosave Events");
        debugMenu.add(printAutosaveEventsToggle);

        printAutoHideEventsToggle.setText("Print Auto-Hide Events");
        debugMenu.add(printAutoHideEventsToggle);

        printListPropChangeToggle.setText("Print List Property Changes");
        debugMenu.add(printListPropChangeToggle);

        clearSelTabItem.setText("Clear Selected Tab");
        clearSelTabItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSelTabItemActionPerformed(evt);
            }
        });
        debugMenu.add(clearSelTabItem);

        clearListSelItem.setText("Clear List Selection");
        clearListSelItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearListSelItemActionPerformed(evt);
            }
        });
        debugMenu.add(clearListSelItem);

        listSetOpItem.setText("List Set Operations");
        listSetOpItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listSetOpItemActionPerformed(evt);
            }
        });
        debugMenu.add(listSetOpItem);

        jMenu2.setText("Dropbox Tests");

        dbxPrintButton.setText("Print Dropbox Data");
        dbxPrintButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dbxPrintButtonActionPerformed(evt);
            }
        });
        jMenu2.add(dbxPrintButton);

        setDropboxTestButton.setText("Set Dropbox Access Token");
        setDropboxTestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setDropboxTestButtonActionPerformed(evt);
            }
        });
        jMenu2.add(setDropboxTestButton);

        dropboxRefreshTestButton.setText("Refresh Dropbox Token");
        dropboxRefreshTestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dropboxRefreshTestButtonActionPerformed(evt);
            }
        });
        jMenu2.add(dropboxRefreshTestButton);

        debugMenu.add(jMenu2);

        menuBar.add(debugMenu);

        setJMenuBar(menuBar);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabsPanelDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newLinkLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(linkTextField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pasteAndAddButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newLinkButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editLinkButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(manageLinksButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, 122, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(removeLinkButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(copyLinkButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(openLinkButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabsPanelDisplay, javax.swing.GroupLayout.DEFAULT_SIZE, 370, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(linkTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newLinkLabel)
                    .addComponent(pasteAndAddButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(newLinkButton)
                        .addComponent(editLinkButton)
                        .addComponent(manageLinksButton))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(openLinkButton)
                        .addComponent(copyLinkButton)
                        .addComponent(removeLinkButton))
                    .addComponent(progressBar, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    /**
     * This updates the progress bar when the progress display settings are 
     * changed.
     * @param evt The ActionEvent.
     */
    private void progressDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_progressDisplayActionPerformed
        progressDisplay.updateProgressString(progressBar);
        config.setProgressDisplaySetting(progressDisplay.getDisplaySettings());
    }//GEN-LAST:event_progressDisplayActionPerformed
    /**
     * This toggles whether the program is set to be always on top.
     * @param evt The ActionEvent.
     */
    private void alwaysOnTopToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_alwaysOnTopToggleActionPerformed
        super.setAlwaysOnTop(alwaysOnTopToggle.isSelected());
        config.setAlwaysOnTop(alwaysOnTopToggle.isSelected());
    }//GEN-LAST:event_alwaysOnTopToggleActionPerformed
    /**
     * This toggles whether blank lines will be added to text files generated by 
     * this program.
     * @param evt The ActionEvent.
     */
    private void doubleNewLinesToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_doubleNewLinesToggleActionPerformed
        config.setAddBlankLines(doubleNewLinesToggle.isSelected());
    }//GEN-LAST:event_doubleNewLinesToggleActionPerformed
    /**
     * This toggles whether the copy and open buttons are enabled.
     * @param evt The ActionEvent.
     */
    private void linkOperationToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_linkOperationToggleActionPerformed
        updateSelectedLink();
        hiddenLinkOperationToggle.setEnabled(active&&linkOperationToggle.isSelected());
        config.setLinkOperationsEnabled(linkOperationToggle.isSelected());
    }//GEN-LAST:event_linkOperationToggleActionPerformed
    /**
     * This tests enabling and disabling the input.
     * @param evt The ActionEvent.
     */
    private void activeToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activeToggleActionPerformed
        setInputEnabled(activeToggle.isSelected());
    }//GEN-LAST:event_activeToggleActionPerformed
    /**
     * This prints a bunch of stuff to help with debugging.
     * @param evt The ActionEvent
     */
    private void printDataItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printDataItemActionPerformed
        System.out.println("Is Input Enabled: " + isInputEnabled());
        System.out.println("Is Loading Files: " + isLoadingFiles());
        System.out.println("Is Saving Files: " + isSavingFiles());
        System.out.println("Program ID: " + config.getProgramID());
        System.out.println("Config File: " + getConfigFile());
        System.out.println("Database File Name: " + config.getDatabaseFileName());
        File file = getDatabaseFile();
        System.out.println("Database File: " + file);
        System.out.print("Canonical Database File: ");
        try {
            System.out.println(file.getCanonicalPath());
        } catch (IOException ex) {
            System.out.println(ex);
        }
        System.out.println("Fully Loaded: " + fullyLoaded);
        System.out.println("Selected Tabs Panel: " + getSelectedTabsPanel().getName());
        System.out.println("Selected List: " + getSelectedList());
        System.out.println("Can Act Upon Selected List: " + canActUponSelectedList());
        System.out.println("Program Will Close Once Saved: " + (saver != null && saver.getExitAfterSaving()));
        System.out.println();
        
        for (LinksListTabsPanel tabsPanel : listsTabPanels){
            System.out.println("Tabs Panel: " + tabsPanel.getName());
            System.out.println("\tLists Edited: " + tabsPanel.isEdited() + " " + 
                    tabsPanel.isStructureEdited() + " " + tabsPanel.getListsEdited());
            System.out.println("\tHidden Lists Edited: " + tabsPanel.getHiddenListsEdited());
            System.out.println("\tSelected Tab Index: " + tabsPanel.getSelectedIndex());
            System.out.println("\tSelected Tab: " + tabsPanel.getSelectedComponent());
            System.out.println("\tSelected List: " + tabsPanel.getSelectedList());
            System.out.println("\tSelected ListID: " + tabsPanel.getSelectedListID());
            System.out.println("\tSelected List Model: " + tabsPanel.getSelectedModel());
            System.out.println("\tTab Amount: " + tabsPanel.getLists().size() + " (" +tabsPanel.getTabCount()+")");
            System.out.println("\tListIDs: " + tabsPanel.getListIDs());
            System.out.println();
        }
        
        System.out.println("Autosave Running: " + autosaveMenu.isRunning());
        System.out.println("Autosave Frequency Index: " + autosaveMenu.getFrequencyIndex());
        System.out.println("Autosave Frequency: " + autosaveMenu.getFrequency());
        System.out.println("Autosave Delay: " + autosaveMenu.getAutosaveTimer().getDelay());
        System.out.println("Autosave Pause Timer Running: "+autosaveMenu.isPauseTimerRunning());
        System.out.println("Autosave Pause Initial Delay: " + autosaveMenu.getPauseTimer().getInitialDelay());
        System.out.println("Autosave Paused: " + autosaveMenu.isPaused());
        System.out.println("Autosave Enabled: " + autosaveMenu.isEnabled());
        System.out.println();
        System.out.println("AutoHide Running: " + autoHideMenu.isRunning());
        System.out.println("AutoHide Was Running: " + autoHideMenu.wasRunning());
        System.out.println("AutoHide Wait Duration Index: " + autoHideMenu.getDurationIndex());
        System.out.println("AutoHide Wait Duration: " + autoHideMenu.getDuration());
        System.out.println("AutoHide Delay: " + autoHideMenu.getAutoHideTimer().getDelay());
        System.out.println("AutoHide Paused: " + autoHideMenu.isPaused());
        System.out.println("AutoHide Enabled: " + autoHideMenu.isEnabled());
        System.out.println();
        
        System.out.println("Lists: ");
            // A for loop to print out the lists
        for (int i = 0; i < allListsTabsPanel.getLists().size(); i++){
                // Get the list to print out
            LinksListPanel panel = allListsTabsPanel.getLists().get(i);
            System.out.printf("%6d: (listID=%4d, edited=%5b, size=%6d, lastMod=%15d, sizeLimit=%7d, hidden=%5b/%5b) %s %s%n",
                i,
                panel.getListID(),
                panel.isEdited(),
                panel.getModel().size(),
                panel.getLastModified(),
                panel.getSizeLimit(),
                panel.isHidden(),
                !shownListsTabsPanel.getLists().contains(panel),
                panel.getListName(),
                panel.getModel()
            );
        }
        System.out.println();
        
        System.out.println("Configuration: " + config.getProperties().size() + 
                " " + config.getProperties().stringPropertyNames().size());
        config.getProperties().list(System.out);
        System.out.println();
        System.out.println("Stored Configuration:");
        try {
            config.getProperties().store(System.out, GENERAL_CONFIG_HEADER);
        } catch (IOException ex) {
            System.out.println("Error: " + ex);
        }
        System.out.println();
        
        System.out.println(getSize());
        System.out.println(getPreferredSize());
        System.out.println(this.getMaximizedBounds());
        System.out.println(this.getBounds());
        System.out.println("Extended State: " + this.getExtendedState());
        System.out.println(this.getMaximumSize());
        System.out.println(Toolkit.getDefaultToolkit().getScreenSize());
        System.out.println(Toolkit.getDefaultToolkit().getScreenResolution());
    }//GEN-LAST:event_printDataItemActionPerformed
    /**
     * This opens up a dialog window that displays the contents of the database.
     * @param evt The ActionEvent.
     */
    private void dbViewItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbViewItemActionPerformed
        loader = new LoadDatabaseViewer(false);
        loader.execute();
            // This will be true if the database dialog has not been opened before
        if (databaseDialog.isLocationByPlatform())
            databaseDialog.setLocationRelativeTo(this);
        databaseDialog.setVisible(true);
    }//GEN-LAST:event_dbViewItemActionPerformed
    /**
     * This toggles whether the linkIDs and listIDs will be shown.
     * @param evt The ActionEvent.
     */
    private void showIDsToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showIDsToggleActionPerformed
        for (LinksListTabsPanel tabsPanel : listsTabPanels){
            tabsPanel.setInDebug(showIDsToggle.isSelected());
        }
    }//GEN-LAST:event_showIDsToggleActionPerformed
    /**
     * This refreshes the tables that display the contents of the database.
     * @param evt The ActionEvent.
     */
    private void dbRefreshButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbRefreshButtonActionPerformed
        loader = new LoadDatabaseViewer(true);
        loader.execute();
    }//GEN-LAST:event_dbRefreshButtonActionPerformed
    /**
     * This resets the list IDs and link IDs of the lists and links in the 
     * database.
     * @param evt The ActionEvent.
     */
    private void dbResetIDsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbResetIDsButtonActionPerformed
        saver = new ResetDatabaseIDs();
        saver.execute();
    }//GEN-LAST:event_dbResetIDsButtonActionPerformed

    @SuppressWarnings("unchecked")
    private void addPrefixButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addPrefixButtonActionPerformed
            // If the add prefix button is disabled
        if (!addPrefixButton.isEnabled()){
            beep();
            return;
        }
        // TODO: Move this into a SwingWorker (or at least update the links in one)?
        String prefix = prefixField.getText();
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            PrefixMap prefixMap = conn.getPrefixMap();
            Integer key = prefixMap.add(prefix);
            prefixField.setText("");
            ((DefaultTableModel)dbPrefixTable.getModel()).addRow(
                    new Object[]{key,prefixMap.get(key)});
            ((List)dbUsedPrefixCombo.getModel()).add(key+" - "+prefix);
            LinkMap linkMap = conn.getLinkMap();
                // Turn off the connection's auto-commit to group the following 
                // database transactions to improve performance
            conn.setAutoCommit(false);
            Set<Long> outdatedLinks = new LinkedHashSet<>(
                    linkMap.getStartsWith(prefix).navigableKeySet());
            for (Long linkID : outdatedLinks){
                conn.updateLinkPrefix(linkID);
//                progressBar.setValue(progressBar.getValue()+1);
            }
               // Ensure that the database last modified time is updated
            conn.setDatabaseLastModified();
            conn.commit();       // Commit the changes to the database
//            conn.setAutoCommit(true);
//            searchUsedPrefixes(conn,key);
        }
        catch (SQLException ex) {
            getLogger().log(Level.WARNING, 
                    "Could not add prefix \""+prefixField.getText()+"\"", ex);
            JOptionPane.showMessageDialog(this, "Could Not Add Prefix \""+
                prefixField.getText()+"\".\n"+
                        "Database Error: " + ex,
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        loader = new LoadDatabaseViewer(true);
        loader.execute();
    }//GEN-LAST:event_addPrefixButtonActionPerformed

    private void removePrefixButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removePrefixButtonActionPerformed
        int selRow = dbPrefixTable.getSelectedRow();
        if (selRow < 0){
            JOptionPane.showMessageDialog(this, "No Prefix Selected",
                "No Prefix Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
            // TODO: Move this into a SwingWorker?
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            String prefix = conn.getPrefixMap().remove((Integer)
                    dbPrefixTable.getValueAt(selRow, 0));
               // Ensure that the database last modified time is updated
            conn.setDatabaseLastModified();
            getLogger().log(Level.FINER,"Removed prefix \"{0}\"",prefix);
            ((DefaultTableModel)dbPrefixTable.getModel()).removeRow(selRow);
        }
        catch (SQLException | IllegalArgumentException ex) {
            getLogger().log(Level.WARNING,String.format("Could not remove prefix %d: \"%s\"",
                    dbPrefixTable.getValueAt(selRow, 0),
                    dbPrefixTable.getValueAt(selRow, 1)), ex);
            JOptionPane.showMessageDialog(this, "Could Not Remove Prefix "+
                    dbPrefixTable.getValueAt(selRow, 0)+": \""+
                    dbPrefixTable.getValueAt(selRow, 1)+"\".\n"+ 
                            "Database Error: " + ex,
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        loader = new LoadDatabaseViewer(true);
        loader.execute();
    }//GEN-LAST:event_removePrefixButtonActionPerformed
    
    private void setDBFileNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDBFileNameButtonActionPerformed
        config.setDatabaseFileName(dbFileNameField.getText());
        updateDatabaseFileFields();
    }//GEN-LAST:event_setDBFileNameButtonActionPerformed

    private void resetDBFilePathButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetDBFilePathButtonActionPerformed
        config.setDatabaseFileName(null);
        updateDatabaseFileFields();
    }//GEN-LAST:event_resetDBFilePathButtonActionPerformed

    private void backupDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backupDBButtonActionPerformed
        saver = new BackupFileSaver(getDatabaseFile(),"Creating Database Backup");
        saver.execute();
    }//GEN-LAST:event_backupDBButtonActionPerformed
    /**
     * This prints a bunch of stuff related to the database to help with 
     * debugging.
     * @param evt The ActionEvent
     */
    private void printDBButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printDBButtonActionPerformed
        System.out.println("Database File Name: " + config.getDatabaseFileName());
        System.out.println("Database File: " + getDatabaseFile());
            // Try to connect to the database and create an SQL statement for it
        try(LinkDatabaseConnection conn = connect(getDatabaseFile());
            Statement stmt = conn.createStatement()){
            System.out.println("Type Map: " + conn.getTypeMap());
            System.out.println("Schema Name: " + conn.getSchema());
            System.out.println("Transaction Isolation Level: " + conn.getTransactionIsolation());
            System.out.println("Supports Foreign Keys: " + conn.getForeignKeysSupported(stmt));
            System.out.println("Foreign Keys Enabled: " + conn.isForeignKeysEnabled(stmt));
//            System.out.println(conn.getForeignKeysConstraint(stmt));
            DatabaseMetaData metadata = conn.getMetaData();
            System.out.println("Metadata: " + metadata);
            System.out.println();
            System.out.println("Table Creation Queries: ");
            for (String temp : LinkDatabaseConnection.TABLE_CREATION_QUERIES){
                System.out.println(temp);
            }
            System.out.println();
            System.out.println("Tables: " + conn.showTables() + " " + conn.showTables().size());
            System.out.println("Views: " + conn.showViews() + " " + conn.showTables().size());
            System.out.println();
            for (int type : conn.getListTypes()){
                ListIDList listIDList = conn.getListIDs(type);
                System.out.printf("List Type %2d (%2d): (Size: %3d, Total Size: %6d) %s%n",
                        listIDList.getListType(),type,
                        listIDList.size(),listIDList.totalSize(),
                        listIDList);
            }
            System.out.println();
            ListIDList shownLists = conn.getShownListIDs();
            ListIDList allLists = conn.getAllListIDs();
            System.out.println("Shown Lists: " + shownLists + " (Size: " + 
                    shownLists.size() + ", Total Size: " + shownLists.totalSize()+")");
            System.out.println("All Lists: " + allLists + " (Size: " + 
                    allLists.size() + ", Total Size: " + allLists.totalSize()+")");
            System.out.println();
            for (ListContents t : conn.getListDataMap().values()){
                System.out.println(t.getListID() +": " + t.size());
            }
            System.out.println("Total Size: " + conn.getListDataMap().totalSize());
            
            PrefixMap prefixMap = conn.getPrefixMap();
            System.out.println("Prefix Map: " + prefixMap);
            System.out.println("Prefix Map Size: " + prefixMap.size());

            LinkMap linkMap = conn.getLinkMap();
//            System.out.println("Link Map: " + linkMap);
            System.out.println("Link Map Size: " + linkMap.size());

            String[] testValues = {
                "https://www.deviantart.com/miinti/art/boop-419637635",
                "https://www.virustotal.com/",
                "https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ConcurrentMap.html",
                "hts://docs.oracle.com/javase/8/docs/api/java/util/Map.html",
                "https://www.reddit.com/r/pokemon/comments/gdyeli/hex_maniac_by_me/",
                "hello there",
                ""
            };

            for (String temp : testValues){
                System.out.println("Prefixes For \""+temp+"\":");
                NavigableMap<Integer, String> matches = prefixMap.getPrefixes(temp);
                NavigableMap<Integer, String> shortened = prefixMap.getSuffixes(temp);
                for (Integer prefixID : matches.keySet()){
                    System.out.println("\t"+prefixID + ": \"" + matches.get(prefixID) + "\" \""+shortened.get(prefixID)+"\"");
                }
                System.out.println("\tLongest: " + prefixMap.getLongestPrefixIDFor(temp) + " \""+prefixMap.getLongestPrefixFor(temp)+"\"");
            }
            
            System.out.println();
            DatabasePropertyMap dbProperty = conn.getDatabaseProperties();
            System.out.println("Properties: " + dbProperty.propertyNameSet());
            System.out.println(dbProperty.listProperties());
            System.out.println("toProperties():");
            Properties dbProp = dbProperty.toProperties();
            dbProp.list(System.out);
        }
        catch (SQLException | UncheckedSQLException ex) {
            System.out.println("Error: "+ex);
        }
    }//GEN-LAST:event_printDBButtonActionPerformed

    private void dbCreateTablesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbCreateTablesButtonActionPerformed
        try(LinkDatabaseConnection conn = connect(getDatabaseFile());
            Statement stmt = conn.createStatement()){
            conn.createTables(stmt);
        }catch (SQLException | UncheckedSQLException ex) {
            getLogger().log(Level.WARNING,"Error creating database tables", ex);
        }
        loader = new LoadDatabaseViewer(true);
        loader.execute();
    }//GEN-LAST:event_dbCreateTablesButtonActionPerformed

    private void foreignKeysToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_foreignKeysToggleActionPerformed
        config.getSQLiteConfig().enforceForeignKeys(foreignKeysToggle.isSelected());
    }//GEN-LAST:event_foreignKeysToggleActionPerformed

    private void updateDBFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateDBFileButtonActionPerformed
        File file = showOpenFileChooser(databaseUpdateFC, null);
        if (file != null){
            saver = new UpdateDatabase(file,updateDBFileCombo.getSelectedIndex());
            saver.execute();
        }
    }//GEN-LAST:event_updateDBFileButtonActionPerformed
    
    private void showDBErrorDetailsToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showDBErrorDetailsToggleActionPerformed
        config.setDatabaseErrorDetailsAreShown(showDBErrorDetailsToggle.isSelected());
    }//GEN-LAST:event_showDBErrorDetailsToggleActionPerformed
    /**
     * This is an action performed by all the file choosers when the user 
     * approves of the selected file(s).
     * @param evt The ActionEvent.
     */
    private void fileChooserActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileChooserActionPerformed
            // Get the file chooser that was used
        JFileChooser fc = (JFileChooser) evt.getSource();
            // If the user approved of the selection
        if (JFileChooser.APPROVE_SELECTION.equals(evt.getActionCommand())){
            File file = fc.getSelectedFile();           // Get the selected file
            if (file.toString().contains("\"")){        // If the file has quotation marks
                file = FilesExtended.removeQuotations(file);
                fc.setSelectedFile(file);
            }
        }
    }//GEN-LAST:event_fileChooserActionPerformed
    /**
     * This performs the action for the search panel.
     * @param evt The ActionEvent.
     */
    private void searchPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchPanelActionPerformed
            // This gets the direction in which to search in
        Position.Bias direction = null;
            // Get the action for the action command
        switch(evt.getActionCommand()){
                // Close the dialog
            case(LinkSearchPanel.SEARCH_CANCEL_COMMAND):
                searchDialog.setVisible(false);
                return;
                // Search for the next match
            case(LinkSearchPanel.SEARCH_FIND_NEXT_COMMAND):
                direction = Position.Bias.Forward;
                break;
                // Search for the previous match
            case(LinkSearchPanel.SEARCH_FIND_PREVIOUS_COMMAND):
                direction = Position.Bias.Backward;
        }
        if (direction != null){ // If the direction is not null
            linksWorker = new SearchLinks(getSelectedList(),
                direction, searchPanel.getSearchText());
            linksWorker.execute();
        }
    }//GEN-LAST:event_searchPanelActionPerformed
    /**
     * This processes a change to the search settings.
     * @param evt The PropertyChangeEvent.
     */
    private void searchPanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_searchPanelPropertyChange
        switch(evt.getPropertyName()){
            case(LinkSearchPanel.MATCH_SPACES_PROPERTY_CHANGED):
                config.setSearchMatchSpaces(searchPanel.getMatchSpaces());
                return;
            case(LinkSearchPanel.MATCH_CASE_PROPERTY_CHANGED):
                config.setSearchMatchCase(searchPanel.getMatchCase());
                return;
            case(LinkSearchPanel.WRAP_AROUND_PROPERTY_CHANGED):
                config.setSearchWrapAround(searchPanel.getWrapAround());
        }
    }//GEN-LAST:event_searchPanelPropertyChange
    /**
     * This opens the search window.
     * @param evt The ActionEvent.
     */
    private void searchMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_searchMenuItemActionPerformed
            // This will be true if the search window has not been opened before
        if (searchDialog.isLocationByPlatform())
            searchDialog.setLocationRelativeTo(this);
        searchDialog.setVisible(true);
    }//GEN-LAST:event_searchMenuItemActionPerformed
    /**
     * This resets the link text field to be blank, and then updates the 
     * buttons.
     */
    private void resetLinkField(){
        linkTextField.setText("");
        updateButtons();
    }
    /**
     * This adds a link to the currently selected list.
     * @param evt The ActionEvent
     */
    private void newLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newLinkButtonActionPerformed
            // If the input is disabled, no link has been entered, or no list
        if (!canAddNewLink()){  // is selected
            beep();
            linkTextField.grabFocus();
            return;
        }
        getSelectedTabsPanel().getSelectedModel().add(linkTextField.getText().trim());

        resetLinkField();
        linkTextField.grabFocus();
    }//GEN-LAST:event_newLinkButtonActionPerformed
    /**
     * This allows the user to edit the currently selected link.
     * @param evt The ActionEvent.
     */
    private void editLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLinkButtonActionPerformed
            // If we can't alter the currently selected list
        if (!canActUponSelectedList()){ 
            beep();
            linkTextField.grabFocus();
            return;
        }   // Get the currently selected list
        LinksListPanel selPanel = getSelectedList();
            // Gets the index of the element to edit
        int selIndex = selPanel.getSelectedIndex();
        linkEditPane.setInitialSelectionValue(null);
        linkEditPane.setInitialSelectionValue(selPanel.getModel().get(selIndex));
            // The dialog for editing the selected link
        JDialog dialog = linkEditPane.createDialog(this,"Edit Link...");
        dialog.setVisible(true);
        Object obj = linkEditPane.getValue();   // The option the user selected
            // If the object is a number, and OK was selected
        if (obj instanceof Number && ((Number) obj).equals(JOptionPane.OK_OPTION)){
                // This stores the input from the user
            String temp = (String)linkEditPane.getInputValue();
                // If the user has changed the link
            if (!Objects.equals(temp, selPanel.getModel().get(selIndex)))
                selPanel.getModel().set(selIndex, temp);
        }
        dialog.dispose();
        linkTextField.grabFocus();
    }//GEN-LAST:event_editLinkButtonActionPerformed
    /**
     * This opens a dialog that the user can use to manage the links in the 
     * currently selected list.
     * @param evt The ActionEvent.
     */
    private void manageLinksButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageLinksButtonActionPerformed
        getLogger().entering(this.getClass().getName(), 
                "manageLinksButtonActionPerformed", evt);
            // If we can alter the currently selected list
        if (canActUponSelectedList()){ 
                // Get the currently selected list
            LinksListPanel selPanel = getSelectedList();
            listManipulator.setDialogTitle("Manage "+selPanel.getListName()+"...");
                // Gets the currently selected element in the list
            String sel = selPanel.getSelectedValue();
            listManipulator.setListData(selPanel.getModel());
            if (sel != null)    // If a link is selected in the list
                listManipulator.ensureIndexIsVisible(selPanel.getSelectedIndex());
                // If the user has accepted the changes to the list
            if (listManipulator.showDialog(this) == JListManipulator.ACCEPT_OPTION){
                linksWorker = new ManipulateListWorker(selPanel,listManipulator.getModelList());
                linksWorker.execute();
            }
        }
        else
            beep();
        listManipulator.setPreferredSize(listManipulator.getSize());
            // Set the list manipulator panel's size in the config
        config.setComponentSize(listManipulator);
        linkTextField.grabFocus();
        getLogger().exiting(this.getClass().getName(), 
                "manageLinksButtonActionPerformed");
    }//GEN-LAST:event_manageLinksButtonActionPerformed
    /**
     * This removes the currently selected link from the currently selected 
     * list. This will also select the next index in the list if there is one.
     * @param evt The ActionEvent.
     */
    private void removeLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeLinkButtonActionPerformed
            // If we can alter the currently selected list
        if (canActUponSelectedList()){ 
                // Get the currently selected list
            LinksListPanel selPanel = getSelectedList();
                // Get the currently selected index
            int index = selPanel.getSelectedIndex();
            selPanel.getModel().remove(index);
                // If the list is not empty
            if (!selPanel.getModel().isEmpty()){
                    // Select the next value in the list
                selPanel.setSelectedIndex(Math.min(index,
                    selPanel.getModel().size()-1));
            }
        }
        else
            beep();
        linkTextField.grabFocus();
    }//GEN-LAST:event_removeLinkButtonActionPerformed
    /**
     * This copies the selected link to the system clipboard.
     * @param evt The ActionEvent
     */
    private void copyLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyLinkButtonActionPerformed
        LinkManagerUtilities.copyText(getSelectedList().getSelectedValue());
        linkTextField.grabFocus();
    }//GEN-LAST:event_copyLinkButtonActionPerformed
    /**
     * This opens the selected link using the system's default browser.
     * @param evt The ActionEvent.
     */
    private void openLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openLinkButtonActionPerformed
        String link = getSelectedList().getSelectedValue();
        try {
            LinkManagerUtilities.openLink(link);
        } catch (URISyntaxException | IOException ex) {
            if (ex instanceof MalformedURLException){
                getLogger().log(Level.WARNING, 
                        "Failed to open link (malformed URL {0})", link);
            } else
                getLogger().log(Level.WARNING, "Failed to open link", ex);
            beep();
            JOptionPane.showMessageDialog(this,
                    "Could not open \""+link+"\". Please check the URL and try "
                            + "again."+((isInDebug())?"\n"+ex:""),
                    "ERROR - Failed To Open Link", JOptionPane.ERROR_MESSAGE);
        }
        linkTextField.grabFocus();
    }//GEN-LAST:event_openLinkButtonActionPerformed
    /**
     * This executes the code to close the program when the window is closing by 
     * programmatically pressing the exit menu button.
     * @param evt The WindowEvent.
     */
    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        if (exitButton.isEnabled())
            exitButton.doClick();
    }//GEN-LAST:event_formWindowClosing
    /**
     * This saves the link files and exits the program.
     * @param evt The ActionEvent.
     */
    private void exitButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitButtonActionPerformed
            // If the program is currently saving a file
        if (isSavingFiles()){
                // Ensure that the program's configuration is up-to-date
            updateProgramConfig();
            // TODO: The program should probably not be able close while a file is saving
                // If the file saver is set to exit the program after it 
                // finishes saving the file
            if (saver.getExitAfterSaving()){
                exitButton.setEnabled(false);
                return;
            }
                // If the file saver is being used to save the database
            else if (saver instanceof AbstractDatabaseSaver){
                    // Make it so that once it finishes saving the database, it 
                    // will close the program
                ((AbstractDatabaseSaver)saver).setExitAfterSaving(true);
                exitButton.setEnabled(false);
                return;
            }
        }   // If the program fully loaded initially and it is to save after the 
            // initial load
        if (fullyLoaded && ENABLE_INITIAL_LOAD_AND_SAVE){
                // Ensure that the program's configuration is up-to-date
            updateProgramConfig();
            exitButton.setEnabled(false);
                // Save the database and close the program
            saver = new DatabaseSaver(true);
            saver.execute();
        }
        else
            System.exit(0);
    }//GEN-LAST:event_exitButtonActionPerformed

    private void showHiddenListsToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showHiddenListsToggleActionPerformed
        updateVisibleTabsPanel();
    }//GEN-LAST:event_showHiddenListsToggleActionPerformed
    /**
     * This is the action performed by any of the timers or buttons relating to 
     * the autosave function.
     * @param evt The ActionEvent to process.
     */
    private void autosaveMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autosaveMenuActionPerformed
        if (isInDebug() && printAutosaveEventsToggle.isSelected()){
            System.out.println("Autosave Menu Action: " + evt);
        }   // If this is to autosave the lists, the lists have been edited, and 
            // the program is not currently saving files.
        if (AutosaveMenu.AUTOSAVE_COMMAND.equals(evt.getActionCommand()) && 
                isEdited() && !isSavingFiles()){
            getLogger().finer("Automatically saving database");
            saver = new DatabaseSaver();
            saver.execute();
        }
    }//GEN-LAST:event_autosaveMenuActionPerformed
    /**
     * 
     * @param evt 
     */
    private void autosaveMenuPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_autosaveMenuPropertyChange
        switch(evt.getPropertyName()){
            case(AutosaveMenu.AUTOSAVE_FREQUENCY_PROPERTY_CHANGED):
                config.setAutosaveFrequencyIndex(autosaveMenu.getFrequencyIndex());
            case(AutosaveMenu.AUTOSAVE_PAUSED_PROPERTY_CHANGED):
            case(AutosaveMenu.AUTOSAVE_RUNNING_PROPERTY_CHANGED):
            case("enabled"):
                getLogger().log(Level.FINER, "Autosave menu property changed "
                        + "(Name: {0}, Old: {1}, New: {2})", 
                        new Object[]{evt.getPropertyName(), evt.getOldValue(), 
                            evt.getNewValue()});
                if (isInDebug() && printAutosaveEventsToggle.isSelected()){
                    System.out.println("Autosave Menu Prop Changed: " + evt);
                }
        }
    }//GEN-LAST:event_autosaveMenuPropertyChange

    private void listOpComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listOpComboActionPerformed
        updateListOperationButtons();
    }//GEN-LAST:event_listOpComboActionPerformed

    private void listSetOpCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listSetOpCancelButtonActionPerformed
        listSetOpDialog.setVisible(false);
    }//GEN-LAST:event_listSetOpCancelButtonActionPerformed

    private void listSetOpApplyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listSetOpApplyButtonActionPerformed
        List<String> listA = getSelectedTabsPanel().getModels().get(listAOpCombo.getSelectedIndex());
        List<String> listB = getSelectedTabsPanel().getModels().get(listBOpCombo.getSelectedIndex());
        List<String> listC = getSelectedTabsPanel().getModels().get(listCOpCombo.getSelectedIndex());
        if (listA == listB)
            return;
        listSetOpDialog.setVisible(false);
        if (listA != listC){
            listC.clear();
            listC.addAll(listA);
        }
        List<String> temp = new ArrayList<>(listC);
        switch(listOperationCombo.getSelectedIndex()){
            case(0):
                listC.addAll(listB);
                break;
            case(1):
                listC.retainAll(listB);
                break;
            case(2):
                listC.addAll(listB);
                temp.retainAll(listB);
                listC.removeAll(temp);
                break;
            case(3):
                listC.removeAll(listB);
        }
    }//GEN-LAST:event_listSetOpApplyButtonActionPerformed
    /**
     * This is used to listen to whether a change has been made to whether the 
     * lists have been edited.
     * @param evt The ActionEvent.
     */
    private void listsTabsPanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_listsTabsPanelPropertyChange
        String propName = evt.getPropertyName();
        if (printListPropChangeToggle.isSelected())
            System.out.println(evt);
        if (fullyLoaded && propName != null){
            switch(propName){
                case(LinksListTabsPanel.LISTS_EDITED_PROPERTY_CHANGED):
                case(LinksListTabsPanel.STRUCTURE_EDITED_PROPERTY_CHANGED):
                    updateProgramTitle();
                    autosaveMenu.startAutosave();
                    break;
                default:
                    if (propName.startsWith(LinksListTabsPanel.
                            LIST_IS_READ_ONLY_PROPERTY_CHANGED_PREFIX)){
                        updateButtons();
                    }
            }
        }
    }//GEN-LAST:event_listsTabsPanelPropertyChange
    /**
     * This updates which tab is currently selected.
     * @param evt The ChangeEvent that occurred.
     */
    private void listsTabsPanelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_listsTabsPanelStateChanged
        updateButtons();
            // If the program has fully loaded
        if (fullyLoaded){
                // This is the index for the list tabs panel
            int type = -1;
                // Go through the list tabs panels
            for (int i = 0; i < listsTabPanels.length && type < 0; i++){
                    // If the event source is the current list tabs panel
                if (evt.getSource() == listsTabPanels[i])
                    type = i;
            }   // If the event source is found in the list tabs panel array (it 
            if (type >= 0)  // should be)
                config.setCurrentTab(type, listsTabPanels[type]);
            else{
                getLogger().log(Level.WARNING, 
                        "Source not found in list tabs panels: {0}", evt.getSource());
                    // If the program is in debug mode
                if (isInDebug())
                    System.out.println("Source not found in list tabs panels: " +evt.getSource());
            }
        }
    }//GEN-LAST:event_listsTabsPanelStateChanged
    /**
     * This processes a change to the selection in the currently selected list.
     * @param evt The ListSelectionEvent.
     */
    private void listsTabsPanelValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listsTabsPanelValueChanged
            // If the selection is not currently being adjusted
        if (!evt.getValueIsAdjusting()){
                // If the source of the change is the currently selected list
            if (Objects.equals(getSelectedList(),evt.getSource())){
                updateSelectedLink(); 
            }   // If the program has fully loaded and the program isn't loading 
                // the database
            if (fullyLoaded && !isLoadingDatabase()){
                    // This will get the listID of the list that the selection 
                Integer listID;     // changed
                    // This will get the newly selected value
                String selValue;
                    // If the source of the event is a LinksListPanel
                if (evt.getSource() instanceof LinksListPanel){
                        // Get the source as a panel
                    LinksListPanel panel = (LinksListPanel)evt.getSource();
                    listID = panel.getListID();
                    selValue = panel.getSelectedValue();
                    // If the source of the event is a LinksListModel
                } else if (evt.getSource() instanceof LinksListModel){
                        // Get the source as a model
                    LinksListModel model = (LinksListModel)evt.getSource();
                    listID = model.getListID();
                    selValue = model.getSelectedValue();
                } else
                    return;
                    // If the listID for this list is not null
                if (listID != null)
                        // Set the selected link for the list
                    config.setSelectedLink(listID, selValue);
            }
        }
    }//GEN-LAST:event_listsTabsPanelValueChanged

    private void clearSelTabItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSelTabItemActionPerformed
        for (LinksListTabsPanel tabsPanel : listsTabPanels){
            tabsPanel.setSelectedIndex(-1);
        }
    }//GEN-LAST:event_clearSelTabItemActionPerformed

    private void clearListSelItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearListSelItemActionPerformed
        for (LinksListPanel panel : allListsTabsPanel)
            panel.clearSelection();
    }//GEN-LAST:event_clearListSelItemActionPerformed
    
    private void setUpListSetOpCombo(JComboBox<List<String>> combo){
        int sel = combo.getSelectedIndex();
        combo.setModel(new ArrayComboBoxModel<>(getSelectedTabsPanel().getModels()));
        if (sel >= 0)
            combo.setSelectedIndex(sel);
    }
    
    private void listSetOpItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listSetOpItemActionPerformed
        setUpListSetOpCombo(listAOpCombo);
        setUpListSetOpCombo(listBOpCombo);
        setUpListSetOpCombo(listCOpCombo);
        updateListOperationButtons();
        listSetOpDialog.setVisible(true);
    }//GEN-LAST:event_listSetOpItemActionPerformed

    private void manageListsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageListsItemActionPerformed
        LinksListTabsPanel tabsPanel = getSelectedTabsPanel();
        listTabsManipulator.setListData(tabsPanel.getModels());
        List<LinksListModel> models = new ArrayList<>(allListsTabsPanel.getModels());
        models.removeAll(tabsPanel.getModels());
        listTabsManipulator.getUsedNames().clear();
        listTabsManipulator.addUsedNames(models);
            // If the user has confirmed the changes to the lists
        if (listTabsManipulator.showDialog(this) == JListManipulator.ACCEPT_OPTION){
            List<LinksListModel> oldModels = new ArrayList<>(tabsPanel.getModels());
            listTabsManipulator.updateListTabs(tabsPanel);
            models = new ArrayList<>(tabsPanel.getModels());
            Set<LinksListModel> added = new LinkedHashSet<>(models);
            added.removeAll(oldModels);
            
            if (tabsPanel == allListsTabsPanel){
                models.removeAll(shownListsTabsPanel.getModels());
                models.addAll(0, shownListsTabsPanel.getModels());
                models.retainAll(allListsTabsPanel.getModels());
                models.removeIf((LinksListModel t) -> t == null || t.isHidden());
                shownListsTabsPanel.setModels(models);
            }
            else if (tabsPanel == shownListsTabsPanel){
                allListsTabsPanel.getModels().addAll(added);
                Set<LinksListModel> removed = new HashSet<>(oldModels);
                removed.removeAll(models);
                allListsTabsPanel.getModels().removeAll(removed);
                shownListsTabsPanel.getModels().removeIf((LinksListModel t) -> 
                        t == null || t.isHidden());
            }
        }
        listTabsManipulator.setPreferredSize(listTabsManipulator.getSize());
            // Set the list tabs manipulator panel's size in the config
        config.setComponentSize(listTabsManipulator);
        System.gc();
    }//GEN-LAST:event_manageListsItemActionPerformed
    /**
     * This saves the lists to the database.
     * @param evt The ActionEvent.
     */
    private void updateDatabaseItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateDatabaseItemActionPerformed
        saver = new DatabaseSaver();
        saver.execute();
    }//GEN-LAST:event_updateDatabaseItemActionPerformed
    /**
     * 
     * @param loadFlags 
     */
    private void loadDatabase(int loadFlags){
        File file = getDatabaseFile();
            // If the local file should be checked for more up-to-date lists
        if (LinkManagerUtilities.getFlag(loadFlags,DATABASE_LOADER_CHECK_LOCAL_FLAG)){
            try {   // Create a temporary file for the downloaded database
                file = createTempFile();
                    // Make sure the file is deleted on exit
                file.deleteOnExit();
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, 
                        "Failed to create temporary database file", ex);
            }
        }   // If this will sync the database to the cloud and the user is 
            // logged into dropbox
        if (syncDBToggle.isSelected() && isLoggedInToDropbox()){
            saver = new FileDownloader(file,config.getDropboxDatabaseFileName(),0,loadFlags);
            saver.execute();
        } else {
            loader = new DatabaseLoader(LinkManagerUtilities.setFlag(loadFlags,
                    DATABASE_LOADER_CHECK_LOCAL_FLAG,false));
            loader.execute();
        }
    }
    /**
     * 
     * @param loadAll 
     */
    private void loadDatabase(boolean loadAll){
        loadDatabase((loadAll)?DATABASE_LOADER_LOAD_ALL_FLAG:0);
    }
    /**
     * This reloads the unedited lists from the database.
     * @param evt The ActionEvent.
     */
    private void updateListsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_updateListsItemActionPerformed
        loadDatabase(false);
    }//GEN-LAST:event_updateListsItemActionPerformed
    /**
     * This reloads all the lists from the database.
     * @param evt The ActionEvent.
     */
    private void reloadListsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_reloadListsItemActionPerformed
        if (isEdited()){        // If the lists have been edited
                // If the user chooses not to discard changes to the lists
            if (JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to discard all changes made to the lists?",
                    "Discard Changes to Lists?",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE)
                    != JOptionPane.OK_OPTION)
            return;
        }
        loadDatabase(true);
    }//GEN-LAST:event_reloadListsItemActionPerformed
    /**
     * This exports the lists to files.
     * @param evt The ActionEvent.
     */
    private void exportListsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportListsItemActionPerformed
            // Gets the file to save to
        File file = showSaveFileChooser(exportFC,null);
        if (file != null){  // If the user selected a file
            saver = new ExportDatabase(file);
            saver.execute();
        }
    }//GEN-LAST:event_exportListsItemActionPerformed

    private void saveConfigItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveConfigItemActionPerformed
        File file = showSaveFileChooser(configFC, "Save Configuration To File...");
        if (file != null){
            if (!FilesExtended.endsWithFileExtension(file,ConfigExtensions.CFG)){
                file = new File(file.toString()+"."+ConfigExtensions.CFG);
                configFC.setSelectedFile(file);
            }
            saver = new ConfigSaver(file);
            saver.execute();
        }
    }//GEN-LAST:event_saveConfigItemActionPerformed

    private void loadConfigItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadConfigItemActionPerformed
        File file = showOpenFileChooser(configFC, "Load Configuration From File...");
        if (file != null){
            loader = new ConfigLoader(file);
            loader.execute();
        }
    }//GEN-LAST:event_loadConfigItemActionPerformed

    private void dbRemoveUnusedDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbRemoveUnusedDataButtonActionPerformed
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            conn.getLinkMap().removeUnusedRows();
            conn.getListNameMap().removeUnusedRows();
            conn.getPrefixMap().removeUnusedRows();
               // Ensure that the database last modified time is updated
            conn.setDatabaseLastModified();
        } catch (SQLException ex) {
            getLogger().log(Level.WARNING, "Error removing unused data", ex);
        }
        loader = new LoadDatabaseViewer(true);
        loader.execute();
    }//GEN-LAST:event_dbRemoveUnusedDataButtonActionPerformed
    /**
     * This toggles whether the copy and open buttons are enabled for hidden 
     * lists.
     * @param evt The ActionEvent.
     */
    private void hiddenLinkOperationToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hiddenLinkOperationToggleActionPerformed
        updateSelectedLink();
        config.setHiddenLinkOperationsEnabled(hiddenLinkOperationToggle.isSelected());
    }//GEN-LAST:event_hiddenLinkOperationToggleActionPerformed
    
    private void setAllListVisible(boolean value){
        for (LinksListPanel panel : allListsTabsPanel){
            panel.setHidden(!value);
        }
        if (value){
            Set<LinksListModel> models = new LinkedHashSet<>(allListsTabsPanel.getModels());
            models.removeAll(shownListsTabsPanel.getModels());
            shownListsTabsPanel.getModels().addAll(models);
        } else
            shownListsTabsPanel.getModels().clear();
    }
    
    private void showAllListsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showAllListsItemActionPerformed
        setAllListVisible(true);
    }//GEN-LAST:event_showAllListsItemActionPerformed

    private void hideAllListsItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hideAllListsItemActionPerformed
        setAllListVisible(false);
    }//GEN-LAST:event_hideAllListsItemActionPerformed

    private void prefixApplyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefixApplyButtonActionPerformed
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            DatabasePropertyMap properties = conn.getDatabaseProperties();
            String[][] dataArr = {
                {PREFIX_THRESHOLD_CONFIG_KEY,prefixThresholdSpinner.getValue().toString()},
                {PREFIX_SEPARATORS_CONFIG_KEY,prefixSeparatorField.getText()}
            };
            for (String[] arr : dataArr){
                if (arr[1] == null)
                    properties.remove(arr[0]);
                else if (properties.containsKey(arr[0]) || 
                        !arr[1].equals(properties.getDefaults().get(arr[0])))
                    properties.setProperty(arr[0], arr[1]);
            }
               // Ensure that the database last modified time is updated
            setDBLastModLabelText(conn.setDatabaseLastModified());
        }catch (SQLException | IllegalArgumentException ex) {
            getLogger().log(Level.WARNING,"Error applying prefix settings", ex);
        }
    }//GEN-LAST:event_prefixApplyButtonActionPerformed
    /**
     * This is the action performed by any of the timers or buttons relating to 
     * the auto-hide function.
     * @param evt The ActionEvent to process.
     */
    private void autoHideMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoHideMenuActionPerformed
        if (isInDebug() && printAutoHideEventsToggle.isSelected()){
            System.out.println("Auto-Hide Menu Action: " + evt);
        }   // If this is to automatically hide the hidden lists
        if (AutoHideMenu.AUTO_HIDE_COMMAND.equals(evt.getActionCommand())){
            getLogger().log(Level.FINER, "Automatically hiding hidden lists (visible: {0})", 
                    showHiddenListsToggle.isSelected());
            showHiddenListsToggle.setSelected(false);
            updateVisibleTabsPanel();
                // Clear whether hidden lists are shown in the configuration
            config.setHiddenListsAreShown(null);
        }
    }//GEN-LAST:event_autoHideMenuActionPerformed
    /**
     * 
     * @param evt 
     */
    private void autoHideMenuPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_autoHideMenuPropertyChange
        switch(evt.getPropertyName()){
            case(AutoHideMenu.AUTO_HIDE_WAIT_DURATION_PROPERTY_CHANGED):
                config.setAutoHideWaitDurationIndex(autoHideMenu.getDurationIndex());
            case(AutoHideMenu.AUTO_HIDE_PAUSED_PROPERTY_CHANGED):
            case(AutoHideMenu.AUTO_HIDE_RUNNING_PROPERTY_CHANGED):
            case("enabled"):
                getLogger().log(Level.FINER, "Auto-Hide menu property changed "
                        + "(Name: {0}, Old: {1}, New: {2})", 
                        new Object[]{evt.getPropertyName(), evt.getOldValue(), 
                            evt.getNewValue()});
                if (isInDebug() && printAutoHideEventsToggle.isSelected()){
                    System.out.println("Auto-Hide Menu Prop Changed: " + evt);
                }
        }
    }//GEN-LAST:event_autoHideMenuPropertyChange

    private void dbUpdateUsedPrefixesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbUpdateUsedPrefixesButtonActionPerformed
        saver = new LinkPrefixUpdater();
        saver.execute();
    }//GEN-LAST:event_dbUpdateUsedPrefixesButtonActionPerformed
    
    private void setListEditSettings(LinkDatabaseConnection conn, int listID) 
            throws SQLException{
        ListContents list = conn.getListContents(listID);
        dbListNameField.setText(list.getName());
        dbListFlagsField.setValue(list.getFlags());
        dbListSizeLimitToggle.setSelected(list.getSizeLimit() != null);
        if (dbListSizeLimitToggle.isSelected())
            dbListSizeLimitSpinner.setValue(list.getSizeLimit());
        dbListSizeLimitSpinner.setEnabled(dbListSizeLimitToggle.isSelected());
    }
    
    private void dbListIDComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbListIDComboActionPerformed
        Integer listID = (Integer)dbListIDCombo.getSelectedItem();
        if (listID == null){
            dbListIDCombo.setEnabled(false);
            updateListEditButtons();
            return;
        }
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            setListEditSettings(conn,listID);
        }catch (SQLException | IllegalArgumentException ex) {
            getLogger().log(Level.WARNING, "Error loading settings for list " + 
                    listID, ex);
        }
    }//GEN-LAST:event_dbListIDComboActionPerformed

    private void dbListSizeLimitToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbListSizeLimitToggleActionPerformed
        dbListSizeLimitSpinner.setEnabled(dbListSizeLimitToggle.isSelected());
    }//GEN-LAST:event_dbListSizeLimitToggleActionPerformed

    private void dbListEditApplyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbListEditApplyButtonActionPerformed
        Integer listID = (Integer)dbListIDCombo.getSelectedItem();
        String name = dbListNameField.getText();
        int flags = ((Number)dbListFlagsField.getValue()).intValue();
        Integer sizeLimit = (dbListSizeLimitToggle.isSelected()) ? 
                (Integer)dbListSizeLimitSpinner.getValue() : null;
        if (listID != null){
            try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
                ListContents list = conn.getListContents(listID);
                list.setName(name);
                list.setFlags(flags);
                list.setSizeLimit(sizeLimit);
                long lastMod = list.setLastModified();
                TableModel model = dbListTable.getModel();
                int row = -1;
                for (int r = 0; r < model.getRowCount() && row < 0; r++){
                    if (Objects.equals(model.getValueAt(r, 0), listID))
                        row = r;
                }
                if (row >= 0){
                    model.setValueAt(name, row, 1);
                    model.setValueAt(new java.util.Date(lastMod), row, 3);
                    model.setValueAt(flags, row, 4);
                    model.setValueAt(sizeLimit, row, 5);
                }
                   // Ensure that the database last modified time is updated
                setDBLastModLabelText(conn.setDatabaseLastModified());
            } catch (SQLException | IllegalArgumentException ex) {
                getLogger().log(Level.WARNING,"Error changing settings for list " 
                        + listID, ex);
            }
        }
    }//GEN-LAST:event_dbListEditApplyButtonActionPerformed
    
    private int getSearchPrefix(String prefixStr){
        return Integer.parseInt(prefixStr.substring(0,prefixStr.indexOf(" - ")));
    }
    
    private void dbUsedPrefixSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbUsedPrefixSearchButtonActionPerformed
        String prefixStr = dbUsedPrefixCombo.getSelectedItem().toString();
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            searchUsedPrefixes(conn,getSearchPrefix(prefixStr));
        } catch (SQLException | IllegalArgumentException ex) {
            getLogger().log(Level.WARNING, "Error searching for prefix " + 
                    prefixStr, ex);
            JOptionPane.showMessageDialog(this, "Could Not Search For Prefix "+
                    prefixStr+".\nDatabase Error: " + ex,
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_dbUsedPrefixSearchButtonActionPerformed

    private void prefixCopyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_prefixCopyButtonActionPerformed
        int selRow = dbPrefixTable.getSelectedRow();
        if (selRow < 0){
            JOptionPane.showMessageDialog(this, "No Prefix Selected",
                "No Prefix Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Object prefix = dbPrefixTable.getValueAt(selRow, 1);
        if (prefix != null)
            LinkManagerUtilities.copyText(prefix.toString());
    }//GEN-LAST:event_prefixCopyButtonActionPerformed

    private void dbRemoveDuplDataButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbRemoveDuplDataButtonActionPerformed
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            conn.getLinkMap().removeDuplicateRows();
               // Ensure that the database last modified time is updated
            conn.setDatabaseLastModified();
        } catch (SQLException ex) {
            getLogger().log(Level.WARNING, "Error removing duplicate data", ex);
        }
        loader = new LoadDatabaseViewer(true);
        loader.execute();
    }//GEN-LAST:event_dbRemoveDuplDataButtonActionPerformed
    
    private void formComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentResized
//        System.out.println(evt);
            // If the window is not maximized
        if (!isMaximized())
                // Set the windows's size in the config
            config.setComponentBounds(this);
    }//GEN-LAST:event_formComponentResized

    private void formWindowStateChanged(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowStateChanged
        // TODO: Implement saving the window state
//        System.out.println(evt);
//        config.setProperty(LINK_MANAGER_WINDOW_STATE_KEY)
//        config.setPropertyDefault(LINK_MANAGER_WINDOW_STATE_KEY, Integer.toString(JFrame.NORMAL));
    }//GEN-LAST:event_formWindowStateChanged
    
    private void updateDBSearchPrefixCombo(){
        dbSearchPrefixCombo.setEnabled(dbSearchPrefixCheckBox.isSelected() && 
                dbSearchPrefixCheckBox.isEnabled());
    }
    
    private void dbSearchPrefixCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbSearchPrefixCheckBoxActionPerformed
        updateDBSearchPrefixCombo();
    }//GEN-LAST:event_dbSearchPrefixCheckBoxActionPerformed

    private void dbSearchButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbSearchButtonActionPerformed
        String prefixStr = dbUsedPrefixCombo.getSelectedItem().toString();
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            Integer prefixID = (dbSearchPrefixCheckBox.isSelected()) ? 
                    getSearchPrefix(prefixStr) : null;
            searchListContents(conn,dbSearchField.getText(),prefixID);
        } catch (SQLException | IllegalArgumentException ex) {
            getLogger().log(Level.WARNING, "Error searching for prefix " + 
                    prefixStr, ex);
            JOptionPane.showMessageDialog(this, "Could Not Search For Prefix "+
                    prefixStr+".\nDatabase Error: " + ex,
                "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_dbSearchButtonActionPerformed
    
    private void setDropboxTestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDropboxTestButtonActionPerformed
        String token = JOptionPane.showInputDialog(this, 
                "Set the access token to use for the Dropbox test.", "Set the Dropbox Access Token", 
                JOptionPane.QUESTION_MESSAGE);
        dbxUtils.setAccessToken(token);
    }//GEN-LAST:event_setDropboxTestButtonActionPerformed
    
    private void dropboxRefreshTestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dropboxRefreshTestButtonActionPerformed
        System.out.println("Dropbox Access Token: " + dbxUtils.getAccessToken());
        System.out.println("Dropbox Refresh Token: " + dbxUtils.getRefreshToken());
        System.out.println("Dropbox Expires At: " + dbxUtils.getTokenExpiresAtDate());
        try{
            DbxRequestConfig requestConfig = dbxUtils.createRequest();
            DbxCredential cred = dbxUtils.getCredentials();
            System.out.println("Credentials have/will expire: " + cred.aboutToExpire());
            dbxUtils.refreshCredentials(cred.refresh(requestConfig));
            System.out.println("New Dropbox Access Token: " + dbxUtils.getAccessToken());
            System.out.println("New Dropbox Expiration Time: " + dbxUtils.getTokenExpiresAtDate());
        } catch (DbxException ex){
            getLogger().log(Level.WARNING, "Failed to refresh Dropbox token", ex);
        }
    }//GEN-LAST:event_dropboxRefreshTestButtonActionPerformed

    private void setDBLocationItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDBLocationItemActionPerformed
            // This will be true if the database location dialog has not been opened before
        if (setLocationDialog.isLocationByPlatform())
            setLocationDialog.setLocationRelativeTo(this);
        setDatabaseFileFields(config.getDatabaseFileName());
        setDropboxDatabaseFileFields(config.getDropboxDatabaseFileName());
        loadExternalAccountData();
        updateDBLocationEnabled();
        setLocationDialog.setVisible(true);
    }//GEN-LAST:event_setDBLocationItemActionPerformed
    
    private void setDatabaseFileFields(String fileName){
        File file = getDatabaseFile(fileName);
        dbFileField.setText(fileName);
        databaseFC.setCurrentDirectory(file);
    }
    
    private void setDropboxDatabaseFileFields(String fileName){
        dbxDbFileField.setText(DropboxUtilities.formatDropboxPath(fileName));
    }
    
    private void setDBCancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDBCancelButtonActionPerformed
        setLocationDialog.setVisible(false);
    }//GEN-LAST:event_setDBCancelButtonActionPerformed
    
    private void setDBAcceptButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDBAcceptButtonActionPerformed
        String fileName = dbFileField.getText();
        if (fileName != null)
            fileName = FilesExtended.removeQuotations(fileName);
        if (fileName == null || fileName.isBlank()){
            JOptionPane.showMessageDialog(this, 
                    "There is no file name specifed for the database file.", 
                    "Invalid Database File", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
            // If the user is logged into Dropbox
        if (isLoggedInToDropbox()){
            // TODO: Implement setting the location for the dropbox file using 
            // a browse dialog and make it more robust
            
                // Get the database file name for Dropbox
            String dbxFileName = dbxDbFileField.getText().trim();
                // If the Dropbox database file name is empty  or ends with a 
            if (dbxFileName.isEmpty() || dbxFileName.endsWith("/")) // slash
                    // Add the database file name to the path
                dbxFileName+=LINK_DATABASE_FILE;
                // Format the Dropbox database file name
            dbxFileName = DropboxUtilities.formatDropboxPath(dbxFileName);
                // If the Dropbox database file name has changed
            if (!Objects.equals(dbxFileName, config.getDropboxDatabaseFileName())){
                    // Set the Dropbox database file name
                config.setDropboxDatabaseFileName(dbxFileName);
                setDropboxDatabaseFileFields(dbxFileName);
            }
        }
        
        File file = new File(fileName.trim());
        fileName = file.toString();
            // Get the operation to perform on the old file
        int op = dbFileChangeCombo.getSelectedIndex();
            // Check if the new file name is the same as the old file name
        if (Objects.equals(fileName, config.getDatabaseFileName())){
                // No change will occur
            setLocationDialog.setVisible(false);
            return;
        }
        
        File newFile = getDatabaseFile(fileName);
        Path newPath;
            // Check if the new file name is a valid path
        try{
            newPath = newFile.toPath();
        } catch (InvalidPathException ex){
            getLogger().log(Level.INFO,"Invalid path for database file", ex);
                // If the new file name is not a valid path, do not allow it to 
                // be used
            String message = null;      // The error message to print
                // Get the full file name as a String
            String tempName = newFile.toString();
                // If the index is within the full file name
            if (ex.getIndex() >= 0 && ex.getIndex() < tempName.length()){
                    // Get the problematic character
                char c = tempName.charAt(ex.getIndex());
                    // If the path is invalid due to an illegal character
                if (ex.getReason().startsWith("Illegal char"))
                    message = "The new database file name cannot contain the "
                            + "character \'" + c + "\'.";
                    // If the path is invalid due to a trailing character
                else if (ex.getReason().startsWith("Trailing char"))
                    message = "The new database file name has a trailing \'" + 
                            c + "\' at index " + ex.getIndex()+".";
            }
            if (message == null)
                message = "The new database file name is invalid due to the "
                        + "following reason:\n" + ex.getMessage();
            JOptionPane.showMessageDialog(this, message, 
                    "Invalid Database File", JOptionPane.WARNING_MESSAGE);
            return;
        }   // If the new file is a directory
        if (newFile.isDirectory()){
                // Don't allow the file to be used
            JOptionPane.showMessageDialog(this, 
                    "The new database file cannot be used as the database "
                            + "file, as a directory exists with that name.",
                    "Invalid Database File", JOptionPane.WARNING_MESSAGE);
            return;
        }   // Get the location of the old database file
        File oldFile = getDatabaseFile();
            // If the new database file already exists
        if (newFile.exists()){
                // If the old database file exists and the user wants to do 
                // something to deal with the old file
            if (oldFile.exists() && op != 0){
                try {    // If the new file is the same as the old file
                    if (Files.isSameFile(newPath, oldFile.toPath())){
                            // Set the path in the config
                        config.setDatabaseFileName(fileName);
                            // No change will occur to the file itself
                        setLocationDialog.setVisible(false);
                        return;
                    }
                } catch (IOException ex) {
                    getLogger().log(Level.WARNING, 
                            "Failed to check to see if the files are the same" , 
                            ex);
                        // We could not check if the files are the same file. 
                        // Ask the user if we should continue
                    int option = JOptionPane.showConfirmDialog(this, 
                            "An error occured while checking to see if the files "
                                    + "are the same. Would you like to continue anyway?\n\n"
                                    + "Error: "+ex, 
                            "Same File Check Error", JOptionPane.YES_NO_OPTION, 
                            JOptionPane.ERROR_MESSAGE);
                        // Use the user's option to determine how to continue
                    switch (option){
                            // User wants to continue
                        case(JOptionPane.YES_OPTION):
                            break;
                            // User aborted the operation
                        default:
                            return;
                    }
                }
                    // Ask the user if they want to replace the existing new file
                int option = JOptionPane.showConfirmDialog(this, 
                        "The new database file already exists. Would you like to "
                                + "replace it with the old database file?",
                        "New Database File Already Exists",
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.WARNING_MESSAGE);
                    // Determine how to handle the existing file
                switch (option){
                        // User does not want to replace the file
                    case(JOptionPane.NO_OPTION):
                        op = 0;
                        // User wants to replace the file
                    case(JOptionPane.YES_OPTION):
                        break;
                        // User aborted the operation
                    default:
                        return;
                }
            }
            // How to handle no old file or no operation?
        }

        if (oldFile.exists() && !oldFile.isDirectory() && op != 0){
                // Change the database file using the selected operation
            saver = new DatabaseFileChanger(op,oldFile,fileName);
            saver.execute();
        } else {
            config.setDatabaseFileName(fileName);
            setLocationDialog.setVisible(false);
        }
    }//GEN-LAST:event_setDBAcceptButtonActionPerformed

    private void setDBResetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setDBResetButtonActionPerformed
        setDatabaseFileFields(LINK_DATABASE_FILE);
        setDropboxDatabaseFileFields(LINK_DATABASE_FILE);
    }//GEN-LAST:event_setDBResetButtonActionPerformed
    
    private void dbFileBrowseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbFileBrowseButtonActionPerformed
        File file = showSaveFileChooser(databaseFC,null);
        if (file != null){
            if (file.isDirectory()){
                String fileName = dbFileField.getText();
                if (fileName != null)
                    fileName = FilesExtended.removeQuotations(fileName);
                if (fileName != null && !fileName.isBlank())
                    fileName = new File(fileName.trim()).getName();
                else
                    fileName = LINK_DATABASE_FILE;
                file = new File(file,fileName);
            }
            dbFileField.setText(file.toString());
        }
    }//GEN-LAST:event_dbFileBrowseButtonActionPerformed

    private void dbFileChangeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbFileChangeComboActionPerformed
        config.setDatabaseFileChangeOperation(dbFileChangeCombo.getSelectedIndex());
    }//GEN-LAST:event_dbFileChangeComboActionPerformed

    private void dbFileRelativeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbFileRelativeButtonActionPerformed
        String fileName = dbFileField.getText();
        if (fileName != null)
            fileName = FilesExtended.removeQuotations(fileName);
        if (fileName == null || fileName.isBlank()){
            JOptionPane.showMessageDialog(this, 
                    "There is no file name to make relative.", 
                    "Cannot Relativize File Name", JOptionPane.WARNING_MESSAGE);
            return;
        }
        File progFile = new File(LinkManagerUtilities.getWorkingDirectory());
        File file = getDatabaseFile(fileName);
        Path progPath = progFile.toPath().normalize();
        Path filePath = file.toPath().normalize();
        if (progPath.getRoot() != null && filePath.startsWith(progPath.getRoot())){
            dbFileField.setText(progPath.relativize(filePath).toString());
        } else {
            JOptionPane.showMessageDialog(this, 
                    "Cannot relativize the file name, as the file does not "
                            + "share the same root directory as this "
                            + "program!", 
                "Cannot Relativize File Name", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_dbFileRelativeButtonActionPerformed
    /**
     * This returns whether this window is maximized in either the horizontal or 
     * vertical directions.
     * @return Whether this window is horizontally or vertically maximized.
     */
    private boolean isMaximized(){
        return (getExtendedState() & JFrame.MAXIMIZED_BOTH) != 0;
    }
    
    private void formComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentMoved
//        System.out.println("Moved: " + evt);
            // If the window is not maximized
        if (!isMaximized()){
            config.setComponentBounds(this);
        }
    }//GEN-LAST:event_formComponentMoved
    /**
     * This updates the stored size when the window is resized.
     * @param evt The ComponentEvent to be processed.
     */
    private void setLocationDialogComponentResized(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_setLocationDialogComponentResized
            // Update the dialog's bounds in the config
        config.setComponentBounds(setLocationDialog);
    }//GEN-LAST:event_setLocationDialogComponentResized

    private void dbxPrintButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbxPrintButtonActionPerformed
        System.out.println("Dropbox API App Key: " + dbxUtils.getAppKey());
        System.out.println("Dropbox API Secret Key: " + dbxUtils.getSecretKey());
        System.out.println("Dropbox Access Token: " + dbxUtils.getAccessToken());
        System.out.println("Dropbox Refresh Token: " + dbxUtils.getRefreshToken());
        System.out.println("Dropbox Expires At: " + dbxUtils.getTokenExpiresAtDate());
    }//GEN-LAST:event_dbxPrintButtonActionPerformed

    private void dbxLogOutButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbxLogOutButtonActionPerformed
        // TODO: Figure out how to properly deal with logging out of dropbox
            // Clear the account credentials
        dbxUtils.clearCredentials();
            // Load the external account data
        loadExternalAccountData();
            // Remind the user that this program is still connected to their 
            // Dropbox account, and that they've only logged out on this end
        JOptionPane.showMessageDialog(setLocationDialog, 
                "Don't forget to disconnect this app from your Dropbox account.",
                "Dropbox Log out",JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_dbxLogOutButtonActionPerformed

    private void dbxLogInButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbxLogInButtonActionPerformed
        if (loadDbxUtils() == null){
            JOptionPane.showMessageDialog(setLocationDialog,
                    "Dropbox API data failed to load.", 
                    "Dropbox API Failure", JOptionPane.ERROR_MESSAGE);
            return;
        }
        try{    // Run through the Dropbox API application process
            DbxRequestConfig requestConfig = dbxUtils.createRequest();
            DbxAppInfo appInfo = dbxUtils.getAppInfo();
            DbxWebAuthWrapper webAuth = new DbxWebAuthWrapper(requestConfig,appInfo);
            DbxWebAuth.Request webAuthRequest = dbxUtils.createWebAuthRequest();
            
            String authorizeURL = webAuth.authorize(webAuthRequest);
            
                // Try to open the authorization web page in the user's browser
            try {
                LinkManagerUtilities.openLink(authorizeURL);
            } catch (URISyntaxException | IOException ex) {
                getLogger().log(Level.WARNING, 
                        "Failed to open Dropbox authorization URL" , ex);
            }
            
            if (dropboxSetupPanel.showDialog(setLocationDialog, authorizeURL) != 
                    DropboxSetupPanel.ACCEPT_OPTION){
                return;
            }
            String code = dropboxSetupPanel.getAuthorizationCode();
            
            if (code == null || code.isBlank()){
                return;
            }
            
            DbxAuthFinish authFinish = webAuth.finishFromCode(code);
            
            if (dbxUtils.getPermissionScope() != null && 
                    !dbxUtils.getPermissionScope().isEmpty()){
                List<String> permissions = Arrays.asList(authFinish.getScope().split(" "));
                if (!permissions.containsAll(dbxUtils.getPermissionScope())){
                    ArrayList<String> missing = new ArrayList<>(permissions);
                    missing.removeAll(dbxUtils.getPermissionScope());
                    String message = "The app does not have the appropriate scope.\n"
                            + "Missing the following required permissions:";
                    for (String temp : missing){
                        message += "\n\t"+temp;
                    }
                    JOptionPane.showMessageDialog(setLocationDialog,message,
                            "Missing Permissions",JOptionPane.ERROR_MESSAGE);
                        // Clear the account credentials
                    dbxUtils.clearCredentials();
                        // Load the external account data
                    loadExternalAccountData();
                    return;
                }
            }   // Set the account credentials
            dbxUtils.setCredentials(authFinish);
                // Load the external account data
            loadExternalAccountData();
        } catch (DbxException ex){
            getLogger().log(Level.WARNING, "Failed to login to Dropbox", ex);
            String message = "An error occurred while setting up Dropbox.";
            if (showDBErrorDetailsToggle.isSelected())
                message += "\nError: " + ex;
            JOptionPane.showMessageDialog(setLocationDialog, message, 
                    "Dropbox Error Occurred", JOptionPane.ERROR_MESSAGE);
        }
    }//GEN-LAST:event_dbxLogInButtonActionPerformed

    private void uploadDBItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uploadDBItemActionPerformed
        if (isLoggedInToDropbox()){
            if (!getDatabaseFile().exists()){
                saver = new DatabaseSaver(){
                    @Override
                    protected void uploadDatabase(){
                        if (getExitAfterSaving())
                            super.uploadDatabase();
                    }
                    @Override
                    protected void done(){
                        super.done();
                        if (!getExitAfterSaving()){
                            saver = new FileUploader(getDatabaseFile(),config.getDropboxDatabaseFileName(),0);
                            saver.execute();
                        }
                    }
                };
            } else {
                saver = new FileUploader(getDatabaseFile(),config.getDropboxDatabaseFileName(),0);
            }
            saver.execute();
        }
    }//GEN-LAST:event_uploadDBItemActionPerformed

    private void downloadDBItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downloadDBItemActionPerformed
        if (isLoggedInToDropbox()){
            saver = new FileDownloader(getDatabaseFile(),config.getDropboxDatabaseFileName(),0);
            saver.execute();
        }
    }//GEN-LAST:event_downloadDBItemActionPerformed

    private void syncDBToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_syncDBToggleActionPerformed
        config.setDatabaseWillSync(syncDBToggle.isSelected());
    }//GEN-LAST:event_syncDBToggleActionPerformed

    private void dbUpdateLastModButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbUpdateLastModButtonActionPerformed
        try(LinkDatabaseConnection conn = connect(getDatabaseFile())){
            setDBLastModLabelText(conn.setDatabaseLastModified());
        }catch (SQLException | UncheckedSQLException ex) {
            getLogger().log(Level.WARNING, 
                    "Failed to update last modified time of database" , ex);
        }
    }//GEN-LAST:event_dbUpdateLastModButtonActionPerformed

    private void setLocationDialogComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_setLocationDialogComponentMoved
            // Update the dialog's bounds in the config
        config.setComponentBounds(setLocationDialog);
    }//GEN-LAST:event_setLocationDialogComponentMoved

    private void searchDialogComponentMoved(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_searchDialogComponentMoved
            // Update the location of the search dialog in the config
        config.setComponentLocation(searchDialog);
    }//GEN-LAST:event_searchDialogComponentMoved

    private void dbxChunkSizeSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_dbxChunkSizeSpinnerStateChanged
            // Update the chunk size in the configuration
        config.setDropboxChunkSizeMultiplier(dbxChunkSizeModel.getMultiplier());
    }//GEN-LAST:event_dbxChunkSizeSpinnerStateChanged
    /**
     * This executes a query directly on the database.
     * @param evt The ActionEvent.
     */
    private void dbQueryPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dbQueryPanelActionPerformed
            // This gets if we need to update the tables
        boolean updated = false;
            // Try to connect to the database and create an SQL statement for it
        try(LinkDatabaseConnection conn = connect(getDatabaseFile());
            Statement stmt = conn.createStatement()){
                // Get the current time
            long time = System.currentTimeMillis();
                // Execute the query and get if the query returns a ResultSet
            boolean hasResults = stmt.execute(dbQueryPanel.getQuery());
                // Get the time it took to execute the query
            dbQueryPanel.setExecutionTime(System.currentTimeMillis() - time);
                // This gets the number of updated rows.
            int updateCount = stmt.getUpdateCount();
            if (hasResults)     // If the query returned a ResultSet
                    // Create and display a table model from the results
                dbQueryPanel.showResults(stmt.getResultSet());
            else
                dbQueryPanel.showUpdates(updateCount);
            updated = updateCount > 0;
        } catch (SQLException | UncheckedSQLException ex) {
            dbQueryPanel.setExecutionTime(0);
            dbQueryPanel.showError(ex);
            getLogger().log(Level.WARNING,
                    "Failed to run query (code: "+dbQueryPanel.getErrorCode()+")",
                    ex);
            JOptionPane.showMessageDialog(this, "Database Error: " + ex,
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
        System.gc();
        if (updated){   // Update the database view if there were changes
            loader = new LoadDatabaseViewer(true);
            loader.execute();
        }
    }//GEN-LAST:event_dbQueryPanelActionPerformed
    
    private CustomTableModel getListSearchTableModel(){
        CustomTableModel model = new CustomTableModel("ListID", "List Name", 
                    "Index", "LinkID", "Link");
        model.setColumnClass(0, Integer.class);
        model.setColumnClass(1, String.class);
        model.setColumnClass(2, Integer.class);
        model.setColumnClass(3, Long.class);
        model.setColumnClass(4, String.class);
        return model;
    }
    
    private CustomTableModel getListSearchTableModel(
            LinkDatabaseConnection conn, ResultSet rs, 
            Map<Integer, String> listNames) throws SQLException{
        CustomTableModel model = getListSearchTableModel();
        while (rs.next()){
            int listID = rs.getInt(LinkDatabaseConnection.LIST_ID_COLUMN_NAME);
            model.addRow(new Object[]{
                listID,
                listNames.get(listID),
                rs.getInt(LinkDatabaseConnection.LINK_INDEX_COLUMN_NAME),
                rs.getLong(LinkDatabaseConnection.LINK_ID_COLUMN_NAME),
                rs.getString(LinkDatabaseConnection.LINK_URL_COLUMN_NAME)
            });
        }
        return model;
    }
    
    private void searchUsedPrefixes(LinkDatabaseConnection conn, int prefixID) 
            throws SQLException{
        TreeMap<Integer, String> listNames = new TreeMap<>(conn.getListNameMap());
        try(PreparedStatement pstmt = conn.prepareStatement(
                LinkDatabaseConnection.USED_PREFIX_SEARCH_QUERY_TEMPLATE)){
            pstmt.setInt(1, prefixID);
            CustomTableModel model = getListSearchTableModel(conn,
                    pstmt.executeQuery(),listNames);
            dbUsedPrefixTable.setModel(model);
        }
        dbUsedPrefixSizeLabel.setText(""+conn.getPrefixMap().getPrefixCount(prefixID));
    }
    
    private void searchListContents(LinkDatabaseConnection conn, String text, 
            Integer prefixID)throws SQLException{
        TreeMap<Integer, String> listNames = new TreeMap<>(conn.getListNameMap());
        try(PreparedStatement pstmt = conn.prepareStatement(
                LinkDatabaseConnection.getListContentsSearchQuery(
                        text != null && !text.isEmpty(), prefixID != null))){
            if (prefixID != null)
                pstmt.setInt(1, prefixID);
            if (text != null && !text.isEmpty())
                pstmt.setString((prefixID != null)?2:1, 
                        "%"+formatSearchQueryPattern(text)+"%");
            dbLinkSearchTable.setModel(getListSearchTableModel(conn,
                    pstmt.executeQuery(),listNames));
        }
    }
    
    private void loadExternalAccountData(){
        if (isLoggedInToDropbox()){
            dbxLoader = new DbxAccountLoader();
            dbxLoader.execute();
        }
        else {
            LinkManagerUtilities.setCard(setLocationPanel,setExternalCard);
            updateExternalDBButtons();
        }
    }
    
    private void setDBLastModLabelText(Long lastMod){
            // If the last modified is null
        if (lastMod == null)
            dbLastModLabel.setText("N/A");
        else
            dbLastModLabel.setText(DEBUG_DATE_FORMAT.format(new java.util.Date(lastMod)));
    }
    /**
     * 
     * @param args
     * @param arg
     * @param name
     * @return 
     * @throws IllegalStateException 
     */
    private static String getArgument(String[] args, String arg, String name){
            // This gets an array containing all the arguments that could be the 
            // argument we're looking for
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
            // Remove any arguments that are either null or don't start with the 
            // argument we're looking for
        argsList.removeIf((String t) -> t == null || !t.startsWith(arg));
            // If there are no arguments that match
        if (argsList.isEmpty())
            return null;
            // If there are too many arguments
        if (argsList.size() > 1){
                // Log that there are too many arguments that match
            getLogger().log(Level.INFO, "Too many arguments for {0}, expected at most 1", name);
                // Tell the user that there are too many arguments that match
            JOptionPane.showMessageDialog(null, 
                    "Too many arguments provided for the "+name+".\n"+
                            "This program expects at most one.", 
                    "ERROR - Too Many "+name.substring(0, 1).toUpperCase()+name.substring(1),
                    JOptionPane.ERROR_MESSAGE);
            throw new IllegalStateException();
        }
        return argsList.get(0).substring(arg.length());
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
            // Set the logger's level to the lowest level in order to log all
        getLogger().setLevel(Level.FINEST);
        try {   // Get the parent file for the log file
            File file = new File(PROGRAM_LOG_PATTERN.replace("%h", 
                    System.getProperty("user.home"))
                    .replace('/', File.separatorChar)).getParentFile();
                // If the parent of the log file doesn't exist
            if (!file.exists()){
                try{    // Try to create the directories for the log file
                    Files.createDirectories(file.toPath());
                } catch (IOException ex){
                    getLogger().log(Level.WARNING, 
                            "Failed to create directories for log file", ex);
                }
            }   // Add a file handler to log messages to a log file
            getLogger().addHandler(new java.util.logging.FileHandler(
                    PROGRAM_LOG_PATTERN,0,16));
        } catch (IOException | SecurityException ex) {
            getLogger().log(Level.SEVERE, "Failed to get log file", ex);
        }   // Log the user's OS name
        getLogger().log(Level.CONFIG, "OS: {0}, version: {1}, arch: {2}", new Object[]{
                System.getProperty("os.name"),
                System.getProperty("os.version"),
                System.getProperty("os.arch")});
            // Log the Java vendor name and url
        getLogger().log(Level.CONFIG, "Java vendor: {0}, URL: {1}", new Object[]{
                System.getProperty("java.vendor"),
                System.getProperty("java.vendor.url")});
            // Log the Java version
        getLogger().log(Level.CONFIG, "Java version: {0}", 
                System.getProperty("java.version"));
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | 
                IllegalAccessException | UnsupportedLookAndFeelException ex) {
            getLogger().log(Level.SEVERE, 
                    "Failed to load Nimbus LnF.", ex);
        }
        //</editor-fold>
            // If there is no look and feel set
        if (UIManager.getLookAndFeel() == null)
            getLogger().log(Level.CONFIG, "Look and Feel: null");
        else    // Log the current Look and Feel
            getLogger().log(Level.CONFIG, "Look and Feel: {0}",
                    UIManager.getLookAndFeel().getName());
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(() -> {
                // This will get the program ID for the program
            UUID programID = null;
            try{    // Get the value for the UUID
                String value = getArgument(args,PROGRAM_ID_ARGUMENT,"program ID");
                    // If there is a program ID provided
                if (value != null)
                        // Get the UUID from the argument
                    programID = UUID.fromString(value);
            } catch (IllegalStateException ex){
                return;
            } catch (IllegalArgumentException ex){
                    // Log the fact that the program ID is invalid
                getLogger().log(Level.INFO, "Program ID is invalid, expected UUID", ex);
                    // Tell the user that the program ID is invalid
                JOptionPane.showMessageDialog(null, 
                        "The program ID is invalid.\n"+ 
                                "The program ID should be a UUID.", 
                        "ERROR - Invalid Program ID",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }   // The configuration file for the program
            File configFile = null;
                // This gets whether the configuration file is invalid
            boolean invalidFile = false;
            try{    // Get the value for the configuration file
                String value = getArgument(args,CONFIG_FILE_ARGUMENT,"configuration file");
                    // If there is a configuration file provided
                if (value != null){
                        // Remove any quotation marks
                    value = FilesExtended.removeQuotations(value).trim();
                        // File path is invalid if empty
                    invalidFile = value.isEmpty();
                        // If the file path is not invalid
                    if (!invalidFile){
                            // Get the configuration file from the argument
                        configFile = new File(value);
                            // Try to turn the file into a path to test if it's valid
                        configFile.toPath();
                    } else {
                            // Log the fact that the config file is invalid
                        getLogger().log(Level.INFO, "Configuration file is invalid (empty path)");
                    }
                }
            } catch (IllegalStateException ex){
                return;
            } catch (InvalidPathException ex){
                invalidFile = true;
                     // Log the fact that the config file is invalid
                getLogger().log(Level.INFO, "Configuration file is invalid", ex);
            }   // If the configuration file is invalid
            if (invalidFile){
                JOptionPane.showMessageDialog(null, 
                        "The configuration file is invalid.", 
                        "ERROR - Invalid Configuration File",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            new LinkManager(DebugCapable.checkForDebugArgument(args),programID,configFile)
                    .setVisible(true);
        });
    }
    @Override
    public boolean isInDebug() {
        return debugMode;
    }
    
    private boolean canActUponSelectedList(){
        return fullyLoaded && isInputEnabled() && 
                !getSelectedTabsPanel().isNonListSelected() && 
                getSelectedTabsPanel().isSelectionEnabled();
    }
    @Override
    public void setInputEnabled(boolean enabled) {
        active = enabled;
        
        dbViewItem.setEnabled(enabled);
        dbRefreshButton.setEnabled(enabled);
        showSchemaToggle.setEnabled(enabled);
        dbResetIDsButton.setEnabled(enabled);
        backupDBButton.setEnabled(enabled);
        dbCreateTablesButton.setEnabled(enabled);
        updateDBFileButton.setEnabled(enabled);
        updateDBFileCombo.setEnabled(updateDBFileButton.isEnabled());
        resetDBFilePathButton.setEnabled(enabled);
        setDBFileNameButton.setEnabled(enabled);
        dbFileNameField.setEditable(setDBFileNameButton.isEnabled());
        dbRemoveUnusedDataButton.setEnabled(enabled);
        dbRemoveDuplDataButton.setEnabled(enabled);
        prefixApplyButton.setEnabled(enabled);
        dbUpdateUsedPrefixesButton.setEnabled(enabled);
        dbUsedPrefixCombo.setEnabled(enabled);
        dbSearchButton.setEnabled(enabled);
        dbSearchField.setEnabled(enabled);
        dbSearchPrefixCheckBox.setEnabled(dbUsedPrefixCombo.isEnabled());
        dbUpdateLastModButton.setEnabled(enabled);
        dbQueryPanel.setEnabled(enabled);
        updateDBSearchPrefixCombo();
        updatePrefixButtons();
        updateListEditButtons();
        updateExternalDBButtons();
        
        setDBLocationItem.setEnabled(enabled);
        updateDBLocationEnabled();
        
        doubleNewLinesToggle.setEnabled(enabled);
        linkOperationToggle.setEnabled(enabled);
        hiddenLinkOperationToggle.setEnabled(enabled&&linkOperationToggle.isSelected());
        autosaveMenu.setEnabled(enabled && !isSavingDatabase());
        autoHideMenu.setEnabled(enabled);
        manageListsItem.setEnabled(enabled);
        updateDatabaseItem.setEnabled(enabled);
        updateListsItem.setEnabled(enabled);
        reloadListsItem.setEnabled(enabled);
        exportListsItem.setEnabled(enabled);
        allListsTabsPanel.setEnabled(enabled);
        shownListsTabsPanel.setEnabled(enabled);
        saveConfigItem.setEnabled(enabled);
        loadConfigItem.setEnabled(enabled);
        listManipulator.setEnabled(enabled);
        listTabsManipulator.setEnabled(enabled);
        addLinksPanel.setEnabled(enabled);
        copyOrMoveListSelector.setEnabled(enabled);
        updateButtons();
        
        listSetOpItem.setEnabled(enabled);
        updateListOperationButtons();
    }
    
    private void updateExternalDBButtons(){
        uploadDBItem.setEnabled(active && isLoggedInToDropbox());
        downloadDBItem.setEnabled(uploadDBItem.isEnabled());
        syncDBToggle.setEnabled(uploadDBItem.isEnabled());
    }
    
    private void updateDBLocationEnabled(){
        setDBResetButton.setEnabled(setDBLocationItem.isEnabled());
        dbFileChangeCombo.setEnabled(setDBLocationItem.isEnabled());
        
            // Local database file settings
        dbFileBrowseButton.setEnabled(setDBLocationItem.isEnabled());
        dbFileField.setEditable(dbFileBrowseButton.isEnabled());
        
        dbxLogInButton.setEnabled(setDBLocationItem.isEnabled() && dbxUtils != null);
        dbxLogOutButton.setEnabled(setDBLocationItem.isEnabled());
        dbxDbFileField.setEditable(dbFileField.isEditable() && dbxUtils != null);
        dbxChunkSizeSpinner.setEnabled(dbxLogInButton.isEnabled());
        
        updateDBLocationButtons();
    }
    
    private void updateDBLocationButtons(){
        boolean enabled = setDBLocationItem.isEnabled();
        String fileName = dbFileField.getText();
        if (fileName != null)
            fileName = FilesExtended.removeQuotations(fileName);
        boolean validFileName = fileName != null && !fileName.isBlank();
        enabled &= validFileName;
        dbFileRelativeButton.setEnabled(dbFileBrowseButton.isEnabled() && validFileName);
        setDBAcceptButton.setEnabled(enabled);
    }
    
    private void updateListEditButtons(){
        dbListEditApplyButton.setEnabled(active && dbListIDCombo.isEnabled());
    }
    
    private void updatePrefixButtons(){
        addPrefixButton.setEnabled(active);
        removePrefixButton.setEnabled(active && dbPrefixTable.getSelectedRow() >= 0);
        prefixCopyButton.setEnabled(removePrefixButton.isEnabled());
    }
    /**
     * This updates the buttons for rearranging, removing, and searching for 
     * links, along with updating the selected link.
     */
    private void updateButtons(){
        searchMenuItem.setEnabled(canActUponSelectedList() && 
                !getSelectedTabsPanel().getSelectedModel().isEmpty());
        searchPanel.setEnabled(searchMenuItem.isEnabled());
        manageLinksButton.setEnabled(searchMenuItem.isEnabled()&&
                !getSelectedList().isReadOnly());
        boolean listMenuEnabled = getSelectedTabsPanel().isEnabled() && canActUponSelectedList();
        setListMenuEnabled(SAVE_TO_FILE_ACTION_KEY, listMenuEnabled);
        setListMenuEnabled(ADD_FROM_FILE_ACTION_KEY,listMenuEnabled);
        setListMenuEnabled(ADD_FROM_TEXT_AREA_ACTION_KEY,listMenuEnabled);
        setListMenuEnabled(REMOVE_FROM_LIST_ACTION_KEY, listMenuEnabled);
        setListMenuEnabled(COPY_TO_LIST_ACTION_KEY,listMenuEnabled);
        setListMenuEnabled(MOVE_TO_LIST_ACTION_KEY,listMenuEnabled && !getSelectedList().isReadOnly());
        setListMenuEnabled(HIDE_LIST_ACTION_KEY,listMenuEnabled);
        setListMenuEnabled(MAKE_LIST_READ_ONLY_ACTION_KEY,listMenuEnabled);
        updatePasteAndAddAction();
        updateSelectedLink();
        updateNewLinkButton();
    }
    
    private void updatePasteAndAddAction(){
        pasteAndAddAction.setEnabled(
                editCommands.get(linkTextField).getPasteAction().isEnabled()&& 
                canActUponSelectedList() &&
                !getSelectedList().isReadOnly() &&
                !getSelectedTabsPanel().getSelectedModel().isFull());
    }
    /**
     * This updates the selected link, along with the buttons that deal with the 
     * selected link.
     */
    private void updateSelectedLink(){
        boolean linkSelected = canActUponSelectedList() && 
                !getSelectedList().isSelectionEmpty();
        openLinkButton.setEnabled(linkSelected&&linkOperationToggle.isEnabled()&&
                linkOperationToggle.isSelected()&&
                (!getSelectedList().isHidden()||hiddenLinkOperationToggle.isSelected()));
        copyLinkButton.setEnabled(openLinkButton.isEnabled());
        editLinkButton.setEnabled(linkSelected &&!getSelectedList().isReadOnly());
        removeLinkButton.setEnabled(editLinkButton.isEnabled());
    }
    
    private boolean canAddNewLink(){
        String text = linkTextField.getText();
        return text != null && !text.isBlank() && isInputEnabled() && 
                !getSelectedTabsPanel().isNonListSelected() &&
                !getSelectedList().isReadOnly() &&
                !getSelectedTabsPanel().getSelectedModel().isFull();
    }
    /**
     * This updates the new link button based off whether a link is being 
     * entered.
     */
    private void updateNewLinkButton(){
        newLinkButton.setEnabled(canAddNewLink());
    }
    /**
     * 
     */
    private void updateListOperationButtons(){
        listSetOpApplyButton.setEnabled(isInputEnabled() &&
            listAOpCombo.getSelectedIndex() != listBOpCombo.getSelectedIndex());
    }
    @Override
    public boolean isInputEnabled() {
        return active;
    }
    @Override
    public JProgressBar getProgressBar() {
        return progressBar;
    }
    @Override
    public JProgressDisplayMenu getProgressDisplayMenu() {
        return progressDisplay;
    }
    @Override
    public void useWaitCursor(boolean isWaiting) {
        setCursor((isWaiting)?Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR):null);
    }
    /**
     * 
     * @param panel 
     */
    private void repaintIfSelected(LinksListPanel panel){
        if (panel == getSelectedList())
            panel.repaint();
    }
    /**
     * This returns whether the program is currently saving a file.
     * @return Whether the program is currently saving a file.
     */
    public boolean isSavingFiles(){
        return !isInputEnabled() && saver != null && saver.isSaving();
    }
    /**
     * This returns whether the program is currently loading a file.
     * @return Whether the program is currently loading a file.
     */
    public boolean isLoadingFiles(){
        return !isInputEnabled() && loader != null && loader.isLoading();
    }
    /**
     * This returns whether the program is currently saving a database file.
     * @return Whether the program is currently saving a database file.
     */
    public boolean isSavingDatabase(){
        return isSavingFiles() && saver instanceof AbstractDatabaseSaver;
    }
    /**
     * This returns whether the program is currently loading a database file.
     * @return Whether the program is currently loading a database file.
     */
    public boolean isLoadingDatabase(){
        return isLoadingFiles() && loader instanceof AbstractDatabaseLoader;
    }
    /**
     * This returns whether there is any 
     * @return 
     */
    private boolean isEdited(){
            // Go through the lists tabs panels
        for (LinksListTabsPanel panel : listsTabPanels){
                // If the current panel is edited or its structure has been 
            if (panel.isEdited() || panel.isStructureEdited())  // modified
                return true;
        }
        return false;
    }
    /**
     * This updates the program title to reflect if there have been any changes 
     * to the data.
     */
    private void updateProgramTitle(){
            // If the data has been modified, put an asterisk in front of the 
            // program name
        setTitle(((isEdited())?"*":"")+PROGRAM_NAME);
    }
    /**
     * 
     * @param fc
     * @param title
     * @return 
     */
    private File showOpenFileChooser(JFileChooser fc, String title){
        return LinkManagerUtilities.showOpenFileChooser(fc, this, config,title);
    }
    /**
     * 
     * @param fc
     * @param title
     * @return 
     */
    private File showSaveFileChooser(JFileChooser fc, String title){
        return LinkManagerUtilities.showSaveFileChooser(fc, this, config,title);
    }
    /**
     * This is the model used for the spinner to set the chunk size for 
     * uploading files to Dropbox.
     */
    private DbxChunkSizeSpinnerModel dbxChunkSizeModel;
    /**
     * This is the configuration file if one was specified at the start of the 
     * program.
     */
    private File configFile = null;
    /**
     * This is used to load data from files.
     */
    private FileLoader loader = null;
    /**
     * This is used to save files.
     */
    private FileSaver saver = null;
    /**
     * This is used to process files in a way that {@code FileLoader} and {@code 
     * FileSaver} don't apply to.
     */
    private FileWorker fileWorker = null;
    /**
     * This is used to perform changes to a LinksListPanel in the background.
     */
    private LinksListWorker linksWorker = null;
    /**
     * This is used to load the account details for the user's Dropbox account.
     */
    private DbxAccountLoader dbxLoader = null;
    /**
     * This is a progress observer used to observe the progress, particularly 
     * when loading and saving lists.
     */
    private ProgressObserver progressObserver;
    /**
     * This is a map that maps the text components to their text edit commands.
     */
    private Map<JTextComponent, TextComponentCommands> editCommands;
    /**
     * This is a map that maps the text components to their undo commands.
     */
    private Map<JTextComponent, UndoManagerCommands> undoCommands;
    /**
     * This is a map that maps the text components to their respective popup 
     * menus.
     */
    private Map<JTextComponent, JPopupMenu> textPopupMenus;
    /**
     * The action for adding an item from the clipboard to the selected list.
     */
    private Action pasteAndAddAction;
    /**
     * This is an array containing all the list models for the lists.
     */
//    private ArrayList<LinksListModel> listModels = new ArrayList<>();
    /**
     * This is used to store and manage the configuration for this program.
     */
    private LinkManagerConfig config;
    /**
     * 
     */
    private DropboxLinkUtils dbxUtils = null;
    /**
     * This is used to format file sizes when displaying the size of a file.
     */
    private ByteUnitFormat byteFormatter = new ByteUnitFormat(true);
    /**
     * This stores whether the program had fully loaded the lists when started.
     */
    private boolean fullyLoaded = false;
    /**
     * Whether this program is currently accepting input.
     */
    private volatile boolean active = true;
    /**
     * Whether this application is in debug mode.
     */
    private final boolean debugMode;
    /**
     * This is an array containing the list tabs panels that are used to display 
     * the lists of links.
     */
    private LinksListTabsPanel[] listsTabPanels;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBoxMenuItem activeToggle;
    private manager.AddLinksFromListPanel addLinksPanel;
    private javax.swing.JButton addPrefixButton;
    private manager.links.LinksListTabsPanel allListsTabsPanel;
    private javax.swing.JLabel allTotalSizeLabel;
    private javax.swing.JCheckBoxMenuItem alwaysOnTopToggle;
    private manager.timermenu.AutoHideMenu autoHideMenu;
    private manager.timermenu.AutosaveMenu autosaveMenu;
    private javax.swing.JButton backupDBButton;
    private javax.swing.JMenuItem clearListSelItem;
    private javax.swing.JMenuItem clearSelTabItem;
    private javax.swing.JPopupMenu.Separator configExitSeparator;
    private javax.swing.JFileChooser configFC;
    private javax.swing.JScrollPane configScrollPane;
    private javax.swing.JTable configTable;
    private javax.swing.JButton copyLinkButton;
    private components.JListSelector<String> copyOrMoveListSelector;
    private manager.SelectedItemCountPanel copyOrMoveSelCountPanel;
    private javax.swing.JDialog databaseDialog;
    private javax.swing.JFileChooser databaseFC;
    private javax.swing.JFileChooser databaseUpdateFC;
    private javax.swing.JScrollPane dbCreatePrefixScrollPane;
    private javax.swing.JTree dbCreatePrefixTree;
    private javax.swing.JButton dbCreateTablesButton;
    private javax.swing.JButton dbFileBrowseButton;
    private javax.swing.JComboBox<String> dbFileChangeCombo;
    private javax.swing.JTextField dbFileField;
    private javax.swing.JTextField dbFileNameField;
    private javax.swing.JPanel dbFilePanel;
    private javax.swing.JButton dbFileRelativeButton;
    private javax.swing.JLabel dbFileSizeLabel;
    private javax.swing.JLabel dbLastModLabel;
    private javax.swing.JPanel dbLinkSearchPanel;
    private javax.swing.JScrollPane dbLinkSearchScrollPane;
    private javax.swing.JTable dbLinkSearchTable;
    private javax.swing.JButton dbListEditApplyButton;
    private javax.swing.JFormattedTextField dbListFlagsField;
    private javax.swing.JComboBox<Integer> dbListIDCombo;
    private javax.swing.JTextField dbListNameField;
    private javax.swing.JPanel dbListPanel;
    private javax.swing.JScrollPane dbListScrollPane;
    private javax.swing.JSpinner dbListSizeLimitSpinner;
    private javax.swing.JCheckBox dbListSizeLimitToggle;
    private javax.swing.JTable dbListTable;
    private javax.swing.JScrollPane dbPrefixScrollPane;
    private javax.swing.JTable dbPrefixTable;
    private javax.swing.JPanel dbPrefixesPanel;
    private javax.swing.JPanel dbPropPanel;
    private manager.database.DatabaseQueryTestPanel dbQueryPanel;
    private javax.swing.JButton dbRefreshButton;
    private javax.swing.JButton dbRemoveDuplDataButton;
    private javax.swing.JButton dbRemoveUnusedDataButton;
    private javax.swing.JButton dbResetIDsButton;
    private javax.swing.JButton dbSearchButton;
    private javax.swing.JTextField dbSearchField;
    private javax.swing.JCheckBox dbSearchPrefixCheckBox;
    private javax.swing.JComboBox<String> dbSearchPrefixCombo;
    private javax.swing.JTabbedPane dbTabbedPane;
    private javax.swing.JPanel dbTablePanel;
    private javax.swing.JScrollPane dbTableScrollPane;
    private javax.swing.JTextArea dbTableStructText;
    private javax.swing.JTable dbTableTable;
    private javax.swing.JLabel dbUUIDLabel;
    private javax.swing.JButton dbUpdateLastModButton;
    private javax.swing.JButton dbUpdateUsedPrefixesButton;
    private javax.swing.JComboBox<String> dbUsedPrefixCombo;
    private javax.swing.JScrollPane dbUsedPrefixScrollPane;
    private javax.swing.JLabel dbUsedPrefixSizeLabel;
    private javax.swing.JTable dbUsedPrefixTable;
    private javax.swing.JPanel dbUsedPrefixesPanel;
    private javax.swing.JLabel dbVersionLabel;
    private javax.swing.JMenuItem dbViewItem;
    private manager.database.DatabaseTableViewer dbViewer;
    private javax.swing.JLabel dbxAccountLabel;
    private javax.swing.JSpinner dbxChunkSizeSpinner;
    private javax.swing.JPanel dbxDataPanel;
    private javax.swing.JTextField dbxDbFileField;
    private javax.swing.JButton dbxLogInButton;
    private javax.swing.JButton dbxLogOutButton;
    private components.JThumbnailLabel dbxPfpLabel;
    private javax.swing.JMenuItem dbxPrintButton;
    private javax.swing.JLabel dbxSpaceFreeLabel;
    private javax.swing.JLabel dbxSpaceUsedLabel;
    private javax.swing.JMenu debugMenu;
    private javax.swing.JCheckBoxMenuItem doubleNewLinesToggle;
    private javax.swing.JMenuItem downloadDBItem;
    private javax.swing.JMenuItem dropboxRefreshTestButton;
    private manager.dropbox.DropboxSetupPanel dropboxSetupPanel;
    private javax.swing.JButton editLinkButton;
    private javax.swing.JMenuItem exitButton;
    private javax.swing.JFileChooser exportFC;
    private javax.swing.JMenuItem exportListsItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JCheckBox foreignKeysToggle;
    private javax.swing.JCheckBoxMenuItem hiddenLinkOperationToggle;
    private javax.swing.JMenuItem hideAllListsItem;
    private javax.swing.JMenu hideListsMenu;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator4;
    private javax.swing.JLabel linkCountLabel;
    private javax.swing.JOptionPane linkEditPane;
    private javax.swing.JCheckBoxMenuItem linkOperationToggle;
    private javax.swing.JTextField linkTextField;
    private javax.swing.JComboBox<List<String>> listAOpCombo;
    private javax.swing.JComboBox<List<String>> listBOpCombo;
    private javax.swing.JComboBox<List<String>> listCOpCombo;
    private manager.SelectedItemCountPanel listManipSelCountPanel;
    private components.JListManipulator<String> listManipulator;
    private javax.swing.JMenu listMenu;
    private javax.swing.JComboBox<String> listOperationCombo;
    private javax.swing.JButton listSetOpApplyButton;
    private javax.swing.JButton listSetOpCancelButton;
    private javax.swing.JDialog listSetOpDialog;
    private javax.swing.JMenuItem listSetOpItem;
    private manager.links.LinksListTabsManipulator listTabsManipulator;
    private javax.swing.JMenuItem loadConfigItem;
    private javax.swing.JPanel locationControlPanel;
    private javax.swing.JMenu makeListReadOnlyMenuAll;
    private javax.swing.JMenu makeListReadOnlyMenuShown;
    private javax.swing.JButton manageLinksButton;
    private javax.swing.JMenuItem manageListsItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton newLinkButton;
    private javax.swing.JFileChooser openFC;
    private javax.swing.JButton openLinkButton;
    private javax.swing.JMenu optionsMenu;
    private javax.swing.JButton pasteAndAddButton;
    private javax.swing.JButton prefixApplyButton;
    private javax.swing.JButton prefixCopyButton;
    private javax.swing.JTextField prefixField;
    private javax.swing.JTextField prefixSeparatorField;
    private javax.swing.JSpinner prefixThresholdSpinner;
    private javax.swing.JCheckBoxMenuItem printAutoHideEventsToggle;
    private javax.swing.JCheckBoxMenuItem printAutosaveEventsToggle;
    private javax.swing.JButton printDBButton;
    private javax.swing.JMenuItem printDataItem;
    private javax.swing.JCheckBoxMenuItem printListPropChangeToggle;
    private javax.swing.JLabel programIDLabel;
    private javax.swing.JProgressBar progressBar;
    private components.progress.JProgressDisplayMenu progressDisplay;
    private javax.swing.JMenuItem reloadListsItem;
    private javax.swing.JButton removeLinkButton;
    private javax.swing.JButton removePrefixButton;
    private javax.swing.JButton resetDBFilePathButton;
    private javax.swing.JMenuItem saveConfigItem;
    private javax.swing.JFileChooser saveFC;
    private javax.swing.JDialog searchDialog;
    private javax.swing.JMenu searchMenu;
    private javax.swing.JMenuItem searchMenuItem;
    private manager.LinkSearchPanel searchPanel;
    private javax.swing.JButton setDBAcceptButton;
    private javax.swing.JButton setDBCancelButton;
    private javax.swing.JButton setDBFileNameButton;
    private javax.swing.JMenuItem setDBLocationItem;
    private javax.swing.JButton setDBResetButton;
    private javax.swing.JPanel setDropboxCard;
    private javax.swing.JMenuItem setDropboxTestButton;
    private javax.swing.JPanel setExternalCard;
    private javax.swing.JDialog setLocationDialog;
    private javax.swing.JPanel setLocationPanel;
    private javax.swing.JMenuItem showAllListsItem;
    private javax.swing.JCheckBoxMenuItem showDBErrorDetailsToggle;
    private javax.swing.JCheckBoxMenuItem showHiddenListsToggle;
    private javax.swing.JCheckBoxMenuItem showIDsToggle;
    private javax.swing.JCheckBox showSchemaToggle;
    private manager.links.LinksListTabsPanel shownListsTabsPanel;
    private javax.swing.JLabel shownTotalSizeLabel;
    private components.debug.SlowTestMenuItem slowTestToggle;
    private javax.swing.JCheckBoxMenuItem syncDBToggle;
    private javax.swing.JPanel tabsPanelDisplay;
    private javax.swing.JButton updateDBFileButton;
    private javax.swing.JComboBox<String> updateDBFileCombo;
    private javax.swing.JMenuItem updateDatabaseItem;
    private javax.swing.JMenuItem updateListsItem;
    private javax.swing.JMenuItem uploadDBItem;
    // End of variables declaration//GEN-END:variables
    /**
     * 
     */
    protected void incrementProgressValue(){
        progressBar.setValue(progressBar.getValue()+1);
    }
    /**
     * This attempts to write the List of Strings to the given file.
     * @param file The file to write to.
     * @param list The list of String to write to the file.
     * @return If the file was successfully written.
     */
    private boolean writeToFile(File file, List<String> list){
        return LinkManagerUtilities.writeToFile(file, list, 
                doubleNewLinesToggle.isSelected(), progressObserver);
    }
    /**
     * 
     * @return 
     */
    private boolean saveConfigFile() throws IOException{
            // Get the configuration file
        File file = getConfigFile();
            // If the configuration properties is not empty or the file exists
        if (!config.getProperties().isEmpty() || file.exists())
                // Save the configuration properties to file
            return LinkManagerUtilities.saveProperties(file,config.getProperties(),
                    GENERAL_CONFIG_HEADER);
        return true;
    }
    /**
     * This updates the values in the program's configuration that would update 
     * too frequently if updated in real time or that would be too difficult to 
     * cover all possible ways of the value being set.
     */
    private void updateProgramConfig(){
            // If the program has fully loaded
        if (fullyLoaded){
                // Map the list panels to their listIDs
            Map<Integer,LinksListPanel> panels = new HashMap<>();
                // Go through the list panels in the selected tabs panel
            for (LinksListPanel panel : getSelectedTabsPanel()){
                    // If the list panel's listID is not null
                if (panel.getListID() != null)
                    panels.put(panel.getListID(), panel);
            }   // Go through the tabs panel
            for (LinksListTabsPanel tabsPanel : listsTabPanels){
                    // If the current tabs panel is the selected panel
                if (tabsPanel == getSelectedTabsPanel())
                    continue;
                    // Go through the list panels in the current tabs panel
                for (LinksListPanel panel : tabsPanel){
                        // If the list panel's listID is not null
                    if (panel.getListID() != null)
                        panels.putIfAbsent(panel.getListID(), panel);
                }
            }   // Go through the list panels
            for (LinksListPanel panel : panels.values()){
                    // Update the visible properties for the panel in the config
                config.setVisibleSection(panel);
            }
        }   // Set the search text in the configuration
        config.setSearchText(searchPanel.getSearchText());
            // Set the entered link text in the configuration
        config.setEnteredLinkText(linkTextField.getText());
    }
    /**
     * This loads the configuration for the program from the configuration map.
     */
    private void configureProgram(){
            // Get the progress display value from the config
        int temp = config.getProgressDisplaySetting(0);
            // If the progress display value is not zero
        if (temp != 0)
                // Set the progress display value
            progressDisplay.setDisplaySettings(temp);
            // Set the always on top from the config
        alwaysOnTopToggle.setSelected(config.isAlwaysOnTop(
                alwaysOnTopToggle.isSelected()));
        super.setAlwaysOnTop(alwaysOnTopToggle.isSelected());
            // Set the double new lines property from the config
        doubleNewLinesToggle.setSelected(config.getAddBlankLines(
                doubleNewLinesToggle.isSelected()));
            // Set the link operations enabled property from the config
        linkOperationToggle.setSelected(config.isLinkOperationsEnabled(
                linkOperationToggle.isSelected()));
            // Set the search matches spaces property from the config
        searchPanel.setMatchSpaces(config.getSearchMatchSpaces(
                searchPanel.getMatchSpaces()));
            // Set the search matches case property from the config
        searchPanel.setMatchCase(config.getSearchMatchCase(
                searchPanel.getMatchCase()));
            // Set the search matches wrap around property from the config
        searchPanel.setWrapAround(config.getSearchWrapAround(
                searchPanel.getWrapAround()));
            // Set the search text from the config
        searchPanel.setSearchText(config.getSearchText());
            // Enable the hidden lists link operation if link operations are 
            // enabled
        hiddenLinkOperationToggle.setEnabled(active&&linkOperationToggle.isSelected());
            // Set whether hidden lists link operations enabled property from 
            // the config
        hiddenLinkOperationToggle.setSelected(config.isHiddenLinkOperationsEnabled(
                hiddenLinkOperationToggle.isSelected()));
            // Set whether additional details are shown when an error occurs 
            // with the database
        showDBErrorDetailsToggle.setSelected(
                config.getDatabaseErrorDetailsAreShown(
                        showDBErrorDetailsToggle.isSelected()));
            // Set whether the program syncs the database to an external source 
            // upon saving or loading
        syncDBToggle.setSelected(config.getDatabaseWillSync(
                syncDBToggle.isSelected()));
            // Set the operation to use when changing the location of the 
            // database file
        dbFileChangeCombo.setSelectedIndex(
                config.getDatabaseFileChangeOperation(
                        dbFileChangeCombo.getSelectedIndex()));
            // Update the fields showing the database file location
        updateDatabaseFileFields();
            // Set the autosave frequency index
        autosaveMenu.setFrequencyIndex(config.getAutosaveFrequencyIndex(
                autosaveMenu.getFrequencyIndex()));
            // Set the auto-hide wait duration index
        autoHideMenu.setDurationIndex(config.getAutoHideWaitDurationIndex(
                autoHideMenu.getDurationIndex()));
            // Set the Dropbox chunk size multiplier
        dbxChunkSizeModel.setMultiplier(config.getDropboxChunkSizeMultiplier(
                dbxChunkSizeModel.getMultiplier()));
            // If the program has fully loaded
        if (fullyLoaded){
                // Set the selection from the config
            setSelectedFromConfig();
        } else {    // Only load these settings when the program first starts up
                // If the auto hide menu is set to automatically hide the hidden 
            if (autoHideMenu.getDurationIndex() != 0)   // lists
                    // Clear the hidden lists are chown value
                config.setHiddenListsAreShown(null);
                // Set the entered link from the config
            linkTextField.setText(config.getEnteredLinkText());
                // Go through the components with sizes saved to config
            for (Component comp : config.getComponentNames().keySet()){
                    // Load the size for the component from the config
                config.loadComponentSize(comp);
                    // Load the location for the component from the config
                config.loadComponentLocation(comp);
                    // Load the bounds for the component from the config
                config.loadComponentBounds(comp);
            }   // Remove the database location dialog's size from the config 
                // since it uses the bounds now
                // TODO: If and when this would affect the actual bounds 
                // property, then don't set this anymore.
            config.setComponentSize(setLocationDialog, null);
                // If the bounds for the database location dialog is not set
            if (!config.isComponentBoundsSet(setLocationDialog))
                    // Make the database location dialog's position relative to 
                    // to the program's
                setLocationDialog.setLocationRelativeTo(this);
                // If the location for the search dialog is not set
            if (!config.isComponentLocationSet(searchDialog))
                    // Make the search dialog's position relative to the 
                    // program's
                searchDialog.setLocationRelativeTo(this);
        }   // Set the show hidden lists property from the config
        showHiddenListsToggle.setSelected(config.getHiddenListsAreShown(
                showHiddenListsToggle.isSelected()));
            // Update the visible lists
        updateVisibleTabsPanel();
    }
    /**
     * 
     */
    private void setSelectedFromConfig(){
            // This maps listIDs to the selected link for that list
        Map<Integer,String> selMap = config.getSelectedLinkMap();
            // This maps the listIDs to whether the selected link is visible for 
            // that list
        Map<Integer,Boolean> selVisMap = config.getSelectedLinkIsVisibleMap();
            // This maps the listIDs to the first visible index for that list
        Map<Integer,Integer> firstVisMap = config.getFirstVisibleIndexMap();
            // This maps the listIDs to the last visible index for that list
        Map<Integer,Integer> lastVisMap = config.getLastVisibleIndexMap();
            // This maps the tabs panel indexes to the listID of the selected 
            // list for that tabs panel
        Map<Integer,Integer> selListIDMap = new HashMap<>(config.getCurrentTabListIDMap());
            // This maps the tabs panel indexes to the selected index of the 
            // tab for that tabs panel
        Map<Integer,Integer> selListMap = config.getCurrentTabIndexMap();
            // This maps the listIDs to the visible rectangle for that list
        Map<Integer,Rectangle> visRectMap = config.getVisibleRectMap();
            // Go through the list tabs panels
        for (LinksListTabsPanel tabsPanel : listsTabPanels){
                // Go through the list panels in the current list tabs panel
            for (LinksListPanel panel : tabsPanel){
                    // If the current list panel does not have a listID
                if (panel.getListID() == null)
                    continue;
                    // Get the current list panel's listID
                int listID = panel.getListID();
                    // If the link selection map contains the listID for the list
                if (selMap.containsKey(listID)){
                        // Get the selected link for the list
                    String selected = selMap.get(listID);
                        // If the list does not contain the selected link
                    if (!panel.getModel().contains(selected))
                            // No link will be selected for the list
                        selected = null;
                        // Set the selected link for the list, scrolling to the 
                        // link if it is meant to be visible
                    panel.setSelectedValue(selected, 
                            selVisMap.getOrDefault(listID, false));
                }   // Get the visible rectangle for the list
                Rectangle rect = visRectMap.get(listID);
                    // If there is a visible rectangle for the list
                if (rect != null)
                        // Scroll the list to the visbile rectangle
                    panel.getList().scrollRectToVisible(rect);
                    // Get the first visible index for the list, defaulting to 
                    // the last visible index if there isn't one
                Integer visIndex = firstVisMap.getOrDefault(listID,
                        lastVisMap.get(listID));
                    // If there is a visible index for the list
                if (visIndex != null)
                        // Ensure the visible index is visible
                    panel.getList().ensureIndexIsVisible(visIndex);
            }
        }   // Replace the selected listIDs with the indexes for those listIDs
        selListIDMap.replaceAll((Integer listType, Integer listID) -> {
                // If the list type is not null and is a valid index in the 
                // tabs panel array and the listID is not null
            if (listType != null && listType >= 0 && 
                    listType < listsTabPanels.length && listID != null)
                    // Get the index of the listID
                return listsTabPanels[listType].getListIDs().indexOf(listID);
            return null;
        }); 
            // Remove any null and negative selected indexes for listIDs
        selListIDMap.values().removeIf((Integer t) -> t == null || t < 0);
            // Go through the tabs panel array
        for (int i = 0; i < listsTabPanels.length; i++){
                // Get the index of the selected list, prioritizing the index 
                // for the selected listID (since listIDs don't typically change 
                // between instances of the program), then the index of the 
                // last selected index, and defaulting to 0 to select the first tab
            int index = selListIDMap.getOrDefault(i, selListMap.getOrDefault(i,0));
                // If the selected index is in bounds or the selection should be 
            if (index >= -1 && index<listsTabPanels[i].getTabCount()) // cleared
                listsTabPanels[i].setSelectedIndex(index);
        }
    }
    /**
     * 
     * @param conn
     * @param models
     * @throws SQLException 
     */
    private void writeToDatabase(LinkDatabaseConnection conn, 
            Collection<LinksListModel> models) throws SQLException {
        getLogger().entering(this.getClass().getName(), "writeToDatabase");
            // Get the current state of the connection's auto-commit
        boolean autoCommit = conn.getAutoCommit();
            // Turn off the connection's auto-commit to group the following 
            // database transactions to improve performance
        conn.setAutoCommit(false);
            // This gets the map of list names from the database
        ListNameMap listNameMap = conn.getListNameMap();
            // This gets the map of list contents from the database
        ListDataMap listDataMap = conn.getListDataMap();
            // This gets the map of links from the database
        LinkMap linkMap = conn.getLinkMap();
            // A set to use to sort the list models that have a listID
        TreeSet<LinksListModel> sortedModels = new TreeSet<>();
            // A set to store the listIDs encounted while sorting the models
        HashSet<Integer> usedListIDs = new HashSet<>();
            // This will get a set of unsorted models that do not have a listID
        LinkedHashSet<LinksListModel> unsortedModels = new LinkedHashSet<>(models);
            // Go through the models to be sorted
        for (LinksListModel model : unsortedModels){
                // If the model has a listID set for it
            if (model.getListID() != null){
                    // If some other list is already using that listID
                if (usedListIDs.contains(model.getListID()))
                        // Remove the listID from the model
                    model.setListID(null);
                else{
                    sortedModels.add(model);
                    usedListIDs.add(model.getListID());
                }
            }
        }   // Turn the collection of models to be saved into a set containing 
            // the sorted models
        models = new LinkedHashSet<>(sortedModels);
        models.addAll(unsortedModels);
            // This is the total size of the models that had their contents 
        int total = 0;  // modified
            // This is a set containing the links from the models
        Set<String> linksSet = new LinkedHashSet<>();
            // Go through the models to be saved
        for (LinksListModel model : models){
                // Add the list to the list map if not absent
            Integer listID = listNameMap.addIfAbsent(model);
                // Update the properties of the list based off the model
            listDataMap.get(listID).updateProperties(model);
                // If the model's contents were modified
            if (model.getContentsModified())
                total += model.size();
            linksSet.addAll(model);
        }   // Remove null if present in the set
        linksSet.remove(null);
            // This gets the map of prefixes from the database
        PrefixMap prefixMap = conn.getPrefixMap();
            // This creates any prefixes that need to be created from the links 
            // and gets a map containing the new prefixes
        Map<Integer,String> newPrefixes = prefixMap.createPrefixesFrom(linksSet);
        conn.commit();          // Commit the changes to the database
        System.gc();            // Run the garbage collector
            // Remove all the links already in the link map to get any new links
        linksSet.removeAll(new HashSet<>(linkMap.values()));
            // This is a set that will get all the links that may need to have 
            // their prefix updated to the new longest prefix
        Set<Long> outdatedLinks = new LinkedHashSet<>();
            // Go through the new prefixes
        for (String prefix : newPrefixes.values()){
                // Add all the links with the current new prefix
            outdatedLinks.addAll(linkMap.getStartsWith(prefix).navigableKeySet());
        }
        
        progressBar.setMaximum(linksSet.size()+outdatedLinks.size()+total);
        progressBar.setIndeterminate(false);
            // Update the prefixes of the outdated links
        conn.updateLinkPrefix(outdatedLinks, progressObserver);
        
        progressBar.setIndeterminate(true);
        conn.commit();          // Commit the changes to the database
        System.gc();            // Run the garbage collector
            // Add the new links to the database.
        linkMap.addAll(linksSet, progressObserver);
        progressBar.setIndeterminate(false);
            // This gets a cached copy of the inverse link map
        Map<String,Long> linkIDMap = new HashMap<>(conn.getLinkMap().inverse());
            // Go through the models to be saved
        for (LinksListModel model : models){
                // If the model's contents were modified
            if (model.getContentsModified()){
                conn.updateListContents(model, linkIDMap, progressObserver);
            }
        }
        progressBar.setIndeterminate(true);
            // Go through the tab panels
        for (int i = 0; i < listsTabPanels.length; i++){
                // Update the list of the listIDs
            conn.updateListIDList(i, listsTabPanels[i], progressObserver);
        }   // Remove any listIDs from the shown listIDs list that are hidden
        conn.getShownListIDs().removeIf((Integer t) -> {
                // Get the model with the current listID
            LinksListModel model = allListsTabsPanel.getModelWithListID(t);
                // Remove if no model has that listID or the model is hidden
            return model == null || model.isHidden();
        });
        conn.commit();       // Commit the changes to the database
        System.gc();         // Run the garbage collector
            // Remove any duplicate links
        linkMap.removeDuplicateRows();
            // Remove any unused links
        linkMap.removeUnusedRows();
        conn.commit();       // Commit the changes to the database
            // Restore the connection's auto-commit back to what it was set to 
        conn.setAutoCommit(autoCommit);     // before
        getLogger().entering(this.getClass().getName(), "writeToDatabase");
    }
    /**
     * 
     * @param conn
     * @return
     * @throws SQLException 
     */
    private boolean saveDatabase(LinkDatabaseConnection conn) throws SQLException{
        getLogger().entering(this.getClass().getName(), "saveDatabase");
            // Remove the listIDs of any lists that have been removed
        conn.getAllListIDs().removeAll(allListsTabsPanel.getRemovedListIDs());
            // Remove the listIDs of any lists that have been removed
        conn.getShownListIDs().removeAll(allListsTabsPanel.getRemovedListIDs());
            // Remove the listIDs of any lists that have been hidden
        conn.getShownListIDs().removeAll(shownListsTabsPanel.getRemovedListIDs());
            // Remove any lists that have been removed
        conn.getListNameMap().keySet().removeAll(allListsTabsPanel.getRemovedListIDs());
            // Remove the preference nodes for the removed lists
        config.removeListPreferences(allListsTabsPanel.getRemovedListIDs());
            // Clear the sets of removed ListIDs
        allListsTabsPanel.clearRemovedListIDs();
        shownListsTabsPanel.clearRemovedListIDs();
        conn.commit();       // Commit the changes to the database
            // This is a set containing all the models in the program
        Set<LinksListModel> models = new LinkedHashSet<>(allListsTabsPanel.getModels());
            // Add any models that are in the shown lists panel that are 
            // missing from the all lists panel
        models.addAll(shownListsTabsPanel.getModels());
            // Write the models to the database
        writeToDatabase(conn, models);
        getLogger().exiting(this.getClass().getName(), "saveDatabase", true);
        return true;
    }
    /**
     * 
     * @param file
     * @param path
     * @return
     * @throws DbxException
     * @throws IOException 
     */
    private FileMetadata downloadFromDropbox(File file, String path) throws 
            DbxException, IOException{
        getLogger().entering(this.getClass().getName(), "downloadFromDropbox", 
                new Object[]{file, path});
            // Get a client to communicate with Dropbox, refreshing the Dropbox 
            // credentials if necessary
        DbxClientV2 client = dbxUtils.createClientUtils().getClientWithRefresh();
            // Get the file namespace for Dropbox
        DbxUserFilesRequests dbxFiles = client.files();
            // This gets the size of the file to be downloaded
        Long size = null;
        try{    // Get the metadata for the file
            Metadata metadata = dbxFiles.getMetadataBuilder(path).start();
                // If the metadata is actually file metadata
            if (metadata instanceof FileMetadata){
                    // Get the size of the file
                size = ((FileMetadata) metadata).getSize();
            }
        } catch (GetMetadataErrorException ex){
            getLogger().log(Level.WARNING,"Failed to download from Dropbox",ex);
                // If the error because the file doesn't exist
            if (DropboxUtilities.fileNotFound(ex)){
                getLogger().exiting(this.getClass().getName(), 
                        "downloadFromDropbox", null);
                return null;
            }else 
                throw ex;
        }   // This is the progress listener to use to listen to how many bytes 
            // have been downloaded so far.
        ProgressListener listener = null;
            // If the file size was loaded
        if (size != null){
                // Setup the progress bar and get the progress listener
            listener = DropboxUtilities.setUpProgressListener(size, progressObserver);
                // Set the progress bar to not be indeterminate
            progressBar.setIndeterminate(false);
        }   // Download the file from Dropbox
        FileMetadata data = DropboxUtilities.download(file, path, dbxFiles, listener);
        getLogger().exiting(this.getClass().getName(), "downloadFromDropbox", data);
        return data;
    }
    /**
     * 
     * @param file
     * @param path
     * @return
     * @throws DbxException
     * @throws IOException 
     */
    private FileMetadata uploadToDropbox(File file, String path) throws 
            DbxException, IOException{
        getLogger().entering(this.getClass().getName(), "uploadToDropbox", 
                new Object[]{file, path});
            // Get a client to communicate with Dropbox, refreshing the Dropbox 
            // credentials if necessary
        DbxClientV2 client = dbxUtils.createClientUtils().getClientWithRefresh();
            // Setup the progress bar and get the progress listener used to 
            // update the progress bar to reflect the bytes that have been 
            // uploaded so far.
        ProgressListener listener = DropboxUtilities.setUpProgressListener(
                file.length(), progressObserver);
            // Set the progress bar to not be indeterminate
        progressBar.setIndeterminate(false);
            // Upload the file to Dropbox, using the set chunk size and 
            // overwriting the file if it already exists
        FileMetadata data = DropboxUtilities.upload(file, path, client.files(), 
                dbxChunkSizeModel.getChunkSize(), true, listener);
        getLogger().exiting(this.getClass().getName(), "uploadToDropbox", data);
        return data;
    }
    /**
     * 
     * @param model
     * @param list
     * @return 
     */
    private LinksListModel addAllToCopy(LinksListModel model, Collection<String> list){
            // Clone the model
        model = new LinksListModel(model);
        try{
            model.addAll(list);
        } catch(Exception ex){
            getLogger().log(Level.WARNING, 
                    "Issue encountered while adding collection to model", ex);
        }
        return model;
    }
    /**
     * 
     * @param panel
     * @param list
     * @return 
     */
    private LinksListModel addAllToCopy(LinksListPanel panel, Collection<String> list){
        return addAllToCopy(panel.getModel(),list);
    }
    /**
     * This is a LinksListTabAction that saves the links from a list to a 
     * file.
     */
    private class SaveToFileAction extends LinksListTabAction{
        /**
         * This constructs an SaveToFileAction that will save the links from the 
         * given list panel to a file.
         * @param panel The list panel to save the links from.
         */
        SaveToFileAction(LinksListTabsPanel tabsPanel, LinksListPanel panel) {
            super(SAVE_TO_FILE_ACTION_KEY, tabsPanel, panel);
        }
        @Override
        public void actionPerformed(ActionEvent evt, LinksListPanel panel, 
                LinksListTabsPanel tabsPanel) {
                // Get the file to save to
            File file = showSaveFileChooser(saveFC,
                    "Save "+panel.getListName()+" To File...");
            if (file != null){  // If the user selected a file
                saver = new ListSaver(file,panel.getModel());
                saver.execute();
            }
        }
        @Override
        protected String getNewActionName(LinksListPanel panel){
            return "Save "+getListName(panel);
        }
    }
    /**
     * This is a LinksListTabAction that adds links from a text file to a 
     * list.
     */
    private class AddFromFileAction extends LinksListTabAction.LinksListTabEditAction{
        /**
         * This constructs an AddFromFileAction that will add links to the given
         * list panel.
         * @param panel The panel to add the links to.
         */
        AddFromFileAction(LinksListTabsPanel tabsPanel, LinksListPanel panel){
            super(ADD_FROM_FILE_ACTION_KEY,tabsPanel,panel);
        }
        @Override
        public void actionPerformed(ActionEvent evt, LinksListPanel panel, 
                LinksListTabsPanel tabsPanel) {
                // Get the file to load from
            File file = showOpenFileChooser(openFC,
                    "Load "+panel.getListName()+" From File...");
            if (file != null){  // If the user selected a file
                loader = new ListLoader(file,panel);
                loader.execute();
            }
        }
        @Override
        protected String getNewActionName(LinksListPanel panel){
            return "Add To "+getListName(panel);
        }
    }
    /**
     * This is a LinksListTabAction that adds links from a text area to a 
     * list.
     */
    private class AddFromTextAreaAction extends LinksListTabAction.LinksListTabEditAction{
        /**
         * This constructs an AddFromTextAreaAction that will add links to the 
         * given list panel.
         * @param panel The list panel to add the links to.
         */
        AddFromTextAreaAction(LinksListTabsPanel tabsPanel,LinksListPanel panel){
            super(ADD_FROM_TEXT_AREA_ACTION_KEY,tabsPanel, panel);
        }
        @Override
        public void actionPerformed(ActionEvent evt, LinksListPanel panel, 
                LinksListTabsPanel tabsPanel) {
            if (beepWhenDisabled()) // If input is disabled
                return;
                // Set the dialog title
            addLinksPanel.setDialogTitle("Add To "+panel.getListName()+"...");
                // Show the add links dialog and if the user selected the accept 
            if (addLinksPanel.showDialog(LinkManager.this) == // option
                    AddLinksFromListPanel.ACCEPT_OPTION){
                linksWorker = new AddFromTextWorker(panel,addLinksPanel.getText());
                linksWorker.execute();
            }
            addLinksPanel.setPreferredSize(addLinksPanel.getSize());
                // Set the add list panel's size in the config
            config.setComponentSize(addLinksPanel);
        }
        @Override
        protected String getNewActionName(LinksListPanel panel){
            return "Add To "+getListName(panel);
        }
    }
    /**
     * This is a LinksListTabAction that copies or moves links from the 
     * selected list to another list.
     */
    private class CopyOrMoveToListAction extends LinksListTabAction.LinksListTabEditAction{
        /**
         * This stores whether this moves or copies links.
         */
        private final boolean move;
        /**
         * This constructs an CopyOrMoveToListAction that will copy or move 
         * links to the given list panel, depending on the {@code move} value.
         * @param panel The list panel to copy or move the links to.
         * @param move Whether this will copy or move links to the panel.
         */
        CopyOrMoveToListAction(LinksListTabsPanel tabsPanel, 
                LinksListPanel panel, boolean move) {
                // If this moves links, use the move to list action key. 
                // Otherwise, use the copy to list action key
            super((move)?MOVE_TO_LIST_ACTION_KEY:COPY_TO_LIST_ACTION_KEY,
                    tabsPanel,panel);
            this.move = move;
                // Update the action name
            updateActionName();
        }
        /**
         * 
         * @return 
         */
        public boolean getMovesLinks(){
            return move;
        }
        /**
         * 
         * @return 
         */
        public boolean getCopiesLinks(){
            return !move;
        }
        /**
         * 
         * @return 
         */
        public String getActionText(){
                // If this moves links, return the word "Move". Otherwise, 
                // return the word "Copy"
            return (move)?"Move":"Copy";
        }
        @Override
        public void actionPerformed(ActionEvent evt, LinksListPanel panel, 
                LinksListTabsPanel tabsPanel) {
                // If the given panel is selected or no panels are selected
            if (tabsPanel.isSelected(panel) || tabsPanel.isNonListSelected())
                return;
            copyOrMoveListSelector.setModel(tabsPanel.getSelectedModel());
                // Get the currently selected panel
            LinksListPanel selPanel = tabsPanel.getSelectedList();
            copyOrMoveListSelector.setSelectedValue(selPanel.getSelectedValue(), true);
            copyOrMoveListSelector.setAcceptButtonText(getActionText());
            copyOrMoveListSelector.setListTitle("Select from "+selPanel.getListName()+"...");
            copyOrMoveListSelector.setDialogTitle(getActionText()+" To "+panel.getListName()+"...");
            copyOrMoveSelCountPanel.setMaximumValue(panel.getModel().getSpaceRemaining());
                // Show the list selector dialog and if the user pressed the 
                // accept option
            if (copyOrMoveListSelector.showDialog(LinkManager.this) == 
                    JListSelector.ACCEPT_OPTION){
                linksWorker = new CopyOrMoveLinks(selPanel,panel,
                        copyOrMoveListSelector.getSelectedValuesList(),move);
                linksWorker.execute();
            }
            copyOrMoveListSelector.setPreferredSize(
                    copyOrMoveListSelector.getSize());
                // Set the copy or move selector panel's size in the config
            config.setComponentSize(copyOrMoveListSelector);
        }
        @Override
        protected String getNewActionName(LinksListPanel panel){
            return getActionText()+" To "+getListName(panel);
        }
        @Override
        public int getActionControlFlags(){
            return super.getActionControlFlags() | 
                    LinksListAction.LIST_MUST_NOT_BE_SELECTED_FLAG;
        }
    }
    /**
     * 
     */
    private class RemoveFromListAction extends LinksListTabAction.LinksListTabEditAction{
        /**
         * 
         * @param tabsPanel
         * @param panel 
         */
        RemoveFromListAction(LinksListTabsPanel tabsPanel, LinksListPanel panel){
            super(REMOVE_FROM_LIST_ACTION_KEY,tabsPanel,panel);
        }
        @Override
        public void actionPerformed(ActionEvent evt, LinksListPanel panel, 
                LinksListTabsPanel tabsPanel) {
                // If the given panel is not selected and a list is selected
            if (!tabsPanel.isSelected(panel) && !tabsPanel.isNonListSelected()){
                linksWorker = new RemoveLinksWorker(tabsPanel.getSelectedList(),panel);
                linksWorker.execute();
            }
        }
        @Override
        protected String getNewActionName(LinksListPanel panel){
            return "Remove From "+getListName(panel);
        }
        @Override
        public int getActionControlFlags(){
            return LinksListAction.LIST_MUST_BE_ENABLED_FLAG | 
                    LinksListAction.LIST_MUST_NOT_BE_SELECTED_FLAG;
        }
    }
    /**
     * 
     */
    private class RemoveFromListsAction extends LinksListTabAction.LinksListTabEditAction{
        /**
         * Whether this action will remove the current list from the other 
         * lists. {@code true} if this will remove the current list from the 
         * other lists, {@code false} if this removes the other lists from the 
         * current list.
         */
        private final boolean removeFromOtherLists;
        /**
         * Whether this action affects only hidden lists.
         */
        private final boolean hiddenOnly;
        /**
         * 
         * @param tabsPanel
         * @param panel
         * @param removeFromOtherLists Whether this action will remove the 
         * currently selected list from the other lists ({@code true} if this 
         * will remove the currently selected list from the other lists, {@code 
         * false} if this removes the other lists from the currently selected 
         * list).
         * @param hiddenOnly Whether this action only removes from hidden lists.
         */
        RemoveFromListsAction(LinksListTabsPanel tabsPanel, LinksListPanel panel, 
                boolean removeFromOtherLists, boolean hiddenOnly) {
            super(REMOVE_FROM_LIST_ACTION_KEY, tabsPanel, panel);
            this.removeFromOtherLists = removeFromOtherLists;
            this.hiddenOnly = hiddenOnly;
            updateActionName();
        }
        /**
         * 
         * @param tabsPanel
         * @param panel
         * @param removeFromOtherLists Whether this action will remove the 
         * currently selected list from the other lists ({@code true} if this 
         * will remove the currently selected list from the other lists, {@code 
         * false} if this removes the other lists from the currently selected 
         * list).
         */
        RemoveFromListsAction(LinksListTabsPanel tabsPanel, LinksListPanel panel, 
                boolean removeFromOtherLists) {
            this(tabsPanel,panel,removeFromOtherLists,false);
        }
        /**
         * 
         * @param tabsPanel
         * @param panel 
         */
        RemoveFromListsAction(LinksListTabsPanel tabsPanel, LinksListPanel panel){
            this(tabsPanel,panel,true);
        }
        /**
         * This returns whether this action will will remove the currently 
         * selected list from the other lists.
         * @return {@code true} if this will remove the currently selected list 
         * from the other lists, {@code false} if this removes the other lists 
         * from the currently selected list.
         */
        public boolean getRemovesFromOtherLists(){
            return removeFromOtherLists;
        }
        /**
         * This returns whether this action will only remove from hidden lists.
         * @return Whether this action will only remove from hidden lists.
         */
        public boolean getHiddenListsOnly(){
            return hiddenOnly;
        }
        @Override
        public void actionPerformed(ActionEvent evt, LinksListPanel panel, 
                LinksListTabsPanel tabsPanel) {
            linksWorker = new RemoveLinksWorker2(panel,tabsPanel,
                    removeFromOtherLists,hiddenOnly);
            linksWorker.execute();
        }
        @Override
        protected String getNewActionName(LinksListPanel panel){
                // If this only affects hidden lists, specifiy that only hidden 
                // lists are to be affected
            String otherLists = (hiddenOnly)?"Other Hidden Lists":"Other Lists";
                // If this action removes the current list from the other lists
            if (removeFromOtherLists)
                return String.format("Remove From All %s",otherLists);
            else
                return String.format("Remove %s From Current List",otherLists);
        }
        @Override
        public int getActionControlFlags(){
            return LinksListAction.LIST_MUST_BE_ENABLED_FLAG;
        }
        @Override
        public int getRequiredFlags(){
                // If this action removes the current list from the other lists
            if (removeFromOtherLists)
                return 0;
            return super.getRequiredFlags();
        }
    }
    /**
     * 
     */
    private class HideListAction extends LinksListTabAction.LinksListTabCheckAction{
        /**
         * 
         * @param tabsPanel
         * @param panel 
         */
        HideListAction(LinksListTabsPanel tabsPanel, LinksListPanel panel){
            super(HIDE_LIST_ACTION_KEY,tabsPanel,panel);
        }
        @Override
        protected String getNewActionName(LinksListPanel panel){
            return "Hide " + getListName(panel);
        }
        @Override
        protected void actionPerformed(ActionEvent evt, LinksListPanel panel, 
                LinksListTabsPanel tabsPanel, AbstractButton button, 
                boolean isSelected) {
                // If there would be no change as to whether the panel would be 
            if (panel.isHidden() == isSelected) // hidden
                return;
            panel.setHidden(isSelected);
                // If this item is selected
            if (isSelected)
                shownListsTabsPanel.getModels().remove(panel.getModel());
            else
                shownListsTabsPanel.getModels().add(panel.getModel());
        }
        @Override
        protected boolean isSelected(LinksListPanel panel) {
            return panel.isHidden();
        }
    }
    /**
     * 
     */
    private class MakeListReadOnlyAction extends LinksListTabAction.LinksListTabCheckAction{
        /**
         * 
         * @param tabsPanel
         * @param panel 
         */
        MakeListReadOnlyAction(LinksListTabsPanel tabsPanel, LinksListPanel panel){
            super(MAKE_LIST_READ_ONLY_ACTION_KEY,tabsPanel,panel);
        }
        @Override
        protected String getNewActionName(LinksListPanel panel){
            return getListName(panel) + " is Read Only";
        }
        @Override
        protected void actionPerformed(ActionEvent evt, LinksListPanel panel, 
                LinksListTabsPanel tabsPanel, AbstractButton button, 
                boolean isSelected) {
            panel.setReadOnly(isSelected);
        }
        @Override
        protected boolean isSelected(LinksListPanel panel) {
            return panel.isReadOnly();
        }
    }
    /**
     * This is an abstract class that provides the basic framework for doing 
     * something in a background thread for the LinkManager program. This 
     * primarily handles setting up the program before and after performing the 
     * background task.
     */
    private abstract class LinkManagerWorker<E> extends SwingWorker<E,Void>{
        /**
         * This returns the String that is displayed for the progress bar.
         * @return The String to display on the progress bar.
         */
        public abstract String getProgressString();
        /**
         * This is the action performed in the background by this 
         * LinkManagerWorker after everything has been set up. This is called by 
         * {@link #doInBackground} to perform the action in the background.
         * @return The computed result.
         * @throws Exception If an error occurs.
         */
        protected abstract E backgroundAction() throws Exception;
        /**
         * 
         */
        protected void updateProgressString(){
            progressDisplay.setString(getProgressString());
        }
        @Override
        protected E doInBackground() throws Exception {
            getLogger().entering(this.getClass().getName(), "doInBackground");
            useWaitCursor(true);
            setInputEnabled(false);
            updateProgressString();
            progressBar.setIndeterminate(true);
            progressBar.setStringPainted(true);
            progressBar.setValue(0);
            E value = backgroundAction();
            getLogger().exiting(this.getClass().getName(), "doInBackground",value);
            return value;
        }
        @Override
        protected void done(){
            System.gc();        // Run the garbage collector
            progressBar.setValue(0);
            progressBar.setIndeterminate(false);
            progressBar.setStringPainted(false);
            setInputEnabled(true);
            useWaitCursor(false);
        }
    }
    /**
     * 
     * @param <E> 
     */
    private abstract class LinksListWorker<E> extends LinkManagerWorker<E>{
        
        protected LinksListPanel panel;
        
        LinksListWorker(LinksListPanel panel){
            this.panel = Objects.requireNonNull(panel);
        }
        
        public LinksListPanel getPanel(){
            return panel;
        }
        
        protected abstract E processLinks(LinksListPanel panel);
        
        @Override
        protected E backgroundAction() throws Exception{
            panel.setEnabled(false);
            showHiddenListsToggle.setEnabled(false);
            return processLinks(panel);
        }
        @Override
        protected void done(){
            panel.setEnabled(true);
            showHiddenListsToggle.setEnabled(true);
            super.done();
        }
    }
    
    private class ManipulateListWorker extends LinksListWorker<Void>{
        
        protected List<String> list;
        /**
         * A copy of the LinksListModel with the changes made.
         */
        protected LinksListModel model = null;
        
        ManipulateListWorker(LinksListPanel panel, List<String> list){
            super(panel);
            this.list = list;
        }
        @Override
        protected Void processLinks(LinksListPanel panel) {
            if (panel.isReadOnly())
                return null;
            getLogger().entering(this.getClass().getName(), "processLinks",panel);
                // Clone the panel's model
            model = new LinksListModel(panel.getModel());
            try{
                model.setContents(list);
            } catch (Exception ex){
                getLogger().log(Level.WARNING, "Error manipulating list", ex);
            }
            getLogger().exiting(this.getClass().getName(), "processLinks");
            return null;
        }
        @Override
        public String getProgressString() {
            return "Updating List";
        }
        @Override
        protected void done(){
            if (model != null)
                panel.setModel(model,true);
            super.done();
        }
    }
    /**
     * This attempts to search for an element that contains a given String 
     * within a given list.
     */
    private class SearchLinks extends LinksListWorker<Integer>{
        /**
         * The direction to search in.
         */
        private final Position.Bias direction;
        /**
         * The String to search for.
         */
        private final String text;
        /**
         * The settings for the search. matchSpaces indicates that white spaces 
         * are checked, matchCase indicates that the search is case sensitive, 
         * and wrapAround indicates that the search will wrap around if it 
         * reaches the end of the list.
         */
        private final boolean matchSpaces,matchCase,wrapAround;
        /**
         * The current index to select.
         */
        private int index = -1;
        /**
         * This constructs a SearchLinks that will search for the given text in 
         * the given list panel in the given direction.
         * @param panel The list panel in which to search through.
         * @param direction The direction to search in.
         * @param text The text to search for.
         */
        public SearchLinks(LinksListPanel panel, Position.Bias direction, 
                String text) {
            super(panel);
            this.direction = direction;
            this.text = text;
            matchSpaces = searchPanel.getMatchSpaces();
            matchCase = searchPanel.getMatchCase();
            wrapAround = searchPanel.getWrapAround();
        }
        /**
         * 
         * @return 
         */
        public int getIndexOfMatch(){
            return index;
        }
        @Override
        protected Integer processLinks(LinksListPanel panel) {
            if (direction == null) // If the direction is null
                return index;
                // Get the next matching string, starting from the currently 
                // selected index
            index = panel.getModel().getNextMatch(text, 
                    panel.getSelectedIndex(), direction, 
                    matchSpaces, matchCase, wrapAround);
            return index;
        }
        @Override
        public String getProgressString() {
            return "Searching";
        }
        @Override
        protected void done(){
            if (index != -1){   // If the search was successful
                panel.setSelectedIndex(index, true);
            }
            super.done();
            if (index == -1){    // If no link was found
                beep();
                JOptionPane.showMessageDialog(LinkManager.this, 
                        "Could not find \""+text+"\"", "Search Results", 
                        JOptionPane.INFORMATION_MESSAGE);
            }
        }
    }
    
    private class AddFromTextWorker extends LinksListWorker<Void>{
        
        protected List<String> list;
        
        protected String text;
        /**
         * A copy of the LinksListModel with the changes made.
         */
        protected LinksListModel model = null;

        AddFromTextWorker(LinksListPanel panel, String text) {
            super(panel);
            list = new ArrayList<>();
            this.text = text;
        }
        @Override
        protected Void processLinks(LinksListPanel panel) {
                // Creates a Scanner that goes through the entered list
            try (Scanner scanner = new Scanner(text)) {
                list = LinkManagerUtilities.readIntoList(scanner,list);
            }
            model = addAllToCopy(panel,list);
            return null;
        }
        @Override
        public String getProgressString() {
            return "Adding Links";
        }
        @Override
        protected void done(){
            if (model != null)
                panel.setModel(model, true);
            super.done();
        }
    }
    /**
     * 
     */
    private class CopyOrMoveLinks extends LinksListWorker<Void>{
        /**
         * 
         */
        private final List<String> list;
        /**
         * 
         */
        private final LinksListPanel source;
        /**
         * A copy of the LinksListModel of the source with the changes made.
         */
        protected LinksListModel srcModel = null;
        /**
         * A copy of the LinksListModel of the target with the changes made.
         */
        protected LinksListModel model = null;
        /**
         * 
         */
        private final boolean move;
        /**
         * 
         * @param source
         * @param target
         * @param list
         * @param move 
         */
        CopyOrMoveLinks(LinksListPanel source, LinksListPanel target, 
                List<String> list, boolean move) {
            super(target);
            this.source = source;
            this.list = Objects.requireNonNull(list);
            this.move = move;
        }
        /**
         * 
         * @return 
         */
        public LinksListPanel getSource(){
            return source;
        }
        /**
         * 
         * @return 
         */
        public LinksListPanel getTarget(){
            return panel;
        }
        /**
         * 
         * @return 
         */
        public boolean isMovingLinks(){
            return move;
        }
        @Override
        protected Void processLinks(LinksListPanel panel) {
            getLogger().entering(this.getClass().getName(), "processLinks", panel);
            getLogger().log(Level.FINER, "Source list: {0}", source);
                // If this is moving links
            if (move)
                getLogger().finer("Moving links between lists");
            else
                getLogger().finer("Copying links between lists");
                // If the given list is read only or full
            if (panel.isReadOnly() || panel.getModel().isFull()){
                if (panel.isReadOnly())
                    getLogger().finer("Links list is read only.");
                else
                    getLogger().log(Level.FINER, "Links list is full (limit: {0},"
                            + " size: {1}).", new Object[]{
                                panel.getModel().getSizeLimit(), 
                                panel.getModel().size()});
                getLogger().exiting(this.getClass().getName(), "processLinks");
                return null;
            }   // If the source list is not null
            if (source != null)
                source.setEnabled(false);
                // This gets a list of items that can and will be added to the 
                // given panel's model
            List<String> added = panel.getModel().getCompatibleList(list);
            model = addAllToCopy(panel,added);
                // If this is moving links between lists, a source list has been 
                // provided, and that source list is not read only
            if (move && source != null && !source.isReadOnly()){
                try{    // If the whole source list has been added to the target
                    if (added.size() == source.getModel().size())
                        source.getModel().clear();
                    else{   // Copy the source model
                        srcModel = new LinksListModel(source.getModel());
                            // Remove all the links that were added.
                        srcModel.removeAll(added);
                    }
                } catch(Exception ex){
                    getLogger().log(Level.WARNING, 
                            "Issue encountered while removing shared links from source",
                            ex);
                }
            }
            getLogger().exiting(this.getClass().getName(), "processLinks");
            return null;
        }
        @Override
        public String getProgressString() {
                // If this is moving links, then say it.
            return ((move)?"Mov":"Copy")+"ing Between Lists";
        }
        @Override
        protected void done(){
            if (model != null)
                panel.setModel(model, true);
                // If a source list was given
            if (source != null){
                if (move && srcModel != null)
                    source.setModel(srcModel, true);
                source.setEnabled(true);
            }
            super.done();
        }
    }
    /**
     * 
     */
    private class RemoveLinksWorker extends LinksListWorker<Void>{
        /**
         * 
         */
        private final LinksListPanel source;
        /**
         * A copy of the LinksListModel with the changes made.
         */
        protected LinksListModel model = null;
        /**
         * 
         * @param source
         * @param target 
         */
        RemoveLinksWorker(LinksListPanel source, LinksListPanel target){
            super(target);
            this.source = Objects.requireNonNull(source);
        }
        /**
         * 
         * @return 
         */
        public LinksListPanel getSource(){
            return source;
        }
        /**
         * 
         * @return 
         */
        public LinksListPanel getTarget(){
            return panel;
        }
        @Override
        protected Void processLinks(LinksListPanel panel) {
                // If the given panel is read only
            if (panel.isReadOnly())
                return null;
                // Disable the source list
            source.setEnabled(false);
                // Clone the panel's model
            model = new LinksListModel(panel.getModel());
                // Remove all the links that the given panel has in common with 
                // the source list
            model.removeAll(source.getModel());
            return null;
        }
        @Override
        public String getProgressString() {
            return "Removing Shared Links";
        }
        @Override
        protected void done(){
            if (model != null)
                panel.setModel(model, true);
                // Enable the source list
            source.setEnabled(true);
            super.done();
        }
    }
    /**
     * 
     */
    private class RemoveLinksWorker2 extends LinksListWorker<Void>{
        /**
         * This stores whether the given list is the source to be removed from 
         * all the other lists, or if the other lists are to be removed from the 
         * given list. {@code true} if the given list is to be removed from the 
         * other lists, {@code false} if the other lists are to be removed from 
         * the given list.
         */
        private final boolean isSource;
        /**
         * Whether this worker affects only hidden lists.
         */
        private final boolean hiddenOnly;
        /**
         * 
         */
        private final LinksListTabsPanel tabsPanel;
        /**
         * 
         */
        private Set<LinksListPanel> panels = null;
        /**
         * A list of the panels that were affected and the altered models for 
         * those lists.
         */
        private Map<LinksListPanel,LinksListModel> models = new LinkedHashMap<>();
        /**
         * 
         * @param panel
         * @param tabsPanel
         * @param isSource
         * @param hiddenOnly 
         */
        RemoveLinksWorker2(LinksListPanel panel, LinksListTabsPanel tabsPanel, 
                boolean isSource, boolean hiddenOnly) {
            super(panel);
            this.isSource = isSource;
            this.tabsPanel = Objects.requireNonNull(tabsPanel);
            this.hiddenOnly = hiddenOnly;
        }
        /**
         * 
         * @param panel
         * @param tabsPanel
         * @param isSource 
         */
        RemoveLinksWorker2(LinksListPanel panel, LinksListTabsPanel tabsPanel, 
                boolean isSource){
            this(panel,tabsPanel,isSource,false);
        }
        /**
         * 
         * @return 
         */
        public boolean isPanelSource(){
            return isSource;
        }
        /**
         * 
         * @return 
         */
        public boolean getHiddenListsOnly(){
            return hiddenOnly;
        }
        /**
         * 
         * @return 
         */
        public LinksListTabsPanel getTabsPanel(){
            return tabsPanel;
        }
        @Override
        protected Void processLinks(LinksListPanel panel) {
                // Disable all the lists
            setTabsPanelListsEnabled(false);
                // Get a set of all the panels to go through
            panels = new LinkedHashSet<>(tabsPanel.getLists());
                // Remove the given panel
            panels.remove(panel);
                // Remove any panels that are null, have the same model as the 
                // given panel, are not hidden (if this only affects hidden 
                // lists), or are read only (if this is removing from these 
            panels.removeIf((LinksListPanel t) -> t == null || // lists)
                        // The model is the same as the given panel
                    t.getModel() == panel.getModel() || 
                        // This worker affects hidden lists and the current list 
                        // is not hidden
                    (hiddenOnly && !t.isHidden()) || 
                        // If this worker will be removing the given list from 
                        // the current list but the current list is read only
                    (isSource && t.isReadOnly()));
                // Get the model for the panel
            LinksListModel model = panel.getModel();
                // If the current panel is to be removed from the other panels
            if (!isSource){
                    // Copy the model
                model = new LinksListModel(model);
                models.put(panel, model);
            }
            progressBar.setMaximum(panels.size());
            progressBar.setIndeterminate(false);
                // Go through the panels 
            for (LinksListPanel current : panels){
                    // If the given panel is to be removed from the current panel
                if (isSource){
                        // Copy the current panel's model
                    LinksListModel temp = new LinksListModel(current.getModel());
                    temp.removeAll(model);
                    models.put(current, temp);
                }
                else
                    model.removeAll(current.getModel());
                incrementProgressValue();
            }
            return null;
        }
        @Override
        public String getProgressString() {
            return "Removing Shared Links";
        }
        @Override
        protected void done(){
            for (Map.Entry<LinksListPanel,LinksListModel> entry : models.entrySet()){
                entry.getKey().setModel(entry.getValue());
            }
                // Re-enable the lists
            setTabsPanelListsEnabled(true);
            super.done();
        }
    }
    /**
     * This is an abstract class that provides the basic framework for working 
     * with a file, such as saving to a file or loading from a file.
     */
    private abstract class FileWorker extends LinkManagerWorker<Void>{
        /**
         * The file to process.
         */
        protected File file;
        /**
         * Whether this was successful at processing the file.
         */
        protected boolean success = false;
        /**
         * This constructs a FileWorker that will process the given file.
         * @param file The file to process.
         */
        FileWorker(File file){
            this.file = file;
        }
        /**
         * This returns whether this was successful at processing the file. 
         * This will be inaccurate up until the file is finished being 
         * processed.
         * @return Whether this has successfully processed the file.
         */
        public boolean isSuccessful(){
            return success;
        }
        /**
         * This returns the file being processed by this FileWorker.
         * @return The file that will be processed.
         */
        public File getFile(){
            return file;
        }
        /**
         * This is used to display a success prompt to the user when the file is 
         * successfully processed.
         * @param file The file that was successfully processed.
         */
        protected void showSuccessPrompt(File file){}
        /**
         * This is used to display a failure prompt to the user when the file 
         * fails to be processed. If the failure prompt is a retry prompt, then 
         * this method should return whether to try processing the file again. 
         * Otherwise, this method should return {@code false}.
         * @param file The file that failed to be processed.
         * @return {@code true} if this should attempt to process the file 
         * again, {@code false} otherwise.
         */
        protected boolean showFailurePrompt(File file){
            return false;
        }
        /**
         * This processes the given file.
         * @param file The file to be processed.
         * @return Whether this was successful at processing the file.
         */
        protected abstract boolean processFile(File file);
        @Override
        protected Void backgroundAction() throws Exception {
            getLogger().entering(this.getClass().getName(), "backgroundAction",file);
                // Whether the user wants this to try processing the file again 
            boolean retry = false;  // if unsuccessful
            do{
                success = processFile(file);    // Try to process the file
                if (success)    // If the file was successfully processed
                    showSuccessPrompt(file);    // Show the success prompt
                else            // If the file failed to be processed
                        // Show the failure prompt and get if the user wants to 
                    retry = showFailurePrompt(file);    // try again
            }   // While the file failed to be processed and the user wants to 
            while(!success && retry);   // try again
            getLogger().exiting(this.getClass().getName(), "backgroundAction");
            return null;
        }
    }
    
    private class FileMover extends FileWorker{
        
        private final File target;
        
        private IOException exc = null;

        FileMover(File source, File target) {
            super(source);
            this.target = Objects.requireNonNull(target);
        }
        
        public File getSource(){
            return getFile();
        }
        
        public File getTarget(){
            return target;
        }
        
        protected IOException getIOExceptionThrown(){
            return exc;
        }
        @Override
        protected void showSuccessPrompt(File file){
            JOptionPane.showMessageDialog(LinkManager.this, 
                    "The file was successfully renamed\n"
                            + "Source: \""+file+"\"\n"
                                    + "Target: \""+target+"\"", 
                    "File Moved Successfully", 
                    JOptionPane.INFORMATION_MESSAGE);
        }
        @Override
        protected boolean showFailurePrompt(File file){
            String msg;
            boolean canRetry = false;
            if (exc instanceof FileNotFoundException)
                msg = exc.getMessage();
            else if (exc instanceof FileAlreadyExistsException)
                msg = ((FileAlreadyExistsException)exc).getReason();
            else if (!file.exists())
                msg = "The source file \""+file+"\" does not exist";
            else{
                msg = "The file failed to move.";
                canRetry = true;
            }
            if (canRetry){
                    // Ask the user if they would like to try move the file again
                return JOptionPane.showConfirmDialog(LinkManager.this, 
                        msg+"\nWould you like to try again?",
                        "ERROR - Could Not Move File",JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION;
            }
            else{
                JOptionPane.showMessageDialog(LinkManager.this, msg,
                        "ERROR - Could Not Move File", 
                        JOptionPane.ERROR_MESSAGE);
            }
            return false;
        }
        @Override
        protected boolean processFile(File file) {
            try{
                return FilesExtended.rename(file, target);
            } catch(IOException ex){
                getLogger().log(Level.WARNING, "Failed to rename file", ex);
                exc = ex;
                return false;
            }
        }
        @Override
        public String getProgressString() {
            return "Moving File";
        }
    }
    /**
     * This is an abstract class that provides the framework for loading from a 
     * file.
     */
    private abstract class FileLoader extends FileWorker{
        /**
         * Whether this is currently loading a file.
         */
        protected volatile boolean loading = false;
        /**
         * Whether file not found errors should be shown.
         */
        protected boolean showFileNotFound;
        /**
         * This constructs a FileLoader that will load the data from the given 
         * file.
         * @param file The file to load the data from.
         * @param showFileNotFound Whether a file not found error should result 
         * in a popup being shown to the user.
         */
        FileLoader(File file, boolean showFileNotFound){
            super(file);
            this.showFileNotFound = showFileNotFound;
        }
        /**
         * This constructs a FileLoader that will load the data from the given 
         * file.
         * @param file The file to load the data from.
         */
        FileLoader(File file){
            this(file,true);
        }
        @Override
        public String getProgressString(){
            return "Loading";
        }
        /**
         * This returns whether this is currently loading from a file.
         * @return Whether a file is currently being loaded.
         */
        public boolean isLoading(){
            return loading;
        }
        /**
         * This returns whether this was successful at loading from the file. 
         * This will be inaccurate up until the file is loaded.
         * @return Whether this has successfully loaded the file.
         */
        @Override
        public boolean isSuccessful(){
            return super.isSuccessful();
        }
        /**
         * This returns whether this shows a failure prompt when the file is not 
         * found.
         * @return Whether the file not found failure prompt is shown.
         */
        public boolean getShowsFileNotFoundPrompts(){
            return showFileNotFound;
        }
        /**
         * This returns the file being loaded by this FileLoader.
         * @return The file that will be loaded.
         */
        @Override
        public File getFile(){
            return super.getFile();
        }
        /**
         * This loads the data from the given file. This is called by {@link 
         * #processFile(File) processFile} in order to load the file.
         * @param file The file to load the data from.
         * @return Whether the file was successfully loaded.
         * @see #processFile(File) 
         */
        protected abstract boolean loadFile(File file);
        /**
         * {@inheritDoc } This delegates to {@link #loadFile(File) loadFile}.
         * @see #loadFile(File) 
         */
        @Override
        protected boolean processFile(File file){
            loading = true;
            return loadFile(file);
        }
        /**
         * This is used to display a success prompt to the user when the file is 
         * successfully loaded.
         * @param file The file that was successfully loaded.
         */
        @Override
        protected void showSuccessPrompt(File file){}
        /**
         * This is used to display a failure prompt to the user when the file 
         * fails to be loaded. 
         * @param file The file that failed to load.
         * @return {@inheritDoc}
         */
        @Override
        protected boolean showFailurePrompt(File file){
            if (!file.exists()){    // If the file doesn't exist
                    // If this should show file not found prompts
                if (showFileNotFound){
                    JOptionPane.showMessageDialog(LinkManager.this, 
                            getFileNotFoundMessage(file), getFailureTitle(file), 
                            JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
            else{   // Ask the user if they would like to try loading the file
                return JOptionPane.showConfirmDialog(LinkManager.this, // again
                        getFailureMessage(file)+"\nWould you like to try again?",
                        getFailureTitle(file),JOptionPane.YES_NO_OPTION,
                        JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION;
            }
        }
        /**
         * This returns the title for the dialog to display if the file fails to 
         * be saved.
         * @param file The file that failed to load.
         * @return The title for the dialog to display if the file fails to
         * save.
         */
        protected String getFailureTitle(File file){
            return "ERROR - File Failed To Load";
        }
        /**
         * This returns the message to display if the file fails to load.
         * @param file The file that failed to load.
         * @return The message to display if the file fails to load.
         */
        protected String getFailureMessage(File file){
            return "The file failed to load.";
        }
        /**
         * This returns the message to display if the file does not exist.
         * @param file The file that did not exist.
         * @return The message to display if the file does not exist.
         */
        protected String getFileNotFoundMessage(File file){
            return "The file does not exist.";
        }
        @Override
        protected void done(){
            loading = false;
            super.done();
        }
    }
    /**
     * This is an abstract class that provides the framework for saving to a 
     * file.
     */
    private abstract class FileSaver extends FileWorker{
        /**
         * Whether this is currently saving a file.
         */
        protected volatile boolean saving = false;
        /**
         * This stores whether this should exit the program after saving.
         */
        protected volatile boolean exitAfterSaving;
        /**
         * This stores the backup of the existing version of the file being 
         * saved if a backup is to be created.
         */
        protected File backupFile = null;
        /**
         * This stores whether the file failed to save due to an error occurring 
         * while creating the backup file.
         */
        private boolean backupFailed = false;
        /**
         * This constructs a FileSaver that will save data to the given file 
         * and, if {@code exit} is {@code true}, will exit the program after 
         * saving the file.
         * @param file The file to save the data to.
         * @param exit Whether the program will exit after saving the file.
         */
        FileSaver(File file, boolean exit){
            super(file);
            exitAfterSaving = exit;
        }
        /**
         * This constructs a FileSaver that will save data to the given file.
         * @param file The file to save the data to.
         */
        FileSaver(File file){
            this(file,false);
        }
        @Override
        public String getProgressString(){
            return "Saving";
        }
        /**
         * This returns whether this is currently saving to a file.
         * @return Whether a file is currently being saved to.
         */
        public boolean isSaving(){
            return saving;
        }
        /**
         * This returns whether this was successful at saving to the file. This 
         * will be inaccurate up until the file is saved.
         * @return Whether this has successfully saved the file.
         */
        @Override
        public boolean isSuccessful(){
            return super.isSuccessful();
        }
        /**
         * This returns whether the program will exit after this finishes saving 
         * the file.
         * @return Whether the program will exit once the file is saved.
         */
        public boolean getExitAfterSaving(){
            return exitAfterSaving;
        }
        /**
         * This returns the file being saved to by this FileSaver.
         * @return The file that will be saved.
         */
        @Override
        public File getFile(){
            return super.getFile();
        }
        /**
         * This returns whether this should consider the file returned by {@link 
         * #getFile() getFile} as a directory in which to save files into.
         * @return Whether the file given to this FileSaver is actually a 
         * directory.
         */
        protected boolean isFileTheDirectory(){
            return false;
        }
        /**
         * This returns the file containing a backup of the file before being 
         * saved if it previously existed and a backup was created.
         * @return The backup of the file, or null if no backup was created.
         */
        public File getBackupFile(){
            return backupFile;
        }
        /**
         * This returns whether this should first try to create a backup of the 
         * file being saved before saving the file.
         * @return Whether this should try to backup the file before saving.
         */
        protected boolean willCreateBackup(){
            return false;
        }
        /**
         * This will delete the backup file if the program successfully saved 
         * the file.
         */
        protected void deleteBackupIfSuccessful(){
                // If the file was successfully saved and there is a backup file
            if (success && backupFile != null){
                backupFile.delete();
            }
        }
        /**
         * This returns whether this tried and failed to create a backup of the 
         * file before saving it.
         * @return Whether the backup failed to be created.
         */
        protected boolean didBackupFail(){
            return backupFailed;
        }
        /**
         * This attempts to save to the given file. This is called by {@link 
         * #processFile(File) processFile} in order to save the file.
         * @param file The file to save.
         * @return Whether the file was successfully saved to.
         * @see #processFile(File) 
         */
        protected abstract boolean saveFile(File file);
        /**
         * {@inheritDoc } This delegates the saving of the file to {@link 
         * #saveFile(File) saveFile}.
         * @see #saveFile(File) 
         */
        @Override
        protected boolean processFile(File file){
            saving = true;
                // Try to create the directories and if that fails, then give up 
                // on saving the file. (If the file is the directory, include it 
                // as a directory to be created. Otherwise, create the parent 
                // file of the file to be saved)
            if (!FilesExtended.createDirectories(LinkManager.this, 
                    (isFileTheDirectory())?file:file.getParentFile()))
                return false;
                // If this is to create a backup of the file
            if (willCreateBackup()){
                try {   // Try to create a backup of the file
                    backupFile = LinkManagerUtilities.createBackupCopy(file);
                    backupFailed = false;
                } catch (IOException ex) {
                    getLogger().log(Level.WARNING,"Failed to create backup file",
                            ex);
                    backupFailed = true;    // The backup failed
                    return false;
                }
            }
            return saveFile(file);
        }
        /**
         * This returns the title for the dialog to display if the file is 
         * successfully saved.
         * @param file The file that was successfully saved.
         * @return The title for the dialog to display if the file is 
         * successfully saved.
         */
        protected String getSuccessTitle(File file){
            return "File Saved Successfully";
        }
        /**
         * This returns the message to display if the file is successfully 
         * saved.
         * @param file The file that was successfully saved.
         * @return The message to display if the file is successfully saved.
         */
        protected String getSuccessMessage(File file){
            return "The file was successfully saved.";
        }
        /**
         * This returns the title for the dialog to display if the file fails to 
         * be saved.
         * @param file The file that failed to be saved.
         * @return The title for the dialog to display if the file fails to
         * save.
         */
        protected String getFailureTitle(File file){
            return "ERROR - File Failed To Save";
        }
        /**
         * This returns the message to display if the backup of the file failed 
         * to be created.
         * @param file The file that failed to be backed up.
         * @return The message to display if a backup of the file failed to be 
         * created.
         */
        protected String getBackupFailedMessage(File file){
            return "The backup file failed to be created.";
        }
        /**
         * This returns the message to display if the file fails to be saved.
         * @param file The file that failed to be saved.
         * @return The message to display if the file fails to save.
         */
        protected String getFailureMessage(File file){
            return "The file failed to save.";
        }
        /**
         * This is used to display a success prompt to the user when the file is 
         * successfully saved. The success prompt will display the message 
         * returned by {@link #getSuccessMessage()}. If the program is to exit 
         * after saving the file, then this will show nothing.
         * @param file The file that was successfully saved.
         */
        @Override
        protected void showSuccessPrompt(File file){
                // If the program is not to exit after saving the file
            if (!exitAfterSaving)   
                JOptionPane.showMessageDialog(LinkManager.this, 
                        getSuccessMessage(file), getSuccessTitle(file), 
                        JOptionPane.INFORMATION_MESSAGE);
        }
        /**
         * This is used to display a failure and retry prompt to the user when 
         * the file fails to be saved.
         * @param file The file that failed to be saved.
         * @return {@inheritDoc }
         */
        @Override
        protected boolean showFailurePrompt(File file){
                // Get the message to be displayed. If the file failed to be 
                // backed up, show the backup failed message. Otherwise show the 
                // normal failure message.
            String message = (backupFailed) ? getBackupFailedMessage(file) : 
                    getFailureMessage(file);
                // Show a dialog prompt asking the user if they would like to 
                // try and save the file again and get their input. 
            int option = JOptionPane.showConfirmDialog(LinkManager.this, 
                    message+"\nWould you like to try again?",
                    getFailureTitle(file),
                        // If the program is to exit after saving the file, show 
                        // a third "cancel" option to allow the user to cancel 
                        // exiting the program
                    (exitAfterSaving)?JOptionPane.YES_NO_CANCEL_OPTION:
                            JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE);
                // If the program was going to exit after saving the file
            if (exitAfterSaving){   
                    // If the option selected was the cancel option or the user 
                    // closed the dialog without selecting anything, then don't 
                    // exit the program
                exitAfterSaving = option != JOptionPane.CLOSED_OPTION && 
                        option != JOptionPane.CANCEL_OPTION;
            }   // Return whether the user selected yes
            return option == JOptionPane.YES_OPTION;    
        }
        /**
         * This is used to exit the program after this finishes saving the file.
         */
        protected void exitProgram(){
            System.exit(0);         // Exit the program
        }
        @Override
        protected void done(){
            if (exitAfterSaving)    // If the program is to exit after saving
                exitProgram();      // Exit the program
            saving = false;
            super.done();
        }
    }
    /**
     * This is a FileSaver dedicated to creating a backup of a given file.
     */
    private class BackupFileSaver extends FileSaver{
        /**
         * The progress string to display on the progress bar.
         */
        private String progressStr;
        /**
         * This constructs a BackupFileSaver that will create a backup of the 
         * given file.
         * @param file The file to create a backup of.
         * @param progressStr The string to display on the progress bar, or null 
         * to display the default string.
         */
        BackupFileSaver(File file, String progressStr) {
            super(file);
            this.progressStr = progressStr;
        }
        /**
         * This constructs a BackupFileSaver that will create a backup of the 
         * given file.
         * @param file The file to create a backup of.
         */
        BackupFileSaver(File file){
            this(file,null);
        }
        @Override
        public String getProgressString(){
                // If the progress string is not null
            if (progressStr != null)
                return progressStr;
            return "Creating Backup";
        }
        /**
         * This sets the progress string to the given string.
         * @param text The string to display on the progress bar, or null 
         * to display the default string.
         */
        public void setProgressString(String text){
            progressStr = text;
            if (super.isSaving())   // If this is currently saving a file
                updateProgressString();
        }
        @Override
        protected boolean willCreateBackup(){
            return true;
        }
        @Override
        protected String getSuccessMessage(File file){
            return "The backup file was successfully created.";
        }
        @Override
        protected String getBackupFailedMessage(File file){
            return getFailureMessage(file);
        }
        @Override
        protected String getFailureMessage(File file){
                // If the file does not exist
            if (!file.exists())
                return "Cannot create backup of a non-existent file.";
            else
                return "The backup file failed to be created.";
        }
        @Override
        protected boolean showFailurePrompt(File file){
            if (!file.exists()){    // If the file doesn't exist
                JOptionPane.showMessageDialog(LinkManager.this, 
                        getFailureMessage(file),getFailureTitle(file), 
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
            return super.showFailurePrompt(file);
        }
        @Override
        protected boolean saveFile(File file) {
                // Since we're only interested in creating a backup of the given 
                // file, a process already covered by FileSaver, just return 
                // whether the backup was successful and whether the backup file 
                // is not null
            return !didBackupFail() && backupFile != null;    
        }
    }
    /**
     * This loads the data from a file into a List of Strings and, if one is 
     * provided, will add the list to the model of a SetOfLinksPanel.
     */
    private class ListLoader extends FileLoader{
        /**
         * The List of Strings to store the data from the file in.
         */
        protected List<String> list;
        /**
         * The panel to add the loaded list's contents to (optional).
         */
        protected LinksListPanel panel;
        /**
         * A copy of the LinksListModel with the changes made.
         */
        protected LinksListModel model = null;
        
        ListLoader(File file, List<String> list, LinksListPanel panel){
            super(file);
            this.list = Objects.requireNonNull(list);
            this.panel = panel;
        }
        
        ListLoader(File file, List<String> list){
            this(file,list,null);
        }
        
        ListLoader(File file, LinksListPanel panel){
            this(file,new ArrayList<>(),panel);
        }
        
        ListLoader(File file) {
            this(file,new ArrayList<>());
        }
        /**
         * This returns the List used to retrieve the data from the file.
         * @return The List used to retrieve data from the file.
         */
        public List<String> getData(){
            return list;
        }
        /**
         * This returns the panel with the model that the list of Strings will 
         * be added to.
         * @return The panel who's model that is being added to, or null.
         */
        public LinksListPanel getPanel(){
            return panel;
        }
        @Override
        public String getProgressString(){
            return "Loading List";
        }
        @Override
        protected boolean loadFile(File file) {
                // Try to create a scanner to read from a FileReader used to 
                // read from the file
            try(FileReader reader = new FileReader(file);
                    Scanner scanner = new Scanner(reader)){
                list = LinkManagerUtilities.readIntoList(scanner,list);
                    // If the file was a shortcut file and we're adding to a panel
                if (panel != null && SHORTCUT_FILE_FILTER.accept(file)){
                        // Get the URL from the file
                    String temp = LinkManagerUtilities.getShortcutURL(list);
                    list.clear();
                    if (temp != null)   // If a URL was found in the file
                        list.add(temp);
                }   // If we're adding the results to a panel's model
                if (panel != null)
                    model = addAllToCopy(panel,list);
                return true;
            } catch (IOException ex) {
                getLogger().log(Level.WARNING,"Failed to load list into panel", 
                        ex);
                return false;
            }
        }
        @Override
        protected void done(){
            if (panel != null && model != null)
                panel.setModel(model, true);
            super.done();
        }
    }
    /**
     * This is used to save a List of Strings to a file.
     */
    private class ListSaver extends FileSaver{
        /**
         * The List of Strings to save to the file.
         */
        protected List<String> list;
        
        ListSaver(File file, List<String> list, boolean exit) {
            super(file, exit);
            this.list = Objects.requireNonNull(list);
        }
        
        ListSaver(File file, List<String> list){
            this(file,list,false);
        }
        /**
         * This returns the List containing the data that will be saved to the 
         * file.
         * @return The List of the data to save to the file.
         */
        public List<String> getData(){
            return list;
        }
        @Override
        public String getProgressString(){
            return "Saving List";
        }
        @Override
        protected boolean saveFile(File file) {
            progressBar.setMaximum(list.size());
            return writeToFile(file,list);
        }
    }
    
    private class ConfigLoader extends FileLoader{
        
        private ConfigProperties prop = new ConfigProperties();

        ConfigLoader(File file) {
            super(file,fullyLoaded);
        }
        @Override
        public String getProgressString(){
            return "Loading Configuration";
        }
        @Override
        protected boolean loadFile(File file) {
                // Disable the hidden list toggle
            showHiddenListsToggle.setEnabled(false);
            try {   // Load the properties from the file and get if we are 
                    // successful
                if (LinkManagerUtilities.loadProperties(file, prop)){
                        // If the properties has a value for the database 
                        // location dialog's size
                    if (prop.containsKey(config.getComponentName(setLocationDialog)
                            +LinkManagerConfig.COMPONENT_SIZE_KEY_SUFFIX)){
                            // If the properties doesn't have a value for the 
                            // database location dialog's bounds
                        if (!prop.containsKey(config.getComponentName(setLocationDialog)
                                +LinkManagerConfig.COMPONENT_BOUNDS_KEY_SUFFIX)){
                                // Get the database location dialog's bounds
                            Rectangle rect = config.getComponentBounds(setLocationDialog);
                                // Set the size from the properties
                            rect.setSize(prop.getDimensionProperty(
                                    config.getComponentName(setLocationDialog)+
                                            LinkManagerConfig.COMPONENT_SIZE_KEY_SUFFIX));
                                // Set it's bounds
                            config.setComponentBounds(setLocationDialog,rect);
                        }
                            // Remove the value
                        prop.remove(config.getComponentName(setLocationDialog)+
                                LinkManagerConfig.COMPONENT_SIZE_KEY_SUFFIX);
                    }
                        // Import the properties into the configuration
                    config.importProperties(prop);
                    return true;
                }
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, 
                        "Failed to load configuration file", ex);
            }
            return false;
        }
        @Override
        protected void done(){
                // Configure the program
            configureProgram();
                // Wrap up the loading process
            super.done();
                // Re-enable the hidden list toggle
            showHiddenListsToggle.setEnabled(true);
        }
    }
    
    private abstract class AbstractConfigSaver extends FileSaver{
        
        AbstractConfigSaver(File file, boolean exit) {
            super(file, exit);
        }
        
        AbstractConfigSaver(File file){
            super(file);
        }
        
        protected abstract boolean savePropertiesFile(File file) throws IOException;
        @Override
        protected boolean saveFile(File file) {
                // Set the program to be indeterminate
            progressBar.setIndeterminate(true);
                // Update the program configuration
            updateProgramConfig();
            try {   // Try to save the properties to file
                return savePropertiesFile(file);
            } catch (IOException ex) {
                getLogger().log(Level.WARNING,"Failed to save configuration file", ex);
                return false;
            }
        }
        @Override
        public String getProgressString(){
            return "Saving Configuration";
        }
    }
    
    private class ConfigSaver extends AbstractConfigSaver{
        
        ConfigSaver(File file){
            super(file);
        }
        @Override
        protected boolean savePropertiesFile(File file) throws IOException {
                // Disable the hidden lists toggle
            showHiddenListsToggle.setEnabled(false);
                // Get the settings for the program, as a Properties object
            Properties prop = config.exportProperties();
                // If the settings somehow failed to be exported
            if (prop == null)
                return false;
            return LinkManagerUtilities.saveProperties(file,prop,GENERAL_CONFIG_HEADER);
        }
        @Override
        protected void done(){
            super.done();
                // Re-enable the hidden lists toggle if the program isn't 
                // closing after this is done
            showHiddenListsToggle.setEnabled(!exitAfterSaving);
        }
    }
    /**
     * 
     */
    private class ProgramConfigSaver extends AbstractConfigSaver{

        public ProgramConfigSaver(boolean exit) {
            super(getConfigFile(), exit);
        }
        
        public ProgramConfigSaver(){
            this(false);
        }
        @Override
        protected boolean savePropertiesFile(File file) throws IOException {
            return saveConfigFile();
        }
    }
    /**
     * This is an abstract class that provides the framework for loading from a 
     * database file.
     */
    private abstract class AbstractDatabaseLoader extends FileLoader{
        /**
         * The SQLException thrown while loading from the database if an error 
         * occurred.
         */
        protected SQLException sqlExc = null;
        /**
         * This constructs a AbstractDatabaseLoader that will load the data from 
         * the database stored in the given file.
         * @param file The database file to load the data from.
         * @param showFileNotFound Whether a file not found error should result 
         * in a popup being shown to the user.
         */
        AbstractDatabaseLoader(File file, boolean showFileNotFound) {
            super(file, showFileNotFound);
        }
        /**
         * This constructs a AbstractDatabaseLoader that will load the data from 
         * the database stored in the given file.
         * @param file The database file to load the data from.
         */
        AbstractDatabaseLoader(File file) {
            super(file);
        }
        /**
         * This constructs a AbstractDatabaseLoader that will load the data from 
         * the program's {@link #getDatabaseFile() database file}.
         * @param showFileNotFound Whether a file not found error should result 
         * in a popup being shown to the user.
         */
        AbstractDatabaseLoader(boolean showFileNotFound){
            this(getDatabaseFile(), showFileNotFound);
        }
        /**
         * This constructs a AbstractDatabaseLoader that will load the data from 
         * the program's {@link #getDatabaseFile() database file}.
         */
        AbstractDatabaseLoader(){
            this(true);
        }
        /**
         * This attempts to load from the database using the given database 
         * connection and provided reusable statement.
         * @param conn The connection to the database.
         * @param stmt An SQL statement that can be used to interact with the 
         * database.
         * @return Whether this successfully loaded from the database.
         * @throws SQLException If a database error occurs.
         * @see #loadFile(File) 
         */
        protected abstract boolean loadDatabase(LinkDatabaseConnection conn, 
                Statement stmt) throws SQLException;
        @Override
        protected boolean loadFile(File file){
            sqlExc = null;
            if (!file.exists())     // If the file doesn't exist
                return false;
            boolean value = false;
            getLogger().entering(this.getClass().getName(), "loadFile", file);
                // Connect to the database and create an SQL statement
            try(LinkDatabaseConnection conn = connect(file);
                    Statement stmt = conn.createStatement()){
                value = loadDatabase(conn,stmt); // Load from the database
            } catch(SQLException ex){
                getLogger().log(Level.WARNING, "Failed to load database", ex);
                sqlExc = ex;
            } catch (UncheckedSQLException ex){
                getLogger().log(Level.WARNING,"Failed to load database", ex);
                sqlExc = ex.getCause();
            }
            getLogger().exiting(this.getClass().getName(), "loadFile", value);
            return value;
        }
        /**
         * This returns any SQLExceptions that were thrown while this was 
         * loading data from the database.
         * @return The SQLException thrown while loading, or null if no 
         * SQLException was thrown.
         */
        protected SQLException getSQLExceptionThrown(){
            return sqlExc;
        }
        @Override
        protected String getFailureMessage(File file){
                // The message to return
            String msg = "The database failed to load.";
            if (sqlExc != null){    // If an SQLException was thrown
                    // Custom error messages for certain error codes
                switch(sqlExc.getErrorCode()){
                        // If the database failed to save because it was busy
                    case (Codes.SQLITE_BUSY):
                        msg = "Please wait, the database is currently busy.";
                        break;
                        // If the database could not be opened
                    case(Codes.SQLITE_CANTOPEN):
                        msg = "The database could not be opened.";
                        break;
                        // If the database is corrupted
                    case(Codes.SQLITE_CORRUPT):
                        msg = "The database failed to load due to being corrupted.";
                }   // If the program is either in debug mode or if details are to be shown
                if (isInDebug() || showDBErrorDetailsToggle.isSelected())    
                    msg += "\nError: " + sqlExc + 
                            "\nError Code: " + sqlExc.getErrorCode();
            }
            return msg;
        }
        @Override
        protected String getFileNotFoundMessage(File file){
            return "The database file does not exist.";
        }
        @Override
        protected void done(){
            super.done();
                // Update the program configuration
            updateProgramConfig();
        }
    }
    /**
     * This is an abstract class that provides the framework for saving to a 
     * database file.
     */
    private abstract class AbstractDatabaseSaver extends FileSaver{
        /**
         * The SQLException thrown while saving to the database if an error 
         * occurred.
         */
        protected SQLException sqlExc = null;
        /**
         * Whether this is currently verifying the changes to the database.
         */
        private boolean verifying = false;
        /**
         * This constructs a AbstractDatabaseSaver that will save the data to 
         * the database stored in the given file.
         * @param file The database file to save the data to.
         */
        AbstractDatabaseSaver(File file) {
            super(file);
        }
        /**
         * This constructs a AbstractDatabaseSaver that will save the data to 
         * the program's {@link #getDatabaseFile() database file}.
         */
        AbstractDatabaseSaver(){
            this(getDatabaseFile());
        }
        /**
         * This constructs a AbstractDatabaseSaver that will save the data to 
         * the database stored in the given file and, if {@code exit} is {@code 
         * true}, will exit the program afterwards.
         * @param file The database file to save the data to.
         * @param exit Whether the program will exit after saving the file.
         */
        AbstractDatabaseSaver(File file, boolean exit) {
            super(file, exit);
        }
        /**
         * This constructs a AbstractDatabaseSaver that will save the data to 
         * the program's {@link #getDatabaseFile() database file} and, if {@code 
         * exit} is {@code true}, will exit the program afterwards.
         * @param exit Whether the program will exit after saving the file.
         */
        AbstractDatabaseSaver(boolean exit){
            this(getDatabaseFile(), exit);
        }
        /**
         * This attempts to save to the database using the given database 
         * connection and provided reusable statement.
         * @param conn The connection to the database.
         * @param stmt An SQL statement that can be used to interact with the 
         * database.
         * @return Whether this successfully saved to the database.
         * @throws SQLException If a database error occurs.
         * @see #saveFile(File) 
         */
        protected abstract boolean saveDatabase(LinkDatabaseConnection conn, 
                Statement stmt) throws SQLException;
        /**
         * This returns whether the connection will be in auto-commit mode or 
         * not. For more information, refer to {@link Connection#setAutoCommit}.
         * @return Whether the database will be in auto-commit mode.
         */
        protected boolean getAutoCommit(){
            return false;
        }
        /**
         * This sets whether the program will exit after this finishes saving 
         * the file.
         * @param value Whether the program will exit once the file is saved.
         */
        public void setExitAfterSaving(boolean value){
            exitAfterSaving = value;
        }
        /**
         * 
         * @param file
         * @param conn
         * @param stmt
         * @return
         * @throws SQLException 
         */
        protected boolean prepareDatabase(File file, LinkDatabaseConnection conn, 
                Statement stmt) throws SQLException{
            conn.createTables(stmt);
            return conn.updateDatabaseDefinitions(stmt,progressObserver);
        }
        @Override
        protected boolean willCreateBackup(){
            return true;
        }
        /**
         * 
         * @return 
         */
        protected boolean isVerifyingDatabase(){
            return verifying;
        }
        /**
         * 
         * @param value 
         */
        protected void setVerifyingDatabase(boolean value){
            verifying = value;
            updateProgressString();
        }
        /**
         * 
         * @return 
         */
        public abstract String getNormalProgressString();
        /**
         * 
         * @return 
         */
        public String getVerifyingProgressString(){
            return "Verifying Changes";
        }
        @Override
        public String getProgressString(){
                // If this is verifying the changes to the database
            if (verifying)
                return getVerifyingProgressString();
            return getNormalProgressString();
        }
        @Override
        protected boolean saveFile(File file){
            getLogger().entering(this.getClass().getName(), "saveFile", file);
            sqlExc = null;
            boolean value = false;
                // Connect to the database and create an SQL statement
            try(LinkDatabaseConnection conn = connect(file);
                    Statement stmt = conn.createStatement()){
                conn.setAutoCommit(getAutoCommit());
                    // Try to prepare the database
                value = prepareDatabase(file,conn,stmt);
                    // If the connection is not in auto-commit mode
                if (!conn.getAutoCommit())
                    conn.commit();       // Commit the changes to the database
                if (!value){    // If the database failed to be prepared
                    getLogger().log(Level.WARNING,"Failed to prepare database");
                    getLogger().exiting(this.getClass().getName(), "saveFile",false);
                    return false;
                }
                    // Save to the database and get if we are successful
                value = saveDatabase(conn,stmt);
                   // Ensure that the database last modified time is updated
                conn.setDatabaseLastModified();
                    // If the connection is not in auto-commit mode
                if (!conn.getAutoCommit()){
                    progressBar.setIndeterminate(true);
                    conn.commit();       // Commit the changes to the database
                }
                return value;
            } catch(SQLException ex){
                getLogger().log(Level.WARNING, "Failed to save database", ex);
                sqlExc = ex;
            } catch (UncheckedSQLException ex){
                getLogger().log(Level.WARNING, "Failed to save database", ex);
                sqlExc = ex.getCause();
            } catch(Exception ex){
                getLogger().log(Level.WARNING, "Failed to save database", ex);
            }
            getLogger().exiting(this.getClass().getName(), "saveFile", value);
            return value;
        }
        @Override
        protected String getSuccessMessage(File file){
            return "The database was successfully saved.";
        }
        /**
         * This returns any SQLExceptions that were thrown while this was 
         * saving data to the database.
         * @return The SQLException thrown while saving, or null if no 
         * SQLException was thrown.
         */
        protected SQLException getSQLExceptionThrown(){
            return sqlExc;
        }
        @Override
        protected String getBackupFailedMessage(File file){
            return "The database backup file failed to be created.";
        }
        @Override
        protected String getFailureMessage(File file){
                // The message to return
            String msg = "The database failed to save.";
            if (sqlExc != null){    // If an SQLException was thrown
                    // Custom error messages for certain error codes
                switch(sqlExc.getErrorCode()){
                        // If the database failed to save because it was busy
                    case (Codes.SQLITE_BUSY):
                        msg = "Please wait, the database is currently busy.";
                        break;
                        // If the database is read only
                    case(Codes.SQLITE_READONLY):
                        msg = "The database could not be saved due to being read only.";
                        break;
                        // If the database is full
                    case(Codes.SQLITE_FULL):
                        msg = "The database could not be saved due to being full.";
                        break;
                        // If the database could not be opened
                    case(Codes.SQLITE_CANTOPEN):
                        msg = "The database could not be opened for saving.";
                        break;
                        // If the database is corrupted
                    case(Codes.SQLITE_CORRUPT):
                        msg = "The database failed to save due to being corrupted.";
                }   // If the program is either in debug mode or if details are to be shown
                if (isInDebug() || showDBErrorDetailsToggle.isSelected())    
                    msg += "\nError: " + sqlExc + 
                            "\nError Code: " + sqlExc.getErrorCode();
            }
            return msg;
        }
        /**
         * 
         */
        protected void uploadDatabase(){
            saver = new FileUploader(file,config.getDropboxDatabaseFileName(),0,false,exitAfterSaving);
            saver.execute();
        }
        @Override
        protected void exitProgram(){
            if (syncDBToggle.isSelected() && isLoggedInToDropbox()){
                uploadDatabase();
            } else {
                saver = new ProgramConfigSaver(true);
                saver.execute();
            }
        }
        @Override
        protected void done(){
            if (success){   // If this was successful
                allListsTabsPanel.clearEdited();
                shownListsTabsPanel.clearEdited();
                    // Stop the autosave
                autosaveMenu.stopAutosave();
            }   // Update the program configuration
            updateProgramConfig();
            super.done();
                // If we are not exiting the program after saving the database
            if (!exitAfterSaving){   
                if (success && syncDBToggle.isSelected() && isLoggedInToDropbox()){
                    uploadDatabase();
                }
            }
        }
    }
    /**
     * This loads the lists of links from the database.
     */
    private class DatabaseLoader extends AbstractDatabaseLoader{
        /**
         * This is a map that maps the tabs panels to the list of models that 
         * will be displayed by those tabs panels when we finish loading.
         */
        private HashMap<LinksListTabsPanel, List<LinksListModel>> tabsModels = 
                new HashMap<>();
        /**
         * This stores the flags for this DatabaseLoader, which indicate things 
         * such as whether this will be loading all the lists from the database 
         * or only the lists that are outdated.
         */
        private int loadFlags;
        /**
         * This stores whether this failed to load the database due to the 
         * database being an incompatible version that cannot be updated 
         * automatically by the program.
         */
        private boolean isDBOutdated = false;
        /**
         * This stores the version of the database being loaded.
         */
        private String dbVersion = "N/A";
        
        DatabaseLoader(File file, int loadFlags){
            super(file,fullyLoaded);
            this.loadFlags = loadFlags;
            if (!fullyLoaded)
                this.loadFlags |= DATABASE_LOADER_LOAD_ALL_FLAG;
        }
        
        DatabaseLoader(File file, boolean loadAll){
            this(file,(loadAll) ? DATABASE_LOADER_LOAD_ALL_FLAG : 0);
        }
        
        DatabaseLoader(File file){
            this(DATABASE_LOADER_LOAD_ALL_FLAG);
        }
        
        DatabaseLoader(int loadFlags){
            this(getDatabaseFile(),loadFlags);
        }
        
        DatabaseLoader(boolean loadAll){
            this(getDatabaseFile(),loadAll);
        }
        
        DatabaseLoader(){
            this(DATABASE_LOADER_LOAD_ALL_FLAG);
        }
        /**
         * This returns if all the lists will be loaded from the database of 
         * if only or only the lists that are outdated.
         * @return Whether all the lists will be loaded.
         */
        private boolean getLoadsAll(){
            return LinkManagerUtilities.getFlag(loadFlags,DATABASE_LOADER_LOAD_ALL_FLAG);
        }
        @Override
        public String getProgressString(){
            return "Loading Lists";
        }
        @Override
        protected boolean loadDatabase(LinkDatabaseConnection conn, 
                Statement stmt) throws SQLException {
                // Disable all the lists
            setTabsPanelListsEnabled(false);
                // Create any tables in the database that need to be created
            conn.createTables(stmt);
                // Get the version of the database
            dbVersion = conn.getDatabaseVersionStr();
                // Get whether the database is outdated
            isDBOutdated = conn.isDatabaseOutdated();
                // If the database is incompatible with this version of the 
            if (!conn.isDatabaseCompatible()){      // program
                getLogger().log(Level.INFO, 
                        "Database is incompatible with this program (version: {0})", 
                        dbVersion);
                return false;
            }
            // TODO: This should be made to backup the database, just in case
                // If the database was not successfully updated to the latest 
                // version this program supports
            if (!conn.updateDatabaseDefinitions(stmt,progressObserver)){
                getLogger().log(Level.WARNING, 
                        "Database could not be updated (version: {0})", 
                        dbVersion);
                return false;
            }
                // Get a set of models that currently already exist
            Set<LinksListModel> oldModels = new HashSet<>(allListsTabsPanel.getModels());
                // Make sure this set has ALL the models, even those that are 
                // somehow absent from the all lists panel
            oldModels.addAll(shownListsTabsPanel.getModels());
                // This will get a map of the existing models mapped to their 
                // listID. Models that don't have a listID won't be in this map
            Map<Integer, LinksListModel> oldModelsMap = new HashMap<>();
                // Go through the existing models
            for (LinksListModel model : oldModels){
                    // If the current model does not have a null listID
                if (model.getListID() != null){
                    oldModelsMap.put(model.getListID(), model);
                }
            }   // Get the map containing the list IDs and data
            ListDataMap listDataMap = conn.getListDataMap();
                // Get the map of list data that will be loaded. If we are 
                // loading all the lists, then this will just be the list data 
            NavigableMap<Integer, ListContents> loadData = listDataMap; // map
                // This will get a map of list IDs and models to be used
            HashMap<Integer, LinksListModel> models = new HashMap<>();
                // This will get the total size of the lists that will be loaded
            int total = 0;
                // If we are loading all the lists
            if (getLoadsAll()){
                    // Get the total size of all the lists in the database
                total = listDataMap.totalSize();
            } else {// Put the map of existing models into the map of models to 
                models.putAll(oldModelsMap);    // be used
                    // Copy the list data map, since we don't want to edit the 
                loadData = new TreeMap<>(listDataMap);  // actual database
                    // Remove any lists that do not need to be re-loaded
                    // Remove all the lists that have been removed
                loadData.keySet().removeAll(allListsTabsPanel.getRemovedListIDs());
                    // Remove all the lists that are outdated compared to the 
                    // existing models
                loadData.values().removeIf((ListContents t) -> {
                        // Get the model with the listID of the current list if 
                        // there is one
                    LinksListModel model = models.get(t.getListID());
                        // Return whether there is a model with that listID and 
                        // the list in the database is outdated
                    return model != null && t.isOutdated(model);
                });
                    // Go through the lists that will be loaded
                for (ListContents temp : loadData.values()){
                    total += temp.size();
                }
            }   // Set the progress maximum to the amount of links that will be 
            progressBar.setMaximum(total);  // loaded
            progressBar.setIndeterminate(false);
                // Go through the lists to be loaded
            for (Map.Entry<Integer,ListContents> listData:loadData.entrySet()){
                    // Get the listID of the list being loaded
                Integer listID = listData.getKey();
                    // Get a model version of the current list
                LinksListModel model = listData.getValue().toModel(progressObserver);
                    // Get the old version of the model (the one that this model 
                    // is replacing), and copy the selection from the old model
                model.setSelectionFrom(oldModelsMap.get(listID));
                    // Put the model in the map containing the loaded models
                models.put(listID, model);
            }
            progressBar.setIndeterminate(true);
                // This gets a map mapping the tabs panels to the list of 
                // listIDs for the lists to be shown by the panels
            Map<LinksListTabsPanel,List<Integer>> tabsListIDs = new HashMap<>();
                // Get a copy of the list of listIDs for the all lists panel
            List<Integer> allListIDs = new ArrayList<>(conn.getAllListIDs());
                // Remove any null listIDs
            allListIDs.removeIf((Integer t) -> t == null);
                // Put the copy into the map, mapping it to the all lists panel
            tabsListIDs.put(allListsTabsPanel, allListIDs);
                // Get a copy of the list of shown listIDs
            List<Integer> shownListIDs = new ArrayList<>(conn.getShownListIDs());
                // Remove any null listIDs
            shownListIDs.removeIf((Integer t) -> t == null);
                // Put the copy into the map, mapping it to the shown lists 
            tabsListIDs.put(shownListsTabsPanel, shownListIDs); // panel
                // This is a set that will get the listIDs missing from the all 
                // listIDs list
            Set<Integer> missingListIDs = new LinkedHashSet<>(shownListIDs);
                // Remove any listIDs that are already in the all listIDs list
            missingListIDs.removeAll(allListIDs);
                // Add any missing listIDs to the all listIDs list
            allListIDs.addAll(missingListIDs);
                // Go through the panels and listIDs to get models for
            for (Map.Entry<LinksListTabsPanel,List<Integer>> entry : tabsListIDs.entrySet()){
                    // A list to get the models to use for the current tabs panel
                List<LinksListModel> modelList = new ArrayList<>();
                    // If we are not loading all the lists, only the outdated ones
                if (!getLoadsAll()){
                        // Remove all listIDs of lists that were removed by the 
                        // program
                    entry.getValue().removeAll(allListsTabsPanel.getRemovedListIDs());
                        // Remove all listIDs of lists that are already loaded, 
                        // since we will be replacing them in-situ instead of 
                        // re-adding them. This is to perserve the order of the 
                        // existing lists, including those that have not been 
                        // assigned a listID yet.
                    entry.getValue().removeAll(entry.getKey().getListIDs());
                        // Add all the currently existing models from the tabs 
                        // panel
                    modelList.addAll(entry.getKey().getModels());
                        // Replace any outdated models with ones loaded from the 
                        // database
                    modelList.replaceAll((LinksListModel t) -> {
                        // If this model is somehow null or (more realistically) 
                        // this model's listID is null (i.e. no listID assigned, 
                        // typical of newly created lists)
                        if (t == null || t.getListID() == null)
                            return t;   // Do not replace this model
                        // Check for any models loaded from the database with 
                        // the same listID, and if there is one, replace the  
                        // current model with the loaded model. If not, then use 
                        // the current model, don't replace it.
                        LinksListModel temp = models.getOrDefault(t.getListID(), t);
                        // If the replacement model is the same as this model or 
                        // if the replacement model is somehow null
                        if (temp == t || temp == null)
                            return t;   // Do not replace this model
                        return temp;
                    });
                }
                // Go through the listIDs for the lists in the database that are 
                // to be displayed on this tabs panel. If we were only loading 
                // any outdated lists, this will only be going through lists 
                // that were added to this tabs panel later and aren't currently 
                // being displayed.
                for (Integer listID : entry.getValue()){
                    // Append the loaded list's model to the models for this 
                    modelList.add(models.get(listID));  // panel
                }
                // Set the list of models for this tabs panel
                tabsModels.put(entry.getKey(), modelList);
            }   // If we are only reloading outdated lists
            if (!getLoadsAll()){
                    // The shown lists tabs panel may be showing lists that have 
                    // since been hidden. Remove any lists that are now hidden
                tabsModels.get(shownListsTabsPanel).removeIf((LinksListModel t) 
                        -> t == null || t.isHidden());
            }
            
            return true;
        }
        @Override
        protected String getFailureMessage(File file){
                // If the database is outdated and cannot be updated 
            if (isDBOutdated){  // automatically by this program
                return String.format(
                        "The database is incompatable with this version of the program.%n"
                        + "Database Version is %s, latest supported major version is %d.x.x", 
                        dbVersion, DATABASE_MAJOR_VERSION);
            }
            return super.getFailureMessage(file);
        }
        @Override
        protected void done(){
                // If this should create the default lists during the initial 
                // load since the lists were not loaded either due to this 
                // failing to load the database or the database file does not 
                // exist
            boolean createLists = !success && !file.exists() && !fullyLoaded;
                // If this should create the default lists
            if (createLists){
                    // Create a list to contain the created models
                List<LinksListModel> modelList = new ArrayList<>();
                    // Go through the names for the default lists
                for (String name : DEFAULT_LIST_NAMES){
                        // Create the list
                    LinksListModel model = new LinksListModel(name);
                        // Set it to edited
                    model.setEdited(true);
                    modelList.add(model);
                }
                tabsModels.put(allListsTabsPanel, modelList);
                tabsModels.put(shownListsTabsPanel, modelList);
            }   // If this successfully loaded the database or this created the 
            if (success || createLists){    // default lists
                    // Go through the model lists for each tabs panel
                for (Map.Entry<LinksListTabsPanel,List<LinksListModel>> entry : 
                        tabsModels.entrySet()){
                        // Get the tabs panel
                    LinksListTabsPanel tabsPanel = entry.getKey();
                        // Set the models for the tabs panel
                    tabsPanel.setModels(entry.getValue());
                        // Go through the lists for the tabs panel
                    for (LinksListPanel panel : tabsPanel){
                            // Set the read only toggle for the current list to 
                            // show whether the list is read only
                        tabsPanel.getListMenuItem(panel, MAKE_LIST_READ_ONLY_ACTION_KEY)
                                .setSelected(panel.isReadOnly());
                            // Get the item used to hide the list if there is one
                        JMenuItem hideItem = tabsPanel.getListMenuItem(panel, 
                                HIDE_LIST_ACTION_KEY);
                            // If there is an item to hide the list
                        if (hideItem != null)
                            hideItem.setSelected(panel.isHidden());
                    }   // If the lists were completely reloaded
                    if (getLoadsAll())
                        tabsPanel.setStructureEdited(false);
                        // If this successfully loaded the lists, none of the 
                        // lists are edited, and there are no structural changes 
                        // to the tabs panel
                    if (success && !tabsPanel.getListsEdited() && 
                            !tabsPanel.isStructureEdited())
                        tabsPanel.clearEdited();
                        // If the program has not fully loaded (this is the 
                    if (!fullyLoaded){  // initial load)
                            // Go through the lists in the tabs panel
                        for (LinksListPanel panel : tabsPanel){
                                // Ensure the last item is visible
                            panel.ensureIndexIsVisible(panel.getModel().size()-1);
                        }
                    }
                }
            }   //If the program has not fully loaded (this is the initial load)
            if (!fullyLoaded){
                    // Set the selected items from the configuration
                setSelectedFromConfig();
                    // Update the program title
                updateProgramTitle();
            }   // Update whether the program has fully loaded
            fullyLoaded = fullyLoaded || getLoadsAll();
                // Re-enable all the lists
            setTabsPanelListsEnabled(true);
                // If all the lists were loaded and this successfully loaded the 
                // lists
            if (getLoadsAll() && success)
                autosaveMenu.stopAutosave();
            super.done();
                // If this should check the local file for any more up-to-date 
                // lists and the file that was loaded is not the local file
            if (LinkManagerUtilities.getFlag(loadFlags,DATABASE_LOADER_CHECK_LOCAL_FLAG) && 
                    !file.equals(getDatabaseFile())){
                loader = new DatabaseLoader(LinkManagerUtilities.setFlag(loadFlags,
                        DATABASE_LOADER_LOAD_ALL_FLAG | DATABASE_LOADER_CHECK_LOCAL_FLAG, false));
                loader.execute();
            }
        }
    }
//    /**
//     * This updates the database before loading the database
//     */
//    private class DatabaseLoadUpdater extends AbstractDatabaseSaver{
//
//        @Override
//        protected boolean saveDatabase(LinkDatabaseConnection conn, 
//                Statement stmt) throws SQLException {
//                // This does nothing. Everything was handled in prepareDatabase
//            return true;
//        }
//        @Override
//        public String getNormalProgressString() {
//            return "Updating Database";
//        }
//        @Override
//        protected void done(){
//            loader = new DatabaseLoader(true);
//            loader.execute();
//        }
//    }
    /**
     * This saves the lists of links to the database.
     */
    private class DatabaseSaver extends AbstractDatabaseSaver{
        
        DatabaseSaver(boolean exit){
            super(exit);
        }
        
        DatabaseSaver(){
            super();
        }
        @Override
        public String getNormalProgressString(){
            return "Saving Lists";
        }
        @Override
        protected boolean saveDatabase(LinkDatabaseConnection conn, 
                Statement stmt) throws SQLException {
            return LinkManager.this.saveDatabase(conn);
        }
        @Override
        protected void showSuccessPrompt(File file){ }
        @Override
        protected void done(){
            deleteBackupIfSuccessful();
            super.done();
        }
    }
    /**
     * This loads the tables in the database and displays them in the database 
     * viewer.
     */
    private class LoadDatabaseViewer extends AbstractDatabaseLoader{
        /**
         * This is the table model displaying the configuration for the program 
         * and the database. If this is null after loading, then the 
         * configuration data failed to load. 
         */
        private CustomTableModel configTableModel = null;
        /**
         * This is the table model displaying the prefixes for the links in the 
         * database. If this is null after loading, then the prefixes failed to 
         * load.
         */
        private CustomTableModel prefixTableModel = null;
        /**
         * This is the table model displaying the list metadata for the lists of 
         * links in the database. If this is null after loading, then the lists 
         * failed to load.
         */
        private CustomTableModel listTableModel = null;
        /**
         * This is the table model displaying the structure of the tables, 
         * views, and indexes in the database. If this is null after loading, 
         * then the structure data failed to load.
         */
        private CustomTableModel tableTableModel = null;
        /**
         * This is the combo box model of the combo box 
         */
        private ArrayComboBoxModel<Integer> listIDComboModel = null;
        /**
         * 
         */
        private ArrayComboBoxModel<String> usedPrefixComboModel = null;
        /**
         * This is the file size of the file storing the database.
         */
        private Long dbFileSize = null;
        /**
         * This is the last time the database was updated.
         */
        private Long dbLastMod = null;
        /**
         * 
         */
        private UUID dbUUID = null;
        /**
         * 
         */
        private String dbVersion = null;
        /**
         * 
         */
        private Integer prefixThreshold = null;
        /**
         * 
         */
        private Integer linkCount = null;
        /**
         * 
         */
        private Integer shownTotalSize = null;
        /**
         * 
         */
        private Integer allTotalSize = null;
        /**
         * 
         */
        private String prefixSeparators = null;
        /**
         * 
         */
        private DefaultMutableTreeNode createPrefixTestNode = null;
        /**
         * This constructs a LoadDatabaseViewer.
         * @param showFileNotFound Whether a file not found error should result 
         * in a popup being shown to the user.
         */
        LoadDatabaseViewer(boolean showFileNotFound){
            super(showFileNotFound);
        }
        @Override
        public String getProgressString(){
            return "Loading Tables";
        }
        @Override
        protected boolean loadDatabase(LinkDatabaseConnection conn, 
                Statement stmt) throws SQLException {
                // Load the table view from the database
            dbViewer.loadTables(showSchemaToggle.isSelected(), conn, stmt, progressBar);
            
            // TODO: Foreign Key Toggle Stuff
            
            prefixTableModel = new CustomTableModel("PrefixID","Prefix");
            prefixTableModel.setColumnClass(0, Integer.class);
            prefixTableModel.setColumnClass(1, String.class);
                // This gets the prefix map from the database
            PrefixMap prefixMap = conn.getPrefixMap();
                // Make sure the prefix map contains the empty prefix
            prefixMap.getEmptyPrefixID();
            usedPrefixComboModel = new ArrayComboBoxModel<>();
            progressBar.setValue(0);
            progressBar.setMaximum(prefixMap.size());
            progressBar.setIndeterminate(false);
                // Go through the prefix map's entries
            for (Map.Entry<Integer,String> entry : prefixMap.entrySet()){
                    // Add a row to the prefix table model
                prefixTableModel.addRow(new Object[]{entry.getKey(), entry.getValue()});
                    // If the current entry is the entry for the empty prefix
                if (entry.getValue().isEmpty()){
                    usedPrefixComboModel.add(entry.getKey() + " - \"\"");
                    usedPrefixComboModel.setSelectedItem(
                            usedPrefixComboModel.get(usedPrefixComboModel.size()-1));
                }
                else
                    usedPrefixComboModel.add(entry.getKey() + " - " + entry.getValue());
                incrementProgressValue();
            }
            
            progressBar.setIndeterminate(true);
                // Search for the empty prefix
            searchUsedPrefixes(conn,prefixMap.getEmptyPrefixID());
            dbLinkSearchTable.setModel(getListSearchTableModel());
            
                // Get the database properties
            DatabasePropertyMap dbProperty = conn.getDatabaseProperties();
                // Add the database properies to the config table
            addConfigRows("Database",dbProperty.toProperties(),
                    dbProperty.getDefaults().toProperties());
            
            progressBar.setIndeterminate(true);
                // Get the list data map from the database
            ListDataMap listDataMap = conn.getListDataMap();
            listTableModel = new CustomTableModel("ListID","List Name",
                    "List Created","Last Modified","Flags","Size Limit","Size");
            listTableModel.setColumnClass(0, Integer.class);
            listTableModel.setColumnClass(1, String.class);
            listTableModel.setColumnClass(2, java.util.Date.class);
            listTableModel.setColumnClass(3, java.util.Date.class);
            listTableModel.setColumnClass(4, Integer.class);
            listTableModel.setColumnClass(5, Integer.class);
            listTableModel.setColumnClass(6, Integer.class);
            listIDComboModel = new ArrayComboBoxModel<>(listDataMap.navigableKeySet());
            try{    // Get the listID of the currently selected list in the 
                    // listID combo model
                Integer listID = (Integer)listIDComboModel.getSelectedItem();
                    // If there is a listID selected
                if (listID != null)
                        // Configure the values shown by the list edit settings
                    setListEditSettings(conn,listID);
            } catch (IllegalArgumentException ex){
                getLogger().log(Level.WARNING, 
                        "Failed to load list settings for list "+
                                listIDComboModel.getSelectedItem(),ex);
            }
            progressBar.setValue(0);
            progressBar.setMaximum(listDataMap.size());
            progressBar.setIndeterminate(false);
                // Go through the list contents objects
            for (ListContents list : listDataMap.values()){
                listTableModel.addRow(new Object[]{
                    list.getListID(),
                    list.getName(),
                        // Get the date and time the list was created
                    new java.util.Date(list.getCreationTime()),
                        // Get the date and time the list was last modified
                    new java.util.Date(list.getLastModified()),
                    list.getFlags(),
                    list.getSizeLimit(),
                    list.size()
                });
                incrementProgressValue();
            }
            
            progressBar.setIndeterminate(true);
                // Get the set of table names in the database
            Set<String> tableSet = conn.showTables();
                // Get the set of views names in the database
            Set<String> viewSet = conn.showViews();
                // Get the set of indexes names in the database
            Set<String> indexSet = conn.showIndexes();
                // Get a copy of the map of the structures in the database
            Map<String,String> structMap = new HashMap<>(conn.showStructures());
                // Remove any structures that are null
            structMap.values().removeIf((String t) -> t == null);
            progressBar.setValue(0);
            progressBar.setMaximum(structMap.size());
            tableTableModel = new CustomTableModel("Name", "Type", "Structure");
            tableTableModel.setColumnClass(0, String.class);
            tableTableModel.setColumnClass(1, String.class);
            tableTableModel.setColumnClass(2, String.class);
            progressBar.setIndeterminate(false);
                // Load the structures for the tables
            loadStructure(tableSet,structMap,"Table");
                // Load the structures for the views
            loadStructure(viewSet,structMap,"View");
                // Load the structures for the indexes
            loadStructure(indexSet,structMap,"Index");
            
            progressBar.setIndeterminate(true);
            
            try{
                prefixThreshold = Integer.valueOf(dbProperty.getProperty(
                        LinkDatabaseConnection.PREFIX_THRESHOLD_CONFIG_KEY));
            } catch(NumberFormatException ex){
                prefixThreshold = null;
            }
            prefixSeparators = dbProperty.getProperty(
                    LinkDatabaseConnection.PREFIX_SEPARATORS_CONFIG_KEY);
            dbVersion = dbProperty.getProperty(
                    LinkDatabaseConnection.DATABASE_VERSION_CONFIG_KEY,"N/A");
            dbUUID = conn.getDatabaseUUID();
            dbLastMod = conn.getDatabaseLastModified();
            linkCount = conn.getLinkMap().size();
            shownTotalSize = conn.getShownListIDs().totalSize();
            allTotalSize = conn.getAllListIDs().totalSize();
                // This gets a set of all the links in the program
            Set<String> linksSet = new LinkedHashSet<>();
                // This gets a set of the list models
            Set<LinksListModel> modelSet = new LinkedHashSet<>(allListsTabsPanel.getModels());
                // Add any shown list models that were somehow not available in 
                // the all tabs panel
            modelSet.addAll(shownListsTabsPanel.getModels());
                // Go through the list modesl
            for (LinksListModel model : modelSet){
                linksSet.addAll(model);
            }   // Remove null if present in the set of links
            linksSet.remove(null);
                // Create the prefix tree
            createPrefixTestNode = prefixMap.createPrefixTree(linksSet);
                // An iterator to go through the nodes in preorder
            Iterator<TreeNode> itr = createPrefixTestNode.preorderEnumeration().asIterator();
                // While there are nodes to go through
            while (itr.hasNext()){
                    // Get the next node
                TreeNode temp = itr.next();
                    // If the node is a DefaultMutableTreeNode
                if (temp instanceof DefaultMutableTreeNode){
                        // Get the node as a DefaultMutableTreeNode
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)temp;
                        // If the current node is either the root, has a user 
                        // object of null, or does not allow children
                    if (node.isRoot() || node.getUserObject() == null || 
                            !node.getAllowsChildren())
                            // Skip this node
                        continue;
                        // Get the first key for the node's user object if there 
                        // is one
                    Integer prefixID = prefixMap.firstKeyFor(node.getUserObject().toString());
                        // If a prefixID for the user object was found
                    if (prefixID != null)
                        node.setUserObject(prefixID + ": \""+node.getUserObject()+"\"");
                    else
                        node.setUserObject("\""+node.getUserObject()+"\"");
                }
            }
            return true;
        }
        /**
         * 
         * @param names
         * @param structMap
         * @param type 
         */
        private void loadStructure(Set<String>names,Map<String,String>structMap,
                String type){
                // Go through the names of the items
            for (String name : names){
                    // If the structure map does not contain the item
                if (!structMap.containsKey(name))
                        // Skip it
                    continue;
                tableTableModel.addRow(new Object[]{
                    name,type,structMap.get(name)
                });
                incrementProgressValue();
            }
        }
        /**
         * 
         */
        private void createConfigModel(){
                // If the config table model has not been constructed yet
            if (configTableModel == null){
                configTableModel = new CustomTableModel(
                        "Source","Property Name","Property",
                        "Is Set","Default Value");
                configTableModel.setColumnClass(0, String.class);
                configTableModel.setColumnClass(1, String.class);
                configTableModel.setColumnClass(2, Object.class);
                configTableModel.setColumnClass(3, String.class);
                configTableModel.setColumnClass(4, Object.class);
            }
        }
        /**
         * 
         * @param source
         * @param property
         * @param value
         * @param isSet
         * @param defaultValue 
         */
        private void addConfigRow(String source, String property, Object value, 
                Boolean isSet, Object defaultValue){
            configTableModel.addRow(new Object[]{
                source,
                property,
                value,
                Objects.toString(isSet, ""),
                defaultValue
            });
        }
        /**
         * 
         * @param source
         * @param config
         * @param defaultConfig 
         * @param setIfNotEqual
         */
        private void addConfigRows(String source, Properties config, 
                Properties defaultConfig, boolean setIfNotEqual){
            progressBar.setIndeterminate(true);
                // Create the config table model if not already created
            createConfigModel();
                // Get the property names for the config
            Set<String> propNames = new TreeSet<>(config.stringPropertyNames());
                // If a default config was provided
            if (defaultConfig != null)
                    // Add all the default property names too
                propNames.addAll(defaultConfig.stringPropertyNames());
            progressBar.setValue(0);
            progressBar.setMaximum(propNames.size());
            progressBar.setIndeterminate(false);
                // Go through the program's property names
            for (String property : propNames){
                    // Get the set property value
                String propValue = config.getProperty(property);
                    // Get the default property value
                String propDefault = (defaultConfig != null) ? 
                        defaultConfig.getProperty(property) : null;
                addConfigRow(
                        source,
                        property,
                        propValue,
                        config.containsKey(property) && 
                                (!setIfNotEqual || !Objects.equals(propValue, propDefault)),
                        propDefault
                );
                incrementProgressValue();
            }
        }
        /**
         * 
         * @param source
         * @param config
         * @param defaultConfig 
         */
        private void addConfigRows(String source, Properties config, 
                Properties defaultConfig){
            addConfigRows(source,config,defaultConfig,false);
        }
        /**
         * 
         * @param source
         * @param prop 
         */
        private void addConfigRows(String source, ConfigProperties prop){
            addConfigRows(source,prop,prop.getDefaults());
        }
        /**
         * 
         * @param source
         * @param node
         */
        private void addConfigRows(String source, ConfigPreferences node){
            getLogger().entering(this.getClass().getName(), "addConfigRows", 
                    new Object[]{source,node});
                // If the preference node is null
            if (node == null)
                return;
            try {   // If the node exists
                if (node.nodeExists(""))
                    addConfigRows(source,node.toProperties());
            } catch (BackingStoreException | IllegalStateException ex) {
                getLogger().log(Level.WARNING, "Failed to load settings from node",
                        ex);
            }
            getLogger().exiting(this.getClass().getName(), "addConfigRows");
        }
        @Override
        protected boolean loadFile(File file){
            if (file.exists())  // If the database file exists
                dbFileSize = file.length();
            return super.loadFile(file);
        }
        @Override
        protected Void backgroundAction() throws Exception {
                // Load the database stuff
            super.backgroundAction();
            
                // Add the configuration for SQLite
            addConfigRows("SQLiteConfig",config.getSQLiteConfig().toProperties(),
                    new SQLiteConfig().toProperties(),true);
                // Add all the properties for this program
            addConfigRows("Properties",config.getProperties(),null);
                // Go through the rows that have been added so far
            for (int i = 0; i < configTableModel.getRowCount(); i++){
                    // If it's a row from the properties and it's the user 
                    // encryption key encryption key
                if ("Properties".equals(configTableModel.getValueAt(i, 0)) && 
                        USER_ENCRYPTION_KEY_KEY.equals(configTableModel.getValueAt(i, 1))){
                        // Remove that row
                    configTableModel.removeRow(i);
                    break;
                }
            }
                // Add all the shared preferences for this program
            addConfigRows("Shared Preferences",config.getSharedPreferences());
                // Add all the preferences for this program
            addConfigRows("Preferences",config.getPreferences());
                // Go through the list type nodes
            for (Integer type : config.getListTypes()){
                    // Add all the preferences for this list type
                addConfigRows("List Type "+type+" Preferences",
                        config.getListTypePreferences(type));
            }   // Go through the listID nodes
            for (Integer id : config.getListIDs()){
                    // Add all the preferences for this listID
                addConfigRows("List ID "+id+" Preferences",
                        config.getListPreferences(id));
            }   // Add all the Dropbox preferences for this program
            addConfigRows("Dropbox Preferences",config.getDropboxPreferences());
            
            return null;
        }
        /**
         * 
         * @param tab The tab component
         * @param enabled 
         */
        protected void setTabEnabled(JComponent tab, boolean enabled){
            dbTabbedPane.setEnabledAt(dbTabbedPane.indexOfComponent(tab), enabled);
        }
        /**
         * 
         * @param table
         * @param model
         * @param tab The tab component
         */
        protected void setTableModel(JTable table, TableModel model, JComponent tab){
                // If the tab component is not null
            if (tab != null)
                    // Set the tab enabled if the table model is not null
                setTabEnabled(tab, model != null);
                // If the table model is not null
            if (model != null)
                table.setModel(model);
        }
        @Override
        protected void done(){
            if (success)    // If this successfully loaded the database
                    // Populate the tables in the database view
                dbViewer.populateTables();
            setTableModel(configTable,configTableModel,configScrollPane);
            setTableModel(dbPrefixTable,prefixTableModel,null);
            setTableModel(dbListTable,listTableModel,dbListPanel);
            setTableModel(dbTableTable,tableTableModel,dbTablePanel);
            if (dbFileSize == null)
                dbFileSizeLabel.setText("N/A");
            else
                dbFileSizeLabel.setText(String.format("%s (%,d Bytes)", 
                        byteFormatter.format(dbFileSize), dbFileSize));
            if (dbUUID == null)
                dbUUIDLabel.setText("N/A");
            else
                dbUUIDLabel.setText(dbUUID.toString());
            dbVersionLabel.setText(Objects.toString(dbVersion,"N/A"));
            setDBLastModLabelText(dbLastMod);
            if (prefixThreshold != null)
                prefixThresholdSpinner.setValue(prefixThreshold);
            if (prefixSeparators != null)
                prefixSeparatorField.setText(prefixSeparators);
            if (listIDComboModel != null)
                dbListIDCombo.setModel(listIDComboModel);
            dbListIDCombo.setEnabled(listIDComboModel != null);
            updateListEditButtons();
            if (usedPrefixComboModel != null){
                dbUsedPrefixCombo.setModel(usedPrefixComboModel);
                dbSearchPrefixCombo.setModel(usedPrefixComboModel);
            }
            setTabEnabled(dbUsedPrefixesPanel,usedPrefixComboModel != null);
            linkCountLabel.setText(Objects.toString(linkCount,"N/A"));
            shownTotalSizeLabel.setText(Objects.toString(shownTotalSize,"N/A"));
            allTotalSizeLabel.setText(Objects.toString(allTotalSize,"N/A"));
            programIDLabel.setText(Objects.toString(config.getProgramID(), "N/A"));
            
            setTabEnabled(dbCreatePrefixScrollPane,createPrefixTestNode != null);
            if (createPrefixTestNode != null)
                dbCreatePrefixTree.setModel(new DefaultTreeModel(createPrefixTestNode,true));
            
            super.done();
        }
    }
    /**
     * This resets the IDs in the database.
     */
    private class ResetDatabaseIDs extends AbstractDatabaseSaver{
        @Override
        public String getNormalProgressString(){
            return "Resetting IDs";
        }
        @Override
        protected boolean saveDatabase(LinkDatabaseConnection conn, Statement stmt) 
                throws SQLException {
                // Remove all existing IDs from the program
            allListsTabsPanel.removeAllIDs();
            shownListsTabsPanel.removeAllIDs();
                // Get the map containing the list data
            ListDataMap listDataMap = conn.getListDataMap();
                // Get the map containing the links
            LinkMap linkMap = conn.getLinkMap();
                // Get the list of all listIDs
            ListIDList allListsList = conn.getAllListIDs();
                // Get the list of shown listIDs
            ListIDList shownListsList = conn.getShownListIDs();
                // Get the map containing the list names
            ListNameMap listNameMap = conn.getListNameMap();
            
                // Clear the list data
            listDataMap.clearAll();
                // Remove all links
            linkMap.clear();
                // Clear the list of all listIDs
            allListsList.clear();
                // Clear the list of shown listIDs
            shownListsList.clear();
                // Remove all the lists
            listNameMap.clear();
            conn.commit();          // Commit the changes to the database
            System.gc();            // Run the garbage collector
                // This is a set containing all the models in the program
            Set<LinksListModel> models = new LinkedHashSet<>(allListsTabsPanel.getModels());
                // Add any models that are in the shown lists panel that are 
                // missing from the all lists panel
            models.addAll(shownListsTabsPanel.getModels());
                // Write the models to the database
            writeToDatabase(conn, models);
            
            progressBar.setIndeterminate(true);
            conn.commit();          // Commit the changes to the database
            System.gc();            // Run the garbage collector
            
                // Verify the database to ensure all the data has been written
            setVerifyingDatabase(true);
                // This is a set containing all the links in the models
            Set<String> linkSet1 = new LinkedHashSet<>();
                // This is a set containing all the links in the database
            Set<String> linkSet2 = new LinkedHashSet<>(linkMap.values());
                // This is a map to get the names of the lists from the models
            Map<Integer,String> listNames = new HashMap<>();
                // This is a map that maps the models to their listIDs
            Map<Integer,LinksListModel> modelMap = new LinkedHashMap<>();
                // Go through the models that were saved
            for (LinksListModel model : models){
                    // Add all the links in the model
                linkSet1.addAll(model);
                listNames.put(model.getListID(), model.getListName());
                modelMap.put(model.getListID(), model);
            }
            
            progressBar.setValue(0);
            progressBar.setMaximum(listDataMap.size()+4);
            progressBar.setIndeterminate(false);
                // Check to make sure the database contains all the links in the 
            if (!linkSet2.containsAll(linkSet1))    // program
                return false;
            incrementProgressValue();
                // Check to make sure the database contains all the lists and 
                // their names in the program
            if (!listNames.equals(listNameMap))
                return false;
            incrementProgressValue();
                // Check to make sure the all listIDs list contains all the 
                // listIDs of the lists shown by the all lists panel
            if (!allListsTabsPanel.getListIDs().equals(allListsList))
                return false;
            incrementProgressValue();
                // Check to make sure the shown listIDs list contains all the 
                // listIDs of the shown lists
            if (!shownListsTabsPanel.getListIDs().equals(shownListsList))
                return false;
            incrementProgressValue();
                // Go through the listIDs of the lists in the database
            for (Integer listID : listDataMap.navigableKeySet()){
                    // Check to make sure the list in the database matches its 
                    // corresponding model
                if (!listDataMap.get(listID).equalsModel(modelMap.get(listID)))
                    return false;
                incrementProgressValue();
            }
            return true;
        }
        @Override
        protected void done(){
            deleteBackupIfSuccessful();
            super.done();
                // Reload the database view data
            loader = new LoadDatabaseViewer(true);
            loader.execute();
        }
    }
    
    private class LinkPrefixUpdater extends AbstractDatabaseSaver{
        @Override
        public String getNormalProgressString(){
            return "Updating Prefixes";
        }
        @Override
        protected boolean saveDatabase(LinkDatabaseConnection conn, Statement stmt) 
                throws SQLException {
                // Get the map of prefixes and their prefixIDs
            PrefixMap prefixes = conn.getPrefixMap();
                // Get the map of links and their linkIDs
            LinkMap links = conn.getLinkMap();
                // Make sure that we have the longest possible prefixes for all 
                // the links in the database
            prefixes.createPrefixesFrom(links.values());
            conn.commit();       // Commit the changes to the database
                // Get a copy of the links map to compare with after the 
                // prefixes have been updated
            Map<Long,String> storedLinks = new LinkedHashMap<>(links);
            
            progressBar.setMaximum(links.size());
            progressBar.setIndeterminate(false);
                // Update the prefixes for the links in the database
            conn.updateLinkPrefix(links.navigableKeySet());
            conn.commit();       // Commit the changes to the database
            
            progressBar.setIndeterminate(true);
            
                // Check to make sure everything is effectively the same, just 
                // updated
            setVerifyingDatabase(true);
            return storedLinks.equals(links);
        }
        @Override
        protected void done(){
            deleteBackupIfSuccessful();
            super.done();
                // Reload the database view data
            loader = new LoadDatabaseViewer(true);
            loader.execute();
        }
    }
    
    private class UpdateDatabase extends AbstractDatabaseSaver{
        
        private int mode;
        
        UpdateDatabase(File file, int updateType){
            super(file);
            this.mode = updateType;
        }
        @Override
        public String getNormalProgressString(){
            return "Updating Database";
        }
        @Override
        protected boolean prepareDatabase(File file, LinkDatabaseConnection conn, 
                Statement stmt) throws SQLException{ 
            return true;
        }
        @Override
        protected boolean saveDatabase(LinkDatabaseConnection conn, 
                Statement stmt) throws SQLException {
            getLogger().entering(this.getClass().getName(), "saveDatabase", mode);
            boolean updateSuccess = true;
            if (mode != 0)
                conn.setForeignKeysEnabled(false, stmt);
            switch(mode){
                case(0):
                    updateSuccess = conn.updateDatabaseDefinitions(stmt, progressObserver);
                    break;
                case(1):
                    updateSuccess = conn.updateAddConfigTable(stmt, progressObserver);
                    break;
                case(2):
                    updateSuccess = conn.updateToVersion1_0_0(stmt, progressObserver);
                    break;
                case(3):
                    updateSuccess = conn.updateAddListSizeLimitColumn(stmt, progressObserver);
                    break;
                case(4):
                    updateSuccess = conn.updateToVersion0_5_0(stmt, progressObserver);
                    break;
                case(5):
                    updateSuccess = conn.updateAddPrefixTable(stmt, progressObserver);
                    break;
                case(6):
                    updateSuccess = conn.updateAddListFlagsColumn(stmt, progressObserver);
                    break;
                case(7):
                    updateSuccess = conn.updateRenameInitialColumns(stmt, progressObserver);
                    break;
                case(8):
                    updateSuccess = conn.updateRenameLinkColumns(stmt, progressObserver);
            }
            if (mode != 0)
                conn.setForeignKeysEnabled(true, stmt);
            getLogger().exiting(this.getClass().getName(), "saveDatabase", updateSuccess);
            return updateSuccess;
        }
    }
    
    private class ExportDatabase extends FileSaver{

        ExportDatabase(File file) {
            super(file);
        }
        
        @Override
        protected boolean isFileTheDirectory(){
            return true;
        }
        @Override
        public String getProgressString(){
            return "Exporting Lists";
        }
        @Override
        protected String getSuccessMessage(File file){
            return "The files were successfully created.";
        }
        @Override
        protected String getFailureMessage(File file){
            return "The files failed to be created.";
        }
        @Override
        protected boolean saveFile(File file) {
            int max = 0;
            for (LinksListPanel panel : allListsTabsPanel)
                max += panel.getModel().size();
            progressBar.setMaximum(max);
            /* Copied path checking code from FileRearranger
            try{
                path = newFile.toPath();
            }
            catch (java.nio.file.InvalidPathException exc){
                String message = null;  // The error message to print
                    // If it's an illegal character, and the index is within the string
                if (exc.getReason().startsWith("Illegal char") && 
                        exc.getIndex() >= 0 && exc.getIndex() < newFileName.length()){
            */
            for (LinksListPanel panel : allListsTabsPanel){
                    // TODO: Check for whether any for the list names are invalid
//                File listFile = null;
//                String fileName = panel.getListName()+".txt";
//                do{
//                    listFile = new File(file,fileName);
//                    try{
//                        listFile.toPath();
//                    }catch (InvalidPathException exc){
//                        int index = exc.
//                    }
//                }
//                while (listFile == null);
                
                if (!writeToFile(new File(file,panel.getListName()+".txt"),panel.getModel()))
                    return false;
            }
            return true;
        }
    }
    /**
     * 
     */
    private class DatabaseFileChanger extends FileSaver{
        
        private int mode;
        
        private File source;
        
        private String target;
        
        private Exception exc = null;

        DatabaseFileChanger(int mode, File source, String target) {
            super(getDatabaseFile(target));
            this.mode = mode;
            this.source = source;
            this.target = target;
        }
        @Override
        public String getProgressString() {
            switch(mode){
                case(1):
                    return "Copying Database File";
                case(2):
                    return "Moving Database File";
                default:
                    return "Changing Database File";
            }
        }
        @Override
        protected boolean saveFile(File file) {
            getLogger().entering(this.getClass().getName(), "saveFile",file);
            try{
                Path path = file.toPath();
                switch(mode){
                    case(0):
                        getLogger().exiting(this.getClass().getName(), "saveFile",true);
                        return true;
                    case(1):
                        Files.copy(source.toPath(), path, 
                                StandardCopyOption.COPY_ATTRIBUTES, 
                                StandardCopyOption.REPLACE_EXISTING);
                        getLogger().exiting(this.getClass().getName(), "saveFile",true);
                        return true;
                    case(2):
                        Files.move(source.toPath(), path, 
                                StandardCopyOption.REPLACE_EXISTING);
                        getLogger().exiting(this.getClass().getName(), "saveFile",true);
                        return true;
                }
            } catch(InvalidPathException | IOException ex){ 
                getLogger().log(Level.WARNING, 
                        "Failed to handle changing database file (mode: "+mode+")",
                        ex);
                exc = ex;
            }
            getLogger().exiting(this.getClass().getName(), "saveFile",false);
            return false;
        }
        @Override
        protected String getSuccessMessage(File file){
            switch (mode){
                case(1):
                    return "The database file was successfully copied.";
                case(2):
                    return "The database file was successfully moved.";
                default:
                    return "The database file was successfully changed.";
            }
        }
        @Override
        protected String getFailureMessage(File file){
            String msg;
            if (didBackupFail())   // If this failed to create the backup file
                msg = "The backup file failed to be created.";
            else{
                switch (mode){
                    case(1):
                        msg = "The database file failed to be copied.";
                        break;
                    case(2):
                        msg = "The database file failed to be moved.";
                        break;
                    default:
                        msg = "The database file failed to be changed.";
                }
            }
            if (exc != null)
                msg += "\n\nError: " + exc;
            return msg;
        }
        @Override
        protected void done(){
            if (success){
                config.setDatabaseFileName(target);
            }
            super.done();
            setLocationDialog.setVisible(false);
        }
    }
    /**
     * 
     */
    private abstract class FilePathSaver extends FileSaver{
        /**
         * The path for the file to be downloaded.
         */
        protected String filePath;
        /**
         * This gets any IOExceptions that get thrown while uploading or 
         * downloading the file.
         */
        protected IOException ioEx = null;
        /**
         * This gets any Dropbox exceptions that get thrown while uploading or 
         * downloading the file.
         */
        protected DbxException dbxEx = null;
        /**
         * Whether the file was found.
         */
        protected boolean fileFound = true;
        /**
         * Whether file not found errors should be shown.
         */
        protected boolean showFileNotFound = true;
        /**
         * The mode in which to use for uploading and downloading the file
         */
        protected int mode;
        /**
         * 
         * @param file
         * @param path
         * @param mode
         * @param exit 
         */
        public FilePathSaver(File file, String path, int mode, boolean exit) {
            super(file, exit);
            filePath = Objects.requireNonNull(path);
            this.mode = mode;
        }
        /**
         * 
         * @param file
         * @param path 
         * @param mode
         */
        public FilePathSaver(File file, String path, int mode){
            this(file,path,mode,false);
        }
        /**
         * 
         * @return 
         */
        public String getFilePath(){
            return filePath;
        }
        /**
         * 
         * @return 
         */
        public boolean getFileNotFound(){
            return !fileFound;
        }
        /**
         * 
         * @return 
         */
        public int getMode(){
            return mode;
        }
        /**
         * This returns whether this shows a failure prompt when the file is not 
         * found.
         * @return Whether the file not found failure prompt is shown.
         */
        public boolean getShowsFileNotFoundPrompt(){
            return showFileNotFound;
        }
        /**
         * This sets whether this shows a failure prompt when the file is not 
         * found.
         * @param showFileNotFound Whether the file not found failure prompt is 
         * shown.
         * @return This FilePathSaver.
         */
        public FilePathSaver setShowsFileNotFoundPrompt(boolean showFileNotFound){
            this.showFileNotFound = showFileNotFound;
            return this;
        }
        /**
         * This returns whether this will show the success prompt if the file 
         * was successfully saved.
         * @param file The file that was successfully saved.
         * @param path The path of the file that was successfully saved.
         * @return Whether the success prompt will be shown.
         */
        protected boolean getShowSuccessPrompt(File file, String path){
            return true;
        }
        /**
         * This returns the title for the dialog to display if the file is 
         * successfully downloaded.
         * @param file The file that was successfully downloaded to.
         * @param path The path of the file that was successfully downloaded.
         * @return The title for the dialog to display if the file is 
         * successfully downloaded.
         */
        protected String getSuccessTitle(File file, String path){
            return super.getSuccessTitle(file);
        }
        @Override
        protected String getSuccessTitle(File file){
            return getSuccessTitle(file,filePath);
        }
        /**
         * This returns the message to display if the file is successfully 
         * downloaded.
         * @param file The file that was successfully downloaded to.
         * @param path The path of the file that was successfully downloaded.
         * @return The message to display if the file is successfully 
         * downloaded.
         */
        protected String getSuccessMessage(File file, String path){
            return super.getSuccessMessage(file);
        }
        @Override
        protected String getSuccessMessage(File file){
            return getSuccessMessage(file,filePath);
        }
        /**
         * This returns the title for the dialog to display if the file fails to 
         * download.
         * @param file The file that failed to be downloaded to.
         * @param path The path of the file that failed to download.
         * @return The title for the dialog to display if the file fails to
         * download.
         */
        protected String getFailureTitle(File file, String path){
            return super.getFailureTitle(file);
        }
        @Override
        protected String getFailureTitle(File file){
            return getFailureTitle(file,filePath);
        }
        /**
         * This returns the message to display if the backup of the file failed 
         * to be created.
         * @param file The file that failed to be backed up.
         * @param path The path of the file that failed to be backed up.
         * @return The message to display if a backup of the file failed to be 
         * created.
         */
        protected String getBackupFailedMessage(File file, String path){
            return super.getBackupFailedMessage(file);
        }
        /**
         * This returns the message to display if the backup of the file failed 
         * to be created.
         * @param file The file that failed to be backed up.
         * @return The message to display if a backup of the file failed to be 
         * created.
         */
        @Override
        protected String getBackupFailedMessage(File file){
            return getBackupFailedMessage(file,filePath);
        }
        /**
         * This returns the message to display if the file fails to download.
         * @param file The file that failed to be downloaded to.
         * @param path The path of the file that failed to download.
         * @return The message to display if the file fails to download.
         */
        protected String getFailureMessage(File file, String path){
            return super.getFailureMessage(file);
        }
        /**
         * This returns the message to display for any exceptions that were 
         * thrown while attempting to download the file.
         * @param file The file that failed to be downloaded to.
         * @param path The path of the file that failed to download.
         * @return The message to display for any exceptions that occurred, or 
         * null.
         */
        protected String getExceptionMessage(File file, String path){
                // The message to provide for the exception
            String msg = null;
                // If a Dropbox exception occurred
            if (dbxEx != null)
                msg = dbxEx.toString();
                // If an IOException occurred, say so. Otherwise, return null.
            return (ioEx == null) ? msg : (((msg != null) ? msg +" " : "") + 
                    ioEx.toString());
        }
        /**
         * 
         * @return 
         */
        protected String getLoggingExceptionMessage(int mode){
            return "Failed to process file";
        }
        /**
         * This returns the message to display if the file was not found.
         * @param file The file that failed to be downloaded to.
         * @param path The path for the file that was not found.
         * @return The message to display if the file was not found.
         */
        protected String getFileNotFoundMessage(File file, String path){
            return "The file does not exist.";
        }
        /**
         * This returns the message to display if the file fails to be 
         * downloaded.
         * @return The message to display if the file fails to download.
         */
        @Override
        protected String getFailureMessage(File file){
                // The message to return
            String msg = getFailureMessage(file,filePath);
                // If the program is either in debug mode or if details are to 
                // be shown
            if (isInDebug() || showDBErrorDetailsToggle.isSelected()){
                String errorMsg = getExceptionMessage(file,filePath);
                if (errorMsg != null)
                    msg += "\nError: " + errorMsg;
            }
            return msg;
        }
        @Override
        protected void showSuccessPrompt(File file){
            if (getShowSuccessPrompt(file,filePath))
                super.showSuccessPrompt(file);
        }
        @Override
        protected boolean showFailurePrompt(File file){
            if (getFileNotFound()){
                    // If this should show file not found prompts
                if (showFileNotFound){
                    JOptionPane.showMessageDialog(LinkManager.this, 
                            getFileNotFoundMessage(file,filePath), 
                            getFailureTitle(file,filePath), 
                            JOptionPane.ERROR_MESSAGE);
                }
                return false;
            }
            return super.showFailurePrompt(file);
        }
        /**
         * 
         * @param file
         * @param path
         * @param mode
         * @return
         * @throws IOException 
         * @throws DbxException
         */
        protected abstract boolean saveFile(File file, String path, int mode) 
                throws IOException, DbxException;
        @Override
        protected boolean saveFile(File file) {
            getLogger().entering(this.getClass().getName(), "saveFile", file);
                // Reset the exception to null
            ioEx = null;
                // Set the progress to be zero
            progressBar.setValue(0);
                // Set the progress to be indeterminate
            progressBar.setIndeterminate(true);
            boolean value = false;
            switch(mode){
                case(0):
                    filePath = DropboxUtilities.formatDropboxPath(filePath);
            }
            try{    // Try to download the file from the path
                value = saveFile(file,filePath,mode);
            } catch (IOException ex){
                getLogger().log(Level.WARNING, getLoggingExceptionMessage(mode),ex);
                ioEx = ex;
            } catch (DbxException ex) {
                getLogger().log(Level.WARNING, getLoggingExceptionMessage(mode),ex);
                dbxEx = ex;
            }
            getLogger().exiting(this.getClass().getName(), "saveFile", value);
            return value;
        }
        @Override
        protected void done(){
            super.done();
                // Update the program configuration
            updateProgramConfig();
        }
    }
    /**
     * 
     */
    private class FileDownloader extends FilePathSaver{
        /**
         * The flags to use for loading the lists. If this is null, then the 
         * database will not be loaded after this.
         */
        protected Integer loadFlags = null;
        /**
         * 
         * @param file
         * @param path
         * @param mode
         * @param loadFlags
         * @param exit 
         */
        FileDownloader(File file, String path, int mode, Integer loadFlags, boolean exit){
            super(file,path,mode,exit);
            this.loadFlags = loadFlags;
        }
        /**
         * 
         * @param file
         * @param path
         * @param mode
         * @param loadFlags 
         */
        FileDownloader(File file, String path, int mode, Integer loadFlags){
            this(file,path,mode,loadFlags,false);
        }
        /**
         * 
         * @param file
         * @param path 
         * @param mode
         */
        FileDownloader(File file, String path, int mode){
            this(file,path,mode,null);
        }
        /**
         * 
         * @return 
         */
        public boolean willLoadDatabase(){
            return loadFlags != null;
        }
        /**
         * 
         * @return 
         */
        public int getDatabaseLoaderFlags(){
                // If the database loader flags are not null
            if (loadFlags != null)
                return loadFlags;
            return 0;
        }
        @Override
        public String getProgressString() {
            return "Downloading File";
        }
        @Override
        protected boolean getShowSuccessPrompt(File file, String path){
            return !willLoadDatabase();
        }
        @Override
        protected String getSuccessTitle(File file, String path){
            return "File Downloaded Successfully";
        }
        @Override
        protected String getSuccessMessage(File file, String path){
            return "The file was successfully downloaded.";
        }
        @Override
        protected String getFailureTitle(File file, String path){
            return "ERROR - File Failed To Download";
        }
        @Override
        protected String getFailureMessage(File file, String path){
            return "The file failed to download.";
        }
        @Override
        protected String getFileNotFoundMessage(File file, String path){
            String msg = "";
            switch(mode){
                case (0):
                    msg = " on Dropbox";
            }
            return "The file was not found"+msg+" at the path\n\""+path+"\"";
        }
        @Override
        protected String getLoggingExceptionMessage(int mode){
            String msg = "";
            switch(mode){
                case(0):
                    msg = " from Dropbox";
            }
            return "Failed to download file" + msg;
        }
        @Override
        protected boolean saveFile(File file, String path, int mode) 
                throws IOException, DbxException{
            getLogger().entering(this.getClass().getName(), "saveFile",
                    new Object[]{file,path,mode});
            switch(mode){
                case(0):    // Try to download the file from Dropbox
                    FileMetadata data = downloadFromDropbox(file,path);
                    fileFound = data != null;
                    getLogger().exiting(this.getClass().getName(), "saveFile",fileFound);
                    return fileFound;
            }
            getLogger().exiting(this.getClass().getName(), "saveFile",false);
            return false;
        }
        /**
         * 
         * @param loadAll 
         */
        protected void loadDatabase(int loadFlags){
            loader = new DatabaseLoader(file,loadFlags);
            loader.execute();
        }
        @Override
        protected void done(){
            super.done();
            if (willLoadDatabase()){
                loadDatabase(getDatabaseLoaderFlags());
            }
        }
    }
    /**
     * 
     */
    private class FileUploader extends FilePathSaver{
        /**
         * Whether the success prompt should be shown.
         */
        protected boolean showSuccess;
        /**
         * 
         * @param file
         * @param path
         * @param mode
         * @param showSuccess
         * @param exit 
         */
        FileUploader(File file, String path, int mode, boolean showSuccess, boolean exit){
            super(file, path, mode, exit);
            this.showSuccess = showSuccess;
        }
        /**
         * 
         * @param file
         * @param path
         * @param mode
         * @param showSuccess 
         */
        FileUploader(File file, String path, int mode, boolean showSuccess){
            this(file,path,mode,showSuccess,false);
        }
        /**
         * 
         * @param file
         * @param path 
         * @param mode
         */
        FileUploader(File file, String path, int mode){
            this(file,path,mode,true);
        }
        /**
         * This sets whether the program will exit after this finishes saving 
         * the file.
         * @param value Whether the program will exit once the file is saved.
         * @return This FileUploader.
         */
        public FileUploader setExitAfterSaving(boolean value){
            exitAfterSaving = value;
            return this;
        }
        @Override
        public String getProgressString(){
            return "Uploading File";
        }
        /**
         * 
         * @return 
         */
        public boolean getShowSuccessPrompt(){
            return showSuccess;
        }
        /**
         * 
         * @param value
         * @return This FileUploader.
         */
        public FileUploader setShowSuccessPrompt(boolean value){
            showSuccess = value;
            return this;
        }
        @Override
        protected boolean getShowSuccessPrompt(File file, String path){
            return showSuccess && !exitAfterSaving;
        }
        @Override
        protected String getSuccessTitle(File file, String path){
            return "File Uploaded Successfully";
        }
        @Override
        protected String getSuccessMessage(File file, String path){
            return "The file was successfully uploaded.";
        }
        @Override
        protected String getFailureTitle(File file, String path){
            return "ERROR - File Failed To Upload";
        }
        @Override
        protected String getFailureMessage(File file, String path){
            return "The file failed to upload.";
        }
        @Override
        protected String getLoggingExceptionMessage(int mode){
            String msg = "";
            switch(mode){
                case(0):
                    msg = " to Dropbox";
            }
            return "Failed to upload file" + msg;
        }
        @Override
        protected boolean saveFile(File file, String path, int mode) throws 
                IOException, DbxException{
            getLogger().entering(this.getClass().getName(),"saveFile",
                    new Object[]{file,path,mode});
            switch(mode){
                case(0):     // Try to upload the file to Dropbox
                    uploadToDropbox(file,path);
                    getLogger().exiting(this.getClass().getName(), "saveFile",true);
                    return true;
            }
            getLogger().exiting(this.getClass().getName(), "saveFile",false);
            return false;
        }
        @Override
        protected boolean processFile(File file){
                // If the file does not exist
            if (!file.exists()){
                fileFound = false;
                return false;
            }
            return super.processFile(file);
        }
        @Override
        protected void exitProgram(){
            saver = new ProgramConfigSaver(true);
            saver.execute();
        }
    }
    /**
     * 
     */
    private class DbxAccountLoader extends LinkManagerWorker<Void>{
        /**
         * This gets any Dropbox exceptions that get thrown while loading the 
         * Dropbox account.
         */
        private DbxException dbxEx = null;
        /**
         * Whether this successfully loaded the account information.
         */
        private boolean success = false;
        /**
         * Whether the account is a valid Dropbox account.
         */
        private boolean validAccount = true;
        /**
         * The Dropbox account's user name.
         */
        private String accountName = null;
        /**
         * The Dropbox account's profile picture.
         */
        private Icon pfpIcon = null;
        /**
         * The amount of space that the user has used in their Dropbox account.
         */
        private long used = 0;
        /**
         * The amount of space allocated to the user's Dropbox account.
         */
        private long allocated = 0;
        @Override
        public String getProgressString() {
            return "Loading Dropbox Account";
        }
        /**
         * This is used to display a failure prompt to the user when the dropbox 
         * account fails to load. If the failure prompt is a retry prompt, then 
         * this method should return whether to try load the account again. 
         * Otherwise, this method should return {@code false}.
         * @return {@code true} if this should attempt to load the account 
         * again, {@code false} otherwise.
         */
        protected boolean showFailurePrompt(){
            if (!validAccount){
                JOptionPane.showMessageDialog(setLocationDialog, 
                        "Dropbox failed to load due to the account being invalid.",
                        "ERROR - Dropbox Account Load Failed",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
                // The message to return
            String msg = "An error occurred loading the information for your "
                    + "Dropbox account.";
                // If the program is either in debug mode or if details are to 
                // be shown
            if (isInDebug() || showDBErrorDetailsToggle.isSelected()){
                if (dbxEx != null)
                    msg += "\nError: " + dbxEx;
            }
               // Ask the user if they would like to try loading the account
            return JOptionPane.showConfirmDialog(LinkManager.this, // again
                    msg+"\nWould you like to try again?",
                    "ERROR - Dropbox Account Load Failed",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.ERROR_MESSAGE) == JOptionPane.YES_OPTION;
        }
        /**
         * 
         * @throws DbxException 
         * @return 
         */
        protected boolean loadDropboxAccount(){
            getLogger().entering(this.getClass().getName(), "loadDropboxAccount");
                // Reset the exceptions
            dbxEx = null;
            try{    // Get a client to communicate with Dropbox, refreshing the 
                    // Dropbox credentials if necessary
                DbxClientV2 client = dbxUtils.createClientUtils().getClientWithRefresh();
                    // Get the request for the user
                DbxUserUsersRequests users = client.users();
                    // Get the account details for the user
                FullAccount account = users.getCurrentAccount();
                    // Get the user's account name
                accountName = account.getName().getDisplayName();
                    // Load the profile picture for the user
                pfpIcon = DropboxUtilities.getProfilePicture(account, accountName);
                    // Get the space usage for the user
                SpaceUsage spaceUsage = users.getSpaceUsage();
                    // Get the amount of space used by the user
                used = spaceUsage.getUsed();
                    // Get the amount of space allocated to the user
                allocated = DropboxUtilities.getAllocatedSpace(spaceUsage);
                getLogger().exiting(this.getClass().getName(), "loadDropboxAccount",true);
                return true;
            } catch (InvalidAccessTokenException ex){
                getLogger().log(Level.INFO, "Dropbox account token has expired", 
                        ex);
                validAccount = false;
            } catch(DbxException ex){
                getLogger().log(Level.INFO,"Failed to load Dropbox account",ex);
                dbxEx = ex;
            }
            getLogger().exiting(this.getClass().getName(), "loadDropboxAccount",false);
            return false;
        }
        @Override
        protected Void backgroundAction() throws Exception {
                // Whether the user wants this to try loading the account again 
            boolean retry = false;  // if unsuccessful
            do{
                success = loadDropboxAccount();    // Try to load the account
                if (!success)    // If the acount failed to load
                        // Show the failure prompt and get if the user wants to 
                    retry = showFailurePrompt();    // try again
            }   // While the account failed to load and the user wants to try 
            while(!success && retry);   // again
            return null;
        }
        @Override
        protected void done(){
            if (success){
                dbxAccountLabel.setText(accountName);
                dbxPfpLabel.setIcon(pfpIcon);
                dbxSpaceUsedLabel.setText(String.format("%s (%,d Bytes)", 
                        byteFormatter.format(used),used));
                    // Get the space the user has free
                long free = allocated - used;
                dbxSpaceFreeLabel.setText(String.format("%s (%,d Bytes)", 
                        byteFormatter.format(free),free));
                LinkManagerUtilities.setCard(setLocationPanel,setDropboxCard);
            } else if (!validAccount){
                dbxUtils.clearCredentials();
            } else {
                LinkManagerUtilities.setCard(setLocationPanel,setExternalCard);
            }
            super.done();
        }
    }
}
