/**
 * Utilities um per SQL auf die Datenbank zuzugreifen
 * Nur für die Testprozesse
 * For PicDatabase
 * 17.08.2023 RZ - copy from testprogram
 */
package bas;

import hib.PicJPAUtil;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author rene
 */
public class SQLUtil {

//  private DriverManager m_driverManager = null;
  private Connection m_connection;
  private String m_truncateCommand;
  private String m_ignoreForeignKeysCommand;

  public SQLUtil() {
    PicJPAUtil jpaUtil = PicJPAUtil.getInstance();
    String url = jpaUtil.getDriverUrl();
    String user = jpaUtil.getUserName();
    String pwd = jpaUtil.getUserPwd();
    m_truncateCommand = jpaUtil.getTruncateCommand();
    m_ignoreForeignKeysCommand = jpaUtil.getIgnoreForeignKeysCommand();

    try {
//      m_connection = DriverManager.getConnection(
//          "jdbc:mariadb://localhost:3306/PIC_TEST",
//          "rene", "");
      m_connection = DriverManager.getConnection(
          url,
          user, pwd);
    } catch (SQLException ex) {
      System.out.println("Error -- Kann Connection zur Datenbak nicht aufbauen");
      System.out.println("Exception -- " + ex.getLocalizedMessage());
    }
  }

  public Connection getConnection() {
    return m_connection;
  }

  public Statement createStatement() throws SQLException {
    return getConnection().createStatement();
  }

  /**
   * Methode loescht alle Elemente einer Tabelle per SQL und setzt
   * den primary key auf 0 zurück
   *
   * @param table
   */
  public void deleteTableData(String table) {
//    System.out.println("Delete table " + table);
    Statement stmt;
    try {
      stmt = m_connection.createStatement();
    } catch (SQLException ex) {
      System.out.println("ERROR -- cannot create Statement");
      System.out.println("ERROR -- " + ex.getLocalizedMessage());
      return;
    }
    try {
//      stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
//      stmt.execute("SET REFERENTIAL_INTEGRITY False;");
      stmt.execute(m_ignoreForeignKeysCommand);
    } catch (SQLException ex) {
//      System.out.println("ERROR -- beim Löschen der Daten in Tabelle "+table);
//      System.out.println("ERROR -- "+ex.getLocalizedMessage());
    }
    try {
      stmt.execute("delete from " + table + ";");
      if (!table.endsWith("MAP"))
        stmt.execute(String.format(m_truncateCommand, table));
    } catch (SQLException ex) {
      System.out.println("ERROR -- beim Löschen der Daten in Tabelle " + table);
      System.out.println("ERROR -- " + ex.getLocalizedMessage());
    }
  }

  /**
   * Methode zaehlt die Elemente in einer Tabelle
   *
   * @param table
   * @return Number of rows in the table or -1 if an error occured
   */
  public int countElementsInTable(String table) {
    try {
      Statement stmt = m_connection.createStatement();
      ResultSet res = stmt.executeQuery("select count(*) from " + table + ";");
      res.next();
      return res.getInt(1);
    } catch (SQLException ex) {
      System.out.println("ERROR -- beim Zählen der Daten in Tabelle " + table);
      return -1;
    }
  }
}
