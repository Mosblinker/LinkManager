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
import java.util.*;

/**
 *
 * @author Mosblinker
 */
public class ConfigProperties extends Properties{
    /**
     * 
     */
    public ConfigProperties(){
        super();
    }
    /**
     * 
     * @param defaults 
     */
    public ConfigProperties(Properties defaults){
        super(defaults);
    }
    /**
     * 
     * @param initialCapacity 
     */
    public ConfigProperties(int initialCapacity){
        super(initialCapacity);
    }
    /**
     * This returns the {@code Properties} object that stores the defaults for 
     * this {@code Properties}.
     * @return The {@code Properties} object that stores the defaults, or null.
     */
    public Properties getDefaults(){
        return defaults;
    }
    /**
     * {@inheritDoc }
     * @param key {@inheritDoc }
     * @param value {@inheritDoc }
     * @return {@inheritDoc }
     * @see #getProperty(String) 
     */
    @Override
    public synchronized Object setProperty(String key, String value){
            // If the new value is null
        if (value == null)
                // Remove the value from the map
            return remove(key);
            // Set the property to the given value
        return super.setProperty(key, value);
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public synchronized Object setProperty(String key, byte[] value){
            // This will get the value as a String
        String str = null;
            // If the value is not null
        if (value != null)
                // Encode the value in base64
            str = Base64.getEncoder().encodeToString(value);
            // Set the property to the base64 encoded value
        return setProperty(key,str);
    }
    /**
     * 
     * @param key
     * @param width
     * @param height
     * @return 
     */
    public synchronized Object setDimensionProperty(String key, int width, int height){
        return setProperty(key,ConfigUtilities.dimensionToByteArray(width, height));
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public synchronized Object setDimensionProperty(String key,Dimension value){
            // If the new value is null
        if (value == null)
                // Remove the value from the map
            return remove(key);
            // Set the dimension property
        return setDimensionProperty(key,value.width,value.height);
    }
    /**
     * 
     * @param key
     * @param x
     * @param y
     * @return 
     */
    public synchronized Object setPointProperty(String key, int x, int y){
        return setProperty(key,ConfigUtilities.pointToByteArray(x, y));
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public synchronized Object setPointProperty(String key, Point value){
            // If the new value is null
        if (value == null)
                // Remove the value from the map
            return remove(key);
            // Set the point property
        return setPointProperty(key,value.x,value.y);
    }
    /**
     * 
     * @param key
     * @param x
     * @param y
     * @param width
     * @param height
     * @return 
     */
    public synchronized Object setRectangleProperty(String key, int x, int y, 
            int width, int height){
        return setProperty(key,ConfigUtilities.rectangleToByteArray(x,y,width,
                height));
    }
    /**
     * 
     * @param key
     * @param width
     * @param height
     * @return 
     */
    public synchronized Object setRectangleProperty(String key, int width, 
            int height){
        return setRectangleProperty(key,0,0,width,height);
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public synchronized Object setRectangleProperty(String key,Rectangle value){
            // If the new value is null
        if (value == null)
                // Remove the value from the map
            return remove(key);
            // Set the rectangle property
        return setRectangleProperty(key,value.x,value.y,value.width,value.height);
    }
    /**
     * 
     * @param key
     * @param value
     * @return 
     */
    public synchronized Object setProperty(String key, Object value){
            // If the new value is null
        if (value == null)
                // Remove the value from the map
            return remove(key);
            // If the new value is a byte array
        else if (value instanceof byte[])
                // Set the property as a byte array
            return setProperty(key, (byte[]) value);
            // If the new value is a dimension
        else if (value instanceof Dimension)
                // Set the value as a dimension
            return setDimensionProperty(key,(Dimension)value);
            // If the new value is a point
        else if (value instanceof Point)
                // Set the value as a point
            return setPointProperty(key,(Point)value);
            // If the new value is a rectangle
        else if (value instanceof Rectangle)
                // Set the value as a rectangle
            return setRectangleProperty(key,(Rectangle)value);
            // If the new value is a UUID
        else if (value instanceof UUID)
                // Set the value as a UUID
            return setUUIDProperty(key,(UUID)value);
            // Set the property as a String
        return setProperty(key,value.toString());
    }
    /**
     * 
     * @param map 
     */
    public synchronized void addProperties(Map<?,?> map){
            // Make sure the map isn't null
        if (map == null)
            throw new NullPointerException();
            // Go through the entries in the map
        for (Map.Entry<?,?> entry : map.entrySet()){
                // Set the property for that entry
            setProperty(entry.getKey().toString(),entry.getValue());
        }
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Integer getIntProperty(String key, Integer defaultValue){
            // Get the value from the properties, as a String
        String value = getProperty(key);
            // If the value is not null and not empty
        if (value != null && !value.isEmpty())
            try{    // Try to parse the value and return it
                return Integer.valueOf(value);
            } catch (NumberFormatException ex){ }
        return defaultValue;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Integer getIntProperty(String key){
        return getIntProperty(key,null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Long getLongProperty(String key, Long defaultValue){
            // Get the value from the properties, as a String
        String value = getProperty(key);
            // If the value is not null and not empty
        if (value != null && !value.isEmpty())
            try{    // Try to parse the value and return it
                return Long.valueOf(value);
            } catch (NumberFormatException ex){ }
        return defaultValue;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Long getLongProperty(String key){
        return getLongProperty(key,null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Boolean getBooleanProperty(String key, Boolean defaultValue){
            // Get the value from the properties
        Boolean value = booleanValueOf(getProperty(key));
            // If the value is not null, return it. Otherwise, return the given 
            // default value
        return (value != null) ? value : defaultValue;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Boolean getBooleanProperty(String key){
        return getBooleanProperty(key,null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Float getFloatProperty(String key, Float defaultValue){
            // Get the value from the properties, as a String
        String value = getProperty(key);
            // If the value is not null and not empty
        if (value != null && !value.isEmpty())
            try{    // Try to parse the value and return it
                return Float.valueOf(value);
            } catch (NumberFormatException ex){ }
        return defaultValue;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Float getFloatProperty(String key){
        return getFloatProperty(key,null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Double getDoubleProperty(String key, Double defaultValue){
            // Get the value from the properties, as a String
        String value = getProperty(key);
            // If the value is not null and not empty
        if (value != null && !value.isEmpty())
            try{    // Try to parse the value and return it
                return Double.valueOf(value);
            } catch (NumberFormatException ex){ }
        return defaultValue;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Double getDoubleProperty(String key){
        return getDoubleProperty(key,null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public byte[] getByteArrayProperty(String key, byte[] defaultValue){
            // Get the value from the properties, as a String
        String value = getProperty(key);
            // If the value is not null and not empty
        if (value != null && !value.isEmpty())
            try{    // Try to decode the value in Base64 into an array of bytes
                return Base64.getDecoder().decode(value);
            } catch (IllegalArgumentException ex) {}
        return defaultValue;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public byte[] getByteArrayProperty(String key){
        return getByteArrayProperty(key,null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Dimension getDimensionProperty(String key, Dimension defaultValue){
            // Get the dimensions as a byte array. Then convert the byte array 
            // into a dimension object, defaulting to the given default value 
            // if null
        return ConfigUtilities.dimensionFromByteArray(getByteArrayProperty(key),
                defaultValue);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Dimension getDimensionProperty(String key){
        return getDimensionProperty(key, null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Point getPointProperty(String key, Point defaultValue){
            // Get the point as a byte array. Then convert the byte array into a 
            // point object, defaulting to the given default value if null
        return ConfigUtilities.pointFromByteArray(getByteArrayProperty(key),
                defaultValue);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Point getPointProperty(String key){
        return getPointProperty(key, null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public Rectangle getRectangleProperty(String key, Rectangle defaultValue){
            // Get the rectangle as a byte array. Then convert the byte array 
            // into a rectangle object, defaulting to the given default value if 
            // null
        return ConfigUtilities.rectangleFromByteArray(getByteArrayProperty(key),
                defaultValue);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public Rectangle getRectangleProperty(String key){
        return getRectangleProperty(key, null);
    }
    /**
     * 
     * @param key
     * @param value 
     * @return  
     */
    public Object setUUIDProperty(String key, UUID value){
        return setProperty(key,(value!=null)?value.toString():null);
    }
    /**
     * 
     * @param key
     * @param defaultValue
     * @return 
     */
    public UUID getUUIDProperty(String key, UUID defaultValue){
        String value = getProperty(key);
        if (value != null && !value.isEmpty()){
            try{
                return UUID.fromString(value);
            } catch (IllegalArgumentException ex){ }
        }
        return defaultValue;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public UUID getUUIDProperty(String key){
        return getUUIDProperty(key,null);
    }
}
