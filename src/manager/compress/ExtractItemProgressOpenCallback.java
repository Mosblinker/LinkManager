/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.compress;

import java.util.Objects;
import manager.ProgressObserver;
import net.sf.sevenzipjbinding.*;

/**
 *
 * @author Mosblinker
 */
public class ExtractItemProgressOpenCallback implements IArchiveOpenCallback{
    /**
     * 
     */
    private ProgressObserver l;
    /**
     * 
     * @param l 
     */
    public ExtractItemProgressOpenCallback(ProgressObserver l){
        this.l = Objects.requireNonNull(l);
    }
    /**
     * 
     * @return 
     */
    public ProgressObserver getProgressObserver(){
        return l;
    }
    @Override
    public void setTotal(Long files, Long bytes) throws SevenZipException {
        if (bytes != null)
            l.setTotal(bytes);
    }
    @Override
    public void setCompleted(Long files, Long bytes) throws SevenZipException {
        if (bytes != null)
            l.setCompleted(bytes);
    }
}
