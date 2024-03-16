/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package picdata;

import java.util.List;
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
public class SearcherTest extends TestBaseClass {

  private static final String MANU1 = "Kodak";
  private static final String MANU2 = "AGFA";
  private static final String MANU3 = "KODAK";
  private static final String MODEL1 = "AG1";
  private static final String MODEL2 = "AG2";
  private static final String MODEL3 = "FinePic";
  private static final String FILE1 = "P1.JPG";
  private static final String FILE2 = "P2.JPG";
//  private static final String FILE3 = "p1.jpg";  database is NOT case sensitive
  private static final int MILLIS1 = 10000;
  private static final int MILLIS2 = 20000;
  private static final int MILLIS3 = 10001;

  private static final String MEDIA_TITLE1 = "Title 1";
  private static final String MEDIA_TITLE10 = "Title 10";
  private static final String MEDIA_TITLE2 = "Title 2";
  private static final String MEDIA_TITLE3 = "Title 3";
  private static final String MEDIA_TITLE4 = "Title 4";
  private static final String MEDIA_TITLE5 = "Title 5";
  private static final String MEDIA_TITLE6 = "Title 6";

  private static final String MEDIA_LABEL1 = "Label 1";
  private static final String MEDIA_LABEL10 = "Label 10";
  private static final String MEDIA_LABEL2 = "Label 2";
  private static final String MEDIA_LABEL3 = "Label 3";
  private static final String MEDIA_LABEL4 = "Label 4";
  private static final String MEDIA_LABEL5 = "Label 5";
  private static final String MEDIA_LABEL6 = "Label 6";

  private static final String MEDIA_CONTENT1 = "Content 1";
  private static final String MEDIA_CONTENT2 = "Content 2";
  private static final String MEDIA_CONTENT3 = "Content 3";
  private static final String MEDIA_CONTENT4 = "Content 4";
  private static final String MEDIA_CONTENT5 = "Content 5";
  private static final String MEDIA_CONTENT6 = "Content 6";
  private static final String MEDIA_CONTENT7 = "Content 7";
  private static final String MEDIA_CONTENT8 = "Content 8";

  private static final String DIR1 = "Urlaub 1";
  private static final String DIR2 = "Urlaub 2";
  private static final String DIR3 = "Urlaub 3";
  private static final String DIR4 = "Urlaub 4";
  private static final String DIR5 = "Angelurlaub";


