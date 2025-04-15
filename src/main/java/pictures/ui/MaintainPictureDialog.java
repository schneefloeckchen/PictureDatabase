package pictures.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.logging.Level;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.table.TableColumnModel;
import javax.swing.tree.TreePath;
import picdata.DigiPicture;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.extended.DigiPictureAttributeTableModel;
import rzx.graphics.ZxPicturePanel;
import rzx.ui.ZxUIElementFactory;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxCheckBox;
import rzx.ui.ZxComboBox;
import rzx.ui.ZxGridBagPanel;
import rzx.ui.ZxMessageDialog;
import rzx.ui.ZxPanel;
import rzx.ui.ZxScrollPane;
import rzx.ui.ZxTable;
import rzx.ui.ZxTextField;
import rzx.ui.ZxTreeTableDataModel;

/**
 * Dialog to view and maintain data of the picture. BaseDialogUI is providing a
 * BorderLayout.
 *
 * NORTH: Search and storage medium data
 * WEST: Tree Table w. filestructure of the storage medium. A fixed size is
 * defined, to ensure that the picture in CENTER can be seen all the time.
 * CENTER: Picture, BorderLayout aloows it not to define a size for this part.
 * EAST: The EXIF Data in a DialogTable
 * SOUTH: Buttons
 *
 * @author rene
 */
public class MaintainPictureDialog extends BaseDialogUI implements MouseListener {

  private static final String RESOURCEFILE_ENTRY = "pictureMaintain";
  // Components NORTH Panel (Storage Medium Data and Search)
  private ZxTextField mt_mediumCodeTextField = null;
  private ZxTextField mt_mediumLabelTextField = null;
  private ZxComboBox mc_mediumTitleComboBox = null;
  private ZxTextField mt_mediumContentTextField = null;
  private ZxTextField mt_mediumDateWrittenTextField = null;
  private ZxTextField mt_mediumWrittenByTextField = null;
  private ZxTextField mt_mediumMediumTypeTextField = null;
  private ZxTextField mt_mediumRemarkTextField = null;
  private ZxCheckBox mc_indicateDuplicates = null;
  private ZxCheckBox mc_noTransformation = null;      // Controls if the m_currentPicture shall be rotated (using orientation) or not.

  private JPopupMenu mu_treePopupMenu = new JPopupMenu("Additional Actions");  // @todo move to ressource
  private JMenuItem mu_listDuplicates = new JMenuItem("*List Duplicates");

  // Components for the WEST Panel (Filestructure of the storage Medium)
//    private ZxTreeTable mu_storageMediumStructure = new ZxTreeTable();
  private final JTree mu_fileStructureTree = new JTree();
  private final ZxTable mu_pictureFileTable = new ZxTable();
  private final ZxTreeTableDataModel m_storageMediumStructureData = new ZxTreeTableDataModel();

  // Elements of the SOUTH Panel (Buttons) 
  private final ZxUIElementFactory m_buttonFactory
          = ZxUIElementFactory.getInstance();
  private JButton mb_findStorageMediumButton = null;
  private JButton mb_nextButton = null;
  private JButton mb_previousButton = null;
  private JButton mb_rotateButton = null;

  private final ZxPicturePanel mu_displayPicturePanel = new ZxPicturePanel();

  // Components for the EAST Panel (Picture Data)
  private final ZxTable mt_exifTable = new ZxTable();
  private final DigiPictureAttributeTableModel m_exifTableModel = new DigiPictureAttributeTableModel();

  private PictureMedium m_loadedMedium = null;

  private int m_currentTableRow = -1;                 // from picture table, for next and previous
  private DigiPicture m_currentPicture = null;

  // Elements for JUnit testing
  private ArrayList<JButton> mu_buttons = null;

  public MaintainPictureDialog() {
    jInit();
  }

