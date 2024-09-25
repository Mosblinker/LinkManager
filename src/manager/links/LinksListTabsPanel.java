/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import components.debug.DebugCapable;
import event.DisabledComponentMouseListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author Milo Steier
 */
public class LinksListTabsPanel extends JPanel implements Iterable<LinksListPanel>, 
        LinksListNameProvider, DebugCapable{
    
    public static final String LISTS_EDITED_PROPERTY_CHANGED = 
            "ListsEditedPropertyChanged";
    
    public static final String STRUCTURE_EDITED_PROPERTY_CHANGED = 
            "StructureEditedPropertyChanged";
    
    public static final String LISTS_ENABLED_PROPERTY_CHANGED = 
            "ListsEnabledPropertyChanged";
    
    public static final String PANEL_ACTION_MAPPER_PROPERTY_CHANGED = 
            "PanelActionMapperPropertyChanged";
    
    public static final String DEBUG_MODE_PROPERTY_CHANGED = 
            "DebugModePropertyChange";
    
    public static final String IS_SHOWING_HIDDEN_LISTS_PROPERTY_CHANGED = 
            "ShowsHiddenListsPropertyChanged";
    
//    public static final String HIDDEN_LISTS
    
    public static final String LIST_PANEL_LIST_ID_PROPERTY_CHANGED_PREFIX = 
            LinksListModel.LIST_ID_PROPERTY_CHANGED+"ForIndex";
    
    public static final String LIST_PANEL_LIST_NAME_PROPERTY_CHANGED_PREFIX = 
            LinksListModel.LIST_NAME_PROPERTY_CHANGED+"ForIndex";
    
    public static final String LIST_PANEL_MODEL_PROPERTY_CHANGED_PREFIX = 
            LinksListPanel.MODEL_PROPERTY_CHANGED+"ForIndex";
    
    public static final String LIST_PANEL_SCROLLS_TO_BOTTOM_PROPERTY_CHANGED_PREFIX = 
            LinksListPanel.SCROLLS_TO_BOTTOM_PROPERTY_CHANGED+"ForIndex";
    
    public static final String LIST_IS_HIDDEN_PROPERTY_CHANGED_PREFIX = 
            LinksListModel.LIST_IS_HIDDEN_PROPERTY_CHANGED+"ForIndex";
    
    public static final String LIST_IS_READ_ONLY_PROPERTY_CHANGED_PREFIX = 
            LinksListModel.LIST_IS_READ_ONLY_PROPERTY_CHANGED+"ForIndex";
    
    private static final String EDITED_LIST_INDICATOR = "*";
    
    private static final String HIDDEN_LIST_INDICATOR = "👁";
    
    private static final String READ_ONLY_LIST_INDICATOR = "🔏";
    
//    private static final String SIZE_LIMITED_LIST_INDICATOR = "";
    
    private static final String SIZE_LIMITED_FULL_LIST_INDICATOR = "❗";
    
    public static final String RESERVED_LIST_NAME_CHARACTERS = 
            EDITED_LIST_INDICATOR+
            HIDDEN_LIST_INDICATOR+
            READ_ONLY_LIST_INDICATOR+
//            SIZE_LIMITED_LIST_INDICATOR+
            SIZE_LIMITED_FULL_LIST_INDICATOR;
    
    /**
     * The JTabbedPane used to display the panels that display the lists.
     */
    private JTabbedPane tabbedPane;
    /**
     * The List containing the panels for the lists.
     */
    private List<LinksListPanel> panels = null;
    /**
     * A list containing the models for the panels.
     */
    private List<LinksListModel> models = null;
    /**
     * A list containing the listIDs for the panels.
     */
    private List<Integer> listIDs = null;
    /**
     * A list containing the tab components for the panels.
     */
    private List<Component> tabComponents = null;
//    /**
//     * This stores the index of the currently selected tab. This is used to 
//     * detect when the selected tab changes.
//     */
//    private int currSelTab = -1;
    /**
     * This is a map used to store the menus for the list actions.
     */
    private Map<String,JMenu> panelActionMenus;
    /**
     * This is a map used to store whether the menus are enabled.
     */
    private Map<String,Boolean> panelActionMenuEnabled;
    /**
     * This is a map used to store the separators between the dedicated list 
     * action menu items and the currently selected list action menu items.
     */
    private Map<String,JPopupMenu.Separator> panelActionSeparators;
    /**
     * This is a map that stores the actions associated with the currently 
     * selected panel.
     */
    private Map<String,LinksListAction> currPanelActionMap;
    /**
     * This is a map that stores the menu items that perform the actions 
     * associated with the currently selected panel.
     */
    private Map<String,JMenuItem> currPanelMenuItemMap;
    
    private ListPanelHandler listHandler;
    
    private boolean edited = false;
    
    private boolean structEdited = false;
    
    private boolean listsEnabled = true;
    
    private boolean debugMode = false;
    
    private Set<Integer> removedListIDs;
    
    private BiFunction<? super String, ? super LinksListPanel, 
            ? extends LinksListAction> panelActionMapper;
    
    private boolean showsHiddenLists;
    /**
     * This is used to provide error feedback to the user when this is disabled. 
     * This is initialized the first time it is requested.
     */
    private DisabledComponentMouseListener disabledListener = null;
    
    private void initialize(){
        removedListIDs = new HashSet<>();
        panelActionMenus = new LinkedHashMap<>();
        panelActionMenuEnabled = new HashMap<>();
        panelActionSeparators = new HashMap<>();
        currPanelActionMap = new HashMap<>();
        currPanelMenuItemMap = new HashMap<>();
        panelActionMapper = null;
        tabbedPane = new JTabbedPane();
        listHandler = new ListPanelHandler();
        Handler handler = new Handler();
        tabbedPane.addChangeListener(handler);
        tabbedPane.addMouseListener(getDisabledComponentListener());
        this.add(tabbedPane, BorderLayout.CENTER);
    }
    
    public LinksListTabsPanel(){
        super(new BorderLayout());
        initialize();
    }
    /**
     * This returns the mouse listener used to cause disabled components to 
     * provide error feedback to the user when they are pressed.
     * @return The mouse listener used to make disabled components provide 
     * error feedback when pressed.
     */
    protected MouseListener getDisabledComponentListener(){
            // If the disabled component mouse listener has not been initialized 
        if (disabledListener == null)   // yet
            disabledListener = new DisabledComponentMouseListener();
        return disabledListener;
    }
    @Override
    public void setFont(Font font){
        super.setFont(font);
        try{
            tabbedPane.setFont(font);
            for (LinksListPanel panel : this){
                panel.setFont(font);
            }
        }
        catch(NullPointerException ex){}
    }
    @Override
    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        try{
            for (Map.Entry<String,JMenu> entry : panelActionMenus.entrySet()){
                if (entry.getValue() != null){
                    entry.getValue().setEnabled(enabled && 
                            panelActionMenuEnabled.getOrDefault(entry.getKey(), true));
                }
            }
        }
        catch(NullPointerException ex){}
    }
    
    public JTabbedPane getTabbedPane(){
        return tabbedPane;
    }
    
    public List<LinksListPanel> getLists(){
        if (panels == null)
            panels = new ListPanelList();
        return panels;
    }
    
    public List<LinksListModel> getModels(){
        if (models == null)
            models = new LinksListModelList(getLists());
        return models;
    }
    
    public List<Integer> getListIDs(){
        if (listIDs == null)
            listIDs = new LinksListIDList(getModels());
        return listIDs;
    }
    
    public List<Component> getTabComponents(){
        if (tabComponents == null)
            tabComponents = new TabComponentList();
        return tabComponents;
    }
    
    public boolean getListsEnabled(){
        return listsEnabled;
    }
    
    public void setListsEnabled(boolean enabled){
        if (listsEnabled == enabled)
            return;
        listsEnabled = enabled;
        tabbedPane.setEnabled(enabled);
        for (LinksListPanel panel : this){
            if (panel != null)
                panel.setEnabled(enabled);
        }
        firePropertyChange(LISTS_ENABLED_PROPERTY_CHANGED,!enabled,enabled);
    }
    
    public boolean getListsEdited(){
        for (LinksListPanel panel : this){
            if (panel.isEdited())
                return true;
        }
        return false;
    }
    
    public boolean getHiddenListsEdited(){
        for (LinksListPanel panel : this){
            if (panel.isHidden() && panel.isEdited())
                return true;
        }
        return false;
    }
    
    public boolean isEdited(){
        return edited;
    }
    
    public void setEdited(boolean edited){
        if (this.edited == edited)
            return;
        this.edited = edited;
        if (!edited){
            for (LinksListPanel panel : this)
                panel.clearEdited();
            clearRemovedListIDs();
        }
        firePropertyChange(LISTS_EDITED_PROPERTY_CHANGED,!edited,edited);
    }
    
    public boolean isStructureEdited(){
        return structEdited;
    }
    
    public void setStructureEdited(boolean edited){
        if (structEdited == edited)
            return;
        structEdited = edited;
        firePropertyChange(STRUCTURE_EDITED_PROPERTY_CHANGED,!edited,edited);
    }
    
    public void clearEdited(){
        setEdited(false);
        setStructureEdited(false);
    }
    
    public void setInDebug(boolean debugMode){
        if (this.debugMode == debugMode)
            return;
        this.debugMode = debugMode;
        firePropertyChange(DEBUG_MODE_PROPERTY_CHANGED,!debugMode,debugMode);
        for (LinksListPanel panel : this){
            updateTabTitle(panel);
        }
        repaint();
    }
    @Override
    public boolean isInDebug() {
        return debugMode;
    }
    
    public BiFunction<? super String, ? super LinksListPanel, 
            ? extends LinksListAction> getListActionMapper(){
        return panelActionMapper;
    }
    
    public void setListActionMapper(BiFunction<? super String, 
            ? super LinksListPanel, ? extends LinksListAction> mapper){
        if (mapper == panelActionMapper)
            return;
        BiFunction<? super String, ? super LinksListPanel, 
                ? extends LinksListAction> old = panelActionMapper;
        this.panelActionMapper = mapper;
        firePropertyChange(PANEL_ACTION_MAPPER_PROPERTY_CHANGED,old,mapper);
    }
    
    protected Function<? super String, ? extends LinksListAction> 
            getActionMappingFunction(LinksListPanel panel){
        return (String t) -> {
            if (panelActionMapper == null)
                return null;
            return panelActionMapper.apply(t, panel);
        };
    }
    
    public Set<String> getListActionCommands(){
        return panelActionMenus.keySet();
    }
    
    public int getListActionCommandCount(){
        return panelActionMenus.size();
    }
    
    public Map<String, JMenu> getListActionMenus(){
        return panelActionMenus;
    }
    
    public Map<String, JPopupMenu.Separator> getListActionSeparators(){
        return panelActionSeparators;
    }
    
    public Map<String,LinksListAction> getListActionMap(LinksListPanel panel){
        if (panel == null)
            return currPanelActionMap;
        else
            return panel.getListActionMap();
    }
    
    public boolean containsListActionKey(LinksListPanel panel, String actionCmd){
        return getListActionMap(panel).containsKey(actionCmd);
    }
    
    public boolean containsListActionValue(LinksListPanel panel, LinksListAction action){
        return getListActionMap(panel).containsValue(action);
    }
    
    public LinksListAction getListAction(LinksListPanel panel, String actionCmd){
        return getListActionMap(panel).get(actionCmd);
    }
    
    public LinksListAction getOrCreateListAction(LinksListPanel panel, String actionCmd){
        return getListActionMap(panel).computeIfAbsent(actionCmd, 
                getActionMappingFunction(panel));
    }
    
    public LinksListAction setListAction(LinksListPanel panel, String actionCmd, 
            LinksListAction action){
        return getListActionMap(panel).put(actionCmd, action);
    }
    
    public LinksListAction removeListAction(LinksListPanel panel, String actionCmd){
        return getListActionMap(panel).remove(actionCmd);
    }
    
    public Map<String,JMenuItem> getListMenuItemMap(LinksListPanel panel){
        if (panel == null)
            return currPanelMenuItemMap;
        else
            return panel.getListMenuItemMap();
    }
    
    public boolean containsListMenuItemKey(LinksListPanel panel, String actionCmd){
        return getListMenuItemMap(panel).containsKey(actionCmd);
    }
    
    public boolean containsListMenuItemValue(LinksListPanel panel, JMenuItem menuItem){
        return getListMenuItemMap(panel).containsValue(menuItem);
    }
    
    public JMenuItem getListMenuItem(LinksListPanel panel, String actionCmd){
        return getListMenuItemMap(panel).get(actionCmd);
    }
    
    public JMenuItem getOrCreateListMenuItem(LinksListPanel panel, String actionCmd){
        getOrCreateListAction(panel,actionCmd);
        if (panel != null)
            return panel.getOrCreateListMenuItem(actionCmd);
        else
            return currPanelMenuItemMap.computeIfAbsent(actionCmd, 
                    LinksListPanel.getMenuItemActionMapper(currPanelActionMap));
    }
    
    public JMenuItem setListMenuItem(LinksListPanel panel, String actionCmd, 
            JMenuItem menuItem){
        return getListMenuItemMap(panel).put(actionCmd, menuItem);
    }
    
    public JMenuItem removeListMenuItem(LinksListPanel panel, String actionCmd){
        return getListMenuItemMap(panel).remove(actionCmd);
    }
    
    protected JPopupMenu.Separator getOrCreateSeparator(String actionCmd){
        return panelActionSeparators.computeIfAbsent(actionCmd, (String t) -> {
            if (currPanelActionMap.containsKey(t) || currPanelMenuItemMap.containsKey(t))
                return new JPopupMenu.Separator();
            return null;
        });
    }
    
    public JMenu setListActionMenu(String actionCmd, JMenu menu){
        JMenu old = panelActionMenus.put(actionCmd, menu);
        panelActionMenuEnabled.put(actionCmd, menu != null && menu.isEnabled());
        if (old != null){
            for (LinksListPanel panel : this){
                removeItemFromMenu(actionCmd,old,panel);
            }
            removeItemFromMenu(actionCmd,old,null);
        }
        if (menu != null){
            for (LinksListPanel panel : this){
                addItemToMenu(actionCmd,menu,panel);
            }
            addItemToMenu(actionCmd,menu,null);
        }
        return old;
    }
    
    public JMenu setListActionMenu(JMenu menu){
        return setListActionMenu(menu.getActionCommand(),menu);
    }
    
    public JMenu getListActionMenu(String actionCmd){
        return panelActionMenus.get(actionCmd);
    }
    
    public JMenu removeListActionMenu(String actionCmd){
        panelActionMenuEnabled.remove(actionCmd);
        return panelActionMenus.remove(actionCmd);
    }
    
    public void putListActionMenus(Collection<JMenu> menus){
        Objects.requireNonNull(menus);
        for (JMenu menu : menus){
            setListActionMenu(menu);
        }
    }
    
    public void putListActionMenus(JMenu... menus){
        if (menus != null && menus.length > 0)
            putListActionMenus(Arrays.asList(menus));
        else
            throw new IllegalArgumentException();
    }
    
    public boolean containsListActionMenuKey(String actionCmd){
        return panelActionMenus.containsKey(actionCmd);
    }
    
    public boolean containsListActionMenuValue(JMenu menu){
        return panelActionMenus.containsValue(menu);
    }
    
    public void setListActionMenuEnabled(String actionCmd, boolean enabled){
        JMenu menu = panelActionMenus.get(actionCmd);
        if (menu != null){
            panelActionMenuEnabled.put(actionCmd, enabled);
            menu.setEnabled(isEnabled() && enabled);
        }
    }
    
    public boolean isListActionMenuEnabled(String actionCmd){
        JMenu menu = panelActionMenus.get(actionCmd);
        return menu != null && panelActionMenuEnabled.getOrDefault(actionCmd, menu.isEnabled());
    }
    
    private void insertItemIntoMenu(String actionCmd, JMenu menu, 
            LinksListPanel panel, int index){
        JMenuItem item = getOrCreateListMenuItem(panel,actionCmd);
        if (item == null)
            return;
        int menuSize = menu.getMenuComponentCount();
        JPopupMenu.Separator sep = panelActionSeparators.get(actionCmd);
        if (sep != null && menu.isMenuComponent(sep)){
            int sepIndex = -1;
            for (int i = menuSize-1; i >= 0 && sepIndex < 0; i--){
                if (sep.equals(menu.getMenuComponent(i)))
                    sepIndex = i;
            }
            if (sepIndex >= 0)
                menuSize = sepIndex;
        }
        int offset = 0;
        for (int i = 0; i < index; i++){
            JMenuItem temp = getLists().get(i).getListMenuItem(actionCmd);
            if (temp == null || !menu.isMenuComponent(temp))
                offset++;
        }
        menu.insert(item, Math.min(Math.max(index-offset, 0), menuSize));
//        if (panel != null && panel.isPrivate() && !showPrivate)
//            item.setVisible(false);
    }
    
    private void addItemToMenu(String actionCmd, JMenu menu, LinksListPanel panel){
        JMenuItem item = getOrCreateListMenuItem(panel,actionCmd);
        if (item != null){
                // If the panel is null (if we're adding the menu item for the 
            if (panel == null){     // currently selected list)
                JPopupMenu.Separator separator = getOrCreateSeparator(actionCmd);
                menu.add(separator);
            }
            menu.add(item);
//            if (panel != null && panel.isPrivate() && !showPrivate)
//                item.setVisible(false);
        }
    }
    
    private void removeItemFromMenu(String actionCmd, JMenu menu, LinksListPanel panel){
        JMenuItem item = getListMenuItem(panel,actionCmd);
        if (item != null)
            menu.remove(item);
            // If the panel is null (if we're removing the menu item for the 
            // currently selected list) and there is a separator for the menu
        if (panel == null && panelActionSeparators.containsKey(actionCmd))
            menu.remove(panelActionSeparators.get(actionCmd));
    }
    
    private void addListToPanel(int index, LinksListPanel panel){
        panel.addChangeListener(listHandler);
        panel.addPropertyChangeListener(listHandler);
        panel.addListDataListener(listHandler);
        panel.addListSelectionListener(listHandler);
        for (String actionCmd : panelActionMenus.keySet()){
            insertItemIntoMenu(actionCmd,panelActionMenus.get(actionCmd),panel,
                    index);
        }
        if (panel.getListID() != null)
            removedListIDs.remove(panel.getListID());
    }
    
    private void removeListFromPanel(LinksListPanel panel){
        if (panel.getListID() != null)
            removedListIDs.add(panel.getListID());
        for (String actionCmd : panelActionMenus.keySet()){
            removeItemFromMenu(actionCmd,panelActionMenus.get(actionCmd),panel);
        }
        panel.removeChangeListener(listHandler);
        panel.removePropertyChangeListener(listHandler);
        panel.removeListDataListener(listHandler);
        panel.removeListSelectionListener(listHandler);
    }
    
    public LinksListPanel getListWithListID(int listID){
        int index = getListIDs().indexOf(listID);
        return (index >= 0) ? getLists().get(index) : null;
    }
    
    public LinksListModel getModelWithListID(int listID){
        int index = getListIDs().indexOf(listID);
        return (index >= 0) ? getModels().get(index) : null;
    }
    
    protected void updateRemovedListIDs(){
        removedListIDs.removeAll(getListIDs());
    }
    
    public Set<Integer> getRemovedListIDs(){
        updateRemovedListIDs();
        return removedListIDs;
    }
    
    public void clearRemovedListIDs(){
        removedListIDs.clear();
    }
    /**
     * This is used by the {@link #setModels(List) setModels} method
     * @param panel
     * @param modelIDs
     * @param selValues
     * @param visibleRects 
     */
    private void restoreListAfterModelChange(LinksListPanel panel, 
            Map<Integer,LinksListModel> modelIDs,
            Map<LinksListModel,String> selValues, 
            Map<LinksListModel,Rectangle> visibleRects){
        LinksListModel model = panel.getModel();
        LinksListModel idModel = modelIDs.get(model.getListID());
        String selValue = selValues.getOrDefault(model, selValues.get(idModel));
        Rectangle visibleRect = visibleRects.getOrDefault(model, visibleRects.get(idModel));
        if (visibleRect != null && !visibleRect.equals(panel.getList().getVisibleRect())){
            panel.getList().scrollRectToVisible(new Rectangle());
            panel.getList().scrollRectToVisible(visibleRect);
        }   // If the selected values map contains the old model or the new model
        if (selValues.containsKey(model) || selValues.containsKey(idModel)){
            if (selValue == null)   // If there is nothing selected for that model
                panel.clearSelection();
            else
                panel.setSelectedValue(selValue, false);
        }
    }
    
    public void setModels(List<LinksListModel> models){
            // Check if the given list is null
        Objects.requireNonNull(models);
            // Check if the given list contains null
        if (models.contains(null))
            throw new NullPointerException("Model cannot be null");
            // If the list is empty
        if (models.isEmpty()){
                // Remove all the models from this
            getModels().clear();
            return;
        }   // If the list contains all the models in this panel in the exact 
        if (models.equals(getModels())) // same order as this panel
            return;
            // Get the currently selected panel
        LinksListPanel selPanel = getSelectedList();
            // The index of the currently selected panel
        int selIndex = -1;
            // The listID of the currently selected panel, or null if the panel 
        Integer selListID = null;   // does not have a listID
            // If the currently selected panel is not null
        if (selPanel != null){
            selIndex = models.indexOf(selPanel.getModel());
            selListID = selPanel.getListID();
        }   // This gets whether no change has occurred
        boolean noChange = !structEdited && models.size()==getModels().size();
        HashMap<Integer,LinksListModel> modelIDs = new HashMap<>();
        HashMap<LinksListModel,String> selValues = new HashMap<>();
        HashMap<LinksListModel,Rectangle> visibleRects = new HashMap<>();
        for (LinksListPanel panel : this){
            LinksListModel model = panel.getModel();
            if (model.getListID() != null)
                modelIDs.put(model.getListID(), model);
            selValues.put(model, panel.getSelectedValue());
            visibleRects.put(model, panel.getList().getVisibleRect());
        }
        int index = 0;
        Iterator<LinksListPanel> itr = getLists().iterator();
        while (itr.hasNext()){
            LinksListPanel panel = itr.next();
            if (panel != null){
                LinksListModel oldModel = panel.getModel();
                if (index < models.size()){
                    LinksListModel model = models.get(index);
                    panel.setModel(model);
                        // If the new model has a listID
                    if (model.getListID() != null)
                        noChange &= Objects.equals(model.getListID(), oldModel.getListID());
                    else    // TODO: Check for equivalence instead?
                        noChange &= model.equals(oldModel);
                }
                else
                    itr.remove();
                index++;
            }
        }
        for (int m = index; m < models.size(); m++){
            LinksListPanel panel = new LinksListPanel(models.get(m));
            getLists().add(panel);
        }
        for (LinksListPanel panel : this){
            restoreListAfterModelChange(panel,modelIDs,selValues,visibleRects);
        }
        updateRemovedListIDs();
        if (selIndex < 0 && selListID != null && selListID >= 0)
            selIndex = getListIDs().indexOf(selListID);
        if (selIndex >= 0)
            setSelectedIndex(selIndex);
        setStructureEdited(!noChange);
    }
    
    public int getTabCount(){
        return tabbedPane.getTabCount();
    }
    
    public boolean isSelected(Component component){
        return getSelectedComponent() == component;
    }
    
    public boolean isSelected(int index){
        return getSelectedIndex() == index;
    }
    
    public boolean isSelectedListID(int listID){
        return Objects.equals(getSelectedListID(), listID);
    }
    
    public boolean isSelectionEmpty(){
        return getSelectedIndex() == -1;
    }
    
    public boolean isNonListSelected(){
        return getSelectedList() == null;
    }
    
    public boolean isSelectionEnabled(){
        if (isSelectionEmpty())
            return false;
        return getSelectedComponent().isEnabled();
    }
    
    public int getSelectedIndex(){
        return tabbedPane.getSelectedIndex();
    }
    
    public void setSelectedIndex(int index){
        tabbedPane.setSelectedIndex(index);
    }
    
    public Component getSelectedComponent(){
        return tabbedPane.getSelectedComponent();
    }
    
    public void setSelectedComponent(Component comp){
        tabbedPane.setSelectedComponent(comp);
    }
    
    public LinksListPanel getSelectedList(){
        Component comp = tabbedPane.getSelectedComponent();
        if (comp instanceof LinksListPanel)
            return (LinksListPanel) comp;
        return null;
    }
    
    public void setSelectedList(LinksListPanel panel){
        tabbedPane.setSelectedComponent(panel);
    }
    
    public LinksListModel getSelectedModel(){
        LinksListPanel panel = getSelectedList();
        return (panel!=null)?panel.getModel():null;
    }
    
    public Integer getSelectedListID(){
        LinksListPanel panel = getSelectedList();
        return (panel != null) ? panel.getListID() : null;
    }
    
    public void setSelectedListID(int listID){
        setSelectedIndex(getListIDs().indexOf(listID));
    }
    
    @Override
    public String getListName(LinksListPanel panel){
        return (panel != null) ? panel.getListName() : "Current List";
    }
    
    public void removeAllIDs(){
        for (LinksListPanel panel : this){
            panel.setListID(null);
            panel.getModel().setContentsModified();
        }
    }
    
    public Set<String> getAllLinksSet(){
        LinkedHashSet<String> links = new LinkedHashSet<>();
        for(LinksListModel model : getModels())
            links.addAll(model);
        return links;
    }
    
    public boolean isShowingHiddenLists(){
        return showsHiddenLists;
    }
    
    public void setShowingHiddenLists(boolean value){
        if (value == showsHiddenLists)
            return;
        showsHiddenLists = value;
        firePropertyChange(IS_SHOWING_HIDDEN_LISTS_PROPERTY_CHANGED,!value,value);
    }
    
    public void setListActionMenusVisible(boolean visible){
        for (JMenu menu : panelActionMenus.values()){
            menu.setVisible(visible);
        }
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
    
    protected String getTitleForList(LinksListPanel panel){
        String name = panel.getListName();
        if (debugMode)
            name = Objects.toString(panel.getListID(),"N/A") + ": " + name;
        if (panel.isEdited())
            name = EDITED_LIST_INDICATOR+name;
        if (panel.isHidden())
            name += " "+HIDDEN_LIST_INDICATOR;
        if (panel.isReadOnly())
            name += " "+READ_ONLY_LIST_INDICATOR;
        if (panel.getSizeLimit() != null && panel.getModel().isFull())
            name += " "+SIZE_LIMITED_FULL_LIST_INDICATOR;
        
        return name;
    }
    
    protected void updateTabTitle(int index, LinksListPanel panel){
        tabbedPane.setTitleAt(index,getTitleForList(panel));
    }
    
    protected void updateTabTitle(LinksListPanel panel){
        updateTabTitle(tabbedPane.indexOfComponent(panel),panel);
    }
    
    protected void updateTabToolTip(int index, LinksListPanel panel){
        tabbedPane.setToolTipTextAt(index, panel.getListToolTipText());
    }
    
    protected void updateTabToolTip(LinksListPanel panel){
        updateTabToolTip(tabbedPane.indexOfComponent(panel),panel);
    }
    
    @Override
    protected String paramString(){
        return super.paramString()+
                ",edited="+isEdited()+
                ",listsEdited="+getListsEdited()+
                ",structureEdited="+isStructureEdited()+
                ",selectedTab="+getSelectedIndex()+
                ((isInDebug())?",debug":"");
    }
    @Override
    public Iterator<LinksListPanel> iterator() {
        return new ListPanelIterator();
    }
    
    private class ListPanelIterator implements Iterator<LinksListPanel>{
        
        private final Iterator<LinksListPanel> itr;
        
        public ListPanelIterator(){
            itr = getLists().iterator();
        }

        @Override
        public boolean hasNext() {
            return itr.hasNext();
        }

        @Override
        public LinksListPanel next() {
            return itr.next();
        }
    }
    
    private class ListPanelList extends AbstractList<LinksListPanel>{

        @Override
        public LinksListPanel get(int index) {
            Objects.checkIndex(index, size());
            Component comp = tabbedPane.getComponentAt(index);
            return (comp instanceof LinksListPanel) ? (LinksListPanel) comp:null;
        }
        @Override
        public void add(int index, LinksListPanel panel){
            Objects.requireNonNull(panel);
            Objects.checkIndex(index, size()+1);
            tabbedPane.insertTab(getTitleForList(panel), null, panel, 
                    panel.getListToolTipText(), index);
            tabbedPane.setTabComponentAt(index, panel.getTabComponent());
            addListToPanel(index,panel);
            setStructureEdited(true);
        }
        @Override
        public LinksListPanel remove(int index){
            LinksListPanel panel = get(index);
            if (panel != null){
                removeListFromPanel(panel);
            }
            tabbedPane.removeTabAt(index);
            setStructureEdited(structEdited && panel != null);
            return panel;
        }
        @Override
        public LinksListPanel set(int index, LinksListPanel panel){
            Objects.requireNonNull(panel);
            LinksListPanel old = get(index);
            if (panel == old)
                return old;
            if (old != null)
                removeListFromPanel(panel);
            tabbedPane.setComponentAt(index, panel);
            updateTabTitle(index,panel);
            updateTabToolTip(index,panel);
            tabbedPane.setTabComponentAt(index, panel.getTabComponent());
            addListToPanel(index,panel);
            setStructureEdited(true);
            return old;
        }
        @Override
        public int size() {
            return tabbedPane.getTabCount();
        }
    }
    
    private class TabComponentList extends AbstractList<Component>{

        @Override
        public Component get(int index) {
            Objects.checkIndex(index, size());
            return tabbedPane.getTabComponentAt(index);
        }
        
        @Override
        public Component set(int index, Component comp){
            Component old = get(index);
            tabbedPane.setTabComponentAt(index, comp);
            if (index < getLists().size()){
                LinksListPanel list = getLists().get(index);
                if (list != null)
                    list.setTabComponent(comp);
            }
            return old;
        }
        @Override
        public int size() {
            return tabbedPane.getTabCount();
        }
    }
    
    private void updateListActionsEnabled(){
        for (LinksListAction action : currPanelActionMap.values())
            action.updateActionEnabled();
        for (LinksListPanel list : panels){
            if (list != null)
                list.updateActionEnabled();
        }
    }
    
    private class Handler implements ChangeListener{
        @Override
        public void stateChanged(ChangeEvent evt) {
//            if (currSelTab != tabbedPane.getSelectedIndex()){
//                if (currSelTab >= 0 && currSelTab < panels.size())
//                    
//            }
//            currSelTab = tabbedPane.getSelectedIndex();
            updateListActionsEnabled();
            fireStateChanged();
        }
    }
    
    private class ListPanelHandler implements ChangeListener, 
            PropertyChangeListener, ListDataListener, ListSelectionListener{
        
        private LinksListPanel getPanel(EventObject evt){
            if (evt.getSource() instanceof LinksListPanel){
                LinksListPanel panel = (LinksListPanel)evt.getSource();
                if (getLists().contains(panel))
                    return panel;
            }
            return null;
        }
        @Override
        public void stateChanged(ChangeEvent evt) {
            LinksListPanel panel = getPanel(evt);
            if (panel != null){
//                updateTabToolTip(panel);
                updateTabTitle(panel);
                if (panel.isEdited())
                    setEdited(true);
            }
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            LinksListPanel panel = getPanel(evt);
            if (evt.getPropertyName() == null || panel == null)
                return;
            String newPropName = null;
            switch(evt.getPropertyName()){
                case(LinksListModel.LIST_ID_PROPERTY_CHANGED):
                    newPropName = LIST_PANEL_LIST_ID_PROPERTY_CHANGED_PREFIX;
                    break;
                case(LinksListModel.LIST_NAME_PROPERTY_CHANGED):
                    newPropName = LIST_PANEL_LIST_NAME_PROPERTY_CHANGED_PREFIX;
                    break;
                case(LinksListPanel.MODEL_PROPERTY_CHANGED):
                    newPropName = LIST_PANEL_MODEL_PROPERTY_CHANGED_PREFIX;
                    break;
                case(LinksListPanel.SCROLLS_TO_BOTTOM_PROPERTY_CHANGED):
                    newPropName = LIST_PANEL_SCROLLS_TO_BOTTOM_PROPERTY_CHANGED_PREFIX;
                    break;
                case(LinksListModel.LIST_IS_HIDDEN_PROPERTY_CHANGED):
                    newPropName = LIST_IS_HIDDEN_PROPERTY_CHANGED_PREFIX;
                    break;
                case(LinksListModel.LIST_IS_READ_ONLY_PROPERTY_CHANGED):
                    newPropName = LIST_IS_READ_ONLY_PROPERTY_CHANGED_PREFIX;
            }
            int index = getLists().indexOf(panel);
            switch(evt.getPropertyName()){
                case(LinksListModel.LIST_SIZE_LIMIT_PROPERTY_CHANGED):
                    updateTabToolTip(index,panel);
                    if (panel.isEdited())
                        setEdited(true);
                    break;
                case(LinksListPanel.TAB_COMPONENT_PROPERTY_CHANGED):
                    if (Objects.equals(tabbedPane.getTabComponentAt(index),evt.getOldValue()))
                        tabbedPane.setTabComponentAt(index, panel.getTabComponent());
                    break;
                case(LinksListPanel.MODEL_PROPERTY_CHANGED):
                    if (evt.getOldValue() != null){
                        LinksListModel oldModel = (LinksListModel) evt.getOldValue();
                        if (oldModel.getListID() != null)
                            removedListIDs.add(oldModel.getListID());
                    }
                    if (evt.getNewValue() != null){
                        LinksListModel newModel = (LinksListModel) evt.getNewValue();
                        if (newModel.getListID() != null)
                            removedListIDs.remove(newModel.getListID());
                    }
                    setStructureEdited(true);
                    updateTabToolTip(index,panel);
                case(LinksListModel.LIST_IS_HIDDEN_PROPERTY_CHANGED):
                    setEdited(true);
                case(LinksListModel.LIST_ID_PROPERTY_CHANGED):
                case(LinksListModel.LIST_NAME_PROPERTY_CHANGED):
                case(LinksListModel.LIST_IS_READ_ONLY_PROPERTY_CHANGED):
                    updateTabTitle(index,panel);
                    if (panel.isEdited())
                        setEdited(true);
            }
            if (newPropName != null)
                firePropertyChange(newPropName+index,evt.getOldValue(),evt.getNewValue());
            updateListActionsEnabled();
        }
        @Override
        public void intervalAdded(ListDataEvent evt) {
            LinksListPanel panel = getPanel(evt);
            if (panel != null){
                updateTabTitle(panel);
                updateTabToolTip(panel);
            }
        }
        @Override
        public void intervalRemoved(ListDataEvent evt) {
            LinksListPanel panel = getPanel(evt);
            if (panel != null){
                updateTabTitle(panel);
                updateTabToolTip(panel);
            }
        }
        @Override
        public void contentsChanged(ListDataEvent evt) {
            LinksListPanel panel = getPanel(evt);
            if (panel != null){
                updateTabTitle(panel);
                updateTabToolTip(panel);
            }
        }
        @Override
        public void valueChanged(ListSelectionEvent evt) {
            if (getPanel(evt) != null){
                for (ListSelectionListener l : getListSelectionListeners()){
                    if (l != null)
                        l.valueChanged(evt);
                }
            }
        }
    }
    
    public class AddTabComponent extends JPanel{
        
        private final JButton addButton;
        
        public AddTabComponent(){
            super(new BorderLayout());
            addButton = new ListTabButton(ListTabButton.ADD_TAB_ACTION);
            add(addButton, BorderLayout.CENTER);
            setToolTipText("Add List");
            setOpaque(false);
        }
        
        
    }
    
//    public class ListTabComponent extends JPanel{
//        
//        
//        
//    }
}
