/*
 *  Bilder-Archiv, die zweite.
 *  Nachfolger der existierenden Lösung, Ideen
 *    1. Java Application mit Hibernate, zum Einlesen, Bearbeiten am PC
 *    2. Zusätzlich Web-Application (Grails, JSF, ...), um über Tablet, Smart-Phone
 *        die Bilder auch zu betrachten, ggf. Infos zu ergänzen.
 *    3. Ggf. Abbindung an Printing Dienste, via internet, sowie display Dienste,
 *       Instagram and the like
 *    4. Anbindung an Identifikationsdienste, e.g. Gesichtserkennung a.la. google
 *    5. Parallel dazu auch gleich meine Bibliotheken modernisieren (Java 9??)
 *
 * ----------------------------------------------------------------------------
  Hier im Header auch log Info zu benutzter Technik, e.g. Hibernate
  Using Hibernate in a swing application:
  https://netbeans.org/kb/docs/java/hibernate-java-se.html
 
   Zum Start:
   Aus NETBEANS 8.2 heraus, neue Java Application erzeugt, gibt u.a. diese Klasse
   MYSQL/MariaDB starten, database anlegen (PIC_P), als end user tabelle anlegen.
   Hibernate Support hinzufuegen:
   o Hibernate Configuration file erzeugen, dies soll die Hibernate Libraries auch
     hinzufügen.
     * Neue Database connection in Netbeans hinzugefuegt, auf Service Tab. 
     * Dann Source Package selectiert, neu -> Hibernate Configuration Wizard
     * Alles default lassen, aber die neue Database Connection aus Combobox. und 
       finish.
     Bei den Libraries tauchen jetzt Hibernate 4.3.x, sowie der
     mysql-connector 5.1.23 auf.
     Im in NB eingebetteten XML Editor das hibernate.cfg.xml file oeffnen, und unter 
     Miscellaneous Properties  die Property hibernate.query.factory_class mit
     org.hibernate.hql.internal.classic.ClassicQueryTranslatorFactory belegen. Gueltig
     fuer die mit NB8.2 mitgelieferte Hibernate Version. Fuer 7.x anderer Wert.
   o HibernateUtil.java Helper Klasse erzeugen.Folder hib erzeugt (Klassen 
     sollen ja nicht in den root. Dann mit new HibernateUtil.java suchen, besserer
     Name geben und finish. Edit nicht noetig.

Okt 7, 2019: Umstellung auf Java 11 und NB 11.1:
   o modules-info.java von Netbeans erstellt.
   o Manuell java.desktop und die zwei java.sql module ergaenzt
   o Hibernate benoetigt noch opens picdata, damit der Hibernate persistenzer auf die
     Domnain-Klassen zugreifen kann.

Feb 10, 2020: Umstellung auf NB 11.2 und Java 13.
   Hibernate Version nicht aktualisieren. weiter benutzt: 5.4.6
   Neue Version des EXIF Extraktors 2.13. genutzt, dieser benoetigt neuere
   Version des xmpcore, hier 6.1.10. 
   Anzeige und import eines Verzeichnisses getestet, scheint soweit ok zu sein.

Aug 2020: Umstellung NB 12.0, Java 14. Zudem alte Version (PicturesDatabase mit PicturesDatabase2)
   konsolidiert zu PicturesDatabase.
   GIT fuer Versionsverwaltung neu aufgesetzt.

1.9.20 Backup fuer die Datenbanken wieder aufgesetzt.
3.Sep.20: Anzeige eines Datensatzes in den Tools ergaenzt.
22.Sep.20: Nutzung des members orientation bei DigiPicture, um das Bild richtig zu positionieren,
zudem Rotate Funktion, um das Bild zu positionieren, Save Operation fuer das Bild.


11.8.21: Depricated aus Thread in MediumLoadProzessor entfernen, Testframework starten
Java ist jetzt V16.
17.8.21: Alle existierenden Test laufen nun.

13.11.21: Creation of presentation added, tagged as 1.4. Initial tested and ok,
needs better error handling.

13.11.21: Implementation of java.util.logging started.
28.11.21: Implementazion ofjava.util.logging finished, adding of EXIF Data to
the created presentation images, error handling improved.
5.12.21: Added: Handling of DEV Environment, Editing of description field in 
the root directory (of EXIF Data) of the created presentation files
xx??xx Added: Tool to create presentation pcitures, by reducing size of the image.
26.10.22 Started: Find PictureMedium by title, label, Content or folder name.

10.04.22: Moved to NB 13, migrated to JDB 17.0.2.
      Next steps: Upgrade required libraries
                  Check git
                  Repair tests
04.11.22 RZ: Migrated to NB14, module definition updated to enable the test again,
depricated Vector replaced by List, Display from the folder in Maintain Images
added, lots of todos done. Find Medium by Title, Name, Content and find folder
by name added.
04.11.2022 RZ: In MaintainPictureDialog WEST panel w. preferred size, so that it
is easier to see the image in CENTER.
Mid Nov2022 RZ: Process to search for duplicates in the filesystem added,
(CompareFolder and UI) added. Testing for the new classes added.
24.11.2022: For Validate filestables added, so that the found and missing files
can easier be analyzed.
29.11.22, testmoduile f. validate files startet.
June 23, Java 17 migration, and check for uncompilable code. Analysis shows
that a lot of changes are required, incl. some code cleanup, cleaning JPA, hibernate etc.
July 23: move to Maven - later versions of hibernate require this. Creation of
the POM file.
change package from jacax.persistence to jakarta.persistence
move from hibernate legacy Session to JPA standard EntityManager and EntityManagerFactory
replace legacy Criteria Builder with JPA standard
16.3.2024 RZ Migration to JPA w. Hibernate (6.2.6.Final) implementation, Java 21
and Maven finished. Tagged as 1.5. tar sicherung per skript erstellt
24.4.24 Migration to Java 21, JPA and Hibernate 6.2.6.final) abgeschlossen,
als 2.0 mit allen dependencies produktiv gesetzt. With 2.0 tagged, commouued and
pushed to remote
28.6.25 als 2.1 produktiv gesetzt. Memory-Leak correkted by introducing DigiPictureFctory. A lot of
JUnit Tests added. Open: Tag in git and push to github

2.2 Planung: Minor bugfixes, Cleanup, move to Java 23 or 24, move to latest Java. Keep hibernate Version, Move to latest Netbeans
bugfixes: 
(1)  2 errormessages about missing entries in hib configuration
(2)  Cannot start maintain DVD from first Dialog (adding DVDs)
(3)  Optimize JUnit Report (Surefire???)
(ff) What pops up while using it





Globale ToDos:
@todo Test for validate images, further tests and amend Java Doc.
@todo PRIO -- create index to speed up loading of folder content in maintain in
images Dialog. (1) Index on DIRE_ID in the PIC_DIR_MAP for DEV and PROD, not
really an improvement.
@todo Export Image from database, from Maintain Image dialog
@todo List to collect information about images found, for later loading from the media
@todo GLOBAL -- remove deprecated methods and classes (multithreading framework in ZxLogpanel
@todo Idea -- Docker Task??
@todo On dialog to create the presentation images allow to edit the textfields,
right now data are only written but not read.
@todo add HP 10 Tablet to the list of possible devices (Presentation pictures)
@todo optimize the start for the folder search for the presentation pics, curr. points to the test area
@todo finish Multithreading in databaseChecker, so that the result table display the progress of tje validation
 */
