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
public class LinksListModelList extends AbstractList<LinksListModel>{
    
    private final List<LinksListPanel> panelList;
    
    public LinksListModelList(List<LinksListPanel> panelList){
        this.panelList = Objects.requireNonNull(panelList);
    }
    
    public List<LinksListPanel> getPanelList(){
        return panelList;
    }
    @Override
    public LinksListModel get(int index) {
        LinksListPanel list = panelList.get(index);
        return (list != null) ? list.getModel() : null;
    }
    @Override
    public LinksListModel set(int index, LinksListModel model){
        LinksListPanel list = panelList.get(index);
        if (list == null){
            panelList.set(index, new LinksListPanel(model));
            return null;
        }
        LinksListModel old = list.getModel();
        list.setModel(model);
        return old;
    }
    @Override
    public LinksListModel remove(int index){
        LinksListPanel list = panelList.remove(index);
        return (list != null) ? list.getModel() : null;
    }
    @Override
    public void add(int index, LinksListModel model){
        panelList.add(index, new LinksListPanel(model));
    }
    @Override
    public int size() {
        return panelList.size();
    }
}
