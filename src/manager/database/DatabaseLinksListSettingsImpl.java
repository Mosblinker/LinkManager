/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import config.ConfigUtilities;
import java.awt.Rectangle;
import java.sql.*;
import java.util.*;
import manager.LinkManager;
import manager.config.*;
import static manager.database.LinkDatabaseConnection.*;
import sql.UncheckedSQLException;

/**
 *
 * @author Mosblinker
 */
class DatabaseLinksListSettingsImpl extends AbstractLinksListSettings 
        implements DatabaseLinksListSettings{
    /**
     * This is the template for updating a value in the tables.
     */
    private static final String UPDATE_VALUE_TEMPLATE = 
            "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?";
    /**
     * This is the template for inserting a value into the tables.
     */
    private static final String INSERT_VALUE_TEMPLATE = 
            "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)";
    /**
     * This is the template for selecting a value from the tables.
     */
    private static final String SELECT_VALUE_TEMPLATE = 
            "SELECT %s FROM %s WHERE %s = ? AND %s = ?";
    
    /**
     * The connection to the database.
     */
    private final LinkDatabaseConnection conn;
    /**
     * 
     */
    private final int programID;
    /**
     * This is initially 
     * null and is initialized when first used.
     */
    private Map<Integer, Long> selLinkIDMap = null;
    /**
     * 
     * @param conn
     * @param programID The connection to the database (cannot be null).
     */
    public DatabaseLinksListSettingsImpl(LinkDatabaseConnection conn, int programID){
        this.conn = Objects.requireNonNull(conn);
        this.programID = programID;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public LinkDatabaseConnection getConnection(){
        return conn;
    }
    @Override
    public int getProgramID() {
        return programID;
    }
    /**
     * 
     * @return 
     */
    private Set<Integer> getKeySet(String columnName, String tableName){
        Set<Integer> keys = new TreeSet<>();
        try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                "SELECT DISTINCT %s FROM %s WHERE %s = ?",
                    columnName,
                    tableName,
                    PROGRAM_ID_COLUMN_NAME))){
            pstmt.setInt(1, programID);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                keys.add(rs.getInt(columnName));
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        return keys;
    }
    /**
     * 
     * @param columnName
     * @param tableName
     * @return 
     */
    private int getKeyCount(String columnName, String tableName){
            // Prepare a statement to count the unique instances of the list 
            // types in the list of lists table
        try(PreparedStatement pstmt = conn.prepareStatement(
                String.format(TABLE_SIZE_QUERY_TEMPLATE+" WHERE %s = ?", 
                        "DISTINCT "+columnName,
                        tableName,
                        PROGRAM_ID_COLUMN_NAME))){
            pstmt.setInt(1, programID);
                // Query the database
            ResultSet rs = pstmt.executeQuery();
                // If there are any results from the query
            if (rs.next())
                return rs.getInt(COUNT_COLUMN_NAME);
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        return 0;
    }
    /**
     * 
     * @param key
     * @param columnName
     * @param tableName
     * @return 
     */
    private boolean containsKeySQL(int key, String columnName, String tableName) 
            throws SQLException{
        try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                    TABLE_CONTAINS_QUERY_TEMPLATE+" AND %s = ?", 
                        PROGRAM_ID_COLUMN_NAME,
                        tableName,
                        PROGRAM_ID_COLUMN_NAME,
                        columnName))){
            pstmt.setInt(1, programID);
            pstmt.setInt(2, key);
            return containsCountResult(pstmt.executeQuery());
        }
    }
    /**
     * 
     * @param key
     * @param columnName
     * @param tableName
     * @return 
     */
    private boolean containsKey(int key, String columnName, String tableName){
        try{
            return containsKeySQL(key,columnName,tableName);
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    protected Set<Integer> getListIDSet(){
        return getKeySet(LIST_ID_COLUMN_NAME,LIST_SETTINGS_TABLE_NAME);
    }
    @Override
    protected int getListIDSize(){
        return getKeyCount(LIST_ID_COLUMN_NAME,LIST_SETTINGS_TABLE_NAME);
    }
    @Override
    protected boolean containsListID(int listID){
        return containsKey(listID,LIST_ID_COLUMN_NAME,LIST_SETTINGS_TABLE_NAME);
    }
    @Override
    protected  Set<Integer> getListTypeSet(){
        return getKeySet(LIST_TYPE_COLUMN_NAME,LIST_TYPE_SETTINGS_TABLE_NAME);
    }
    @Override
    protected int getListTypeSize(){
        return getKeyCount(LIST_TYPE_COLUMN_NAME,LIST_TYPE_SETTINGS_TABLE_NAME);
    }
    @Override
    protected boolean containsListType(int listType){
        return containsKey(listType,LIST_TYPE_COLUMN_NAME,LIST_TYPE_SETTINGS_TABLE_NAME);
    }
    /**
     * 
     * @param tableName
     * @param keyColumnName
     * @param valueColumnName
     * @param key
     * @return
     * @throws SQLException 
     */
    protected PreparedStatement createSetStatement(String tableName, 
            String keyColumnName, String valueColumnName, int key) 
            throws SQLException{
        PreparedStatement pstmt = conn.prepareStatement(String.format(
                (containsKeySQL(key,tableName,keyColumnName))?
                        UPDATE_VALUE_TEMPLATE:INSERT_VALUE_TEMPLATE,
                    tableName,
                    valueColumnName,
                    PROGRAM_ID_COLUMN_NAME,
                    keyColumnName));
        pstmt.setInt(2, programID);
        pstmt.setInt(3, key);
        return pstmt;
    }
    /**
     * 
     * @param tableName
     * @param keyColumnName
     * @param valueColumnName
     * @param key
     * @return
     * @throws SQLException 
     */
    protected PreparedStatement createGetStatement(String tableName, 
            String keyColumnName, String valueColumnName, int key) 
            throws SQLException{
        PreparedStatement pstmt = conn.prepareStatement(String.format(
                SELECT_VALUE_TEMPLATE,
                    valueColumnName,
                    tableName,
                    PROGRAM_ID_COLUMN_NAME,
                    keyColumnName));
        pstmt.setInt(1, programID);
        pstmt.setInt(2, key);
        return pstmt;
    }
    @Override
    public void setSelectedLinkID(int listID, Long value) {
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "setSelectedLinkID",new Object[]{listID,value,getProgramID()});
        if (!containsListID(listID) && value == null){
            LinkManager.getLogger().exiting(this.getClass().getName(), 
                    "setSelectedLinkID");
            return;
        }
        try(PreparedStatement pstmt = createSetStatement(LIST_SETTINGS_TABLE_NAME,
                LIST_ID_COLUMN_NAME,LINK_ID_COLUMN_NAME,listID)){
            setParameter(pstmt,1,value);
            pstmt.executeUpdate();
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        LinkManager.getLogger().exiting(this.getClass().getName(), 
            "setSelectedLinkID");
    }
    @Override
    public Long getSelectedLinkID(int listID) {
        try(PreparedStatement pstmt = createGetStatement(LIST_SETTINGS_TABLE_NAME,
                LIST_ID_COLUMN_NAME,LINK_ID_COLUMN_NAME,listID)){
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                long value = rs.getLong(LINK_ID_COLUMN_NAME);
                if (!rs.wasNull())
                    return value;
            }
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
        return null;
    }
    @Override
    public void setSelectedLinkVisible(int listID, Boolean value) {
        if (!containsListID(listID) && value == null)
            return;
        try(PreparedStatement pstmt = createSetStatement(LIST_SETTINGS_TABLE_NAME,
                LIST_ID_COLUMN_NAME,SELECTION_IS_VISIBLE_COLUMN_NAME,listID)){
            setParameter(pstmt,1,value);
            pstmt.executeUpdate();
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    public Boolean isSelectedLinkVisible(int listID) {
        try(PreparedStatement pstmt = createGetStatement(LIST_SETTINGS_TABLE_NAME,
                LIST_ID_COLUMN_NAME,SELECTION_IS_VISIBLE_COLUMN_NAME,listID)){
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                Boolean value = rs.getBoolean(SELECTION_IS_VISIBLE_COLUMN_NAME);
                if (!rs.wasNull())
                    return value;
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        return null;
    }
    /**
     * 
     * @param listID
     * @param value
     * @param columnName
     */
    private void setSelectionInteger(int listID, Integer value, String columnName) {
        if (!containsListID(listID) && value == null)
            return;
        try(PreparedStatement pstmt = createSetStatement(LIST_SETTINGS_TABLE_NAME,
                LIST_ID_COLUMN_NAME,columnName,listID)){
            setParameter(pstmt,1,value);
            pstmt.executeUpdate();
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    private Integer getSelectionInteger(int listID, String columnName){
        try(PreparedStatement pstmt = createGetStatement(LIST_SETTINGS_TABLE_NAME,
                LIST_ID_COLUMN_NAME,columnName,listID)){
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                int value = rs.getInt(columnName);
                if (!rs.wasNull())
                    return value;
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        return null;
    }
    @Override
    public void setFirstVisibleIndex(int listID, Integer value) {
        setSelectionInteger(listID,value,FIRST_VISIBLE_INDEX_COLUMN_NAME);
    }
    @Override
    public Integer getFirstVisibleIndex(int listID) {
        return getSelectionInteger(listID,FIRST_VISIBLE_INDEX_COLUMN_NAME);
    }
    @Override
    public void setLastVisibleIndex(int listID, Integer value) {
        setSelectionInteger(listID,value,LAST_VISIBLE_INDEX_COLUMN_NAME);
    }
    @Override
    public Integer getLastVisibleIndex(int listID) {
        return getSelectionInteger(listID,LAST_VISIBLE_INDEX_COLUMN_NAME);
    }
    @Override
    public void setVisibleRect(int listID, Rectangle value) {
        if (!containsListID(listID) && value == null)
            return;
        try(PreparedStatement pstmt = createSetStatement(LIST_SETTINGS_TABLE_NAME,
                LIST_ID_COLUMN_NAME,VISIBLE_RECTANGLE_COLUMN_NAME,listID)){
            setParameter(pstmt,1,value);
            pstmt.executeUpdate();
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    public Rectangle getVisibleRect(int listID, Rectangle defaultValue) {
        try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                "SELECT %s FROM %s WHERE %s = ? AND %s = ?",
                    VISIBLE_RECTANGLE_COLUMN_NAME,
                    LIST_SETTINGS_TABLE_NAME,
                    PROGRAM_ID_COLUMN_NAME,
                    LIST_ID_COLUMN_NAME))){
            pstmt.setInt(1, programID);
            pstmt.setInt(2, listID);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                byte[] bytes = rs.getBytes(VISIBLE_RECTANGLE_COLUMN_NAME);
                if (!rs.wasNull())
                    return ConfigUtilities.rectangleFromByteArray(bytes);
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        return null;
    }
    @Override
    public void setVisibleSection(int listID, Boolean isVisible, 
            Integer firstIndex, Integer lastIndex, Rectangle visibleRect){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "setVisibleSection", new Object[]{listID,isVisible,firstIndex,
                    lastIndex,visibleRect});
            // If the first visible index is negative
        if (firstIndex < 0)
            firstIndex = null;
            // If the last visible index is negative
        if (lastIndex < 0)
            lastIndex = null;
        try{
            boolean contains = containsListID(listID);
            if (contains || isVisible != null || firstIndex != null || 
                    lastIndex != null || visibleRect != null){
                try(PreparedStatement pstmt = conn.prepareStatement(String.format((contains)?
                                "UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ? AND %s = ?":
                                "INSERT INTO %s(%s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            LIST_SETTINGS_TABLE_NAME,
                            SELECTION_IS_VISIBLE_COLUMN_NAME,
                            FIRST_VISIBLE_INDEX_COLUMN_NAME,
                            LAST_VISIBLE_INDEX_COLUMN_NAME,
                            VISIBLE_RECTANGLE_COLUMN_NAME,
                            PROGRAM_ID_COLUMN_NAME,
                            LIST_ID_COLUMN_NAME))){
                    pstmt.setBoolean(1, isVisible);
                    setParameter(pstmt,2,firstIndex);
                    setParameter(pstmt,3,lastIndex);
                    setParameter(pstmt,4,visibleRect);
                    pstmt.setInt(5, programID);
                    pstmt.setInt(6, listID);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "setVisibleSection");
    }
    @Override
    public void setListSettings(int listID, Long linkID, Boolean isVisible, 
            Integer firstIndex, Integer lastIndex, Rectangle visibleRect){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "setListSettings", new Object[]{listID,linkID,isVisible,
                firstIndex,lastIndex,visibleRect});
            // If the first visible index is negative
        if (firstIndex < 0)
            firstIndex = null;
            // If the last visible index is negative
        if (lastIndex < 0)
            lastIndex = null;
        try{
            boolean contains = containsListID(listID);
            if (contains || linkID != null || isVisible != null || 
                    firstIndex != null || lastIndex != null || visibleRect != null){
                try(PreparedStatement pstmt = conn.prepareStatement(String.format((contains)?
                                "UPDATE %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ? WHERE %s = ? AND %s = ?":
                                "INSERT INTO %s(%s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?)",
                            LIST_SETTINGS_TABLE_NAME,
                            LINK_ID_COLUMN_NAME,
                            SELECTION_IS_VISIBLE_COLUMN_NAME,
                            FIRST_VISIBLE_INDEX_COLUMN_NAME,
                            LAST_VISIBLE_INDEX_COLUMN_NAME,
                            VISIBLE_RECTANGLE_COLUMN_NAME,
                            PROGRAM_ID_COLUMN_NAME,
                            LIST_ID_COLUMN_NAME))){
                    setParameter(pstmt,1,linkID);
                    setParameter(pstmt,2,isVisible);
                    setParameter(pstmt,3,firstIndex);
                    setParameter(pstmt,4,lastIndex);
                    setParameter(pstmt,5,visibleRect);
                    pstmt.setInt(6, programID);
                    pstmt.setInt(7, listID);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "setListSettings");
    }
    @Override
    public boolean removeListSettings(int listID) {
            // Prepare a statement to remove the entry with the given program ID 
            // and listID
        try (PreparedStatement pstmt = conn.prepareStatement(String.format(
                "DELETE FROM %s WHERE %s = ? AND %s = ?", 
                        LIST_SETTINGS_TABLE_NAME,
                        PROGRAM_ID_COLUMN_NAME,
                        LIST_ID_COLUMN_NAME))) {
                // Set the program ID to remove for
            pstmt.setInt(1, programID);
            pstmt.setInt(2, listID);
                // Update the database
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    public boolean removeListSettings(Collection<Integer> listIDs) {
        try{    // Get the current state of the auto-commit
            boolean autoCommit = conn.getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            conn.setAutoCommit(false);
            boolean modified = DatabaseLinksListSettings.super.removeListSettings(listIDs);
                // Commit the changes to the database
            conn.commit();
                // Restore the auto-commit back to what it was set to before
            conn.setAutoCommit(autoCommit);
            return modified;
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    public void clearListSettings(){
            // Prepare a statement to remove the entries with the given program ID 
        try (PreparedStatement pstmt = conn.prepareStatement(String.format(
                "DELETE FROM %s WHERE %s = ?", 
                        LIST_SETTINGS_TABLE_NAME,
                        PROGRAM_ID_COLUMN_NAME))) {
                // Set the program ID to remove for
            pstmt.setInt(1, programID);
                // Update the database
            pstmt.executeUpdate();
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    public void setSelectedListID(int listType, Integer listID) {
        try{
            if (listID == null){
                try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                        "DELETE FROM %s WHERE %s = ? AND %s = ?", 
                            LIST_TYPE_SETTINGS_TABLE_NAME,
                            PROGRAM_ID_COLUMN_NAME,
                            LIST_TYPE_COLUMN_NAME))){
                    pstmt.setInt(1, programID);
                    pstmt.setInt(2, listType);
                    pstmt.executeUpdate();
                }
            } else{
                try(PreparedStatement pstmt = createSetStatement(LIST_TYPE_SETTINGS_TABLE_NAME,
                        LIST_TYPE_COLUMN_NAME,LIST_ID_COLUMN_NAME,listType)){
                    setParameter(pstmt,1,listID);
                    pstmt.executeUpdate();
                }
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    public Integer getSelectedListID(int listType) {
        try(PreparedStatement pstmt = createGetStatement(LIST_TYPE_SETTINGS_TABLE_NAME,
                LIST_TYPE_COLUMN_NAME,LIST_ID_COLUMN_NAME,listType)){
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                int value = rs.getInt(LIST_ID_COLUMN_NAME);
                if (!rs.wasNull())
                    return value;
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        return null;
    }
    @Override
    public boolean removeSelectedTab(int listType) {
            // Prepare a statement to remove the entry with the given program ID 
            // and list type
        try (PreparedStatement pstmt = conn.prepareStatement(String.format(
                "DELETE FROM %s WHERE %s = ? AND %s = ?", 
                        LIST_TYPE_SETTINGS_TABLE_NAME,
                        PROGRAM_ID_COLUMN_NAME,
                        LIST_TYPE_COLUMN_NAME))) {
                // Set the program ID to remove for
            pstmt.setInt(1, programID);
            pstmt.setInt(2, listType);
                // Update the database
            return pstmt.executeUpdate() > 0;
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    public void clearSelectedTabs() {
            // Prepare a statement to remove the entries with the given program ID 
        try (PreparedStatement pstmt = conn.prepareStatement(String.format(
                "DELETE FROM %s WHERE %s = ?", 
                        LIST_TYPE_SETTINGS_TABLE_NAME,
                        PROGRAM_ID_COLUMN_NAME))) {
                // Set the program ID to remove for
            pstmt.setInt(1, programID);
                // Update the database
            pstmt.executeUpdate();
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    @Override
    public Map<Integer, Long> getSelectedLinkIDMap() {
        if (selLinkIDMap == null){
            selLinkIDMap = new ListConfigDataMap<>(){
                @Override
                protected Long getValue(int key) {
                    return getSelectedLinkID(key);
                }
                @Override
                protected void putValue(int key, Long value) {
                    setSelectedLinkID(key,value);
                }
                @Override
                protected Set<Integer> getKeys() {
                    return removeUnusedKeys(getListIDs());
                }
            };
        }
        return selLinkIDMap;
    }
}
