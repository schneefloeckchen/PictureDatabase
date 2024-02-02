package rzx.ui;

import javax.swing.JTextArea;

/**
 * Panel mit funktionen zum loggen, ohne konfiguration je Environment
 *
 * @author rene
 */
public class ZxLogPanel extends JTextArea implements Runnable {

    private String m_text = " <empty> ";

    public ZxLogPanel() {
        super(25, 80);
    }

    public void write(String text) {
        m_text = text;
        run();

    }

    public void clear() {
        setText("");
    }

    public void newLine() {
        write("");
    }
    @Override
    public void run() {
        append(m_text + "\n");
        try {
            update(this.getGraphics());
        } catch (NullPointerException ex) {
        }
    }
}
