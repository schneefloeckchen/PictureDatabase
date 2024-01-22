package rzx.graphics;

import com.drew.imaging.bmp.BmpMetadataReader;
import com.drew.imaging.jpeg.JpegMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngMetadataReader;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffMetadataReader;
import com.drew.imaging.tiff.TiffProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
// import com.drew.metadata.file.FileMetadataDirectory;    // Not available in the current implementation, and not used in this software
import com.drew.metadata.jpeg.JpegDirectory;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Just a little capsule around Drew's JPEG extractor
 *
 * @author rene
 */
public class ExifHelper {

  private Metadata m_metadata = null;
  private com.drew.metadata.jpeg.JpegDirectory m_jepegDirectory = null;
  private com.drew.metadata.exif.ExifIFD0Directory m_exifIFD0Directory = null;
  private com.drew.metadata.exif.ExifSubIFDDirectory m_exifSubIFDDirectory = null;
  private com.drew.metadata.exif.makernotes.CanonMakernoteDirectory m_canonMakernoteDirectory = null;
  private com.drew.metadata.exif.ExifInteropDirectory m_exifInteropDirectory = null;
  private com.drew.metadata.exif.GpsDirectory m_gpsDirectory = null;   // for later use
  private com.drew.metadata.exif.ExifThumbnailDirectory m_exifThumbnailDirectory = null;
  private com.drew.metadata.xmp.XmpDirectory m_xmpDirectory = null;
  private com.drew.metadata.jpeg.HuffmanTablesDirectory m_huffmanTablesDirectory = null;
//    private com.drew.metadata.file.FileMetadataDirectory m_fileMetadataDirectory = null;

  private final Logger m_logger = Logger.getLogger(getClass().getName());
  private File m_file = null;
  private String m_fileName = null;

  public ExifHelper() {
    m_metadata = null;
  }

  public void load(File file) throws JpegProcessingException, IOException, TiffProcessingException, PngProcessingException {
    m_file = file;
    m_fileName = m_file.getName();
    resetDirectories();     // Clear data from presious file.
    String f = m_fileName.toLowerCase();
    if (f.endsWith(".jpeg") || f.endsWith(".jpg"))
      m_metadata = JpegMetadataReader.readMetadata(m_file);
    else if (f.endsWith(".tiff") || f.endsWith(".tif"))
      m_metadata = TiffMetadataReader.readMetadata(m_file);
    else if (f.endsWith(".bmp"))
      m_metadata = BmpMetadataReader.readMetadata(m_file);
    else if (f.endsWith(".png"))
      m_metadata = PngMetadataReader.readMetadata(file);
    else
      m_metadata = null;
  }

  public int getPictureHeight() throws MetadataException {
    loadJepegDirectory();
    if (m_jepegDirectory != null)
      return m_jepegDirectory.getImageHeight();
    else
      return 0;
  }

  public int getPictureWidth() throws MetadataException {
    loadJepegDirectory();
    if (m_jepegDirectory != null)
      return m_jepegDirectory.getImageWidth();
    else
      return 0;
  }

  public int getPictureCompressionType() throws MetadataException {
    loadJepegDirectory();
    if (m_jepegDirectory != null)
      return m_jepegDirectory.getInteger(JpegDirectory.TAG_COMPRESSION_TYPE);
    else
      return 0;
  }

  public int getPictureOrientation() throws MetadataException {
    loadExifIFD0Directory();
    if (m_exifIFD0Directory != null)
      return m_exifIFD0Directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
    else
      return -1;
  }

  public String getCameraMaker() {
    return getCameraData(ExifIFD0Directory.TAG_MAKE, "Unknown Make");
  }

  public String getCameraModel() {
    return getCameraData(ExifIFD0Directory.TAG_MODEL, "Unknown Model");
  }

  private String getCameraData(int type, String unknownMessage) {
    loadExifIFD0Directory();
    if (m_exifIFD0Directory == null)
      return unknownMessage;
    else {
      String data;
      data = m_exifIFD0Directory.getString(type);
      return data == null ? unknownMessage : data;
    }
  }

