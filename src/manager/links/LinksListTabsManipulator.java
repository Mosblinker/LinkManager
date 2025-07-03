/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import components.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.*;
import java.util.*;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Position;
import manager.LinkManager;

/**
 *
 * @author Milo Steier
 */
public class LinksListTabsManipulator extends JListManipulator<LinksListModel>{
    /**
     * This identifies that the create list button has been set to be shown or 
     * hidden.
     */
    public static final String CREATE_BUTTON_IS_SHOWN_PROPERTY_CHANGED = 
            "CreateButttonIsShownPropertyChanged";
    /**
     * This identifies that the rename list button has been set to be shown or 
     * hidden.
     */
    public static final String RENAME_BUTTON_IS_SHOWN_PROPERTY_CHANGED = 
            "RenameButttonIsShownPropertyChanged";
    
    public static final String HIDE_TOGGLE_BUTTON_IS_SHOWN_PROPERTY_CHANGED = 
            "HideToggleButttonIsShownPropertyChanged";
    
    public static final String READ_ONLY_CHECK_BOX_IS_SHOWN_PROPERTY_CHANGED = 
            "ReadOnlyToggleButttonIsShownPropertyChanged";
    
    public static final String SIZE_LIMIT_SETTINGS_ARE_SHOWN_PROPERTY_CHANGED = 
            "SizeLimitSettingsAreShownPropertyChanged";
    
    private static final int CREATE_BUTTON_SHOWN_FLAG = 0x01;
    
    private static final int RENAME_BUTTON_SHOWN_FLAG = 0x02;
    
    private static final int HIDE_TOGGLE_SHOWN_FLAG = 0x04;
    
    private static final int SIZE_LIMIT_SHOWN_FLAG = 0x08;
    
    private static final int READ_ONLY_TOGGLE_SHOWN_FLAG = 0x10;
    
    private static final int DEFAULT_SHOWN_FLAGS = 
            CREATE_BUTTON_SHOWN_FLAG | RENAME_BUTTON_SHOWN_FLAG | 
            HIDE_TOGGLE_SHOWN_FLAG | SIZE_LIMIT_SHOWN_FLAG;
    /**
     * This is the instruction to create and add a new item to the list.
     */
    public static final String CREATE_NEW_ITEM = "CreateNewItem";
    /**
     * This is the instruction to rename the selected items, if any, in the 
     * list.
     */
    public static final String RENAME_SELECTION = "RenameSelection";
    
    public static final String HIDE_SELECTION = "HideSelection";
    
    public static final String TOGGLE_SIZE_LIMIT = "ToggleSizeLimit";
    
    public static final String SET_SIZE_LIMIT = "SetSizeLimit";
    