  private void jInit() {
    setTitleFromResource(RESOURCEFILE_ENTRY);
    // Build NORTH Panel
    ZxGridBagPanel mediumInfoPanel = new ZxGridBagPanel("pictureMaintain.storageMediumPanel", this);
    m_buttonFactory.configure("pictureMaintain", this);
    mb_findStorageMediumButton = m_buttonFactory.createButton("findCD");
    mediumInfoPanel.configureResource("pictureMaintain");

    mt_mediumCodeTextField = mediumInfoPanel.createAndAddLabelAndField("mediumCode", 5);
    mt_mediumLabelTextField = mediumInfoPanel.createAndAddLabelAndField("mediumLabel", 20);
    mediumInfoPanel.add(mb_findStorageMediumButton);
    PictureMedium m = new PictureMedium();
    mc_mediumTitleComboBox = new ZxComboBox(m.getKeyValuePairs());
    mc_mediumTitleComboBox.addActionListener(this);
    mediumInfoPanel.createAndAddLabel("mediumTitle");
    mediumInfoPanel.add(mc_mediumTitleComboBox);
    mediumInfoPanel.newLine();

    mt_mediumContentTextField = mediumInfoPanel.createAndAddLabelAndField("mediumContent", 100, 6);
    mediumInfoPanel.newLine();

    mt_mediumWrittenByTextField = mediumInfoPanel.createAndAddLabelAndField("writtenBy", 20);
    mt_mediumDateWrittenTextField = mediumInfoPanel.createAndAddLabelAndField("dateWritten", 20);
    mt_mediumMediumTypeTextField = mediumInfoPanel.createAndAddLabelAndField("mediumType", 20);
    mediumInfoPanel.newLine();

    mt_mediumRemarkTextField = mediumInfoPanel.createAndAddLabelAndField("remark", 100, 6);
    mediumInfoPanel.newLine();
    mc_indicateDuplicates = mediumInfoPanel.createAndAddLabelAndCheckBox("duplicates", this);
    mc_indicateDuplicates.setSelected(false);
    mc_noTransformation = mediumInfoPanel.createAndAddLabelAndCheckBox("noTransformation");
    mc_noTransformation.setSelected(false);

    add(mediumInfoPanel, BorderLayout.NORTH);

    // Build WEST Panel (Storage Medium File Structure)
    ZxPanel structurePanel = new ZxPanel("pictureMaintain", "storageStructurePanel");
    JSplitPane splitPane = new JSplitPane();
    mu_fileStructureTree.addMouseListener(this);

    mu_listDuplicates.addActionListener(this);
    mu_treePopupMenu.add(mu_listDuplicates);
    mu_fileStructureTree.setComponentPopupMenu(mu_treePopupMenu);

    splitPane.setLeftComponent(new JScrollPane(mu_fileStructureTree));
    splitPane.setRightComponent(new JScrollPane(mu_pictureFileTable));
    splitPane.setPreferredSize(new Dimension(500, DigiPicture.THUMB_SIZE));     // @finish size test
    splitPane.setDividerLocation(150);
    splitPane.setDividerSize(3);
    structurePanel.add(new JScrollPane(splitPane));
    add(structurePanel, BorderLayout.WEST);
    mu_fileStructureTree.setModel(m_storageMediumStructureData);
    mu_pictureFileTable.addMouseListener(this);
    mu_pictureFileTable.setModel(m_storageMediumStructureData);
    TableColumnModel colModel = mu_pictureFileTable.getColumnModel();
    colModel.getColumn(0).setPreferredWidth(50);
    colModel.getColumn(1).setPreferredWidth(100);
    colModel.getColumn(2).setPreferredWidth(80);
    colModel.getColumn(3).setPreferredWidth(80);
    //        colModel.getColumn(4).setPreferredWidth(10);

    // Build CENTER panel, Picture here
    Dimension pictureSize = new Dimension(DigiPicture.THUMB_SIZE, DigiPicture.THUMB_SIZE);
    mu_displayPicturePanel.setPreferredSize(pictureSize);
    mu_displayPicturePanel.setMinimumSize(pictureSize);

    ZxScrollPane picturePanel = new ZxScrollPane(
            "pictureMaintain", "thumbnailPanel", mu_displayPicturePanel);
    picturePanel.setMinimumSize(pictureSize);
    picturePanel.setPreferredSize(pictureSize);
    picturePanel.setSize(pictureSize);
    add(picturePanel, BorderLayout.CENTER);

    // Build EAST Panel, Exif Data and more
    mt_exifTable.setModel(m_exifTableModel);
    ZxScrollPane exifPanel = new ZxScrollPane("pictureMaintain", "pictureDataPanel", mt_exifTable);
    exifPanel.setPreferredSize(new Dimension(350, 140));
    add(exifPanel, BorderLayout.EAST);
    colModel = mt_exifTable.getColumnModel();
    colModel.getColumn(0).setPreferredWidth(100);
    colModel.getColumn(1).setPreferredWidth(250);

    // Build SOUTH Panel (Buttons)
    ZxButtonPanel buttonPanel = new ZxButtonPanel();
    m_buttonFactory.configure("pictureMaintain", this);
    mb_previousButton = buttonPanel.createAndAddButton("previous");
    mb_nextButton = buttonPanel.createAndAddButton("next");
    mb_rotateButton = buttonPanel.createAndAddButton("rotate");
    buttonPanel.add(mb_saveButton);
    buttonPanel.add(mu_cancelButton);
    buttonPanel.align();
    add(buttonPanel, BorderLayout.SOUTH);

    finish();
  }

