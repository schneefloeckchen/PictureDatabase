package pictures.tools;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.EventListener;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import pictures.ui.DisplayDataBaseCheckerResultsUI;
// import org.hibernate.Session;
// import org.hibernate.query.Query;
//import rzi.logger.Logger;
import rzx.ui.ZxLogPanel;

/**
 * Class to check the consistency of the database
 *
 * @todo Optimize SQL Code and result report, move to precompiled code @NamedQueries and @NamedNativeQueries.
 * @author rene
 *
 * Aug 23: during hibernate etc. upgrade move to pure SQL
 * Mar 2024: Reporting improved.
 */
public class DataBaseChecker {

  private ZxLogPanel mu_logPanel = null;
  Path m_file;
  BufferedWriter m_writer = null;
  Logger m_logger = Logger.getLogger(getClass().getName());
// For the Reporting
  private int m_currentTestId = -1;    // not yet set

  private static final String[] TABLE_NAMES = {
    "CAMERA", "DIGI_PICTURE", "MEDIUM_TYPE",
    "PICTURE_MEDIUM", "PIC_DIRECTORY", "PIC_DIR_MAP"
  };
  private static final int TABLE_ID_CAMERA = 0;
  private static final int TABLE_ID_DIGI_PICTURE = TABLE_ID_CAMERA + 1;
  private static final int TABLE_ID_MEDIUM_TYPE = TABLE_ID_DIGI_PICTURE + 1;
  private static final int TABLE_ID_PICTURE_MEDIUM = TABLE_ID_MEDIUM_TYPE + 1;
  private static final int TABLE_ID_PIC_DIRECTORY = TABLE_ID_PICTURE_MEDIUM + 1;
  private static final int TABLE_ID_DIR_MAP = TABLE_ID_PIC_DIRECTORY + 1;

  private static final String[] TEST_NAMES = {
    "Field Not Null",
    "Number of Roots equal number if media",
    "Empty Media",
    "Missing Media",
    "Search Hanging Folder",
    "Search Hanging Pictures",
    "Cross Table Validation"
  };
  private static final int TEST_ID_FIELD_NOT_NULL = 0;
  private static final int TEST_ID_MISSMANTCH_ROOT_MEDIA = 1;
  private static final int TEST_ID_EMPTY_MEDIA = 2;
  private static final int TEST_ID_MISSING_MEDIA = 3;
  private static final int TEST_ID_SEARCH_HANGING_FOLDER = 4;
  private static final int TEST_ID_SEARCH_HANGING_PICTURES = TEST_ID_SEARCH_HANGING_FOLDER + 1;
  ;
  private static final int TEST_ID_CROSS_TABLE_VALIDTION = TEST_ID_SEARCH_HANGING_PICTURES + 1;

  private static final int TEST_OK = 0;
  private static final int TEST_FAILURE = 1;
  private int m_lastTestId = -1;

  private List<TableModelListener> m_tableModelListener = new ArrayList();
  private TableModel m_reportTableModel = null;
  
  private ArrayList<ReportRecord> m_checkReport = new ArrayList<>() {
    @Override
    public boolean add(ReportRecord obj) {
      boolean res = super.add(obj);
      m_logger.fine("ADDED");
      for (TableModelListener tml : m_tableModelListener) {
        tml.tableChanged(new TableModelEvent(m_reportTableModel));
      }
//      if (m_resultUI != null)
//        m_resultUI.repaint();
      return res;
    }
  };

//    Session m_session = null;
  private EntityManager m_em = null;

  public DataBaseChecker() {
  }

