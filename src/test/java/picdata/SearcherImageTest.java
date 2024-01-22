/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package picdata;

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
 *
 * @author rene
 */
public class SearcherImageTest extends TestBaseClass {

  private static final String FILE_NAME_TO_SEARCH = "IMAGE99.JPG";
  private static final long FILE_TAKEN_MILIS = 234567;
  private long idForSearching;

  public SearcherImageTest() {
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
    sqlUtil.deleteTableData("DIGI_PICTURE");
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Tests if an image is there
   */
  @Test
  protected void testSearchImageSimple() {
    createImageEntries();
    Searcher searcher = new Searcher();
    DigiPicture pic = searcher.searchPictureByNameAndMilis(
        FILE_NAME_TO_SEARCH, FILE_TAKEN_MILIS);
    assertNotNull(pic);
    assertEquals(idForSearching, pic.getId(), "Wrong id of picture found");
  }

  /**
   * tests the failure of searching for an image
   * due to mismatch takenMilis
   */
  @Test
  protected void testSearchNotFound() {
    createImageEntries();
    Searcher searcher = new Searcher();
    DigiPicture pic = searcher.searchPictureByNameAndMilis(
        FILE_NAME_TO_SEARCH, FILE_TAKEN_MILIS + 1);
    assertNull(pic);
  }
  
  /**
   * same as above, but error in filename
   */
  @Test
  protected void testSearchNotFound2() {
    createImageEntries();
    Searcher searcher = new Searcher();
    DigiPicture pic = searcher.searchPictureByNameAndMilis(
        "X"+FILE_NAME_TO_SEARCH, FILE_TAKEN_MILIS);
    assertNull(pic);
  }

  private void createImageEntries() {
    createImage("IMG1.JPG", 123456);
    createImage("IMG2.JPG", 123457);
    createImage("IMG3.JPG", 123458);
    createImage("IMG4.JPG", 123459);
    createImage("IMG5.JPG", 123460);
    idForSearching = createImage(FILE_NAME_TO_SEARCH, FILE_TAKEN_MILIS);
    countRecords("DIGI_PICTURE", 6);
    Assertions.assertEquals(6, idForSearching, "ID of image to search");
  }

  private long createImage(String name, long takenMilis) {
    DigiPicture pic = new DigiPicture();
    pic.setCamera(m_camera1);
    pic.setFileName(name);
    pic.setPictureTakenMilis(takenMilis);
    pic.update();
    return pic.getId();
  }

}
