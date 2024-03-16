package pictures.ui.tools;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SpringLayout;
import rzx.ui.ZxGridBagPanel;
import rzx.ui.ZxResourceFactory;
import rzx.ui.ZxTextField;
import rzx.ui.ZxUIElementFactory;
import rzx.ui.ZxUIHelper;

import static javax.swing.JFileChooser.APPROVE_OPTION;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import javax.swing.JOptionPane;
import javax.swing.border.Border;
import picdata.PictureResolutions;
import pictures.tools.PresentationPicturesProzessor;
import rzx.ui.ZxMessageDialog;

/**
 * Tool to create pictures with a lower resolution from pictures stored
 * in the file system.
 *
 * UI is a popup dialog, 2 columns and a message box below.
 * left columns for the input definition, right for the output settings.
 * The input folder has a file sequence.inp, where the sequence of the
 * files to process is defined. On the output column the target folder, the
 * target filenames and the target resolution.
 *
 * DIalog from scratch, without using BaseDialogUI. Test a new layout.
 *
 * @author rene
 */
public class CreatePresentationPicturesUI extends JDialog implements ActionListener {

  private static final String DIALOG_KEY = "createPresentationPictures";
  private static final int X_GAP = 6;      // Horizontal gap between elements
  private static final int Y_GAP = 5;      // Vertical gap between elements

  // defines the sequence of the files to process
  private static final String SEQUEMCE_FILE_NAME = "sequence.txt";

  private ZxResourceFactory m_resourceFactory = ZxResourceFactory.getInstance();
  private ZxUIElementFactory m_uiElementFactory = ZxUIElementFactory.getInstance();

  private ZxTextField mu_inputFileTextField = new ZxTextField(25);
  private JRadioButton mu_sortByFileNameRadioButton = null;
  private JRadioButton mu_sortByFileDateRadioButton = null;
  private JButton mu_createSequenceFileButton = null;
  private JButton mu_selectFolderButton = null;

  // Filenames of the created images use the following structure:
  //  <head><counter>.<extension>
  private ZxTextField mu_fileNameHeadTextField = null;
  private ZxTextField mu_fileNameExtensionTextField = null;
  private ZxTextField mu_fileNameCounterStartTextField = null;  // Start value f.the counter part
  private ZxTextField mu_fileNameCounterLengthTextField = null; // Number of digits for the counter
  /**
   * to be added to all created images as description
   */
  private ZxTextField mu_descriptionTextField = null;
  private ZxTextField mu_exportFolderTextField = null;
  private JComboBox mu_resolutionComboBox = null;
  private ZxTextField mu_resolutionXTextField = null;
  private ZxTextField mu_resolutionYTextField = null;
  private JButton mu_selectExportFolderButton = null;
  private JButton mu_startCreatePresentationPicturesButton = null;
  private JButton mu_closeButton = null;

  private PresentationPicturesProzessor m_controller
      = new PresentationPicturesProzessor(this);

  private String m_inputFolder = null;
  private String m_exportFolder = null;

  private Logger m_logger = Logger.getLogger(getClass().getName());

  public CreatePresentationPicturesUI() {
    jInit();
  }

