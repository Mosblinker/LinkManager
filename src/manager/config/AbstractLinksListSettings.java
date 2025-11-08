/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.config;

import java.awt.Rectangle;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import manager.database.CacheSetIterator;

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
    /**
     * This is a map view of the current tab listIDs. This is initially null 
     * and is initialized when first used.
     */
    private Map<Integer, Integer> currTabIDMap = null;
    /**
     * 
     */
    private Set<Integer> listIDs = null;
    /**
     * 
     */
    private Set<Integer> listTypes = null;
    @Override
    public Map<Integer, String> getSelectedLinkMap() {
        if (selLinkMap == null){
            selLinkMap = new ListConfigDataMapImpl<>(){
                @Override
                protected String getValue(int key) {
                    return getSelectedLink(key);
                }
                @Override
                protected void putValue(int key, String value) {
                    setSelectedLink(key,value);
                }
                @Override
                protected Set<Integer> getAllKeys() {
                    return getListIDs();
                }
            };
        }
        return selLinkMap;
    }
    @Override
    public Map<Integer, Boolean> getSelectedLinkVisibleMap() {
        if (selLinkVisMap == null){
            selLinkVisMap = new ListConfigDataMapImpl<>(){
                @Override
                protected Boolean getValue(int key) {
                    return isSelectedLinkVisible(key);
                }
                @Override
                protected void putValue(int key, Boolean value) {
                    setSelectedLinkVisible(key,value);
                }
                @Override
                protected Set<Integer> getAllKeys() {
                    return getListIDs();
                }
            };
        }
        return selLinkVisMap;
    }
    @Override
    public Map<Integer, Integer> getFirstVisibleIndexMap() {
        if (firstVisIndexMap == null){
            firstVisIndexMap = new ListConfigDataMapImpl<>(){
                @Override
                protected Integer getValue(int key) {
                    return getFirstVisibleIndex(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setFirstVisibleIndex(key,value);
                }
                @Override
                protected Set<Integer> getAllKeys() {
                    return getListIDs();
                }
            };
        }
        return firstVisIndexMap;
    }
    @Override
    public Map<Integer, Integer> getLastVisibleIndexMap() {
        if (lastVisIndexMap == null){
            lastVisIndexMap = new ListConfigDataMapImpl<>(){
                @Override
                protected Integer getValue(int key) {
                    return getLastVisibleIndex(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setLastVisibleIndex(key,value);
                }
                @Override
                protected Set<Integer> getAllKeys() {
                    return getListIDs();
                }
            };
        }
        return lastVisIndexMap;
    }
    @Override
    public Map<Integer, Rectangle> getVisibleRectMap() {
        if (visRectMap == null){
            visRectMap = new ListConfigDataMapImpl<>(){
                @Override
                protected Rectangle getValue(int key) {
                    return getVisibleRect(key);
                }
                @Override
                protected void putValue(int key, Rectangle value) {
                    setVisibleRect(key,value);
                }
                @Override
                protected Set<Integer> getAllKeys() {
                    return getListIDs();
                }
            };
        }
        return visRectMap;
    }
    /**
     * 
     * @return 
     */
    @Override
    public Map<Integer, Integer> getSelectedListIDMap(){
        if (currTabIDMap == null){
            currTabIDMap = new ListConfigDataMapImpl<>(){
                @Override
                protected Integer getValue(int key) {
                    return getSelectedListID(key);
                }
                @Override
                protected void putValue(int key, Integer value) {
                    setSelectedListID(key,value);
                }
                @Override
                protected Set<Integer> getAllKeys() {
                    return getListTypes();
                }
            };
        }
        return currTabIDMap;
    }
    /**
     * 
     * @return 
     */
    protected abstract Set<Integer> getListIDSet();
    /**
     * 
     * @return 
     */
    protected Iterator<Integer> getListIDIterator(){
        return new CacheSetIterator<>(getListIDSet()){
            @Override
            protected void remove(Integer value) {
                removeListSettings(value);
            }
        };
    }
    /**
     * 
     * @return 
     */
    protected abstract int getListIDSize();
    /**
     * 
     * @param listID
     * @return 
     */
    protected boolean containsListID(int listID){
        return getListIDSet().contains(listID);
    }
    @Override
    public Set<Integer> getListIDs(){
        if (listIDs == null){
            listIDs = new AbstractSet<>(){
                @Override
                public Iterator<Integer> iterator() {
                    return getListIDIterator();
                }
                @Override
                public int size() {
                    return getListIDSize();
                }
                @Override
                public boolean remove(Object o){
                    if (o instanceof Integer)
                        return removeListSettings((Integer)o);
                    return false;
                }
                @Override
                public boolean contains(Object o){
                    if (o instanceof Integer)
                        return containsListID((Integer)o);
                    return false;
                }
                @Override
                public boolean removeAll(Collection<?> c){
                    Collection<Integer> t = new HashSet<>();
                    for (Object o : c){
                        if (o instanceof Integer)
                            t.add((Integer)o);
                        else if (o != null)
                            throw new ClassCastException();
                    }
                    return removeListSettings(t);
                }
                @Override
                public void clear(){
                    clearListSettings();
                }
            };
        }
        return listIDs;
    }
    /**
     * 
     * @return 
     */
    protected abstract Set<Integer> getListTypeSet();
    /**
     * 
     * @return 
     */
    protected Iterator<Integer> getListTypeIterator(){
        return new CacheSetIterator<>(getListTypeSet()){
            @Override
            protected void remove(Integer value) {
                removeSelectedTab(value);
            }
        };
    }
    /**
     * 
     * @return 
     */
    protected abstract int getListTypeSize();
    /**
     * 
     * @param listType
     * @return 
     */
    protected boolean containsListType(int listType){
        return getListTypeSet().contains(listType);
    }
    @Override
    public Set<Integer> getListTypes(){
        if (listTypes == null){
            listTypes = new AbstractSet<>(){
                @Override
                public Iterator<Integer> iterator() {
                    return getListTypeIterator();
                }
                @Override
                public int size() {
                    return getListTypeSize();
                }
                @Override
                public boolean remove(Object o){
                    if (o instanceof Integer)
                        return removeSelectedTab((Integer)o);
                    return false;
                }
                @Override
                public boolean contains(Object o){
                    if (o instanceof Integer)
                        return containsListType((Integer)o);
                    return false;
                }
                @Override
                public void clear(){
                    clearSelectedTabs();
                }
            };
        }
        return listTypes;
    }
    /**
     * 
     * @param <V> 
     */
    private abstract class ListConfigDataMapImpl<V> extends ListConfigDataMap<V>{
        /**
         * 
         * @return 
         */
        protected abstract Set<Integer> getAllKeys();
        @Override
        protected Set<Integer> getKeys(){
            return removeUnusedKeys(new TreeSet<>(getAllKeys()));
        }
    }
}
