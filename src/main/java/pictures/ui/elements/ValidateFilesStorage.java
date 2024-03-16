package pictures.ui.elements;

import java.util.LinkedList;
import java.util.List;
import javax.swing.table.TableModel;
import rzx.ui.ZxBaseROTableModel;

/**
 * Class stores information about the images found in the database and/or are
 * missing in the database
 *
 * @author rene
 */
public class ValidateFilesStorage {

  List<MissingEntry> m_missingEntries = null;
  List<FoundEntry> m_foundEntries = null;

  public ValidateFilesStorage() {
    m_missingEntries = new LinkedList<>();
    m_foundEntries = new LinkedList<>();
  }

  public void addMissingEntry(String fileName, String folderName, long entryID) {
    m_missingEntries.add(new MissingEntry(fileName, folderName, entryID));
  }

  public void addFoundEntry(String fileName, String folderName,
      long imageID, int cdCode, String cdLabel, String cdFolder) {
    m_foundEntries.add(new FoundEntry(
        fileName, folderName, imageID, cdCode, cdLabel, cdFolder));
  }
  
  public TableModel getFoundFilesTableModel() {
    return new FoundFilesTableModel();
  }
  
  public TableModel getMissingFilesTableModel() {
    return new MissingFilesTableModel();
  }

  private class FoundFilesTableModel extends ZxBaseROTableModel {

    public FoundFilesTableModel() {
      setLabels(new String[] {
        "FIle Name", "Folder Name",
        "ID of Image", "Code of Medium",
        "Medium Label", "Folder in Database"
      });
    }
  
    @Override
    public int getRowCount() {
      return m_foundEntries.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      FoundEntry entry = m_foundEntries.get(rowIndex);
      return switch (columnIndex) {
        case 0 ->
          entry.fileName;
        case 1 ->
          entry.folderName;
        case 2 ->
          "" + entry.imageID;
        case 3 ->
          "" + entry.cdCode;
        case 4 ->
          entry.cdLabel;
        case 5 ->
          entry.cdFolder;
        default ->
          "wrong column index";
      };
    }

  }

  private class MissingFilesTableModel extends ZxBaseROTableModel {

    MissingFilesTableModel() {
      setLabels (new String[] {
        "File Name", "Folder Name", "Details"
      });
    }
    
    @Override
    public int getRowCount() {
      return m_missingEntries.size();
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
      return String.class;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
      MissingEntry entry = m_missingEntries.get(rowIndex);
      return switch (columnIndex) {
        case 0 ->
          entry.fileName;
        case 1 ->
          entry.folderName;
        case 2 -> {
          long id = entry.entryID;
          if (id > 0)
            yield "Wrong time: ID=" + id;
          else if (id == -1)
            yield "not in DB";
          else if (id == -2)
            yield "Exception reading data from file";
          else
            yield "Internal error";
        }
        default ->
          "Missing";
      };
    }

  }

  class MissingEntry {

    String fileName;
    String folderName;
    long entryID;      // if same filename, but different timestamp the image ID
    // -1 if fileName not in dataBase

    MissingEntry(String fileName, String folderName, long entryID) {
      this.fileName = fileName;
      this.folderName = folderName;
      this.entryID = entryID;
    }
  }

  class FoundEntry {

    String fileName;
    String folderName;          // From the file in the file-system
    long imageID;         // ID in database
    int cdCode;
    String cdLabel;
    String cdFolder;               // From the folder structure in the database

    FoundEntry(String fileName, String folderName,
        long imageID, int cdCode, String cdLabel, String cdFolder) {
      this.fileName = fileName;
      this.folderName = folderName;
      this.imageID = imageID;
      this.cdCode = cdCode;
      this.cdLabel = cdLabel;
      this.cdFolder = cdFolder;

    }
  }
}
