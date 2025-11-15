/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.compress;

import java.io.*;
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
     * @param out 
     */
    public OutputStreamSequentialOutStream(OutputStream out){
        if (out == null)
            throw new NullPointerException();
        this.out = out;
    }
    @Override
    public int write(byte[] data) throws SevenZipException {
        try{
            out.write(data);
        } catch (IOException ex){
            throw new SevenZipException(ex);
        }
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
