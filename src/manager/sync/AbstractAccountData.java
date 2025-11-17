/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.sync;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 *
 * @author Mosblinker
 */
public abstract class AbstractAccountData implements AccountData{
    /**
     * 
     */
    private PropertyChangeSupport changeSupport;
    /**
     * 
     */
    protected AbstractAccountData(){
        changeSupport = new PropertyChangeSupport(this);
    }
    @Override
    public void addPropertyChangeListener(PropertyChangeListener l) {
        if (l != null)
            changeSupport.addPropertyChangeListener(l);
    }
    @Override
    public void addPropertyChangeListener(String propertyName, PropertyChangeListener l) {
        if (l != null)
            changeSupport.addPropertyChangeListener(propertyName, l);
    }
    @Override
    public void removePropertyChangeListener(PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(l);
    }
    @Override
    public void removePropertyChangeListener(String propertyName, PropertyChangeListener l) {
        changeSupport.removePropertyChangeListener(propertyName, l);
    }
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners() {
        return changeSupport.getPropertyChangeListeners();
    }
    @Override
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName) {
        return changeSupport.getPropertyChangeListeners(propertyName);
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name,
     * old value, and new value. This method is for {@code Object} properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, Object oldValue, 
            Object newValue){
            // If the PropertyChangeSupport has been initialized
        if (changeSupport != null)  
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name, 
     * old value, and new value. This method is for {@code boolean} properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, boolean oldValue, 
            boolean newValue){
            // If the PropertyChangeSupport has been initialized
        if (changeSupport != null)  
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name and 
     * new value. This method is for {@code boolean} properties and the old 
     * value is assumed to be the inverse of the new value.
     * @param propertyName The name of the property.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, boolean newValue){
        firePropertyChange(propertyName, !newValue, newValue);
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name, 
     * old value, and new value. This method is for character properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, char oldValue, 
            char newValue){
        firePropertyChange(propertyName,Character.valueOf(oldValue),
                Character.valueOf(newValue));
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name, 
     * old value, and new value. This method is for integer properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, int oldValue, 
            int newValue){
            // If the PropertyChangeSupport has been initialized
        if (changeSupport != null)  
            changeSupport.firePropertyChange(propertyName, oldValue, newValue);
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name, 
     * old value, and new value. This method is for byte properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, byte oldValue, 
            byte newValue){
        firePropertyChange(propertyName,Byte.valueOf(oldValue),
                Byte.valueOf(newValue));
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name, 
     * old value, and new value. This method is for short properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, short oldValue, 
            short newValue){
        firePropertyChange(propertyName,Short.valueOf(oldValue),
                Short.valueOf(newValue));
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name, 
     * old value, and new value. This method is for long properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, long oldValue, 
            long newValue){
        firePropertyChange(propertyName,Long.valueOf(oldValue),
                Long.valueOf(newValue));
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name, 
     * old value, and new value. This method is for float properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, float oldValue, 
            float newValue){
        firePropertyChange(propertyName,Float.valueOf(oldValue),
                Float.valueOf(newValue));
    }
    /**
     * This fires a {@code PropertyChangeEvent} with the given property name, 
     * old value, and new value. This method is for double properties.
     * @param propertyName The name of the property.
     * @param oldValue The old value.
     * @param newValue The new value.
     */
    protected void firePropertyChange(String propertyName, double oldValue, 
            double newValue){
        firePropertyChange(propertyName,Double.valueOf(oldValue),
                Double.valueOf(newValue));
    }
}
