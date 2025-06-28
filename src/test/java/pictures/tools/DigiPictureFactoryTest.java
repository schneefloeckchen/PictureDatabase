/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit5TestClass.java to edit this template
 */
package pictures.tools;

import bas.SQLUtil;
import bas.TestBase;
import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.sql.ResultSet;
import java.sql.Statement;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Disabled;
import picdata.DigiPicture;

/**
 *
 * @author rene
 */
public class DigiPictureFactoryTest extends TestBase {

  public DigiPictureFactoryTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    setupDatabaseConnection();
  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    cleanDatabase();
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Test of loadImageFile method, of class DigiPictureFactory.
   */
  @Test
  @Disabled    // later use?
  public void testLoadImageFile() {
  }

  /**
   * Test of createDigiPicture method, of class DigiPictureFactory.
   * Create an entry from a file and validate the result.
   */
  @Test
  public void testCreateDigiPicture() throws Exception {

    DigiPictureFactory factory = new DigiPictureFactory();
    DigiPicture pic = factory.createDigiPicture(
            TEST_DATA_FOLDER + "/CD0_1/M1/", "P1011615.JPG");
    try (EntityManager em
            = PicJPAUtil.getInstance().createEntityManager()) {
      em.getTransaction().begin();
      pic.persist(em);
      em.getTransaction().commit();
    }

// Validation
    SQLUtil sqlUtil = new SQLUtil();
    Statement sqlStatement = sqlUtil.createStatement();
    ResultSet set = sqlStatement.executeQuery(
            "SELECT ORIENTATION,WIDTH,HEIGHT,PICTURE_MILIS,FILE_NAME from DIGI_PICTURE");
    set.last();
    assertEquals(1, set.getRow(), "Improper size of ResultSet");
    set.first();
    assertEquals(1, set.getInt(1), "Wrong orientation");
    assertEquals(4608, set.getInt(2), "Wrong Picture Width");
    assertEquals(2592, set.getInt(3), "Wrong Picture Height");
    assertEquals(1609517517, set.getInt(4), "Wrong Taken millisF");
    assertEquals("P1011615.JPG", set.getString(5), "Wrong Filename");
  }

}
