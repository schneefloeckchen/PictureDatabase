package hib;

import jakarta.persistence.EntityManager;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Disabled;
import picdata.Camera;
import picdata.DigiPicture;
import picdata.MediumType;
import picdata.PicDirectory;
import picdata.PictureMedium;
import picdata.TestBaseClass;

/**
 * Tests Hibernate / JPA Functionalities, which are currently not widely used in the
 * PictureDatabase. E.g. Delete Operations
 *
 * Uses the PictureDatabase Data Model
 *
 * created 13.4.24
 *
 * @author rene
 */
public class HibTest extends TestBaseClass {

  public HibTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    System.out.println("Setting up database connection for HibTest");
    setupDatabaseConnection();
    System.out.println("Database connection setup");
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    TestBaseClass.cleanDatabase();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test to create a simple structure, beginning with the VD/DVD.
   * Shall use only standard JPA/Hibernate calls with an EntityManager
   */
  @Test
  protected void createSimpleStructureMultipleEntityManager() {
    System.out.println("Starting createSimpleStructureMultipleEntityManager");
    PicJPAUtil jpaUtil = PicJPAUtil.getInstance();

    MediumType type = new MediumType();
    type.setDepiction("DVD");
    type.setCapacity(4700);
    try (EntityManager em = jpaUtil.createEntityManager()) {
      em.getTransaction().begin();
      em.persist(type);
      em.getTransaction().commit();
    }
    countRecords("MEDIUM_TYPE", 1);

    PictureMedium medium = new PictureMedium();
    medium.setCode(100);
    medium.setLabel("Label1");
    medium.setTitle("Title1");
    medium.setStorageMedium(type);
    try (EntityManager em = jpaUtil.createEntityManager()) {
      em.getTransaction().begin();
      em.persist(medium);
      em.getTransaction().commit();
    }
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);

    PicDirectory dir = new PicDirectory();
    dir.setDirectoryName("Dir1");
    dir.setMedium(medium);
    try (EntityManager em = jpaUtil.createEntityManager()) {
      em.getTransaction().begin();
      em.persist(dir);
      em.getTransaction().commit();
    }
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(1);

// Check all versions
    try (EntityManager em = jpaUtil.createEntityManager()) {
      MediumType t1 = em.find(MediumType.class, 1L);
      assertEquals(0, t1.getVersion(), "Version of MediumType");
      PictureMedium m1 = em.find(PictureMedium.class, 1L);
      assertEquals(0, m1.getVersion(), "Version of PictureMedium");
      PicDirectory p1 = em.find(PicDirectory.class, 1L);
      assertEquals(0, p1.getVersion(), "Version of PicDirectory");
    }

    Set<DigiPicture> newPictures = new HashSet<>();
    try (EntityManager em = jpaUtil.createEntityManager()) {
      for (int i = 0; i < 10; i++) {
        em.getTransaction().begin();
        DigiPicture pic = new DigiPicture();
        pic.setFileName("PIC" + i + ".JPEG");
        pic.addDirectory(dir);
        newPictures.add(pic);
        em.persist(pic);
        em.getTransaction().commit();
      }
    }
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(1);
    countDigiPicture(10);
// Check all versions
    try (EntityManager em = jpaUtil.createEntityManager()) {
      MediumType t1 = em.find(MediumType.class, 1L);
      assertEquals(0, t1.getVersion(), "Version of MediumType");
      PictureMedium m1 = em.find(PictureMedium.class, 1L);
      assertEquals(0, m1.getVersion(), "Version of PictureMedium");
      PicDirectory p1 = em.find(PicDirectory.class, 1L);
      assertEquals(0, p1.getVersion(), "Version of PicDirectory");
    }

