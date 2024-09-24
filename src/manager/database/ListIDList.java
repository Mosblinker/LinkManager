/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import sql.UncheckedSQLException;
import sql.util.*;

/**
 * This is a list view of the 
 * @author Milo Steier
 */
public interface ListIDList extends SQLList<Integer>{
    /**
     * This returns the 
     * @return 
     */
    public int getListType();
    /**
     * This returns the total size of all the lists whose listIDs are present in 
     * this list.
     * @return The sum of the sizes of the lists whose listIDs are in this list.
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     * @throws UnsupportedOperationException If the lists in the database 
     * cannot be accessed by this list.
     */
    public int totalSize();
}
