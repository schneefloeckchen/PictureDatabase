package pictures.ui;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.imaging.png.PngProcessingException;
import com.drew.imaging.tiff.TiffProcessingException;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JScrollPane;
import javax.swing.border.BevelBorder;
import picdata.DigiPicture;
import pictures.ui.elements.SearchImageResultStorage;
import rzx.graphics.ZxPicturePanel;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxErrorDialog;
import rzx.ui.ZxPanel;
import rzx.ui.ZxTable;
import rzx.ui.ZxTextField;

/**
 * Dialog to search for a single picture.
 *
 * Picture is provided with drag and drop.
 *
 * Structure of the dialog:
 *
 * Row 1: Elements to locate a single file using file selected dialog<br>
 * Row 2: 2 Areas to display the selected picture from the file-system(left) and
 * one to display the picture found in the database<br>
 * Row 3: Tables below the images to display the image properties Row 4: Table
 * with entries of the found images in the database. Selecting one of the rows
 * display it in row 2, right box.<br>
 * Row 5: A Message box
 *
 * Use of BorderLayout: 1 - 3 one layout and a main layout, in NORTH 1 to 3, and
 * CENTER row 4, SOUTh Row 5
 *
 * @author rene
 */
public class SearchImageDialog extends BaseDialogUI implements MouseListener, DropTargetListener {

    private JButton mu_selectFileButton = null;   // To start FileSelect Box
    private JButton mu_loadFileButton = null;     // to start the load operation
    private JButton mu_pasteButton = null;        // To paste and load a file in the past-Buffer
    private ZxTextField mu_fileNameTextField = new ZxTextField(40);

    private ZxPicturePanel m_fileSystemImagePanel = new ZxPicturePanel();
    private DropTarget m_dropTarget = new DropTarget(m_fileSystemImagePanel, DnDConstants.ACTION_COPY, this);
    private ZxPicturePanel m_dataBaseImagePanel = new ZxPicturePanel();
    private ZxTable m_imageInfoTable = new ZxTable();    // Infos about the displayed image
    private ZxTable m_searchResultTable = new ZxTable();
//    private ZxLogPanel m_logPanel = new ZxLogPanel();

    private SearchImageResultStorage m_resultStorage = new SearchImageResultStorage();
    private DigiPicture m_fileSystemImage = null;

//    private Searcher m_searcher = new Searcher();
    SearchImageDialog() {
        super();
        jInit();
    }

    private void jInit() {
        setTitle("searchImage");
        ZxPanel panel_1_3 = new ZxPanel();
        panel_1_3.setLayout(new BorderLayout());
        ZxButtonPanel buttonFrame = new ZxButtonPanel();
        m_uiElementFactory.configure("searchImage", this);
        mu_selectFileButton = buttonFrame.createAndAddButton("selectFile");
        buttonFrame.add(mu_fileNameTextField);
        mu_loadFileButton = buttonFrame.createAndAddButton("loadFile");
        mu_pasteButton = buttonFrame.createAndAddButton("pasteFile");
        buttonFrame.add(mu_cancelButton);
        panel_1_3.add(buttonFrame, BorderLayout.NORTH);

        ZxPanel imageFrame = new ZxPanel();
        imageFrame.setLayout(new BoxLayout(imageFrame, BoxLayout.X_AXIS));
        imageFrame.add(m_fileSystemImagePanel);
        m_fileSystemImagePanel.setPreferredSize(new Dimension(DigiPicture.THUMB_SIZE, DigiPicture.THUMB_SIZE));
        m_fileSystemImagePanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        imageFrame.add(m_dataBaseImagePanel);
        m_dataBaseImagePanel.setPreferredSize(new Dimension(DigiPicture.THUMB_SIZE, DigiPicture.THUMB_SIZE));
        m_dataBaseImagePanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        panel_1_3.add(imageFrame, BorderLayout.CENTER);

// Prepare the m_fileSystemImagePanel panel for drag and drop
        m_imageInfoTable.setModel(m_resultStorage.getDigiPictureInformationDataModel());
        m_imageInfoTable.setSize(new Dimension(2 * DigiPicture.THUMB_SIZE, 120));
        m_imageInfoTable.setColumnPreferredWidth(100);
        m_imageInfoTable.setColumnWidth(2, DigiPicture.THUMB_SIZE);
        JScrollPane imageInfoPane = new JScrollPane(m_imageInfoTable);
        imageInfoPane.setPreferredSize(new Dimension(2 * DigiPicture.THUMB_SIZE, 120));
        panel_1_3.add(imageInfoPane, BorderLayout.SOUTH);

        add(panel_1_3, BorderLayout.NORTH);
        m_searchResultTable.setModel(m_resultStorage.getSearchResultTableModel());
        JScrollPane resultPane = new JScrollPane(m_searchResultTable);
        resultPane.setBorder(new BevelBorder(BevelBorder.LOWERED));
        resultPane.setPreferredSize(new Dimension(2 * DigiPicture.THUMB_SIZE, 120));
        add(resultPane, BorderLayout.CENTER);
//        m_logPanel.setSize(10, 10);
//        JScrollPane logPane = new JScrollPane(m_logPanel);
//        logPane.setSize(2 * DigiPicture.THUMB_SIZE, 50);
//        add(logPane, BorderLayout.SOUTH);
//        m_logPanel.write("UI Initiated");
        setVisible(true);
        finish();

    }

