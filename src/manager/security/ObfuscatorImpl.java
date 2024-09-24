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
class ObfuscatorImpl extends Obfuscator{
    /**
     * 
     */
    public static final int ENCRYPTION_CIPHER_TYPE_COUNT = 13;
    /**
     * 
     */
    public static final int ENCRYPTION_VERSION = 1;
    /**
     * 
     */
    protected static final String[] SECURE_HASH_ALGORITHMS = {
        SHA3_256_HASH_ALGORITHM,
        SHA_256_HASH_ALGORITHM,
        SHA_1_HASH_ALGORITHM
    };
    /**
     * 
     */
    public static final char ENCRYPTION_SEPARATOR_CHAR = ':';
    /**
     * 
     */
    public static final String ENCRYPTION_SEPARATOR = ""+ENCRYPTION_SEPARATOR_CHAR;
    /**
     * 
     */
    public static final int ENCRYPTION_CHECKSUM_SECTION_INDEX = 0;
    /**
     * 
     */
    public static final int ENCRYPTION_SIGNATURE_ALGORITHM_SECTION_INDEX = 1;
    /**
     * 
     */
    public static final int ENCRYPTION_SIGNATURE_SECTION_INDEX = 2;
    /**
     * 
     */
    public static final int ENCRYPTION_VERSION_SECTION_INDEX = 3;
    /**
     * 
     */
    public static final int ENCRYPTION_PUBLIC_KEY_SECTION_INDEX = 4;
    /**
     * 
     */
    public static final int ENCRYPTION_TEXT_SECTION_INDEX = 5;
    @Override
    public String insertRandomData(String text, boolean decipher, Random rand) {
        String startData = "";
        String endData = "";
        if (rand.nextBoolean())
            startData = generateRandomString(rand,rand.nextInt(20)+10);
        if (rand.nextBoolean())
            endData = generateRandomString(rand,rand.nextInt(20)+7);
        if (decipher)
            text = text.substring(startData.length(), text.length()-endData.length());
        for (int i = 0; i < text.length(); i+=10){
            if (rand.nextBoolean()){
                String extraData = generateRandomString(rand,rand.nextInt(20)+5);
                if (decipher){
                    text = text.substring(0, i)+text.substring(
                            Math.min(i+extraData.length(), text.length()));
                } else{
                    text = text.substring(0, i)+extraData+text.substring(i);
                    i += extraData.length();
                }
            }
        }
        return (decipher) ? text : startData+text+endData;
    }
    /**
     * {@inheritDoc }
     * <ol start="0">
     *  <li>Uncontrolled Caesar Cipher, randomly controlled, shifts between 
     * 3 - 40 (inclusive), direction is random</li>
     *  <li>Controlled Caesar Cipher, randomly controlled, shifts between 
     * 3 - 40 (inclusive), direction is random</li>
     *  <li>Uncontrolled Atbash cipher</li>
     *  <li>Controlled Atbash cipher</li>
     *  <li>Invert Character cipher</li>
     *  <li>Binary stream cipher</li>
     *  <li>Uncontrolled Shift stream cipher</li>
     *  <li>Controlled Shift stream cipher</li>
     *  <li>Rail fence cipher with 2 - 7 rails</li>
     *  <li>Scytale cipher with a radius of 2 - 7</li>
     *  <li>Columnar cipher with 3 - 8 columns</li>
     *  <li>Reverse string</li>
     *  <li>Insert random data</li>
     * </ol>
     * Any other number will result in a binary stream cipher being used.
     * 
     * @param text {@inheritDoc }
     * @param decipher {@inheritDoc }
     * @param action {@inheritDoc }
     * @param rand {@inheritDoc }
     * @return {@inheritDoc }
     */
    @Override
    public String applyCipher(String text, boolean decipher, int action, Random rand) {
        CipherMap cipher;   // The cipher map to use if one is to be used
            // Determine the action to perform
        switch(action){
            case(0):        // Use an uncontrolled Caesar cipher
            case(1):        // Use a controlled Caesar cipher
                cipher = new CipherMap.CaesarCipherMap(action == 1, 
                        rand.nextInt(37)+3, rand.nextBoolean());
                break;
            case(2):        // Use an uncontrolled Atbash cipher
                cipher = CipherMap.ATBASH_CIPHER;
                break;
            case(3):        // Use a controlled Atbash cipher
                cipher = CipherMap.CONTROLLED_ATBASH_CIPHER;
                break;
            case(4):        // Use an invert character cipher
                cipher = CipherMap.INVERT_CHARACTER_CIPHER;
                break;
                // Both case 5 and the default perform the same action
            case(5):        // Use a stream cipher
            default:        // Default to using a stream cipher
                return CipherMap.streamCipher(text, rand);
            case(6):        // Use an uncontrolled shift stream cipher
            case(7):        // Use a controlled shift stream cipher
                return CipherMap.shiftStreamCipher(text, decipher, rand, 
                        action % 2 != 0);
            case(8):
                return CipherMap.railFenceCipher(text, decipher, rand.nextInt(5)+2);
            case(9):
                return CipherMap.scytaleCipher(text, decipher, rand.nextInt(5)+2);
            case(10):
                return CipherMap.columnarCipher(text, decipher, rand.nextInt(5)+3, 
                        rand);
            case(11):
                return reverseString(text);
                
                
            case(12):
                return insertRandomData(text,decipher,rand);
            
        }
        if (decipher)
            cipher = cipher.getDecipherMap();
        if (rand.nextBoolean())
            return cipher.applyCipher(text, rand);
        return cipher.applyCipher(text);
    }
    @Override
    public int getMaximumCipherAction() {
        return ENCRYPTION_CIPHER_TYPE_COUNT;
    }
    @Override
    protected int getCipherActionCount(Random rand) {
        return rand.nextInt(15)+10;
    }
    @Override
    public String encryptText(String text, boolean decrypt, long seed) 
            throws NoSuchAlgorithmException, SignatureException {
        long publicSeed;
        if (decrypt){
            text = decodeBase64(text);
            String[] textSections = text.split(ENCRYPTION_SEPARATOR);
            byte[] checksum = bytesFromHexString(textSections[ENCRYPTION_CHECKSUM_SECTION_INDEX]);
            byte[] hash = generateHashMD5(text.substring(text.indexOf(ENCRYPTION_SEPARATOR)+1));
            if (!MessageDigest.isEqual(checksum, hash))
                throw new SignatureException("Checksum Error (MD5)");
            
                // Retrieve secure hash from token
            checksum = bytesFromHexString(textSections[ENCRYPTION_SIGNATURE_SECTION_INDEX]);
            String temp = textSections[ENCRYPTION_VERSION_SECTION_INDEX];
            for (int i = ENCRYPTION_VERSION_SECTION_INDEX+1; 
                    i < textSections.length; i++){
                temp += ENCRYPTION_SEPARATOR+textSections[i];
            }
            hash = generateHash(temp,textSections[ENCRYPTION_SIGNATURE_ALGORITHM_SECTION_INDEX]);
                // Check token against secure hash
            if (!MessageDigest.isEqual(checksum, hash))
                throw new SignatureException("Signature Error");
            
            if (Integer.parseInt(textSections[ENCRYPTION_VERSION_SECTION_INDEX]) 
                    != getEncryptionVersion())
                throw new SignatureException("Wrong Version");
                
                // Retrieve Public key from token
            publicSeed = Long.parseUnsignedLong(textSections[ENCRYPTION_PUBLIC_KEY_SECTION_INDEX], 16);
                // Deobfuscate the public key
            publicSeed = Long.rotateLeft(publicSeed, 8);
            publicSeed = Long.reverseBytes(publicSeed);
            publicSeed = Long.reverse(publicSeed);
            publicSeed = Long.rotateRight(publicSeed, 14);
            publicSeed = Long.reverse(publicSeed);
            publicSeed = Long.rotateLeft(publicSeed, 19);
            publicSeed = Long.reverse(publicSeed);
            publicSeed = Long.reverseBytes(publicSeed);
                // Decrypt token using public key before applying ciphers
            // String publicKey = textSections[ENCRYPTION_PUBLIC_KEY_SECTION_INDEX];
            text = textSections[ENCRYPTION_TEXT_SECTION_INDEX];
            
        } else {
            Random rand = new Random();
            publicSeed = rand.nextLong();
            while (seed == publicSeed)
                publicSeed = rand.nextLong();
        }
        text = applyCiphers(text,decrypt,new Random(seed ^ publicSeed),false);
        if (!decrypt){
                // Use another encryption algorithm with public and private keys
                
                // Obfuscate the public key
            publicSeed = Long.reverseBytes(publicSeed);
            publicSeed = Long.reverse(publicSeed);
            publicSeed = Long.rotateRight(publicSeed, 19);
            publicSeed = Long.reverse(publicSeed);
            publicSeed = Long.rotateLeft(publicSeed, 14);
            publicSeed = Long.reverse(publicSeed);
            publicSeed = Long.reverseBytes(publicSeed);
            publicSeed = Long.rotateRight(publicSeed, 8);
            
                // Append Public Key to token (currently blank)
            text = String.format("%d%s%016X%s%s",getEncryptionVersion(),
                    ENCRYPTION_SEPARATOR,publicSeed,
                    ENCRYPTION_SEPARATOR,text);
            
                // Generate secure hash for token
            String algorithm = null;
            byte[] hash = null;
            for (String a : SECURE_HASH_ALGORITHMS){
                try{
                    hash = generateHash(text,a);
                    algorithm = a;
                    break;
                } catch (NoSuchAlgorithmException ex){}
            }
            if (hash == null){
                hash = generateHashMD5(text);
                algorithm = MD5_HASH_ALGORITHM;
            }
            
                // Append secure hash to token
            text = String.format("%s%s%s%s%s", algorithm,ENCRYPTION_SEPARATOR,
                    bytesToHexString(hash),ENCRYPTION_SEPARATOR,text);
            
            String md5 = bytesToHexString(generateHashMD5(text));
            text = md5 + ENCRYPTION_SEPARATOR + text;
            text = encodeBase64(text);
        }
        return text;
    }
    @Override
    public int getEncryptionVersion() {
        return ENCRYPTION_VERSION;
    }
}
