package rzx.ui;

import java.awt.Component;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.border.Border;

/**
 *
 * Customized Scrollpane, provides an additional border
 * 
 * @author rene
 */
public class ZxScrollPane extends JScrollPane {
    
    public ZxScrollPane(String heading, String key, Component view) {
        super(view);
        ZxResourceFactory resourceFactory = ZxResourceFactory.getInstance();
        Border border = BorderFactory.createTitledBorder(
                resourceFactory.getString(heading+"."+key+".label"));
        setBorder(border);
    }
}
