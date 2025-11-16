/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.util.Objects;
import javax.swing.*;

/**
 *
 * @author Mosblinker
 */
public class DefaultProgressObserver implements ProgressObserver{
    /**
     * 
     */
    protected JProgressBar progressBar;
    /**
     * 
     */
    protected double divisor = 1;
    /**
     * 
     * @param progressBar 
     */
    public DefaultProgressObserver(JProgressBar progressBar){
        if (progressBar == null)
            throw new NullPointerException();
        this.progressBar = progressBar;
    }
    /**
     * 
     * @return 
     */
    public JProgressBar getProgressBar(){
        return progressBar;
    }
    @Override
    public boolean isIndeterminate() {
        return progressBar.isIndeterminate();
    }
    @Override
    public DefaultProgressObserver setIndeterminate(boolean value) {
        progressBar.setIndeterminate(value);
        return this;
    }
    @Override
    public int getValue() {
        return progressBar.getValue();
    }
    @Override
    public DefaultProgressObserver setValue(int value) {
        progressBar.setValue(value);
        return this;
    }
    @Override
    public int getMaximum() {
        return progressBar.getMaximum();
    }
    @Override
    public DefaultProgressObserver setMaximum(int max) {
        progressBar.setMaximum(max);
        return this;
    }
    @Override
    public int getMinimum() {
        return progressBar.getMinimum();
    }
    @Override
    public DefaultProgressObserver setMinimum(int min) {
        progressBar.setMinimum(min);
        return this;
    }
    @Override
    public DefaultProgressObserver setMaximumLong(long max){
            // Get the value needed to divide the file length to get it back 
            // into the range of integers
        divisor = LinkManagerUtilities.getFileSizeDivider(max);
        return setMaximum((int)Math.ceil(max / divisor));
    }
    @Override
    public DefaultProgressObserver setValueLong(long value){
            // Update the progress with the amount of bytes written
        return setValue((int)Math.ceil(value / divisor));
    }
    /**
     * 
     * @return 
     */
    protected String paramString(){
        return (isIndeterminate()?"intermediate,":"")+
                "value="+getValue()+
                ",minimum="+getMinimum()+
                ",maximum="+getMaximum()+
                ",text="+Objects.toString(getText(),"")+
                ",textShown="+isTextShown()+
                ",divisor="+divisor;
    }
    @Override
    public String toString(){
        return getClass().getName()+"["+paramString()+"]";
    }
    @Override
    public String getText() {
        return progressBar.getString();
    }
    @Override
    public DefaultProgressObserver setText(String text) {
        progressBar.setString(text);
        return this;
    }
    @Override
    public boolean isTextShown() {
        return progressBar.isStringPainted();
    }
    @Override
    public DefaultProgressObserver setTextShown(boolean value) {
        progressBar.setStringPainted(value);
        return this;
    }
}
