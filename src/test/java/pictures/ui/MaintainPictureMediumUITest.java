/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pictures.ui;

import hib.PicJPAUtil;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import picdata.MediumType;
import picdata.TestBaseClass;

import static pictures.ui.MaintainPictureMediumUI.CODE_TEXT_FIELD;

/**
 * Erste Testgruppe fuer die MaintainPictureMediumUI Klasse. Tests gehen auf
 * eine leere Datenbank
 *
 * @author rene
 */
public class MaintainPictureMediumUITest extends TestBaseClass {

    private PicJPAUtil m_jpaUtil = PicJPAUtil.getInstance();
    public MaintainPictureMediumUITest() {
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
        m_jpaUtil.deleteTableContent("PictureMedium");
        m_jpaUtil.deleteTableContent("MediumType");
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Testet die initiale Anlage eines neuen picture mediums in der
     * Datenbank
     */
    @Test
    protected void testCreatePictureMedium() {
        createMediumType(101, "DVD");
        MaintainPictureMediumUI ui = new MaintainPictureMediumUI();
        ui.setTextFieldValue(CODE_TEXT_FIELD, "101");
    }
    
    private void createMediumType(long id, String depict) {
        MediumType mt = new MediumType();
        mt.setDepiction(depict);
        mt.update();
    }
}
