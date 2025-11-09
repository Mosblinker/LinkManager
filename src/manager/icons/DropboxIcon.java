/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.icons;

import icons.Icon2D;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.geom.Path2D;

/**
 *
 * @author Mosblinker
 */
public class DropboxIcon implements Icon2D{
    
    private static final double INTERNAL_RENDERING_WIDTH = 64;
    
    private static final double INTERNAL_RENDERING_HEIGHT = 64;
    
    private Path2D path = null;
    
    private int width, height;
    
    private Color color;
    
    public DropboxIcon(int width, int height, Color color){
        if (width <= 0 || height <= 0)
            throw new IllegalArgumentException();
        if (color == null)
            throw new NullPointerException();
        this.width = width;
        this.height = height;
        this.color = color;
    }
    
    public DropboxIcon(int width, int height){
        this(width,height,Color.BLACK);
    }
    
    public DropboxIcon(int size, Color color){
        this(size,size,color);
    }
    
    public DropboxIcon(int size){
        this(size,Color.BLACK);
    }
    
    public DropboxIcon(Color color){
        this(64,color);
    }
    
    public DropboxIcon(){
        this(Color.BLACK);
    }
    
    public Color getColor(){
        return color;
    }
    
    public void setColor(Color color){
        if (color == null)
            throw new NullPointerException();
        this.color = color;
    }

    @Override
    public void paintIcon2D(Component c, Graphics2D g, int x, int y) {
        g.translate(x, y);
        g.scale(getIconWidth()/INTERNAL_RENDERING_WIDTH, getIconHeight()/INTERNAL_RENDERING_HEIGHT);
        g.setColor(color);
        if (path == null){
            path = new Path2D.Double();
            
            path.moveTo(21.851, 15);
            path.lineTo(11.7021, 21.375);
            path.lineTo(21.851, 27.75);
            path.lineTo(32.0015, 21.375);
            path.lineTo(42.1503, 27.75);
            path.lineTo(52.2992, 21.375);
            path.lineTo(42.1503, 15);
            path.lineTo(32.0015, 21.375);
            path.lineTo(21.851, 15);
            path.closePath();
            
            path.moveTo(21.851, 40.5001);
            path.lineTo(11.7021, 34.1251);
            path.lineTo(21.851, 27.75);
            path.lineTo(32.0015, 34.1251);
            path.lineTo(21.851, 40.5001);
            path.closePath();
            
            path.moveTo(32.0015, 34.1251);
            path.lineTo(42.1503, 27.75);
            path.lineTo(52.2992, 34.1251);
            path.lineTo(42.1503, 40.5001);
            path.lineTo(32.0015, 34.1251);
            path.closePath();
            
            path.moveTo(32.0015, 49);
            path.lineTo(21.851, 42.625);
            path.lineTo(32.0015, 36.25);
            path.lineTo(42.1503, 42.625);
            path.lineTo(32.0015, 49);
            path.closePath();
        }
        
        g.fill(path);
    }
    @Override
    public int getIconWidth() {
        return width;
    }
    @Override
    public int getIconHeight() {
        return height;
    }
}
