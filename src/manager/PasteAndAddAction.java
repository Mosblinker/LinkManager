/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.awt.datatransfer.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/**
 * This is an action that is used to add text from the clipboard to a list.
 * @author Milo Steier
 */
public abstract class PasteAndAddAction extends AbstractAction{
    /**
     * This constructs a {@code PasteAndAddAction}.
     */
    public PasteAndAddAction(){
        super("Paste and Add");
        putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_V, 
                InputEvent.SHIFT_DOWN_MASK | InputEvent.CTRL_DOWN_MASK));
        putValue(Action.MNEMONIC_KEY, KeyEvent.VK_A);
        putValue(Action.DISPLAYED_MNEMONIC_INDEX_KEY,10);
        putValue(Action.SHORT_DESCRIPTION,
                "This adds the contents of the clipboard to the list");
    }
    /**
     * This returns the list that this will add to.
     * @return The list that this will add to.
     */
    public abstract List<String> getList();
    /**
     * This returns the text component used for data entry.
     * @return The text component used for data entry.
     */
    public abstract JTextComponent getTextComponent();
    @Override
    public void actionPerformed(ActionEvent evt) {
        LinkManager.getLogger().entering(this.getClass().getName(), "actionPerformed", evt);
            // Get the clipboard from the text component
        Clipboard clipboard = getTextComponent().getToolkit().getSystemClipboard();
            // If a String is currently in the clipboard
        if (clipboard.isDataFlavorAvailable(DataFlavor.stringFlavor)){
            try {   // Get the String from the clipboard
                String text = (String) clipboard.getData(DataFlavor.stringFlavor);
                    // Add the String to the list
                getList().add(text.trim());
                    // Have the text component grab the focus.
                getTextComponent().grabFocus();
            } catch (UnsupportedFlavorException | IOException ex) {
                LinkManager.getLogger().log(Level.INFO, 
                        "Unable to add from clipboard", ex);
            }
        }
        LinkManager.getLogger().exiting(this.getClass().getName(), "actionPerformed");
    }
}