  /**
   * starts the test process, calls the single individual tests.
   * Results are written to the logPanel and a report file.
   * Aditional a tableModule dis updated with test results.
   *
   * @param logPanel The logpanel for the output of testresults and messages.
   */
  public void start(ZxLogPanel logPanel) {
    mu_logPanel = logPanel;
    m_reportTableModel = new ResultTableModel();
    openResultTable();
    try {
      openFile();
      createEntityManager();
      performCheckDigiPicture();
      performSearchForEmptyMedia();
      performSearchMissingMedia();
      performValidateForHangingPictures();
      performSeachForHangingDirectories();
      performValidateCrossTable();
      write("Validation finished");
      closeFile();
//      DisplayDataBaseCheckerResultsUI ui = new DisplayDataBaseCheckerResultsUI();
//      TableModel m = getResult();
//      ui.setModel(m);
//      ui.setVisible(true);
    } catch (IOException ex) {
      m_logger.log(Level.SEVERE,
              "Error on report file: {0}", ex.getLocalizedMessage());
    }
  }

  /**
   * display the result table, which will display the test results and also
   * doing some progress reporting
   * The display is embedded into a seperate thread
   */
  private void openResultTable() {
    Thread thread = new Thread() {
      private DisplayDataBaseCheckerResultsUI m_resultUI = null;
      @Override
      public void run() {
        m_resultUI = new DisplayDataBaseCheckerResultsUI();
        m_resultUI.setModel(m_reportTableModel);
        m_resultUI.setVisible(true);
      }
    };
    thread.start();
  }

  private void createEntityManager() {
    m_em = PicJPAUtil.getInstance().createEntityManager();
  }

  /**
   * Checks for the DIGI_PICTURE Table for: 1) missing ID, FILE_NAME, CAMERA?
   *
   * checks for PIC_DIRECTORY for: 1) missing ID, MEDIUM, DIRECTORY_NAME
   *
   * check, if the number of media corresponds to the number of directories,
   * which do not have a parent.
   *
   * @throws IOException
   */
  private void performCheckDigiPicture() throws IOException {
    Query query;   // local, to be destroyed after this function
    write("Checking Class DIGI_PICTURE");
    writeL2("All Required fields populated?");
    countNullEntries("DIGI_PICTURE", "ID");
    countNullEntries("DIGI_PICTURE", "FILE_NAME");
    countNullEntries("DIGI_PICTURE", "CAMERA");

    writeL2("Valid Cameras for all Pictures?");
    query = m_em.createNativeQuery("SELECT DISTINCT CAMERA FROM DIGI_PICTURE");
    List<Long> cameras = query.getResultList();
    int errcount = 0;
    for (Long camera : cameras) {
      query = m_em.createNativeQuery("SELECT COUNT(ID) FROM CAMERA WHERE ID=" + camera);
      int numCameras = ((Long) query.getSingleResult()).intValue();
      if (numCameras == 0) {
        writeL2("No camera found for ID " + camera);
        errcount++;
      }
    }
    if (errcount == 0)
      writeSuccess("only valid cameras in data");
    else
      writeFailure("Number of missing cameras " + errcount);
    m_checkReport.add(new ReportRecord(
            TABLE_NAMES[TABLE_ID_DIGI_PICTURE], errcount,
            errcount == 0 ? 0 : 1, "Missing Cameras in entries"));

    write("Checking Class PIC_DIRECTORY");
    countNullEntries("PIC_DIRECTORY", "ID");
    countNullEntries("PIC_DIRECTORY", "MEDIUM");
    countNullEntries("PIC_DIRECTORY", "DIRECTORY_NAME");
    query = m_em.createNativeQuery("SELECT COUNT(ID) FROM PIC_DIRECTORY where PARENT is null");
    Long numberRootDirectories = (Long) query.getSingleResult();
    query = m_em.createNativeQuery("SELECT COUNT(ID) FROM PICTURE_MEDIUM");
    Long numberOfMedia = (Long) query.getSingleResult();
    boolean areEqual = numberRootDirectories.longValue() == numberOfMedia.longValue();
    if (areEqual) {
      writeSuccess("Number of media equals number of roots " + numberOfMedia);
      m_checkReport.add(new ReportRecord(
              TEST_ID_MISSMANTCH_ROOT_MEDIA,
              TABLE_ID_PIC_DIRECTORY, 0, 0,
              "Medianumber matches number of root elements"));
    } else {
      writeFailure("ERROR number of Media (" + numberOfMedia + ") not euqal to roots (" + numberRootDirectories + ")");
      m_checkReport.add(new ReportRecord(
              TEST_ID_MISSMANTCH_ROOT_MEDIA,
              TABLE_ID_PIC_DIRECTORY, numberOfMedia.intValue(), 1,
              "Number of Media (" + numberOfMedia + ") not euqal to roots (" + numberRootDirectories + ")"));
//      performSearchMissingMedia();
    }
    // previous test will not fir, e have some mediua without an pictures (Audio..)
  }

