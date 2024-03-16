package pictures.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.Searcher;
import pictures.tools.DataBaseChecker;
import pictures.tools.DirectoryRemoveProcessor;
import pictures.tools.DisplayPictureDataProcessor;
import pictures.tools.DisplayRecord;
import pictures.tools.ExifDataReader;
import pictures.tools.LoadPictureTester;
import pictures.tools.MediumDeleteProcessor;
import pictures.ui.tools.CompareFolderUI;
import pictures.ui.tools.CreatePresentationPicturesUI;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxComboBox;
import rzx.ui.ZxComboBoxEntry;
import rzx.ui.ZxComboBoxModel;
import rzx.ui.ZxLogPanel;
import rzx.ui.ZxMessageDialog;
import rzx.ui.ZxPanel;
import rzx.ui.ZxTextField;

/**
 * Layout erst mal: NORTH: Button Panel, start tool CENTER: Je nach tool
 * erstellt. SOUTH: Log Panel
 *
 * @author rene
 *
 * 2.10.22: Compare Folder added. STarted with mu_compareFolderButton.
 * Function will search all files in a folder (typically on a sd card) and locate
 * them in multiple steps from the file storage. Purpose is to check, if all
 * pictures from a sd card are already stored on a disk.
 *
 */
public class ToolsUI extends BaseDialogUI {

  private final ZxLogPanel mu_logPanel = new ZxLogPanel();
  private final ZxButtonPanel mu_buttonPanel = new ZxButtonPanel();
  private final ZxButtonPanel mu_row1ButtonPanel = new ZxButtonPanel();
  private final ZxButtonPanel mu_row2ButtonPanel = new ZxButtonPanel();
  private final ZxButtonPanel mu_row3ButtonPanel = new ZxButtonPanel();
  // Buttons on the button Frame, starting action
  private JButton mu_loadPictureButton = null;
  private JButton mu_testExifHelperButton = null;
  private JButton mu_readEXIFDataButton = null;
  private JButton mu_removeStorageMediaButton = null;
  private JButton mu_removeDirectoryButton = null;
  private JButton mu_checkDatabaseButton = null;
  private JButton mu_displayDatabaseRecordButton = null;
  private JButton mu_displayPictureDataButton = null;
  private JButton mu_createPresentationPicturesButton = null;
  private JButton mu_compareFolderButton = null;
  private JButton mu_testButton = null;

  // Elements for remove storage media action
  private JButton mu_removeStorageMediaGoButton = null;
  private JButton mu_removeStorageMediaDeleteButton = null;
  private ZxTextField mu_mediumRemoveCodeTextField = null;      // Also used for remove folder
  // Elements for removing Directories action
  private JButton mu_loadDirectoriesToRemoveButton = null;
  private final ZxComboBox mu_folderComboBox = new ZxComboBox();
  private JButton mu_removeDirectoryGoButton = null;
  private JButton mu_removeDirectoryDeleteButton = null;
  // Elements for displayDatabaseRecord
  private ZxComboBox mu_selectTableCombobox = null;
  private ZxTextField mu_IDField = null;      // used also in further dislogs
  private JButton mu_displayDatabaseRecordButtonGo = null;
  // Elements for display picture information, Picture loaded by ID
  private JButton mu_displayPictureDataGoButton = null;
  private JRadioButton mu_displayPictureDataUsingSQL = new JRadioButton("By SQL");
  private JRadioButton mu_displayPictureDataUsingHibernate = new JRadioButton("By Hibernate");
  private ButtonGroup mu_displayPictureDataMethodSelection = new ButtonGroup();

  private MediumDeleteProcessor m_deleteProcessor = null;
  private DirectoryRemoveProcessor m_directoryRemoveProcessor = null;

  public ToolsUI() {
    jInit();
  }

