package rzx.ui;

import java.awt.Component;

/**
 * Panel to store Buttons
 * 
 * @author rene
 */
public class ZxButtonPanel extends ZxPanel {
    
    private ZxUIElementFactory m_buttonFactory = ZxUIElementFactory.getInstance();
    private int m_rows = 1;      // Number of rows in the button Panel
    
    public ZxButtonPanel() {
      jInit(1);
    }
    
    public ZxButtonPanel(int rows) {
      jInit(rows);
    }
    
    private void jInit(int rows) {
      
    }
/**
 * Aligns the size of the buttons in the UI
 * (when finished)
 * @todo finish method
 */    
    public void align() {
        Component[] buttons = getComponents();
//        for (Component button: buttons) {
//            System.err.println(button.getClass().getName());
//            
//        }
    }
}
