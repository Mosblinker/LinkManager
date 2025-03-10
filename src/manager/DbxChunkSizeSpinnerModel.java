/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import javax.swing.AbstractSpinnerModel;

/**
 * This is a spinner model for setting the Dropbox chunk size to use when 
 * uploading large files. This is in Mebibytes.
 * @author Mosblinker
 */
class DbxChunkSizeSpinnerModel extends AbstractSpinnerModel{
    /**
     * This is the minimum value for the multiplier.
     */
    public static final int MULTIPLIER_MINIMUM = 1;
    /**
     * This is the maximum value for the multiplier.
     */
    public static final int MULTIPLIER_MAXIMUM = 32;
    /**
     * This is the base value for the chunk size. This is what gets multiplied 
     * to get the displayed chunk size.
     */
    private static final int BASE_VALUE = 4;
    /**
     * This is the minimum value for this spinner.
     */
    public static final int VALUE_MINIMUM = BASE_VALUE * MULTIPLIER_MINIMUM;
    /**
     * This is the maximum value for this spinner.
     */
    public static final int VALUE_MAXIMUM = BASE_VALUE * MULTIPLIER_MAXIMUM;
    /**
     * This is the base chunk size, in bytes. This is equal to 4 MiB.
     */
    protected static final long BASE_CHUNK_SIZE = 0x400000;
    /**
     * This is the multiplier for the value.
     */
    private int value = MULTIPLIER_MINIMUM;
    /**
     * This constructs a DbxChunkSizeSpinnerModel that is set to 8 MiB.
     */
    protected DbxChunkSizeSpinnerModel(){
        value = MULTIPLIER_MINIMUM+1;
    }
    
    public int getMultiplier(){
        return value;
    }
    
    public void setMultiplier(int value){
        if (this.value == value)
            return;
        if (value < MULTIPLIER_MINIMUM || value > MULTIPLIER_MAXIMUM)
            throw new IllegalArgumentException();
        this.value = value;
        fireStateChanged();
    }
    
    public int getNumber(){
        return BASE_VALUE * getMultiplier();
    }
    
    public long getChunkSize(){
        return BASE_CHUNK_SIZE * getMultiplier();
    }
    @Override
    public Object getValue() {
        return getNumber();
    }
    @Override
    public void setValue(Object value) {
        if (value == null || !(value instanceof Number))
            throw new IllegalArgumentException();
        setMultiplier(((Number)value).intValue()/BASE_VALUE);
    }
    @Override
    public Object getNextValue() {
        if (getMultiplier() < MULTIPLIER_MAXIMUM)
            return BASE_VALUE * (getMultiplier()+1);
        return null;
    }
    @Override
    public Object getPreviousValue() {
        if (getMultiplier() > MULTIPLIER_MINIMUM)
            return BASE_VALUE * (getMultiplier()-1);
        return null;
    }
}
