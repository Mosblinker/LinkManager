/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.renderer;

import java.awt.Component;
import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

/**
 *
 * @author Mosblinker
 */
public class CompressionLevelListCellRenderer extends DefaultListCellRenderer{
    /**
     * 
     */
    private static final String[] COMPRESSION_LEVEL_NAMES = {
        "Store",
        "Fastest",
        "Fast",
        "Normal",
        "Maximum",
        "Ultra"
    };
    /**
     * 
     */
    private static final int MAXIMUM_COMPRESSION_LEVEL = 9;
    /**
     * 
     */
    public static final Map<Integer,String> COMPRESSION_LEVEL_NAME_MAP = 
            new CompressionLevelNameMap();
    @Override
    public Component getListCellRendererComponent(JList list,
            Object value,int index,boolean isSelected,boolean cellHasFocus){
        if (value instanceof Integer){
            String name = COMPRESSION_LEVEL_NAME_MAP.get((Integer)value);
            if (name != null)
                value = value+" - " +name;
        }
        return super.getListCellRendererComponent(list, value, index, 
                isSelected, cellHasFocus);
    }
    /**
     * 
     */
    private static class CompressionLevelNameMap extends AbstractMap<Integer, String> {
        /**
         * 
         */
        private Set<Entry<Integer, String>> entries = null;
        @Override
        public boolean containsKey(Object key){
            if (key instanceof Integer){
                Integer index = (Integer) key;
                if (index < 0 || index > MAXIMUM_COMPRESSION_LEVEL)
                    return false;
                return index == 0 || index % 2 != 0;
            }
            return false;
        }
        @Override
        public String get(Object key){
            if (key instanceof Integer){
                Integer index = (Integer) key;
                if (index < 0 || index > MAXIMUM_COMPRESSION_LEVEL)
                    return null;
                if (index < 2)
                    return COMPRESSION_LEVEL_NAMES[index];
                return COMPRESSION_LEVEL_NAMES[Math.floorDiv(index-1, 2)+1];
            }
            return null;
        }
        @Override
        public Set<Entry<Integer, String>> entrySet() {
            if (entries == null){
                entries = new AbstractSet<>(){
                    @Override
                    public Iterator<Entry<Integer, String>> iterator() {
                        return new Iterator<>(){
                            int index = 0;
                            @Override
                            public boolean hasNext() {
                                return index < COMPRESSION_LEVEL_NAMES.length;
                            }
                            @Override
                            public Entry<Integer, String> next() {
                                if (!hasNext())
                                    throw new NoSuchElementException();
                                Integer key = index;
                                if (index >= 2){
                                    key = (index-1)*2 + 1;
                                }
                                String value = COMPRESSION_LEVEL_NAMES[index];
                                index++;
                                return new AbstractMap.SimpleImmutableEntry<>(key,value);
                            }
                        };
                    }
                    @Override
                    public int size() {
                        return COMPRESSION_LEVEL_NAMES.length;
                    }
                };
            }
            return entries;
        }
    }
}
