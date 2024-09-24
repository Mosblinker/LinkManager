/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package manager.security;

import java.util.*;
import selection.choice.YesOrNo;

/**
 *
 * @author Milo Steier
 */
public class ObfuscationGenerator {
    
    private static void generateSeedObfuscations(ArrayList<Integer> operations, 
            ArrayList<Integer> distance, Random rand){
        int opCount = rand.nextInt(5)+5;
        for (int i = 0; i < opCount; i++){
            int value;
            int prevOp = -1;
            if (i > 0)
                prevOp = operations.get(operations.size()-1);
            do{
                value = rand.nextInt(4);
                if (value == prevOp)
                    value = -1;
                if (value > 1){
                    switch(prevOp){
                        case(2):
                            if (value == 3)
                                value = -1;
                            break;
                        case(3):
                            if (value == 2)
                                value = -1;
                    }
                }
            }
            while (value < 0);
            operations.add(value);
            distance.add(rand.nextInt(19)+1);
        }
    }
    
    private static long getDeobfuscatedSeed(long seed, ArrayList<Integer> operations, 
            ArrayList<Integer> distance){
        for (int i = operations.size()-1; i >= 0; i--){
            switch(operations.get(i)){
                case(0):
                    seed = Long.reverse(seed);
                    break;
                case(1):
                    seed = Long.reverseBytes(seed);
                    break;
                case(3):
                    seed = Long.rotateLeft(seed, distance.get(i));
                    break;
                case(2):
                    seed = Long.rotateRight(seed, distance.get(i));
            }
        }
        return seed;
    }
    
    private static long getObfuscatedSeed(long seed, ArrayList<Integer> operations, 
            ArrayList<Integer> distance){
        for (int i = 0; i < operations.size(); i++){
            switch(operations.get(i)){
                case(0):
                    seed = Long.reverse(seed);
                    break;
                case(1):
                    seed = Long.reverseBytes(seed);
                    break;
                case(2):
                    seed = Long.rotateLeft(seed, distance.get(i));
                    break;
                case(3):
                    seed = Long.rotateRight(seed, distance.get(i));
            }
        }
        return seed;
    }
    
    private static String getDeobfuscateSeedOpText(String seedName, 
            int operation, int distance){
        String action;
        switch(operation){
            case(0):
                action = "reverse(%s)";
                break;
            case(1):
                action = "reverseBytes(%s)";
                break;
            case(3):
                action = "rotateLeft(%s, %d)";
                break;
            case(2):
                action = "rotateRight(%s, %d)";
                break;
            default:
                return seedName + " = " + seedName + ";";
        }
        return String.format("%s = Long."+action+";", seedName,seedName,distance);
    }
    
    private static void printDeobfuscateSeedCode(String seedName, 
            ArrayList<Integer> operations, 
            ArrayList<Integer> distance){
        for (int i = operations.size()-1; i >= 0; i--){
            System.out.println(getDeobfuscateSeedOpText(seedName,operations.get(i),distance.get(i)));
//            System.out.print(seedName+" = Long.");
//            switch(operations.get(i)){
//                case(0):
//                    System.out.println("reverse("+seedName+");");
//                    break;
//                case(1):
//                    System.out.println("reverseBytes("+seedName+");");
//                    break;
//                case(3):
//                    System.out.printf("rotateLeft(%s, %d);%n",seedName,distance.get(i));
//                    break;
//                case(2):
//                    System.out.printf("rotateRight(%s, %d);%n",seedName,distance.get(i));
//            }
        }
    }
    
    private static String caesarCipher(String text, boolean decipher, Random rand){
        int distance = rand.nextInt(37)+3 * ((rand.nextBoolean())?-1:1);
        return new CipherMap.CaesarCipherMap(rand.nextBoolean(),distance,decipher).applyCipher(text);
    }
    
    private static class RandomSubstitutionCipherMap extends CipherMap{
        
        private final HashMap<Character,Character> cache;
        
        private CipherMap reverseMap = null;
        