  /**
   * Compares the PICTURE_MEDIUM objects with the list of root-directories.
   * Checks, if for all Media we have a root-directory, and also if for all
   * root-directories a PICTURE_MEDIUM object is in the database;
   *
   * @throws IOException
   */
  private void performSearchMissingMedia() throws IOException {
    write("Checking mismatch medium and root folder");
    Query query;
    query = m_em.createNativeQuery("Select MEDIUM from PIC_DIRECTORY where PARENT is null");
    List<Long> mediumListFromDirectory = query.getResultList();     // List of Medium IDs which have a root folder
    query = m_em.createNativeQuery("Select ID from PICTURE_MEDIUM");
    List<Long> medium = query.getResultList();      // List of IDs of all Mediums
    writeL2("Number root directory: " + mediumListFromDirectory.size() + " -- Number of medium: " + medium.size());
    int errCount = 0;
    for (Long i : medium)
      if (!mediumListFromDirectory.contains(i)) {
        query = m_em.createNativeQuery("Select CODE from PICTURE_MEDIUM where ID=" + i);
        Object res = query.getSingleResult();      // Get CODE for the medium, which has no root folder
        write("medium w. ID " + i + " has no directories attached (CODE is " + res + ")");
        m_checkReport.add(new ReportRecord(
                TEST_ID_MISSING_MEDIA,
                TABLE_ID_PICTURE_MEDIUM,
                i.intValue(), 1,
                "Medium w. Code: " + res + " has no folder attached"));
        errCount++;
      }
    for (Long i : mediumListFromDirectory)
      if (!medium.contains(i)) {
        write("Directory w. medium-ID " + i + " has no medium attached");
        m_checkReport.add(new ReportRecord(
                TEST_ID_MISSING_MEDIA,
                TABLE_ID_PIC_DIRECTORY,
                i.intValue(), 1,
                "Directory has no medium attached"));
        errCount++;
      }
    m_checkReport.add(new ReportRecord(
            TEST_ID_MISSING_MEDIA,
            TABLE_ID_PICTURE_MEDIUM,
            errCount,
            errCount == 0 ? 0 : 1,
            "Medium without Folder or Folder without medium detected"));
  }

  /**
   * Checks, if for all media at least one directory is stored in the
   * database. Process: Step 1: Get the List of all Media IDs Step 2: Check,
   * if for all IDs we find at least one entry in the PIC_DIRECTORY table
   * Note: Is not looking for root folder
   */
  private void performSearchForEmptyMedia() throws IOException {
    write("Checking for empty media - media, where no directories are linked to");
    Query query;
    query = m_em.createNativeQuery("Select ID from PICTURE_MEDIUM");
    List<Long> mediumIds = query.getResultList();
    int errCount = 0;
    for (Long id : mediumIds) {
      query = m_em.createNativeQuery("Select count(ID) from PIC_DIRECTORY where PARENT is null and MEDIUM=" + id);
      Long count = (Long) query.getSingleResult();
      if (count.intValue() == 0) {
        errCount++;
        writeL2("For medium w. ID " + id + " folder found");
        m_checkReport.add(new ReportRecord(
                TEST_ID_EMPTY_MEDIA,
                TABLE_ID_PICTURE_MEDIUM,
                id.intValue(), 1, "No folder for this medium found"));
      } else if (count.intValue() > 1)
        writeL2("for medium w. ID " + id.intValue() + ": " + count.intValue() + " root folder found");
    }
    m_checkReport.add(new ReportRecord(
            TEST_ID_EMPTY_MEDIA,
            TABLE_ID_PICTURE_MEDIUM,
            errCount,
            errCount > 0 ? 1 : 0,
            errCount > 0
                    ? "Missing directories of multiple media"
                    : "All Media have at least one directory"));
  }

