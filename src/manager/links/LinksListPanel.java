/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import event.DisabledComponentMouseListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Level;
import javax.swing.*;
import javax.swing.event.*;
import manager.LinkManager;

/**
 *
 * @author Milo Steier
 */
public class LinksListPanel extends JPanel implements Comparable<LinksListPanel>{
    
    public static final String MODEL_PROPERTY_CHANGED = "ModelPropertyChanged";
    
    public static final String SCROLLS_TO_BOTTOM_PROPERTY_CHANGED = 
            "ScrollsToBottomPropertyChanged";
    
    public static final String TAB_COMPONENT_PROPERTY_CHANGED = 
            "TabComponentPropertyChanged";
    
    /**
     * The model for this LinksListPanel.
     */
    private LinksListModel model = null;
    /**
     * This is a map that stores the actions associated with this panel.
     */
    private HashMap<String,LinksListAction> panelActionMap;
    /**
     * This is a map that stores the menu items that perform the actions 
     * associated with this panel.
     */
    private HashMap<String,JMenuItem> panelMenuItemMap;
    /**
     * This stores whether the list will automatically scroll to the bottom when 
     * an item is added.
     */
    private boolean bottomScrolls;
    /**
     * The JList used to display the list.
     */
    private JList<String> list;
    /**
     * The scroll pane displaying the JList.
     */
    private JScrollPane scrollPane;
    
    private Handler handler;
    
    private Component tabComponent = null;
    
