/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package pictures.tools;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static pictures.tools.MediumLoadProcessorTestBase.initLogging;

/**
 * Class with test methods which incorporate mass tests in terms of folder or images
 *
 * @author rene
 */
public class MediumLoadProcessorMassTest extends MediumLoadProcessorTestBase {

  public MediumLoadProcessorMassTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    setupDatabaseConnection();
    initLogging();
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    cleanDatabase();
    setUpBaseData();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * 
   */
  @Test
  protected void massTest1() {
    for (TestControlRecord cr : controlRecords) {
      cleanDatabase();
      setUpBaseData();
      doStandardTest(
              cr.folder,
              cr.expectedFolderCount,
              cr.expectedPicDirMapCount,
              cr.expectedImageCount,
              cr.expectedCameraCount);
    }
  }

  List<TestControlRecord> controlRecords
          = new ArrayList<>(List.of(
                  new TestControlRecord("CD1B", 2, 12, 12, 2),
                  new TestControlRecord("CD1C", 4, 24, 24, 2),
                  new TestControlRecord("CD1D", 5, 29, 24, 2),
                  new TestControlRecord("CD2", 3, 20, 11, 2),
                  new TestControlRecord("CD3", 4, 27, 16, 4),
                  new TestControlRecord("CD4", 3, 28, 28, 2)
          )
          );

  record TestControlRecord(String folder,
          int expectedFolderCount,
          int expectedPicDirMapCount,
          int expectedImageCount,
          int expectedCameraCount) {

  }
}
