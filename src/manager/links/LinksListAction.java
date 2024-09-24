/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import java.awt.event.*;
import java.util.Objects;
import javax.swing.*;
import static javax.swing.Action.*;

/**
 *
 * @author Milo Steier
 */
public abstract class LinksListAction extends AbstractAction implements 
        LinksListNameProvider/*, PropertyChangeListener*/{
    /**
     * 
     */
    public static final String PANEL_KEY = "PanelKey";
    /**
     * 
     */
    public static final int CHECK_BOX_FLAG = 0x0001;
    /**
     * 
     */
    public static final int RADIO_BUTTON_FLAG = 0x0002;
    /**
     * 
     */
    public static final int LIST_MUST_BE_ENABLED_FLAG = 0x0004;
    /**
     * 
     */
    public static final int LIST_MUST_BE_DISABLED_FLAG = 0x0008;
    /**
     * 
     */
    public static final int LIST_MUST_BE_SELECTED_FLAG = 0x0010;
    /**
     * 
     */
    public static final int LIST_MUST_NOT_BE_SELECTED_FLAG = 0x0020;
    /**
     * 
     */
    public static final int LIST_MUST_BE_EDITED_FLAG = 0x0040;
    /**
     * 
     */
    public static final int LIST_MUST_NOT_BE_EDITED_FLAG = 0x0080;
    /**
     * 
     */
    public static final int LIST_MUST_BE_EMPTY_FLAG = 0x0100;
    /**
     * 
     */
    public static final int LIST_MUST_NOT_BE_EMPTY_FLAG = 0x0200;
    /**
     * 
     */
    public static final int LIST_MUST_BE_FULL_FLAG = 0x0400;
    /**
     * 
     */
    public static final int LIST_MUST_NOT_BE_FULL_FLAG = 0x0800;
    /**
     * 
     */
    public static final int LIST_MUST_BE_HIDDEN_FLAG = 0x1000;
    /**
     * 
     */
    public static final int LIST_MUST_NOT_BE_HIDDEN_FLAG = 0x2000;
    /**
     * 
     * @param name The name ({@code Action.NAME}) for the action, or null.
     * @param actionCmd The action command ({@code Action.ACTION_COMMAND_KEY}) 
     * for the action, or null.
     * @param panel The panel ({@code LinksListAction.PANEL_KEY}) that this 
     * action affects, or null
     */
    public LinksListAction(String name, String actionCmd, LinksListPanel panel){
        super(name);
        putValue(ACTION_COMMAND_KEY, actionCmd);
        putValue(PANEL_KEY,panel);
    }
    
    public LinksListAction(String name, LinksListPanel panel){
        this(name,name,panel);
    }
    
    public LinksListAction(String name, String actionCmd){
        this(name,actionCmd,null);
    }
    
    public LinksListAction(String name){
        this(name,name,null);
    }
    
    public LinksListAction(LinksListPanel panel){
        this(null,null,panel);
    }
    
    public LinksListAction(){
        this(null,null,null);
    }
    /**
     * {@inheritDoc } This forwards the call to the {@link 
     * #actionPerformed(ActionEvent, LinksListPanel) other actionPerformed} 
     * method with the given {@code ActionEvent} and the {@code LinksListPanel} 
     * returned by {@link #getPanel() getPanel}. In other words, this is 
     * equivalent to calling {@code actionPerformed(evt,getPanel())}.
     */
    @Override
    public void actionPerformed(ActionEvent evt){
        actionPerformed(evt,getPanel());
    }
    /**
     * Invoked when an action occurs. 
     * @param evt The event to be processed.
     * @param panel The LinksListPanel to use for the event.
     */
    public abstract void actionPerformed(ActionEvent evt,LinksListPanel panel);
    
    protected String getNewActionName(LinksListPanel panel){
        return getName();
    }
    
    public void updateActionName(){
        setName(getNewActionName(getPanel()));
    }
    
    public LinksListPanel getPanel(){
        Object value = getValue(PANEL_KEY);
        return (value instanceof LinksListPanel) ? (LinksListPanel) value : null;
    }
    
    public void setPanel(LinksListPanel panel){
        putValue(PANEL_KEY,panel);
        updateActionName();
    }
    
    public String getListName(){
        return getListName(getPanel());
    }
    
    public String getName(){
        Object value = getValue(NAME);
        return (value != null) ? value.toString() : null;
    }
    
    public void setName(String name){
        String oldName = getName();
        if (!Objects.equals(oldName, name))
            putValue(NAME,name);
    }
    
    public String getActionCommand(){
        Object value = getValue(ACTION_COMMAND_KEY);
        return (value != null) ? value.toString() : null;
    }
    
    public void setActionCommand(String actionCmd){
        putValue(ACTION_COMMAND_KEY,actionCmd);
    }
    
    public boolean isForSelectedList(){
        return false;
    }
    /**
     * This returns an integer containing the flags used to determine how this 
     * action will be used
     * @return 
     */
    public int getActionControlFlags(){
        return LIST_MUST_BE_ENABLED_FLAG;
    }
    
    public boolean getActionControlFlag(int flag){
        return LinksListModel.getFlag(flag,getActionControlFlags());
    }
    
    public boolean isForCheckBox(){
        return getActionControlFlag(CHECK_BOX_FLAG);
    }
    
    public boolean isForRadioButton(){
        return getActionControlFlag(RADIO_BUTTON_FLAG);
    }
    
    public boolean getListMustBeEnabled(){
        return getActionControlFlag(LIST_MUST_BE_ENABLED_FLAG);
    }
    
    public boolean getListMustBeDisabled(){
        return getActionControlFlag(LIST_MUST_BE_DISABLED_FLAG);
    }
    
    public boolean getListMustBeSelected(){
        return getActionControlFlag(LIST_MUST_BE_SELECTED_FLAG);
    }
    
    public boolean getListMustNotBeSelected(){
        return getActionControlFlag(LIST_MUST_NOT_BE_SELECTED_FLAG);
    }
    
    public boolean getListMustBeHidden(){
        return getActionControlFlag(LIST_MUST_BE_HIDDEN_FLAG);
    }
    
    public boolean getListMustNotBeHidden(){
        return getActionControlFlag(LIST_MUST_NOT_BE_HIDDEN_FLAG);
    }
    
    public boolean getListMustBeFull(){
        return getActionControlFlag(LIST_MUST_BE_FULL_FLAG);
    }
    
    public boolean getListMustNotBeFull(){
        return getActionControlFlag(LIST_MUST_NOT_BE_FULL_FLAG);
    }
    
    public boolean getListMustBeEmpty(){
        return getActionControlFlag(LIST_MUST_BE_EMPTY_FLAG);
    }
    
    public boolean getListMustNotBeEmpty(){
        return getActionControlFlag(LIST_MUST_NOT_BE_EMPTY_FLAG);
    }
    
    public boolean getListMustBeEdited(){
        return getActionControlFlag(LIST_MUST_BE_EDITED_FLAG);
    }
    
    public boolean getListMustNotBeEdited(){
        return getActionControlFlag(LIST_MUST_NOT_BE_EDITED_FLAG);
    }
    /**
     * This returns an integer containing the flags that must match up with what 
     * is 
     * @return 
     */
    public int getRequiredFlags(){
        return 0;
    }
    
    public int getRequiredFlagValues(){
        return 0;
    }
    
    public boolean getListHasRequiredFlags(LinksListPanel panel){
        return (panel.getFlags() & getRequiredFlags()) == 
                (getRequiredFlagValues() & getRequiredFlags());
    }
    
    protected Boolean isListSelected(LinksListPanel panel){
        return null;
    }
    
    protected Boolean isNonListSelected(){
        return null;
    }
    
    protected boolean isListHidden(LinksListPanel panel){
        return panel.isHidden();
    }
    
    protected Boolean[][] getControlRequirements(LinksListPanel panel){
        return new Boolean[][]{
            {getListMustBeEnabled(),getListMustBeDisabled(),panel.isEnabled()},
            {getListMustBeSelected(),getListMustNotBeSelected(),isListSelected(panel)},
            {getListMustBeEdited(),getListMustNotBeEdited(),panel.isEdited()},
            {getListMustBeEmpty(),getListMustNotBeEmpty(),panel.getModel().isEmpty()},
            {getListMustBeFull(),getListMustNotBeFull(),panel.getModel().isFull()},
            {getListMustBeHidden(),getListMustNotBeHidden(),isListHidden(panel)}
        };
    }
    
    protected Boolean getActionShouldBeEnabled(LinksListPanel panel){
        if (panel == null)
            return null;
        Boolean nonListSelected = isNonListSelected();
        if (isForSelectedList() && nonListSelected != null && nonListSelected)
            return false;
        if (!getListHasRequiredFlags(panel))
            return false;
        Boolean enableAction = null;
        for (Boolean[] arr : getControlRequirements(panel)){
            if (arr[2] == null || Objects.equals(arr[0], arr[1]))
                continue;
            boolean value = (arr[1]) ? !arr[2] : arr[2];
            if (enableAction == null)
                enableAction = value;
            else
                enableAction &= value;
        }
        return enableAction;
    }
    
    public void updateActionEnabled(){ 
        Boolean enable = getActionShouldBeEnabled(getPanel());
        if (enable != null)
            setEnabled(enable);
    }
    
    protected void updateButton(LinksListPanel panel, AbstractButton button){
        
    }
    
    protected void updateButton(AbstractButton button){
        updateButton(getPanel(), button);
    }
//    @Override
//    public void propertyChange(PropertyChangeEvent evt){
//        
//    }
}
