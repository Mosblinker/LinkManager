/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package manager.links;

import components.JListManipulator;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Milo Steier
 */
public class LinksListPanelTester extends javax.swing.JFrame {

    /**
     * Creates new form SetOfLinksPanelTester
     */
    public LinksListPanelTester() {
        initComponents();
        Handler handler = new Handler();
        linksPanel.addChangeListener(handler);
        linksPanel.addListDataListener(handler);
        linksPanel.addListSelectionListener(handler);
        linksPanel.addPropertyChangeListener(handler);
        jTabbedPane1.setTabComponentAt(jTabbedPane1.indexOfComponent(linksPanel), 
                linksPanel.getTabComponent());
        String[] defaultValues = {"Hello","Test","Hi"};
        for (String defaultValue : defaultValues) {
            linksPanel.getModel().add(defaultValue);
        }
        String[] actionCmds = {"PrintList","RemoveDuplicates","SetLastModToCurr","ScrollToCenter","DuplicateToggle"};
        for (String actionCmd : actionCmds){
            linksPanel.setListAction(actionCmd, createPanelAction(actionCmd,linksPanel));
        }
        linksPanel.setListMenuItem("DuplicateToggle", new JCheckBoxMenuItem(linksPanel.getListAction("DuplicateToggle")));
        Object[][] actionData = new Object[actionCmds.length][9];
        for (int i = 0; i < actionCmds.length; i++){
            String actionCmd = actionCmds[i];
            actionData[i][0] = actionCmd;
            actionData[i][1] = linksPanel.containsListActionKey(actionCmd);
            actionData[i][2] = linksPanel.getListAction(actionCmd);
            actionData[i][3] = linksPanel.getListAction(actionCmd).getActionCommand();
            actionData[i][4] = linksPanel.containsListMenuItemKey(actionCmd);
            actionData[i][5] = linksPanel.getListMenuItem(actionCmd);
            jMenu2.add(linksPanel.getOrCreateListMenuItem(actionCmd));
            actionData[i][6] = linksPanel.containsListMenuItemKey(actionCmd);
            actionData[i][7] = linksPanel.getListMenuItem(actionCmd);
            actionData[i][8] = linksPanel.getListMenuItem(actionCmd).getActionCommand();
        }
        actionsTable.setModel(new DefaultTableModel(actionData,new String[]{
            "Action Command", "Contains Action", "Action", "Action Command", 
            "Old Contains Menu Item", "Old Menu Item", 
            "Contains Menu Item", "Menu Item", "Menu Item Command"
        }){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        });
        linksPanel2.setModel(linksPanel.getModel());
    }
    
