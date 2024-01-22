package picdata;

import bas.SQLUtil;
import bas.TestBase;
import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Stellt Methoden fuer die Tests zur Verfuegung
 * Keinen Bezug zu jUnit Versionen
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
  protected Camera m_camera1 = null;

  protected PicJPAUtil pju = PicJPAUtil.getInstance();
  protected SQLUtil sqlUtil = new SQLUtil();
  
  public TestBaseClass() {
  }

  protected void setUpBaseData() {
    m_mType1 = createMediumType(1);
    m_mType2 = createMediumType(2);
    m_mType3 = createMediumType(3);
    m_mType4 = createMediumType(4);
    m_picMedium1 = createPictureMedium(101, m_mType1);
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
    camera.update();
    camera.setRemark("Created during setup");
    return camera;
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

  protected DigiPicture createDigiPicture() {
    DigiPicture pic = createDigiPictureVolatile();
    pic.update();
    return pic;
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
   * @param expectedCount 
   */
  protected void countPicDirMap(int expectedCount) {
    countRecords("PIC_DIR_MAP", expectedCount);
  }

  protected void countDigiPicture(int expectedCount) {
    countRecords("DIGI_PICTURE", expectedCount);
  }

  protected void countDirectory(int expectedCount) {
    countRecords("PIC_DIRECTORY", expectedCount);
  }
  
  protected void countMedium(int expectedCount) {
    countRecords("PICTURE_MEDIUM", expectedCount);
  }

  protected void countRecords(String tableName, int expectedCount) {
    int countValue = countRecords(tableName);
    assertEquals(expectedCount, countValue, "Expected records in " + tableName+" not found");
  }

  protected int countRecords(String tableName) {
    /*    Session session = PicHibernateUtil.getSessionFactory().openSession();
    Query query = session.createSQLQuery("Select count(*) from " + tableName);
    Object count = query.getSingleResult();
    Session session = PicHibernateUtil.getSessionFactory().openSession();
     */
    int countValue ;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery("Select count(*) from " + tableName);
      Object count = query.getSingleResult();
      countValue = ((Long) count).intValue();
    }
    return countValue;
  }

  protected void print(String message) {
    System.out.println(" >> TestRun -- " + message);
  }
}
