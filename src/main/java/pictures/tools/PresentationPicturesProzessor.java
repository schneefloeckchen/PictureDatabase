package pictures.tools;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import com.drew.metadata.MetadataException;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import javax.imageio.ImageIO;
import javax.swing.JDialog;
import java.util.logging.Logger;
import javax.imageio.stream.ImageOutputStream;
import org.apache.commons.imaging.ImageReadException;
import org.apache.commons.imaging.ImageWriteException;
import org.apache.commons.imaging.Imaging;
import org.apache.commons.imaging.formats.jpeg.JpegImageMetadata;
import org.apache.commons.imaging.formats.jpeg.exif.ExifRewriter;
import org.apache.commons.imaging.formats.tiff.TiffImageMetadata;
import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputDirectory;
import org.apache.commons.imaging.formats.tiff.write.TiffOutputSet;
import rzx.graphics.ExifHelper;
import rzx.ui.ZxErrorDialog;
import rzx.ui.ZxMessageDialog;

/**
 * Class processes all activities around the creation of the presentation
 * pictures. Includes the creation of the sequence file.
 *
 * Format of the sequence file:
 * Remarks start with !
 * one line per input file
 * Following switches per line / image file:
 * /L turn the image 90 degrees left
 * /R same right
 * /U rotate by 180 degrees.
 *
 * @author rene
 * 
 * 15.4.25 RZ finished, sort by photo taken date added
 */
public class PresentationPicturesProzessor {

  private JDialog m_uiClass = null;  // Class, which creates the UI. Just
  // needed to display the error messages
  private String m_workFileName = null;
  private Logger m_logger = Logger.getLogger(getClass().getName());

  public PresentationPicturesProzessor(JDialog uiClass) {
    Level level = m_logger.getLevel();
//    System.out.println("Logger is using "+level.toString());
//
//    m_logger.log(Level.FINEST, "Finest");
//    m_logger.log(Level.FINER, "Finer");
//    m_logger.log(Level.FINE, "Fine");
//    m_logger.log(Level.INFO, "Info");
//    m_logger.log(Level.WARNING, "Warning");
//    m_logger.log(Level.SEVERE, "Severe");
//    
//
    m_uiClass = uiClass;
  }

  /**
   * Creates the sequence file for further processing
   *
   * @param sequenceFile
   * @param folderName
   * @param byDate if true, the sequence file will be sorted by creation date
   */
  public void createSequenceFile(File sequenceFile, String folderName,
          boolean byDate) {
    try {
      BufferedWriter writer = new BufferedWriter(new FileWriter(sequenceFile));
      Timestamp currentDate = new Timestamp(System.currentTimeMillis());
      writeLine(writer, "!");
      writeLine(writer, "! SequenceFile from " + currentDate);
      writeLine(writer, "! Folder: " + folderName);
      writeLine(writer, "!");
      File folder = new File(folderName);
      File[] files = folder.listFiles((File dir, String name)
              -> name.endsWith(".jpg") || name.endsWith(".JPG")
              || name.endsWith(".jpeg") || name.endsWith(".JPEG"));
      String[] fileNames = new String[files.length];
      Map fileEntries = new HashMap<Long, String>();
      int counter = 0;
      for (File file : files) {
        String line = file.getName();
        // determine orientation of the picture
        ExifHelper helper = new ExifHelper();
        try {
          helper.load(new File(folderName + "/" + line));
          int orientation = helper.getPictureOrientation();
          long pictureTakenMillis = helper.getPictureTakenDateSeconds();
          // Orientierungen unter
          // https://www.impulseadventure.com/photo/exif-orientation.html
          // 1: Bild ok, 8: 90 drees left, so turn right
          // 3: turn image 180 degrees, 6: tuen 90 degrees left
          line = line + switch (orientation) {
            case 8 ->
              "/R";
            case 3 ->
              "/U";
            case 6 ->
              "/L";
            default ->
              "";
          };
          m_logger.log(Level.FINE, "File processed:{0}", line);
          fileNames[counter++] = line;
          fileEntries.put(pictureTakenMillis, line);
        } catch (JpegProcessingException
                | PngProcessingException
                | TiffProcessingException
                | IOException
                | MetadataException ex) {
          m_logger.log(Level.SEVERE,
                  "Exception loading image {0} : {1}", new Object[]{
                    line,
                    ex.getLocalizedMessage()});
        }
      }

      if (!byDate) {
        Arrays.sort(fileNames);
        for (String name : fileNames)
          writeLine(writer, name);
      } else {
        SortedSet<Long> keys = new TreeSet(fileEntries.keySet());
        for (Long key : keys)
          writeLine(writer, (String) fileEntries.get(key));
      }
      writer.flush();
      writer.close();
    } catch (IOException ex) {
      ZxErrorDialog.displaySimpleErrorMessage(m_uiClass,
              "createPresentationPictures.sequenceFile.errorWriting.message", ex);
    }
  }

