/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.painters;

import java.awt.*;
import java.awt.geom.*;
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
    
    public static final Color BORDER_OUTLINE_COLOR = new Color(0x3967A3);
    
    public static final Color PROGRAM_BACKGROUND_COLOR = new Color(0xF0F0F0);
    
    public static final Color LIST_OUTLINE_COLOR = Color.BLACK;
    
    public static final Color LIST_BACKGROUND_COLOR = Color.WHITE;
    
    public static final Color LIST_FOREGROUND_COLOR = Color.BLACK;
    
    public static final Color LIST_SELECTED_BACKGROUND_COLOR = new Color(0x0078D7);
    
    public static final Color LIST_SELECTED_FOREGROUND_COLOR = LIST_BACKGROUND_COLOR;
    
    public static final Color SIDE_BUTTON_OUTLINE_COLOR = new Color(0xADADAD);
    
    public static final Color SIDE_BUTTON_BACKGROUND_COLOR = new Color(0xE1E1E1);
    
    public static final Color ADD_BUTTON_FOREGROUND_COLOR = new Color(0x007F00);
    
    public static final Color ARROW_BUTTONS_FOREGROUND_COLOR = Color.BLACK;
    
    public static final Color REMOVE_BUTTON_FOREGROUND_COLOR = Color.RED;
    
    private static final int SIDE_BUTTON_COUNT = 4;
    
    private static final int LIST_LINE_COUNT = 10;
    
    private static final int SELECTED_LINE_INDEX = 5;
    /**
     * A scratch rounded rectangle object used to draw the image.
     */
    private RoundRectangle2D roundRect = null;
    /**
     * A scratch rectangle object used to draw the image.
     */
    private Rectangle2D rect = null;
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
        if (roundRect == null)
            roundRect = new RoundRectangle2D.Double();
        if (rect == null)
            rect = new Rectangle2D.Double();
        roundRect.setRoundRect(8, 8, 496, 496, 20, 20);
        g.setColor(BORDER_OUTLINE_COLOR);
        g.fill(roundRect);
        roundRect.setRoundRect(12, 12, 488, 488, 20, 20);
        g.setColor(PROGRAM_BACKGROUND_COLOR);
        g.fill(roundRect);
        rect.setRect(32, 32, 400, 448);
        g.setColor(LIST_OUTLINE_COLOR);
        g.fill(rect);
        rect.setRect(36, 36, 392, 440);
        g.setColor(LIST_BACKGROUND_COLOR);
        g.fill(rect);
        for (int i = 0; i < LIST_LINE_COUNT; i++){
            paintListLine(g, i);
        }
        for (int i = 0; i < SIDE_BUTTON_COUNT; i++){
            int yOff = 48*i;
            rect.setFrame(436, 164+yOff, 40, 40);
            g.setColor(SIDE_BUTTON_OUTLINE_COLOR);
            g.fill(rect);
            rect.setFrame(440, 168+yOff, 32, 32);
            g.setColor(SIDE_BUTTON_BACKGROUND_COLOR);
            g.fill(rect);
        }
            // Dispose of the copy of the graphics context
        g.dispose();
    }
    /**
     * 
     * @param g
     * @param index 
     */
    private void paintListLine(Graphics2D g, int index){
        if (rect == null)
            rect = new Rectangle2D.Double();
        boolean selected = index == SELECTED_LINE_INDEX;
            // Create a copy of the given graphics context
        g = (Graphics2D) g.create();
            // Translate the graphics context to the top-left corner for this line
        g.translate(36, 36+(44*index));
        if (selected){
            rect.setFrame(0, 0, 392, 44);
            g.setColor(LIST_SELECTED_BACKGROUND_COLOR);
            g.fill(rect);
        }
        rect.setFrame(10, 10, 329, 24);
        g.setColor((selected)?LIST_SELECTED_FOREGROUND_COLOR:LIST_FOREGROUND_COLOR);
        g.fill(rect);
            // Dispose of the copy of the graphics context
        g.dispose();
    }
    
}
