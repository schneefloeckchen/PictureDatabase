package pictures.ui;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import picdata.MediumType;
import picdata.PictureMedium;
import pictures.ui.popups.MediumLoadStatisticsDisplay;
import pictures.ui.elements.StorageMediaTableModel;
import rzx.ui.CDVDHelper;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxComboBox;
import rzx.ui.ZxDateField;
import rzx.ui.ZxErrorDialog;
import rzx.ui.ZxGridBagPanel;
import rzx.ui.ZxMessageDialog;
import rzx.ui.ZxTextField;
import rzx.ui.ZxUIHelper;
import rzx.ui.ZxPanel;
import rzx.ui.ZxTable;

/**
 * Dialog to maintain and create new PictureMedium objects in the database.
 *
 * Structure: NORTH: Fields to enter and update the member os the selected
 * class, buttons to save, create, cancel. etc. to create a new entry as copy
 * the existing is loaded, and via create the new entry is created. Also button
 * to scan the m_medium.
 *
 *
 * @author rene
 */
public class MaintainPictureMediumUI extends BaseDialogUI implements MouseListener {

  /**
   * die static Strings sind fuer die Tests vorgesehen. Vergabe dieser Name
   * und belegen der Felder mit diesen Namen.
   */
  public static final String CODE_TEXT_FIELD = "CODE";
  private ZxPanel mu_dialogPanel = null;       // for all the textfields
  private ZxGridBagPanel mu_dialogFieldPanel = null;
  private ZxTextField mu_idTextField = null;     // Database ID number
  private ZxTextField mu_codeTextField = null;     // External Code number
  private ZxTextField mu_labelTextField = null;     // Volume Label
  private ZxTextField mu_titleTextField = null;     // Volume Title
  private ZxTextField mu_contentTextField = null;     // Volume content description
  private ZxTextField mu_remarkTextField = null;     // generic remark field
  private ZxTextField mu_writtenByTextField = null;     // Who has created the volume
  private ZxDateField mu_creationDateTextField = null;
  private ZxComboBox mu_mediaComboBox = null;        // MediumType type used

  private JButton mu_loadMediumButton = null;         // to select the m_medium in the file structure, can we read data from the m_medium
  private JButton mu_scanMediumButton = null;         // Starts the scan process of the loaded button
  private JButton mu_reloadButton = null;             // reloads the data from the database
  private JButton mu_displayMediumButton = null;      // displays the pictures of the medium on the maintain user interface 
  private JButton mu_newButton = null;                // Saves the data as new record to the database
  private JButton mu_clearButton = null;              // Clears all dialog fields
  private JButton mu_ejectMediumButton = null;

  private ZxTable mu_storageMediaTable = null;

  private File m_DVDRoot = null;          // Root of the mounted DVD drive
  private String m_DVDRootAsString = null;          // Root of the mounted DVD drive
  private PictureMedium m_medium = null;
  private StorageMediaTableModel m_storageMediaModel = null;     // the already stored media with pictures
  private int m_selectedRow = -1;
  private PictureMedium m_selectedMedium = null;

// Storage for tests
  private ArrayList<ZxTextField> mu_textFieldList = null;
  private ArrayList<JButton> mu_buttonList = null;

  public MaintainPictureMediumUI() {
    jInit();
  }

