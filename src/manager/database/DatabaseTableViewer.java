/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.awt.BorderLayout;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.table.TableModel;

/**
 *
 * @author Milo Steier
 */
public class DatabaseTableViewer extends JPanel{
    /**
     * This is the suffix appended to the names of tables that are actually 
     * views that are displayed by the database viewer.
     */
    private static final String TABLE_VIEW_SUFFIX = " (View)";
    /**
     * A list to store the TableModels generated while reading the 
     * tables in the database.
     */
    private final List<TableModel> tableModels = new ArrayList<>();
    /**
     * A list containing the names of the tables in the database.
     */
    private final List<String> tableNames = new ArrayList<>();
    /**
     * 
     */
    private final Map<String,Boolean> tableIsView = new HashMap<>();
    /**
     * 
     */
    private final List<JTable> tables = new ArrayList<>();
    /**
     * 
     */
    private final List<JScrollPane> scrollPanes = new ArrayList<>();
    /**
     * 
     */
    private final JTabbedPane tabbedPane;
    /**
     * 
     */
    public DatabaseTableViewer(){
        super(new BorderLayout(),true);
        tabbedPane = new JTabbedPane();
        this.add(tabbedPane, BorderLayout.CENTER);
    }
    
    public JTabbedPane getTabbedPane(){
        return tabbedPane;
    }
    
    public List<TableModel> getTableModels(){
        return tableModels;
    }
    
    public List<String> getTableNames(){
        return tableNames;
    }
    
    public Map<String,Boolean> getTableViewMap(){
        return tableIsView;
    }
    
    public List<JTable> getTables(){
        return tables;
    }
    
    public List<JScrollPane> getTableScrollPanes(){
        return scrollPanes;
    }
    
    public void loadTables(boolean showSchema, LinkDatabaseConnection conn, 
            Statement stmt, JProgressBar progressBar) throws SQLException{
        tableModels.clear();
        tableNames.clear();
        tableIsView.clear();
        tables.clear();
        scrollPanes.clear();
        tableNames.addAll(conn.showTables());
        tableNames.addAll(conn.showViews());
        for (String name : conn.showTables())
            tableIsView.put(name, false);
        for (String name : conn.showViews())
            tableIsView.put(name, true);
        if (showSchema){
            tableNames.add(LinkDatabaseConnection.SCHEMA_TABLE_NAME);
            tableIsView.put(LinkDatabaseConnection.SCHEMA_TABLE_NAME, false);
            tableNames.add(LinkDatabaseConnection.TABLES_VIEW_NAME);
            tableIsView.put(LinkDatabaseConnection.TABLES_VIEW_NAME, true);
            tableNames.add(LinkDatabaseConnection.TABLE_INDEX_VIEW_NAME);
            tableIsView.put(LinkDatabaseConnection.TABLE_INDEX_VIEW_NAME, true);
        }
        if (progressBar != null)
            progressBar.setMaximum(tableNames.size());
        for (String name : tableNames){
            TableModel model = conn.getTableModelForTable(name, stmt);
            JTable table = new JTable(model);
            table.setAutoCreateRowSorter(true);
            if (tableIsView.get(name))
                table.setName(name+TABLE_VIEW_SUFFIX);
            else
                table.setName(name);
            JScrollPane scrollPane = new JScrollPane(table);
            scrollPane.setName(table.getName());
            tableModels.add(model);
            tables.add(table);
            scrollPanes.add(scrollPane);
            if (progressBar != null)
                progressBar.setValue(progressBar.getValue()+1);
        }
        if (progressBar != null)
            progressBar.setIndeterminate(true);
    }
    
    public void loadTables(boolean showSchema, LinkDatabaseConnection conn, 
            Statement stmt)throws SQLException{
        loadTables(showSchema,conn,stmt,null);
    }
    
    public void loadTables(boolean showSchema, LinkDatabaseConnection conn, 
            JProgressBar progressBar)throws SQLException{
        try(Statement stmt = conn.createStatement()){
            loadTables(showSchema,conn,stmt,progressBar);
        }
    }
    
    public void loadTables(boolean showSchema, LinkDatabaseConnection conn)
            throws SQLException{
        loadTables(showSchema,conn,(JProgressBar)null);
    }
    
    public void populateTables(){
        tabbedPane.removeAll();
        for (JScrollPane pane : scrollPanes){
            tabbedPane.add(pane.getName(),pane);
        }
    }
}
