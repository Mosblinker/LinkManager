/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import manager.LinkManager;
import manager.ProgressObserver;
import static manager.database.LinkDatabaseConnection.*;
import manager.links.LinksListModel;
import sql.UncheckedSQLException;
import sql.util.ConnectionBased;

/**
 *
 * @author Milo Steier
 */
class ListContentsImpl extends AbstractQueryList<String> implements ListContents{
    /**
     * This is a template for the SQL query for getting the metadata for a list 
     * with a given listID.
     */
    private static final String GET_LIST_METADATA_QUERY_TEMPLATE = 
            String.format("SELECT %%s FROM %s WHERE %s = ?", 
                    LIST_TABLE_NAME,
                    LIST_ID_COLUMN_NAME);
    /**
     * This is a template for the SQL query for setting the metadata for a list 
     * with a given listID.
     */
    private static final String SET_LIST_METADATA_QUERY_TEMPLATE = 
            String.format("UPDATE %s SET %%s = ? WHERE %s = ?", 
                    LIST_TABLE_NAME,
                    LIST_ID_COLUMN_NAME);
    /**
     * The listID of the list
     */
    private final int listID;
    /**
     * 
     * @param listID The listID of the list
     */
    public ListContentsImpl(LinkDatabaseConnection conn, int listID){
        super(conn);
        this.listID = listID;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public int getListID() {
        return listID;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean isHidden(){
        try {
            return !getConnection().getShownListIDs().contains(listID);
        } catch (SQLException ex) {
            LinkManager.getLogger().log(Level.WARNING, 
                    "Failed to get if list " + listID + " is hidden.", ex);
            appendWarning(ex);
            return false;
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean exists() {
        try {
            return getConnection().containsListID(listID);
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * This checks whether the list exists and, if not, throws an {@code 
     * IllegalStateException}.
     * @throws SQLException If a database error occurs.
     * @throws IllegalStateException If the list does not exist.
     * @see #exists() 
     * @see #getListID() 
     */
    private void requireListExists() throws SQLException{
            // If this list does not exist
        if (!exists()){
            throw new IllegalStateException("List with ID "+listID+" does not exist");
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public String getName() {
        try {
            requireListExists();    // Require the list to exist
                // Get the list's name
            return getConnection().getListNameMap().get(listID);
        } catch (SQLException ex) {
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void setName(String name) {
        try {
            requireListExists();    // Requre the list to exist
                // Set the list's name
            getConnection().getListNameMap().put(listID, name);
        } catch (SQLException ex) {
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public long getLastModified() {
        try{
            requireListExists();    // Requre the list to exist
                // Prepare a statement to get the list's last modified time
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(GET_LIST_METADATA_QUERY_TEMPLATE, 
                            LIST_LAST_MODIFIED_COLUMN_NAME))) {
                    // Set the listID of the list to get the last modified time 
                pstmt.setInt(1, listID);    // for
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If the query has any results
                if (rs.next())
                    return rs.getLong(LIST_LAST_MODIFIED_COLUMN_NAME);
                return 0;
            }
        } catch (SQLException ex){
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void setLastModified(long lastMod) {
        try{
            requireListExists();    // Requre the list to exist
                // Prepare a statement to set the last modified time of the list
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(SET_LIST_METADATA_QUERY_TEMPLATE, 
                            LIST_LAST_MODIFIED_COLUMN_NAME))) {
                    // Set the new last modified time
                pstmt.setLong(1, lastMod);
                    // Set the listID of the list to alter
                pstmt.setInt(2, listID);
                    // Update the database
                pstmt.executeUpdate();
            }
        } catch (SQLException ex){
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public long getCreationTime() {
        try{
            requireListExists();    // Requre the list to exist
                // Prepare a statement to get the list's creation time
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(GET_LIST_METADATA_QUERY_TEMPLATE, 
                            LIST_CREATED_COLUMN_NAME))) {
                    // Set the listID of the list to use
                pstmt.setInt(1, listID);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If the query returned any results
                if (rs.next())
                    return rs.getLong(LIST_CREATED_COLUMN_NAME);
                return 0;
            }
        } catch (SQLException ex){
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void setCreationTime(long time) {
        try{
            requireListExists();    // Requre the list to exist
                // Prepare a statement to set the list's creation time
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(SET_LIST_METADATA_QUERY_TEMPLATE, 
                            LIST_CREATED_COLUMN_NAME))) {
                    // Set the new creation time for the list
                pstmt.setLong(1, time);
                    // Set the listID of the list to alter
                pstmt.setInt(2, listID);
                    // Update the database
                pstmt.executeUpdate();
            }
        } catch (SQLException ex){
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public int getFlags() {
        try{
            requireListExists();    // Requre the list to exist
                // Prepare a statement to get the list's flags
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(GET_LIST_METADATA_QUERY_TEMPLATE, 
                            LIST_FLAGS_COLUMN_NAME))) {
                    // Set the listID of the list to use
                pstmt.setInt(1, listID);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If the query returned any results
                if (rs.next())
                    return rs.getInt(LIST_FLAGS_COLUMN_NAME);
                return 0;
            }
        } catch (SQLException ex){
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void setFlags(int flags) {
        try{
            requireListExists();    // Requre the list to exist
                // Prepare a statement to set the list's flags
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(SET_LIST_METADATA_QUERY_TEMPLATE, 
                            LIST_FLAGS_COLUMN_NAME))) {
                    // Set the list's new flags
                pstmt.setInt(1, flags);
                    // Set the listID of the list to alter
                pstmt.setInt(2, listID);
                    // Update the database
                pstmt.executeUpdate();
            }
        } catch (SQLException ex){
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public Integer getSizeLimit() {
        try{
            requireListExists();    // Requre the list to exist
                // Prepare a statement to get the list's size limit
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(GET_LIST_METADATA_QUERY_TEMPLATE, 
                            LIST_SIZE_LIMIT_COLUMN_NAME))) {
                    // Set the listID of the list to use
                pstmt.setInt(1, listID);
                    // Query the database
                ResultSet rs = pstmt.executeQuery();
                    // If the query returned any results
                if (rs.next()){
                        // Get the size limit
                    int limit = rs.getInt(LIST_SIZE_LIMIT_COLUMN_NAME);
                        // If the size limit was null, return null. 
                        // Otherwise, return the size limit
                    return (rs.wasNull()) ? null : limit;
                }
                return 0;
            }
        } catch (SQLException ex){
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void setSizeLimit(Integer sizeLimit) {
        try{
            requireListExists();    // Requre the list to exist
                // Prepare a statement to set the list's size limit
            try (PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(SET_LIST_METADATA_QUERY_TEMPLATE, 
                            LIST_SIZE_LIMIT_COLUMN_NAME))) {
                    // Set the new size limit for the list
                setParameter(pstmt,1,sizeLimit);
                    // Set the listID of the list to alter
                pstmt.setInt(2, listID);
                    // Update the database
                pstmt.executeUpdate();
            }
        } catch (SQLException ex){
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getTableName(){
        return LIST_DATA_TABLE_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getDataViewName(){
        return LIST_CONTENTS_VIEW_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getTypeIDColumn(){
        return LIST_ID_COLUMN_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getIndexColumn(){
        return LINK_INDEX_COLUMN_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getElementColumn(){
        return LINK_ID_COLUMN_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getDataElementColumn(){
        return LINK_URL_COLUMN_NAME;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void setPreparedTypeID(PreparedStatement pstmt, 
            int parameterIndex)throws SQLException{
        pstmt.setInt(parameterIndex, listID);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void setPreparedElement(PreparedStatement pstmt, 
            int parameterIndex, String element)throws SQLException{
        pstmt.setString(parameterIndex, element);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void setReplaceIndexElement(PreparedStatement pstmt, 
            int parameterIndex, String element)throws SQLException{
        setParameter(pstmt,parameterIndex,getConnection().getLinkMap().addIfAbsent(element));
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getElementFromResults(ResultSet rs)throws SQLException{
        return rs.getString(LINK_URL_COLUMN_NAME);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected int indexOfSQL(Object o, boolean descending) throws SQLException {
        requireListExists();    // Requre the list to exist
            // If the object is not null and not a String
        if (o != null && !(o instanceof String))
            return -1;
        return indexOfElement((String)o,descending);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected int sizeSQL() throws SQLException{
        if (!exists())  // If the list does not exist
            return 0;
        return getConnection().getListSize(listID);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected String getSQL(int index) throws SQLException {
        requireListExists();    // Requre the list to exist
        return super.getSQL(index);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void checkElement(String element)throws SQLException {
        requireListExists();    // Requre the list to exist
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public LinksListModel toModel(LinksListModel model, 
            ProgressObserver observer){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "toModel");
        try{    
            requireListExists();    // Require the list to exist
                // Prepare a statement to get the properties of this list from 
                // the database
            try(PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                    "SELECT %s, %s, %s, %s, %s FROM %s WHERE %s = ?", 
                        LIST_NAME_COLUMN_NAME,
                        LIST_FLAGS_COLUMN_NAME,
                        LIST_LAST_MODIFIED_COLUMN_NAME, 
                        LIST_CREATED_COLUMN_NAME,
                        LIST_SIZE_LIMIT_COLUMN_NAME,
                        LIST_TABLE_NAME,
                        LIST_ID_COLUMN_NAME))){
                    // Provide this list's listID
                pstmt.setInt(1, listID);
                    // Get the results of the query
                ResultSet rs = pstmt.executeQuery();
                    // If the query had no results
                if (!rs.next()){
                    LinkManager.getLogger().exiting(this.getClass().getName(), 
                            "toModel");
                    return model;
                }   // Get the name of this list
                String name = rs.getString(LIST_NAME_COLUMN_NAME);
                    // If the given model is null (this is to create a new 
                if (model == null)  // model)
                    model = new LinksListModel(name,listID);
                else{
                    model.setListID(listID);
                    model.setListName(name);
                    model.clear();
                }   // Set the model's flags to be this list's flags
                model.setFlags(rs.getInt(LIST_FLAGS_COLUMN_NAME));
                    // Set the model's last modified time
                model.setLastModified(rs.getLong(LIST_LAST_MODIFIED_COLUMN_NAME));
                    // Set the model's creation time
                model.setCreationTime(rs.getLong(LIST_CREATED_COLUMN_NAME));
                    // Get the size limit for this list
                int sizeLimit = rs.getInt(LIST_SIZE_LIMIT_COLUMN_NAME);
                if (!rs.wasNull())  // If the size limit is not null
                    model.setSizeLimit(sizeLimit);
            }   // Get whether the model's modification limitations are enabled
            boolean modLimit = model.isModificationLimitEnabled();
                // Disable the model's modification limitations
            model.setModificationLimitEnabled(false);
                // The size for this list
            int size = size();
                // Prepare a statement to read the contents of this list from 
                // the database
            try(PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                    "SELECT %s%s FROM %s WHERE %s = ? ORDER BY %s", 
                                // If the model does not allow duplicates, 
                                // then only get the distict elements
                            (model.getAllowsDuplicates())?"":"DISTINCT ",
                            getDataElementColumn(),
                            getDataViewName(),
                            getTypeIDColumn(),
                            getIndexColumn()))){
                    // Provide this list's listID
                pstmt.setInt(1, listID);
                    // Get the results of the query
                ResultSet rs = pstmt.executeQuery();
                    // While there are still results
                while (rs.next()){
                        // Get the link from the current row
                    String temp = rs.getString(getDataElementColumn());
                        // If the link is not null and not blank
                    if (temp != null && !temp.isBlank()){
                        try{
                            model.add(temp);    // Add the link to the model
                                // If an observer was provided
                            if (observer != null)
                                observer.incrementValue();
                        } catch(IllegalArgumentException | IllegalStateException ex){ 
                            LinkManager.getLogger().log(Level.WARNING, 
                                    "Issue adding value to model", ex);
                        }
                    }
                }
            }   // If the model does not allow duplicates
            if (!model.getAllowsDuplicates())
                    // Remove any duplicates from the model
                model.removeDuplicates();
                // Restore the model's modification limitations back to what 
                // it was set to before
            model.setModificationLimitEnabled(modLimit);
                // Set whether the model is hidden
            model.setHidden(isHidden());
                // Set the model to be edited if its size does not match 
            model.setEdited(model.size() != size);  // this list's size
        } catch (SQLException ex){
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "toModel");
        return model;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public void updateContents(LinksListModel model, 
            ProgressObserver observer,Map<String,Long> linkIDMap){
        LinkManager.getLogger().entering(this.getClass().getName(), 
                "updateContents");
            // Check if the model is null
        Objects.requireNonNull(model);
        try{    // Get the current state of the auto-commit
            boolean autoCommit = getConnection().getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            getConnection().setAutoCommit(false);
                // Get the link map for this database
            LinkMap linkMap = getConnection().getLinkMap();
                // Add any links from the model that are not already in the link 
            linkMap.addAllIfAbsent(model); // map.
                // If no map of links to linkIDs was provided
            if (linkIDMap == null)
                    // Get the map from the linkMap
                linkIDMap = linkMap.inverse();
            clear();    // Clear this list
                // Go through the contents of the model
            for (int index = 0; index < model.size(); index++){
                    // Get the value at the current index in the model
                String value = model.get(index);
                    // Get the linkID for the value
                Long linkID = linkIDMap.get(value);
                    // If the map did not provide a linkID for the value
                if (linkID == null)
                        // Get the linkID from the link map's inverse map
                    linkID = linkMap.inverse().get(value);
                    // If the linkID for the value is still null
                if (linkID == null)
                        // Try adding the value to the link map and get its new 
                    linkID = linkMap.addIfAbsent(value);    // linkID
                    // Prepare a statement to insert the entry in the list into 
                    // the list data table
                try(PreparedStatement pstmt = getConnection().prepareStatement(String.format(
                        "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)", 
                                LIST_DATA_TABLE_NAME,
                                LIST_ID_COLUMN_NAME,
                                LINK_INDEX_COLUMN_NAME,
                                LINK_ID_COLUMN_NAME))){
                        // Set the listID of the list to insert into
                    pstmt.setInt(1, listID);
                        // Set the index at which to insert at
                    pstmt.setInt(2, index);
                        // Set the linkID of the link to insert
                    pstmt.setLong(3, linkID);
                        // Update the database
                    pstmt.executeUpdate();
                }   // If an observer was provided
                if (observer != null)
                    observer.incrementValue();
            }   // Commit the changes to the database
            getConnection().commit();       
                // Restore the auto-commit back to what it was set to before
            getConnection().setAutoCommit(autoCommit);
        } catch (SQLException ex){
            ConnectionBased.throwConstraintException(ex);
            appendWarning(ex);
            throw new UncheckedSQLException(ex);
        }   // Set both this and the model's last modified time to the 
        model.setLastModified(setLastModified());   // current time
            // Clear whether the model has been edited
        model.clearEdited();
        LinkManager.getLogger().exiting(this.getClass().getName(), 
                "updateContents");
    }
}
