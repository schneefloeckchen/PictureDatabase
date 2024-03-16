package picdata;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

/**
 * Aug 23: RZ / migrated to Junit 5 and JPA 2.2
 * This class tests the basic functions of the datamodel, like adding pictures
 * to a directory.
 * Picture handling methods like loading, exif analysis are in the
 * test class DigiPictureLoadTest. Just to easy handling.
 *
 * @author rene
 */
public class DigiPictureTest extends TestBaseClass {

  PicDirectory m_pDir1;
  PicDirectory m_pDir2;

  public DigiPictureTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    setupDatabaseConnection();
  }

  @AfterAll
  public static void tearDownClass() {
//    cleanDatabase();
  }

  @BeforeEach
  public void setUp() {
    cleanDatabase();
    super.setUpBaseData();
  }

  @AfterEach
  public void tearDown() {
//    cleanDatabase();
  }

  /**
   * adds two pictures to an directory and uses a separate EntityManager
   * to validate this (count pics in folder).
   */
  @Test
  public void testAddingPictures() {
    PicDirectory pDir = new PicDirectory();
    pDir.setMedium(m_picMedium1);
    pDir.update();
    DigiPicture pic1 = createDigiPictureVolatile();
    DigiPicture pic2 = createDigiPictureVolatile();
    assertEquals(Long.valueOf(-1), pic2.getId(), "Noch nicht gesichert?");
    pic1.addDirectory(pDir);
    pic2.addDirectory(pDir);
    pic1.update();
    pic2.update();
    long id = pDir.getId();
//    Session session = PicHibernateUtil.getSessionFactory().openSession();
//    PicDirectory pDir2 = session.get(PicDirectory.class, id);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
//      em.getTransaction().begin();
      PicDirectory pDir2 = em.find(PicDirectory.class, id);
      Set pics = pDir2.getPictures();
      System.out.println("Anzahl Pics: " + pics.size());
      assertEquals(2, pics.size(), "Wirklich 2 pics?");
      assertEquals(0, pDir2.getVersion(), "Immer noch version 0 des directories?");
    }
  }

  /**
   * Simple Test, if a remove of a picture also removes the cross table element
   * Disabled, as with the creation of the tables using the current hibernate
   * implementation
   * Foreign Keys are also created which let the test fail
   * as it was initially implemented
   */
  @Test
  @Disabled
  public void crossTable1stSimpleTest() {
//    disableForeignKeyChecks()
    log("Start");
    createM_pDir1("pDir1", m_picMedium1);

    DigiPicture pic1 = createDigiPictureVolatile("Pic1");
    m_pDir1.addPicture(pic1);   // creates in database
    pic1.update();
    m_pDir1.update();
    countDigiPicture(1);
    countPicDirMap(1);
    long pic1Id = pic1.getId();

    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      DigiPicture pic = em.find(DigiPicture.class, pic1Id);
      em.remove(pic);
      em.getTransaction().commit();
    }
    countDigiPicture(0);
    countPicDirMap(0);
  }

  /**
   * Simple Test, if a remove of a picture also removes the cross table element
   * see: crossTable1stSimpleTest
   *
   * Ablauf:
   * (1) Create PicDirectory m_pDir1 in and DigiPicture pic1 in database,
   * Check Id and Version of the new entries
   * (2) Add picture to dir and check
   * (3) Remove the picture and check, picture still there, bas entry in
   * cross-table removed.
   */
  @Test
  @Disabled
  public void crossTable2ndSimpleTest() {
    log("starte crossTable2ndSimpleTest");
//    disableForeignKeyChecks();
// (1) Create dir and map
    createM_pDir1("pDir1", m_picMedium1);
    assertEquals(1L, m_pDir1.getId(), "Wrong ID of m_pDir1 after initial creation in the database");
    assertEquals(0, m_pDir1.getVersion(), "Wrong version of m_pDir1 after initial creation");
    DigiPicture pic1 = createDigiPictureVolatile("Pic1");
    pic1.update();
    assertEquals(1L, pic1.getId(), "Wrong ID of Picture1 after initial creation in the database");
    assertEquals(0, pic1.getVersion(), "Wrong version of Picture1 after initial creation");

// (2) Add pic to dir and check
    m_pDir1.addPicture(pic1);
    m_pDir1.update();
    assertEquals(1L, m_pDir1.getId(), "Wrong ID of m_pDir1 after update in the database");
    assertEquals(0, m_pDir1.getVersion(), "Wrong version of m_pDir1 after update");
    assertEquals(1L, pic1.getId(), "Wrong ID of Picture1 after initial creation in the database");
    assertEquals(0, pic1.getVersion(), "Wrong version of Picture1 after initial creation");
    countDigiPicture(1);
    countPicDirMap(1);
    Set<DigiPicture> pictures = m_pDir1.getPictures();
    assertEquals(1, pictures.size(), "Wrong length of digiPictures Set");

// (3) remove one picture in transaction
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      DigiPicture toRemove = em.merge(DigiPicture.getById(1L));     // The getByID method
      PicDirectory dirUnderTest = em.merge(PicDirectory.getById(1L));  // Has its own transaction,
      // merge moves the object into the current transaction.
      Set dummy = dirUnderTest.getPictures();
      System.out.println("  SIZE OF DUMMY " + dummy.size());
      dirUnderTest.removePicture(toRemove);
      em.persist(dirUnderTest);
      em.getTransaction().commit();
    }
    countDigiPicture(1);     // removing the picture from the object, does not
    countPicDirMap(0);       // remove the picture itself.
  }

  /**
   * standardmaessig werden durch hibernate foreign keys (constraints) angelegt,
   * die verhindern dass ueber keys gebundene Records geloescht werden. Fuer 2
   * alte Tests
   * muss das (voruebergehend) ausgeschaltet werden. Dies macht diese Funktion.
   * Okt 2023: Nicht mehr genutzt
   */
  private void disableForeignKeyChecks() {
//    SQLUtil sqlUtil = new SQLUtil();
    Connection conn = sqlUtil.getConnection();
    try {
      Statement stmt = conn.createStatement();
      boolean result = stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
      assertFalse(result, "No data deleivered via this statement");
    } catch (SQLException ex) {
      log("Error in disabling Foreign Key CHecksw");
    }
  }

  /**
   * Simple Test, if a remove of a picture also removes the cross table element
   * Function not used in the application
   *
   * Process:
   * (1) create m_pDir1 and 4 digiPicture elements, add the pics to the
   * directory and validate result by counting in database.
   * (2) remove one picture from the directory, but do not remove the picture
   * the database
   */
  @Test
  @Disabled
  public void crossTable3rdSimpleTest() {
    log("Starte crossTable3rdSimpleTest");
    createM_pDir1("pDir1", m_picMedium1);

    DigiPicture pic1 = createDigiPictureVolatile("Pic1");
    pic1.update();
    DigiPicture pic2 = createDigiPictureVolatile("Pic2");
    pic2.update();
    DigiPicture pic3 = createDigiPictureVolatile("Pic3");
    pic3.update();
    DigiPicture pic4 = createDigiPictureVolatile("Pic4");
    pic4.update();
    m_pDir1.addPicture(pic1);
    m_pDir1.addPicture(pic2);
    m_pDir1.addPicture(pic3);
    m_pDir1.addPicture(pic4);
    m_pDir1.update();
    Set<DigiPicture> pictures = m_pDir1.getPictures();
    assertEquals(4, pictures.size(), "Wrong number of Pictures in directory");
    countDigiPicture(4);
    countPicDirMap(4);
    log("Entries in database created an checked.");
//    (2) Remove 1 picture
    PicDirectory dir;
    DigiPicture pic;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      dir = em.find(PicDirectory.class, m_pDir1.getId());
      pic = em.find(DigiPicture.class, pic4.getId());
      Set<DigiPicture> pics = dir.getPictures();
      log("Set of pictures retained from database");
      assertEquals(4, pics.size(), "Wrong number of pics in Set");
//      dir.removePicture(pic4);
      pic.removeDirectory(dir);
      log("Removed");
      em.persist(pic);
      log("Persisted");
      Set<DigiPicture> picsForTest = dir.getPictures();
      log("Set loaded");
      int setSize = picsForTest.size();
      log("Length of set " + setSize);
//      assertEquals(3, dir.getPictures().size(), "Wrong number of pics in Set after remove pic4");
//      dir.update();
      log("Starting Commit");
      em.getTransaction().commit();
      log("and commited");
    }
    countDigiPicture(4);
    countPicDirMap(3);

  }

  /**
   * Tests handling of the cross table / the n x m relationship
   * Use two folders, which have some picture in common.
   */
  @Test
  public void CrossTableBaseTest() {
    log("starte CrossTableBaseTest");
    createM_pDir1("pDir1", m_picMedium1);
    createM_pDir2("pDir2", m_picMedium2);
    countRecords("PIC_DIRECTORY", 2);
    DigiPicture pic1 = createDigiPictureVolatile("Pic1");
    DigiPicture pic2 = createDigiPictureVolatile("Pic2");
    DigiPicture pic3 = createDigiPictureVolatile("Pic3");
    DigiPicture pic4 = createDigiPictureVolatile("Pic4");
    countDigiPicture(0);

    pic1.update();
    pic2.update();
    pic3.update();
    m_pDir1.addPicture(pic1);
    m_pDir1.addPicture(pic2);
    m_pDir1.addPicture(pic3);
    m_pDir1.update();
    countDigiPicture(3);

    /*    try (Session session = PicHibernateUtil.getSessionFactory().openSession()) {
      PicDirectory dirUnderTest = session.get(PicDirectory.class, m_pDir1.getId());
      assertNotNull(dirUnderTest);
      assertEquals(3, dirUnderTest.getPictures().size(), "Pictures in folder");
    } */
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      PicDirectory dirUnderTest = em.find(PicDirectory.class, m_pDir1.getId());
      assertNotNull(dirUnderTest);
      assertEquals(3, dirUnderTest.getPictures().size(), "Pictures in folder");
    }

    testFolderSize(pic1, 1);
    testFolderSize(pic2, 1);
    testFolderSize(pic3, 1);

    pic4.update();
    countPicDirMap(3);

    // Now store all pictures in the second folder
    m_pDir2.addPicture(pic1);
    m_pDir2.addPicture(pic2);
    m_pDir2.addPicture(pic3);
    m_pDir2.addPicture(pic4);
    m_pDir2.update();
    countDigiPicture(4);
    testFolderSize(pic1, 2);
    testFolderSize(pic2, 2);
    testFolderSize(pic3, 2);
    testFolderSize(pic4, 1);
    countPicDirMap(7);

    System.out.println("CrossTableBaseTest finished");
  }

  /**
   * Tests handling of the cross table / the n x m relationship
   * Use two folders, which have some picture in common.
   * Removal of pictures included
   *
   * Not working, check first simplier entry removal.
   *
   */
  @Test
  @Disabled
  public void CrossTableDelete2Test() {

    // Create 2 directories, each assigned to its own medium, save to the database
    // Test, if really 2 in DB
    createM_pDir1("pDir1", m_picMedium1);
    createM_pDir2("pDir2", m_picMedium2);
    countRecords("PIC_DIRECTORY", 2);
    // Create 4 pics, different name, same time stamp, and save to DB
    // Count them, shall be 4
    DigiPicture pic1 = createDigiPictureVolatile("Pic1");
    DigiPicture pic2 = createDigiPictureVolatile("Pic2");
    DigiPicture pic3 = createDigiPictureVolatile("Pic3");
    DigiPicture pic4 = createDigiPictureVolatile("Pic4");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      em.persist(pic1);
      em.persist(pic2);
      em.persist(pic3);
      em.persist(pic4);
      em.getTransaction().commit();
    }
    countDigiPicture(4);

    // adding all 4 pictures to the first folder, test via count
    System.out.println("PicDirectory version before updating with 4 pictures: " + m_pDir1.getVersion());
    m_pDir1.addPicture(pic1);
    System.out.println("PicDirectory version after adding first picture: " + m_pDir1.getVersion());
    m_pDir1.addPicture(pic2);
    m_pDir1.addPicture(pic3);
    m_pDir1.addPicture(pic4);
    m_pDir1.update();
    log("PicDirectory version after update: " + m_pDir1.getVersion());
    countDigiPicture(4);
    countPicDirMap(4);

