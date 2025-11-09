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
    
    private static final double INTERNAL_RENDERING_SIZE = 40.5971;//41;
    
    private static final double INTERNAL_RENDERING_Y_OFFSET = (INTERNAL_RENDERING_SIZE-34)/2.0;
    
    public static final Color DROPBOX_COLOR = new Color
    
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
        this(48,color);
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
        g.scale(getIconWidth()/INTERNAL_RENDERING_SIZE, getIconHeight()/INTERNAL_RENDERING_SIZE);
        g.translate(0, INTERNAL_RENDERING_Y_OFFSET);
        g.setColor(color);
        if (path == null){
            path = new Path2D.Double();
            
            path.moveTo(10.1489, 0);
            path.lineTo(0, 6.375);
            path.lineTo(10.1489, 12.75);
            path.lineTo(20.2994, 6.375);
            path.lineTo(30.4482, 12.75);
            path.lineTo(40.5971, 6.375);
            path.lineTo(30.4482, 0);
            path.lineTo(20.2994, 6.375);
            path.lineTo(10.1489, 0);
            path.closePath();
            
            path.moveTo(10.1489, 25.5001);
            path.lineTo(0, 19.1251);
            path.lineTo(10.1489, 12.75);
            path.lineTo(20.2994, 19.1251);
            path.lineTo(10.1489, 25.5001);
            path.closePath();
            
            path.moveTo(20.2994, 19.1251);
            path.lineTo(30.4482, 12.75);
            path.lineTo(40.5971, 19.1251);
            path.lineTo(30.4482, 25.5001);
            path.lineTo(20.2994, 19.1251);
            path.closePath();
            
            path.moveTo(20.2994, 34);
            path.lineTo(10.1489, 27.625);
            path.lineTo(20.2994, 21.25);
            path.lineTo(30.4482, 27.625);
            path.lineTo(20.2994, 34);
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
