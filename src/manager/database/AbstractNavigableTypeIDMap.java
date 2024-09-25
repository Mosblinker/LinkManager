/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.*;
import java.util.*;
import sql.UncheckedSQLException;
import sql.util.*;

/**
 *
 * @author Milo Steier
 * @param <V> The type of mapped values.
 */
abstract class AbstractNavigableTypeIDMap <V> extends 
        AbstractNavigableSQLMap<Integer,V>{
    /**
     * 
     */
    protected final TreeMap<Integer, V> cache = new TreeMap<>();
    /**
     * 
     */
    protected final NavigableSet<Integer> typeIDSet;
    /**
     * 
     * @param typeIDSet 
     */
    protected AbstractNavigableTypeIDMap(NavigableSet<Integer> typeIDSet){
        this.typeIDSet = typeIDSet;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public abstract LinkDatabaseConnection getConnection() throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    public Comparator<? super Integer> comparator() {
        return typeIDSet.comparator();
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean containsKeySQL(Object key) throws SQLException{
            // If the given key is null or not an integer
        if (key == null || !(key instanceof Integer))
            return false;
        return typeIDSet.contains((Integer)key);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected V removeSQL(Object key) throws SQLException{
        throw new UnsupportedOperationException("remove");
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected V putSQL(Integer key, V value) throws SQLException{
        throw new UnsupportedOperationException("put");
    }
    /**
     * 
     * @param key
     * @return 
     */
    protected abstract V createValueSQL(Integer key) throws SQLException;
    /**
     * 
     * @param key
     * @return 
     */
    protected V createValue(Integer key){
        try{
            return createValueSQL(key);
        } catch (SQLException ex){
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected V getSQL(Object key) throws SQLException{
            // If the given key is null or not an integer
        if (key == null || !(key instanceof Integer))
            return null;
            // Get the key as an integer
        int typeID = (Integer)key;
            // If the type/ID set does not contain the given key
        if (!typeIDSet.contains(typeID))
            return null;
            // Return the value for the key, constructing and caching it if 
            // it has not been created yet
        return cache.computeIfAbsent(typeID, (Integer t) -> createValue(t));
    }
    /**
     * 
     * @param key
     * @return 
     */
    private Entry<Integer, V> getEntry(Integer key){
        if (key == null)    // If the given key is null
            return null;
        return new AbstractMap.SimpleImmutableEntry<>(key,get(key));
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Integer firstKeySQL() throws SQLException {
        return typeIDSet.first();
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Entry<Integer, V> firstEntrySQL() throws SQLException {
            // If this map is empty, return null. Otherwise return the entry 
            // for the first key
        return (isEmpty()) ? null : getEntry(firstKey());   
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Integer lastKeySQL() throws SQLException {
        return typeIDSet.last();
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Entry<Integer, V> lastEntrySQL() throws SQLException {
            // If this map is empty, return null. Otherwise return the entry 
            // for the last key
        return (isEmpty()) ? null : getEntry(lastKey());
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Integer lowerKeySQL(Integer key) throws SQLException {
        return typeIDSet.lower(key);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Entry<Integer, V> lowerEntrySQL(Integer key) throws SQLException {
        return getEntry(lowerKey(key));
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Integer floorKeySQL(Integer key) throws SQLException {
        return typeIDSet.floor(key);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Entry<Integer, V> floorEntrySQL(Integer key) throws SQLException {
        return getEntry(floorKey(key));
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Integer ceilingKeySQL(Integer key) throws SQLException {
        return typeIDSet.ceiling(key);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Entry<Integer, V> ceilingEntrySQL(Integer key) throws SQLException {
        return getEntry(ceilingKey(key));
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Integer higherKeySQL(Integer key) throws SQLException {
        return typeIDSet.higher(key);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Entry<Integer, V> higherEntrySQL(Integer key) throws SQLException {
        return getEntry(higherKey(key));
    }
    /**
     * 
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @return 
     */
    private NavigableSet<Integer> keySubSet(
            boolean fromStart, Integer fromKey, boolean fromInclusive, 
            boolean toEnd, Integer toKey, boolean toInclusive){
        return NavigableUtilities.getSubSet(typeIDSet, 
                fromStart, fromKey, fromInclusive, 
                toEnd, toKey, toInclusive);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected int sizeSQL(
            boolean fromStart, Integer fromKey, boolean fromInclusive, 
            boolean toEnd, Integer toKey, boolean toInclusive) throws SQLException {
        return keySubSet(fromStart,fromKey,fromInclusive,toEnd,toKey,toInclusive).size();
    }
    /**
     * 
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @param descending
     * @return 
     */
    private Iterator<Entry<Integer, V>> entryIterator(
            boolean fromStart, Integer fromKey, boolean fromInclusive, 
            boolean toEnd, Integer toKey, boolean toInclusive, boolean descending){
            // Get a subset of the keys to iterate through
        NavigableSet<Integer> keys = keySubSet(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive);
            // If descending, use the descending key iterator. Otherwise, 
            // use the regular key iterator
        return new EntryIterator((descending)?keys.descendingIterator():
                keys.iterator());
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Iterator<Entry<Integer, V>> entryIterator(
            boolean fromStart, Integer fromKey, boolean fromInclusive, 
            boolean toEnd, Integer toKey, boolean toInclusive) {
        return entryIterator(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive,false);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Iterator<Entry<Integer, V>> descendingEntryIterator(
            boolean fromStart, Integer fromKey, boolean fromInclusive, 
            boolean toEnd, Integer toKey, boolean toInclusive) {
        return entryIterator(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive,true);
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
            Map<Integer, V> temp = new TreeMap<>(this);   // queries)
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
        Map<Integer, V> temp = new TreeMap<>(this);
        return temp.hashCode();
    }
    /**
     * 
     */
    private class EntryIterator implements Iterator<Entry<Integer, V>>{
        /**
         * 
         */
        private final Iterator<Integer> itr;
        /**
         * 
         * @param keyItr 
         */
        EntryIterator(Iterator<Integer> keyItr){
            itr = keyItr;
        }
        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }
        @Override
        public Entry<Integer, V> next() {
            return getEntry(itr.next());
        }
    }
}
