/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/GUIForms/JFrame.java to edit this template
 */
package manager.links;

import components.JListManipulator;
import icons.DebuggingIcon;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.table.DefaultTableModel;
import manager.icons.ListIndicatorIcon;

/**
 *
 * @author Milo Steier
 */
public class LinksListTabsPanelTester extends javax.swing.JFrame {
    
    private static final String PRINT_LIST_ACTION = "PrintList";
    
    private static final String REMOVE_DUPLICATES_ACTION = "RemoveDuplicates";
    
    private static final String CLEAR_LIST_EDITED_ACTION = "ClearEdited";
    
    private static final String SELECT_LIST_ACTION = "SelectList";
    
    private static final String CLEAR_LIST_CONTENTS_ACTION = "ClearList";
    
    private static final String SCROLL_TO_CENTER_ACTION = "CenterScroll";
    
    private static final String POPULATE_LIST_ACTION = "PopulateList";
    
    private static final String PRIVATE_LIST_ACTION = "PrivateList";
    
    private static final String READ_ONLY_LIST_ACTION = "ReadOnlyList";

    /**
     * Creates new form LinksListTabsPanelTester
     */
    public LinksListTabsPanelTester() {
        initComponents();
        listTabsPanel.setListActionMapper((String t, LinksListPanel u) -> {
            return createPanelAction(t,u);
        });
        String[][] testListNames = {{"Hello", "Hi"},{"Test","Testing"}};
        for (int i = 0; i < testListNames[0].length; i++){
            listTabsPanel.getModels().add(new LinksListModel(testListNames[0][i],i));
        }
        listTabsPanel.putListActionMenus(printMenu, removeDupMenu, clearEditMenu, 
                selListMenu,clearListMenu, centerScrollMenu, populateMenu,
                privateMenu, readOnlyMenu);
        for (int i = 0; i < testListNames[1].length; i++){
            listTabsPanel.getModels().add(new LinksListModel(testListNames[1][i],i+testListNames[0].length));
        }
        listTabsPanel.clearEdited();
        
        for (String temp : new String[]{ListTabButton.MOVE_TAB_LEFT_ACTION,
            ListTabButton.MOVE_TAB_RIGHT_ACTION,ListTabButton.REMOVE_TAB_ACTION,
            ListTabButton.ADD_TAB_ACTION,ListTabButton.RENAME_TAB_ACTION}){
            JButton button = new ListTabButton(temp);
            jPanel3.add(button);
            addTabCompFiller();
        }
        jPanel3.add(listTabsPanel.new AddTabComponent());
        addTabCompFiller();
        
        jTabbedPane1.setTabComponentAt(jTabbedPane1.indexOfComponent(jPanel4), listTabsPanel.new AddTabComponent());
        
        jPanel5.setVisible(false);
        
        refreshTablesButtonActionPerformed(null);
        
        listIcon = new ListIndicatorIcon();
        listIconDebug = new DebuggingIcon(listIcon,iconDebugToggle.isSelected());
        listIconTestLabel.setIcon(listIconDebug);
        listIcon.addChangeListener((ChangeEvent e) -> {
            listIconTestLabel.repaint();
        });
    }
    
    private void addTabCompFiller(){
        jPanel3.add(new javax.swing.Box.Filler(
                new java.awt.Dimension(6, 6), 
                new java.awt.Dimension(6, 6), 
                new java.awt.Dimension(6, 6)));
    }
    
