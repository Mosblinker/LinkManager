/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import icons.Icon2D;
import java.awt.*;
import java.awt.geom.*;

/**
 *
 * @author Milo Steier
 */
public class DefaultPfpIcon implements Icon2D{
    
    private Ellipse2D head = null;
    
    private Area body = null;
    
    private Color bg;
    
    private Color fg;
    
    public DefaultPfpIcon(Color color){
        if (color == null)
            throw new NullPointerException();
        this.bg = color;
        fg = Color.WHITE;
        float[] hsb = Color.RGBtoHSB(bg.getRed(), bg.getGreen(), bg.getBlue(), null);
        if (hsb[2] > 0.95 && hsb[1] < 0.05)
            fg = Color.GRAY;
        constructShapes();
    }
    
    public DefaultPfpIcon(){
        this(Color.GRAY);
    }
    
    private void constructShapes(){
        double centerX = getIconWidth()/2.0;
        double centerY = getIconHeight()/2.0;
        if (head == null){
            head = new Ellipse2D.Double();
            head.setFrameFromCenter(centerX, centerY-20, centerX-20, centerY);
        }
        if (body == null){
            Ellipse2D ellipse = new Ellipse2D.Double();
            ellipse.setFrameFromCenter(centerX, centerY+25, getIconWidth()/4.0, centerY+10);
            Rectangle2D rect = new Rectangle2D.Double();
            rect.setFrameFromDiagonal(ellipse.getMinX(), ellipse.getCenterY(), 
                    ellipse.getMaxX(), getIconHeight());
            body = new Area(ellipse);
            body.add(new Area(rect));
        }
    }

    @Override
    public void paintIcon2D(Component c, Graphics2D g, int x, int y) {
        g.translate(x, y);
        g.clipRect(0, 0, getIconWidth(), getIconHeight());
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, 
                RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(bg);
        g.fillRect(0, 0, getIconWidth(), getIconHeight());
        g.setColor(fg);
        g.fill(head);
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
