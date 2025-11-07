/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.config;

import java.awt.Rectangle;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Mosblinker
 */
public abstract class AbstractLinksListSettings implements LinksListSettings{
    /**
     * This is a map view of the selected links for the lists. This is initially 
     * null and is initialized when first used.
     */
    private Map<Integer, String> selLinkMap = null;
    /**
     * This is a map view of whether the selected links are visible for the 
     * lists. This is initially null and is initialized when first used.
     */
    private Map<Integer, Boolean> selLinkVisMap = null;
    /**
     * This is a map view of the first visible indexes in the lists. This is 
     * initially null and is initialized when first used.
     */
    private Map<Integer, Integer> firstVisIndexMap = null;
    /**
     * This is a map view of the last visible indexes in the lists. This is 
     * initially null and is initialized when first used.
     */
    private Map<Integer, Integer> lastVisIndexMap = null;
    /**
     * This is a map view of the visible rectangles for the lists. This is 
     * initially null and is initialized when first used.
     */
    private Map<Integer, Rectangle> visRectMap = null;
    @Override
    public Map<Integer, String> getSelectedLinkMap() {
        if (selLinkMap == null){
            selLinkMap = new ListIDConfigDataMap<>(){
                @Override
                protected String getValue(int key) {
                    return getSelectedLink(key);
                }
                @Override
                protected void putValue(int key, String value) {
                    setSelectedLink(key,value);
                }
            };
        }
        return selLinkMap;
    }
    @Override
    public Map<Integer, Boolean> getSelectedLinkVisibleMap() {
        if (selLinkVisMap == null){
            selLinkVisMap = new ListIDConfigDataMap<>(){
                @Override
                protected Boolean getValue(int key) {
                    return isSelectedLinkVisible(key);
                }
                @Override
                protected void putValue(int key, Boolean value) {
                    setSelectedLinkVisible(key,value);
                }
            };
        }
        return selLinkVisMap;
    }
    @Override
    public Map<Integer, Integer> getFirstVisibleIndexMap() {
        if (firstVisIndexMap == null){
            firstVisIndexMap = new ListIDConfigDataMap<>(){
                @Override
                protected Integer getValue(int key) {
                    return getFirstVisibleIndex(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setFirstVisibleIndex(key,value);
                }
            };
        }
        return firstVisIndexMap;
    }
    @Override
    public Map<Integer, Integer> getLastVisibleIndexMap() {
        if (lastVisIndexMap == null){
            lastVisIndexMap = new ListIDConfigDataMap<>(){
                @Override
                protected Integer getValue(int key) {
                    return getLastVisibleIndex(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setLastVisibleIndex(key,value);
                }
            };
        }
        return lastVisIndexMap;
    }
    @Override
    public Map<Integer, Rectangle> getVisibleRectMap() {
        if (visRectMap == null){
            visRectMap = new ListIDConfigDataMap<>(){
                @Override
                protected Rectangle getValue(int key) {
                    return getVisibleRect(key);
                }
                @Override
                protected void putValue(int key, Rectangle value) {
                    setVisibleRect(key,value);
                }
            };
        }
        return visRectMap;
    }
    /**
     * 
     * @param <V> 
     */
    private abstract class ListIDConfigDataMap<V> extends ListConfigDataMap<V>{
        @Override
        protected Set<Integer> getKeys(){
            return removeUnusedKeys(new TreeSet<>(getListIDs()));
        }
    }
}
