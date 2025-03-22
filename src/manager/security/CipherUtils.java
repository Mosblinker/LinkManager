/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.security;

import java.security.*;
import java.util.Objects;

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
     * 
     * @param rand 
     */
    public CipherUtils(SecureRandom rand){
        this.rand = Objects.requireNonNull(rand);
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
    }
    /**
     * 
     * @return 
     */
    public SecureRandom getSecureRandom(){
        return rand;
    }
    /**
     * 
     * @param rand 
     */
    public CipherUtils setSecureRandom(SecureRandom rand){
        this.rand = Objects.requireNonNull(rand);
        return this;
    }
}
