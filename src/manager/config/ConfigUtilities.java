/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.config;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.*;

/**
 * A utility library used with configuration stuff.
 * @author Mosblinker
 */
public class ConfigUtilities {
    /**
     * This is the header short for dimensions stored in byte arrays. This 
     * should result in a Base64 encoded String that starts with "DM".
     */
    public static final short DIMENSION_BYTE_ARRAY_HEADER = (short) 0x0CC3;
    /**
     * This is the header short for points stored in byte arrays. This should 
     * result in a Base64 encoded String that starts with "PT".
     */
    public static final short POINT_BYTE_ARRAY_HEADER = (short) 0x3D30;
    /**
     * This is the header short for rectangles stored in byte arrays. This 
     * should result in a Base64 encoded String that starts with "RT".
     */
    public static final short RECTANGLE_BYTE_ARRAY_HEADER = (short) 0x4530;
    /**
     * This class cannot be constructed.
     */
    private ConfigUtilities() {}
    /**
     * 
     * @param arr
     * @param length
     * @return 
     */
    public static byte[] expandByteArray(byte[] arr, int length){
            // If the given byte array is null
        if (arr == null)
                // Create a new array with the correct length
            arr = new byte[length];
            // If the given byte array is too short
        else if (arr.length < length){
                // Store the array in a temporary variable
            byte[] temp = arr;
                // Create a new array with the correct length
            arr = new byte[length];
                // Copy the contents of the old array into the new array
            System.arraycopy(temp, 0, arr, 0, temp.length);
        }
        return arr;
    }
    /**
     * 
     * @param header
     * @param values
     * @return 
     */
    private static byte[] intArraytoByteArray(short header, int... values){
            // This will get a byte array representation of the given integers
        byte[] arr = new byte[Short.BYTES+(Integer.BYTES*values.length)];
            // Wrap the byte array with a byte buffer to write to it
        ByteBuffer buffer = ByteBuffer.wrap(arr);
            // Add the given header to the buffer. This will signify what type 
            // of data this byte array represents.
        buffer.putShort(header);
            // Go through the integers in the given array
        for (int i : values){
                // Add the current integer to the buffer
            buffer.putInt(i);
        }
        return arr;
    }
    /**
     * 
     * @param header
     * @param value
     * @return 
     */
    private static IntBuffer toIntBuffer(short header, byte[] value){
            // Wrap the byte array with a read only byte buffer to read from to 
        ByteBuffer buffer = ByteBuffer.wrap(value).asReadOnlyBuffer();  // it
        try{    // If the byte array's header matches the given header
            if (buffer.getShort() == header){
                    // Return the byte buffer as an IntBuffer
                return buffer.asIntBuffer();
            }
        } catch (BufferUnderflowException ex){ }
        return null;
    }
    /**
     * 
     * @param width
     * @param height
     * @return 
     */
    public static byte[] dimensionToByteArray(int width, int height){
        return intArraytoByteArray(DIMENSION_BYTE_ARRAY_HEADER,width,height);
    }
    /**
     * 
     * @param value
     * @return 
     */
    public static byte[] dimensionToByteArray(Dimension value){
            // If the given dimension object is null
        if (value == null)
            return null;
        return dimensionToByteArray(value.width,value.height);
    }
    /**
     * 
     * @param value
     * @param defaultValue
     * @return 
     */
    public static Dimension dimensionFromByteArray(byte[] value, 
            Dimension defaultValue){
            // If the given array is null
        if (value == null)
            return defaultValue;
            // Get an integer buffer to get the values for the dimension
        IntBuffer buffer = toIntBuffer(DIMENSION_BYTE_ARRAY_HEADER,value);
            // If the buffer is null (did not match the header) or there aren't 
            // 2 integers in the buffer
        if (buffer == null || buffer.remaining() != 2)
            return defaultValue;
        return new Dimension(buffer.get(),buffer.get());
    }
    /**
     * 
     * @param value
     * @return 
     */
    public static Dimension dimensionFromByteArray(byte[] value){
        return dimensionFromByteArray(value,null);
    }
    /**
     * 
     * @param x
     * @param y
     * @return 
     */
    public static byte[] pointToByteArray(int x, int y){
        return intArraytoByteArray(POINT_BYTE_ARRAY_HEADER,x,y);
    }
    /**
     * 
     * @param value
     * @return 
     */
    public static byte[] pointToByteArray(Point value){
            // If the given point object is null
        if (value == null)
            return null;
        return pointToByteArray(value.x,value.y);
    }
    /**
     * 
     * @param value
     * @param defaultValue
     * @return 
     */
    public static Point pointFromByteArray(byte[] value, Point defaultValue){
            // If the given array is null
        if (value == null)
            return defaultValue;
            // Get an integer buffer to get the values for the point
        IntBuffer buffer = toIntBuffer(POINT_BYTE_ARRAY_HEADER,value);
            // If the buffer is null (did not match the header) or there aren't 
            // 2 integers in the buffer
        if (buffer == null || buffer.remaining() != 2)
            return defaultValue;
        return new Point(buffer.get(),buffer.get());
    }
    /**
     * 
     * @param value
     * @return 
     */
    public static Point pointFromByteArray(byte[] value){
        return pointFromByteArray(value,null);
    }
    /**
     * 
     * @param x
     * @param y
     * @param width
     * @param height
     * @return 
     */
    public static byte[] rectangleToByteArray(int x,int y,int width,int height){
        return intArraytoByteArray(RECTANGLE_BYTE_ARRAY_HEADER,x,y,width,height);
    }
    /**
     * 
     * @param width
     * @param height
     * @return 
     */
    public static byte[] rectangleToByteArray(int width,int height){
        return rectangleToByteArray(0,0,width,height);
    }
    /**
     * 
     * @param value
     * @return 
     */
    public static byte[] rectangleToByteArray(Rectangle value){
            // If the given rectangle object is null
        if (value == null)
            return null;
            // Convert the rectangle object into an array of integers, and 
            // convert that into an array of bytes
        return rectangleToByteArray(value.x,value.y,value.width,value.height);
    }
    /**
     * 
     * @param value
     * @param defaultValue
     * @return 
     */
    public static Rectangle rectangleFromByteArray(byte[] value, 
            Rectangle defaultValue){
            // If the given array is null
        if (value == null)
            return defaultValue;
            // Get an integer buffer to get the values for the rectangle
        IntBuffer buffer = toIntBuffer(RECTANGLE_BYTE_ARRAY_HEADER,value);
            // If the buffer is not null (the byte array matched the header)
        if (buffer != null){
                // If there are two integers in the buffer
            if (buffer.remaining() == 2)
                return new Rectangle(buffer.get(),buffer.get());
                // If there are four integers in the buffer
            else if (buffer.remaining() == 4)
                return new Rectangle(buffer.get(),buffer.get(),
                        buffer.get(),buffer.get());
        }
        return defaultValue;
    }
    /**
     * 
     * @param value
     * @return 
     */
    public static Rectangle rectangleFromByteArray(byte[] value){
        return rectangleFromByteArray(value,null);
    }
    /**
     * 
     * @param value
     * @return 
     */
    public static Boolean booleanValueOf(String value){
            // If the value is equal to the word "true", ignoring case
        if ("true".equalsIgnoreCase(value))
            return true;
            // If the value is equal to the word "false", ignoring case
        else if ("false".equalsIgnoreCase(value))
            return false;
        return null;
    }
    
}
