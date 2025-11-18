/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.compress;

import java.io.*;
import java.nio.file.Files;
import java.util.logging.Level;
import manager.*;
import net.sf.sevenzipjbinding.*;

/**
 *
 * @author Mosblinker
 */
public class SevenZipUtilities {
    /**
     * This class cannot be constructed.
     */
    private SevenZipUtilities() {}
    /**
     * 
     * @throws SevenZipNativeInitializationException 
     */
    public static void initializeSevenZip() throws SevenZipNativeInitializationException{
        LinkManager.getLogger().entering("LinkManager", "initializeSevenZip");
        if (SevenZip.isInitializedSuccessfully()){
            LinkManager.getLogger().exiting("LinkManager", "initializeSevenZip");
            return;
        }
        try{
            SevenZip.initSevenZipFromPlatformJAR();
        } catch (SevenZipNativeInitializationException | RuntimeException ex){
            LinkManager.getLogger().log(Level.INFO, "Could not load 7-Zip library in default location", ex);
            File tempDir;
            try {
                tempDir = Files.createTempDirectory("SevenZipJBinding-").toFile();
            } catch (IOException exc){
                LinkManager.getLogger().log(Level.WARNING, "Could not create temp directory", ex);
                tempDir = new File(LinkManagerUtilities.getProgramDirectory());
            }
            LinkManager.getLogger().log(Level.FINER, "Initializing 7-Zip from directory {0}", tempDir);
            SevenZip.initSevenZipFromPlatformJAR(tempDir);
        }
        LinkManager.getLogger().exiting("LinkManager", "initializeSevenZip");
    }
}
