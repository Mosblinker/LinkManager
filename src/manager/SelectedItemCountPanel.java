/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager;

import components.JListSelector;
import java.awt.BorderLayout;
import java.util.Objects;
import javax.swing.*;
import javax.swing.event.*;

/**
 *
 * @author Milo Steier
 */
public class SelectedItemCountPanel extends JPanel implements ListSelectionListener{
    
    public static final String NAME_TEXT_PROPERTY_CHANGED = "NameTextPropertyChanged";
    
    public static final String MAXIMUM_VALUE_PROPERTY_CHANGED = "MaximumValuePropertyChanged";
    
    private JLabel nameLabel;
    
    private JLabel countLabel;
    
    private int value;
    
    private Integer maximum = null;
    
    private void initialize(){
        nameLabel = new JLabel("Selected:");
        countLabel = new JLabel(getValueText());
        
        add(nameLabel,BorderLayout.LINE_START);
        add(countLabel, BorderLayout.CENTER);
    }
    
    public SelectedItemCountPanel(int value){
        super(new BorderLayout(6,0));
        this.value = value;
        initialize();
    }
    
    public SelectedItemCountPanel(){
        this(0);
    }
    
    public void addChangeListener(ChangeListener l){
        if (l != null)
            listenerList.add(ChangeListener.class, l);
    }
    
    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class, l);
    }
    
    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }
    
    protected void fireStateChanged(){
        ChangeEvent evt = new ChangeEvent(this);
        for (ChangeListener l : getChangeListeners()){
            if (l != null)
                l.stateChanged(evt);
        }
    }
    
    public JLabel getNameLabel(){
        return nameLabel;
    }
    
    public JLabel getCountLabel(){
        return countLabel;
    }
    
    public int getValue(){
        return value;
    }
    
    public void setValue(int value){
        if (this.value == value)
            return;
        this.value = value;
        updateCountText();
    }
    
    protected String getValueText(){
        String text = "";
        if (maximum != null)
            text = " / "+maximum;
        return value+text;
    }
    
    protected void updateCountText(){
        countLabel.setText(getValueText());
        fireStateChanged();
    }
    
    public Integer getMaximumValue(){
        return maximum;
    }
    
    public void setMaximumValue(Integer maximum){
        if (Objects.equals(maximum, this.maximum))
            return;
        Integer old = this.maximum;
        this.maximum = maximum;
        firePropertyChange(MAXIMUM_VALUE_PROPERTY_CHANGED,old,maximum);
        updateCountText();
    }
    
    public String getNameText(){
        return nameLabel.getText();
    }
    
    public void setNameText(String text){
        if (Objects.equals(text, getNameText()))
            return;
        String old = getNameText();
        nameLabel.setText(text);
        firePropertyChange(NAME_TEXT_PROPERTY_CHANGED,old,text);
    }

    @Override
    public void valueChanged(ListSelectionEvent evt) {
        if (evt.getSource() instanceof JListSelector){
            setValue(((JListSelector) evt.getSource()).getSelectedItemsCount());
        } else if (evt.getSource() instanceof JList){
            setValue(((JList)evt.getSource()).getSelectedIndices().length);
        } else if (evt.getSource() instanceof ListSelectionModel){
            setValue(((ListSelectionModel)evt.getSource()).getSelectedItemsCount());
        }
    }
}