package pictures;

//import hib.PicHibernateUtil;
import hib.PicJPAUtil;
import jakarta.persistence.EntityManagerFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import pictures.ui.MainUI;
//import rzi.logger.Logger;
import rzx.ui.ZxResourceFactory;

/**
 * Datenbank fuer Bilder aus Digicams bzw. gescannte Bilder.
 *
 * @author rene
 *
 * @todo: Liste implementieren fue Bilder, bei denen in der Verarbeitung etwas
 * schief gegangen ist, Zunaechst im Speicher, mit export funktion.
 * @todo: Workflow beim laden der CD/DVD. Erst scan starten, wenn medium erzeugt
 * in DB
 */
public class PicturesDatabase {
  
  private static MainUI ui = null;
  private static final LoggingStarter loggingStarter = new LoggingStarter();
  
  /**
   *
   * @param args the command line arguments
   */
  public static void main(String[] args) {
  
    loggingStarter.startLogging();
    Logger logger = Logger.getLogger("pictures.PicturesDatabase");
    logger.log(Level.FINE, args == null ? "Args = null" : "Args vorhanden");
    if (args != null) {
      logger.log(Level.FINE, "Laenge args = {0}", args.length);
      for (String arg : args)
        logger.fine(arg);
    }
//    logger.log(Level.FINE, "Parameter are {0} / {1} / {2}",
//            new Object[]{args[0], args[1], args[2]});
    logger.info(args == null || args.length == 0 ? "" : "Call Argument is "+args[0]);
    
    ZxResourceFactory factory = ZxResourceFactory.getInstance();
    factory.loadResorceFile("PicturesDatabase", Locale.GERMANY);
    logger.log(Level.FINE, ">>  {0}", factory.getString("main.resTest"));
    String javaVersion = System.getProperty("java.vm.name") + "  ( "
            + System.getProperty("java.vm.version") + " / "
            + System.getProperty("java.vm.info") + " ) Version: "
            + System.getProperty("java.version");
    logger.info(javaVersion);
    String hibernateVersion = org.hibernate.Version.getVersionString();
    logger.log(Level.INFO, "Hibernate Version is: {0}", hibernateVersion);
    String databaseName = "Not determined";
    
    PicJPAUtil jpaUtil = PicJPAUtil.getInstance();
    jpaUtil.configure(args == null || args.length == 0
            ? "PIC_PROD" : "PIC_" + args[0]);
    EntityManagerFactory emFactory = jpaUtil.getEntityManagerFactory();
    Map<String, Object> emfParameter = emFactory.getProperties();
    String databaseUrl = (String) emfParameter.get("hibernate.connection.url");
    logger.log(Level.INFO, "Database Connection is: {0}", databaseUrl);
    String[] urlParts = databaseUrl.split(":");
    String[] lastElements = urlParts[urlParts.length - 1].split("/");
    databaseName = lastElements[1];
    ui = new MainUI();
    ui.setDatabaseName(databaseName);
    ui.setVisible(true);
  }

