package rzx.util;

/**
 * Some common operations on Strings, which are not covered there
 * 
 * @author rene
 */
public class StringHelper {

  /**
   * CHecks if the string is null, empty (length = 0), or blank, e.g.
   * contains only whitespace
   * 
   * @param string
   * @return 
   */
    public static boolean isEmpty(String string) {
      return string == null ? true : string.isBlank();
    }
}
