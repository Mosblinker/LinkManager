/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import java.awt.Component;
import java.awt.Dimension;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

/**
 *
 * @author Mosblinker
 */
public class ListNameOptionPane extends JOptionPane{
    /**
     * 
     */
    private static final LinksListNameProvider DEFAULT_NAME_PROVIDER = 
            new LinksListNameProvider(){};
    /**
     * 
     */
    public static final String LINKS_LIST_NAME_PROVIDER_PROPERTY = 
            "LinksListNameProvider";
    /**
     * The prompt to display on the dialog used to get the name for a new item.
     */
    protected static final String CREATE_NEW_ITEM_PROMPT = 
            "Enter the name for the new list:";
    /**
     * The prompt to display on the dialog used to get the new name for an 
     * existing item.
     */
    protected static final String RENAME_ITEM_PROMPT = 
            "Enter the new name for the list:";
    /**
     * 
     */
    private LinksListNameProvider nameProvider = null;
    /**
     * 
     */
    private Set<String> usedNames = new HashSet<>();
    /**
     * 
     */
    private Set<LinksListModel> models = new HashSet<>();
    /**
     * 
     * @param message
     * @param nameProvider 
     */
    public ListNameOptionPane(String message,LinksListNameProvider nameProvider){
        super(message,JOptionPane.QUESTION_MESSAGE,JOptionPane.OK_CANCEL_OPTION);
        super.setWantsInput(true);
        super.setPreferredSize(new Dimension(560, 120));
        super.setMinimumSize(new Dimension(560, 120));
        super.setMaximumSize(new Dimension(32769, 120));
        this.nameProvider = nameProvider;
    }
    /**
     * 
     * @param nameProvider 
     */
    public ListNameOptionPane(LinksListNameProvider nameProvider){
        this("Enter the name for the list:",nameProvider);
    }
    /**
     * 
     * @param message 
     */
    public ListNameOptionPane(String message){
        this(message,null);
    }
    /**
     * 
     */
    public ListNameOptionPane(){
        this((LinksListNameProvider)null);
    }
    /**
     * 
     * @return 
     */
    public LinksListNameProvider getNameProvider(){
        return (nameProvider != null) ? nameProvider : DEFAULT_NAME_PROVIDER;
    }
    /**
     * 
     * @return 
     */
    public boolean isNameProviderSet(){
        return nameProvider != null;
    }
    /**
     * 
     * @param nameProvider 
     */
    public void setNameProvider(LinksListNameProvider nameProvider){
        if (!Objects.equals(nameProvider, this.nameProvider)){
            LinksListNameProvider old = this.nameProvider;
            this.nameProvider = nameProvider;
            firePropertyChange(LINKS_LIST_NAME_PROVIDER_PROPERTY,old,nameProvider);
        }
    }
    /**
     * 
     * @param parent
     * @param model
     * @return 
     */
    public String showListNameDialog(Component parent, LinksListModel model){
        String title;   // This gets the title for the dialog
        String prompt;  // This gets the prompt for the dialog
            // This gets the current name of the model, or null if no model was 
        String oldName = getNameProvider().getListName(model);    // given
        if (model == null){ // If no model was provided (creating a new list)
            title = "Create New List";
            prompt = CREATE_NEW_ITEM_PROMPT;
        }
        else{               // If a model was provided (renaming a list)
            title = "Rename List \""+oldName+"\"";
            prompt = RENAME_ITEM_PROMPT;
        }
        setMessage(prompt);
        setInitialSelectionValue(null);
        setInitialSelectionValue(oldName);
            // Create a dialog to display the option pane used to enter the name
        JDialog dialog = createDialog(this, title);
            // This gets the name that was entered by the user.
        String name = null;
            // This stores whether the name entered by the user is valid (i.e. a 
        boolean valid;  // non-blank name not currently used by any other list)
        do{
            valid = true;
            dialog.setVisible(true);    // Show the dialog
                // Get the option selected by the user
            Object option = getValue();
                // This gets the message to display if there is an issue with 
            String msg = null;  // the name
                // If the option is a number, and OK was selected
            if (option instanceof Number && 
                    ((Number) option).equals(JOptionPane.OK_OPTION)){
                    // This gets the name from the user's input
                name = (String)getInputValue();
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
                        for (LinksListModel temp : getModels()){
                                // If the name of this list model is the same as 
                                // the entered name (we've already checked the 
                                // given model and confirmed it's not the same 
                                // as the old name for that model)
                            if (name.equals(getNameProvider().getListName(temp))){
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
     * 
     * @return 
     */
    public Set<String> getUsedNames(){
        return usedNames;
    }
    /**
     * 
     * @param models 
     */
    public void addUsedNames(Collection<LinksListModel> models){
        for (LinksListModel model : models){
            getUsedNames().add(model.getListName());
        }
    }
    /**
     * 
     * @param tabsPanel 
     */
    public void addUsedNames(LinksListTabsPanel tabsPanel){
        addUsedNames(tabsPanel.getModels());
    }
    /**
     * 
     * @return 
     */
    public Set<LinksListModel> getModels(){
        return models;
    }
}
