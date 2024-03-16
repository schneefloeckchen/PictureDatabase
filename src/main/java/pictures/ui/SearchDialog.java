package pictures.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.border.BevelBorder;
import rzx.ui.ZxButtonPanel;
import rzx.ui.ZxGridBagPanel;
import rzx.ui.ZxPanel;
import rzx.ui.ZxTextField;
import rzx.ui.ZxTreeTable;
import rzx.ui.ZxTreeTableDataModel;

/**
 * Searches for pictures, CD ROMS and Picture Directories
 *
 * Structure, all in BorderLayout
 *
 * NORTH: Dialog to define search for CD or Directory / Top Datafields to
 * display the information about the directory, which is selected in the tree
 * CENTER: SplitPanel left: Tree with folder structure of directory or CD right:
 * List of pictures
 *
 * @author rene
 */
public class SearchDialog extends BaseDialogUI {

    private static final String RESOURCEFILE_ENTRY = "searchDialog";
    private ZxGridBagPanel mu_searchPanel = new ZxGridBagPanel(this);
    private ZxTextField mu_idTextField = null;
    private JButton mu_loadButton = null;
    private JButton mu_cancelButton = null;
    private ZxButtonPanel mu_buttonPanel = new ZxButtonPanel();

    private ZxTreeTable mu_displayPanel = new ZxTreeTable();
    private ZxTreeTableDataModel m_dataModel = new ZxTreeTableDataModel();

    public SearchDialog() {
        jInit();
    }

    private void jInit() {
        setTitleFromResource(RESOURCEFILE_ENTRY);
        m_uiElementFactory.configure(RESOURCEFILE_ENTRY);
        mu_searchPanel.configureResource(RESOURCEFILE_ENTRY);
        setLayout(new BorderLayout());
        ZxPanel northPanel = new ZxPanel();
        northPanel.setLayout(new BoxLayout(northPanel, 1));
        mu_idTextField = mu_searchPanel.createAndAddLabelAndField("id", 10);
        mu_loadButton = mu_searchPanel.createAndAddButton("load");
        northPanel.add(mu_searchPanel);
        add(northPanel, BorderLayout.NORTH);

        mu_displayPanel.setDataModel(m_dataModel);
        mu_displayPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
        add(mu_displayPanel, BorderLayout.CENTER);
        setPreferredSize(new Dimension(600,500));
        finish();
    }

    private void performLoadOperation() {
        m_dataModel.loadMedium(mu_idTextField.getInt());
        mu_displayPanel.invalidate();
        mu_displayPanel.repaint();
    }

    // Implementation of the ActionListener from the BaseDialogUI
    @Override
    public void actionPerformed(ActionEvent e) {
        Object src = e.getSource();
        if (src == mu_loadButton)
            performLoadOperation();
        else
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
