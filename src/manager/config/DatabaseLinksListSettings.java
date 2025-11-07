/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.config;

import java.awt.Rectangle;
import java.sql.SQLException;
import java.util.Map;
import manager.LinkManager;
import manager.database.LinkDatabaseConnection;
import manager.database.LinkMap;
import manager.links.LinksListPanel;
import sql.UncheckedSQLException;

/**
 *
 * @author Mosblinker
 */
public interface DatabaseLinksListSettings extends LinksListSettings{
    /**
     * 
     * @return 
     */
    public LinkDatabaseConnection getConnection();
    /**
     * 
     * @return 
     */
    public int getProgramID();
    /**
     * 
     * @return 
     */
    public default LinkMap getDefaultLinkMap(){
        try {
            return getConnection().getLinkMap();
        } catch (SQLException ex) {
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * 
     * @return 
     */
    public default Map<String,Long> getDefaultLinkIDMap(){
        return getDefaultLinkMap().inverse();
    }
    /**
     * 
     * @param listID
     * @param value 
     */
    public void setSelectedLinkID(int listID, Long value);
    /**
     * 
     * @param listID
     * @param value
     * @param linkIDMap 
     */
    public default void setSelectedLink(int listID, String value, 
            Map<String,Long> linkIDMap){
        LinkManager.getLogger().entering("DatabaseLinksListSettings", 
                "setSelectedLink",new Object[]{listID,value,getProgramID()});
        if (linkIDMap == null)
            linkIDMap = getDefaultLinkIDMap();
        setSelectedLinkID(listID,linkIDMap.get(value));
        LinkManager.getLogger().exiting("DatabaseLinksListSettings", 
                "setSelectedLink");
    }
    @Override
    public default void setSelectedLink(int listID, String value){
        setSelectedLink(listID,value,getDefaultLinkIDMap());
    }
    /**
     * 
     * @param listID
     * @return 
     */
    public Long getSelectedLinkID(int listID);
    /**
     * 
     * @param listID
     * @param linkMap
     * @return 
     */
    public default String getSelectedLink(int listID, Map<Long,String> linkMap){
        if (linkMap == null)
            linkMap = getDefaultLinkMap();
        return linkMap.get(getSelectedLinkID(listID));
    }
    @Override
    public default String getSelectedLink(int listID){
        return getSelectedLink(listID,getDefaultLinkMap());
    }
    /**
     * 
     * @param listID
     * @param linkID
     * @param isVisible
     * @param firstIndex
     * @param lastIndex
     * @param visibleRect 
     */
    public default void setListSettings(int listID, Long linkID, Boolean isVisible, 
            Integer firstIndex, Integer lastIndex, Rectangle visibleRect){
        LinkManager.getLogger().entering("DatabaseLinksListSettings", 
                "setListSettings", new Object[]{listID,linkID,isVisible,firstIndex,
                    lastIndex,visibleRect});
        if (linkID == null)
            isVisible = null;
        setSelectedLinkID(listID,linkID);
        setVisibleSection(listID,isVisible,firstIndex,lastIndex,visibleRect);
        LinkManager.getLogger().exiting("DatabaseLinksListSettings", 
                "setListSettings");
    }
    /**
     * 
     * @param listID
     * @param selection
     * @param isVisible
     * @param firstIndex
     * @param lastIndex
     * @param visibleRect
     * @param linkIDMap 
     */
    public default void setListSettings(int listID, String selection, 
            Boolean isVisible, Integer firstIndex, Integer lastIndex, 
            Rectangle visibleRect, Map<String,Long> linkIDMap){
        LinkManager.getLogger().entering("DatabaseLinksListSettings", 
                "setListSettings", new Object[]{listID,selection,isVisible,firstIndex,
                    lastIndex,visibleRect});
        if (linkIDMap == null)
            linkIDMap = getDefaultLinkIDMap();
        setListSettings(listID,linkIDMap.get(selection),isVisible,firstIndex,
                lastIndex,visibleRect);
        LinkManager.getLogger().exiting("DatabaseLinksListSettings", 
                "setListSettings");
    }
    @Override
    public default void setListSettings(int listID, String selection, 
            Boolean isVisible, Integer firstIndex, Integer lastIndex, 
            Rectangle visibleRect){
        setListSettings(listID,selection,isVisible,firstIndex,lastIndex,
                visibleRect,getDefaultLinkIDMap());
    }
    /**
     * 
     * @param listID
     * @param panel
     * @param linkIDMap 
     */
    public default void setListSettings(int listID, LinksListPanel panel, 
            Map<String,Long> linkIDMap){
        LinkManager.getLogger().entering("DatabaseLinksListSettings", 
                "setListSettings", new Object[]{listID,panel});
        String selection = null;
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
            selection = panel.getSelectedValue();
        }
        setListSettings(listID,selection,isVisible,firstIndex,lastIndex,
                visibleRect,linkIDMap);
        LinkManager.getLogger().exiting("DatabaseLinksListSettings", 
                "setListSettings");
    }
    @Override
    public default void setListSettings(int listID, LinksListPanel panel){
        setListSettings(listID,panel,getDefaultLinkIDMap());
    }
    /**
     * 
     * @param panel
     * @param linkIDMap 
     */
    public default void setListSettings(LinksListPanel panel, Map<String,Long> linkIDMap){
            // If the panel is not null and has a non-null listID
        if (panel != null && panel.getListID() != null)
            setListSettings(panel.getListID(),panel,linkIDMap);
    }
}
