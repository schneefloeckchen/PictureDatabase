/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package picdata;

import java.util.List;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import rzx.ui.ZxComboBoxEntry;

/**
 *
 * @todo improve handling from nulls from exif data
 *
 * @author rene
 *
 */
// Named queries
@org.hibernate.annotations.NamedQueries({
  @org.hibernate.annotations.NamedQuery(
      name = "Camera_findByModelAndManufacturer",
      query = "from Camera where model = :modelName and hersteller = :manufacturerName")
})
@Entity
@Table(name = "CAMERA")
public class Camera extends PicDataBaseClass implements PicCatalogItem {

  @Column(name = "MODEL")
  private String model;
  @Column(name = "MANUFACTURER")
  private String hersteller;

  public Camera() {
  }

  public Camera(String model, String hersteller) {
    setModel(model);
    setHersteller(hersteller);
  }

  public String getModel() {
    return model;
  }

  public void setModel(String cameraModel) {
    this.model = cameraModel;
    setDirty();
  }

  public String getHersteller() {
    return hersteller;
  }

  public void setHersteller(String manufacturer) {
    this.hersteller = manufacturer;
    setDirty();
  }

  @Override
  public String toString() {
    return model + " / " + hersteller;
  }

//    public void update() {
//        m_logger.logDebug("Updating ");
//        Session session = PicHibernateUtil.getSessionFactory().openSession();
//        Transaction tx = null;
//        try {
//            tx = session.beginTransaction();
//            session.update(this);
//            tx.commit();
//        } catch (HibernateException e) {
//            if (tx != null)
//                tx.rollback();
//            e.printStackTrace();
//            ZxErrorDialog.displaySimpleErrorMessage(null, "generic.error.hibernateError");
//        } finally {
//            session.close();
//            clearDirty();
//        }
//    }
  // Implementation of the PicCatalogItem interface
  @Override
  public String getEntry(int no) {
    return switch (no) {
      case 0 ->
        getModel();
      case 1 ->
        getHersteller();
      case 2 ->
        getRemark();
      default ->
        " invalid index ";
    };
  }

  @Override
  public void setEntry(String data, int no) {
    setDirty();
    switch (no) {
      case 0 ->
        setModel(data);
      case 1 ->
        setHersteller(data);
      case 2 ->
        setRemark(data);
    }
  }

  @Override
  public List<ZxComboBoxEntry> getKeyValuePairs() {
    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
  }

  public static Camera getById(long id) {
    return (Camera) getById(Camera.class, id);
  }
}
