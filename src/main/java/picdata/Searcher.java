package picdata;

// import hib.PicHibernateUtil;
import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.NonUniqueResultException;
import jakarta.persistence.Query;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sammlung aller search Funktionen, kein Singelton, da wir ggf das ganze in
 * verschiedene Threads packen wollen
 *
 * https://www.baeldung.com/hibernate-named-query Infos for precompiled queries.
 *
 * @author rene
 * @todo Add SearchPicturesWithNameAndMilis - result as List to hunt for
 * duplicates
 *
 * AUg 2023 - migrate to JPA 2.2, Hibernate Implementation, JDK 18, and from
 * Session to EntityManager
 */
public class Searcher {
  private Logger m_logger = Logger.getLogger(getClass().getName());

//  Session m_session = null;
  private Query m_cameraFindByModelAndManufacturerQuery = null;
  private EntityManager m_entityManager = null;

  public Searcher() {
//    m_session = PicHibernateUtil.getSession();
  }

  public void setEntityManager(EntityManager em) {
    m_entityManager = em;
    m_cameraFindByModelAndManufacturerQuery = m_entityManager.createNamedQuery(
        "Camera_findByModelAndManufacturer", Camera.class);
  }

  /**
   * Locates the first entry of the camera/hersteller pair in the Camera table
   *
   * @param model
   * @param manufacturer
   * @return first hit, or null is nothing was found.
   */
  public Camera searchCameraByModelAndManufacturer(String model, String manufacturer) {
    if (m_entityManager == null)
      setEntityManager(PicJPAUtil.getInstance().createEntityManager());
    m_cameraFindByModelAndManufacturerQuery.setParameter("modelName", model);
    m_cameraFindByModelAndManufacturerQuery.setParameter("manufacturerName", manufacturer);
    try {
      Camera result = (Camera) m_cameraFindByModelAndManufacturerQuery.getSingleResult();
      return result;
    } catch (NoResultException ex) {
      return null;
    } catch (NonUniqueResultException ex) {
      m_logger.log(Level.SEVERE,
          "Multiple Entries found for {0} / {1}", new Object[]{model, manufacturer});
      List<Camera>cameras = m_cameraFindByModelAndManufacturerQuery.getResultList();
      return cameras.getFirst();
    }
  }

  /**
   * Searches a picture by its filename and date, when it was taken. It uses
   * the session, which is globally managed from the HibernateUtil class
   *
   * @param name filename of the picture
   * @param date date, when it was taken, as loaded from the EXIF Data
   *
   * @return
   *
   * @todo optimize performance, can precompiled query be used?
   */
  public DigiPicture searchPictureByNameAndMilis(String name, long date) {
    String name2 = name.replace("'", "");      // if a ' is in the file name
    if (m_entityManager == null)
      m_entityManager = PicJPAUtil.getInstance().createEntityManager();
//    Query query = em.createNativeQuery(
//        "from DIGI_PICTURE where FILE_NAME='" + name2 + "' and PICTURE_MILIS=" + date)
    Query query = m_entityManager.createQuery("Select p from DigiPicture p where p.fileName='" + name2
        + "' and p.pictureTakenMilis=" + date);
    try {
      DigiPicture pic = (DigiPicture) query.getSingleResult();
      return pic;
    } catch (NoResultException ex) {
      return null;
    }
  }

  /*public List<DigiPicture> seachPicturesByName(String name) {
    String name2 = name.replace("'", "");      // if a ' is in the file name
    Query query = m_session.createQuery(
        "from DigiPicture where FILE_NAME='" + name2 + "'");
    try {
      List<DigiPicture> pic = query.getResultList();
      return pic;
    } catch (NoResultException ex) {
      return null;
    }
  } */

 /* public PictureMedium searchPictureMediumByCode(int code) {
    String queryString = "from PictureMedium m where m.code=" + code;
    Query query = m_session.createQuery(queryString);
    try {
      PictureMedium medium = (PictureMedium) query.getSingleResult();
      return medium;
    } catch (NoResultException ex) {
      return null;
    }
  }*/

 /*public List<PictureMedium> searchPictureMediumsByTitle(String title) {
    String queryString
        = "from PictureMedium m where m.title like \"" + title + "\"";
    return searchPictureMediums(queryString);
  }*/

 /* public List<PictureMedium> searchPictureMediumsByLabel(String label) {
    String queryString
        = "from PictureMedium m where m.label like \"" + label + "\"";
    return searchPictureMediums(queryString);
  } */

 /*public List<PictureMedium> searchPictureMediumsByContent(String content) {
    String queryString
        = "from PictureMedium m where m.content like \"" + content + "\"";
    return searchPictureMediums(queryString);
  }*/

 /* private List<PictureMedium> searchPictureMediums(String queryString) {
    Query query = m_session.createQuery(queryString);
    try {
//          List<PictureMedium> result = 
      return query.getResultList();
    } catch (NoResultException ex) {
      return null;
    }
  }*/

 /*public List<PicDirectory> searchPictureDirectory(String name) {
    String queryString
        = "from PicDirectory p where p.directoryName like \"" + name + "\"";
    Query query = m_session.createQuery(queryString);
    try {
      return query.getResultList();
    } catch (NoResultException ex) {
      return null;
    }
  }*/
}
