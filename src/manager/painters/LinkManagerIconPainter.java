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
    
    public static final Color LINK_SYMBOL_COLOR = BORDER_OUTLINE_COLOR;
    
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
    
    private static final double SLASH_CLEAR_TRIANGLE_LENGTH = Math.sqrt(18);
    /**
     * A scratch rounded rectangle object used to draw the image.
     */
    private RoundRectangle2D roundRect = null;
    /**
     * A scratch rectangle object used to draw the image.
     */
    private Rectangle2D rect = null;
    /**
     * A scratch path object used to draw the image.
     */
    private Path2D path = null;
    /**
     * This is the shape used to draw the link icon at the end of the item in 
     * the list.
     */
    private Area linkShape = null;
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
        if (path == null)
            path = new Path2D.Double();
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
        g.setColor(ADD_BUTTON_FOREGROUND_COLOR);
        rect.setFrame(453, 172, 6, 24);
        g.fill(rect);
        rect.setFrame(444, 181, 24, 6);
        g.fill(rect);
        g.setColor(ARROW_BUTTONS_FOREGROUND_COLOR);
        g.fill(getTriangle(444, 220, 24, 24, false, path));
        g.fill(getTriangle(444, 268, 24, 24, true, path));
        g.setColor(REMOVE_BUTTON_FOREGROUND_COLOR);
        g.fill(getSlash(444, 316, 24, 24,false,path));
        g.fill(getSlash(444, 316, 24, 24,true,path));
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
        if (linkShape == null){
            if (roundRect == null)
                roundRect = new RoundRectangle2D.Double();
            if (path == null)
                path = new Path2D.Double();
            roundRect.setRoundRect(350, 10, 32, 32, 10, 10);
            linkShape = new Area(roundRect);
            roundRect.setRoundRect(354, 14, 24, 24, 10, 10);
            linkShape.subtract(new Area(roundRect));
            linkShape.subtract(new Area(getLinkSlash(366,3,23,23,8.5,path)));
            rect.setFrame(371, 2, 19, 4);
            linkShape.add(new Area(rect));
            rect.setFrame(386, 2, 4, 19);
            linkShape.add(new Area(rect));
            linkShape.add(new Area(getLinkSlash(366,3,23,23,3.5,path)));
        }
        if (!selected)
            g.setColor(LINK_SYMBOL_COLOR);
        g.fill(linkShape);
            // Dispose of the copy of the graphics context
        g.dispose();
    }
    /**
     * 
     * @param x
     * @param y
     * @param w
     * @param h
     * @param thickness
     * @param path
     * @return 
     */
    private Path2D getLinkSlash(double x, double y, double w, double h, 
            double thickness, Path2D path){
        if (path == null)
            path = new Path2D.Double();
        else
            path.reset();
        path.moveTo(x, y+h-thickness);
        path.lineTo(x, y+h);
        path.lineTo(x+thickness, y+h);
        path.lineTo(x+w, y+thickness);
        path.lineTo(x+w, y);
        path.lineTo(x+w-thickness, y);
        path.closePath();
        return path;
    }
    /**
     * 
     * @param x
     * @param y
     * @param w
     * @param h
     * @param down
     * @param path
     * @return 
     */
    private Path2D getTriangle(double x, double y, double w, double h, 
            boolean down, Path2D path){
        if (path == null)
            path = new Path2D.Double();
        else
            path.reset();
        double centerX = x + (w/2.0);
        if (down){
            path.moveTo(x, y);
            path.lineTo(centerX, y+h);
            path.lineTo(x+w, y);
        } else {
            path.moveTo(x, y+h);
            path.lineTo(centerX, y);
            path.lineTo(x+w, y+h);
        }
        path.closePath();
        return path;
    }
    /**
     * 
     * @param x
     * @param y
     * @param w
     * @param h
     * @param flip
     * @param path
     * @return 
     */
    private Path2D getSlash(double x, double y, double w, double h,boolean flip,
            Path2D path){
        if (path == null)
            path = new Path2D.Double();
        else
            path.reset();
        if (flip){
            path.moveTo(x, y+h-SLASH_CLEAR_TRIANGLE_LENGTH);
            path.lineTo(x+SLASH_CLEAR_TRIANGLE_LENGTH, y+h);
            path.lineTo(x+w, y+SLASH_CLEAR_TRIANGLE_LENGTH);
            path.lineTo(x+w-SLASH_CLEAR_TRIANGLE_LENGTH, y);
        } else {
            path.moveTo(x, y+SLASH_CLEAR_TRIANGLE_LENGTH);
            path.lineTo(x+SLASH_CLEAR_TRIANGLE_LENGTH, y);
            path.lineTo(x+w, y+h-SLASH_CLEAR_TRIANGLE_LENGTH);
            path.lineTo(x+w-SLASH_CLEAR_TRIANGLE_LENGTH, y+h);
        }
        return path;
    }
}
