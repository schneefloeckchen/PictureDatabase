/*
 * Class to support some simple tasks while creating a new User Interface
 */
package rzx.ui;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.Iterator;
import javax.swing.JButton;

/**
 *
 *
 * @author rene
 */
public class ZxUIHelper {

    /**
     * Method packs the window and centers it on the screen
     *
     * @param window, of type Window, as swing classes JFrame and JDialog are
     * also inherited from this awt class
     */
    public static void packAndCenter(Window window) {
        window.pack();
        Dimension frameSize = window.getSize();
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int newXPosition = calcPostion(screenSize.width, frameSize.width);
        int newYPosition = calcPostion(screenSize.height, frameSize.height);
        window.setLocation(newXPosition, newYPosition);
    }

    private static int calcPostion(int screen, int component) {
        return (screen - component) / 2;
    }

    /**
     * Updates the JButtons on the frame, so that all buttons have the same size
     * The method creates initially an Arraylist of the JButtons from the frame
     *
     * @param buttonFrame
     */
    public static void alignButtonSizes(Container buttonFrame) {
        ArrayList buttonList = new ArrayList();
        Component frameComponents[] = buttonFrame.getComponents();
        for (Component component : frameComponents) {
            if (component instanceof JButton) {
                buttonList.add(component);
            }
        }
        alignComponents(buttonList);
    }

    public static void alignComponents(ArrayList components) {
        int maxX = 0, maxY = 0;
        Iterator<Component> iter = components.iterator();
        while (iter.hasNext()) {       // Determine max height and witdh
            Component c = iter.next();
            Dimension d = c.getSize();
            if (d.height > maxY) {
                maxY = d.height;
            }
            if (d.width > maxX) {
                maxX = d.width;
            }
        }
        Dimension maxSize = new Dimension(maxX, maxY);
        iter = components.iterator();    // no reset in iterator interface
        while (iter.hasNext()) {
            Component c = iter.next();
            c.setSize(maxSize);
            c.setMaximumSize(maxSize);
            c.setPreferredSize(maxSize);
        }
    }

}
