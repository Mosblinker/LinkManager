/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.security;

import java.security.*;
import java.util.*;
import javax.crypto.*;

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
    
}