  /**
   * used from LoadPictureTester
   *
   * @param id
   */
  public void loadById(long id) {
    m_currentPicture = DigiPicture.getById(id);
    m_logger.log(Level.FINE, "Loaded as {0}", m_currentPicture.getThumbFormat());

    try {
      if (mc_noTransformation.isSelected()) {
        mu_displayPicturePanel.loadPicture(m_currentPicture.getThumbAsBufferedImage());
      } else {
        mu_displayPicturePanel.loadPicture(m_currentPicture.getThumbAsOrientedBufferedImage());
      }
    } catch (SQLException | IOException ex) {
      ZxMessageDialog.displayExceptionMessage(this,
              "pictureMaintain.exception.cannotload", ex);
    }
  }

  private void performIndicateDuplicate() {
    m_storageMediumStructureData.addDuplicateInfo(mc_indicateDuplicates.isSelected());
  }

  private void performPreviousOperation() {
    m_logger.fine("Display previous pricture");
    if (m_currentTableRow > 0) try {
      m_currentTableRow--;
      displaySelectedPicture(m_currentTableRow);
    } catch (SQLException | IOException ex) {
      ZxMessageDialog.displayExceptionMessage(this,
              "pictureMaintain.exception.cannotload", ex);
    }
  }

  private void performNextOperation() {
    m_logger.fine("Display next pricture");
    if (m_currentTableRow < mu_pictureFileTable.getRowCount() - 1) try {
      m_currentTableRow++;
      displaySelectedPicture(m_currentTableRow);
    } catch (SQLException | IOException ex) {
      ZxMessageDialog.displayExceptionMessage(this,
              "pictureMaintain.exception.cannotload", ex);
    }
  }

  private void performRotate() {
    int orientation = m_currentPicture.getOrientation();
    switch (orientation) {
      case -1, 0, 1 ->
        orientation = 6;
      case 6 ->
        orientation = 3;
      case 3 ->
        orientation = 8;
      case 8 ->
        orientation = 1;
      default -> {
        m_logger.warning("Unexpected value for orientation: " + orientation + " for image " + m_currentPicture.getId());
        return;
      }
    }
    m_currentPicture.setOrientation(orientation);

    try {
      mu_displayPicturePanel.loadPicture(m_currentPicture.getThumbAsOrientedBufferedImage());
    } catch (SQLException | IOException ex) {
      ZxMessageDialog.displayExceptionMessage(this,
              "pictureMaintain.exception.cannotload", ex);
    }
  }

  private void performSaveOperation() {
    m_currentPicture.update();
  }

  private void performLoadStorageMedium() {
    long id = (long) mc_mediumTitleComboBox.getSelectedKey();
    performLoadStorageMedium(id);
  }

  public void performLoadStorageMedium(long id) {
    m_loadedMedium = PictureMedium.getById(id);
    mt_mediumCodeTextField.setValue(m_loadedMedium.getCode());
    mt_mediumLabelTextField.setText(m_loadedMedium.getLabel());
    mt_mediumContentTextField.setText(m_loadedMedium.getContent());
    mt_mediumDateWrittenTextField.setText(m_loadedMedium.getDateWritten().toLocaleString());
    mt_mediumWrittenByTextField.setText(m_loadedMedium.getWrittenBy());
    mt_mediumMediumTypeTextField.setText(m_loadedMedium.getStorageMedium().toString());
    mt_mediumRemarkTextField.setText(m_loadedMedium.getRemark());
    m_storageMediumStructureData.loadMedium(id);
  }

  private void performTreeSelected(MouseEvent e) {
    TreePath selPath = mu_fileStructureTree.getPathForLocation(e.getX(), e.getY());
    if (selPath != null) {
      Object obj = selPath.getLastPathComponent();
      if (obj instanceof PicDirectory picDirectory) {
        m_storageMediumStructureData.loadFiles(picDirectory);
        if (m_storageMediumStructureData.getRowCount() > 0)
                    try {
          displaySelectedPicture(0);
          m_currentTableRow = 0;
        } catch (SQLException | IOException ex) {
          ZxMessageDialog.displayExceptionMessage(this,
                  "pictureMaintain.exception.cannotload", ex);
        }
      }
    }
  }

