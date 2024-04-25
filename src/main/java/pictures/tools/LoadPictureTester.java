package pictures.tools;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import com.drew.metadata.MetadataException;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Date;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import picdata.DigiPicture;
import rzx.graphics.ExifHelper;
import rzx.graphics.ZxPicturePanel;
import rzx.ui.ZxLogPanel;

/**
 * Class to testrun the load m_picture functionality, including creation of
 * thumb, examination of EXIF Data and save to the database. No UI intended
 *
 * Seperation from tools dialog to keep the classes little.
 *
 * @author rene
 */
public class LoadPictureTester {

    private final JDialog mu_parentDialog;
    private final ZxLogPanel mu_logPanel;
    private DigiPicture m_picture = null;

    public LoadPictureTester(JDialog dialog, ZxLogPanel logPanel) {
        mu_parentDialog = dialog;
        mu_logPanel = logPanel;
        mu_logPanel.write("LoadPictureTester created");
    }

    public void loadFile(File file) {
        long newId;
        try {
            m_picture = new DigiPicture();
            m_picture.load(file);
            write("FileName from File: " + m_picture.getFileName());
            write("Camera: " + m_picture.getCamera().toString());
            m_picture.update();
            newId = m_picture.getId();
            Date takenTime = m_picture.getPictureTakenDate();
            if (takenTime == null)
                write("Time Taken = null");
            else
                write("Aufgenommen am " + takenTime.toString());

            displayPicture();
            DigiPicture picL = DigiPicture.getById(newId);
            Date takenTimeL = picL.getPictureTakenDate();
            write("Aufgenommen am " + takenTimeL.toString());

        } catch (JpegProcessingException ex) {
            write("JpecProcessingError -- " + ex.getLocalizedMessage());
            ex.printStackTrace();
        } catch (IOException ex) {
            write("JpecIOError -- " + ex.getLocalizedMessage());
            ex.printStackTrace();
        } catch (SQLException ex) {
            write("JpecSQLError -- " + ex.getLocalizedMessage());
            ex.printStackTrace();
        } catch (TiffProcessingException ex) {
            write("TiffError -- " + ex.getLocalizedMessage());
            ex.printStackTrace();
        } catch (PngProcessingException ex) {
            write("PngError -- " + ex.getLocalizedMessage());
            ex.printStackTrace();
        }

//        MaintainPictureDialog dialog = new MaintainPictureDialog();
//        dialog.loadById(newId);
//        dialog.setVisible(true);
    }

    public void displayPicture() {
        JDialog dialog = new JDialog();
        ZxPicturePanel picturePanel = new ZxPicturePanel();
        dialog.add(new JScrollPane(picturePanel));
        picturePanel.loadPicture(m_picture.getImage());
        dialog.setVisible(true);
    }

    public void listExifData(File file) {
        write("Testing EXIF Helper");
        ExifHelper eh = new ExifHelper();
        try {
            eh.load(file);
            try {
                write("Orientation: " + eh.getPictureOrientation());
                write("Taken: " + eh.getPictureTakenDate());
                write("Width: " + eh.getPictureWidth());
                write("Model: " + eh.getCameraModel());
            } catch (MetadataException ex) {
                write("Meta Data Exception");
                ex.printStackTrace();
            }
        } catch (JpegProcessingException | IOException | TiffProcessingException | PngProcessingException ex) {
            write("Jpeg or IO Exception");
            ex.printStackTrace();
        }

    }

    private void write(String text) {
        mu_logPanel.write(text);
    }
}
