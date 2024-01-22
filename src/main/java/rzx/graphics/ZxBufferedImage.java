package rzx.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.sql.rowset.serial.SerialBlob;
import java.util.logging.Logger;

/**
 * Class to load, and resize an image.
 *
 * @author rene
 */
public class ZxBufferedImage {

//    private final Logger m_logger = Logger.getInstance();
//    private BufferedImage m_image;
  private BufferedImage m_thumb;
//    private Logger m_logger = Logger.getLogger(getClass().getName());

  /**
   * Method that creates a thumbnail of an image.It the thumbnail cannot be
   * created, this is recorded in the logger and a null is returned.
   *
   * @param img the image to precess
   * @param thumbNailSize the size in pixels of the longer side of the
   * thumbnail.
   * @param orientation
   * @return the thumbnail.
   */
  public static BufferedImage createThumbNail(BufferedImage img, int thumbNailSize, int orientation) {
    float scale;
    int thumbWidth;
    int thumbHeight;
    if (img == null)
      return null;
    else
            try {
      int width = img.getWidth();
      int height = img.getHeight();
      if (width >= height) {
        scale = (float) thumbNailSize / (float) width;
        thumbWidth = thumbNailSize;
        thumbHeight = (int) ((float) height * scale);
      } else {
        scale = (float) thumbNailSize / (float) height;
        thumbWidth = (int) ((float) width * scale);
        thumbHeight = thumbNailSize;
      }

      BufferedImage thumbNail = new BufferedImage(
          thumbWidth, thumbHeight,
          BufferedImage.TYPE_USHORT_565_RGB);
//                        BufferedImage.TYPE_INT_ARGB);
      AffineTransform transform = new AffineTransform();
//                if (orientation == 6) transform.quadrantRotate(1);      // Wohl besser, die rotation bei der Anzeige durchzuführen
      transform.scale(scale, scale);
      Graphics2D g2d = thumbNail.createGraphics();
      g2d.drawImage(img, transform, null);
      return thumbNail;
    } catch (Exception | OutOfMemoryError ex) {
      Logger logger = Logger.getLogger("rzx.graphics.ZxBufferedImage");
      logger.log(
          Level.SEVERE, "{0} occured while creating Thumbnail with msg: {1}",
          new Object[]{ex.getClass().getName(), ex.getMessage()});
      logger.severe("create null Thumbnail");
      return null;
    }
  }

  public static Blob createBlob(BufferedImage image, String format) throws IOException, SQLException {
    if (image == null)
      return null;
    else {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ImageIO.write(image, format, baos);
      Blob blob = new SerialBlob(baos.toByteArray());
      return blob;
    }
  }

  public static Blob createBlobForThumbNail(BufferedImage img, int thumbNailSize, String format, int orientation) throws IOException, SQLException {
    return createBlob(createThumbNail(img, thumbNailSize, orientation), format);
  }

  public static BufferedImage createImageFromBlob(Blob blob, String format) throws SQLException, IOException {
    if (blob == null)
      return null;
    else {
      InputStream is = blob.getBinaryStream();
      return ImageIO.read(is);
    }
  }

//    public void loadImage(File file) throws IOException {
//        m_image = ImageIO.read(file);
//        m_thumb = createThumbNail(m_image, 800);
//    }
//
  public Image getThumb() {
    return m_thumb;
  }
}