    PicDirectory dir2 = new PicDirectory();
    dir2.setPictures(newPictures);
    dir2.setDirectoryName("Dir2");
    dir2.setMedium(medium);
    try (EntityManager em = jpaUtil.createEntityManager()) {
      em.getTransaction().begin();
      em.persist(dir2);
      em.getTransaction().commit();
    }
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(2);
    countDigiPicture(10);
    countRecords("PIC_DIR_MAP", 20);
  }

  /**
   * Test to create a simple structure, beginning with the VD/DVD.
   * Shall use only standard JPA/Hibernate calls with an EntityManager
   */
  @Test
  protected void createSimpleStructureSingleEntityManager() {
    System.out.println("starting createSimpleStructureSingleEntityManager");
    PicJPAUtil jpaUtil = PicJPAUtil.getInstance();

    MediumType type = new MediumType();
    type.setDepiction("DVD");
    type.setCapacity(4700);
    EntityManager em = jpaUtil.createEntityManager();
    em.getTransaction().begin();
    em.persist(type);
    em.getTransaction().commit();
    countRecords("MEDIUM_TYPE", 1);

    PictureMedium medium = new PictureMedium();
    medium.setCode(100);
    medium.setLabel("Label1");
    medium.setTitle("Title1");
    medium.setStorageMedium(type);
    em.getTransaction().begin();
    em.persist(medium);
    em.getTransaction().commit();
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);

    PicDirectory dir = new PicDirectory();
    dir.setDirectoryName("Dir1");
    dir.setMedium(medium);
    em.getTransaction().begin();
    em.persist(dir);
    em.getTransaction().commit();
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(1);

// Check all versions
    MediumType t1 = em.find(MediumType.class, 1L);
    assertEquals(0, t1.getVersion(), "Version of MediumType");
    PictureMedium m1 = em.find(PictureMedium.class, 1L);
    assertEquals(0, m1.getVersion(), "Version of PictureMedium");
    PicDirectory p1 = em.find(PicDirectory.class, 1L);
    assertEquals(0, p1.getVersion(), "Version of PicDirectory");

    Set<DigiPicture> newPictures = new HashSet<>();
    em.getTransaction().begin();
    for (int i = 0; i < 10; i++) {
      DigiPicture pic = new DigiPicture();
      pic.setFileName("PIC" + i + ".JPEG");
      pic.addDirectory(dir);
      newPictures.add(pic);
      em.persist(pic);
    }
    em.getTransaction().commit();
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(1);
    countDigiPicture(10);

// Check all versions
    t1 = em.find(MediumType.class, 1L);
    assertEquals(0, t1.getVersion(), "Version of MediumType");
    m1 = em.find(PictureMedium.class, 1L);
    assertEquals(0, m1.getVersion(), "Version of PictureMedium");
    p1 = em.find(PicDirectory.class, 1L);
    assertEquals(0, p1.getVersion(), "Version of PicDirectory");

    PicDirectory dir2 = new PicDirectory();
    dir2.setPictures(newPictures);
    dir2.setDirectoryName("Dir2");
    dir2.setMedium(medium);
    em.getTransaction().begin();
    em.persist(dir2);
    em.getTransaction().commit();
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(2);
    countDigiPicture(10);
    countRecords("PIC_DIR_MAP", 20);
    em.close();
  }

  /**
   * Test creates a set of images, links the to two directories and starts
   * removing some images from one of the folder
   * two EnitityManager uses, one to setup the data and one for the
   * remove operation.
   */
  @Test
  protected void removeImageSimple() {
    System.out.println("starting removeImageSimple");

    PicJPAUtil jpaUtil = PicJPAUtil.getInstance();

    MediumType type = new MediumType();
    type.setDepiction("DVD");
    type.setCapacity(4700);
    EntityManager em = jpaUtil.createEntityManager();
    em.getTransaction().begin();
    em.persist(type);
    em.getTransaction().commit();
    countRecords("MEDIUM_TYPE", 1);

    PictureMedium medium = new PictureMedium();
    medium.setCode(100);
    medium.setLabel("Label1");
    medium.setTitle("Title1");
    medium.setStorageMedium(type);
    em.getTransaction().begin();
    em.persist(medium);
    em.getTransaction().commit();
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);


    PicDirectory dir1 = new PicDirectory();
    PicDirectory dir2 = new PicDirectory();
    dir1.setDirectoryName("Dir1");
    dir1.setMedium(medium);
    dir2.setDirectoryName("Dir2");
    dir2.setMedium(medium);
    em.getTransaction().begin();
    em.persist(dir1);
    em.persist(dir2);
    em.getTransaction().commit();
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(2);
    
    em.getTransaction().begin();
    for (int i = 0; i < 10; i++) {
      DigiPicture pic = new DigiPicture();
      pic.setFileName("PIC" + i + ".JPEG");
      pic.addDirectory(dir1);
      pic.addDirectory(dir2);
      em.persist(pic);
    }
    em.getTransaction().commit();

    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(2);
    countDigiPicture(10);
    countRecords("PIC_DIR_MAP", 20);
