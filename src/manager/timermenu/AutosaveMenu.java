/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.timermenu;

import java.awt.event.*;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import static manager.timermenu.AutoTimerMenu.getDurationText;

/**
 * This is a menu that controls the autosave feature, along with providing the 
 * timers used to automatically save the lists to file.
 * @author Milo Steier
 */
public class AutosaveMenu extends JMenu{
    /**
     * This contains the frequencies at which the program automatically saves 
     * the lists to file, in milliseconds. <p>
     * 0: 0 - No automatic saving <p>
     * 1: 900000 - Every 15 minutes <p>
     * 2: 1800000 - Every 30 minutes <p>
     * 3: 3600000 - Every 1 hour <p>
     * 4: 7200000 - Every 2 hours <p>
     * 5: 14400000 - Every 4 hours <p>
     * 6: 28800000 - Every 8 hours <p>
     * 7: 43200000 - Every 12 hours <p>
     * 8: 86400000 - Every 24 hours
     */
    public static final int[] AUTOSAVE_FREQUENCIES = {
        0,
//        5000,
        900000,
        1800000,
        3600000,
        7200000,
        14400000,
        28800000,
        43200000,
        86400000
    };
    /**
     * This contains the durations, in milliseconds, to pause the automatic 
     * saving of the lists. <p>
     * 0: 900000 - Pause for 15 minutes <p>
     * 1: 1800000 - Pause for 30 minutes <p>
     * 2: 3600000 - Pause for 1 hour <p>
     * 3: 7200000 - Pause for 2 hours <p>
     * 4: 28800000 - Pause for 8 hours <p>
     * 5: 43200000 - Pause for 12 hours <p>
     * 6: 86400000 - Pause for 24 hours
     */
    public static final int[] PAUSE_AUTOSAVE_DURATION = {
        900000,
        1800000,
        3600000,
        7200000,
        28800000,
        43200000,
        86400000
    };
    /**
     * This contains the amount of milliseconds there are in a minute.
     */
    public static final int MILLISECONDS_PER_MINUTE = 60000;
    /**
     * This contains the amount of minutes there are in an hour.
     */
    public static final int MINUTES_PER_HOUR = 60;
    /**
     * This contains the prefix for the action commands used to pause the 
     * autosave. The index for the duration of the pause is appended to the end 
     * of the string.
     * @see #PAUSE_AUTOSAVE_DURATION
     */
    protected static final String PAUSE_AUTOSAVE_COMMAND_PREFIX = "PauseAutosave";
    /**
     * This contains the prefix for the action commands used to set the 
     * frequency at which the program will autosave. The index for the frequency 
     * is appended to the end of the string.
     * @see #AUTOSAVE_FREQUENCIES
     */
    protected static final String SET_AUTOSAVE_FREQUENCY_COMMAND_PREFIX = 
            "SetAutosaveFreq";
    /**
     * This is the action command used to resume the autosave after being 
     * paused.
     */
    public static final String RESUME_AUTOSAVE_COMMAND = "ResumeAutosave";
    /**
     * This is the action command for automatically saving the lists.
     */
    public static final String AUTOSAVE_COMMAND = "Autosave";
    /**
     * This identifies that the autosave frequency has been changed.
     */
    public static final String AUTOSAVE_FREQUENCY_PROPERTY_CHANGED = 
            "AutosaveFreqPropertyChanged";
    /**
     * This identifies a change in the property controlling whether the autosave 
     * is paused or running.
     */
    public static final String AUTOSAVE_PAUSED_PROPERTY_CHANGED = 
            "AutosavePausedPropertyChanged";
    /**
     * This identifies a change as to whether the autosave is actually running 
     * or not.
     */
    public static final String AUTOSAVE_RUNNING_PROPERTY_CHANGED = 
            "AutosaveRunningPropertyChanged";
    