  private void jInit() {
    setTitleFromResource("toolsUI");
    m_uiElementFactory.configure("toolsUI");
    mu_loadPictureButton = mu_row1ButtonPanel.createAndAddButton("loadPicture");
    mu_testExifHelperButton = mu_row1ButtonPanel.createAndAddButton("testExifHelper");
    mu_readEXIFDataButton = mu_row1ButtonPanel.createAndAddButton("readEXIFData");
    mu_displayPictureDataButton = mu_row1ButtonPanel.createAndAddButton("displayPictureData");
    mu_createPresentationPicturesButton = mu_row1ButtonPanel.createAndAddButton("createPresentationPictures");
    mu_removeStorageMediaButton = mu_row2ButtonPanel.createAndAddButton("removeStorageMedium");
    mu_removeDirectoryButton = mu_row2ButtonPanel.createAndAddButton("removeDirectory");
    mu_checkDatabaseButton = mu_row2ButtonPanel.createAndAddButton("checkDatabase");
    mu_displayDatabaseRecordButton = mu_row2ButtonPanel.createAndAddButton("displayRecord");
    mu_row2ButtonPanel.add(mu_cancelButton);
    
    mu_compareFolderButton = mu_row3ButtonPanel.createAndAddButton ("compareFolder");
    mu_testButton = mu_row3ButtonPanel.createAndAddButton("testButton");
    BoxLayout buttonLayout = new BoxLayout(mu_buttonPanel, BoxLayout.Y_AXIS);
//    mu_buttonPanel.setLayout(new FlowLayout());
    mu_buttonPanel.setLayout(buttonLayout);
    mu_buttonPanel.add(mu_row1ButtonPanel);
    mu_buttonPanel.add(mu_row2ButtonPanel);
    mu_buttonPanel.add(mu_row3ButtonPanel);
    add(mu_buttonPanel, BorderLayout.NORTH);
    add(new JScrollPane(mu_logPanel), BorderLayout.SOUTH);
    mu_logPanel.clear();
    finish();
    mu_logPanel.write("Started");
    String javaVersion = System.getProperty("java.vm.name") + "  ( "
        + System.getProperty("java.vm.version") + " / "
        + System.getProperty("java.vm.info") + " ) Version: "
        + System.getProperty("java.version");
    mu_logPanel.write(javaVersion);
    String hibernateVersion = org.hibernate.Version.getVersionString();
    mu_logPanel.write("Hibernate Version is: " + hibernateVersion);

  }

  private void performLoadPicture() {
    m_logger.fine("Starting load picture");
    File file = selectFile();
    if (file == null) {
    } else {
      LoadPictureTester tester = new LoadPictureTester(this, mu_logPanel);
      tester.loadFile(file);
    }
  }

  private void performTestExifHelper() {
    mu_logPanel.write("Testing EXIF HELPER");
    File file = selectFile();
    if (file != null) {
      LoadPictureTester tester = new LoadPictureTester(this, mu_logPanel);
      tester.listExifData(file);
    }
 }

  private void performReadExifData() {
    mu_logPanel.write("Reading EXIF Data");
    File file = selectFile();
    if (file != null) {
      ExifDataReader reader = new ExifDataReader(this, mu_logPanel);
      reader.readExifData(file);
    }
 }

  private void performRemoveStorageMedia() {
    mu_logPanel.write("Remove Storage Medium");
    ZxPanel removeStoragePanel = createRemovePanel();
    mu_removeStorageMediaGoButton = m_uiElementFactory.createButton("removeStorageMediumGo");
    mu_removeStorageMediaDeleteButton = m_uiElementFactory.createButton("removeStorageMediumDelete");
    mu_removeStorageMediaDeleteButton.setEnabled(false);
    removeStoragePanel.add(mu_removeStorageMediaGoButton);
    removeStoragePanel.add(mu_removeStorageMediaDeleteButton);
    add(removeStoragePanel, BorderLayout.CENTER);
    finish();
  }

  private void performRemoveStorageMediaGo() {
    m_deleteProcessor = new MediumDeleteProcessor();
    m_deleteProcessor.load(mu_mediumRemoveCodeTextField.getInt(), mu_logPanel);
    mu_removeStorageMediaDeleteButton.setEnabled(true);
  }

  private void performRemoveStorageMediaDelete() {
    if (m_deleteProcessor == null)
      mu_logPanel.write("Prepare run not done");
    else {
      m_deleteProcessor.delete();
      mu_removeStorageMediaDeleteButton.setEnabled(false);
    }
  }

