/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package picdata;

import hib.PicJPAUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests the Searcher class
 *
 * @author rene
 * 10.12.23 initial
 *
 * @ToDo Cleanup inheritance of Test CLasses, from 2 parents to one"!
 */
public class SearcherCameraTest extends TestBaseClass {

  private PicJPAUtil jpaUtil = PicJPAUtil.getInstance();
  private static final String MANUFACTURER1 = "Manu1";
  private static final String MANUFACTURER2 = "Manu2";
  private static final String MANUFACTURER3 = "Manu3";
  private static final String[] MANUFACTURER = {
    MANUFACTURER1, MANUFACTURER2, MANUFACTURER3};

  public SearcherCameraTest() {
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
//    jpaUtil.deleteTableContent("Camera");
    sqlUtil.deleteTableData("CAMERA");
    fillDataForCameraSearch();
  }

  @AfterEach
  protected void tearDown() {
  }

  @Test
  protected void testSimpleCameraSearch() {
    Searcher searcher = new Searcher();
    Camera c = searcher.searchCameraByModelAndManufacturer("M2", MANUFACTURER2);
    assertNotNull(c, "Didn't find a camera");
    assertEquals(2, c.getId(), "Wrong ID of camera found");
  }
  
  /**
   * check correct answer, if entry was not found
   */
  @Test
  protected void testSimpleCameraNegativSearch() {
    Searcher searcher = new Searcher();
    Camera c = searcher.searchCameraByModelAndManufacturer("M1", MANUFACTURER2);
    assertNull(c, "Shouldn't find a camera");
  }
  
  /**
   * test to locate multiple searches with one searcher object
   */
  @Test
  protected void testMultipleCameraSearch() {
    Searcher searcher = new Searcher();
    Camera c = searcher.searchCameraByModelAndManufacturer("M2", MANUFACTURER2);
    assertNotNull(c, "Didn't find a camera");
    assertEquals(2, c.getId(), "Wrong ID of camera found");
    Camera c2 = searcher.searchCameraByModelAndManufacturer("M3", MANUFACTURER3);
    assertNotNull(c2, "Didn't find a camera");
    assertEquals(3, c2.getId(), "Wrong ID of camera found");
  }
  
  @Test
  protected void testDoubleCamera() {
    fillDataForCameraSearch(false);     // Add 3 more cameras with equal data
    Searcher searcher = new Searcher();
    Assertions.assertEquals(2*MANUFACTURER.length, countRecords("CAMERA"), "Wrong number of cameraObjects created");
    Camera c = searcher.searchCameraByModelAndManufacturer("M2", MANUFACTURER2);
    assertNotNull(c, "Didn't find a camera");
    assertEquals(2, c.getId(), "Wrong ID of camera found");
    Camera c2 = searcher.searchCameraByModelAndManufacturer("M3", MANUFACTURER3);
    assertNotNull(c2, "Didn't find a camera");
    assertEquals(3, c2.getId(), "Wrong ID of camera found");
    
  }
  
  private void fillDataForCameraSearch() {
    fillDataForCameraSearch(true);
  }
  /**
   * adds 3 cameras to the database and checks if the number of
   * created objects, if the boolean is set to true
   * 
   * @param check if true, check the number of generated objects
   */
  private void fillDataForCameraSearch(boolean check) {
    int modelno = 1;
    for (String manu : MANUFACTURER) {
      Camera camera = new Camera("M" + modelno++, manu);
      camera.update();
    }
    if (check) Assertions.assertEquals(MANUFACTURER.length, countRecords("CAMERA"), "Wrong number of cameraObjects created");
  }

}
