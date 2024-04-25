package picdata;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import com.drew.metadata.MetadataException;
// import hib.PicHibernateUtil;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import pictures.tools.StatisticCollector;
import rzx.graphics.ExifHelper;
import rzx.graphics.ZxBufferedImage;

/**
 * represents a digital picture, including thumbnail and jpeg header information
 *
 * The picture can be stored on multiple folders as duplicates, therefore we
 * need a many to many relation to the folder. And therefor we can also not
 * store the ID of the storage medium here.
 *
 *
 * @author rene
 *
 * edit Sept 2023: Update JPA version, hibernate, add equals method
 */
@Entity
@Table(name = "DIGI_PICTURE")
public class DigiPicture extends PicDataBaseClass {

  // Persistent member
  @Column(name = "FILE_NAME")
  private String fileName = null;
  @Column(name = "PICTURE_TAKEN_DATE")
  private Date pictureTakenDate;
  @Column(name = "PICTURE_MILIS")
  private long pictureTakenMilis;
  @Column(name = "THUMB")
  private Blob thumb;
  @Column(name = "THUMB_FORMAT")
  private String thumbFormat = "JPEG";
  @Column(name = "ORIENTATION")
  private int orientation;
  @Column(name = "HEIGHT")
  private int height;
  @Column(name = "WIDTH")
  private int width;
  @ManyToOne
  @JoinColumn(name = "CAMERA")
  private Camera camera = null;
//    private Set<PicDirMap> dirMap = null;     Manuelle Bedienung der nxm Relation
  @ManyToMany(fetch = FetchType.EAGER) // Ansonsten gibt es probleme beim getDirectories (n x m Relation)
  @JoinTable(name = "PIC_DIR_MAP",
      joinColumns = @JoinColumn(name = "DIGI_PICTURE_ID"),
      inverseJoinColumns = @JoinColumn(name = "PIC_DIRECTORY_ID"))
  private Set<PicDirectory> directories = new HashSet();

  // Non persistent members
  @Transient
  private BufferedImage m_image = null;
  // private BufferedImage m_thumb = null;
  @Transient
  public static final int THUMB_SIZE = 550;
  @Transient
  private Searcher m_searcher = null;
  @Transient
  private ExifHelper m_exifHelper = null;

