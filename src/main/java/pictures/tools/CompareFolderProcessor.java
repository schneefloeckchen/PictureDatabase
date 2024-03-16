package pictures.tools;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.TableModel;
import rzx.graphics.ExifHelper;
import rzx.ui.ZxBaseROTableModel;

/**
 * Processor to do the folder compare. It reads and stores the file names
 * of the images in the folder, which have already been loaded to the storage.
 * in a 2nd step it checks, of the images of a further folder are in this set.
 *
 * 2 Step process, which can individually started to allow that several folders
 * can be checked.
 *
 * the data from the storage folder are stored in a Map, where the fileName
 * is the key and the filename, folder name and the timeTaken (in seconds
 * since 1974 are the values - stored in an imageObject
 *
 * @author rene
 *
 * 8.11.22 RZ initial.
 * 5.2.2024 RZ updated to Java21 and Test added
 *
 */
public class CompareFolderProcessor {

  private Map<String, ImageEntry> m_storageImages = null;
  private ArrayList<DuplicateFileEntry> m_duplicateFiles = null;  // Duplicates in the storage
  private ArrayList<ResultEntry> m_nissingFileList = null;        // Results from the check
  private ArrayList<ResultEntry> m_foundFileList = null;        // Results from the check

  private ExifHelper m_exifHelper = new ExifHelper();
  private Logger m_logger
          = Logger.getLogger("pictures.tools.CompareFolderProcessor");

  private int m_fileCounter = 0;      // Counts processed files

  public CompareFolderProcessor() {
    m_storageImages = new TreeMap();
  }

  /**
   * Starts the process to read the relevant data from all image files
   * in the storage area.
   *
   * @param folder
   * @return
   */
  public int load(File folder) {
    m_logger.entering(getClass().getName(), "load");
    m_storageImages = new TreeMap<>();
    m_duplicateFiles = new ArrayList<>();
    m_fileCounter = 0;
    loadLoop(folder);
    m_logger.log(Level.INFO,
            "Processed file count: {0} - Length of Map: {1}",
            new Object[]{m_fileCounter, m_storageImages.size()});
    return m_storageImages.size();
  }

  private void loadLoop(File folder) {
    File[] files = folder.listFiles();
    String folderName = folder.getAbsolutePath();
    if (files != null) {
      for (File file : files) {
        if (file.isDirectory()) {
          m_logger.log(Level.INFO, "Working on folder: {0}", file.getAbsolutePath());
          loadLoop(file);    // Jump one lovel down
        } else {
          String fileName = "";
          m_fileCounter++;
          try {
//          m_exifHelper = new ExifHelper();
            m_exifHelper.load(file);
            fileName = file.getName();
            long time = m_exifHelper.getPictureTakenDateSeconds();
            ImageEntry entry = m_storageImages.get(fileName+time);
            if (entry == null) {
              m_storageImages.put(fileName+time, new ImageEntry(fileName, time, folderName));
              m_logger.log(Level.FINE, "Adding File {0}", fileName);
            } else if (time == entry.takenTime) {
              m_duplicateFiles.add(new DuplicateFileEntry(
                      fileName, folderName, time, m_exifHelper.getPictureTakenDate(),
                      entry.fileName, entry.folderName, entry.takenTime));
              m_logger.log(Level.FINE,
                      "Found duplicate: File: {0} / {1} -->> {2} / {3}",
                      new Object[]{
                        entry.folderName, entry.fileName, folderName, fileName});
            } else {
              m_storageImages.put(fileName+time,
                      new ImageEntry(fileName, time, folderName));
              m_logger.log(Level.FINE,
                      "Duplicate Filename {0} but different taken time",
                      new String[]{
                        fileName, "" + time, "" + entry.takenTime});
            }
          } catch (IOException
                  | TiffProcessingException
                  | PngProcessingException
                  | JpegProcessingException ex) {
            m_storageImages.put("",
                    new ImageEntry(fileName, 0, ex.getLocalizedMessage()));
          }
        }
      }
    }
  }

  /**
   * Checks if all files in folder (and the subfolder) have been loaded
   * in the map.
   *
   * @param folder
   */
  public void checkFiles(File folder) {
    m_nissingFileList = new ArrayList<>();
    m_foundFileList = new ArrayList<>();
    checkLoop(folder);
  }

  private void checkLoop(File folder) {
    String folderName = folder.getAbsolutePath();
    File[] files = folder.listFiles();
    for (File file : files) {
      if (file.isDirectory()) {
        checkLoop(file);
      } else {
        String fileName = "";
        try {
          m_exifHelper.load(file);
          fileName = file.getName();
          long time = m_exifHelper.getPictureTakenDateSeconds();
          ImageEntry storageEntry = m_storageImages.get(fileName+time);   // Filename + time is the unique key!
          if (storageEntry == null) {
            m_nissingFileList.add(new ResultEntry(
                    null, new ImageEntry(fileName, time,
                            folderName), "Not found in existing storage"));
          } else if (time != storageEntry.takenTime) {    // @todo: check if needed, as time is now in the key
            m_nissingFileList.add(new ResultEntry(
                    storageEntry, new ImageEntry(fileName, time, folderName),
                    "Same file name, but different taken date"));
          } else {
            m_foundFileList.add(new ResultEntry(
                    storageEntry, new ImageEntry(fileName, time, folderName),
                    ""));
          }

        } catch (IOException
                | TiffProcessingException
                | PngProcessingException
                | JpegProcessingException ex) {
          m_storageImages.put(null,
                  new ImageEntry(fileName, 0, ex.getLocalizedMessage()));
        }
      }
    }
  }

