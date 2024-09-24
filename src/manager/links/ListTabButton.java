/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import java.awt.*;
import javax.swing.*;
import javax.swing.plaf.basic.BasicButtonUI;

/**
 *
 * @author Milo Steier
 */
public class ListTabButton extends JButton{
    
    public static final String MOVE_TAB_LEFT_ACTION = "MoveTabLeft";
    
    public static final String MOVE_TAB_RIGHT_ACTION = "MoveTabRight";
    
    public static final String REMOVE_TAB_ACTION = "RemoveTab";
    
    public static final String ADD_TAB_ACTION = "AddTab";
    
    public static final String RENAME_TAB_ACTION = "RenameTab";
    
    public ListTabButton(String actionCommand){
        super();
        setActionCommand(actionCommand);
        setPreferredSize(new Dimension(17, 17));
        setMaximumSize(new Dimension(17, 17));
        setMinimumSize(new Dimension(17, 17));
        //Make the button looks the same for all Laf's
        setUI(new BasicButtonUI());
        //Make it transparent
        setContentAreaFilled(false);
        //No need to be focusable
        setFocusable(false);
        if (ADD_TAB_ACTION.equals(actionCommand)){
            setBorderPainted(false);
            setInheritsPopupMenu(true);
        }
        else{
            setBorder(BorderFactory.createEtchedBorder());
        }
        setRolloverEnabled(true);
        String toolTip = null;
        switch(actionCommand){
            case(ADD_TAB_ACTION):
                toolTip = "Add List";
                break;
            case(MOVE_TAB_LEFT_ACTION):
                toolTip = "Move List to the Left";
                break;
            case(MOVE_TAB_RIGHT_ACTION):
                toolTip = "Move List to the Right";
                break;
            case(REMOVE_TAB_ACTION):
                toolTip = "Remove List";
                break;
            case(RENAME_TAB_ACTION):
                toolTip = "Rename List";
        }
        setToolTipText(toolTip);
    }

    //we don't want to update UI for this button
    @Override
    public void updateUI() { }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        //shift the image for pressed buttons
        if (getModel().isPressed()) {
            g2.translate(1, 1);
        }
        g2.setStroke(new BasicStroke(2));
        Color color = Color.BLACK;
        if (getModel().isRollover() || getModel().isPressed()){
            switch(getActionCommand()){
                case(REMOVE_TAB_ACTION):
                    color = Color.RED;
                    break;
                case(ADD_TAB_ACTION):
                    color = Color.GREEN;
                    break;
                case(RENAME_TAB_ACTION):
                    color = Color.CYAN;
                    break;
                case(MOVE_TAB_LEFT_ACTION):
                case(MOVE_TAB_RIGHT_ACTION):
                    color = Color.BLUE;
            }
        }
        g2.setColor(color);
        int delta = 5;
        int[] xPoints = null;
//            System.out.println((getWidth() - delta - 1) + " " + (getHeight() - delta - 1));
        switch(getActionCommand()){
            case(REMOVE_TAB_ACTION):
                delta++;
                g2.drawLine(delta, delta, getWidth() - delta - 1, getHeight() - delta - 1);
                g2.drawLine(getWidth() - delta - 1, delta, delta, getHeight() - delta - 1);
                break;
            case(ADD_TAB_ACTION):
                g2.drawLine(getWidth()/2, delta, getWidth()/2, getHeight() - delta - 1);
                g2.drawLine(delta, getHeight()/2, getWidth() - delta - 1, getHeight()/2);
                break;
            case(RENAME_TAB_ACTION):
                g2.fillRect(7, delta, getWidth()-15, getHeight() - delta - delta-1);
                break;
            case(MOVE_TAB_LEFT_ACTION):
                xPoints = new int[]{delta, getWidth() - delta - 1, getWidth() - delta - 1};
            case(MOVE_TAB_RIGHT_ACTION):
                if (xPoints == null)
                    xPoints  = new int[]{getWidth() - delta - 1, delta, delta};
                g2.fillPolygon(xPoints, 
                        new int[]{getHeight()/2,delta,getHeight() - delta - 1}, 3);
                
        }

        g2.dispose();
    }
    
}
