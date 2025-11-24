/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager.sync;

import java.io.File;
import java.io.IOException;
import manager.ProgressObserver;

/**
 *
 * @author Mosblinker
 * @param <U>
 * @param <D>
 * @param <A>
 */
public interface SyncMethod <U, D, A extends AccountData>{
    /**
     * 
     * @return 
     */
    public SyncMode getSyncMode();
    /**
     * 
     * @return
     * @throws Exception 
     */
    public A getAccountData() throws Exception;
    /**
     * 
     * @return 
     */
    public boolean isUsable();
    /**
     * 
     * @return 
     */
    public boolean isLoggedIn();
    /**
     * 
     * @param file
     * @param path
     * @param l
     * @return
     * @throws Exception
     * @throws IOException 
     */
    public D download(File file, String path, ProgressObserver l) throws Exception, IOException;
    /**
     * 
     * @param file
     * @param path
     * @return
     * @throws Exception
     * @throws IOException 
     */
    public default D download(File file, String path) throws Exception, IOException{
        return download(file,path,null);
    }
    /**
     * 
     * @param file
     * @param path
     * @param l
     * @return
     * @throws Exception
     * @throws IOException 
     */
    public U upload(File file, String path, ProgressObserver l) throws Exception, IOException;
    /**
     * 
     * @param file
     * @param path
     * @return
     * @throws Exception
     * @throws IOException 
     */
    public default U upload(File file, String path) throws Exception, IOException{
        return upload(file,path,null);
    }
}