  public DigiPicture() {
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public String getThumbFormat() {
    return thumbFormat;
  }

  public void setThumbFormat(String thumbFormat) {
    this.thumbFormat = thumbFormat;
  }

  public Blob getThumb() {
    return thumb;
  }

  public long getThumbSize() {
    try {
      return thumb.length();
    } catch (SQLException | NullPointerException ex) {
      return 0;
    }
  }

  public BufferedImage getThumbAsBufferedImage() throws SQLException, IOException {
    return ZxBufferedImage.createImageFromBlob(thumb, thumbFormat);
  }

  /**
   * Creates an BufferedImnage object out of the Database Bob,
   * it uses the orientation member to rotate the image according to
   * the value of the orientation member.
   *
   * see http://jpegclub.org/exif_orientation.html
   *
   * so far in the database::
   *
   * -1 : Value not set
   * 0 : no transformation, image is correct
   * 1 : no transformation, image is correct
   * 3 : 180 grad rotate
   * 6 : -90 grad rotate
   * 8 : 90 grad rotate
   *
   * The rotates define the operation to bring the image to the right position.
   *
   * @return
   * @throws SQLException
   * @throws IOException
   */
  public BufferedImage getThumbAsOrientedBufferedImage() throws SQLException, IOException {
    BufferedImage imageFromDb = getThumbAsBufferedImage();
    if (orientation <= 1)
      return imageFromDb;    // No transformation
    else {
      int w, h, xTran, yTran;
      double angle = 0.0;
      switch (orientation) {
        case 3:   // 180 degrees, same height and width
          w = imageFromDb.getWidth();
          h = imageFromDb.getHeight();
          xTran = 0;
          yTran = 0;
          angle = Math.PI;
          break;
        case 6:   // new image has height and width switched
          w = imageFromDb.getHeight();
          h = imageFromDb.getWidth();
          angle = Math.PI / 2.;
          xTran = w - imageFromDb.getWidth();
          yTran = imageFromDb.getHeight() - h;
          break;
        case 8:    // new image has height and width switched
          w = imageFromDb.getHeight();
          h = imageFromDb.getWidth();
          angle = -Math.PI / 2.;
          xTran = imageFromDb.getWidth() - w;
          yTran = h - imageFromDb.getHeight();
          break;
        default:
          m_logger.log(Level.SEVERE, "Unknown Image rorientation for ID={0}  Orientation={1}", new Object[]{id, orientation});
          return imageFromDb;
      }
      BufferedImage rotatedImage = new BufferedImage(w, h, imageFromDb.getType());
      Graphics2D gr = rotatedImage.createGraphics();
      gr.translate(xTran / 2, yTran / 2);
      gr.rotate(angle, w / 2, h / 2);
      gr.drawRenderedImage(imageFromDb, null);
      return rotatedImage;
    }
  }

  public BufferedImage getImage() {
    return m_image;
  }

  public void setThumb(Blob thumb) {
    this.thumb = thumb;
  }

  public Camera getCamera() {
    return camera;
  }

  public void setCamera(Camera camera) {
    this.camera = camera;
  }

  public Date getPictureTakenDate() {
    return pictureTakenDate;
  }

  public void setPictureTakenDate(Date pictureTakenDate) {
    this.pictureTakenDate = pictureTakenDate;
    if (pictureTakenDate == null)
      this.pictureTakenMilis = 0L;
    else
      this.pictureTakenMilis = pictureTakenDate.getTime() / 1000;
  }

  public long getPictureTakenMilis() {
    return pictureTakenMilis;
  }

  public void setPictureTakenMilis(long milis) {
    this.pictureTakenMilis = milis;
  }

  public int getOrientation() {
    return orientation;
  }

  public void setOrientation(int orientation) {
    this.orientation = orientation;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.height = width;
  }

  public Set<PicDirectory> getDirectories() {
    return directories;
  }

  public void setDirectories(Set<PicDirectory> directories) {
    this.directories = directories;
  }

  /**
   * returns true, if the picture is stored on multiple media
   * @return 
   */
  public boolean hasDuplicates() {    // @todo create junit test
    boolean duplicates = (directories.size() > 1);
    return duplicates;
  }

  public void addDirectory(PicDirectory directory) {
    this.directories.add(directory);
  }

  public void removeDirectory(PicDirectory directory) {
    directories.remove(directory);
  }

  public String getPictureTakenDay() {
    if (pictureTakenDate == null)
      return "";
    else
      return new SimpleDateFormat("dd.MMM.yyy").format(pictureTakenDate);
  }

  public String getPictureTakenTime() {
    if (pictureTakenDate == null)
      return "";
    else
      return new SimpleDateFormat("hh:mm:ss").format(pictureTakenDate);
  }

  /**
   * reduced load, just to get filename and date taken out of file and exif
   * data for search of duplicates
   *
   * @param file
   * @throws IOException
   * @throws JpegProcessingException
   * @throws com.drew.imaging.tiff.TiffProcessingException
   * @throws com.drew.imaging.png.PngProcessingException
   */
  public void preLoad(File file) throws IOException, JpegProcessingException, TiffProcessingException, PngProcessingException {
    m_exifHelper = new ExifHelper();
    m_exifHelper.load(file);
    String fName = file.getName().replace("'", "");  // if a apostroph is in the filename,
    setFileName(fName);                              // it would kill an created SQL statement, thanks DonO...
    setPictureTakenDate(m_exifHelper.getPictureTakenDate());
  }

  /**
   * copies the entityManager to the searcher class-
   * Just some repair..
   * @param em the EntityManager which is used in the calling module
   */
  public void pushEntityManagerToSearcher(EntityManager em) {
    if (m_searcher == null)
      m_searcher = new Searcher();
    m_searcher.setEntityManager(em);
  }
  
  public void load(File file) throws JpegProcessingException, IOException, SQLException, TiffProcessingException, PngProcessingException {
    if (m_searcher == null)
      m_searcher = new Searcher();
    setFileName(file.getName());
    try {
      m_image = ImageIO.read(file);
    } catch (OutOfMemoryError | NullPointerException ex) {
      m_logger.log(Level.SEVERE, "Out of Memory or Error while loading file {0}", fileName);
      StatisticCollector.getInstance().addError(fileName, "Unknown",
          "Out of Memory or Null pointer Error while loading file " + ex.getLocalizedMessage());
      m_image = null;
    }
    if (m_exifHelper == null) {
      m_exifHelper = new ExifHelper();
      m_exifHelper.load(file);    // if not done in preload
    }
    setPictureTakenDate(m_exifHelper.getPictureTakenDate());
    try {
      height = m_exifHelper.getPictureHeight();
      width = m_exifHelper.getPictureWidth();
      orientation = m_exifHelper.getPictureOrientation();
      String cameraModel = trimToLength(m_exifHelper.getCameraModel(), 64);
      String cameraMaker = trimToLength(m_exifHelper.getCameraMaker(), 64);
      camera = m_searcher.searchCameraByModelAndManufacturer(cameraModel, cameraMaker);
      if (camera == null)
        camera = new Camera(cameraModel, cameraMaker);
//            m_logger.logDevelop("Orientation: " + m_exifHelper.getPictureOrientation());

    } catch (MetadataException ex) {
      StatisticCollector.getInstance().addError(fileName, "Unknown",
          "Exif Error");
      m_logger.log(Level.SEVERE, "Exif Error in file: {0}", fileName);
    }

// Create blob, need orientation from exif, therefore blob created at the end
    if (m_image != null) {
      thumb = ZxBufferedImage.createBlobForThumbNail(m_image, THUMB_SIZE, thumbFormat, orientation);
      if (thumb == null) {
        m_logger.fine("Thumb is null");
        StatisticCollector.getInstance().addError(fileName, "Unknown",
            "No thumb created");
      } else
        m_logger.log(Level.FINE, "Blob Size is: {0}", thumb.length());
    } else
      m_logger.log(Level.FINE, "image = null for: {0}", fileName);

  }

  public static DigiPicture getById(long id) {
    return (DigiPicture) PicDataBaseClass.getById(DigiPicture.class, id);
  }

  /**
   * basically a wrapper around persist method of the entitymanager.
   * It also checks, if the camera object is new and persists this as well
   * 
   * @param em The EntityManager object from the calling class
   */
  public void persist(EntityManager em) {
    if (camera != null && camera.getId() == -1) {
      m_logger.fine("Persisting CAMERA Object while saving picture");
      em.persist(camera);
    }
    em.persist(this);
  }
  /**
   * Adds the check if we got a new camera object to the update method. Using
   * cascade = all or similar re-saves existing camera object in all cases,
   * which adds IO overhead and increases the version counter.
   */
  @Override
  public void update() {
    if (camera != null && camera.getId() == -1) {
      m_logger.fine("Creating CAMERA Object while saving picture");
      camera.update();
    }
    super.update();
  }

  @Override
  public void updateInSession() {
    if (camera != null && camera.getId() == -1) {
      m_logger.fine("Creating CAMERA Object while saving picture");
      camera.updateInSession();
    }
    super.updateInSession();
  }

  @Override
  public boolean equals(Object object) {
    if (!(object instanceof DigiPicture))
      return false;
    DigiPicture other = (DigiPicture) object;
    if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id)))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "DigiPicture[ id=" + id + " ]";
  }

}
