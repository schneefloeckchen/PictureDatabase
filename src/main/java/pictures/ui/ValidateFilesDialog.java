package pictures.ui;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import picdata.DigiPicture;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.Searcher;
import pictures.ui.elements.ValidateFilesStorage;
import rzx.graphics.ExifHelper;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxCheckBox;
import rzx.ui.ZxLogPanel;
import rzx.ui.ZxMessageDialog;
import rzx.ui.ZxUIElementFactory;

/**
 * Validates if files or folder have already been imported into the database.
 *
 * UI structure:
 * NORTH: Buttons to select a file or folder
 * CENTER: Table with missing / found files
 * SOUTH: Report area
 *
 * @todo add table to display the results, similar to the compare
 * directory function.
 *
 * @author rene
 */
public class ValidateFilesDialog extends BaseDialogUI {

  private static final String RESOURCEFILE_ENTRY = "validateFiles";

  private final ZxUIElementFactory m_buttonFactory = ZxUIElementFactory.getInstance();
  private JButton mu_selectFileButton = null;
  private JButton mu_selectFolderButton = null;
  private ZxCheckBox mu_recursive = null;
  private JButton mu_clearDisplayButton = null;

  private int m_fileFoundCounter = 0;
  private int m_fileNotFoundCounter = 0;
  private boolean m_recursiveFlag = false;

  private JTable m_resultTable = new JTable();
  private final ZxLogPanel mu_messages = new ZxLogPanel();

  private Searcher m_searcher = null;
  private ExifHelper m_exifHelper = null;
  private ValidateFilesStorage m_resultStorage = null;
  /** true, if the class if run from the test process. UI is not used. */
  private boolean m_underTest = false;
  // 

  public ValidateFilesDialog() {
    jInit();
  }

  /**
   * constructor for the usage in the test process.
   * @param underTest must be true
   * @param recursiv true, if the process shall walk trough subfolder in
   * the file system
   */
  public ValidateFilesDialog(boolean underTest, boolean recursiv) {
    m_underTest = underTest;
    m_recursiveFlag = recursiv;
  }
  
  private void jInit() {
    setTitleFromResource(RESOURCEFILE_ENTRY);

    ZxButtonPanel buttonPanel = new ZxButtonPanel();
    m_buttonFactory.configure(RESOURCEFILE_ENTRY, this);
    buttonPanel.configureResource(RESOURCEFILE_ENTRY);
    mu_selectFileButton = buttonPanel.createAndAddButton("selectFile");
    mu_selectFolderButton = buttonPanel.createAndAddButton("selectFolder");
    mu_recursive = buttonPanel.createAndAddCheckBox("recursive");
    mu_clearDisplayButton = buttonPanel.createAndAddButton("clearDisplay");
    mu_cancelButton = buttonPanel.createAndAddCancelButton();
    buttonPanel.createAndAddButton("missing", this::performDisplayMissingFiles);
    buttonPanel.createAndAddButton("found", this::performDisplayFoundFiles);

    add(buttonPanel, BorderLayout.NORTH);
    add(new JScrollPane(m_resultTable), BorderLayout.CENTER);
    add(new JScrollPane(mu_messages), BorderLayout.SOUTH);

    finish();
  }

  /*
  The 2 plugs for the testprocess into the dialog
  */
  /**
   * Starts the validate process from the test process.
   * @param folder with the test files
   */
  public void testValidateFolder(String folder) {
    m_resultStorage = new ValidateFilesStorage();
    m_searcher = new Searcher();
    m_exifHelper = new ExifHelper();
    executeOnFolder(new File(folder));
  }
  
  /**
   * returns the result storage for further evaluation in the
   * test process
   * @return 
   */
  public ValidateFilesStorage getValidateFilesStorage() {
    return m_resultStorage;
  }
  /* End of test plugs */
  
  private void performValidateFiles() {
    m_searcher = new Searcher();
    JFileChooser fc = new JFileChooser("/mnt/speicher/DigiBild/");
    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    int answer = fc.showOpenDialog(this);
    if (answer == JFileChooser.APPROVE_OPTION) {
      File file = fc.getSelectedFile();
      writeMessage("File " + file.getPath() + " selected");
      DigiPicture picSearch = locatePicture(file);
      if (picSearch == null)
        writeMessage("Not in database");
      else
        writeMessage("Found in " + picSearch.getDirectories());
    }
  }