    public static final String AUTOSAVE_FREQUENCY_TEXT_PROPERTY_CHANGED = 
            "AutosaveFreqTextPropertyChanged";
    /**
     * The index for the default autosave frequency.
     */
    private static final int DEFAULT_AUTOSAVE_FREQUENCY = 1;
    /**
     * The menu containing the menu items used to pause the autosave for a 
     * specified amount of time.
     */
    private JMenu pauseAutosaveMenu;
    /**
     * A list storing the menu items used to pause the autosave for a period of 
     * time.
     */
    private ArrayList<JMenuItem> pauseAutosaveItems;
    /**
     * The menu item used to resume the autosave when paused.
     */
    private JMenuItem resumeAutosaveItem;
    /**
     * This is the action listener used to listen to buttons and timers that 
     * relate to the autosave functionality.
     */
    private ActionListener autosaveActionListener;
    /**
     * This is the timer menu used to control the timer that is used to 
     * periodically save the lists automatically.
     */
    private AutosaveTimerMenu autosaveTimerMenu;
    /**
     * This is the timer used to pause the autosave timer for a set period of 
     * time.
     */
    private Timer pauseTimer = null;
    /**
     * This initializes the menu items and timers.
     */
    private void initialize(){
        autosaveActionListener = (ActionEvent evt) -> {
            autosaveActionPerformed(evt);
        };
        autosaveTimerMenu = new AutosaveTimerMenu();
        autosaveTimerMenu.addActionListener(autosaveActionListener);
        autosaveTimerMenu.addPropertyChangeListener((PropertyChangeEvent evt) -> {
            autosavePropertyChanged(evt);
        });
        this.add(autosaveTimerMenu);
        pauseAutosaveMenu = new JMenu("Pause Autosave");
        
        pauseAutosaveItems = new ArrayList<>();
        
            // A for loop to create the menu items for pausing the autosave
        for (int i = 0; i < PAUSE_AUTOSAVE_DURATION.length; i++){
                // Create the menu item with text stating the duration of the 
            JMenuItem button = new JMenuItem("Pause for "+      // pause
                    getDurationText(PAUSE_AUTOSAVE_DURATION[i]));
            button.setActionCommand(PAUSE_AUTOSAVE_COMMAND_PREFIX+i);
            button.addActionListener(autosaveActionListener);
            pauseAutosaveMenu.add(button);
            pauseAutosaveItems.add(button);
        }
        this.add(pauseAutosaveMenu);
        
        resumeAutosaveItem = new JMenuItem("Resume Autosave");
        resumeAutosaveItem.setActionCommand(RESUME_AUTOSAVE_COMMAND);
        resumeAutosaveItem.setEnabled(false);
        resumeAutosaveItem.addActionListener(autosaveActionListener);
        this.add(resumeAutosaveItem);
    }
    /**
     * This constructs an AutosaveMenu.
     */
    public AutosaveMenu(){
        super("Autosave");
        initialize();
    }
    /**
     * This returns the menu used to set the autosave frequency.
     * @return The menu used to set the autosave frequency.
     */
    public JMenu getFrequencyMenu(){
        return autosaveTimerMenu;
    }
    /**
     * This returns the menu used to pause the autosave for a certain period of 
     * time.
     * @return The menu used to pause the autosave for a certain period of time.
     */
    public JMenu getPauseMenu(){
        return pauseAutosaveMenu;
    }
    /**
     * This returns a list containing the radio button menu items used to set 
     * the autosave frequency.
     * @return 
     */
    public List<JRadioButtonMenuItem> getFrequencyButtons(){
        return autosaveTimerMenu.getDurationButtons();
    }
    
    public List<JMenuItem> getPauseButtons(){
        return pauseAutosaveItems;
    }
    
    public JMenuItem getResumeButton(){
        return resumeAutosaveItem;
    }
    
    public Timer getAutosaveTimer(){
        return autosaveTimerMenu.getTimer();
    }
    
    public Timer getPauseTimer(){
            // If the autosave pause timer has not been initialized yet
        if (pauseTimer == null){
            pauseTimer = new Timer(0,autosaveActionListener);
            pauseTimer.setActionCommand(RESUME_AUTOSAVE_COMMAND);
            pauseTimer.setRepeats(false);
        }
        return pauseTimer;
    }
    
    protected ActionListener getAutosaveListener(){
        return autosaveActionListener;
    }
    
    private void autosaveActionPerformed(ActionEvent evt){
            // This gets the autosave command
        String command = evt.getActionCommand();
            // If this is setting the autosave frequency and the command did not 
            // come from the timer menu (it would have already processed it)
        if (command.startsWith(SET_AUTOSAVE_FREQUENCY_COMMAND_PREFIX) && 
                evt.getSource() != autosaveTimerMenu){
            setFrequencyIndex(Integer.parseInt(command.substring(
                    SET_AUTOSAVE_FREQUENCY_COMMAND_PREFIX.length())));
        }   // If this is pausing the autosave
        else if (command.startsWith(PAUSE_AUTOSAVE_COMMAND_PREFIX)){
            setPaused(Integer.parseInt(command.substring(
                    PAUSE_AUTOSAVE_COMMAND_PREFIX.length())));
        }   // If this is resuming the autosave
        else if (RESUME_AUTOSAVE_COMMAND.equals(command)){
            AutosaveMenu.this.setPaused(false);
        }   // If this is to autosave the lists
//        else if (AUTOSAVE_COMMAND.equals(command)){
//            
//        }
        fireActionPerformed(evt);
    }
    
