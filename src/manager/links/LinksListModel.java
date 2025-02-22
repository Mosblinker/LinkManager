/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package manager.links;

import components.ArrayListModel;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;
import java.util.function.*;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.Position;

/**
 *
 * @author Milo Steier
 */
public class LinksListModel extends ArrayListModel<String> implements 
        Comparable<LinksListModel>, ListSelectionModel{
    /**
     * This identifies that the listID of the list has changed.
     */
    public static final String LIST_ID_PROPERTY_CHANGED = 
            "ListIDPropertyChanged";
    /**
     * This identifies that the name of the list has changed.
     */
    public static final String LIST_NAME_PROPERTY_CHANGED = 
            "ListNamePropertyChanged";
    /**
     * This identifies that the size limit for the list has changed.
     */
    public static final String LIST_SIZE_LIMIT_PROPERTY_CHANGED = 
            "ListSizeLimitPropertyChanged";
    /**
     * This identifies a change as to whether the list allows duplicates or not.
     */
    public static final String LIST_ALLOWS_DUPLICATES_PROPERTY_CHANGED = 
            "listAllowsDuplicatesPropertyChanged";
    /**
     * This identifies a change as to whether the list is read only or not.
     */
    public static final String LIST_IS_READ_ONLY_PROPERTY_CHANGED = 
            "listIsReadOnlyPropertyChanged";
    /**
     * This identifies a change as to whether the list is hidden or not.
     */
    public static final String LIST_IS_HIDDEN_PROPERTY_CHANGED = 
            "listIsHiddenPropertyChanged";
    /**
     * This is the flag that controls whether the list allows duplicates.
     */
    public static final int ALLOW_DUPLICATES_FLAG = 0x01;
    /**
     * This is the flag that controls whether the list is read only.
     */
    public static final int READ_ONLY_FLAG = 0x02;
    /**
     * This returns a map that maps the list control flags to their property 
     * names. The returned map is unmodifiable.
     * @return 
     */
    private static NavigableMap<Integer, String> getFlagPropertyNamesMap(){
            // This is the map that will map the flags to their property names
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(ALLOW_DUPLICATES_FLAG, LIST_ALLOWS_DUPLICATES_PROPERTY_CHANGED);
        map.put(READ_ONLY_FLAG, LIST_IS_READ_ONLY_PROPERTY_CHANGED);
        
        return Collections.unmodifiableNavigableMap(map);
    }
    /**
     * 
     */
    public static final NavigableMap<Integer, String> FLAG_PROPERTY_NAMES_MAP = 
            getFlagPropertyNamesMap();
    /**
     * This is the listID for this list in the database.
     */
    private Integer listID;
    /**
     * This stores the name of this list.
     */
    private String name;
    /**
     * This stores the last time the list was modified in the database.
     */
    private long lastMod;
    /**
     * This stores when the list in the database was created.
     */
    private long created;
    /**
     * This stores the flags used to store the settings for this list.
     */
    private int flags = 0;
    /**
     * 
     */
    private Integer sizeLimit = null;
    /**
     * This stores whether this list is to be considered hidden by the program.
     */
    private boolean hidden = false;
    /**
     * This stores whether the list has been edited since last loaded or saved.
     */
    private boolean edited;
    /**
     * This stores whether the contents of the list have been edited since last 
     * loaded or saved.
     */
    private boolean contentsEdited;
    /**
     * This stores the old listID which is used to detect whether the listID has 
     * changed and to update the database accordingly.
     */
    protected Integer oldListID;
    /**
     * This stores the old name for this list which is used to detect whether 
     * the name has changed and to update the database accordingly.
     */
    protected String oldName;
    /**
     * This stores the old flags for this list which is used to detect whether 
     * the flags have changed and to update the database accordingly.
     */
    protected int oldFlags = 0;
    /**
     * 
     */
    protected Integer oldSizeLimit = null;
    /**
     * 
     */
    private boolean modLimitEnabled = true;
    /**
     * A set to store the links using a set. This is used to detect duplicates.
     */
    protected final Set<String> set;
    /**
     * This is the PropertyChangeSupport used to handle changes to the 
     * properties of this model.
     */
    private final PropertyChangeSupport changeSupport;
    /**
     * 
     */
    protected final DefaultListSelectionModel listSelModel;
    /**
     * 
     */
    private final List<ListDataEvent> events = new ArrayList<>();
    /**
     * 
     * @param name
     * @param listID 
     */
    public LinksListModel(String name, Integer listID){
        this.listID = listID;
        oldListID = listID;
            // If the list is non-null, trim the name.
        this.name = (name != null) ? name.trim() : null;
        checkName(this.name);
        oldName = this.name;
            // Set both the last modified and creation time to now
        lastMod = created = System.currentTimeMillis();
        contentsEdited = false;
        edited = false;
        set = new HashSet<>();
        listSelModel = new DefaultListSelectionModel();
        listSelModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        changeSupport = new PropertyChangeSupport(this);
    }
    /**
     * 
     * @param name 
     */
    public LinksListModel(String name){
        this(name,null);
    }
    /**
     * 
     * @param listID 
     */
    public LinksListModel(Integer listID){
        this(null,listID);
    }
    /**
     * 
     */
    public LinksListModel(){
        this(null,null);
    }
    /**
     * This returns whether this list has been edited
     * @return 
     */
    public boolean isEdited(){
        return edited;
    }
    /**
     * 
     * @param edited 
     */
    public void setEdited(boolean edited){
            // If no change would occur
        if (edited == this.edited)
            return;
        this.edited = edited;
            // If this list is no longer edited
        if (!edited){
            oldListID = listID;
            oldName = name;
            oldFlags = flags;
            oldSizeLimit = sizeLimit;
            contentsEdited = false;
            events.clear();
        }
        fireStateChanged();
    }
    /**
     * 
     */
    public void clearEdited(){
        setEdited(false);
    }
    /**
     * 
     */
    protected void updatePropertyEdited(){
        setEdited(!Objects.equals(oldListID, listID) || 
                !Objects.equals(oldName, name) || 
                oldFlags != flags || 
                !Objects.equals(oldSizeLimit, sizeLimit) || 
                contentsEdited);
    }
    /**
     * This returns whether the contents of this list have been modified
     * @return 
     */
    public boolean getContentsModified(){
        return contentsEdited;
    }
    /**
     * 
     */
    public void setContentsModified(){
        contentsEdited = true;
        setEdited(true);
    }
    /**
     * 
     * @return 
     */
    public Integer getListID(){
        return listID;
    }
    /**
     * 
     * @param listID 
     */
    public void setListID(Integer listID){
            // If there would be no change to the listID of this list
        if (Objects.equals(this.listID, listID))
            return;
            // Get the old listID for this list
        Integer oldID = this.listID;
        this.listID = listID;
            // Update whether this list has been edited
        updatePropertyEdited();
        firePropertyChange(LIST_ID_PROPERTY_CHANGED,oldID,listID);
    }
    /**
     * 
     * @return 
     */
    public String getListName(){
        return name;
    }
    /**
     * 
     * @param name 
     */
    private void checkName(String name){
            // If the name is not null and the name contains an asterisk
        if (name != null && name.contains("*"))
            throw new IllegalArgumentException("List name cannot contain an asterisk(*)");
    }
    /**
     * 
     * @param name 
     */
    public void setListName(String name){
            // If the list name is not null
        if (name != null)
            name = name.trim();
            // If there would be no change to the list name
        if (Objects.equals(this.name, name))
            return;
            // Check the list name
        checkName(name);
            // Get the old list name
        String old = this.name;
        this.name = name;
            // Update whether this list has been edited
        updatePropertyEdited();
        firePropertyChange(LIST_NAME_PROPERTY_CHANGED,old,name);
    }
    /**
     * 
     * @return 
     */
    public long getLastModified(){
        return lastMod;
    }
    /**
     * 
     * @param lastMod 
     */
    public void setLastModified(long lastMod){
        this.lastMod = lastMod;
    }
    /**
     * 
     */
    public void setLastModified(){
        setLastModified(System.currentTimeMillis());
    }
    /**
     * 
     * @return 
     */
    public long getCreationTime(){
        return created;
    }
    /**
     * 
     * @param created 
     */
    public void setCreationTime(long created){
        this.created = created;
    }
    /**
     * This returns whether this list is hidden
     * @return 
     */
    public boolean isHidden(){
        return hidden;
    }
    /**
     * 
     * @param value 
     */
    public void setHidden(boolean value){
            // If there would be no change as to whether this list is hidden
        if (value == hidden)
            return;
        hidden = value;
        firePropertyChange(LIST_IS_HIDDEN_PROPERTY_CHANGED,value);
    }
    /**
     * This returns an integer storing the flags used to store the settings for 
     * this list.
     * @return An integer containing the flags for this list.
     * @see #setFlags(int) 
     * @see #getFlag(int) 
     * @see #setFlag(int, boolean) 
     * @see #ALLOW_DUPLICATES_FLAG
     * @see #READ_ONLY_FLAG
     */
    public int getFlags(){
        return flags;
    }
    /**
     * This sets the flags used to store the settings for this list.
     * @param flags An integer containing the new flags for this list.
     * @see #getFlags() 
     * @see #getFlag(int) 
     * @see #setFlag(int, boolean) 
     * @see #ALLOW_DUPLICATES_FLAG
     * @see #READ_ONLY_FLAG
     * @see FLAG_PROPERTY_NAMES_MAP
     */
    public void setFlags(int flags){
            // If there would be no change to the list flags
        if (this.flags == flags)
            return;
            // This gets the flags that will be changed. The old and new flags 
            // are XOR'd to get the flags that will be changed, so that 
        int changed = this.flags ^ flags;
        this.flags = flags;
            // Update whether this list has been edited
        updatePropertyEdited();
            // Get the highest bit that was changed for the flags
        int highestChanged = Integer.highestOneBit(changed);
            // Go through the flags that have a property name assigned to them
        for (Integer flag : FLAG_PROPERTY_NAMES_MAP.navigableKeySet()){
                // If the flag is somehow null
            if (flag == null)
                continue;   // Skip this flag
                // If the current flag is one of the flags that changed
            if (getFlag(flag,changed))
                firePropertyChange(FLAG_PROPERTY_NAMES_MAP.get(flag),getFlag(flag));
                // If this is the last bit that changed in the flags
            if (Integer.highestOneBit(flag) >= highestChanged)
                break;
        }
    }
    /**
     * This returns whether the given flag has been set on the given value. 
     * @param flags The value to check whether the flag is set for.
     * @param flag The flag to check for.
     * @return Whether the given flag is set.
     */
    public static boolean getFlag(int flag, int flags){
        return (flags & flag) == flag;
    }
    /**
     * This returns whether the given flag is set for this list.
     * @param flag The flag to check for.
     * @return Whether the flag is set.
     * @see #getFlags() 
     * @see #setFlags(int) 
     * @see #setFlag(int, boolean) 
     * @see #ALLOW_DUPLICATES_FLAG
     * @see #READ_ONLY_FLAG
     */
    public boolean getFlag(int flag){
        return getFlag(flag,getFlags());
    }
    /**
     * This sets whether the given flag is set for this list based off the given 
     * value. 
     * @param flag The flag to be set or cleared based off {@code value}.
     * @param value Whether the flag should be set or cleared.
     * @see #getFlags() 
     * @see #setFlags(int) 
     * @see #getFlag(int) 
     * @see #ALLOW_DUPLICATES_FLAG
     * @see #READ_ONLY_FLAG
     */
    public void setFlag(int flag, boolean value){
            // If the flag is to be set, OR the flags with the flag. Otherwise, 
            // AND the flags with the inverse of the flag.
        setFlags((value) ? getFlags() | flag : getFlags() & ~flag);
    }
    /**
     * 
     * @return 
     */
    public boolean getAllowsDuplicates(){
        return getFlag(ALLOW_DUPLICATES_FLAG);
    }
    /**
     * 
     * @param value 
     */
    public void setAllowsDuplicates(boolean value){
        setFlag(ALLOW_DUPLICATES_FLAG,value);
    }
    /**
     * 
     * @return 
     */
    public boolean isReadOnly(){
        return getFlag(READ_ONLY_FLAG);
    }
    /**
     * 
     * @param value 
     */
    public void setReadOnly(boolean value){
        setFlag(READ_ONLY_FLAG,value);
    }
    /**
     * 
     * @return 
     */
    public Integer getSizeLimit(){
        return sizeLimit;
    }
    /**
     * 
     * @param limit 
     */
    public void setSizeLimit(Integer limit){
            // If the size limit would not change
        if (Objects.equals(sizeLimit, limit))
            return;
            // If the size limit is not null and is less than or equal to zero
        if (limit != null && limit <= 0)
            throw new IllegalArgumentException("Size limit must be greater than zero");
            // Get the old size limit
        Integer old = sizeLimit;
        sizeLimit = limit;
            // Update whether this list has been edited
        updatePropertyEdited();
        firePropertyChange(LIST_SIZE_LIMIT_PROPERTY_CHANGED,old,sizeLimit);
    }
    /**
     * 
     * @return 
     */
    public Integer getSpaceRemaining(){
            // If the size limit is null (there is no size limit)
        if (sizeLimit == null)
            return null;
        return Math.max(0, sizeLimit-size());
    }
    /**
     * 
     * @return 
     */
    public boolean isFull(){
        return sizeLimit != null && size() >= sizeLimit;
    }
    /**
     * 
     * @return 
     */
    public boolean isModificationLimitEnabled(){
        return modLimitEnabled;
    }
    /**
     * 
     * @param value 
     */
    public void setModificationLimitEnabled(boolean value){
        modLimitEnabled = value;
    }
    
    
    
    /**
     * 
     * @param text
     * @param searchText
     * @param matchCase
     * @return 
     */
    protected boolean matchText(String text, String searchText, boolean matchCase){
        if (!matchCase)             // If the search is case insensitive
            text = text.toUpperCase();
        return text != null && text.contains(searchText);
    }
    /**
     * 
     * @param text
     * @param matchSpaces
     * @param matchCase
     * @return 
     */
    protected String getSearchString(String text, boolean matchSpaces, boolean matchCase){
        if (!matchSpaces)           // If the search is not matching white spaces
            text = text.trim();
        if (!matchCase)             // If the search is case insensitive
            text = text.toUpperCase();
        return text;
    }
    /**
     * 
     * @param text
     * @param start
     * @param direction
     * @param matchSpaces
     * @param matchCase
     * @param wrapAround
     * @return 
     */
    public int getNextMatch(String text, int start, Position.Bias direction, 
            boolean matchSpaces, boolean matchCase, boolean wrapAround){
        if (direction == null)      // If no direction was given
            return -1;
            // Format the search text based off the given settings
        text = getSearchString(text,matchSpaces,matchCase);
            // This gets the value to use to iterate through the list. If the 
            // direction is backwards, this will get a negative value. 
            // Otherwise, this will be a positive value.
        int dir = (direction == Position.Bias.Backward) ? -1 : 1;
        int index;  // This gets the index of the next matching string
            // If the search does not start in the list and the search is going 
        if (start < 0 && direction == Position.Bias.Backward)   // backwards
            index = size();
        else
            index = start;
            // A for loop to search for the next matching string in the list, 
            // starting at the next/previous index and ending once it has 
            // reached the end of the list
        for (index += dir; index >= 0 && index < size(); index += dir){
                // If the current string contains the given search string
            if (matchText(get(index),text,matchCase))    
                return index;
        }   // If we are to wrap around and did not start at the beginning of the list
        if (wrapAround && start >= 0){
                // A for loop to search for the next matching string in the 
                // list, starting at the other end of the list and ending once 
                // it has reached where we originally started searching from
            for (index = (index+size())%size(); index >= 0 && 
                    index < size() && index != start; index += dir){
                    // If the current string contains the given search string
                if (matchText(get(index),text,matchCase))    
                    return index;
            }
        }
        return -1;
    }
    /**
     * 
     * @param text
     * @param matchSpaces
     * @param matchCase
     * @return 
     */
    public List<String> getMatches(String text, boolean matchSpaces, boolean matchCase){
            // Format the search text based off the given settings
        String searchText = getSearchString(text,matchSpaces,matchCase);
            // An ArrayList to get a list of matching strings
        ArrayList<String> arr = new ArrayList<>(this);
            // Remove the non-matching strings
        arr.removeIf((String t) -> {
            return !matchText(t,searchText,matchCase);
        });
        return arr;
    }
    /**
     * 
     * @param text
     * @param matchSpaces
     * @param matchCase
     * @return 
     */
    public List<Integer> getMatchingIndexes(String text, boolean matchSpaces, 
            boolean matchCase){
            // Format the search text based off the given settings
        text = getSearchString(text,matchSpaces,matchCase);
            // An ArrayList to get a list of indexes of matches
        ArrayList<Integer> indexes = new ArrayList<>();
            // A for loop to go through the elements in this list
        for (int index = 0; index < size(); index++){
                // If the current string contains the given search string
            if (matchText(get(index),text,matchCase))
                indexes.add(index);
        }
        return indexes;
    }
    /**
     * 
     * @param list
     * @return 
     */
    public List<String> getCompatibleList(List<String> list){
            // If this model is read only or full.
        if (isReadOnly() || isFull())
            return new ArrayList<>();
            // Create and use a copy of the given list
        list = new ArrayList<>(list);
            // Remove any elements from the list that can't be added to this 
        list.removeIf((String t) -> !isValidElement(t));    // model
            // Get the space remaining in this model (or null if this model does 
        Integer remaining = getSpaceRemaining();    // not have a size limit)
            // If this model does not have a size limit or the given list could 
            // fit within this model as is
        if (remaining == null || remaining >= list.size())
            return list;
            // If this model does not allow duplicates (i.e. adding an item will 
            // remove any previous instances of it from this model)
        if (!getAllowsDuplicates()){
                // Items shared between this model and the list will not count 
                // towards the space remaining
                
                // This stores the index of the section of the list to check for 
                // shared items in.
            int startIndex = 0;
            do{     // Get a portion of the list to check for shared items in.
                List<String> shared = new ArrayList<>(list.subList(startIndex,
                        remaining));
                    // Remove all the items already in this model, since they 
                    // will be moved instead of added to this model
                shared.retainAll(this);
                    // Store the remaining space in this model that does not 
                    // account for these shared items. This will be used as the 
                    // starting index if this loop runs again, so as to not 
                    // check for items that are already accounted for.
                startIndex = remaining;
                    // Add the number of items shared by this model and the 
                    // portion of the list
                remaining += shared.size();
            }   // While there is still the potential to add more items due to 
                // the list sharing items with this model (if the start index is 
                // less than the remaining space, then there were items shared 
                // between the checked sublist and this model)
            while (remaining < list.size() && startIndex < remaining);
                // If, once the shared items are accounted for, this model can 
            if (remaining >= list.size())   // fit the whole list
                return list;
        }   // Return a sublist of the given list that is the right size to be 
            // added to this model without exceeding the space remaining
        return list.subList(0, remaining);
    }
    /**
     * 
     * @return 
     */
    public String getToolTipText(){
            // Get the size of the list
        int size = size();
            // This is the text for the tool tip for the tab
        String str = size + " Link";
            // If the size is not 1
        if (size != 1)
            str += "s";
            // If the size limit is not null (if a size limit is set)
        if (sizeLimit != null)
            str = String.format("%s (%d / %d)", str, size, sizeLimit);
        return str;
    }
    /**
     * 
     * @param element
     * @return 
     */
    protected boolean isValidElement(String element){
        return element != null && !element.isBlank();
    }
    /**
     * 
     * @param element
     * @return 
     */
    protected String checkElement(String element){
            // Check if the element is null
        Objects.requireNonNull(element);
            // If the element is not valid
        if (!isValidElement(element))
            throw new IllegalArgumentException();
        return element;
    }
    /**
     * 
     */
    private void checkIfReadOnly(){
            // If the modification limit is enabled and the list is read only
        if (modLimitEnabled && isReadOnly())
            throw new IllegalStateException("List is read only");
    }
    /**
     * 
     */
    private void checkIfFull(){
            // If the modification limit is enabled and the list is full
        if (modLimitEnabled && isFull())
            throw new IllegalStateException("List is full (list size: " +size()+
                    ", size limit: " + sizeLimit+")");
    }
    /**
     * 
     * @return 
     */
    private boolean canAddDuplicates(){
        return !modLimitEnabled || getAllowsDuplicates();
    }
    /**
     * 
     * @param index
     * @param element
     * @return 
     */
    private int retainIndex(int index, String element){
            // Copy the index that was given
        int initIndex = index;
            // Get the last instance of the given element
        int temp = lastIndexOf(element);
            // While there are duplicates of the given element that come after 
        while (temp > index){   // the given index
                // Remove the duplicate
            super.remove(temp);
                // Find the last instance of the given element again
            temp = lastIndexOf(element);
        }   // Get the first instance of the given element
        temp = indexOf(element);
            // While there are duplicates of the given element that come before 
        while (temp < index){   // the given index
                // Remove the duplicate
            super.remove(temp);
                // Find the first instance of the given element again
            temp = indexOf(element);
                // Decrement the index to account for the removed element
            index--;
        }
        return initIndex - index;
    }
    /**
     * 
     * @param index
     * @param element
     * @return 
     */
    private int addToSet(int index, String element){
            // If the element was added to the set
        if (set.add(element))
            return 0;
            // If this list allows duplicates
        if (canAddDuplicates())
            return 0;
            // Retain only the given index
        return retainIndex(index,element);
    }
    /**
     * 
     * @param element 
     */
    private void removeFromSet(String element){
            // If this list does not contain the given element
        if (!super.contains(element))
            set.remove(element);
    }
    @Override
    public void add(int index, String element){
            // Check if the list is read only
        checkIfReadOnly();
            // Check the given element
        checkElement(element);
            // If this list can add duplicates or the set does not contain the 
            // given element (this assumes the set contains all the elements in 
        if (canAddDuplicates() || !set.contains(element))   // this list)
                // Check if the list is full
            checkIfFull();
            // Add the element to this list
        super.add(index, element);
            // Add the element to the set
        addToSet(index,element);
            // The contents of the list have been modified
        setContentsModified();
    }
    // TODO: Implement a helper method for the addAll method that throws 
    // exceptions, with the main addAll method catching those exceptions, 
    // getting the interval that was successfully added, notifying the listeners 
    // of that interval, removing duplicates if necessary, and then forwarding 
    // the exception to the called
//    /**
//     * 
//     * @param index
//     * @param c
//     * @throws Exception 
//     */
//    private void addAllImpl(int index, Collection<? extends String> c) 
//            throws Exception{
//            
//    }
    @Override
    public boolean addAll(int index, Collection<? extends String> c){
            // Check if the list is read only
        checkIfReadOnly();
            // Check if the collection is null
        Objects.requireNonNull(c);
            // If the collection is empty
        if (c.isEmpty())
            return false;
            // If duplicates cannot be added to this list
        if (!canAddDuplicates()){
                // Create a set version of the given collection
            c = new LinkedHashSet<>(c);
        }   // Go through the elements in the given collection to check them
            // TODO: Allow up to the invalid elements to be added before 
            // throwing the exception
        for (String temp : c){
                // Check the current element in the collection
            checkElement(temp);
        }   // Increment the modification count
        modCount++;
            // This is the offset for the index for the next element to be added 
        int offset = 0;     // at in this list
            // Get the space remaining in this list, or null if this list does 
        Integer remaining = getSpaceRemaining();    // not have a size limit
            // If the modification limits are disabled for this list
        if (!modLimitEnabled)
                // Ignore the size limit
            remaining = null;
            // Go through the elements in the given collection
        for (String temp : c){
                // If the current element is not a valid element for this list
            if (!isValidElement(temp))
                break;
                // If either duplicates can be added to this list or if the 
                // set does not contain the current element (assuming the set 
                // contains all the elements in this list) and this list has a 
                // size limit set for it
            if ((canAddDuplicates() || !set.contains(temp)) && remaining!=null)
                    // Decrement the remaining spaces
                remaining --;
                // Add the value to the internal list
            list.add(index+offset, temp);
                // Increment the offset
            offset++;
                // If this list has a size limit and there is no space remaining 
            if (remaining != null && remaining <= 0)    // in this list
                break;
        }   // Get the last index in the interval that was added, exclusive
        int endIndex = index+offset;
            // Notify any listeners of the interval that was added to the list
        fireIntervalAdded(index,endIndex-1);
            // Add all the values to the set
        set.addAll(c);
            // If this list cannot add duplicates
        if (!canAddDuplicates()){
                // If this list's size is larger than the last index in the 
                // added interval (i.e. if there are elements in this list that 
                // come after the interval that was added)
            if (size() > endIndex)
                    // Remove any duplicates that come after the interval that 
                    // was added
                subList(endIndex,size()).removeAll(c);
                // If the start of the interval is not the start of this list 
                // (i.e. there are elements in this list that come before the 
                // interval that was added)
            if (index > 0)
                    // Remove any duplicates that come before the interval that 
                    // was added
                subList(0,index).removeAll(c);
        }   // The contents of the list have been modified
        setContentsModified();
            // If the size of the interval that was added is less than the size 
            // of the collection that was to be added (i.e. if there was an 
            // interuption while adding the elements to this list, either 
            // because of an invalid element or the list being full)
        if (offset < c.size()){
                // Check if this list is now full
            checkIfFull();
            // TODO: Check for invalid elements
        }
        return true;
    }
    @Override
    public String set(int index, String element){
            // Check if the list is read only
        checkIfReadOnly();
            // Set the value in this list, getting the old value from the list
        String old = super.set(index, checkElement(element));
            // Remove the old value from the set
        removeFromSet(old);
            // Add the new value to the set
        addToSet(index,element);
            // The contents of the list have been modified
        setContentsModified();
        return old;
    }
    @Override
    public String remove(int index){
            // Check if the list is read only
        checkIfReadOnly();
            // Get the value that was removed
        String value = super.remove(index);
            // Remove the value from the set
        removeFromSet(value);
            // The contents of the list have been modified
        setContentsModified();
        return value;
    }
    /**
     * 
     */
    private void retainListInSet(){
            // If this list is empty
        if (isEmpty())
                // Clear the set
            set.clear();
        else    // Retain only elements that are in this list
            set.retainAll(list);
            // The contents of the list have been modified
        setContentsModified();
    }
    @Override
    protected void removeRange(int fromIndex, int toIndex){
            // If the fromIndex and toIndex are the same
        if (fromIndex == toIndex)
            return;
            // Check if the list is read only
        checkIfReadOnly();
            // Remove the given range from this list
        super.removeRange(fromIndex, toIndex);
            // Retain only the items in the set that are in this list
        retainListInSet();
    }
    @Override
    protected void sort(Comparator<? super String> c,int fromIndex,int toIndex){
            // Check if the list is read only
        checkIfReadOnly();
            // Get whether the modification limits are enabled
        boolean modLimit = modLimitEnabled;
            // Disable the modification limits
        modLimitEnabled = false;
            // Sort the set
        super.sort(c, fromIndex, toIndex);
            // Restore the modification limits
        modLimitEnabled = modLimit;
            // The contents of the list have been modified
        setContentsModified();
    }
    @Override
    protected boolean removeIf(Predicate<? super String> filter, int fromIndex, 
            int toIndex){
            // Remove all matching elements and get whether the list was modified
        boolean modified = super.removeIf(filter, fromIndex, toIndex);
            // If this list was modified
        if (modified)
                // Retain only the items in the set that are in this list
            retainListInSet();
        return modified;
    }
    @Override
    protected boolean batchRemove(Collection<?> c, boolean retain, 
            int fromIndex, int toIndex){
            // Batch remove the elements and get whether this list was modified
        boolean modified = super.batchRemove(c, retain, fromIndex, toIndex);
            // If this list was modified
        if (modified)
                // Retain only the items in the set that are in this list
            retainListInSet();
        return modified;
    }
    @Override
    protected void replaceRange(UnaryOperator<String> operator, int fromIndex, 
            int toIndex){
            // Replace the elements that are in the given range
        super.replaceRange(operator, fromIndex, toIndex);
            // Clear the set
        set.clear();
            // Add all the items in this list to the set
        set.addAll(list);
            // The contents of the list have been modified
        setContentsModified();
    }
    /**
     * 
     * @return 
     */
    public boolean removeDuplicates(){
            // Check if the list is read only
        checkIfReadOnly();
            // Get the current size of this list
        int size = size();
            // Go through the elements in this list
        for (int i = 0; i < size(); i++){
                // Get the current value
            String value = get(i);
                // Get the last instance of the current value
            int last = lastIndexOf(value);
                // While the current value is not the only instance (the first 
                // and last indexes are not the same)
            while (i < last){
                    // Remove the last instance of the value
                remove(last);
                    // Find the last instance of the current value
                last = lastIndexOf(value);
            }
        }   // Clear the set
        set.clear();
            // Add all the items in this list to the set
        set.addAll(list);
            // The contents of the list have been modified
        setContentsModified();
        return size != size();
    }
    /**
     * 
     * @param c 
     */
    public void setContents(List<String> c){
            // Check if the list is read only
        checkIfReadOnly();
            // Check if the collection is null
        Objects.requireNonNull(c);
            // Clear this list in preparation for the new values
        clear();
           // Add all the elements of the given list
        addAll(c);
    }
    /**
     * 
     * @param obj
     * @return 
     */
    public boolean listEquals(Object obj){
        return super.equals(obj);
    }
    @Override
    public boolean equals(Object obj){
            // If the object is not a LinksListModel
        if (!(obj instanceof LinksListModel))
            return false;
        return this == obj;
    }
    @Override
    public int hashCode(){
        return System.identityHashCode(this);
    }
    @SuppressWarnings("unchecked")
    @Override
    public <T extends EventListener> T[] getListeners(Class<T> listenerType){
            // If we're getting the PropertyChangeListeners
        if (listenerType == PropertyChangeListener.class)
            return (T[])getPropertyChangeListeners();
            // If we're getting the ListSelectionListeners
        else if (listenerType == ListSelectionListener.class)
            return (T[])getListSelectionListeners();
        else
            return super.getListeners(listenerType);
    }
    @Override
    protected void fireListDataEvent(int type, int index0, int index1){
            // Fire the list data event
        super.fireListDataEvent(type, index0, index1);
            // Add the list data event to the event list
        events.add(new ListDataEvent(this,type, index0, index1));
    }
    /**
     * 
     * @return 
     */
    public List<ListDataEvent> getListDataEvents(){
        return events;
    }
    /**
     * 
     * @param l 
     */
    public void addChangeListener(ChangeListener l){
        if (l != null)  // If the listener is not null
            listenerList.add(ChangeListener.class, l);
    }
    /**
     * 
     * @param l 
     */
    public void removeChangeListener(ChangeListener l){
        listenerList.remove(ChangeListener.class, l);
    }
    /**
     * 
     * @return 
     */
    public ChangeListener[] getChangeListeners(){
        return listenerList.getListeners(ChangeListener.class);
    }
    /**
     * 
     */
    protected void fireStateChanged(){
            // Create a change event for this list
        ChangeEvent evt = new ChangeEvent(this);
            // Go through the change listeners registered to this list
        for (ChangeListener l : getChangeListeners()){
            if (l != null)  // If the current change listener is not null
                l.stateChanged(evt);
        }
    }
    /**
     * This adds a {@code PropertyChangeListener} to this link. This listener is 
     * registered for all bound properties of this link. 
     * @param l The listener to be added.
     * @see #addPropertyChangeListener(String, PropertyChangeListener) 
     * @see #removePropertyChangeListener(PropertyChangeListener) 
     * @see #getPropertyChangeListeners() 
     */
    public void addPropertyChangeListener(PropertyChangeListener l){
        changeSupport.addPropertyChangeListener(l);
    }
    /**
     * This adds a {@code PropertyChangeListener} to this snake that listens for 
     * a specific property.
     * @param propertyName The name of the property to listen for.
     * @param l The listener to be added.
     * @see #addPropertyChangeListener(PropertyChangeListener) 
     * @see #removePropertyChangeListener(String, PropertyChangeListener) 
     * @see #getPropertyChangeListeners(String) 
     */
    public void addPropertyChangeListener(String propertyName, 
            PropertyChangeListener l){
        changeSupport.addPropertyChangeListener(propertyName, l);
    }
    /**
     * This removes a {@code PropertyChangeListener} from this link. This method 
     * should be used to remove {@code PropertyChangeListener}s that were 
     * registered for all bound properties of this link. 
     * @param l The listener to be removed.
     * @see #addPropertyChangeListener(PropertyChangeListener) 
     * @see #removePropertyChangeListener(String, PropertyChangeListener) 
     * @see #getPropertyChangeListeners() 
     */
    public void removePropertyChangeListener(PropertyChangeListener l){
        changeSupport.removePropertyChangeListener(l);
    }
    /**
     * This removes a {@code PropertyChangeListener} to this link that listens 
     * for a specific property. This method should be used to remove {@code 
     * PropertyChangeListener}s that were registered for a specific property
     * @param propertyName The name of the property.
     * @param l The listener to be removed.
     * @see #removePropertyChangeListener(PropertyChangeListener)
     * @see #addPropertyChangeListener(String, PropertyChangeListener) 
     * @see #getPropertyChangeListeners(String) 
     */
    public void removePropertyChangeListener(String propertyName, 
            PropertyChangeListener l){
        changeSupport.removePropertyChangeListener(propertyName, l);
    }
    /**
     * This returns an array of all {@code PropertyChangeListener}s that are 
     * registered on this link.
     * @return An array of the {@code PropertyChangeListener}s that have been 
     * added, or an empty array if no listeners have been added.
     * @see #getPropertyChangeListeners(String) 
     * @see #addPropertyChangeListener(PropertyChangeListener) 
     * @see #removePropertyChangeListener(PropertyChangeListener) 
     */
    public PropertyChangeListener[] getPropertyChangeListeners(){
        return changeSupport.getPropertyChangeListeners();
    }
    /**
     * This returns an array of all {@code PropertyChangeListener}s that are 
     * registered on this link for a specific property.
     * @param propertyName The name of the property.
     * @return An array of the {@code PropertyChangeListener}s that have been 
     * added for the specified property, or an empty array if no listeners have 
     * been added or the specified property is null.
     * @see #getPropertyChangeListeners() 
     * @see #addPropertyChangeListener(String, PropertyChangeListener) 
     * @see #removePropertyChangeListener(String, PropertyChangeListener) 
     */
    public PropertyChangeListener[] getPropertyChangeListeners(String propertyName){
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
    @Override
    public int compareTo(LinksListModel o) {
            // If the listIDs are the same
        if (Objects.equals(listID, o.listID)){
                // If the list names are the same
            if (Objects.equals(name, o.name)){
                    // If the last modified time of the lists are the same
                if (lastMod == o.lastMod){
                        // If the creation time of the lists are the same
                    if (created == o.created)
                            // Compare whether the lists are edited
                        return Boolean.compare(edited, o.edited);
                    else    // Compare the creation time of the lists
                        return Long.compare(created, o.created);
                }
                else    // Compare the last modified time of the lists
                    return Long.compare(lastMod, o.lastMod);
            }   // If this list's name is null
            else if (name == null)
                return 1;
                // If the given list's name is null
            else if (o.name == null)
                return -1;
            else    // Compare the list names
                return name.compareTo(o.name);
        }   // If this list's listID is null
        else if (listID == null)
            return 1;
            // If the given list's listID is null
        else if (o.listID == null)
            return -1;
        else    // Compare the listIDs
            return listID.compareTo(o.listID);
    }
    @Override
    public void setSelectionInterval(int index0, int index1) {
        listSelModel.setSelectionInterval(index0, index1);
    }
    @Override
    public void addSelectionInterval(int index0, int index1) {
        listSelModel.addSelectionInterval(index0, index1);
    }
    @Override
    public void removeSelectionInterval(int index0, int index1) {
        listSelModel.removeSelectionInterval(index0, index1);
    }
    @Override
    public int getMinSelectionIndex() {
        return listSelModel.getMinSelectionIndex();
    }
    @Override
    public int getMaxSelectionIndex() {
        return listSelModel.getMaxSelectionIndex();
    }
    @Override
    public boolean isSelectedIndex(int index) {
        return listSelModel.isSelectedIndex(index);
    }
    @Override
    public int getAnchorSelectionIndex() {
        return listSelModel.getAnchorSelectionIndex();
    }
    @Override
    public void setAnchorSelectionIndex(int index) {
        listSelModel.setAnchorSelectionIndex(index);
    }
    @Override
    public int getLeadSelectionIndex() {
        return listSelModel.getLeadSelectionIndex();
    }
    @Override
    public void setLeadSelectionIndex(int index) {
        listSelModel.setLeadSelectionIndex(index);
    }
    @Override
    public void clearSelection() {
        listSelModel.clearSelection();
    }
    @Override
    public boolean isSelectionEmpty() {
        return listSelModel.isSelectionEmpty();
    }
    @Override
    public void insertIndexInterval(int index, int length, boolean before) {
        listSelModel.insertIndexInterval(index, length, before);
    }
    @Override
    public void removeIndexInterval(int index0, int index1) {
        listSelModel.removeIndexInterval(index0, index1);
    }
    @Override
    public void setValueIsAdjusting(boolean valueIsAdjusting) {
        listSelModel.setValueIsAdjusting(valueIsAdjusting);
    }
    @Override
    public boolean getValueIsAdjusting() {
        return listSelModel.getValueIsAdjusting();
    }
    @Override
    public void setSelectionMode(int selectionMode) {
        listSelModel.setSelectionMode(selectionMode);
    }
    @Override
    public int getSelectionMode() {
        return listSelModel.getSelectionMode();
    }
    @Override
    public void addListSelectionListener(ListSelectionListener x) {
        listSelModel.addListSelectionListener(x);
    }
    @Override
    public void removeListSelectionListener(ListSelectionListener x) {
        listSelModel.removeListSelectionListener(x);
    }
    public ListSelectionListener[] getListSelectionListeners(){
        return listSelModel.getListSelectionListeners();
    }
    @Override
    public int[] getSelectedIndices() {
        return listSelModel.getSelectedIndices();
    }
    @Override
    public int getSelectedItemsCount() {
        return listSelModel.getSelectedItemsCount();
    }
    /**
     * 
     * @return 
     */
    public String getSelectedValue(){
            // Get the index of the first selected value
        int index = getMinSelectionIndex();
            // If the first selected index is within range, get the value at 
            // that index. Otherwise, return null
        return (index >= 0 && index < size()) ? get(index) : null;
    }
    /**
     * 
     * @param model 
     */
    public void setSelectionFrom(LinksListModel model){
            // If the model is this model or the model is null
        if (model == this || model == null)
            return;
            // Set this model's selection mode from the given model
        setSelectionMode(model.getSelectionMode());
            // If the selected items are the same for both models
        if (Arrays.equals(getSelectedIndices(), model.getSelectedIndices()))
            return;
            // Get if this model is currently adjusting the selection
        boolean adjusting = getValueIsAdjusting();
            // If the given model is not empty (we would only need to do one 
        if (!model.isSelectionEmpty())  // thing if it was)
            setValueIsAdjusting(true);
            // Clear the current selection
        clearSelection();
            // If the given model is empty
        if (model.isSelectionEmpty())
            return;
            // If only one item is selected
        if (model.getSelectedItemsCount() == 1){
                // Get the index of the selected value in this list
            int selIndex = indexOf(model.getSelectedValue());
                // If the selected value is in this list
            if (selIndex >= 0)
                setSelectionInterval(selIndex,selIndex);
        } else {
                // Go through the selected indexes in the given model
            for (int i : model.getSelectedIndices()){
                    // Get the index of the current selected value in this list
                int selIndex = indexOf(model.get(i));
                    // If the selected value is in this list
                if (selIndex >= 0)
                    addSelectionInterval(selIndex,selIndex);
            }   // Get the index of the anchor selection index in this list
            int anchor = indexOf(model.get(model.getAnchorSelectionIndex()));
                // Get the index of the lead selection index in this list
            int lead = indexOf(model.get(model.getLeadSelectionIndex()));
                // If both the lead and anchor selection are not in this list
            if (anchor < 0 && lead < 0)
                lead = anchor = getMinSelectionIndex();
                // If the anchor selection is not in this list
            else if (anchor < 0)    
                anchor = lead;
                // If the lead selection is not in this list
            else if (lead < 0)
                lead = anchor;
                // Transfer over the lead and anchor selections
            setAnchorSelectionIndex(anchor);
            setLeadSelectionIndex(lead);
        }
            // Restore whether this model was being adjusted
        setValueIsAdjusting(adjusting);
    }
}
