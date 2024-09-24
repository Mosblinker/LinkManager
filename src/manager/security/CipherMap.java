/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.security;

import java.util.*;

/**
 *
 * @author Milo Steier
 */
public abstract class CipherMap extends AbstractMap<Character,Character>{
    /**
     * This is the largest value that a character can have, as an integer, plus 
     * one. This is primarily used for when the range being used is exclusive of 
     * the largest value, such as when generating a random character.
     */
    protected static final int MAX_CHAR_VALUE = ((int)Character.MAX_VALUE)+1;
    /**
     * 
     */
    public static final CipherMap ATBASH_CIPHER = new AtbashCipherMap();
    
    public static final CipherMap CONTROLLED_ATBASH_CIPHER = new AtbashCipherMap(true);
    
    public static final CipherMap INVERT_CHARACTER_CIPHER = new InvertCharacterCipherMap();
    /**
     * This applies a binary additive synchronous stream cipher to the given 
     * String using the given random number generator. In this type of cipher, 
     * a stream of pseudo-random digits are generated independently of the text, 
     * which are then {@code XOR}'d with the characters of the text. This will 
     * either encrypt or decrypt the text, depending on whether the given text 
     * is plaintext (unencrypted) or ciphertext (encrypted), respectively. When 
     * decrypting ciphertext, this assumes that the random number generator is 
     * using the same seed that was used to encrypt the text, and that the given 
     * ciphertext will remain in sync with the random number generator. This 
     * does not do anything to maintain synchronization. For more information 
     * about binary additive synchronous stream ciphers, please refer to this 
     * <a href="https://en.wikipedia.org/wiki/Stream_cipher#Synchronous_stream_ciphers">
     * Wikipedia article on stream ciphers</a>
     * @param text The text to encrypt or decrypt.
     * @param rand The random number generator to use.
     * @return The resulting encrypted (if {@code text} is plaintext) or 
     * decrypted (if {@code text} is ciphertext) text.
     * @see <a href="https://en.wikipedia.org/wiki/Stream_cipher#Synchronous_stream_ciphers">
     * https://en.wikipedia.org/wiki/Stream_cipher#Synchronous_stream_ciphers</a>
     */
    public static String streamCipher(String text, Random rand){
            // This will get the version of the string with the cipher applied
        String newText = "";
            // Go through the characters in the given String
        for (char c : text.toCharArray()){
                // XOR each character in the given String with a random value
            newText += (char)(c ^ rand.nextInt(MAX_CHAR_VALUE));
        }
        return newText;
    }
    /**
     * Stream cipher that randomly rotates characters. If controlled, then the 
     * characters are rotated in sections, otherwise, they are rotated in the 
     * entire range
     * @param text The text to encrypt or decrypt.
     * @param decipher Whether this should encrypt or decrypt the given text.
     * @param rand The random number generator to use.
     * @param controlled Whether characters should be sectioned off into groups
     * @return 
     */
    public static String shiftStreamCipher(String text, boolean decipher, 
            Random rand, boolean controlled){
        String newText = "";
        for (char c : text.toCharArray()){
            int min = 0;
            int max = MAX_CHAR_VALUE;
            if (controlled){
                if (c < 32)
                    max = 32;
                else if (c < 127){
                    min = 32;
                    max = 127;
                } else {
                    min = 127;
                }
                max -= min;
            }
            int offset = rand.nextInt(max);
            if (decipher)
                offset *= -1;
            c = (char) (((max+((c - min) + offset))%max)+min);
            
            newText += c;
        }
        return newText;
    }
    /**
     * 
     * @param text The text to encrypt or decrypt.
     * @param decipher Whether this should encrypt or decrypt the given text.
     * @param rand The random number generator to use.
     * @return 
     */
    public static String shiftStreamCipher(String text, boolean decipher, Random rand){
        return shiftStreamCipher(text,decipher,rand,false);
    }
    /**
     * An implementation of a rail fence cipher
     * @param text The text to encrypt or decrypt.
     * @param decipher Whether this should encrypt or decrypt the given text.
     * @param rails The number of rails to use (must be greater than 0)
     * @return 
     */
    public static String railFenceCipher(String text, boolean decipher, int rails){
            // If the given rail count is less than or equal to zero
        if (rails <= 0)
            throw new IllegalArgumentException("Rails cannot be less than or equal to zero.");
        if (rails == 1)
            return text;
        int lastRail = (rails * 2) - 2;
        String[] railText = new String[rails];
        int rail = 0;
        if (decipher){
            int sections = text.length() / lastRail;
            int extra = text.length() % lastRail;
            int start = 0;
            for (int r = 0; r < railText.length; r++){
                int length = sections;
                if (r > 0 && r < railText.length - 1){
                    length *= 2;
                    if (extra > lastRail - r)
                        length++;
                }
                if (extra > r)
                    length ++;
                railText[r] = text.substring(start,start+length);
                start += length;
            }
            String plainText = "";
            for (int i = 0; i < text.length(); i++){
                int r = i % lastRail;
                int j = i / lastRail;
                if (r > 0 && r != (rails-1)){
                    j *= 2;
                    j += (r / rails);
                }
                if (r >= rails)
                    r = rails - (r % rails) - 2;
                plainText += railText[r].charAt(j);
            }
            return plainText;
        } else {
            for (char c : text.toCharArray()){
                int r = rail;
                if (r >= rails)
                    r = rails - (r % rails) - 2;
                if (railText[r] == null)
                    railText[r] = "";
                railText[r] += c;
                rail ++;
                rail %= lastRail;
            }
            String cipherText = "";
            for (String t : railText)
                cipherText += t;
            return cipherText;
        }
    }
    
