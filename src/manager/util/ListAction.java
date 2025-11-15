package manager.util;

import java.awt.event.*;
import javax.swing.*;

/**
 *	Add an Action to a JList that can be invoked either by using
 *  the keyboard or a mouse.
 *
 *  By default the Enter will will be used to invoke the Action
 *  from the keyboard although you can specify and KeyStroke you wish.
 *
 *  A double click with the mouse will invoke the same Action.
 *
 *  The Action can be reset at any time.
 * 
 * <p>
 * <a href="https://tips4java.wordpress.com/2008/10/14/list-action/">List Action</a>
 * <br>
 * <a href="https://github.com/tips4java/tips4java/blob/main/source/ListAction.java">ListAction.java</a>
 * 
 * @author Rob Camick
 * @author Mosblinker
 */
public class ListAction implements MouseListener {
    private static final KeyStroke ENTER = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    
    private JList list;
    private KeyStroke keyStroke;
    /**
     * The number of times the list needs to be clicked on before the action is 
     * invoked.
     */
    private int clickCount = 2;
    /*
     *	Add an Action to the JList bound by the default KeyStroke
     */
    public ListAction(JList list, Action action){
        this(list, action, ENTER);
    }

    /*
     *	Add an Action to the JList bound by the specified KeyStroke
     */
    public ListAction(JList list, Action action, KeyStroke keyStroke){
        this.list = list;
        this.keyStroke = keyStroke;

            //  Add the KeyStroke to the InputMap

        InputMap im = list.getInputMap();
        im.put(keyStroke, keyStroke);

            //  Add the Action to the ActionMap

        ListAction.this.setAction( action );

            //  Handle mouse double click

        list.addMouseListener( ListAction.this );
    }

    /*
     *  Add the Action to the ActionMap
     */
    public void setAction(Action action) {
        list.getActionMap().put(keyStroke, action);
    }
    /**
     * This returns the number of times the list needs to be clicked on before 
     * the action is invoked.
     * @return The number of times the list needs to be clicked on before 
     * preforming the action.
     */
    public int getClickCount(){
        return clickCount;
    }
    /**
     * This sets the number of times the list needs to be clicked on before the 
     * action is invoked. The default for this value is {@code 2}. If this is 
     * set to 0 or below, then the action cannot be invoked by clicking on it.
     * @param count The number of times the list will need to be clicked on 
     * before the action is preformed.
     */
    public void setClickCount(int count){
        this.clickCount = count;
    }
    /**
     * This performs the action as if the list action was invoked.
     */
    public void doClick(){
        Action action = list.getActionMap().get(keyStroke);

        if (action != null) {
            ActionEvent event = new ActionEvent(list,ActionEvent.ACTION_PERFORMED, 
                    (String) action.getValue(Action.ACTION_COMMAND_KEY));
            action.actionPerformed(event);
        }
    }
    //  Implement MouseListener interface
    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == getClickCount()) {
            doClick();
        }
    }
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
}
