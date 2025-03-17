/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import components.text.action.commands.*;
import java.awt.Desktop;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.*;

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
}
