/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.icons;

import icons.Icon2D;
import java.awt.*;
import java.awt.geom.*;
import java.util.EventListener;
import javax.swing.event.*;
import manager.LinkManager;
import manager.links.*;

/**
 *
 * @todo Make this icon listen to changes to the state, properties, and list data of 
 * LinksListModels and LinksListPanels, so that they can update automatically when a 
 * change occurs that needs to be indicated.
 * 
 * @author Milo Steier
 */
public class ListIndicatorIcon implements Icon2D{
    /**
     * This is the height of all the indicator icons.
     */
    protected static final int INDICATOR_HEIGHT = 17;
    /**
     * This is the width of the hidden list indicator icon.
     */
    protected static final int HIDDEN_INDICATOR_WIDTH = INDICATOR_HEIGHT;
    /**
     * This is the width of the read-only list indicator icon.
     */
    protected static final int READ_ONLY_INDICATOR_WIDTH = INDICATOR_HEIGHT-4;
    /**
     * This is the width of the indicator icon for full lists.
     */
    protected static final int FULL_LIST_INDICATOR_WIDTH = INDICATOR_HEIGHT;
    /**
     * This is the spacing to use between the indicator icons.
     */
    protected static final int INDICATOR_SPACING = 4;
    
    public static final int HIDDEN_LIST_FLAG = 0x01;
    
    public static final int READ_ONLY_LIST_FLAG = 0x02;
    
    public static final int FULL_LIST_FLAG = 0x04;
    /**
     * This is an EventListenerList to store the listeners for this class.
     */
    protected EventListenerList listenerList = new EventListenerList();
    
    private int flags = 0;
    
    protected static Area padlockShackle = null;
    
    protected static RoundRectangle2D padlockBody = null;
    
    

    public ListIndicatorIcon(int flags){
        this.flags = flags;
        constructShapes();
    }

    public ListIndicatorIcon(){
        this(0);
    }
    
    private void constructShapes(){
            // If all the shapes have been initialized
        if (padlockShackle != null && padlockBody != null)
            return;
        
        Ellipse2D e = new Ellipse2D.Double();
        Rectangle2D rect = new Rectangle2D.Double();
        
        // Create the eye shape
        
            // If the padlock body has not been initialized yet
        if (padlockBody == null){
            padlockBody = new RoundRectangle2D.Double();
            padlockBody.setFrameFromDiagonal(0, 
                    ((INDICATOR_HEIGHT/2.0)+(INDICATOR_HEIGHT/3.0))/2.0, 
                    READ_ONLY_INDICATOR_WIDTH, INDICATOR_HEIGHT);
            ((RoundRectangle2D.Double)padlockBody).arcwidth = 2.5;
            ((RoundRectangle2D.Double)padlockBody).archeight = 2.5;
        }   // If the padlock shackle has not been initialized yet
        if (padlockShackle == null){
            double padlockX1 = padlockBody.getMinX()+1.5;
            double padlockX2 = padlockBody.getMaxX()-1.5;
            e.setFrameFromDiagonal(padlockX1, 0, padlockX2, (padlockX2-padlockX1));
            rect.setFrameFromDiagonal(padlockX1, e.getCenterY(), padlockX2, padlockBody.getMinY()+1);
            padlockShackle = new Area(e);
            padlockShackle.add(new Area(rect));
            e.setFrameFromCenter(e.getCenterX(), e.getCenterY(), e.getMinX()+2, e.getMinY()+2);
            rect.setFrameFromCenter(rect.getCenterX(), rect.getCenterY(), e.getMinX(), rect.getMinY());
            padlockShackle.subtract(new Area(e));
            padlockShackle.subtract(new Area(rect));
        }
        
        // Create the full indicator
    }
    
    public int getFlags(){
        return flags;
    }
    
    public ListIndicatorIcon setFlags(int flags){
        if (this.flags == flags)
            return this;
        this.flags = flags;
        fireStateChanged();
        return this;
    }
    
    public boolean getFlag(int flag){
        return LinkManager.getFlag(getFlags(), flag);
    }
    
    public ListIndicatorIcon setFlag(int flag, boolean value){
        return setFlags(LinkManager.setFlag(getFlags(), flag, value));
    }
    
    public boolean isHidden(){
        return getFlag(HIDDEN_LIST_FLAG);
    }
    
    public ListIndicatorIcon setHidden(boolean value){
        return setFlag(HIDDEN_LIST_FLAG,value);
    }
    
    public boolean isReadOnly(){
        return getFlag(READ_ONLY_LIST_FLAG);
    }
    
    public ListIndicatorIcon setReadOnly(boolean value){
        return setFlag(READ_ONLY_LIST_FLAG,value);
    }
    
    public boolean isFull(){
        return getFlag(FULL_LIST_FLAG);
    }
    
    public ListIndicatorIcon setFull(boolean value){
        return setFlag(FULL_LIST_FLAG,value);
    }
    
    public ListIndicatorIcon updateFromList(LinksListModel model){
        return setHidden(model.isHidden()).
            setReadOnly(model.isReadOnly()).
            setFull(model.getSizeLimit() != null && model.isFull());
    }
    
    public ListIndicatorIcon updateFromList(LinksListPanel panel){
        return updateFromList(panel.getModel());
    }

    @Override
    public void paintIcon2D(Component c, Graphics2D g, int x, int y) {
        g.translate(x, y);
        g.clipRect(0, 0, getIconWidth(), getIconHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(c.getForeground());
        int xOff = 0;
        if (isHidden()){
            paintHiddenIndicator(c,g,xOff,0);
            xOff += HIDDEN_INDICATOR_WIDTH + INDICATOR_SPACING;
        }
        if (isReadOnly()){
            paintReadOnlyIndicator(c,g,xOff,0);
            xOff += READ_ONLY_INDICATOR_WIDTH + INDICATOR_SPACING;
        }
        if (isFull()){
            painFullIndicator(c,g,xOff,0);
        }
    }
    
    protected void paintHiddenIndicator(Component c, Graphics2D g, int x, int y){
        // TODO: Implement painting an eye
    }
    
    protected void paintReadOnlyIndicator(Component c, Graphics2D g, int x, int y){
        g = (Graphics2D) g.create();
        g.translate(x, y);
        g.fill(padlockShackle);
        g.fill(padlockBody);
        g.dispose();
    }
    
    protected void painFullIndicator(Component c, Graphics2D g, int x, int y){
        // TODO: Implement painting an exclaimation mark
    }

    @Override
    public int getIconWidth() {
        int width = 0;
        if (isHidden())
            width += HIDDEN_INDICATOR_WIDTH;
        if (isReadOnly())
            width += READ_ONLY_INDICATOR_WIDTH;
        if (isFull())
            width += FULL_LIST_INDICATOR_WIDTH;
        return Math.max(0, width+(INDICATOR_SPACING*(Integer.bitCount(getFlags())-1)));
    }

    @Override
    public int getIconHeight() {
        return INDICATOR_HEIGHT;
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
