/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import java.util.*;
import sql.util.NavigableSQLMap;

/**
 * This is a specialized {@code NavigableSQLMap} that provides methods for 
 * getting the keys associated with a given value, along with methods for adding 
 * a value with an automatically generated key. This is typically used for map 
 * views of the rows in a database, with the keys for the map being the primary 
 * keys for the rows. <p>
 * 
 * Implementations may, but are not required to, throw an {@code 
 * UncheckedSQLException} when an {@code SQLException} is unable to be thrown 
 * for the methods of this interface.
 * 
 * @author Milo Steier
 * @param <K> The type of keys maintained by the map.
 * @param <V> The type of mapped values.
 */
public interface SQLRowMap<K,V> extends NavigableSQLMap<K, V>{
    /**
     * This returns the first (lowest) key currently mapped to the given value 
     * in this map, or null if the given value is not found in this map. <p>
     * 
     * More formally, if this map contains at least one mapping from a key 
     * {@code k} to a value {@code v} such that {@code Objects.equals(v, 
     * value)}, then this returns the lowest value for {@code k}. Otherwise, 
     * this returns null. <p>
     * 
     * If this map permits null keys, then a return value of null does not 
     * <i>necessarily</i> indicate that the map contains no mapping to the given 
     * value; it is also possible that the map explicitly maps null to the 
     * value. The {@link #containsValue containsValue} operation may be used to 
     * distinguish these two cases.
     * 
     * @param value The value whose first associated key is to be returned.
     * @return The first (lowest) key currently mapped to the given value in 
     * this map, or null if this map contains no mapping to the value.
     * @throws ClassCastException If the value is of an inappropriate type for 
     * this map
     * @throws NullPointerException If the given value is null and this map does 
     * not permit null values
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public K firstKeyFor(V value);
    /**
     * This returns the last (highest) key currently mapped to the given value 
     * in this map, or null if the given value is not found in this map. <p>
     * 
     * More formally, if this map contains at least one mapping from a key 
     * {@code k} to a value {@code v} such that {@code Objects.equals(v, 
     * value)}, then this returns the highest value for {@code k}. Otherwise, 
     * this returns null. <p>
     * 
     * If this map permits null keys, then a return value of null does not 
     * <i>necessarily</i> indicate that the map contains no mapping to the given 
     * value; it is also possible that the map explicitly maps null to the 
     * value. The {@link #containsValue containsValue} operation may be used to 
     * distinguish these two cases.
     * 
     * @param value The value whose last associated key is to be returned.
     * @return The last (highest) key currently mapped to the given value in 
     * this map, or null if this map contains no mapping to the value.
     * @throws ClassCastException If the value is of an inappropriate type for 
     * this map
     * @throws NullPointerException If the given value is null and this map does 
     * not permit null values
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public K lastKeyFor(V value);
    /**
     * This returns the greatest key currently mapped to the given value in this 
     * map that is strictly less than the given key, or null if either there is 
     * no such key or if the given value is not found in this map. <p>
     * 
     * More formally, if this map contains at least one mapping from a key 
     * {@code k} to a value {@code v} such that {@code Objects.equals(v, 
     * value)}, then this returns the greatest value for {@code k} that is 
     * strictly less than {@code key}. Otherwise, this returns null. <p>
     * 
     * If this map permits null keys, then a return value of null does not 
     * <i>necessarily</i> indicate that the map contains no mapping to the given 
     * value; it is also possible that the map explicitly maps null to the 
     * value. The {@link #containsValue containsValue} operation may be used to 
     * distinguish these two cases.
     * 
     * @param key The key to match.
     * @param value The value whose associated key is to be returned.
     * @return The greatest key less than {@code key} that is currently mapped 
     * to the given value in this map, or null if there is no such key in this 
     * map.
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map
     * @throws NullPointerException If the given key or value are null and this 
     * map does not permit null keys or values
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public K lowerKeyFor(K key, V value);
    /**
     * This returns the greatest key currently mapped to the given value in this 
     * map that is less than or equal to the given key, or null if either there 
     * is no such key or if the given value is not found in this map. <p>
     * 
     * More formally, if this map contains at least one mapping from a key 
     * {@code k} to a value {@code v} such that {@code Objects.equals(v, 
     * value)}, then this returns the greatest value for {@code k} that is less 
     * than or equal to {@code key}. Otherwise, this returns null. <p>
     * 
     * If this map permits null keys, then a return value of null does not 
     * <i>necessarily</i> indicate that the map contains no mapping to the given 
     * value; it is also possible that the map explicitly maps null to the 
     * value. The {@link #containsValue containsValue} operation may be used to 
     * distinguish these two cases.
     * 
     * @param key The key to match.
     * @param value The value whose associated key is to be returned.
     * @return The greatest key less than or equal to {@code key} that is 
     * currently mapped to the given value in this map, or null if there is no 
     * such key in this map.
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map
     * @throws NullPointerException If the given key or value are null and this 
     * map does not permit null keys or values
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public K floorKeyFor(K key, V value);
    /**
     * This returns the least key currently mapped to the given value in this 
     * map that is greater than or equal to the given key, or null if either 
     * there is no such key or if the given value is not found in this map. <p>
     * 
     * More formally, if this map contains at least one mapping from a key 
     * {@code k} to a value {@code v} such that {@code Objects.equals(v, 
     * value)}, then this returns the least value for {@code k} that is greater 
     * than or equal to {@code key}. Otherwise, this returns null. <p>
     * 
     * If this map permits null keys, then a return value of null does not 
     * <i>necessarily</i> indicate that the map contains no mapping to the given 
     * value; it is also possible that the map explicitly maps null to the 
     * value. The {@link #containsValue containsValue} operation may be used to 
     * distinguish these two cases.
     * 
     * @param key The key to match.
     * @param value The value whose associated key is to be returned.
     * @return The least key greater than or equal to {@code key} that is 
     * currently mapped to the given value in this map, or null if there is no 
     * such key in this map.
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map
     * @throws NullPointerException If the given key or value are null and this 
     * map does not permit null keys or values 
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public K ceilingKeyFor(K key, V value);
    /**
     * This returns the least key currently mapped to the given value in this 
     * map that is strictly greater than the given key, or null if either there 
     * is no such key or if the given value is not found in this map. <p>
     * 
     * More formally, if this map contains at least one mapping from a key 
     * {@code k} to a value {@code v} such that {@code Objects.equals(v, 
     * value)}, then this returns the least value for {@code k} that is strictly 
     * greater than {@code key}. Otherwise, this returns null. <p>
     * 
     * If this map permits null keys, then a return value of null does not 
     * <i>necessarily</i> indicate that the map contains no mapping to the given 
     * value; it is also possible that the map explicitly maps null to the 
     * value. The {@link #containsValue containsValue} operation may be used to 
     * distinguish these two cases.
     * 
     * @param key The key to match.
     * @param value The value whose associated key is to be returned.
     * @return The least key greater than {@code key} that is currently mapped 
     * to the given value in this map, or null if there is no such key in this 
     * map.
     * @throws ClassCastException If either the key or value are of an 
     * inappropriate type for this map 
     * @throws NullPointerException If the given key or value are null and this 
     * map does not permit null keys or values 
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public K higherKeyFor(K key, V value);
    /**
     * This returns a view of all the keys in this map that are mapped to the 
     * given value. The set is backed by this map, so changes to the map are 
     * reflected in the set, and vice-versa. If the map is modified while an 
     * iteration over the set is in progress (except through the iterator's own 
     * {@code remove} operation), then the results of the iteration are 
     * undefined. The set supports element removal, which removes the
     * corresponding mapping from the map, via the {@code Iterator.remove}, 
     * {@code Set.remove}, {@code removeAll}, {@code retainAll}, and {@code 
     * clear} operations. It does not support the {@code add} or {@code addAll} 
     * operations.
     * @param value The value to get the set of associated keys for.
     * @return A set view of the keys contained in this map that are mapped to 
     * the given value.
     * @throws ClassCastException If the value is of an inappropriate type for 
     * this map 
     * @throws NullPointerException If the given value is null and this map does 
     * not permit null values 
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public NavigableSet<K> keySetFor(V value);
    /**
     * This maps the given value to a new, unique key and returns that key. More 
     * formally, this generates a key currently not found in this map, {@link 
     * #put maps} the generated key to the given value, and returns the 
     * generated key.
     * @param value The value to put in this map.
     * @return The generated key with which the value is now associated with. 
     * @throws UnsupportedOperationException If the {@code add} operation is not 
     * supported by this map.
     * @throws ClassCastException If the value is of an inappropriate type for 
     * this map.
     * @throws NullPointerException If the given value is null and this map does 
     * not permit null values.
     * @throws IllegalArgumentException If some property of the given value 
     * prevents it from being stored in this map.
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public K add(V value);
    /**
     * If the given value is not associated with a key, then this will map the 
     * given value to a new, unique key and returns that key, else this returns 
     * the first key mapped to the given value.
     * 
     * @implSpec The default implementation is equivalent to, for this {@code
     * map}:
     * 
     * <pre> {@code 
     * if (map.containsValue(value))
     *     return map.firstKeyFor(value);
     * return map.add(value);
     * }</pre>
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
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public default K addIfAbsent(V value){
            // If this map contains the value
        if (containsValue(value))
                // Return the first key mapped to the value
            return firstKeyFor(value);
        return add(value);              // Add the value to the map
    }
    /**
     * This maps the elements in the given collection to new, unique keys for 
     * each element, and returns whether this map was altered as a result of 
     * calling this method. More formally, this generates a key currently not 
     * found in this map for each element in the given collection, and maps the 
     * generated keys to each element.
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
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public boolean addAll(Collection<? extends V> c);
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
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     */
    public boolean addAllIfAbsent(Collection<? extends V> c);
    /**
     * This removes any currently unused rows from this map. The exact 
     * definition of an unused row depends on the implementation, but typically 
     * it refers to a row that is not being referenced to by another table in 
     * the database. <p>
     * 
     * Implementations of this map may choose not to support this operation if 
     * either doing so would cause issues or if there is no way to determine 
     * whether a row is truly unused. If this is the case, then implementations 
     * must throw an {@code UnsupportedOperationException}. 
     * 
     * @return If this map was altered as a result of calling this method.
     * @throws UnsupportedOperationException If the {@code removeUnusedRows} 
     * operation is not supported by this map.
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     * @see #removeDuplicateRows
     */
    public boolean removeUnusedRows();
    /**
     * This removes any duplicate values from this map, leaving only the {@link 
     * #firstKeyFor first (lowest) key} for any given value. After this is 
     * called, there will be only one instance of any given value in this map. 
     * This does nothing if this map does not support duplicate values. <p>
     * 
     * Implementations of this map may choose not to support this operation if 
     * doing so would cause issues, such as if what counts as a duplicate value 
     * cannot be boiled down to a simple check for equality or if removing a 
     * duplicate value could cause unrecoverable data loss. If this is the case, 
     * then implementations must throw an {@code UnsupportedOperationException}.
     * 
     * @return If this map was altered as a result of calling this method. 
     * @throws UnsupportedOperationException If the {@code removeDuplicateRows} 
     * operation is not supported by this map.
     * @throws sql.UncheckedSQLException Implementations may, but 
     * are not required to, throw this if a database error occurs.
     * @see #removeUnusedRows
     * @see #firstKeyFor 
     */
    public boolean removeDuplicateRows();
}
