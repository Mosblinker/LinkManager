/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import java.util.*;
import java.util.function.BiConsumer;
import sql.UncheckedSQLException;

/**
 * This is a map view of the link table in the database storing the links. This 
 * view eliminates needing to deal with the prefix system when inserting or 
 * retrieving links from the database. The keys in this map are often referred 
 * to as link IDs, and the values are often referred to as links or URLs. This 
 * map should not allow null values.
 * @author Milo Steier
 */
public interface LinkMap extends SQLRowMap<Long, String>{
    /**
     * This removes any duplicate links from this map, leaving only the {@link 
     * #firstKeyFor first (lowest) key} for any given link. After this is 
     * called, there will be only one instance of any given link in this map. 
     * Additionally, after this is called, any lists that refer to any entries 
     * removed by this method will be referring to the first and only remaining 
     * instance of each link. This does nothing if this map does not support 
     * duplicate values. 
     * @return {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     * @see #removeUnusedRows
     * @see #firstKeyFor 
     */
    @Override
    public boolean removeDuplicateRows();
    /**
     * This removes any currently unused links from this map. An unused link is 
     * a link that is not found in any list. <p>
     * 
     * Implementations of this map that cannot access the lists in the database 
     * should throw an {@code UnsupportedOperationException}.
     * 
     * @return {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc } If the lists in the 
     * database cannot be accessed by this map.
     * @throws UncheckedSQLException {@inheritDoc }
     * @see #removeDuplicateRows
     */
    @Override
    public boolean removeUnusedRows();
    /*
    The 
     * returned map may optionally support element removal, which will remove 
     * the corresponding mapping (and duplicates if a key was removed from the 
     * returned map) from this map, via the map's {@code remove}, 
    
    {@code Iterator.remove}, 
     * {@code Set.remove}, {@code removeAll}, {@code retainAll}, and {@code 
     * clear} operations. It does not support the {@code add} or {@code addAll} 
     * operations.
    */
    /**
     * This returns a view of this map where the mappings are reversed so that 
     * the {@link #values values} of this map are the returned map's {@link 
     * #keySet keys} and vice versa. If this map contains any duplicate values, 
     * then only the mapping with the {@link #firstKeyFor first (lowest) key} 
     * for any given value in this map will appear in the returned map. For 
     * example, if both keys {@code k1} and {@code k2} are mapped to a value 
     * {@code v}, and {@code k1} is less than {@code k2}, then the returned map 
     * will have key {@code v} mapped to value {@code k1}. The returned map is 
     * backed by this map, so changes to the returned map will be reflected in 
     * this map and vice-versa. If this map is modified while an iteration over 
     * the returned map's {@code entrySet}, {@code keySet}, or {@code values} 
     * collections are in progress (except through the iterator's own {@code 
     * remove} operation), then the results of the iteration are undefined. The 
     * returned map is unmodifiable except through this map. The returned map is 
     * not required to maintain any particular order of its entries, nor is it 
     * required to ensure that order remains constant over time.
     * @return A view of this map with the keys swapped with the values. 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public Map <String, Long> inverse();
    /**
     * This returns a {@code NavigableMap} containing all the links with 
     * substrings beginning at the given index that start with the given prefix. 
     * In other words, the returned map will contain all entries from this map 
     * with a value {@code v} such that {@code v.startsWith(prefix, offset)} is 
     * {@code true}. The returned map is not required to be backed by this map. 
     * 
     * @implSpec The default implementation creates a TreeMap copy of this map 
     * and then removes any values that make the expression {@code 
     * v.startsWith(prefix, offset)} return {@code false} for any of the values 
     * in the copy of the map.
     * 
     * @param prefix The prefix to search for.
     * @param offset Where to begin looking in the links.
     * @return A map containing 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @throws NullPointerException If the prefix is null.
     * @see #getStartsWith(String) 
     * @see #getEndsWith(String) 
     * @see #getContains(String) 
     * @see #getMatches(String) 
     * @see String#startsWith(String, int) 
     */
    /*
    true if the character sequence represented by the argument is a prefix of 
    the substring of this object starting at index toffset; false otherwise. 
    The result is false if toffset is negative or greater than the length of 
    this String object; otherwise the result is the same as the result of the expression
          this.substring(toffset).startsWith(prefix)
    */
    public default NavigableMap<Long, String> getStartsWith(String prefix, 
            int offset){
            // Create a TreeMap that is a copy of this map
        TreeMap<Long, String> map = new TreeMap<>(this);
            // Remove any values which don't start with the given prefix
        map.values().removeIf((String t) -> {
            return !t.startsWith(prefix, offset);
        });
        return map;
    }
    /**
     * 
     * @param prefix
     * @return 
     */
    public default NavigableMap<Long, String> getStartsWith(String prefix){
        return getStartsWith(prefix, 0);
    }
    /**
     * 
     * @param suffix
     * @return 
     */
    public default NavigableMap<Long, String> getEndsWith(String suffix){
            // Create a TreeMap that is a copy of this map
        TreeMap<Long, String> map = new TreeMap<>(this);
            // Remove any values which don't start with the given suffix
        map.values().removeIf((String t) -> {
            return !t.endsWith(suffix);
        });
        return map;
    }
    /**
     * 
     * @param s
     * @return 
     */
    public default NavigableMap<Long, String> getContains(String s){
            // Create a TreeMap that is a copy of this map
        TreeMap<Long, String> map = new TreeMap<>(this);
            // Remove any values which don't contain the given String
        map.values().removeIf((String t) -> {
            return !t.contains(s);
        });
        return map;
    }
    /**
     * 
     * @param regex
     * @return 
     */
    public default NavigableMap<Long, String> getMatches(String regex){
            // Make sure the regex pattern is not null
        Objects.requireNonNull(regex);
            // Create a TreeMap that is a copy of this map
        TreeMap<Long, String> map = new TreeMap<>(this);
            // Remove any values which don't match the given regex pattern
        map.values().removeIf((String t) -> {
            return !t.matches(regex);
        });
        return map;
    }
    /**
     * This maps the elements in the given collection to new, unique keys for 
     * each element, and returns whether this map was altered as a result of 
     * calling this method. More formally, this generates a key currently not 
     * found in this map for each element in the given collection, and maps the 
     * generated keys to each element.
     * @param c The collection of values to put in this map.
     * @param observer An observer to use to observe the progress of this 
     * method, or null. The first parameter provided to it will be the progress 
     * value, and the second parameter will be the progress maximum. A null 
     * value for the second parameter indicates that this is to switch whether 
     * the progress is indeterminate, with a non-zero first parameter indicating 
     * that the progress is currently indeterminate.
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
    public boolean addAll(Collection<? extends String> c, 
            BiConsumer<Integer,Integer> observer);
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
     * @param observer An observer to use to observe the progress of this 
     * method, or null. The first parameter provided to it will be the progress 
     * value, and the second parameter will be the progress maximum. A null 
     * value for the second parameter indicates that this is to switch whether 
     * the progress is indeterminate, with a non-zero first parameter indicating 
     * that the progress is currently indeterminate.
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
    public boolean addAllIfAbsent(Collection<? extends String> c, 
            BiConsumer<Integer,Integer> observer);
}
