/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.security;

import java.security.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 *
 * @author Mosblinker
 */
public class CipherUtils {
    /**
     * This is the SecureRandom used to generate random numbers for the cipher.
     */
    private SecureRandom rand;
    /**
     * The key generator used to generate the secret keys.
     */
    private KeyGenerator keyGen;
    /**
     * This is the secret key used for the cipher.
     */
    private SecretKey secretKey = null;
    /**
     * This is the IV Parameter used for the cipher.
     */
    private IvParameterSpec ivParam = null;
    /**
     * 
     * @param rand 
     * @param keyGen 
     */
    public CipherUtils(SecureRandom rand, KeyGenerator keyGen){
        this.rand = Objects.requireNonNull(rand);
        this.keyGen = keyGen;
    }
    /**
     * 
     * @param rand 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public CipherUtils(SecureRandom rand) throws NoSuchAlgorithmException{
        this(rand,null);
        CipherUtils.this.setKeyGenerator();
    }
    /**
     * 
     * @throws java.security.NoSuchAlgorithmException
     */
    public CipherUtils() throws NoSuchAlgorithmException{
        this(SecureRandom.getInstanceStrong());
    }
    /**
     * 
     * @param utils 
     */
    public CipherUtils(CipherUtils utils){
        this.rand = utils.rand;
        this.keyGen = utils.keyGen;
        this.secretKey = utils.secretKey;
        this.ivParam = utils.ivParam;
    }
    /**
     * 
     * @return 
     */
    public CipherUtils createBlankClone(){
        return new CipherUtils(rand,keyGen);
    }
    /**
     * 
     * @return 
     */
    public SecureRandom getRandom(){
        return rand;
    }
    /**
     * 
     * @param rand 
     * @return  
     */
    public CipherUtils setRandom(SecureRandom rand){
        this.rand = Objects.requireNonNull(rand);
        return this;
    }
    /**
     * 
     * @return 
     */
    public KeyGenerator getKeyGenerator(){
        return keyGen;
    }
    /**
     * 
     * @param keyGen 
     * @return  
     */
    public CipherUtils setKeyGenerator(KeyGenerator keyGen){
        this.keyGen = keyGen;
        return this;
    }
    /**
     * 
     * @param rand
     * @return
     * @throws NoSuchAlgorithmException 
     */
    protected KeyGenerator createKeyGenerator(SecureRandom rand) throws 
            NoSuchAlgorithmException{
        return CipherUtilities.getKeyGenerator(rand);
    }
    /**
     * 
     * @return 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public CipherUtils setKeyGenerator() throws NoSuchAlgorithmException{
        return setKeyGenerator(createKeyGenerator(getRandom()));
    }
    /**
     * 
     * @return 
     */
    public SecretKey getKey(){
        return secretKey;
    }
    /**
     * 
     * @param key
     * @return 
     */
    public CipherUtils setKey(SecretKey key){
        this.secretKey = key;
        return this;
    }
    /**
     * 
     * @return 
     */
    public IvParameterSpec getIV(){
        return ivParam;
    }
    /**
     * 
     * @param iv
     * @return 
     */
    public CipherUtils setIV(IvParameterSpec iv){
        this.ivParam = iv;
        return this;
    }
    /**
     * 
     * @param iv
     * @return 
     */
    public CipherUtils setIV(byte[] iv){
        return setIV(new IvParameterSpec(iv));
    }
    /**
     * 
     * @return 
     */
    public byte[] getEncryptionKey(){
        return CipherUtilities.getEncryptionKey(getKey(), getIV());
    }
    /**
     * 
     * @param key
     * @param iv
     * @return 
     */
    public CipherUtils setEncryptionKey(SecretKey key, IvParameterSpec iv){
        return setKey(key).setIV(iv);
    }
    /**
     * 
     * @param key
     * @param iv
     * @return 
     */
    public CipherUtils setEncryptionKey(SecretKey key, byte[] iv){
        return setEncryptionKey(key,new IvParameterSpec(iv));
    }
    /**
     * 
     * @param key
     * @return 
     */
    public CipherUtils setEncryptionKey(byte[] key){
        return setEncryptionKey(
                CipherUtilities.getSecretKeyFromEncryptionKey(key),
                CipherUtilities.getIVFromEncryptionKey(key));
    }
    /**
     * 
     * @param keyGen
     * @return 
     */
    protected SecretKey generateKey(KeyGenerator keyGen){
        return keyGen.generateKey();
    }
    /**
     * 
     * @param rand
     * @return 
     */
    protected IvParameterSpec generateIV(SecureRandom rand){
        return CipherUtilities.generateIV(rand);
    }
    /**
     * 
     * @return 
     */
    public CipherUtils generateEncryptionKey(){
        return setEncryptionKey(generateKey(getKeyGenerator()),
                generateIV(getRandom()));
    }
    /**
     * 
     * @return 
     */
    public CipherUtils clearEncryptionKey(){
        return setKey(null).setIV((IvParameterSpec)null);
    }
}
