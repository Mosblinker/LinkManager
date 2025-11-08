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
class DatabaseLinksListSettingsImpl implements DatabaseLinksListSettings{
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
     * This is the template for deleting a key from the tables.
     */
    private static final String DELETE_KEY_TEMPLATE = 
            "DELETE FROM %s WHERE %s = ? AND %s = ?";
    /**
     * This is the template for deleting all the keys from the table.
     */
    private static final String CLEAR_KEYS_TEMPLATE = 
            "DELETE FROM %s WHERE %s = ?";
    /**
     * The connection to the database.
     */
    private final LinkDatabaseConnection conn;
    /**
     * 
     */
    private final int programID;
    /**
     * 
     */
    private Set<Integer> listIDs = null;
    /**
     * 
     */
    private Set<Integer> listTypes = null;
    /**
     * This is initially 
     * null and is initialized when first used.
     */
    private Map<Integer, Long> selLinkIDMap = null;
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
     * This is a map view of the visible rectangles for the lists. This is 
     * initially null and is initialized when first used.
     */
    private Map<Integer, Rectangle> visRectMap = null;
    /**
     * This is a map view of the current tab listIDs. This is initially null 
     * and is initialized when first used.
     */
    private Map<Integer, Integer> currTabIDMap = null;
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
    @Override
    public Set<Integer> getListIDs(){
        if (listIDs == null)
            listIDs = new KeyQuerySet(){
                @Override
                protected String getColumnName() {
                    return LIST_ID_COLUMN_NAME;
                }
                @Override
                protected String getTableName() {
                    return LIST_SETTINGS_TABLE_NAME;
                }
            };
        return listIDs;
    }
    @Override
    public Set<Integer> getListTypes(){
        if (listTypes == null)
            listTypes = new KeyQuerySet(){
                @Override
                protected String getColumnName() {
                    return LIST_TYPE_COLUMN_NAME;
                }
                @Override
                protected String getTableName() {
                    return LIST_TYPE_SETTINGS_TABLE_NAME;
                }
            };
        return listTypes;
    }
    /**
     * 
     * @param tableName
     * @param keyColumnName
     * @param valueColumnName
     * @param key
     * @param update
     * @return
     * @throws SQLException 
     */
    private PreparedStatement createSetStatement(String tableName, 
            String keyColumnName, String valueColumnName, int key, 
            boolean update) throws SQLException{
        PreparedStatement pstmt = conn.prepareStatement(String.format(
                (update)?UPDATE_VALUE_TEMPLATE:INSERT_VALUE_TEMPLATE,
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
    protected PreparedStatement createSetStatement(String tableName, 
            String keyColumnName, String valueColumnName, int key) 
            throws SQLException{
        return createSetStatement(tableName,keyColumnName,valueColumnName,key,
                containsKeySQL(key,keyColumnName,tableName));
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
        if (!getListIDs().contains(listID) && value == null){
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
    public Map<Integer, Long> getSelectedLinkIDMap() {
        if (selLinkIDMap == null){
            selLinkIDMap = new ListConfigDataQueryMap<>(){
                @Override
                protected String getTableName() {
                    return LIST_SETTINGS_TABLE_NAME;
                }
                @Override
                protected String getKeyColumnName() {
                    return LIST_ID_COLUMN_NAME;
                }
                @Override
                protected String getValueColumnName() {
                    return LINK_ID_COLUMN_NAME;
                }
                @Override
                protected Long getValue(ResultSet rs) throws SQLException {
                    return rs.getLong(getValueColumnName());
                }
                @Override
                protected void setValue(PreparedStatement pstmt, 
                        int parameterIndex, Long value) throws SQLException {
                    setParameter(pstmt,parameterIndex,value);
                }
            };
        }
        return selLinkIDMap;
    }
    @Override
    public Map<Integer, String> getSelectedLinkMap() {
        if (selLinkMap == null){
            selLinkMap = new ListConfigDataQueryMap<>(){
                @Override
                protected String getTableName() {
                    return LIST_SETTINGS_TABLE_NAME;
                }
                @Override
                protected String getKeyColumnName() {
                    return LIST_ID_COLUMN_NAME;
                }
                @Override
                protected String getValueColumnName() {
                    return LINK_ID_COLUMN_NAME;
                }
                @Override
                protected String getValue(ResultSet rs) throws SQLException {
                    return conn.getLinkMap().get(rs.getLong(getValueColumnName()));
                }
                @Override
                protected void setValue(PreparedStatement pstmt, 
                        int parameterIndex, String value) throws SQLException {
                    Long linkID = null;
                    if (value != null)
                        linkID = conn.getLinkMap().inverse().get(value);
                    setParameter(pstmt,parameterIndex,linkID);
                }
                @Override
                protected Set<Entry<Integer, String>> entryCacheSet() throws SQLException {
                    Set<Entry<Integer, String>> cache = new LinkedHashSet<>();
                    Map<Long,String> linkMap = new HashMap<>(conn.getLinkMap());
                    try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                            "SELECT %s, %s FROM %s WHERE %s = ? AND %s IS NOT NULL", 
                                getKeyColumnName(),
                                getValueColumnName(),
                                getTableName(),
                                PROGRAM_ID_COLUMN_NAME,
                                getValueColumnName()))){
                            // Get the results of the query
                        ResultSet rs = pstmt.executeQuery();
                        while (rs.next()){
                            cache.add(new AbstractMap.SimpleImmutableEntry<>(
                                    rs.getInt(getKeyColumnName()),
                                    linkMap.get(rs.getLong(getValueColumnName()))));
                        }
                    }
                    return cache;
                } 
            };
        }
        return selLinkMap;
    }
    @Override
    public void setSelectedLinkVisible(int listID, Boolean value) {
        if (!getListIDs().contains(listID) && value == null)
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
    @Override
    public Map<Integer, Boolean> getSelectedLinkVisibleMap() {
        if (selLinkVisMap == null){
            selLinkVisMap = new ListConfigDataQueryMap<>(){
                @Override
                protected String getTableName() {
                    return LIST_SETTINGS_TABLE_NAME;
                }
                @Override
                protected String getKeyColumnName() {
                    return LIST_ID_COLUMN_NAME;
                }
                @Override
                protected String getValueColumnName() {
                    return SELECTION_IS_VISIBLE_COLUMN_NAME;
                }
                @Override
                protected Boolean getValue(ResultSet rs) throws SQLException {
                    return rs.getBoolean(getValueColumnName());
                }
                @Override
                protected void setValue(PreparedStatement pstmt, 
                        int parameterIndex, Boolean value) throws SQLException {
                    setParameter(pstmt,parameterIndex,value);
                }
            };
        }
        return selLinkVisMap;
    }
    /**
     * 
     * @param listID
     * @param value
     * @param columnName
     */
    private void setSetttingInteger(int listID, Integer value, String columnName) {
        if (!getListIDs().contains(listID) && value == null)
            return;
        try(PreparedStatement pstmt = createSetStatement(LIST_SETTINGS_TABLE_NAME,
                LIST_ID_COLUMN_NAME,columnName,listID)){
            setParameter(pstmt,1,value);
            pstmt.executeUpdate();
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param listID
     * @param columnName
     * @return 
     */
    private Integer getSettingInteger(int listID, String columnName){
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
        setSetttingInteger(listID,value,FIRST_VISIBLE_INDEX_COLUMN_NAME);
    }
    @Override
    public Integer getFirstVisibleIndex(int listID) {
        return getSettingInteger(listID,FIRST_VISIBLE_INDEX_COLUMN_NAME);
    }
    @Override
    public Map<Integer, Integer> getFirstVisibleIndexMap() {
        if (firstVisIndexMap == null){
            firstVisIndexMap = new ListConfigIntegerQueryMap(){
                @Override
                protected String getTableName() {
                    return LIST_SETTINGS_TABLE_NAME;
                }
                @Override
                protected String getKeyColumnName() {
                    return LIST_ID_COLUMN_NAME;
                }
                @Override
                protected String getValueColumnName() {
                    return FIRST_VISIBLE_INDEX_COLUMN_NAME;
                }
            };
        }
        return firstVisIndexMap;
    }
    @Override
    public void setLastVisibleIndex(int listID, Integer value) {
        setSetttingInteger(listID,value,LAST_VISIBLE_INDEX_COLUMN_NAME);
    }
    @Override
    public Integer getLastVisibleIndex(int listID) {
        return getSettingInteger(listID,LAST_VISIBLE_INDEX_COLUMN_NAME);
    }
    @Override
    public Map<Integer, Integer> getLastVisibleIndexMap() {
        if (lastVisIndexMap == null){
            lastVisIndexMap = new ListConfigIntegerQueryMap(){
                @Override
                protected String getTableName() {
                    return LIST_SETTINGS_TABLE_NAME;
                }
                @Override
                protected String getKeyColumnName() {
                    return LIST_ID_COLUMN_NAME;
                }
                @Override
                protected String getValueColumnName() {
                    return LAST_VISIBLE_INDEX_COLUMN_NAME;
                }
            };
        }
        return lastVisIndexMap;
    }
    @Override
    public void setVisibleRect(int listID, Rectangle value) {
        if (!getListIDs().contains(listID) && value == null)
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
                return ConfigUtilities.rectangleFromByteArray(
                        rs.getBytes(VISIBLE_RECTANGLE_COLUMN_NAME));
            }
        } catch (SQLException ex){
            throw new UncheckedSQLException(ex);
        }
        return null;
    }
    @Override
    public Map<Integer, Rectangle> getVisibleRectMap() {
        if (visRectMap == null){
            visRectMap = new ListConfigDataQueryMap<>(){
                @Override
                protected String getTableName() {
                    return LIST_SETTINGS_TABLE_NAME;
                }
                @Override
                protected String getKeyColumnName() {
                    return LIST_ID_COLUMN_NAME;
                }
                @Override
                protected String getValueColumnName() {
                    return VISIBLE_RECTANGLE_COLUMN_NAME;
                }
                @Override
                protected Rectangle getValue(ResultSet rs) throws SQLException {
                    return ConfigUtilities.rectangleFromByteArray(
                            rs.getBytes(getValueColumnName()));
                }
                @Override
                protected void setValue(PreparedStatement pstmt, 
                        int parameterIndex, Rectangle value) throws SQLException {
                    setParameter(pstmt,parameterIndex,value);
                }
            };
        }
        return visRectMap;
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
            boolean contains = getListIDs().contains(listID);
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
            boolean contains = getListIDs().contains(listID);
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
        return getListIDs().remove(listID);
    }
    @Override
    public boolean removeListSettings(Collection<Integer> listIDs) {
        return getListIDs().removeAll(listIDs);
    }
    @Override
    public void clearListSettings(){
        getListIDs().clear();
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
    public Map<Integer, Integer> getSelectedListIDMap() {
        if (currTabIDMap == null){
            currTabIDMap = new ListConfigIntegerQueryMap(){
                @Override
                protected String getTableName() {
                    return LIST_TYPE_SETTINGS_TABLE_NAME;
                }
                @Override
                protected String getKeyColumnName() {
                    return LIST_TYPE_COLUMN_NAME;
                }
                @Override
                protected String getValueColumnName() {
                    return LIST_ID_COLUMN_NAME;
                }
            };
        }
        return currTabIDMap;
    }
    @Override
    public boolean removeSelectedTab(int listType) {
        return getListTypes().remove(listType);
    }
    @Override
    public void clearSelectedTabs() {
        getListTypes().clear();
    }
    /**
     * 
     * @param <V> 
     */
    private abstract class ListConfigDataQueryMap<V> extends AbstractQueryMap<Integer,V>{
        /**
         * 
         */
        ListConfigDataQueryMap() {
            super(conn);
        }
        /**
         * 
         * @return 
         */
        protected abstract String getTableName();
        /**
         * 
         * @return 
         */
        protected abstract String getKeyColumnName();
        /**
         * 
         * @return 
         */
        protected abstract String getValueColumnName();
        /**
         * 
         * @param rs
         * @return
         * @throws SQLException 
         */
        protected abstract V getValue(ResultSet rs) throws SQLException;
        /**
         * 
         * @param pstmt
         * @param parameterIndex
         * @param value
         * @throws SQLException 
         */
        protected abstract void setValue(PreparedStatement pstmt,
                int parameterIndex, V value) throws SQLException;

        @Override
        protected boolean containsKeySQL(Object key) throws SQLException {
            if (!(key instanceof Integer))
                return false;
            try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                    TABLE_CONTAINS_QUERY_TEMPLATE+" AND %s = ? AND %s IS NOT NULL", 
                        PROGRAM_ID_COLUMN_NAME,
                        getTableName(),
                        PROGRAM_ID_COLUMN_NAME,
                        getKeyColumnName(),
                        getValueColumnName()))){
                pstmt.setInt(1, programID);
                pstmt.setInt(2, (Integer)key);
                return containsCountResult(pstmt.executeQuery());
            }
        }
        @Override
        protected V removeSQL(Object key) throws SQLException {
            V value = getSQL(key);
            if (value != null && key instanceof Integer){
                try(PreparedStatement pstmt = createSetStatement(getTableName(),
                        getKeyColumnName(),getValueColumnName(),(Integer)key)){
                    setValue(pstmt,1,null);
                    pstmt.executeUpdate();
                }
            }
            return value;
        }
        @Override
        protected V getSQL(Object key) throws SQLException {
            if (key instanceof Integer){
                try(PreparedStatement pstmt = createGetStatement(getTableName(),
                        getKeyColumnName(),getValueColumnName(),(Integer)key)){
                        // Get the results of the query
                    ResultSet rs = pstmt.executeQuery();
                    if (rs.next())
                        return getValue(rs);
                }
            }
            return null;
        }
        @Override
        protected V putSQL(Integer key, V value) throws SQLException {
            Objects.requireNonNull(key);
            Objects.requireNonNull(value);
            V old = getSQL(key);
            try(PreparedStatement pstmt = createSetStatement(getTableName(),
                    getKeyColumnName(),getValueColumnName(),key)){
                setValue(pstmt,1,value);
                pstmt.executeUpdate();
            }
            return old;
        }
        @Override
        protected Set<Entry<Integer, V>> entryCacheSet() throws SQLException {
            Set<Entry<Integer, V>> cache = new LinkedHashSet<>();
            try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                    "SELECT %s, %s FROM %s WHERE %s = ? AND %s IS NOT NULL", 
                        getKeyColumnName(),
                        getValueColumnName(),
                        getTableName(),
                        PROGRAM_ID_COLUMN_NAME,
                        getValueColumnName()))){
                    // Get the results of the query
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()){
                    cache.add(new AbstractMap.SimpleImmutableEntry<>(
                            rs.getInt(getKeyColumnName()),
                            getValue(rs)));
                }
            }
            return cache;
        }
        @Override
        protected int sizeSQL() throws SQLException {
            try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                    TABLE_SIZE_QUERY_TEMPLATE+" WHERE %s = ? AND %s IS NOT NULL",
                        "DISTINCT "+getKeyColumnName(),
                        getTableName(),
                        PROGRAM_ID_COLUMN_NAME,
                        getValueColumnName()))){
                pstmt.setInt(1, programID);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results from the query
                if (rs.next())
                    return rs.getInt(COUNT_COLUMN_NAME);
            }
            return 0;
        }
    }
    /**
     * 
     */
    private abstract class ListConfigIntegerQueryMap extends ListConfigDataQueryMap<Integer>{
        @Override
        protected Integer getValue(ResultSet rs) throws SQLException {
            return rs.getInt(getValueColumnName());
        }
        @Override
        protected void setValue(PreparedStatement pstmt, int parameterIndex, 
                Integer value) throws SQLException {
            setParameter(pstmt,parameterIndex,value);
        }
    }
    /**
     * 
     */
    private abstract class KeyQuerySet extends AbstractQuerySet<Integer>{
        /**
         * 
         */
        KeyQuerySet() {
            super(conn);
        }
        /**
         * 
         * @return 
         */
        protected abstract String getColumnName();
        /**
         * 
         * @return 
         */
        protected abstract String getTableName();
        @Override
        protected boolean containsSQL(Object o) throws SQLException {
            if (!(o instanceof Integer))
                return false;
            return containsKeySQL((Integer)o,getColumnName(),getTableName());
        }
        @Override
        protected boolean removeSQL(Object o) throws SQLException {
            if (o instanceof Integer){
                    // Prepare a statement to remove the entry with the given 
                    // program ID and key
                try (PreparedStatement pstmt = conn.prepareStatement(String.format(
                        DELETE_KEY_TEMPLATE, 
                            getTableName(),
                            PROGRAM_ID_COLUMN_NAME,
                            getColumnName()))) {
                        // Set the program ID to remove for
                    pstmt.setInt(1, programID);
                    pstmt.setInt(2, (Integer)o);
                        // Update the database
                    return pstmt.executeUpdate() > 0;
                }
            }
            return false;
        }
        @Override
        protected Set<Integer> valueCacheSet() throws SQLException {
            Set<Integer> keys = new TreeSet<>();
            try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                    "SELECT DISTINCT %s FROM %s WHERE %s = ?",
                        getColumnName(),
                        getTableName(),
                        PROGRAM_ID_COLUMN_NAME))){
                pstmt.setInt(1, programID);
                    // Get the results of the query
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()){
                    keys.add(rs.getInt(getColumnName()));
                }
            }
            return keys;
        }
        @Override
        protected int sizeSQL() throws SQLException {
                // Prepare a statement to count the unique instances of the key 
                // in the table
            try(PreparedStatement pstmt = conn.prepareStatement(String.format(
                    TABLE_SIZE_QUERY_TEMPLATE+" WHERE %s = ?", 
                        "DISTINCT "+getColumnName(),
                        getTableName(),
                        PROGRAM_ID_COLUMN_NAME))){
                pstmt.setInt(1, programID);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If there are any results from the query
                if (rs.next())
                    return rs.getInt(COUNT_COLUMN_NAME);
            }
            return 0;
        }
        @Override
        protected void clearSQL() throws SQLException {
                // Prepare a statement to remove the entries with the given program ID 
            try (PreparedStatement pstmt = conn.prepareStatement(String.format(
                    CLEAR_KEYS_TEMPLATE, 
                        getTableName(),
                        PROGRAM_ID_COLUMN_NAME))) {
                    // Set the program ID to remove for
                pstmt.setInt(1, programID);
                    // Update the database
                pstmt.executeUpdate();
            }
        }
    }
}
