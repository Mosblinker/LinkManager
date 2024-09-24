/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.util;

import java.awt.Component;
import java.util.AbstractList;
import java.util.Objects;
import javax.swing.*;

/**
 *
 * @author Milo Steier
 */
public class MenuComponentList extends AbstractList<Component>{
    
    private final JMenu menu;
    
    public MenuComponentList(JMenu menu){
        this.menu = Objects.requireNonNull(menu);
    }
    
    public JMenu getMenu(){
        return menu;
    }
    
    @Override
    public void add(int index, Component element){
        Objects.checkIndex(index, size()+1);
        Objects.requireNonNull(element);
        if (index == size())
            menu.add(element);
        else
            menu.add(element, index);
    }
    
    public JMenuItem add(int index, JMenuItem menuItem){
        Objects.checkIndex(index, size()+1);
        Objects.requireNonNull(menuItem);
        if (index == size())
            return menu.add(menuItem);
        else
            return menu.insert(menuItem, index);
    }
    
    public JMenuItem add(JMenuItem menuItem){
        return add(size(),menuItem);
    }
    
    public JMenuItem add(int index, Action action){
        Objects.checkIndex(index, size()+1);
        Objects.requireNonNull(action);
        if (index == size())
            return menu.add(action);
        else
            return menu.insert(action, index);
    }
    
    public JMenuItem add(Action action){
         return add(size(),action);
    }
    
    public JMenuItem add(int index, String text){
        Objects.checkIndex(index, size()+1);
        Objects.requireNonNull(text);
        if (index == size())
            menu.add(text);
        else
            menu.insert(text, index);
        return getItem(index);
    }
    
    public JMenuItem add(String text){
        return add(size(),text);
    }
    
    public JPopupMenu.Separator addSeparator(int index){
        Objects.checkIndex(index, size()+1);
        if (index == size())
            menu.addSeparator();
        else
            menu.insertSeparator(index);
        return (JPopupMenu.Separator)get(index);
    }
    
    public JPopupMenu.Separator addSeparator(){
        return addSeparator(size());
    }
    @Override
    public Component get(int index) {
        Objects.checkIndex(index, size());
        return menu.getMenuComponent(index);
    }
    
    public JMenuItem getItem(int index){
        Objects.checkIndex(index, size());
        return menu.getItem(index);
    }
    @Override
    public int size() {
        return menu.getMenuComponentCount();
    }
    @Override
    public Component set(int index, Component element){
        Objects.checkIndex(index, size());
        Objects.requireNonNull(element);
        Component old = remove(index);
        add(index,element);
        return old;
    }
    @Override
    public Component remove(int index){
        Objects.checkIndex(index, size());
        Component old = get(index);
        menu.remove(index);
        return old;
    }
    @Override
    public boolean contains(Object o){
        if (!(o instanceof Component))
            return false;
        return menu.isMenuComponent((Component)o);
    }
}
