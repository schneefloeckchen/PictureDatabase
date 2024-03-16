package pictures.tools;

import hib.PicJPAUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.math.BigInteger;
import java.util.List;
import java.util.Set;
import picdata.Camera;
import picdata.DigiPicture;
import picdata.PicDirectory;
import picdata.PictureMedium;
import rzx.ui.ZxLogPanel;

/**
 * Processor loads a picture, given by ID and displays the data from the
 * DIGI_PICTURE record and the folder and media, where it is stored.
 *
 * @author rene
 */
public class DisplayPictureDataProcessor {

  private static final String[] DIGI_PIC_COLUMNS = {
    "CREATION_DATE", "PICTURE_TAKEN_DATE", "CAMERA", "REMARK", "FILE_NAME",
    "ORIENTATION", "HEIGHT", "WIDTH"
  };
  private static final String[] DIGI_PIC_LABELS = {
    "Creation date of Record: ", "Picture taken at: ", "Camera ID: ",
    "Remark: ", "File name of picture: ", "Orientation: ", "Height: ", "Width: "
  };

  private ZxLogPanel mu_logPanel = null;

//  private Session m_session = null;
  /**
   *
   * @param logPanel Panel w. TextArea to display the results
   */
  public DisplayPictureDataProcessor(ZxLogPanel logPanel) {

    mu_logPanel = logPanel;
  }

  public void loadBySQL(long id) {
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
//      m_session = PicHibernateUtil.getSession();
      // Load the picture
      Query query = em.createNativeQuery("SELECT " + String.join(",", DIGI_PIC_COLUMNS)
              + " FROM DIGI_PICTURE WHERE ID=" + id);
      List<Object[]> result = query.getResultList();
      mu_logPanel.newLine();
      mu_logPanel.write("Data for ID: " + id + "   (using SQL)");
      if (result == null || result.isEmpty()) {
        mu_logPanel.write("No Entry found for this ID");
      } else if (result.size() >= 1) {
        if (result.size() > 1) {
          mu_logPanel.write("Multiple entries found for this ID, displaying the first one");
        }
        Object[] line = result.get(0);
        for (int i = 0; i < DIGI_PIC_LABELS.length; i++) {
          mu_logPanel.write(DIGI_PIC_LABELS[i] + String.valueOf(line[i]));
        }
        int camaraId = ((BigInteger) line[2]).intValue();
        query = em.createNativeQuery("SELECT MODEL,MANUFACTURER FROM CAMERA WHERE ID=" + camaraId);
        List<Object[]> cameraData = query.getResultList();
        line = cameraData.get(0);
        mu_logPanel.write(" >> Camera is: " + line[0] + " from " + line[1]);
      }
      query = em.createNativeQuery("SELECT PIC_DIRECTORY_ID FROM PIC_DIR_MAP WHERE DIGI_PICTURE_ID=" + id);
      List<BigInteger> mapEntries = query.getResultList();
      mu_logPanel.write("Picture is stored in " + mapEntries.size() + " directories");
      for (BigInteger dirId : mapEntries) {
        query = em.createNativeQuery("SELECT DIRECTORY_NAME, MEDIUM FROM PIC_DIRECTORY WHERE ID=" + dirId);
        result = query.getResultList();
        Object[] line = result.get(0);
        mu_logPanel.write("Dir ID: " + dirId + " Name: " + line[0] + " Medium Id: " + line[1]);
        int mediumId = ((BigInteger) line[1]).intValue();
        query = em.createNativeQuery("SELECT CODE,LABEL,TITLE,CONTENT FROM PICTURE_MEDIUM WHERE ID=" + mediumId);
        result = query.getResultList();
        line = result.get(0);
        mu_logPanel.write("Medium Info -  Code: " + line[0] + "  Label: " + line[1] + "/" + line[2] + " (" + line[3] + ")");
      }
    }
  }

  /**
   * loads an image by ID using JPA/Hibernate
   * @todo: is this still in use in the program?
   * @param id 
   */
//    @Transactional
  public void loadByHibernate(long id) {
    try (EntityManager em = PicJPAUtil.getInstance().createEntityManager()) {
      mu_logPanel.newLine();
      mu_logPanel.write("Loading picture using hibernate - id is: " + id);

      DigiPicture picture = DigiPicture.getById(id);
      if (picture != null) {
        mu_logPanel.write("Picture Data.");
        mu_logPanel.write(DIGI_PIC_LABELS[0] + picture.getCreationDate());
        mu_logPanel.write(DIGI_PIC_LABELS[1] + picture.getPictureTakenDay());
        Camera camera = picture.getCamera();
        if (camera != null) {
          mu_logPanel.write(DIGI_PIC_LABELS[2] + camera.getModel() + " / " + camera.getHersteller() + " (" + camera.getId() + ")");
        } else {
          mu_logPanel.write("No camera Data");
        }
        mu_logPanel.write(DIGI_PIC_LABELS[3] + picture.getRemark());
        mu_logPanel.write(DIGI_PIC_LABELS[4] + picture.getFileName());
        mu_logPanel.write(DIGI_PIC_LABELS[5] + picture.getOrientation());
        mu_logPanel.write(DIGI_PIC_LABELS[6] + picture.getHeight());
        mu_logPanel.write(DIGI_PIC_LABELS[7] + picture.getWidth());

        Set<PicDirectory> directories = picture.getDirectories();
        mu_logPanel.write("Picture stored in " + directories.size() + " directories");
        for (PicDirectory directory : directories) {
          mu_logPanel.write("Dir ID: " + directory.getId() + " Name: " + directory.getDirectoryName()
                  + " Medium Id: " + directory.getMedium());
          PictureMedium medium = directory.getMedium();
          if (medium != null) {
            mu_logPanel.write("Medium Info -  Code: " + medium.getCode() + "  Label: " + medium.getLabel() + "/" + medium.getTitle()
                    + " (" + medium.getContent() + ")");
          } else {
            mu_logPanel.write("Could not load medium ");
          }
        }
      } else {
        mu_logPanel.write("Cannot load picture");
      }
    }
  }
}