  private void performRemoveDirectory() {
    mu_logPanel.write("Removing folder");
    ZxPanel removeDirectoryPanel = createRemovePanel();
    mu_loadDirectoriesToRemoveButton = m_uiElementFactory.createButton("loadDirectories");
    removeDirectoryPanel.add(mu_loadDirectoriesToRemoveButton);
    removeDirectoryPanel.add(mu_folderComboBox);
    mu_removeDirectoryGoButton = m_uiElementFactory.createButton("removeDirectoryGo");
    mu_removeDirectoryDeleteButton = m_uiElementFactory.createButton("removeDirectoryDelete");
    removeDirectoryPanel.add(mu_removeDirectoryGoButton);
    mu_removeDirectoryGoButton.setEnabled(false);
    removeDirectoryPanel.add(mu_removeDirectoryDeleteButton);
    mu_removeDirectoryDeleteButton.setEnabled(false);
    add(removeDirectoryPanel, BorderLayout.CENTER);
    finish();
  }

  private void performLoadFolderCombobox() {
    int code = mu_mediumRemoveCodeTextField.getInt();
    Searcher searcher = new Searcher();
    PictureMedium medium = searcher.searchPictureMediumByCode(code);
    if (medium == null)
      mu_logPanel.write("Cannot find medium with code " + code);
    else {
      Set<PicDirectory> directories = medium.getPicDirectories();
      List<ZxComboBoxEntry> dirs = new ArrayList();
      for (PicDirectory dir : directories)
        dirs.add(new ZxComboBoxEntry(dir.getId().intValue(), dir.getDirectoryName()));
      mu_logPanel.write("Loaded " + dirs.size() + " / " + directories.size());
      ZxComboBoxModel<ZxComboBoxEntry> model = new ZxComboBoxModel(dirs);
      mu_folderComboBox.setModel(model);
      mu_removeDirectoryGoButton.setEnabled(true);
    }
  }

  private void performRemoveDirectoryGo() {
    m_directoryRemoveProcessor = new DirectoryRemoveProcessor();
    m_directoryRemoveProcessor.load((long) mu_folderComboBox.getSelectedKey(), mu_logPanel);
    mu_removeDirectoryDeleteButton.setEnabled(true);
  }

  private void performRemoveDirectoryDelete() {
    m_directoryRemoveProcessor.delete();
    mu_removeDirectoryDeleteButton.setEnabled(false);
    mu_removeDirectoryGoButton.setEnabled(false);
  }

  /**
   * Entry point for the tool, which performs a downscale process to
   * create smaller pictures for e.g. presentation, digital frames,
   * HD TV display and the like.
   */
  private void performCreatePresentationPictures() {
    CreatePresentationPicturesUI dialog = new CreatePresentationPicturesUI();
    dialog.setVisible(true);

  }

  private ZxPanel createRemovePanel() {
    ZxPanel panel = new ZxPanel();
    panel.configureResource("toolsUI");
    mu_mediumRemoveCodeTextField = panel.createAndAddLabelAndField("mediumCode");
    mu_mediumRemoveCodeTextField.setColumns(10);
//        mu_displayDatabaseRecordButtonGo = panel.createAndAddButton("")
    return panel;
  }

