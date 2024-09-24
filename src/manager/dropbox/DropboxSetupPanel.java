/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import components.*;
import components.label.HyperlinkLabel;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author Milo Steier
 */
public class DropboxSetupPanel extends AbstractConfirmDialogPanel{
    
    public static final String AUTHORIZATION_LINK_PROPERTY_CHANGED = 
            "AuthorizationLinkPropertyChanged";

    @Override
    protected String getDefaultAcceptButtonToolTipText() {
        return "Setup Dropbox.";
    }
    @Override
    protected String getDefaultCancelButtonToolTipText() {
        return "Cancel Dropbox setup.";
    }
    
    private void addInstructionComponent(JComponent comp, int y, double weightX, double weightY){
           // This is the constraints object to use to place the component
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = y;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridwidth = 2;
        constraints.weightx = weightX;
        constraints.weighty = weightY;
        constraints.insets = new Insets(7,0,0,0);
        instPanel.add(comp,constraints);
    }
    
    private void addInstructionComponent(JComponent comp, int y){
        addInstructionComponent(comp,y,0,0);
    }
    
    private void initialize(String link){
        setBorder(javax.swing.BorderFactory.createEmptyBorder(11, 10, 11, 10));
        setMinimumSize(new java.awt.Dimension(380, 240));
        setPreferredSize(new java.awt.Dimension(480, 240));
        Handler handler = new Handler();
        popupMenu = new JPopupMenu();
        authCodeField = new JTextField();
        authCodeField.addMouseListener(getDisabledComponentListener());
        authCodeField.getDocument().addDocumentListener(handler);
        authCodeField.setComponentPopupMenu(popupMenu);
        authURLLabel = new HyperlinkLabel(
                "Click on this link to go to the Dropbox Authorization Website", 
                link);
        hyperlinkPopup = new HyperlinkLabel.HyperlinkPopup(authURLLabel);
        hyperlinkPopup.addMouseListener(getDisabledComponentListener());
        authURLLabel.setComponentPopupMenu(hyperlinkPopup);
        
        instPanel = new JPanel(new GridBagLayout());
        textLabels = new JLabel[]{
            new JLabel("1."),
            authURLLabel,
            new JLabel("if it does not open automatically."),
            new JLabel("2. Click \"Allow\" (you might have to login to Dropbox first)."),
            new JLabel("3. Copy the authorization code."),
            new JLabel("4. Enter the authorization code here:"),
            new JLabel("5. Press \"OK\" to continue.")
        };
           // This is the constraints object to use to place the components
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.anchor = GridBagConstraints.NORTHWEST;
        constraints.gridheight = 2;
        constraints.insets = new Insets(0,0,0,7);
        instPanel.add(textLabels[0],constraints);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 0;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        instPanel.add(textLabels[1],constraints);
        constraints = new GridBagConstraints();
        constraints.gridx = 1;
        constraints.gridy = 1;
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.anchor = GridBagConstraints.NORTHEAST;
        constraints.insets = new Insets(7,0,0,0);
        instPanel.add(textLabels[2],constraints);
        addInstructionComponent(textLabels[3],2);
        addInstructionComponent(textLabels[4],3);
        addInstructionComponent(textLabels[5],4);
        addInstructionComponent(authCodeField,5,0.5,0);
        addInstructionComponent(textLabels[6],6);
        addInstructionComponent(new Box.Filler(new Dimension(0, 0), 
                new Dimension(0, 0), new Dimension(0, 32767)),7,0,0.9);
        add(instPanel,BorderLayout.CENTER);
        
        controlPanel = new JPanel(new GridLayout(1, 0, 6, 0));
        controlPanel.setInheritsPopupMenu(true);
        controlPanel.add(acceptButton);
        controlPanel.add(cancelButton);
        
        bottomPanel = new JPanel(new BorderLayout(6,0));
        bottomPanel.add(controlPanel, BorderLayout.LINE_END);
        add(bottomPanel,BorderLayout.PAGE_END);
    }
    
    public DropboxSetupPanel(String link, String title){
        super(new BorderLayout(10, 11), title);
        initialize(link);
    }
    