    private void autosaveFreqStateChanged(ItemEvent evt){
        if (evt.getItem() instanceof JRadioButtonMenuItem){
            JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem)evt.getItem();
            String command = menuItem.getActionCommand();
            if (menuItem.isSelected() && 
                    command.startsWith(SET_AUTOSAVE_FREQUENCY_COMMAND_PREFIX)){
                setFrequencyIndex(Integer.parseInt(command.substring(
                        SET_AUTOSAVE_FREQUENCY_COMMAND_PREFIX.length())));
            }
        }
    }
    
    private void autosavePropertyChanged(PropertyChangeEvent evt){
        switch(evt.getPropertyName()){
            case(AUTOSAVE_PAUSED_PROPERTY_CHANGED):
            case(AUTOSAVE_FREQUENCY_PROPERTY_CHANGED):
                updateAutosavePauseItems();
                if (autosaveTimerMenu.wasRunning() && autosaveTimerMenu.isStartable())
                    stopPauseTimer();
            case(AUTOSAVE_RUNNING_PROPERTY_CHANGED):
                firePropertyChange(evt.getPropertyName(),evt.getOldValue(),evt.getNewValue());
                break;
            case("text"):
                firePropertyChange(AUTOSAVE_FREQUENCY_TEXT_PROPERTY_CHANGED,
                        evt.getOldValue(),evt.getNewValue());
        }
    }
    
    protected void updateAutosavePauseItems(){
        resumeAutosaveItem.setEnabled(isPaused() && getFrequencyIndex() > 0);
        pauseAutosaveMenu.setEnabled(!isPaused() && getFrequencyIndex() > 0);
    }
    
    public String getAutosaveFrequencyText(){
        return autosaveTimerMenu.getText();
    }
    
    public void setAutosaveFrequencyText(String text){
        autosaveTimerMenu.setText(text);
    }
    
    public int getFrequencyIndex(){
        return autosaveTimerMenu.getDurationIndex();
    }
    
    public void setFrequencyIndex(int index){
        autosaveTimerMenu.setDurationIndex(index);
    }
    
    public int getFrequency(){
        return autosaveTimerMenu.getDuration();
    }
    
    public boolean isPaused(){
        return autosaveTimerMenu.isPaused();
    }
    
    public void setPaused(boolean paused){
        autosaveTimerMenu.setPaused(paused);
    }
    
    public void setPaused(int durIndex){
        if (durIndex < 0)
            AutosaveMenu.this.setPaused(false);
        else{
            AutosaveMenu.this.setPaused(true);
            if (durIndex < PAUSE_AUTOSAVE_DURATION.length){
                getPauseTimer().setInitialDelay(PAUSE_AUTOSAVE_DURATION[durIndex]);
                startPauseTimer();
            }
        }
    }
    
    @Override
    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        autosaveTimerMenu.setEnabled(enabled);
    }
    
    public boolean isRunning(){
        return autosaveTimerMenu.isRunning();
    }
    
    public void startAutosave(){
        autosaveTimerMenu.startTimer();
    }
    
    public void resumeAutosave(){
        autosaveTimerMenu.resumeTimer();
    }
    
    public void stopAutosave(){
        autosaveTimerMenu.stopTimer();
    }
    
    public boolean isPauseTimerRunning(){
        return pauseTimer != null && pauseTimer.isRunning();
    }
    
    public void startPauseTimer(){
        autosaveTimerMenu.setPaused(true);
        getPauseTimer().restart();
    }
    
    public void stopPauseTimer(){
        if (pauseTimer != null)
            pauseTimer.stop();
    }
    
    private class AutosaveTimerMenu extends AutoTimerMenu{
        
        public AutosaveTimerMenu(){
            super("Autosave Frequency",DEFAULT_AUTOSAVE_FREQUENCY);
        }
        @Override
        protected int[] getDurations() {
            return AUTOSAVE_FREQUENCIES;
        }
        @Override
        protected String getDuration0Text(){
            return "Turn Off Autosave";
        }
        @Override
        protected String getDurationTextPrefix(){
            return "Every ";
        }
        @Override
        protected String getTimerCommand() {
            return AUTOSAVE_COMMAND;
        }
        @Override
        protected String getDurationCommandPrefix() {
            return SET_AUTOSAVE_FREQUENCY_COMMAND_PREFIX;
        }
        @Override
        protected String getDurationIndexPropertyName(){
            return AUTOSAVE_FREQUENCY_PROPERTY_CHANGED;
        }
        @Override
        protected String getPausedPropertyName(){
            return AUTOSAVE_PAUSED_PROPERTY_CHANGED;
        }
        @Override
        protected String getTimerRunningPropertyName(){
            return AUTOSAVE_RUNNING_PROPERTY_CHANGED;
        }
        @Override
        protected String getTimerCommandPropertyName(){
            return "AutosaveActionCommandPropertyChanged";
        }
        @Override
        public void startTimer(){
            stopPauseTimer();
            super.startTimer();
        }
        @Override
        public void resumeTimer(){
            stopPauseTimer();
            super.resumeTimer();
        }
    }
}
