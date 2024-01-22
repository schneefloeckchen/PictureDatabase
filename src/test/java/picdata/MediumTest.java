package picdata;

import bas.SQLUtil;
import bas.TestBase;
import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rzx.ui.ZxComboBoxEntry;

/**
 * Some tests to test the MediumType and PictureMedium class. Some historic reason for this
 * additional test class.
 * 
 * @author rene
 */
public class MediumTest extends TestBase {

  public MediumTest() {
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
   * creates a 
   */
  @Test
  public void testCreateMedia() {
    String mediaName = "Test";
    MediumType m = new MediumType();
    assertEquals(m.getId(), Long.valueOf(-1));
    m.setDepiction(mediaName);
    m.update();
    assertNotNull(m.getId());
  }

  @Test
  public void testGetkeyValuePairs() {
    int anzMedium = 20;
    EntityManager em = PicJPAUtil.getInstance().createEntityManager();
    for (int i = 0; i < anzMedium; i++) {
      PictureMedium medium = new PictureMedium();
      medium.setTitle("TITEL" + i);
      medium.update();
    }
    SQLUtil util = new SQLUtil();
    assertEquals(anzMedium, util.countElementsInTable("PICTURE_MEDIUM"), "Correct number of generated medium");
    PictureMedium worker = new PictureMedium();
    List<ZxComboBoxEntry> pairs = worker.getKeyValuePairs();
    assertEquals(anzMedium, pairs.size());
//
    pairs.forEach(entry -> {
      int id = entry.id;
      assertEquals(String.format("TITEL%d", id - 1), entry.name);
    });
  }
  
  @Test
  public void testGetNewCode() {
    int anzMedium = 10;
    EntityManager em = PicJPAUtil.getInstance().createEntityManager();
    int mediumCode = 0;    // Auch maximalwert!
    for (int m=0; m<anzMedium; m++) {
      PictureMedium pm = new PictureMedium();
      pm.setTitle("MEDIUM"+m);
      mediumCode = 100+m;
      pm.setCode(mediumCode);
      pm.update();
    }
    SQLUtil util = new SQLUtil();
    assertEquals(anzMedium, util.countElementsInTable("PICTURE_MEDIUM"), "Correct number of generated medium");
    int newCode = PictureMedium.getNewCode();
    assertEquals(mediumCode+1, newCode, "New code for storage medium ok?");
  }
}
