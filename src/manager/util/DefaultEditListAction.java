package manager.util;

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
 * @author Rob Camick
 * @author Mosblinker
 */
public class DefaultEditListAction extends AbstractEditListAction<Object> {
    @Override
    protected String valueToString(Object value) {
        return (value!=null)?value.toString():null;
    }
    @Override
    protected Object valueFromString(String value, Object oldValue) {
        return value;
    }
}
