/*
 * ZxUIComponent.java
 *
 * Created on 17. April 2006, 15:17
 *
 */

package rzx.ui;

/**
 * Interface offers some common methods to some UI-Components in the zgui library.
 * @author Rene Zillmann
 */
public interface ZxUIComponent {
    
    /** Erases the content of the UI element, e.g. set it to blank or selects
     * the default entry.
     */
    public void clear();
    
}
