package pictures.ui.tools;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import pictures.tools.CompareFolderProcessor;
import rzx.ui.ZxDropTargetTextField;
import rzx.ui.ZxGridBagPanel;
import rzx.ui.ZxMessageDialog;
import rzx.ui.ZxResourceFactory;
import rzx.ui.ZxTable;
import rzx.ui.ZxTextField;
import rzx.ui.ZxUIElementFactory;
import rzx.ui.ZxUIHelper;

/**
 * UI for the compare folder tool. Structure of UI: BorderLayout.
 * NORTH: 2 panels, left the option to locate the folder which content has
 * to be investigated. So, JfileChooser and button. Investigate Panel
 * On the right side the option to select a 2nd folder, where we will
 * search for these files. Storage Panel.
 * CENTER: A table showing the data of these images:
 * Col 1: File Name
 * Col 2: image taken date as date
 * Col 3: image taken date as int
 * Col 4: Folder where the image is stored on the file system
 *
 * @author rene
 * 2.10.22 initial created
 *
 */
public class CompareFolderUI extends JDialog implements ActionListener {

  private static final String DIALOG_KEY = "compareFolder";
  private ZxResourceFactory m_resourceFactory = ZxResourceFactory.getInstance();
  private ZxTextField mt_investigateFolderTextField = null;
  private JButton mb_investigateFolderLocateButton = null;
  private JButton mb_investigateFolderLoadButton = null;
  private JButton mb_displayMissingFilesButton = null;
  private JButton mb_displayFoundFilesButton = null;
  private ZxTextField mt_storageFolderTextField = null;
  private JButton mb_storageFolderLocateButton = null;
  private JButton mb_storageFolderLoadButton = null;

  private ZxDropTargetTextField mt_investigateDropTextField
          = new ZxDropTargetTextField(20);

  private ZxTable m_missingFilesTable = new ZxTable();

  private CompareFolderProcessor m_processor = null;
  private int m_processNumber = 0; // for test purpose, result from latest
  // load or investigate run

  private static final Logger m_logger
          = Logger.getLogger("pictures.ui.tools.CompareFolderUI");
  private List<JButton> m_buttonList = null;
  private List<JTextField> m_textFieldList = null;
  private boolean m_testModus = false;

  public CompareFolderUI() {
    jInit();
  }

  private void jInit() {
    setTitle(m_resourceFactory.getString(DIALOG_KEY + ".title"));
    setLayout(new BorderLayout());
    JPanel northPanel = new JPanel();
    northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.X_AXIS));
    ZxUIElementFactory.getInstance().setActionListener(this);

// Build left panel , investigate panel. Files to check.   
    ZxGridBagPanel leftPanel = new ZxGridBagPanel(DIALOG_KEY + ".left", this);
    mt_investigateFolderTextField
            = leftPanel.createAndAddLabelAndField("investigate", 20);
    mb_investigateFolderLocateButton = leftPanel.
            createAndAddButton("locateSource");
    mb_investigateFolderLoadButton = leftPanel.createAndAddButton("loadSource");
    mb_displayMissingFilesButton = leftPanel.createAndAddButton("missingFiles");
    mb_displayFoundFilesButton = leftPanel.createAndAddButton("foundFiles");
//    leftPanel.add(mt_investigateDropTextField); 
// @todo finish drag and drop for folder compare

