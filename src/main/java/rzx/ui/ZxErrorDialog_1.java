package rzx.ui;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * To display error dialogs, using the resource bundle.
 * In order to use it, the resourceFactory must be proper initiated
 * 
 * @author rene
 */
public class ZxErrorDialog_1 {
   
    /**
     *  Various error dialog / do not use it. use ZxMessageDialog
     * 
     * @param comp calling graphic component
     * @param key full key into the resource bundle
     */
    public static void displaySimpleErrorMessage (Component comp, String key) {
        String message = ZxResourceFactory.getInstance().getString(key);
        JOptionPane.showMessageDialog(comp, message);
    }
    
    public static void displaySimpleErrorMessage (
        Component comp, String key, Exception ex) {
        String message = ZxResourceFactory.getInstance().getString(key);
        JOptionPane.showMessageDialog(comp, message+"\n"+ex.getMessage());
    }
}
