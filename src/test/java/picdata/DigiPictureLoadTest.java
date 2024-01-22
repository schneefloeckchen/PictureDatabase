/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package picdata;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static bas.TestBase.TEST_DATA_FOLDER;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import java.io.File;
import java.io.IOException;

/**
 * Class tests load, preload and further analysis functions of the
 * DigiPicture Class
 *
 * @author rene
 * 2.11.2023
 */
public class DigiPictureLoadTest extends TestBaseClass {

  public DigiPictureLoadTest() {
  }

  @BeforeAll
  public static void setUpClass() {
    System.out.println("Setting up Database Connection");
    setupDatabaseConnection();

  }

  @AfterAll
  public static void tearDownClass() {
  }

  @BeforeEach
  public void setUp() {
    sqlUtil.deleteTableData("DIGI_PICTURE");
  }

  @AfterEach
  public void tearDown() {
  }

  /**
   * Tests the preload function, no database access.
   */
  @Test
  public void testPreLoad() {
    DigiPicture pic1 = new DigiPicture();
    String fileName1 = TEST_DATA_FOLDER + "/CD1/Landschaftsbau2021/P1011615.JPG";
    log("Testimage is " + fileName1);
    try {
      pic1.preLoad(new File(fileName1));
    } catch (IOException ex) {
      print("Error loading file " + fileName1);
      print(ex.getLocalizedMessage());
    } catch (JpegProcessingException | TiffProcessingException | PngProcessingException ex) {
      print("Error processing image");
      print(ex.getLocalizedMessage());
    }
    log("Image preloaded");
    log("" + pic1.getPictureTakenMilis());
    assertEquals("01.Jan.2021", pic1.getPictureTakenDay(), "Wrong takenDay data");
    assertEquals(1609517517L, pic1.getPictureTakenMilis(), "Wrong taken day in millies");

    String fileName2 = TEST_DATA_FOLDER + "/CD1/Landschaftsbau2021/PC301600.JPG";
    try {
      pic1.preLoad(new File(fileName2));
    } catch (IOException ex) {
      print("Error loading file " + fileName2);
      print(ex.getLocalizedMessage());
    } catch (JpegProcessingException | TiffProcessingException | PngProcessingException ex) {
      print("Error processing image");
      print(ex.getLocalizedMessage());
    }
    log("Image preloaded");
    log("" + pic1.getPictureTakenMilis());
    assertEquals("30.Dec.2020", pic1.getPictureTakenDay(), "Wrong takenDay data for file 2");
    assertEquals(1609342397L, pic1.getPictureTakenMilis(), "Wrong taken day in millies for file 2");

  }

  /**
   * Test to preload a imag to the database and check if it is ok
   */
  @Test
  public void testPreLoadAndSave() {
    countDigiPicture(0);
    DigiPicture pic1 = new DigiPicture();
    String fileName1 = TEST_DATA_FOLDER + "/CD1/Landschaftsbau2021/P1011615.JPG";
    log("Testimage is " + fileName1);
    try {
      pic1.preLoad(new File(fileName1));
    } catch (IOException ex) {
      print("Error loading file " + fileName1);
      print(ex.getLocalizedMessage());
    } catch (JpegProcessingException | TiffProcessingException | PngProcessingException ex) {
      print("Error processing image");
      print(ex.getLocalizedMessage());
    }
    log("Image preloaded");
    log("" + pic1.getPictureTakenMilis());
    assertEquals("01.Jan.2021", pic1.getPictureTakenDay(), "Wrong takenDay data");
    assertEquals(1609517517L, pic1.getPictureTakenMilis(), "Wrong taken day in millies");

    pic1.update();

    DigiPicture picTest = DigiPicture.getById(1L);
    assertEquals("01.Jan.2021", picTest.getPictureTakenDay(), "Wrong takenDay data");
    assertEquals(1609517517L, picTest.getPictureTakenMilis(), "Wrong taken day in millies");
  }

  /**
   * Tests updating an existing entry
   */
  @Test
  public void testPreloadAndUpdate() {
    log();
    countDigiPicture(0);
    DigiPicture pic1 = new DigiPicture();
    String fileName1 = TEST_DATA_FOLDER + "/CD1/Landschaftsbau2021/P1011615.JPG";
    log("Testimage is " + fileName1);
    try {
      pic1.preLoad(new File(fileName1));
    } catch (IOException ex) {
      print("Error loading file " + fileName1);
      print(ex.getLocalizedMessage());
    } catch (JpegProcessingException | TiffProcessingException | PngProcessingException ex) {
      print("Error processing image");
      print(ex.getLocalizedMessage());
    }
    log("Image preloaded");
    log("" + pic1.getPictureTakenMilis());
    assertEquals("01.Jan.2021", pic1.getPictureTakenDay(), "Wrong takenDay data");
    assertEquals(1609517517L, pic1.getPictureTakenMilis(), "Wrong taken day in millies");
    pic1.update();
    assertEquals(0, pic1.getVersion(), "Initial Version, no refresh");
    assertEquals(1L, pic1.getId(), "Initial Id, no refresh");
    
    pic1.setOrientation(5);
    pic1.update();
    countDigiPicture(1);
//    assertEquals(1, pic1.getVersion(), "Version after 1st update, no refresh");
    assertEquals(1L, pic1.getId(), "Id, no refresh");
    
    DigiPicture picTest = DigiPicture.getById(1L);
    assertNotNull(picTest, "Could not find picture with ID = 1");
    assertEquals(1, picTest.getVersion(), "Wrong Version");
    assertEquals(5, picTest.getOrientation());
    assertEquals(1609517517L, picTest.getPictureTakenMilis(), "Wrong taken day in millies");
  }
  // TODO add test methods here.
  // The methods must be annotated with annotation @Test. For example:
  //
  // @Test
  // public void hello() {}
}
