package picdata;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.util.Set;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;

/**
 *
 * @author rene
 */
public class PicDirectoryTest extends TestBaseClass {

  public PicDirectoryTest() {
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
    super.setUpBaseData();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Adding pictures to the PictureDirectory using the basic JPA methods
   * persist etc. in transactions
   */
  @Test
  public void testBasicAddingPictures() {
    log("Testing adding pictures using transactions and the regular JPA methods");
    MediumType mt = new MediumType();
    mt.setDepiction("TEST TYPE");
    try (EntityManager em = m_picJPAUtil.createEntityManager()) {
      em.getTransaction().begin();
      em.persist(mt);
      em.getTransaction().commit();
    }
    PictureMedium picMedium = new PictureMedium();
    picMedium.setStorageMedium(mt);
    picMedium.setLabel("TEST LABEL");
    try (EntityManager em = m_picJPAUtil.createEntityManager()) {
      em.getTransaction().begin();
      em.persist(picMedium);
      em.getTransaction().commit();
    }
    assertEquals(0, mt.getVersion(), "Version von Mediumtype");
    assertEquals(0, picMedium.getVersion(), "Version von PictureMedium");

    // Jetzt das PicDirectory erst
    PicDirectory picDirectory = new PicDirectory();
    picDirectory.setMedium(picMedium);
    DigiPicture pic1 = new DigiPicture();
    pic1.setFileName("TestBild.jpeg");
    pic1.setCamera(m_camera1);
    pic1.setPictureTakenMilis(10000);
    try (EntityManager em = m_picJPAUtil.createEntityManager()) {
      em.getTransaction().begin();
      em.persist(pic1);
      picDirectory.addPicture(pic1);
      em.persist(picDirectory);
      em.getTransaction().commit();
    }
    assertEquals(1, sqlUtil.countElementsInTable("DIGI_PICTURE"), "Wrong number of Elements in DigiPicture");
    assertEquals(1, sqlUtil.countElementsInTable("PIC_DIRECTORY"), "Wrong number of Elements in PicDirectory");
    assertEquals(0, picDirectory.getVersion(), "Wrong Version of PicDirectory Object");
    assertEquals(0, m_camera1.getVersion(), "Wrong Version of Camera");
    assertEquals(0, pic1.getVersion(), "Wrong Version of DigiPicture");
    assertEquals(1, sqlUtil.countElementsInTable("PIC_DIR_MAP"), "Wrong length of Kreuztabelle");
  }

  @Test
  public void testCreation() {
    log("PicDirectory - Teste Erzeugung PicDirectory");
    MediumType mt = new MediumType();
    mt.setDepiction("TEST TYPE");
    mt.update();
    PictureMedium pm = new PictureMedium();
    pm.setStorageMedium(mt);
    pm.setLabel("TEST LABEL");
    pm.update();
    assertEquals(0, mt.getVersion(), "Version von Mediumtype");
    assertEquals(0, pm.getVersion(), "Version von PictureMedium");
    PicDirectory pd = new PicDirectory();
    pd.setMedium(pm);
    pd.update();
    assertEquals(0, mt.getVersion(), "Version von Mediumtype");
    assertEquals(0, pm.getVersion(), "Version von PictureMedium");
    assertEquals(0, pd.getVersion(), "Version von PicDirectory");
  }

  @Test
  public void testAddPicture() {
    log("Teste Hinzufuegen von DigiPictures");
    PicDirectory pd1 = new PicDirectory();
    pd1.setMedium(m_picMedium1);
    pd1.update();
    long id = pd1.getId();
    DigiPicture dp = new DigiPicture();
    dp.setCamera(m_camera1);
    dp.setFileName("TEST");
    dp.addDirectory(pd1);
    dp.update();
    long dpId = dp.getId();
    DigiPicture t1 = DigiPicture.getById(dpId);
    assertEquals(0, t1.getVersion(), "Version des Pictures");
    PicDirectory pd2 = new PicDirectory();
    pd2.setMedium(m_picMedium1);
    pd2.update();
    dp.addDirectory(pd2);
    dp.update();     // 19082023 wg.: detached entity passed to persist fehler
    long pd2Id = pd2.getId();
    DigiPicture t2 = DigiPicture.getById(dpId);
    assertEquals(1, t2.getVersion(), "Version des Pictures");
    assertEquals(2, t2.getDirectories().size(), "Anzahl Directories for picture");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      PicDirectory d1 = em.find(PicDirectory.class, id);
      Set<DigiPicture> pics1 = d1.getPictures();
      assertEquals(1, pics1.size(), "Anzahl bilder in Directory 1");
      PicDirectory d2 = em.find(PicDirectory.class, pd2Id);
      Set<DigiPicture> pics2 = d2.getPictures();
      assertEquals(1, pics2.size(), "Anzahl bilder in Directory 2");
    }
  }

