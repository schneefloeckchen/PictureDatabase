
package pictures.ui.elements;

import picdata.PictureMedium;

/**
 *
 * @author rene
 */
public class MediumSearchTableModel  extends SearchTableModel {
  
  @Override
  public Object getValueAt(int rowIndex, int columnIndex) {
    if (m_data == null)
      return "";
    else {
      PictureMedium medium = (PictureMedium)m_data.get(rowIndex);
      switch (columnIndex) {
        case 0 -> {
          return medium.getLabel();
        }
        case 1 -> {
          return medium.getTitle();
        }
        case 2 -> {
          return medium.getContent();
        }
        case 3 -> {
          return medium.getRemark();
        }
        default -> {
          return "Error";
        }
      }
    }
  }

}
