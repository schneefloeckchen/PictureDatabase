/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rzx.ui;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Singleton, to manage the resource bundle centrally.
 * key used is the full key, any key groups (trailing..) are in the UI component
 * factory classes.
 *
 * @author rene
 */
public class ZxResourceFactory {

  private ResourceBundle m_resourceBundle = null;

  private ZxResourceFactory() {
  }

  public static ZxResourceFactory getInstance() {
    return ResourceFactoryHolder.INSTANCE;
  }

  public void loadResorceFile(String fileName, Locale locale) {
//      File test = new File("Wo_bin_ich.text");
    try {
      Writer writer = new OutputStreamWriter(new FileOutputStream("Wo_bin_ich.text"));
      try {
        writer.write("Nun?");
        writer.close();
      } catch (IOException ex) {
        ex.printStackTrace();
      }
    } catch (FileNotFoundException ex) {
      ex.printStackTrace();
    }
    m_resourceBundle = ResourceBundle.getBundle(fileName, locale);
  }

  /**
   * Return a string for display from the full key in the bundle
   *
   * @param key
   * @return the string loaded from the resource file
   */
  public String getString(String key) {
    if (m_resourceBundle == null) {
      return "No resource bundle loaded";
    } else {
      return m_resourceBundle.getString(key);
    }
  }

  private static class ResourceFactoryHolder {

    private static final ZxResourceFactory INSTANCE = new ZxResourceFactory();
  }
}
