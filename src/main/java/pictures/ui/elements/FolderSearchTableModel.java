package pictures.ui.elements;

import picdata.PicDirectory;
import picdata.PictureMedium;

/**
 *
 * @author rene
 */
public class FolderSearchTableModel extends SearchTableModel {

//  @Override
//  public void setData(List<PicDirectory> data) {
//    m_data = data;
//  }
//  
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (m_data == null)
      return "";
    else {
      PicDirectory directory = (PicDirectory) m_data.get(rowIndex);
      PictureMedium medium = directory.getMedium();
      switch (columnIndex) {
        case 0 -> {
          return directory.getDirectoryName();
        }
        case 1 -> {
          return medium.getCode();
        }
        case 2 -> {
          return medium.getLabel();
        }
        case 3 -> {
          return medium.getTitle();
        }
        case 4 -> {
          return medium.getContent();
        }
        case 5 -> {
          return medium.getRemark();
        }
        default -> {
          return "Internal Error";
        }
      }
    }
  }
}