    public static final String TOGGLE_READ_ONLY = "ToggleReadOnly";
    /**
     * The prompt to display on the dialog used to get the name for a new item.
     */
    private static final String CREATE_NEW_ITEM_PROMPT = 
            "Enter the name for the new list:";
    /**
     * The prompt to display on the dialog used to get the new name for an 
     * existing item.
     */
    private static final String RENAME_ITEM_PROMPT = 
            "Enter the new name for the list:";
    /**
     * This initializes the components for this list manager.
     */
    private void initialize(){
        setMinimumSize(new Dimension(480, 420));
        setPreferredSize(new Dimension(480, 420));
        renameMap = new HashMap<>();
        hiddenMap = new HashMap<>();
        sizeLimitMap = new HashMap<>();
        readOnlyMap = new HashMap<>();
            // Create a handler to listen to the buttons
        Handler handler = new Handler();
        
        createButton = new JButton("Create");
        createButton.setActionCommand(CREATE_NEW_ITEM);
        createButton.addMouseListener(getDisabledComponentListener());
        createButton.addActionListener(handler);
        addSideComponent(createButton,LAST_RESERVED_BUTTON_ROW+1,getCreateButtonIsShown());
        
        renameButton = new JButton("Rename");
        renameButton.setActionCommand(RENAME_SELECTION);
        renameButton.addMouseListener(getDisabledComponentListener());
        renameButton.addActionListener(handler);
        addSideComponent(renameButton,LAST_RESERVED_BUTTON_ROW+2,getRenameButtonIsShown());
        
        hideToggle = new JToggleButton("Hidden");
        hideToggle.setActionCommand(HIDE_SELECTION);
        hideToggle.addMouseListener(getDisabledComponentListener());
        hideToggle.addActionListener(handler);
        addSideComponent(hideToggle,LAST_RESERVED_BUTTON_ROW+3,getHideToggleButtonIsShown());
        
        sizeLimitToggle = new JCheckBox("Size Limit:");
        sizeLimitToggle.setActionCommand(TOGGLE_SIZE_LIMIT);
        sizeLimitToggle.addMouseListener(getDisabledComponentListener());
        sizeLimitToggle.addActionListener(handler);
        
        sizeLimitSpinner = new JSpinner(new SpinnerNumberModel(10000, 1, null, 100));
        sizeLimitSpinner.addChangeListener(handler);
        sizeLimitSpinner.addMouseListener(getDisabledComponentListener());
        sizeLimitSpinner.setMinimumSize(new Dimension(70,22));
        sizeLimitSpinner.setMaximumSize(new Dimension(70,32767));
        sizeLimitSpinner.setPreferredSize(new Dimension(70,22));
        
        sizeLimitButton = new JButton("Apply Limit");
        sizeLimitButton.setActionCommand(SET_SIZE_LIMIT);
        sizeLimitButton.addMouseListener(getDisabledComponentListener());
        sizeLimitButton.addActionListener(handler);
        
        updateSizeLimitSpinnerEnabled();
        
        bottomLeftPanel = new JPanel();
        bottomLeftPanel.setLayout(new BoxLayout(bottomLeftPanel,BoxLayout.X_AXIS));
        bottomLeftPanel.addMouseListener(getDisabledComponentListener());
        
        sizeLimitPanel = new JPanel(new BorderLayout(6,0));
        sizeLimitPanel.add(sizeLimitToggle,BorderLayout.LINE_START);
        sizeLimitPanel.add(sizeLimitSpinner, BorderLayout.CENTER);
        sizeLimitPanel.add(sizeLimitButton,BorderLayout.LINE_END);
        bottomLeftPanel.add(sizeLimitPanel);
        sizeLimitPanel.setEnabled(getSizeLimitSettingsAreShown());
        
        sizeLimitReadOnlyFiller = new Box.Filler(
                new Dimension(6, 0), 
                new Dimension(6, 0), 
                new Dimension(6, 0));
        bottomLeftPanel.add(sizeLimitReadOnlyFiller);
        
        readOnlyToggle = new JCheckBox("Read Only");
        readOnlyToggle.setActionCommand(TOGGLE_READ_ONLY);
        readOnlyToggle.addMouseListener(getDisabledComponentListener());
        readOnlyToggle.addActionListener(handler);
        bottomLeftPanel.add(readOnlyToggle);
        
        bottomPanel.add(bottomLeftPanel,BorderLayout.LINE_START);
        
        setCellRenderer(new ModelNameCellRenderer());
        
        listNamePane = new JOptionPane("Enter the name for the list:",
                JOptionPane.QUESTION_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        listNamePane.setWantsInput(true);
        listNamePane.setPreferredSize(new Dimension(560, 120));
        listNamePane.setMinimumSize(new Dimension(560, 120));
        listNamePane.setMaximumSize(new Dimension(32769, 120));
        
        updateListButtonsEnabled();
    }
    /**
     * This constructs a LinksListTabsManipulator.
     */
    public LinksListTabsManipulator(){
        super("Manage Lists...");
        initialize();
    }
    /**
     * This returns the button used to create and add a new item to the list.
     * @return The create button.
     * @see #CREATE_NEW_ITEM
     * @see #createList() 
     */
    public JButton getCreateButton(){
        return createButton;
    }
    /**
     * This returns the button used to rename the selected item in the list.
     * @return The rename button.
     * @see #RENAME_SELECTION
     * @see #renameList() 
     */
    public JButton getRenameButton(){
        return renameButton;
    }
    
    public JToggleButton getHideToggleButton() {
        return hideToggle;
    }
    
    public JCheckBox getReadOnlyCheckBox() {
        return readOnlyToggle;
    }
    
    public JCheckBox getSizeLimitCheckBox() {
        return sizeLimitToggle;
    }
    
    public JSpinner getSizeLimitSpinner(){
        return sizeLimitSpinner;
    }
    
    public JButton getSizeLimitButton(){
        return sizeLimitButton;
    }
    /**
     * This sets whether the create button will be shown by this panel. This 
     * property is true by default.
     * @param value Whether the create button should be shown or not.
     * @see #getCreateButtonIsShown() 
     * @see #getCreateButton() 
     */
    public void setCreateButtonIsShown(boolean value){
        if (value == getCreateButtonIsShown())        // If no change will occur
            return;
        setFlag(CREATE_BUTTON_SHOWN_FLAG,value);
        firePropertyChange(CREATE_BUTTON_IS_SHOWN_PROPERTY_CHANGED,!value,value);
        createButton.setVisible(value);
    }
    /**
     * This returns whether the create button is shown by this panel.
     * @return Whether the create button is shown.
     * @see #setCreateButtonIsShown(boolean) 
     * @see #getCreateButton() 
     */
    public boolean getCreateButtonIsShown(){
        return getFlag(CREATE_BUTTON_SHOWN_FLAG);
    }
    /**
     * This sets whether the rename button will be shown by this panel. This 
     * property is true by default.
     * @param value Whether the rename button should be shown or not.
     * @see #getRenameButtonIsShown() 
     * @see #getRenameButton() 
     */
    public void setRenameButtonIsShown(boolean value){
        if (value == getRenameButtonIsShown())    // If no change will occur
            return;
        setFlag(RENAME_BUTTON_SHOWN_FLAG,value);
        firePropertyChange(RENAME_BUTTON_IS_SHOWN_PROPERTY_CHANGED,!value,value);
        renameButton.setVisible(value);
    }
    /**
     * This returns whether the rename button is shown by this panel.
     * @return Whether the rename button is shown.
     * @see #setRenameButtonIsShown(boolean) 
     * @see #getRenameButton() 
     */
    public boolean getRenameButtonIsShown(){
        return getFlag(RENAME_BUTTON_SHOWN_FLAG);
    }
    /**
     * This sets whether the hide toggle button will be shown by this panel. This 
     * property is true by default.
     * @param value Whether the hide toggle button should be shown or not.
     * @see #getHideToggleButtonIsShown() 
     * @see #getHideToggleButton() 
     */
    public void setHideToggleButtonIsShown(boolean value){
        if (value == getHideToggleButtonIsShown())        // If no change will occur
            return;
        setFlag(HIDE_TOGGLE_SHOWN_FLAG,value);
        firePropertyChange(HIDE_TOGGLE_BUTTON_IS_SHOWN_PROPERTY_CHANGED,!value,value);
        hideToggle.setVisible(value);
    }
    /**
     * This returns whether the hide toggle button is shown by this panel.
     * @return Whether the hide toggle button is shown.
     * @see #setHideToggleButtonIsShown(boolean) 
     * @see #getHideToggleButton() 
     */
    public boolean getHideToggleButtonIsShown(){
        return getFlag(HIDE_TOGGLE_SHOWN_FLAG);
    }
    /**
     * This sets whether the read only check box will be shown by this panel. This 
     * property is true by default.
     * @param value Whether the read only check box should be shown or not.
     * @see #getReadOnlyCheckBoxIsShown() 
     * @see #getReadOnlyCheckBox() 
     */
    public void setReadOnlyCheckBoxIsShown(boolean value){
        if (value == getReadOnlyCheckBoxIsShown())        // If no change will occur
            return;
        setFlag(READ_ONLY_TOGGLE_SHOWN_FLAG,value);
        firePropertyChange(READ_ONLY_CHECK_BOX_IS_SHOWN_PROPERTY_CHANGED,!value,value);
        readOnlyToggle.setVisible(value);
        sizeLimitReadOnlyFiller.setVisible(sizeLimitPanel.isVisible() && readOnlyToggle.isVisible());
    }
    /**
     * This returns whether the hide toggle button is shown by this panel.
     * @return Whether the hide toggle button is shown.
     * @see #setReadOnlyCheckBoxIsShown(boolean) 
     * @see #getReadOnlyCheckBox() 
     */
    public boolean getReadOnlyCheckBoxIsShown(){
        return getFlag(READ_ONLY_TOGGLE_SHOWN_FLAG);
    }
    /**
     * This sets whether the size limit settings will be shown by this panel. 
     * This property is true by default.
     * @param value Whether the size limit settings should be shown or not.
     * @see #getSizeLimitSettingsAreShown() 
     * @see #getSizeLimitCheckBox() 
     * @see #getSizeLimitSpinner()
     */
    public void setSizeLimitSettingsAreShown(boolean value){
        if (value == getRenameButtonIsShown())    // If no change will occur
            return;
        setFlag(SIZE_LIMIT_SHOWN_FLAG,value);
        firePropertyChange(SIZE_LIMIT_SETTINGS_ARE_SHOWN_PROPERTY_CHANGED,!value,value);
        sizeLimitPanel.setVisible(value);
        sizeLimitReadOnlyFiller.setVisible(sizeLimitPanel.isVisible() && readOnlyToggle.isVisible());
    }
    /**
     * This returns whether the size limit settings are shown by this panel.
     * @return Whether the size limit settings are shown.
     * @see #setSizeLimitSettingsAreShown(boolean) 
     * @see #getSizeLimitCheckBox() 
     * @see #getSizeLimitSpinner()
     */
    public boolean getSizeLimitSettingsAreShown(){
        return getFlag(SIZE_LIMIT_SHOWN_FLAG);
    }
    /**
     * This returns a map that maps LinksListModels that have been renamed 
     * using this list manager to their new names. Only LinksListModels that 
     * have been renamed will be in this map.
     * @return A map that maps LinksListModels to their new names.
     */
    public Map<LinksListModel, String> getRenamedListMap(){
        updateRenamedListMap();
        return new HashMap<>(renameMap);
    }
    /**
     * This updates the map storing the new names for renamed LinksListModels, 
     * removing any entries for LinksListModels that are not in the list's 
     * model, entries where the new name for the LinksListModel is the same as 
     * the current name for that LinksListModel, and entries where the new 
     * name is null.
     */
    protected void updateRenamedListMap(){
        if (renameMap == null)  // If the rename map has yet to be initialized
            return;
            // Get the list's model list
        ListModelList<LinksListModel> model = getModelList();
        renameMap.entrySet().removeIf((Map.Entry<LinksListModel, String> entry) -> 
                    // If the list of links model is null or not in the model list
                    // or if the new name is null or the same as the old name
                entry.getKey() == null || !model.contains(entry.getKey()) || 
                entry.getValue() == null ||
                entry.getValue().equals(entry.getKey().getListName()) 
        );
//            // An iterator to go through the entries in the map
//        Iterator<Map.Entry<LinksListModel,String>> itr = 
//                renameMap.entrySet().iterator();
//            // A while loop to go through the entries in the map
//        while(itr.hasNext()){
//                // Get the current entry in the map
//            Map.Entry<LinksListModel,String> entry = itr.next();
//                // If the list of links model is null or not in the model list 
//                // or if the new name is null or the same as the old name
//            if (entry.getKey() == null || !model.contains(entry.getKey()) || 
//                    entry.getValue() == null || 
//                    entry.getValue().equals(entry.getKey().getListName()))
//                itr.remove();   // Remove the entry
//        }
    }
    
    public Map<LinksListModel, Boolean> getHiddenListMap(){
        updateHiddenListMap();
        return new HashMap<>(hiddenMap);
    }
    
    protected void updateHiddenListMap(){
        if (hiddenMap == null)  // If the rename map has yet to be initialized
            return;
            // Get the list's model list
        ListModelList<LinksListModel> model = getModelList();
        hiddenMap.entrySet().removeIf((Map.Entry<LinksListModel, Boolean> entry) -> 
                    // If the list of links model is null or not in the model list
                    // or if the new name is null or the same as the old name
                entry.getKey() == null || !model.contains(entry.getKey()) || 
                entry.getValue() == null || entry.getValue() == entry.getKey().isHidden()
        );
    }
    
    public Map<LinksListModel, Integer> getSizeLimitListMap(){
        updateSizeLimitListMap();
        return new HashMap<>(sizeLimitMap);
    }
    
    protected void updateSizeLimitListMap(){
        if (sizeLimitMap == null)  // If the rename map has yet to be initialized
            return;
            // Get the list's model list
        ListModelList<LinksListModel> model = getModelList();
        sizeLimitMap.entrySet().removeIf((Map.Entry<LinksListModel, Integer> entry) -> 
                    // If the list of links model is null or not in the model list
                    // or if the new name is null or the same as the old name
                entry.getKey() == null || !model.contains(entry.getKey()) || 
                        Objects.equals(entry.getValue(), entry.getKey())
        );
    }
    
    protected void updateListMaps(){
        updateRenamedListMap();
        updateHiddenListMap();
        updateSizeLimitListMap();
    }
    /**
     * This returns the name to use for the given model.
     * @param model The model to get the name for.
     * @return The name to display for the model, or null if the model is null.
     */
    protected String getNameForModel(LinksListModel model){
        if (model == null)      // If the model is null
            return null;
        if (renameMap != null)  // If the rename map has been initialized
            return renameMap.getOrDefault(model, model.getListName());
        else
            return model.getListName();
    }
    
    protected void clearListMaps(){
        if (renameMap != null)  // If the rename map has been initialized
            renameMap.clear();
        if (hiddenMap != null)
            hiddenMap.clear();
        if (sizeLimitMap != null)
            sizeLimitMap.clear();
    }
    @Override
    public void setModel(ListModel<LinksListModel> model){
        super.setModel(model);
//        addUsedNames(getModelList());
        clearListMaps();
    }
    /**
     * This opens a pop up that the user can use to enter in a name for a list. 
     * This can be used for getting the name for a new list or getting the new 
     * name for a list that is being renamed. If a LinksListModel is provided, 
     * then this will get the new name for that list. Otherwise, this will get 
     * the name for a new list. The name returned will not be blank, contain 
     * asterisks, or be the same name as another list.
     * @param model The list model to get the new name for, or null to get the 
     * name for a new list model.
     * @return The new name for the model, or null to cancel creating/renaming 
     * a list model.
     */
    protected String showListNameDialog(LinksListModel model){
        String title;   // This gets the title for the dialog
        String prompt;  // This gets the prompt for the dialog
            // This gets the current name of the model, or null if no model was 
        String oldName = getNameForModel(model);    // given
        if (model == null){ // If no model was provided (creating a new list)
            title = "Create New List";
            prompt = CREATE_NEW_ITEM_PROMPT;
        }
        else{               // If a model was provided (renaming a list)
            title = "Rename List \""+oldName+"\"";
            prompt = RENAME_ITEM_PROMPT;
        }
        listNamePane.setMessage(prompt);
        listNamePane.setInitialSelectionValue(null);
        listNamePane.setInitialSelectionValue(oldName);
            // Create a dialog to display the option pane used to enter the name
        JDialog dialog = listNamePane.createDialog(this, title);
            // This gets the name that was entered by the user.
        String name = null;
            // This stores whether the name entered by the user is valid (i.e. a 
        boolean valid;  // non-blank name not currently used by any other list)
        do{
            valid = true;
            dialog.setVisible(true);    // Show the dialog
                // Get the option selected by the user
            Object option = listNamePane.getValue();
                // This gets the message to display if there is an issue with 
            String msg = null;  // the name
                // If the option is a number, and OK was selected
            if (option instanceof Number && 
                    ((Number) option).equals(JOptionPane.OK_OPTION)){
                    // This gets the name from the user's input
                name = (String)listNamePane.getInputValue();
                    // If the entered name is null or blank
                if (name == null || name.isBlank()){
                    valid = false;
                    msg = "The list name cannot be blank.";
                }   // If the name contains an asterisk
                else if (name.contains("*")){
                    valid = false;
                    msg = "The list name cannot contain an asterisk(*).";
                }
                else{
                    name = name.trim(); // Trim the name
                        // If a model was provided and the entered name is the 
                        // same as the current name for the model
                    if (model != null && name.equals(oldName)){
                        name = null;
                    }
                    else if (getUsedNames().contains(name)){
                        valid = false;
                        msg = "The list name \""+name+"\" is already in use.";
                    }
                    else{   // Go through the list models 
                        for (LinksListModel temp : getModelList()){
                                // If the name of this list model is the same as 
                                // the entered name (we've already checked the 
                                // given model and confirmed it's not the same 
                                // as the old name for that model)
                            if (name.equals(getNameForModel(temp))){
                                valid = false;
                                msg = "There is already a list with the name \""
                                        +name+"\"";
                                break;
                            }
                        }
                    }
                }
            }
            else
                name = null;    // No change will be made
                // If the name is not valid and a message is to be displayed
            if (!valid && msg != null){
                JOptionPane.showMessageDialog(this,msg,"Invalid List Name",
                        JOptionPane.WARNING_MESSAGE);
            }
        }
        while (!valid);     // While the name entered is not valid
        dialog.dispose();   // Dispose of the dialog
        return name;
    }
    /**
     * This pops up a dialog for the user to enter the name for a new list and, 
     * if one is entered, this will create a new list. This method is called 
     * when the create button is pressed.
     * @see #getCreateButton() 
     * @see #CREATE_NEW_ITEM
     */
    public void createList(){
            // Get the name for the new list
        String name = showListNameDialog(null);
        if (name != null){  // If a name was provided
                // Create a new list with the given name and a listID of null
            LinksListModel newList = new LinksListModel();
                // Set the name separately so the list considers itself to be 
            newList.setListName(name);  // edited
            getModelList().add(newList);
//                // Select the new list
//            setSelectedIndex(getModelList().size()-1);
        }
    }
    /**
     * This pops up a dialog for the user to enter the new name for the 
     * currently selected list and, if one is entered, this will rename the 
     * selected list. If nothing is selected or more than one thing is selected, 
     * then this will do nothing. This method is called when the rename button 
     * is pressed.
     * @see #getRenameButton() 
     * @see #RENAME_SELECTION
     */
    public void renameList(){
            // If there is nothing selected or more than one thing selected
        if (getSelectedItemsCount()!=1)
            return;
            // Get the selected list panel
        LinksListModel sel = getSelectedValue();
            // Get the new name for the selected list model
        String name = showListNameDialog(sel);
        if (name != null){  // If a new name was entered
                // If the new name is the same as the original name (list is no 
            if (name.equals(sel.getListName())) // longer being renamed)
                renameMap.remove(sel);      // Remove the list from the rename map
                // If the selected list is a new list added by this
            else if (!getInitialData().contains(sel))
                    // No point in adding it to the rename map, since the list 
                sel.setListName(name);  // is being created
            else    // Insert the list into the rename map
                renameMap.put(sel, name);
            getList().repaint();    // Repaint the list
        }
    }
    
    public void hideLists(){
        if (isSelectionEmpty())
            return;
        for (LinksListModel model : getSelectedValuesList()){
            hiddenMap.put(model, hideToggle.isSelected());
        }
    }
    
    public void setListsReadOnly(){
        if (isSelectionEmpty())
            return;
        for (LinksListModel model : getSelectedValuesList()){
            readOnlyMap.put(model, readOnlyToggle.isSelected());
        }
    }
    
    protected void setListSizeLimit(LinksListModel model){
        if (model != null)
            sizeLimitMap.put(model, (sizeLimitToggle.isSelected()) ? 
                    (Integer) sizeLimitSpinner.getValue() : null);
    }
    
    public void setListSizeLimit(){
        updateSizeLimitSpinnerEnabled();
            // If there is nothing selected or more than one thing selected
        if (getSelectedItemsCount()!=1)
            return;
        setListSizeLimit(getSelectedValue());
    }
    /**
     * This applies the changes made to the lists to the given {@code 
     * LinksListTabsPanel}.
     * @param listTabsPanel 
     */
    public void updateListTabs(LinksListTabsPanel listTabsPanel){
        listTabsPanel.setModels(getListData(),true);
        updateListMaps();
        for (LinksListModel model : renameMap.keySet()){
            model.setListName(renameMap.get(model));
        }
        for (LinksListModel model : hiddenMap.keySet()){
            model.setHidden(hiddenMap.get(model));
        }
        for (LinksListModel model : sizeLimitMap.keySet()){
            model.setSizeLimit(sizeLimitMap.get(model));
        }
        for (LinksListModel model : readOnlyMap.keySet()){
            model.setReadOnly(readOnlyMap.get(model));
        }
    }
    @Override
    public void reset(){
        clearListMaps();
        super.reset();
    }
    @Override
    public void accept(){
        updateListMaps();
        super.accept();
    }
    
    public Set<String> getUsedNames(){
        if (usedNames == null)
            usedNames = new HashSet<>();
        return usedNames;
    }
    
    public void addUsedNames(Collection<LinksListModel> models){
        for (LinksListModel model : models){
            getUsedNames().add(model.getListName());
        }
    }
    
    public void addUsedNames(LinksListTabsPanel tabsPanel){
        addUsedNames(tabsPanel.getModels());
    }
    /**
     * This returns a comparator that can be used to sort a list of 
     * LinksListModels based off where they were in the model when the 
     * comparator was constructed. Any LinksListModels that were not in the 
     * model will end up at the end of the list and sorted via their {@code 
     * compareTo} method. Any changes made to the structure of the model will 
     * not be reflected in the comparator. 
     * @return A comparator that can be used to rearrange a list of 
     * LinksListModels to reflect the order they are in the model.
     */
    public Comparator<LinksListModel> getIndexComparator(){
        return new ModelIndexComparator();
    }
    /**
     * This returns a comparator that can be used to sort a list of 
     * LinksListPanels based off where their models were in the model when the 
     * comparator was constructed. Any LinksListPanels with models that were 
     * not in the model will end up at the end of the list and sorted via their 
     * {@code compareTo} method. Any changes made to the structure of the model 
     * will not be reflected in the comparator. 
     * @return A comparator that can be used to rearrange a list of 
     * LinksListPanels to reflect the order they are in the model.
     */
    public Comparator<LinksListPanel> getPanelComparator(){
        return new PanelIndexComparator();
    }
    /**
     * This returns a String representation of this LinksListTabsManipulator. 
     * This method is primarily intended to be used only for debugging purposes, 
     * and the content and format of the returned String may vary between 
     * implementations.
     * @return A String representation of this LinksListTabsManipulator.
     */
    @Override
    protected String paramString(){
        return super.paramString()+
                "createButtonShown="+getCreateButtonIsShown()+
                "renameButtonShown="+getRenameButtonIsShown();
    }
    
    private void setFlag(int flag, boolean value){
        if (value)
            showFlags |= flag;
        else
            showFlags &= ~flag;
    }
    
    private boolean getFlag(int flag){
        return (showFlags & flag) == flag;
    }
    
    @Override
    protected void moveElements(int[] indexes, Position.Bias direction,
            int distance, ListModelList<LinksListModel> model){
        boolean update = skipUpdate;
        skipUpdate = true;
        super.moveElements(indexes, direction, distance, model);
        skipUpdate = update;
    }
    @Override
    protected void moveElementsToBoundary(int[] indexes,Position.Bias direction,
            ListModelList<LinksListModel> model){
        boolean update = skipUpdate;
        skipUpdate = true;
        super.moveElementsToBoundary(indexes, direction, model);
        skipUpdate = update;
    }
    @Override
    protected void reverseElements(int[] indexes, ListModelList<LinksListModel> model){
        boolean update = skipUpdate;
        skipUpdate = true;
        super.reverseElements(indexes, model);
        skipUpdate = update;
    }
    /**
     * Whether the list maps should not be updated if the list model is changed 
     * in some way.
     */
    private boolean skipUpdate = false;
    
    private Set<String> usedNames = null;
    /**
     * This is a map that maps list models that have been renamed to their new 
     * name. 
     */
    protected Map<LinksListModel,String> renameMap = null;
    
    protected Map<LinksListModel,Boolean> hiddenMap = null;
    
    protected Map<LinksListModel,Integer> sizeLimitMap = null;
    
    protected Map<LinksListModel,Boolean> readOnlyMap = null;
    /**
     * This controls what buttons and things are to be shown.
     */
    private int showFlags = DEFAULT_SHOWN_FLAGS;
    /**
     * This is the button used to create and add a new list.
     */
    protected JButton createButton;
    /**
     * This is the button used to rename the selected list.
     */
    protected JButton renameButton;
    
    protected JToggleButton hideToggle;
    
    protected JCheckBox readOnlyToggle;
    
    protected Box.Filler sizeLimitReadOnlyFiller;
    
    protected JCheckBox sizeLimitToggle;
    
    protected JSpinner sizeLimitSpinner;
    
    protected JButton sizeLimitButton;
    
    protected JPanel sizeLimitPanel;
    
    protected JPanel bottomLeftPanel;
    /**
     * This is the JOptionPane used to enter names for lists. This is used both 
     * when creating a new list and renaming an existing list.
     */
    protected JOptionPane listNamePane;
    
    @Override
    protected void fireSelectionChanged(int firstIndex, int lastIndex, 
            boolean isAdjusting){
        try{
            if (!isAdjusting){
                List<LinksListModel> selectedList = getSelectedValuesList();
                boolean hidden = false;
                for (LinksListModel model : selectedList){
                    hidden = hiddenMap.getOrDefault(model, model.isHidden());
                    if (hidden)
                        break;
                }
                boolean readOnly = true;
                for (LinksListModel model : selectedList){
                    readOnly = readOnlyMap.getOrDefault(model, model.isReadOnly());
                    if (!readOnly)
                        break;
                }
                hideToggle.setSelected(hidden);
                readOnlyToggle.setSelected(readOnly);
                if (getSelectedItemsCount()==1){
                    LinksListModel model = getSelectedValue();
                    Integer sizeLimit = sizeLimitMap.getOrDefault(model, model.getSizeLimit());
                    sizeLimitToggle.setSelected(sizeLimit != null);
                    if (sizeLimit != null)
                        sizeLimitSpinner.setValue(sizeLimit);
                }
            }
        }
        catch(NullPointerException ex){
            LinkManager.getLogger().log(Level.WARNING, 
                    "Null encountered when selection changed", ex);
        }
        super.fireSelectionChanged(firstIndex, lastIndex, isAdjusting);
    }
    @Override
    protected void updateListButtonsEnabled(){
        super.updateListButtonsEnabled();
        try{
            createButton.setEnabled(isEnabled());
            renameButton.setEnabled(isEnabled() && getSelectedItemsCount()==1);
            hideToggle.setEnabled(isEnabled() && !isSelectionEmpty());
            sizeLimitToggle.setEnabled(renameButton.isEnabled());
            updateSizeLimitSpinnerEnabled();
            sizeLimitButton.setEnabled(sizeLimitToggle.isEnabled());
            readOnlyToggle.setEnabled(hideToggle.isEnabled());
        }
        catch(NullPointerException ex){}
    }
    
    protected void updateSizeLimitSpinnerEnabled(){
        sizeLimitSpinner.setEnabled(sizeLimitToggle.isEnabled() && sizeLimitToggle.isSelected());
    }
    @Override
    protected void fireIntervalRemoved(int index0, int index1){
            // If this should update the maps
        if (!skipUpdate)
            updateListMaps();
        super.fireIntervalRemoved(index0, index1);
    }
    @Override
    protected void fireContentsChanged(int index0, int index1){
            // If this should update the maps
        if (!skipUpdate)
            updateListMaps();
        super.fireContentsChanged(index0, index1);
    }
    /**
     * This is the handler used to listen to the list buttons and perform their 
     * respective actions.
     */
    private class Handler implements ActionListener, ChangeListener{
        @Override
        public void actionPerformed(ActionEvent evt) {
                // Get the action command for the action event
            String command = evt.getActionCommand();
            if (command == null)                // If no action command was given
                return;
            switch(command){
                case(RENAME_SELECTION):         // If the rename item command was given
                    renameList();
                    return;
                case(CREATE_NEW_ITEM):          // If the create new item command was given
                    createList();
                    break;
                case(HIDE_SELECTION):
                    hideLists();
                    break;
                case(TOGGLE_SIZE_LIMIT):
                    updateSizeLimitSpinnerEnabled();
                    break;
                case(SET_SIZE_LIMIT):
                    setListSizeLimit();
                    break;
                case(TOGGLE_READ_ONLY):
                    setListsReadOnly();
            }
        }
        @Override
        public void stateChanged(ChangeEvent evt) {
            
        }
    }
    /**
     * This is a List Cell Renderer used to render the names of the list models.
     */
    private class ModelNameCellRenderer extends DefaultListCellRenderer{
        @Override
        public Component getListCellRendererComponent(JList list, Object value, 
                int index, boolean isSelected, boolean cellHasFocus){
                // If the value is a LinksListModel
            if (value instanceof LinksListModel){
                value = getNameForModel((LinksListModel) value);
            }
            return super.getListCellRendererComponent(list, value, index, 
                    isSelected, cellHasFocus);
        }
    }
    /**
     * This is a comparator that sorts LinksListModels by order in which they 
     * appeared in the model when this comparator was constructed. 
     * LinksListModels that were not in the model are moved to the back and 
     * sorted by their {@code compareTo} method.
     */
    private class ModelIndexComparator implements Comparator<LinksListModel>{
        /**
         * The list containing the LinksListModels in the model.
         */
        private final List<LinksListModel> panelList;
        /**
         * This constructs a ModelIndexComparator.
         */
        public ModelIndexComparator(){
            panelList = getListData();
        }
        @Override
        public int compare(LinksListModel o1, LinksListModel o2) {
                // Get the index of the first panel
            int index1 = panelList.indexOf(o1);
                // Get the index of the second panel
            int index2 = panelList.indexOf(o2);
                // If the indexes for the panels are the same or they are both 
                // not contained within the list
            if (index1 == index2 || (index1 < 0 && index2 < 0)){
                if (o1 == null && o2 == null)   // If the panels are both null
                    return 0;
                else if (o1 != null)            // If the first panel is not null
                    return o1.compareTo(o2);
                else    // Invert the result of the second panel's compareTo 
                        // method to get the result in relation to the first one
                    return -1*o2.compareTo(o1);
            }
            else if (index1 < 0)    // If the first panel is not in the list
                return 1;
            else if (index2 < 0)    // If the second panel is not in the list
                return -1;
            return Integer.compare(index1, index2);
        }
    }
    /**
     * This is a comparator that sorts LinksListPanels by order in which they 
     * appeared in the model when this comparator was constructed. 
     * LinksListPanels that were not in the model are moved to the back and 
     * sorted by their {@code compareTo} method. This is effectively a 
     * ModelIndexComparator that compares LinksListPanels by their models.
     */
    private class PanelIndexComparator implements Comparator<LinksListPanel>{
        /**
         * The ModelIndexComparator to use to compare the models of 
         * LinksListPanels.
         */
        private final Comparator<LinksListModel> modelComparator;
        /**
         * This constructs a PanelIndexComparator.
         */
        public PanelIndexComparator(){
            modelComparator = getIndexComparator();
        }
        @Override
        public int compare(LinksListPanel o1, LinksListPanel o2) {
                // If both panels are null
            if (o1 == null && o2 == null)
                return 0;
            else if (o1 == null)    // If the first panel is null
                return 1;
            else if (o2 == null)    // If the second panel is null
                return -1;
                // Get the comparison between the two panel's models
            int value = modelComparator.compare(o1.getModel(), o2.getModel());
                // If the result of the comparison is not zero, return it. 
                // Otherwise, use the panel's compareTo method.
            return (value != 0) ? value : o1.compareTo(o2);
        }
    }
}
