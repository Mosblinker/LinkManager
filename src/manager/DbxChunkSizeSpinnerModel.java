/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import java.util.*;
import javax.swing.*;

/**
 * This is a spinner model for setting the Dropbox chunk size to use when 
 * uploading large files. This is in Mebibytes.
 * @author Mosblinker
 */
class DbxChunkSizeSpinnerModel extends SpinnerListModel{
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
     * This constructs a DbxChunkSizeSpinnerModel that is set to 8 MiB.
     */
    protected DbxChunkSizeSpinnerModel(){
        super(new DbxChunkSizeList());
        DbxChunkSizeSpinnerModel.this.setMultiplier(2);
    }
    /**
     * 
     * @return 
     */
    public int getMultiplier(){
        return getList().indexOf(getValue())+MULTIPLIER_MINIMUM;
    }
    /**
     * 
     * @param value 
     */
    public void setMultiplier(int value){
        setValue(getList().get(value-MULTIPLIER_MINIMUM));
    }
    /**
     * 
     * @return 
     */
    public long getChunkSize(){
        return BASE_CHUNK_SIZE * getMultiplier();
    }
    @Override
    public void setValue(Object elt){
            // If the value is a String
        if (elt instanceof String){
            try{    // Try to parse the value as an integer and use that instead
                elt = Integer.valueOf((String)elt);
            } catch (NumberFormatException ex) {}
        }
        super.setValue(elt);
    }
    /**
     * 
     */
    protected static class DbxChunkSizeList extends AbstractList<Integer>{
        @Override
        public Integer get(int index) {
                // Check the size
            Objects.checkIndex(index, size());
            return BASE_VALUE*(index+MULTIPLIER_MINIMUM);
        }
        @Override
        public int indexOf(Object o){
            if (o instanceof Integer){
                int value = (Integer)o;
                if (value % BASE_VALUE != 0)
                    return -1;
                value /= BASE_VALUE;
                value -= MULTIPLIER_MINIMUM;
                if (value >= 0 && value < size())
                    return value;
            }
            return -1;
        }
        @Override
        public int lastIndexOf(Object o){
            return indexOf(o);
        }
        @Override
        public int size() {
            return MULTIPLIER_MAXIMUM-MULTIPLIER_MINIMUM+1;
        }
    }
}
