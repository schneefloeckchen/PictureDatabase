package picdata;

import bas.SQLUtil;
import bas.TestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.TestInstance;

/**
 *
 * @author rene
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CameraTest extends TestBase {
     protected SQLUtil util ;

    public CameraTest() {
        log("CameraTest instantiated");
    }
    
    @BeforeAll
    public void setUpClass() {
      System.out.println ("Cameratest setupClass");
      setupDatabaseConnection();
      util = new SQLUtil();   // needs to be executed after establishing
      // the database connection through JPA because it reads data from the
      // persistence context
    }
    
    @AfterAll
    public void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
      util.deleteTableData("CAMERA");
    }
    
    @AfterEach
    public void tearDown() {
    }

    @Test
    public void testCreateCamera() {
        String testModel = "test Model";
        Camera c = new Camera();
        c.setModel(testModel);
        Long idn = c.getId();
        assertEquals(Long.valueOf(-1), idn, "ID from unsaved camera must be -1");
        c.update();
        Long id = c.getId();
        assertNotNull(id);
        Camera c2 = Camera.getById(id);
        assertNotNull(c2, "Is the Camera Object loaded");
        assertEquals(testModel, c2.getModel(), "Model name correct stored");
    }
    
    /**
     * tests if the version number is increased
     */
    @Test
    public void testVersionHandling() {
      SQLUtil util = new SQLUtil();
      String tm1 = "Test Model 1";
      String tm2 = "Test Model 2";
      String tm3 = "Test Model 3";
      Camera camera = new Camera(tm1, "Nikon");
      camera.update();
      assertEquals(0, camera.getVersion());
      
      camera.setHersteller("Canon");
      camera.update();
      assertEquals(0, camera.getVersion());
      
      Camera cameraTest = Camera.getById(1L);
      assertEquals(1, cameraTest.getVersion());
      
      cameraTest.setHersteller("HP");
      cameraTest.update();
      assertEquals(1, cameraTest.getVersion());

      cameraTest = Camera.getById(1L);
      assertEquals(2, cameraTest.getVersion());
      assertEquals(1, util.countElementsInTable("CAMERA"), "Number of camera objects in database");
      
      cameraTest.setHersteller("NIKON");
      cameraTest.update();
//      int newVersion = cameraTest.version++;
//      log("New Version is "+newVersion);
//      cameraTest.setVersion(newVersion);
//      cameraTest.setHersteller("Minolta");
//      cameraTest.update();
      cameraTest = Camera.getById(1L);
      assertEquals(3, cameraTest.getVersion());
      assertEquals(1, util.countElementsInTable("CAMERA"), "Number of camera objects in database");
    }

    @Test
    public void testCreateMultipleCamera() {
      int anzahlCameras = 20;
      for (int c=0; c<anzahlCameras; c++) {
        Camera camera = new Camera("M"+c, "Hersteller"+2*c);
        camera.update();
      }
      SQLUtil util = new SQLUtil();
      assertEquals(anzahlCameras, util.countElementsInTable("CAMERA"), "expected number of Cameras in database");
//
//   Check loadById and correct model
      Camera testCamera = Camera.getById(2);
      log("Camera id: "+testCamera.getId() + "   - Model is: "+testCamera.getModel());
      assertEquals("M1", testCamera.getModel()," Model ok?");
    }
    
    @Test
    public void testGetEntry() {
      Camera c = new Camera();
      c.setModel("MODEL");
      c.setHersteller("HERSTELLER");
      c.setRemark("REMARK");
      c.update();
      assertEquals("MODEL", c.getModel());
      assertEquals("HERSTELLER", c.getHersteller());
      assertEquals("REMARK", c.getRemark());
      assertEquals(1L, c.getId());
    }
    
    @Test
    public void testSetEntry() {
      Camera c = new Camera();
//      c.update();
      c.setEntry("MODEL", 0);
      c.setEntry("HERSTELLER", 1);
      c.setEntry("REMARK", 2);
      c.update();
      Camera testCamera = Camera.getById(1L);
      assertNotNull(testCamera);
      assertEquals("MODEL", testCamera.getModel());
      assertEquals("HERSTELLER", testCamera.getHersteller());
      assertEquals("REMARK", testCamera.getRemark());
    }
}
