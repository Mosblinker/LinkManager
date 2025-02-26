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
 * This is a painter that is used to paint the icon for LinkManager.
 * @author Mosblinker
 * @see manager.LinkManager
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
    /**
     * This is the color for the border around the program in the painted image.
     */
    public static final Color BORDER_OUTLINE_COLOR = new Color(0x3967A3);
    /**
     * This is the color for the background of the program in the painted image.
     */
    public static final Color PROGRAM_BACKGROUND_COLOR = new Color(0xF0F0F0);
    /**
     * This is the color for the outline of the list in the painted image.
     */
    public static final Color LIST_OUTLINE_COLOR = Color.BLACK;
    /**
     * This is the color for the background of the list in the painted image.
     */
    public static final Color LIST_BACKGROUND_COLOR = Color.WHITE;
    /**
     * This is the color for the foreground of the list in the painted image.
     */
    public static final Color LIST_FOREGROUND_COLOR = Color.BLACK;
    /**
     * This is the color for the symbols for the links in the list.
     */
    public static final Color LINK_SYMBOL_COLOR = BORDER_OUTLINE_COLOR;
    /**
     * This is the color for the background fro the selected item in the list.
     */
    public static final Color LIST_SELECTED_BACKGROUND_COLOR = new Color(0x0078D7);
    /**
     * This is the color for the foreground fro the selected item in the list.
     */
    public static final Color LIST_SELECTED_FOREGROUND_COLOR = LIST_BACKGROUND_COLOR;
    /**
     * This is the color for the outline for the side buttons in the painted 
     * image.
     */
    public static final Color SIDE_BUTTON_OUTLINE_COLOR = new Color(0xADADAD);
    /**
     * This is the color for the background for the side buttons in the painted 
     * image.
     */
    public static final Color SIDE_BUTTON_BACKGROUND_COLOR = new Color(0xE1E1E1);
    /**
     * This is the color for the symbol for the add button in the painted image.
     */
    public static final Color ADD_BUTTON_FOREGROUND_COLOR = new Color(0x007F00);
    /**
     * This is the color for the symbol for the arrow buttons in the painted 
     * image.
     */
    public static final Color ARROW_BUTTONS_FOREGROUND_COLOR = Color.BLACK;
    /**
     * This is the color for the symbol for the remove button in the painted 
     * image.
     */
    public static final Color REMOVE_BUTTON_FOREGROUND_COLOR = Color.RED;
    /**
     * This is the amount of side buttons in the image.
     */
    private static final int SIDE_BUTTON_COUNT = 4;
    /**
     * This is the amount of items in the list in the image.
     */
    private static final int LIST_ITEM_COUNT = 10;
    /**
     * This is the index for the selected item in the list in the image.
     */
    private static final int LIST_SELECTED_ITEM_INDEX = 5;
    /**
     * A scratch rounded rectangle object used to draw the image. This is 
     * initially null and is initialized the first time it's used.
     */
    private RoundRectangle2D roundRect = null;
    /**
     * A scratch rectangle object used to draw the image. This is initially null 
     * and is initialized the first time it's used.
     */
    private Rectangle2D rect = null;
    /**
     * A scratch path object used to draw the image. This is initially null and 
     * is initialized the first time it's used.
     */
    private Path2D path = null;
    /**
     * This is the shape used to draw the link symbol at the end of the item in 
     * the list. This is initially null and is initialized the first time it's 
     * used.
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
            // If the scratch rounded rectangle has not been initialized yet
        if (roundRect == null)
            roundRect = new RoundRectangle2D.Double();
            // If the scratch rectangle has not been initialized yet
        if (rect == null)
            rect = new Rectangle2D.Double();
            // If the scratch path has not been initialized yet
        if (path == null)
            path = new Path2D.Double();
            // Draw the border around the program
        g.setColor(BORDER_OUTLINE_COLOR);
        roundRect.setRoundRect(8, 8, 496, 496, 20, 20);
        g.fill(roundRect);
            // Draw the background for the program
        g.setColor(PROGRAM_BACKGROUND_COLOR);
        roundRect.setRoundRect(12, 12, 488, 488, 20, 20);
        g.fill(roundRect);
            // Draw the outline for the list
        g.setColor(LIST_OUTLINE_COLOR);
        rect.setRect(32, 32, 400, 448);
        g.fill(rect);
            // Draw the background for the list
        g.setColor(LIST_BACKGROUND_COLOR);
        rect.setRect(36, 36, 392, 440);
        g.fill(rect);
            // Go through the items in the list
        for (int i = 0; i < LIST_ITEM_COUNT; i++){
                // Paint this item in the list
            paintListItem(g, i);
        }   // Go through the side buttons
        for (int i = 0; i < SIDE_BUTTON_COUNT; i++){
                // Calculate the appropriate y offset for this button to 
                // position it
            int yOff = 48*i;
                // Draw the outline for the button
            g.setColor(SIDE_BUTTON_OUTLINE_COLOR);
            rect.setFrame(436, 164+yOff, 40, 40);
            g.fill(rect);
                // Draw the background for the button
            g.setColor(SIDE_BUTTON_BACKGROUND_COLOR);
            rect.setFrame(440, 168+yOff, 32, 32);
            g.fill(rect);
        }   // Set the color to the add button foreground to draw the add button 
        g.setColor(ADD_BUTTON_FOREGROUND_COLOR);        // symbol
            // Draw the vertical part of the add button's plus
        rect.setFrame(453, 172, 6, 24);
        g.fill(rect);
            // Draw the horizontal part of the add button's plus
        rect.setFrame(444, 181, 24, 6);
        g.fill(rect);
            // Set the color to the arrow buttons foreground to draw the arrow  
        g.setColor(ARROW_BUTTONS_FOREGROUND_COLOR);     // button symbols
            // Draw the up arrow
        g.fill(getTriangle(444, 220, 24, 24, false, path));
            // Draw the down arrow
        g.fill(getTriangle(444, 268, 24, 24, true, path));
            // Set the color to the remove button foreground to draw the remove 
        g.setColor(REMOVE_BUTTON_FOREGROUND_COLOR);     // button symbol
            // Draw a slash going top-left to down-right
        g.fill(getSlash(444, 316, 24, 24, 6, false, path));
            // Draw a slash going top-right to down-left
        g.fill(getSlash(444, 316, 24, 24, 6, true, path));
            // Dispose of the copy of the graphics context
        g.dispose();
    }
    /**
     * This is used to paint the items in the list in the painted image.
     * @param g The graphics context to render to.
     * @param index The index for the item in the list to paint.
     */
    private void paintListItem(Graphics2D g, int index){
            // If the scratch rectangle has not been initialized yet
        if (rect == null)
            rect = new Rectangle2D.Double();
            // Get whether this is the selected item
        boolean selected = index == LIST_SELECTED_ITEM_INDEX;
            // Create a copy of the given graphics context
        g = (Graphics2D) g.create();
            // Translate the graphics context to the top-left corner for this 
        g.translate(36, 36+(44*index));     // item
            // If this is the selected item
        if (selected){
                // Draw the selected item background
            g.setColor(LIST_SELECTED_BACKGROUND_COLOR);
            rect.setFrame(0, 0, 392, 44);
            g.fill(rect);
        }   // Set the color to the foreground color in order to draw the list 
            // item. If the item is selected, use the selected item foreground 
            // color. Otherwise, use the normal list foreground color
        g.setColor((selected)?LIST_SELECTED_FOREGROUND_COLOR:LIST_FOREGROUND_COLOR);
            // Draw a rectangle to represent text in the item
        rect.setFrame(10, 10, 329, 24);
        g.fill(rect);
            // If the link symbol shape has not been initialized yet
        if (linkShape == null){
                // If the scratch rounded rectangle has not been initialized yet
            if (roundRect == null)
                roundRect = new RoundRectangle2D.Double();
                // If the scratch path has not been initialized yet
            if (path == null)
                path = new Path2D.Double();
                // Add a rounded rectangle to the link symbol
            roundRect.setRoundRect(350, 10, 32, 32, 10, 10);
            linkShape = new Area(roundRect);
                // Hollow out that rounded rectangle
            roundRect.setRoundRect(354, 14, 24, 24, 10, 10);
            linkShape.subtract(new Area(roundRect));
                // Remove a portion of the rounded rectangle to make way for the 
                // link's arrow
            linkShape.subtract(new Area(getLinkSlash(366,3,23,23,8.5,path)));
                // Add the two lines for the arrow's point
            rect.setFrame(371, 2, 19, 4);
            linkShape.add(new Area(rect));
            rect.setFrame(386, 2, 4, 19);
            linkShape.add(new Area(rect));
                // Add a slash for the diagonal line of the link
            linkShape.add(new Area(getLinkSlash(366,3,23,23,3.5,path)));
        }   // If this is not the selected item
        if (!selected)
                // Draw the link symbol in it's corresponding color
            g.setColor(LINK_SYMBOL_COLOR);
            // Draw the link symbol.
        g.fill(linkShape);
            // Dispose of the copy of the graphics context
        g.dispose();
    }
    /**
     * This generates and returns a slash to use for creating the link symbol.
     * @param x The x-coordinate of the top-left corner of the slash.
     * @param y The y-coordinate of the top-left corner of the slash.
     * @param w The width of the slash.
     * @param h The height of the slash.
     * @param thickness The thickness of the slash.
     * @param path The path to store the slash in, or null.
     * @return A path with the slash stored in it.
     */
    private Path2D getLinkSlash(double x, double y, double w, double h, 
            double thickness, Path2D path){
            // If the given path is null
        if (path == null)
            path = new Path2D.Double();
        else    // Reset the path
            path.reset();
        path.moveTo(x, y+h-thickness);
        path.lineTo(x, y+h);
        path.lineTo(x+thickness, y+h);
        path.lineTo(x+w, y+thickness);
        path.lineTo(x+w, y);
        path.lineTo(x+w-thickness, y);
            // Close the path
        path.closePath();
        return path;
    }
    /**
     * This generates and returns a triangle to use for the arrow buttons.
     * @param x The x-coordinate of the top-left corner of the triangle.
     * @param y The y-coordinate of the top-left corner of the triangle.
     * @param w The width of the triangle.
     * @param h The height of the triangle.
     * @param down {@code true} if the triangle should point down, {@code false} 
     * if the triangle should point up.
     * @param path The path to store the triangle in, or null.
     * @return A path with the triangle stored in it.
     */
    private Path2D getTriangle(double x, double y, double w, double h, 
            boolean down, Path2D path){
            // If the given path is null
        if (path == null)
            path = new Path2D.Double();
        else    // Reset the path
            path.reset();
            // Get the center x-coordinate
        double centerX = x + (w/2.0);
        if (down){  // If the triangle is pointing down
            path.moveTo(x, y);
            path.lineTo(centerX, y+h);
            path.lineTo(x+w, y);
        } else {
            path.moveTo(x, y+h);
            path.lineTo(centerX, y);
            path.lineTo(x+w, y+h);
        }   // Close the path
        path.closePath();
        return path;
    }
    /**
     * This generates and returns a slash to use for the remove button's symbol.
     * @param x The x-coordinate of the top-left corner of the slash.
     * @param y The y-coordinate of the top-left corner of the slash.
     * @param w The width of the slash.
     * @param h The height of the slash.
     * @param thickness The thickness of the slash.
     * @param flip {@code true} if the slash should go top-right to down-left, 
     * {@code false} if the slash should go top-left to down-right.
     * @param path The path to store the slash in, or null.
     * @return A path with the slash stored in it.
     */
    private Path2D getSlash(double x, double y, double w, double h,
            double thickness,boolean flip,Path2D path){
            // If the given path is null
        if (path == null)
            path = new Path2D.Double();
        else    // Reset the path
            path.reset();
            // This is the offset to use to remove the corners of the area from 
            // the slash
        double offset = Math.sqrt(Math.pow(thickness, 2)/2.0);
            // If the slash is flipped
        if (flip){
            path.moveTo(x, y+h-offset);
            path.lineTo(x+offset, y+h);
            path.lineTo(x+w, y+offset);
            path.lineTo(x+w-offset, y);
        } else {
            path.moveTo(x, y+offset);
            path.lineTo(x+offset, y);
            path.lineTo(x+w, y+h-offset);
            path.lineTo(x+w-offset, y+h);
        }   // Close the path
        path.closePath();
        return path;
    }
}