  /**
   * checks for directories, which have no links to a medium. Process: Step 1:
   * Create a list of media ID from the PIC_DIRECTORY MEDIUM field Step 2:
   * Check if all media exists - search for the IDs.
   *
   * @throws IOException
   */
  private void performSeachForHangingDirectories() throws IOException {
    write("Checking for hanging Directories - No medium for this directory");
    Query query;
    query = m_em.createNativeQuery("SELECT DISTINCT MEDIUM FROM PIC_DIRECTORY");
    List<Long> mediumIds = query.getResultList();
    writeL2("Check for " + mediumIds.size() + " IDs");
    int errCount = 0;
    for (Long id : mediumIds) {
      query = m_em.createNativeQuery("SELECT COUNT(ID) FROM PICTURE_MEDIUM WHERE ID=" + id);
      int count = ((Long) query.getSingleResult()).intValue();
      if (count == 0) {
        writeL2("for medium ID " + count + " no media found");
        errCount++;
      } else if (count > 1) {
        writeL2("for medium ID " + count + " multiple media found");
        errCount++;
      }
    }
    m_checkReport.add(new ReportRecord(
            TEST_ID_SEARCH_HANGING_FOLDER,
            TABLE_ID_PICTURE_MEDIUM,
            errCount,
            errCount == 0 ? TEST_OK : TEST_FAILURE,
            errCount == 0
                    ? "All folder are connected to a medium and vis versa, no dplicates"
                    : "Hanging Folder or missing medium found"));
  }

  /**
   * Checks, if for all pictures at least one entry is in the cross table
   *
   * Process: Step 1: Get all IDs from the pictures Step 2: Search, if we have
   * at least one entry for this in the cross table
   *
   * @throws IOException
   */
  private void performValidateForHangingPictures() throws IOException {
    write("Checking, if all pictures have an entry in the cross table");
    Query query = m_em.createNativeQuery("SELECT DISTINCT ID FROM DIGI_PICTURE");
    List<Long> pictureIds = query.getResultList();      // List of IDs of all Pics
    writeL2("Check for " + pictureIds.size() + " Picture IDs");
    int counter = 0;
    int foundIds = 0;
    int missingIds = 0;
    int maxHit = 0;  // Number of directories, which store this picture / Maximum
//
    for (Long id : pictureIds) {
      Query q2 = m_em.createNativeQuery(
              "SELECT COUNT(*) FROM PIC_DIR_MAP WHERE DIGI_PICTURE_ID=" + id);
      long count = (Long) q2.getSingleResult();
      if (count == 0) {
        writeL2("For Picture ID " + id + " No entry in crosstable found");
        missingIds++;
      } else {
        foundIds++;
        if (maxHit < count)
          maxHit = (int) count;
      }
//      if ((counter / 100) * 100 == counter)
//        write("Counter = " + counter);
//      counter++;
    }
    /** Connection conn = null;
     * try {
     * // conn = PicHibernateUtil.getConnection();
     * conn =PicJPAUtil.getInstance().getConnection();
     * PreparedStatement pQuery = conn.prepareStatement
     * ("SELECT COUNT(DIGI_PICTURE_ID) FROM PIC_DIR_MAP WHERE DIGI_PICTURE_ID=?");
     * for (BigInteger id : pictureIds) {
     * // query = m_session.createSQLQuery(
     * // "SELECT COUNT(DIGI_PICTURE_ID) FROM PIC_DIR_MAP WHERE DIGI_PICTURE_ID="+id);
     * pQuery.setLong(1, id.longValue());
     * // int count = ((BigInteger) pQuery.getSingleResult()).intValue();
     * ResultSet set = pQuery.executeQuery();
     * set.first();
     * int count = set.getInt(1);
     * if (count == 0) {
     * writeL2("For Picture ID " + id + " No entry in crosstable found");
     * missingIds++;
     * } else {
     * foundIds++;
     * if (maxHit < count)
     * maxHit = count;
     * }
     * if ((counter / 100) * 100 == counter)
     * write("Counter = " + counter);
     * counter++;
     * }
     * } catch (SQLException ex) {
     * write("Cannot get connection from SessionFactory or issues w.the resultset, Aboting this test");
     * write(ex.getLocalizedMessage());
     * write(ex.getMessage());
     * } */

    writeL2("Missing cross table entries: " + missingIds + "  IDs found" + foundIds);
    writeL2("Maximum number of entries in the cross table: " + maxHit);
    m_checkReport.add(new ReportRecord(
            TEST_ID_SEARCH_HANGING_PICTURES,
            TABLE_ID_DIGI_PICTURE, missingIds,
            missingIds == 0 ? TEST_OK : TEST_FAILURE,
            "Search wether all pictures are linked to an entry in the crosstable"));
    m_checkReport.add(new ReportRecord(
            TEST_ID_SEARCH_HANGING_PICTURES,
            TABLE_ID_DIGI_PICTURE, maxHit,
            TEST_OK,
            "Maximum number of entries per picture in the cross table: "));

  }

