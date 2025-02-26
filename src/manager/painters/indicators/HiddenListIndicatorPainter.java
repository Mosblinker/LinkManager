/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.painters.indicators;

import java.awt.*;
import java.awt.geom.*;

/**
 * This is the list indicator to indicate a list is hidden.
 * @author Mosblinker
 */
public class HiddenListIndicatorPainter extends AbstractListIndicatorPainter{
    /**
     * The width at which this is internally rendered at.
     */
    private static final int INTERNAL_WIDTH = 10;
    /**
     * The height at which this is internally rendered at.
     */
    private static final int INTERNAL_HEIGHT = 13;
    /**
     * This is the path that forms the outline for the bounds of the eye. This 
     * is initially null and is initialized the first time it's used.
     */
    private Path2D outline = null;
    /**
     * This is the arc used to form the outline for the iris of the eye. This is 
     * initially null and is initialized the first time it's used.
     */
    private Arc2D irisOutline = null;
    /**
     * This is the shape used to draw the pupil of the eye. This is initially 
     * null and is initialized the first time it's used.
     */
    private Area pupil = null;
    @Override
    protected void paintIndicator(Graphics2D g, Component c, int width, int height) {
            // If the eye iris outline has not been initialized yet
        if (irisOutline == null){
            irisOutline = new Arc2D.Double();
                // The arc is centered, and has a radius of 2.5.
            irisOutline.setArcByCenter(INTERNAL_WIDTH/2.0,INTERNAL_HEIGHT/2.0, 
                    2.5, 160, 310, Arc2D.OPEN);
        }   // If the eye pupil shape has not been initialized yet
        if (pupil == null){
                // Create an ellipse object to use to create the pupil
            Ellipse2D e = new Ellipse2D.Double();
                // The pupil is 2 pixels smaller than the iris outline
            e.setFrameFromCenter(irisOutline.getCenterX(),irisOutline.getCenterY(),
                    irisOutline.getMinX()+1,irisOutline.getMinY()+1);
            pupil = new Area(e);
                // Create an arc to copy the iris outline
            Arc2D arc = new Arc2D.Double();
            arc.setArc(irisOutline);
                // Make this arc's type a pie arc
            arc.setArcType(Arc2D.PIE);
                // Use it to keep only the part of the pupil in the arc for the 
                // iris outline
            pupil.intersect(new Area(arc));
        }   // If the eye outline has not been initialized yet
        if (outline == null){
            outline = new Path2D.Double();
            outline.moveTo(0, irisOutline.getCenterY());
                // This is the minimum y-coordinate for the eye outline
            double minY = irisOutline.getMinY()-1;
                // This is the maximum y-coordinate for the eye outline
            double maxY = irisOutline.getMaxY()+1;
                // This is the x-coordinate for the first control point in the 
                // top-left curve.
            double ctrlPt1X = irisOutline.getMinX()/2.0;
                // This is the x-coordinate for the second control point in the 
                // top-right curve
            double ctrlPt2X = irisOutline.getCenterX()+(irisOutline.getMaxX()/2.0);
            outline.curveTo(ctrlPt1X, irisOutline.getMinY(), 
                    irisOutline.getMinX()+0.5, minY, 
                    irisOutline.getCenterX(), minY);
            outline.curveTo(irisOutline.getMaxX()+0.5, minY, 
                    ctrlPt2X, irisOutline.getMinY(), 
                    INTERNAL_WIDTH, irisOutline.getCenterY());
                // Do the same for the bottom, but backwards
            outline.curveTo(ctrlPt2X,irisOutline.getMaxY(), 
                    irisOutline.getMaxX()+0.5, maxY, 
                    irisOutline.getCenterX(), maxY);
            outline.curveTo(irisOutline.getMinX()+0.5, maxY, 
                    ctrlPt1X, irisOutline.getMaxY(), 
                    0, irisOutline.getCenterY());
            outline.closePath();
        }   // Scale the graphics to fit the internal rendering size
        g.scale(width/((double)INTERNAL_WIDTH),height/((double)INTERNAL_HEIGHT));
            // Paint the eye iris outline
        g.draw(irisOutline);
            // Paint the eye pupil
        g.fill(pupil);
            // Set the stroke's line width to be 1.25
        g.setStroke(new BasicStroke(1.25f));
            // Paint the eye outline
        g.draw(outline);
    }
}
