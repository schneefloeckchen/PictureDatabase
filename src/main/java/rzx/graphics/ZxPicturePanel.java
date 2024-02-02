package rzx.graphics;

import java.awt.Graphics;
import java.awt.Image;
import javax.swing.JPanel;

/**
 * Panel to display a picture
 * 
 * @author rene
 */
public class ZxPicturePanel extends JPanel {

    private Image m_image;

    public ZxPicturePanel() {
    }

    ZxPicturePanel(Image image) {
        loadPicture(image);
    }

    public void loadPicture(Image picture) {
        m_image = picture;
        repaint();
    }

    /**
     * Routine to paint the component. 
     * @param g is provided by calling routine
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (m_image != null)
            g.drawImage(m_image, 0, 0, this);
    }
}
