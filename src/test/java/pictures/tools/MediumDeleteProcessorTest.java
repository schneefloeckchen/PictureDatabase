/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pictures.tools;

import java.io.File;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import picdata.PictureMedium;
import picdata.TestBaseClass;
import rzx.ui.ZxLogPanel;

/**
 * @author rene
 */
public class MediumDeleteProcessorTest extends TestBaseClass {

  public MediumDeleteProcessorTest() {
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
   * test loads from one CD and deletes the cd later checking if
   * all data are gone.
   */
  @Test
  protected void testSimple() {
    MediumLoadProcessor loadProcessor = new MediumLoadProcessor();
    PictureMedium medium = new PictureMedium();
    medium.update();
    loadProcessor.setMedium(medium);
    loadProcessor.setToTest();
    loadProcessor.process(new File(TEST_DATA_FOLDER+"/CD1/"));
    countDigiPicture(28);
    countMedium(1);
    countDirectory(2);
    countRecords("PIC_DIR_MAP", 28);
// und nu medium loeschen
    MediumDeleteProcessor deleteProcessor = new MediumDeleteProcessor();
    deleteProcessor.load(medium.getCode(), null);
    deleteProcessor.delete();
    countDigiPicture(0);
    countMedium(0);
    countDirectory(0);
    countRecords("PIC_DIR_MAP", 0);
  }
}
