/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pictures.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import picdata.PicCatalogItem;
import rzx.ui.ZxErrorDialog;
import rzx.ui.ZxUIHelper;

/**
 * To Edit the catalogs. We need: CameraName = Model, some more infos as read
 * from the jpeg header, includes vendor MediaType = CD ROM, DVD + / - and the
 * like, manually entered
 *
 * Base UI Structure: NORTH: Buttons to switch between the catalogs, to store,
 * create new, finish CENTER: Dialog to view, edit the data. Needs updates
 * depending on the selected catalog SOUTH: Scrolltable with the data
 *
 * @author rene
 */
public class CatalogUI extends BaseDialogUI {

  private final JPanel mu_buttonPanel = new JPanel();
  private JButton mu_useCameraCatalogButton = null;
  private JButton mu_useMediaTypeCatalogButton = null;
  private JButton mu_addButton = null;

  private CatalogEditTable mu_editTable = new CatalogEditTable();

  String[] m_cameraLabels = new String[]{ //  Labels by translation in resource file
    "name", "manufacturer", "remark" //  are not the database column names
  };
  String[] m_mediaLabels = new String[]{
    "depiction", "capacity", "remark"
  };

  // Elements for JUnit testing
  private ArrayList<JButton> mu_buttons = null;

  public CatalogUI() {
    jInit();
  }

  private void jInit() {
    setTitleFromResource("catalogUI");
    m_uiElementFactory.configure("catalogUI");    // ActionListener already defined
    mu_useCameraCatalogButton = m_uiElementFactory.createButton("useCameraCatalog");
    mu_useMediaTypeCatalogButton = m_uiElementFactory.createButton("useMediaTypeCatalog");
    mu_addButton = m_uiElementFactory.createButton("add");
    mu_buttonPanel.setLayout(new BoxLayout(mu_buttonPanel, BoxLayout.LINE_AXIS));
    mu_buttonPanel.add(mu_useCameraCatalogButton);
    mu_buttonPanel.add(mu_useMediaTypeCatalogButton);
    mu_buttonPanel.add(mu_addButton);
    mu_buttonPanel.add(mb_saveButton);
    mu_buttonPanel.add(mu_cancelButton);
    this.add(mu_buttonPanel, BorderLayout.NORTH);
    this.add(new JScrollPane(mu_editTable), BorderLayout.CENTER);
    this.pack();                                  // needed, so that the button alignment works  
    ZxUIHelper.alignButtonSizes(mu_buttonPanel);
    ZxUIHelper.packAndCenter(this);
  }

  private void executeCameraCatalogButton() {
    m_logger.fine("Starte Camera Catalog");
    mu_addButton.setEnabled(false);    // Not possible to add cameras, done while import
    mu_editTable.initializeCatalog("Camera", m_cameraLabels);
  }

  private void executeMediaTypeCatalogButton() {
    m_logger.fine("Starte Media Catalog");
    mu_addButton.setEnabled(true);
    mu_editTable.initializeCatalog("MediumType", m_mediaLabels);
  }

  private void executeSaveButton() {
    mu_editTable.saveChanges();
  }

  /*
        Erzeugt einen neuen Eintrag in Media. 
   */
  private void executeAddButton() {
    try {
      String catalog = mu_editTable.getCatalogName();
      if (catalog == null) {
        ZxErrorDialog.displaySimpleErrorMessage(this,
                "catalogUI.error.noDataLoaded");
      }
      Constructor<?> constructor = Class.forName("picdata." + catalog).getConstructor();
      PicCatalogItem newItem = (PicCatalogItem) constructor.newInstance();
      mu_editTable.addEntry(newItem);

    } catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
      ex.printStackTrace();
    }
  }

//   Methods to support JUnit testing
  
  /**
   * returns the buttons (to the Test Framework)
   * 
   * (0) -> add button, only valid for MediumTypes
   * (1) -> save button
   * (2) -> use camera catalog button
   * (3) -> use medium type catalog button
   * 
   * @return List of buttons
   */
  public List<JButton> getButtons() {
    if (mu_buttons == null) {
      mu_buttons = new ArrayList<>();
      mu_buttons.add(mu_addButton);
      mu_buttons.add(mb_saveButton);
      mu_buttons.add(mu_useCameraCatalogButton);
      mu_buttons.add(mu_useMediaTypeCatalogButton);
    }
    return mu_buttons;
  }
  
  /**
   * returns the table used to display or modify the selected catalog
   * @return the table from the ui
   */
  public JTable getTable() {
    return mu_editTable;
  }
  
  @Override
  public void actionPerformed(ActionEvent ae) {
    Object obj = ae.getSource();
    if (obj == mu_useCameraCatalogButton) {
      executeCameraCatalogButton();
    } else if (obj == mu_useMediaTypeCatalogButton) {
      executeMediaTypeCatalogButton();
    } else if (obj == mu_addButton) {
      executeAddButton();
    } else if (obj == mb_saveButton) {
      executeSaveButton();
    } else if (obj == mu_cancelButton) {
      dispose();
    }

  }
}
