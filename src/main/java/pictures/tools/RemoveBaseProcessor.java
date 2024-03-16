package pictures.tools;

import java.util.logging.Logger;
import rzx.ui.ZxLogPanel;

/**
 * Base Class for the remove functionalities: Remove folder, remove medium
 * Basically for logging purpose om ui
 *
 * @author rene
 */
public class RemoveBaseProcessor {

  protected ZxLogPanel mu_logPanel = null;
  protected final Logger m_logger = Logger.getLogger(getClass().getName());

  public RemoveBaseProcessor() {
  }

  public RemoveBaseProcessor(ZxLogPanel logPanel) {
    mu_logPanel = logPanel;
  }

  protected void writeToLogPanel(String text) {
    if (mu_logPanel != null) {
      mu_logPanel.write(text);
    }
  }

}
