/*
 */
package pictures.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionListener;
import javax.swing.JButton;
import javax.swing.JDialog;
import java.util.logging.Logger;
import rzx.ui.ZxUIElementFactory;
import rzx.ui.ZxResourceFactory;
import rzx.ui.ZxUIHelper;

/**
 * Base Class for the dialogs. Always: Borderlayout: NORTH: Buttons to select
 * stuff, if needed CENTER: Data Area SOUTH: Buttons like close, cancel,..
 *
 * @author rene
 */
public abstract class BaseDialogUI extends JDialog implements ActionListener {

    protected ZxUIElementFactory m_uiElementFactory = ZxUIElementFactory.getInstance();
    protected ZxResourceFactory m_resourceFactory = ZxResourceFactory.getInstance();
    protected JButton mu_cancelButton = null;
    protected JButton mb_saveButton = null;
    protected Logger m_logger = Logger.getLogger(getClass().getName());

    public BaseDialogUI() {
        jInit();
    }

    private void jInit() {    // Private as called only from the subclass constructor
        setLayout(new BorderLayout());
        m_uiElementFactory.configure("generic", this);
        mu_cancelButton = m_uiElementFactory.createCancelButton();
        mb_saveButton = m_uiElementFactory.createSaveButton();
    }
    
    /**
     *  executes the finishing operations:
     *   packs and centers the dialog on the screen
     */
    protected void finish() {
//        setModalityType(Dialog.ModalityType.APPLICATION_MODAL);  // Removed Sep20,
                                         // um mehrere DIaloge bei den tools parallel oeffnen zu koennen
        ZxUIHelper.packAndCenter(this);
    }
    
    /**
     * set the teitle of a dialog using the entry in the resource file
     * the String ".title" is added to the key.
     * 
     * @param key without ".title"
     */
    protected void setTitleFromResource (String key) {
        setTitle (m_resourceFactory.getString(key+".title"));
    }
}
