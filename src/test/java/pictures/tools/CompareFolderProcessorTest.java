/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */

package pictures.tools;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
//import picdata.TestBaseClass;
import static bas.TestBase.TEST_DATA_FOLDER;

/**
 * Tests the CompareFolderProcessor. Doubletten im Filesystem, kein
 * compare mit der Datenbank.
 * 
 * Use the same testfiles as the LoadProcessorTests
 * 
 * CD 4: 1 Duplicate P1011617.JPG, 27 unique files.
 * 
 * @author rene
 */
public class CompareFolderProcessorTest  {
    private Logger m_logger = null;
    public CompareFolderProcessorTest() {
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
      m_logger = Logger.getLogger
        ("pictures.tools.CompareFolderProcessor");
      Logger.getLogger("").getHandlers()[0].setLevel(Level.FINEST);
      m_logger.setLevel(Level.FINEST);
      m_logger.fine("Logger auf FINEST gesetzt");
    }

    @AfterEach
    public void tearDown() {
    }

      /**
   * Test of load method, of class CompareFolderProcessor.
   */
  @Test
  public void testLoad() {
    System.out.println("load");
    File folder = new File(TEST_DATA_FOLDER+"/CD7/Landschaftsbau2021/");
    CompareFolderProcessor processor = new CompareFolderProcessor();
    int expResult = 28;
    int result = processor.load(folder);
    assertEquals(expResult, result,
            "Number of files loaded as returned from the processor");
    // TODO review the generated test code and remove the default call to fail.
  }

  /**
   * Test of load method, of class CompareFolderProcessor. With Recursion into
   * subfolder
   */
  @Test
  public void testLoad2() {
    System.out.println("load");
    File folder = new File(TEST_DATA_FOLDER+"/CD7/");
    CompareFolderProcessor processor = new CompareFolderProcessor();
    int expResult = 28;
    int result = processor.load(folder);
    assertEquals(expResult, result, "Number of files in tree loaded");
  }

  /**
   * Test of load method with duplicates in stotage images.
   * of class CompareFolderProcessor. With Recursion into subfolder
   * Tests if duplicates are found and we still get the correct
   * number of images in storage,
   */
  @Test
  public void testLoadWithDuplicats1() {
    System.out.println("Testing duplicatesd");
    File folder = new File(TEST_DATA_FOLDER+"/CD8/");
    CompareFolderProcessor processor = new CompareFolderProcessor();
    int expResult = 28;
    int result = processor.load(folder);
    assertEquals(expResult, result, "Number of files in tree loaded");
    // Did we find the duplicates?
    assertEquals(10, processor.getDuplicateTableModel().getRowCount()
    , "Number of duplicates: ");
  }

  /**
   * Test of load method with duplicates in stotage images.
   * of class CompareFolderProcessor. With Recursion into subfolder
   * Tests if duplicates are found and we still get the correct
   * number of images in storage,
   * in CD8.1 are 2 doublettes with same filename, but different
   * taken_milis, so count as different images.
   */
  @Test
  public void testLoadWithDuplicats2() {
    System.out.println(
        "Testing duplicates incl. same filename but different taken time");
    CompareFolderProcessor processor = new CompareFolderProcessor();
    File folder = new File(TEST_DATA_FOLDER+"/CD8.1/");
    int expResult = 30;
    int result = processor.load(folder);
    assertEquals(expResult, result,
            "Number of files in tree loaded");
    // Did we find the duplicates?
    assertEquals(13, processor.getDuplicateTableModel().getRowCount(),
            "Number of duplicates: ");
  }

  /**
   * Test of checkFiles method, of class CompareFolderProcessor.
   */
  @Test
  public void testCheckFiles() {
    System.out.println("checkFiles");
    File folder = new File(TEST_DATA_FOLDER+"/CD7/");
    CompareFolderProcessor processor = new CompareFolderProcessor();
    int expResult = 28;
    int result = processor.load(folder);
    assertEquals(expResult, result, "Number of files in tree loaded");
    
    File checkFolder = new File(TEST_DATA_FOLDER+"/CD7.1/");
    expResult = 0;
    processor.checkFiles(checkFolder);
    TableModel missingFiles = processor.getMissingFilesTableModel();
    assertEquals(expResult,
        missingFiles.getRowCount(), "Missing files should be: ");
    
    TableModel foundFiles = processor.getFoundFilesTableModel();
    assertEquals(28, foundFiles.getRowCount(), "Found files: ");
  }


