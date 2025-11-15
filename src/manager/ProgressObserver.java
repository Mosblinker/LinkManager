/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package manager;

/**
 *
 * @author Mosblinker
 */
public interface ProgressObserver {
    /**
     * 
     * @return 
     */
    public boolean isIndeterminate();
    /**
     * 
     * @param value
     * @return 
     */
    public ProgressObserver setIndeterminate(boolean value);
    /**
     * 
     * @return 
     */
    public int getValue();
    /**
     * 
     * @param value
     * @return 
     */
    public ProgressObserver setValue(int value);
    /**
     * 
     * @param value
     * @return 
     */
    public default ProgressObserver setValueLong(long value){
        return setValue((int)value);
    }
    /**
     * 
     * @param offset
     * @return 
     */
    public default ProgressObserver incrementValue(int offset){
        return setValue(getValue()+offset);
    }
    /**
     * 
     * @return 
     */
    public default ProgressObserver incrementValue(){
        return incrementValue(1);
    }
    /**
     * 
     * @param offset
     * @return 
     */
    public default ProgressObserver decrementValue(int offset){
        return setValue(getValue()-offset);
    }
    /**
     * 
     * @return 
     */
    public default ProgressObserver decrementValue(){
        return decrementValue(1);
    }
    /**
     * 
     * @return 
     */
    public default ProgressObserver clearValue(){
        return setValue(getMinimum());
    }
    /**
     * 
     * @return 
     */
    public int getMaximum();
    /**
     * 
     * @param max
     * @return 
     */
    public ProgressObserver setMaximum(int max);
    /**
     * 
     * @param max
     * @return 
     */
    public default ProgressObserver setMaximumLong(long max){
        return setMaximum((int)max);
    }
    /**
     * 
     * @return 
     */
    public int getMinimum();
    /**
     * 
     * @param min
     * @return 
     */
    public ProgressObserver setMinimum(int min);
    /**
     * 
     * @return 
     */
    public String getText();
    /**
     * 
     * @param text 
     * @return  
     */
    public ProgressObserver setText(String text);
    /**
     * 
     * @return 
     */
    public boolean isTextShown();
    /**
     * 
     * @param value 
     * @return  
     */
    public ProgressObserver setTextShown(boolean value);
}
