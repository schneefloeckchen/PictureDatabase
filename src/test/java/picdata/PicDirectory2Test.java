package picdata;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Tests der PicDirectory Klasse, fokussiert auf tests, in denen
 * die Pics in mehreren Medien oder Directories audftauchen
 *
 * Tests auf reines Mocken der Bilder, das heisst, diese werden manuell angelegt
 * und nicht aus Image-Dateien.
 *
 * @author rene
 */
public class PicDirectory2Test extends TestBaseClass {

  private Set<DigiPicture> m_picSet1, m_picSet2;    // 2 sets of pictures for
  // later use
  private Object[][] m_picSet1Data = { // Data are filename, taken millis
    {"File1.JPEG", 10000},
    {"File2.JPEG", 10000},
    {"File3.JPEG", 10000},
    {"File4.JPEG", 10000},
    {"File5.JPEG", 10000},
    {"File6.JPEG", 10000},
    {"File7.JPEG", 10000},
    {"File8.JPEG", 10000},
    {"File9.JPEG", 10000},
    {"File9.JPEG", 10001},
    {"File10.JPEG", 10001}
  };
  private Object[][] m_picSet2Data = {
    {"CAM10.jpeg", 110000300},
    {"CAM11.jpeg", 11000100},
    {"CAM12.jpeg", 110000300},
    {"CAM13.jpeg", 110004000},
    {"CAM14.jpeg", 110500000},
    {"CAM15.jpeg", 110060000},
    {"CAM16.jpeg", 110007000},
    {"CAM17.jpeg", 110000800},
    {"CAM18.jpeg", 1101200000},};

  public PicDirectory2Test() {
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
  protected void simpleTest() {
    createDirectories();
    m_picSet1 = createPictures(m_picSet1Data);
    m_picSet2 = createPictures(m_picSet2Data);
    int numPics = countRecords("DIGI_PICTURE");
    int numData = m_picSet1Data.length + m_picSet2Data.length;
    assertEquals(numData,
            numPics, "Number of pics in database");
    PicDirectory dir1 = createPicDirectory("DIR1");
    PicDirectory dir2 = createPicDirectory("DIR2");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      m_picSet1.forEach(pic -> dir1.addPicture(pic));
      dir1.update();
      em.getTransaction().commit();
// now transaction per add
      PicDirectory dir2m = em.merge(dir2);
      m_picSet2.forEach(pic -> {
        em.getTransaction().begin();
        dir2m.addPicture(pic);
        em.persist(dir2m);
        em.getTransaction().commit();
      });
    }
    assertEquals(numData, countRecords("PIC_DIR_MAP"), "Check length of Cross Table");
  }

  /**
   * adds a set of pictures to 2 separate folder
   */
  @Test
  protected void multipleFolder1SimpleTest() {
    Set<DigiPicture> pics = createPictures(m_picSet1Data);
    int picCount = pics.size();
    assertNotEquals(0, picCount);
    createDirectories();
    try (EntityManager em = m_picJPAUtil.createEntityManager()) {
      em.getTransaction().begin();
      PicDirectory dir1 = em.merge(PicDirectory.getById(1L));
      PicDirectory dir2 = em.merge(PicDirectory.getById(2L));
      assertNotNull(dir1);
      assertNotNull(dir2);
      pics.forEach(pic -> {
        dir1.addPicture(pic);
        dir2.addPicture(pic);
      });
      em.persist(dir1);
      em.persist(dir2);
      em.getTransaction().commit();
    }
    countRecords("PIC_DIR_MAP", 2*picCount);
    countDigiPicture(picCount);

    PicDirectory dir1 = PicDirectory.getById(1L);
    PicDirectory dir2 = PicDirectory.getById(2L);
    assertEquals(1, dir1.getVersion(), "Correct Version of Dir1");
    assertEquals(1, dir2.getVersion(), "Correct Version of Dir2");
  }

  private Set<DigiPicture> createPictures(Object[][] data) {
    Set<DigiPicture> set = new HashSet<>();
    Arrays.stream(data).forEach(row -> {
      DigiPicture pic = new DigiPicture();
      pic.setFileName((String) row[0]);
      pic.setPictureTakenMilis((Integer) row[1]);
      pic.update();
      set.add(pic);
    });
    assertEquals(data.length, set.size(), "Anzahl erzeugter Pics.");
    return set;
  }

  private void createDirectories() {
    String[] names = {"DIR1", "DIR2", "DIR3"};
    Arrays.stream(names).forEach(dirCreator);
  }

  /**
   * Traditionell programmierte Consumer Klasse zur Erzeugung eines
   * Verzeichnisses in der Datenbank mit dem gegebenen Namen
   */
  private final Consumer<String> dirCreator = new Consumer<>() {
    @Override
    public void accept(String t) {
      PicDirectory dir = new PicDirectory();
      dir.setDirectoryName(t);
      dir.update();
    }
  };

}
