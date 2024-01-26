package rzx.ui;

import java.awt.Component;
import javax.swing.JOptionPane;

/**
 * Various Message dialogs with and without choice
 *
 * @author rene
 */
public class ZxMessageDialog {

    public static int displayChoiceDialog(Component comp, String key) {
        String message = ZxResourceFactory.getInstance().getString(key);
        String title = ZxResourceFactory.
            getInstance().getString("generic.decision.text");
        int response = JOptionPane.showConfirmDialog
            (comp, message, title, JOptionPane.YES_NO_OPTION);
        return response;
    }
    
    /**
     * Displays a popup dialog with a generic message. The message text is
     * taken from the resource file. The title for the message box is loaded
     * from the resource as well, using the key "generic.message.text". No
     * return value is provided.
     * 
     * @param comp The calling component, or null
     * @param key the key String for the resource file, no prefix or similar.
     */
    public static void displayMessage (Component comp, String key) {
        String message = ZxResourceFactory.getInstance().getString(key);
        String title = ZxResourceFactory.
            getInstance().getString("generic.message.text");
        JOptionPane.showMessageDialog(comp, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void displayMessage(Component comp, String key, int value) {
        String message = ZxResourceFactory.getInstance().getString(key);
        String title = ZxResourceFactory.
            getInstance().getString("generic.message.text");
        String finalMessage = String.format(message, value);
        JOptionPane.showMessageDialog(comp, finalMessage, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Displays a popup dialog with a message about a caught exception. The
     * localized message of the text is displayed along with the type of the
     * Exception.
     * 
     * @param comp The calling component, or null
     * @param key the key String for the resource file, no prefix or similar.
     * @param ex the exception caught
     */
    public static void displayExceptionMessage (Component comp, String key, Exception ex) {
      String message = ZxResourceFactory.getInstance().getString(key) +
          "\n" + ex.getClass().getName() +
          "\n" + ex.getLocalizedMessage();
      String title =  ZxResourceFactory.getInstance().
          getString("exception.message.text");
      JOptionPane.showMessageDialog(comp, message, title,
          JOptionPane.ERROR_MESSAGE);
    }
}
