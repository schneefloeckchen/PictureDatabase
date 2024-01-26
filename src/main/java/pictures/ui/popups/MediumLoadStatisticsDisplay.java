package pictures.ui.popups;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import picdata.PictureMedium;
import pictures.tools.MediumLoadProcessor;
import pictures.tools.StatisticCollector;
import pictures.ui.BaseDialogUI;
import rzx.ui.ZxBaseTableModel;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxGenericTableDialog;
import rzx.ui.ZxTable;
import rzx.ui.ZxTextArea;

/**
 *
 * Displays statistics like duration and number of processed pictures while
 * importing a storage Medium
 *
 * BaseDialog uses BorderLayout.
 *
 * NORTH is the table with statistics <br>
 * CENTER a log panel <br>
 * SOUTH buttons
 *
 *
 * @author rene
 */
public class MediumLoadStatisticsDisplay extends BaseDialogUI {

    private ZxTable mt_statisticsTable = new ZxTable();
    private ZxTextArea mu_logArea = new ZxTextArea();
    private JButton mb_startRunButton = null;   // Start import
    private JButton mb_cancelButton = null;
    private JButton mb_suspendButton = null;
    private JButton mb_resumeButton = null;
    private JButton mb_listDuplicates = null;
    private JButton mb_listErrorButton = null;
    private JButton mb_closeButton = null;
    private ArrayList<String> m_duplicates = null;
    private ArrayList<String> m_errors = null;

    private File m_DVDRoot = null;
    private PictureMedium m_medium = null;
    MediumLoadProcessor m_processor = null;

    public MediumLoadStatisticsDisplay() {
        jInit();
    }

    public void setMediumAndRoot(PictureMedium medium, File root) {
        m_DVDRoot = root;
        m_medium = medium;
    }

    private void jInit() {
        setTitleFromResource("mediaLoadStatistics");
        mt_statisticsTable.setModel(StatisticCollector.getInstance());
        add(new JScrollPane(mt_statisticsTable), BorderLayout.NORTH);
        ZxButtonPanel buttonPanel = new ZxButtonPanel();
        buttonPanel.configureResource("mediaLoadStatistics");
        mb_startRunButton = buttonPanel.createAndAddButton("start");
        mb_listDuplicates = buttonPanel.createAndAddButton("showDuplicates");
        mb_listErrorButton = buttonPanel.createAndAddButton("importError");
        mb_cancelButton = buttonPanel.createAndAddCancelButton();
        mb_suspendButton = buttonPanel.createAndAddButton("suspend");
        mb_resumeButton = buttonPanel.createAndAddButton("resume");
        mb_closeButton = buttonPanel.createAndAddButton("close");
        add(buttonPanel, BorderLayout.SOUTH);
        finish();
//        setVisible(true);
    }

    private void performStartRun() {
        m_logger.fine("Starting Run");
        m_processor = new MediumLoadProcessor();
        m_processor.setMedium(m_medium);
        m_processor.start(m_DVDRoot);
    }

    private void performCancelRun() {
        m_logger.fine("Canceling this run");
        m_processor.stop();
    }

    private void performSuspendRun() {
        m_logger.fine("Suspending this run");
        m_processor.suspendThreadExecution();
    }

    private void performResumeRun() {
        m_logger.fine("Resuming this run");
        m_processor.resumeThreadExecution();
    }
    
    private void performListDuplicates() {
        m_logger.fine("Listing duplicates");
        m_duplicates = StatisticCollector.getInstance().getDuplicatePictureList();
        ZxGenericTableDialog dialog = new ZxGenericTableDialog();
        dialog.setModel(duplicatesModel);
        dialog.setModalityType(DEFAULT_MODALITY_TYPE);
        dialog.setVisible(true);
    }
    
    private void performListErrors() {
        m_logger.fine("Displaying errors");
        ZxGenericTableDialog dialog = new ZxGenericTableDialog();
        dialog.setModel(StatisticCollector.getInstance().getErrorTabelModel());
        dialog.setModalityType(DEFAULT_MODALITY_TYPE);
        dialog.setVisible(true);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == mb_startRunButton)
            performStartRun();
        else if (src == mb_cancelButton)
            performCancelRun();
        else if (src == mb_suspendButton)
            performSuspendRun();
        else if (src == mb_resumeButton)
            performResumeRun();
        else if (src == mb_listDuplicates)
            performListDuplicates();
        else if (src == mb_listErrorButton)
            performListErrors();
        else if (src == mb_closeButton)
            dispose();
        else
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    ZxBaseTableModel duplicatesModel = new ZxBaseTableModel() {
        @Override
        public int getRowCount() {
            return m_duplicates.size();
        }

        @Override
        public int getColumnCount() {
            return 1;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return "";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return m_duplicates.get(rowIndex);
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    };

    
}
