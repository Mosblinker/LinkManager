/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import manager.LinkManager;
import sql.util.*;

/**
 *
 * @author Mosblinker
 * @param <K> The type of keys maintained by the map.
 * @param <V> The type of mapped values.
 */
abstract class AbstractQueryMap<K, V> extends AbstractSQLMap<K, V>{
    /**
     * The connection to the database.
     */
    private final LinkDatabaseConnection conn;
    /**
     * This constructs an AbstractQueryMap with the given connection to the 
     * database
     * @param conn The connection to the database (cannot be null).
     */
    protected AbstractQueryMap(LinkDatabaseConnection conn){
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
    protected abstract boolean containsKeySQL(Object key) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract V removeSQL(Object key) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract V getSQL(Object key) throws SQLException;
    /**
     * {@inheritDoc }
     */
    @Override
    protected abstract V putSQL(K key, V value) throws SQLException;
    /**
     * 
     * @return
     * @throws SQLException 
     */
    protected abstract Set<Entry<K,V>> entryCacheSet() throws SQLException;
    /**
     * 
     * @return
     * @throws SQLException 
     */
    protected Iterator<Entry<K,V>> entryIteratorSQL() throws SQLException{
        return new CacheSetIterator<>(entryCacheSet()){
            @Override
            protected void remove(Entry<K,V> value) {
                AbstractQueryMap.this.remove(value.getKey(),value.getValue());
            }
        };
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected Iterator<Entry<K,V>> entryIterator(){
        try{
            return entryIteratorSQL();
        } catch (SQLException ex) {
            LinkManager.getLogger().log(Level.WARNING, 
                    "Failed to get iterator for entries in query map", ex);
            appendWarning(ex);
            return Collections.emptyIterator();
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void putAllSQL(Map<? extends K, ? extends V> m) 
            throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = conn.getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        conn.setAutoCommit(false);
            // Put all the entries in the given map into this map
        super.putAllSQL(m);
        conn.commit();       // Commit the changes to the database
            // Restore the auto-commit back to what it was set to before
        conn.setAutoCommit(autoCommit);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object obj){
            // If the given object is this map
        if (obj == this)
            return true;
            // If the given object is a map
        else if (obj instanceof Map){
                // Create a copy of this map (to reduce the number of 
            Map<K, V> temp = new HashMap<>(this);   // queries)
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
            // Create a copy of this map (to reduce the number of queries)
        Map<K, V> temp = new HashMap<>(this);
        return temp.hashCode();
    }
}
