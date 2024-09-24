/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import java.sql.SQLException;
import sql.UncheckedSQLException;
import sql.util.*;

/**
 *
 * @author Milo Steier
 */
public abstract class ListDataList extends AbstractSQLList<ListContents>{
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public abstract ListIDList getListIDs();
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public abstract ListDataMap getListDataMap();
    
    @Override
    protected ListContents getSQL(int index) throws SQLException {
        return getListDataMap().get(getListIDs().get(index));
    }
    @Override
    protected int sizeSQL() throws SQLException {
        return getListIDs().size();
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public int totalSize(){
        return getListIDs().totalSize();
    }
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public int getListType(){
        return getListIDs().getListType();
    }
}