  /**
   * returns date and time when the picture was taken. The information
   * is taken from the exif-data. If the corresponding directory entry
   * (ExifSubIFDirectory) is not present, the date is taken from the file
   * itself (last modified entry)
   *
   * @return creation date of the image
   */
  public Date getPictureTakenDate() {
    loadExifSubIFDDirectory();
    if (null == m_exifSubIFDDirectory) {
      log("Date taken from file " + m_fileName);
      return new Date(m_file.lastModified());
    } else {
      Date d = m_exifSubIFDDirectory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL); //TAG_DATETIME_ORIGINAL -> TAG_DATE_TIME_ORIGINAL
      if (d == null)
        m_logger.log(Level.FINE, "Date is null in {0}", m_fileName);
      return d;
    }
  }

  /**
   * returns the time in seconds since Jan 1st, 1970.
   *
   * Note: Databasefield is _MILIS, which is misleading, database also uses
   * seconds.
   *
   * @return seconds since Jan 1, 2070
   */
  public long getPictureTakenDateSeconds() {
    Date pictureTaken = getPictureTakenDate();
    if (pictureTaken == null)
      return 0;
    else
      return pictureTaken.getTime() / 1000;
  }

  /**
   * resets the pointer into the EXIF structure after loading a new file.
   */
  private void resetDirectories() {
    m_jepegDirectory = null;
    m_exifIFD0Directory = null;
    m_exifSubIFDDirectory = null;
    m_canonMakernoteDirectory = null;
    m_exifInteropDirectory = null;
    m_gpsDirectory = null;   // for later use
    m_exifThumbnailDirectory = null;
    m_xmpDirectory = null;
    m_huffmanTablesDirectory = null;
  }

  // Error logging
  private void log(String message) {
    m_logger.log(Level.FINE, "ExifHelper -- {0} for: {1}", new Object[]{message, m_fileName});
  }

  // directory loader
  private Directory loadDirectory(Directory directory, Class directoryClass) {
    if (directory == null && m_metadata != null)
      return m_metadata.getFirstDirectoryOfType(directoryClass);
    else
      return null;
  }

  private boolean loadOk(Directory dir) {
    return (dir == null && m_metadata != null);
  }

  private void loadJepegDirectory() {
    if (loadOk(m_jepegDirectory))
      m_jepegDirectory
          = m_metadata.getFirstDirectoryOfType(JpegDirectory.class);
//        m_jepegDirectory = (JpegDirectory) loadDirectory(m_jepegDirectory, JpegDirectory.class);
  }

  private void loadExifIFD0Directory() {
    if (loadOk(m_exifIFD0Directory))
      m_exifIFD0Directory
          = m_metadata.getFirstDirectoryOfType(com.drew.metadata.exif.ExifIFD0Directory.class);
  }

  private void loadExifSubIFDDirectory() {
    if (loadOk(m_exifSubIFDDirectory))
      m_exifSubIFDDirectory
          = m_metadata.getFirstDirectoryOfType(com.drew.metadata.exif.ExifSubIFDDirectory.class);
  }

  private void loadExifInteropDirectory() {
    if (loadOk(m_exifInteropDirectory))
      m_exifInteropDirectory
          = m_metadata.getFirstDirectoryOfType(com.drew.metadata.exif.ExifInteropDirectory.class);
  }

  private void loadCanonMakernoteDirectory() {
    if (loadOk(m_canonMakernoteDirectory))
      m_canonMakernoteDirectory
          = m_metadata.getFirstDirectoryOfType(com.drew.metadata.exif.makernotes.CanonMakernoteDirectory.class);

  }

  private void loadGpsDirectory() {
    if (loadOk(m_gpsDirectory))
      m_gpsDirectory
          = m_metadata.getFirstDirectoryOfType(com.drew.metadata.exif.GpsDirectory.class);
  }

  private void loadExifThumbnailDirectory() {
    if (loadOk(m_exifThumbnailDirectory))
      m_exifThumbnailDirectory
          = m_metadata.getFirstDirectoryOfType(com.drew.metadata.exif.ExifThumbnailDirectory.class);
  }

  private void loadXmpDirectory() {
    if (loadOk(m_xmpDirectory))
      m_xmpDirectory
          = m_metadata.getFirstDirectoryOfType(com.drew.metadata.xmp.XmpDirectory.class);
  }

  private void loadHuffmanTablesDirectory() {
    if (loadOk(m_huffmanTablesDirectory))
      m_huffmanTablesDirectory
          = m_metadata.getFirstDirectoryOfType(com.drew.metadata.jpeg.HuffmanTablesDirectory.class);
  }

//    private void loadFileMetadataDirectory() {
//        if (loadOk(m_fileMetadataDirectory))
//            m_fileMetadataDirectory
//                    = m_metadata.getFirstDirectoryOfType(com.drew.metadata.file.FileMetadataDirectory.class);
//    }
//
}
