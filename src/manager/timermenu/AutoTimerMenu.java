/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.timermenu;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.swing.*;

/**
 * This is an abstract menu that provides a timer to control an automated 
 * action, along with controls for the timer's duration.
 * @author Milo Steier
 */
public abstract class AutoTimerMenu extends JMenu{
    /**
     * This contains the amount of milliseconds there are in a second.
     */
    public static final int MILLISECONDS_PER_SECOND = 1000;
    /**
     * This contains the amount of seconds there are in a minute.
     */
    public static final int SECONDS_PER_MINUTE = 60;
    /**
     * This contains the amount of minutes there are in an hour.
     */
    public static final int MINUTES_PER_HOUR = 60;
    /**
     * This contains the duration units, in singular terms, from smallest to 
     * largest.
     */
    private static final String[] DURATION_UNIT_LABELS = {
        "Millisecond",
        "Second",
        "Minute",
        "Hour"
    };
    /**
     * This contains the numbers to use to divide each duration unit to get the 
     * next largest unit. This array must end with a one and be as long as 
     * {@link DURATION_UNIT_LABELS}.
     */
    private static final int[] DURATION_DIVISIONS = {
        MILLISECONDS_PER_SECOND,
        SECONDS_PER_MINUTE,
        MINUTES_PER_HOUR,
        1
    };
    /**
     * This gets a String stating the given millisecond value in terms of the 
     * closest equivalent amount of milliseconds, seconds, minutes and/or hours.
     * @param millis The millisecond value to format.
     * @return A String stating the given milliseconds in terms of milliseconds, 
     * seconds, minutes and hours.
     */
    public static String getDurationText(int millis){
        if (millis <= 0)                    // If the duration is negative or zero
            return "Instantly";
        String text = "";                   // The String to return
            // The durations, as individual units, from smallest unit to largest
        int[] time = new int[DURATION_UNIT_LABELS.length];
        time[0] = millis;
            // Calculate the time, as separate units
        for (int i = 1; i < time.length; i++){
            time[i] = time[i-1] / DURATION_DIVISIONS[i-1];
            time[i-1] %= DURATION_DIVISIONS[i-1];
        }   // Create the time duration string
        for (int i = 0; i < DURATION_UNIT_LABELS.length && i < time.length; i++){
                // If there is a non-zero value for this unit
            if (time[i] > 0){
                    // This gets the text for this unit
                String temp = time[i] + " " + DURATION_UNIT_LABELS[i];
                    // If this time unit is not just 1 (prevents an "s" added to singles)
                if (time[i] > 1)
                    temp += "s";
                    // If there were any smaller units of time
                if (!text.isEmpty())
                    temp += " and ";
                text = temp + text;
            }
        }   // If the text is empty (somehow the duration is less than or equal 
            // to zero, despite that being checked for already), return a String 
            // stating that the duration is 0 ms. Otherwise, return the text.
        return (text.isEmpty()) ? "0 Milliseconds" : text;
    }
    /**
     * This is the timer used to perform actions automatically.
     */
    private Timer timer;
    /**
     * This stores whether the timer is paused.
     */
    private boolean paused = false;
    /**
     * This stores whether the timer is (or would be) running when enabled.
     */
    private boolean running = false;
    /**
     * This stores the index of the duration for the timer.
     */
    private int timerDur;
    /**
     * A list storing the radio button menu items used to set the timer duration.
     */
    private ArrayList<JRadioButtonMenuItem> durItems;
    /**
     * The button group handling the radio button menu items used to set the 
     * timer duration.
     */
    private ButtonGroup durGroup;
    /**
     * This is the action listener used to listen to the timer.
     */
    private ActionListener timerActionListener;
    /**
     * This is the item listener used to listen to the timer duration menu 
     * items.
     */
    private ItemListener timerDurListener;
    /**
     * This initializes the menu items and timers.
     */
    private void initialize(int index){
        timerDur = Objects.checkIndex(index, getDurations().length);
        timerActionListener = (ActionEvent evt) -> {
            timerActionPerformed(evt);
        };
        timerDurListener = (ItemEvent e) -> {
            timerDurStateChanged(e);
        };
        timer = new Timer(getDurations()[timerDur], timerActionListener);
        timer.setActionCommand(getTimerCommand());
        timer.setRepeats(isRepeating());
        
        durItems = new ArrayList<>();
        durGroup = new ButtonGroup();
            // A for loop to create the menu items for setting the duration 
        for (int i = 0; i < getDurations().length; i++){
                // Create the menu item to set the duration at the current index
            JRadioButtonMenuItem button = createDurationMenuItem(i,getDurations()[i]);
            durGroup.add(button);
            button.setSelected(i == timerDur);
            this.add(button);
            durItems.add(button);
        }
    }
    /**
     * 
     * @param index
     * @param duration
     * @return 
     */
    protected JRadioButtonMenuItem createDurationMenuItem(int index, int duration){
            // Get the text for the button
        String text = getDurationText(duration);
            // If the duration is zero
        if (duration == 0)
            text = Objects.requireNonNullElse(getDuration0Text(), text);
        else
            text = getDurationTextPrefix() + text;
           // Create the menu item with text stating the duration for the timer
        JRadioButtonMenuItem button = new JRadioButtonMenuItem(text);
        button.setActionCommand(getDurationCommandPrefix()+index);
        button.addItemListener(timerDurListener);
        return button;
    }
    /**
     * 
     * @param text
     * @param durIndex 
     */
    protected AutoTimerMenu(String text, int durIndex){
        super(text);
        initialize(durIndex);
    }
    /**
     * 
     * @param text 
     */
    protected AutoTimerMenu(String text){
        this(text, 0);
    }
    /**
     * 
     * @param durIndex 
     */
    protected AutoTimerMenu(int durIndex){
        this("", durIndex);
    }
    /**
     * 
     */
    protected AutoTimerMenu(){
        this(0);
    }
    /**
     * 
     * @param command
     * @return 
     */
    private boolean isDurationCommand(String command){
        return command.startsWith(getDurationCommandPrefix());
    }
    /**
     * 
     * @param command 
     */
    private void setDurationIndex(String command){
            // If the command does not contain the duration index
        if (command.length() <= getDurationCommandPrefix().length())
            return;
            // Remove the duration command prefix to get the duration index, as 
            // a String
        command = command.substring(getDurationCommandPrefix().length());
        try{    // Parse the duration index
            int index = Integer.parseInt(command);
                // If the duration index is within bounds
            if (index >= 0 && index < getDurations().length)
                    // Set the duration index
                setDurationIndex(index);
        }catch(NumberFormatException ex){ }
    }
    /**
     * This is invoked whenever the timer fires.
     * @param evt The action event.
     */
    private void timerActionPerformed(ActionEvent evt){
            // This gets the command
        String command = evt.getActionCommand();
            // If this command is setting the duration
        if (isDurationCommand(command)){
            setDurationIndex(command);
        }   // If the source is the timer and the timer does not repeat
        if (timer == evt.getSource() && !isRepeating()){
                // The timer is no longer running
            running = false;
            fireTimerRunningPropertyChanged(true);
        }
        fireActionPerformed(evt);
    }
    /**
     * This is invoked when a radio menu item is used to change the timer 
     * duration.
     * @param evt The item event.
     */
    private void timerDurStateChanged(ItemEvent evt){
            // If the item that changed is a radio menu item
        if (evt.getItem() instanceof JRadioButtonMenuItem){
                // Get the radio menu item that was used
            JRadioButtonMenuItem menuItem = (JRadioButtonMenuItem)evt.getItem();
                // Get the radio menu item's action command
            String command = menuItem.getActionCommand();
                // If the radio menu item is selected and its command is a set 
                // duration command
            if (menuItem.isSelected() && isDurationCommand(command)){
                setDurationIndex(command);
            }
        }
    }
    /**
     * This returns an array of integers representing the possible durations, in 
     * milliseconds, for the timer.
     * @return An array containing the durations for the timer.
     */
    protected abstract int[] getDurations();
    /**
     * This returns the text used for the radio menu item that sets the duration 
     * to zero, if one is present. If this is null, then whatever is returned 
     * by {@link #getDurationText} will be used instead. The default is null.
     * @return The text to use for a duration of zero, or null.
     */
    protected String getDuration0Text(){
        return null;
    }
    /**
     * This returns the prefix for the text used for the radio menu items that 
     * set the duration of the timer. This excludes the menu item used to set 
     * the duration to zero, which is covered by {@link #getDuration0Text()}
     * @return The prefix for the text for the duration radio menu items.
     */
    protected String getDurationTextPrefix(){
        return "";
    }
    /**
     * This returns whether the timer should be a repeating one. The default is 
     * {@code false}.
     * @return Whether the timer should be a repeating timer.
     */
    protected boolean isRepeating(){
        return false;
    }
    /**
     * This returns the default action command to use when the timer fires.
     * @return The default action command for the timer.
     */
    protected abstract String getTimerCommand();
    /**
     * This returns the prefix for the action command fired when the timer 
     * duration changes when using the radio menu items. The index for the 
     * duration is appended to the end of the action command.
     * @return The prefix for the action command for the duration menu items.
     */
    protected abstract String getDurationCommandPrefix();
    /**
     * This returns the name of the property for the duration index. The default 
     * is {@code durationIndex}.
     * @return The property name of the duration index.
     */
    protected String getDurationIndexPropertyName(){
        return "durationIndex";
    }
    /**
     * This returns the name of the property controlling whether the timer is 
     * paused or not. The default is {@code timerPaused}.
     * @return The name of the property controlling whether the timer is paused.
     */
    protected String getPausedPropertyName(){
        return "timerPaused";
    }
    /**
     * This returns the name of the property that indicates whether the timer is 
     * actually running or not. The default is {@code timerRunning}.
     * @return The name of the property indicating whether the timer is running.
     */
    protected String getTimerRunningPropertyName(){
        return "timerRunning";
    }
    /**
     * This returns the name of the property for the timer action command. The 
     * default is {@code timerActionCommand}.
     * @return The name of the property for the timer action command.
     */
    protected String getTimerCommandPropertyName(){
        return "timerActionCommand";
    }
    /**
     * 
     * @param wasRunning Whether the timer was previously running.
     */
    private void fireTimerRunningPropertyChanged(boolean wasRunning){
            // Get whether the timer is currently running
        boolean isRunning = isRunning();
            // If the timer changed whether it is running
        if (wasRunning != isRunning)
            firePropertyChange(getTimerRunningPropertyName(),wasRunning,
                    isRunning);
    }
    /**
     * This returns a list containing the radio button menu items used to set 
     * the timer duration.
     * @return 
     */
    public List<JRadioButtonMenuItem> getDurationButtons(){
        return durItems;
    }
    /**
     * 
     * @return 
     */
    public Timer getTimer(){
        return timer;
    }
    /**
     * 
     * @return 
     */
    public String getTimerActionCommand(){
        return timer.getActionCommand();
    }
    /**
     * 
     * @param command 
     */
    public void setTimerActionCommand(String command){
            // If the command is null
        if (command == null)
                // Use the default command for the timer
            command = getTimerCommand();
        
        if (command.equals(getTimerActionCommand()))
            return;
        String old = getTimerActionCommand();
        timer.setActionCommand(command);
        firePropertyChange(getTimerCommandPropertyName(),old,command);
    }
    