        private Character getRandomUnusedChar(Random rand, int min, int max, 
                Collection<Character> used){
            Character c;
            do{
                c = (char) ((rand.nextInt(max-min))+min);
                if (used.contains(c))
                    c = null;
            } while (c == null);
            return c;
        }
        
        private void generateCharMapping(Random rand, int min, int max, 
                Collection<Character> usedChar, boolean reverse){
            for (int i = min; i < max; i++){
                Character c = getRandomUnusedChar(rand,min,max,usedChar);
                if (reverse)
                    cache.put(c, (char) i);
                else
                    cache.put((char) i, c);
            }
        }
        
        public RandomSubstitutionCipherMap(Random rand, boolean reverse){
            cache = new HashMap<>();
            Collection<Character> usedChar = (reverse) ? cache.keySet() : cache.values();
            generateCharMapping(rand,0,32,usedChar,reverse);
            generateCharMapping(rand,32,127,usedChar,reverse);
            generateCharMapping(rand,127,0x10000,usedChar,reverse);
        }
        
        protected RandomSubstitutionCipherMap(HashMap<Character,Character> map, 
                boolean reverse){
            if (reverse){
                cache = new HashMap<>();
                for (Map.Entry<Character,Character> entry : map.entrySet()){
                    cache.put(entry.getValue(), entry.getKey());
                }
            } else {
                cache = new HashMap<>(map);
            }
        }
        @Override
        protected Character applyCipher(Character key){
            return cache.getOrDefault(key,key);
        }
        @Override
        public CipherMap getDecipherMap() {
            if (reverseMap == null)
                reverseMap = new RandomSubstitutionCipherMap(cache,true);
            return reverseMap;
        }
    }
    
    
    
    private static final int SECTION_SEED_COUNT = 4;
    
    private static long[] generateSectionSeeds(Random rand){
        long[] seeds = new long[SECTION_SEED_COUNT];
        for (int i = 0; i < seeds.length; i++)
            seeds[i] = rand.nextLong();
        return seeds;
    }
    
    private static final int MAX_EXTRA_LENGTH = 25;
    
    private static String generateRandomStr(Random rand, int length, int min, int max){
        return Obfuscator.generateRandomString(rand, length, min, max-1);
    }
    
    private static String generateRandomStr(Random rand, int min, int max){
        return generateRandomStr(rand,rand.nextInt(MAX_EXTRA_LENGTH),min,max);
    }
    
    private static String generateRandomStr(Random rand){
        return generateRandomStr(rand,32,127);
    }
    
    private static String encryptDecryptHelper(String text, boolean decipher, 
            int action, Random rand, long[] seeds){
        if (action >= 0)
            rand.setSeed(seeds[action]);
        switch(action){
            case(0):
                return CipherMap.shiftStreamCipher(text, decipher, rand, true);
            case(1):
                CipherMap invertChar = CipherMap.INVERT_CHARACTER_CIPHER;
                if (decipher)
                    invertChar = invertChar.getDecipherMap();
                return invertChar.applyCipher(text, rand);
            case(2):
                for (int i = 0; i < text.length(); i+=10){
                    if (rand.nextBoolean()){
                        String extraData = generateRandomStr(rand);
                        if (decipher){
                            text = text.substring(0, i)+text.substring(i+extraData.length());
                        } else {
                            text = text.substring(0, i)+extraData+text.substring(i);
                            i += extraData.length();
                        }
                    }
                }
                return text;
            case(3):
                String[] extraText = new String[2];
                for (int i = 0; i < extraText.length; i++){
                    extraText[i] = generateRandomStr(rand,rand.nextInt(MAX_EXTRA_LENGTH),33,127);
                }
                return (decipher) ? 
                        text.substring(extraText[0].length(), text.length() - extraText[1].length()) : 
                        extraText[0] + text + extraText[1];
            default:
                return Obfuscator.reverseString(text);
        }
    }
    