    private LinksListAction createPanelAction(String command, LinksListPanel panel){
        if (panel == null){
            switch(command){
                case(SELECT_LIST_ACTION):
                case(PRIVATE_LIST_ACTION):
                case(READ_ONLY_LIST_ACTION):
                    return null;
            }
        }
        return new LinksListTabAction(command,listTabsPanel,panel) {
            @Override
            public void actionPerformed(ActionEvent evt, LinksListPanel panel, LinksListTabsPanel tabsPanel) {
                if (panel == null)
                    return;
                LinksListModel model = panel.getModel();
                switch(getActionCommand()){
                    case(PRINT_LIST_ACTION):
                        System.out.println(getListName(panel) + ": ");
                        System.out.println("\tPanel: " + panel);
                        System.out.println("\tModel: " + model);
                        System.out.println("\tListID: " + panel.getListID());
                        System.out.println("\tList Name: " + panel.getListName());
                        System.out.println("\tFlags: "+model.getFlags());
                        System.out.println("\tAllows Duplicates: " + panel.getAllowsDuplicates());
                        System.out.println("\tIs Edited: " + panel.isEdited());
//                        System.out.println("\tOld ListID: " + model.getOldListID()+
//                                " (Changed: " + !Objects.equals(panel.getListID(), model.getOldListID())+")");
//                        System.out.println("\tOld List Name: " + model.getOldListName()+
//                                " (Changed: " + !Objects.equals(panel.getListName(), model.getOldListName())+")");
//                        System.out.println("\tOld Flags: " + model.getOldFlags() + 
//                                " (Changed: " + (model.getFlags() != model.getOldFlags())+")");
                        System.out.println("\tContents Modified: " + model.getContentsModified());
                        System.out.println("\tLast Modified: " + panel.getLastModified());
                        System.out.println("\tList Tool Tip: " + panel.getListToolTipText());
                        System.out.println("\tTab Index: " + listTabsPanel.getLists().indexOf(panel));
                        System.out.println("\tIs Selected: " + listTabsPanel.isSelected(panel) + " " + 
                                listTabsPanel.isSelected(listTabsPanel.getLists().indexOf(panel)));
                        System.out.println("\tTab Name: " + listTabsPanel.getListName(panel));
                        System.out.println("\tTab Title: " + listTabsPanel.getTitleForList(panel));
                        return;
                    case(REMOVE_DUPLICATES_ACTION):
                        System.out.println(model.removeDuplicates());
                        return;
                    case(CLEAR_LIST_EDITED_ACTION):
                        panel.clearEdited();
                        return;
                    case(SELECT_LIST_ACTION):
                        listTabsPanel.setSelectedList(panel);
                        return;
                    case(CLEAR_LIST_CONTENTS_ACTION):
                        model.clear();
                        return;
                    case(SCROLL_TO_CENTER_ACTION):
                        Dimension visibleSize = panel.getViewport().getExtentSize();
                        Dimension listSize = panel.getList().getSize();
                        Rectangle newView = new Rectangle(visibleSize);
                        newView.setLocation((int)((listSize.getWidth()-visibleSize.getWidth())/2), 
                                (int)((listSize.getHeight()-visibleSize.getHeight())/2));
                        panel.getList().scrollRectToVisible(newView);
                        System.out.println(panel.getList().getVisibleRect() + " " + newView);
                        return;
                    case(POPULATE_LIST_ACTION):
                        for (int i = 0; i < 10; i++){
                            String text = "Item " + model.size();
                            model.add(text);
                        }
                        return;
                    case(PRIVATE_LIST_ACTION):
                        if (evt.getSource() instanceof AbstractButton)
                            panel.setAllowsDuplicates(((AbstractButton)evt.getSource()).isSelected());
                        return;
                    case(READ_ONLY_LIST_ACTION):
                        if (evt.getSource() instanceof AbstractButton)
                            panel.setReadOnly(((AbstractButton)evt.getSource()).isSelected());
                }
            }
            @Override
            protected String getNewActionName(LinksListPanel panel){
                String panelName = getListName(panel);
                if (panelName == null)
                    panelName = "List";
                switch(getActionCommand()){
                    case(PRINT_LIST_ACTION):
                        return "Print " + panelName;
                    case(REMOVE_DUPLICATES_ACTION):
                        return "Remove Duplicates in " + panelName;
                    case(CLEAR_LIST_EDITED_ACTION):
                        return "Clear if " + panelName + " is Edited";
                    case(SELECT_LIST_ACTION):
                        return "Select " + panelName;
                    case(CLEAR_LIST_CONTENTS_ACTION):
                        return "Clear " + panelName;
                    case(SCROLL_TO_CENTER_ACTION):
                        return "Scroll To Center Of " + panelName;
                    case(POPULATE_LIST_ACTION):
                        return "Populate " + panelName;
                    case(PRIVATE_LIST_ACTION):
                        return "Allow Duplicates In " + panelName;
                    case(READ_ONLY_LIST_ACTION):
                        return "Set " + panelName + " To Read Only";
                }
                return super.getNewActionName(panel);
            }
            @Override
            public int getActionControlFlags(){
                int flags = super.getActionControlFlags();
                switch(getActionCommand()){
                    case(PRIVATE_LIST_ACTION):
                    case(READ_ONLY_LIST_ACTION):
                        flags |= LinksListAction.CHECK_BOX_FLAG;
                }
                return flags;
            }
            @Override
            public int getRequiredFlags(){
                switch(getActionCommand()){
                    case(REMOVE_DUPLICATES_ACTION):
                    case(POPULATE_LIST_ACTION):
                        return LinksListModel.READ_ONLY_FLAG;
                }
                return super.getRequiredFlags();
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        listTabsManipulator = new manager.links.LinksListTabsManipulator();
        jPanel1 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 32767));
        setListIDSpinner = new javax.swing.JSpinner();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 32767));
        setListIDButton = new javax.swing.JButton();
        filler3 = new javax.swing.Box.Filler(new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 0), new java.awt.Dimension(6, 32767));
        clearListIDButton = new javax.swing.JButton();
        filler4 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        jButton1 = new javax.swing.JButton();
        tabManipListIDLabel = new javax.swing.JLabel();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        listTabsPanel = new manager.links.LinksListTabsPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        actionTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        iconDebugToggle = new javax.swing.JCheckBox();
        iconHiddenToggle = new javax.swing.JCheckBox();
        iconReadOnlyToggle = new javax.swing.JCheckBox();
        iconFullToggle = new javax.swing.JCheckBox();
        printIconButton = new javax.swing.JButton();
        listIconTestLabel = new javax.swing.JLabel();
        linkField = new javax.swing.JTextField();
        listAddButton = new javax.swing.JButton();
        listInsertButton = new javax.swing.JButton();
        listManageButton = new javax.swing.JButton();
        printButton = new javax.swing.JButton();
        isEditedToggle = new javax.swing.JCheckBox();
        listEditedDisplay = new javax.swing.JCheckBox();
        removeListButton = new javax.swing.JButton();
        clearListsButton = new javax.swing.JButton();
        clearEditedButton = new javax.swing.JButton();
        debugToggle = new javax.swing.JCheckBox();
        enabledToggle = new javax.swing.JCheckBox();
        jLabel1 = new javax.swing.JLabel();
        listIDSpinner = new javax.swing.JSpinner();
        indexOfIDButton = new javax.swing.JButton();
        selectIDButton = new javax.swing.JButton();
        clearSelButton = new javax.swing.JButton();
        isSelIDButton = new javax.swing.JButton();
        refreshTablesButton = new javax.swing.JButton();
        listNameField = new javax.swing.JTextField();
        newListIDToggle = new javax.swing.JCheckBox();
        newListIDSpinner = new javax.swing.JSpinner();
        addLinkButton = new javax.swing.JButton();
        listsEnabledToggle = new javax.swing.JCheckBox();
        showPrivateToggle = new javax.swing.JCheckBox();
        structEditedToggle = new javax.swing.JCheckBox();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        listSelListenerToggle = new javax.swing.JCheckBoxMenuItem();
        changeListenerToggle = new javax.swing.JCheckBoxMenuItem();
        propChangeListenerToggle = new javax.swing.JCheckBoxMenuItem();
        jMenu2 = new javax.swing.JMenu();
        printMenu = new javax.swing.JMenu();
        removeDupMenu = new javax.swing.JMenu();
        clearEditMenu = new javax.swing.JMenu();
        selListMenu = new javax.swing.JMenu();
        clearListMenu = new javax.swing.JMenu();
        centerScrollMenu = new javax.swing.JMenu();
        populateMenu = new javax.swing.JMenu();
        privateMenu = new javax.swing.JMenu();
        readOnlyMenu = new javax.swing.JMenu();

        listTabsManipulator.setBottomAccessory(jPanel1);
        listTabsManipulator.setSideAccessory(tabManipListIDLabel);
        listTabsManipulator.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listTabsManipulatorValueChanged(evt);
            }
        });

        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.X_AXIS));

        jLabel2.setText("ListID:");
        jPanel1.add(jLabel2);
        jPanel1.add(filler1);

        setListIDSpinner.setModel(new javax.swing.SpinnerNumberModel());
        setListIDSpinner.setEnabled(false);
        jPanel1.add(setListIDSpinner);
        jPanel1.add(filler2);

        setListIDButton.setText("Set ID");
        setListIDButton.setEnabled(false);
        setListIDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                setListIDButtonActionPerformed(evt);
            }
        });
        jPanel1.add(setListIDButton);
        jPanel1.add(filler3);

        clearListIDButton.setText("Clear ID");
        clearListIDButton.setEnabled(false);
        clearListIDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearListIDButtonActionPerformed(evt);
            }
        });
        jPanel1.add(clearListIDButton);
        jPanel1.add(filler4);

        jButton1.setText("Print Stuff");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel1.add(jButton1);

        tabManipListIDLabel.setText("ListID: ");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        listTabsPanel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                listTabsPanelStateChanged(evt);
            }
        });
        listTabsPanel.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                listTabsPanelPropertyChange(evt);
            }
        });
        listTabsPanel.addListSelectionListener(new javax.swing.event.ListSelectionListener() {
            public void valueChanged(javax.swing.event.ListSelectionEvent evt) {
                listTabsPanelValueChanged(evt);
            }
        });
        jTabbedPane1.addTab("Lists", listTabsPanel);

        jScrollPane1.setViewportView(actionTable);

        jTabbedPane1.addTab("Actions", jScrollPane1);

        jPanel3.setBorder(javax.swing.BorderFactory.createEmptyBorder(10, 10, 10, 10));
        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.X_AXIS));
        jTabbedPane1.addTab("Tab Components", jPanel3);

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 648, Short.MAX_VALUE)
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 103, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("tab5", jPanel4);

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 648, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 103, Short.MAX_VALUE)
        );

        jTabbedPane1.addTab("Hidden Tab", jPanel5);

        iconDebugToggle.setText("Debug");
        iconDebugToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconDebugToggleActionPerformed(evt);
            }
        });

        iconHiddenToggle.setText("Hidden");
        iconHiddenToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconHiddenToggleActionPerformed(evt);
            }
        });

        iconReadOnlyToggle.setText("Read Only");
        iconReadOnlyToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconReadOnlyToggleActionPerformed(evt);
            }
        });

        iconFullToggle.setText("Full");
        iconFullToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                iconFullToggleActionPerformed(evt);
            }
        });

        printIconButton.setText("Print Icon");

        listIconTestLabel.setText("Hello There");
        listIconTestLabel.setBorder(javax.swing.BorderFactory.createCompoundBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        listIconTestLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(iconDebugToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iconHiddenToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iconReadOnlyToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(iconFullToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 346, Short.MAX_VALUE)
                        .addComponent(printIconButton))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(listIconTestLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(listIconTestLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(iconDebugToggle)
                    .addComponent(iconHiddenToggle)
                    .addComponent(iconReadOnlyToggle)
                    .addComponent(iconFullToggle)
                    .addComponent(printIconButton))
                .addContainerGap())
        );

        jTabbedPane1.addTab("Icons", jPanel2);

        linkField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLinkButtonActionPerformed(evt);
            }
        });

        listAddButton.setText("Add");
        listAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listAddButtonActionPerformed(evt);
            }
        });

        listInsertButton.setText("Insert");
        listInsertButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listInsertButtonActionPerformed(evt);
            }
        });

        listManageButton.setText("Manage");
        listManageButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listManageButtonActionPerformed(evt);
            }
        });

        printButton.setText("Print Data");
        printButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printButtonActionPerformed(evt);
            }
        });

        isEditedToggle.setSelected(listTabsPanel.isEdited());
        isEditedToggle.setText("Is Edited");
        isEditedToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isEditedToggleActionPerformed(evt);
            }
        });

        listEditedDisplay.setSelected(listTabsPanel.getListsEdited());
        listEditedDisplay.setText("Lists Edited");
        listEditedDisplay.setEnabled(false);

        removeListButton.setText("Remove");
        removeListButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeListButtonActionPerformed(evt);
            }
        });

        clearListsButton.setText("Clear");
        clearListsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearListsButtonActionPerformed(evt);
            }
        });

        clearEditedButton.setText("Clear Edited");
        clearEditedButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearEditedButtonActionPerformed(evt);
            }
        });

        debugToggle.setSelected(listTabsPanel.isInDebug());
        debugToggle.setText("Debug");
        debugToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                debugToggleActionPerformed(evt);
            }
        });

        enabledToggle.setSelected(listTabsPanel.isEnabled());
        enabledToggle.setText("Is Enabled");
        enabledToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enabledToggleActionPerformed(evt);
            }
        });

        jLabel1.setText("ListID:");

        listIDSpinner.setModel(new javax.swing.SpinnerNumberModel());

        indexOfIDButton.setText("Index Of ID");
        indexOfIDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                indexOfIDButtonActionPerformed(evt);
            }
        });

        selectIDButton.setText("Set Selected ID");
        selectIDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectIDButtonActionPerformed(evt);
            }
        });

        clearSelButton.setText("Clear Selection");
        clearSelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearSelButtonActionPerformed(evt);
            }
        });

        isSelIDButton.setText("Is ID Selected");
        isSelIDButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isSelIDButtonActionPerformed(evt);
            }
        });

        refreshTablesButton.setText("Refresh Tables");
        refreshTablesButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                refreshTablesButtonActionPerformed(evt);
            }
        });

        listNameField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listAddButtonActionPerformed(evt);
            }
        });

        newListIDToggle.setText("ListID:");
        newListIDToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newListIDToggleActionPerformed(evt);
            }
        });

        newListIDSpinner.setModel(new javax.swing.SpinnerNumberModel(4, null, null, 1));
        newListIDSpinner.setEnabled(false);

        addLinkButton.setText("Add Link");
        addLinkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLinkButtonActionPerformed(evt);
            }
        });

        listsEnabledToggle.setSelected(true);
        listsEnabledToggle.setText("Lists Enabled");
        listsEnabledToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listsEnabledToggleActionPerformed(evt);
            }
        });

        showPrivateToggle.setSelected(true);
        showPrivateToggle.setText("Show Private Lists");
        showPrivateToggle.setEnabled(false);
        showPrivateToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPrivateToggleActionPerformed(evt);
            }
        });

        structEditedToggle.setText("Structure Edited");
        structEditedToggle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                structEditedToggleActionPerformed(evt);
            }
        });

        jMenu1.setText("Listeners");

        listSelListenerToggle.setSelected(true);
        listSelListenerToggle.setText("ListSelectionListeners");
        jMenu1.add(listSelListenerToggle);

        changeListenerToggle.setSelected(true);
        changeListenerToggle.setText("ChangeListeners");
        jMenu1.add(changeListenerToggle);

        propChangeListenerToggle.setSelected(true);
        propChangeListenerToggle.setText("PropertyChangeListeners");
        jMenu1.add(propChangeListenerToggle);

        jMenuBar1.add(jMenu1);

        jMenu2.setText("Panel Actions");

        printMenu.setText("Print List");
        printMenu.setActionCommand(PRINT_LIST_ACTION);
        jMenu2.add(printMenu);

        removeDupMenu.setText("Remove Duplicates");
        removeDupMenu.setActionCommand(REMOVE_DUPLICATES_ACTION);
        jMenu2.add(removeDupMenu);

        clearEditMenu.setText("Clear List Edited");
        clearEditMenu.setActionCommand(CLEAR_LIST_EDITED_ACTION);
        jMenu2.add(clearEditMenu);

        selListMenu.setText("Select List");
        selListMenu.setActionCommand(SELECT_LIST_ACTION);
        jMenu2.add(selListMenu);

        clearListMenu.setText("Clear List");
        clearListMenu.setActionCommand(CLEAR_LIST_CONTENTS_ACTION);
        jMenu2.add(clearListMenu);

        centerScrollMenu.setText("Scroll To Center");
        centerScrollMenu.setActionCommand(SCROLL_TO_CENTER_ACTION);
        jMenu2.add(centerScrollMenu);

        populateMenu.setText("Populate List");
        populateMenu.setActionCommand(POPULATE_LIST_ACTION);
        jMenu2.add(populateMenu);

        privateMenu.setText("Allow Duplicates");
        privateMenu.setActionCommand(PRIVATE_LIST_ACTION);
        jMenu2.add(privateMenu);

        readOnlyMenu.setText("Set List To Read Only");
        readOnlyMenu.setActionCommand(READ_ONLY_LIST_ACTION);
        jMenu2.add(readOnlyMenu);

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
                        .addComponent(linkField)
                        .addGap(11, 11, 11)
                        .addComponent(refreshTablesButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(listNameField)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(newListIDToggle)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(newListIDSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 50, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(listAddButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(listInsertButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(removeListButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearListsButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(listManageButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(printButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(addLinkButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(showPrivateToggle))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel1)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(listIDSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(indexOfIDButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(isSelIDButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(selectIDButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearSelButton))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(isEditedToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(listEditedDisplay)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(structEditedToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(clearEditedButton)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(enabledToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(debugToggle)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(listsEnabledToggle)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTabbedPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 133, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(linkField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(refreshTablesButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listNameField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newListIDSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(newListIDToggle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(listAddButton)
                    .addComponent(listInsertButton)
                    .addComponent(listManageButton)
                    .addComponent(printButton)
                    .addComponent(removeListButton)
                    .addComponent(clearListsButton)
                    .addComponent(addLinkButton)
                    .addComponent(showPrivateToggle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isEditedToggle)
                    .addComponent(listEditedDisplay)
                    .addComponent(clearEditedButton)
                    .addComponent(enabledToggle)
                    .addComponent(debugToggle)
                    .addComponent(listsEnabledToggle)
                    .addComponent(structEditedToggle))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(listIDSpinner, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(indexOfIDButton)
                    .addComponent(selectIDButton)
                    .addComponent(clearSelButton)
                    .addComponent(isSelIDButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void listTabsPanelPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_listTabsPanelPropertyChange
        if (propChangeListenerToggle.isSelected())
            System.out.println("PropertyChanged: " +evt);
        listEditedDisplay.setSelected(listTabsPanel.getListsEdited());
        isEditedToggle.setSelected(listTabsPanel.isEdited());
        structEditedToggle.setSelected(listTabsPanel.isStructureEdited());
    }//GEN-LAST:event_listTabsPanelPropertyChange

    private void listTabsPanelStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_listTabsPanelStateChanged
        if (changeListenerToggle.isSelected())
            System.out.println("StateChanged: " + evt);
    }//GEN-LAST:event_listTabsPanelStateChanged

    private void listTabsPanelValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listTabsPanelValueChanged
        if (listSelListenerToggle.isSelected())
            System.out.println("ListSelectionChanged: " +evt);
    }//GEN-LAST:event_listTabsPanelValueChanged

    private void listTabsManipulatorValueChanged(javax.swing.event.ListSelectionEvent evt) {//GEN-FIRST:event_listTabsManipulatorValueChanged
        setListIDSpinner.setEnabled(listTabsManipulator.getSelectedItemsCount() == 1);
        setListIDButton.setEnabled(setListIDSpinner.isEnabled());
        clearListIDButton.setEnabled(setListIDSpinner.isEnabled());
        String listIDStr;
        if (setListIDSpinner.isEnabled()){
            LinksListModel model = listTabsManipulator.getSelectedValue();
            listIDStr = Objects.toString(model.getListID(), "N/A");
            if (model.getListID()!=null)
                setListIDSpinner.setValue(model.getListID());
            else
                setListIDSpinner.setValue(-1);
        }
        else
            listIDStr = "";
        tabManipListIDLabel.setText("ListID: " + listIDStr);
    }//GEN-LAST:event_listTabsManipulatorValueChanged

    private void setListIDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_setListIDButtonActionPerformed
        listTabsManipulator.getSelectedValue().setListID((Integer)setListIDSpinner.getValue());
        tabManipListIDLabel.setText("ListID: " + listTabsManipulator.getSelectedValue().getListID());
    }//GEN-LAST:event_setListIDButtonActionPerformed

    private void clearListIDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearListIDButtonActionPerformed
        listTabsManipulator.getSelectedValue().setListID(null);
        tabManipListIDLabel.setText("ListID: " + Objects.toString(listTabsManipulator.getSelectedValue().getListID(), "N/A"));
    }//GEN-LAST:event_clearListIDButtonActionPerformed
    
    private Number getIDSpinnerValue(JSpinner spinner, JToggleButton toggle){
        if (toggle.isSelected()){
            Number value = (Number)spinner.getValue();
            spinner.setValue(spinner.getNextValue());
            return value;
        }
        else
            return null;
    }
    
    private LinksListModel createNewModel(){
        String text = listNameField.getText();
        Number listID = getIDSpinnerValue(newListIDSpinner,newListIDToggle);
        LinksListModel model = new LinksListModel(text,(listID != null) ? listID.intValue() : null);
        listNameField.setText("");
        return model;
    }
    
    private void listAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listAddButtonActionPerformed
        try{
            listTabsPanel.getModels().add(createNewModel());
        }
        catch(Exception ex){
            System.out.println("Error: " + ex);
        }
    }//GEN-LAST:event_listAddButtonActionPerformed

    private void listInsertButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listInsertButtonActionPerformed
        try{
            listTabsPanel.getModels().add(Math.max(0, listTabsPanel.getSelectedIndex()), createNewModel());
        }
        catch(Exception ex){
            System.out.println("Error: " + ex);
        }
    }//GEN-LAST:event_listInsertButtonActionPerformed

    private void listManageButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listManageButtonActionPerformed
        listTabsManipulator.setListData(listTabsPanel.getModels());
            // If the user has confirmed the changes to the lists
        if (listTabsManipulator.showDialog(this) == JListManipulator.ACCEPT_OPTION){
            listTabsManipulator.updateListTabs(listTabsPanel);
        }
        System.gc();
    }//GEN-LAST:event_listManageButtonActionPerformed

    private void printButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printButtonActionPerformed
        System.out.println("Panel: " + listTabsPanel);
        System.out.println("Tabbed Pane: " + listTabsPanel.getTabbedPane());
        System.out.println("Is Edited: " + listTabsPanel.isEdited());
        System.out.println("Lists Edited: " + listTabsPanel.getListsEdited());
        System.out.println("Structure Edited: " + listTabsPanel.isStructureEdited());
        System.out.println("Removed ListIDs: " + listTabsPanel.getRemovedListIDs());
        System.out.println("Tab Components: " + listTabsPanel.getTabComponents());
        System.out.println("Tab Count: " + listTabsPanel.getTabCount());
        System.out.println("ListIDs: " + listTabsPanel.getListIDs());
        System.out.println("List Count: " + listTabsPanel.getLists().size());
        System.out.println("Lists:");
        for (int i = 0; i < listTabsPanel.getLists().size(); i++){
            LinksListPanel panel = listTabsPanel.getLists().get(i);
            System.out.printf("%6d: (listID=%4d, edited=%5b, size=%6d, lastMod=%15d) %s %s%n",
                i,
                panel.getListID(),
                panel.isEdited(),
                panel.getModel().size(),
                panel.getLastModified(),
                panel.getName(),
                panel.getModel()
            );
        }
        for (int i = 0; i < listTabsPanel.getLists().size() || i < listTabsPanel.getTabCount(); i++){
            LinksListPanel panel = (i < listTabsPanel.getLists().size()) ? listTabsPanel.getLists().get(i):null;
            String panelName = (panel != null) ? panel.getName() : "null";
            Component comp = (i < listTabsPanel.getTabCount()) ?
            listTabsPanel.getTabbedPane().getComponentAt(i):null;
            String compName = (comp != null) ? comp.getName() : "null";
            System.out.printf("%6d: %10s %10s %5b%n", i,panelName,compName,Objects.equals(panel, comp));
        }
        System.out.println("Action Mapper: " + listTabsPanel.getListActionMapper());
        System.out.println("Is Selection Empty: " + listTabsPanel.isSelectionEmpty());
        System.out.println("Is Selection Not A List: " + listTabsPanel.isNonListSelected());
        System.out.println("Selected Index: " + listTabsPanel.getSelectedIndex());
        System.out.println("Selected Component: " + listTabsPanel.getSelectedComponent());
        System.out.println("Selected List: " + listTabsPanel.getSelectedList());
        System.out.println("Selected Model: " + listTabsPanel.getSelectedModel());
        System.out.println("Selected ListID: " + listTabsPanel.getSelectedListID());
        
    }//GEN-LAST:event_printButtonActionPerformed

    private void removeListButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeListButtonActionPerformed
        if (!listTabsPanel.isSelectionEmpty())
            listTabsPanel.getLists().remove(listTabsPanel.getSelectedIndex());
    }//GEN-LAST:event_removeListButtonActionPerformed

    private void clearListsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearListsButtonActionPerformed
        listTabsPanel.getLists().clear();
    }//GEN-LAST:event_clearListsButtonActionPerformed

    private void clearEditedButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearEditedButtonActionPerformed
        listTabsPanel.clearEdited();
        listEditedDisplay.setSelected(listTabsPanel.getListsEdited());
    }//GEN-LAST:event_clearEditedButtonActionPerformed

    private void isEditedToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isEditedToggleActionPerformed
        listTabsPanel.setEdited(isEditedToggle.isSelected());
        listEditedDisplay.setSelected(listTabsPanel.getListsEdited());
    }//GEN-LAST:event_isEditedToggleActionPerformed

    private void debugToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_debugToggleActionPerformed
        listTabsPanel.setInDebug(debugToggle.isSelected());
    }//GEN-LAST:event_debugToggleActionPerformed

    private void enabledToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enabledToggleActionPerformed
        listTabsPanel.setEnabled(enabledToggle.isSelected());
    }//GEN-LAST:event_enabledToggleActionPerformed

    private void indexOfIDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_indexOfIDButtonActionPerformed
        int listID = (Integer) listIDSpinner.getValue();
        System.out.println("Index Of ListID " + listID + ": " + listTabsPanel.getListIDs().indexOf(listID));
    }//GEN-LAST:event_indexOfIDButtonActionPerformed

    private void selectIDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectIDButtonActionPerformed
        listTabsPanel.setSelectedListID((Integer)listIDSpinner.getValue());
    }//GEN-LAST:event_selectIDButtonActionPerformed

    private void clearSelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearSelButtonActionPerformed
        listTabsPanel.setSelectedIndex(-1);
    }//GEN-LAST:event_clearSelButtonActionPerformed
    
    private int createActionRows(Object[][] data, String[] colNames, int index, LinksListPanel panel){
        data[index][0] = data[index+1][0] = data[index+2][0] = listTabsPanel.getListName(panel);
        data[index][1] = "Action";
        data[index+1][1] = "Action Name";
        data[index+2][1] = "Menu Item";
        for (int c = 2; c < colNames.length-1; c++){
            LinksListAction action = listTabsPanel.getListAction(panel, colNames[c]);
            data[index][c] = action;
            data[index+1][c] = (action != null) ? action.getName() : "null";
            data[index+2][c] = listTabsPanel.getListMenuItem(panel, colNames[c]);
        }
        data[index][colNames.length-1] = listTabsPanel.getActionMappingFunction(panel);
        data[index+2][colNames.length-1] = LinksListPanel.getMenuItemActionMapper(listTabsPanel.getListActionMap(panel));
        return index+3;
    }
    
    private void refreshTablesButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_refreshTablesButtonActionPerformed
        String[] colNames = new String[listTabsPanel.getListActionCommandCount()+3];
        colNames[0] = "Panel";
        colNames[1] = "Value Type";
        int index = 2;
        for (String temp : listTabsPanel.getListActionCommands()){
            colNames[index] = temp;
            index++;
        }
        colNames[index] = "Mapper";
        Object[][] data = new Object[((listTabsPanel.getLists().size()+1)*3)+3][colNames.length];
        index = 0;
        for (LinksListPanel panel : listTabsPanel){
            index = createActionRows(data,colNames,index,panel);
        }
        data[index][0] = "";
        data[index][1] = "Separator";
        for (int c = 2; c < colNames.length-1; c++){
            data[index][c] = listTabsPanel.getListActionSeparators().get(colNames[c]);
        }
        index = createActionRows(data,colNames,index+1,null);
        data[index][0] = data[index+1][0] = "";
        data[index][1] = "Menu";
        data[index+1][1] = "Menu Text";
        for (int c = 2; c < colNames.length-1; c++){
            JMenu menu = listTabsPanel.getListActionMenu(colNames[c]);
            data[index][c] = menu;
            data[index+1][c] = (menu != null) ? menu.getText() : "null";
        }
        
        actionTable.setModel(new DefaultTableModel(data,colNames){
            @Override
            public boolean isCellEditable(int row, int column){
                return false;
            }
        });
    }//GEN-LAST:event_refreshTablesButtonActionPerformed

    private void isSelIDButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isSelIDButtonActionPerformed
        int listID = (Integer) listIDSpinner.getValue();
        System.out.println("Is ListID " + listID + " Selected: " + listTabsPanel.isSelectedListID(listID));
    }//GEN-LAST:event_isSelIDButtonActionPerformed

    private void newListIDToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newListIDToggleActionPerformed
        newListIDSpinner.setEnabled(newListIDToggle.isSelected());
    }//GEN-LAST:event_newListIDToggleActionPerformed

    private void addLinkButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLinkButtonActionPerformed
        String text = linkField.getText();
        try{
            listTabsPanel.getSelectedModel().add(text);
            linkField.setText("");
        }
        catch(Exception ex){
            System.out.println("Error: " + ex);
        }
    }//GEN-LAST:event_addLinkButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        System.out.println(listTabsManipulator.getModelList());
        System.out.println(listTabsManipulator.getModel());
        System.out.println(listTabsManipulator.getListData());
        System.out.println(listTabsManipulator.getList().getModel());
    }//GEN-LAST:event_jButton1ActionPerformed

    private void listsEnabledToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listsEnabledToggleActionPerformed
        listTabsPanel.setListsEnabled(listsEnabledToggle.isSelected());
    }//GEN-LAST:event_listsEnabledToggleActionPerformed

    private void showPrivateToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPrivateToggleActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_showPrivateToggleActionPerformed

    private void structEditedToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_structEditedToggleActionPerformed
        listTabsPanel.setStructureEdited(structEditedToggle.isSelected());
    }//GEN-LAST:event_structEditedToggleActionPerformed

    private void iconDebugToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconDebugToggleActionPerformed
        listIconDebug.setDebugEnabled(iconDebugToggle.isSelected());
        listIconTestLabel.repaint();
    }//GEN-LAST:event_iconDebugToggleActionPerformed

    private void iconHiddenToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconHiddenToggleActionPerformed
        listIcon.setHidden(iconHiddenToggle.isSelected());
    }//GEN-LAST:event_iconHiddenToggleActionPerformed

    private void iconReadOnlyToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconReadOnlyToggleActionPerformed
        listIcon.setReadOnly(iconReadOnlyToggle.isSelected());
    }//GEN-LAST:event_iconReadOnlyToggleActionPerformed

    private void iconFullToggleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_iconFullToggleActionPerformed
        listIcon.setFull(iconFullToggle.isSelected());
    }//GEN-LAST:event_iconFullToggleActionPerformed

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
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(LinksListTabsPanelTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
        
        /* Set the System Look and Feel look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If System is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html 
         */
//        try {
//            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
//        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
//            java.util.logging.Logger.getLogger(LinksListTabsPanelTester.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
//        }
        //</editor-fold>
        
        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new LinksListTabsPanelTester().setVisible(true);
            }
        });
    }
    
    private ListIndicatorIcon listIcon;
    private DebuggingIcon listIconDebug;
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTable actionTable;
    private javax.swing.JButton addLinkButton;
    private javax.swing.JMenu centerScrollMenu;
    private javax.swing.JCheckBoxMenuItem changeListenerToggle;
    private javax.swing.JMenu clearEditMenu;
    private javax.swing.JButton clearEditedButton;
    private javax.swing.JButton clearListIDButton;
    private javax.swing.JMenu clearListMenu;
    private javax.swing.JButton clearListsButton;
    private javax.swing.JButton clearSelButton;
    private javax.swing.JCheckBox debugToggle;
    private javax.swing.JCheckBox enabledToggle;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.Box.Filler filler3;
    private javax.swing.Box.Filler filler4;
    private javax.swing.JCheckBox iconDebugToggle;
    private javax.swing.JCheckBox iconFullToggle;
    private javax.swing.JCheckBox iconHiddenToggle;
    private javax.swing.JCheckBox iconReadOnlyToggle;
    private javax.swing.JButton indexOfIDButton;
    private javax.swing.JCheckBox isEditedToggle;
    private javax.swing.JButton isSelIDButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField linkField;
    private javax.swing.JButton listAddButton;
    private javax.swing.JCheckBox listEditedDisplay;
    private javax.swing.JSpinner listIDSpinner;
    private javax.swing.JLabel listIconTestLabel;
    private javax.swing.JButton listInsertButton;
    private javax.swing.JButton listManageButton;
    private javax.swing.JTextField listNameField;
    private javax.swing.JCheckBoxMenuItem listSelListenerToggle;
    private manager.links.LinksListTabsManipulator listTabsManipulator;
    private manager.links.LinksListTabsPanel listTabsPanel;
    private javax.swing.JCheckBox listsEnabledToggle;
    private javax.swing.JSpinner newListIDSpinner;
    private javax.swing.JCheckBox newListIDToggle;
    private javax.swing.JMenu populateMenu;
    private javax.swing.JButton printButton;
    private javax.swing.JButton printIconButton;
    private javax.swing.JMenu printMenu;
    private javax.swing.JMenu privateMenu;
    private javax.swing.JCheckBoxMenuItem propChangeListenerToggle;
    private javax.swing.JMenu readOnlyMenu;
    private javax.swing.JButton refreshTablesButton;
    private javax.swing.JMenu removeDupMenu;
    private javax.swing.JButton removeListButton;
    private javax.swing.JMenu selListMenu;
    private javax.swing.JButton selectIDButton;
    private javax.swing.JButton setListIDButton;
    private javax.swing.JSpinner setListIDSpinner;
    private javax.swing.JCheckBox showPrivateToggle;
    private javax.swing.JCheckBox structEditedToggle;
    private javax.swing.JLabel tabManipListIDLabel;
    // End of variables declaration//GEN-END:variables
}
