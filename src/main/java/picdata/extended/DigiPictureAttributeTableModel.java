package picdata.extended;

import java.util.Set;
import picdata.DigiPicture;
import picdata.PicDirectory;
import rzx.ui.ZxBaseROTableModel;

/**
 * Provides a tablemodel with the data from the picture including the exif data
 *
 * @author rene
 */
public class DigiPictureAttributeTableModel extends ZxBaseROTableModel {

  private static final String COLUMN_NAMES[] = {"Field", "Value"}; // @todo move to resourcefile
  private static final String LABELS[] = {
    "Code", "File Name", "Folder",
    "Camera", "Taken Day", "Taken Hour", "Orientation", "Height", "Width"
  };
  private DigiPicture m_pic;
  private Set<PicDirectory> m_directories;

  public DigiPictureAttributeTableModel() {
  }

  /**
   * Loads the picture object and redraws the table content
   *
   * @param pic to be loaded
   */
  public void load(DigiPicture pic) {
    m_pic = pic;
    m_directories = pic.getDirectories();
    fireAllTableModelListener();
  }

  @Override
  public int getRowCount() {
    return LABELS.length + ((m_directories != null) ? m_directories.size() : 0);
  }

  @Override
  public int getColumnCount() {
    return 2;
  }

  @Override
  public String getColumnName(int columnIndex) {
    return COLUMN_NAMES[columnIndex];
  }

  @Override
  public Class<?> getColumnClass(int columnIndex) {
    return String.class;
  }

  /**
   * 
   * Returns the label text for column 1, and the data for column 2. For
   * rownumbers larger than the number of labels, the directory names are returned,
   * where the picture is stored. In brackets are the Codes of the storage
   * media shown.
   * 
   * @param rowIndex see interface documentation
   * @param columnIndex 1st column are the labels, 2nd the data from the picture.
   * @return 
   */
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    switch (columnIndex) {
      case 0 -> {
        if (rowIndex < LABELS.length)
          return LABELS[rowIndex];
        else
          return "";
      }
      case 1 -> {
        if (m_pic == null)
          return "";
        else
          return switch (rowIndex) {
            case 0 ->
              m_pic.getId();
            case 1 ->
              m_pic.getFileName();
            case 2 ->
              "Number of Folders: " + m_directories.size();
            case 3 ->
              m_pic.getCamera();
            case 4 ->
              m_pic.getPictureTakenDay();
            case 5 ->
              m_pic.getPictureTakenTime();
            case 6 ->
              m_pic.getOrientation();
            case 7 ->
              m_pic.getHeight();
            case 8 ->
              m_pic.getWidth();
            default -> {              // All rows larger than label count.
              PicDirectory dir
                  = (PicDirectory) (m_directories.toArray())[rowIndex - LABELS.length];
              yield dir.getDirectoryName() + " (Code: " + dir.getMedium().getCode() + ")";
            }
          };
      }
      default -> {
        return "invalid column";
      }
    }
  }

//    @Override
//    // only display of data
//    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
//    }
//
}
