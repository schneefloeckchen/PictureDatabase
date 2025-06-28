
import org.junit.platform.suite.api.BeforeSuite;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

/**
 * Sollte zu beginn aller Tests laufen.
 * Doku zu JUnit Suite:
 * 
 * https://junit.org/junit5/docs/current/user-guide/#junit-platform-suite-engine
 * STackoverflow-Post dazu
 * https://stackoverflow.com/questions/79636939/global-class-to-start-junit-5/79636941#79636941
 * 
 * @author rene
 */

@Suite
@SuiteDisplayName("Test Suite f. ImageDB")
@SelectPackages("pictures.*")
public class TestStart {
  
  @BeforeSuite
  public static void beforeTestSuite() {
    System.out.println("Init of Suite");
  }
  
}
