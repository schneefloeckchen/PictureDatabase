package hib;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.Query;
import java.util.Map;

/**
 * Singelton to connect with the database and provide EntityManagerFactory and
 * EntityManager objects.
 *
 * for PictureDatabase V2
 *
 * @author rene
 * Aug 23: initially created
 */
public class PicJPAUtil {

  private EntityManagerFactory m_entityManagerFactory = null;
  
  private String m_driverUrl = "";
  private String m_userName = "";
  private String m_userPwd = "";
  private String m_truncateCommand ="";     // Command to reset the ID Generation
  private String m_ignoreForeignKeysCommand = "";
  private Map<String, Object> m_properties = null;  // Propert. of the Persistence
  
  private PicJPAUtil() {
  }

  public static PicJPAUtil getInstance() {
    return PicJPAUtilHolder.INSTANCE;
  }

  /**
   * creates the entityManagerFactory using the configuration from
   * persistence.xml. COnfiguration is ignored, if the entityManagerFactory
   * already exists.
   *
   * @param configuration name of the configuration as used in persistence.xml
   */
  public void configure(String configuration) {
    if (m_entityManagerFactory == null) {
      System.out.println("\nPicJPAUtil -- configuration is " + configuration);
      m_entityManagerFactory = Persistence.createEntityManagerFactory(configuration);
      m_properties = m_entityManagerFactory.getProperties();
      m_driverUrl = loadProperty("hibernate.connection.url");
      if (m_driverUrl.contains("mariadb")) {        // check for memory database in test process
        m_userName = "rene";
        m_userPwd = "";
      } else {
        m_userName = "sa";
        m_userPwd = "sa";
      }
      m_truncateCommand = loadProperty("rzi.truncate.sql");
      m_ignoreForeignKeysCommand = loadProperty("rzi.ignoreForeignKeys.sql");
    }
  }

  private String loadProperty (String key) {
    Object obj = m_properties.get(key);
    if (obj == null) {
      System.err.println ("Cannot load property "+key);
      return null;
    } else
      return obj.toString();
  }

  public String getDriverUrl() {
    return m_driverUrl;
  }

  public String getUserName() {
    return m_userName;
  }

  public String getUserPwd() {
    return m_userPwd;
  }

  public String getTruncateCommand() {
    return m_truncateCommand;
  }
  
  public String getIgnoreForeignKeysCommand() {
    return m_ignoreForeignKeysCommand;
  }
  
  /**
   * returns the already created EntityManagerFactory. This factory is unique
   * and created only once.
   *
   * @return
   */
  public EntityManagerFactory getEntityManagerFactory() {
    return m_entityManagerFactory;
  }

  /**
   * returns a new EntityManager
   *
   * @return
   */
  public EntityManager createEntityManager() {
    return m_entityManagerFactory.createEntityManager();
  }

  /**
   * Deletes all records from a given clazz.
   * 
   * @param clazz jpa clazz name representing the table.
   */
  public void deleteTableContent(String clazz) {
    try (EntityManager em = m_entityManagerFactory.createEntityManager()) {
      em.getTransaction().begin();
      Query query = em.createQuery("DELETE FROM " + clazz);
      query.executeUpdate();
      em.getTransaction().commit();
    }
    
  }

  private static class PicJPAUtilHolder {

    static final PicJPAUtil INSTANCE = new PicJPAUtil();
  }
}
