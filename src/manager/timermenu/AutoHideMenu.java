/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.timermenu;

import java.util.List;
import javax.swing.*;

/**
 * This is a menu that controls the auto-hide feature, along with providing the 
 * timers used to automatically hide the hidden lists after being shown.
 * @author Milo Steier
 */
public class AutoHideMenu extends AutoTimerMenu{
    /**
     * This contains the durations, in milliseconds, to wait before 
     * automatically hiding the hidden lists after they have been shown. <p>
     * 0: 0 - Do not automatically hide hidden lists <p>
     * 1: 900000 - After 15 minutes <p>
     * 2: 1800000 - After 30 minutes <p>
     * 3: 2700000 - After 45 minutes <p>
     * 4: 3600000 - After 1 hour <p>
     * 5: 7200000 - After 2 hours <p>
     * 6: 14400000 - After 4 hours
     */
    public static final int[] AUTO_HIDE_WAIT_DURATIONS = {
        0,
//        5000,
//        60000,
        900000,
        1800000,
        2700000,
        3600000,
        7200000,
        14400000
    };
    /**
     * This contains the prefix for the action commands used to set the 
     * duration at which the program will wait for before auto-hiding hidden 
     * lists. The index for the duration 
     * is appended to the end of the string.
     * @see AUTO_HIDE_DURATIONS#AUTO_HIDE_WAIT_DURATIONS
     */
    protected static final String SET_AUTO_HIDE_DURATION_COMMAND_PREFIX = 
            "SetAutoHideDuration";
    /**
     * This is the action command for automatically hiding hidden lists.
     */
    public static final String AUTO_HIDE_COMMAND = "AutoHide";
    /**
     * This identifies that the auto-hide duration has been changed.
     */
    public static final String AUTO_HIDE_WAIT_DURATION_PROPERTY_CHANGED = 
            "AutoHideDurationPropertyChanged";
    /**
     * This identifies a change in the property controlling whether the auto-hide
     * is paused or running.
     */
    public static final String AUTO_HIDE_PAUSED_PROPERTY_CHANGED = 
            "AutoHidePausedPropertyChanged";
    /**
     * This identifies a change as to whether the auto-hide is actually running 
     * or not.
     */
    public static final String AUTO_HIDE_RUNNING_PROPERTY_CHANGED = 
            "AutoHideRunningPropertyChanged";
    /**
     * The index for the default auto-hide wait duration.
     */
    private static final int DEFAULT_AUTO_HIDE_WAIT_DURATION = 2;
    /**
     * This constructs an AutoHideMenu.
     */
    public AutoHideMenu(){
        super("Automatically Hide Lists",DEFAULT_AUTO_HIDE_WAIT_DURATION);
        setToolTipText("How long to wait for before automatically hiding hidden lists");
    }
    /**
     * This returns a list containing the radio button menu items used to set 
     * the auto-hide wait duration.
     * @return 
     */
    @Override
    public List<JRadioButtonMenuItem> getDurationButtons(){
        return super.getDurationButtons();
    }
        
    public Timer getAutoHideTimer(){
        return getTimer();
    }
    
    public void startAutoHide(){
        super.startTimer();
    }
    
    public void resumeAutoHide(){
        super.resumeTimer();
    }
    
    public void stopAutoHide(){
        super.stopTimer();
    }
    @Override
    protected int[] getDurations() {
        return AUTO_HIDE_WAIT_DURATIONS;
    }
    @Override
    protected String getDuration0Text(){
        return "Do not hide lists";
    }
    @Override
    protected String getDurationTextPrefix(){
        return "After ";
    }
    @Override
    protected String getTimerCommand() {
        return AUTO_HIDE_COMMAND;
    }
    @Override
    protected String getDurationCommandPrefix() {
        return SET_AUTO_HIDE_DURATION_COMMAND_PREFIX;
    }
    @Override
    protected String getDurationIndexPropertyName(){
        return AUTO_HIDE_WAIT_DURATION_PROPERTY_CHANGED;
    }
    @Override
    protected String getPausedPropertyName(){
        return AUTO_HIDE_PAUSED_PROPERTY_CHANGED;
    }
    @Override
    protected String getTimerRunningPropertyName(){
        return AUTO_HIDE_RUNNING_PROPERTY_CHANGED;
    }
    @Override
    protected String getTimerCommandPropertyName(){
        return "AutoHideActionCommandPropertyChanged";
    }
}
