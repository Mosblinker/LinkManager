/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.painters.indicators;

import java.awt.*;
import java.awt.geom.*;

/**
 * This is the list indicator to indicate a list is read only.
 * @author Mosblinker
 */
public class ReadOnlyListIndicatorPainter extends AbstractListIndicatorPainter{
    /**
     * The width at which this is internally rendered at.
     */
    private static final int INTERNAL_WIDTH = 10;
    /**
     * The height at which this is internally rendered at.
     */
    private static final int INTERNAL_HEIGHT = 13;
    /**
     * This is the shape used to render the shackle of the padlock. This is 
     * initially null and is initialized the first time it's used.
     */
    private Area shackle = null;
    /**
     * This is the shape used to render the body of the padlock. This is 
     * initially null and is initialized the first time it's used.
     */
    private RoundRectangle2D body = null;
    @Override
    protected void paintIndicator(Graphics2D g, Component c, int width, int height) {
            // If the padlock body has not been initialized yet
        if (body == null){
            body = new RoundRectangle2D.Double();
                // Set the frame using a diagonal. It's currently a normal 
                // rectangle
            body.setFrameFromDiagonal(0, (INTERNAL_HEIGHT*3)/8.0, 
                    INTERNAL_WIDTH, INTERNAL_HEIGHT);
                // Set the shape of it again, but with the size and location 
                // that it already has, thus just setting the arc width and 
                // height
            body.setRoundRect(body.getX(), body.getY(), body.getWidth(), 
                    body.getHeight(), 2.5, 2.5);
        }   // If the padlock shackle has not been initialized yet
        if (shackle == null){
                // Get the minimum x for the padlock shackle
            double padlockX1 = body.getMinX()+1.5;
                // Get the maximum x for the padlock shackle
            double padlockX2 = body.getMaxX()-1.5;
                // Create an ellipse object to use to create the shackle
            Ellipse2D e = new Ellipse2D.Double();
                // Create a rectangle object to use to create the shackle
            Rectangle2D rect = new Rectangle2D.Double();
            e.setFrameFromDiagonal(padlockX1, 0, padlockX2, (padlockX2-padlockX1));
            rect.setFrameFromDiagonal(padlockX1, e.getCenterY(), padlockX2, 
                    body.getMinY()+1);
            shackle = new Area(e);
            shackle.add(new Area(rect));
                // Shrink the ellipse by 1.25 on all sides
            e.setFrameFromCenter(e.getCenterX(), e.getCenterY(), 
                    e.getMinX()+1.25, e.getMinY()+1.25);
                // Make the rectangle match the width of the ellipse
            rect.setFrameFromCenter(rect.getCenterX(), rect.getCenterY(), 
                    e.getMinX(), rect.getMinY());
                // Remove this area to make the shackle a curve
            shackle.subtract(new Area(e));
            shackle.subtract(new Area(rect));
        }   // Scale the graphics to fit the internal rendering size
        g.scale(width/((double)INTERNAL_WIDTH),height/((double)INTERNAL_HEIGHT));
            // Paint the padlock shackle
        g.fill(shackle);
            // Paint the padlock body
        g.fill(body);
    }
}