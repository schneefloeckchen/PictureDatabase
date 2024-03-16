package pictures.ui.elements;

import java.util.Set;
import javax.swing.table.TableModel;
import picdata.DigiPicture;
import picdata.PicDirectory;
import picdata.Searcher;
import java.util.logging.Logger;
import rzx.ui.ZxBaseROTableModel;

/**
 * Class to store the search result for an image, works with the
 * SearchImageDIalog class
 *
 * @author rene
 */
public class SearchImageResultStorage {

    private Searcher m_searcher = new Searcher();
    private Set<PicDirectory> m_directories = null;
    private PicDirectory m_directoryArray[] = null;
    private DigiPicture m_currentFileSystemImage = null;     // as loaded from the filesystem
    private DigiPicture m_selectedDataBaseImage = null;     // currently selected from the result list
    private Logger m_logger = Logger.getLogger(getClass().getName());

    private static final String[] SEARCH_RESULT_LABELS = new String[]{
        "CD Code", "CD Label", "CD Title", "Directory"
    };
    private static final String[] IMAGE_DATA_LABELS = new String[]{
        "ID", "Camera", "Picture Taken", "File Name" /*, "Folder", "CD Code", "CD Label" */
    };

    private SearchResultDataModel m_searchResultTableModel = new SearchResultDataModel();
    private DigiPictureInformationDataModel m_digiPictureInformationDataModel = new DigiPictureInformationDataModel();

    public SearchImageResultStorage() {
    }

    /**
     * to erase the content of the storage, e.g. is an image is not found
     * in the database
     */
    public void clear() {
        m_directoryArray = null;
        m_directories = null;
        m_searchResultTableModel.fireAllTableModelListener();
    }
    /**
     * searches in the database for an already loaded copy of the given picture.
     *
     * @param fileSystemImage
     * @return
     */
    public boolean load(DigiPicture fileSystemImage) {
        m_currentFileSystemImage = fileSystemImage;
        m_digiPictureInformationDataModel.fireAllTableModelListener();
        return load(fileSystemImage.getFileName(),
                fileSystemImage.getPictureTakenMilis());
    }

    /**
     * method to locate the image in the database and to load it to the table
     * model, found / not found indicated by method return value. Only one
     * DigPicture instance will be found, so no need to manage different blobs/
     * pictures from different folder. (n x m Relationship!!)
     *
     * @param imageFileName
     * @param dateMilis
     * @return true, if the image was found in the database
     */
    public boolean load(String imageFileName, long dateMilis) {
        m_selectedDataBaseImage = m_searcher.searchPictureByNameAndMilis(imageFileName, dateMilis);
        if (m_selectedDataBaseImage == null) {
            m_logger.severe("Picture not found: " + imageFileName + " from " + dateMilis);
            return false;     // Error Popup in calling dialog
        } else {
            m_directories = m_selectedDataBaseImage.getDirectories();
            m_directoryArray = new PicDirectory[m_directories.size()];
            int i = 0;
            for (PicDirectory dir : m_directories)
                m_directoryArray[i++] = dir;
            //m_directoryArray = (PicDirectory[]) m_directories.toArray();
            m_searchResultTableModel.fireAllTableModelListener();
            return true;
        }
    }

    public DigiPicture getSelectedPicture() {
        return m_selectedDataBaseImage;
    }

    public DigiPictureInformationDataModel getDigiPictureInformationDataModel() {
        return m_digiPictureInformationDataModel;
    }

    public TableModel getSearchResultTableModel() {
        return m_searchResultTableModel;
    }

    /**
     * Table model for the list of directories, where the image was found
     */
    class SearchResultDataModel extends ZxBaseROTableModel {

        public SearchResultDataModel() {
            m_labels = SEARCH_RESULT_LABELS;
        }

        @Override
        public int getRowCount() {
            if (m_directories == null)
                return 0;
            else
                return m_directories.size();
        }

        @Override
        public int getColumnCount() {
            return SEARCH_RESULT_LABELS.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return SEARCH_RESULT_LABELS[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            PicDirectory directory = m_directoryArray[rowIndex];
            switch (columnIndex) {
                case 0:
                    return directory.getMedium().getCode();
                case 1:
                    return directory.getMedium().getLabel();
                case 2:
                    return directory.getMedium().getTitle();
                case 3:
                    return directory.getDirectoryName();
                default:
                    return "Not Valid";
            }
        }
    }

    class DigiPictureInformationDataModel extends ZxBaseROTableModel {

        @Override
        public int getRowCount() {
            return IMAGE_DATA_LABELS.length;
        }

        @Override
        public int getColumnCount() {
            return 3;
        }

        @Override
        public String getColumnName(int columnIndex) {
            String names[] = new String[]{
                "", "Image from Filesystem", "Image from Database"
            };
            return names[columnIndex];
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return IMAGE_DATA_LABELS[rowIndex];
                case 1:
                    if (m_currentFileSystemImage == null)
                        return "";
                    else
                        return switch (rowIndex) {
                            case 1 ->
                                m_currentFileSystemImage.getCamera();
                            case 2 ->
                                m_currentFileSystemImage.getPictureTakenDate();
                            case 3 ->
                                m_currentFileSystemImage.getFileName();
                            default ->
                                "";
                        };
                case 2:
                    if (m_selectedDataBaseImage == null)
                        return "";
                    else
                        return switch (rowIndex) {
                            case 0 ->
                                m_selectedDataBaseImage.getId();
                            case 1 ->
                                m_selectedDataBaseImage.getCamera();
                            case 2 ->
                                m_selectedDataBaseImage.getPictureTakenDate();
                            case 3 ->
                                m_selectedDataBaseImage.getFileName();
                            case 4 ->
                                "not yet implemented";
                            case 5 ->
                                "not yet implemented";
                            case 6 ->
                                "not yet implemented";
                            default ->
                                "Internal Error";
                        };
                default:
                    return "Internal Error";

            }
        }

    }
}
