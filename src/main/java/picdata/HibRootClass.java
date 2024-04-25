package picdata;

//import hib.PicHibernateUtil;
import hib.PicJPAUtil;
import java.io.Serializable;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import jakarta.persistence.Version;
import jakarta.persistence.EntityManager;
//import org.hibernate.HibernateException;
//import org.hibernate.Session;
//import org.hibernate.Transaction;
import java.util.logging.Logger;

/**
 *
 * @author rene
 */
@MappedSuperclass
public abstract class HibRootClass implements Serializable {

  @Transient
  protected Logger m_logger = Logger.getLogger(getClass().getName());

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "ID")
  protected Long id = -1L;
  @Version
  @Column(name = "VERSION")
  Integer version;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public void setVersion(int version) {
    this.version = version;
  }

  public int getVersion() {
    return version;
  }

  protected String trimToLength(String in, int length) {
    String sTmp = in.length() < length ? in : in.substring(0, length - 1);
    return sTmp.trim();
  }

  // Updates the entry within a session
  public void updateInSession() {
    updateInSession(PicJPAUtil.getInstance().createEntityManager());
  }

//    Old code using legacy hibernate session
//    To JPA EntityManager transformed
/*    
    public void updateInSession(Session session) {
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            session.saveOrUpdate(this);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null)
                tx.rollback();
            ZxErrorDialog.displaySimpleErrorMessage(null, "generic.error.hibernateError");
        }
    }
   */
  public void updateInSession(EntityManager em) {
    try (em) {
      em.getTransaction().begin();
      em.persist(this);
      em.getTransaction().commit();
//      em.getTransaction().close();
    }
  }

  /*    public void update() {
      try (Session session = PicHibernateUtil.getSessionFactory().openSession()) {
        updateInSession(session);
      }
    }
   */
  /**
   * saves the object to the database using an EntityManager.
   * If the object was not loaded from the database previously (id still not
   * set),
   * it is created new in the database. Otherwise the existing object in the
   * database is updated. prior to update is has to ensure that the item is
   * managed by the EntityManager
   *
   */
  public void update() {
    PicJPAUtil putil = PicJPAUtil.getInstance();
    Object o;
    try (EntityManager em = putil.createEntityManager()) {
      em.getTransaction().begin();
      if (id < 0L) // Create a new entry, if id is -1
      {
        em.persist(this);
      } else // if already there, update the object.
      if (em.contains(this)) {
        em.persist(this);   // if already managed by the EM
      } else {
        Object o_ = em.merge(this);
        em.persist(o_); // @todo: validate this, merge creates a new object,
        // which is linked to the transaction. Ever used? yes, but isn't working
      }
      em.getTransaction().commit();
    }
  }

  /* old code
  public void update() {
    PicJPAUtil putil = PicJPAUtil.getInstance();
    try (EntityManager em = putil.createEntityManager()) {
      em.getTransaction().begin();
      if (id < 0L)             // Create a new entry, if id is -1
        em.persist(this);
      else                     // if already there, update the object.
        em.merge(this);        // @todo: validate this, merge creates a new object,
                                // which is linked to the transaction. Ever used?
      em.getTransaction().commit();
    }
  } */

 /*  public void refresh() {
    try (Session session = PicHibernateUtil.getSessionFactory().openSession()) {
      session.refresh(this);
    }
  }*/
  public static Object getById(Class clazz, Long id) {
    Object obj;
    /*    try (Session session = PicHibernateUtil.getSessionFactory().openSession()) {
      obj = session.get(clazz, id);
      session.close();
    }
     */
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      obj = em.find(clazz, id);
    }
    return obj;
  }
}
