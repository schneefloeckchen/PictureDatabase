package rzx.ui;

import java.io.IOException;

/**
 * Helper functions to work with CD or DVD drives and media
 * Only for Linux
 * 
 * @author rene
 */
public class CDVDHelper {
    
    /**
     * Ejects a CD Rom on Linux
     */
    public static void eject() {
        try {
            Runtime.getRuntime().exec("eject");
        } catch (IOException ex) {
            ZxErrorDialog.displaySimpleErrorMessage(null, "cannotEject");
        }
    }
}
