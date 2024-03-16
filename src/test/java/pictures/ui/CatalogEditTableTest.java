/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pictures.ui;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import picdata.MediumType;
import picdata.TestBaseClass;
import rzx.ui.ZxResourceFactory;

/**
 * @author rene
 */
public class CatalogEditTableTest extends TestBaseClass {

  String[] m_mediaTypes = {
    "CD", "CD-R", "DVD", "DVD+R", "DVD-R", "DVD-RW"
  };

  public CatalogEditTableTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    setupDatabaseConnection();
    ZxResourceFactory rf = ZxResourceFactory.getInstance();
    rf.loadResorceFile("PicturesDatabase", Locale.GERMANY);
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    cleanDatabase();
    createMediaTypes();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of loading a catalog.
   */
  @Test
  public void testInitializeCatalog() {
    log("Starte Initialize Catalog Test");
    CatalogEditTable table = new CatalogEditTable();
    String labels[] = new String[]{
      "depiction", "capacity", "remark"
    };
    table.initializeCatalog("MediumType", labels);
    int numLabels = table.getColumnCount();
    assertEquals(labels.length, numLabels, "Number of labels");
    assertEquals(6, table.getRowCount(), "Number of catalog entries ok?");
  }

  /**
   * Test of setLabels method, of class CatalogEditTable.
   */
  @Test
  public void testSetLabels() {
    log("Starte Set Labels Test");
    CatalogEditTable table = new CatalogEditTable();
    String labels[] = new String[]{
      "name", "manufacturer", "remark"
    };
    table.setLabels(labels);
    int numLabels = table.getColumnCount();
    assertEquals(labels.length, numLabels, "Number of labels");
  }

  /**
   * Tests an update in the database. Create a set of MediaTypes, load
   * into the table, update one entry and save back.
   *
   */
  @Test
  protected void testUpdateEntry() {
    log("Starte update test");
    CatalogEditTable table = new CatalogEditTable();
    String labels[] = new String[]{
      "depiction", "capacity", "remark"
    };
    table.initializeCatalog("MediumType", labels);
    // Just checking if existing entries are ok.
    assertEquals(6, table.getRowCount(), "Number of catalog entries ok?");
    // need one entry and its ID for the tests,
    String newPattern = "T E S T";
    String depict = (String) table.getValueAt(1, 0);
    Long testId;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery(
              "select ID from MEDIUM_TYPE where DEPICTION='" + depict + "'");
      testId = (Long) query.getSingleResult();
      System.out.println("ID is " + testId);
    }
    table.setValueAt(newPattern, 1, 0);
    table.saveChanges();
// Now try to check, if changes are in the database
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      MediumType mt = em.find(MediumType.class, testId);
      assertNotNull(mt, "Updated entry not found");
      assertEquals(newPattern, mt.getDepiction(), "Updated value?");
      assertEquals(1, mt.getVersion(), "Neue Version in database");
    }
    countRecords("MEDIUM_TYPE", m_mediaTypes.length);
// now check, that nothng else was changed
    List<String> types = new ArrayList(Arrays.asList(m_mediaTypes));
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
        Query query = em.createQuery("SELECT m.depiction from MediumType as m");
        List<String> result = query.getResultList();
        assertEquals (m_mediaTypes.length, result.size(), "Length of resultset");
    // Check content of resultset
        for (String t: types) {
          if (result.contains(t)) result.remove(t);
        }
        assertEquals (1, result.size(), "Left is only the updated entry");
        assertEquals(newPattern, result.getFirst(), "Remaining entry is the updated");
    }
  }

  private void createMediaTypes() {
    for (String type : m_mediaTypes) {
      MediumType medium = new MediumType();
      medium.setDepiction(type);
      medium.update();
    }
    countRecords("MEDIUM_TYPE", m_mediaTypes.length);
  }
}
