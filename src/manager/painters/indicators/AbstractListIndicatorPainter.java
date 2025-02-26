/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.painters.indicators;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;

/**
 * This is an abstract class used as the basis for list indicator icons.
 * @author Mosblinker
 */
public abstract class AbstractListIndicatorPainter implements Painter<Component>{
    /**
     * {@inheritDoc }
     * @param g {@inheritDoc }
     * @param c A {@code Component} to get useful properties for painting.
     * @param width {@inheritDoc }
     * @param height {@inheritDoc }
     */
    @Override
    public void paint(Graphics2D g, Component c, int width, int height) {
            // Check if the graphics context is null
        Objects.requireNonNull(g);
            // If either the width or height are less than or equal to zero 
            // (nothing would be rendered anyway)
        if (width <= 0 || height <= 0)
            return;
            // Create a copy of the given graphics context and configure it
        g = configureGraphics((Graphics2D) g.create(),c);
            // Paint the list indicator
        paintIndicator(g,c,width,height);
            // Dispose of the copy of the graphics context
        g.dispose();
    }
    /**
     * This is used to configure the graphics context used to render the 
     * indicator. It's assumed that the returned graphics context is the same as 
     * the given graphics context, or at least that the returned graphics 
     * context references the given graphics context in some way. 
     * @param g The graphics context to render to.
     * @param c A {@code Component} to get useful properties for painting.
     * @return The given graphics context, now configured for rendering the 
     * indicator.
     * @see #paint 
     */
    protected Graphics2D configureGraphics(Graphics2D g, Component c){
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
            // If the given component is not null
        if (c != null)
                // Set the foreground color to the component's foreground color
            g.setColor(c.getForeground());
        return g;
    }
    /**
     * This is the method that actually renders the list indicator. This is 
     * handed a scratch graphics context after it has been configured by the 
     * {@link #configureGraphics(Graphics2D, Component) configureGraphics} 
     * method.
     * @param g The graphics context to render to.
     * @param c A {@code Component} to get useful properties for painting.
     * @param width The width of the area to fill.
     * @param height The height of the area to fill.
     */
    protected abstract void paintIndicator(Graphics2D g, Component c, int width, 
            int height);
}
