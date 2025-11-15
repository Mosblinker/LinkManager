/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.compress;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import manager.ProgressObserver;
import net.sf.sevenzipjbinding.*;
import net.sf.sevenzipjbinding.impl.*;

/**
 *
 * @author Mosblinker
 * @param <T>
 */
public abstract class AbstractFileCreateCallback<T extends IOutItemBase> 
        implements IOutCreateCallback<T> {
    /**
     * 
     */
    protected ProgressObserver observer;
    /**
     * 
     */
    protected List<File> files;
    /**
     * 
     */
    protected Map<File,String> nameMap;
    /**
     * 
     * @param observer
     * @param files 
     */
    public AbstractFileCreateCallback(ProgressObserver observer, Collection<File> files){
        if (files == null || files.size() < 1)
            throw new IllegalArgumentException();
        this.observer = observer;
        this.files = new ArrayList<>(files);
        nameMap = new HashMap<>();
    }
    /**
     * 
     * @param observer
     * @param files 
     */
    public AbstractFileCreateCallback(ProgressObserver observer, File... files){
        this(observer,Arrays.asList(files));
    }
    /**
     * 
     * @return 
     */
    public ProgressObserver getProgressObserver(){
        return observer;
    }
    /**
     * 
     * @return 
     */
    public List<File> getFiles(){
        return files;
    }
    /**
     * 
     * @return 
     */
    public Map<File,String> getFilePathMap(){
        return nameMap;
    }
    @Override
    public void setOperationResult(boolean operationResultOk) throws SevenZipException {
        // TODO: Deal with the operation being completed
    }
    /**
     * 
     * @param index
     * @param outItemFactory
     * @param file
     * @return
     * @throws SevenZipException 
     */
    protected abstract T getItemInformation(int index, 
            OutItemFactory<T> outItemFactory, File file) throws SevenZipException;
    @Override
    public T getItemInformation(int index, OutItemFactory<T> outItemFactory) throws SevenZipException {
        return getItemInformation(index,outItemFactory,files.get(index));
    }
    @Override
    public ISequentialInStream getStream(int index) throws SevenZipException {
        File file = files.get(index);
        if (file == null || file.isDirectory() || !file.exists())
            return null;
        InputStream input;
        try{
            FileInputStream fileIn = new FileInputStream(file);
            input = new BufferedInputStream(fileIn);
        } catch (FileNotFoundException ex){
            throw new SevenZipException("File not found",ex);
        }
        return new InputStreamSequentialInStream(input);
    }
    @Override
    public void setTotal(long total) throws SevenZipException {
        if (observer == null)
            return;
        observer.setMaximumLong(total);
    }
    @Override
    public void setCompleted(long complete) throws SevenZipException {
        if (observer == null)
            return;
            // Update the progress with the amount of bytes written
        observer.setValueLong(complete);
    }
}
