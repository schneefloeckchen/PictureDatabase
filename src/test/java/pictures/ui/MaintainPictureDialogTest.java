/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pictures.ui;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.io.File;
import java.util.List;
import javax.swing.JButton;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import picdata.DigiPicture;
import picdata.PictureMedium;
import picdata.Searcher;
import picdata.TestBaseClass;
import pictures.tools.MediumLoadProcessor;

/**
 * @author rene
 */
public class MaintainPictureDialogTest extends TestBaseClass {
  
  private Searcher m_searcher = null;
  
  public MaintainPictureDialogTest() {
  }
  
  @BeforeAll
  public static void setUpClass() {
    System.out.println("Setting up database connection for MaintainPictureDialog test");
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
   * Test of loadById method, of class MaintainPictureDialog.
   * However, first need to load a DVD into the database
   */
  @Test
  public void testLoadById() {
    log();
    MediumLoadProcessor loadProcessor = new MediumLoadProcessor();
    loadProcessor.setToTest();
    PictureMedium medium = new PictureMedium();
    medium.update();
    loadProcessor.setMedium(medium);
    int countRoots = loadProcessor.process(new File(TEST_DATA_FOLDER + "/CD1/"));
    assertEquals(1, countRoots,
            "Just checking if CD is correctlyloaded, shall have just one root");
    countDigiPicture(28);
    long idOfTestPic = -1;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery("SELECT ID FROM DIGI_PICTURE");
      List<Long> ids = query.getResultList();
      assertEquals(28, ids.size(), "Resultsetlaenge ok?");
      idOfTestPic = ids.get(0);
    }
    Assertions.assertNotEquals(-1, idOfTestPic, "Id gefunden");
    System.out.println("Gefundene Id = " + idOfTestPic);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      DigiPicture pic = DigiPicture.getById(idOfTestPic);
      assertEquals(0, pic.getVersion(), "Start Version");
      System.out.println("Version is " + pic.getVersion());
    }
    
    MaintainPictureDialog dialog = new MaintainPictureDialog();
    dialog.loadById(idOfTestPic);
    List<JButton> buttons = dialog.getButtons();
    JButton saveButton = buttons.get(0);
    saveButton.doClick();
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      DigiPicture pic = DigiPicture.getById(idOfTestPic);
      assertEquals(1, pic.getVersion(), "Updated Version");
      System.out.println("Version is " + pic.getVersion());
    }
    countDigiPicture(28);
    countPicDirMap(28);
  }

  /**
   * as above, but the pic is rotated before saving
   */
  @Test
  public void testLoadByIdAndRotate() {
//    MediumLoadProcessor loadProcessor = new MediumLoadProcessor();
//    loadProcessor.setToTest();
//    PictureMedium medium = new PictureMedium();
//    medium.update();
//    loadProcessor.setMedium(medium);
//    int countRoots = loadProcessor.process(new File(TEST_DATA_FOLDER + "/CD1/"));
//    assertEquals(1, countRoots,
//            "Just checking if CD is correctlyloaded, shall have just one root");
    log();
    loadDVD("CD1");
    countDigiPicture(28);
    long idOfTestPic = -1;
    int startOrientation = -1;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery("SELECT ID FROM DIGI_PICTURE");
      List<Long> ids = query.getResultList();
      assertEquals(28, ids.size(), "Resultsetlaenge ok?");
      idOfTestPic = ids.get(10);
    }
    Assertions.assertNotEquals(-1, idOfTestPic, "Id gefunden");
    System.out.println("Gefundene Id = " + idOfTestPic);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      DigiPicture pic = DigiPicture.getById(idOfTestPic);
      assertEquals(0, pic.getVersion(), "Start Version");
      startOrientation = pic.getOrientation();
      System.out.println("Version is " + pic.getVersion()
              + "  Orientation: " + startOrientation);
    }
    
    MaintainPictureDialog dialog = new MaintainPictureDialog();
    dialog.loadById(idOfTestPic);
    List<JButton> buttons = dialog.getButtons();
    JButton rotateButton = buttons.get(1);
    rotateButton.doClick();
    JButton saveButton = buttons.get(0);
    saveButton.doClick();
    
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      DigiPicture pic = DigiPicture.getById(idOfTestPic);
      assertEquals(1, pic.getVersion(), "Updated Version");
      int orientation = pic.getOrientation();
      System.out.println("Version is " + pic.getVersion()
              + "  EndOrientation " + orientation);
      assertNotEquals(startOrientation, orientation, "Neue Orientation?");
    }
    countDigiPicture(28);
    countPicDirMap(28);
  }

  /**
   * test jses the rotate button 4 times and expects to get gthe same orientation
   * value back.
   */
  @Test
  protected void testRotateMultiple() {
    log();
    loadDVD("CD1");
    long idOfTestPic = -1;
    int startOrientation = -1;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery("SELECT ID FROM DIGI_PICTURE");
      List<Long> ids = query.getResultList();
      assertEquals(28, ids.size(), "Resultsetlaenge ok?");
      idOfTestPic = ids.get(20);
    }
    Assertions.assertNotEquals(-1, idOfTestPic, "Id gefunden");
    System.out.println("Gefundene Id = " + idOfTestPic);
//    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      DigiPicture pic = DigiPicture.getById(idOfTestPic);
      assertEquals(0, pic.getVersion(), "Start Version");
      startOrientation = pic.getOrientation();
      System.out.println("Version is " + pic.getVersion()
              + "  Orientation: " + startOrientation);
//    }
    
    MaintainPictureDialog dialog = new MaintainPictureDialog();
    dialog.loadById(idOfTestPic);
    List<JButton> buttons = dialog.getButtons();
    JButton rotateButton = buttons.get(1);
    rotateButton.doClick();
    rotateButton.doClick();
    rotateButton.doClick();
    rotateButton.doClick();      // 360 degree turn
    JButton saveButton = buttons.get(0);
    saveButton.doClick();

      DigiPicture picNew = DigiPicture.getById(idOfTestPic);
      assertEquals(1, picNew.getVersion(), "Updated Version");
      int orientation = picNew.getOrientation();
      System.out.println("Version is " + picNew.getVersion()
              + "  EndOrientation (4 clicks)" + orientation);
      assertEquals(startOrientation, orientation, "Gleiche Orientation?");
    
  }
  
  private void loadDVD(String dvdFolder) {
    MediumLoadProcessor loadProcessor = new MediumLoadProcessor();
    loadProcessor.setToTest();
    PictureMedium medium = new PictureMedium();
    medium.update();
    loadProcessor.setMedium(medium);
    int countRoots = loadProcessor.process(new File(TEST_DATA_FOLDER
            + "/" + dvdFolder + "/"));
    assertEquals(1, countRoots,
            "Just checking if CD is correctlyloaded, shall have just one root");
  }
}
