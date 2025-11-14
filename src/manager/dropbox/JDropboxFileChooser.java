/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.v2.*;
import com.dropbox.core.v2.files.*;
import components.AbstractConfirmDialogPanel;
import components.ArrayComboBoxModel;
import components.ArrayListModel;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.event.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import manager.*;
import manager.renderer.*;
import manager.util.*;

/**
 *
 * @author Mosblinker
 */
public class JDropboxFileChooser extends AbstractConfirmDialogPanel {
    /**
     * 
     */
    public static final String SELECTED_PATH_PROPERTY_CHANGED = 
            "SelectedPathPropertyChanged";
    /**
     * 
     */
    public static final String DIRECTORY_PROPERTY_CHANGED = 
            JFileChooser.DIRECTORY_CHANGED_PROPERTY;
    
    protected static final SimpleDateFormat MODIFIED_DATE_FORMAT = 
            new SimpleDateFormat("M/d/yyyy h:mm a");
    /**
     * 
     */
    private static final MetadataComparator METADATA_COMPARATOR = new MetadataComparator();
    /**
     * 
     */
    private static final int FOLDER_INDENTATION = 8;
    /**
     * 
     */
    private static final KeyStroke ENTER_KEYSTROKE = 
            KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
    /**
     * Creates new form JDropboxFileChooser
     */
    public JDropboxFileChooser() {
        initComponents();
        
            // Set up navigation buttons
        UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
        setButtonIcon(newFolderButton,uiDefaults,"FileChooser.newFolderIcon",
                "New Folder");
        setButtonIcon(listViewToggle,uiDefaults,"FileChooser.listViewIcon",
                "List");
        setButtonIcon(detailsViewToggle,uiDefaults,"FileChooser.detailsViewIcon",
                "Details");
        setButtonIcon(homeFolderButton,uiDefaults,"FileChooser.homeFolderIcon",
                "Home");
        setButtonIcon(upFolderButton,uiDefaults,"FileChooser.upFolderIcon",
                "Up One Level");
        
            // Handler for listening to components and models
        Handler handler = new Handler();
            // Action and mouse listener for the selection
        SelectionAction selAction = new SelectionAction();
        
        fileNameField.getDocument().addDocumentListener(handler);
        fileNameField.setAction(selAction);
        
            // Create and set up the model for the list view
        fileListModel = new ArrayListModel<>();
        fileListModel.addListDataListener(handler);
        filePaths = new MetadataPathLowerList(fileListModel);
        
            // Set up the list view
        fileListList.setModel(fileListModel);
        fileListList.setCellRenderer(new MetadataNameListCellRenderer());
        fileListList.addListSelectionListener(handler);
        fileListList.addMouseListener(selAction);
        addAction(fileListList,selAction,ENTER_KEYSTROKE);
        addAction(listScrollPane,selAction,ENTER_KEYSTROKE);
        fileListEditAction = new MetadataEditListAction();
        
            // Create and set up the model for the details view
        fileDetailsModel = new MetadataDetailsTableModel(fileDetailsTable,fileListModel){
            @Override
            public void setValueAt(Object aValue, int row, int column){
                super.setValueAt(aValue, row, column);
                sortMetadata();
            }
        };
        fileDetailsModel.addTableModelListener(handler);
        
            // Set up the details table
        fileDetailsTable.setModel(fileDetailsModel);
        fileDetailsTable.setDefaultRenderer(Metadata.class, 
                new MetadataNameTableCellRenderer());
        fileDetailsTable.setDefaultRenderer(Date.class, 
                new DateTableCellRenderer(MODIFIED_DATE_FORMAT));
        fileDetailsTable.getColumnModel()
                .getColumn(fileDetailsModel.findColumn("Size"))
                .setCellRenderer(new FileSizeTableCellRenderer());
        MetadataNameCellEditor cellEditor = new MetadataNameCellEditor();
        cellEditor.setClickCountToStart(3);
        fileDetailsTable.setDefaultEditor(Metadata.class, cellEditor);
        TableRowSorter<MetadataDetailsTableModel> detailsRowSorter = new TableRowSorter<>(fileDetailsModel);
        detailsRowSorter.setComparator(0, METADATA_COMPARATOR);
        detailsRowSorter.setSortsOnUpdates(true);
        fileDetailsTable.setRowSorter(detailsRowSorter);
        fileDetailsTable.getSelectionModel().addListSelectionListener(handler);
        fileDetailsTable.addMouseListener(selAction);
        addAction(fileDetailsTable,selAction,ENTER_KEYSTROKE);
        addAction(detailsScrollPane,selAction,ENTER_KEYSTROKE);
        
        renameItem.setVisible(false);
        lookInComboModel = new ArrayComboBoxModel<>();
        lookInComboModel.add(new DbxRootMetadata());
        lookInComboBox.setModel(lookInComboModel);
        lookInComboBox.setRenderer(new LookInListCellRenderer());
    }
    /**
     * 
     * @param comp
     * @param action
     * @param keyStroke 
     */
    private void addAction(JComponent comp, Action action, KeyStroke keyStroke){
        comp.getInputMap(JComponent.WHEN_FOCUSED).put(keyStroke, 
                action.getValue(Action.NAME));
        comp.getActionMap().put(action.getValue(Action.NAME), action);
    }
    /**
     * 
     * @param button
     * @param uiDefaults
     * @param key
     * @param defaultText 
     */
    private void setButtonIcon(AbstractButton button, UIDefaults uiDefaults, 
            String key, String defaultText){
        Icon icon = uiDefaults.getIcon(key);
        if (icon == null)
            button.setText(defaultText);
        else
            button.setIcon(icon);
    }
    @Override
    protected String getDefaultAcceptButtonToolTipText() {
        return "Save selected file";
    }
    @Override
    protected String getDefaultCancelButtonToolTipText() {
        return "Abort Dropbox file chooser dialog";
    }
    @Override
    protected String getDefaultAcceptButtonText(){
        return "Save";
    }
    /**
     * 
     * @return 
     */
    public DbxClientV2 getDropboxClient(){
        return dbxClient;
    }
    /**
     * 
     * @param client 
     */
    public void setDropboxClient(DbxClientV2 client){
        dbxClient = client;
        updateAcceptEnabled();
        updateComponentsEnabled();
        fileDetailsModel.setDropboxClient(client);
    }
    /**
     * 
     * @param newPath 
     * @return  
     */
    protected boolean updateSelectedPath(String newPath){
        if (Objects.equals(selectedPath, newPath))
            return false;
        String old = selectedPath;
        selectedPath = newPath;
        firePropertyChange(SELECTED_PATH_PROPERTY_CHANGED,old,newPath);
        return true;
    }
    /**
     * 
     */
    protected void updateSelectedPath(){
        updateSelectedPath(currDirPath + "/" + fileNameField.getText());
    }
    /**
     * 
     * @return 
     */
    public String getSelectedPath(){
        return selectedPath;
    }
    /**
     * 
     * @param path 
     */
    public void setSelectedPath(String path){
        if (updateSelectedPath(path)){
            
        }
    }
    /**
     * 
     * @param panel
     * @param client
     * @return 
     */
    public int showOpenDialog(Component panel, DbxClientV2 client){
        return showDialog(panel,client,"Open");
    }
    /**
     * 
     * @param panel
     * @param client
     * @return 
     */
    public int showSaveDialog(Component panel, DbxClientV2 client){
        return showDialog(panel,client,"Save");
    }
    /**
     * 
     * @param panel
     * @param client
     * @param acceptText
     * @return 
     */
    public int showDialog(Component panel, DbxClientV2 client, String acceptText){
        setAcceptButtonText(acceptText);
        return showDialog(panel,client);
    }
    /**
     * 
     * @param panel
     * @param client
     * @return 
     */
    public int showDialog(Component panel, DbxClientV2 client){
        setDropboxClient(client);
        return showDialog(panel);
    }
    @Override
    public int showDialog(Component panel){
        if (getDialog() != null)     // If the dialog is already showing
            return ERROR_OPTION;
        if (getDropboxClient() == null)
            throw new IllegalStateException("No Dropbox client set");
        refreshCurrentDirectory();
        return super.showDialog(panel);
    }
    /**
     * 
     * @return 
     */
    public String getCurrentDirectory(){
        return currDirPath;
    }
    /**
     * 
     * @param path 
     */
    public void setCurrentDirectory(String path){
        if (!Objects.equals(path, currDirPath))
            changeCurrentDirectory(path);
    }
    /**
     * 
     * @param client
     * @param path
     * @return 
     * @throws com.dropbox.core.DbxException
     */
    protected boolean loadDirectory(DbxClientV2 client, String path) throws DbxException{
        LinkManager.getLogger().entering("JDropboxFileChooser", "loadDirectory",
                new Object[]{client,path});
        metadataLoadList.clear();
        try{
            metadataLoadList = DropboxUtilities.listFolder(client, path, metadataLoadList);
            metadataLoadList.sort(METADATA_COMPARATOR);
            fileListModel.clear();
            fileListModel.addAll(metadataLoadList);
            LinkManager.getLogger().exiting("JDropboxFileChooser", "loadDirectory", true);
            return true;
        } catch (ListFolderErrorException ex){
            if (ex.errorValue.isPath()){
                if (ex.errorValue.getPathValue().isNotFound()){
                    LinkManager.getLogger().log(Level.INFO, "Path not found", ex);
                    LinkManager.getLogger().exiting("JDropboxFileChooser", "loadDirectory", false);
                    return false;
                }
            }
            throw ex;
        }
    }
    /**
     * 
     * @param path 
     * @return  
     */
    protected boolean changeCurrentDirectory(String path){
        try{
            String oldDirPath = currDirPath;
            if (loadDirectory(getDropboxClient(),path)){
                currDirPath = (path!=null)?path:"";
                firePropertyChange(DIRECTORY_PROPERTY_CHANGED,oldDirPath,
                        currDirPath);
                String[] paths = currDirPath.split("/");
                String temp = "";
                lookInComboModel.clear();
                lookInComboModel.add(new DbxRootMetadata());
                for (int i = 1; i < paths.length; i++){
                    temp += "/"+paths[i];
                    try{
                        lookInComboModel.add(getDropboxClient().files().getMetadataBuilder(temp).start());
                    } catch (DbxException ex){
                        LinkManager.getLogger().log(Level.WARNING, "Failed to load Directory from Dropbox", ex);
                    }
                }
                lookInComboModel.setSelectedItem(lookInComboModel.get(lookInComboModel.size()-1));
                updateUpFolderButtonEnabled();
                return true;
            }
        } catch (DbxException ex){
            LinkManager.getLogger().log(Level.WARNING, "Failed to load files from Dropbox", ex);
            throw new UncheckedDbxException(ex);
        }
        return false;
    }
    /**
     * 
     */
    protected void refreshCurrentDirectory(){
        if (!changeCurrentDirectory(currDirPath))
            goUpInDirectoryTree();
    }
    /**
     * 
     */
    protected void goUpInDirectoryTree(){
        String path = currDirPath;
        boolean success = false;
        while (path != null && !path.isBlank() && !success){
            String prevPath = path.substring(0, path.lastIndexOf("/"));
            success = changeCurrentDirectory(prevPath);
            if (success){
                int index = filePaths.indexOf(path);
                if (index < 0)
                    fileDetailsTable.clearSelection();
                else
                    fileDetailsTable.setRowSelectionInterval(index, index);
            } else
                path = prevPath;
        }
    }
    @Override
    public void accept(){
        updateSelectedPath();
        super.accept();
    }
    @Override
    public void cancel(){
        updateSelectedPath();
        super.cancel();
    }
    /**
     * 
     * @param index 
     */
    protected void setSelectedDetailsIndex(int index){
        if (index < 0 || index >= fileListModel.size())
            fileDetailsTable.clearSelection();
        else
            fileDetailsTable.setRowSelectionInterval(index, index);
    }
    /**
     * 
     * @param index 
     */
    protected void setSelectedListIndex(int index){
        if (index < 0 || index >= fileListModel.size())
            fileListList.clearSelection();
        else 
            fileListList.setSelectedIndex(index);
    }
    /**
     * 
     * @param index 
     */
    protected void setSelectedIndex(int index){
        setSelectedListIndex(index);
        setSelectedDetailsIndex(index);
    }
    /**
     * 
     * @param metadata 
     */
    protected void setSelectedMetadata(Metadata metadata){
        setSelectedIndex(fileListModel.indexOf(metadata));
    }
    /**
     * 
     * @return 
     */
    protected int getSelectedDetailsIndex(){
        return fileDetailsTable.getSelectedRow();
    }
    /**
     * 
     * @return 
     */
    protected int getSelectedListIndex(){
        return fileListList.getSelectedIndex();
    }
    /**
     * 
     * @return 
     */
    protected int getSelectedIndex(){
        int index = -1;
        if (listViewToggle.isSelected())
            index = getSelectedListIndex();
        else if (detailsViewToggle.isSelected())
            index = getSelectedDetailsIndex();
        if (index < 0 || index >= fileListModel.size())
            return -1;
        return index;
    }
    /**
     * 
     * @return 
     */
    protected Metadata getSelectedMetadata(){
        int index = getSelectedIndex();
        if (index < 0 || index >= fileListModel.size())
            return null;
        return fileListModel.get(index);
    }
    /**
     * 
     * @param value 
     */
    protected void updateSelectedFileName(Object value){
        if (value instanceof FileMetadata)
            fileNameField.setText(((FileMetadata)value).getName());
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        viewButtonGroup = new javax.swing.ButtonGroup();
        filePopupMenu = new javax.swing.JPopupMenu();
        refreshItem = new javax.swing.JMenuItem();
        newFolderItem = new javax.swing.JMenuItem();
        renameItem = new javax.swing.JMenuItem();
        controlButtonPanel = new javax.swing.JPanel();
        javax.swing.JButton acceptButton = getAcceptButton();
        javax.swing.JButton cancelButton = getCancelButton();
        fileNameLabel = new javax.swing.JLabel();
        fileNameField = new javax.swing.JTextField();
        newFolderButton = new javax.swing.JButton();
        detailsViewToggle = new javax.swing.JToggleButton();
        listViewToggle = new javax.swing.JToggleButton();
        fileViewPanel = new javax.swing.JPanel();
        listPanel = new javax.swing.JPanel();
        listScrollPane = new javax.swing.JScrollPane();
        fileListList = new javax.swing.JList<>();
        detailsView = new javax.swing.JPanel();
        detailsScrollPane = new javax.swing.JScrollPane();
        fileDetailsTable = new javax.swing.JTable();
        homeFolderButton = new javax.swing.JButton();
        upFolderButton = new javax.swing.JButton();
        lookInLabel = new javax.swing.JLabel();
        lookInComboBox = new javax.swing.JComboBox<>();

        refreshItem.setText("Refresh");
        refreshItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshItemActionPerformed(evt);
            }
        });
        filePopupMenu.add(refreshItem);

        newFolderItem.setText("New Folder");
        newFolderItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFolderActionPerformed(evt);
            }
        });
        filePopupMenu.add(newFolderItem);

        renameItem.setText("Rename");
        renameItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                renameItemActionPerformed(evt);
            }
        });
        filePopupMenu.add(renameItem);

        setPreferredSize(new java.awt.Dimension(722, 458));

        controlButtonPanel.setLayout(new java.awt.GridLayout(1, 0, 6, 0));
        controlButtonPanel.add(acceptButton);
        controlButtonPanel.add(cancelButton);

        fileNameLabel.setLabelFor(fileNameField);
        fileNameLabel.setText("File Name:");

        fileNameField.setActionCommand(ACCEPT_SELECTED);

        newFolderButton.setToolTipText("Create New Folder");
        newFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFolderActionPerformed(evt);
            }
        });

        viewButtonGroup.add(detailsViewToggle);
        detailsViewToggle.setToolTipText("Details");
        detailsViewToggle.setActionCommand("detailsView");
        detailsViewToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileViewToggleActionPerformed(evt);
            }
        });

        viewButtonGroup.add(listViewToggle);
        listViewToggle.setSelected(true);
        listViewToggle.setToolTipText("List");
        listViewToggle.setActionCommand("listView");
        listViewToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileViewToggleActionPerformed(evt);
            }
        });

        fileViewPanel.setComponentPopupMenu(filePopupMenu);
        fileViewPanel.setLayout(new java.awt.CardLayout());

        listPanel.setInheritsPopupMenu(true);
        listPanel.setLayout(new java.awt.BorderLayout());

        listScrollPane.setInheritsPopupMenu(true);

        fileListList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        fileListList.setInheritsPopupMenu(true);
        fileListList.setLayoutOrientation(javax.swing.JList.VERTICAL_WRAP);
        fileListList.setVisibleRowCount(-1);
        fileListList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                fileListListValueChanged(evt);
            }
        });
        listScrollPane.setViewportView(fileListList);

        listPanel.add(listScrollPane, java.awt.BorderLayout.CENTER);

        fileViewPanel.add(listPanel, "listView");

        detailsView.setInheritsPopupMenu(true);
        detailsView.setLayout(new java.awt.BorderLayout());

        detailsScrollPane.setInheritsPopupMenu(true);

        fileDetailsTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        fileDetailsTable.setInheritsPopupMenu(true);
        fileDetailsTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        detailsScrollPane.setViewportView(fileDetailsTable);

        detailsView.add(detailsScrollPane, java.awt.BorderLayout.CENTER);

        fileViewPanel.add(detailsView, "detailsView");

        homeFolderButton.setToolTipText("Home");
        homeFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                homeFolderButtonActionPerformed(evt);
            }
        });

        upFolderButton.setToolTipText("Up One Level");
        upFolderButton.setEnabled(false);
        upFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                upFolderButtonActionPerformed(evt);
            }
        });

        lookInLabel.setLabelFor(lookInComboBox);
        lookInLabel.setText("Look In:");

        lookInComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                lookInComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(fileViewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 558, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fileNameField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(controlButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(lookInLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lookInComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(upFolderButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(homeFolderButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newFolderButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listViewToggle)
                        .addGap(0, 0, 0)
                        .addComponent(detailsViewToggle)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(newFolderButton)
                    .addComponent(detailsViewToggle)
                    .addComponent(listViewToggle)
                    .addComponent(homeFolderButton)
                    .addComponent(upFolderButton)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(lookInLabel)
                        .addComponent(lookInComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(fileViewPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(fileNameLabel)
                    .addComponent(fileNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(controlButtonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newFolderActionPerformed
        try{
            CreateFolderResult result = getDropboxClient().files().createFolderV2(currDirPath+"/New Folder", true);
            Metadata metadata = result.getMetadata();
            fileListModel.add(metadata);
            fileListModel.sort(METADATA_COMPARATOR);
            if (listViewToggle.isSelected()){
                int index = fileListModel.indexOf(metadata);
                fileListList.setSelectedIndex(index);
                // TODO: Start editing the selected index
            } else if (detailsViewToggle.isSelected()){
                int index = fileDetailsModel.getMetadataList().indexOf(metadata);
                fileDetailsTable.setRowSelectionInterval(index, index);
                fileDetailsTable.editCellAt(index, 0);
            }
        } catch (DbxException ex){
            LinkManager.getLogger().log(Level.WARNING, "Failed to create folder in Dropbox", ex);
        }
    }//GEN-LAST:event_newFolderActionPerformed

    private void refreshItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshItemActionPerformed
        refreshCurrentDirectory();
    }//GEN-LAST:event_refreshItemActionPerformed

    private void fileViewToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileViewToggleActionPerformed
        if (firstTimeShowingDetails && detailsViewToggle.isSelected()){
            firstTimeShowingDetails = false;
            TableColumnModel columns = fileDetailsTable.getColumnModel();
            columns.getColumn(2).setPreferredWidth(90);
            columns.getColumn(3).setPreferredWidth(108);
            columns.getColumn(4).setPreferredWidth(108);
            int firstWidth = detailsScrollPane.getViewport().getWidth();
            for (int i = 1; i < columns.getColumnCount(); i++){
                firstWidth -= columns.getColumn(i).getPreferredWidth();
            }
            columns.getColumn(0).setPreferredWidth(firstWidth);
        }
        LinkManagerUtilities.setCard(fileViewPanel, evt.getActionCommand());
    }//GEN-LAST:event_fileViewToggleActionPerformed

    private void fileListListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_fileListListValueChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_fileListListValueChanged

    private void homeFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeFolderButtonActionPerformed
        changeCurrentDirectory(null);
    }//GEN-LAST:event_homeFolderButtonActionPerformed
    
    private void upFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upFolderButtonActionPerformed
        if (currDirPath == null || currDirPath.isBlank()){
            upFolderButton.setEnabled(false);
        } else {
            goUpInDirectoryTree();
        }
    }//GEN-LAST:event_upFolderButtonActionPerformed

    private void lookInComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lookInComboBoxActionPerformed
        int selIndex = lookInComboBox.getSelectedIndex();
        if (selIndex >= 0){
            Metadata metadata = lookInComboModel.get(selIndex);
            String path = "";
            if (metadata != null && !(metadata instanceof DbxRootMetadata))
                path = metadata.getPathLower();
            if (!currDirPath.equals(path))
                changeCurrentDirectory(path);
        }
    }//GEN-LAST:event_lookInComboBoxActionPerformed

    private void renameItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameItemActionPerformed
        if (detailsViewToggle.isSelected()){
            int row = fileDetailsTable.getSelectedRow();
            if (row >= 0)
                fileDetailsTable.editCellAt(row, 0);
        } else if (listViewToggle.isSelected()){
            fileListEditAction.actionPerformed(new ActionEvent(fileListList,
                    ActionEvent.ACTION_PERFORMED,""));
        }
    }//GEN-LAST:event_renameItemActionPerformed
    /**
     * 
     * @param parent
     * @param renamedMetadata
     * @return 
     */
    protected Metadata renameFile(Component parent, RenamedMetadata renamedMetadata){
        return renamedMetadata.renameWithError(getDropboxClient(), parent);
    }
    @Override
    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        updateComponentsEnabled();
    }
    /**
     * 
     */
    protected void updateComponentsEnabled(){
        updateUpFolderButtonEnabled();
        try{
            boolean enabled = isEnabled() && getDropboxClient() != null;
            newFolderButton.setEnabled(enabled);
            newFolderItem.setEnabled(newFolderButton.isEnabled());
            refreshItem.setEnabled(enabled);
        } catch (NullPointerException ex){
            LinkManager.getLogger().log(Level.WARNING, 
                    "Null encountered while setting enable value", ex);
        }
    }
    /**
     * 
     */
    protected void updateUpFolderButtonEnabled(){
        if (upFolderButton != null)
            upFolderButton.setEnabled(isEnabled() && getDropboxClient() != null && 
                    currDirPath != null && !currDirPath.isBlank());
    }
    @Override
    protected boolean isAcceptEnabled(){
        return super.isAcceptEnabled() && getDropboxClient() != null;
    }
    @Override
    protected void updateAcceptEnabled(){
        super.updateAcceptEnabled();
        if (fileNameField != null)
            fileNameField.setEnabled(isAcceptEnabled());
    }
    /**
     * 
     */
    protected void sortMetadata(){
        Metadata selected = getSelectedMetadata();
        fileListModel.sort(METADATA_COMPARATOR);
        if (selected != null)
            setSelectedMetadata(selected);
    }
    /**
     * 
     */
    private String selectedPath = null;
    /**
     * 
     */
    private String currDirPath = "";
    /**
     * 
     */
    private List<Metadata> metadataLoadList = new ArrayList<>();
    /**
     * This is the Dropbox client being used currently.
     */
    private DbxClientV2 dbxClient = null;
    /**
     * 
     */
    private MetadataDetailsTableModel fileDetailsModel;
    /**
     * 
     */
    private ArrayListModel<Metadata> fileListModel;
    /**
     * 
     */
    private MetadataEditListAction fileListEditAction;
    /**
     * 
     */
    private List<String> filePaths;
    /**
     * 
     */
    private boolean firstTimeShowingDetails = true;
    /**
     * 
     */
    private ArrayComboBoxModel<Metadata> lookInComboModel;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlButtonPanel;
    private javax.swing.JScrollPane detailsScrollPane;
    private javax.swing.JPanel detailsView;
    private javax.swing.JToggleButton detailsViewToggle;
    private javax.swing.JTable fileDetailsTable;
    private javax.swing.JList<Metadata> fileListList;
    private javax.swing.JTextField fileNameField;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JPopupMenu filePopupMenu;
    private javax.swing.JPanel fileViewPanel;
    private javax.swing.JButton homeFolderButton;
    private javax.swing.JPanel listPanel;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JToggleButton listViewToggle;
    private javax.swing.JComboBox<Metadata> lookInComboBox;
    private javax.swing.JLabel lookInLabel;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JMenuItem newFolderItem;
    private javax.swing.JMenuItem refreshItem;
    private javax.swing.JMenuItem renameItem;
    private javax.swing.JButton upFolderButton;
    private javax.swing.ButtonGroup viewButtonGroup;
    // End of variables declaration//GEN-END:variables

    private class Handler implements DocumentListener, TableModelListener, 
            ListSelectionListener, ListDataListener{
        @Override
        public void insertUpdate(DocumentEvent evt) {
            
        }
        @Override
        public void removeUpdate(DocumentEvent evt) {
            
        }
        @Override
        public void changedUpdate(DocumentEvent evt) {
            
        }
        @Override
        public void tableChanged(TableModelEvent evt) {
            
        }
        @Override
        public void valueChanged(ListSelectionEvent evt) {
            Object targetSource = null;
            if (detailsViewToggle.isSelected())
                targetSource = fileDetailsTable.getSelectionModel();
            else if (listViewToggle.isSelected())
                targetSource = fileListList;
            if (evt.getSource() != targetSource)
                return;
            int index = getSelectedIndex();
            setSelectedIndex(index);
            Metadata selected = null;
            if (index >= 0)
                selected = fileListModel.get(index);
            updateSelectedFileName(selected);
            if (selected != null)
                renameItem.setVisible(true);
            else
                renameItem.setVisible(false);
        }
        @Override
        public void intervalAdded(ListDataEvent evt) {
            
        }
        @Override
        public void intervalRemoved(ListDataEvent evt) {
            
        }
        @Override
        public void contentsChanged(ListDataEvent evt) {
            
        }
    }
    /**
     * 
     * @param <E> 
     */
    private abstract class MetadataPropertyList<E> extends AbstractList<E>{
        /**
         * 
         */
        private final List<Metadata> metadata;
        /**
         * 
         * @param metadata 
         */
        MetadataPropertyList(List<Metadata> metadata){
            this.metadata = Objects.requireNonNull(metadata);
        }
        /**
         * 
         * @return 
         */
        public List<Metadata> getMetadata(){
            return metadata;
        }
        /**
         * 
         * @param metadata
         * @return 
         */
        protected abstract E getValue(Metadata metadata);
        @Override
        public E get(int index) {
            Metadata value = metadata.get(index);
            return (value!=null)?getValue(value):null;
        }
        @Override
        public int size() {
            return metadata.size();
        }
    }
    /**
     * 
     */
    private class MetadataPathLowerList extends MetadataPropertyList<String>{
        /**
         * 
         * @param metadata 
         */
        public MetadataPathLowerList(List<Metadata> metadata) {
            super(metadata);
        }
        @Override
        protected String getValue(Metadata metadata) {
            return metadata.getPathLower();
        }
    }
    /**
     * 
     */
    private class LookInListCellRenderer extends MetadataNameListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList list,
                Object value,int index,boolean isSelected,boolean cellHasFocus){
            Component comp = super.getListCellRendererComponent(list, value, 
                    index, isSelected, cellHasFocus);
            if (value instanceof Metadata && !(value instanceof DbxRootMetadata)) {
                Border border = getBorder();
                String path = ((Metadata)value).getPathLower();
                if (path != null){
                    String[] paths = path.split("/");
                    setBorder(BorderFactory.createCompoundBorder(border, 
                            BorderFactory.createEmptyBorder(0, 
                                    FOLDER_INDENTATION*(paths.length-1), 0, 0)));
                }
            }
            return comp;
        }
        @Override
        protected Object getValue(Metadata value){
            if (value == null || value instanceof DbxRootMetadata)
                return "Dropbox";
            return value.getName();
        }
    }
    /**
     * 
     */
    private class MetadataEditListAction extends AbstractEditListAction<Metadata>{
        @Override
        protected String valueToString(Metadata value) {
            return (value!=null)?value.getName():null;
        }
        @Override
        protected Metadata valueFromString(String value, Metadata oldValue) {
            RenamedMetadata renamed = new RenamedMetadata(oldValue,value);
            return renamed.renameWithError(getDropboxClient(), fileListList);
        }
        @Override
        protected void setValue(ActionEvent evt){
            super.setValue(evt);
            sortMetadata();
        }
    }
    /**
     * 
     */
    private class SelectionAction extends AbstractAction implements MouseListener{
        /**
         * 
         */
        SelectionAction(){
            super("SelectRow");
            super.putValue(Action.ACTION_COMMAND_KEY, ACCEPT_SELECTED);
        }
        /**
         * 
         * @param index 
         * @param evt
         */
        public void actionPerformed(int index, ActionEvent evt){
            if (index >= 0){
                Metadata metadata = fileDetailsModel.getMetadataList().get(index);
                if (metadata instanceof DbxRootMetadata)
                    changeCurrentDirectory(null);
                else if (metadata instanceof FolderMetadata)
                    changeCurrentDirectory(metadata.getPathLower());
                else if (isAcceptEnabled())
                    JDropboxFileChooser.this.accept(evt);
            }
        }
        /**
         * 
         * @param index
         * @param evt 
         */
        protected void mouseClicked(int index, MouseEvent evt){
            actionPerformed(index,new ActionEvent(evt.getSource(),
                    ActionEvent.ACTION_PERFORMED,
                    (String)getValue(Action.ACTION_COMMAND_KEY),
                    evt.getModifiersEx()));
        }
        @Override
        public void actionPerformed(ActionEvent evt) {
            if (evt.getSource() == fileDetailsTable || 
                    evt.getSource() == detailsScrollPane)
                actionPerformed(getSelectedDetailsIndex(),evt);
            else if (evt.getSource() == fileListList || 
                    evt.getSource() == listScrollPane)
                actionPerformed(getSelectedListIndex(),evt);
            else {
                if (listViewToggle.isSelected())
                    actionPerformed(getSelectedListIndex(),evt);
                else if (detailsViewToggle.isSelected())
                    actionPerformed(getSelectedDetailsIndex(),evt);
            }
        }
        @Override
        public void mouseClicked(MouseEvent evt) {
            if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2){
                Point point = evt.getPoint();
                if (evt.getSource() == fileDetailsTable)
                    mouseClicked(fileDetailsTable.rowAtPoint(point),evt);
                else if (evt.getSource() == fileListList)
                    mouseClicked(fileListList.locationToIndex(point),evt);
            }
        }
        @Override
        public void mousePressed(MouseEvent e) { }
        @Override
        public void mouseReleased(MouseEvent e) { }
        @Override
        public void mouseEntered(MouseEvent e) { }
        @Override
        public void mouseExited(MouseEvent e) { }
    }
}