  /**
   * Check the cross table between directory and DIGI_Pictures.
   *
   * Step 1: Get all IDs from the Directories in the cross Table Step 2:
   * Search, if we find for all IDs one directory.
   *
   * Same for the Picture side
   *
   * @throws IOException
   */
  private void performValidateCrossTable() throws IOException {
    write("Checking, if we have for all directory entry in the cross table a directory");
    Query query;
    query = m_em.createNativeQuery("SELECT DISTINCT DIGI_PICTURE_ID FROM PIC_DIR_MAP");
    List<Long> crossIds = query.getResultList();
    writeL2("Checking for " + crossIds.size() + " Picture IDs");
    int errCount = 0;
    int okCount = 0;
    for (Long id : crossIds) {
      query = m_em.createNativeQuery("SELECT COUNT(ID) FROM DIGI_PICTURE WHERE ID=" + id);
      int count = ((Long) query.getSingleResult()).intValue();
      if (count == 0) {
        writeL2("for picture ID " + count + " no picture found");
        errCount++;
      } else if (count > 1) {
        writeL2("for picture ID " + count + " multiple pictures found");
        errCount++;
      } else
        okCount++;
    }
    m_checkReport.add(new ReportRecord(
            TEST_ID_CROSS_TABLE_VALIDTION,
            TABLE_ID_DIR_MAP,
            errCount,
            errCount == 0 ? TEST_OK : TEST_FAILURE,
            "Picture IDs in cross table without pictures"));
    writeL2("Finished >> Missing Pictures: " + errCount + "  Pictures found: " + okCount);

    write("Checking, if we have for all directory entry in the cross table a directory");
    query = m_em.createNativeQuery("SELECT DISTINCT PIC_DIRECTORY_ID FROM PIC_DIR_MAP");
    crossIds = query.getResultList();
    writeL2("Checking for " + crossIds.size() + " Directory IDs");
    errCount = 0;
    okCount = 0;
    for (Long id : crossIds) {
      query = m_em.createNativeQuery("SELECT COUNT(ID) FROM PIC_DIRECTORY WHERE ID=" + id);
      int count = ((Long) query.getSingleResult()).intValue();
      if (count == 0) {
        writeL2("for directory ID " + count + " no directory found");
        errCount++;
      } else if (count > 1) {
        writeL2("for directory ID " + count + " multiple directories found");
        errCount++;
      } else
        okCount++;
    }
    m_checkReport.add(new ReportRecord(
            TEST_ID_CROSS_TABLE_VALIDTION,
            TABLE_ID_DIR_MAP,
            errCount,
            errCount == 0 ? TEST_OK : TEST_FAILURE,
            "Check, if all folder IDs in the cross table point to valid PIC_DIRECTORY entries"));
    writeL2("Finished >> Missing directories: " + errCount + "  Directories found: " + okCount);
  }