  /**
   * Ablauf: Anlegen zweier Verzeichnisse und 3 Pictures,
   * Zwei Bilder jeweils in ein Verzeichnis, pic 2 doppelt.
   *
   */
  @Test
  public void testAddPictureToMultupleDirectories() {
    log("Start");
    PicDirectory pDir1 = create_pDir("Dir1", m_picMedium1);
    PicDirectory pDir2 = create_pDir("Dir1", m_picMedium1);
    assertEquals(2, sqlUtil.countElementsInTable("PIC_DIRECTORY"), "Falsche Anzahl PicDirectories");
    DigiPicture pic1 = createDigiPictureVolatile("File1.jpg");
    DigiPicture pic2 = createDigiPictureVolatile("File2.jpg");
    DigiPicture pic3 = createDigiPictureVolatile("File3.jpg");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      em.persist(pic1);
      em.persist(pic2);
      em.persist(pic3);
      pDir1.addPicture(pic1);
      pDir1.addPicture(pic2);
      pDir2.addPicture(pic2);
      pDir2.addPicture(pic3);
      em.persist(em.merge(pDir1));
      em.persist(em.merge(pDir2));
      em.refresh(pic1);
      em.refresh(pic2);
      em.refresh(pic3);
      /**
       * Set<PicDirectory> dirs1 = pic1.getDirectories();
       * assertEquals(1, dirs1.size(), "Number of Directories for pic1 in
       * transaction");
       * Set<PicDirectory> dirs2 = pic2.getDirectories();
       * assertEquals(2, dirs2.size(), "Number of Directories for pic2 in
       * transaction");
       * Set<PicDirectory> dirs3 = pic3.getDirectories();
       * assertEquals(1, dirs3.size(), "Number of Directories for pic3 in
       * transaction");
       * * */
      em.getTransaction().commit();
    }
    countDigiPicture(3);
    countPicDirMap(4);
    countDirectory(2);

    DigiPicture picUnderTest = DigiPicture.getById(1L);
    assertNotNull(picUnderTest, "Could not load picture");
    assertEquals("File1.jpg", picUnderTest.getFileName(), "Wronng pic located");
    Set<PicDirectory> picturest1 = picUnderTest.getDirectories();
    assertEquals(1, picturest1.size(), "Number of directories for picturet1");

    picUnderTest = DigiPicture.getById(2L);
    assertNotNull(picUnderTest, "Could not load picture");
    assertEquals("File2.jpg", picUnderTest.getFileName(), "Wronng pic located");
    picturest1 = picUnderTest.getDirectories();
    assertEquals(2, picturest1.size(), "Number of directories for picture w. ID 2");

    picUnderTest = DigiPicture.getById(3L);
    assertNotNull(picUnderTest, "Could not load picture");
    assertEquals("File3.jpg", picUnderTest.getFileName(), "Wronng pic located");
    picturest1 = picUnderTest.getDirectories();
    assertEquals(1, picturest1.size(), "Number of directories for picture w. ID 3");

    Set<DigiPicture> pictures1 = pDir1.getPictures();
    assertEquals(2, pictures1.size(), "Number of pictures on dir 1");
    Set<DigiPicture> pictures2 = pDir2.getPictures();
    assertEquals(2, pictures2.size(), "Number of pictures on dir 2");

    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
//      em.refresh(pic1);
      DigiPicture pic1managed = em.merge(pic1);
      em.refresh(pic1managed);
      Set<PicDirectory> dirs1 = pic1managed.getDirectories();
      assertEquals(1, dirs1.size(), "Number of Directories for pic1");
      
      DigiPicture pic2managed = em.merge(pic2);
      em.refresh(pic2managed);
      Set<PicDirectory> dirs2 = pic2managed.getDirectories();
      assertEquals(2, dirs2.size(), "Number of Directories for pic2");