    private void performSelectFileAction() {
        JFileChooser fileChooser = new JFileChooser("/home/rene");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            mu_fileNameTextField.setText(file.getAbsolutePath());
        }
    }

    private void performLoadFileAction() {
        try {
            executeFileLoadAction(mu_fileNameTextField.getText());
        } catch (IOException ex) {
            Logger.getLogger(SearchImageDialog.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void performPasteFileAction() {
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable transferable = clipboard.getContents(this);
        if (transferable != null) processTransferable(transferable);
    }

    private void processTransferable(Transferable transferable) {
        if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            m_logger.fine("file List flavor gefunden: ");
            try {
                List<File> files = (List) transferable.getTransferData(DataFlavor.javaFileListFlavor);
                if (files.size() > 1) {
                    m_logger.severe("Too many file selected");
                    ZxErrorDialog.displaySimpleErrorMessage(this, "searchImage.message.toomanyfiles");
                } else {            // If no file is selected we do not get a drop operation
                    File imageFile = files.get(0);
                    mu_fileNameTextField.setText(imageFile.getAbsolutePath());
                    executeFileLoadAction(imageFile);
                }
            } catch (UnsupportedFlavorException | IOException ex) {
                m_logger.fine("internal -- fileListFlavor not found - " + ex.getMessage());
                ZxErrorDialog.displaySimpleErrorMessage(this, "searchImage.message.nofileflavor");
            }
        }
    }

    private void executeFileLoadAction(String fileName) throws
            IOException {
        executeFileLoadAction(new File(fileName));
    }

    private void executeFileLoadAction(File file) throws
            IOException {
        try {
            m_fileSystemImage = new DigiPicture();
            m_fileSystemImage.load(file);
            m_fileSystemImagePanel.loadPicture(
                    m_fileSystemImage.getThumbAsBufferedImage());
            boolean status = m_resultStorage.load(m_fileSystemImage);
            if (!status) {
                m_dataBaseImagePanel.loadPicture(null);
                m_resultStorage.clear();
                ZxErrorDialog.displaySimpleErrorMessage(this, "searchImage.message.imagenotfound");
            } else
                m_dataBaseImagePanel.loadPicture(m_resultStorage.getSelectedPicture().getThumbAsBufferedImage());
        } catch (JpegProcessingException
                | SQLException | TiffProcessingException | PngProcessingException ex) {
            m_logger.fine("Error processing image: " + ex.getLocalizedMessage());
            ZxErrorDialog.displaySimpleErrorMessage(this, "searchImage.message.imageprocessingerror");
        }
    }

    // ActionListener from BaseDialogUI implementation
    // for the Buttons and the like
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == mu_cancelButton)
            dispose();
        else if (src == mu_selectFileButton)
            performSelectFileAction();
        else if (src == mu_loadFileButton)
            performLoadFileAction();
        else if (src == mu_pasteButton)
            performPasteFileAction();
        else
            m_logger.severe("Action not implemented for this event");
    }

    // MouseListener implementation
    @Override
    public void mouseClicked(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mousePressed(MouseEvent e) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void mouseReleased(MouseEvent e) {  // do nothing
    }

    @Override
    public void mouseEntered(MouseEvent e) { // do nothing
    }

    @Override
    public void mouseExited(MouseEvent e) {  // do notghing
    }

    // DropTargetListener implementation
    @Override
    public void dragEnter(DropTargetDragEvent dtde) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dragOver(DropTargetDragEvent dtde) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dropActionChanged(DropTargetDragEvent dtde) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void dragExit(DropTargetEvent dte) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void drop(DropTargetDropEvent dtde) {
        m_logger.fine("dropped");
        dtde.acceptDrop(DnDConstants.ACTION_COPY);
        processTransferable(dtde.getTransferable());
    }
}