    public DropboxSetupPanel(String link){
        this(link,null);
    }
    
    public DropboxSetupPanel(){
        this(null);
    }
    
    public String getAuthorizationLink(){
        return authURLLabel.getLink();
    }
    
    public void setAuthorizationLink(String link){
        resetHyperlink();
        if (Objects.equals(link, getAuthorizationLink()))
            return;
        String oldLink = getAuthorizationLink();
        authURLLabel.setLink(link);
        firePropertyChange(AUTHORIZATION_LINK_PROPERTY_CHANGED,oldLink,link);
    }
    
    public URL getAuthorizationURL() throws MalformedURLException{
        return authURLLabel.getURL();
    }
    
    public void resetHyperlink(){
        authURLLabel.resetHyperlink();
    }
    
    public boolean openAuthorizationLink(){
        return authURLLabel.openLink();
    }
    
    public void copyAuthorizationLink(Clipboard clipboard){
        authURLLabel.copyLink(clipboard);
    }
    
    public void copyAuthorizationLink(){
        authURLLabel.copyLink();
    }
    
    public String getAuthorizationCode(){
        if (authCodeField == null)
            return null;
        String text = authCodeField.getText();
        return (text != null) ? text.trim() : null;
    }
    
    public void setAuthorizationCode(String code){
        if (authCodeField == null)
            return;
        if (code != null)
            code = code.trim();
        authCodeField.setText(code);
    }
    
    public void clearAuthorizationCode(){
        setAuthorizationCode(null);
    }
    
    public JTextField getAuthorizationCodeField(){
        return authCodeField;
    }
    
    public JPopupMenu getAuthorizationCodePopupMenu(){
        return popupMenu;
    }
    
    public void addDocumentListener(DocumentListener l){
        if (l != null)
            listenerList.add(DocumentListener.class, l);
    }
    
    public void removeDocumentListener(DocumentListener l){
        listenerList.remove(DocumentListener.class, l);
    }
    
    public DocumentListener[] getDocumentListeners(){
        return listenerList.getListeners(DocumentListener.class);
    }
    
    @Override
    protected boolean isAcceptEnabled(){
        return super.isAcceptEnabled()&&getAuthorizationCode()!=null && 
                !getAuthorizationCode().isBlank();
    }
    @Override
    public void cancel(){
        clearAuthorizationCode();
        resetHyperlink();
        super.cancel();
    }
    @Override
    protected void setupDialog(JDialog dialog){
        super.setupDialog(dialog);
        dialog.setResizable(false);
    }
    @Override
    public int showDialog(Component parent){
        clearAuthorizationCode();
        resetHyperlink();
        return super.showDialog(parent);
    }
    
    public int showDialog(Component parent, String link){
        if (getDialog() == null)    // If the dialog is not showing
            setAuthorizationLink(link);
        return showDialog(parent);
    }
    /**
     * This is the panel that contains the control buttons.
     */
    protected JPanel controlPanel;
    /**
     * This is the panel at the bottom of this panel used to contain the control 
     * panel.
     */
    protected JPanel bottomPanel;
    
    protected JPanel instPanel;
    
    private HyperlinkLabel authURLLabel;
    
    private HyperlinkLabel.HyperlinkPopup hyperlinkPopup;
    
    private JTextField authCodeField;
    
    private JPopupMenu popupMenu;
    
    private JLabel[] textLabels;
    
    private class Handler implements DocumentListener{
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateAcceptEnabled();
            for (DocumentListener l : getDocumentListeners()){
                if (l != null)
                    l.insertUpdate(e);
            }
        }
        @Override
        public void removeUpdate(DocumentEvent e) {
            updateAcceptEnabled();
            for (DocumentListener l : getDocumentListeners()){
                if (l != null)
                    l.removeUpdate(e);
            }
        }
        @Override
        public void changedUpdate(DocumentEvent e) {
            updateAcceptEnabled();
            for (DocumentListener l : getDocumentListeners()){
                if (l != null)
                    l.changedUpdate(e);
            }
        }
    }
}
