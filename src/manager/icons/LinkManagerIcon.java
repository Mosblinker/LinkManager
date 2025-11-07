/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.icons;

import icons.Icon2D;
import java.awt.Component;
import java.awt.Graphics2D;
import java.util.Objects;
import manager.painters.LinkManagerIconPainter;

/**
 *
 * @author Mosblinker
 */
public class LinkManagerIcon implements Icon2D{
    /**
     * The width and height for this icon.
     */
    private int size;
    /**
     * The painter for this icon.
     */
    private LinkManagerIconPainter painter;
    /**
     * 
     * @param size
     * @param painter 
     */
    public LinkManagerIcon(int size, LinkManagerIconPainter painter){
        this.size = size;
        this.painter = Objects.requireNonNull(painter);
    }
    @Override
    public void paintIcon2D(Component c, Graphics2D g, int x, int y) {
        g.translate(x, y);
        painter.paint(g, null, getIconWidth(), getIconHeight());
    }
    @Override
    public int getIconWidth() {
        return size;
    }
    @Override
    public int getIconHeight() {
        return size;
    }
    
}
