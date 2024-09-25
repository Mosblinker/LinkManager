/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import java.awt.event.ActionEvent;

/**
 *
 * @author Milo Steier
 */
public abstract class LinksListTabAction extends LinksListAction{
    /**
     * 
     */
    public static final String TABS_PANEL_KEY = "TabsPanelKey";
    /**
     * 
     * @param name
     * @param actionCmd
     * @param tabsPanel
     * @param panel 
     */
    public LinksListTabAction(String name, String actionCmd, 
            LinksListTabsPanel tabsPanel, LinksListPanel panel){
        super(name,actionCmd,panel);
        putValue(TABS_PANEL_KEY,tabsPanel);
        LinksListTabAction.this.updateActionEnabled();
    }
    /**
     * 
     * @param name
     * @param actionCmd
     * @param tabsPanel 
     */
    public LinksListTabAction(String name, String actionCmd, 
            LinksListTabsPanel tabsPanel){
        this(name,actionCmd,tabsPanel,null);
    }
    /**
     * 
     * @param actionCmd
     * @param tabsPanel
     * @param panel 
     */
    public LinksListTabAction(String actionCmd, LinksListTabsPanel tabsPanel, 
            LinksListPanel panel){
        this(actionCmd,actionCmd,tabsPanel,panel);
        LinksListTabAction.this.updateActionName();
    }
    /**
     * 
     * @param actionCmd
     * @param tabsPanel 
     */
    public LinksListTabAction(String actionCmd, LinksListTabsPanel tabsPanel){
        this(actionCmd,tabsPanel,null);
    }
    /**
     * 
     * @param tabsPanel
     * @param panel 
     */
    public LinksListTabAction(LinksListTabsPanel tabsPanel,LinksListPanel panel){
        this(null,tabsPanel,panel);
    }
    /**
     * 
     * @param tabsPanel 
     */
    public LinksListTabAction(LinksListTabsPanel tabsPanel){
        this(tabsPanel,null);
    }
    /**
     * 
     */
    public LinksListTabAction(){
        this(null,null,null,null);
    }
    /**
     * {@inheritDoc } This forwards the call to the {@link 
     * #actionPerformed(ActionEvent, LinksListPanel, LinksListTabsPanel) other actionPerformed} 
     * method with the given {@code ActionEvent} and {@code LinksListPanel}, 
     * along with the {@code LinksListTabsPanel} returned by {@link 
     * #getTabsPanel()}. In other words, this is 
     * equivalent to calling {@code actionPerformed(evt,panel,getTabsPanel())}.
     */
    @Override
    public void actionPerformed(ActionEvent evt,LinksListPanel panel){
        actionPerformed(evt,panel,getTabsPanel());
    }
    /**
     * Invoked when an action occurs. 
     * @param evt The event to be processed.
     * @param panel The LinksListPanel to use for the event.
     * @param tabsPanel The LinksListTabsPanel to use for the event.
     */
    public abstract void actionPerformed(ActionEvent evt,LinksListPanel panel,
            LinksListTabsPanel tabsPanel);
    
    @Override
    public String getListName(LinksListPanel panel){
        LinksListTabsPanel tabsPanel = getTabsPanel();
        return (tabsPanel != null) ? tabsPanel.getListName(panel) : 
                super.getListName(panel);
    }
    /**
     * 
     * @return 
     */
    public LinksListTabsPanel getTabsPanel(){
        Object value = getValue(TABS_PANEL_KEY);
        return (value instanceof LinksListTabsPanel) ? 
                (LinksListTabsPanel) value : null;
    }
    /**
     * 
     * @param panel 
     */
    public void setTabsPanel(LinksListTabsPanel panel){
        putValue(TABS_PANEL_KEY,panel);
        updateActionName();
    }
    @Override
    public LinksListPanel getPanel(){
        if (isForSelectedList())
            return getTabsPanel().getSelectedList();
        return super.getPanel();
    }
    @Override
    public boolean isForSelectedList(){
        return getValue(PANEL_KEY) == null && getTabsPanel() != null;
    }
    @Override
    protected Boolean isListSelected(LinksListPanel panel){
        if (getTabsPanel() != null)
            return getTabsPanel().isSelected(panel);
        return null;
    }
    @Override
    protected Boolean isNonListSelected(){
        if (getTabsPanel() != null)
            return getTabsPanel().isNonListSelected();
        return null;
    }
    @Override
    public void updateActionName(){
        setName(getNewActionName((isForSelectedList())?null:super.getPanel()));
    }
    @Override
    protected String getNewActionName(LinksListPanel panel){
        return super.getNewActionName(panel);
    }
//    @Override
//    public void updateActionEnabled(){
//        if (getTabsPanel() != null){
//            boolean isListSelected = getTabsPanel().isSelected(getPanel());
//            if (isForSelectedList())
//                setEnabled(!getTabsPanel().isNonListSelected());
//            else if (getListMustBeSelected())
//                setEnabled(isListSelected);
//            else if (getListMustNotBeSelected())
//                setEnabled(!isListSelected);
//        }
//    }
    /**
     * 
     */
    public static abstract class LinksListTabEditAction extends LinksListTabAction{
        /**
         * 
         * @param name
         * @param actionCmd
         * @param tabsPanel
         * @param panel 
         */
        public LinksListTabEditAction(String name, String actionCmd, 
            LinksListTabsPanel tabsPanel, LinksListPanel panel){
            super(name,actionCmd,tabsPanel,panel);
        }
        /**
         * 
         * @param name
         * @param actionCmd
         * @param tabsPanel 
         */
        public LinksListTabEditAction(String name, String actionCmd, 
            LinksListTabsPanel tabsPanel){
            super(name,actionCmd,tabsPanel);
        }
        /**
         * 
         * @param actionCmd
         * @param tabsPanel
         * @param panel 
         */
        public LinksListTabEditAction(String actionCmd, 
                LinksListTabsPanel tabsPanel, LinksListPanel panel){
            super(actionCmd,tabsPanel,panel);
        }
        /**
         * 
         * @param actionCmd
         * @param tabsPanel 
         */
        public LinksListTabEditAction(String actionCmd, 
                LinksListTabsPanel tabsPanel){
            super(actionCmd,tabsPanel);
        }
        /**
         * 
         * @param tabsPanel
         * @param panel 
         */
        public LinksListTabEditAction(LinksListTabsPanel tabsPanel,
                LinksListPanel panel){
            super(tabsPanel,panel);
        }
        /**
         * 
         * @param tabsPanel 
         */
        public LinksListTabEditAction(LinksListTabsPanel tabsPanel){
            super(tabsPanel);
        }
        /**
         * 
         */
        public LinksListTabEditAction(){
            super();
        }
        @Override
        public int getActionControlFlags(){
            return super.getActionControlFlags() | 
                    LIST_MUST_NOT_BE_FULL_FLAG;
        }
        @Override
        public int getRequiredFlags(){
            return super.getRequiredFlags() | LinksListModel.READ_ONLY_FLAG;
        }
    }
}
