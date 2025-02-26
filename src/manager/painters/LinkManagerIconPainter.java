/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.painters;

import java.awt.*;
import java.util.Objects;
import javax.swing.*;

/**
 *
 * @author Mosblinker
 */
public class LinkManagerIconPainter implements Painter<Object>{
    /**
     * The width at which this is internally rendered at.
     */
    private static final int INTERNAL_WIDTH = 512;
    /**
     * The height at which this is internally rendered at.
     */
    private static final int INTERNAL_HEIGHT = 512;
    @Override
    public void paint(Graphics2D g, Object object, int width, int height) {
            // Check if the graphics context is null
        Objects.requireNonNull(g);
            // If either the width or height are less than or equal to zero 
            // (nothing would be rendered anyway)
        if (width <= 0 || height <= 0)
            return;
            // Create a copy of the given graphics context
        g = (Graphics2D) g.create();
            // Scale the graphics context to the appropriate size
        g.scale(width/((double)INTERNAL_WIDTH), height/((double)INTERNAL_HEIGHT));
            // Enable antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            // Prioritize rendering quality over speed
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
        
        
            // Dispose of the copy of the graphics context
        g.dispose();
    }
    
}