  /**
   * Structure of the UI: Panels:
   *
   * this - NORTH - mu_dialogPanel - NORTH -   <empty>
   * - CENTER - mialogFieldPanel (Elements to edit/create an entry) - SOUTH -
   * dialogButtonPanel (Buttons to edit..) - CENTER - table w. data from
   * database - SOUTH (Idea: Log area??)
   */
  private void jInit() {
    setTitleFromResource("mediaMaintain");
    mu_dialogPanel = new ZxPanel();
    mu_dialogPanel.setLayout(new BorderLayout());
    mu_dialogFieldPanel = new ZxGridBagPanel();
    mu_dialogFieldPanel.configureResource("mediaMaintain");
    mu_idTextField = mu_dialogFieldPanel.createAndAddLabelAndField("id", 5);
    mu_idTextField.setEditable(false);
    mu_codeTextField = mu_dialogFieldPanel.createAndAddLabelAndField("code", 5);
    mu_labelTextField = mu_dialogFieldPanel.createAndAddLabelAndField("label", 20);
    mu_titleTextField = mu_dialogFieldPanel.createAndAddLabelAndField("title", 20);
    mu_creationDateTextField = new ZxDateField(new SimpleDateFormat("dd.MM.yyyy"));
    mu_dialogFieldPanel.addLabelAndField("creationDate", mu_creationDateTextField);
    MediumType m = new MediumType();
    mu_mediaComboBox = new ZxComboBox(m.getKeyValuePairs());
    mu_dialogFieldPanel.addLabelAndField("mediaType", mu_mediaComboBox);
    // timestamps als RO field
    mu_dialogFieldPanel.newLine();
    mu_contentTextField = mu_dialogFieldPanel.createAndAddLabelAndField("content", 100, 11);
    mu_dialogFieldPanel.newLine();
    mu_remarkTextField = mu_dialogFieldPanel.createAndAddLabelAndField("remark", 100, 11);
    mu_dialogPanel.add(mu_dialogFieldPanel, BorderLayout.CENTER);

// Namen fuer die Tests
    mu_codeTextField.setName(CODE_TEXT_FIELD);

    ZxButtonPanel dialogButtonPanel = new ZxButtonPanel();
    dialogButtonPanel.configureResource("mediaMaintain");
    mu_loadMediumButton = dialogButtonPanel.createAndAddButton("loadMedium");
    mu_scanMediumButton = dialogButtonPanel.createAndAddButton("scan");
    mu_reloadButton = dialogButtonPanel.createAndAddButton("reload");
    mu_displayMediumButton = dialogButtonPanel.createAndAddButton("display");
    mb_saveButton = dialogButtonPanel.createAndAddButton("save");
    mu_newButton = dialogButtonPanel.createAndAddButton("new");
    mu_cancelButton = dialogButtonPanel.createAndAddButton("cancel");
    mu_ejectMediumButton = dialogButtonPanel.createAndAddButton("eject");
    mu_clearButton = dialogButtonPanel.createAndAddButton("clear");
    dialogButtonPanel.doLayout();
    ZxUIHelper.alignButtonSizes(dialogButtonPanel);
    mu_dialogPanel.add(dialogButtonPanel, BorderLayout.SOUTH);

    add(mu_dialogPanel, BorderLayout.NORTH);

    mu_storageMediaTable = new ZxTable();
    m_storageMediaModel = new StorageMediaTableModel(PictureMedium.getAllStorageMedia());
    mu_storageMediaTable.setModel(m_storageMediaModel);
    mu_storageMediaTable.addMouseListener(this);
    add(new JScrollPane(mu_storageMediaTable), BorderLayout.CENTER);
    finish();
//        ZxUIHelper.packAndCenter(this);
// inline document listener, just control anable/disable the buttons
    mu_idTextField.getDocument().addDocumentListener(
            new DocumentListener() {
      @Override
      public void insertUpdate(DocumentEvent e) {
        m_logger.fine("DocumentListener - insert");
        setSaveEntryState();
      }

      @Override
      public void removeUpdate(DocumentEvent e) {
        m_logger.fine("DocumentListener - remove");
        setNewEntryState();
      }

      @Override
      public void changedUpdate(DocumentEvent e) {
        m_logger.fine("DocumentListener - change");  // ID field cannot be changed frpm the end-user
      }
    });
    setNewEntryState();
  }

  /**
   * controls the enable/disable state of the buttons
   * Parameter names mach somehow the button names
   */
  private void enableDisableButtons(
          boolean loadMediumState, boolean scanMediaState,
          boolean reloadState, boolean displayState,
          boolean saveState, boolean newState,
          boolean cancelState, boolean ejectState, boolean clearState) {
    mu_loadMediumButton.setEnabled(loadMediumState);
    mu_scanMediumButton.setEnabled(scanMediaState);
    mu_reloadButton.setEnabled(false);     // Not Yet Implemented
    mu_displayMediumButton.setEnabled(displayState);
    mb_saveButton.setEnabled(saveState);
    mu_newButton.setEnabled(newState);
    mu_cancelButton.setEnabled(cancelState);
    mu_ejectMediumButton.setEnabled(ejectState);
    mu_clearButton.setEnabled(clearState);
  }

  /**
   * sets the button configuration if the ID is not displayed,
   * purpose is to create a new entry
   */
  private void setNewEntryState() {
    enableDisableButtons(true, false,
            false, false,
            false, true,
            true, true, true);
  }

  /**
   * sets the button configuration, if the ID is already created and displayed.
   * purpose is to edit the entry and to scan a CD/DVD
   */
  private void setSaveEntryState() {
    enableDisableButtons(true, true,
            false, true,
            true, false,
            true, true, true);
  }

  /**
   * Creates new entry in the database, add the object to the table data
   * model. The add method fires the listener as well, to redraw the updated
   * table.
   *
   * @todo add testing, if this code of a dvd/CD entry is already in use
   */
  private void performNewEntryFunction() {
    if (mu_idTextField.getText().length() > 0) {
      int response = ZxMessageDialog.displayChoiceDialog(this, "mediaMaintain.message.mediaCopy");
      if (response != JOptionPane.YES_OPTION)
        return; // to ensure, that a simple 
    }                   // close of the dialog (response = -1) also blocks further execution 
    if (checkRequiredFields()) {
      m_medium = new PictureMedium();
      loadMediumObject(m_medium);
      m_medium.update();          // Displaying the ID avoids double creation
      mu_idTextField.setValue(m_medium.getId());  //  of a new entry
      m_storageMediaModel.add(m_medium);
    }
  }

