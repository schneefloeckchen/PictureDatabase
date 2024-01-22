/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package picdata;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests various delete scenarios for Deletion of DigiPicture, PicDirectory etc.
 *
 * @author rene
 */
public class PictureDeleteTest extends TestBaseClass {

  public PictureDeleteTest() {
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
   * Tests the simple removal of objects without relation.
   * Classes are DigiPicture, PicDirectory, PictureMedium
   */
  @Test
  public void testSimpleObjectDeletion() {
    // Create an object for each of the classes in test
    PicDirectory dir;
    DigiPicture pic;
    PictureMedium medium;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      dir = new PicDirectory();
      dir.setDirectoryName("DIR1");
      em.persist(dir);
      pic = new DigiPicture();
      pic.setFileName("Test1.jpeg");
      pic.setRemark("In testSimpleObjectDeletion erzeugt");
      em.persist(pic);
      medium = new PictureMedium();
      medium.setLabel("Medium1");
      medium.setRemark("In testSimpleObjectDeletion erzeugt");
      em.persist(medium);
      em.getTransaction().commit();
    }
    countDigiPicture(1);
    countDirectory(1);
    countRecords("PICTURE_MEDIUM", 1);

    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      DigiPicture p = em.find(DigiPicture.class, pic.getId());
      em.remove(p);
      em.getTransaction().commit();
    }
    countDigiPicture(0);
    countDirectory(1);
    countRecords("PICTURE_MEDIUM", 1);
  }

  /**
   * tests the deletion of objects out of classes with multiple Objects in
   * Database
   */
  @Test
  public void testSimpleObjectDeletion2() {
    List<PicDirectory> dirList = new ArrayList<>();
    List<PictureMedium> mediumList = new ArrayList<>();
    List<DigiPicture> picList = new ArrayList<>();
// Create the objects in the database
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      for (int d = 0; d < 20; d++)
        dirList.add(createPicDirectory(em, "DirNo" + d));
      for (int m = 0; m < 20; m++)
        mediumList.add(createMedium(em, "Medium-No" + m));
      for (int p = 0; p < 200; p++)
        picList.add(createDigiPicture(em, "IMG-" + p));
      em.getTransaction().commit();
    }
    countDigiPicture(200);
    countDirectory(20);
    countRecords("PICTURE_MEDIUM", 20);

// Remove one object from each class and count
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      DigiPicture pic = em.merge(picList.get(20));
      assertNotNull(pic, "Picture merged to Transaction");
      em.remove(pic);
      PicDirectory dir = em.merge(dirList.get(10));
      assertNotNull(pic, "PicDirectory merged to Transaction");
      em.remove(dir);
      PictureMedium medium = em.merge(mediumList.get(10));
      assertNotNull(medium, "PicMedium merged to Transaction");
      em.remove(medium);
      em.getTransaction().commit();
    }
    countDigiPicture(199);
    countDirectory(19);
    countRecords("PICTURE_MEDIUM", 19);
  }

  /**
   * Test creates some PictureMedium Object and adds some PicDirectories to
   * them,
   * then removes the directories and checks the result.
   * This is a oneToMany relationship and uses Lazy Loading by default
   */
  @Test
  public void testRemovalOfDirectories() {
    PictureMedium medium1;
    long medium1Id;
    PictureMedium medium2;
    long medium2Id;
    List<PicDirectory> dirs2 = new ArrayList<>();
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      medium1 = createMedium(em, "MEDIUM-1");
      medium1Id = medium1.getId();
      medium2 = createMedium(em, "MEDIUM-2");
      medium2Id = medium2.getId();
      for (int d = 0; d < 20; d++) {
        PicDirectory dir = new PicDirectory();
        dir.setDirectoryName("DirM1_" + d);
        dir.setMedium(medium1);
        em.persist(dir);
      }
      for (int d = 0; d < 25; d++) {
        PicDirectory dir = new PicDirectory();
        dir.setDirectoryName("DirM2_" + d);
        dir.setMedium(medium2);
        em.persist(dir);
        dirs2.add(dir);
      }
      em.getTransaction().commit();
    }
    countDirectory(45);
    countMedium(2);
//
// 2nd check
    PictureMedium mediumUnderTest;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      mediumUnderTest = em.find(PictureMedium.class, medium2Id);
      Set<PicDirectory> dirs = mediumUnderTest.getPicDirectories();
      assertEquals(25, dirs.size(), "Anzahl Dirs on medium 2");
      em.getTransaction().commit();
    }

// Remove one dictionary
    long idOfRemovedDir;
    log("Removing the directory and initial validating");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PictureMedium testMedium = em.merge(mediumUnderTest);
      PicDirectory removedDir = dirs2.get(10);
      idOfRemovedDir = removedDir.getId();
      PicDirectory dirT = em.merge(removedDir);     // Dir in diese Transaktion holen
      assertNotNull(dirT, "Directory to remove");
      testMedium.removePicDirectory(dirT);
      int si = testMedium.getPicDirectories().size();
      assertEquals(24, si,
          "Number of PicDirectories after removal");
      em.getTransaction().commit();
    }

// Count
    log("Counting again");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PictureMedium mediumUnderTest2 = em.find(PictureMedium.class, medium2Id);
      Set<PicDirectory> dirs = mediumUnderTest2.getPicDirectories();
      int si = dirs.size();
      assertEquals(24, si, "Anzahl Dirs on medium 2");
      PicDirectory dt = em.find(PicDirectory.class, idOfRemovedDir);
      assertNotNull(dt, "Removed Directory Missing");
      PictureMedium mt = dt.getMedium();
      assertNull(mt, "Medium should be null");
      em.getTransaction().commit();
    }

    // Final check
    log("Final check, count all picDirs and double check the null from Medium");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PicDirectory dt = em.find(PicDirectory.class, idOfRemovedDir);
      assertNotNull(dt, "Removed Directory Missing");
      PictureMedium mt = dt.getMedium();
      assertNull(mt, "Medium should be null");
      em.getTransaction().commit();
      countDirectory(45);
    }
  }

  private PicDirectory createPicDirectory(EntityManager em, String name) {
    PicDirectory dir = new PicDirectory();
    dir.setDirectoryName(name);
    em.persist(dir);
    return dir;
  }

  private PictureMedium createMedium(EntityManager em, String name) {
    PictureMedium medium = new PictureMedium();
    medium.setLabel(name);
    medium.setTitle("Title og " + name);
    em.persist(medium);
    return medium;
  }

  private DigiPicture createDigiPicture(EntityManager em, String name) {
    DigiPicture picture = new DigiPicture();
    picture.setFileName(name);
    em.persist(picture);
    return picture;
  }

}
