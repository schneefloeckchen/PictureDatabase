package pictures.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import picdata.PictureMedium;
import pictures.tools.MediumLoadProcessor;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxErrorDialog;
import rzx.ui.ZxPanel;

/**
 * Dialog to define options for the load and scan function
 *
 * Planned Options:
 *
 * Monitor on logfile Check, if picture is already loaded Parallel processing
 * Wird nicht mehr benutzt, später löschen!!!
 *
 * @author rene
 */
public class MediumLoadOptionsDialog extends BaseDialogUI {

    private JCheckBox mu_logExecutionCheckBox = null;
    private JCheckBox mu_checkDuplicatesCheckBox = null;
    private JCheckBox mu_parallelProcessingCheckBox = null;

    private JButton mu_goButton = null;
//    private JButton mu_cancelButton = null;

    private File m_DVDRoot = null;
    private PictureMedium m_medium = null;
    private boolean m_logExecution = false;
    private boolean m_checkDuplicates = false;
    private boolean m_parallelProcessing = false;

//    public MediumLoadOptionsDialog(File DVDRoot, PictureMedium medium) {
//        m_DVDRoot = DVDRoot;
//        m_medium = medium;
//        jInit();
//    }
//
//    private void jInit() {
//        m_uiElementFactory.configure("mediaLoadOptions");
//        setTitleFromResource("mediaLoadOptions");
//        mu_logExecutionCheckBox = m_uiElementFactory.createCheckBox("logExecution");
//        mu_checkDuplicatesCheckBox = m_uiElementFactory.createCheckBox("checkDuplicates");
//        mu_parallelProcessingCheckBox = m_uiElementFactory.createCheckBox("parallelProcessing");
//        ZxPanel dialogPanel = new ZxPanel();
//        dialogPanel.setLayout(new BoxLayout(dialogPanel, BoxLayout.Y_AXIS));
//        dialogPanel.add(mu_logExecutionCheckBox);
//        dialogPanel.add(mu_checkDuplicatesCheckBox);
//        dialogPanel.add(mu_parallelProcessingCheckBox);
//        add(dialogPanel, BorderLayout.CENTER);
//        ZxButtonPanel buttonPanel = new ZxButtonPanel();
////        m_uiElementFactory.configure("mediaLoadOptions.button");
//        mu_goButton = buttonPanel.createAndAddButton("go");
//        mu_cancelButton = buttonPanel.createAndAddCancelButton();
//        buttonPanel.align();
//        add(buttonPanel, BorderLayout.NORTH);
//        finish();
//    }
//
//    private void start() {
//        m_logger.logDevelop("Starting Scan");
//        setVisible(false);
//        MediumLoadProcessor processor = new MediumLoadProcessor();
//        if (m_medium == null)
//            ZxErrorDialog.displaySimpleErrorMessage(null, "generic.error.noMedium");
//        else {
//            processor.setMedium(m_medium);    // Medium to create
//            // processor.process(m_DVDRoot);
//            processor.start(m_DVDRoot);
//            m_logger.logDevelop("Finished");
//        }
//        dispose();
//    }
//
    @Override
    public void actionPerformed(ActionEvent e) {
//        Object source = e.getSource();
//        if (source == mu_goButton)
//            start();
//        else if (source == mu_cancelButton)
//            dispose();
//        else
//            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
