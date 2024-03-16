/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pictures.tools;

import java.io.File;
import java.util.List;
import org.junit.AfterClass;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.Searcher;
import picdata.TestBaseClass;
import rzx.ui.ZxLogPanel;

/**
 * Testet den DirectoryRemoveProzessor. Unklar, ob der ueberhaupt benotigt.
 * Daher nur kleiner Test
 * 
 * Spaeter bitte noch Test, bei denen in den Verzeichnissen Duplikate vorkommen
 *
 * @author rene
 */
public class DirectoryRemoveProcessorTest extends TestBaseClass {

  public DirectoryRemoveProcessorTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    System.out.println("Setting up Database Connection");
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
   * Einfacher Test mit Verzeichnis CD2, in dem 2 Unterverzeichnisse,
   * ohne duplikate.
   * 
   */
  @Test
  public void testSimple() {
    System.out.println("Test Simple");
    MediumLoadProcessor processor = new MediumLoadProcessor();
    PictureMedium medium = new PictureMedium();
    medium.update();
    processor.setToTest();
    processor.setMedium(medium);
    processor.process(new File(TEST_DATA_FOLDER+"/CD2"));
    countMedium(1);
    countDirectory(3);
    countDigiPicture(11);
    DirectoryRemoveProcessor removeProcessor = new DirectoryRemoveProcessor();
    Searcher searcher = new Searcher();
    List<PicDirectory> dirs = searcher.searchPictureDirectory("TestVerzeichnis");
    assertEquals (1, dirs.size(), "Just one diretory");
    PicDirectory dirToRemove = dirs.getFirst();
    removeProcessor.load(dirToRemove.getId(), null);
    removeProcessor.delete();
    countDirectory(2);
    countDigiPicture(10);
    
    
  }

 }