    private static String encryptDecrypt(String text, boolean decipher, Random rand){
        long[] sectionSeeds = generateSectionSeeds(rand);
        
        rand = new Random();
        for (int i = -1; i < SECTION_SEED_COUNT; i++){
            int t = (decipher) ? ((SECTION_SEED_COUNT - 2) - i) : i;
            text = encryptDecryptHelper(text,decipher,t,rand,sectionSeeds);
        }
        
        return text;
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        CipherMap m1 = new CipherMap.InvertCharacterCipherMap();
//        System.out.println(m1);
        CipherMap m2 = m1.getDecipherMap();
        boolean mapIsValid = true;
        for (Map.Entry<Character,Character> entry : m1.entrySet()){
            Character temp = m2.get(entry.getValue());
            boolean match = entry.getKey().equals(temp);
            mapIsValid &= match;
            if (!match)
                System.out.printf("%s -> %s -> %s%n",entry.getKey(),entry.getValue(),temp);
        }
        System.out.println("Maps Match: "+mapIsValid);
        
//        System.out.println();
//        for (int i = 0; i < 11; i++){
//            long seed = rand.nextLong();
//            String text = "Hello World";
//            String cipherText = applyCipher(text,false,i,new Random(seed));
//            String decipherText = applyCipher(cipherText,true,i,new Random(seed));
//            System.out.printf("%2d: %s -> %s -> %s (%5b)%n",i,text,cipherText,decipherText,(text.equals(decipherText)));
//        }
//        System.out.println();
//        for (int i = 0; i < 11; i++){
//            for (int j = 0; j < 11; j++){
//                if (i == j)
//                    continue;
//                long seed = rand.nextLong();
//                long seed2 = rand.nextLong();
//                String text = "Hello World";
//                Random actRand = new Random(seed);
//                String cipherText = applyCipher(text,false,i,actRand);
//                actRand = new Random(seed2);
//                cipherText = applyCipher(cipherText,false,j,actRand);
//                actRand = new Random(seed2);
//                String decipherText = applyCipher(cipherText,true,j,actRand);
//                actRand = new Random(seed);
//                decipherText = applyCipher(decipherText,true,i,actRand);
//                System.out.printf("%2d, %2d: %s -> %s -> %s (%5b)%n",i,j,text,cipherText,decipherText,(text.equals(decipherText)));
//            }
//        }
        
        System.out.println();
        
        Random rand = new Random();
        Obfuscator obfuscator = Obfuscator.getInstance();
        
        long encryptKey = rand.nextLong();
        System.out.println("Encryption key: " + encryptKey);
        String encryptText = Long.toHexString(encryptKey).toUpperCase();
        System.out.println("Encryption key: 0x" + encryptText);
        
        System.out.println();
        long obfuscateSeed = rand.nextLong();
        System.out.println("Obfuscation Seed: " + obfuscateSeed);
        ArrayList<Integer> seedOperations = new ArrayList<>();
        ArrayList<Integer> seedDistance = new ArrayList<>();
        generateSeedObfuscations(seedOperations,seedDistance,rand);
        long obf = getObfuscatedSeed(obfuscateSeed,seedOperations,seedDistance);
        System.out.println("Obfuscated Obfuscation Seed: " + obf);
        long time = System.currentTimeMillis();
        long deobf = getDeobfuscatedSeed(obf,seedOperations,seedDistance);
        time = System.currentTimeMillis() - time;
        System.out.println("Deobfuscated Obfuscation Seed: " + deobf);
        System.out.println("Time to deobfuscate: " + time);
        
        System.out.println();
        
        System.out.println("    // The obfuscated Obfuscation Key");
        System.out.println("long seed = " + obf+";");
        System.out.println("    // Deobfuscate the Obfuscation Key");
        printDeobfuscateSeedCode("seed",seedOperations,seedDistance);
        System.out.println("    // A random number generator to use to deobfuscate the value");
        System.out.println("Random rand = new Random(seed);");
        
        System.out.println();
        
        Scanner input = new Scanner(System.in);
        
        YesOrNo again = new YesOrNo();
        
        boolean doTests = again.askQuestion(input, "Would you like to run encrypt tests?");
        
        do{
            System.out.println();
            
            System.out.print("Enter a value to obscure: ");
            String text = "";
            while (text.isBlank())
                text = input.nextLine();
            ObfuscateSeed o = new ObfuscateSeed(rand,text);
            long seed = o.seed;
            
            System.out.println();
            
            if (doTests){
                testEncryption(text,seed,obfuscator);
                System.out.println();
            }
            
            System.out.printf("%s Obfuscator (0x%016X)%n",text,seed);
            o.printSeedDeobfuscationCode();
            System.out.println();
            
            for (int a = 12; a <= obfuscator.getMaximumCipherAction(); a++){
                System.out.print("Actions: ");
                encryptValuesTest(a,text,seed,a,obfuscator);
            }
            
            System.out.println();
            
            System.out.print("Final: ");
            String cipherText = obfuscator.applyCiphers(text,false,seed);
            String decipherText = obfuscator.applyCiphers(cipherText,true,seed);
            printEncryptTest(obfuscator.getMaximumCipherAction(),text,cipherText,decipherText);
            
            System.out.println();
        }
        while(again.askQuestion(input, "Obscure another value?"));
        
        input.close();
    }
    