    public static String scytaleCipher(String text, boolean decipher, int radius){
        if (radius <= 0)
            throw new IllegalArgumentException("Radius cannot be less than or equal to zero.");
        if (radius == 1)
            return text;
        String[] rows = new String[radius];
        if (decipher){
            int length = text.length() / radius;
            int extra = text.length() % radius;
            int start = 0;
            for (int i = 0; i < rows.length; i++){
                int l = length;
                if (extra > 0){
                    l ++;
                    extra --;
                }
                rows[i] = text.substring(start,start+l);
                start += l;
            }
            String plainText = "";
            for (int i = 0; i < text.length(); i++)
                plainText += rows[i % radius].charAt(i / radius);
            return plainText;
        } else {
            for (int i = 0; i < rows.length; i++)
                rows[i] = "";
            for (int i = 0; i < text.length(); i++)
                rows[i % radius] += text.charAt(i);
            String cipherText = "";
            for (String t : rows)
                cipherText += t;
            return cipherText;
        }
    }
    
    public static String columnarCipher(String text, boolean decipher, Map<Integer, Integer> key){
        Objects.requireNonNull(key);
        if (key.size() < 3)
            throw new IllegalArgumentException("There must be at least 3 entries in the key.");
        ArrayList<String> columns = new ArrayList<>();
        for (int i = 0; i < key.size(); i++)
            columns.add("");
        if (decipher){
            int sections = text.length() / columns.size();
            int extra = text.length() % columns.size();
            int start = 0;
            for (int i = 0; i < columns.size(); i++){
                int l = sections;
                int c = key.get(i);
                if (extra > c)
                    l ++;
                columns.set(c, text.substring(start, start+l));
                start += l;
            }
            String plainText = "";
            for (int i = 0; i < text.length(); i++)
                plainText += columns.get(i % columns.size()).charAt(i / columns.size());
            return plainText;
        } else {
            for (int i = 0; i < text.length(); i++){
                int c = i % columns.size();
                String cText = columns.get(c);
                columns.set(c, cText + text.charAt(i));
            }
            String cipherText = "";
            for (int i = 0; i < columns.size(); i++)
                cipherText += columns.get(key.getOrDefault(i, i));
            return cipherText;
        }
    }
    
