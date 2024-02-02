package pictures.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.Searcher;
import pictures.ui.elements.FolderSearchTableModel;
import pictures.ui.elements.MediumSearchTableModel;
import pictures.ui.elements.SearchTableModel;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxGridBagPanel;
import rzx.ui.ZxTable;
import rzx.ui.ZxTextField;
import rzx.ui.ZxUIElementFactory;
import rzx.util.StringHelper;

/**
 * Dialog to locate / find a StrageMedium by:
 * o Name and Title
 * o Content
 * on a separate Tab :
 * o folder
 *
 * User Interface:
 * BorderLayout
 * NORTH: 2 Tabs (maybe more later), 1st to find by fields in the PictureMedium
 * class, (title, label, content)
 * 2nd to find in the PIC_DIRECTORY fields. currently only name field
 * used
 * CENTER: Table with results from database
 * SOUTHE: Buttons to return the selected entry in the table or to cancel
 * the method
 * 24 Oct 22
 *
 * @author rene
 */
public class FindStorageMediumDialog extends BaseDialogUI {

  private static final String RESOURCEFILE_ENTRY = "findStorageMedium";
  private static final String[] MEDIUM_TABLE_HEADER = {
    "label", "title", "content", "remark"
  };
  private static final String[] FOLDER_TABLE_HEADER = {
    "name", "code", "label", "title", "content", "remark"
  };

  // Elements for the NORTH Panel
  private ZxTextField mt_mediumLabelTextField = null;
  private ZxTextField mt_mediumTitleTextField = null;
  private ZxTextField mt_mediumContentTextField = null;
  private JButton mb_mediumSearchButton = null;

  private ZxTextField mt_folderNameTextField = null;
  private JButton mb_folderSearchButton = null;

  // Table in the CENTER part of the layout
  private ZxTable m_resultTable = new ZxTable();
  private SearchTableModel m_searchTableModel =
      new MediumSearchTableModel();  // Dummy for initial drawing of table!

  // Buttons for the SOUTH part if the dialog
  private JButton mb_selectButton = null;
  private JButton mb_cancelButton = null;

  // non UI member
  private Searcher m_searcher = new Searcher();      // need in anyway, so build it.

  private long m_selectedId = -1;

  public FindStorageMediumDialog() {
    jInit();
  }

  private void jInit() {
    setTitleFromResource(RESOURCEFILE_ENTRY);

    setLayout(new BorderLayout());
    ZxUIElementFactory uiElementFactory = ZxUIElementFactory.getInstance();
    uiElementFactory.configure(RESOURCEFILE_ENTRY, this);

    JTabbedPane northPane = new JTabbedPane();
    ZxGridBagPanel mediumGridBagPanel = new ZxGridBagPanel(
        "findStorageMedium.mediumTab", this);
    mt_mediumLabelTextField = mediumGridBagPanel.
        createAndAddLabelAndField("label", 25);
    mt_mediumTitleTextField = mediumGridBagPanel.
        createAndAddLabelAndField("title", 25);
    mediumGridBagPanel.newLine();
    mt_mediumContentTextField = mediumGridBagPanel.
        createAndAddLabelAndField("content", 50, 3);
    mediumGridBagPanel.newLine();
    mb_mediumSearchButton = mediumGridBagPanel.createAndAddButton("search");
    northPane.add("Medium", mediumGridBagPanel);     // @todo move to ressource file

    ZxGridBagPanel folderGridBagPanel = new ZxGridBagPanel(
        "findStorageMedium.folderTab", this);
    mt_folderNameTextField = folderGridBagPanel.
        createAndAddLabelAndField("folder", 30);
    folderGridBagPanel.newLine();
    mb_folderSearchButton = folderGridBagPanel.createAndAddButton("search");
    northPane.add("Folder", folderGridBagPanel);

    add(northPane, BorderLayout.NORTH);

// CENTER Part of the layout
    add(new JScrollPane(m_resultTable), BorderLayout.CENTER);
    m_resultTable.setModel(m_searchTableModel);

    ZxButtonPanel buttonPanel = new ZxButtonPanel();
    uiElementFactory.configure(RESOURCEFILE_ENTRY);
    mb_selectButton = buttonPanel.createAndAddButton("select");
    mb_cancelButton = buttonPanel.createAndAddCancelButton();

    add(buttonPanel, BorderLayout.SOUTH);
//    setMinimumSize(new Dimension(100, 700));
    setModal(true);       // Needed so that the result can be retrieved from
    // the calling method.
    finish();
    setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
  }

  public long getSelectedId() {
    return m_selectedId;
  }

  private void performSelectOperation() {
    int selectedRow = m_resultTable.getSelectedRow();
    if (selectedRow > -1) {
      Object selectedObject = m_searchTableModel.getMedium(selectedRow);
      System.out.println("Object is "+selectedObject.getClass().getName());
      if (selectedObject instanceof PictureMedium medium) {
        m_selectedId = medium.getId();
      } else if (selectedObject instanceof PicDirectory directory) {
        m_selectedId = directory.getMedium().getId();
      }
      
//      m_selectedId = ((PicDataBaseClass) m_searchTableModel.getMedium(selectedRow))
//          .getId();
    }
    else
      m_selectedId = -1;
    dispose();
  }

  private void performSearchMediumOperation() {
    List<PictureMedium> result = null;
    String label, title, content;
//    m_searchTableModel = new SearchTableModel<PictureMedium>();
    m_resultTable.setModel(m_searchTableModel);
    label = mt_mediumLabelTextField.getText();
    if (StringHelper.isEmpty(label)) {
      title = mt_mediumTitleTextField.getText();
      if (StringHelper.isEmpty(title)) {
        content = mt_mediumContentTextField.getText();
        if (!StringHelper.isEmpty(content)) {
          result = m_searcher.searchPictureMediumsByContent(content);
        }
      } else result = m_searcher.searchPictureMediumsByTitle(title);
    } else result = m_searcher.searchPictureMediumsByLabel(label);
    
//    String muster = mt_mediumTitleTextField.getText();
//    List<PictureMedium> result = m_searcher.searchPictureMediumsByTitle(muster);
    m_searchTableModel.setLabels("medium", MEDIUM_TABLE_HEADER);
    m_searchTableModel.setData(result);
    m_selectedId = -1; // Nothing selected so far
  }

  private void performSearchFolderOperation() {
    String name = mt_folderNameTextField.getText();
    List<PicDirectory> result = m_searcher.searchPictureDirectory(name);
    System.out.println("Länge " + result.size());
    System.out.println(result);
    m_searchTableModel = new FolderSearchTableModel();
    m_searchTableModel.setLabels("folder", FOLDER_TABLE_HEADER);
    m_searchTableModel.setData(result);
    m_resultTable.setModel(m_searchTableModel);
  }

  /**
   * Implementation of ActionListener Interface, which is requested from
   * BaseDIalogUI
   *
   * @param e
   */
  @Override
  public void actionPerformed(ActionEvent e) {
    Object source = e.getSource();
    if (source == mb_cancelButton)
      dispose();
    else if (source == mb_selectButton)
      performSelectOperation();
    else if (source == mb_folderSearchButton)
      performSearchFolderOperation();
    else if (source == mb_mediumSearchButton)
      performSearchMediumOperation();
    else
      throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }

}