    /**
     * Loads CD1B (12 unique Files) and checks with CD1 (28 files incl.
     * CD1B content
     * So we shall count 12 found and 16 missing
     */
    @Test
    protected void testCD1() {
      m_logger.entering(getClass().getName(), "testCD1");
      CompareFolderProcessor processor = new CompareFolderProcessor();
      File testFolder = new File(TEST_DATA_FOLDER+"/CD1");
      File checkFolder = new File(TEST_DATA_FOLDER+"/CD1B");
      processor.load(checkFolder);
      int duplicates = processor.getDuplicateTableModel().getRowCount();
      assertEquals (0, duplicates, "Duplicates in initial set");
//
      processor.checkFiles(testFolder);
      TableModel foundFilesModel = processor.getFoundFilesTableModel();
      int filesFound = foundFilesModel.getRowCount();
      assertEquals(12, filesFound, "Anzahl Files found in set");
      TableModel missingFilesModel = processor.getMissingFilesTableModel();
      int missingFiles = missingFilesModel.getRowCount();
      assertEquals(16, missingFiles, "Anzahl Missing Files");  
    }
    
    /**
     * as testCD1, but first folder is CD1C, with 24 files in 3 subfolders,
     * check folder is CD1 again.
     * So, 24 found and 4 missing
     */
    @Test
    protected void testCD1C() {
      m_logger.entering(getClass().getName(), "testCD1C");
      CompareFolderProcessor processor = new CompareFolderProcessor();
      File testFolder = new File(TEST_DATA_FOLDER+"/CD1");
      File checkFolder = new File(TEST_DATA_FOLDER+"/CD1C");
      processor.load(checkFolder);
      int duplicates = processor.getDuplicateTableModel().getRowCount();
      assertEquals (0, duplicates, "Duplicates in initial set");
//
      processor.checkFiles(testFolder);
      TableModel foundFilesModel = processor.getFoundFilesTableModel();
      int filesFound = foundFilesModel.getRowCount();
      assertEquals(24, filesFound, "Anzahl Files found in set");
      TableModel missingFilesModel = processor.getMissingFilesTableModel();
      int missingFiles = missingFilesModel.getRowCount();
      assertEquals(4, missingFiles, "Anzahl Missing Files");  
      
    }
    /**
     * as testCD1D, but first folder is CD1D, with 30 files in 4 subfolders,
     but 5 duplicate. check folder is CD1 again.
     * So, 24 found and 4 missing, same result as above
     */
    @Test
    protected void testCD1D() {
      m_logger.entering(getClass().getName(), "testCD1C");
      CompareFolderProcessor processor = new CompareFolderProcessor();
      File testFolder = new File(TEST_DATA_FOLDER+"/CD1");
      File checkFolder = new File(TEST_DATA_FOLDER+"/CD1D");
      processor.load(checkFolder);
      int duplicates = processor.getDuplicateTableModel().getRowCount();
      assertEquals (5, duplicates, "Duplicates in initial set");
//
      processor.checkFiles(testFolder);
      TableModel foundFilesModel = processor.getFoundFilesTableModel();
      int filesFound = foundFilesModel.getRowCount();
      assertEquals(24, filesFound, "Anzahl Files found in set");
      TableModel missingFilesModel = processor.getMissingFilesTableModel();
      int missingFiles = missingFilesModel.getRowCount();
      assertEquals(4, missingFiles, "Anzahl Missing Files");  
    }

    /**
     * test using CD4, one image is douplicate, bus the 2 files
     * have different taken millis.
     */
    @Test
    protected void testCD4() {
      m_logger.fine("Running Test CD4");
      CompareFolderProcessor processor = new CompareFolderProcessor();
      File testFolder = new File(TEST_DATA_FOLDER+"/CD4");
      processor.load(testFolder);
      int duplicates = processor.getDuplicateTableModel().getRowCount();
      // Next line is ok, as the images P1011617.JPG (twice in CD4 Data
      // have different taken dates.
      assertEquals (0, duplicates, "Duplicates in initial set");
      processor.checkFiles(testFolder);
      TableModel foundFilesModel = processor.getFoundFilesTableModel();
      int filesFound = foundFilesModel.getRowCount();
      assertEquals(28, filesFound, "Anzahl Files found in set");
      TableModel duplicateFilesModel = processor.getDuplicateTableModel();
      int duplicateFiles = duplicateFilesModel.getRowCount();
      assertEquals(0, duplicateFiles, "Anzahl Missing Files");  
    }
    
    
}