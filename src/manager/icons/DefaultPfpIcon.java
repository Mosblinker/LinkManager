/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.icons;

import icons.Icon2D;
import java.awt.*;
import java.awt.geom.*;

/**
 * This is the icon to use as the default profile picture when there is no 
 * profile picture set for a user.
 * @author Milo Steier
 */
public class DefaultPfpIcon implements Icon2D{
    /**
     * This is the ellipse used to draw the head.
     */
    private Ellipse2D head = null;
    /**
     * This is the shape used to draw the body.
     */
    private Area body = null;
    /**
     * This is the background color for the icon.
     */
    private Color bg;
    /**
     * This is the foreground color for the icon.
     */
    private Color fg;
    /**
     * This constructs a DefaultPfpIcon with the given background color.
     * @param color The background color for the icon (cannot be null).
     * @throws NullPointerException If the given background color is null.
     */
    public DefaultPfpIcon(Color color){
            // If the given color is null
        if (color == null)
            throw new NullPointerException();
        this.bg = color;
        fg = Color.WHITE;
            // Get the hue, saturation, and brightness of the background color
        float[] hsb = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
            // If the background color is close enough to white that the 
            // foreground may be too difficult to see
        if (hsb[2] > 0.95 && hsb[1] < 0.05)
                // Use gray as the foreground color instead
            fg = Color.GRAY;
            // Construct the shapes to use
        constructShapes();
    }
    /**
     * This constructs a DefaultPfpIcon with a {@link Color#GRAY gray} 
     * background.
     * @see Color#GRAY
     */
    public DefaultPfpIcon(){
        this(Color.GRAY);
    }
    /**
     * This returns the background color for this icon.
     * @return The background color for this icon.
     */
    public Color getBackground(){
        return bg;
    }
    /**
     * This constructs the shapes used to draw the icon.
     */
    private void constructShapes(){
            // This is the x-coordinate of the center of the icon
        double centerX = getIconWidth()/2.0;
            // This is the y-coordinate of the center of the icon
        double centerY = getIconHeight()/2.0;
            // If the head has not been initialized yet
        if (head == null){
            head = new Ellipse2D.Double();
                // Head is in the horizontal center, is 40x40, and the bottom 
                // of the head is at the vertical center
            head.setFrameFromCenter(centerX, centerY-20, centerX-20, centerY);
        }   // If the body shape has not been initialized yet
        if (body == null){
                // Create an ellipse object to use to create the body
            Ellipse2D ellipse = new Ellipse2D.Double();
                // This ellipse is 10 pixels below the center, is half the width 
                // of the icon, and is 30 pixels tall
            ellipse.setFrameFromCenter(centerX, centerY+25, getIconWidth()/4.0, 
                    centerY+10);
                // Create a rectangle object to use to create the body
            Rectangle2D rect = new Rectangle2D.Double();
                // This rectangle is from the center-left of the ellipse to the 
                // right of the ellipse and bottom of the icon
            rect.setFrameFromDiagonal(ellipse.getMinX(), ellipse.getCenterY(), 
                    ellipse.getMaxX(), getIconHeight());
                // Create the body shape using the ellipse and rectangle
            body = new Area(ellipse);
            body.add(new Area(rect));
        }
    }
    @Override
    public void paintIcon2D(Component c, Graphics2D g, int x, int y) {
            // Translate the graphics to the top-left of the icon
        g.translate(x, y);
            // Clip the graphics to the icon's size
        g.clipRect(0, 0, getIconWidth(), getIconHeight());
            // Enable antialiasing
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
            // Prioritize rendering quality over speed
        g.setRenderingHint(RenderingHints.KEY_RENDERING, 
                RenderingHints.VALUE_RENDER_QUALITY);
            // Draw the icon's background
        g.setColor(bg);
        g.fillRect(0, 0, getIconWidth(), getIconHeight());
            // Set the color to the icon's foreground
        g.setColor(fg);
            // Draw the head
        g.fill(head);
            // Draw the body
        g.fill(body);
    }
    @Override
    public int getIconWidth() {
        return 100;
    }
    @Override
    public int getIconHeight() {
        return 100;
    }
}
