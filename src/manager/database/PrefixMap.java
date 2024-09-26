/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import java.util.*;
import sql.UncheckedSQLException;

/**
 * This is a map view of the prefix table in the database storing the links. 
 * There should typically be at least one value in this map, that being an 
 * empty String. This map should not allow null or duplicate values. Each prefix 
 * must be non-null and unique. The keys in this map are often referred to as 
 * prefix IDs, and the values are often referred to as prefixes.
 * @author Milo Steier
 */
public interface PrefixMap extends SQLRowMap<Integer, String>{
    /**
     * This returns the first prefix ID for the empty String prefix. There 
     * should be an entry in this map for an empty String.
     * 
     * @implSpec The default implementation is equivalent to, for this {@code
     * map}:
     * 
     * <pre> {@code 
     * return map.addIfAbsent("");
     * }</pre>
     * 
     * @return The prefix ID for the empty prefix.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @see #firstKeyFor 
     * @see #getEmptyPrefixEntry
     */
    public default int getEmptyPrefixID(){
        return addIfAbsent("");
    }
    /**
     * This returns the first entry for the empty String prefix. There should be 
     * an entry in this map for an empty String.
     * 
     * @implSpec The default implementation is equivalent to, for this {@code
     * map}:
     * 
     * <pre> {@code 
     * return new AbstractMap.SimpleImmutableEntry<>(map.getEmptyPrefixID(),"");
     * }</pre>
     * 
     * @return The entry for the empty prefix.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @see #firstKeyFor 
     * @see #getEmptyPrefixID 
     */
    public default Map.Entry<Integer, String> getEmptyPrefixEntry(){
        return new AbstractMap.SimpleImmutableEntry<>(getEmptyPrefixID(),"");
    }
    /**
     * This attempts to add prefixes to this map based off the given collection 
     * of Strings. 
     * @param values The values to generate prefixes for.
     * @return A map of the prefixes that were added as a result of this call, 
     * or an empty map if no prefixes were added to this map. The returned map 
     * is not backed by this map.
     * @throws NullPointerException If the collection is null or contains a null 
     * element.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public NavigableMap<Integer,String> createPrefixesFrom(Collection<String> 
            values);
    /**
     * This returns the root of a tree that 
     * @param values
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default javax.swing.tree.DefaultMutableTreeNode createPrefixTree(
            Collection<String> values){
        return null;
    }
    /**
     * This removes any currently unused prefixes from this map. An unused 
     * prefix is a prefix that is not used by any links. <p>
     * 
     * Implementations of this map that cannot access the links in the database 
     * should throw an {@code UnsupportedOperationException}.
     * 
     * @return {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc } If the links in the 
     * database cannot be accessed by this map.
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public boolean removeUnusedRows();
    /**
     * This removes any duplicate prefixes from this map. Since this map does 
     * not support duplicate values, this does nothing.
     * @return {@code false}, since this map does not support duplicate 
     * prefixes.
     * @throws UnsupportedOperationException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     * @see #removeUnusedRows
     * @see #firstKeyFor 
     */
    @Override
    public default boolean removeDuplicateRows(){
        return false;
    }
    /**
     * This returns a {@code NavigableMap} containing all the prefixes and their 
     * corresponding prefix ID that match the given String. The map should be 
     * at least one entry in length, with the only assured entry being that of 
     * the empty prefix (which matches all Strings). The map will be sorted by 
     * the length of the prefix associated with a given prefix ID, with longer 
     * prefixes coming before shorter prefixes. The returned map is not required 
     * to be backed by this map.
     * @param value The value to get the matching prefixes for (cannot be null).
     * @return A map of the prefixes that match the given String.
     * @throws NullPointerException If the given String is null.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @see #getSuffixes(java.lang.String) 
     * @see #getLongestPrefixFor(java.lang.String) 
     * @see #getLongestPrefixIDFor(java.lang.String) 
     * @see String#startsWith(java.lang.String) 
     */
    public NavigableMap<Integer, String> getPrefixes(String value);
    /**
     * This returns a {@code NavigableMap} containing all the prefix IDs for the 
     * prefixes that match the given String mapped to the suffix that, when 
     * appended to the prefix that corresponds to its prefix ID, recreate the 
     * given String. In other words, for any key, {@code key}, that is mapped to 
     * a prefix which matches {@code value}, the expression {@code 
     * getPrefixes(value).get(key) + getSuffixes(value).get(key)} should produce 
     * a String that is equivalent to {@code value}. The map should be at least 
     * one entry in length, with the only assured entry being that of the {@link 
     * #getEmptyPrefixID empty prefix ID} mapped to {@code value}. The map will 
     * be sorted by the length of the suffix associated with a given prefix ID, 
     * with shorter suffixes coming before longer suffixes. The returned map is 
     * not required to be backed by this map.
     * @param value The value to get the matching suffixes for (cannot be null).
     * @return A map of suffixes that match the given String.
     * @throws NullPointerException If the given String is null.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @see #getPrefixes(java.lang.String) 
     * @see #getSuffix(java.lang.String, int) 
     * @see #getLongestPrefixFor(java.lang.String) 
     * @see #getLongestPrefixIDFor(java.lang.String) 
     * @see String#startsWith(java.lang.String) 
     */
    public NavigableMap<Integer, String> getSuffixes(String value);
    /**
     * This returns the entry for the longest prefix that matches the given 
     * String. In other words, this returns the entry for the longest prefix, 
     * {@code prefix} for which the expression {@code value.startsWith(prefix)} 
     * returns {@code true}. There is guaranteed to be at least one matching 
     * prefix for any given String, that being the empty prefix.
     * 
     * @implSpec The default implementation is equivalent to, for this {@code 
     * map}:
     * 
     * <pre> {@code 
     * return map.getPrefixes(value).firstEntry();
     * }</pre>
     * 
     * This implementation assumes that the map returned by {@link getPrefixes 
     * getPrefixes} is sorted as described by the method's documentation (i.e. 
     * longest prefixes first).
     * 
     * @param value The value to get the longest matching prefix for (cannot be 
     * null).
     * @return The entry for the longest prefix that matches the given String. 
     * @throws NullPointerException If the given String is null.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @see #getPrefixes 
     * @see #getSuffixes 
     * @see #getLongestPrefixIDFor 
     * @see #getLongestPrefixFor 
     * @see String#startsWith(java.lang.String) 
     * @see #getEmptyPrefixID() 
     */
    public default Map.Entry<Integer, String>getLongestPrefixEntryFor(String value){
            // This gets the map of matching prefixes
        NavigableMap<Integer, String> matches = getPrefixes(value);
            // If the map is somehow empty
        if (matches.isEmpty())
                // Return the empty prefix entry
            return getEmptyPrefixEntry();
            // Return the entry for the first (longest) prefix in the map
        return matches.firstEntry();
    }
    /**
     * This returns the prefix ID for the longest prefix that matches the given 
     * String. In other words, this returns the prefix ID for the longest 
     * prefix, {@code prefix} for which the expression {@code 
     * value.startsWith(prefix)} returns {@code true}. There is guaranteed to be 
     * at least one matching prefix for any given String, that being the empty 
     * prefix.
     * 
     * @implSpec The default implementation is equivalent to, for this {@code 
     * map}:
     * 
     * <pre> {@code 
     * return map.getLongestPrefixEntryFor(value).getKey();
     * }</pre>
     * 
     * This implementation assumes that the map returned by {@link getPrefixes 
     * getPrefixes} is sorted as described by the method's documentation (i.e. 
     * longest prefixes first).
     * 
     * @param value The value to get the longest matching prefix for (cannot be 
     * null).
     * @return The prefix ID for the longest prefix that matches the given 
     * String. 
     * @throws NullPointerException If the given String is null.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @see #getPrefixes 
     * @see #getSuffixes 
     * @see #getLongestPrefixFor 
     * @see String#startsWith(java.lang.String) 
     * @see #getEmptyPrefixID() 
     */
    public default int getLongestPrefixIDFor(String value){
        return getLongestPrefixEntryFor(value).getKey();
    }
    /**
     * This returns the longest prefix that matches the given String. In other 
     * words, this returns the longest prefix, {@code prefix} for which the 
     * expression {@code value.startsWith(prefix)} returns {@code true}. There 
     * is guaranteed to be at least one matching prefix for any given String, 
     * that being the empty prefix.
     * 
     * @implSpec The default implementation is equivalent to, for this {@code 
     * map}:
     * 
     * <pre> {@code 
     * return map.getLongestPrefixEntryFor(value).getValue();
     * }</pre>
     * 
     * @param value The value to get the longest matching prefix for (cannot be 
     * null).
     * @return The longest prefix that matches the given String. 
     * @throws NullPointerException If the given String is null.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @see #getPrefixes 
     * @see #getSuffixes 
     * @see #getLongestPrefixIDFor 
     * @see String#startsWith(java.lang.String) 
     * @see #getEmptyPrefixID() 
     */
    public default String getLongestPrefixFor(String value){
        return getLongestPrefixEntryFor(value).getValue();
    }
    /**
     * 
     * @param values
     * @return 
     */
    public default Map<String, Map.Entry<Integer, String>>getLongestPrefixesFor(
            Collection<? extends String> values){
            // This will get the entries for the longest matching prefixes 
            // for the values
        LinkedHashMap<String, Map.Entry<Integer, String>> prefixes = new LinkedHashMap<>();
            // If the given collection is empty
        if (values.isEmpty())
            return prefixes;
            // If the given collection is not a set
        if (!(values instanceof Set))
                // Turn it into a set
            values = new LinkedHashSet<>(values);
            // Go through the values in the collection
        for (String value : values){
            prefixes.put(value, getLongestPrefixEntryFor(value));
        }
        return prefixes;
    }
    /**
     * This returns the suffix for the given String when using the prefix mapped 
     * to the given key. In other words, this returns what remains of {@code 
     * value} after removing the prefix returned by {@code get(key)}. 
     * 
     * @implSpec The default implementation is equivalent to, for this {@code 
     * map}:
     * 
     * <pre> {@code 
     * String prefix = map.get(key);
     * if (prefix == null || prefix.isEmpty())
     *      return value;
     * return value.substring(prefix.length());
     * }</pre>
     * 
     * The default implementation does not check to see if {@code value} starts 
     * with the prefix mapped to the given key.
     * 
     * @param value The String to get the suffix for.
     * @param key The key whose associated prefix is to be removed from {@code 
     * value} to get the suffix.
     * @return The suffix for the given String after the prefix has been 
     * removed.
     * @throws NullPointerException If the given String is null.
     * @throws IllegalArgumentException Implementations may, but are not 
     * required to, throw this if either this map does not {@link #containsKey 
     * contain} {@code key} or if {@code value} does not {@link 
     * String#startsWith start with} the prefix mapped to {@code key}.
     * @see #get(java.lang.Object) 
     * @see String#startsWith
     * @see #containsKey
     * @see #getPrefixes(java.lang.String) 
     * @see #getSuffixes(java.lang.String) 
     * @see #getLongestPrefixFor(java.lang.String) 
     * @see #getLongestPrefixIDFor(java.lang.String) 
     */
    public default String getSuffix(String value, int key){
            // Get the prefix mapped to the given key
        String prefix = get(key);
            // If that prefix is null or empty
        if (prefix == null || prefix.isEmpty())
            return value;
        return value.substring(prefix.length());
    }
    /**
     * {@inheritDoc } <p>
     * 
     * {@code PrefixMap} does not permit duplicate values.
     * 
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @return {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc }
     * @throws ClassCastException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }. Additionally, this will 
     * be thrown if attempting to map a value that is already in this map.
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public String put(Integer key, String value);
    /**
     * {@inheritDoc } <p>
     * 
     * {@code PrefixMap} does not permit duplicate values.
     * 
     * @param value {@inheritDoc }
     * @return {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc }
     * @throws ClassCastException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc } Additionally, this will 
     * be thrown if attempting to map a value that is already in this map.
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public Integer add(String value);
    /**
     * This is used to sync the cache with the database.
     * @see #clearCache 
     */
    public void syncCache();
    /**
     * This returns the amount of links in the database that use the prefix with 
     * the given prefixID. The returned count does not include links that start 
     * with the prefix with the given prefixID, and only consists of the links 
     * that use the given prefixID as their prefix. In other words, this returns 
     * the amount of links in the database that use the given prefixID. 
     * 
     * @param prefixID The prefixID to match.
     * @return The number of links in the database that use the given prefixID.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @throws UnsupportedOperationException If either the {@code 
     * getPrefixCount} is not supported by this map or if the links in the 
     * database cannot be accessed by this map.
     * @throws IllegalArgumentException If the given prefixID is not currently 
     * in this map.
     */
    public int getPrefixCount(int prefixID);
//    /**
//     * 
//     * @param value
//     * @return 
//     * @throws UncheckedSQLException Implementations may, but are not required 
//     * to, throw this if a database error occurs.
//     * @throws UnsupportedOperationException If either the {@code 
//     * getPrefixCount} is not supported by this map or if the links in the 
//     * database cannot be accessed by this map.
//     * @throws NullPointerException If the specified value is null.
//     */
//    public default int getPrefixCount(String value){
//            // Get the key for the given value
//        Integer key = firstKeyFor(value);
//        return (key != null) ? getPrefixCount(key) : 0;
//    }
}
