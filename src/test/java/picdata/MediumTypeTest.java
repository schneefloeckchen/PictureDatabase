package picdata;

import bas.SQLUtil;
import bas.TestBase;
import hib.PicJPAUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author rene
 * 19.8.23 / initial for JUnit 5 etc. Used also for testing the timestamp
 * 27.10.23 / Test ok with Mariadb database, count errors w. H2.
 * 
 * 
 */
public class MediumTypeTest extends TestBase {
  SQLUtil sqlUtil = new SQLUtil();
  PicJPAUtil jpaUtil = PicJPAUtil.getInstance();
  
  public MediumTypeTest() {
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
//    jpaUtil.deleteTableContent("MediumType");
  }
  
  @AfterEach
  public void tearDown() {
  }

  @Test
  public void testCreateMediumType() {
    MediumType mt = new MediumType();
    mt.setDepiction("DVD");
    mt.update();
    int anzMedium=sqlUtil.countElementsInTable("MEDIUM_TYPE");
    assertEquals(1, anzMedium, "One mediumtype created?");
  }
  
  @Test
  public void testTimeStamp() {
    MediumType mt = new MediumType();
    mt.setDepiction("DVD");
    mt.setCapacity(4700);
    mt.setRemark("For Testing only");
    mt.update();
    assertNotNull(mt.getId(), "ID created and did he reload?");
    log("Checking timestamp - "+mt.getCreationDate());
    assertNotNull(mt.getCreationDate(), "Crweation Date created in database?");
  }
  
  @Test
  public void testCreateAndLoadMediumType() {
    MediumType mt = new MediumType();
    mt.setDepiction("DVD");
    mt.setCapacity(4700);
    mt.setRemark("For Testing only");
    mt.update();
    assertEquals(1, sqlUtil.countElementsInTable("MEDIUM_TYPE"), "Wrong number of objects in database");
//
    MediumType mtTest = (MediumType) MediumType.getById(MediumType.class, 1L);
    assertNotNull(mtTest, "Could not load MediumType with ID = 1");
  }
  
  @Test
  public void testCreateMultipleAndEditOne() {
    for (int i=0; i<5; i++) {
      MediumType mt = new MediumType();
      mt.setDepiction("Medium"+i);
      mt.update();
    }
    int anzEntries = sqlUtil.countElementsInTable("MEDIUM_TYPE");
    
    assertEquals(5, anzEntries, "Wrong number of entries in MEDIUM_TYPE");
    MediumType mtTest = (MediumType) MediumType.getById(MediumType.class, 3L);
    assertNotNull(mtTest, "Could not load MediumType with ID = 3");
    mtTest.setDepiction("Geaendert");
    mtTest.update();
    anzEntries = sqlUtil.countElementsInTable("MEDIUM_TYPE");
    assertEquals(5, anzEntries, "Wrong number of entries in MEDIUM_TYPE after edit");
    
    MediumType mtCheck = (MediumType) MediumType.getById(MediumType.class, 3L);
    assertNotNull(mtTest, "Could not load MediumType with ID = 3");
    assertEquals("Geaendert", mtCheck.getDepiction(), "Wrong depiction in updated entry");
  }
  
  /**
   * just some code to evaluate the properties of the entityManager factory
   */
/*  @Test
  public void testEntityManager() {
//    PicJPAUtil jpaUtil = PicJPAUtil.getInstance();
    EntityManagerFactory factory = jpaUtil.getEntityManagerFactory();
    Map<String, Object>properties = factory.getProperties();
    properties.forEach((String k, Object v) ->{
        System.out.println ("Property "+k+ "Value Type: "+
            v.getClass().getName() + "  Value: "+v.toString());
    });
    
    Object to = properties.get("rzi.test");
    if (to != null) System.out.println(">> Ergebnis = "+to.toString());
    else System.out.println(">> Cannot find key in Map");
  }*/
}