// Build right panel, storage are where the already saved files are stored 
    ZxGridBagPanel rightPanel = new ZxGridBagPanel(DIALOG_KEY + ".right", this);
    mt_storageFolderTextField
            = rightPanel.createAndAddLabelAndField("storage", 20);
    mb_storageFolderLocateButton = rightPanel.createAndAddButton("locateStorage");
    mb_storageFolderLoadButton = rightPanel.createAndAddButton("loadStorage");

    northPanel.add(leftPanel);
    northPanel.add(rightPanel);
    add(northPanel, BorderLayout.NORTH);

    add(new JScrollPane(m_missingFilesTable), BorderLayout.CENTER);

    this.doLayout();
    ZxUIHelper.packAndCenter(this);
    setModal(true);

  }

  private void performLocateInvestigateFolder() {
    selectDirectory(mt_investigateFolderTextField);
  }

  private void performLocateStorageFolder() {
    selectDirectory(mt_storageFolderTextField);
  }

  private void performProcessStorageFolder() {
    m_logger.info("Starting storage folder read");
    m_processor = new CompareFolderProcessor();
    int number = m_processor.load(new File(mt_storageFolderTextField.getText()));
    if (m_testModus) {
      System.out.println("Elements found " + number);
    } else {
      ZxMessageDialog.displayMessage(this, "compareFolder.load.finalMessage", number);
    }
    m_processNumber = number;
    m_logger.log(Level.INFO, "{0} Files processed", number);
    m_missingFilesTable.setModel(m_processor.getDuplicateTableModel());
//    m_processor.dumpStorage();
  }

  private void performProcessInvestigateFolder() {
    m_logger.info("Starting checking Files");
    String investigateFolderName = mt_investigateFolderTextField.getText();
    m_processor.checkFiles(new File(investigateFolderName));
    displayMissingFiles();
    int missingFiles = m_processor.getMissingFilesTableModel().getRowCount();
    if (m_testModus) {
      System.out.println("Number missing files= " + missingFiles);
    } else {
      ZxMessageDialog.displayMessage(this, "compareFolder.process.finalMessage", missingFiles);
    }
  }

  private void displayMissingFiles() {
    m_missingFilesTable.setModel(
            m_processor.getMissingFilesTableModel());
  }

  private void displayFoundFiles() {
    m_missingFilesTable.setModel(
            m_processor.getFoundFilesTableModel());
  }

  private void selectDirectory(ZxTextField textField) {
    JFileChooser fc = new JFileChooser();
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    fc.showOpenDialog(this);
    File folder = fc.getSelectedFile();
    if (folder != null) {
      textField.setText(folder.getAbsolutePath());
    }
  }

  // Methods needed for junit testing
  /**
   * returns a list with references to the buttons in the UI
   * (0) -> Mb_storageFolderLoadButton : Referenz Verzeichnis Laden
   * (1) -> mb_investigateFolderLoadButton : Folder where we want to check, if
   * the files are in the reference folder
   *
   * @return
   */
  public List<JButton> getButtons() {
    if (m_buttonList == null) {
      m_buttonList = new ArrayList<>();
      m_buttonList.add(mb_storageFolderLoadButton);
      m_buttonList.add(mb_investigateFolderLoadButton);
    }
    return m_buttonList;
  }

  /**
   * (0) mt_storageFolderTextField, field to define the reference storage
   * location
   * (1) mt_investigateFolderTextField, where to check if the files are in the
   * first folder
   *
   * @return a list of relevant TextFields for use in junit testing
   */
  public List<JTextField> getTextFields() {
    if (m_textFieldList == null) {
      m_textFieldList = new ArrayList<>();
      m_textFieldList.add(mt_storageFolderTextField);
      m_textFieldList.add(mt_investigateFolderTextField);
    }
    return m_textFieldList;
  }

  /**
   * set the class into test modus, which prevent any popup dialog
   */
  public void setToTest() {
    m_testModus = true;
  }

  /**
   * returns the result number from the latest process run (load or investigate)
   *
   * @return
   */
  public int getProcessNumber() {
    return m_processNumber;
  }

  /**
   * for testing only, returns number of missing files after the test
   *
   * @return
   */
  public int getMissingFilesCount() {
    return m_processor.getMissingFilesTableModel().getRowCount();
  }

  /**
   * for testing only, returns number of found files after the test
   *
   * @return
   */
  public int getFoundFilesCount() {
    return m_processor.getFoundFilesTableModel().getRowCount();
  }

  // Interface Implementation
  @Override
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == mb_investigateFolderLocateButton) {
      performLocateInvestigateFolder();
    } else if (src == mb_storageFolderLocateButton) {
      performLocateStorageFolder();
    } else if (src == mb_storageFolderLoadButton) {
      performProcessStorageFolder();
    } else if (src == mb_investigateFolderLoadButton) {
      performProcessInvestigateFolder();
    } else if (src == mb_displayMissingFilesButton) {
      displayMissingFiles();
    } else if (src == mb_displayFoundFilesButton) {
      displayFoundFiles();
    } else {
      throw new UnsupportedOperationException("Not supported yett."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }
  }

}
