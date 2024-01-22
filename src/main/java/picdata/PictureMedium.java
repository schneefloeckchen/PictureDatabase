package picdata;

//import hib.PicHibernateUtil;
import hib.PicJPAUtil;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import jakarta.persistence.EntityManager;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Query;
//import org.hibernate.Criteria;
//  import org.hibernate.Query;
// import org.hibernate.Session;
//import org.hibernate.criterion.Projections;
import rzx.ui.ZxComboBoxEntry;

/**
 * Stores the information for a storage storageMedium (CD, DVD; ..) ID is
 * defined in the BaseClass
 *
 * @todo load storage medium EAGER
 *
 * @author rene
 *
 */
@Entity
@Table(name = "PICTURE_MEDIUM")
public class PictureMedium extends PicDataBaseClass {

  @Column(name = "CODE")
  private int code = 0;              // Code Nr. as written on the storageMedium
  @Column(name = "LABEL")
  private String label = "";       // Label of the storageMedium, as written w.the burner
  @Column(name = "TITLE")
  private String title = "";       // As written with pen on Medium
  @Column(name = "CONTENT")
  private String content = "";     // Content of Medium
  @Column(name = "DATE_WRITTEN")
  private Date dateWritten = null; // When the Medium was burned
  @Column(name = "WRITTEN_BY")
  private String writtenBy = "";   // Who has burned the storageMedium
  @ManyToOne
  @JoinColumn(name = "STORAGE_MEDIUM")
  @OrderBy("ID")
  private MediumType storageMedium = null;     // CD, DVD or the like
  @OneToMany(targetEntity = PicDirectory.class, cascade = CascadeType.REMOVE) //, fetch = FetchType.EAGER)
  @JoinColumn(name = "MEDIUM")
  private Set<PicDirectory> picDirectories;

  public PictureMedium() {

  }

  public MediumType getStorageMedium() {
    return storageMedium;
  }

  public void setStorageMedium(MediumType medium) {
    this.storageMedium = medium;
    setDirty();
  }

  public void setStorageMediumId(long id) {
//    try ( Session session = PicHibernateUtil.getSessionFactory().openSession()) {
//      storageMedium = (MediumType) session.load(MediumType.class, id);
//    }
    throw new UnsupportedOperationException();
  }

  public int getCode() {
    return code;
  }

  public void setCode(int code) {
    this.code = code;
    setDirty();
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
    setDirty();
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
    setDirty();
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
    setDirty();
  }

  public Date getDateWritten() {
    return dateWritten;
  }

  public void setDateWritten(Date dateWritten) {
    this.dateWritten = dateWritten;
    setDirty();
  }

  public String getWrittenBy() {
    return writtenBy;
  }

  public void setWrittenBy(String writtenBy) {
    this.writtenBy = writtenBy;
    setDirty();
  }

  public void removePicDirectory(PicDirectory dir) {
    picDirectories.remove(dir);
  }

  public Set<PicDirectory> getPicDirectories() {
    return picDirectories;
  }

  public static List<PictureMedium> getAllStorageMedia() {
    List<PictureMedium> mediaList;
//    try ( Session session = PicHibernateUtil.getSessionFactory().openSession()) {
//      session.beginTransaction();
//      mediaList = session.createQuery("from PictureMedium").list();
//    }
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      mediaList = em.createQuery("Select p from PictureMedium p").getResultList();
    }
    return mediaList;
  }

  public static PictureMedium getById(long id) {
    return (PictureMedium) HibRootClass.getById(PictureMedium.class, id);
  }

  /**
   * Creates a new code for an PitureMedium as maximum of existing codes plus
   * 1.
   *
   * @return created new code
   */
  public static int getNewCode() {
    /*    Session session = PicHibernateUtil.getSessionFactory().openSession();
    Criteria criteria = session.createCriteria(PictureMedium.class);
    criteria.setProjection(Projections.max("code"));
    Integer maxCode = (Integer) criteria.uniqueResult();
    try {
      return maxCode + 1;
    } catch (NullPointerException ex) {
      return 100;
    }*/
    EntityManager em = PicJPAUtil.getInstance().createEntityManager();
    Query query = em.createNativeQuery("select MAX(code) from PICTURE_MEDIUM");
    return (Integer) query.getSingleResult() + 1;
  }

  public List<ZxComboBoxEntry> getKeyValuePairs() {
    List<ZxComboBoxEntry> media;
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      Query query = em.createNativeQuery("select media.id, media.title from PICTURE_MEDIUM as media");
      List<Object[]> result = query.getResultList();
      media = new ArrayList();
      result.forEach((Object[] line) -> {
        media.add(new ZxComboBoxEntry(((Long) line[0]).intValue(), (String) line[1]));
      });
    }
    return media;
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PictureMedium))
      return false;
    PictureMedium other = (PictureMedium) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "PictureMedium[ id=" + id + " ]";
  }
}
