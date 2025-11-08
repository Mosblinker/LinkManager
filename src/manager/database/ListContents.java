/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import java.util.*;
import java.util.logging.Level;
import manager.LinkManager;
import manager.ProgressObserver;
import manager.links.LinksListModel;
import sql.UncheckedSQLException;
import sql.util.SQLList;

/**
 * This is a list view of the 
 * @author Milo Steier
 */
public interface ListContents extends SQLList<String>{
    /**
     * This returns the listID for this list in the database.
     * @return The listID for this list.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public int getListID();
    /**
     * This returns the name for this list.
     * @return The name for this list.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @see #setName(java.lang.String) 
     */
    public String getName();
    /**
     * This sets the name for this list.
     * @param name The new name for this list.
     * @throws UnsupportedOperationException If the {@code setName} operation is 
     * not supported.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @throws NullPointerException If null names are not supported and the 
     * given name is null.
     * @throws IllegalArgumentException If some property of the given name 
     * prevents it from being used as this list's name.
     * @see #getName() 
     */
    public void setName(String name);
    /**
     * This returns when this list was last modified
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public long getLastModified();
    /**
     * 
     * @param lastMod 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public void setLastModified(long lastMod);
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default long setLastModified(){
            // Get the current time
        long time = System.currentTimeMillis();
            // Set the last modified time to the current time
        setLastModified(time);
        return time;
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public long getCreationTime();
    /**
     * 
     * @param time 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public void setCreationTime(long time);
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public int getFlags();
    /**
     * 
     * @param flag
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default boolean getFlag(int flag){
        return (getFlags() & flag) == flag;
    }
    /**
     * 
     * @param flags 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public void setFlags(int flags);
    /**
     * 
     * @param flag
     * @param value 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default void setFlag(int flag, boolean value){
            // Set the flag based on whether the value is true.
        setFlags((value) ? getFlags() | flag : getFlags() & ~flag);
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public Integer getSizeLimit();
    /**
     * 
     * @param sizeLimit 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public void setSizeLimit(Integer sizeLimit);
    /**
     * This returns whether the list is full
     * @return Whether the list is full
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default boolean isFull(){
            // Get the size limit for this list
        Integer limit = getSizeLimit();
            // Return whether this list has a size limit and its size exceeds 
        return limit != null && size() >= limit;    // the size limit
    }
    /**
     * This returns whether the list exists in the database
     * @return Whether the list exists in the database
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public boolean exists();
    /**
     * 
     * @return Whether the list exists
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default boolean isHidden(){
        return false;
    }
    /**
     * 
     * @param model The model to reuse, or null
     * @param observer An observer to use to observe the progress of this 
     * method, or null.
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default LinksListModel toModel(LinksListModel model, 
            ProgressObserver observer){
            // If this list does not exist in the database
        if (!exists())
            throw new IllegalStateException("List does not exist (listID: "+
                    getListID()+")");
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "toModel",getListID());
            // If the given model is null (this is to create a new model)
        if (model == null)
            model = new LinksListModel(getName(),getListID());
        else{   // Set the model's listID
            model.setListID(getListID());
                // Set the model's name
            model.setListName(getName());
                // Clear the model
            model.clear();
        }   // Set the model's flags
        model.setFlags(getFlags());
            // Set the model's last modified time
        model.setLastModified(getLastModified());
            // Set the model's creation time
        model.setCreationTime(getCreationTime());
            // Set the model's size limit
        model.setSizeLimit(getSizeLimit());
            // Get whether the model's modification limitations are enabled
        boolean modLimit = model.isModificationLimitEnabled();
            // Disable the model's modification limitations
        model.setModificationLimitEnabled(false);
            // The size for this list
        int size = size();
            // Go through the links in this list
        for (String temp : this){
                // If the current link is null or blank
            if (temp == null || temp.isBlank())
                continue;   // Skip the link
            try{
                model.add(temp);        // Add the link to the model
                if (observer != null)   // If an observer was provided
                    observer.incrementValue();
            } catch(IllegalArgumentException | IllegalStateException ex){ 
                LinkManager.getLogger().log(Level.WARNING, 
                        "Issue adding value to model", ex);
            }
        }   // If the model does not allow duplicates
        if (!model.getAllowsDuplicates())
                // Remove any duplicates from the model
            model.removeDuplicates();
            // Restore the model's modification limitations back to what it was 
        model.setModificationLimitEnabled(modLimit);    // set to before
            // Set whether the model is hidden
        model.setHidden(isHidden());
            // Set the model to be edited if its size does not match this list's 
        model.setEdited(model.size() != size);  // size
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "toModel");
        return model;
    }
    /**
     * 
     * @param model
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default LinksListModel toModel(LinksListModel model){
        return toModel(model, null);
    }
    /**
     * 
     * @param observer
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default LinksListModel toModel(ProgressObserver observer){
        return toModel(null, observer);
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default LinksListModel toModel(){
        return toModel(null, null);
    }
    /**
     * Returns whether this list is outdated, based off the given model
     * @param model
     * @return If this list is outdated
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default boolean isOutdated(LinksListModel model){
            // Check if the model is null
        Objects.requireNonNull(model);
            // If the model is edited or it was last modified later than this 
            // list, then this list is outdated
        return model.isEdited() || model.getLastModified() >= getLastModified();
    }
    /**
     * 
     * @param model
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default boolean updateProperties(LinksListModel model){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "updateProperties",getListID());
            // Check if the model is null
        Objects.requireNonNull(model);
            // If this list is up to date
        if (!isOutdated(model)){
            LinkManager.getLogger().exiting(this.getClass().getName(), 
                    "updateProperties", false);
            return false;
        }
            // This gets whether the list in the database was modified in any way
        boolean modified = false;
            // If this list's name is not the same as the model's name
        if (!Objects.equals(getName(), model.getListName())){
                // Set this list's name
            setName(model.getListName());
                // Tell the model that its contents were modified?
            model.setContentsModified();
                // The list properties in the database were modified
            modified = true;
        }   // If the list's flags are different from the model's flags
        if (model.getFlags() != getFlags()){
            setFlags(model.getFlags());
                // The list properties in the database were modified
            modified = true;
        }   // If the list's size limit is different from the model's size limit
        if (!Objects.equals(model.getSizeLimit(), getSizeLimit())){
            setSizeLimit(model.getSizeLimit());
                // The list properties in the database were modified
            modified = true;
        }   // If the list's properties were modified
        if (modified)
                // Set the list's and model's last modified time to the current 
            model.setLastModified(setLastModified());   // time
            // If the model's contents were not modified
        if (!model.getContentsModified())
                // Clear whether the model is edited, since we've updated the 
            model.clearEdited();    // list
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "updateProperties", modified);
        return modified;
    }
    /**
     * 
     * @param model The model to save
     * @param observer An observer to use to observe the progress of this 
     * method, or null.
     * @param linkIDMap A cached map that maps the links to their linkIDs, or 
     * null.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default void updateContents(LinksListModel model, 
            ProgressObserver observer, Map<String, Long> linkIDMap){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "updateContents",getListID());
            // Check if the model is null
        Objects.requireNonNull(model);
            // If an observer has been provided
        if (observer != null)
                // Set the progress to be indeterminate
            observer.setIndeterminate(true);
        clear();    // Clear this list
            // If the given model is not empty
        if (!model.isEmpty()){
                // If an observer has been provided
            if (observer != null)
                    // Set the progress to not be indeterminate
                observer.setIndeterminate(false);
                // Go through the contents of the model
            for (int index = 0; index < model.size(); index++){
                    // Add the value at the current index
                add(model.get(index));
                    // If an observer has been provided
                if (observer != null)
                    observer.incrementValue();
            }
        }   // Set both this and the model's last modified time to the current 
        model.setLastModified(setLastModified());   // time
            // Clear whether the model has been edited
        model.clearEdited();
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "updateContents");
    }
    /**
     * 
     * @param model
     * @param observer 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default void updateContents(LinksListModel model, 
            ProgressObserver observer){
        updateContents(model,observer,null);
    }
    /**
     * 
     * @param model
     * @param linkIDMap 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default void updateContents(LinksListModel model, 
            Map<String, Long> linkIDMap){
        updateContents(model,null,linkIDMap);
    }
    /**
     * 
     * @param model 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default void updateContents(LinksListModel model){
        updateContents(model,null,null);
    }
    /**
     * 
     * @param model
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default boolean equalsModel(LinksListModel model){
            // If the given model is null
        if (model == null)
            return false;
        return model.getFlags() == getFlags() && 
                model.getCreationTime() == getCreationTime() &&
                Objects.equals(model.getSizeLimit(), getSizeLimit()) &&
                model.listEquals(new ArrayList<>(this));
    }
}