  public TableModel getMissingFilesTableModel() {
    return new ImagesTableModel(m_nissingFileList);
  }

  public TableModel getFoundFilesTableModel() {
    return new ImagesTableModel(m_foundFileList);
  }

  /**
   * data of the duplicate images found during the initial load operation
   * of the processor.
   *
   * @return
   */
  public TableModel getDuplicateTableModel() {
    return new DuplicateFileTableModel();
  }

  /**
   * prints the content of the Storage Images Map to standard output
   * Just for testing purpose
   */
  public void dumpStorage() {
    m_storageImages.forEach((key, entry) -> {
      System.out.println(key.getClass().getName() + " - " + entry.getClass().getName());
    });
  }

  class DuplicateFileTableModel extends ZxBaseROTableModel {

    DuplicateFileTableModel() {
      setLabels(new String[]{
        "File Name", "Folder Name", "TakenTime Seconds", "TakenTime",
        "Duplicate File Name", "Duplicate File Folder", "Dup Taken Time Seconds"
      });
    }

    @Override
    public int getRowCount() {
      return m_duplicateFiles.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      DuplicateFileEntry entry = m_duplicateFiles.get(rowIndex);
      return switch (columnIndex) {
        case 0 ->
          entry.fileName;
        case 1 ->
          entry.folderName;
        case 2 ->
          entry.takenTimeSeconds;
        case 3 ->
          entry.takenTime == null ? "" : entry.takenTime.toString();
        case 4 ->
          entry.duplicateFileName;
        case 5 ->
          entry.duplicateFolderName;
        case 6 ->
          entry.duplicateTakenTimeSeconds;
        default ->
          "invalid col index " + columnIndex;
      };
    }

  }

  /**
   * provides the data in a TableModel for the files found and not found
   * during the duplicate check run.
   */
  class ImagesTableModel extends ZxBaseROTableModel {

    ArrayList<ResultEntry> imageEntries = null;

    public ImagesTableModel(ArrayList<ResultEntry> entries) {
      this();
      imageEntries = entries;
    }

    public ImagesTableModel() {

      setLabels(new String[]{"File in Storage", "Time Stamp", "Folder in Storage",
        "new File", "Time Stamp", "New Folder", "Remark"});
    }

    @Override
    public int getRowCount() {
      if (imageEntries == null) {
        return 0;
      } else {
        return imageEntries.size();
      }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      ResultEntry entry = imageEntries.get(rowIndex);
      return switch (columnIndex) {
        case 0 -> {
          if (entry.storageEntry != null) {
            yield entry.storageEntry.fileName;
          } else {
            yield "";
          }
        }
        case 1 ->
          entry.storageEntry == null ? "" : "" + entry.storageEntry.takenTime;

        case 2 -> {
          if (entry.storageEntry != null) {
            yield entry.storageEntry.folderName;
          } else {
            yield "";
          }
        }
        case 3 ->
          entry.seachEntry.fileName;
        case 4 ->
          "" + entry.seachEntry.takenTime;
        case 5 ->
          entry.seachEntry.folderName;
        case 6 ->
          entry.remark;
        default ->
          "invalid index";
      };
    }
  }

  class ImageEntry {

    String fileName = "";
    String folderName = "";
    long takenTime = 0;      // seconds since 1974.

    ImageEntry(String fileName, long takenTime, String folderName) {
      this.fileName = fileName;
      this.folderName = folderName;
      this.takenTime = takenTime;
    }
  }

  /**
   * Objects of this class store the image file information.
   */
  class ResultEntry {

    ImageEntry storageEntry = null;
    ImageEntry seachEntry = null;
    String remark = "";

    ResultEntry(ImageEntry storage, ImageEntry search, String remark) {
      this.storageEntry = storage;
      this.seachEntry = search;
      this.remark = remark;
    }
  }

  /**
   *
   */
  class DuplicateFileEntry {

    String fileName = "";
    String folderName = "";
    long takenTimeSeconds = 0;
    Date takenTime = null;

    String duplicateFileName = "";
    String duplicateFolderName = "";
    long duplicateTakenTimeSeconds = 0;

    public DuplicateFileEntry(String fileName, String folderName,
            long takenTimeMillies, Date takenTime,
            String duplicateFileName, String duplicateFolderName,
            long duplicateTakenTimeSeconds) {
      this.fileName = fileName;
      this.folderName = folderName;
      this.takenTimeSeconds = takenTimeMillies;
      this.takenTime = takenTime;

      this.duplicateFileName = duplicateFileName;
      this.duplicateFolderName = duplicateFolderName;
      this.duplicateTakenTimeSeconds = duplicateTakenTimeSeconds;
    }
  }

}
