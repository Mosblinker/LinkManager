/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.database;

import sql.UncheckedSQLException;
import sql.util.*;

/**
 *
 * @author Milo Steier
 */
public interface ListDataMap extends NavigableSQLMap<Integer,ListContents>{
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public ListNameMap getListNameMap();
    /**
     * 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public void clearAll();
    /**
     * 
     * @return 
     * @throws UncheckedSQLException Implementations may, but are not required 
     * to, throw this if a database error occurs.
     */
    public default int totalSize(){
            // This will get the total size
        int size = 0;
            // Go through the lists in this map
        for (ListContents list : values())
                // If the list is not null
            if (list != null)
                size += list.size();
        return size;
    }
    
}