      DigiPicture pic3managed = em.merge(pic3);
      em.refresh(pic3managed);
      Set<PicDirectory> dirs3 = pic3managed.getDirectories();
      assertEquals(1, dirs3.size(), "Number of Directories for pic3");
      em.getTransaction().commit();
    }
  }
  
  /**
   * Vereinfachte Variante des LoadProzessors, testet Transactions und commit
   * Verhalten.
   * 
   * hier wird nur ein Verzeichnis angelegt, dann dort eine Anzahl generierter
   * Bilder angefügt. Das ganze in eine Transaktion verpackt.
   */
  @Test
  public void testLoadProcessorSimulationSimple() {
    log ("Start");
    PictureMedium medium = createPictureMedium(101, m_mType1);   // via update to the database (in transaction)
    PicDirectory directory = create_pDir("Root Verzeichnis", medium);  // via update to database
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      for (int picNo = 0; picNo<20; picNo++) {
        DigiPicture pic = new DigiPicture();
        pic.setFileName("file"+picNo+".jpg");
        pic.setPictureTakenMilis(1000000L);
        pic.addDirectory(directory);
        em.persist(pic);
      }
      em.getTransaction().commit();
    }
    
    log ("Noew directory creation within the transaction");
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PicDirectory dir = new PicDirectory();
      dir.setDirectoryName("Neues Root Verzeichnis");
      dir.setMedium(medium);
      em.persist(dir);
      for (int picNo = 0; picNo<20; picNo++) {
        DigiPicture pic = new DigiPicture();
        pic.setFileName("NochnFile"+picNo+".jpg");
        pic.setPictureTakenMilis(1000000L);
        pic.addDirectory(dir);
        em.persist(pic);
      }
      em.getTransaction().commit();
    }
  }
  
  @Test   // - Läuft auf Lock Timeout aus bei mariadb, H2 ok.
      // Nur zur Referenz hier
  public void testLoadProcessorSimulationRecuriv() {
    loadRecursion(m_picMedium1, null, 2);
  }

  @Test
  public void testLoadProcessorSimulationRecuriv2() {
    loadRecursion2(m_picMedium1, null, 2);   // Jeder Run hat 80 neue Entries
    countDigiPicture(240);
    countDirectory(3);
    assertEquals(240, sqlUtil.countElementsInTable("PIC_DIR_MAP"), "Groesse Kreiztabelle");
  }

  @Test
  public void testLoadProcessorSimulationRecuriv3() {
    loadRecursion3(m_picMedium1, null, 2);
  }

  /**
   * uses recursion and creates the folder within the transaction
   * @param level  counts down, if 0, no further recursion
   */
  private void loadRecursion (PictureMedium medium, PicDirectory parentDir, int level) {
    log ("Recursion "+level);
    try (EntityManager em=PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      PicDirectory directory = new PicDirectory();
      directory.setDirectoryName("DirLevel_"+level);
      directory.setParent(parentDir);
      directory.setMedium(medium);
      em.persist(directory);
      for (int picNo = 0; picNo<80; picNo++) {
        DigiPicture pic = new DigiPicture();
        pic.setFileName("NochnFile_L"+level+"_No"+picNo+".jpg");
        pic.setPictureTakenMilis(1000000L+level*100+picNo);
        pic.addDirectory(directory);
        em.persist(pic);
      }
      em.getTransaction().commit();
      if (level >0) loadRecursion(medium, directory, --level);
    }
    
    
  }
  private void loadRecursion2 (PictureMedium medium, PicDirectory parentDir, int level) {
    log ("Recursion "+level+" create folder in extra transaction");
    Searcher searcher = new Searcher();
    PicDirectory directory = create_pDir("DirLevel_"+level, parentDir, medium);
    try (EntityManager em=PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      for (int picNo = 0; picNo<80; picNo++) {
        DigiPicture ptest = searcher.searchPictureByNameAndMilis("TEST", 1234567890L);
        if (ptest != null) {
          System.out.println("Aborting");
          return;
        }
        
        DigiPicture pic = new DigiPicture();
        pic.setFileName("NochnFile_L"+level+"_No"+picNo+".jpg");
        pic.setPictureTakenMilis(1000000L+level*100+picNo);
        pic.addDirectory(directory);
        em.persist(pic);
      }
      if (level >0) loadRecursion2(medium, directory, --level);
      em.getTransaction().commit();
    }
    
    
  }
  private void loadRecursion3 (PictureMedium medium, PicDirectory parentDir, int level) {
    log ("Recursion "+level+" create folder in extra transaction");
    PicDirectory directory = create_pDir("DirLevel_"+level, parentDir, medium);
    try (EntityManager em=PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      for (int picNo = 0; picNo<80; picNo++) {
        DigiPicture pic = new DigiPicture();
        pic.setFileName("NochnFile_L"+level+"_No"+picNo+".jpg");
        pic.setPictureTakenMilis(1000000L+level*100+picNo);
        pic.addDirectory(directory);
        em.persist(pic);
      }
      em.getTransaction().commit();
      if (level >0) loadRecursion3(medium, directory, --level);
    }
  }
}
