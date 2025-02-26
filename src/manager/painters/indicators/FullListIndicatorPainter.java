/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.painters.indicators;

import java.awt.*;
import java.awt.geom.*;

/**
 * This is the list indicator to indicate a list is full.
 * @author Mosblinker
 */
public class FullListIndicatorPainter extends AbstractListIndicatorPainter{
    /**
     * The width at which this is internally rendered at.
     */
    private static final int INTERNAL_WIDTH = 3;
    /**
     * The height at which this is internally rendered at.
     */
    private static final int INTERNAL_HEIGHT = 13;
    /**
     * This is the path used to draw the body of the exclamation mark. This is 
     * initially null and is initialized the first time it's used.
     */
    private Path2D mark = null;
    /**
     * This is the ellipse used to draw the point on the exclamation mark. This 
     * is initially null and is initialized the first time it's used.
     */
    private Ellipse2D point = null;
    @Override
    protected void paintIndicator(Graphics2D g, Component c, int width, int height) {
            // If the point ellipse has not been initialized yet
        if (point == null){
            point = new Ellipse2D.Double();
            point.setFrameFromDiagonal(0, INTERNAL_HEIGHT, INTERNAL_WIDTH, 
                    INTERNAL_HEIGHT-INTERNAL_WIDTH);
        }   // If the body path has not been initialized yet
        if (mark == null){
            mark = new Path2D.Double();
            mark.moveTo(0, 0);
            mark.lineTo(INTERNAL_WIDTH, 0);
                // This is the y-coordinate for the bottom of the body
            double bottomY = point.getMinY() - 1.5;
            mark.lineTo(INTERNAL_WIDTH-0.75, bottomY);
            mark.lineTo(0.75, bottomY);
            mark.closePath();
        }   // Scale the graphics to fit the internal rendering size
        g.scale(width/((double)INTERNAL_WIDTH),height/((double)INTERNAL_HEIGHT));
            // Draw the body of the exclamation mark
        g.fill(mark);
            // Draw the point of the exclamation mark
        g.fill(point);
    }
}