  private File selectFile() {
    JFileChooser chooser = new JFileChooser(
        "/home/rene/technik/JavaDevNB124/PicturesDatabase/testData");
    chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
    chooser.setVisible(true);
    if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
      mu_logPanel.write("File selected");
      return chooser.getSelectedFile();
    } else {
      mu_logPanel.write("Aborted");
      return null;
    }
  }

  // Start methode fo database check 
  private void performCheckDatabase() {
    m_logger.fine("Start checking database structure");
    DataBaseChecker checker = new DataBaseChecker();
    checker.start(mu_logPanel);
  }

  //
  // Methods for the display database record functionality.
  //
  /**
   * Startmethode zur darstellung eines Database Rcords. Display in a popup,
   * not modal window, so that multiple records can be displayed
   * simultaneously. This method just build the sub-ui to select table and
   * enter ID.
   */
  private void performDisplayDatabaseRecord() {
    mu_logPanel.write("Starte perform display database record");
    // adding elements to tools ui
    ZxPanel displayDatabaseRecordSelectionPanel = new ZxPanel();
    displayDatabaseRecordSelectionPanel.configureResource("toolsUI");
    mu_selectTableCombobox = displayDatabaseRecordSelectionPanel.addLabelAndComboBox("selecttable", DisplayRecord.getSelectTableComboBox());
    mu_IDField = displayDatabaseRecordSelectionPanel.createAndAddLabelAndField("idtoload");
    mu_displayDatabaseRecordButtonGo = displayDatabaseRecordSelectionPanel.createAndAddButton("displayRecordGo");
    add(displayDatabaseRecordSelectionPanel, BorderLayout.CENTER);
    finish();
  }

  /**
   * load and display the selected record
   */
  private void performDisplayDatabaseRecordGo() {
    m_logger.fine("Loading database record");
    DisplayRecord record = new DisplayRecord();
    record.load(mu_selectTableCombobox.getSelectedKey(),
        mu_IDField.getInt());
  }

  /**
   * Start Method to display picture data, showing: Folder, Media, plus Data
   * from the DIGI_PICTURE record. This start module creates just the required
   * buttons and input field
   */
  private void performDisplayPictureData() {
    m_logger.fine("Display Picture Data");
    ZxPanel panel = new ZxPanel();
    panel.configureResource("toolsUI");
    mu_displayPictureDataMethodSelection.add(mu_displayPictureDataUsingSQL);
    mu_displayPictureDataMethodSelection.add(mu_displayPictureDataUsingHibernate);
    panel.add(mu_displayPictureDataUsingSQL);
    mu_displayPictureDataUsingSQL.setSelected(true);
    panel.add(mu_displayPictureDataUsingHibernate);
    mu_IDField = panel.createAndAddLabelAndField("displayPictureDataID");
    mu_displayPictureDataGoButton = panel.createAndAddButton("displayPictureDataGo");
    add(panel, BorderLayout.CENTER);
    finish();
  }

  /**
   * method is actually displaying the selected data
   */
  private void performDisplayPictureDataGo() {
    int id = mu_IDField.getInt();
    DisplayPictureDataProcessor processor = new DisplayPictureDataProcessor(mu_logPanel);
    if (mu_displayPictureDataUsingSQL.isSelected())
      processor.loadBySQL(id);
    else if (mu_displayPictureDataUsingHibernate.isSelected())
      processor.loadByHibernate(id);
  }
  
  private void performCompareFolder() {
    CompareFolderUI dialog = new CompareFolderUI();
    dialog.setVisible(true);
  }

  @Override
  public void actionPerformed(ActionEvent ae) {
    m_logger.finer("im action handler");
    Object src = ae.getSource();
    if (src == mu_loadPictureButton)
      performLoadPicture();
    else if (src == mu_testExifHelperButton)
      performTestExifHelper();
    else if (src == mu_readEXIFDataButton)
      performReadExifData();
    else if (src == mu_removeStorageMediaButton)
      performRemoveStorageMedia();
    else if (src == mu_cancelButton)
      dispose();
    else if (src == mu_removeStorageMediaButton)
      performRemoveStorageMedia();
    else if (src == mu_removeStorageMediaGoButton)
      performRemoveStorageMediaGo();
    else if (src == mu_removeStorageMediaDeleteButton)
      performRemoveStorageMediaDelete();
    else if (src == mu_removeDirectoryButton)
      performRemoveDirectory();
    else if (src == mu_loadDirectoriesToRemoveButton)
      performLoadFolderCombobox();
    else if (src == mu_removeDirectoryGoButton)
      performRemoveDirectoryGo();
    else if (src == mu_removeDirectoryDeleteButton)
      performRemoveDirectoryDelete();
    else if (src == mu_checkDatabaseButton)
      performCheckDatabase();
    else if (src == mu_displayDatabaseRecordButton)
      performDisplayDatabaseRecord();
    else if (src == mu_displayDatabaseRecordButtonGo)
      performDisplayDatabaseRecordGo();
    else if (src == mu_displayPictureDataButton)
      performDisplayPictureData();
    else if (src == mu_displayPictureDataGoButton)
      performDisplayPictureDataGo();
    else if (src == mu_createPresentationPicturesButton)
      performCreatePresentationPictures();
    else if (src == mu_compareFolderButton)
      performCompareFolder();
    else if (src == mu_testButton) {
      // zur Zeit exception dialog fangen
      ZxMessageDialog.displayExceptionMessage(this, "test.exception", new SQLException("Test Error in Database"));
    }
    else
      mu_logPanel.write("Not yet implemented");
  }

}