  /**
   * Saves an previously loaded medium after e.g. an update
   */
  private void performSaveEntryFunction() {
    if (checkRequiredFields())
      if (m_selectedMedium == null)
        ZxErrorDialog.displaySimpleErrorMessage(this, "mediaMaintain.message.noMediaLoaded");
      else {
        loadMediumObject(m_selectedMedium);
        try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
          em.getTransaction().begin();
          PictureMedium m = em.merge(m_selectedMedium);
          em.persist(m);
          em.getTransaction().commit();
        }
        m_medium = m_selectedMedium;
        m_storageMediaModel.fireAllTableModelListener(
                new TableModelEvent(m_storageMediaModel, m_selectedRow));
      }    // end checkrequired fields.
  }

  /**
   * checks, if the fields CODE and TITLE are populated
   * if not a errormessage is displayed
   *
   * @return false, if data are missing
   */
  private boolean checkRequiredFields() {
    if (mu_codeTextField.isEmpty() && mu_titleTextField.isEmpty()) {
      ZxErrorDialog.displaySimpleErrorMessage(this, "mediaMaintain.message.dataMissing");
      return false;
    } else
      return true;

  }
// alte Version, unter proprietaerer Hibernate Implementierung
//    private void performSaveEntryFunction() {
//        if (m_selectedMedium == null)
//            ZxErrorDialog.displaySimpleErrorMessage(this, "mediaMaintain.message.noMediaLoaded");
//        else {
//            Session session = PicHibernateUtil.getSessionFactory().openSession();
//            Transaction trans = session.beginTransaction();
//            session.refresh(m_selectedMedium);
//            loadMediumObject(m_selectedMedium);
//            session.save(m_selectedMedium);
//            trans.commit();
//            session.close();
//            m_medium = m_selectedMedium;
//            m_storageMediaModel.fireAllTableModelListener(new TableModelEvent(m_storageMediaModel, m_selectedRow));
//        }
//    }
//

  private void loadMediumObject(PictureMedium medium) {
    medium.setCode(mu_codeTextField.getInt());
    int storageID = mu_mediaComboBox.getSelectedKey();
    medium.setStorageMediumId(storageID);
    medium.setTitle(mu_titleTextField.getText());
    medium.setLabel(mu_labelTextField.getText());
    medium.setContent(mu_contentTextField.getText());
    medium.setRemark(mu_remarkTextField.getText());
    medium.setDateWritten(mu_creationDateTextField.getDate());
  }

  /**
   * Loads some data from the mounted CD/DVD mount(home block)
   */
  private void performLoadMediumFunction() {
//        JFileChooser fc = new JFileChooser("/mnt/speicher/DigiBild/");
    JFileChooser fc = new JFileChooser("/run/media/");
    fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
    int answer = fc.showOpenDialog(this);
    if (answer == JFileChooser.APPROVE_OPTION) {
      m_DVDRoot = fc.getSelectedFile();
      m_DVDRootAsString = m_DVDRoot.toString();
      Path path = Paths.get(m_DVDRootAsString);
      BasicFileAttributeView basicAttView = Files.getFileAttributeView(path, BasicFileAttributeView.class);
      try {
        BasicFileAttributes attributes = basicAttView.readAttributes();
        mu_labelTextField.setText(m_DVDRootAsString.substring(m_DVDRootAsString.lastIndexOf("/") + 1));
        FileTime creationTime = attributes.creationTime();
        Date date = new Date(creationTime.toMillis());
        mu_creationDateTextField.setValue(date);
      } catch (IOException ex) {
        Logger.getLogger(MaintainPictureMediumUI.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
  }

  private void performScanMediumFunction() {
    MediumLoadStatisticsDisplay display = new MediumLoadStatisticsDisplay();   // Set visible is inside the class
    display.setMediumAndRoot(m_medium, m_DVDRoot);
    display.setVisible(true);
  }

  /**
   * auskommentiert waerend test sessions
   *
   * @todo !!! reactivate !!!
   */
  private void performDisplayMediumFunction() {
//      System.out.println("Display Medium");
//      MaintainPictureDialog dialog = new MaintainPictureDialog();
//      long id = m_selectedMedium.getId();
//      dialog.performLoadStorageMedium(id);
//      dialog.setVisible(true);
  }

  private void performEjectFunction() {
    CDVDHelper.eject();
  }

  private void performClearFunction() {
    Component components[] = mu_dialogFieldPanel.getComponents();
    for (Component component : components)
      if (component instanceof ZxTextField zxTextField)
        zxTextField.clear();
  }

  private void performMouseClicked() {
    m_selectedRow = mu_storageMediaTable.getSelectedRow();
    m_logger.log(Level.FINE, "Selected row is: {0}", m_selectedRow);
    StorageMediaTableModel model = (StorageMediaTableModel) mu_storageMediaTable.getModel();
    m_selectedMedium = model.getEntryAt(m_selectedRow);
//        Session session = PicHibernateUtil.getSessionFactory().openSession();
//        session.refresh(m_selectedMedium);
//        session.close();
    m_medium = m_selectedMedium;
    storeSelectedMedium();
  }

  /**
   * writes the data from the selected medium into the ui
   */
  private void storeSelectedMedium() {
    mu_idTextField.setValue(m_selectedMedium.getId());
    mu_codeTextField.setValue(m_selectedMedium.getCode());
    mu_mediaComboBox.setSelectionByKey(m_selectedMedium.getStorageMedium().getId());
    mu_creationDateTextField.setValue(m_selectedMedium.getDateWritten());
    mu_labelTextField.setText(m_selectedMedium.getLabel());
    mu_titleTextField.setText(m_selectedMedium.getTitle());
    mu_contentTextField.setText(m_selectedMedium.getContent());
    mu_remarkTextField.setText(m_selectedMedium.getRemark());
  }

  /**
   * Method for junit testing, or further testing frameworks. Returns an
   * ArrayList with some (testing relevant) UI-Elements in a ArrayList.
   * Current implementation:
   * 0 -> mu_codeTextField (to enter the code of the
   * dvd/CD)
   * 1 -> mu_labelTextField (Label as written in the home/mount Block
   * of the DVD)
   * 2 -> mu-IDteXTfIELD (GENERATED ID OF THE NEW ENTRY)
   * The arraylist is build during the first call of this method.
   *
   * @return
   */
  public ArrayList<ZxTextField> getTextFields() {
    if (mu_textFieldList == null) {
      mu_textFieldList = new ArrayList<>();
      mu_textFieldList.add(mu_codeTextField);   // 0
      mu_textFieldList.add(mu_labelTextField);  // 1
      mu_textFieldList.add(mu_idTextField);     // 2
    }
    return mu_textFieldList;
  }

  /**
   * like getTextFields, but for the JButtons in the UI Current
   * implementation:
   * 0 -> mu_newButton (create a new entry in the database)
   * 1 -> mb_saveButton (saves the loaded entry);
   * 2 -> mu_clearButton (clear the ui)
   *
   * @return
   */
  public ArrayList<JButton> getButtons() {
    if (mu_buttonList == null) {
      mu_buttonList = new ArrayList<>();
      mu_buttonList.add(mu_newButton);      // 0
      mu_buttonList.add(mb_saveButton);     // 1
      mu_buttonList.add(mu_clearButton);    // 2
    }
    return mu_buttonList;
  }

  public void setSelectedMedium(PictureMedium medium) {
    m_selectedMedium = medium;
    m_medium = medium;
    storeSelectedMedium();
  }

  /**
   * kopiert (die Referenz) des zuletzt erzeugten/gespeicherten
   * Mediums auf das m_selectedMedium. Das wird beim regulaeren
   * Betrieb durch die selection in der Tabelle gemacht.
   */
  public void updateSelectedMedium() {
    m_selectedMedium = m_medium;
  }

  /**
   * Returns the id of the latest created or updated medium. The id of
   * the current m_medium object
   */
  public long getLastId() {
    return m_medium.getId();
  }

  /*
 * Ende der Methoden, die nur zum junit Testen gedacht sind 
   */
  // Implementation of the ActionListener interface
  @Override
  public void actionPerformed(ActionEvent ae) {
    Object source = ae.getSource();
    if (source == mu_cancelButton)
      dispose();
    else if (source == mu_newButton)
      performNewEntryFunction();
    else if (source == mb_saveButton)
      performSaveEntryFunction();
    else if (source == mu_loadMediumButton)
      performLoadMediumFunction();
    else if (source == mu_scanMediumButton)
      performScanMediumFunction();
    else if (source == mu_displayMediumButton)
      performDisplayMediumFunction();
    else if (source == mu_clearButton)
      performClearFunction();
    else if (source == mu_ejectMediumButton)
      performEjectFunction();
    else
      throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  // Implementation of MouseListener
  @Override
  public void mouseClicked(MouseEvent me) {
    performMouseClicked();
  }

  @Override
  public void mousePressed(MouseEvent me) {
  }

  @Override
  public void mouseReleased(MouseEvent me) {
  }

  @Override
  public void mouseEntered(MouseEvent me) {
  }

  @Override
  public void mouseExited(MouseEvent me) {
  }
}