// adding pictures 1 to 3 to folder 2, check with counting pic objects and map objects
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      m_pDir2 = em.merge(m_pDir2);
      m_pDir2.addPicture(pic1);
      m_pDir2.addPicture(pic2);
      m_pDir2.addPicture(pic3);
      em.merge(m_pDir2);
      // remove one of the pictures, which is in both folders
      m_pDir2.removePicture(pic3);
      em.merge(m_pDir2);
      em.getTransaction().commit();
      countDigiPicture(4);
      countPicDirMap(6);
    }
    countDigiPicture(4);
    countPicDirMap(6);

    // remove pic4, which is only in folder 1
    m_pDir1.removePicture(pic4);
    m_pDir1.update();
    countDigiPicture(4);
    countPicDirMap(5);
    testPictureCount(m_pDir1, 3);
    testPictureCount(m_pDir2, 2);
    // Delete Pic 2, shall be removed in both folder
/*    try (Session session = PicHibernateUtil.getSessionFactory().openSession()) {
      Transaction transaction = session.beginTransaction();
      session.delete(pic2);
      transaction.commit();
    } */
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.remove(pic2);
    }

    // List the pictures in a folder
    listPicturesInFolder(m_pDir1);
    listPicturesInFolder(m_pDir2);
    testPictureCount(m_pDir1, 2);
    testPictureCount(m_pDir2, 1);
    countDigiPicture(3);
