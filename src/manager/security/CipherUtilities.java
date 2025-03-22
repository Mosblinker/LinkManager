/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.security;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.security.*;
import java.util.Objects;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 *
 * @author Mosblinker
 */
public class CipherUtilities {
    /**
     * This is the header short for encrypted values stored in byte arrays. This 
     * should result in a Base64 encoded String that starts with "EN".
     */
    public static final short ENCRYPTED_VALUE_HEADER = (short) 0x10D0;
    /**
     * This is the cipher algorithm that is used by the program, along with all 
     * the details of the variation being used.
     */
    public static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    /**
     * This is the overall cipher algorithm being used by the program.
     */
    public static final String CIPHER_KEY_ALGORITHM = CIPHER_ALGORITHM.substring(0, 3);
    /**
     * This is the amount of bytes in a block size in the AES encryption 
     * algorithm.
     */
    public static final int CIPHER_BLOCK_SIZE = 16;
    /**
     * This is the bit size of the AES encryption key.
     */
    public static final int CIPHER_KEY_BIT_SIZE = 256;
    /**
     * This class cannot be constructed.
     */
    private CipherUtilities() { }
    /**
     * 
     * @param rand
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public static KeyGenerator getKeyGenerator(SecureRandom rand) throws NoSuchAlgorithmException{
        KeyGenerator keyGen = KeyGenerator.getInstance(CIPHER_KEY_ALGORITHM);
        keyGen.init(CIPHER_KEY_BIT_SIZE, rand);
        return keyGen;
    }
    /**
     * 
     * @param rand
     * @return 
     */
    public static IvParameterSpec generateIV(SecureRandom rand){
        byte[] iv = new byte[CIPHER_BLOCK_SIZE];
        rand.nextBytes(iv);
        return new IvParameterSpec(iv);
    }
    /**
     * 
     * @param key
     * @param iv
     * @return 
     */
    public static byte[] getEncryptionKey(SecretKey key, byte[] iv){
        byte[] keyBytes = key.getEncoded();
        byte[] encrKey = new byte[keyBytes.length+iv.length];
        System.arraycopy(keyBytes, 0, encrKey, 0, keyBytes.length);
        System.arraycopy(iv, 0, encrKey, keyBytes.length, iv.length);
        return encrKey;
    }
    /**
     * 
     * @param key
     * @param iv
     * @return 
     */
    public static byte[] getEncryptionKey(SecretKey key, IvParameterSpec iv){
        return getEncryptionKey(key, iv.getIV());
    }
    /**
     * 
     * @param key
     * @return 
     */
    public static IvParameterSpec getIVFromEncryptionKey(byte[] key){
        return new IvParameterSpec(key,key.length-CIPHER_BLOCK_SIZE,CIPHER_BLOCK_SIZE);
    }
    /**
     * 
     * @param key
     * @return 
     */
    public static SecretKey getSecretKeyFromEncryptionKey(byte[] key){
        return new SecretKeySpec(key,0,key.length-CIPHER_BLOCK_SIZE,CIPHER_KEY_ALGORITHM);
    }
    /**
     * 
     * @param mode
     * @param key
     * @param iv
     * @param rand
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException 
     */
    public static Cipher createCipher(int mode,SecretKey key,IvParameterSpec iv, 
            SecureRandom rand) throws NoSuchAlgorithmException, 
            NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException{
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        cipher.init(mode, key, iv, rand);
        return cipher;
    }
    /**
     * 
     * @param key
     * @param iv
     * @param rand
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException 
     */
    public static Cipher createEncryptCipher(SecretKey key, IvParameterSpec iv, 
            SecureRandom rand) throws NoSuchAlgorithmException, 
            NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException{
        return createCipher(Cipher.ENCRYPT_MODE,key,iv,rand);
    }
    /**
     * 
     * @param key
     * @param iv
     * @param rand
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException 
     */
    public static Cipher createDecryptCipher(SecretKey key, IvParameterSpec iv, 
            SecureRandom rand) throws NoSuchAlgorithmException, 
            NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException{
        return createCipher(Cipher.DECRYPT_MODE,key,iv,rand);
    }
    /**
     * 
     * @param value
     * @param cipher
     * @return
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     * @throws NullPointerException
     */
    public static byte[] encryptByteArray(byte[] value, Cipher cipher) throws 
            IllegalBlockSizeException, BadPaddingException{
            // Check that the value is not null
        Objects.requireNonNull(value);
            // Encrypt the given byte array
        byte[] encrypted = cipher.doFinal(value);
            // This will get the encrypted byte array expanded by two bytes
        byte[] output = new byte[encrypted.length+2];
            // Create a byte buffer to write the expanded array
        ByteBuffer buffer = ByteBuffer.wrap(output);
            // Put the encrypted value header at the start
        buffer.putShort(ENCRYPTED_VALUE_HEADER);
            // Put the encrypted value into the byte buffer
        buffer.put(encrypted);
        return output;
    }
    /**
     * 
     * @param value
     * @param key
     * @param iv
     * @param rand
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     * @throws NullPointerException
     */
    public static byte[] encryptByteArray(byte[] value, SecretKey key, 
            IvParameterSpec iv, SecureRandom rand) throws NoSuchAlgorithmException, 
            NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException{
        return encryptByteArray(value,createEncryptCipher(key,iv,rand));
    }
    /**
     * 
     * @param encryptedValue
     * @param cipher
     * @return
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     * @throws NullPointerException
     */
    public static byte[] decryptByteArray(byte[] encryptedValue, Cipher cipher) 
            throws IllegalBlockSizeException, BadPaddingException{
            // Check that the value is not null
        Objects.requireNonNull(encryptedValue);
            // Wrap the byte array with a read only byte buffer to read from to 
            // it
        ByteBuffer buffer = ByteBuffer.wrap(encryptedValue).asReadOnlyBuffer(); 
        try{    // If the byte array's header matches the encrypted value header
            if (buffer.getShort() == ENCRYPTED_VALUE_HEADER){
                    // Decrypt all but the first two bytes, since the first two 
                    // bytes are a header
                return cipher.doFinal(encryptedValue, Short.BYTES, 
                        encryptedValue.length-Short.BYTES);
            }
        } catch (BufferUnderflowException ex){ }
        return null;
    }
    /**
     * 
     * @param encryptedValue
     * @param key
     * @param iv
     * @param rand
     * @return
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     * @throws NullPointerException
     */
    public static byte[] decryptByteArray(byte[] encryptedValue, SecretKey key, 
            IvParameterSpec iv, SecureRandom rand) throws NoSuchAlgorithmException, 
            NoSuchPaddingException, InvalidKeyException, 
            InvalidAlgorithmParameterException, IllegalBlockSizeException, 
            BadPaddingException{
        return decryptByteArray(encryptedValue,createDecryptCipher(key,iv,rand));
    }
}
