/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package manager.sync;

/**
 *
 * @author Mosblinker
 */
public enum SyncMode {
    
    DROPBOX;
    
    
    private String name;
    
    private SyncMode(String name){
        this.name = name;
    }
    
    private SyncMode(){
        this(null);
    }
    
    @Override
    public String toString(){
        if (name != null)
            return name;
        name = name().replace('_', ' ').toLowerCase();
        int index = 0;
        do{
            name = name.substring(0, index)+name.substring(index, index+1).toUpperCase()+name.substring(index+1);
            index = name.indexOf(" ", index)+1;
        }
        while (index > 0);
        return name;
    }
}
