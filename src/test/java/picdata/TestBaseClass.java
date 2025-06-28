package picdata;

import bas.SQLUtil;
import bas.TestBase;
import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stellt Methoden fuer die Tests zur Verfuegung Keinen Bezug zu jUnit Versionen
 *
 * @author rene
 */
public class TestBaseClass extends TestBase {

  protected MediumType m_mType1 = null;
  protected MediumType m_mType2 = null;
  protected MediumType m_mType3 = null;
  protected MediumType m_mType4 = null;
  protected PictureMedium m_picMedium1 = null;
  protected PictureMedium m_picMedium2 = null;
  protected PictureMedium m_picMedium3 = null;
  protected long m_picMedium1Id = 0L;
  protected Camera m_camera1 = null;

  protected PicJPAUtil m_picJPAUtil = PicJPAUtil.getInstance();
  protected SQLUtil sqlUtil = new SQLUtil();

  public TestBaseClass() {
  }

  protected void setUpBaseData() {
    m_mType1 = createMediumType(1);
    m_mType2 = createMediumType(2);
    m_mType3 = createMediumType(3);
    m_mType4 = createMediumType(4);
    m_picMedium1 = createPictureMedium(101, m_mType1);
    m_picMedium1Id = m_picMedium1.getId();
    m_picMedium2 = createPictureMedium(102, m_mType1);
    m_picMedium3 = createPictureMedium(103, m_mType1);
    m_camera1 = createCamera();
  }

  protected MediumType createMediumType(int no) {
    MediumType mt = new MediumType();
    mt.setDepiction("Type" + no);
    mt.setRemark("Created during setup");
    mt.update();
    return mt;
  }

  protected MediumType createMediumType(String depict) {
    MediumType mt = new MediumType();
    mt.setDepiction(depict);
    mt.update();
    return mt;
  }

  protected PictureMedium createPictureMedium(int code, MediumType mType) {
    PictureMedium pm = new PictureMedium();
    pm.setStorageMedium(mType);
    pm.setCode(code);
    pm.setLabel("Label" + code);
    pm.setRemark("Created during setup");
    pm.update();
    return pm;
  }

  protected Camera createCamera() {
    Camera camera = new Camera();
    camera.setModel("Modell1");
    camera.setRemark("Created during setup");
    camera.update();
    return camera;
  }

  protected Camera createCamera(String manufacturer, String model) {
    Camera camera = new Camera();
    camera.setModel(model);
    camera.setHersteller(manufacturer);
    camera.update();
    return camera;
  }

  protected PictureMedium createPictureMedium(String title, String label, String content) {
    PictureMedium medium = new PictureMedium();
    medium.setTitle(title);
    medium.setLabel(label);
    medium.setContent(content);
    medium.update();
    return medium;
  }

  protected PictureMedium createPictureMedium(int code, String title, String label) {
    PictureMedium medium = new PictureMedium();
    medium.setTitle(title);
    medium.setLabel(label);
    medium.setCode(code);
    medium.update();
    return medium;
  }

  protected DigiPicture createDigiPictureVolatile() {
    return createDigiPictureVolatile("T1.jpg");
  }

  /**
   * creates a DigiPicture object, which is not saved to the database an
   * therefore not yet managed by the persistent layer.
   *
   * @param fileName
   * @return the created object
   */
  protected DigiPicture createDigiPictureVolatile(String fileName) {
    DigiPicture pic = new DigiPicture();
    pic.setFileName(fileName);
    pic.setPictureTakenMilis(10000);
    pic.setCamera(m_camera1);
    pic.setRemark("Created during setup");
    return pic;
  }

  protected DigiPicture createDigiPicture(Camera camera, String FileName, long millis) {
    DigiPicture pic = new DigiPicture();
    pic.setCamera(camera);
    pic.setFileName(FileName);
    pic.setPictureTakenMilis(millis);
    pic.update();
    return pic;
  }

  protected DigiPicture createDigiPicture() {
    DigiPicture pic = createDigiPictureVolatile();
    pic.update();
    return pic;
  }

  protected PicDirectory createPicDirectory(String name) {
    PicDirectory picDir = new PicDirectory();
    picDir.setDirectoryName(name);
    picDir.update();
    return picDir;
  }

  protected PicDirectory create_pDir(String name, PictureMedium medium) {
    PicDirectory pDir = new PicDirectory();
    pDir.setDirectoryName(name);
    pDir.setMedium(medium);
    pDir.update();
    return pDir;
  }

