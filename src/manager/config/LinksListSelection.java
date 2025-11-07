/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.config;

import java.awt.Rectangle;
import java.util.Collection;
import java.util.Map;
import manager.LinkManager;
import manager.links.LinksListPanel;

/**
 *
 * @author Mosblinker
 */
public interface LinksListSelection {
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
     * @param panel 
     */
    public default void setVisibleSection(int listID, LinksListPanel panel){
        LinkManager.getLogger().entering("LinksListSelection", 
                "setVisibleSection", new Object[]{listID,panel});
            // This will get the first visible index
        Integer firstVisIndex = null;
            // This will get the last visible index
        Integer lastVisIndex = null;
            // This will get whether the selected index is visible
        Boolean isSelVis = null;
            // This will get the visible rectangle for the list
        Rectangle visRect = null;
            // If the panel is not null
        if (panel != null){
                // Get the first visible index for the list
            firstVisIndex = panel.getList().getFirstVisibleIndex();
                // If the first visible index is negative
            if (firstVisIndex < 0)
                firstVisIndex = null;
                // Get the last visible index for the list
            lastVisIndex = panel.getList().getLastVisibleIndex();
                // If the last visible index is negative
            if (lastVisIndex < 0)
                lastVisIndex = null;
                // If the panel's selection is not empty
            if (!panel.isSelectionEmpty())
                    // Get whether the selected indes is visible
                isSelVis = panel.isIndexVisible(panel.getSelectedIndex());
                // Get the panel's list's visible rectangle
            visRect = panel.getList().getVisibleRect();
        }   // Set the first visible index for the list
        setFirstVisibleIndex(listID,firstVisIndex);
            // Set the last visible index for the list
        setLastVisibleIndex(listID,lastVisIndex);
            // Set whether the selected link is visible
        setSelectedLinkVisible(listID,isSelVis);
            // Set the visible rectangle for the list
        setVisibleRect(listID,visRect);
        LinkManager.getLogger().exiting("LinksListSelection", 
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
     * @return 
     */
    public boolean removeListPreferences(int listID);
    /**
     * 
     * @param listIDs
     * @return 
     */
    public default boolean removeListPreferences(Collection<Integer> listIDs){
        boolean changed = false;
        for (Integer listID : listIDs){
            if (listID != null){
                boolean removed = removeListPreferences(listID);
                changed |= removed;
            }
        }
        return changed;
    }
}
