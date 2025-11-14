package manager.util;

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
    
    public AbstractEditListAction() {
        
    }
    
    protected void applyValueToModel(String value, ListModel model, int row){
        DefaultListModel dlm = (DefaultListModel)model;
        dlm.set(row, value);
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
    public void actionPerformed(ActionEvent e) {
        list = (JList)e.getSource();
        ListModel model = list.getModel();


            //  Do a lazy creation of the popup editor

        if (editPopup == null)
            createEditPopup();

            //  Position the popup editor over top of the selected row

        int row = list.getSelectedIndex();
        Rectangle r = list.getCellBounds(row, row);

        editPopup.setPreferredSize(new Dimension(r.width, r.height));
        editPopup.show(list, r.x, r.y);

            //  Prepare the text field for editing

        editTextField.setText( list.getSelectedValue().toString() );
        editTextField.selectAll();
        editTextField.requestFocusInWindow();
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
            String value = editTextField.getText();
            ListModel model = list.getModel();
            int row = list.getSelectedIndex();
            applyValueToModel(value, model, row);
            editPopup.setVisible(false);
        });

            //  Add the editor to the popup

        editPopup = new JPopupMenu();
        editPopup.setBorder( new EmptyBorder(0, 0, 0, 0) );
        editPopup.add(editTextField);
    }
}
