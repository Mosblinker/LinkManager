/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import javax.swing.event.*;

/**
 *
 * @author Milo Steier
 */
public abstract class SingleMethodDocumentListener  implements DocumentListener{
    @Override
    public void insertUpdate(DocumentEvent e) {
        documentUpdate(e,DocumentEvent.EventType.INSERT);
    }
    @Override
    public void removeUpdate(DocumentEvent e) {
        documentUpdate(e,DocumentEvent.EventType.REMOVE);
    }
    @Override
    public void changedUpdate(DocumentEvent e) {
        documentUpdate(e,DocumentEvent.EventType.CHANGE);
    }
    /**
     * 
     * @param evt
     * @param type 
     */
    public abstract void documentUpdate(DocumentEvent evt, DocumentEvent.EventType type);
    
}
