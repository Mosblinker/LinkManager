/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.*;
import java.util.*;
import java.util.function.BiConsumer;
import static manager.database.LinkDatabaseConnection.*;
import sql.*;
import sql.util.*;

/**
 *
 * @author Milo Steier
 */
class LinkMapImpl extends AbstractQueryRowMap<Long,String> implements LinkMap {
    /**
     * These are the conditions to use to get all the linkIDs that have the same 
     * link, excluding the one that will be used in the end. This is used when 
     * removing the duplicate links and is appended to the filter for the update 
     * and delete queries. The parameters for a prepared statement are as 
     * follows: 
     * <ol>
     *  <li>(String) The link to get the duplicates of.</li>
     *  <li>(Integer) The linkID for the link that will remain after the 
     *      duplicates have been removed. In other words, this is the linkID 
     *      that will be kept.</li>
     * </ol>
     */
    private static final String DUPLICATE_LINK_ID_QUERY_CONDITIONS = String.format(
            "%s IN (SELECT %s FROM %s WHERE %s = ? AND %s != ?)", 
                LINK_ID_COLUMN_NAME,
                LINK_ID_COLUMN_NAME,
                FULL_LINK_VIEW_NAME,
                LINK_URL_COLUMN_NAME,
                LINK_ID_COLUMN_NAME);
    /**
     * 
     */
    protected static final int LINK_ADDING_AUTO_COMMIT = 10000;
    /**
     * 
     */
    protected static final int LINK_REMOVE_UNUSED_SPLIT = 1000;
    /**
     * This constructs a LinkMapImpl with the given connection to the database
     * @param conn The connection to the database (cannot be null).
     */
    public LinkMapImpl(LinkDatabaseConnection conn){
        super(conn);
    }
    /**
     * 
     */
    private InverseMap inverseMap = null;
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean containsKeySQL(Object key) throws SQLException {
            // If the given key is null or not a long
        if (key == null || !(key instanceof Long))
            return false;
        return getConnection().containsLinkID((long)key);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean containsValueSQL(Object value) throws SQLException {
            // If the given value is null or not a String
        if (value == null || !(value instanceof String))
            return false;
            // Prepare a statement to check for the presense of the given value
        try(PreparedStatement pstmt = getConnection().prepareContainsStatement(
                getDataViewName(),
                getKeyColumn(),
                getValueColumn())){
            setPreparedValue(pstmt,1,(String)value);
            return containsCountResult(pstmt.executeQuery());
        }
    }

    private void deleteSQL(Long linkID) throws SQLException {
            // Prepare a statement to remove the entry from the links table
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                String.format("DELETE FROM %s WHERE %s = ?", 
                        LINK_TABLE_NAME,
                        LINK_ID_COLUMN_NAME))) {
                // Set the key to remove
            pstmt.setLong(1, linkID);
                // Update the database
            pstmt.executeUpdate();
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String removeSQL(Object key) throws SQLException {
            // If the given key is null or not a long
        if (key == null || !(key instanceof Long))
            return null;
            // Get the key as a long
        Long linkID = (Long)key;
            // Get the old value
        String value = getSQL(linkID);
        // TODO: Remove value from the list data table and shift the indexes accordingly?
            // Remove the value from the table
        deleteSQL(linkID);
        return value;
    }
    /**
     * 
     * @param prefixID
     * @param suffix
     * @return 
     * @throws SQLException
     */
    private Long insertSQL(int prefixID, String suffix) throws SQLException {
            // This is the listID of the link that just was added
        Long linkID;
            // Prepare a statement to insert the link into the link table
        try (PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                "INSERT INTO %s(%s, %s) VALUES (?, ?)", 
                        LINK_TABLE_NAME,
                        PREFIX_ID_COLUMN_NAME,
                        LINK_URL_COLUMN_NAME))) {
                // Set the prefixID for the link's prefix
            pstmt.setInt(1, prefixID);
                // Set the link's suffix
            pstmt.setString(2, suffix);
                // Update the database
            pstmt.executeUpdate();
                // Get the key that was generated
            linkID = getGeneratedLongKey(pstmt);
        }
        return linkID;
    }
    /**
     * 
     * @param value
     * @param prefix
     * @return
     * @throws SQLException 
     */
    private Long insertSQL(String value, Map.Entry<Integer, String> prefix) 
            throws SQLException {
        return insertSQL(prefix.getKey(),value.substring(prefix.getValue().length()));
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Long addSQL(String value) throws SQLException {
            // Check if the link is null
        Objects.requireNonNull(value);
            // Get the entry for the longest prefix that matches the link
        Map.Entry<Integer, String> prefixEntry = getConnection().getPrefixMap().
                getLongestPrefixEntryFor(value);
            // This gets a set of linkIDs currently in this map for the given 
            // value
        Set<Long> existingIDs = new TreeSet<>(keySetFor(value));
            // Insert the link and get the linkID that was just added
        Long linkID = insertSQL(value,prefixEntry);
            // If the linkID of the added link was found, return it. Otherwise, 
            // find it and return the linkID for the added link
        return (linkID != null) ? linkID:getGeneratedKey(value,existingIDs);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean addAllSQL(Collection<? extends String> c)throws SQLException{
        return addAllSQL(c,null);
    }
    /**
     * 
     * @param c
     * @param observer
     * @return
     * @throws SQLException 
     */
    protected boolean addAllSQL(Collection<? extends String> c, 
            BiConsumer<Integer,Integer> observer) throws SQLException{
        if (c.isEmpty())
            return false;
            // If the given collection is not a set
        if (!(c instanceof Set))
            c = new LinkedHashSet<>(c);
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following database 
            // transactions to improve performance
        getConnection().setAutoCommit(false);
            // If an observer has been provided
        if (observer != null)
                // Set the progress to be indeterminate
            observer.accept(1, null);
            // Get the prefixes for the new links
        Map<String,Map.Entry<Integer,String>> prefixes = 
                getConnection().getPrefixMap().getLongestPrefixesFor(c);
            // If an observer has been provided
        if (observer != null)
                // Set the progress to not be indeterminate
            observer.accept(0, null);
        int size = size();      // Get the current size of the map
        int index = 0;          // The index for the current link
        for (String value : c){ // Go through the elements in the collection
                // Ensure that the value is not null
            Objects.requireNonNull(value);
                // Insert the value to the map
            insertSQL(value,prefixes.get(value));
            index++;
                // If an observer has been provided
            if (observer != null)
                observer.accept(index, c.size());
                // If we should commit the changes (prevents too many 
                // changes from being done all at once)
            if (index % LINK_ADDING_AUTO_COMMIT == 0){
                    // Commit the changes to the database
                getConnection().commit();
            }
        }   // If an observer has been provided
        if (observer != null)
                // Set the progress to be indeterminate
            observer.accept(1, null);
            // Commit the changes to the database
        getConnection().commit();
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
        return size != size();
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean addAll(Collection<? extends String> c, 
            BiConsumer<Integer,Integer> observer){
        try{
            return addAllSQL(c,observer);
        } catch (SQLException ex) {
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param c
     * @param observer
     * @return
     * @throws SQLException 
     */
    protected boolean addAllIfAbsentSQL(Collection<? extends String> c, 
            BiConsumer<Integer,Integer> observer) throws SQLException{
            // Turn the collection into a LinkedHashSet, so as to remove any 
            // duplicates (since every element will only be added once if at 
            // all) while also maintaining the order of the given collection. 
            // This also allows changes to be made to the collection without 
            // altering the original collection.
        c = new LinkedHashSet<>(c);
            // Create a set copy of the values in this map, so as to reduce 
            // calls to the underlying database while preparing the collection 
        Set<String> values = new HashSet<>(values());    // to add
            // Remove any elements shared between the given collection and this 
            // map's value set (since these elements would otherwise have been 
        c.removeAll(values);    // skipped)
            // If there are no elements remaining in the set to be added (the 
            // given collection was empty or only contained values already 
        if (c.isEmpty())    // present in this map)
            return false;
            // Try to add all the remaining elements to this map
        return addAll(c, observer);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean addAllIfAbsent(Collection<? extends String> c, 
            BiConsumer<Integer,Integer> observer){
        try{
            return addAllIfAbsentSQL(c,observer);
        } catch (SQLException ex) {
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getSQL(Object key) throws SQLException {
            // If the given key is null or not a long
        if (key == null || !(key instanceof Long))
            return null;
            // Prepare a statement to request the link with the given key
        try (PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                "SELECT %s FROM %s WHERE %s = ?", 
                        LINK_URL_COLUMN_NAME,
                        FULL_LINK_VIEW_NAME,
                        LINK_ID_COLUMN_NAME))) {
                // Set the key
            pstmt.setLong(1, (Long)key);
                // Query the database
            ResultSet rs = pstmt.executeQuery();
                // If the query had any results
            if (rs.next())
                return rs.getString(LINK_URL_COLUMN_NAME);
        }
        return null;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String putSQL(Long key, String value) throws SQLException {
            // Check if the key is null
        Objects.requireNonNull(key);
            // Check if the value is null
        Objects.requireNonNull(value);
            // Get the old value for the key
        String oldValue = getSQL(key);
            // If the new and old values are the same (there would be no 
        if (Objects.equals(oldValue, value))    // change)
            return oldValue;
            // Get the prefixID for the longest matching prefix for the value
        int prefixID = getConnection().getPrefixMap().getLongestPrefixIDFor(value);
            // Get the suffix for the value
        value = getConnection().getPrefixMap().getSuffix(value, prefixID);
            // If there was no value set for the given key
        if (oldValue == null){
                // Prepare a statement to insert the link into the database
            try (PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                    "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)", 
                            LINK_TABLE_NAME,
                            LINK_ID_COLUMN_NAME,
                            PREFIX_ID_COLUMN_NAME,
                            LINK_URL_COLUMN_NAME))){
                    // Set the linkID for the link
                pstmt.setLong(1, key);
                    // Set the prefixID for the prefix for the link
                pstmt.setInt(2, prefixID);
                    // Set the suffix for the link
                pstmt.setString(3, value);
                    // Update the database
                pstmt.executeUpdate();
            }
        } else // Update the link for the given key
            getConnection().updateLink(key,prefixID,value);
        return oldValue;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getTableName() {
        return LINK_TABLE_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getDataViewName(){
        return FULL_LINK_VIEW_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getKeyColumn() {
        return LINK_ID_COLUMN_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getValueColumn() {
        return LINK_URL_COLUMN_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getUsedTableName() {
        return LIST_DATA_TABLE_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void setPreparedKey(PreparedStatement pstmt, 
            int parameterIndex, Long key) throws SQLException {
        setParameter(pstmt,parameterIndex,key);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void setPreparedValue(PreparedStatement pstmt, 
            int parameterIndex, String value) throws SQLException {
        pstmt.setString(parameterIndex, value);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Long getKeyFromResults(ResultSet rs) throws SQLException {
            // Get the key from the results
        long key = rs.getLong(LINK_ID_COLUMN_NAME);
            // If the key was null, then return null. Otherwise, return the key
        return (rs.wasNull()) ? null : key;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getValueFromResults(ResultSet rs) throws SQLException {
        return rs.getString(LINK_URL_COLUMN_NAME);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getRemoveRangeConditions(boolean fromStart, boolean fromInclusive, 
            boolean toEnd, boolean toInclusive, boolean useValue, String value){
            // If this is not using the given value
        if (!useValue)
            return getRangeConditions(fromStart,fromInclusive,toEnd,toInclusive);
            // The conditions will need to check the full links view since the 
            // links table stores prefixIDs and suffixes. As such, the values in 
            // the links column would not match the value otherwise
        return String.format("%s IN (SELECT %s FROM %s WHERE %s)", 
                LINK_ID_COLUMN_NAME,
                LINK_ID_COLUMN_NAME,
                FULL_LINK_VIEW_NAME,
                    // Get the conditions for the full links view
                super.getRemoveRangeConditions(fromStart, fromInclusive, 
                        toEnd, toInclusive,useValue,value));
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void removeSQL(boolean fromStart, Long fromKey, boolean fromInclusive, 
            boolean toEnd, Long toKey, boolean toInclusive, 
            boolean useValue, String value)throws SQLException{
        super.removeSQL(fromStart, fromKey, fromInclusive, 
                toEnd, toKey, toInclusive, useValue, value);
        // TODO: Update the list data table accordingly?
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean removeDuplicateRowsSQL() throws SQLException{
            // TODO: Can the speed of this be improved?
            // This is a map to contain the first linkID and link of all the 
            // links with two or more entries in this map
        Map<Long, String> duplicateLinks = new TreeMap<>();
            // Get a prepared statement to search through the distinct link view 
            // for any links that occur more than once
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                String.format("SELECT %s, %s FROM %s WHERE %s > 1",
                        LINK_ID_COLUMN_NAME,
                        LINK_URL_COLUMN_NAME,
                        DISTINCT_LINK_VIEW_NAME,
                        LINK_COUNT_COLUMN_NAME))){
                // Go through the results of the query
            ResultSet rs = pstmt.executeQuery();
                // While there are still rows in the query
            while (rs.next()){
                    // Add the linkID and link from the current row
                duplicateLinks.put(getKeyFromResults(rs), 
                        getValueFromResults(rs));
            }
        }   // If there are no duplicate links
        if (duplicateLinks.isEmpty())
            return false;
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following database 
            // transactions to improve performance
        getConnection().setAutoCommit(false);
            // Go through the duplicated links
        for (Map.Entry<Long, String> entry : duplicateLinks.entrySet()){
                // Prepare a statement to set every instance of a duplicate's 
                // linkID in the list data table to the linkID that will be kept
            try(PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                    "UPDATE %s SET %s = ? WHERE %s", 
                        LIST_DATA_TABLE_NAME,
                        LINK_ID_COLUMN_NAME,
                        DUPLICATE_LINK_ID_QUERY_CONDITIONS))){
                    // Set the linkID that will be kept
                pstmt.setLong(1, entry.getKey());
                    // Set the link to find the duplicates of
                pstmt.setString(2, entry.getValue());
                    // Set the linkID that will not be changed (since it is the 
                    // one that will be kept)
                pstmt.setLong(3, entry.getKey());
                    // Update the database
                pstmt.executeUpdate();
            }   // A prepared statement to delete all but the kept instance of 
                // the link from the link table
            try(PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                    "DELETE FROM %s WHERE %s", 
                        LINK_TABLE_NAME,
                        DUPLICATE_LINK_ID_QUERY_CONDITIONS))){
                    // Set the link to find and remove the duplicates of
                pstmt.setString(1, entry.getValue());
                    // Set the linkID that will be kept
                pstmt.setLong(2, entry.getKey());
                    // Update the database
                pstmt.executeUpdate();
            }
        }
        getConnection().commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
        return true;
    }

    private void deleteListSQL(List<Long> linkIDs) throws SQLException{
        if (linkIDs.isEmpty())
            return;
        if (linkIDs.size() == 1){
            deleteSQL(linkIDs.get(0));
            return;
        }
        String idStr = "?, ".repeat(linkIDs.size());
        idStr = idStr.substring(0, idStr.length()-2);
//        String idStr = "";
//        for (Long temp : linkIDs)
//            idStr += temp + ", ";
        try(PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                "DELETE FROM %s WHERE %s IN (%s)",
                    LINK_TABLE_NAME,
                    LINK_ID_COLUMN_NAME,
                    idStr
                ))){
            for (int i = 0; i < linkIDs.size(); i++){
                setPreparedKey(pstmt,i+1,linkIDs.get(i));
            }
            pstmt.executeUpdate();
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean removeUnusedRowsSQL() throws SQLException{
        String condition = String.format("%s WHERE %s NOT IN (SELECT %s FROM %s)",
                LINK_TABLE_NAME,
                LINK_ID_COLUMN_NAME,
                LINK_ID_COLUMN_NAME,
                LIST_DATA_TABLE_NAME);
        ArrayList<Long> linkIDs = new ArrayList<>();
        try(PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                "SELECT %s FROM %s",
                LINK_ID_COLUMN_NAME,
                condition))){
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
            while (rs.next())
                linkIDs.add(rs.getLong(LINK_ID_COLUMN_NAME));
        }
        if (linkIDs.size() >= LINK_REMOVE_UNUSED_SPLIT){
            int size = size();
                // Get the current state of the auto-commit
            boolean autoCommit = getConnection().getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            getConnection().setAutoCommit(false);
            for (int index = 0; index < linkIDs.size(); index += LINK_REMOVE_UNUSED_SPLIT){
                deleteListSQL(linkIDs.subList(index, Math.min(linkIDs.size(),index+LINK_REMOVE_UNUSED_SPLIT)));
            }
//            for (Long linkID : linkIDs){
//                deleteSQL(linkID);
//            }
                // Commit the changes to the database
            getConnection().commit();
                // Restore the auto-commit back to what it was set to before
            getConnection().setAutoCommit(autoCommit);
            return size != size();
        } else {
            return super.removeUnusedRowsSQL();
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Long firstKeyForSQL(String value) throws SQLException{
        checkValue(value);  // Check the value
            // Prepare a statement to find the linkID of the given value in the 
            // distinct links view
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                String.format("SELECT %s FROM %s WHERE %s = ?", 
                        LINK_ID_COLUMN_NAME,
                        DISTINCT_LINK_VIEW_NAME,
                        LINK_URL_COLUMN_NAME))){
                // Set the value to search for
            setPreparedValue(pstmt,1,value);
                // Query the database
            ResultSet rs = pstmt.executeQuery();
                // If the query had any results
            if (rs.next())
                return getKeyFromResults(rs);
        }
        return null;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public Map<String, Long> inverse() {
            // If the inverse map has not been initialized yet
        if (inverseMap == null)
            inverseMap = new InverseMap();
        return inverseMap;
    }
    /**
     * This returns a {@code NavigableMap} containing all the links in the 
     * database that matches the given pattern. This is a helper function for 
     * the {@code getStartsWith}, {@code getEndsWith}, and {@code getContains} 
     * methods.
     * @param pattern The pattern to use to filter the links.
     * @return A {@code NavigableMap} with all the links matching the given 
     * pattern.
     * @throws UncheckedSQLException If a database error occurs.
     * @throws NullPointerException If the pattern is null.
     * @see #getStartsWith(java.lang.String, int) 
     * @see #getStartsWith(java.lang.String) 
     * @see #getEndsWith(java.lang.String) 
     * @see #getContains(java.lang.String) 
     */
    private NavigableMap<Long, String> getSearchMap(String pattern){
            // Make sure the pattern is not null
        Objects.requireNonNull(pattern);
            // A TreeMap to get the results of the search
        TreeMap<Long, String> map = new TreeMap<>();
            // Prepare a statement to search for links matching the pattern
        try(PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                "SELECT %s, %s FROM %s WHERE "+TEXT_SEARCH_TEMPLATE, 
                    LINK_ID_COLUMN_NAME,
                    LINK_URL_COLUMN_NAME,
                    FULL_LINK_VIEW_NAME,
                    LINK_URL_COLUMN_NAME,
                    "?"))){
                // Set the pattern to use
            pstmt.setString(1, pattern);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // While there are still rows from the results
            while(rs.next())
                map.put(getKeyFromResults(rs), getValueFromResults(rs));
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
        return map;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public NavigableMap<Long,String>getStartsWith(String prefix,int offset){
            // Make sure the prefix is not null
        Objects.requireNonNull(prefix);
            // If the offset is negative
        if (offset < 0)
            return new TreeMap<>();
            // Get a map containing the results of the database search for all 
            // the links that match the given prefix (offset by the given 
            // offset)
        NavigableMap<Long, String> map = getSearchMap("_".repeat(offset)+
                formatSearchQueryPattern(prefix)+"%");
            // Remove any values which don't start with the given prefix (the 
            // search was case insensitive, this makes it case sensitive)
        map.values().removeIf((String t) -> {
            return !t.startsWith(prefix, offset);
        });
        return map;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public NavigableMap<Long, String> getEndsWith(String suffix){
            // Get a map containing the results of the database search for all 
            // the links that match the given suffix
        NavigableMap<Long, String> map = getSearchMap("%"+
                formatSearchQueryPattern(suffix));
            // Remove any values which don't start with the given prefix (the 
            // search was case insensitive, this makes it case sensitive)
        map.values().removeIf((String t) -> {
            return !t.endsWith(suffix);
        });
        return map;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public NavigableMap<Long, String> getContains(String s){
            // Get a map containing the results of the database search for all 
            // the links that contain the given string
        NavigableMap<Long, String> map = getSearchMap("%"+
                formatSearchQueryPattern(s)+"%");
            // Remove any values which don't start with the given prefix (the 
            // search was case insensitive, this makes it case sensitive)
        map.values().removeIf((String t) -> {
            return !t.contains(s);
        });
        return map;
    }
    /**
     * This is the inverse map for this {@code LinkMap}. This uses the values of 
     * the parent map as the keys for this map and vice versa.
     */
    private class InverseMap extends AbstractMap<String,Long>{
        /**
         * This is the set containing the entries for this map. This is 
         * initially null and is initialized the first time it is requested.
         */
        private Set<Entry<String,Long>> entries = null;
        /**
         * This returns the parent {@code LinkMap} for this {@code InverseMap}.
         * @return The parent {@code LinkMap}.
         */
        protected LinkMap getParentMap(){
            return LinkMapImpl.this;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public boolean containsKey(Object key){
                // If the given key is not a String
            if (!(key instanceof String))
                return false;
                // Return whether the parent map contains the key as a value
            return getParentMap().containsValue((String)key);
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Long get(Object key){
                // If the given key is a String or null
            if ((key instanceof String) || key == null){
                    // Get the first key in the parent map for the given key
                return getParentMap().firstKeyFor((String)key);
            }
            return null;
        }
        /**
         * {@inheritDoc }
         */
        @Override
        public Set<Entry<String, Long>> entrySet() {
                // If the entry set has not been initialized yet
            if (entries == null)
                    // Create a new set for the entry set
                entries = new AbstractSet<>(){
                    @Override
                    public Iterator<Entry<String, Long>> iterator() {
                            // Prepare a statement to read the linkIDs and links 
                            // from the distinct links view
                        try(PreparedStatement pstmt = getConnection().prepareStatement(
                                String.format("SELECT %s, %s FROM %s",
                                        LINK_ID_COLUMN_NAME,
                                        LINK_URL_COLUMN_NAME,
                                        DISTINCT_LINK_VIEW_NAME))){
                               // Create a linked hash set to act as a cache
                            LinkedHashSet<Entry<String, Long>> cache = 
                                    new LinkedHashSet<>();
                               // Get the results of the query
                            ResultSet rs = pstmt.executeQuery();
                                // While there are still rows in the results
                            while(rs.next())
                                    // Add the current row to the cache
                                cache.add(new AbstractMap.SimpleImmutableEntry<>(
                                        rs.getString(LINK_URL_COLUMN_NAME),
                                        rs.getLong(LINK_ID_COLUMN_NAME)
                                ));
                                // Return an iterator to go over the cache
                            return new CacheSetIterator<>(cache){
                                @Override
                                protected void remove(Entry<String, Long> value) {
                                    removeInverse(value.getKey(),value.getValue());
                                }
                            };
                        } catch (SQLException ex) {
                            appendWarning(ex);
                            return Collections.emptyIterator();
                        }
                    }
                    @Override
                    public int size() {
                        try {   //Return the size of the distinct links view
                            return getConnection().getTableSize(
                                    DISTINCT_LINK_VIEW_NAME, 
                                    LINK_ID_COLUMN_NAME);
                        } catch (SQLException ex) {
                            appendWarning(ex);
                        }
                        return 0;
                    }
                };
            return entries;
        }
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     * @throws SQLException 
     */
    protected int removeInverseSQL(String key, Long value)throws SQLException {
        // TODO: Remove values from the list data table and shift the indexes accordingly?
//            try (PreparedStatement pstmt = getConnection().prepareStatement(
//                    String.format("DELETE FROM %s WHERE %s = ?", 
//                            LINK_TABLE_NAME,
//                            LINK_URL_COLUMN_NAME))) {
//                pstmt.setString(1, key);
//                pstmt.executeUpdate();
//                return pstmt.getUpdateCount();
//            }
        throw new UnsupportedOperationException("remove");
    }
    /**
     * 
     * @param key
     * @param value 
     * @return 
     */
    protected int removeInverse(String key, Long value){
        try {
            return removeInverseSQL(key, value);
        } catch (SQLException ex) {
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
}
