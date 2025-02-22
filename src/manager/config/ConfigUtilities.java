/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.config;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.nio.*;
import java.util.Objects;

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
     * @param value
     * @param arr
     * @param offset
     * @param size The number of bytes that are in the value.
     * @return 
     */
    private static byte[] toByteArray(long value, byte[] arr, int offset, int size){
            // If the offset into the array is negative
        if (offset < 0)
            throw new IndexOutOfBoundsException("Array offset cannot be negative ("+
                    offset+")");
            // Expand the byte array if it's too small (or create it if it's 
        arr = expandByteArray(arr, offset+size);    // null)
            // A for loop to go through the bytes of the given value
        for (int i = 0; i < size && value != 0; i++){
                // Add the current least significant byte to the array
            arr[offset+i] = (byte)(value & 0xFF);
                // Shift the value by one byte
            value >>= Byte.SIZE;
        }
        return arr;
    }
    /**
     * 
     * @param value
     * @param arr
     * @param offset
     * @return 
     */
    public static byte[] toByteArray(int value, byte[] arr, int offset){
        return toByteArray(Integer.toUnsignedLong(value),arr,offset,
                Integer.BYTES);
    }
    /**
     * 
     * @param value
     * @param arr
     * @param offset
     * @return 
     */
    public static byte[] toByteArray(short value, byte[] arr, int offset){
        return toByteArray(Short.toUnsignedLong(value),arr,offset,Short.BYTES);
    }
    /**
     * 
     * @param value
     * @param arr
     * @param offset
     * @return 
     */
    public static byte[] toByteArray(long value, byte[] arr, int offset){
        return toByteArray(value, arr, offset, Long.BYTES);
    }
    /**
     * 
     * @param value
     * @param arr
     * @param offset
     * @return 
     */
    public static byte[] toByteArray(float value, byte[] arr, int offset){
        return toByteArray(Float.floatToIntBits(value),arr,offset);
    }
    /**
     * 
     * @param value
     * @param arr
     * @param offset
     * @return 
     */
    public static byte[] toByteArray(double value, byte[] arr, int offset){
        return toByteArray(Double.doubleToLongBits(value),arr,offset,
                Double.BYTES);
    }
    /**
     * 
     * @param value
     * @param arr
     * @param offset
     * @return 
     */
    public static byte[] toByteArray(char value, byte[] arr, int offset){
        return toByteArray((short)value, arr, offset);
    }
    /**
     * 
     * @param value
     * @param offset
     * @param size The number of bytes that are in the value to return.
     * @return 
     */
    private static Number toNumber(byte[] value, int offset, int size){
            // Check if the array is null
        Objects.requireNonNull(value);
            // Check if the offset is in the array
        Objects.checkIndex(offset, value.length);
            // This will get the value to return
        long number = 0;
           // Go through the bytes for the value, making sure not to exceed the 
           // size of the array
        for (int i = 0; i < size && i+offset < value.length; i++){
                // Get the current byte and bit-shift it into place
            number |= Byte.toUnsignedInt(value[i+offset]) << (Byte.SIZE*i);
        }
        return number;
    }
    /**
     * 
     * @param value
     * @param offset
     * @return 
     */
    public static int toInteger(byte[] value, int offset){
        return toNumber(value,offset,Integer.BYTES).intValue();
    }
    /**
     * 
     * @param value
     * @param offset
     * @return 
     */
    public static short toShort(byte[] value, int offset){
        return toNumber(value,offset,Short.BYTES).shortValue();
    }
    /**
     * 
     * @param value
     * @param offset
     * @return 
     */
    public static long toLong(byte[] value, int offset){
        return toNumber(value,offset,Long.BYTES).longValue();
    }
    /**
     * 
     * @param value
     * @param offset
     * @return 
     */
    public static float toFloat(byte[] value, int offset){
        return Float.intBitsToFloat(toInteger(value,offset));
    }
    /**
     * 
     * @param value
     * @param offset
     * @return 
     */
    public static double toDouble(byte[] value, int offset){
        return Double.longBitsToDouble(toLong(value, offset));
    }
    /**
     * 
     * @param value
     * @param offset
     * @return 
     */
    public static char toCharacter(byte[] value, int offset){
        return (char) toShort(value,offset);
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
     * @param values
     * @param arr
     * @param offset
     * @return 
     */
    public static byte[] intArrayToBytes(int[] values, byte[] arr, int offset){
            // Go through the array of integers
        for (int i : values){
                // Add the integer to the byte array
            arr = toByteArray(i,arr,offset);
                // Next integer is offset by the number of bytes
            offset += Integer.BYTES;
        }
        return arr;
    }
    /**
     * 
     * @param value
     * @param offset
     * @param length
     * @return 
     */
    public static int[] intArrayFromBytes(byte[] value, int offset, int length){
            // An array to get the integers in the byte array
        int[] arr = new int[length];
            // Go through the integers in the byte array for as long as there 
            // are integer to add to the integer array
        for (int i = 0; i < length && offset < value.length; i++, 
                offset+=Integer.BYTES){
                // Get the next integer from the array
            arr[i] = toInteger(value,offset);
        }
        return arr;
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
            // Put the two integers into an array of integers, and convert that 
            // into an array of bytes
        return intArrayToBytes(new int[]{x,y},null,0);
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
            // If the given array is null or the array is not two integers long
        if (value == null || value.length != Integer.BYTES*2)
            return defaultValue;
            // Convert the array of bytes into two integers
        int[] arr = intArrayFromBytes(value,0,2);
            // Create and return a new Point object with the two integers
        return new Point(arr[0],arr[1]);
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
            // Put the 4 integers into an array of integers, and convert that 
            // into an array of bytes
        return intArrayToBytes(new int[]{x,y,width,height},null,0);
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
            // If the given array is null or the given array is neither 2 or 4 
            // integers long
        if (value == null || !(value.length == Integer.BYTES*2 || 
                value.length == Integer.BYTES*4))
            return defaultValue;
            // Convert the array of bytes into 4 integers
        int[] arr = intArrayFromBytes(value,0,4);
            // If there were actually only 2 integers in the byte array
        if (value.length <= Integer.BYTES*2)
                // Create and return a new Rectangle object with the 2 integers 
                // as the size of the rectangle
            return new Rectangle(arr[0],arr[1]);
            // Create and return a new Rectangle object with the 4 integers
        return new Rectangle(arr[0],arr[1],arr[2],arr[3]);
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
