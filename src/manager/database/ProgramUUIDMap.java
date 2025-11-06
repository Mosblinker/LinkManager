/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.database;

import java.util.UUID;
import sql.util.SQLMap;

/**
 * This is a map view that maps the program UUIDs associated with a given user 
 * UUID to the row IDs the rows. This map is unmodifiable.
 * @author Mosblinker
 */
public interface ProgramUUIDMap extends SQLMap<UUID,Integer>{
    /**
     * 
     * @return 
     */
    public UUID getUserID();
}
