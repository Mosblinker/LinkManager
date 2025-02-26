/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.icons;

import icons.Icon2D;
import java.awt.*;
import java.awt.geom.*;
import java.util.EventListener;
import javax.swing.Painter;
import javax.swing.event.*;
import manager.LinkManager;
import manager.links.*;
import manager.painters.indicators.*;

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
    protected static final int HIDDEN_INDICATOR_WIDTH = 12;
    /**
     * This is the width of the read-only list indicator icon.
     */
    protected static final int READ_ONLY_INDICATOR_WIDTH = 10;
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
     * This is an EventListenerList to store the listeners for this class.
     */
    protected EventListenerList listenerList = new EventListenerList();
    
    private int flags = 0;
    
    protected static Area padlockShackle = null;
    
    protected static RoundRectangle2D padlockBody = null;
    
    protected static Path2D eyeOutline = null;
    
    protected static Arc2D eyeIrisOutline = null;
    
    protected static Area eyePupil = null;
    /**
     * This is the painter used to paint the full list indicator. This is 
     * initially null and is initialized the first time a ListIndicatorIcon is 
     * constructed.
     */
    protected static Painter<Component> fullListPainter = null;

    public ListIndicatorIcon(int flags){
        this.flags = flags;
        constructShapes();
            // Construct the painters 
        constructPainters();
    }

    public ListIndicatorIcon(){
        this(0);
    }
    /**
     * This is used to initialize the painters that are used to paint the 
     * indicators.
     */
    private void constructPainters(){
            // If the full list indicator painter has not been initialized yet
        if (fullListPainter == null)
            fullListPainter = new FullListIndicatorPainter();
    }
    
    private void constructShapes(){
            // If all the shapes have been initialized
        if (padlockShackle != null && padlockBody != null && 
                eyePupil != null && eyeIrisOutline != null && 
                eyeOutline != null)
            return;
        
        Ellipse2D e = new Ellipse2D.Double();
        Rectangle2D rect = new Rectangle2D.Double();
        
            // If the eye iris outline has not been initialized yet
        if (eyeIrisOutline == null){
            eyeIrisOutline = new Arc2D.Double();
            eyeIrisOutline.setArcByCenter(HIDDEN_INDICATOR_WIDTH/2.0,INDICATOR_HEIGHT/2.0, 
                    2.5, 160, 310, Arc2D.OPEN);
        }   // If the eye pupil shape has not been initialized yet
        if (eyePupil == null){
            e.setFrameFromCenter(eyeIrisOutline.getCenterX(),eyeIrisOutline.getCenterY(),
                    eyeIrisOutline.getMinX()+1,eyeIrisOutline.getMinY()+1);
            eyePupil = new Area(e);
            Arc2D arc = new Arc2D.Double();
            arc.setArc(eyeIrisOutline);
            arc.setArcType(Arc2D.PIE);
            eyePupil.intersect(new Area(arc));
        }   // If the eye outline has not been initialized yet
        if (eyeOutline == null){
            eyeOutline = new Path2D.Double();
            eyeOutline.moveTo(0, eyeIrisOutline.getCenterY());
            double minY = eyeIrisOutline.getMinY()-1;
            double maxY = eyeIrisOutline.getMaxY()+1;
            double ctrlPt1X = eyeIrisOutline.getMinX()/2.0;
            double ctrlPt2X = eyeIrisOutline.getCenterX()+(eyeIrisOutline.getMaxX()/2.0);
            eyeOutline.curveTo(
                    ctrlPt1X, eyeIrisOutline.getMinY(), 
                    eyeIrisOutline.getMinX()+0.5, minY, 
                    eyeIrisOutline.getCenterX(), minY);
            eyeOutline.curveTo(
                    eyeIrisOutline.getMaxX()+0.5, minY, 
                    ctrlPt2X, eyeIrisOutline.getMinY(), 
                    HIDDEN_INDICATOR_WIDTH, eyeIrisOutline.getCenterY());
            eyeOutline.curveTo(
                    ctrlPt2X,eyeIrisOutline.getMaxY(), 
                    eyeIrisOutline.getMaxX()+0.5, maxY, 
                    eyeIrisOutline.getCenterX(), maxY);
            eyeOutline.curveTo(
                    eyeIrisOutline.getMinX()+0.5, maxY, 
                    ctrlPt1X, eyeIrisOutline.getMaxY(), 
                    0, eyeIrisOutline.getCenterY());
            eyeOutline.closePath();
        }   // If the padlock body has not been initialized yet
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
            // Translate the position to the top-left corner of the icon
        g.translate(x, y);
            // Clip the area rendered by the icon
//        g.clipRect(0, 0, getIconWidth(), getIconHeight());
            // Enable antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            // Prioritize rendering quality over speed
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
            // Set the stroke normalization to be pure, i.e. geometry should be 
            // left unmodified
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, 
                RenderingHints.VALUE_STROKE_PURE);
            // Set the color to be the component's foreground
        g.setColor(c.getForeground());
            // This stores the x-coordinate for the next icon to paint
        int xOff = INDICATOR_SPACING;
            // If the list is hidden
        if (isHidden()){
                // Paint the hidden list indicator
            paintHiddenIndicator(c,g,xOff,0);
            xOff += HIDDEN_INDICATOR_WIDTH + INDICATOR_SPACING;
        }   // If the list is read only
        if (isReadOnly()){
                // Paint the read only list indicator
            paintReadOnlyIndicator(c,g,xOff,0);
            xOff += READ_ONLY_INDICATOR_WIDTH + INDICATOR_SPACING;
        }   // If the list is full
        if (isFull()){
                // Paint the full list indicator
            painFullIndicator(c,g,xOff,0);
        }
    }
    
    protected void paintHiddenIndicator(Component c, Graphics2D g, int x, int y){
            // Create a copy of the graphics context
        g = (Graphics2D) g.create();
            // Translate the graphics context to where the indicator should be 
        g.translate(x, y);  // rendered
            // Paint the hidden list indicator
        g.draw(eyeIrisOutline);
        g.fill(eyePupil);
        g.setStroke(new BasicStroke(1.25f));
        g.draw(eyeOutline);
            // Dispose of the graphics context
        g.dispose();
    }
    
    protected void paintReadOnlyIndicator(Component c, Graphics2D g, int x, int y){
            // Create a copy of the graphics context
        g = (Graphics2D) g.create();
            // Translate the graphics context to where the indicator should be 
        g.translate(x, y);  // rendered
            // Paint the read only list indicator
        g.fill(padlockShackle);
        g.fill(padlockBody);
            // Dispose of the graphics context
        g.dispose();
    }
    
    protected void painFullIndicator(Component c, Graphics2D g, int x, int y){
            // Create a copy of the graphics context
        g = (Graphics2D) g.create();
            // Translate the graphics context to where the indicator should be 
        g.translate(x, y);  // rendered
            // Paint the full list indicator
        fullListPainter.paint(g, c, FULL_LIST_INDICATOR_WIDTH,INDICATOR_HEIGHT);
            // Dispose of the graphics context
        g.dispose();
    }
    @Override
    public int getIconWidth() {
            // Start off with a width of zero
        int width = 0;
            // If the hidden list indicator is painted
        if (isHidden())
            width += HIDDEN_INDICATOR_WIDTH;
            // If the read-only list indicator is painted
        if (isReadOnly())
            width += READ_ONLY_INDICATOR_WIDTH;
            // If the full list indicator is painted
        if (isFull())
            width += FULL_LIST_INDICATOR_WIDTH;
        return Math.max(0, width+(INDICATOR_SPACING*(Integer.bitCount(getFlags())))+1);
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
