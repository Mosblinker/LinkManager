/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JPanel.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.*;
import com.dropbox.core.v2.*;
import com.dropbox.core.v2.files.*;
import components.AbstractConfirmDialogPanel;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableRowSorter;
import javax.swing.tree.*;
import manager.LinkManager;
import manager.LinkManagerUtilities;
import manager.renderer.*;

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
    
    protected static final SimpleDateFormat MODIFIED_DATE_FORMAT = 
            new SimpleDateFormat("M/d/yyyy h:mm a");
    /**
     * 
     */
    private static final MetadataComparator METADATA_COMPARATOR = new MetadataComparator();
    /**
     * Creates new form JDropboxFileChooser
     */
    public JDropboxFileChooser() {
        initComponents();
        MetadataNameTreeCellRenderer treeCellRenderer = new MetadataNameTreeCellRenderer();
        dropboxFileTree.setCellRenderer(treeCellRenderer);
        MetadataNameCellEditor treeCellEditor = new MetadataNameCellEditor();
        dropboxFileTree.setCellEditor(treeCellEditor);
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
        Handler handler = new Handler();
        fileTreeModel = new DefaultTreeModel(null,true);
        fileTreeModel.addTreeModelListener(handler);
        dropboxFileTree.setModel(fileTreeModel);
        ToolTipManager.sharedInstance().registerComponent(dropboxFileTree);
        fileNameField.getDocument().addDocumentListener(handler);
        fileDetailsModel = new MetadataDetailsTableModel(detailsFileTable);
        fileDetailsPaths = new MetadataPathLowerList(fileDetailsModel.getMetadataList());
        fileDetailsModel.addTableModelListener(handler);
        detailsFileTable.setModel(fileDetailsModel);
        detailsFileTable.setDefaultRenderer(Metadata.class, 
                new MetadataNameTableCellRenderer());
        detailsFileTable.setDefaultRenderer(Date.class, 
                new DateTableCellRenderer(MODIFIED_DATE_FORMAT));
        detailsFileTable.getColumnModel()
                .getColumn(fileDetailsModel.findColumn("Size"))
                .setCellRenderer(new FileSizeTableCellRenderer());
        MetadataNameCellEditor cellEditor = new MetadataNameCellEditor();
        cellEditor.setClickCountToStart(3);
        detailsFileTable.setDefaultEditor(Metadata.class, cellEditor);
        TableRowSorter<MetadataDetailsTableModel> detailsRowSorter = new TableRowSorter<>(fileDetailsModel);
        detailsRowSorter.setComparator(0, METADATA_COMPARATOR);
        detailsRowSorter.setSortsOnUpdates(true);
        detailsFileTable.setRowSorter(detailsRowSorter);
        detailsFileTable.getSelectionModel().addListSelectionListener(handler);
        renameItem.setVisible(false);
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
     * @return 
     */
    protected String getSelectedPathFromTree(){
        String name = fileNameField.getText();
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null && (name == null || name.isBlank()))
            return null;
        if (node == null || !(node.getUserObject() instanceof Metadata))
            return "/"+name;
        while (node != null && 
                !(node.getUserObject() instanceof DbxRootMetadata) && 
                !(node.getUserObject() instanceof FolderMetadata)){
            TreeNode parent = node.getParent();
            while (!(parent instanceof DefaultMutableTreeNode) && parent != null)
                parent = parent.getParent();
            node = (DefaultMutableTreeNode)parent;
        }
        if (node == null || node.getUserObject() == null)
            return "/"+name;
        Metadata metadata = (Metadata)node.getUserObject();
        if (!(node.getUserObject() instanceof DbxRootMetadata))
            name = "/"+name;
        return metadata.getPathLower()+name;
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
     * @param path 
     */
    protected void updateSelectedTreePath(String path){
        if (path == null)
            dropboxFileTree.clearSelection();
        else{
            if (!(fileTreeModel.getRoot() instanceof DefaultMutableTreeNode)){
                dropboxFileTree.clearSelection();
                return;
            }
            DefaultMutableTreeNode node = (DefaultMutableTreeNode)fileTreeModel.getRoot();
            String[] paths = path.split("/");
            String currPath = "";
            for (int i = 1; i < paths.length; i++){
                String nextPath = currPath+"/"+paths[i].toLowerCase();
                Iterator<TreeNode> itr = node.children().asIterator();
                DefaultMutableTreeNode nextNode = null;
                while(itr.hasNext() && nextNode == null){
                    TreeNode temp = itr.next();
                    if (temp instanceof DefaultMutableTreeNode){
                        DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)temp;
                        if (currNode.getUserObject() instanceof Metadata){
                            Metadata metadata = (Metadata)currNode.getUserObject();
                            if (nextPath.equals(metadata.getPathLower()))
                                nextNode = currNode;
                        }
                    }
                }
                if (nextNode != null){
                    node = nextNode;
                    currPath = nextPath;
                } else {
                    break;
                }
            }
            TreePath selPath = new TreePath(node.getPath());
            dropboxFileTree.setSelectionPath(selPath);
            dropboxFileTree.expandPath(selPath);
            if (currPath.length()+1 < path.length()){
                fileNameField.setText(path.substring(currPath.length()+1));
            } else if (node.getUserObject() instanceof FileMetadata){
                fileNameField.setText(((Metadata)node.getUserObject()).getName());
            } else
                fileNameField.setText("");
        }
    }
    /**
     * 
     */
    protected void updateSelectedPath(){
        updateSelectedPath(getSelectedPathFromTree());
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
            updateSelectedTreePath(path);
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
        try{
            String selectedPath = getSelectedPath();
            loadFiles(getDropboxClient());
            updateSelectedTreePath(selectedPath);
        } catch (DbxException ex){
            LinkManager.getLogger().log(Level.WARNING, "Failed to load files from Dropbox", ex);
            throw new UncheckedDbxException(ex);
        }
        return super.showDialog(panel);
    }
    /**
     * 
     * @param client 
     * @throws com.dropbox.core.DbxException 
     */
    protected void loadFiles(DbxClientV2 client) throws DbxException{
        LinkManager.getLogger().entering("JDropboxFileChooser", "loadFiles",
                client);
        DefaultMutableTreeNode node = DropboxUtilities.listFolderTree(client);
        fileTreeModel.setRoot(node);
        loadDirectory(client,currDirPath);
        LinkManager.getLogger().exiting("JDropboxFileChooser", "loadFiles");
    }
    /**
     * 
     * @param client
     * @param path
     * @throws com.dropbox.core.DbxException
     */
    protected void loadDirectory(DbxClientV2 client, String path) throws DbxException{
        LinkManager.getLogger().entering("JDropboxFileChooser", "loadDirectory",
                new Object[]{client,path});
        metadataLoadList.clear();
        metadataLoadList = DropboxUtilities.listFolder(client, path, metadataLoadList);
        metadataLoadList.sort(METADATA_COMPARATOR);
        fileDetailsModel.getMetadataList().clear();
        fileDetailsModel.getMetadataList().addAll(metadataLoadList);
        LinkManager.getLogger().exiting("JDropboxFileChooser", "loadDirectory");
    }
    /**
     * 
     * @param path 
     */
    protected void setCurrentDirectory(String path){
        try{
            loadDirectory(getDropboxClient(),path);
            currDirPath = (path!=null)?path:"";
            updateUpFolderButtonEnabled();
        } catch (DbxException ex){
            LinkManager.getLogger().log(Level.WARNING, "Failed to load files from Dropbox", ex);
            throw new UncheckedDbxException(ex);
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
    
    protected DefaultMutableTreeNode getSelectedNode(){
        TreePath selection = dropboxFileTree.getSelectionPath();
        if (selection == null)
            return null;
        if (selection.getLastPathComponent() instanceof DefaultMutableTreeNode){
            return (DefaultMutableTreeNode)selection.getLastPathComponent();
        }
        return null;
    }
    /**
     * 
     * @return 
     */
    protected Metadata getSelectedDetails(){
        int index = detailsFileTable.getSelectedRow();
        if (index < 0)
            return null;
        return fileDetailsModel.getMetadataList().get(index);
    }
    /**
     * 
     * @param metadata 
     */
    protected void setSelectedDetails(Metadata metadata){
        if (metadata == null)
            detailsFileTable.clearSelection();
        else {
            int index = fileDetailsModel.getMetadataList().indexOf(metadata);
            detailsFileTable.setRowSelectionInterval(index, index);
        }
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
        treePanel = new javax.swing.JPanel();
        treeScrollPanel = new javax.swing.JScrollPane();
        dropboxFileTree = new javax.swing.JTree();
        listPanel = new javax.swing.JPanel();
        listScrollPane = new javax.swing.JScrollPane();
        dropboxFileList = new javax.swing.JList<>();
        detailsView = new javax.swing.JPanel();
        detailsScrollPane = new javax.swing.JScrollPane();
        detailsFileTable = new javax.swing.JTable();
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
        fileNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNameFieldActionPerformed(evt);
            }
        });

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
        listViewToggle.setToolTipText("Tree");
        listViewToggle.setActionCommand("treeView");
        listViewToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileViewToggleActionPerformed(evt);
            }
        });

        fileViewPanel.setComponentPopupMenu(filePopupMenu);
        fileViewPanel.setLayout(new java.awt.CardLayout());

        treePanel.setInheritsPopupMenu(true);
        treePanel.setLayout(new java.awt.BorderLayout());

        treeScrollPanel.setInheritsPopupMenu(true);

        dropboxFileTree.setEditable(true);
        dropboxFileTree.setInheritsPopupMenu(true);
        dropboxFileTree.setShowsRootHandles(true);
        dropboxFileTree.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                dropboxFileTreeValueChanged(evt);
            }
        });
        treeScrollPanel.setViewportView(dropboxFileTree);

        treePanel.add(treeScrollPanel, java.awt.BorderLayout.CENTER);

        fileViewPanel.add(treePanel, "treeView");

        listPanel.setInheritsPopupMenu(true);
        listPanel.setLayout(new java.awt.BorderLayout());

        listScrollPane.setInheritsPopupMenu(true);

        dropboxFileList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        dropboxFileList.setInheritsPopupMenu(true);
        dropboxFileList.setLayoutOrientation(javax.swing.JList.VERTICAL_WRAP);
        dropboxFileList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                dropboxFileListValueChanged(evt);
            }
        });
        listScrollPane.setViewportView(dropboxFileList);

        listPanel.add(listScrollPane, java.awt.BorderLayout.CENTER);

        fileViewPanel.add(listPanel, "listView");

        detailsView.setInheritsPopupMenu(true);
        detailsView.setLayout(new java.awt.BorderLayout());

        detailsScrollPane.setInheritsPopupMenu(true);

        detailsFileTable.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        detailsFileTable.setInheritsPopupMenu(true);
        detailsFileTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        detailsFileTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                detailsFileTableMouseClicked(evt);
            }
        });
        detailsScrollPane.setViewportView(detailsFileTable);

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
            DefaultMutableTreeNode root = getSelectedNode();
            String path = "";
            while (root != null && !root.getAllowsChildren()){
                TreeNode parent = root.getParent();
                if (parent instanceof DefaultMutableTreeNode)
                    root = (DefaultMutableTreeNode) parent;
            }
            if (root == null)
                root = (DefaultMutableTreeNode)fileTreeModel.getRoot();
            else if (root.getUserObject() instanceof FolderMetadata){
                path = ((Metadata)root.getUserObject()).getPathLower();
            }
            CreateFolderResult result = getDropboxClient().files().createFolderV2(path+"/New Folder", true);
            Metadata metadata = result.getMetadata();
            DefaultMutableTreeNode node = new DefaultMutableTreeNode(metadata,true);
            boolean added = false;
            for (int i = 0; i < root.getChildCount() && !added; i++){
                TreeNode temp = root.getChildAt(i);
                if (temp instanceof DefaultMutableTreeNode){
                    DefaultMutableTreeNode tempNode = (DefaultMutableTreeNode)temp;
                    if (compareNodes(tempNode, node) >= 0){
                        root.insert(node, i);
                        fileTreeModel.nodesWereInserted(root, new int[]{i});
                        added = true;
                    }
                }
            }
            if (!added){
                root.add(node);
                fileTreeModel.nodesWereInserted(root, new int[]{root.getChildCount()-1});
            }
            fileDetailsModel.getMetadataList().add(metadata);
            fileDetailsModel.getMetadataList().sort(METADATA_COMPARATOR);
            if (listViewToggle.isSelected()){
                TreePath nodePath = new TreePath(node.getPath());
                dropboxFileTree.setSelectionPath(nodePath);
                dropboxFileTree.startEditingAtPath(nodePath);
            } else if (detailsViewToggle.isSelected()){
                int index = fileDetailsModel.getMetadataList().indexOf(metadata);
                detailsFileTable.setRowSelectionInterval(index, index);
                detailsFileTable.editCellAt(index, 0);
            }
        } catch (DbxException ex){
            LinkManager.getLogger().log(Level.WARNING, "Failed to create folder in Dropbox", ex);
        }
    }//GEN-LAST:event_newFolderActionPerformed

    private void refreshItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshItemActionPerformed
        try{
            
            DefaultMutableTreeNode selected = getSelectedNode();
            loadFiles(getDropboxClient());
            if (selected != null){
                DefaultMutableTreeNode node = (DefaultMutableTreeNode)fileTreeModel.getRoot();
                Object[] objPath = selected.getUserObjectPath();
                for (int i = 1; i < objPath.length && node != null; i++){
                    Iterator<TreeNode> nodes = node.children().asIterator();
                    String selID = null;
                    String selPath = null;
                    if (objPath[i] instanceof FileMetadata)
                        selID = ((FileMetadata)objPath[i]).getId();
                    else if (objPath[i] instanceof FolderMetadata)
                        selID = ((FolderMetadata)objPath[i]).getId();
                    if (objPath[i] instanceof Metadata)
                        selPath = ((Metadata)objPath[i]).getPathLower();
                    DefaultMutableTreeNode nextNode = null;
                    while(nodes.hasNext() && nextNode == null){
                        TreeNode temp = nodes.next();
                        if (temp instanceof DefaultMutableTreeNode){
                            DefaultMutableTreeNode currNode = (DefaultMutableTreeNode)temp;
                            Object o = currNode.getUserObject();
                            if (o instanceof Metadata){
                                String oID = null;
                                if (o instanceof FileMetadata)
                                    oID = ((FileMetadata)o).getId();
                                else if (o instanceof FolderMetadata)
                                    oID = ((FolderMetadata)o).getId();
                                if (Objects.equals(oID, selID) || 
                                        Objects.equals(selPath, ((Metadata) o).getPathLower()))
                                    nextNode = currNode;
                            } else if (Objects.equals(o, objPath[i]))
                                nextNode = currNode;
                        }
                    }
                    node = nextNode;
                }
                if (node != null){
                    dropboxFileTree.setSelectionPath(new TreePath(node.getPath()));
                }
            }
        } catch (DbxException ex){
            LinkManager.getLogger().log(Level.WARNING, "Failed to load files from Dropbox", ex);
        }
    }//GEN-LAST:event_refreshItemActionPerformed

    private void dropboxFileTreeValueChanged(javax.swing.event.TreeSelectionEvent evt) {//GEN-FIRST:event_dropboxFileTreeValueChanged
        DefaultMutableTreeNode selected = getSelectedNode();
        if (selected != null)
            updateSelectedFileName(selected.getUserObject());
    }//GEN-LAST:event_dropboxFileTreeValueChanged

    private void fileNameFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNameFieldActionPerformed
        if (isAcceptEnabled())
            accept(evt);
    }//GEN-LAST:event_fileNameFieldActionPerformed

    private void fileViewToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileViewToggleActionPerformed
        if (firstTimeShowingDetails && detailsViewToggle.isSelected()){
            firstTimeShowingDetails = false;
            TableColumnModel columns = detailsFileTable.getColumnModel();
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

    private void dropboxFileListValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_dropboxFileListValueChanged
        // TODO add your handling code here:
    }//GEN-LAST:event_dropboxFileListValueChanged

    private void homeFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_homeFolderButtonActionPerformed
        setCurrentDirectory(null);
    }//GEN-LAST:event_homeFolderButtonActionPerformed

    private void upFolderButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upFolderButtonActionPerformed
        if (currDirPath == null || currDirPath.isBlank()){
            upFolderButton.setEnabled(false);
        } else {
            setCurrentDirectory(currDirPath.substring(0, currDirPath.lastIndexOf("/")));
        }
    }//GEN-LAST:event_upFolderButtonActionPerformed

    private void lookInComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_lookInComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_lookInComboBoxActionPerformed

    private void detailsFileTableMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_detailsFileTableMouseClicked
        System.out.println(evt);
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2){
            Point point = evt.getPoint();
            int row = detailsFileTable.rowAtPoint(point);
            System.out.println("Double click on Cell (" + row + 
                    ", " + detailsFileTable.columnAtPoint(point)+")");
            if (row >= 0){
                Metadata metadata = fileDetailsModel.getMetadataList().get(row);
                if (metadata instanceof DbxRootMetadata)
                    setCurrentDirectory(null);
                if (metadata instanceof FolderMetadata)
                    setCurrentDirectory(metadata.getPathLower());
                else if (isAcceptEnabled())
                    accept();
            }
        }
    }//GEN-LAST:event_detailsFileTableMouseClicked

    private void renameItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_renameItemActionPerformed
        if (detailsViewToggle.isSelected()){
            
        }
    }//GEN-LAST:event_renameItemActionPerformed
    /**
     * 
     * @param parent
     * @param renamedMetadata
     * @return 
     */
    protected Metadata renameFile(Component parent, RenamedMetadata renamedMetadata){
        try{
            Metadata temp = renamedMetadata.rename(getDropboxClient());
            if (temp != null)
                return temp;
        } catch (RelocationErrorException ex){
        } catch (DbxException ex){
            LinkManager.getLogger().log(Level.WARNING, "Failed to rename file in Dropbox", ex);
        }
        renamedMetadata.giveRenameErrorFeedback(parent);
        return renamedMetadata.getMetadata();
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
            dropboxFileTree.setEnabled(enabled);
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
     * @param node1
     * @param node2
     * @return 
     */
    protected int compareNodes(DefaultMutableTreeNode node1, DefaultMutableTreeNode node2){
        return DropboxUtilities.METADATA_TREE_NODE_COMPARATOR.compare(node1,node2);
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
    private DefaultTreeModel fileTreeModel;
    /**
     * 
     */
    private MetadataDetailsTableModel fileDetailsModel;
    /**
     * 
     */
    private List<String> fileDetailsPaths;
    /**
     * 
     */
    private boolean firstTimeShowingDetails = true;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlButtonPanel;
    private javax.swing.JTable detailsFileTable;
    private javax.swing.JScrollPane detailsScrollPane;
    private javax.swing.JPanel detailsView;
    private javax.swing.JToggleButton detailsViewToggle;
    private javax.swing.JList<String> dropboxFileList;
    private javax.swing.JTree dropboxFileTree;
    private javax.swing.JTextField fileNameField;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JPopupMenu filePopupMenu;
    private javax.swing.JPanel fileViewPanel;
    private javax.swing.JButton homeFolderButton;
    private javax.swing.JPanel listPanel;
    private javax.swing.JScrollPane listScrollPane;
    private javax.swing.JToggleButton listViewToggle;
    private javax.swing.JComboBox<String> lookInComboBox;
    private javax.swing.JLabel lookInLabel;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JMenuItem newFolderItem;
    private javax.swing.JMenuItem refreshItem;
    private javax.swing.JMenuItem renameItem;
    private javax.swing.JPanel treePanel;
    private javax.swing.JScrollPane treeScrollPanel;
    private javax.swing.JButton upFolderButton;
    private javax.swing.ButtonGroup viewButtonGroup;
    // End of variables declaration//GEN-END:variables

    private class Handler implements TreeModelListener, DocumentListener, 
            TableModelListener, ListSelectionListener{
        @Override
        public void treeNodesChanged(TreeModelEvent evt) {
            boolean renamedNode = false;
            for (Object child : evt.getChildren()){
                if (child instanceof DefaultMutableTreeNode){
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode)child;
                    if (node.getUserObject() instanceof RenamedMetadata){
                        node.setUserObject(renameFile(dropboxFileTree,
                                (RenamedMetadata)node.getUserObject()));
                        fileTreeModel.nodeChanged(node);
                        renamedNode = true;
                    }
                }
            }
            if (renamedNode)
                return;
            TreePath parentPath = evt.getTreePath();
            if (parentPath == null || !(parentPath.getLastPathComponent() instanceof DefaultMutableTreeNode))
                return;
            DefaultMutableTreeNode parent = (DefaultMutableTreeNode)parentPath.getLastPathComponent();
            boolean swapped = false;
            for (int i = 0; i < parent.getChildCount()-1; i++){
                if (!(parent.getChildAt(i) instanceof DefaultMutableTreeNode))
                    continue;
                DefaultMutableTreeNode node1 = (DefaultMutableTreeNode)parent.getChildAt(i);
                for (int j = i+1; j < parent.getChildCount(); j++){
                    if (!(parent.getChildAt(j) instanceof DefaultMutableTreeNode))
                        continue;
                    DefaultMutableTreeNode node2 = (DefaultMutableTreeNode)parent.getChildAt(j);
                    if (compareNodes(node1, node2) >= 0){
                        parent.remove(node1);
                        parent.remove(node2);
                        parent.insert(node2, i);
                        parent.insert(node1, j);
                        swapped = true;
                    }
                }
            }
            if (swapped)
                fileTreeModel.reload(parent);
        }
        @Override
        public void treeNodesInserted(TreeModelEvent evt) {
            
        }
        @Override
        public void treeNodesRemoved(TreeModelEvent evt) {
            
        }
        @Override
        public void treeStructureChanged(TreeModelEvent evt) {
            
        }
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
            System.out.println(evt);
            System.out.println(evt.getType());
        }
        @Override
        public void valueChanged(ListSelectionEvent evt) {
            System.out.println(evt);
            if (detailsViewToggle.isSelected()){
                updateSelectedFileName(getSelectedDetails());
                int row = detailsFileTable.getSelectedRow();
                if (row >= 0)
                    renameItem.setVisible(true);
                else
                    renameItem.setVisible(false);
            }
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
}