  private void performValidateFolder() {
    m_searcher = new Searcher();
    m_fileFoundCounter = 0;
    m_fileNotFoundCounter = 0;
    m_exifHelper = new ExifHelper();
    m_resultStorage = new ValidateFilesStorage();
    JFileChooser fc = new JFileChooser("/mnt/speicher/DigiBild/");
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int answer = fc.showOpenDialog(this);
    if (answer == JFileChooser.APPROVE_OPTION) {
      m_recursiveFlag = mu_recursive.isSelected();
      if (m_recursiveFlag)
        writeMessage("Walking through the sub-folder");
      File folder = fc.getSelectedFile();
      executeOnFolder(folder);
      writeMessage("Run finished:");
      writeMessage("Files found " + m_fileFoundCounter);
      writeMessage("Files not found " + m_fileNotFoundCounter);
      m_resultTable.setModel(m_resultStorage.getMissingFilesTableModel());
    }
  }

  private void performDisplayMissingFiles(Object dummy) {
    m_resultTable.setModel(m_resultStorage.getMissingFilesTableModel());
  }

  private void performDisplayFoundFiles(Object dummy) {
    m_resultTable.setModel(m_resultStorage.getFoundFilesTableModel());
  }

  private void executeOnFolder(File folder) {
    writeMessage("Working on " + folder.getAbsolutePath());
    File files[] = folder.listFiles();
    for (File file : files)
      if (file.isDirectory() && m_recursiveFlag)
        executeOnFolder(file);
      else {
        DigiPicture pic = locatePicture(file);
        if (pic == null) {
          m_fileNotFoundCounter++;
          m_resultStorage.addMissingEntry(
              file.getName(), folder.getAbsolutePath(), -1);
//                    mu_messages.write("File " + file.getName() + " not found in database\n");
        } else
          try {
          m_exifHelper.load(file);
          long fileMillis = m_exifHelper.getPictureTakenDateSeconds();
          if (fileMillis != pic.getPictureTakenMilis())
            m_resultStorage.
                addMissingEntry(file.getName(), folder.getAbsolutePath(),
                    pic.getId());
          else {
            Set<PicDirectory> dirs = pic.getDirectories();
            PicDirectory dir = null;    // need only one (the first, if multiple)
            if (dirs != null)
              dir = dirs.iterator().next();
            if (dir != null) {
              PictureMedium medium = dir.getMedium();
              m_resultStorage.addFoundEntry(
                  file.getName(), folder.getPath(),
                  pic.getId(), medium.getCode(),
                  medium.getLabel(), dir.getDirectoryName());
            } else
              m_resultStorage.addFoundEntry(
                  file.getName(), folder.getPath(),
                  pic.getId(), -1,
                  "No medium f. image in DB", "Database inconsistent");
          }
          m_fileFoundCounter++;
        } catch (JpegProcessingException
            | PngProcessingException
            | TiffProcessingException | IOException ex) {
          m_resultStorage.addMissingEntry(file.getName(),
              folder.getAbsolutePath(), -2);
        }
      }

  }

  private void performClearLogArea() {
    mu_messages.clear();
  }

  private DigiPicture locatePicture(File file) {
    try {
      DigiPicture pic = new DigiPicture();
      pic.preLoad(file);
      String name = file.getName();
      long milis = pic.getPictureTakenMilis();
      DigiPicture picSearch = m_searcher.searchPictureByNameAndMilis(name, milis);
      if (picSearch == null) {
        mu_messages.write("Picture/Video not found: " + name + "  Milis: " + milis);
        List<DigiPicture> list = m_searcher.seachPicturesByName(name);
        if (list == null || list.isEmpty())
          mu_messages.write("No entries w. this name");   // @todo move to resourec
        else {
          mu_messages.write("Entries found w. this name");   // @todo move to resourec
          for (DigiPicture p : list) {
            long delta = p.getPictureTakenMilis() - pic.getPictureTakenMilis();
            mu_messages.write(p.getFileName() + " milis: " + p.getPictureTakenMilis() + "   -- delta is " + delta);
            Set<PicDirectory> dirs = p.getDirectories();
            mu_messages.write("On Media and Folder:");
            for (PicDirectory dir : dirs)
              mu_messages.write(dir.getDirectoryName() + " (" + dir.getMedium() + ")");
          }
        }
        mu_messages.write("");
      }
      return picSearch;
    } catch (IOException
        | JpegProcessingException
        | TiffProcessingException
        | PngProcessingException ex) {
      ZxMessageDialog.displayExceptionMessage(this,
          "pictureMaintain.exception.cannotload", ex);
    }
    return null;
  }
  
  private void writeMessage(String message) {
    if (!m_underTest) mu_messages.write(message);   // Write adds /n
  }
  
  // implementation of the ActionListerner Interface defined in BaseDialogUI
  @Override
  public void actionPerformed(ActionEvent e) {
    Object obj = e.getSource();
    if (obj == mu_selectFileButton)
      performValidateFiles();
    else if (obj == mu_selectFolderButton)
      performValidateFolder();
    else if (obj == mu_clearDisplayButton)
      performClearLogArea();
    else if (obj == mu_cancelButton)
      dispose();
  }

}