    private void initialize(LinksListModel model){
        handler = new Handler();
        MouseListener disabledListener = new DisabledComponentMouseListener();
        bottomScrolls = true;
        panelActionMap = new HashMap<>();
        panelMenuItemMap = new HashMap<>();
        list = new JList<>();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setDoubleBuffered(true);
        list.setInheritsPopupMenu(true);
        list.addMouseListener(disabledListener);
        list.addListSelectionListener(handler);
        scrollPane = new JScrollPane(list,ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS,
            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setDoubleBuffered(true);
        scrollPane.setInheritsPopupMenu(true);
        scrollPane.addMouseListener(disabledListener);
        add(scrollPane, BorderLayout.CENTER);
        setModel(model);
    }
    
    public LinksListPanel(LinksListModel model){
        super(new BorderLayout(),true);
        initialize(Objects.requireNonNull(model));
    }
    
    public LinksListPanel(String name, Integer listID){
        this(new LinksListModel(name,listID));
    }
    
    public LinksListPanel(String name){
        this(name,null);
    }
    
    public LinksListPanel(Integer listID){
        this(null,listID);
    }
    
    public LinksListPanel(){
        this(new LinksListModel());
    }
    
    public JList<String> getList(){
        return list;
    }
    
    public JScrollPane getScrollPane(){
        return scrollPane;
    }
    
    public JViewport getViewport(){
        return scrollPane.getViewport();
    }
    
    public LinksListModel getModel(){
        return model;
    }
    
    public void setModel(LinksListModel model, boolean keepSelection, 
            boolean shouldScroll){
            // Check if the model is null
        Objects.requireNonNull(model, "Model cannot be null");
        if (model == this.model)
            return;
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "setModel", new Object[]{keepSelection,shouldScroll});
        String selected = list.getSelectedValue();
        LinksListModel old = this.model;
        this.model = model;
        if (old != null){
            old.removeChangeListener(handler);
            old.removePropertyChangeListener(handler);
            old.removeListDataListener(handler);
        }
        model.addChangeListener(handler);
        model.addPropertyChangeListener(handler);
        model.addListDataListener(handler);
        list.setModel(model);
        list.setSelectionModel(model);
        firePropertyChange(MODEL_PROPERTY_CHANGED,old,model);
        fireContentsChanged(0,(old!=null)?Math.max(old.size()-1,model.size()-1):
                model.size()-1);
        if (old != null){
            if (!Objects.equals(old.getListID(),model.getListID()))
                firePropertyChange(LinksListModel.LIST_ID_PROPERTY_CHANGED,
                        old.getListID(),model.getListID());
            if (!Objects.equals(old.getListName(), model.getListName()))
                firePropertyChange(LinksListModel.LIST_NAME_PROPERTY_CHANGED,
                        old.getListName(), model.getListName());
            if (!Objects.equals(old.getSizeLimit(), model.getSizeLimit()))
                firePropertyChange(LinksListModel.LIST_SIZE_LIMIT_PROPERTY_CHANGED,
                        old.getSizeLimit(), model.getSizeLimit());
            for (Integer flag : LinksListModel.FLAG_PROPERTY_NAMES_MAP.keySet()){
                if (old.getFlag(flag) != model.getFlag(flag))
                    firePropertyChange(LinksListModel.FLAG_PROPERTY_NAMES_MAP.get(flag),
                            old.getFlag(flag),model.getFlag(flag));
            }
            if (old.isHidden() != model.isHidden())
                firePropertyChange(LinksListModel.LIST_IS_HIDDEN_PROPERTY_CHANGED,
                        old.isHidden(),model.isHidden());
        }
        updateActionNames();
        fireStateChanged();
        if (keepSelection){
            if (model.contains(selected))
                list.setSelectedValue(selected, shouldScroll);
            else
                list.clearSelection();
        }
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "setModel");
    }
    
    public void setModel(LinksListModel model, boolean keepSelection){
        setModel(model,keepSelection,keepSelection);
    }
    
    public void setModel(LinksListModel model){
        setModel(model, false);
    }
    
    public Integer getListID(){
        return model.getListID();
    }
    
    public void setListID(Integer listID){
        model.setListID(listID);
    }
    
    public String getListName(){
        return model.getListName();
    }
    
    public void setListName(String name){
        model.setListName(name);
    }
    
    public int getFlags(){
        return model.getFlags();
    }
    
    public void setFlags(int flags){
        model.setFlags(flags);
    }
    
    public boolean getFlag(int flag){
        return model.getFlag(flag);
    }
    
    public void setFlag(int flag, boolean value){
        model.setFlag(flag, value);
    }
    
    public boolean getAllowsDuplicates(){
        return model.getAllowsDuplicates();
    }
    
    public void setAllowsDuplicates(boolean value){
        model.setAllowsDuplicates(value);
    }
    
    public boolean isReadOnly(){
        return model.isReadOnly();
    }
    
    public void setReadOnly(boolean value){
        model.setReadOnly(value);
    }
    
    public boolean isHidden(){
        return model.isHidden();
    }
    
    public void setHidden(boolean value){
        model.setHidden(value);
    }
    
    public boolean isEdited(){
        return model.isEdited();
    }
    
    public void setEdited(boolean edited){
        model.setEdited(edited);
    }
    
    public void clearEdited(){
        model.clearEdited();
    }
    
    public long getLastModified(){
        return model.getLastModified();
    }
    
    public void setLastModified(long lastMod){
        model.setLastModified(lastMod);
    }
    
    public void setLastModified(){
        model.setLastModified();
    }
    
    public long getCreationTime(){
        return model.getCreationTime();
    }
    
    public void setCreationTime(long created){
        model.setCreationTime(created);
    }
    
    public Integer getSizeLimit(){
        return model.getSizeLimit();
    }
    
    public void setSizeLimit(Integer limit){
        model.setSizeLimit(limit);
    }
    
    public String getListToolTipText(){
        return model.getToolTipText();
    }
    @Override
    public int compareTo(LinksListPanel o) {
        return model.compareTo(o.model);
    }
    
    protected void updateActionEnabled(){
        for (LinksListAction action : getListActionMap().values()){
            if (action != null)
                action.updateActionEnabled();
        }
    }
    
    protected void updateActionNames(){
        for (LinksListAction action : getListActionMap().values()){
            if (action != null)
                action.updateActionName();
        }
    }
    
    protected void updateMenuItems(){
        if (getListMenuItemMap().isEmpty())
            return;
        for (Map.Entry<String, LinksListAction> entry : getListActionMap().entrySet()){
            if (entry.getValue() == null)
                continue;
            JMenuItem menuItem = getListMenuItemMap().get(entry.getKey());
            if (menuItem != null){
                entry.getValue().updateButton(menuItem);
            }
        }
    }
    
    public void updateModelContents(List<String> values, boolean shouldScroll){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "updateModelContents", shouldScroll);
        String selected = list.getSelectedValue();
        model.setContents(values);
        if (selected == null || model.contains(selected))
            list.setSelectedValue(selected, shouldScroll);
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "updateModelContents");
    }
    
    public void updateModelContents(List<String> values){
        updateModelContents(values,true);
    }
    
    public boolean isSelectionEmpty(){
        return list.isSelectionEmpty();
    }
    
    public boolean isSelectedIndex(int index){
        return list.isSelectedIndex(index);
    }
    
    public int getSelectedIndex(){
        return list.getSelectedIndex();
    }
    
    public String getSelectedValue(){
        return list.getSelectedValue();
    }
    
    public void clearSelection(){
        list.clearSelection();
    }
    
    public void setSelectedIndex(int index){
        list.setSelectedIndex(index);
    }
    
    public void setSelectedIndex(int index, boolean shouldScroll){
        setSelectedIndex(index);
        if (!isSelectionEmpty() && shouldScroll && index >= 0 && index < model.size())
            ensureIndexIsVisible(index);
    }
    
    public void setSelectedValue(String value, boolean shouldScroll){
        list.setSelectedValue(value, shouldScroll);
    }
    
    public void ensureIndexIsVisible(int index){
        list.ensureIndexIsVisible(index);
    }
    
    public boolean isIndexVisible(int index){
        return index >= list.getFirstVisibleIndex() && index <= list.getLastVisibleIndex();
    }
    
    public void setScrollsToBottom(boolean enabled){
        if (enabled == bottomScrolls)
            return;
        bottomScrolls = enabled;
        firePropertyChange(SCROLLS_TO_BOTTOM_PROPERTY_CHANGED,!enabled,enabled);
    }
    
    public boolean getScrollsToBottom(){
        return bottomScrolls;
    }
    
    public Map<String,LinksListAction> getListActionMap(){
        return panelActionMap;
    }
    
    public LinksListAction getListAction(String key){
        return getListActionMap().get(key);
    }
    
    public LinksListAction setListAction(String key, LinksListAction action){
        return getListActionMap().put(key, action);
    }
    
    public boolean containsListActionKey(String key){
        return getListActionMap().containsKey(key);
    }
    
    public Map<String,JMenuItem> getListMenuItemMap(){
        return panelMenuItemMap;
    }
    
    public JMenuItem getListMenuItem(String key){
        return getListMenuItemMap().get(key);
    }
    
    public JMenuItem setListMenuItem(String key, JMenuItem menuItem){
        return getListMenuItemMap().put(key, menuItem);
    }
    
    public boolean containsListMenuItemKey(String key){
        return getListMenuItemMap().containsKey(key);
    }
    
    protected static Function<? super String, ? extends JMenuItem> 
            getMenuItemActionMapper(Map<? super String,? extends Action>actionMap){
        return (String t) -> {
            if (actionMap == null)
                return null;
            Action action = actionMap.get(t);
            if (action instanceof LinksListAction){
                LinksListAction listAction = (LinksListAction) action;
                if (listAction.isForCheckBox())
                    return new JCheckBoxMenuItem(listAction);
                if (listAction.isForRadioButton())
                    return new JRadioButtonMenuItem(listAction);
            }
            return (action != null) ? new JMenuItem(action) : null;
        };
    }
    
    public JMenuItem getOrCreateListMenuItem(String key){
        return getListMenuItemMap().computeIfAbsent(key, 
                getMenuItemActionMapper(getListActionMap()));
    }
    
    public void setTabComponent(Component component){
        if (Objects.equals(tabComponent, component))
            return;
        Component old = tabComponent;
        tabComponent = component;
        firePropertyChange(TAB_COMPONENT_PROPERTY_CHANGED,old,component);
    }
    
    public Component getTabComponent(){
        return tabComponent;
    }
    /**
     * This attempts to scroll to the last index in the list when elements are 
     * added to the end of the list and if the list was previously scrolled to 
     * the bottom.
     * @param evt The ListDataEvent that occurred.
     * @see #getScrollsToBottom() 
     * @see #setScrollsToBottom(boolean) 
     * @see ListDataEvent#getIndex0() 
     * @see ListDataEvent#getIndex1() 
     * @see javax.swing.JList#getLastVisibleIndex() 
     * @see javax.swing.JList#ensureIndexIsVisible(int) 
     */
    protected void scrollAfterAdding(ListDataEvent evt){
        try{    // If elements were added to the end of the model and the last 
                // visible index was previously the last index in the list
            if(evt.getIndex1()==model.size()-1&&
                    evt.getIndex0()-1==list.getLastVisibleIndex()){
                list.ensureIndexIsVisible(evt.getIndex1());
            }
        } catch (Exception ex){
            LinkManager.getLogger().log(Level.WARNING, 
                    "Exception thrown while scrolling after addition", ex);
        }
    }
    @Override
    protected String paramString(){
        return super.paramString()+
                ",listID="+Objects.toString(getListID(),"")+
                ",listName="+Objects.toString(getListName(),"")+
                ",lastModified="+getLastModified()+
                ",edited="+isEdited()+
                ",scrollsToBottom="+getScrollsToBottom();
    }
    @Override
    public void setFont(Font font){
        super.setFont(font);
        try{
            scrollPane.setFont(font);
            list.setFont(font);
        }
        catch(NullPointerException ex){}
    }
    @Override
    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        try{
            scrollPane.setEnabled(enabled);
            list.setEnabled(enabled);
            updateActionEnabled();
        }
        catch(NullPointerException ex){}
    }
    
    public void addChangeListener(ChangeListener l){
        if (l != null)
            listenerList.add(ChangeListener.class, l);
    }
    
    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class, l);
    }
    
    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }
    
    protected void fireStateChanged(){
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : getChangeListeners()){
            if (l != null)
                l.stateChanged(evt);
        }
    }
    
    public void addListDataListener(ListDataListener listener){
        if (listener != null)
            listenerList.add(ListDataListener.class, listener);
    }
    
    public void removeListDataListener(ListDataListener listener){
        listenerList.remove(ListDataListener.class, listener);
    }
    
    public ListDataListener[] getListDataListeners(){
        return listenerList.getListeners(ListDataListener.class);
    }
    /**
     * This is invoked when elements have been inserted to the list's model. 
     * This will notify any {@code ListDataListener}s that have been added of 
     * the changes to the model, using this {@code JListSelector} as the source 
     * of the {@code ListDataEvent} provided to the listeners. The elements that 
     * have been added will be within the interval between {@code index0} and 
     * {@code index1}, inclusive. Note that {@code index0} does not need to be 
     * less than or equal to {@code index1}. 
     * @param index0 One end of the interval.
     * @param index1 The other end of the interval.
     * @see #addListDataListener(javax.swing.event.ListDataListener) 
     * @see #removeListDataListener(javax.swing.event.ListDataListener) 
     * @see #getListDataListeners() 
     * @see ListDataEvent
     * @see #updateAcceptEnabled() 
     * @see #fireIntervalRemoved(int, int) 
     * @see #fireContentsChanged(int, int) 
     */
    protected void fireIntervalAdded(int index0, int index1){
        ListDataEvent evt = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED,
                index0,index1);
            // A for loop to go through the list data listeners
        for (ListDataListener l : getListDataListeners()){
            if (l != null)      // If the listener is not null
                l.intervalAdded(evt);
        }
    }
    /**
     * This is invoked when elements have been removed from the list's model. 
     * This will notify any {@code ListDataListener}s that have been added of 
     * the changes to the model, using this {@code JListSelector} as the source 
     * of the {@code ListDataEvent} provided to the listeners. The elements that 
     * have been removed will be the elements that were within the interval 
     * between {@code index0} and {@code index1}, inclusive. Note that {@code 
     * index0} does not need to be less than or equal to {@code index1}. 
     * @param index0 One end of the interval.
     * @param index1 The other end of the interval.
     * @see #addListDataListener(javax.swing.event.ListDataListener) 
     * @see #removeListDataListener(javax.swing.event.ListDataListener) 
     * @see #getListDataListeners() 
     * @see ListDataEvent
     * @see #updateAcceptEnabled() 
     * @see #fireIntervalAdded(int, int) 
     * @see #fireContentsChanged(int, int) 
     */
    protected void fireIntervalRemoved(int index0, int index1){
        ListDataEvent evt = new ListDataEvent(this,
                ListDataEvent.INTERVAL_REMOVED,index0,index1);
            // A for loop to go through the list data listeners
        for (ListDataListener l : getListDataListeners()){
            if (l != null)      // If the listener is not null
                l.intervalRemoved(evt);
        }
    }
    /**
     * This is invoked when the elements within the interval between {@code 
     * index0} and {@code index1}, inclusive, in the list's model have changed. 
     * This will notify any {@code ListDataListener}s that have been added of 
     * the changes to the model, using this {@code JListSelector} as the source 
     * of the {@code ListDataEvent} provided to the listeners. Note that {@code 
     * index0} does not need to be less than or equal to {@code index1}. 
     * @param index0 One end of the interval.
     * @param index1 The other end of the interval.
     * @see #addListDataListener(javax.swing.event.ListDataListener) 
     * @see #removeListDataListener(javax.swing.event.ListDataListener) 
     * @see #getListDataListeners() 
     * @see ListDataEvent
     * @see #updateAcceptEnabled() 
     * @see #fireIntervalAdded(int, int) 
     * @see #fireIntervalRemoved(int, int) 
     */
    protected void fireContentsChanged(int index0, int index1){
        ListDataEvent evt = new ListDataEvent(this,
                ListDataEvent.CONTENTS_CHANGED,index0,index1);
            // A for loop to go through the list data listeners
        for (ListDataListener l : getListDataListeners()){
            if (l != null)      // If the listener is not null
                l.contentsChanged(evt);
        }
    }
    
    public void addListSelectionListener(ListSelectionListener listener){
        if (listener != null)
            listenerList.add(ListSelectionListener.class, listener);
    }
    
    public void removeListSelectionListener(ListSelectionListener listener){
        listenerList.remove(ListSelectionListener.class, listener);
    }
    
    public ListSelectionListener[] getListSelectionListeners(){
        return listenerList.getListeners(ListSelectionListener.class);
    }
    
    /**
     * This is invoked when a change to the list's selection has occurred. This 
     * will notify any {@code ListSelectionListener}s that have been added of 
     * the changes to the selection, using this {@code JListSelector} as the 
     * source of the {@code ListSelectionEvent} provided to the listeners. 
     * 
     * @param firstIndex The first index in the range, expected to be {@code <= 
     * lastIndex}.
     * @param lastIndex The last index in the range, expected to be {@code >= 
     * firstIndex}.
     * @param isAdjusting Whether this is one in a series of multiple events, 
     * where changes are still being made to the selection.
     * @see #addListSelectionListener(javax.swing.event.ListSelectionListener) 
     * @see #removeListSelectionListener(javax.swing.event.ListSelectionListener) 
     * @see #getListSelectionListeners() 
     * @see ListSelectionEvent
     * @see #updateAcceptEnabled() 
     */
    protected void fireSelectionChanged(int firstIndex, int lastIndex, 
            boolean isAdjusting){
            // This is the event to be fired
        ListSelectionEvent evt=new ListSelectionEvent(this,firstIndex,lastIndex,
                isAdjusting);
            // A for loop to go through the list selection listeners
        for (ListSelectionListener l : getListSelectionListeners()){
            if (l != null)      // If the listener is not null
                l.valueChanged(evt);
        }
    }
    
    /**
     * A class for handling changes to the model and the selection for the list.
     */
    private class Handler implements ListDataListener, ListSelectionListener, 
            ChangeListener, PropertyChangeListener{
        @Override
        public void intervalAdded(ListDataEvent evt) {
                // If this is to scroll to the bottom after adding an item
            if (bottomScrolls){      
                scrollAfterAdding(evt);
            }
            updateActionEnabled();
            fireIntervalAdded(evt.getIndex0(),evt.getIndex1());
        }
        @Override
        public void intervalRemoved(ListDataEvent evt) {
            updateActionEnabled();
            fireIntervalRemoved(evt.getIndex0(),evt.getIndex1());
        }
        @Override
        public void contentsChanged(ListDataEvent evt) {
            updateActionEnabled();
            fireContentsChanged(evt.getIndex0(),evt.getIndex1());
        }
        @Override
        public void valueChanged(ListSelectionEvent evt) {
            fireSelectionChanged(evt.getFirstIndex(),evt.getLastIndex(),
                    evt.getValueIsAdjusting());
        }
        @Override
        public void stateChanged(ChangeEvent evt) {
            updateActionEnabled();
            fireStateChanged();
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            updateActionEnabled();
            updateMenuItems();
            if (LinksListModel.LIST_NAME_PROPERTY_CHANGED.equals(evt.getPropertyName()))
                updateActionNames();
            firePropertyChange(evt.getPropertyName(),evt.getOldValue(),
                    evt.getNewValue());
        }
    }
}
