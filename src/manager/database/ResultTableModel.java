/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.sql.*;
import java.util.Objects;
import java.util.logging.Level;
import manager.CustomTableModel;
import manager.LinkManager;

/**
 *
 * @author Milo Steier
 */
public class ResultTableModel extends CustomTableModel{
    
    private void initialize(ResultSet results) throws SQLException{
            // Get the metadata for the current table
        ResultSetMetaData rData = results.getMetaData();
            // A for loop to get the columns
        for (int c = 0; c < rData.getColumnCount(); c++){
                // Add the column and its name to the model
            addColumn(rData.getColumnLabel(c+1));
                // Add the column's class to the list of classes
            String className = rData.getColumnClassName(c+1);
            try{    // Try to add the class for the column
                setColumnClass(c, Class.forName(className));
            }
            catch(Exception ex){ 
                LinkManager.getLogger().log(Level.WARNING, 
                        "Failed to get column class", ex);
            }
        }   // A while loop to read all the rows in the current table
        while (results.next()){ 
                // This will get the current rows data
            Object[] data = new Object[getColumnCount()];
                // A for loop to read the data from the row
            for (int i = 0; i < getColumnCount(); i++){
                data[i] = results.getObject(i+1);
            }
            addRow(data);
        }
    }
    /**
     * This constructs a ResultTableModel based off the given ResultSet.
     * @param results The ResultSet to turn into a TableModel.
     * @throws SQLException If a database error occurs.
     * @throws NullPointerException If the ResultSet is null.
     */
    public ResultTableModel(ResultSet results) throws SQLException{
        super();
        Objects.requireNonNull(results);
        initialize(results);
    }
    /**
     * This constructs a ResultTableModel based off the results of the given 
     * query using the given statement.
     * @param sql The query to execute.
     * @param stmt The statement to use to execute the query.
     * @throws SQLException If a database error occurs.
     * @throws NullPointerException If either the query or statement are null.
     */
    public ResultTableModel(String sql, Statement stmt) throws SQLException{
        this(Objects.requireNonNull(stmt).executeQuery(Objects.requireNonNull(sql)));
    }
    /**
     * This constructs a ResultTableModel based off the results of the given 
     * prepared statement.
     * @param pstmt The prepared statement to use to execute the query.
     * @throws SQLException If a database error occurs.
     * @throws NullPointerException If the prepared statement is null.
     */
    public ResultTableModel(PreparedStatement pstmt) throws SQLException{
        this(Objects.requireNonNull(pstmt).executeQuery());
    }
}
