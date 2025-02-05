/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.icons;

import icons.Icon2D;
import java.awt.*;
import java.util.EventListener;
import javax.swing.event.*;
import manager.LinkManager;
import manager.links.*;

/**
 *
 * @author Milo Steier
 */
public class ListIndicatorIcon implements Icon2D{
    
    public static final int INDICATOR_SIZE = 17;
    
    public static final int INDICATOR_SPACING = 4;
    
    public static final int HIDDEN_LIST_FLAG = 0x01;
    
    public static final int READ_ONLY_LIST_FLAG = 0x02;
    
    public static final int FULL_LIST_FLAG = 0x04;
    /**
     * This is an EventListenerList to store the listeners for this class.
     */
    protected EventListenerList listenerList = new EventListenerList();
    
    private int flags = 0;

    public ListIndicatorIcon(int flags){
        this.flags = flags;
    }

    public ListIndicatorIcon(){
        this(0);
    }
    
    public int getFlags(){
        return flags;
    }
    
    public void setFlags(int flags){
        if (this.flags == flags)
            return;
        this.flags = flags;
        fireStateChanged();
    }
    
    public boolean getFlag(int flag){
        return LinkManager.getFlag(getFlags(), flag);
    }
    
    public void setFlag(int flag, boolean value){
        setFlags(LinkManager.setFlag(getFlags(), flag, value));
    }
    
    public boolean isHidden(){
        return getFlag(HIDDEN_LIST_FLAG);
    }
    
    public void setHidden(boolean value){
        setFlag(HIDDEN_LIST_FLAG,value);
    }
    
    public boolean isReadOnly(){
        return getFlag(READ_ONLY_LIST_FLAG);
    }
    
    public void setReadOnly(boolean value){
        setFlag(READ_ONLY_LIST_FLAG,value);
    }
    
    public boolean isFull(){
        return getFlag(FULL_LIST_FLAG);
    }
    
    public void setFull(boolean value){
        setFlag(FULL_LIST_FLAG,value);
    }
    
    public void updateFromList(LinksListModel model){
        setHidden(model.isHidden());
        setReadOnly(model.isReadOnly());
        setFull(model.getSizeLimit() != null && model.isFull());
    }
    
    public void updateFromList(LinksListPanel panel){
        updateFromList(panel.getModel());
    }

    @Override
    public void paintIcon2D(Component c, Graphics2D g, int x, int y) {
        g.translate(x, y);
        g.clipRect(0, 0, getIconWidth(), getIconHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        int yOff = 0;
        if (isHidden()){
            paintHiddenIndicator(c,g,0,yOff);
            yOff += INDICATOR_SIZE + INDICATOR_SPACING;
        }
        if (isReadOnly()){
            paintReadOnlyIndicator(c,g,0,yOff);
            yOff += INDICATOR_SIZE + INDICATOR_SPACING;
        }
        if (isFull()){
            painFullIndicator(c,g,0,yOff);
        }
    }
    
    protected void paintHiddenIndicator(Component c, Graphics2D g, int x, int y){
        // TODO: Implement painting an eye
    }
    
    protected void paintReadOnlyIndicator(Component c, Graphics2D g, int x, int y){
        // TODO: Implement painting a padlock
    }
    
    protected void painFullIndicator(Component c, Graphics2D g, int x, int y){
        // TODO: Implement painting an exclaimation mark
    }

    @Override
    public int getIconWidth() {
        int width = 0;
        for (boolean value : new boolean[]{isHidden(),isReadOnly(),isFull()}){
            if (value)
                width += INDICATOR_SIZE + INDICATOR_SPACING;
        }
        return Math.max(0, width-INDICATOR_SPACING);
    }

    @Override
    public int getIconHeight() {
        return INDICATOR_SIZE;
    }
    /**
     * This returns an array of all the objects currently registered as 
     * <code><em>Foo</em>Listener</code>s on this icon. 
     * <code><em>Foo</em>Listener</code>s are registered via the 
     * <code>add<em>Foo</em>Listener</code> method. <p>
     * 
     * The listener type can be specified using a class literal, such as 
     * <code><em>Foo</em>Listener.class</code>. If no such listeners exist, then 
     * an empty array will be returned.
     * @param <T> The type of {@code EventListener} being requested.
     * @param listenerType The type of listeners being requested. This should 
     * be an interface that descends from {@code EventListener}.
     * @return An array of the objects registered as the given listener type on 
     * this icon, or an empty array if no such listeners have been added.
     */
    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
        return listenerList.getListeners(listenerType);
    }
    /**
     * This adds the given {@code ChangeListener} to this icon.
     * @param l The listener to add.
     * @see #removeChangeListener(ChangeListener) 
     * @see #getChangeListeners() 
     */
    public void addChangeListener(ChangeListener l){
        if (l != null)          // If the listener is not null
            listenerList.add(ChangeListener.class, l);
    }
    /**
     * This removes the given {@code ChangeListener} from this icon.
     * @param l The listener to remove.
     * @see #addChangeListener(ChangeListener) 
     * @see #getChangeListeners() 
     */
    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class, l);
    }
    /**
     * This returns an array containing all the {@code ChangeListener}s that 
     * have been added to this icon.
     * @return An array containing the {@code ChangeListener}s that have been 
     * added, or an empty array if none have been added.
     * @see #addChangeListener(ChangeListener) 
     * @see #removeChangeListener(ChangeListener) 
     */
    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }
    /**
     * This is used to notify the {@code ChangeListener}s that the state of this  
     * icon has changed.
     */
    protected void fireStateChanged(){
            // This constructs the evet to fire
        ChangeEvent evt = new ChangeEvent(this);
            // A for loop to go through the change listeners
        for (ChangeListener l : listenerList.getListeners(ChangeListener.class)){
            if (l != null)  // If the listener is not null
                l.stateChanged(evt);
        }
    }
    /**
     * This returns a String representation of this icon. 
     * This method is primarily intended to be used only for debugging purposes, 
     * and the content and format of the returned String may vary between 
     * implementations.
     * @return A String representation of this icon.
     */
    protected String paramString(){
        return getIconWidth()+"x"+getIconHeight()+",flags=" + getFlags();
    }
    /**
     * This returns a string representation of this icon and its values.
     * @return A string representation of this icon and its values.
     */
    @Override
    public String toString(){
        return getClass().getName()+"["+paramString()+"]";
    }
}