  protected PicDirectory create_pDir(String name,
          PicDirectory parent, PictureMedium medium) {
    PicDirectory pDir = new PicDirectory();
    pDir.setDirectoryName(name);
    pDir.setMedium(medium);
    pDir.setParent(parent);
    pDir.update();
    return pDir;
  }

  /**
   * Checks, if the PicDirMap has the expected number of elements
   *
   * @param expectedCount
   */
  protected void countPicDirMap(int expectedCount) {
    countRecords("PIC_DIR_MAP", expectedCount);
  }

  /**
   * Test correct number of objects in PIC_DIR_MAP
   * @param expectedCount
   * @param folder test data folder, just for the error message
   */
  protected void countPicDirMap(int expectedCount, String folder) {
    countRecords("PIC_DIR_MAP", folder, expectedCount);
  }

  protected void countDigiPicture(int expectedCount) {
    countRecords("DIGI_PICTURE", expectedCount);
  }

  protected void countDigiPicture(int expectedCount, String folder) {
    countRecords("DIGI_PICTURE", folder, expectedCount);
  }

  protected void countDirectory(int expectedCount) {
    countRecords("PIC_DIRECTORY", expectedCount);
  }

  protected void countDirectory(int expectedCount, String folder) {
    countRecords("PIC_DIRECTORY", folder, expectedCount);
  }

  protected void countMedium(int expectedCount) {
    countRecords("PICTURE_MEDIUM", expectedCount);
  }

  protected void countRecords(String tableName,
          int expectedCount) {
    int countValue = countRecords(tableName);
    assertEquals(expectedCount, countValue, "Expected records in "
            + tableName + " not found");
  }

  protected void countRecords(String tableName, String importFolder, 
          int expectedCount) {
    int countValue = countRecords(tableName);
    assertEquals(expectedCount, countValue, "Expected records in "
            + tableName + " not found in "+importFolder);
  }

  /**
   * Count the number of DigiImageObjects (Records in database) where the field THUMB is null, which
   * shows images where an thumbnail could not be created
   *
   * @return Number of records where no thumbnail was created
   */
  protected int countEmptyImages() {
    int countValue;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery(
              "Select count(*) from DIGI_PICTURE WHERE THUMB IS NULL");
      Object count = query.getSingleResult();
      countValue = ((Long) count).intValue();
    }
    return countValue;
  }

  /**
   * checks, if no empty images are there
   */
  protected void checkForEmptyImages() {
    assertEquals(0, countEmptyImages(),
            "Unexpected empty images (THUMB = null) found");
  }

  protected void countEmptyImages(int expectedCount) {
    int countValue = countEmptyImages();
    assertEquals(expectedCount, countValue, "Images with empty thumb entry");
  }

  protected void countCamera(int expectedCount) {
    countRecords("CAMERA", expectedCount);
  }

  protected void countCamera(int expectedCount, String folder) {
    countRecords("CAMERA", folder, expectedCount);
  }

  protected int countRecords(String tableName) {
    /*    Session session = PicHibernateUtil.getSessionFactory().openSession();
    Query query = session.createSQLQuery("Select count(*) from " + tableName);
    Object count = query.getSingleResult();
    Session session = PicHibernateUtil.getSessionFactory().openSession();
     */
    int countValue;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery("Select count(*) from " + tableName);
      Object count = query.getSingleResult();
      countValue = ((Long) count).intValue();
    }
    return countValue;
  }

  /**
   * Method validates if in Set of DigiPictures all Filenames of a given List are used.
   *
   * @return
   */
  protected void validateAllFileNames(Set<DigiPicture> pictures,
          String[] fileNames) {
    Set<String> expectedFileNames
            = new HashSet(Arrays.asList(fileNames));
    assertEquals(pictures.size(), expectedFileNames.size(),
            "Set of Pictures and FIlenames shall have at least the same size");
    for (DigiPicture pic : pictures) {
      String f = pic.getFileName();
      if (expectedFileNames.contains(f))
        expectedFileNames.remove(f);
      else
        assertFalse (true, "Filename "+f+" not found in expected list");
    }
    assertEquals (0, expectedFileNames.size(), "Not all Expected Filenames found");
  }
  
  /**
   * determines the root folder of the provided medium.
   * if a root folder cannot be found null is returned
   * If multiple roots are there the first one is returned. Should not happen.
   * @param medium to search for the root folder
   * @return 
   */
  protected PicDirectory getRootFolder(PictureMedium medium) {
    Set<PicDirectory> dirs = medium.getPicDirectories();
    PicDirectory root = null;
    for (PicDirectory dir : dirs) 
      if (dir.getParent() == null) return dir;
    return null;
  }
  
  protected void print(String message) {
    System.out.println(" >> TestRun -- " + message);
  }
}