    protected ActionListener getTimerListener(){
        return timerActionListener;
    }
    
    protected ItemListener getDurationItemListener(){
        return timerDurListener;
    }
    
    public ButtonGroup getDurationButtonGroup(){
        return durGroup;
    }
    
    public int getDurationIndex(){
        return timerDur;
    }
    
    public void setDurationIndex(int index){
        if (index == timerDur)
            return;
        Objects.checkIndex(index, getDurations().length);
        boolean wasRunning = isRunning();
        timer.stop();
        int old = timerDur;
        timerDur = index;
        durItems.get(index).setSelected(true);
        timer.setInitialDelay(getDurations()[index]);
        timer.setDelay(getDurations()[index]);
        firePropertyChange(getDurationIndexPropertyName(),old,index);
        if (running && isStartable())
            timer.restart();
        fireTimerRunningPropertyChanged(wasRunning);
    }
    
    public int getDuration(){
        return getDurations()[timerDur];
    }
    
    public boolean isPaused(){
        return paused;
    }
    
    public void setPaused(boolean paused){
        if (this.paused == paused)
            return;
        this.paused = paused;
        firePropertyChange(getPausedPropertyName(),!paused,paused);
        if (paused)
            stopTimer(true);
        else if (running) 
            startTimer();
    }
    
