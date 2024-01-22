package picdata;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author rene
 * 18AUg2023/RZ: Java18 Upgrade, move to JPA and JUnit5. Renamed to
 * fit to the POJO it tests.
 * 
 * PictureMedium is the CD etc., which stores the pictures.
 * 
 */
public class PictureMediumTest extends TestBaseClass {
    
    public PictureMediumTest() {
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

    @Test
    public void testCreatePictureMedium() {
        MediumType m = new MediumType();
        m.setDepiction("TM");
        m.update();
        
        PictureMedium pm = new PictureMedium();
        pm.setCode(101);
        pm.setLabel("Test Label");
        pm.setStorageMedium(m);
        pm.update();
        assertNotNull(pm.getId());
    }
    
    /**
     * Tests the creation and update of a storage medium (Picture Medium)
     * without using the EntityManager in this routine. the EM shall be hidden
     * in the PictureMedium class itself.
     */
    @Test
    public void testCreateAndUpdateMedium() {
      MediumType mediumType = createMediumType(720);
      mediumType.update();
      PictureMedium pictureMedium = new PictureMedium();
      pictureMedium.setLabel("pm1-Label");
      pictureMedium.setTitle("pm1-Title");
      pictureMedium.setStorageMedium(mediumType);
      pictureMedium.update();
      countRecords("PICTURE_MEDIUM", 1);
      assertEquals (0, pictureMedium.getVersion());
      assertEquals(1, pictureMedium.getId(), "Initial ID");
      
      String newLabel = "pm1-Label-updated";
      pictureMedium.setLabel(newLabel);
      pictureMedium.update();
      countRecords("PICTURE_MEDIUM", 1);
//      assertEquals (1, pictureMedium.getVersion());     // needs reload to show updated version!
      assertEquals(1, pictureMedium.getId(), "ID after first update");

// Now check, what is in the database
      PictureMedium pmTest = PictureMedium.getById(1L);
      assertEquals(newLabel, pmTest.getLabel(), "CHeck for updated label");
      assertEquals (1, pmTest.getVersion(), "Version after update");
    }
}
