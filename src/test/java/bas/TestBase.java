package bas;

import hib.PicJPAUtil;
import java.lang.StackWalker.StackFrame;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Basic class for all tests, holds the common routines, e.g. creation of
 * database connectivity, and some common methods for logging etc.
 *
 * Aug 23: Move to JUnit 5, JPA naming and from Session to EntityManager
 *
 * @author rene
 */
public class TestBase {
  
  public static final String TEST_DATA_FOLDER
//      = "/home/rene/technik/JavaDevNB140/PicturesDatabase/testData/";
        = "/home/rene/fastDrive/testDaten/pictureDatabase/testData";
//  /home/rene/fastDrive/testDaten/pictureDatabase


  protected static void setupDatabaseConnection() {
//    PicHibernateUtil.getInstance().loadConfiguration("TEST");
//    PicJPAUtil.getInstance().configure("PIC_TEST");
//  System.out.println("Creating database connection for SQL access");
//    PicJPAUtil.getInstance().configure("PIC_TEST_IN_MEMORY");
    PicJPAUtil.getInstance().configure("PIC_TEST");
  }

  protected static void cleanDatabase() {
    SQLUtil util = new SQLUtil();
    util.deleteTableData("DIGI_PICTURE");
    util.deleteTableData("CAMERA");
    util.deleteTableData("PICTURE_MEDIUM");
    util.deleteTableData("PIC_DIRECTORY");
    util.deleteTableData("PIC_DIR_MAP");
    util.deleteTableData("MEDIUM_TYPE");
    assertEquals(0, util.countElementsInTable("MEDIUM_TYPE"), "Table should be empty");
  }

  protected void log() {
    log("Started");
  }
  /**
   * Logs a message incl. info about the origin on System.out.
   *
   * @param message
   */
  protected void log(String message) {
    // get calling class and method
    StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
    var method = walker.walk(s
        -> s.map(StackFrame::getMethodName).skip(1).findFirst()).get();
    try {
      String clazz = getClass().getName();
      String clazzName = clazz.substring(clazz.lastIndexOf(".")+1);
      System.out.println(clazzName + "->" + method + " -- " + message);
    } catch (Exception ex) {
      System.out.println("unknown class and method -- " + message);
      System.out.println("Internal error -- " + ex.getLocalizedMessage());
    }
  }
}
