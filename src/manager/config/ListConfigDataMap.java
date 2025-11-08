/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.config;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author Mosblinker
 * @param <V>
 */
public abstract class ListConfigDataMap <V> extends AbstractMap<Integer,V>{
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
     * This returns a set containing the keys for this map.
     * @return The set containing the keys for this map.
     */
    protected abstract Set<Integer> getKeys();
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
    /**
     * 
     * @param keys
     * @return 
     */
    protected Set<Integer> removeUnusedKeys(Set<Integer> keys){
            // An iterator to check for keys that don't have a value
        Iterator<Integer> itr = keys.iterator();
            // Go through the keys
        while(itr.hasNext()){
                // If this map does not contain a non-null value for that 
            if (!containsKey(itr.next()))   // key
                itr.remove();
        }
        return keys;
    }
}
