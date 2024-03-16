
package pictures.tools;

import java.io.File;
import java.io.IOException;
import javax.swing.JDialog;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.common.ImageMetadata;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import rzx.ui.ZxLogPanel;

/**
 * Class, which simple reads and lists the EXIF data of an image.
 * @author rene
 */
public class ExifDataReader {

    private final ZxLogPanel mu_logPanel;

  public ExifDataReader(JDialog dialog, ZxLogPanel logPanel) {
        mu_logPanel = logPanel;
        mu_logPanel.write("EXIF Reader created");
  }
  
  public void readExifData (File file) {
    mu_logPanel.write("Starting to read the file ");
      try {
        ImageMetadata metaData = Imaging.getMetadata(file);
      JpegImageMetadata jpegMetaData = (JpegImageMetadata) metaData;
      if (jpegMetaData == null)
        mu_logPanel.write("metadata are null");
      else 
        mu_logPanel.write(jpegMetaData.toString());
 
      } catch (ImageReadException | IOException ex) {
        mu_logPanel.write ("Exception reading metadata -- "+ex.getClass().getName());
        mu_logPanel.write (ex.getLocalizedMessage());
      }
  }
}
