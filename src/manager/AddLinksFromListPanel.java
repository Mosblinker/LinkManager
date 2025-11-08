/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import components.*;
import java.awt.Component;
import javax.swing.*;

/**
 *
 * @author Milo Steier
 */
public class AddLinksFromListPanel extends AbstractConfirmDialogPanel{
    
    private void initialize(){
        setBorder(javax.swing.BorderFactory.createEmptyBorder(11, 10, 11, 10));
        setMinimumSize(new java.awt.Dimension(480, 320));
        setPreferredSize(new java.awt.Dimension(480, 320));
        popupMenu = new JPopupMenu();
        textArea = new JTextArea();
        textArea.setColumns(20);
        textArea.setLineWrap(true);
        textArea.setRows(5);
        textArea.setWrapStyleWord(true);
        textArea.addMouseListener(getDisabledComponentListener());
        textArea.setComponentPopupMenu(popupMenu);
        scrollPane = new JScrollPane(textArea);
        scrollPane.addMouseListener(getDisabledComponentListener());
        add(scrollPane, java.awt.BorderLayout.CENTER);
        controlPanel = new JPanel(new java.awt.BorderLayout(6,0));
        controlPanel.add(acceptButton,java.awt.BorderLayout.LINE_START);
        controlPanel.add(cancelButton,java.awt.BorderLayout.LINE_END);
        add(controlPanel,java.awt.BorderLayout.PAGE_END);
    }
    
    public AddLinksFromListPanel(){
        super(new java.awt.BorderLayout(10, 11));
        initialize();
    }
    @Override
    protected String getDefaultAcceptButtonToolTipText() {
        return "Add the links to the selected list.";
    }
    @Override
    protected String getDefaultCancelButtonToolTipText() {
        return null;
    }
    @Override
    protected String getDefaultAcceptButtonText(){
        return "Add Links";
    }
    @Override
    public void accept(){
        super.accept();
    }
    @Override
    public void cancel(){
        super.cancel();
    }
    
    public int showDialog(Component parent, String text){
        setText(text);
        return super.showDialog(parent);
    }
    
    public void setText(String text){
        textArea.setText(text);
    }
    
    public String getText(){
        return textArea.getText();
    }
    
    public JScrollPane getScrollPane(){
        return scrollPane;
    }
    
    public JTextArea getTextArea(){
        return textArea;
    }
    
    public JPopupMenu getTextPopupMenu(){
        return popupMenu;
    }
    
    private JPopupMenu popupMenu;
    private JScrollPane scrollPane;
    private JTextArea textArea;
    private JPanel controlPanel;
}