  private void countNullEntries(String table, String field) throws IOException {
    Query query = m_em.createNativeQuery("select count(*) from " + table + " where " + field + " is null;");
    Long number = (Long) query.getSingleResult();
    write("Table: " + table + " has " + number + " entries in field " + field + " which are null");
    m_checkReport.add(new ReportRecord(
            TEST_ID_FIELD_NOT_NULL, table, (int) (number + 0),
            number == 0 ? 0 : 1,
            "Field " + field + " shall not be null"
    ));
  }

  private void openFile() throws IOException {
    m_file = Paths.get("DataBaseCheckFIle.txt");
    m_writer = Files.newBufferedWriter(m_file, Charset.forName("UTF-8"));
    m_writer.append("Results from Database Check");
  }

  private void closeFile() throws IOException {
    m_writer.close();
  }

  private void write(String text) throws IOException {
    mu_logPanel.write(text);
    m_logger.info(text);
    m_writer.append(text);
    m_writer.newLine();
  }

  private void writeL2(String text) throws IOException {
    write(">> " + text);
  }

  private void writeSuccess(String text) throws IOException {
    writeL2("SUCCESS -- " + text);
  }

  private void writeFailure(String text) throws IOException {
    writeL2("FAILURE -- " + text);
  }

  public TableModel getResult() {
    return m_reportTableModel;
  }

  class ResultTableModel implements TableModel {

    @Override
    public int getRowCount() {
      return m_checkReport.size();
    }

    @Override
    public int getColumnCount() {
      return ReportRecord.RESULT_LABELS.length;
    }

    @Override
    public String getColumnName(int columnIndex) {
      return ReportRecord.RESULT_LABELS[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
      return false;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      ReportRecord rec = m_checkReport.get(rowIndex);
      return switch (columnIndex) {
        case 0 ->
          rec.reportTime.toString();
        case 1 ->
          TEST_NAMES[rec.testId];
        case 2 ->
          rec.tableName;
        case 3 ->
          rec.testFailure == 0 ? "OK" : "ERROR";
        case 4 ->
          "" + rec.reportNumber;
        case 5 ->
          rec.reportText;
        default ->
          "internal Bug";
      };
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
      throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void addTableModelListener(TableModelListener l) {
      m_tableModelListener.add(l);
    }

    @Override
    public void removeTableModelListener(TableModelListener l) {
      m_tableModelListener.remove(l);
    }

  }

  /**
   * Class to store result for one single Test
   */
  class ReportRecord {

    Date reportTime;        // Runtime, when reported
    int testId;             // Id of the test, to be translated via twext library
    String tableName;
    int testFailure;
    int reportNumber;       // Numeric result, e.g. missing number
    String reportText;         // Description of Result

    private static final String[] RESULT_LABELS = {
      "Time", "Test", "Table", "Failure", "Result", "Result"
    };

    public ReportRecord(int id, int table, int number, int failure, String text) {
      this(id, TABLE_NAMES[table], number, failure, text);
    }

    ReportRecord(int id, String table, int number, int failure, String text) {
      reportTime = new Date();
      testId = id;
      m_lastTestId = id;
      tableName = table;
      reportNumber = number;
      testFailure = failure;
      reportText = text;
    }

    public ReportRecord(String table, int number, int failure, String text) {
      this(m_lastTestId, table, number, failure, text);
    }

  }
}
