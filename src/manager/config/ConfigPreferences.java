/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.config;

import java.io.*;
import java.util.*;
import java.util.prefs.*;

/**
 *
 * @author Mosblinker
 */
public class ConfigPreferences extends Preferences{
    /**
     * This contains the word "TRUE".
     */
    private static final String TRUE = "TRUE";
    /**
     * This contains the word "FALSE".
     */
    private static final String FALSE = "FALSE";
    /**
     * This is the preference node that actually stores the values.
     */
    protected final Preferences node;
    /**
     * This is a property list that stores the default values for any keys not 
     * found in the preference node.
     */
    protected Properties defaults;
    /**
     * This constructs a {@code ConfigPreferences} that is a wrapper for the 
     * given preferences node and which has the specified defaults.
     * @param node The preference node to wrap (cannot be null).
     * @param defaults The {@code Properties} map containing the defaults, or 
     * null.
     * @throws NullPointerException If the given preference node is null.
     */
    public ConfigPreferences(Preferences node, Properties defaults){
            // Make sure the preference node is not null
        this.node = Objects.requireNonNull(node);
//            // If the given Properties map is null
//        if (defaults == null)
//            defaults = new Properties();
        this.defaults = defaults;
    }
    /**
     * This constructs a {@code ConfigPreferences} that is a wrapper for the 
     * given preferences node.
     * @param node The preference node to wrap (cannot be null).
     * @throws NullPointerException If the given preference node is null.
     */
    public ConfigPreferences(Preferences node){
        this(node,null);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public boolean isKeySet(String key){
        return node.get(key, null) != null;
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     */
    @Override
    public void put(String key, String value) {
        // TODO: Should this handle something similar to how LinkManagerConfig 
        // deals with setting values to either their defaults or to null?
        node.put(key, value);
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param def {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     */
    @Override
    public String get(String key, String def) {
            // Get the value from the preference node, defaulting to null if not 
        String value = node.get(key, null);     // found
            // If there is a value set for the given key
        if (value != null)
            return value;
            // If there is a default properties list
        if (defaults != null)
                // Return the value from the default properties list
            return defaults.getProperty(key, def);
        return def;
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     */
    @Override
    public void remove(String key) {
        node.remove(key);
    }
    /**
     * {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #removeNode() 
     */
    @Override
    public void clear() throws BackingStoreException {
        node.clear();
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #getInt(String, int) 
     */
    @Override
    public void putInt(String key, int value) {
        // TODO: Should this handle something similar to how LinkManagerConfig 
        // deals with setting values to either their defaults or to null?
        node.putInt(key, value);
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param def {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @see #putInt(String, int) 
     * @see #get(String, String) 
     */
    @Override
    public int getInt(String key, int def) {
            // Get the value set for the given key, as a String, and defaulting 
            // to null if it's not set
        String value = get(key, null);
            // If there is a non-null value stored for the given key
        if (value != null){
            try{    // Try to parse the value as an integer
                return Integer.parseInt(value);
            } catch (NumberFormatException ex) {}
        }
        return def;
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #getLong(String, long) 
     */
    @Override
    public void putLong(String key, long value) {
        // TODO: Should this handle something similar to how LinkManagerConfig 
        // deals with setting values to either their defaults or to null?
        node.putLong(key, value);
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param def {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @see #putLong(String, long) 
     * @see #get(String, String) 
     */
    @Override
    public long getLong(String key, long def) {
            // Get the value set for the given key, as a String, and defaulting 
            // to null if it's not set
        String value = get(key, null);
            // If there is a non-null value stored for the given key
        if (value != null){
            try{    // Try to parse the value as a long
                return Long.parseLong(value);
            } catch (NumberFormatException ex) {}
        }
        return def;
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #getBoolean(String, boolean) 
     * @see #get(String, String) 
     */
    @Override
    public void putBoolean(String key, boolean value) {
        // TODO: Should this handle something similar to how LinkManagerConfig 
        // deals with setting values to either their defaults or to null?
        node.putBoolean(key, value);
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param def {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @see #get(String, String) 
     * @see #putBoolean(String, boolean) 
     */
    @Override
    public boolean getBoolean(String key, boolean def) {
            // Get the value set for the given key, as a String, and defaulting 
            // to null if it's not set
        String value = get(key, null);
            // If the value is equal to the word "true", ignoring case
        if (TRUE.equalsIgnoreCase(value))
            return true;
            // If the value is equal to the word "false", ignoring case
        else if (FALSE.equalsIgnoreCase(value))
            return false;
        return def;
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #getFloat(String, float) 
     */
    @Override
    public void putFloat(String key, float value) {
        // TODO: Should this handle something similar to how LinkManagerConfig 
        // deals with setting values to either their defaults or to null?
        node.putFloat(key, value);
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param def {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @see #putFloat(String, float) 
     * @see #get(String, String) 
     */
    @Override
    public float getFloat(String key, float def) {
            // Get the value set for the given key, as a String, and defaulting 
            // to null if it's not set
        String value = get(key, null);
            // If there is a non-null value stored for the given key
        if (value != null){
            try{    // Try to parse the value as a float
                return Float.parseFloat(value);
            } catch (NumberFormatException ex) {}
        }
        return def;
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #getDouble(String, double) 
     */
    @Override
    public void putDouble(String key, double value) {
        // TODO: Should this handle something similar to how LinkManagerConfig 
        // deals with setting values to either their defaults or to null?
        node.putDouble(key, value);
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param def {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @see #putDouble(String, double) 
     * @see #get(String, String) 
     */
    @Override
    public double getDouble(String key, double def) {
            // Get the value set for the given key, as a String, and defaulting 
            // to null if it's not set
        String value = get(key, null);
            // If there is a non-null value stored for the given key
        if (value != null){
            try{    // Try to parse the value as a double
                return Double.parseDouble(value);
            } catch (NumberFormatException ex) {}
        }
        return def;
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #getByteArray(String, byte[]) 
     * @see #get(String, String) 
     */
    @Override
    public void putByteArray(String key, byte[] value) {
        // TODO: Should this handle something similar to how LinkManagerConfig 
        // deals with setting values to either their defaults or to null?
        node.putByteArray(key, value);
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param def {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @see #get(String, String) 
     * @see #putByteArray(String, byte[]) 
     */
    @Override
    public byte[] getByteArray(String key, byte[] def) {
            // Get the value set for the given key, as a String, and defaulting 
            // to null if it's not set
        String value = get(key, null);
            // If there is a non-null value stored for the given key
        if (value != null){
            try{    // Try to decode the value in Base64 into an array of bytes
                return Base64.getDecoder().decode(value);
            } catch (IllegalArgumentException ex) {}
        }
        return def;
    }
    /**
     * This returns an unmodifiable set containing all the keys that have an 
     * associated value in this preference node. (The returned set will be empty 
     * if this node has no preferences.) 
     * <p>
     * If either this preference node has any <i>stored defaults</i> in its 
     * default properties list or the implementation of the preference node 
     * being wrapped supports <i>stored defaults</i> and there are any such 
     * defaults at this node that have not been overridden, by explicit 
     * preferences, the defaults are returned in the set in addition to any 
     * explicit preferences.
     * <p>
     * The returned set is not backed by the preference node. Changes to this 
     * preference node are not reflected in the returned set.
     * 
     * @return An unmodifiable set of keys that have an associated value in this 
     * preference node, including keys in the default property list.
     * @throws BackingStoreException If this operation cannot be completed due 
     * to a failure in the backing store or an inability to communicate with it.
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @see #keys() 
     */
    public Set<String> keySet() throws BackingStoreException {
            // This set will get all the keys in the node and the default 
            // properties list
        Set<String> keys = new LinkedHashSet<>();
            // Add all the keys from the preference node
        keys.addAll(Arrays.asList(node.keys()));
            // If there is a default properties list
        if (defaults != null)
                // Add all the property names for the default properties
            keys.addAll(defaults.stringPropertyNames());
        return Collections.unmodifiableSet(keys);
    }
    /**
     * {@inheritDoc }
     * @return {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #keySet() 
     */
    @Override
    public String[] keys() throws BackingStoreException {
        return keySet().toArray(String[]::new);
    }
    /**
     * {@inheritDoc }
     * @return {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     */
    @Override
    public String[] childrenNames() throws BackingStoreException {
        return node.childrenNames();
    }
    /**
     * {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     */
    @Override
    public Preferences parent() {
        return node.parent();
    }
    /**
     * {@inheritDoc }
     * @param pathName {@inheritDoc }
     * @return {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #flush() 
     */
    @Override
    public Preferences node(String pathName) {
        return node.node(pathName);
    }
    /**
     * {@inheritDoc }
     * @param pathName {@inheritDoc }
     * @return {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     */
    @Override
    public boolean nodeExists(String pathName) throws BackingStoreException {
        // TODO: May need to account for stored default values. The node MIGHT 
        // technically still exist if there are any stored default values
        return node.nodeExists(pathName);
    }
    /**
     * This returns whether this node exists. It's worth mentioning that this 
     * relies on the {@link Preferences#nodeExists(String) nodeExists("")} 
     * method of the internal preference node to determine if this node exists. 
     * This will return false if the backing store is inaccessible or fails in 
     * some way.
     * @return Whether this node exists.
     * @see #nodeExists(String) 
     * @see #node
     */
    protected boolean exists(){
        try{
            return node.nodeExists("");
        } catch(BackingStoreException ex){
            return false;
        }
    }
    /**
     * This checks if this node exists, and if not, throws an {@code 
     * IllegalStateException}. It's worth mentioning that this method relies on 
     * the {@link #exists() exists()} method and may also throw an {@code 
     * IllegalStateException} if the backing store is inaccessible or fails in 
     * some way.
     * @throws IllegalStateException If this node does not exist.
     * @see #nodeExists(String) 
     * @see #exists() 
     */
    protected void checkExists(){
            // If this node does not exist
        if (!exists())
            throw new IllegalStateException("Preference node does not exist");
    }
    /**
     * {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc }
     * @see #flush() 
     */
    @Override
    public void removeNode() throws BackingStoreException {
        // TODO: May need to deal with the stored default values
        node.removeNode();
    }
    /**
     * {@inheritDoc }
     * @return {@inheritDoc }
     */
    @Override
    public String name() {
        return node.name();
    }
    /**
     * {@inheritDoc }
     * @return {@inheritDoc }
     */
    @Override
    public String absolutePath() {
        return node.absolutePath();
    }
    /**
     * {@inheritDoc }
     * @return {@inheritDoc }
     */
    @Override
    public boolean isUserNode() {
        return node.isUserNode();
    }
    /**
     * {@inheritDoc }
     * @return {@inheritDoc }
     */
    @Override
    public String toString() {
        return node.toString();
    }
    /**
     * {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @see #sync() 
     */
    @Override
    public void flush() throws BackingStoreException {
        node.flush();
    }
    /**
     * {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #flush() 
     */
    @Override
    public void sync() throws BackingStoreException {
        node.sync();
    }
    /**
     * {@inheritDoc }
     * @param os {@inheritDoc }
     * @throws IOException {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #importPreferences(InputStream) 
     */
    @Override
    public void exportNode(OutputStream os) throws IOException, BackingStoreException {
        node.exportNode(os);
    }
    /**
     * {@inheritDoc }
     * @param os {@inheritDoc }
     * @throws IOException {@inheritDoc }
     * @throws BackingStoreException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #importPreferences(InputStream) 
     * @see #exportNode(OutputStream) 
     */
    @Override
    public void exportSubtree(OutputStream os) throws IOException, BackingStoreException {
        node.exportSubtree(os);
    }
    
    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
}