    private LinksListAction createPanelAction(String actionCmd, LinksListPanel panel){
        LinksListAction action = new LinksListAction(actionCmd,panel){
            @Override
            public void actionPerformed(ActionEvent evt, LinksListPanel panel) {
                if (panel == null)
                    return;
                switch(getActionCommand()){
                    case("PrintList"):
                        printButtonActionPerformed(evt);
                        return;
                    case("RemoveDuplicates"):
                        System.out.println(panel.getModel().removeDuplicates());
                        return;
                    case("SetLastModToCurr"):
                        panel.setLastModified();
                        lastModSpinner.setValue(panel.getLastModified());
                        return;
                    case("ScrollToCenter"):
                        Dimension visibleSize = panel.getViewport().getExtentSize();
                        Dimension listSize = panel.getList().getSize();
                        Rectangle newView = new Rectangle(visibleSize);
                        newView.setLocation((int)((listSize.getWidth()-visibleSize.getWidth())/2), 
                                (int)((listSize.getHeight()-visibleSize.getHeight())/2));
                        panel.getList().scrollRectToVisible(newView);
                        return;
                    case("DuplicateToggle"):
                        if (evt.getSource() instanceof AbstractButton)
                            panel.setAllowsDuplicates(((AbstractButton)evt.getSource()).isSelected());
                }
            }
            @Override
            protected String getNewActionName(LinksListPanel panel){ 
                String panelName = (panel != null) ? panel.getListName() : null;
                if (panelName == null)
                    panelName = "List";
                switch(getActionCommand()){
                    case("PrintList"):
                        return "Print " + panelName;
                    case("RemoveDuplicates"):
                        return "Remove Duplicates In " + panelName;
                    case("SetLastModToCurr"):
                        return "Set LastMod For " + panelName + " To Current Time";
                    case("ScrollToCenter"):
                        return "Scroll To The Center Of " + panelName;
                    case("DuplicateToggle"):
                        return panelName + " Allows Duplicates";
                    default:
                        return super.getNewActionName(panel);
                }
            }
            @Override
            public int getActionControlFlags(){
                int flags = super.getActionControlFlags();
                switch(getActionCommand()){
                    case("DuplicateToggle"):
                        flags |= LinksListAction.CHECK_BOX_FLAG;
                }
                return flags;
            }
        };
        action.updateActionName();
        return action;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listManipulator = new components.JListManipulator<>();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 32767));
        addItemsSpinner = new javax.swing.JSpinner();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 32767));
        addItemsButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jTabbedPane1 = new javax.swing.JTabbedPane();
        linksPanel = new manager.links.LinksListPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        actionsTable = new javax.swing.JTable();
        linksPanel2 = new manager.links.LinksListPanel();
        linkField = new javax.swing.JTextField();
        addButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        manageButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        listIDSpinner = new javax.swing.JSpinner();
        editedToggle = new javax.swing.JCheckBox();
        clearEditedButton = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        lastModSpinner = new javax.swing.JSpinner();
        setLastModButton = new javax.swing.JButton();
        nameField = new javax.swing.JTextField();
        setNameButton = new javax.swing.JButton();
        clearNameButton = new javax.swing.JButton();
        bottomScrollsToggle = new javax.swing.JCheckBox();
        enabledToggle = new javax.swing.JCheckBox();
        newModelButton = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu3 = new javax.swing.JMenu();
        listDataListenerToggle = new javax.swing.JCheckBoxMenuItem();
        listSelListenerToggle = new javax.swing.JCheckBoxMenuItem();
        changeListenerToggle = new javax.swing.JCheckBoxMenuItem();
        propChangeListenerToggle = new javax.swing.JCheckBoxMenuItem();
        jMenu2 = new javax.swing.JMenu();

        listManipulator.setBottomAccessory(jPanel2);
        listManipulator.setResetButtonIsShown(false);

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.X_AXIS));

        jLabel3.setText("Add items:");
        jPanel2.add(jLabel3);
        jPanel2.add(filler1);

        addItemsSpinner.setModel(new javax.swing.SpinnerNumberModel(1, 1, null, 1));
        jPanel2.add(addItemsSpinner);
        jPanel2.add(filler2);

        addItemsButton.setText("Add");
        addItemsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addItemsButtonActionPerformed(evt);
            }
        });
        jPanel2.add(addItemsButton);
        jPanel2.add(filler3);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jTabbedPane1.addTab("Links", linksPanel);

        jScrollPane1.setViewportView(actionsTable);

        jTabbedPane1.addTab("Actions", jScrollPane1);
        jTabbedPane1.addTab("tab3", linksPanel2);

        linkField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        addButton.setText("Add");
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        editButton.setText("Edit");
        editButton.setEnabled(false);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });

        manageButton.setText("Manage");
        manageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageButtonActionPerformed(evt);
            }
        });

        printButton.setText("Print Data");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        jLabel1.setText("ListID:");

        listIDSpinner.setModel(new javax.swing.SpinnerNumberModel());
        listIDSpinner.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                listIDSpinnerStateChanged(evt);
            }
        });

        editedToggle.setSelected(linksPanel.isEdited());
        editedToggle.setText("Is Edited");
        editedToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editedToggleActionPerformed(evt);
            }
        });

        clearEditedButton.setText("Clear Edited");
        clearEditedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearEditedButtonActionPerformed(evt);
            }
        });

        jLabel2.setText("lastMod:");

        lastModSpinner.setModel(new javax.swing.SpinnerNumberModel(0L, null, null, 1L));

        setLastModButton.setText("Set LastMod");
        setLastModButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setLastModButtonActionPerformed(evt);
            }
        });

        setNameButton.setText("Set Name");
        setNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setNameButtonActionPerformed(evt);
            }
        });

        clearNameButton.setText("Clear Name");
        clearNameButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearNameButtonActionPerformed(evt);
            }
        });

        bottomScrollsToggle.setSelected(linksPanel.getScrollsToBottom());
        bottomScrollsToggle.setText("Scrolls To Bottom");
        bottomScrollsToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottomScrollsToggleActionPerformed(evt);
            }
        });

        enabledToggle.setSelected(linksPanel.isEnabled());
        enabledToggle.setText("Is Enabled");
        enabledToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enabledToggleActionPerformed(evt);
            }
        });

        newModelButton.setText("New Model");
        newModelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newModelButtonActionPerformed(evt);
            }
        });

        jMenu3.setText("Listeners");

        listDataListenerToggle.setSelected(true);
        listDataListenerToggle.setText("ListDataListeners");
        jMenu3.add(listDataListenerToggle);

        listSelListenerToggle.setSelected(true);
        listSelListenerToggle.setText("ListSelectionListeners");
        jMenu3.add(listSelListenerToggle);

        changeListenerToggle.setSelected(true);
        changeListenerToggle.setText("ChangeListeners");
        jMenu3.add(changeListenerToggle);

        propChangeListenerToggle.setSelected(true);
        propChangeListenerToggle.setText("PropertyChangeListeners");
        jMenu3.add(propChangeListenerToggle);

        jMenuBar1.add(jMenu3);

        jMenu2.setText("Panel Actions");
        jMenuBar1.add(jMenu2);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTabbedPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(editedToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(clearEditedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lastModSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 86, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setLastModButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(enabledToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(newModelButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(editButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(manageButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(printButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(bottomScrollsToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(listIDSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 64, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(nameField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(setNameButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(clearNameButton))
                    .addComponent(linkField))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 224, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(linkField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addButton)
                    .addComponent(editButton)
                    .addComponent(manageButton)
                    .addComponent(printButton)
                    .addComponent(listIDSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(bottomScrollsToggle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(editedToggle)
                    .addComponent(jLabel2)
                    .addComponent(clearEditedButton)
                    .addComponent(lastModSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(setLastModButton)
                    .addComponent(enabledToggle)
                    .addComponent(newModelButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(clearNameButton)
                    .addComponent(setNameButton)
                    .addComponent(nameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    
    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        String text = linkField.getText();
        try{
            linksPanel.getModel().add(text);
            linkField.setText("");
        }
        catch(Exception ex){
            System.out.println("Error: " + ex);
        }
    }//GEN-LAST:event_addButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        if (linksPanel.isSelectionEmpty())
            return;
        String text = linkField.getText();
        try{
            System.out.println("Old Value: "+linksPanel.getModel().set(
                    linksPanel.getSelectedIndex(), text));
            linkField.setText("");
        }
        catch(Exception ex){
            System.out.println("Error: " + ex);
        }
    }//GEN-LAST:event_editButtonActionPerformed

    private void manageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageButtonActionPerformed
        listManipulator.setListData(linksPanel.getModel());
        if (listManipulator.showDialog(this) == JListManipulator.ACCEPT_OPTION){
            linksPanel.updateModelContents(listManipulator.getListData());
        }
    }//GEN-LAST:event_manageButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        System.out.println("Panel: " + linksPanel);
        System.out.println("Model: " + linksPanel.getModel());
        System.out.println("ListID: " + linksPanel.getListID());
        System.out.println("List Name: " + linksPanel.getListName());
        System.out.println("Allows Duplicates: " + linksPanel.getAllowsDuplicates());
        System.out.println("Is Read Only: " + linksPanel.isReadOnly());
        System.out.println("Size Limit: " + linksPanel.getSizeLimit());
        System.out.println("Is Full: " + linksPanel.getModel().isFull());
        System.out.println("Is Edited: " + linksPanel.isEdited());
        System.out.println("Last Modified: " + linksPanel.getLastModified());
        System.out.println("List Tool Tip: " + linksPanel.getListToolTipText());
        System.out.println("Panel Size: " + linksPanel.getSize());
        System.out.println("Panel Preferred Size: "+ linksPanel.getPreferredSize());
        System.out.println("Panel Minimum Size: " + linksPanel.getMinimumSize());
        System.out.println("Panel Maximum Size: " + linksPanel.getMaximumSize());
        System.out.println("Model Size: " + linksPanel.getModel().size());
        JList list = linksPanel.getList();
        System.out.println("First Visible Index: " + list.getFirstVisibleIndex());
        System.out.println("Last Visible Index: " + list.getLastVisibleIndex());
        int index = list.getSelectedIndex();
        System.out.println("Selected Index: " + index);
        System.out.println("Selected Cell Bounds: " + list.getCellBounds(index, index));
        System.out.println("Selected Cell Location: " + list.indexToLocation(index));
        System.out.println("List Size: " + list.getSize());
        System.out.println("List Bounds: " + list.getBounds());
        System.out.println("List Visible Rectangle: " + list.getVisibleRect());
        System.out.println("List Scrollable Tracks Viewport Height: " + list.getScrollableTracksViewportHeight());
        System.out.println("Preferred Viewport Size: " + list.getPreferredScrollableViewportSize());
        JViewport viewport = linksPanel.getViewport();
        System.out.println("Viewport: " + viewport);
        System.out.println("Viewport Bounds: "+viewport.getBounds());
        System.out.println("Viewport ViewRect: " + viewport.getViewRect());
        System.out.println("Viewport ViewSize: " + viewport.getViewSize());
        System.out.println("Viewport ExtentSize: " + viewport.getExtentSize());
        System.out.println();
    }//GEN-LAST:event_printButtonActionPerformed

    private void listIDSpinnerStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_listIDSpinnerStateChanged
        linksPanel.setListID((int)listIDSpinner.getValue());
    }//GEN-LAST:event_listIDSpinnerStateChanged

    private void editedToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editedToggleActionPerformed
        linksPanel.setEdited(editedToggle.isSelected());
    }//GEN-LAST:event_editedToggleActionPerformed

    private void clearEditedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearEditedButtonActionPerformed
        linksPanel.clearEdited();
    }//GEN-LAST:event_clearEditedButtonActionPerformed

    private void setLastModButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setLastModButtonActionPerformed
        linksPanel.setLastModified((long)lastModSpinner.getValue());
    }//GEN-LAST:event_setLastModButtonActionPerformed

    private void setNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setNameButtonActionPerformed
        linksPanel.setListName(nameField.getText());
    }//GEN-LAST:event_setNameButtonActionPerformed

    private void clearNameButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearNameButtonActionPerformed
        linksPanel.setListName(null);
    }//GEN-LAST:event_clearNameButtonActionPerformed

    private void bottomScrollsToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottomScrollsToggleActionPerformed
        linksPanel.setScrollsToBottom(bottomScrollsToggle.isSelected());
    }//GEN-LAST:event_bottomScrollsToggleActionPerformed

    private void enabledToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enabledToggleActionPerformed
        linksPanel.setEnabled(enabledToggle.isSelected());
    }//GEN-LAST:event_enabledToggleActionPerformed

    private void newModelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newModelButtonActionPerformed
        linksPanel.setModel(new LinksListModel((int)listIDSpinner.getValue()));
    }//GEN-LAST:event_newModelButtonActionPerformed

    private void addItemsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addItemsButtonActionPerformed
        int itemCount = (Integer) addItemsSpinner.getValue();
        int offset = linksPanel.getModel().size();
        for (int i = 0; i < itemCount; i++){
            String text = "Item "+(i+offset);
            listManipulator.getModelList().add(text);
        }
    }//GEN-LAST:event_addItemsButtonActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(LinksListPanelTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(LinksListPanelTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(LinksListPanelTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LinksListPanelTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LinksListPanelTester().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable actionsTable;
    private javax.swing.JButton addButton;
    private javax.swing.JButton addItemsButton;
    private javax.swing.JSpinner addItemsSpinner;
    private javax.swing.JCheckBox bottomScrollsToggle;
    private javax.swing.JCheckBoxMenuItem changeListenerToggle;
    private javax.swing.JButton clearEditedButton;
    private javax.swing.JButton clearNameButton;
    private javax.swing.JButton editButton;
    private javax.swing.JCheckBox editedToggle;
    private javax.swing.JCheckBox enabledToggle;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JSpinner lastModSpinner;
    private javax.swing.JTextField linkField;
    private manager.links.LinksListPanel linksPanel;
    private manager.links.LinksListPanel linksPanel2;
    private javax.swing.JCheckBoxMenuItem listDataListenerToggle;
    private javax.swing.JSpinner listIDSpinner;
    private components.JListManipulator<String> listManipulator;
    private javax.swing.JCheckBoxMenuItem listSelListenerToggle;
    private javax.swing.JButton manageButton;
    private javax.swing.JTextField nameField;
    private javax.swing.JButton newModelButton;
    private javax.swing.JButton printButton;
    private javax.swing.JCheckBoxMenuItem propChangeListenerToggle;
    private javax.swing.JButton setLastModButton;
    private javax.swing.JButton setNameButton;
    // End of variables declaration//GEN-END:variables
    private class Handler implements ListDataListener, ChangeListener, PropertyChangeListener, ListSelectionListener{

        @Override
        public void intervalAdded(ListDataEvent evt) {
            if (listDataListenerToggle.isSelected())
                System.out.println("IntervalAdded: " + evt);
            linksPanel.setToolTipText(linksPanel.getListToolTipText());
        }

        @Override
        public void intervalRemoved(ListDataEvent evt) {
            if (listDataListenerToggle.isSelected())
                System.out.println("IntervalRemoved: " + evt);
            linksPanel.setToolTipText(linksPanel.getListToolTipText());
        }

        @Override
        public void contentsChanged(ListDataEvent evt) {
            if (listDataListenerToggle.isSelected())
                System.out.println("ContentsChanged: " + evt);
            linksPanel.setToolTipText(linksPanel.getListToolTipText());
        }

        @Override
        public void stateChanged(ChangeEvent evt) {
            if (changeListenerToggle.isSelected())
                System.out.println("StateChanged: " + evt);
            editedToggle.setSelected(linksPanel.isEdited());
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (propChangeListenerToggle.isSelected())
                System.out.println("PropertyChanged: " +evt);
        }

        @Override
        public void valueChanged(ListSelectionEvent evt) {
            if (listSelListenerToggle.isSelected())
                System.out.println("ListSelectionChanged: " +evt);
            editButton.setEnabled(linksPanel.getList().getSelectedIndices().length == 1);
        }
        
    }

}
