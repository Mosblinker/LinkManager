/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import components.progress.JProgressDisplayMenu;
import java.awt.event.ActionEvent;
import javax.swing.BoundedRangeModel;
import measure.format.binary.ByteUnitFormat;

/**
 *
 * @author Mosblinker
 */
public class JByteProgressDisplayMenu extends JProgressDisplayMenu{
    
    private boolean useBytes = false;
    
    private ByteUnitFormat formatter = new ByteUnitFormat();
    
    public JByteProgressDisplayMenu(){
        super();
    }
    
    public boolean getUseByteFormat(){
        return useBytes;
    }
    
    public void setUseByteFormat(boolean value){
        if (value != useBytes){
            useBytes = value;
            firePropertyChange("useByteFormat",!value,value);
            fireActionPerformed(new ActionEvent(this,ActionEvent.ACTION_FIRST,
                    "UseByteFormat",System.currentTimeMillis(),0));
        }
    }
    @Override
    public String format(BoundedRangeModel model){
        if (!useBytes || !isFractionDisplayed() || !isProgressDisplayed())
            return super.format(model);
            // Gets a copy of the text that is not null
        String str = (getString() != null) ? getString()+": " : "";
            // The current value for the model, offset by the minimum value
        long value = model.getValue() - model.getMinimum();
            // The maximum value for the model, offset by the minimum value
        long max = model.getMaximum() - model.getMinimum();
        if (isPercentageDisplayed()){
            str += String.format("%s%."+getPrecision()+"f%% ",str,
                    ((max==value)?1:(((double)value)/max))*100);
        }
        return str + "("+formatter.format(value) +" / "+formatter.format(max)+")";
    }
}
