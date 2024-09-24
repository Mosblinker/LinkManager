/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import java.util.Objects;
import manager.links.LinksListModel;
import sql.UncheckedSQLException;
import sql.util.*;

/**
 * This is a map view of the list table in the database storing the links which 
 * maps the list IDs to the names of the lists. This map permits null and 
 * duplicate values. The keys in this map are often referred to as list IDs, and 
 * the values are often referred to as list names or just names. 
 * @author Milo Steier
 * @see ListContents
 * @see ListDataMap
 */
public interface ListNameMap extends SQLRowMap<Integer, String>{
    /**
     * {@inheritDoc } This will also create a new list in the database with the 
     * given name that was created and modified just now. The list will use the 
     * default flags and size limit.
     * @param value {@inheritDoc } This is also the name of the list to create 
     * in the database.
     * @return {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc }
     * @throws ClassCastException {@inheritDoc }
     * @throws NullPointerException {@inheritDoc }
     * @throws IllegalArgumentException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     * @see #add(java.lang.String, int) 
     * @see #add(java.lang.String, long) 
     * @see #add(java.lang.String, int, java.lang.Integer) 
     * @see #add(java.lang.String, long, int) 
     * @see #add(java.lang.String, long, long) 
     * @see #add(java.lang.String, long, int, java.lang.Integer) 
     * @see #add(java.lang.String, long, long, int) 
     * @see #add(java.lang.String, long, long, int, java.lang.Integer) 
     * @see #add(manager.links.LinksListModel) 
     * @see #addIfAbsent(java.lang.Object) 
     * @see #addIfAbsent(manager.links.LinksListModel) 
     */
    @Override
    public Integer add(String value);
    /**
     * This adds a 
     * @param value
     * @param lastMod
     * @param created
     * @param flags
     * @param sizeLimit
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public Integer add(String value,long lastMod,long created,int flags,
            Integer sizeLimit);
    /**
     * 
     * @param value
     * @param lastMod
     * @param created
     * @param flags
     * @return 
     */
    public default Integer add(String value,long lastMod,long created,int flags){
        return add(value,lastMod,created,flags,null);
    }
    /**
     * 
     * @param value
     * @param lastMod
     * @param created
     * @return 
     */
    public default Integer add(String value, long lastMod, long created){
        return add(value,lastMod,created,0);
    }
    /**
     * 
     * @param value
     * @param lastMod
     * @param flags
     * @param sizeLimit
     * @return 
     */
    public default Integer add(String value,long lastMod,int flags, 
            Integer sizeLimit){
        return add(value,lastMod,lastMod,flags,sizeLimit);
    }
    /**
     * 
     * @param value
     * @param lastMod
     * @param flags
     * @return 
     */
    public default Integer add(String value,long lastMod,int flags){
        return add(value,lastMod,lastMod,flags);
    }
    /**
     * 
     * @param value
     * @param lastMod
     * @return 
     */
    public default Integer add(String value, long lastMod){
        return add(value,lastMod,lastMod);
    }
    /**
     * 
     * @param value
     * @param flags
     * @param sizeLimit
     * @return 
     */
    public default Integer add(String value,int flags,Integer sizeLimit){
        return add(value,System.currentTimeMillis(),flags,sizeLimit);
    }
    /**
     * 
     * @param value
     * @param flags
     * @return 
     */
    public default Integer add(String value,int flags){
        return add(value,System.currentTimeMillis(),flags);
    }
    /**
     * 
     * @param model
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default Integer add(LinksListModel model){
        Objects.requireNonNull(model);
            // Should this be the case for adding a model that may already exist?
        model.setLastModified();
        model.setCreationTime(model.getLastModified());
            // Tell the model to consider its contents modified (this tells the 
            // program that the model's contents will need to be saved to the 
        model.setContentsModified();    // database)
        return add(model.getListName(),model.getLastModified(),
                model.getCreationTime(),model.getFlags(),model.getSizeLimit());
    }
    /**
     * 
     * @param model
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default Integer addIfAbsent(LinksListModel model){
        Objects.requireNonNull(model);
            // Get the listID for the model
        Integer listID = model.getListID();
            // If the model does not have a listID or the map does not currently 
        if (listID == null || !containsKey(listID)){    // contain the list
            model.setListID(add(model));
        }
        return model.getListID();
    }
    /**
     * This removes any currently unused lists from this map. An unused list is 
     * a list that does not appear in any list of lists, and thus would never be 
     * shown by the program. <p>
     * 
     * Implementations of this map that cannot access any of the lists of lists 
     * in the database should throw an {@code UnsupportedOperationException}.
     * 
     * @return {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc } If the lists of 
     * lists in the database cannot be accessed by this map.
     * @throws UncheckedSQLException {@inheritDoc }
     */
    @Override
    public boolean removeUnusedRows();
    /**
     * This throws an {@code UnsupportedOperationException} since what counts as 
     * a duplicate list is more difficult than just checking if two list names 
     * are the same, and removing duplicate lists may result in data loss.
     * @return {@inheritDoc }
     * @throws UnsupportedOperationException {@inheritDoc }
     * @throws UncheckedSQLException {@inheritDoc }
     * @see #removeUnusedRows
     * @see #firstKeyFor 
     */
    @Override
    public default boolean removeDuplicateRows(){
        throw new UnsupportedOperationException("removeDuplicateRows");
    }
}
