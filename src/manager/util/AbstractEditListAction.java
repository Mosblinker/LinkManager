package manager.util;

import components.ListModelList;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

/**
 *	A simple popup editor for a JList that allows you to change
 *  the value in the selected row.
 *
 *  The default implementation has a few limitations:
 *
 *  a) the JList must be using the DefaultListModel
 *  b) the data in the model is replaced with a String object
 *
 *  If you which to use a different model or different data then you must
 *  extend this class and:
 *
 *  a) invoke the setModelClass(...) method to specify the ListModel you need
 *  b) override the applyValueToModel(...) method to update the model
 * 
 * <p>
 * <a href="https://tips4java.wordpress.com/2008/10/19/list-editor/">List Editor</a>
 * <br>
 * <a href="https://github.com/tips4java/tips4java/blob/main/source/EditListAction.java">EditListAction.java</a>
 * 
 * @param <E>
 * @author Rob Camick
 * @author Mosblinker
 */
public abstract class AbstractEditListAction<E> extends AbstractAction {
    private JList<E> list;

    private JPopupMenu editPopup;
    private JTextField editTextField;
    private E lastValue = null;
    private java.util.List<E> lastList = null;
    
    public AbstractEditListAction() {
        
    }
    /**
     * This converts the given value into a String so that it can be edited 
     * using a JTextField.
     * @param value The value to get the String of.
     * @return The String equivalent of the given value.
     */
    protected abstract String valueToString(E value);
    /**
     * This converts the given String into a value that can be stored in the 
     * JList.
     * @param value The new value, as a String.
     * @param oldValue The old value that is being replaced.
     * @return The new value for the JList.
     */
    protected abstract E valueFromString(String value, E oldValue);
    /**
     * This gets a List equivalent of the given ListModel that can be edited. 
     * Any change to the given List should be reflected in the given ListModel.
     * @param model The model to get a List from.
     * @return A List view of the given model, or null if the ListModel cannot 
     * be accessed using a List.
     */
    @SuppressWarnings("unchecked")
    protected java.util.List<E> getListFromModel(ListModel<E> model){
            // If the model already implements the List interface
        if (model instanceof java.util.List)
            return (java.util.List<E>)model;
        return new ListModelList<>(model);
    }
    
    /*
     *	Display the popup editor when requested
     */
    @Override
    @SuppressWarnings("unchecked")
    public void actionPerformed(ActionEvent e) {
        list = (JList<E>)e.getSource();
        ListModel<E> model = list.getModel();
            // Get a list view of the model
        lastList = getListFromModel(model);
            // If the model cannot be accessed as a List
        if (lastList == null) 
            return;

            //  Do a lazy creation of the popup editor

        if (editPopup == null)
            createEditPopup();

            //  Position the popup editor over top of the selected row

        int row = list.getSelectedIndex();
        if (row < 0)
            return;
        Rectangle r = list.getCellBounds(row, row);

        editPopup.setPreferredSize(new Dimension(r.width, r.height));
        editPopup.show(list, r.x, r.y);

            //  Prepare the text field for editing
        
        lastValue = lastList.get(row);
        editTextField.setText(valueToString(lastValue));
        editTextField.selectAll();
        editTextField.requestFocusInWindow();
    }
    /**
     * 
     * @param evt 
     */
    protected void setValue(ActionEvent evt){
        String value = editTextField.getText();
            if (lastList == null){
                ListModel<E> model = list.getModel();
                lastList = getListFromModel(model);
            }
            if (lastList != null){
                int row = list.getSelectedIndex();
                lastList.set(row, valueFromString(value,lastValue));
            }
            editPopup.setVisible(false);
    }
    /*
     *  Create the popup editor
     */
    private void createEditPopup(){
            //  Use a text field as the editor

        editTextField = new JTextField();
        Border border = UIManager.getBorder("List.focusCellHighlightBorder");
        editTextField.setBorder( border );

            //  Add an Action to the text field to save the new value to the model

        editTextField.addActionListener((ActionEvent e) -> {
            setValue(e);
        });

            //  Add the editor to the popup

        editPopup = new JPopupMenu();
        editPopup.setBorder( new EmptyBorder(0, 0, 0, 0) );
        editPopup.add(editTextField);
    }
}
