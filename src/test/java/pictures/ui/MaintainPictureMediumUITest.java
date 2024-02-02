/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/UnitTests/JUnit4TestClass.java to edit this template
 */
package pictures.ui;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import javax.swing.JButton;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picdata.MediumType;
import picdata.PictureMedium;
import picdata.TestBaseClass;

import rzx.ui.ZxTextField;

/**
 * Erste Testgruppe fuer die MaintainPictureMediumUI Klasse. Tests gehen auf
 * eine leere Datenbank
 *
 * @author rene
 */
public class MaintainPictureMediumUITest extends TestBaseClass {

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
        m_picJPAUtil.deleteTableContent("PictureMedium");
        m_picJPAUtil.deleteTableContent("MediumType");
    }

    @AfterEach
    public void tearDown() {
    }

    /**
     * Testet die initiale Anlage eines neuen picture mediums in der
     * Datenbank sowie ersten update des gerade erzeugten Datensatzes.
     */
    @Test // @Disabled
    protected void testCreatePictureMedium() {
        createMediumType("DVD");
        MaintainPictureMediumUI ui = new MaintainPictureMediumUI();
        ArrayList<ZxTextField> textFields = ui.getTextFields();
        ArrayList<JButton> buttons = ui.getButtons();
        textFields.get(0).setText("102");
        JButton newButton = buttons.get(0);
        newButton.doClick();     // should create the entry in the database
        int numDVDs = sqlUtil.countElementsInTable("PICTURE_MEDIUM");
        assertEquals(1, numDVDs, "Erzeugte Anzahl DVDs");
        ui.updateSelectedMedium();  // prepare for the save operation
        textFields.get(1).setText("New Label");
        JButton saveButton = buttons.get(1);
        saveButton.doClick();
        assertEquals(1, sqlUtil.countElementsInTable("PICTURE_MEDIUM"), "Erzeugte Anzahl DVDs after first update");
    }

    /**
     * a bit more: create some enties, update one and add one more.
     */
    @Test
    protected void testUpdatePictureMedium() {
        createMediumType("DVD");
        createMediumType("DVD_RAM");
        MediumType cd = createMediumType("CD");
        createPictureMedium(101, cd);
        createPictureMedium(102, cd);
        createPictureMedium(103, cd);
 //  Erstes PictureMedium for the test
        MaintainPictureMediumUI ui = new MaintainPictureMediumUI();
        ArrayList<ZxTextField> textFields = ui.getTextFields();
        ArrayList<JButton> buttons = ui.getButtons();
        JButton newButton = buttons.get(0);
        textFields.get(0).setText("104");
        textFields.get(1).setText("Nochn Bildersatz (104)");
        newButton.doClick();
        assertEquals(4, sqlUtil.countElementsInTable("PICTURE_MEDIUM"), "Erzeugte Anzahl DVDs");
//   Update of this entry
        ui.updateSelectedMedium();
        textFields.get(1).setText("Updated Bildersatz (104)");
        JButton saveButton = buttons.get(1);
        saveButton.doClick();
        long dvdId = ui.getLastId();
// Clear ui and create new entry
        JButton clearButton = buttons.get(2);
        clearButton.doClick();
        assertEquals(4, sqlUtil.countElementsInTable("PICTURE_MEDIUM"), "Erzeugte Anzahl DVDs after update");
        textFields.get(0).setText("105");
        textFields.get(1).setText("Urlaubsbilder (105)");
        newButton.doClick();
        assertEquals(5, sqlUtil.countElementsInTable("PICTURE_MEDIUM"), "Erzeugte Anzahl DVDs");
// DVD von oben Laden und aktualisieren
        PictureMedium mediumForTest;
        try (EntityManager em = m_picJPAUtil.createEntityManager()) {
            mediumForTest = em.find(PictureMedium.class, dvdId);
            ui.setSelectedMedium(mediumForTest);
        }
        textFields.get(1).setText("Test");
        saveButton.doClick();
        assertEquals(5, sqlUtil.countElementsInTable("PICTURE_MEDIUM"), "Erzeugte Anzahl DVDs");
        
        PictureMedium mediumToCheck;
        try (EntityManager em = m_picJPAUtil.createEntityManager()) {
            mediumToCheck = em.find(PictureMedium.class, dvdId);
            ui.setSelectedMedium(mediumForTest);
        }
        assertEquals("Test", mediumToCheck.getLabel(), "Updated Label??");
        assertEquals(104, mediumToCheck.getCode(),"Still code ok?");
    }
}