  public SearcherTest() {
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
  protected void testFindCameraByModelAndManufacturer() {
    createCamera(MANU1, MODEL1);
    createCamera(MANU2, MODEL2);
    createCamera(MANU3, MODEL3);
    Camera c1 = createCamera(MANU1, MODEL2);
    Camera c2 = createCamera(MANU1, MODEL3);
    Searcher searcher = new Searcher();
    Camera cTest = searcher.searchCameraByModelAndManufacturer(MODEL2, MANU1);
    assertEquals(c1.getId(), cTest.getId(), "Kamera gefunden");
  }

  /**
   * EInfacher test fuer searchPictureByNameAndMilis, der
   * Test fuer den MediumLoadProzessor nutzt diese Funktion auch,
   */
  @Test
  protected void testSearchPictureByNameAndMilisSimple() {
    createCamera(MANU1, MODEL1);
    Camera camera2 = createCamera(MANU2, MODEL2);
    createDigiPicture(camera2, FILE1, MILLIS1);
    createDigiPicture(camera2, FILE2, MILLIS2);
    createDigiPicture(camera2, FILE1, MILLIS3);
    createDigiPicture(camera2, FILE2, MILLIS1);

    Searcher searcher = new Searcher();
    DigiPicture picSearch = searcher.searchPictureByNameAndMilis(FILE1, MILLIS1);
    Assertions.assertNotNull(picSearch);
    picSearch = searcher.searchPictureByNameAndMilis(FILE2, MILLIS2);
    assertNotNull(picSearch);
    picSearch = searcher.searchPictureByNameAndMilis(FILE1, MILLIS2);
    Assertions.assertNull(picSearch);

    log(" gelaufen");
  }

  /**
   * just a setup function for a couple of test methods
   */
  private void createMediaForTest() {
    createPictureMedium(MEDIA_TITLE10, MEDIA_LABEL10, MEDIA_CONTENT1);

    createPictureMedium(MEDIA_TITLE2, MEDIA_LABEL2, MEDIA_CONTENT2);
    createPictureMedium(MEDIA_TITLE2, MEDIA_LABEL1, MEDIA_CONTENT2);
    createPictureMedium(MEDIA_TITLE2, MEDIA_LABEL3, MEDIA_CONTENT2);
    createPictureMedium(MEDIA_TITLE2, MEDIA_LABEL4, MEDIA_CONTENT2);
    createPictureMedium(MEDIA_TITLE2, MEDIA_LABEL5, MEDIA_CONTENT2);
    
    createPictureMedium(MEDIA_TITLE1, MEDIA_LABEL3, MEDIA_CONTENT3);
    createPictureMedium(MEDIA_TITLE2, MEDIA_LABEL3, MEDIA_CONTENT3);
    createPictureMedium(MEDIA_TITLE3, MEDIA_LABEL3, MEDIA_CONTENT3);
    createPictureMedium(MEDIA_TITLE4, MEDIA_LABEL3, MEDIA_CONTENT3);
    createPictureMedium(MEDIA_TITLE5, MEDIA_LABEL3, MEDIA_CONTENT3);

    createPictureMedium(MEDIA_TITLE4, MEDIA_LABEL4, MEDIA_CONTENT4);
    createPictureMedium(MEDIA_TITLE5, MEDIA_LABEL5, MEDIA_CONTENT5);

  }

  @Test
  protected void testSeachPictureMediumByCode() {
    createPictureMedium(101, MEDIA_TITLE1, MEDIA_LABEL1);
    createPictureMedium(102, MEDIA_TITLE2, MEDIA_LABEL2);
    createPictureMedium(103, MEDIA_TITLE3, MEDIA_LABEL3);
    createPictureMedium(104, MEDIA_TITLE4, MEDIA_LABEL4);
    createPictureMedium(105, MEDIA_TITLE5, MEDIA_LABEL5);
    Searcher searcher = new Searcher();
    PictureMedium medium = searcher.searchPictureMediumByCode(102);
    assertNotNull(medium, "Medium gefunden?");
    assertEquals(MEDIA_TITLE2, medium.getTitle(), "Titel korrekt?");
    assertEquals(MEDIA_LABEL2, medium.getLabel(), "Label korrekt?");
// Now check code not found
    medium = searcher.searchPictureMediumByCode(110);
    assertNull(medium, "Should not be found");
  }
  
  @Test
  protected void testSearchPictureMediumsByTitleSimple() {
    createMediaForTest();
    Searcher searcher = new Searcher();
    List<PictureMedium> mediums
            = searcher.searchPictureMediumsByTitle(MEDIA_TITLE10);
    assertEquals(1, mediums.size(), "Number of hits?");
    PictureMedium m = mediums.getFirst();
    assertEquals(MEDIA_LABEL10, m.getLabel(), "Label ok?");
    assertEquals(MEDIA_CONTENT1, m.getContent(), "Content ok?");
    log("gelaufen");
  }
  
  @Test
  protected void testSearchPictureMediumsByTitle() {
    createMediaForTest();
    Searcher searcher = new Searcher();
    List<PictureMedium> mediums
            = searcher.searchPictureMediumsByTitle(MEDIA_TITLE2);
    assertEquals(6, mediums.size(), "Number of hits?");
    assertEquals (true, isLabelInSetOfMedia(mediums, MEDIA_LABEL1));
    assertEquals (true, isLabelInSetOfMedia(mediums, MEDIA_LABEL2));
    log("medi size is now "+mediums.size());
    assertEquals (true, isLabelInSetOfMedia(mediums, MEDIA_LABEL3));
    assertEquals (true, isLabelInSetOfMedia(mediums, MEDIA_LABEL3));
    assertEquals (true, isLabelInSetOfMedia(mediums, MEDIA_LABEL4));
    assertEquals (true, isLabelInSetOfMedia(mediums, MEDIA_LABEL5));
    assertEquals(0, mediums.size(), "All entries removed?");
//    PictureMedium m = mediums.getFirst();
//    assertEquals(MEDIA_LABEL1, m.getLabel(), "Label ok?");
//    assertEquals(MEDIA_CONTENT1, m.getContent(), "Content ok?");
    log("gelaufen");
  }
  
  @Test
  protected void testSearchPictureMediumsByLabelSimple() {
    createMediaForTest();
    Searcher searcher = new Searcher();
    List<PictureMedium> mediums
            = searcher.searchPictureMediumsByLabel(MEDIA_LABEL10);
    assertEquals(1, mediums.size(), "Number of hits?");
    PictureMedium m = mediums.getFirst();
    assertEquals(MEDIA_TITLE10, m.getTitle(), "Titel ok?");
    assertEquals(MEDIA_CONTENT1, m.getContent(), "Content ok?");
    log("gelaufen");
  }
  
  @Test
  protected void testSearchPictureMediumsByLabel() {
    createMediaForTest();
    Searcher searcher = new Searcher();
    List<PictureMedium> mediums
            = searcher.searchPictureMediumsByLabel(MEDIA_LABEL3);
    assertEquals(6, mediums.size(), "Number of hits?");
    assertEquals (true, isTitleInSetOfMedia(mediums, MEDIA_TITLE1));
    assertEquals (true, isTitleInSetOfMedia(mediums, MEDIA_TITLE2));
    assertEquals (true, isTitleInSetOfMedia(mediums, MEDIA_TITLE2));
    log("medi size is now "+mediums.size());
    assertEquals (true, isTitleInSetOfMedia(mediums, MEDIA_TITLE3));
    assertEquals (true, isTitleInSetOfMedia(mediums, MEDIA_TITLE4));
    assertEquals (true, isTitleInSetOfMedia(mediums, MEDIA_TITLE5));
    assertEquals(0, mediums.size(), "All entries removed?");
//    PictureMedium m = mediums.getFirst();
//    assertEquals(MEDIA_LABEL1, m.getLabel(), "Label ok?");
//    assertEquals(MEDIA_CONTENT1, m.getContent(), "Content ok?");
    log("gelaufen");
  }
  
  @Test
  protected void testSearchPictureMediumsByContentSimple() {
    createMediaForTest();
    Searcher searcher = new Searcher();
    List<PictureMedium> mediums
            = searcher.searchPictureMediumsByContent(MEDIA_CONTENT5);
    assertEquals(1, mediums.size(), "Number of hits?");
    PictureMedium m = mediums.getFirst();
    assertEquals(MEDIA_TITLE5, m.getTitle(), "Titel ok?");
    assertEquals(MEDIA_LABEL5, m.getLabel(), "Label ok?");
    log("gelaufen");
  }
  

  /**
   * checks is a string an be found as label in the set, if yes it
   * removes this PictureMedium and returns true. If not, a false is returned.
   * 
   * @param media
   * @param label
   * @return 
   */
  private boolean isLabelInSetOfMedia(List<PictureMedium>media, String label) {
    for (PictureMedium m:media) {
      if (label.equalsIgnoreCase(m.getLabel())) {
        media.remove(m);
        return true;
      }
    }
    return false;
  }
  private boolean isTitleInSetOfMedia(List<PictureMedium>media, String title) {
    for (PictureMedium m:media) {
      if (title.equalsIgnoreCase(m.getTitle())) {
        media.remove(m);
        return true;
      }
    }
    return false;
  }
  
  @Test
  protected void testSearchPictureDirectory() {
    createPicDirectory(DIR1);
    createPicDirectory(DIR2);
    createPicDirectory(DIR3);
    createPicDirectory(DIR4);
    createPicDirectory(DIR5);
    createPicDirectory(DIR5);
    createPicDirectory(DIR5);
    
    Searcher searcher = new Searcher();

    List<PicDirectory> dirs = searcher.searchPictureDirectory(DIR1);
    assertNotNull(dirs, "Bild Verzeichnisse gefunden?");
    assertEquals(1, dirs.size(), "Ein Verzeichnis?");

    dirs = searcher.searchPictureDirectory(DIR5);
    assertNotNull(dirs, "Bild Verzeichnisse gefunden?");
    assertEquals(3, dirs.size(), "3Anzahl Verzeichnisse ok?");
    
    dirs = searcher.searchPictureDirectory("Urlaub%");
    assertNotNull(dirs, "Bild Verzeichnisse gefunden? mit wildcard");
    assertEquals(4, dirs.size(), "Anzahl ok?");

    dirs = searcher.searchPictureDirectory("%");
    assertNotNull(dirs, "Bild Verzeichnisse gefunden?");
    assertEquals(7, dirs.size(), "Ein Verzeichnis?");

    dirs = searcher.searchPictureDirectory("X%");
    assertNotNull(dirs, "Kein Verzeichnisse gefunden?");
    assertEquals(0, dirs.size(), "Ein Verzeichnis?");
    

  }
}
