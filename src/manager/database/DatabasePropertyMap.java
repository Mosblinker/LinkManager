/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import java.util.*;
import sql.UncheckedSQLException;
import sql.util.*;

/**
 *
 * @author Milo Steier
 */
public interface DatabasePropertyMap extends SQLMap<String, String>{
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public DatabasePropertyMap getDefaults();
    /**
     * 
     * @param key
     * @param value
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default String setProperty(String key, String value){
        return put(key, value);
    }
    /**
     * 
     * @param key
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default String getProperty(String key){
            // Get the default property map for this map
        DatabasePropertyMap defaults = getDefaults();
            // If there is no default property map, just return whatever this 
            // map has set for the given key. Otherwise, get what this map has 
            // set for the given key, and default to the default property map's 
            // value for that key if this map does not have the key
        return (defaults == null) ? get(key) : 
                getOrDefault(key,defaults.getProperty(key));
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default String getProperty(String key, String defaultValue){
            // Get the property for the key
        String value = getProperty(key);
            // If the property for the key is null, return the default value. 
            // Otherwise, return the property
        return (value == null) ? defaultValue : value;
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default Set<String> propertyNameSet(){
            // This gets a set containing the keys for this map
        Set<String> keys = new  LinkedHashSet<>(keySet());
            // Get the default property map for this map
        DatabasePropertyMap defaults = getDefaults();
            // If this map has a default property map
        if (defaults != null)
                // Add its properties to the set
            keys.addAll(defaults.propertyNameSet());
            // Return an unmodifiable copy of the set
        return Collections.unmodifiableSet(keys);
    }
    /**
     * 
     * @param key
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default boolean containsPropertyName(String key){
            // If this contains the given key
        if (containsKey(key))
            return true;
            // Get the default property map for this map
        DatabasePropertyMap defaults = getDefaults();
            // If this map has a default property map
        if (defaults != null)
                // Return whether the default property map contains the given 
            return defaults.containsPropertyName(key);  // property name
        return false;
    }
    /**
     * 
     * @param key
     * @param value
     * @param defaultValue 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default void setProperty(String key, String value, 
            String defaultValue){
        if (key == null)    // If the given key is null
            throw new NullPointerException();
        if (value != null)  // If the given property is not null
                // Set the given property
            setProperty(key,value);
            // Get the default property map for this map
        DatabasePropertyMap defaults = getDefaults();
            // If this map has a default property map and the default value is 
        if (defaults != null && defaultValue != null)   // not null
                // Set the given default property
            defaults.put(key, defaultValue);
    }
    /**
     * 
     * @param key
     * @param value
     * @param defaultValue
     * @return Whether the property was set before this
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default boolean setPropertyIfAbsent(String key, String value, 
            String defaultValue){
            // If this contains the given property name
        if (containsPropertyName(key))
            return false;
            // Set the property
        setProperty(key,value,defaultValue);
        return true;
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default Set<Entry<String, String>> propertyEntrySet(){
            // This gets a set that will get the property entries
        Set<Entry<String, String>> set = new HashSet<>();
            // Go through the property names
        for (String key : propertyNameSet()){
                // Create and add an immutable entry for the current key
            set.add(new AbstractMap.SimpleImmutableEntry<>(key,getProperty(key)));
        }   // Return an unmodifiable set
        return Collections.unmodifiableSet(set);
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default String listProperties(){
            // This will get a string listing the properties
        String str = "";
            // Go through the property entries
        for (Entry<String, String> entry : propertyEntrySet()){
            str += entry.toString() + System.lineSeparator();
        }   // If the string is empty
        if (str.isEmpty())
            return "";
        return str.substring(0, str.length()-System.lineSeparator().length());
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default Properties toProperties(){
            // Get the default property map for this map
        DatabasePropertyMap defaults = getDefaults();
            // This gets the Properties to use as the defaults, or null
        Properties defaultProp = null;
            // If this map has a default property map
        if (defaults != null)
                // Get the default property map as a Properties Object
            defaultProp = defaults.toProperties();
            // Create a Properties Object to get the properties, using the 
            // default property map's Properties as the defaults
        Properties prop = new Properties(defaultProp);
            // Go through the entries in this map
        for (Map.Entry<String,String> entry : entrySet()){
                // If both the key and value are not null
            if (entry.getKey() != null && entry.getValue() != null)
                    // Put the key and value pair into the Properties object
                prop.put(entry.getKey(), entry.getValue());
        }
        return prop;
    }
}