  private void jInit() {
    m_logger.log(Level.INFO, "Building User Interface for {0}", getClass().getName());
    setTitle(m_resourceFactory.getString(DIALOG_KEY + ".title"));
    setLayout(new BoxLayout(this.getContentPane(), BoxLayout.X_AXIS));
    m_uiElementFactory.configure(DIALOG_KEY, this);

// Build left frame
    SpringLayout leftLayout = new SpringLayout();
    JPanel inputPanel = new JPanel(leftLayout);
//    inputPanel.setLayout(leftLayout);
    mu_sortByFileNameRadioButton = m_uiElementFactory.createRadioButton("sortByName");
    mu_sortByFileNameRadioButton.setSelected(true);
    mu_sortByFileDateRadioButton = m_uiElementFactory.createRadioButton("sortByDate");
    ButtonGroup defineSortItemRadioButtonGroup = new ButtonGroup();
    defineSortItemRadioButtonGroup.add(mu_sortByFileNameRadioButton);
    defineSortItemRadioButtonGroup.add(mu_sortByFileDateRadioButton);
    mu_sortByFileDateRadioButton.setEnabled(false);

    mu_createSequenceFileButton = m_uiElementFactory.createButton("createSequence");
    mu_selectFolderButton = m_uiElementFactory.createButton("selectFolder");

    // Next line sets the hight of the textfield to constant. Needed for the SpringLayout
    mu_inputFileTextField.setMaximumSize(mu_inputFileTextField.getPreferredSize());
    inputPanel.add(mu_inputFileTextField);

    JPanel defineSortItemPanel = new JPanel();
    defineSortItemPanel.add(new JLabel(
        m_resourceFactory.getString(DIALOG_KEY + ".sortItem.label")));
    defineSortItemPanel.add(mu_sortByFileNameRadioButton);
    defineSortItemPanel.add(mu_sortByFileDateRadioButton);
    inputPanel.add(defineSortItemPanel);

    inputPanel.add(mu_createSequenceFileButton);
    inputPanel.add(mu_selectFolderButton);

    leftLayout.putConstraint(SpringLayout.WEST, mu_inputFileTextField,
        X_GAP,
        SpringLayout.WEST, inputPanel);
    leftLayout.putConstraint(SpringLayout.NORTH, mu_inputFileTextField,
        Y_GAP,
        SpringLayout.NORTH, inputPanel);

    leftLayout.putConstraint(SpringLayout.WEST, defineSortItemPanel,
        X_GAP,
        SpringLayout.WEST, inputPanel);
    leftLayout.putConstraint(SpringLayout.NORTH, defineSortItemPanel,
        Y_GAP,
        SpringLayout.SOUTH, mu_inputFileTextField);

    leftLayout.putConstraint(SpringLayout.WEST, mu_selectFolderButton,
        X_GAP,
        SpringLayout.WEST, inputPanel);
    leftLayout.putConstraint(SpringLayout.NORTH, mu_selectFolderButton,
        Y_GAP,
        SpringLayout.SOUTH, defineSortItemPanel);

    leftLayout.putConstraint(SpringLayout.WEST, mu_createSequenceFileButton,
        X_GAP,
        SpringLayout.EAST, mu_selectFolderButton);
    leftLayout.putConstraint(SpringLayout.NORTH, mu_createSequenceFileButton,
        Y_GAP,
        SpringLayout.SOUTH, defineSortItemPanel);

    leftLayout.putConstraint(SpringLayout.EAST, inputPanel,
        X_GAP * 3,
        SpringLayout.EAST, mu_inputFileTextField);
    leftLayout.putConstraint(SpringLayout.SOUTH, inputPanel,
        Y_GAP * 2,
        SpringLayout.SOUTH, mu_selectFolderButton);

    Border inputPanelBorder = BorderFactory.createTitledBorder(
        m_resourceFactory.getString("createPresentationPictures.inputPanel.label"));
    inputPanel.setBorder(inputPanelBorder);
    add(inputPanel);

// Now the right panel for the export information
    ZxGridBagPanel exportPanel = new ZxGridBagPanel(
        "createPresentationPictures.exportPanel", this);
    mu_fileNameHeadTextField = exportPanel.createAndAddLabelAndField(
        "fileHead", 10);
    mu_fileNameExtensionTextField = exportPanel.createAndAddLabelAndField(
        "fileExtension", 10);
    mu_fileNameExtensionTextField.setText("JPG");
    mu_fileNameExtensionTextField.setEditable(false);
    exportPanel.newLine();
    mu_fileNameCounterStartTextField = exportPanel.createAndAddLabelAndField(
        "fileCounterStart", 10);
    mu_fileNameCounterStartTextField.setText("0");
    mu_fileNameCounterLengthTextField = exportPanel.createAndAddLabelAndField(
        "fileCounterlength", 5);
    mu_fileNameCounterLengthTextField.setText("3");
    exportPanel.newLine();

    mu_resolutionComboBox = exportPanel.createAndAddLabelAndComboBox(
        PictureResolutions.getNameList(), "resolutionCatalog");
    exportPanel.newLine();

    mu_resolutionXTextField = exportPanel.createAndAddLabelAndField("resolutionX", 5);
    mu_resolutionYTextField = exportPanel.createAndAddLabelAndField("resolutionY");
    exportPanel.newLine();

    mu_descriptionTextField
        = exportPanel.createAndAddLabelAndField("description", 30, 3);
    exportPanel.newLine();

    mu_exportFolderTextField
        = exportPanel.createAndAddLabelAndField("exportFolder", 30, 3);
    exportPanel.newLine();

    mu_selectExportFolderButton = exportPanel.createAndAddButton("selectExportFolder");
    mu_startCreatePresentationPicturesButton = exportPanel.createAndAddButton("startImagesCreation");
    mu_closeButton = exportPanel.createAndAddCancelButton();

    mu_resolutionComboBox.addActionListener((ActionEvent e) -> {
      int i = mu_resolutionComboBox.getSelectedIndex();
      if (i == 0) {
        mu_resolutionXTextField.clear();
        mu_resolutionYTextField.clear();
      } else {
        PictureResolutions res = PictureResolutions.getResolution(i);
        mu_resolutionXTextField.setValue(res.xResolution());
        mu_resolutionYTextField.setValue(res.yResolution());
      }
    });

    add(exportPanel);
    this.doLayout();
    ZxUIHelper.packAndCenter(this);
    setModal(true);
  }

