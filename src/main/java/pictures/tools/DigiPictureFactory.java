package pictures.tools;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import com.drew.metadata.MetadataException;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Blob;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import picdata.Camera;
import picdata.DigiPicture;
import picdata.Searcher;
import rzx.graphics.ExifHelper;
import rzx.graphics.ZxBufferedImage;
import rzx.util.StringHelper;

/**
 * Class is creating a new DigiPicture Object and loads it with the data, creates the thumbnail
 * and loads it in the object as well.
 * These functions will be removed from the DigiPicture class.
 * Expecttation is this, that it reduces the memory footprint of the DigiPicture objects.
 *
 * The class is not a singleton to prepare for parallel execution.
 *
 * @author rene
 */
public class DigiPictureFactory {

  private final Logger m_logger = Logger.getLogger(getClass().getName());
  private final ExifHelper m_exifHelper = new ExifHelper();
  private Searcher m_searcher = new Searcher();
  private String m_fileName = null;
  private String m_folderName = null;
  private String m_fullFileName = null;
  private File m_imageFile = null;

  public DigiPictureFactory() {
  }

  public void loadImageFile(File file) throws Exception {
    m_fileName = file.getName();
    m_folderName = file.getPath();
    m_fullFileName = file.getAbsolutePath();
    createImageFile();
  }

  public void loadImageFile(String directory, String fileName)
          throws Exception {
    m_fileName = fileName;
    m_folderName = directory;
    m_fullFileName = directory + fileName;
    createImageFile();
  }

  private void createImageFile() throws Exception {
    m_imageFile = new File(m_fullFileName);
    m_exifHelper.load(m_imageFile);
  }

  public String getFileName() {
    return m_fileName;
  }

  public long getPictureTakenMillis() {
    return m_exifHelper.getPictureTakenDateSeconds();
  }

  public DigiPicture createDigiPicture(String directory, String fileName) throws
          JpegProcessingException, TiffProcessingException,
          PngProcessingException,
          MetadataException,
          IOException, Exception {
    loadImageFile(directory, fileName);
    return createDigiPicture();
  }

  public DigiPicture createDigiPicture() throws Exception {
    DigiPicture digiPicture = new DigiPicture();
    digiPicture.setFileName(m_fileName);
// Set Metadata of the image from the EXIF Header
    digiPicture.setHeight(m_exifHelper.getPictureHeight());
    digiPicture.setWidth(m_exifHelper.getPictureWidth());
    digiPicture.setOrientation(m_exifHelper.getPictureOrientation());
    // Calculates also the takenMillis
    digiPicture.setPictureTakenDate(m_exifHelper.getPictureTakenDate());
// Check if the camera is already in the database.
// If not, it will be saved during the save of the image in the calling class
    String cameraModel
            = StringHelper.trimToLength(m_exifHelper.getCameraModel(), 64);
    String cameraMaker
            = StringHelper.trimToLength(m_exifHelper.getCameraMaker(), 64);
//    System.out.println(">> Camera is model/maker="+ cameraModel+" / "+cameraMaker);
    Camera camera = m_searcher.searchCameraByModelAndManufacturer(
            cameraModel, cameraMaker);
    if (camera == null)
      camera = new Camera(cameraModel, cameraMaker);
    digiPicture.setCamera(camera);
// Now for the thumbnail
    BufferedImage image;
    try {
      image = ImageIO.read(m_imageFile);  // This is the fullFIleName
      if (image != null) {
        Blob thumb = ZxBufferedImage.createBlobForThumbNail(
                image, DigiPicture.THUMB_SIZE,
                digiPicture.getThumbFormat(),
                digiPicture.getOrientation());
        digiPicture.setThumb(thumb);
      }
    } catch (OutOfMemoryError | NullPointerException ex) {
      m_logger.log(Level.SEVERE,
              "Out of Memory or Error while loading file {0}", m_fileName);
      StatisticCollector.getInstance().addError(m_fileName, "Unknown",
              "Out of Memory or Null pointer Error while loading file " + ex.getLocalizedMessage());
      image = null;
    }

    return digiPicture;
  }
}
