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

  /**
   * Trims a String to the defined length, if the length is below the defined,
   * only leading or trailing whitespace is removed
   * 
   * @param in 
   * @param length Maax length of the result string
   * @return  The strimmed string
   */
  public static String trimToLength(String in, int length) {
    String sTmp = in.length() < length ? in : in.substring(0, length - 1);
    return sTmp.trim();
  }

}
