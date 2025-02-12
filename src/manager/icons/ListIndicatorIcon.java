/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.icons;

import icons.Icon2D;
import java.awt.*;
import java.awt.geom.*;
import java.util.EventListener;
import javax.swing.ImageIcon;
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
    protected static final int INDICATOR_HEIGHT = 13;
    /**
     * This is the width of the hidden list indicator icon.
     */
    protected static final int HIDDEN_INDICATOR_WIDTH = INDICATOR_HEIGHT;
    /**
     * This is the width of the read-only list indicator icon.
     */
    protected static final int READ_ONLY_INDICATOR_WIDTH = INDICATOR_HEIGHT-3;
    /**
     * This is the width of the indicator icon for full lists.
     */
    protected static final int FULL_LIST_INDICATOR_WIDTH = 3;
    /**
     * This is the spacing to use between the indicator icons.
     */
    protected static final int INDICATOR_SPACING = 4;
    /**
     * This is the flag indicating that the hidden list indicator should be 
     * shown.
     */
    public static final int HIDDEN_LIST_FLAG = 0x01;
    /**
     * This is the flag indicating that the read-only list indicator should be 
     * shown.
     */
    public static final int READ_ONLY_LIST_FLAG = 0x02;
    /**
     * This is the flag indicating that the full list indicator should be shown.
     */
    public static final int FULL_LIST_FLAG = 0x04;
    /**
     * This is the image used to indicate that a list is hidden.
     */
    private static final String HIDDEN_LIST_INDICATOR_IMAGE = 
            "/images/Hidden Indicator Icon.png";
    /**
     * This is an EventListenerList to store the listeners for this class.
     */
    protected EventListenerList listenerList = new EventListenerList();
    
    private int flags = 0;
    
    protected static Image hiddenListImage = null;
    
    protected static Area padlockShackle = null;
    
    protected static RoundRectangle2D padlockBody = null;
    
    protected static Path2D fullListBody = null;
    
    protected static Ellipse2D fullListPoint = null;

    public ListIndicatorIcon(int flags){
        this.flags = flags;
        constructShapes();
    }

    public ListIndicatorIcon(){
        this(0);
    }
    
    private void constructShapes(){
            // If all the shapes have been initialized
        if (padlockShackle != null && padlockBody != null && 
                hiddenListImage != null && fullListBody != null && 
                fullListPoint != null)
            return;
        
        Ellipse2D e = new Ellipse2D.Double();
        Rectangle2D rect = new Rectangle2D.Double();
        
        // TODO: Create the eye shape from shapes instead of using an image
        
            // If the hidden list image has not been initialized yet
        if (hiddenListImage == null){
                // Load the image using an ImageIcon and get the resulting image
            hiddenListImage = new ImageIcon(this.getClass().
                    getResource(HIDDEN_LIST_INDICATOR_IMAGE)).getImage();
        }
            // If the padlock body has not been initialized yet
        if (padlockBody == null){
            padlockBody = new RoundRectangle2D.Double();
            padlockBody.setFrameFromDiagonal(0, (INDICATOR_HEIGHT*3)/8.0,
                    READ_ONLY_INDICATOR_WIDTH, INDICATOR_HEIGHT);
            ((RoundRectangle2D.Double)padlockBody).arcwidth = 2.5;
            ((RoundRectangle2D.Double)padlockBody).archeight = 2.5;
        }   // If the padlock shackle has not been initialized yet
        if (padlockShackle == null){
                // Get the minimum x for the padlock shackle
            double padlockX1 = padlockBody.getMinX()+1.5;
                // Get the maximum x for the padlock shackle
            double padlockX2 = padlockBody.getMaxX()-1.5;
            e.setFrameFromDiagonal(padlockX1, 0, padlockX2, (padlockX2-padlockX1));
            rect.setFrameFromDiagonal(padlockX1, e.getCenterY(), padlockX2, padlockBody.getMinY()+1);
            padlockShackle = new Area(e);
            padlockShackle.add(new Area(rect));
            e.setFrameFromCenter(e.getCenterX(), e.getCenterY(), e.getMinX()+1.25, e.getMinY()+1.25);
            rect.setFrameFromCenter(rect.getCenterX(), rect.getCenterY(), e.getMinX(), rect.getMinY());
            padlockShackle.subtract(new Area(e));
            padlockShackle.subtract(new Area(rect));
        }   // If the full list indicator dot has not been initialized yet
        if (fullListPoint == null){
            fullListPoint = new Ellipse2D.Double();
            fullListPoint.setFrameFromDiagonal(0, INDICATOR_HEIGHT, 
                    FULL_LIST_INDICATOR_WIDTH, 
                    INDICATOR_HEIGHT-FULL_LIST_INDICATOR_WIDTH);
        }   // If the body of the full list indicator dot has not been 
            // initialized yet
        if (fullListBody == null){
            fullListBody = new Path2D.Double();
            fullListBody.moveTo(0, 0);
            fullListBody.lineTo(FULL_LIST_INDICATOR_WIDTH, 0);
            double fullListY = fullListPoint.getMinY() - 1.5;
            fullListBody.lineTo(FULL_LIST_INDICATOR_WIDTH-0.75, fullListY);
            fullListBody.lineTo(0.75, fullListY);
            fullListBody.closePath();
        }
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
//        g.clipRect(0, 0, getIconWidth(), getIconHeight());
            // Enable antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            // Prioritize rendering quality over speed
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
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
        g = (Graphics2D) g.create();
//        g.translate(x, y);
        // TODO: Implement painting an eye using shapes instead of an image
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(hiddenListImage, x,y, 
                HIDDEN_INDICATOR_WIDTH, INDICATOR_HEIGHT, c);
        g.dispose();
    }
    
    protected void paintReadOnlyIndicator(Component c, Graphics2D g, int x, int y){
        g = (Graphics2D) g.create();
        g.translate(x, y);
        g.fill(padlockShackle);
        g.fill(padlockBody);
        g.dispose();
    }
    
    protected void painFullIndicator(Component c, Graphics2D g, int x, int y){
        g = (Graphics2D) g.create();
        g.translate(x, y);
        g.fill(fullListBody);
        g.fill(fullListPoint);
        g.dispose();
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