// Check all versions
    MediumType t1 = em.find(MediumType.class, 1L);
    assertEquals(0, t1.getVersion(), "Version of MediumType");
    PictureMedium m1 = em.find(PictureMedium.class, 1L);
    assertEquals(0, m1.getVersion(), "Version of PictureMedium");
    PicDirectory p1 = em.find(PicDirectory.class, 1L);
    assertEquals(0, p1.getVersion(), "Version of PicDirectory");
    PicDirectory p2 = em.find(PicDirectory.class, 2L);
    assertEquals(0, p2.getVersion(), "Version of PicDirectory");
    em.close();
    
// Everything set up, now for the test
// Use new EntityManager
    em = null;          // Kill old em
    em = jpaUtil.createEntityManager();
    em.getTransaction().begin();
    PicDirectory d1 = em.find(PicDirectory.class, 1L);
    assertEquals(10, d1.getPictures().size(), "Number of pictures for dir1");
    PicDirectory d2 = em.find(PicDirectory.class, 2L);
    assertEquals(10, d2.getPictures().size(), "Number of pictures for dir2");
    em.getTransaction().commit();
    em.close();
    
// first remove
    try (EntityManager em2 = jpaUtil.createEntityManager()) {
      em2.getTransaction().begin();
      PicDirectory pd2 = em2.find(PicDirectory.class, 2L);
      assertNotNull(pd2, "Directory not found to delete picture");
      Set<DigiPicture> picsPd2 = pd2.getPictures();
      DigiPicture picture = em2.find(DigiPicture.class, 9L);
      assertNotNull(picture);
      picsPd2.remove(picture);
      em2.persist(pd2);
      em2.getTransaction().commit();
    }
    countRecords("MEDIUM_TYPE", 1);
    countRecords("PICTURE_MEDIUM", 1);
    countDirectory(2);
    countDigiPicture(10);
    countRecords("PIC_DIR_MAP", 19);

// Double checking with JPA toolset
    em = jpaUtil.createEntityManager();
    em.getTransaction().begin();
    d2 = em.find(PicDirectory.class, 2L);
    Set<DigiPicture> reducedSet = d2.getPictures();
    int countReduced = reducedSet.size();
    em.getTransaction().commit();
    em.close();
    assertEquals(9, countReduced, "Number of reduced pictures");
  }

  /**
   * Tests the database cross table between pic Directory and DigiPicture.
   * Create zwo directories and two pictures, add both to dir1, and the first
   * to dor 2. SHall have still 2 pics but 3 entries in the cross table.
   * Than remove pic1 from the first folder, shall still remain in the 2nd
   * directory, but reduce cross table by one.
   */
  @Test
  @Disabled
  protected void crossTable1SimpleTest() {
    Camera camera = createCamera("Blub", "Tata");
    PicDirectory dir1 = createPicDirectory("DIR1");
    PicDirectory dir2 = createPicDirectory("DIR2");
    DigiPicture pic1 = createDigiPicture(camera, "PICTURE001.JPEG", 1001);
    DigiPicture pic2 = createDigiPicture(camera, "PICTURE002.JPEG", 1001);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      dir1.addPicture(pic1);
      dir1.addPicture(pic2);
      dir1.update();
      em.getTransaction().commit();
    }
    countRecords("PIC_DIR_MAP", 2);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      dir2.addPicture(pic1);
      dir2.update();
      em.getTransaction().commit();
    }
    countRecords("PIC_DIR_MAP", 3);
    countDigiPicture(2);
// All created, now remove one pic from dir1
    Set<DigiPicture> dir1Pics = dir1.getPictures();
    assertEquals(2, dir1Pics.size(), "Anzahl Bilder in Dir1");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      PicDirectory dir1_ = em.merge(dir1);
      dir1_.removePicture(pic2);
      dir1_.update();
      em.getTransaction().commit();
    }
    countRecords("PIC_DIR_MAP", 2);
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      dir1 = em.find(PicDirectory.class, 1L);
      assertNotNull(dir1);
      dir1Pics = dir1.getPictures();
      assertEquals(1, dir1Pics.size(), "Anzahl Bilder in Dir1 after removing pic2");
      em.getTransaction().commit();
    }
  }
}