  /**
   * Processes through the sequence file and creates the images for
   * presentation purpose.
   *
   * @param sequenceFileName Full sequence file name incl. path
   * @param exportFolder FUll path, where the created images shall be stored
   * @param fileNameHead Start of each export file, format is
   * <head><counter>.<extension>
   * @param counterStart
   * @param counterLength
   * @param extension
   * @param resX Resolution of the export file in X
   * @param resY Resolution of the export file in X
   * @param description
   * @throws FileNotFoundException
   * @throws IOException
   */
  public void processSequenceFile(
          String sequenceFileName,
          String exportFolder,
          String fileNameHead,
          int counterStart, int counterLength,
          String extension,
          int resX, int resY,
          String description)
          throws FileNotFoundException, IOException {
    File sequenceFile = new File(sequenceFileName);
//    String path = sequenceFile.getPath();

    BufferedReader sequenceFileReader
            = new BufferedReader(new FileReader(sequenceFile));
    int lastSlash = sequenceFileName.lastIndexOf("/");
    String path = sequenceFileName.substring(0, lastSlash);
    String formatString = "%s/%s%0" + counterLength + "d.%s";

    String line;
    m_workFileName = System.getProperty("user.home")
            + File.separator + ".wrkImage.jpeg";
    do {
      line = sequenceFileReader.readLine();
      if (line == null || line.startsWith("!"))
        continue;
      m_logger.log(Level.FINE, "Processing: {0}", line);
      String exportFileName = String.format(formatString, exportFolder,
              fileNameHead, counterStart++, extension, resX, resY);
      int slash = line.indexOf("/");         // Seperate the orientation character
      String orientation = null;             // and remove the flag from the filename
      if (slash >= 0) {
        orientation = line.substring(slash + 1);
        line = line.substring(0, slash);
      }
      createPicture(path + "/" + line, exportFileName, orientation,
              resX, resY, description);
    } while (line != null);
    ZxMessageDialog.displayMessage(m_uiClass,
            "createPresentationPictures.confirmation.text");
  }

