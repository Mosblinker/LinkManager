/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.*;
import java.util.*;
import static manager.database.LinkDatabaseConnection.*;

/**
 *
 * @author Milo Steier
 * @param <K> The type of keys maintained by the map.
 * @param <V> The type of mapped values.
 */
abstract class AbstractQueryRowMap <K,V> extends AbstractSQLRowMap<K,V> {
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract LinkDatabaseConnection getConnection() throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract boolean containsKeySQL(Object key) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract boolean containsValueSQL(Object value) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract V removeSQL(Object key) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract K addSQL(V value) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract V getSQL(Object key) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract V putSQL(K key, V value) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected K addIfAbsentSQL(V value) throws SQLException{
            // Get the first key for the given value if there is one
        K key = firstKeyFor(value);
            // If a non-null key was found, return it. Otherwise, add the 
            // value to this map
        return (key != null) ? key : add(value);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean addAllSQL(Collection<? extends V> c)throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        getConnection().setAutoCommit(false);
            // Add all the elements in the given collection to this map
        boolean modified = super.addAllSQL(c);
            // Commit the changes to the database
        getConnection().commit();
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
        return modified;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void putAllSQL(Map<? extends K, ? extends V> m) 
            throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        getConnection().setAutoCommit(false);
            // Put all the entries in the given map into this map
        super.putAllSQL(m);
            // Commit the changes to the database
        getConnection().commit();
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
    }
    /**
     * This returns the name of the table in the database that this map is a 
     * view of.
     * @return The name of the table in the database.
     * @see #getDataViewName() 
     * @see #getKeyColumn() 
     * @see #getValueColumn() 
     */
    protected abstract String getTableName();
    /**
     * This returns the name of the view in the database used to get the 
     * values for this map. 
     * 
     * @implSpec The default implementation forwards the call to {@link 
     * #getTableName getTableName}.
     * 
     * @return The name of the view in the database used to get the values.
     * @see #getTableName() 
     * @see #getKeyColumn() 
     * @see #getValueColumn() 
     */
    protected String getDataViewName(){
        return getTableName();
    }
    /**
     * This returns the name of the column in the table in the database that 
     * stores the values used as this map's keys.
     * @return The name of the key column in the table.
     * @see #getTableName() 
     * @see #getDataViewName() 
     * @see #getValueColumn() 
     * @see #getKeyFromResults(ResultSet) 
     * @see #setPreparedKey(PreparedStatement, int, Object) 
     */
    protected abstract String getKeyColumn();
    /**
     * This returns the name of the column in the table in the database that 
     * stores the values for this map.
     * @return The name of the value column in the table.
     * @see #getTableName() 
     * @see #getDataViewName() 
     * @see #getKeyColumn() 
     * @see #getValueFromResults(ResultSet) 
     * @see #setPreparedValue(PreparedStatement, int, Object) 
     * @see #canValueBeNull() 
     */
    protected abstract String getValueColumn();
    /**
     * This returns whether this map can store null values. 
     * 
     * @implSpec The default implementation returns {@code false}, 
     * indicating that this map cannot store null values.
     * 
     * @return {@code true} if this map accepts null values, {@code false} 
     * otherwise.
     * @see #getValueColumn() 
     * @see #getValueFromResults(ResultSet) 
     * @see #setPreparedValue(PreparedStatement, int, Object)
     */
    protected boolean canValueBeNull(){
        return false;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void checkValue(V value){
            // If the value is null and this map does not accept null values
        if (!canValueBeNull() && value == null)
            throw new NullPointerException();
    }
    /**
     * This sets the designated parameter on the given prepared statement to 
     * the given key.
     * @param pstmt The prepared statement to set the designated parameter 
     * for.
     * @param parameterIndex The index for the parameter. This uses 1-based 
     * indexing, with the first parameter being 1.
     * @param key The key to use as the parameter value.
     * @throws SQLException If either {@code parameterIndex} does not 
     * correspond to a parameter marker in the SQL statement, a database 
     * access error occurs, or {@code pstmt} is closed.
     * @see #getKeyColumn() 
     * @see #getKeyFromResults(java.sql.ResultSet) 
     */
    protected abstract void setPreparedKey(PreparedStatement pstmt, 
            int parameterIndex, K key)throws SQLException;
    /**
     * This sets the designated parameter on the given prepared statement to 
     * the given value.
     * @param pstmt The prepared statement to set the designated parameter 
     * for.
     * @param parameterIndex The index for the parameter. This uses 1-based 
     * indexing, with the first parameter being 1.
     * @param value The value to use as the parameter value.
     * @throws SQLException If either {@code parameterIndex} does not 
     * correspond to a parameter marker in the SQL statement, a database 
     * access error occurs, or {@code pstmt} is closed.
     * @see #getValueColumn() 
     * @see #getValueFromResults(java.sql.ResultSet) 
     */
    protected abstract void setPreparedValue(PreparedStatement pstmt, 
            int parameterIndex, V value)throws SQLException;
    /**
     * 
     * @param rs The result set to get the key from.
     * @return
     * @throws SQLException 
     */
    protected abstract K getKeyFromResults(ResultSet rs) throws SQLException;
    /**
     * 
     * @param rs The result set to get the value from.
     * @return
     * @throws SQLException 
     */
    protected abstract V getValueFromResults(ResultSet rs) throws SQLException;
    /**
     * 
     * @param rs The result set to get the key and value from.
     * @return
     * @throws SQLException 
     */
    protected Entry<K, V> getEntryFromResults(ResultSet rs) throws SQLException{
        return new AbstractMap.SimpleImmutableEntry<>(
                getKeyFromResults(rs),getValueFromResults(rs));
    }
    /**
     * 
     * @param conditions The conditions for the query, or null.
     * @param descending Whether the results will be in ascending or 
     * descending order ({@code true} for descending, {@code false} for 
     * ascending).
     * @param limit The limit on the amount of rows that the query will 
     * return.
     * @return SELECT getKeyColumn(), getValueColumn() FROM getDataViewName() 
     * WHERE conditions ORDER BY getKeyColumn() (DESC) LIMIT limit
     */
    protected String getSortedQuery(String conditions, boolean descending, 
            Integer limit){
        return LinkDatabaseConnection.getSortedQuery(
                    // Get the keys and values
                getKeyColumn()+", "+getValueColumn(),
                    // Query the data view
                getDataViewName(),
                conditions,
                    // Sort by the key column
                getKeyColumn(), 
                descending,
                limit
        );
    }
    /**
     * 
     * @param inclusive
     * @param operator
     * @return getKeyColumn() operator ?
     */
    protected String getRangeConditions(boolean inclusive, String operator){
        if (inclusive)  // If the conditions are inclusive
            operator += "=";
        return getKeyColumn()+" "+operator+" ?";
    }
    /**
     * 
     * @param fromStart Whether the range should start at the start of this 
     * map.
     * @param fromInclusive Whether the range should be inclusive of the 
     * starting element.
     * @param toEnd Whether the range should end at the end of this map.
     * @param toInclusive Whether the range should be inclusive of the 
     * ending element.
     * @param useValue Whether the range should only contain keys that are 
     * mapped to {@code value}
     * @param value The value to filter the range.
     * @return {@code getValueColumn() = value AND getKeyColumn() >= ? AND getKeyColumn() <= ?}
     */
    protected String getRangeConditions(boolean fromStart,boolean fromInclusive, 
            boolean toEnd, boolean toInclusive, boolean useValue, V value){
            // If the range is the entire map and the range is not limited 
        if (fromStart && toEnd && !useValue)    // to the given value
            return null;
            // This gets the conditions to return. If the range is limited 
            // to the given value, then get the value conditions.
        String conditions = (useValue) ? getValueCondition(value) : "";
            // If the range is the entire map
        if (fromStart && toEnd)
            return conditions;
        conditions += " AND ";
            // If the range includes the start of this map but has a 
            // specified ending different from this map
        if (fromStart)
            return conditions+getRangeConditions(toInclusive,"<");
            // If the range includes the end of this map but has a 
            // specified start different from this map
        else if (toEnd)
            return conditions+getRangeConditions(fromInclusive,">");
        else    // If the range has a specified start and end
            return conditions+getRangeConditions(fromInclusive,">") + " AND " + 
                    getRangeConditions(toInclusive,"<");
    }
    /**
     * 
     * @param fromStart Whether the range should start at the start of this 
     * map.
     * @param fromInclusive Whether the range should be inclusive of the 
     * starting element.
     * @param toEnd Whether the range should end at the end of this map.
     * @param toInclusive Whether the range should be inclusive of the 
     * ending element.
     * @return {@code getKeyColumn() >= ? AND getKeyColumn() <= ?}
     */
    protected String getRangeConditions(boolean fromStart,boolean fromInclusive, 
            boolean toEnd, boolean toInclusive){
        return getRangeConditions(fromStart,fromInclusive,toEnd,toInclusive,
                false,null);
    }
    /**
     * 
     * @param comparison The comparison to use (negative for {@code keys < 
     * ?}, 0 for {@code keys = ?}, positive for {@code keys > ?}) 
     * @param inclusive Whether the query is inclusive
     * @param conditions
     * @return {@code SELECT getKeyColumn(), getValueColumn() FROM getDataViewName() 
     * WHERE getKeyColumn() </>/= ? AND conditions ORDER BY getKeyColumn() (DESC = comparison < 0) LIMIT 1}
     */
    protected String getNavigableQuery(int comparison, boolean inclusive, 
            String conditions){
            // This gets the operator to use
        String operator;
            // If the comparison is less than
        if (comparison < 0)
            operator = "<";
            // If the comparison is greater than
        else if (comparison > 0)
            operator = ">";
        else
            operator = "";
            // If the conditions are not null
        if (conditions != null)
            conditions = " AND " + conditions;
        else
            conditions = "";
        return getSortedQuery(getRangeConditions(inclusive,operator)+conditions,
                comparison < 0,1);
    }
    /**
     * 
     * @param comparison The comparison to use (negative for {@code keys < 
     * ?}, 0 for {@code keys = ?}, positive for {@code keys > ?}) 
     * @param inclusive Whether the query is inclusive
     * @return {@code SELECT getKeyColumn(), getValueColumn() FROM getDataViewName() 
     * WHERE getKeyColumn() </>/= ? ORDER BY getKeyColumn() (DESC = comparison < 0) LIMIT 1}
     */
    protected String getNavigableQuery(int comparison, boolean inclusive){
        return getNavigableQuery(comparison,inclusive,null);
    }
    /**
     * 
     * @param last Whether this should get the first or last entry
     * @return The first or last entry in this map, or null if this map is 
     * empty.
     * @throws SQLException 
     */
    protected Entry<K, V> getEndEntry(boolean last) throws SQLException{
            // Create a statement to get the entry
        try(Statement stmt = getConnection().createStatement()){
                // Query the database to get the entry
            ResultSet rs = stmt.executeQuery(getSortedQuery(null,last,1));
                // If there are any results from the query
            if (rs.next())
                return getEntryFromResults(rs);
        }
        return null;
    }
    /**
     * 
     * @param key The key 
     * @param comparison The comparison to use (negative for {@code keys < 
     * key}, 0 for {@code keys = key}, positive for {@code keys > key}) 
     * @param inclusive Whether the query is inclusive
     * @return
     * @throws SQLException 
     * @throws NullPointerException If the specified key is null and this 
     * map does not permit null keys.
     */
    protected Entry<K, V> getNavEntry(K key,int comparison,boolean inclusive) 
            throws SQLException{
        checkKey(key);  // Check the key
            // Prepare a statement to get the entry higher or lower than the 
            // given key
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                getNavigableQuery(comparison,inclusive))){
                // Set the key
            setPreparedKey(pstmt,1,key);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // If there are any results
            if (rs.next())
                return getEntryFromResults(rs);
        }
        return null;
    }
    /**
     * 
     * @param value
     * @return 
     */
    protected String getValueCondition(V value){
            // If the value is null, look for nulls in the column. 
            // Otherwise, look for values in the column
        return getValueColumn()+((value == null)?" IS NULL":" = ?");
    }
    /**
     * 
     * @param value
     * @param last
     * @return
     * @throws SQLException
     * @throws ClassCastException If the value is of an inappropriate type 
     * for this map
     * @throws NullPointerException If the given value is null and this map 
     * does not permit null values
     */
    protected K getEndKeyForValue(V value, boolean last) throws SQLException{
        checkValue(value);  // Check the value
            // Prepare a statement to get the first or last key for the 
            // given value
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                getSortedQuery(getValueCondition(value),last,1))){
                // If the given value is not null
            if (value != null)
                    // Set the value to search for
                setPreparedValue(pstmt,1,value);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // If there are any results
            if (rs.next())
                return getKeyFromResults(rs);
        }
        return null;
    }
    /**
     * 
     * @param value
     * @param key
     * @param comparison
     * @param inclusive
     * @return
     * @throws SQLException 
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map
     * @throws NullPointerException If the given key or value are null and 
     * this map does not permit null keys or values
     */
    protected K getNavKeyForValue(V value,K key,int comparison,
            boolean inclusive) throws SQLException{
            // Check the key and value
        checkKeyAndValue(key,value);
            // Prepare a statement to get the key mapped to the given value 
            // and that is either higher or lower than the given key
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                getNavigableQuery(comparison,inclusive,getValueCondition(value)))){
                // Set the key
            setPreparedKey(pstmt,1,key);
                // If the value is not null
            if (value != null)
                    // Set the value
                setPreparedValue(pstmt,2,value);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // If there are any results
            if (rs.next())
                return getKeyFromResults(rs);
        }
        return null;
    }
    /**
     * 
     * @param descending
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @return
     * @throws SQLException 
     */
    protected Set<Entry<K,V>> entryCacheSet(boolean descending,
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive) throws SQLException{
            // This is a set that will cache the entries from the map
        Set<Map.Entry<K,V>> cache = new LinkedHashSet<>();
            // Prepare a statement to go through the entries in this map, 
            // sorted and using the given range conditions
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                getSortedQuery(
                        getRangeConditions(fromStart,fromInclusive,toEnd,toInclusive),
                        descending,null))){
                // Populate the range conditions
            populateValueAndRange(pstmt,1,fromStart,fromKey,toEnd,toKey,false,null);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // While there are still rows in the results
            while(rs.next())
                cache.add(getEntryFromResults(rs));
        }
        return cache;
    }

