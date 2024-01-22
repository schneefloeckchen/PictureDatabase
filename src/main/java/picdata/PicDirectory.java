package picdata;

//import hib.PicHibernateUtil;
import hib.PicJPAUtil;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Query;
import jakarta.persistence.Table;
import jakarta.transaction.Transactional;

/**
 * Directories on the data medium. The base directory is defined by parent =
 * null;
 *
 * @author rene
 */
@Entity
@Table(name = "PIC_DIRECTORY")
    @NamedQuery
      (query = "SELECT p from PicDirectory p WHERE p.medium.id = :id",
          name = "FindPicDirectoryByMediumID")
public class PicDirectory extends PicDataBaseClass {

    @Column(name = "DIRECTORY_NAME")
    private String directoryName;
    @Column(name = "DESCRIPTION")
    private String description;
    @ManyToMany
    @JoinTable(name = "PIC_DIR_MAP",
            joinColumns = @JoinColumn(name = "PIC_DIRECTORY_ID"),
            inverseJoinColumns = @JoinColumn(name = "DIGI_PICTURE_ID"))
//    @OrderBy("ID")
    @OrderBy("PICTURE_MILIS")
    private Set<DigiPicture> pictures = new HashSet();
    @ManyToOne
    @JoinColumn(name = "PARENT")
    private PicDirectory parent;    // If root, = null.
    @OneToMany (targetEntity = PicDirectory.class)
    @JoinColumn(name = "PARENT")
    private Set<PicDirectory> children;
    @ManyToOne
    @JoinColumn(name = "MEDIUM")
    private PictureMedium medium;       // where it is stored

    public PicDirectory() {
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public Set<DigiPicture> getPictures() {
        return pictures;
    }

    public void setPictures(Set<DigiPicture> pictures) {
        this.pictures = pictures;
    }

    @Transactional
    public void addPicture(DigiPicture picture) {
        pictures.add(picture);
    }
    
    public void removePicture(DigiPicture picture) {
        pictures.remove(picture);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public PicDirectory getParent() {
        return parent;
    }

    public void setParent(PicDirectory parent) {
        this.parent = parent;
    }

    public Set<PicDirectory> getChildren() {
        return children;
    }
    public PictureMedium getMedium() {
        return medium;
    }

    public void setMedium(PictureMedium medium) {
        this.medium = medium;
    }

    @Override
    public String toString() {
        return directoryName;
    }

    /**
     * creates a list of the directories, which are on selected storage medium
     *
     * @param id of the storage medium
     * @return
     */
    public static List<PicDirectory> getPicDirectoryListByStorageMedium(long id) {
//        Session session = PicHibernateUtil.getSessionFactory().openSession();
//        session.beginTransaction();
        EntityManager em = PicJPAUtil.getInstance().createEntityManager();
        Query query = em.createNamedQuery("FindPicDirectoryByMediumID");
        query.setParameter("id", id);
        return query.getResultList();
//        List<PicDirectory> resultList = em.createQuery(
//            "from PicDirectory where MEDIUM=" + id).list();
//        return resultList;
    }

/*    public static List<PicDirectory> getChildDirectoriesById(int id) {
        Session session = PicHibernateUtil.getSessionFactory().openSession();
        Transaction transaction = session.beginTransaction();
        List<PicDirectory> resultList = session.createQuery("from PicDirectory where PARENT=" + id).list();
        transaction.commit();
        session.close();
        return resultList;
    }*/

    public static PicDirectory getById(long id) {
        return (PicDirectory) HibRootClass.getById(PicDirectory.class, id);
    }
    
  @Override
  public boolean equals(Object object) {
    if (!(object instanceof PicDirectory))
      return false;
    PicDirectory other = (PicDirectory) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
      return false;
    return true;
  }


}