    private static void testEncryption(String text, long seed, Obfuscator obfuscator){
        System.out.printf("Plain Text: %s (Seed: 0x%016X)%n",text,seed);
        Random r = new Random(seed);
        Random tempRand = new Random(seed);

        tempRand.setSeed(seed);
        String cipherText = CipherMap.streamCipher(text,tempRand);
        tempRand.setSeed(seed);
        String decipherText = CipherMap.streamCipher(cipherText,tempRand);
        printCipherTestResults("Stream",text,cipherText,decipherText);

        tempRand.setSeed(seed);
        cipherText = caesarCipher(text,false,tempRand);
        tempRand.setSeed(seed);
        decipherText = caesarCipher(cipherText,true,tempRand);
        printCipherTestResults("Caeser",text,cipherText,decipherText);

        cipherText = CipherMap.ATBASH_CIPHER.applyCipher(text);
        decipherText = CipherMap.ATBASH_CIPHER.getDecipherMap().applyCipher(cipherText);
        printCipherTestResults("Atbash",text,cipherText,decipherText);

        cipherText = Obfuscator.reverseString(text);
        decipherText = Obfuscator.reverseString(cipherText);
        printCipherTestResults("Reverse",text,cipherText,decipherText);

        tempRand.setSeed(seed);
        cipherText = encryptDecrypt(text,false,tempRand);
        tempRand.setSeed(seed);
        decipherText = encryptDecrypt(cipherText,true,tempRand);
        printCipherTestResults("Encrypt/Decrypt",text,cipherText,decipherText);

        cipherText = Obfuscator.encodeBase64(text);
        decipherText = Obfuscator.decodeBase64(cipherText);
        printCipherTestResults("Base 64",text,cipherText,decipherText);

//            for (char c : text.toCharArray()){
//                System.out.printf("%04X",(int)c);
//            }
//            System.out.println();
//            byte[] dec = CipherCrypt.BASE_64_DECODER.decode(cipherText);
//            for (byte b : dec){
//                System.out.printf("%02X",b);
//            }
//            System.out.println();

        int n = r.nextInt(5)+2;
        cipherText = CipherMap.railFenceCipher(text,false,n);
        decipherText = CipherMap.railFenceCipher(cipherText,true,n);
        printCipherTestResults("Rail Fence (n="+n+")",text,cipherText,decipherText);

        n = r.nextInt(5)+2;
        cipherText = CipherMap.scytaleCipher(text,false,n);
        decipherText = CipherMap.scytaleCipher(cipherText,true,n);
        printCipherTestResults("Scytale (n="+n+")",text,cipherText,decipherText);

        n = r.nextInt(5)+3;
        tempRand.setSeed(seed);
        cipherText = CipherMap.columnarCipher(text,false,n,tempRand);
        tempRand.setSeed(seed);
        decipherText = CipherMap.columnarCipher(cipherText,true,n,tempRand);
        printCipherTestResults("Columnar (n="+n+")",text,cipherText,decipherText);

        tempRand.setSeed(seed);
        cipherText = obfuscator.insertRandomData(text, false, tempRand);
        tempRand.setSeed(seed);
        decipherText = obfuscator.insertRandomData(cipherText, true, tempRand);
        printCipherTestResults("Random Data",text,cipherText,decipherText);

        try{
            cipherText = obfuscator.encryptText(text, false, seed);
            decipherText = obfuscator.encryptText(cipherText, true, seed);
            printCipherTestResults("Obfuscator.encryptText",text,cipherText,decipherText);
        } catch(Exception ex){
            System.out.println("Obfuscator.encryptText Cipher: Error - " + ex);
        }
    }
    
