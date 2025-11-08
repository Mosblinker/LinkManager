/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import manager.LinkManager;
import sql.util.AbstractSQLSet;

/**
 *
 * @author Mosblinker 
 * @param <E> The type of elements stored in this set.
 */
abstract class AbstractQuerySet<E> extends AbstractSQLSet<E>{
    /**
     * The connection to the database.
     */
    private final LinkDatabaseConnection conn;
    /**
     * This constructs an AbstractQuerySet with the given connection to the 
     * database
     * @param conn The connection to the database (cannot be null).
     */
    protected AbstractQuerySet(LinkDatabaseConnection conn){
        this.conn = Objects.requireNonNull(conn);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public LinkDatabaseConnection getConnection() throws SQLException{
        return conn;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean addAllSQL(Collection<? extends E> c)throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = conn.getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        conn.setAutoCommit(false);
            // Add all the elements in the given collection to this set and 
            // get if this set was modified as a result
        boolean modified = super.addAllSQL(c);
        conn.commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        conn.setAutoCommit(autoCommit);
        return modified;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean removeAllSQL(Collection<?> c)throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = conn.getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        conn.setAutoCommit(false);
            // Remove any elements in this set that are also in the given 
            // collection and get if this set was modified as a result
        boolean modified = super.removeAllSQL(c);
        conn.commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        conn.setAutoCommit(autoCommit);
        return modified;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean retainAllSQL(Collection<?> c)throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = conn.getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        conn.setAutoCommit(false);
            // Retain only the elements in this set that are also in the 
            // given collection and get if this set was modified as a result
        boolean modified = super.retainAllSQL(c);
        conn.commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        conn.setAutoCommit(autoCommit);
        return modified;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract boolean containsSQL(Object o) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract boolean removeSQL(Object o) throws SQLException;
    /**
     * 
     * @return
     * @throws SQLException 
     */
    protected abstract Set<E> valueCacheSet() throws SQLException;
    /**
     * 
     * @return
     * @throws SQLException 
     */
    protected Iterator<E> iteratorSQL() throws SQLException{
        return new CacheSetIterator<>(valueCacheSet()){
            @Override
            protected void remove(E value) {
                AbstractQuerySet.this.remove(value);
            }
        };
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public Iterator<E> iterator(){
        try{
            return iteratorSQL();
        } catch (SQLException ex) {
            LinkManager.getLogger().log(Level.WARNING, 
                    "Failed to get iterator for query set", ex);
            appendWarning(ex);
            return Collections.emptyIterator();
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object obj){
            // If the given object is this set
        if (obj == this)
            return true;
            // If the given object is a set
        else if (obj instanceof Set){
                // Create a copy of this set (to reduce the number of 
            Set<E> temp = new HashSet<>(this);  // queries)
                // Return whether the object matches the copy
            return temp.equals(obj);
        }
        return false;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
            // Create a copy of this set (to reduce the number of queries)
        Set<E> temp = new HashSet<>(this);
        return temp.hashCode();
    }
}