  /**
   * Method to load the source picture, create the thumbnail and save it under
   * the defined name.
   *
   * @param input full filename for the source image
   * @param export full filename for the target image
   * @param resX resolution for the target image in x
   * @param resY resolution for the target image in y
   */
  private void createPicture(String input, String export, String orientation,
          int resX, int resY, String description) throws IOException {
    float scale;                  // Factor to calculate the size of the target image
    int targetX, targetY;         // size of the target image

    // Some calculation to deal with landscape
    // and portrait formats
    m_logger.log(Level.FINER, "{0} -->> {1}", new Object[]{input, export});
//    ImageReader jpegReader = ImageIO.
    File inputFile = new File(input);
    BufferedImage sourceImage = ImageIO.read(inputFile);
    int sourceImageWidth = sourceImage.getWidth();
    int sourceImageHeight = sourceImage.getHeight();
    m_logger.log(Level.FINER, "Size of source image {0} / {1}", new Object[]{sourceImageWidth, sourceImageHeight});
    if (sourceImageHeight > sourceImageWidth) {       // Portrait Layout
      scale = (float) resY / (float) sourceImageHeight;
      targetX = (int) ((float) sourceImageWidth * scale);
      targetY = resY;
      m_logger.log(Level.FINER, "Portrait -> scale = {0}", scale);
    } else {
      scale = (float) resX / (float) sourceImageWidth;
      targetY = (int) ((float) sourceImageHeight * scale);
      targetX = resX;
      m_logger.log(Level.FINER, "Landscape -> scale = {0}", scale);
    }
    if ("L".equalsIgnoreCase(orientation) || "R".equalsIgnoreCase(orientation)) {
      int tmp = targetX;        // portrait orientation
      targetX = targetY;
      targetY = tmp;
    }
    m_logger.log(Level.FINER, "Scale: {0}  targetX {1}  TargetY {2}", new Object[]{scale, targetX, targetY});
    BufferedImage exportImage = new BufferedImage(targetX, targetY,
            BufferedImage.TYPE_USHORT_565_RGB);

    AffineTransform transform = new AffineTransform();
    if (orientation != null) {
      int rotateIndex;
      int rotaPointX = 0;       // Center of the rotation
      int rotaPointY = 0;
      if (orientation.equalsIgnoreCase("R")) {
        rotateIndex = 3;
        rotaPointX = targetY / 2;
        rotaPointY = targetY / 2;
      } else if (orientation.equalsIgnoreCase("L")) {
        rotateIndex = 1;
        rotaPointX = targetX / 2;
        rotaPointY = targetX / 2;
      } else if (orientation.equalsIgnoreCase("U")) {
        rotateIndex = 2;
        rotaPointX = targetX / 2;
        rotaPointY = targetY / 2;
      } else
        rotateIndex = 0;
      if (rotateIndex != 0)
        transform.quadrantRotate(
                //            rotateIndex, sourceImageHeight / 2, sourceImageWidth / 2);
                //            rotateIndex, sourceImageWidth / 2, sourceImageHeight / 2);
                rotateIndex, rotaPointX, rotaPointY);
    }
    transform.scale(scale, scale);
    Graphics2D g2d = exportImage.createGraphics();
    g2d.drawImage(sourceImage, transform, null);

// Write with ImageIO to a work-file
    File workFile = new File(m_workFileName);
    try (ImageOutputStream ios = ImageIO.createImageOutputStream(workFile)) {
      ImageIO.write(exportImage, "JPEG", ios);
    }

// Add Exif from the inputfile to the export file using apache commons imaging
    TiffImageMetadata exifData;
    try {
      JpegImageMetadata sourceMetadata
              = (JpegImageMetadata) Imaging.getMetadata(inputFile);
      exifData = sourceMetadata.getExif();
    } catch (ImageReadException ex) {
      m_logger.log(Level.SEVERE, "Error reading Metadata from {0}", inputFile);
      return;
    }

    try {
      File exportFile = new File(export);
      BufferedOutputStream bos = new BufferedOutputStream(
              new FileOutputStream(exportFile));
      TiffOutputSet tos = exifData.getOutputSet();       // exif Data of the input file

// The set has the old orientation value in EXIF, need to update it to 1
      TiffOutputDirectory rootDirectory = tos.getOrCreateRootDirectory();
      rootDirectory.removeField(TiffTagConstants.TIFF_TAG_ORIENTATION);
      rootDirectory.add(TiffTagConstants.TIFF_TAG_ORIENTATION,
              (short) TiffTagConstants.ORIENTATION_VALUE_HORIZONTAL_NORMAL);
      if (description != null && description.length() > 0) {
        rootDirectory.removeField(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION);
        rootDirectory.add(TiffTagConstants.TIFF_TAG_IMAGE_DESCRIPTION, description);
      }
// Finally add the EXIF segment to the image file.
      new ExifRewriter().updateExifMetadataLossless(workFile, bos, tos);
    } catch (ImageWriteException | ImageReadException ex) {
      m_logger.log(Level.SEVERE, "Exception writig File {0}", export);
    }
//    File newJpegFile = new File(export);
//    ImageIO.
//    ImageIO.write(exportImage, "jpg", newJpegFile);

  }

  /**
   * just a convenience method to reduce typing
   *
   * @param writer
   * @param line
   * @throws IOException
   */
  private void writeLine(BufferedWriter writer, String line) throws IOException {
    writer.write(line);
    writer.newLine();
  }

  /**
   * Stores some details of a file for further processing
   */
  private class FileDetails {

    private String m_fileName;
    private String m_orientation;
    private long m_takenTimeMillis;      // From Exif Data for sorting the files

    FileDetails(String fileName, String orientation, long takenTimeMillis) {
      m_fileName = fileName;
      m_orientation = orientation;
      m_takenTimeMillis = takenTimeMillis;
    }

    public String getFileName() {
      return m_fileName;
    }

    public String getOrientation() {
      return m_orientation;
    }

    public long getTakenTimeMillis() {
      return m_takenTimeMillis;
    }
  }
}