  private void performFileTableSelected(MouseEvent e) throws SQLException, IOException {
    m_currentTableRow = mu_pictureFileTable.getSelectedRow();
    displaySelectedPicture(m_currentTableRow);
  }

  private void displaySelectedPicture(int row) throws SQLException, IOException {
    m_currentPicture = m_storageMediumStructureData.getPictureFromRow(row);
    mu_pictureFileTable.clearSelection();
    mu_pictureFileTable.addRowSelectionInterval(row, row);
    if (mc_noTransformation.isSelected()) {
      mu_displayPicturePanel.loadPicture(m_currentPicture.getThumbAsBufferedImage());
    } else {
      mu_displayPicturePanel.loadPicture(m_currentPicture.getThumbAsOrientedBufferedImage());
    }
    m_exifTableModel.load(m_currentPicture);
  }

  private void performListDuplicates(ActionEvent e) {
    m_logger.fine("Show duplicates");
    TreePath selPath = mu_fileStructureTree.getSelectionPath();
    if (selPath != null) {
      Object obj = selPath.getLastPathComponent();
      if (obj instanceof PicDirectory picDirectory) {
        m_logger.log(Level.FINE, "Selected is: {0}",
                picDirectory.getDirectoryName());
        DuplicateDisplay display = new DuplicateDisplay();
        display.start(picDirectory);
//                display.setVisible(true);
      }
    }
  }

  /**
   * Opens a dialog and allows to search a storage Medium by name, Title,
   * Content or directory name.
   */
  private void performFindStorageMedium() {
    m_logger.info("building search storage medium dialog");
    FindStorageMediumDialog dialog = new FindStorageMediumDialog();
//      dialog.setModal(true);
    dialog.setVisible(true);
    long selectedMediumId = dialog.getSelectedId();
    m_logger.log(Level.FINE, "Selected id is {0}", selectedMediumId);
    mc_mediumTitleComboBox.setSelectionByKey(selectedMediumId);
  }

  /*
  Funktionen, die zum JUnit (5) Testen benoetigt werden
   */
  /**
   * Returns the relevant buttons for testing
   * 0 -> mb_saveButton -- saves the image after updating the orientation
   * 1 -> mb_rotateButton -- rotates the loaded image
   *
   * @return
   */
  public ArrayList<JButton> getButtons() {
    if (mu_buttons == null) {
      mu_buttons = new ArrayList<>();
      mu_buttons.add(mb_saveButton);
      mu_buttons.add(mb_rotateButton);
    }
    return mu_buttons;
  }

  // ActionListener implementation
  @Override
  public void actionPerformed(ActionEvent ae) {
    Object source = ae.getSource();
    if (source == mu_cancelButton) {
      dispose();
    } else if (source == mb_previousButton) {
      performPreviousOperation();
    } else if (source == mb_nextButton) {
      performNextOperation();
    } else if (source == mb_saveButton) {
      performSaveOperation();
    } else if (source == mc_mediumTitleComboBox) {
      performLoadStorageMedium();
    } else if (source == mc_indicateDuplicates) {
      performIndicateDuplicate();
    } else if (source == mu_listDuplicates) {
      performListDuplicates(ae);
    } else if (source == mb_rotateButton) {
      performRotate();
    } else if (source == mb_findStorageMediumButton) {
      performFindStorageMedium();
    } else {
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
  }

  // MouseListener implementation
  @Override
  public void mouseClicked(MouseEvent e) {
    Object src = e.getSource();
    if (src == mu_fileStructureTree) {
      performTreeSelected(e);
    } else if (src == mu_pictureFileTable)
            try {
      m_logger.fine("File Table selected");
      performFileTableSelected(e);
    } catch (IOException | SQLException | RuntimeException ex) {
      System.err.printf("Exception gefangen...... " + ex.getLocalizedMessage());
      ex.printStackTrace();
    }
  }

  @Override
  public void mousePressed(MouseEvent e) {
  }

  @Override
  public void mouseReleased(MouseEvent e) {
  }

  @Override
  public void mouseEntered(MouseEvent e) {
  }

  @Override
  public void mouseExited(MouseEvent e) {
  }
}