    private static void printEncryptTest(int i, String text, String cipherText, String decipherText){
        System.out.printf("%2d (%5b): %s -> %s -> %s%n",i,(text.equals(decipherText)),text,cipherText,decipherText);
    }
    
    private static void encryptValuesTest(int i, String text, long seed, int maxAction, Obfuscator obf){
        Random rand = new Random(seed);
        String cipherText = encryptValues(text,false,rand,maxAction,obf);
        rand.setSeed(seed);
        String decipherText = encryptValues(cipherText,true,rand,maxAction,obf);
        printEncryptTest(i,text,cipherText,decipherText);
    }
    
    private static String encryptValues(String text, boolean decrypt, Random rand, int maxAction, Obfuscator obf){
        return obf.applyCiphers(text, decrypt, rand, 
                obf.getCipherActionCount(rand), maxAction);
    }
    
    private static void printCipherTestResults(String cipherName, String text, 
            String cipherText, String decipherText){
        System.out.printf("\t%s Cipher (match=%5b): %s -> %s -> %s%n", cipherName,
                (text.equals(decipherText)),text,cipherText,decipherText);
    }
    
    private static class ObfuscateSeed{
        
        public final long seed;
        
        public final ArrayList<Integer> operations = new ArrayList<>();
        
        public final ArrayList<Integer> rotateDirections = new ArrayList<>();
        
        public String plainText;
        
        public String seedName = "seed";
        
        ObfuscateSeed(Random rand, String plainText){
            this.seed = rand.nextLong();
            generateSeedObfuscations(operations,rotateDirections,rand);
            this.plainText = plainText;
        }
        
        ObfuscateSeed(Random rand){
            this(rand,null);
        }
        
        public long obfuscateSeed(){
            return getObfuscatedSeed(seed,operations,rotateDirections);
        }
        
        public long deobfuscateSeed(long seed){
            return getDeobfuscatedSeed(seed,operations,rotateDirections);
        }
        
        public String getDeobfuscationCode(String seedName){
            String text = "    // The obfuscated obfuscation key";
            text += System.lineSeparator();
            text += String.format("long %s = 0x%016XL;%n",seedName,obfuscateSeed());
            text += "    // Deobfuscate the obfuscation key";
            text += System.lineSeparator();
            for (int i = operations.size()-1; i >= 0; i--){
                text += getDeobfuscateSeedOpText(seedName,operations.get(i),rotateDirections.get(i));
                text += System.lineSeparator();
            }
            return text + "    // Deobfuscate the value";
        }
        
        public String getDeobfuscationCode(){
            return getDeobfuscationCode(seedName);
        }
        
        public void printSeedDeobfuscationCode(String seedName){
//            System.out.println("    // The obfuscated obfuscation key");
//            System.out.printf("long %s = 0x%016XL;%n",seedName,obfuscateSeed());
//            System.out.println("    // Deobfuscate the obfuscation key");
//            printDeobfuscateSeedCode(seedName,operations,rotateDirections);
//            System.out.println("    // Deobfuscate the value");
            System.out.println(getDeobfuscationCode(seedName));
        }
        
        public void printSeedDeobfuscationCode(){
            printSeedDeobfuscationCode(seedName);
        }
    }
}
