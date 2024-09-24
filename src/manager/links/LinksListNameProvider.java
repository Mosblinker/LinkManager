/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.links;

/**
 *
 * @author Milo Steier
 */
public interface LinksListNameProvider {
    
    public default String getListName(LinksListPanel panel){
        return (panel != null) ? panel.getListName() : "null";
    }
    
}
