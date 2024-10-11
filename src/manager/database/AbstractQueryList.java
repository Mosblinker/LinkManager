/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.*;
import java.util.*;
import static manager.database.LinkDatabaseConnection.*;
import sql.util.*;

/**
 *
 * @author Milo Steier
 * @param <E> The type of elements stored in this list.
 */
abstract class AbstractQueryList <E> extends AbstractSQLList<E>{
    /**
     * The connection to the database.
     */
    private final LinkDatabaseConnection conn;
    /**
     * This constructs an AbstractQueryList with the given connection to the 
     * database
     * @param conn The connection to the database (cannot be null).
     */
    protected AbstractQueryList(LinkDatabaseConnection conn){
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
    protected boolean addAllSQL(int index, Collection<? extends E> c)
            throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        getConnection().setAutoCommit(false);
            // Add all the elements in the given collection to this list at 
            // the given index and get if this list was modified as a result
        boolean modified = super.addAllSQL(index, c);
            // Commit the changes to the database
        getConnection().commit();
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
        return modified;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean addAllSQL(Collection<? extends E> c)throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        getConnection().setAutoCommit(false);
            // Add all the elements in the given collection to this list and 
            // get if this list was modified as a result
        boolean modified = super.addAllSQL(c);
            // Commit the changes to the database
        getConnection().commit();
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
        return modified;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean removeAllSQL(Collection<?> c)throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        getConnection().setAutoCommit(false);
            // Remove any elements in this list that are also in the given 
            // collection and get if this list was modified as a result
        boolean modified = super.removeAllSQL(c);
            // Commit the changes to the database
        getConnection().commit();
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
        return modified;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean retainAllSQL(Collection<?> c)throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        getConnection().setAutoCommit(false);
            // Retain only the elements in this list that are also in the 
            // given collection and get if this list was modified as a 
        boolean modified = super.retainAllSQL(c);   // result
            // Commit the changes to the database
        getConnection().commit();       
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
        return modified;
    }
    /**
     * This returns the name of the table in the database that this list is a 
     * view of.
     * @return The name of the table in the database.
     * @see #getDataViewName() 
     * @see #getTypeIDColumn() 
     * @see #getIndexColumn() 
     * @see #getElementColumn() 
     * @see #getDataElementColumn() 
     */
    protected abstract String getTableName();
    /**
     * This returns the name of the view in the database used to get the 
     * elements for this list. 
     * 
     * @implSpec The default implementation forwards the call to {@link 
     * #getTableName getTableName}.
     * 
     * @return The name of the view in the database used to get the elements.
     * @see #getTableName() 
     * @see #getTypeIDColumn() 
     * @see #getIndexColumn() 
     * @see #getElementColumn() 
     * @see #getDataElementColumn() 
     */
    protected String getDataViewName(){
        return getTableName();
    }
    /**
     * 
     * @return 
     */
    protected abstract String getTypeIDColumn();
    /**
     * This returns the name of the column in the table in the database that 
     * stores the indexes for the elements in this list.
     * @return The name of the index column in the table.
     * @see #getTableName() 
     * @see #getDataViewName() 
     * @see #getTypeIDColumn() 
     * @see #getElementColumn() 
     * @see #getDataElementColumn() 
     */
    protected abstract String getIndexColumn();
    /**
     * This returns the name of the column in the table in the database that 
     * stores the elements for this list.
     * @return The name of the element column in the table.
     * @see #getTableName() 
     * @see #getDataViewName() 
     * @see #getTypeIDColumn() 
     * @see #getIndexColumn() 
     * @see #getDataElementColumn() 
     * @see #setPreparedElement(PreparedStatement, int, Object) 
     * @see #getElementFromResults(ResultSet) 
     */
    protected abstract String getElementColumn();
    /**
     * 
     * @return 
     */
    protected String getDataElementColumn(){
        return getElementColumn();
    }
    /**
     * 
     * @param pstmt
     * @param parameterIndex
     * @throws SQLException 
     */
    protected abstract void setPreparedTypeID(PreparedStatement pstmt, 
            int parameterIndex)throws SQLException;
    /**
     * 
     * @param pstmt
     * @param parameterIndex
     * @param element
     * @throws SQLException 
     */
    protected abstract void setPreparedElement(PreparedStatement pstmt, 
            int parameterIndex, E element)throws SQLException;
    /**
     * 
     * @param pstmt
     * @param parameterIndex
     * @param element
     * @throws SQLException 
     */
    protected void setReplaceIndexElement(PreparedStatement pstmt, 
            int parameterIndex, E element)throws SQLException{
        setPreparedElement(pstmt,parameterIndex,element);
    }
    /**
     * 
     * @param rs
     * @return
     * @throws SQLException 
     */
    protected abstract E getElementFromResults(ResultSet rs)throws SQLException;
    /**
     * 
     * @param index
     * @return
     * @throws SQLException 
     */
    protected boolean containsIndex(int index) throws SQLException{
            // Prepare a statement to check if the table contains the given 
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                String.format(TABLE_CONTAINS_QUERY_TEMPLATE+" AND %s = ?", 
                        getElementColumn(),
                        getTableName(),
                        getTypeIDColumn(),
                        getIndexColumn()))){
            setPreparedTypeID(pstmt,1);
            pstmt.setInt(2, index);
            return containsCountResult(pstmt.executeQuery());
        }
    }
    /**
     * 
     * @param index
     * @param element
     * @throws SQLException 
     */
    protected void replaceIndex(int index, E element) throws SQLException{
        String query;
        if (containsIndex(index))
            query = "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?";
        else
            query = "INSERT INTO %s(%s, %s, %s) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                String.format(query, 
                        getTableName(),
                        getElementColumn(),
                        getTypeIDColumn(),
                        getIndexColumn()))){
            setReplaceIndexElement(pstmt,1,element);
            setPreparedTypeID(pstmt,2);
            pstmt.setInt(3, index);
            pstmt.executeUpdate();
        }
    }
    /**
     * 
     * @param index
     * @throws SQLException 
     */
    protected void deleteIndex(int index) throws SQLException{
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                String.format("DELETE FROM %s WHERE %s = ? AND %s = ?", 
                        getTableName(),
                        getTypeIDColumn(),
                        getIndexColumn()))){
            setPreparedTypeID(pstmt,1);
            pstmt.setInt(2, index);
            pstmt.executeUpdate();
        }
    }
    /**
     * 
     * @param element
     * @param descending
     * @return
     * @throws SQLException 
     */
    protected int indexOfElement(E element, boolean descending) throws SQLException{
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                getSortedQuery(
                        getIndexColumn(),
                        getDataViewName(),
                        getTypeIDColumn()+" = ? AND "+getDataElementColumn()+
                                ((element==null)?" IS NULL":" = ?"),
                        getIndexColumn(),
                        descending,1))){
            setPreparedTypeID(pstmt,1);
            if (element != null)
                setPreparedElement(pstmt,2,element);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return rs.getInt(getIndexColumn());
        }
        return -1;
    }
    /**
     * 
     * @param o
     * @param descending
     * @return
     * @throws SQLException 
     */
    protected abstract int indexOfSQL(Object o, boolean descending) 
            throws SQLException;
    /**
     * This shifts all the indexes over by {@code distance}, starting at 
     * {@code startIndex}, inclusive. 
     * @param startIndex
     * @param distance
     * @throws SQLException 
     */
    protected void rotateIndexes(int startIndex, int distance) throws SQLException{
        if (distance > 0){
            if (startIndex >= size())
                return;
                // Get the current state of the auto-commit
            boolean autoCommit = getConnection().getAutoCommit();
                // Turn off the auto-commit in order to group the following 
                // database transactions to improve performance
            getConnection().setAutoCommit(false);
            // "UPDATE table SET index = index + distance WHERE index >= startIndex"
            ArrayList<Integer> indexes = new ArrayList<>();
            try(PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(
                            "SELECT %s FROM %s WHERE %s = ? AND %s >= ? ORDER BY %s DESC", 
                                getIndexColumn(),
                                getTableName(),
                                getTypeIDColumn(),
                                getIndexColumn(),
                                getIndexColumn()))){
                setPreparedTypeID(pstmt,1);
                pstmt.setInt(2, startIndex);
                ResultSet rs = pstmt.executeQuery();
                while (rs.next())
                    indexes.add(rs.getInt(getIndexColumn()));
            }
            for (int temp : indexes){
                try(PreparedStatement pstmt = getConnection().prepareStatement(
                        String.format(
                                "UPDATE %s SET %s = ? WHERE %s = ? AND %s = ?",
                                    getTableName(),
                                    getIndexColumn(),
                                    getTypeIDColumn(),
                                    getIndexColumn()))){
                    pstmt.setInt(1, temp+1);
                    setPreparedTypeID(pstmt,2);
                    pstmt.setInt(3, temp);
                    pstmt.executeUpdate();
                }
            }
//            while (index < size()){
//                element = setSQL(index,element);
//                index++;
//            }
                // Commit the changes to the database
            getConnection().commit();       
                // Restore the auto-commit back to what it was set to before
            getConnection().setAutoCommit(autoCommit);
        } else if (distance < 0){
            if (startIndex < 0)
                return;
            try(PreparedStatement pstmt = getConnection().prepareStatement(
                    String.format(
                            "UPDATE %s SET %s = %s - ? WHERE %s = ? AND %s >= ?", 
                                getTableName(),
                                getIndexColumn(),
                                getIndexColumn(),
                                getTypeIDColumn(),
                                getIndexColumn()))){
                pstmt.setInt(1, Math.abs(distance));
                setPreparedTypeID(pstmt,2);
                pstmt.setInt(3, startIndex);
                pstmt.executeUpdate();
            }
        }
    }
    /**
     * 
     * @param element
     * @throws SQLException 
     */
    protected void checkElement(E element) throws SQLException{ }
    /**
     * {@inheritDoc }
     */
    @Override
    protected E getSQL(int index) throws SQLException{
        Objects.checkIndex(index, size());
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                String.format("SELECT %s FROM %s WHERE %s = ? AND %s = ?", 
                        getDataElementColumn(),
                        getDataViewName(),
                        getTypeIDColumn(),
                        getIndexColumn()))){
            setPreparedTypeID(pstmt,1);
            pstmt.setInt(2, index);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next())
                return getElementFromResults(rs);
        }
        return null;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected int sizeSQL() throws SQLException{
        try(PreparedStatement pstmt = getConnection().prepareStatement(
                String.format("SELECT MAX(%s) AS %s FROM %s WHERE %s = ?", 
                        getIndexColumn(),
                        LAST_INDEX_COLUMN_NAME,
                        getTableName(),
                        getTypeIDColumn()))){
            setPreparedTypeID(pstmt,1);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()){
                int index = rs.getInt(LAST_INDEX_COLUMN_NAME);
                if (!rs.wasNull())
                    return index+1;
            }
        }
        return 0;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void clearSQL()throws SQLException{
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                String.format("DELETE FROM %s WHERE %s = ?", 
                        getTableName(),
                        getTypeIDColumn()))) {
            setPreparedTypeID(pstmt,1);
            pstmt.executeUpdate();
        }
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void addSQL(int index, E element) throws SQLException {
        Objects.checkIndex(index, size()+1);
        checkElement(element);
        rotateIndexes(index,1);
        replaceIndex(index,element);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected E setSQL(int index, E element) throws SQLException{
        E old = getSQL(index);
        if (Objects.equals(element, old))
            return old;
        checkElement(element);
        replaceIndex(index,element);
        return old;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected E removeSQL(int index) throws SQLException{
        E old = getSQL(index);
        deleteIndex(index);
        rotateIndexes(index,-1);
        return old;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean removeSQL(Object o) throws SQLException{
        int index = indexOf(o);
        if (index < 0)
            return false;
        remove(index);
        return true;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected void removeRangeSQL(int fromIndex, int toIndex)throws SQLException{
        if (fromIndex == toIndex)
            return;
        try (PreparedStatement pstmt = getConnection().prepareStatement(
                String.format(
                        "DELETE FROM %s WHERE %s = ? AND %s >= ? AND %s < ?",
                            getTableName(),
                            getTypeIDColumn(),
                            getIndexColumn(),
                            getIndexColumn()))){
            setPreparedTypeID(pstmt,1);
            pstmt.setInt(2, Math.min(fromIndex, toIndex));
            pstmt.setInt(3, Math.max(fromIndex, toIndex));
            pstmt.executeUpdate();
        }
        rotateIndexes(Math.min(fromIndex, toIndex),fromIndex-toIndex);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected int indexOfSQL(Object o) throws SQLException{
        return indexOfSQL(o,false);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected int lastIndexOfSQL(Object o) throws SQLException{
        return indexOfSQL(o,true);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean containsSQL(Object o) throws SQLException{
        return indexOf(o) >= 0;
    }
    /**
     * {@inheritDoc }
     */
    @Override
    protected boolean containsAllSQL(Collection<?> c) throws SQLException{
            // Create a temporary copy of this list
        List<E> temp = new ArrayList<>(this);
            // Return whether the copy contains the contents of the given 
        return temp.containsAll(c); // collection
    }
    /**
     * {@inheritDoc }
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void sortSQL(Comparator<? super E> c) throws SQLException{
            // Get the current state of the auto-commit
        boolean autoCommit = getConnection().getAutoCommit();
            // Turn off the auto-commit in order to group the following 
            // database transactions to improve performance
        getConnection().setAutoCommit(false);
            // Sort this list using the given comparator
        super.sortSQL(c);
            // Commit the changes to the database
        getConnection().commit();       
            // Restore the auto-commit back to what it was set to before
        getConnection().setAutoCommit(autoCommit);
    }
    /**
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object obj){
            // If the given object is this list
        if (obj == this)
            return true;
            // If the given object is a list
        else if (obj instanceof List){
                // Create a copy of this list (to reduce the number of 
            List<E> temp = new ArrayList<>(this);   // queries)
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
            // Create a copy of this list (to reduce the number of queries)
        List<E> temp = new ArrayList<>(this);
        return temp.hashCode();
    }
}
