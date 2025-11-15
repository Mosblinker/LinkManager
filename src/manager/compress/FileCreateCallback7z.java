/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.compress;

import java.io.File;
import java.util.Collection;
import java.util.Date;
import manager.ProgressObserver;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.OutItemFactory;

/**
 *
 * @author Mosblinker
 */
public class FileCreateCallback7z extends AbstractFileCreateCallback<IOutItem7z>{

    public FileCreateCallback7z(ProgressObserver observer, Collection<File> files) {
        super(observer, files);
    }
    
    public FileCreateCallback7z(ProgressObserver observer, File... files){
        super(observer,files);
    }

    @Override
    protected IOutItem7z getItemInformation(int index, 
            OutItemFactory<IOutItem7z> outItemFactory, File file) throws SevenZipException {
        IOutItem7z item = outItemFactory.createOutItem();
        if (file.isDirectory())
            item.setPropertyIsDir(true);
        else
            item.setDataSize(file.length());
        item.setPropertyLastModificationTime(new Date(file.lastModified()));
        item.setPropertyPath(file.getName());
        return item;
    }
    
}