  /**
   * Setting up the logger configuration. The properties file is
   * located in home/.imageDB -> see source code. It cannot be modified through
   * the NB IDE!
   * see    https://stackoverflow.com/questions/20389255/reading-a-resource-file-from-within-jar
   * und    https://docs.geotools.org/latest/userguide/library/metadata/logging/java_logging.html
   */
  private void setupLogger() {
    Logger emergencyLogger = Logger.getAnonymousLogger();
    LogManager logManager = LogManager.getLogManager();
    InputStream res = getClass().getResourceAsStream("/logging.properties");
//    System.out.println("logging file exists? : "+f.exists());
    try {
      String path = System.getProperty("user.home");
      logManager.readConfiguration(
              new FileInputStream(
                      "logging.properties"));
      
//      String path = System.getProperty("user.home");
//      logManager.readConfiguration(
//              new FileInputStream(path
//                      + File.separator + ".imageDB/logging.properties"));
      Logger l = Logger.getLogger("initial");
      
      l.log(Level.WARNING, "Logger started with {0}", l.getLevel().toString());
    } catch (IOException | SecurityException ex) {
      emergencyLogger.severe(ex.getLocalizedMessage());
    }
  }
  
  private static void setupLoggerSave() {
    Logger emergencyLogger = Logger.getAnonymousLogger();
    LogManager logManager = LogManager.getLogManager();
    File f = new File ("/logging.properties");
    System.out.println("logging file exists? : "+f.exists());
    try {
      String path = System.getProperty("user.home");
      logManager.readConfiguration(
              new FileInputStream(
                      "logging.properties"));
      
//      String path = System.getProperty("user.home");
//      logManager.readConfiguration(
//              new FileInputStream(path
//                      + File.separator + ".imageDB/logging.properties"));
      Logger l = Logger.getLogger("initial");
      
      l.log(Level.WARNING, "Logger started with {0}", l.getLevel().toString());
    } catch (IOException | SecurityException ex) {
      emergencyLogger.severe(ex.getLocalizedMessage());
    }
  }


//  private static void setupLogger() {
//    try {
//      
//    } 
//            
//      }
//  
}