  private void performSelectInputFolder() {
    JFileChooser selectFileChooser = new JFileChooser("/home/rene/technik/JavaDevNB124/PicturesDatabase/testData");
    selectFileChooser.setFileSelectionMode(DIRECTORIES_ONLY);
//    File selectedFile;
    if (selectFileChooser.showOpenDialog(this) == APPROVE_OPTION) {
      File selectedFile = selectFileChooser.getSelectedFile();
      try {
        m_inputFolder = selectedFile.getCanonicalPath();
        mu_inputFileTextField.setText(m_inputFolder);
        //@todo: Je nach vorhandensein der sequemce datei den offenen start button (in-)aktiv setzen.
      } catch (IOException ex) {
        Logger.getLogger(CreatePresentationPicturesUI.class.getName()).log(Level.SEVERE, null, ex);
        ZxMessageDialog.displayExceptionMessage(this,
            "createPresentationPictures.fileselect.exception.text", ex);
      }
    }
  }

  private void performCreateSequenceFile() {
    File testSequenceFile = new File(m_inputFolder + "/" + SEQUEMCE_FILE_NAME);
    if (testSequenceFile.exists()) {
      int response = ZxMessageDialog.displayChoiceDialog(
          this, "createPresentationPictures.sequenceFile.exist.message");
      if (response == JOptionPane.OK_OPTION)
        performCreateSequenceFile(testSequenceFile);
    } else
      performCreateSequenceFile(testSequenceFile);
    ZxMessageDialog.displayMessage(
        this, "createPresentationPictures.createFileConfirmation");
  }

  private void performCreateSequenceFile(File sequenceFile) {
    m_controller.createSequenceFile(sequenceFile, m_inputFolder);
  }

  private void performSelectExportFolder() {
    JFileChooser selectFileChooser = new JFileChooser("/home/rene/technik/JavaDevNB124/PicturesDatabase/testData");
    selectFileChooser.setFileSelectionMode(DIRECTORIES_ONLY);
//    File selectedFile;
    if (selectFileChooser.showOpenDialog(this) == APPROVE_OPTION) {
      File selectedFile = selectFileChooser.getSelectedFile();
      try {
        m_exportFolder = selectedFile.getCanonicalPath();
        mu_exportFolderTextField.setText(m_exportFolder);
      } catch (IOException ex) {
        Logger.getLogger(CreatePresentationPicturesUI.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  private void performCreatePresentationPictures() {
    try {
      String fileNameHead = mu_fileNameHeadTextField.getText();
      int fileNameCounterStart = mu_fileNameCounterStartTextField.getInt();
      int fileNameCounterLength = mu_fileNameCounterLengthTextField.getInt();
      String fileNameExtension = mu_fileNameExtensionTextField.getText();
      int resolutionX = mu_resolutionXTextField.getInt();
      int resolutionY = mu_resolutionYTextField.getInt();
      String description = mu_descriptionTextField.getText();
      String exportFolder = mu_exportFolderTextField.getText();
      m_controller.processSequenceFile(
          m_inputFolder + "/" + SEQUEMCE_FILE_NAME,
          exportFolder,
          fileNameHead,
          fileNameCounterStart, fileNameCounterLength,
          fileNameExtension,
          resolutionX, resolutionY,
          description
      );
    } catch (FileNotFoundException ex) {
      ZxMessageDialog.displayExceptionMessage(this,
          "createPresentationPictures.openfile.exception.text", ex);
    } catch (IOException ex) {
      ZxMessageDialog.displayExceptionMessage(this,
          "createPresentationPictures.processfile.exception.text", ex);
    }
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    Object src = e.getSource();
    if (src == mu_selectFolderButton)
      performSelectInputFolder();
    else if (src == mu_createSequenceFileButton)
      CreatePresentationPicturesUI.this.performCreateSequenceFile();
    else if (src == mu_selectExportFolderButton)
      performSelectExportFolder();
    else if (src == mu_startCreatePresentationPicturesButton)
      performCreatePresentationPictures();
    else if (src == mu_closeButton)
      dispose();
    else
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

}
