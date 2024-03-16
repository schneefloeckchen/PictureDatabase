/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */

package pictures.ui.tools;

import static bas.TestBase.TEST_DATA_FOLDER;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JTextField;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test for the UI uses the data from the CompereFolderProzessorTest
 * The processor compares folders, so database activity is not needed.
 * 
 * 8.3.24 Initial
 * @author rene
 */
public class CompareFolderUITest {

    private List<JButton> m_buttons = null;
    private List<JTextField> m_textFields = null;
    private CompareFolderUI m_compareUI = new CompareFolderUI();
    
    public CompareFolderUITest() {
      m_buttons = m_compareUI.getButtons();
      m_textFields = m_compareUI.getTextFields();
      m_compareUI.setToTest();
      Logger logger = Logger.getLogger("pictures.tools.CompareFolderProcessor");
      logger.setLevel(Level.FINE);
    }

    @BeforeAll
    public static void setUpClass() {
    }

    @AfterAll
    public static void tearDownClass() {
    }

    @BeforeEach
    public void setUp() {
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * tests just the loading of the reference folder. Same method name
     * in the CompareFolderProcessorTest class
     */
    @Test
    protected void testLoad() {
      log("Starting simple load test");
      String storeLocation = TEST_DATA_FOLDER+"/CD7/";
      log("Loading from "+storeLocation);
      m_textFields.get(0).setText(storeLocation);
      m_buttons.get(0).doClick();
      assertEquals (28, m_compareUI.getProcessNumber(), "Number of unique processed files");
    }
    
    /**
     * see same method in the processorTests
     * @ToDo Check, if process in SW, UI and tests fizts to the documentation and variable/member names
     */
    @Test
    protected void testCD1() {
      log ("Starting CD1 Test");
      String testFolder = TEST_DATA_FOLDER+"/CD1";
      String checkFolder = TEST_DATA_FOLDER+"/CD1B";
      m_textFields.get(0).setText(checkFolder);
      m_textFields.get(1).setText(testFolder);
      m_buttons.get(0).doClick();      // load the test folder
      assertEquals (12, m_compareUI.getProcessNumber(), "Number of unique Files");
      m_buttons.get(1).doClick();  // Now check the files
      assertEquals(12, m_compareUI.getFoundFilesCount(), "Number of found files");
      assertEquals(16, m_compareUI.getMissingFilesCount(), "Number of missing files");
    }
    /**
     * Reduce some typing
     */
    private void log(String text) {
      System.out.println(text);
    }
}