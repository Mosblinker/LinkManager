/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import manager.database.SQLRowMap;
import java.sql.*;
import java.util.*;
import sql.UncheckedSQLException;
import sql.util.AbstractNavigableSQLMap;
import sql.util.AbstractNavigableSQLSet;
import sql.util.ConnectionBased;

/**
 *
 * @author Milo Steier
 * @param <K> The type of keys maintained by the map.
 * @param <V> The type of mapped values.
 */
public abstract class AbstractSQLRowMap<K,V> extends AbstractNavigableSQLMap<K, V> 
        implements SQLRowMap<K,V>{
    
//    protected final TreeMap<K,V> cache;
//    
//    protected AbstractSQLRowMap(Comparator<? super K> comparator){
//        cache = new TreeMap<>(comparator);
//    }
//    
//    protected AbstractSQLRowMap(){
//        this(null);
//    }
    /**
     * 
     * @return 
     */
    @Override
    public Comparator<? super K> comparator() {
        return null;//cache.comparator();
    }
//    @Override
//    public boolean containsKey(Object key){
//        try{
//            if (cache.containsKey(key))
//                return true;
//        } catch(ClassCastException | NullPointerException ex){}
//        return super.containsKey(key);
//    }
//    @Override
//    public boolean containsValue(Object value){
//        try{
//            if (cache.containsValue(value))
//                return true;
//        } catch(ClassCastException | NullPointerException ex){}
//        return super.containsValue(value);
//    }
//    @Override
//    public V get(Object key){
//        try{
//            if (cache.containsKey(key))
//                return cache.get(key);
//        } catch(ClassCastException | NullPointerException ex){}
//        return super.get(key);
//    }
//    
//    protected void putIntoCache(K key, V value){
//        try{
//            cache.put(key, value);
//        } catch(ClassCastException | NullPointerException ex){}
//    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    @Override
    public V put(K key, V value){
        checkKeyAndValue(key,value);
        return super.put(key, value);
//        V old = super.put(key, value);
//        putIntoCache(key,value);
//        return old;
    }
//    @Override
//    public V remove(Object key){
//        V value = super.remove(key);
//        try{
//            cache.remove(key,value);
//        } catch(ClassCastException | NullPointerException ex){}
//        return value;
//    }
    /**
     * 
     * @param value
     * @return
     * @throws SQLException 
     */
    protected K addSQL(V value) throws SQLException{
        throw new UnsupportedOperationException("add");
    }
    /**
     * {@inheritDoc }
     * 
     * @implSpec This implementation first checks the value and then invokes the 
     * {@link addSQL(Object) addSQL} method, throwing an {@code 
     * UncheckedSQLException} if it throws an {@code SQLException}.
     * 
     * @param value {@inheritDoc }
     * @return {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc }
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public K add(V value) {
            // Check the value
        checkValue(value);
        try {   // Add the value to the map
            K key = addSQL(value);
//            putIntoCache(key,value);
            return key;
        } catch (SQLException ex) {
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * If the given value is not associated with a key, then this will map the 
     * given value to a new, unique key and returns that key, else this returns 
     * the first key mapped to the given value.
     * 
     * @implSpec This implementation forwards the call to the default 
     * implementation of the {@link SQLRowMap#addIfAbsent(Object) 
     * SQLRowMap.addIfAbsent} method.
     * 
     * @param value The value to put in this map.
     * @return The generated key with which the value is now associated with, 
     * or the first (lowest) key for which the given value is associated with if 
     * the given value is already in the map.
     * @throws UnsupportedOperationException If the {@code add} operation is not 
     * supported by this map.
     * @throws ClassCastException If the value is of an inappropriate type for 
     * this map.
     * @throws NullPointerException If the given value is null and this map does 
     * not permit null values.
     * @throws IllegalArgumentException If some property of the given value 
     * prevents it from being stored in this map.
     * @throws SQLException If a database error occurs.
     */
    protected K addIfAbsentSQL(V value) throws SQLException{
        return SQLRowMap.super.addIfAbsent(value);
    }
    /**
     * 
     * @param value
     * @return 
     */
    @Override
    public K addIfAbsent(V value){
        checkValue(value);
        try {
            K key = addIfAbsentSQL(value);
//            putIntoCache(key,value);
            return key;
        } catch (SQLException ex) {
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * This maps the elements in the given collection to new, unique keys for 
     * each element, and returns whether this map was altered as a result of 
     * calling this method. More formally, this generates a key currently not 
     * found in this map for each element in the given collection, and maps the 
     * generated keys to each element. This is invoked by the {@code addAll} 
     * method, 
     * 
     * @implSpec This implementation is equivalent to invoking the {@link 
     * #add add} method for each element in the given collection.
     * 
     * @param c
     * @return
     * @throws UnsupportedOperationException If the {@code add} operation is not 
     * supported by this map.
     * @throws ClassCastException If any of the elements in the collection are 
     * of an inappropriate type for this map.
     * @throws NullPointerException If the given collection is null or if the 
     * given collection contains a null element and this map does not permit 
     * null values.
     * @throws IllegalArgumentException If some property of any of the elements 
     * in the given collection prevents it from being stored in this map.
     * @throws SQLException If a database error occurs.
     */
    protected boolean addAllSQL(Collection<? extends V> c)throws SQLException{
        int size = size();  // Get the current size of the map
        for (V value : c)   // Go through the elements in the collection
            add(value);
        return size != size();
    }
    /**
     * {@inheritDoc }
     * 
     * @implSpec This implementation invokes the {@code addAllSQL} method, 
     * throwing an {@code UncheckedSQLException} if it throws an {@code 
     * SQLException}.
     * 
     * @param c {@inheritDoc }
     * @return {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc }
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public boolean addAll(Collection<? extends V> c){
        try{
            return addAllSQL(c);
        } catch (SQLException ex) {
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * This maps any elements in the given collection that are not currently 
     * associated with a key to a new, unique key for that element, and returns 
     * whether this map was altered as a result of calling this method. More 
     * formally, this generates a key currently not found in this map for each 
     * element in the given collection that is not currently mapped to a key in 
     * this map, and maps the generated keys to those elements. Any element in 
     * the collection that is already associated with a key in this map will be 
     * ignored. 
     * 
     * @implSpec The default implementation is equivalent to, for this {@code
     * map}:
     * 
     * <pre> {@code 
     *  // Turn the collection into a LinkedHashSet to remove any duplicates
     * c = new LinkedHashSet<>(c);
     *  // Remove any elements already present in the map
     * c.removeAll(map.values());
     *  // Add the remaining elements to the map
     * return map.addAll(c);
     * }</pre>
     * 
     * @param c The collection of values to put in this map.
     * @return Whether this map was altered as a result of calling this method. 
     * @throws UnsupportedOperationException If the {@code add} operation is not 
     * supported by this map.
     * @throws ClassCastException If any of the elements in the collection are 
     * of an inappropriate type for this map.
     * @throws NullPointerException If the given collection is null or if the 
     * given collection contains a null element and this map does not permit 
     * null values.
     * @throws IllegalArgumentException If some property of any of the elements 
     * in the given collection prevents it from being stored in this map.
     * @throws SQLException If a database error occurs.
     */
    protected boolean addAllIfAbsentSQL(Collection<? extends V> c) throws SQLException{
            // Turn the collection into a LinkedHashSet, so as to remove any 
            // duplicates (since every element will only be added once if at 
            // all) while also maintaining the order of the given collection. 
            // This also allows changes to be made to the collection without 
            // altering the original collection.
        c = new LinkedHashSet<>(c);
            // Create a set copy of the values in this map, so as to reduce 
            // calls to the underlying database while preparing the collection 
        Set<V> values = new HashSet<>(values());    // to add
            // Remove any elements shared between the given collection and this 
            // map's value set (since these elements would otherwise have been 
        c.removeAll(values);    // skipped)
            // If there are no elements remaining in the set to be added (the 
            // given collection was empty or only contained values already 
        if (c.isEmpty())    // present in this map)
            return false;
            // Try to add all the remaining elements to this map
        return addAll(c);
    }
    /**
     * 
     * @param c
     * @return 
     */
    @Override
    public boolean addAllIfAbsent(Collection<? extends V> c){
        try{
            return addAllIfAbsentSQL(c);
        } catch (SQLException ex) {
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param value
     * @return
     * @throws ClassCastException If the value is of an inappropriate type for 
     * this map
     * @throws NullPointerException If the given value is null and this map does 
     * not permit null values
     * @throws SQLException Implementations may, but are not required to, throw 
     * this if a database error occurs.
     */
    protected K firstKeyForSQL(V value) throws SQLException{
        for (Map.Entry<K, V> entry : entrySet()){
            if (Objects.equals(value, entry.getValue()))
                return entry.getKey();
        }
        return null;
    }
    /**
     * 
     * @param value
     * @return 
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public K firstKeyFor(V value) {
        if (isEmpty())
            return null;
        checkValue(value);
        try {
            return firstKeyForSQL(value);
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param value
     * @return
     * @throws ClassCastException If the value is of an inappropriate type for 
     * this map
     * @throws NullPointerException If the given value is null and this map does 
     * not permit null values
     * @throws SQLException Implementations may, but are not required to, throw 
     * this if a database error occurs.
     */
    protected K lastKeyForSQL(V value) throws SQLException {
        for (Map.Entry<K, V> entry : descendingMap().entrySet()){
            if (Objects.equals(value, entry.getValue()))
                return entry.getKey();
        }
        return null;
    }
    /**
     * 
     * @param value
     * @return 
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public K lastKeyFor(V value) {
        if (isEmpty())
            return null;
        checkValue(value);
        try {
            return lastKeyForSQL(value);
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param key
     * @param value
     * @return
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map
     * @throws NullPointerException If the given key or value are null and this 
     * map does not permit null keys or values
     * @throws SQLException Implementations may, but are not required to, throw 
     * this if a database error occurs.
     */
    protected K lowerKeyForSQL(K key, V value)throws SQLException {
        Map.Entry<K,V> entry = lowerEntry(key);
        while (entry != null){
            if (Objects.equals(value, entry.getValue()))
                return entry.getKey();
            entry = lowerEntry(entry.getKey());
        }
        return null;
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public K lowerKeyFor(K key, V value){
        checkKeyAndValue(key,value);
        try {
            return lowerKeyForSQL(key,value);
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param key
     * @param value
     * @return
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map
     * @throws NullPointerException If the given key or value are null and this 
     * map does not permit null keys or values
     * @throws SQLException Implementations may, but are not required to, throw 
     * this if a database error occurs.
     */
    protected K floorKeyForSQL(K key, V value)throws SQLException {
        Map.Entry<K,V> entry = floorEntry(key);
        if (entry == null)
            return null;
        else if (Objects.equals(value, entry.getValue()))
            return entry.getKey();
        else
            return lowerKeyFor(entry.getKey(),value);
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public K floorKeyFor(K key, V value){
        checkKeyAndValue(key,value);
        try {
            return floorKeyForSQL(key,value);
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param key
     * @param value
     * @return
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map
     * @throws NullPointerException If the given key or value are null and this 
     * map does not permit null keys or values
     * @throws SQLException Implementations may, but are not required to, throw 
     * this if a database error occurs.
     */
    protected K ceilingKeyForSQL(K key, V value)throws SQLException {
        Map.Entry<K,V> entry = ceilingEntry(key);
        if (entry == null)
            return null;
        else if (Objects.equals(value, entry.getValue()))
            return entry.getKey();
        else
            return higherKeyFor(entry.getKey(),value);
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public K ceilingKeyFor(K key, V value){
        checkKeyAndValue(key,value);
        try {
            return ceilingKeyForSQL(key,value);
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @param key
     * @param value
     * @return
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map
     * @throws NullPointerException If the given key or value are null and this 
     * map does not permit null keys or values
     * @throws SQLException Implementations may, but are not required to, throw 
     * this if a database error occurs.
     */
    protected K higherKeyForSQL(K key, V value)throws SQLException {
        Map.Entry<K,V> entry = higherEntry(key);
        while (entry != null){
            if (Objects.equals(value, entry.getValue()))
                return entry.getKey();
            entry = higherEntry(entry.getKey());
        }
        return null;
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public K higherKeyFor(K key, V value){
        checkKeyAndValue(key,value);
        try {
            return higherKeyForSQL(key,value);
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
//    @Override
//    public void clearCache(){
//        cache.clear();
//    }
//    
//    protected void removeFromCache(boolean fromStart, K fromKey, boolean fromInclusive, 
//            boolean toEnd, K toKey, boolean toInclusive){
//        try{
//            NavigableUtilities.getSubMap(cache, fromStart, fromKey, fromInclusive, 
//                    toEnd, toKey, toInclusive).clear();
//        } catch(ClassCastException | NullPointerException ex){}
//    }
//    
//    protected void removeFromCache(boolean fromStart, K fromKey, boolean fromInclusive, 
//            boolean toEnd, K toKey, boolean toInclusive, V value){
//        try{
//            NavigableMap<K,V> temp = NavigableUtilities.getSubMap(cache, 
//                    fromStart, fromKey, fromInclusive, 
//                    toEnd, toKey, toInclusive);
//            while(temp.containsValue(value))
//                temp.values().remove(value);
//        } catch(ClassCastException | NullPointerException ex){}
//    }
//    
//    @Override
//    protected void removeSQL(boolean fromStart, K fromKey, boolean fromInclusive, 
//            boolean toEnd, K toKey, boolean toInclusive)throws SQLException{
//        super.removeSQL(fromStart,fromKey,fromInclusive,
//                toEnd,toKey,toInclusive);
//        removeFromCache(fromStart,fromKey,fromInclusive,
//            toEnd,toKey,toInclusive);
//    }
    /**
     * 
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @param value
     * @return
     * @throws SQLException 
     */
    protected int sizeSQL(boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive, V value) throws SQLException {
        int size = 0;
        for (Map.Entry<K, V> entry : subMap(fromStart, fromKey, fromInclusive, 
                toEnd, toKey, toInclusive).entrySet()){
            if (Objects.equals(value, entry.getValue()))
                size++;
        }
        return size;
    }
    /**
     * 
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @param value
     * @throws SQLException 
     */
    protected void removeSQL(boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive, V value) throws SQLException {
        Iterator<K> itr = keyIterator(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive,value);
        while (itr.hasNext()){
            itr.next();
            itr.remove();
        }
//        removeFromCache(fromStart,fromKey,fromInclusive,
//                toEnd,toKey,toInclusive,value);
    }
    /**
     * 
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @param value
     * @return 
     */
    protected Iterator<K> keyIterator(
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive, V value){
        return new ValueKeyIterator(entryIterator(fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive),value);
    }
    /**
     * 
     * @param fromStart
     * @param fromKey
     * @param fromInclusive
     * @param toEnd
     * @param toKey
     * @param toInclusive
     * @param value
     * @return 
     */
    protected Iterator<K> descendinKeyIterator(
            boolean fromStart, K fromKey, boolean fromInclusive, 
            boolean toEnd, K toKey, boolean toInclusive, V value){
        return new ValueKeyIterator(descendingEntryIterator(
                fromStart,fromKey,fromInclusive,
                toEnd,toKey,toInclusive),value);
    }
    /**
     * 
     * @param value
     * @return 
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public NavigableSet<K> keySetFor(V value) {
        checkValue(value);
        return new ValueKeySet(value);
    }
    /**
     * This checks the given value to see if it can be stored in this map, and 
     * if not, throws an exception.
     * @param value The value to check.
     * @throws ClassCastException If the value is of an inappropriate type for 
     * this map 
     * @throws NullPointerException If the given value is null and this map does 
     * not permit null values 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    protected void checkValue(V value){ }
    /**
     * 
     * @param key
     * @param value 
     */
    protected void checkKeyAndValue(K key, V value){
        checkKey(key);
        checkValue(value);
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    protected boolean removeUnusedRowsSQL() throws SQLException{
        return false;
    }
    /**
     * 
     * @return 
     */
    @Override
    public boolean removeUnusedRows(){
        try{
            return removeUnusedRowsSQL();
        } catch(SQLException ex){
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @return
     * @throws SQLException 
     */
    protected boolean removeDuplicateRowsSQL() throws SQLException{
        int size = size();
        Set<V> values = new HashSet<>(values());
        for (V value : values){
            NavigableSet<K> keys = keySetFor(value);
            K firstKey = keys.first();
            while(compareKeys(firstKey,keys.last()) != 0)
                keys.pollLast();
        }
        return size != size();
    }
    /**
     * 
     * @return 
     */
    @Override
    public boolean removeDuplicateRows(){
        try{
            return removeDuplicateRowsSQL();
        } catch(SQLException ex){
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     */
    private class ValueKeySet extends AbstractNavigableSQLSet<K>{
        /**
         * 
         */
        protected final V value;
        /**
         * 
         * @param value 
         */
        ValueKeySet(V value){
            this.value = value;
        }
        /**
         * 
         * @return 
         */
        public V getValue(){
            return value;
        }
        /**
         * This returns the map for which this serves as a set view of its keys.
         * @return The parent map.
         */
        protected AbstractSQLRowMap<K, V> getMap(){
            return AbstractSQLRowMap.this;
        }
        /**
         * 
         * @return 
         */
        @Override
        public Comparator<? super K> comparator() {
            return getMap().comparator();
        }
        /**
         * 
         * @return
         * @throws SQLException 
         */
        @Override
        protected K firstSQL() throws SQLException {
            if (isEmpty())
                throw new NoSuchElementException();
            return getMap().firstKeyFor(value);
        }
        /**
         * 
         * @return
         * @throws SQLException 
         */
        @Override
        protected K lastSQL() throws SQLException {
            if (isEmpty())
                throw new NoSuchElementException();
            return getMap().lastKeyFor(value);
        }
        /**
         * 
         * @param e
         * @return
         * @throws SQLException 
         */
        @Override
        protected K lowerSQL(K e) throws SQLException {
            return getMap().lowerKeyFor(e,value);
        }
        /**
         * 
         * @param e
         * @return
         * @throws SQLException 
         */
        @Override
        protected K floorSQL(K e) throws SQLException {
            return getMap().floorKeyFor(e,value);
        }
        /**
         * 
         * @param e
         * @return
         * @throws SQLException 
         */
        @Override
        protected K ceilingSQL(K e) throws SQLException {
            return getMap().ceilingKeyFor(e,value);
        }
        /**
         * 
         * @param e
         * @return
         * @throws SQLException 
         */
        @Override
        protected K higherSQL(K e) throws SQLException {
            return getMap().higherKeyFor(e,value);
        }
        /**
         * 
         * @param fromStart
         * @param fromElement
         * @param fromInclusive
         * @param toEnd
         * @param toElement
         * @param toInclusive
         * @return
         * @throws SQLException 
         */
        @Override
        protected int sizeSQL(
                boolean fromStart, K fromElement, boolean fromInclusive, 
                boolean toEnd, K toElement, boolean toInclusive) throws SQLException {
            return getMap().sizeSQL(fromStart,fromElement,fromInclusive,
                    toEnd,toElement,toInclusive,value);
        }
        /**
         * 
         * @param fromStart
         * @param fromElement
         * @param fromInclusive
         * @param toEnd
         * @param toElement
         * @param toInclusive
         * @throws SQLException 
         */
        @Override
        protected void removeSQL(
                boolean fromStart, K fromElement, boolean fromInclusive, 
                boolean toEnd, K toElement, boolean toInclusive) throws SQLException {
            getMap().removeSQL(fromStart, fromElement, fromInclusive, 
                    toEnd, toElement, toInclusive, value);
        }
        /**
         * 
         * @param fromStart
         * @param fromElement
         * @param fromInclusive
         * @param toEnd
         * @param toElement
         * @param toInclusive
         * @return 
         */
        @Override
        protected Iterator<K> iterator(
                boolean fromStart, K fromElement, boolean fromInclusive, 
                boolean toEnd, K toElement, boolean toInclusive) {
            return getMap().keyIterator(fromStart, fromElement, fromInclusive, 
                    toEnd, toElement, toInclusive, value);
        }
        /**
         * 
         * @param fromStart
         * @param fromElement
         * @param fromInclusive
         * @param toEnd
         * @param toElement
         * @param toInclusive
         * @return 
         */
        @Override
        protected Iterator<K> descendingIterator(
                boolean fromStart, K fromElement, boolean fromInclusive, 
                boolean toEnd, K toElement, boolean toInclusive) {
            return getMap().descendinKeyIterator(
                    fromStart, fromElement, fromInclusive, 
                    toEnd, toElement, toInclusive, value);
        }
        /**
         * 
         * @return
         * @throws SQLException 
         */
        @Override
        public Connection getConnection() throws SQLException {
            return getMap().getConnection();
        }
        /**
         * 
         * @param exception 
         */
        @Override
        protected void appendWarning(SQLException exception){
            getMap().appendWarning(exception);
        }
        /**
         * 
         * @return
         * @throws SQLException 
         */
        @Override
        public SQLWarning getWarnings() throws SQLException {
            return getMap().getWarnings();
        }
        /**
         * 
         * @throws SQLException 
         */
        @Override
        public void clearWarnings() throws SQLException{
            getMap().clearWarnings();
        }
    }
    /**
     * 
     */
    private class ValueKeyIterator implements Iterator<K>{
        /**
         * 
         */
        private final Iterator<Entry<K, V>> itr;
        /**
         * 
         */
        private final V value;
        /**
         * 
         */
        private boolean hasNext;
        /**
         * 
         */
        private K next;
        /**
         * 
         * @return 
         */
        private K findNext(){
            hasNext = itr.hasNext();
            while (hasNext){
                Entry<K, V> entry = itr.next();
                if (Objects.equals(value, entry.getValue()))
                    return entry.getKey();
                hasNext = itr.hasNext();
            }
            return null;
        }
        /**
         * 
         * @param itr
         * @param value 
         */
        ValueKeyIterator(Iterator<Entry<K, V>> itr, V value){
            this.itr = itr;
            this.value = value;
            next = findNext();
        }
        /**
         * 
         * @return 
         */
        @Override
        public boolean hasNext() {
            return hasNext;
        }
        /**
         * 
         * @return 
         */
        @Override
        public K next() {
            if (!hasNext())
                throw new NoSuchElementException();
            K current = next;
            next = findNext();
            return current;
        }
        /**
         * 
         */
        @Override
        public void remove() {
            itr.remove();
        }
    }
}