    public static String columnarCipher(String text, boolean decipher, int columns, Random rand){
        if (columns < 3)
            throw new IllegalArgumentException("There must be at least 3 columns.");
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < columns; i++)
            indexes.add(i);
        HashMap<Integer, Integer> key = new HashMap<>();
        while (indexes.size() > 1){
            key.put(indexes.remove(rand.nextInt(indexes.size())), key.size());
        }
        key.put(indexes.get(0), key.size());
        return columnarCipher(text,decipher,key);
    }
    
//    public static String routeCipher(String text, boolean decipher, int rows, int directions){
//        if (rows <= 1)
//            throw new IllegalArgumentException("Rows must be greater than 1.");
//        if (decipher){
//            return text;
//        } else {
//            String[] rowText = new String[rows];
//            for (int i = 0; i < rowText.length; i++)
//                rowText[i] = "";
//            for (int i = 0; i < text.length(); i++)
//                rowText[i%rows] += text.charAt(i);
//            
//        }
//    }
    
    
    /**
     * This is a set that contains all the entries in this map.
     */
    private Set<Entry<Character, Character>> entries = null;
    
    protected CipherMap(){ }
    
    protected abstract Character applyCipher(Character key);
    @Override
    public Character get(Object key){
        if (key instanceof Character)
            return applyCipher((Character)key);
        return null;
    }
    protected Set<Entry<Character, Character>> createEntrySet(){
        return new AbstractSet<>(){
            @Override
            public Iterator<Entry<Character, Character>> iterator() {
                return new Iterator<>(){
                    int c = 0;
                    @Override
                    public boolean hasNext() {
                        return c < size();
                    }
                    @Override
                    public Entry<Character, Character> next() {
                        if (!hasNext())
                            throw new NoSuchElementException();
                        char temp = (char) c;
                        c++;
                        return new AbstractMap.SimpleImmutableEntry<>(
                                temp,get(temp));
                    }
                };
            }
            @Override
            public int size() {
                return MAX_CHAR_VALUE;
            }
        };
    }
    @Override
    public Set<Entry<Character, Character>> entrySet() {
        if (entries == null)
            entries = createEntrySet();
        return entries;
    }
    
    public abstract CipherMap getDecipherMap();

    public String applyCipher(String text){
        String newText = "";
        for (char c : text.toCharArray()){
            newText += getOrDefault(c,c);
        }
        return newText;
    }
    
    public String applyCipher(String text, Random rand){
        String newText = "";
        for (char c : text.toCharArray()){
            if (rand.nextBoolean())
                c = getOrDefault(c,c);
            newText += c;
        }
        return newText;
    }
    
    public static abstract class SectionedCipherMap extends CipherMap{
        
        private boolean controlled;
        
        public SectionedCipherMap(boolean controlled){
            this.controlled = controlled;
        }
        
        public SectionedCipherMap(){
            this(false);
        }
        
        public boolean isControlled(){
            return controlled;
        }
        
        protected abstract Character applyCipher(Character key, int min, int max);
        
        protected Character applyCipherControl(Character key){
            return applyCipher(key,0,32);
        }
        
        protected Character applyCipherAscii(Character key){
            return applyCipher(key,32,127);
        }
        
        protected Character applyCipherExpanded(Character key){
            return applyCipher(key,127,MAX_CHAR_VALUE);
        }
        
        protected Character applyCipherRegular(Character key){
            return applyCipher(key,0,MAX_CHAR_VALUE);
        }
        
        @Override
        protected Character applyCipher(Character key) {
            if (controlled){
                if (key < 32)
                    return applyCipherControl(key);
                else if (key < 127)
                    return applyCipherAscii(key);
                else
                    return applyCipherExpanded(key);
            } else 
                return applyCipherRegular(key);
        }
    }
    
    public static class CaesarCipherMap extends SectionedCipherMap{
        
        public final int direction;
        
        private CaesarCipherMap reverseMap = null;
        
        public CaesarCipherMap(boolean controlled, int direction){
            super(controlled);
            this.direction = direction;
        }
        
        public CaesarCipherMap(int direction){
            this(false,direction);
        }
        
        public CaesarCipherMap(boolean controlled,int direction,boolean shiftUp){
            this(controlled,direction * (shiftUp?-1:1));
        }
        
        public CaesarCipherMap(int direction, boolean shiftUp){
            this(false,direction,shiftUp);
        }
        @Override
        protected Character applyCipher(Character key, int min, int max){
            max -= min;
            return (char) ((max+((key+direction-min) % max))%max + min);
        }
        @Override
        public CipherMap getDecipherMap() {
            if (reverseMap == null)
                reverseMap = new CaesarCipherMap(isControlled(),direction,true);
            return reverseMap;
        }
    }
    
    public static class AtbashCipherMap extends SectionedCipherMap{
        
        public AtbashCipherMap(boolean controlled){
            super(controlled);
        }
        
        public AtbashCipherMap(){
            super();
        }
        @Override
        protected Character applyCipher(Character key, int min, int max) {
            max -= 1;
            return (char) (max - key + min);
        }
        @Override
        public CipherMap getDecipherMap() {
            return this;
        }
    }
    /**
     * This is a cipher map that alters characters, with letters mapped to its 
     * reverse (both in position and capitalization), numbers are reversed, and 
     * symbols mapped to their mirror (if there is one) or to some other 
     * symbol.
     */
    public static class InvertCharacterCipherMap extends CipherMap{
        @Override
        protected Character applyCipher(Character key) {
            if (key < 32 || key >= 127)
                return null;
            if (Character.isUpperCase(key))
                key = Character.toLowerCase(key);
            else if (Character.isLowerCase(key))
                key = Character.toUpperCase(key);
            if (Character.isLetter(key)){
                int offset = 0;
                if (Character.isUpperCase(key))
                    offset = 64;
                else if (Character.isLowerCase(key))
                    offset = 96;
                key = (char) (offset + (27 - (key - offset)));
            }
            if (Character.isDigit(key)){
                key = (char) (48 + (9-(key-48)));
            }
            switch(key){
                case('('):
                    return ')';
                case(')'):
                    return '(';
                case('['):
                    return ']';
                case(']'):
                    return '[';
                case('{'):
                    return '}';
                case('}'):
                    return '{';
                case('\\'):
                    return '/';
                case('/'):
                    return '\\';
                case('+'):
                    return '-';
                case('-'):
                    return '+';
                case('<'):
                    return '>';
                case('>'):
                    return '<';
                case(' '):
                    return '_';
                case('_'):
                    return ' ';
                case(':'):
                    return ';';
                case(';'):
                    return ':';
                case('\''):
                    return '\"';
                case('\"'):
                    return '\'';
                case('.'):
                    return ',';
                case(','):
                    return '.';
                case('!'):
                    return '?';
                case('?'):
                    return '!';
            }
            return key;
        }
        @Override
        protected Set<Entry<Character, Character>> createEntrySet(){
            return new AbstractSet<>(){
                @Override
                public Iterator<Entry<Character, Character>> iterator() {
                    return new Iterator<>(){
                        int c = 0;
                        @Override
                        public boolean hasNext() {
                            return c < size();
                        }
                        @Override
                        public Entry<Character, Character> next() {
                            if (!hasNext())
                                throw new NoSuchElementException();
                            char temp = (char) (c+32);
                            c++;
                            return new AbstractMap.SimpleImmutableEntry<>(
                                    temp,get(temp));
                        }
                    };
                }
                @Override
                public int size() {
                    return 95;
                }
            };
        }
        @Override
        public CipherMap getDecipherMap() {
            return this;
        }
    }
    
    
}
