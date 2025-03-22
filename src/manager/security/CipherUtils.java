/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.security;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.*;

/**
 *
 * @author Mosblinker
 */
public class CipherUtils {
    /**
     * This is the secret key used for the cipher.
     */
    protected SecretKey secretKey = null;
    /**
     * This is the IV Parameter used for the cipher.
     */
    protected IvParameterSpec ivParam = null;
    /**
     * This is the SecureRandom used to generate random numbers for the cipher.
     */
    protected SecureRandom secureRand = null;
    /**
     * The key generator used to generate the secret keys.
     */
    protected KeyGenerator keyGen = null;
    
    public CipherUtils(){
        
    }
    /**
     * 
     * @param utils 
     */
    public CipherUtils(CipherUtils utils){
        this.secretKey = utils.secretKey;
        this.ivParam = utils.ivParam;
        this.secureRand = utils.secureRand;
        this.keyGen = utils.keyGen;
    }
}
