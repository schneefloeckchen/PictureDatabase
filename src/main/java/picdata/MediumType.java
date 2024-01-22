/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package picdata;

//import hib.PicHibernateUtil;
import hib.PicJPAUtil;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Table;
import jakarta.persistence.Query;
import rzx.ui.ZxComboBoxEntry;
// import rzx.ui.ZxMessageDialog;

/**
 * These are the media types, like CD, DVD,
 *
 * @author rene
 */
@Entity
@Table(name = "MEDIUM_TYPE")
public class MediumType extends PicDataBaseClass implements PicCatalogItem {

  @Column(name = "DEPICTION")
  private String depiction;
  @Column(name = "CAPACITY")
  private int capacity;

  public String getDepiction() {
    return depiction;
  }

  public void setDepiction(String depiction) {
    this.depiction = depiction;
    setDirty();
  }

  public int getCapacity() {
    return capacity;
  }

  public void setCapacity(int capacity) {
    this.capacity = capacity;
    setDirty();
  }

  public static List<MediumType> getMediaTypes() {
//        m_logger.logDevelop("loading all MediumType Types");
//    Session session = PicHibernateUtil.getSessionFactory().openSession();
//    session.beginTransaction();
//    List<MediumType> mediaTypes = session.createQuery("from Media").list();
    EntityManager em = PicJPAUtil.getInstance().createEntityManager();
    Query query = em.createQuery("Select from MediumType");
    List<MediumType> mediaTypes = query.getResultList();
    return mediaTypes;
  }

  @Override
  public String getEntry(int no) {
    switch (no) {
      case 0:
        return depiction;
      case 1:
        return Integer.toString(capacity);
      case 2:
        return getRemark();
      default:
        return "Invalid index";
    }
  }

  @Override
  public void setEntry(String data, int no) {
    switch (no) {
      case 0:
        setDepiction(data);
        return;
      case 1:
        setCapacity(Integer.decode(data));
        return;
      case 2:
        setRemark(data);
      default:
    }
  }

  @Override
  public List<ZxComboBoxEntry> getKeyValuePairs() {
//    Session session = PicHibernateUtil.getSessionFactory().openSession();
//    session.beginTransaction();
    EntityManager em = PicJPAUtil.getInstance().createEntityManager();
    Query q = em.createNativeQuery("select media.id, media.depiction from MediumType as media");
    List<Object[]> result = q.getResultList();
    List<ZxComboBoxEntry> mediaTypes = new ArrayList();
    result.forEach((Object[] line) -> {
      mediaTypes.add(new ZxComboBoxEntry(((Long) line[0]).intValue(),
          (String) line[1]));
    });
    return mediaTypes;
  }

  public MediumType loadById(int id) {
//    Session session = PicHibernateUtil.getSessionFactory().openSession();
//    try {
//      session.beginTransaction();
//      return (MediumType) session.load(MediumType.class, id);
//    } catch (HibernateException ex) {
//      ZxMessageDialog.displayExceptionMessage(null, "generic.error.hibernateError", ex);
//      return null;
//    }
    EntityManager em = PicJPAUtil.getInstance().createEntityManager();
    return em.find(MediumType.class, id);
  }

  @Override
  public String toString() {
    return depiction;
  }
}
