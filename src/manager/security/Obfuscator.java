/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.security;

import java.security.*;
import java.util.*;

/**
 *
 * @author Milo Steier
 */
public abstract class Obfuscator {
    /**
     * 
     */
    public static final String MD5_HASH_ALGORITHM = "MD5";
    /**
     * 
     */
    public static final String SHA_1_HASH_ALGORITHM = "SHA-1";
    /**
     * 
     */
    public static final String SHA_256_HASH_ALGORITHM = "SHA-256";
    /**
     * 
     */
    public static final String SHA3_256_HASH_ALGORITHM = "SHA3-256";
    
    
    
    /**
     * 
     * @param bytes
     * @return 
     */
    public static String bytesToString(byte[] bytes){
            // This will get the decoded String
        String text = "";
            // Go through the decoded bytes, one characters worth at a time
        for (int i = 0; i < bytes.length; i+=Character.BYTES){
                // This will get the character that the next two bytes encode
            int c = 0;
                // Go through the next two bytes to get the character they encode
            for (int j = 0; j < Character.BYTES && i + j < bytes.length; j++){
                    // Append the current byte to the character
                c |= (Byte.toUnsignedInt(bytes[i+j]) << (Byte.SIZE*j));
            }   // Add the character to the decoded String
            text += (char) (c & ((int)Character.MAX_VALUE));
        }
        return text;
    }
    /**
     * 
     * @param str
     * @return 
     */
    public static byte[] stringToBytes(String str){
            // Create an array that is as long as the given String times the 
            // amount of bytes in a character
        byte[] bytes = new byte[str.length()*Character.BYTES];
            // Go through the characters in the given String
        for (int i = 0; i < str.length(); i++){
                // Get the current character, as an integer
            int c = (int) str.charAt(i);
                // Get the position in the bytes array
            int j = i*Character.BYTES;
                // Go through the bytes in a character, shifting the current 
                // character by a byte each time to get the next byte
            for (int k = 0; k < Character.BYTES; k++, c >>= Byte.SIZE){
                    // Get the current byte in the character
                bytes[j+k] = (byte) (c & 0xFF);
            }
        }
        return bytes;
    }
    /**
     * 
     * @param bytes
     * @return 
     */
    public static String bytesToHexString(byte[] bytes){
            // This will get the hex string
        String text = "";
        for (byte b : bytes){
            text += String.format("%02X", Byte.toUnsignedInt(b));
        }
        return text;
    }
    /**
     * 
     * @param str
     * @return 
     */
    public static byte[] bytesFromHexString(String str){
        if (str.length() % 2 != 0)
            str += "0";
        byte[] bytes = new byte[Math.floorDiv(str.length(), 2)];
        for (int i = 0; i < bytes.length; i++){
            int j = i * 2;
            try{
                bytes[i] = (byte) (Integer.parseInt(str.substring(j, j+2), 16) & 0xFF);
            } catch(NumberFormatException ex){}
        }
        return bytes;
    }
    /**
     * This decodes the given Base64 encoded String, with the bytes that make up 
     * each character encoded in little-endian in the given String.
     * @param str The Base64 encoded String to decode.
     * @return The decoded String.
     */
    public static String decodeBase64(String str){
        return bytesToString(Base64.getDecoder().decode(str));
    }
    /**
     * This encodes the given String in Base64, with the characters encoded in 
     * little-endian, byte wise.
     * @param str The String to encode.
     * @return The String encoded in Base64.
     */
    public static String encodeBase64(String str){
            // Encode the byte array into Base64
        return Base64.getEncoder().encodeToString(stringToBytes(str));
    }
    /**
     * This reverses a String, making it read backwards.
     * @param str The String to reverse.
     * @return The backwards String.
     */
    public static String reverseString(String str){
            // This will get the reversed String
        String newStr = "";
            // Go through the characters in the given String
        for (char c : str.toCharArray())
            newStr = c + newStr;
        return newStr;
    }
//    /**
//     * 
//     * @param str
//     * @param sectLength
//     * @return 
//     */
//    public static String reverseString(String str, int sectLength){
//        if (sectLength >= str.length() || sectLength <= 0)
//            return str;
//        if (sectLength == 1)
//            return reverseString(str);
//        String newStr = "";
//        for (int i = 0; i < str.length(); i+= sectLength){
//            newStr = str.substring(i, Math.min(i+sectLength, str.length()))+newStr;
//        }
//        return newStr;
//    }
    /**
     * This generates a randomly generated String that is the given length and 
     * using characters between the given {@code min} and {@code max} values.
     * @param rand The random number generator to use.
     * @param length The length of the generated String.
     * @param min The smallest character that can 
     * @param max Inclusive
     * @return 
     */
    public static String generateRandomString(Random rand, int length, int min, 
            int max){
            // If the smallest character is larger than the largest character
        if (min > max)
            throw new IllegalArgumentException("Minimum character cannot be "
                    + "greater than maximum character ("+min+" > " + max+")");
            // If the smallest character is too small
        if (min < 0)
            throw new IllegalArgumentException("Minimum character cannot be negative ("+min + " < 0)");
            // If the largest character is too large
        if (max >= CipherMap.MAX_CHAR_VALUE)
            throw new IllegalArgumentException("Maximum character cannot be greater than \\uFFFF ("
                    +max+" > "+(CipherMap.MAX_CHAR_VALUE-1)+")");
            // Manipulate the maximum to get the bounds for the random numbers
        max -= min;
        max++;
            // This will get the generate string
        String text = "";
            // A for loop to generate the characters
        for (int i = 0; i < length; i++){
            text += (char) (min+rand.nextInt(max));
        }
        return text;
    }
    /**
     * 
     * @param rand The random number generator to use.
     * @param length The length of the generated String.
     * @param min
     * @param max
     * @return 
     */
    public static String generateRandomString(Random rand, int length, char min,
            char max){
        return generateRandomString(rand,length,(int)min,(int)max);
    }
    /**
     * 
     * @param rand The random number generator to use.
     * @param length The length of the generated String.
     * @param max
     * @return 
     */
    public static String generateRandomString(Random rand, int length, int max){
        return generateRandomString(rand,length,0,max);
    }
    /**
     * 
     * @param rand The random number generator to use.
     * @param length The length of the generated String.
     * @param max
     * @return 
     */
    public static String generateRandomString(Random rand, int length, char max){
        return generateRandomString(rand,length,Character.MIN_VALUE,max);
    }
    /**
     * 
     * @param rand The random number generator to use.
     * @param length The length of the generated String.
     * @return 
     */
    public static String generateRandomString(Random rand, int length){
        return generateRandomString(rand,length,(int)Character.MAX_VALUE);
    }
    /**
     * 
     * @param str
     * @param algorithm
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public static byte[] generateHash(String str, String algorithm) throws NoSuchAlgorithmException{
        return MessageDigest.getInstance(algorithm).digest(stringToBytes(str));
    }
    /**
     * 
     * @param str
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public static byte[] generateHashMD5(String str) throws NoSuchAlgorithmException{
        return generateHash(str,MD5_HASH_ALGORITHM);
    }
    /**
     * 
     * @param str
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public static byte[] generateHashSHA_1(String str) throws NoSuchAlgorithmException{
        return generateHash(str,SHA_1_HASH_ALGORITHM);
    }
    /**
     * 
     * @param str
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public static byte[] generateHashSHA_256(String str) throws NoSuchAlgorithmException{
        return generateHash(str,SHA_256_HASH_ALGORITHM);
    }
    /**
     * 
     * @param str
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public static byte[] generateHashSHA3_256(String str) throws NoSuchAlgorithmException{
        return generateHash(str,SHA3_256_HASH_ALGORITHM);
    }
    
    
    
    public static Obfuscator getInstance(){
        return new ObfuscatorImpl();
    }
    
    
    
    protected Obfuscator(){ }
    /**
     * 
     * @param text The text to encrypt or decrypt.
     * @param decipher Whether this should encrypt or decrypt the given text.
     * @param rand The random number generator to use.
     * @return 
     */
    public abstract String insertRandomData(String text, boolean decipher, 
            Random rand);
    /**
     * 
     * @param text The text to encrypt or decrypt.
     * @param decipher Whether this should encrypt or decrypt the given text.
     * @param action
     * @param rand The random number generator to use.
     * @return 
     */
    public abstract String applyCipher(String text, boolean decipher, int action, 
            Random rand);
    /**
     * 
     * @return 
     */
    public abstract int getMaximumCipherAction();
    /**
     * 
     * @param rand
     * @return 
     */
    protected abstract int getCipherActionCount(Random rand);
    /**
     * 
     * @param text
     * @param decipher
     * @param rand
     * @param actionCount
     * @param maxAction
     * @param base64
     * @return 
     */
    protected String applyCiphers(String text,boolean decipher,Random rand, 
            int actionCount, int maxAction, boolean base64){
        ArrayDeque<Integer> actions = new ArrayDeque<>();
        ArrayDeque<Long> seeds = new ArrayDeque<>();
        for (int i = 0; i < actionCount; i++){
            long seed = rand.nextLong();
            int action = rand.nextInt(maxAction);
            if (decipher){
                seeds.add(seed);
                actions.add(action);
            } else {
                seeds.push(seed);
                actions.push(action);
            }
        }
        rand = new Random();
        if (decipher && base64)
            text = decodeBase64(text);
        while (!actions.isEmpty() && !seeds.isEmpty()){
            rand.setSeed(seeds.poll());
            text = applyCipher(text,decipher,actions.poll(),rand);
        }
        if (!decipher && base64)
            return encodeBase64(text);
        return text;
    }
    /**
     * 
     * @param text
     * @param decipher
     * @param rand
     * @param actionCount
     * @param base64
     * @return 
     */
    protected String applyCiphers(String text,boolean decipher,Random rand, 
            int actionCount, boolean base64){
        return applyCiphers(text,decipher,rand,actionCount,
                getMaximumCipherAction(),base64);
    }
    /**
     * 
     * @param text
     * @param decipher
     * @param rand
     * @param base64
     * @return 
     */
    protected String applyCiphers(String text,boolean decipher,Random rand,boolean base64){
        return applyCiphers(text,decipher,rand,getCipherActionCount(rand),base64);
    }
    /**
     * 
     * @param text
     * @param decipher
     * @param rand
     * @param actionCount
     * @param maxAction
     * @return 
     */
    public String applyCiphers(String text,boolean decipher,Random rand, 
            int actionCount, int maxAction){
        return applyCiphers(text,decipher,rand,actionCount,maxAction,true);
    }
    /**
     * 
     * @param text
     * @param decipher
     * @param seed
     * @param actionCount
     * @param maxAction
     * @return 
     */
    public String applyCiphers(String text,boolean decipher,long seed, 
            int actionCount, int maxAction){
        return applyCiphers(text,decipher,new Random(seed),actionCount,maxAction);
    }
    /**
     * 
     * @param text
     * @param decipher
     * @param rand
     * @param actionCount
     * @return 
     */
    public String applyCiphers(String text,boolean decipher,Random rand, 
            int actionCount){
        return applyCiphers(text,decipher,rand,actionCount,true);
    }
    /**
     * 
     * @param text
     * @param decipher
     * @param seed
     * @param actionCount
     * @return 
     */
    public String applyCiphers(String text,boolean decipher,long seed, 
            int actionCount){
        return applyCiphers(text,decipher,new Random(seed),actionCount);
    }
    /**
     * 
     * @param text
     * @param decipher
     * @param rand
     * @return 
     */
    public String applyCiphers(String text,boolean decipher,Random rand){
        return applyCiphers(text,decipher,rand,true);
    }
    /**
     * 
     * @param text
     * @param decipher
     * @param seed
     * @return 
     */
    public String applyCiphers(String text,boolean decipher,long seed){
        return applyCiphers(text,decipher,new Random(seed));
    }
    /**
     * 
     * @param text
     * @param decrypt
     * @param seed
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     * @throws java.security.SignatureException 
     */
    public abstract String encryptText(String text, boolean decrypt, long seed) 
            throws NoSuchAlgorithmException, SignatureException;
    /**
     * 
     * @return 
     */
    public abstract int getEncryptionVersion();
}
