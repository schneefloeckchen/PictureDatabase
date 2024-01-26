/*
 * ZxDateField.java
 *
 * Created on 26. Dezember 2006, 14:44
 *
 */

package rzx.ui;

import java.text.Format;
import javax.swing.JFormattedTextField;
import java.util.*;

/**
 * Textfield that is designed for entering and displaying dates.
 *
 * @author Rene Zillmann
 */
public class ZxDateField extends JFormattedTextField {
    
    /** Creates a new instance of ZxDateField */
    public ZxDateField() {
        jInit();
    }

    public ZxDateField(Format format) {
        super(format);
        jInit();
    }
    private void jInit() {
       setValue(new Date());
    }
    
    /**
     * Method returns the date as entered into the textfield as
     * date object. If the date was not properly formatted, an ParseException
     * is thrown.
     * 
     * @throws java.text.ParseException 
     * @return the converted date object
     */

    public Date getDate() {
        return (Date) super.getValue();
    }
//    public Date getDate() throws ParseException {
//        String dateString = getDateAsString();
//        SimpleDateFormat format = new SimpleDateFormat();
//        Date date = format.parse(dateString);
//        return date;
//    }
//    
//    public String getDateAsString() {return (String) super.getValue();}
}
