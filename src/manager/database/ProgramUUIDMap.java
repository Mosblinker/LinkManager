/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.util.UUID;
import sql.util.SQLMap;

/**
 * This is a map view that maps the program UUIDs associated with a given user 
 * UUID to the row IDs the rows. This map does not permit null keys or values.
 * @author Mosblinker
 */
public interface ProgramUUIDMap extends SQLMap<UUID,Integer>{
    /**
     * 
     * @return 
     */
    public UUID getUserID();
    /**
     * 
     * @param key
     * @return 
     */
    public Integer add(UUID key);
    /**
     * 
     * @param key
     * @return 
     */
    public default Integer addIfAbsent(UUID key){
        Integer value = get(key);
        if (value == null)
            return add(key);
        return value;
    }
    /**
     * 
     * @return 
     */
    public boolean exists();
}
