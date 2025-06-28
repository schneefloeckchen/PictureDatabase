/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pictures.tools;

import static bas.TestBase.TEST_DATA_FOLDER;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.jupiter.api.Assertions.assertEquals;
import picdata.PictureMedium;
import picdata.TestBaseClass;

/**
 *
 * @author rene
 */
public class MediumLoadProcessorTestBase extends TestBaseClass {

  protected static void initLogging() {
//                                      pictures.tools.MediumLoadProcessor
    Logger logger = Logger.getLogger(
            "pictures.tools.MediumLoadProcessor");
    logger.setLevel(Level.FINER);
  }

  protected int loadData(PictureMedium medium, 
          String testDataFolder) {
    File folder = new File(testDataFolder);
    MediumLoadProcessor instance = new MediumLoadProcessor();
    instance.setToTest();
    instance.setMedium(medium);
    return instance.process(folder);
  }

  /**
   * Standard process to test execution of the MediumLoardProcessor
   * The processor imports the folder data, and then the expected count of
   * imported/created objects in the database is examinated
   *
   * @param folder folder to import without the path to the folder and the slashes
   * @param expectedFolderCount
   * @param expectedImageCount
   * @param expectedPicDirMapCount
   * @param expectedCameraCount
   */
  protected void doStandardTest(String folder,
          int expectedFolderCount,
          int expectedPicDirMapCount,
          int expectedImageCount,
          int expectedCameraCount) {
    assertEquals(1, loadData(m_picMedium1,
            TEST_DATA_FOLDER + "/"+folder+"/"), "Loading "
                    +folder);
    countDirectory(expectedFolderCount, folder);
    countPicDirMap(expectedPicDirMapCount, folder);
    countDigiPicture(expectedImageCount, folder);
    countCamera(expectedCameraCount, folder);
  }

}
