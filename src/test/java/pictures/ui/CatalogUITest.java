/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pictures.ui;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import picdata.Camera;
import picdata.MediumType;
import picdata.TestBaseClass;

/**
 * @author rene
 */
public class CatalogUITest extends TestBaseClass {

  static final String[] m_mediaTypes = {
    "CD", "CD-R", "DVD", "DVD+R", "DVD-R", "DVD-RW"
  };
  static final String[] m_cameraModels = {
    "Model1", "Model2", "Model3", "Model4"
  };

  public CatalogUITest() {
  }

  @BeforeAll
  public static void setUpClass() {
    setupDatabaseConnection();
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    cleanDatabase();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Tests weather the add button is enabled or not depending on the
   * selected catalog
   */
  @Test
  protected void switchCatalogtest() {
    CatalogUI ui = new CatalogUI();
    List<JButton> buttons = ui.getButtons();
    assertNotNull(buttons, "Any buttons found?");
    assertEquals(true, buttons.get(0).isEnabled(), "Start condition enabled");

    buttons.get(2).doClick();    // load cameraCatalog
    assertEquals(false, buttons.get(0).isEnabled(), "For cameras disabled");
    buttons.get(3).doClick();    // load cameraCatalog
    assertEquals(true, buttons.get(0).isEnabled(), "For mediatypes enabled");
  }

  /**
   * Test to add a remark to an existing camera via the user-interfacwe
   */
  @Test
  protected void updateCameraTest() {
    createCameras();
    CatalogUI ui = new CatalogUI();
    List<JButton> buttons = ui.getButtons();
    assertNotNull(buttons, "Any buttons found?");
    buttons.get(2).doClick();    // load cameraCatalog
    JTable cameraTable = ui.getTable(); // Grab the table from the ui
    int numCameras = cameraTable.getRowCount();
    assertEquals (m_cameraModels.length, numCameras, "Number of Cameras");
    assertEquals (3, cameraTable.getColumnCount(), "Number of Columns in Cameratable");
    
// get ID of first camera, update remark, save and check databasecontent
    String model = (String) cameraTable.getValueAt(1, 0);  // Model Name
    cameraTable.setValueAt("REMARK", 1, 2);
    buttons.get(1).doClick();       // Save Button
// Now check data
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createQuery("select c.id from Camera as c where model='"+model+"'");
      Long id = (Long)query.getSingleResult();
      assertNotNull (id, "Id found?");
      Camera camera = em.find(Camera.class, id);
      assertNotNull(camera, "Camera nor found");
      assertEquals("REMARK", camera.getRemark(), "Found in database");
    }
  }
  
  /**
   * test to add a new media type in the database
   */
  @Test
  protected void addMediaTypeTest() {
    log("Start test");
    createMediaTypes();
    CatalogUI ui = new CatalogUI();
    List<JButton> buttons = ui.getButtons();
    assertNotNull(buttons, "Any buttons found?");
    buttons.get(3).doClick();    // load mediaType Catalog
    JTable mediumTypeTable = ui.getTable(); // Grab the table from the ui
    int numMedia = mediumTypeTable.getRowCount();
    assertEquals (true, buttons.get(0).isEnabled(), "Is add media type enabled?");
    assertEquals (m_mediaTypes.length, numMedia, "Number of preloaded media");
    buttons.get(0).doClick();
    int numMediaNew = mediumTypeTable.getRowCount();
    assertEquals (m_mediaTypes.length+1, numMediaNew, "Number of preloaded media");
    mediumTypeTable.setValueAt("newMedium", numMediaNew-1, 0);
    mediumTypeTable.setValueAt("500", numMediaNew-1, 1);
    mediumTypeTable.setValueAt("Inserted Medium", numMediaNew-1, 2);
    buttons.get(1).doClick();
    countRecords("MEDIUM_TYPE", numMediaNew);
  }
  
  
  private void createMediaTypes() {
    for (String type : m_mediaTypes) {
      MediumType medium = new MediumType();
      medium.setDepiction(type);
      medium.update();
    }
    countRecords("MEDIUM_TYPE", m_mediaTypes.length);
  }

  private void createCameras() {
    for (String model : m_cameraModels) {
      Camera camera = new Camera(model, "Canon");
      camera.update();
    }
    countRecords("CAMERA", m_cameraModels.length);
  }

}
