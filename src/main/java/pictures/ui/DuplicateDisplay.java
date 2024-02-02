package pictures.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import picdata.PicDirectory;
import pictures.tools.DuplicateHunter;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxLogPanel;
import rzx.ui.ZxPanel;
import rzx.ui.ZxTable;

/**
 * Class to display duplicate pictures.
 *
 * Dialog has 3 Panels:
 *
 * NORTH: 2 Frames: NORTH: Some infos about the current folder SOUTH/CENTER:
 * Table with duplicates. Columns are: Filename, and series of CD-Codes, where
 * the Picture is also found. CENTER: A message frame SOUTH: Buttons
 *
 * @author rene
 */
public class DuplicateDisplay extends BaseDialogUI {

    private ZxPanel mu_northPanel = new ZxPanel();
    private ZxPanel mu_infoPanel = new ZxPanel();
    private ZxPanel mu_tablePanel = new ZxPanel();
    private ZxTable mu_duplicateTable = new ZxTable();
    private ZxLogPanel mu_logPanel = new ZxLogPanel();
    private ZxButtonPanel mu_buttonPanel = new ZxButtonPanel();
    private JButton mu_reduceButton = null;
    private JButton mu_increaseButton = null;
    private JButton mu_cancelButton = null;
    
    private DuplicateHunter m_hunter = null;

    public DuplicateDisplay() {
        jInit();
    }

    private void jInit() {

        mu_buttonPanel.configureResource("duplicates");
        mu_northPanel.setLayout(new BorderLayout());
        mu_northPanel.add(mu_infoPanel, BorderLayout.NORTH);
        add(mu_northPanel, BorderLayout.NORTH);
        mu_tablePanel.add(new JScrollPane(mu_duplicateTable));
        add(mu_tablePanel, BorderLayout.CENTER);
        mu_cancelButton = mu_buttonPanel.createAndAddCancelButton();
        mu_reduceButton = mu_buttonPanel.createAndAddButton("reduce");
        mu_increaseButton = mu_buttonPanel.createAndAddButton("increase");
        add(mu_buttonPanel, BorderLayout.SOUTH);
        finish();
    }

    public void start(PicDirectory directory) {
        m_logger.fine("Starting Duplicate Search");
        m_hunter = new DuplicateHunter(directory);
        mu_duplicateTable.setModel(m_hunter);
        invalidate();
        repaint();
        m_hunter.run();
        setVisible(true);
    }

    private void performIncrease() {
        m_hunter.increase();
    }

    private void performreduce() {
        m_hunter.reduce();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Object source = e.getSource();
        if (source == mu_cancelButton)
            dispose();
        else if (source == mu_increaseButton)
            performIncrease();
        else if (source == mu_reduceButton)
            performreduce();
    }

}
