package picdata;

/**
 * Stores the resolutions for different image export formats, e.g.
 * for HD Display, Tablets etc.
 * 
 * No Database table for storage
 *
 * @author rene
 */
public record PictureResolutions(
    String name,
    int xResolution,
    int yResolution) {

  private static final PictureResolutions[] resolutions = new PictureResolutions[]{
    new PictureResolutions (" -- ", 0, 0),
    new PictureResolutions("HD TV", 1280, 720),
    new PictureResolutions("JOBO Rahmen", 800, 600)
  };
  
  public static String[] getNameList() {
    String returnList[] = new String[resolutions.length];
    for (int i=0; i<resolutions.length; i++) returnList[i] = resolutions[i].name;
    return returnList;
  }
  
  public static PictureResolutions getResolution(int index) {
    return resolutions[index];
  }
  
}
