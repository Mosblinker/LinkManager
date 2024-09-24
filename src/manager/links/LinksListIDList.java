/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import java.util.*;

/**
 *
 * @author Milo Steier
 */
public class LinksListIDList extends AbstractList<Integer>{
    
    private final List<LinksListModel> modelList;
    
    public LinksListIDList(List<LinksListModel> modelList){
        this.modelList = Objects.requireNonNull(modelList);
    }
    
    public List<LinksListModel> getModelList(){
        return modelList;
    }
    @Override
    public Integer get(int index) {
        LinksListModel list = modelList.get(index);
        return (list != null) ? list.getListID() : null;
    }
    @Override
    public int size() {
        return modelList.size();
    }
}
