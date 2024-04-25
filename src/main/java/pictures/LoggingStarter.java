package pictures;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.LogManager;

/**
 * This class is starting the logging subsystem.
 *
 * @author rene
 */
public class LoggingStarter {

  public void startLogging() {
    try (InputStream stream = getClass().getResourceAsStream("/logging.properties")) {
      LogManager.getLogManager().readConfiguration(stream);
      System.out.println("Logging Configuration read");
    } catch (IOException ex) {
      System.out.println("Cannot read logging configuration");
    }
  }
}
