/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import components.disable.DisableInput;
import components.text.action.commands.*;
import files.FilesExtended;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import javax.swing.*;
import manager.dropbox.DropboxUtilities;

/**
 *
 * @author Mosblinker
 */
public class LinkManagerUtilities {
    /**
     * This class cannot be constructed.
     */
    private LinkManagerUtilities() {}
    /**
     * This is the directory that contains this program. This is initially null 
     * and is initialized the first time it is requested and successfully 
     * loaded.
     */
    private static String programDir = null;
    /**
     * This adds the given string to the the system clipboard.
     * @param text The text to place into the system clipboard.
     */
    public static void copyText(String text){
            // A StringSelection to transfer the text
        StringSelection selection = new StringSelection(text);  
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(selection, selection);
    }
    /**
     * This opens the link provided if the link is not null or blank.
     * @param link The link to open.
     * @throws NullPointerException If the link is null.
     * @throws IllegalArgumentException If the link is blank.
     * @throws MalformedURLException If the link is not a valid URL.
     * @throws URISyntaxException If the link is not formatted strictly 
     * according to RFC2396 and thus cannot be converted to a URI.
     * @throws IOException If the user's default browser is not found, fails to 
     * launch, or the default handler application fails to launch.
     */
    public static void openLink(String link) throws URISyntaxException, 
            MalformedURLException, IOException {
        Objects.requireNonNull(link);   // Check for null links
        if (link.isBlank())             // If the link is blank
            throw new IllegalArgumentException("Link cannot be blank");
        Desktop.getDesktop().browse(new URL(link).toURI());
    }
    /**
     * This returns the working directory for this program.
     * @return The working directory for the program.
     */
    public static String getWorkingDirectory(){
        return System.getProperty("user.dir");
    }
    /**
     * This returns the directory of this program.
     * @return The directory containing this program.
     */
    public static String getProgramDirectory(){
            // If the program directory has been previously retrieved
        if (programDir != null)
            return programDir;
            // Get the location of this program, as a URL
        URL url = LinkManager.class.getProtectionDomain().getCodeSource().getLocation();
            // If a URL was found
        if (url != null)
            try {   // Get the parent of this program
                programDir = new File(url.toURI()).getParent();
                return programDir;
            } catch (URISyntaxException ex) {
                LinkManager.getLogger().log(java.util.logging.Level.WARNING,
                        "Failed to retrieve program directory.", ex);
            }
        return getWorkingDirectory();
    }
    /**
     * This gets the hexadecimal String representation of the given UUID.
     * @param uuid The UUID to get the hexadecimal representation of (cannot be 
     * null).
     * @return The hexadecimal string representation of the given UUID.
     * @throws NullPointerException If the given UUID is null.
     * @see #uuidFromHex(String) 
     */
    public static String uuidToHex(UUID uuid){
            // Make sure the UUID is not null.
        Objects.requireNonNull(uuid);
        return String.format("%016X%016X",uuid.getMostSignificantBits(),
                    uuid.getLeastSignificantBits());
    }
    /**
     * This creates a UUID from the given hexadecimal string. This effectively 
     * does the reverse of the {@link #uuidToHex(UUID) uuidToHex} method.
     * @param value The String to convert to a UUID.
     * @return The UUID represented by the given String.
     * @throws NullPointerException If the String is null.
     * @throws NumberFormatException If the String does not contain one or two 
     * parsable {@code long} values encoded in base-16 (hexadecimal).
     * @see #uuidToHex(UUID) 
     * @see Long#parseUnsignedLong(String, int) 
     */
    public static UUID uuidFromHex(String value){
            // Make sure the String is not null
        Objects.requireNonNull(value);
            // If the String is empty
        if (value.isEmpty())
            throw new NumberFormatException("UUID hex string cannot be empty");
            // If the String is too long to be two long values
        if (value.length() > 32)
            throw new NumberFormatException("UUID hex string (" + value + 
                    ") is too long ("+ value.length() + " > 32)");
            // This gets the start of the least significant half of the UUID. 
            // If the String only has the least significant half, then this will 
            // be 0. Otherwise, this will contain the offset into the String
        int lowStart = Math.max(value.length() - 16,0);
            // Get the least significant half of the UUID
        long leastSig = Long.parseUnsignedLong(value.substring(lowStart), 16);
            // This will get the most significant half of the UUID
        long mostSig = 0;
            // If the String contains the most significant half of the UUID 
            // (i.e. The string is longer than 16 characters
        if (lowStart > 0)
                // Get the most significant half of the UUID
            mostSig = Long.parseUnsignedLong(value.substring(0, lowStart),16);
            // Create and return a UUID with the least and most significant bits
        return new UUID(mostSig,leastSig);
    }
    /**
     * This adds the given commands to the given popup menu. This will also 
     * apply the commands to the text component that was registered with the 
     * text edit commands.
     * @param menu The popup menu to add the commands to.
     * @param undoCommands The undo commands to add to the popup menu.
     * @param editCommands The text edit commands to add to the popup menu.
     * @param pasteAndAddAction The paste and add action if one should be added 
     * to the popup menu, or null.
     */
    public static void addToPopupMenu(JPopupMenu menu, 
            UndoManagerCommands undoCommands,TextComponentCommands editCommands, 
            Action pasteAndAddAction){
        undoCommands.addToTextComponent(editCommands.getTextComponent());
        editCommands.addToTextComponent();
        menu.add(undoCommands.getUndoOrRedoAction());
        menu.addSeparator();
        menu.add(editCommands.getCutAction());
        menu.add(editCommands.getCopyAction());
        menu.add(editCommands.getPasteAction());
            // If a "paste and add" action was provided 
        if (pasteAndAddAction != null)   
            menu.add(pasteAndAddAction);
        menu.add(editCommands.getDeleteAction());
        menu.addSeparator();
        menu.add(editCommands.getSelectAllAction());
    }
    /**
     * This adds the given commands to the given popup menu. This will also 
     * apply the commands to the text component that was registered with the 
     * text edit commands.
     * @param menu The popup menu to add the commands to.
     * @param undoCommands The undo commands to add to the popup menu.
     * @param editCommands The text edit commands to add to the popup menu.
     */
    public static void addToPopupMenu(JPopupMenu menu, 
            UndoManagerCommands undoCommands,
            TextComponentCommands editCommands){
        addToPopupMenu(menu,undoCommands,editCommands,null);
    }
    /**
     * This returns whether the given flag has been set on the given value. 
     * @param flags The value to check whether the flag is set for.
     * @param flag The flag to check for.
     * @return Whether the given flag is set.
     * @see #setFlag
     * @see #toggleFlag
     */
    public static boolean getFlag(int flags, int flag){
        return (flags & flag) == flag;
    }
    /**
     * This sets whether the given flag is set based off the given {@code 
     * value}.
     * @param flags The value to set the flag on.
     * @param flag The flag to be set or cleared based off {@code value}.
     * @param value Whether the flag should be set or cleared.
     * @return The value with the given flag either set or cleared.
     * @see #getFlag
     * @see #toggleFlag
     */
    public static int setFlag(int flags, int flag, boolean value){
            // If the flag is to be set, OR the flags with the flag. Otherwise, 
            // AND the flags with the inverse of the flag.
        return (value) ? flags | flag : flags & ~flag;
    }
    /**
     * This toggles whether the given flag is set.
     * @param flags The value to toggle the flag on.
     * @param flag The flag to be toggled.
     * @return The value with the given flag toggled.
     * @see #getFlag
     * @see #setFlag
     */
    public static int toggleFlag(int flags, int flag){
        return flags ^ flag;
    }
    /**
     * 
     * @param panel
     * @param cardName 
     */
    public static void setCard(JPanel panel, String cardName){
        ((java.awt.CardLayout) panel.getLayout()).show(panel, cardName);
    }
    /**
     * 
     * @param panel
     * @param card 
     */
    public static void setCard(JPanel panel, JComponent card){
        setCard(panel,card.getName());
    }
    /**
     * 
     * @param sizes
     * @param painter
     * @return 
     */
    public static List<BufferedImage> generateIconImages(int[] sizes, 
            Painter<?> painter){
            // Create a list to get the images
        ArrayList<BufferedImage> iconImages = new ArrayList<>();
            // Go through the sizes for the images
        for (int size : sizes){
            BufferedImage img = new BufferedImage(size,size,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            painter.paint(g, null, size, size);
            g.dispose();
            iconImages.add(img);
        }
        return iconImages;
    }
    /**
     * This attempts to read the contents of the given file and store it in the 
     * given properties map. This will first clear the given properties map and 
     * then load the properties into the map.
     * @param file The file to read from.
     * @param prop The properties map to load into.
     * @return Whether the configuration was successfully loaded.
     * @throws IOException If an error occurs while reading the file.
     */
    public static boolean loadProperties(File file, Properties prop) 
            throws IOException{
            // If the file doesn't exist
        if (!file.exists())
            return false;
            // Try to create a FileReader to read from the file
        try(FileReader reader = new FileReader(file)){
            prop.clear();
            prop.load(reader);
        }
        return true;
    }
    /**
     * This attempts to save the given properties map to the given file.
     * @param file The file to write to.
     * @param prop The properties map to save.
     * @param comments A description of the property list.
     * @return If the file was successfully written.
     * @throws IOException If an error occurs while writing to the file.
     */
    public static boolean saveProperties(File file, Properties prop, 
            String comments) throws IOException{
            // Try to create a PrintWriter to write to the file
        try (PrintWriter writer = new PrintWriter(file)) {
                // Store the configuration
            prop.store(writer, comments);
        }
        return true;
    }
    /**
     * This attempts to create a copy of the given file to act as a backup in 
     * the event that something goes wrong with the original file. The file will 
     * share the same name as the original with the exception that the {@link 
     * LinkManager#BACKUP_FILE_EXTENSION backup file extension} will be appended 
     * to the file name. However, if a file already exists with the proposed 
     * name for the backup file, then the {@link 
     * FilesExtended#getNextAvailableFilePath(File) next available file path} 
     * will be used instead. If the given file is null or does not exist, then 
     * this does nothing and returns null.
     * @param file The file to create a backup of.
     * @return The backup file, or null if the original file is null or 
     * non-existent.
     * @throws IOException If an error occurs while creating the backup file.
     * @see LinkManager#BACKUP_FILE_EXTENSION
     */
    public static File createBackupCopy(File file) throws IOException{
        LinkManager.getLogger().entering(LinkManagerUtilities.class.getName(), 
                "createBackupCopy", file);
            // If the original file is null or does not exist
        if (file == null || !file.exists()){
            LinkManager.getLogger().exiting(LinkManagerUtilities.class.getName(), 
                    "createBackupCopy", null);
            return null;
        }
            // Get the file to use as the backup file
        File target = new File(file.toString()+"."+LinkManager.BACKUP_FILE_EXTENSION);
            // If the target file already exists
        if (target.exists())
            target = FilesExtended.getNextAvailableFilePath(target);
        Path copy;  // This is the path to the backup file.
        try{    // Create a copy of the file
            copy = Files.copy(file.toPath(), target.toPath(), 
                    StandardCopyOption.COPY_ATTRIBUTES);
        } catch(FileAlreadyExistsException ex) {
                // How does the target file already exist?
            LinkManager.getLogger().log(java.util.logging.Level.WARNING, 
                    "Target backup file already exists.", ex);
            target = FilesExtended.getNextAvailableFilePath(target);
            LinkManager.getLogger().log(java.util.logging.Level.INFO, 
                    "New Target backup file {0}", target);
                // Create a copy of the file using the next available file path
            copy = Files.copy(file.toPath(), target.toPath(), 
                    StandardCopyOption.COPY_ATTRIBUTES);
        }
        target = copy.toFile();
        LinkManager.getLogger().exiting(LinkManagerUtilities.class.getName(), 
                "createBackupCopy", target);
        return target;
    }
    /**
     * This gets the String representing the URL from a shortcut file.
     * @param lines A List of Strings extracted from the shortcut file.
     * @return The URL, as a String, or null if not found.
     * @see LinkManager#SHORTCUT_HEADER
     * @see LinkManager#URL_FLAG
     */
    public static String getShortcutURL(List<String> lines){
            // Starts from the shortcut flag and searches for the URL
        for (int pos = lines.indexOf(LinkManager.SHORTCUT_HEADER);
                pos < lines.size();pos++){
            String temp = lines.get(pos).trim();  // The string being checked
                // If the current string starts with the URL flag
            if (temp.startsWith(LinkManager.URL_FLAG))
                return temp.substring(LinkManager.URL_FLAG.length());
        }
        return null;
    }
    /**
     * This reads in the remaining lines from the given Scanner and stores them 
     * into the given List of Strings.
     * @param scanner The Scanner to read the lines from (cannot be null).
     * @param list The List of Strings to store the lines in, or null.
     * @return The List of Strings with the lines stored in it.
     */
    public static List<String> readIntoList(Scanner scanner, List<String> list){
            // If the given list is null
        if (list == null)
            list = new ArrayList<>();
            // While the scanner has data in it
        while (scanner.hasNextLine()){      
                // Gets the next line
            String temp = scanner.nextLine();
                // If the line is blank
            if (!temp.isBlank())
                list.add(temp.trim());
        }
        return list;
    }
    /**
     * This reads in the remaining lines from the given Scanner and stores them 
     * into the given List of Strings.
     * @param scanner The Scanner to read the lines from (cannot be null).
     * @return The List of Strings with the lines stored in it.
     */
    public static List<String> readIntoList(Scanner scanner){
        return readIntoList(scanner,new ArrayList<>());
    }
    /**
     * This attempts to write the List of Strings to the given file.
     * @param file The file to write to.
     * @param list The list of String to write to the file.
     * @param blankLines
     * @param listener
     * @return If the file was successfully written.
     */
    public static boolean writeToFile(File file, List<String> list, 
            boolean blankLines, ProgressObserver listener){
        LinkManager.getLogger().entering(LinkManagerUtilities.class.getName(), 
                "writeToFile", file);
            // Try to create a PrintWriter to write to the file
        try (PrintWriter writer = new PrintWriter(file)) {
                // If a progress observer was given
            if (listener != null)
                listener.setIndeterminate(false);
                // Writes each line to the file
            for (int pos = 0; pos < list.size(); pos++){
                writer.println(list.get(pos));
                    // If there is to be a blank line between each line
                if (blankLines)
                    writer.println();
                    // If a progress observer was given
                if (listener != null)
                    listener.incrementValue();
            }
        } catch (FileNotFoundException ex) {
            LinkManager.getLogger().log(java.util.logging.Level.WARNING, 
                    "File not found", ex);
            LinkManager.getLogger().exiting(LinkManagerUtilities.class.getName(), 
                    "writeToFile", false);
            return false;
        }
        LinkManager.getLogger().exiting(LinkManagerUtilities.class.getName(), 
                "writeToFile", true);
        return true;
    }
    /**
     * This attempts to write the List of Strings to the given file.
     * @param file The file to write to.
     * @param list The list of String to write to the file.
     * @param blankLines
     * @return If the file was successfully written.
     */
    public static boolean writeToFile(File file, List<String> list, 
            boolean blankLines){
        return writeToFile(file,list,blankLines,null);
    }
    /**
     * This attempts to write the List of Strings to the given file.
     * @param file The file to write to.
     * @param list The list of String to write to the file.
     * @param listener
     * @return If the file was successfully written.
     */
    public static boolean writeToFile(File file, List<String> list, 
            ProgressObserver listener){
        return writeToFile(file,list,false,listener);
    }
    /**
     * This attempts to write the List of Strings to the given file.
     * @param file The file to write to.
     * @param list The list of String to write to the file.
     * @return If the file was successfully written.
     */
    public static boolean writeToFile(File file, List<String> list){
        return writeToFile(file,list,null);
    }
    /**
     * 
     * @param fileSize
     * @return 
     */
    public static double getFileSizeDivider(long fileSize){
            // Get the value needed to divide the file length to get it back 
            // into the range of integers
        double divider = 1;
            // While the divided file length is larger than the integer maximum
        while (Math.ceil(fileSize / divider) > Integer.MAX_VALUE)
            divider++;
        return divider;
    }
    /**
     * 
     * @param parent
     * @return 
     */
    public static boolean beepIfParentDisabled(Component parent){
        if (parent instanceof DisableInput){
            return ((DisableInput)parent).beepWhenDisabled();
        }
        return false;
    }
    /**
     * 
     * @param fc
     * @param parent
     * @param config
     * @param title
     * @return 
     */
    public static File showOpenFileChooser(JFileChooser fc, Component parent, 
            LinkManagerConfig config, String title){
        if (beepIfParentDisabled(parent))     // If input is disabled
            return null;
        int option;     // This is used to store which button the user pressed
        File file = null;           // This gets the file to open
        if (title != null)
            fc.setDialogTitle(title);
        do{
            if (fc.getApproveButtonText() != null)
                option = fc.showDialog(parent, fc.getApproveButtonText());
            else
                option = fc.showOpenDialog(parent);
            fc.setPreferredSize(fc.getSize());
                // Set the file chooser's size in the config if it's saved
            config.storeFileChooser(fc);
            if (option == JFileChooser.APPROVE_OPTION){
                file = fc.getSelectedFile();
                if (!file.exists()){
                    JOptionPane.showMessageDialog(parent, 
                        "\""+file.getName()+"\"\nFile not found.\n"
                                + "Check the file name and try again.", 
                        "File Not Found", JOptionPane.WARNING_MESSAGE);
                    file = null;
                }
            }
            else
                file = null;
        }
        while (option == JFileChooser.APPROVE_OPTION && file == null);
        return file;
    }
    /**
     * 
     * @param fc
     * @param parent
     * @param config
     * @return 
     */
    public static File showOpenFileChooser(JFileChooser fc, Component parent, 
            LinkManagerConfig config){
        return showOpenFileChooser(fc,parent,config,null);
    }
    /**
     * 
     * @param fc
     * @param parent
     * @param config
     * @param title
     * @param checkIfExists
     * @return 
     */
    public static File showSaveFileChooser(JFileChooser fc, Component parent, 
            LinkManagerConfig config, String title, boolean checkIfExists){
        if (beepIfParentDisabled(parent))     // If input is disabled
            return null;
        if (title != null)
            fc.setDialogTitle(title);
        int option;     // This is used to store which button the user pressed
        File file = null;   // This is the selected file
        do{
            if (fc.getApproveButtonText() != null)
                option = fc.showDialog(parent, fc.getApproveButtonText());
            else
                option = fc.showSaveDialog(parent);
            fc.setPreferredSize(fc.getSize());
                // Set the file chooser's size in the config if it's saved
            config.storeFileChooser(fc);
            if (option == JFileChooser.APPROVE_OPTION){
                file = fc.getSelectedFile();
                    // If the file already exists and this checks if the file exists
                if (checkIfExists && file.exists()){
                        // Beep at the user
                    parent.getToolkit().beep();
                        // Show the user a confirmation dialog asking if the 
                        // user wants to overwrite the file
                    int option2 = JOptionPane.showConfirmDialog(parent, 
                            "There is already a file with that name.\n"+
                                    "Should the file be overwritten?\n"+
                                    "File: \""+file+"\"", "File Already Exists", 
                                    JOptionPane.YES_NO_CANCEL_OPTION, 
                                    JOptionPane.WARNING_MESSAGE);
                        // Determine the action to perform based off the 
                    switch(option2){ // user's choice
                            // If the user selected No
                        case(JOptionPane.NO_OPTION):
                                // Set the file to null to run the loop 
                            file = null;    // again
                            // If the user selected Yes
                        case(JOptionPane.YES_OPTION):
                            break;
                            // If the user selected Cancel or exited the 
                        default:    // dialog
                                // Cancel the operation, and show a prompt 
                                // notifying that nothing was saved.
                            JOptionPane.showMessageDialog(parent,
                                    "No file was saved.", 
                                    "File Already Exists", 
                                    JOptionPane.INFORMATION_MESSAGE);
                            return null;
                    }
                }
            }
        }
        while(file == null && option == JFileChooser.APPROVE_OPTION);
        return file;
    }
    /**
     * 
     * @param fc
     * @param parent
     * @param config
     * @param checkIfExists
     * @return 
     */
    public static File showSaveFileChooser(JFileChooser fc, Component parent, 
            LinkManagerConfig config, boolean checkIfExists){
        return showSaveFileChooser(fc,parent,config,null,checkIfExists);
    }
    /**
     * 
     * @param fc
     * @param parent
     * @param config
     * @param title
     * @return 
     */
    public static File showSaveFileChooser(JFileChooser fc, Component parent, 
            LinkManagerConfig config, String title){
        return showSaveFileChooser(fc,parent,config,title,true);
    }
    /**
     * 
     * @param fc
     * @param parent
     * @param config
     * @return 
     */
    public static File showSaveFileChooser(JFileChooser fc, Component parent, 
            LinkManagerConfig config){
        return showSaveFileChooser(fc,parent,config,null);
    }
    /**
     * 
     * @param mode
     * @param filePath
     * @return 
     */
    public static String formatExternalFilePath(DatabaseSyncMode mode, String filePath){
        if (mode != null && filePath != null){
            switch(mode){
                case DROPBOX:
                    return DropboxUtilities.formatDropboxPath(filePath);
            }
        }
        return filePath;
    }
    /**
     * 
     * @param file1
     * @param file2
     * @return 
     */
    public static boolean isSameFile(File file1, File file2){
        try{
            return Files.isSameFile(file1.toPath(), file2.toPath());
        } catch (NoSuchFileException ex) {
        } catch (IOException ex){
            LinkManager.getLogger().log(Level.WARNING, 
                    "Failed to check if the downloaded file is the same "
                            + "as the loaded file",ex);
        }
        return file1.equals(file2);
    }
}