//    protected abstract Set<K> keyCacheSet(boolean descending,
//            boolean fromStart, K fromKey, boolean fromInclusive,
//            boolean toEnd, K toKey, boolean toInclusive) throws SQLException;
    /**
     * 
     * @param descending
     * @param value
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @return
     * @throws SQLException 
     */
    protected Set<K> valueKeyCacheSet(boolean descending, V value, 
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey,boolean toInclusive) throws SQLException{
            // If the value is null and the value cannot be null
        if (value == null && !canValueBeNull())
                // Return an empty set
            return Collections.emptySet();
            // This is a set that will cache the keys from the query
        Set<K> cache = new LinkedHashSet<>();
            // Prepare a statement to go through the entries in this map, 
            // sorted and using the given range conditions
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                getSortedQuery(
                        getRangeConditions(fromStart,fromInclusive,toEnd,toInclusive,true,value),
                        descending,null))){
                // Populate the range and value conditions
            populateValueAndRange(pstmt,1,fromStart,fromKey,toEnd,toKey,true,value);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // While there are still rows in the results
            while(rs.next())
                cache.add(getKeyFromResults(rs));
        }
        return cache;
    }
    /**
     * 
     * @param pstmt
     * @param parameterIndex
     * @param fromStart
     * @param fromKey
     * @param toEnd
     * @param toKey
     * @param useValue
     * @param value
     * @return
     * @throws SQLException 
     */
    protected int populateValueAndRange(PreparedStatement pstmt, 
            int parameterIndex,boolean fromStart, K fromKey, 
            boolean toEnd, K toKey, boolean useValue, 
            V value)throws SQLException{
            // If this is using values and the value is not null
        if (useValue && value != null)
            setPreparedValue(pstmt,parameterIndex++,value);
            // If the range does not start at the start of the map
        if (!fromStart)
            setPreparedKey(pstmt,parameterIndex++,fromKey);
            // If the range does not end at the end of the map
        if (!toEnd)
            setPreparedKey(pstmt,parameterIndex++,toKey);
        return parameterIndex;
    }
    /**
     * 
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @param useValue
     * @param value
     * @return
     * @throws SQLException 
     */
    protected int sizeSQL(boolean fromStart,K fromKey,boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive, boolean useValue, 
            V value)throws SQLException{
            // If this is to use the given value, that value is null, and 
            // values in this map cannot be null
        if (useValue && value == null && !canValueBeNull())
            return 0;
            // If the entire range of the map is covered
        if (fromStart && toEnd){
                // If this is to use the given value
            if (useValue){
                    // Prepare a statement to get the number of times the 
                    // given value appears in the map
                try(PreparedStatement pstmt = getConnection().prepareStatement(
                        String.format(TABLE_SIZE_QUERY_TEMPLATE+" WHERE %s", 
                                getKeyColumn(),
                                getDataViewName(),
                                getValueCondition(value)))){
                        // If the value is not null
                    if (value != null)
                            // Set the given value
                        setPreparedValue(pstmt,1,value);
                        // Get the results of the query
                    ResultSet rs = pstmt.executeQuery();
                        // If the query had any results
                    if (rs.next())
                        return rs.getInt(COUNT_COLUMN_NAME);
                }
                return 0;
            } else  // Return the size of the table for the map
                return getConnection().getTableSize(getTableName(), getKeyColumn());
        }   // Prepare a statement to count the number of rows that match 
            // the given conditions
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                String.format(TABLE_SIZE_QUERY_TEMPLATE+" WHERE %s",
                    getKeyColumn(),
                    getDataViewName(),
                    getRangeConditions(fromStart,fromInclusive,
                            toEnd,toInclusive,useValue,value)))){
                // Populate the range and value conditions
            populateValueAndRange(pstmt,1,fromStart,fromKey,toEnd,toKey,
                    useValue,value);
                // Get the results of the query
            ResultSet rs = pstmt.executeQuery();
                // If the query had any results
            if (rs.next())
                return rs.getInt(COUNT_COLUMN_NAME);
        }
        return 0;
    }
    /**
     * 
     * @param fromStart
     * @param fromInclusive
     * @param toEnd
     * @param toInclusive
     * @param useValue
     * @param value
     * @return 
     */
    protected String getRemoveRangeConditions(
            boolean fromStart, boolean fromInclusive, 
            boolean toEnd, boolean toInclusive, boolean useValue, V value){
        return getRangeConditions(fromStart,fromInclusive,toEnd,toInclusive,
                useValue,value);
    }
    /**
     * 
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @param useValue
     * @param value
     * @throws SQLException 
     */
    protected void removeSQL(boolean fromStart, K fromKey,boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive, boolean useValue, 
            V value)throws SQLException{
        if (useValue && value == null && !canValueBeNull())
            return;
        if (fromStart && toEnd && !useValue)
            getConnection().clearTable(getTableName());
        else{
            try(PreparedStatement pstmt = getConnection().prepareStatement(
                    "DELETE FROM "+getTableName()+" WHERE "+getRemoveRangeConditions(
                            fromStart,fromInclusive,toEnd,toInclusive,useValue,value))){
                populateValueAndRange(pstmt,1,fromStart,fromKey,toEnd,toKey,useValue,value);
                pstmt.executeUpdate();
            }
        }
//        if (useValue)
//            removeFromCache(fromStart,fromKey,fromInclusive,
//                    toEnd,toKey,toInclusive,value);
//        else
//            removeFromCache(fromStart,fromKey,fromInclusive,
//                    toEnd,toKey,toInclusive);
    }
    /**
     * 
     * @param descending
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @return 
     */
    private Iterator<Entry<K,V>> createEntryIterator(boolean descending,
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive){
        if (!fromStart)
            checkKey(fromKey);
        if (!toEnd)
            checkKey(toKey);
        if (!fromStart && !toEnd && compareKeys(fromKey,toKey) > 0){
            K temp = fromKey;
            fromKey = toKey;
            toKey = temp;
        }
        try{
            return new CacheSetIterator<>(entryCacheSet(descending,
                    fromStart,fromKey,fromInclusive,
                    toEnd,toKey,toInclusive)){
                @Override
                protected void remove(Entry<K, V> value) {
                    AbstractQueryRowMap.this.remove(value.getKey());
                }
            };
        } catch (SQLException ex) {
            appendWarning(ex);
            return Collections.emptyIterator();
        }
    }
    /**
     * 
     * @param descending
     * @param value
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @return 
     */
    private Iterator<K> createValueKeyIterator(boolean descending, V value,
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive){
        if (!fromStart)
            checkKey(fromKey);
        if (!toEnd)
            checkKey(toKey);
        if (!fromStart && !toEnd && compareKeys(fromKey,toKey) > 0){
            K temp = fromKey;
            fromKey = toKey;
            toKey = temp;
        }
        try{
            return new CacheSetIterator<>(valueKeyCacheSet(descending,value,
                    fromStart,fromKey,fromInclusive,
                    toEnd,toKey,toInclusive)){
                @Override
                protected void remove(K value) {
                    AbstractQueryRowMap.this.remove(value);
                }
            };
        } catch (SQLException ex) {
            appendWarning(ex);
            return Collections.emptyIterator();
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Iterator<Entry<K,V>> entryIterator(
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive){
        return createEntryIterator(false,fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Iterator<Entry<K,V>> descendingEntryIterator(
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive){
        return createEntryIterator(true,fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Iterator<K> keyIterator(
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive, V value){
        return createValueKeyIterator(false,value,fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Iterator<K> descendinKeyIterator(
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive, V value){
        return createValueKeyIterator(true,value,fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Map.Entry<K, V> firstEntrySQL() throws SQLException {
        return getEndEntry(false);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Map.Entry<K, V> lastEntrySQL() throws SQLException {
        return getEndEntry(true);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Map.Entry<K, V> lowerEntrySQL(K key) throws SQLException {
        return getNavEntry(key,-1,false);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Map.Entry<K, V> floorEntrySQL(K key) throws SQLException {
        return getNavEntry(key,-1,true);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Map.Entry<K, V> ceilingEntrySQL(K key) throws SQLException {
        return getNavEntry(key,1,true);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Map.Entry<K, V> higherEntrySQL(K key) throws SQLException {
        return getNavEntry(key,1,false);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected K firstKeyForSQL(V value) throws SQLException{
        return getEndKeyForValue(value,false);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected K lastKeyForSQL(V value) throws SQLException{
        return getEndKeyForValue(value,true);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected K lowerKeyForSQL(K key, V value)throws SQLException {
        return getNavKeyForValue(value,key,-1,false);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected K floorKeyForSQL(K key, V value)throws SQLException {
        return getNavKeyForValue(value,key,-1,true);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected K ceilingKeyForSQL(K key, V value)throws SQLException {
        return getNavKeyForValue(value,key,1,true);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected K higherKeyForSQL(K key, V value)throws SQLException {
        return getNavKeyForValue(value,key,1,false);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected int sizeSQL(boolean fromStart,K fromKey,boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive)throws SQLException{
        return sizeSQL(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive,false,null);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected int sizeSQL(boolean fromStart,K fromKey,boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive,V value)throws SQLException{
        return sizeSQL(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive,true,value);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void removeSQL(boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive)throws SQLException{
        removeSQL(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive,false,null);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void removeSQL(boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive,V value)throws SQLException{
        removeSQL(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive,true,value);
    }
    /**
     * This returns the key that was just generated
     * @param value The value that was added to the map
     * @param existingKeys
     * @return 
     */
    protected K getGeneratedKey(V value, Collection<K> existingKeys){
            // Get a copy of the sorted set of keys in this map after the 
            // value was added
        TreeSet<K> keys = new TreeSet<>(keySetFor(value));
            // Remove any keys that were in the map that existed before
        keys.removeAll(existingKeys);
            // If there are somehow no keys still in that set
        if (keys.isEmpty())
                // Return the first key for the value
            return firstKeyFor(value);
            // This gets the first value in the set, which should be the 
        K key = keys.first();   // generated key
            // If the generated key is not null, return it. Otherwise, 
            // return the first key for the value (as a failsafe)
        return (key != null) ? key : firstKeyFor(value);
    }
    /**
     * This returns the name of the table in the database that references the 
     * data in this map.
     * @return The name of the table in the database that references this map.
     */
    protected abstract String getUsedTableName();
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean removeUnusedRowsSQL() throws SQLException{
            // If the data in this table isn't used by another table
        if (getUsedTableName() == null)
            return false;
            // Clear the cache if there is one
        clearCache();
            // Remove the unused rows
        return getConnection().removeUnusedRows(getTableName(), 
                getUsedTableName(), getKeyColumn()) > 0;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object obj){
            // If the given object is this map
        if (obj == this)
            return true;
            // If the given object is a map
        else if (obj instanceof Map){
                // Create a copy of this map (to reduce the number of 
            Map<K, V> temp = new TreeMap<>(this);   // queries)
                // Return whether the object matches the copy
            return temp.equals(obj);
        }
        return false;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
            // Create a copy of this map (to reduce the number of queries)
        Map<K, V> temp = new TreeMap<>(this);
        return temp.hashCode();
    }
}
