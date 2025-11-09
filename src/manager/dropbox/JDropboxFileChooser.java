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
import java.util.Iterator;
import java.util.Objects;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import manager.LinkManager;

/**
 *
 * @author Mosblinker
 */
public class JDropboxFileChooser extends AbstractConfirmDialogPanel {

    /**
     * Creates new form JDropboxFileChooser
     */
    public JDropboxFileChooser() {
        initComponents();
        MetadataTreeCellRenderer treeCellRenderer = new MetadataTreeCellRenderer();
        dropboxFileTree.setCellRenderer(treeCellRenderer);
        treeCellEditor = new MetadataNameTreeCellEditor(dropboxFileTree,treeCellRenderer);
        dropboxFileTree.setCellEditor(treeCellEditor);
        UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
        setButtonIcon(newFolderButton,uiDefaults,"FileChooser.newFolderIcon",
                "New Folder");
        Handler handler = new Handler();
        fileTreeModel = new DefaultTreeModel(null,true);
        fileTreeModel.addTreeModelListener(handler);
        dropboxFileTree.setModel(fileTreeModel);
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
        treeCellEditor.setDropboxClient(client);
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
            loadFiles(getDropboxClient());
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
        node.setUserObject("Dropbox");
        fileTreeModel.setRoot(node);
        LinkManager.getLogger().exiting("JDropboxFileChooser", "loadFiles");
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
        controlButtonPanel = new javax.swing.JPanel();
        javax.swing.JButton acceptButton = getAcceptButton();
        javax.swing.JButton cancelButton = getCancelButton();
        treePanel = new javax.swing.JPanel();
        treeScrollPanel = new javax.swing.JScrollPane();
        dropboxFileTree = new javax.swing.JTree();
        fileNameLabel = new javax.swing.JLabel();
        fileNameField = new javax.swing.JTextField();
        newFolderButton = new javax.swing.JButton();

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

        controlButtonPanel.setLayout(new java.awt.GridLayout(1, 0, 6, 0));
        controlButtonPanel.add(acceptButton);
        controlButtonPanel.add(cancelButton);

        treePanel.setComponentPopupMenu(filePopupMenu);
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

        fileNameLabel.setLabelFor(fileNameField);
        fileNameLabel.setText("File Name:");

        newFolderButton.setToolTipText("Create New Folder");
        newFolderButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newFolderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(treePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(fileNameLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(fileNameField))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 301, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(controlButtonPanel, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(newFolderButton, javax.swing.GroupLayout.Alignment.TRAILING))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(newFolderButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(treePanel, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE)
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
                    if (DropboxUtilities.METADATA_TREE_NODE_COMPARATOR.compare(tempNode, node) >= 0){
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
            TreePath nodePath = new TreePath(node.getPath());
            dropboxFileTree.setSelectionPath(nodePath);
            dropboxFileTree.startEditingAtPath(nodePath);
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
        if (selected != null){
            if (selected.getUserObject() instanceof FileMetadata)
                fileNameField.setText(((FileMetadata)selected.getUserObject()).getName());
        }
    }//GEN-LAST:event_dropboxFileTreeValueChanged
    
    
//    private String 
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
    private MetadataNameTreeCellEditor treeCellEditor;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel controlButtonPanel;
    private javax.swing.JTree dropboxFileTree;
    private javax.swing.JTextField fileNameField;
    private javax.swing.JLabel fileNameLabel;
    private javax.swing.JPopupMenu filePopupMenu;
    private javax.swing.JButton newFolderButton;
    private javax.swing.JMenuItem newFolderItem;
    private javax.swing.JMenuItem refreshItem;
    private javax.swing.JPanel treePanel;
    private javax.swing.JScrollPane treeScrollPanel;
    private javax.swing.ButtonGroup viewButtonGroup;
    // End of variables declaration//GEN-END:variables

    private class Handler implements TreeModelListener{
        @Override
        public void treeNodesChanged(TreeModelEvent evt) {
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
                    if (DropboxUtilities.METADATA_TREE_NODE_COMPARATOR.compare(node1, node2) >= 0){
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
    }
}
