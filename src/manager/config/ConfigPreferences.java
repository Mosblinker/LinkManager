/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.config;

import config.ConfigUtilities;
import static config.ConfigUtilities.*;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.io.*;
import java.util.*;
import java.util.prefs.*;
import javax.swing.event.EventListenerList;

/**
 *
 * @author Mosblinker
 */
public class ConfigPreferences extends Preferences{
    /**
     * This is the maximum length of a byte array as a value. This is because 
     * the byte array is encoded as a Base64 encoded String, and the byte array 
     * length is limited to three quarters of {@link 
     * Preferences#MAX_VALUE_LENGTH MAX_VALUE_LENGTH} so that the Base64 encoded 
     * String does not exceed {@code MAX_VALUE_LENGTH}.
     * @see Preferences#MAX_VALUE_LENGTH
     * @see #putByteArray(String, byte[]) 
     */
    public static final int MAX_BYTE_ARRAY_LENGTH = 
            (Preferences.MAX_VALUE_LENGTH * 3) / 4;
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
     * The list of EventListeners registered to this node.
     */
    protected EventListenerList listenerList;
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
        listenerList = new EventListenerList();
            // Create a handler to listen to the node
        Handler handler = new Handler();
            // Add the handler as a NodeChangeListener
        node.addNodeChangeListener(handler);
            // Add the handler as a PreferenceChangeListener
        node.addPreferenceChangeListener(handler);
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
     * This returns the {@code Properties} object that stores the defaults for 
     * this preference node.
     * @return The {@code Properties} object that stores the defaults, or null.
     */
    public Properties getDefaults(){
        return defaults;
    }
    /**
     * This returns the value associated with the given key in the preference 
     * node. This will return null if there is no value associated with the 
     * given key or if the backing store is inaccessible. This ignores the 
     * defaults stored in the default property list.
     * @param key The key to get the value associated with.
     * @return The value associated with the key, or null if there is no value 
     * associated with the key or the backing store is inaccessible.
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #isKeySet(String) 
     * @see #get(String, String) 
     * @see Preferences#get(String, String) 
     * @see #node
     */
    protected String getValue(String key){
        return node.get(key, null);
    }
    /**
     * This returns whether there is a value explicitly associated with the 
     * given key in this preference node. This will return {@code false} if 
     * there is no value associated with the given key, even if the given key 
     * has a default value associated with it.
     * @param key The key to check for.
     * @return {@code true} if and only if the given key has a value explicitly 
     * associated with it; {@code false} otherwise.
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #get(String, String) 
     * @see #put(String, String) 
     * @see #keySet() 
     * @see #containsKey(String) 
     */
    public boolean isKeySet(String key){
            // Return if there is a non-null value set for the given key
        return getValue(key) != null;
    }
    /**
     * This returns whether there is a value associated with the given key in 
     * this preference node. This also applies if the key has a default value 
     * and that value has not been overridden by an explicit preference. The 
     * {@link #isKeySet(String) isKeySet} method can be used to determine if a 
     * key has a value explicitly set for it.
     * @param key The key to check for.
     * @return {@code true} if and only if the given key has a value associated 
     * with it; {@code false} otherwise.
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #get(String, String) 
     * @see #put(String, String) 
     * @see #keySet() 
     * @see #isKeySet(String) 
     */
    public boolean containsKey(String key){
            // Return if there is a non-null value set for the given key
        return get(key, null) != null;
    }
    /**
     * {@inheritDoc } If the specified value is null, then the value associated 
     * with the specified key will be {@link #remove(String) removed}.
     * @param key {@inheritDoc }
     * @param value The value to be associated with the specified key, or null.
     * @throws NullPointerException If the key is {@code null}.
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #remove(String) 
     */
    @Override
    public void put(String key, String value) {
            // Make sure the key is not null
        Objects.requireNonNull(key, "Key cannot be null");
            // If the value is null
        if (value == null){
                // Remove the value, resetting it to the default
            remove(key);
        } else {
                // Get the current value, defaulting to null if not set
            String old = get(key, null);
                // If the new value is different from the old value
            if (!value.equals(old))
                node.put(key, value);
        }
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
        String value = getValue(key);       // found
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
        put(key, Integer.toString(value));
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
        put(key,Long.toString(value));
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
        put(key,Boolean.toString(value));
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
            // Get the value set for the given key, defaulting to null if it's 
            // not set or is not either "true" or "false", ignoring case
        Boolean value = booleanValueOf(get(key, null));
            // If the value is not null, return it. Otherwise, return the given 
            // default value
        return (value != null) ? value : def;
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
        put(key,Float.toString(value));
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
        put(key,Double.toString(value));
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
     * @throws NullPointerException If the key is {@code null}.
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #getByteArray(String, byte[]) 
     * @see #get(String, String) 
     */
    @Override
    public void putByteArray(String key, byte[] value) {
            // Make sure the key is not null
        Objects.requireNonNull(key, "Key cannot be null");
            // If the value is null
        if (value == null){
                // Remove the value, resetting it to the default
            remove(key);
            return;
        }   // If the byte array's length exceeds 3/4ths of the max value length
        if (value.length > MAX_BYTE_ARRAY_LENGTH)
            throw new IllegalArgumentException("Byte array is too long ("+
                    value.length+" > "+MAX_BYTE_ARRAY_LENGTH+")");
            // Encode the byte array in Base64 and store it
        put(key,Base64.getEncoder().encodeToString(value));
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
     * 
     * @param key
     * @param width
     * @param height 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putDimension(String, Dimension) 
     * @see #getDimension(String, Dimension) 
     * @see #get(String, String) 
     */
    public void putDimension(String key, int width, int height){
            // Store the integers into the preference node
        putByteArray(key,ConfigUtilities.dimensionToByteArray(width, height));
    }
    /**
     * 
     * @param key
     * @param value 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putDimension(String, int, int) 
     * @see #getDimension(String, Dimension) 
     * @see #get(String, String) 
     * @see #getByteArray(String, byte[]) 
     * @see #putByteArray(String, byte[]) 
     */
    public void putDimension(String key, Dimension value){
            // If the value is null
        if (value == null)
                // Remove the value for the key, resetting it to default
            remove(key);
        else    // Store the dimension object
            putDimension(key,value.width,value.height);
    }
    /**
     * 
     * @param key
     * @param def
     * @return 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putDimension(String, int, int) 
     * @see #putDimension(String, Dimension) 
     * @see #get(String, String) 
     * @see #getByteArray(String, byte[]) 
     * @see #putByteArray(String, byte[]) 
     */
    public Dimension getDimension(String key, Dimension def){
            // Get the dimensions as a byte array, defaulting to null. Then 
            // convert the byte array into a dimension object, defaulting to the 
            // given default value if null
        return ConfigUtilities.dimensionFromByteArray(getByteArray(key,null),
                def);
    }
    /**
     * 
     * @param key 
     * @param x 
     * @param y 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putPoint(String, Point) 
     * @see #getPoint(String, Point) 
     * @see #get(String, String) 
     */
    public void putPoint(String key, int x, int y){
            // Store the integers into the preference node
        putByteArray(key,ConfigUtilities.pointToByteArray(x, y));
    }
    /**
     * 
     * @param key
     * @param value 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putPoint(String, int, int) 
     * @see #getPoint(String, Point) 
     * @see #get(String, String) 
     * @see #getByteArray(String, byte[]) 
     * @see #putByteArray(String, byte[]) 
     */
    public void putPoint(String key, Point value){
            // If the value is null
        if (value == null)
                // Remove the value for the key, resetting it to default
            remove(key);
        else    // Store the point
            putPoint(key,value.x,value.y);
    }
    /**
     * 
     * @param key
     * @param def
     * @return 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putPoint(String, int, int) 
     * @see #putPoint(String, Point) 
     * @see #get(String, String) 
     * @see #getByteArray(String, byte[]) 
     * @see #putByteArray(String, byte[]) 
     */
    public Point getPoint(String key, Point def){
            // Get the point as a byte array, defaulting to null. Then convert 
            // the byte array into a point object, defaulting to the given 
            // default value if null
        return ConfigUtilities.pointFromByteArray(getByteArray(key,null), def);
    }
    /**
     * 
     * @param key 
     * @param x 
     * @param y 
     * @param width 
     * @param height 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putRectangle(java.lang.String, int, int) 
     * @see #getRectangle(String, Rectangle) 
     * @see #get(String, String) 
     */
    public void putRectangle(String key, int x, int y, int width, int height){
            // Store the integers into the preference node
        putByteArray(key,ConfigUtilities.rectangleToByteArray(x,y,width,height));
    }
    /**
     * 
     * @param key 
     * @param width 
     * @param height 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putRectangle(java.lang.String, int, int, int, int) 
     * @see #getRectangle(String, Rectangle) 
     * @see #get(String, String) 
     */
    public void putRectangle(String key, int width, int height){
        putRectangle(key,0,0,width,height);
    }
    /**
     * 
     * @param key
     * @param value 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putRectangle(java.lang.String, int, int, int, int) 
     * @see #putRectangle(java.lang.String, int, int) 
     * @see #getRectangle(String, Rectangle) 
     * @see #get(String, String) 
     * @see #getByteArray(String, byte[]) 
     * @see #putByteArray(String, byte[]) 
     */
    public void putRectangle(String key, Rectangle value){
            // If the value is null
        if (value == null)
                // Remove the value for the key, resetting it to default
            remove(key);
        else    // Store the rectangle
            putRectangle(key,value.x,value.y,value.width,value.height);
    }
    /**
     * 
     * @param key
     * @param def
     * @return 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #putRectangle(java.lang.String, int, int, int, int) 
     * @see #putRectangle(java.lang.String, int, int) 
     * @see #putRectangle(String, Rectangle) 
     * @see #get(String, String) 
     * @see #getByteArray(String, byte[]) 
     * @see #putByteArray(String, byte[]) 
     */
    public Rectangle getRectangle(String key, Rectangle def){
            // Get the rectangle as a byte array, defaulting to null. Then 
            // convert the byte array into a rectangle object, defaulting to the 
            // given default value if null
        return ConfigUtilities.rectangleFromByteArray(getByteArray(key,null),
                def);
    }
    /**
     * 
     * @param key
     * @param value 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given key is null.
     * @see #put(String, String) 
     * @see #putInt(String, int) 
     * @see #putLong(String, long) 
     * @see #putBoolean(String, boolean) 
     * @see #putFloat(String, float) 
     * @see #putDouble(String, double) 
     * @see #putByteArray(String, byte[]) 
     * @see #putDimension(String, Dimension) 
     * @see #putPoint(String, Point) 
     * @see #putRectangle(String, Rectangle) 
     * @see #remove(String) 
     */
    public void putObject(String key, Object value){
            // If the value is null
        if (value == null)
                // Remove the value for the key, resetting it to default
            remove(key);
            // If the value is a boolean
        else if (value instanceof Boolean)
                // Set the value as a boolean
            putBoolean(key, (boolean) value);
            // If the value is a double
        else if (value instanceof Double)
                // Set the value as a double
            putDouble(key, (double) value);
            // If the value is a float
        else if (value instanceof Float)
                // Set the value as a float
            putFloat(key, (float) value);
            // If the value is a long
        else if (value instanceof Long)
                // Set the value as a long
            putLong(key, (long) value);
            // If the value is a number (should only be ints, shorts, and bytes 
        else if (value instanceof Number)   // left)
                // Set the value as an integer
            putInt(key, ((Number)value).intValue());
            // If the value is a byte array
        else if (value instanceof byte[])
            putByteArray(key, (byte[])value);
            // If the value is a dimension
        else if (value instanceof Dimension)
                // Set the value as a dimension
            putDimension(key, (Dimension)value);
            // If the value is a point
        else if (value instanceof Point)
            putPoint(key, (Point)value);
            // If the value is a rectangle
        else if (value instanceof Rectangle)
            putRectangle(key, (Rectangle)value);
        else    // Get the value as a string and set it
            put(key, value.toString());
    }
    /**
     * 
     * @param map 
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @throws NullPointerException If the given map is null or contains a null 
     * key.
     */
    public void putAll(Map<?, ?> map){
            // Go through the entries in the map
        for (Map.Entry<?, ?> entry : map.entrySet()){
                // Put the entry into the preference node
            putObject(entry.getKey().toString(),entry.getValue());
        }
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
     * 
     * @param pathName
     * @param defaults The {@code Properties} map containing the defaults, or 
     * null.
     * @return 
     */
    public ConfigPreferences node(String pathName, Properties defaults){
        return new ConfigPreferences(node.node(pathName),defaults);
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
    public ConfigPreferences node(String pathName) {
        return node(pathName,null);
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
     * relies on the {@link nodeExists(String) nodeExists("")} method to 
     * determine if this node exists. This will return {@code false} if the 
     * backing store is inaccessible or fails to determine if this node exists.
     * @return Whether this node exists.
     * @see #nodeExists(String) 
     * @see #node
     */
    protected boolean exists(){
        try{
            return nodeExists("");
        } catch(BackingStoreException ex){
            return false;
        }
    }
    /**
     * This checks if this node exists, and if not, throws an {@code 
     * IllegalStateException}. It's worth mentioning that this method relies on 
     * the {@link #exists() exists()} method and may also throw an {@code 
     * IllegalStateException} if the backing store is inaccessible or fails to 
     * determine if this node exists.
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
    /**
     * This converts this preference node into a {@code Properties} object. 
     * <p>
     * The returned {@code Properties} object is not backed by this preference 
     * node. Changes to this preference node are not reflected in the {@code 
     * Properties} object, or vice versa. However, the defaults for the returned 
     * {@code Properties} object are backed by the defaults for this preference 
     * node. Changes to the defaults for this preference node will be reflected 
     * in the {@code Properties} object's defaults and vice versa.
     * @return A {@code Properties} object containing all the keys and their 
     * associated values in this preference node.
     * @throws BackingStoreException If this operation cannot be completed due 
     * to a failure in the backing store or an inability to communicate with it.
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @see #keys() 
     * @see #keySet() 
     * @see #isKeySet(String) 
     * @see #containsKey(String) 
     * @see #get(String, String) 
     * @see #getDefaults() 
     */
    public ConfigProperties toProperties() throws BackingStoreException{
            // Create a properties map with the defaults for this node
        ConfigProperties prop = new ConfigProperties(defaults);
            // Go through all the keys that are in the preference node
        for (String key : node.keys()){
                // Get the value for that key
            String value = getValue(key);
                // If the value is not null
            if (value != null)
                    // Store it as a property
                prop.setProperty(key, value);
        }
        return prop;
    }
    /**
     * This returns an array of all the objects currently registered as 
     * <code><em>Foo</em>Listener</code>s on this preference node. 
     * <code><em>Foo</em>Listener</code>s are registered via the 
     * <code>add<em>Foo</em>Listener</code> method. <p>
     * 
     * The listener type can be specified using a class literal, such as 
     * <code><em>Foo</em>Listener.class</code>. If no such listeners exist, then 
     * an empty array will be returned.
     * @param <T> The type of {@code EventListener} being requested.
     * @param listenerType The type of listeners being requested. This should 
     * be an interface that descends from {@code EventListener}.
     * @return An array of the objects registered as the given listener type on 
     * this preference node, or an empty array if no such listeners have been 
     * added.
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }
    /**
     * {@inheritDoc }
     * @param pcl {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #removePreferenceChangeListener(PreferenceChangeListener) 
     * @see #getPreferenceChangeListeners() 
     * @see #addNodeChangeListener(NodeChangeListener) 
     */
    @Override
    public void addPreferenceChangeListener(PreferenceChangeListener pcl) {
            // Check if this node exists
        checkExists();
            // If the given listener is not null
        if (pcl != null)
                // Add the listener to the list of listeners
            listenerList.add(PreferenceChangeListener.class, pcl);
    }
    /**
     * {@inheritDoc }
     * @param pcl {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #addPreferenceChangeListener(PreferenceChangeListener) 
     * @see #getPreferenceChangeListeners() 
     */
    @Override
    public void removePreferenceChangeListener(PreferenceChangeListener pcl) {
            // Check if this node exists
        checkExists();
            // Remove the listener from the list of listeners
        listenerList.remove(PreferenceChangeListener.class, pcl);
    }
    /**
     * This returns an array containing all the {@code 
     * PreferenceChangeListener}s that have been registered to this preference 
     * node. 
     * @return An array containing the {@code PreferenceChangeListener}s that 
     * have been registered, or an empty array if none have been registered.
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @see #addPreferenceChangeListener(PreferenceChangeListener) 
     * @see #removePreferenceChangeListener(PreferenceChangeListener) 
     */
    public PreferenceChangeListener[] getPreferenceChangeListeners(){
            // Check if this node exists
        checkExists();
        return listenerList.getListeners(PreferenceChangeListener.class);
    }
    /**
     * This is used to notify the {@code PreferenceChangeListener}s registered 
     * to this preference node that a preference has been added, removed, or has 
     * had its value changed.
     * @param evt The {@code PreferenceChangeEvent} to send out to the 
     * listeners.
     * @see #firePreferenceChanged(String, String) 
     * @see #addPreferenceChangeListener(PreferenceChangeListener) 
     * @see #removePreferenceChangeListener(PreferenceChangeListener) 
     * @see #getPreferenceChangeListeners() 
     */
    protected void firePreferenceChanged(PreferenceChangeEvent evt){
            // A for loop to go through the preference change listeners
        for (PreferenceChangeListener l : 
                listenerList.getListeners(PreferenceChangeListener.class)){
            if (l != null)  // If the current listener is not null
                l.preferenceChange(evt);
        }
    }
    /**
     * This is used to notify the {@code PreferenceChangeListener}s registered 
     * to this preference node that a preference has been added, removed, or has 
     * had its value changed.
     * @param key The key of the preference that was changed.
     * @param newValue The new value for the preference, or null if the 
     * preference is being removed.
     * @see PreferenceChangeEvent
     * @see #firePreferenceChanged(PreferenceChangeEvent) 
     * @see #addPreferenceChangeListener(PreferenceChangeListener) 
     * @see #removePreferenceChangeListener(PreferenceChangeListener) 
     * @see #getPreferenceChangeListeners() 
     */
    protected void firePreferenceChanged(String key, String newValue){
        firePreferenceChanged(new PreferenceChangeEvent(this,key,newValue));
    }
    /**
     * {@inheritDoc }
     * @param ncl {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #removeNodeChangeListener(NodeChangeListener) 
     * @see #getNodeChangeListeners() 
     * @see #addPreferenceChangeListener(PreferenceChangeListener) 
     */
    @Override
    public void addNodeChangeListener(NodeChangeListener ncl) {
            // Check if this node exists
        checkExists();
            // If the given listener is not null
        if (ncl != null)
                // Add the listener to the list of listeners
            listenerList.add(NodeChangeListener.class, ncl);
    }
    /**
     * {@inheritDoc }
     * @param ncl {@inheritDoc }
     * @throws IllegalStateException {@inheritDoc }
     * @see #addNodeChangeListener(NodeChangeListener) 
     * @see #getNodeChangeListeners() 
     */
    @Override
    public void removeNodeChangeListener(NodeChangeListener ncl) {
            // Check if this node exists
        checkExists();
            // Remove the listener from the list of listeners
        listenerList.remove(NodeChangeListener.class, ncl);
    }
    /**
     * This returns an array containing all the {@code NodeChangeListener}s that 
     * have been registered to this preference node. 
     * @return An array containing the {@code NodeChangeListener}s that have 
     * been registered, or an empty array if none have been registered.
     * @throws IllegalStateException If this node (or an ancestor) has been 
     * removed with the {@link #removeNode() removeNode()} method.
     * @see #addNodeChangeListener(NodeChangeListener) 
     * @see #removeNodeChangeListener(NodeChangeListener) 
     */
    public NodeChangeListener[] getNodeChangeListeners(){
            // Check if this node exists
        checkExists();
        return listenerList.getListeners(NodeChangeListener.class);
    }
    /**
     * This is used to notify the {@code NodeChangeListener}s registered to this 
     * preference node that a child node has been added or removed from its 
     * parent.
     * @param evt The {@code NodeChangeEvent} to send out to the listeners.
     * @param removed {@code true} if the child node was removed, {@code false} 
     * if the child node was added.
     */
    private void fireNodeChangeEvent(NodeChangeEvent evt, boolean removed){
            // A for loop to go through the node change listeners
        for (NodeChangeListener l : listenerList.getListeners(NodeChangeListener.class)){
            if (l != null){     // If the current listener is not null
                if (removed)    // If a child node has been removed
                        // Notify the listener of a child node being removed
                    l.childRemoved(evt);
                else    // Notify the listener of a child node being added
                    l.childAdded(evt);
            }
        }
    }
    /**
     * This is used to notify the {@code NodeChangeListener}s registered to this 
     * preference node that a child node has been added to its parent.
     * @param evt The {@code NodeChangeEvent} to send out to the listeners.
     * @see #fireChildNodeAdded(Preferences, Preferences) 
     * @see #fireChildNodeAdded(Preferences) 
     * @see #fireChildNodeRemoved(NodeChangeEvent) 
     * @see #fireChildNodeRemoved(Preferences, Preferences) 
     * @see #fireChildNodeRemoved(Preferences) 
     */
    protected void fireChildNodeAdded(NodeChangeEvent evt){
        fireNodeChangeEvent(evt,false);
    }
    /**
     * This is used to notify the {@code NodeChangeListener}s registered to this 
     * preference node that the given child node has been added to the given 
     * parent node.
     * @param parent The parent of the node that was added.
     * @param child The child node that was added.
     * @see NodeChangeEvent
     * @see #fireChildNodeAdded(NodeChangeEvent) 
     * @see #fireChildNodeAdded(Preferences) 
     * @see #fireChildNodeRemoved(NodeChangeEvent) 
     * @see #fireChildNodeRemoved(Preferences, Preferences) 
     * @see #fireChildNodeRemoved(Preferences) 
     */
    protected void fireChildNodeAdded(Preferences parent, Preferences child){
        fireChildNodeAdded(new NodeChangeEvent(parent,child));
    }
    /**
     * This is used to notify the {@code NodeChangeListener}s registered to this 
     * preference node that the given child node has been added to this node.
     * @param child The child node that was added.
     * @see NodeChangeEvent
     * @see #fireChildNodeAdded(NodeChangeEvent) 
     * @see #fireChildNodeAdded(Preferences, Preferences) 
     * @see #fireChildNodeRemoved(NodeChangeEvent) 
     * @see #fireChildNodeRemoved(Preferences, Preferences) 
     * @see #fireChildNodeRemoved(Preferences) 
     */
    protected void fireChildNodeAdded(Preferences child){
        fireChildNodeAdded(this,child);
    }
    /**
     * This is used to notify the {@code NodeChangeListener}s registered to this 
     * preference node that a child node has been removed from its parent.
     * @param evt The {@code NodeChangeEvent} to send out to the listeners.
     * @see #fireChildNodeAdded(NodeChangeEvent) 
     * @see #fireChildNodeAdded(Preferences, Preferences) 
     * @see #fireChildNodeAdded(Preferences) 
     * @see #fireChildNodeRemoved(Preferences, Preferences) 
     * @see #fireChildNodeRemoved(Preferences) 
     */
    protected void fireChildNodeRemoved(NodeChangeEvent evt){
        fireNodeChangeEvent(evt,true);
    }
    /**
     * This is used to notify the {@code NodeChangeListener}s registered to this 
     * preference node that the given child node has been removed from the given 
     * parent node.
     * @param parent The parent of the node that was added.
     * @param child The child node that was added.
     * @see NodeChangeEvent
     * @see #fireChildNodeAdded(NodeChangeEvent) 
     * @see #fireChildNodeAdded(Preferences, Preferences) 
     * @see #fireChildNodeAdded(Preferences) 
     * @see #fireChildNodeRemoved(NodeChangeEvent) 
     * @see #fireChildNodeRemoved(Preferences) 
     */
    protected void fireChildNodeRemoved(Preferences parent, Preferences child){
        fireChildNodeRemoved(new NodeChangeEvent(parent,child));
    }
    /**
     * This is used to notify the {@code NodeChangeListener}s registered to this 
     * preference node that the given child node has been removed from this 
     * node.
     * @param child The child node that was added.
     * @see NodeChangeEvent
     * @see #fireChildNodeAdded(NodeChangeEvent) 
     * @see #fireChildNodeAdded(Preferences, Preferences) 
     * @see #fireChildNodeAdded(Preferences) 
     * @see #fireChildNodeRemoved(NodeChangeEvent) 
     * @see #fireChildNodeRemoved(Preferences, Preferences) 
     */
    protected void fireChildNodeRemoved(Preferences child){
        fireChildNodeRemoved(this,child);
    }
    /**
     * This is a handler class used for listening to the internal preference 
     * node and forwarding its {@code NodeChangeEvent}s and {@code 
     * PreferenceChangeEvent}s to the listeners registered to this preference 
     * node.
     */
    private class Handler implements NodeChangeListener, PreferenceChangeListener{
        @Override
        public void childAdded(NodeChangeEvent evt) {
                // If the event is not null
            if (evt != null){
                    // If the parent node is the internal node
                if (evt.getParent() == node)
                        // Use this node as the parent node instead
                    fireChildNodeAdded(evt.getChild());
                    // If the child node is the internal node
                else if (evt.getChild() == node)
                        // Use this node as the child node instead
                    fireChildNodeAdded(evt.getParent(),ConfigPreferences.this);
                else    // Forward the event as is
                    fireChildNodeAdded(evt);
            }
        }
        @Override
        public void childRemoved(NodeChangeEvent evt) {
                // If the event is not null
            if (evt != null){
                    // If the parent node is the internal node
                if (evt.getParent() == node)
                        // Use this node as the parent node instead
                    fireChildNodeRemoved(evt.getChild());
                    // If the child node is the internal node
                else if (evt.getChild() == node)
                        // Use this node as the child node instead
                    fireChildNodeRemoved(evt.getParent(),ConfigPreferences.this);
                else    // Forward the event as is
                    fireChildNodeRemoved(evt);
            }
        }
        @Override
        public void preferenceChange(PreferenceChangeEvent evt) {
                // If the event is not null and its source is the internal node
            if (evt != null && evt.getSource() == node)
                    // Fire the preference change event with this node as the 
                    // source
                firePreferenceChanged(evt.getKey(),evt.getNewValue());
            else    // Fire the preference change event as is
                firePreferenceChanged(evt);
        }
    }
}