//    countPicDirMap(3);      // is 5, as removing the picture only will not affect
    // the map.

  }

  /*
    Creates 2 folder and some DigiPictureObjects. 1 picture is stored in both folder,
    other only in one. Than checks wether the hasDuplicates method delivers
    the right answer.
    the pic objects are synthetic, not created from a image file
   */
  @Test
  protected void testHasDuplicatesSimple() {
    log("Starte test hasDuplicates simple version");
    assertNotNull(m_picMedium1);
    assertNotNull(m_picMedium2);
    createM_pDir1("pDir1", m_picMedium1);
    createM_pDir2("pDir2", m_picMedium2);
    countRecords("PIC_DIRECTORY", 2);
    assertNotNull (m_pDir1);
    assertNotNull (m_pDir2);      // Just checking, if already created
// Create and validate first pic, which is just in one folder
    DigiPicture pic1 = new DigiPicture();
    pic1.setFileName("PIC1");
    pic1.setPictureTakenMilis(1000);
    pic1.addDirectory(m_pDir1);
    pic1.update();
    assertNotEquals(-1, pic1.getId(), "Picture in database?");
    long pic1Id = pic1.getId();
    log ("Created id for new pic is "+pic1Id);
    countPicDirMap(1);
// Create and validate 2nd pic, which is stored in two folder
    DigiPicture pic2 = new DigiPicture();
    pic2.setFileName("PIC2");
    pic2.setPictureTakenMilis(1000);
    pic2.addDirectory(m_pDir1);
    pic2.addDirectory(m_pDir2);
    pic2.update();
    assertNotEquals(-1, pic2.getId(), "Picture in database?");
    long pic2Id = pic2.getId();
    log ("Created id for 2nd pic is "+pic2Id);
    countPicDirMap(3);
 //  now load the two pics and validate the hasDuplicate Method
    DigiPicture test = DigiPicture.getById(pic1Id);
    assertFalse(test.hasDuplicates(), "Just one folder");
    test = DigiPicture.getById(pic2Id);
    assertTrue(test.hasDuplicates(), "SHall have duplicates");
  }

  private void createM_pDir1(String name, PictureMedium medium) {
    m_pDir1 = create_pDir(name, medium);
  }

  private void createM_pDir2(String name, PictureMedium medium) {
    m_pDir2 = create_pDir(name, medium);
  }

  /**
   * validates the number of folder, where a picture is stored
   */
  private void testFolderSize(DigiPicture pic, int expectedSize) {
    /*    try (Session session = PicHibernateUtil.getSessionFactory().openSession()) {
      DigiPicture picUnderTest = session.get(DigiPicture.class, pic.getId()); */
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      DigiPicture picUnderTest = em.find(DigiPicture.class, pic.getId());
      assertNotNull(picUnderTest);
      assertEquals(
              expectedSize, picUnderTest.getDirectories().size(),
              "Number of Folder, where the picture is stored");
    }
  }

  /**
   * Validates the number of pictures in a directory
   */
  private void testPictureCount(PicDirectory dir, int expectedCount) {
    /*    try (Session session = PicHibernateUtil.getSessionFactory().openSession()) {
      PicDirectory directoryUnderTest = session.get(PicDirectory.class, dir.getId());*/
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      PicDirectory directoryUnderTest = em.find(PicDirectory.class, dir.getId());
      assertNotNull(directoryUnderTest);
      assertEquals(
              expectedCount, directoryUnderTest.getPictures().size(),
              "Number of pictures in Folder ");
    }
  }

  /**
   * List all pictures in a folder
   */
  private void listPicturesInFolder(PicDirectory dir) {
    /*    try (Session sess = PicHibernateUtil.getSessionFactory().openSession()) {
      PicDirectory directoryUnderTest = sess.get(
          PicDirectory.class, dir.getId()); */
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      PicDirectory directoryUnderTest = em.find(
              PicDirectory.class, dir.getId());
      System.out.println("Dir loaded - " + dir.getDirectoryName());
      directoryUnderTest.getPictures().forEach(pic -> {
        System.out.println(pic.getFileName());
      });
    }
  }
}
