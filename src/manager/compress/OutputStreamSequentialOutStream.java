/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.compress;

import java.io.*;
import manager.ProgressObserver;
import net.sf.sevenzipjbinding.*;

/**
 *
 * @author Mosblinker
 */
public class OutputStreamSequentialOutStream implements ISequentialOutStream, 
        Closeable, Flushable{
    /**
     * 
     */
    private final OutputStream out;
    /**
     * 
     */
    private ProgressObserver observer;
    /**
     * 
     */
    private long size = 0L;
    /**
     * 
     * @param out 
     * @param l 
     */
    public OutputStreamSequentialOutStream(OutputStream out, ProgressObserver l){
        if (out == null)
            throw new NullPointerException();
        this.out = out;
        observer = l;
    }
    /**
     * 
     * @param out 
     */
    public OutputStreamSequentialOutStream(OutputStream out){
        this(out,null);
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
     * @param l 
     */
    public void setProgressObserver(ProgressObserver l){
        observer = l;
    }
    @Override
    public int write(byte[] data) throws SevenZipException {
        try{
            out.write(data);
        } catch (IOException ex){
            throw new SevenZipException(ex);
        }
        size += data.length;
        if (observer != null)
            observer.setCompleted(size);
        return data.length;
    }
    @Override
    public void close() throws IOException {
        out.close();
    }
    @Override
    public void flush() throws IOException {
        out.flush();
    }
}