    @Override
    public void setEnabled(boolean enabled){
        super.setEnabled(enabled);
        setTimerEnabled(enabled);
    }
    
    protected void setTimerEnabled(boolean enabled){
        if (enabled && running)
            resumeTimer();
        else
            stopTimer(true);
    }
    
    public boolean isRunning(){
        return timer.isRunning();
    }
    
    public boolean wasRunning(){
        return running;
    }
    
    protected void setRunning(boolean value){
        running = value;
    }
    
    protected boolean isStartable(){
        return isEnabled() && getDuration() > 0 && !paused;
    }
    
    public void startTimer(){
        boolean wasRunning = isRunning();
        setRunning(true);
        if (isStartable())
            timer.restart();
        fireTimerRunningPropertyChanged(wasRunning);
    }
    
    public void resumeTimer(){
        boolean wasRunning = isRunning();
        setRunning(true);
        if (isStartable())
            timer.start();
        fireTimerRunningPropertyChanged(wasRunning);
    }
    
    protected void stopTimer(boolean keepRunning){
        boolean wasRunning = isRunning();
        timer.stop();
        setRunning(running && keepRunning);
        fireTimerRunningPropertyChanged(wasRunning);
    }
    
    public void stopTimer(){
        stopTimer(false);
    }
    
    @Override
    protected String paramString(){
        return super.paramString()+
                ",durationIndex="+getDurationIndex()+
                ",duration="+getDuration()+
                ",timerActionCommand="+getTimerActionCommand()+
                ",paused="+isPaused()+
                ",running="+isRunning();
    }
}
