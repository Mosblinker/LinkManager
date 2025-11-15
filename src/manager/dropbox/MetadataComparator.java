/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.dropbox;

import com.dropbox.core.v2.files.*;
import java.util.Comparator;
import java.util.Objects;

/**
 *
 * @author Mosblinker
 */
public class MetadataComparator implements Comparator<Metadata>{

    @Override
    public int compare(Metadata o1, Metadata o2) {
        if (Objects.equals(o1, o2))
            return 0;
        else if (o1 == null)
            return -1;
        else if (o2 == null)
            return 1;
        int comparison = compareClass(o1,o2,FolderMetadata.class);
        if (comparison == 0)
            comparison = compareClass(o1,o2,FileMetadata.class);
        if (comparison == 0)
            comparison = compareClass(o2,o1,DeletedMetadata.class);
        if (comparison != 0)
            return comparison;
        String o1Path = o1.getPathLower();
        String o2Path = o2.getPathLower();
        if (Objects.equals(o1Path, o2Path)){
            o1Path = o1.getName();
            o2Path = o2.getName();
        }
        if (Objects.equals(o1Path, o2Path))
            return 0;
        else if (o1Path == null)
            return -1;
        else if (o2Path == null)
            return 1;
        else
            return o1Path.compareTo(o2Path);
    }
    /**
     * 
     * @param o1
     * @param o2
     * @param classType
     * @return 
     */
    private int compareClass(Metadata o1, Metadata o2, Class<? extends Metadata> classType){
        if (classType.isInstance(o1) && !classType.isInstance(o2))
            return -1;
        else if (classType.isInstance(o2) && !classType.isInstance(o1))
            return 1;
        return 0;
    }
}
