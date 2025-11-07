/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.config;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import manager.LinkManager;
import manager.links.LinksListPanel;

/**
 *
 * @author Mosblinker
 */
public interface LinksListSettings {
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setSelectedLink(int listID, String value);
    /**
     * 
     * @param listID
     * @return 
     */
    public String getSelectedLink(int listID);
    /**
     * 
     * @return 
     */
    public Map<Integer,String> getSelectedLinkMap();
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setSelectedLinkVisible(int listID, Boolean value);
    /**
     * 
     * @param listID
     * @return 
     */
    public Boolean isSelectedLinkVisible(int listID);
    /**
     * 
     * @return 
     */
    public Map<Integer,Boolean> getSelectedLinkVisibleMap();
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setFirstVisibleIndex(int listID, Integer value);
    /**
     * 
     * @param listID
     * @return 
     */
    public Integer getFirstVisibleIndex(int listID);
    /**
     * 
     * @return 
     */
    public Map<Integer, Integer> getFirstVisibleIndexMap();
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setLastVisibleIndex(int listID, Integer value);
    /**
     * 
     * @param listID
     * @return 
     */
    public Integer getLastVisibleIndex(int listID);
    /**
     * 
     * @return 
     */
    public Map<Integer, Integer> getLastVisibleIndexMap();
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setVisibleRect(int listID, Rectangle value);
    /**
     * 
     * @param listID
     * @param defaultValue
     * @return 
     */
    public Rectangle getVisibleRect(int listID, Rectangle defaultValue);
    /**
     * 
     * @param listID
     * @return 
     */
    public default Rectangle getVisibleRect(int listID){
        return getVisibleRect(listID,null);
    }
    /**
     * 
     * @return 
     */
    public Map<Integer, Rectangle> getVisibleRectMap();
    /**
     * 
     * @param listID
     * @param isVisible whether the selected index is visible
     * @param firstIndex first visible index
     * @param lastIndex last visible index
     * @param visibleRect visible rectangle for the list
     */
    public default void setVisibleSection(int listID, Boolean isVisible, 
            Integer firstIndex, Integer lastIndex, Rectangle visibleRect){
        LinkManager.getLogger().entering("LinksListSettings", 
                "setVisibleSection", new Object[]{listID,isVisible,firstIndex,
                    lastIndex,visibleRect});
            // If the first visible index is negative
        if (firstIndex < 0)
            firstIndex = null;
            // If the last visible index is negative
        if (lastIndex < 0)
            lastIndex = null;
            // Set the first visible index for the list
        setFirstVisibleIndex(listID,firstIndex);
            // Set the last visible index for the list
        setLastVisibleIndex(listID,lastIndex);
            // Set whether the selected link is visible
        setSelectedLinkVisible(listID,isVisible);
            // Set the visible rectangle for the list
        setVisibleRect(listID,visibleRect);
        LinkManager.getLogger().exiting("LinksListSettings", 
                "setVisibleSection");
    }
    /**
     * 
     * @param listID
     * @param panel 
     */
    public default void setVisibleSection(int listID, LinksListPanel panel){
        LinkManager.getLogger().entering("LinksListSettings", 
                "setVisibleSection", new Object[]{listID,panel});
            // This will get the first visible index
        Integer firstIndex = null;
            // This will get the last visible index
        Integer lastIndex = null;
            // This will get whether the selected index is visible
        Boolean isVisible = null;
            // This will get the visible rectangle for the list
        Rectangle visibleRect = null;
            // If the panel is not null
        if (panel != null){
                // Get the first visible index for the list
            firstIndex = panel.getList().getFirstVisibleIndex();
                // Get the last visible index for the list
            lastIndex = panel.getList().getLastVisibleIndex();
                // If the panel's selection is not empty
            if (!panel.isSelectionEmpty())
                    // Get whether the selected indes is visible
                isVisible = panel.isIndexVisible(panel.getSelectedIndex());
                // Get the panel's list's visible rectangle
            visibleRect = panel.getList().getVisibleRect();
        }
        setVisibleSection(listID,isVisible,firstIndex,lastIndex,visibleRect);
        LinkManager.getLogger().exiting("LinksListSettings", 
                "setVisibleSection");
    }
    /**
     * 
     * @param panel 
     */
    public default void setVisibleSection(LinksListPanel panel){
            // If the panel is not null and has a non-null listID
        if (panel != null && panel.getListID() != null)
            setVisibleSection(panel.getListID(),panel);
    }
    /**
     * 
     * @param listID
     * @param selection
     * @param isVisible whether the selected index is visible
     * @param firstIndex first visible index
     * @param lastIndex last visible index
     * @param visibleRect visible rectangle for the list
     */
    public default void setListSettings(int listID, String selection, 
            Boolean isVisible, Integer firstIndex, Integer lastIndex, 
            Rectangle visibleRect){
        LinkManager.getLogger().entering("LinksListSettings", 
                "setListSettings", new Object[]{listID,selection,isVisible,firstIndex,
                    lastIndex,visibleRect});
        if (selection == null)
            isVisible = null;
        setSelectedLink(listID,selection);
        setVisibleSection(listID,isVisible,firstIndex,lastIndex,visibleRect);
        LinkManager.getLogger().exiting("LinksListSettings", 
                "setListSettings");
    }
    /**
     * 
     * @param listID
     * @param panel 
     */
    public default void setListSettings(int listID, LinksListPanel panel){
        LinkManager.getLogger().entering("LinksListSettings", 
                "setListSettings", new Object[]{listID,panel});
        String selection = (panel!=null)?panel.getSelectedValue():null;
        setSelectedLink(listID,selection);
        setVisibleSection(listID,panel);
        LinkManager.getLogger().exiting("LinksListSettings", 
                "setListSettings");
    }
    /**
     * 
     * @param panel 
     */
    public default void setListSettings(LinksListPanel panel){
            // If the panel is not null and has a non-null listID
        if (panel != null && panel.getListID() != null)
            setListSettings(panel.getListID(),panel);
    }
    /**
     * 
     * @param listID
     * @return 
     */
    public boolean removeListSettings(int listID);
    /**
     * 
     * @param listIDs
     * @return 
     */
    public default boolean removeListSettings(Collection<Integer> listIDs){
        boolean changed = false;
        for (Integer listID : listIDs){
            if (listID != null){
                boolean removed = removeListSettings(listID);
                changed |= removed;
            }
        }
        return changed;
    }
    /**
     * 
     * @return 
     */
    public Set<Integer> getListIDs();
    
//    public void removeAll
}
