/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pictures.ui;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import java.util.logging.Logger;
import rzx.ui.ZxUIElementFactory;
import rzx.ui.ZxUIHelper;

/**
 * Main UI
 * 
 * @author rene
 */
public class MainUI extends JFrame implements ActionListener {
    
    private BoxLayout mu_layout = null;
    private Container mu_mainContainer = null;    // Box Layout kann nicht auf den JFrame 
                                                  // losgelassen werden
    private ZxUIElementFactory m_buttonFactory = null;
    private JButton mu_importButton = null;
    private JButton mu_maintainButton = null;
    private JButton mu_searchButton = null;
    private JButton mu_validateButton = null;
    private JButton mu_catalogButton = null;
    private JButton mu_toolsButton = null;
    
    private Logger m_logger = Logger.getLogger(getClass().getName());
    
    public MainUI() {
        jInit();
    }
    
    /**
     * All operations to build the UI
     */
    private void jInit() {
        setTitle("Picture Database");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mu_mainContainer = getContentPane();
        mu_layout = new BoxLayout(mu_mainContainer, BoxLayout.X_AXIS);
        mu_mainContainer.setLayout(mu_layout);
        m_buttonFactory = ZxUIElementFactory.getInstance();
        m_buttonFactory.configure("mainUI", this);
        mu_importButton = m_buttonFactory.createButton("import");
        mu_maintainButton = m_buttonFactory.createButton("maintain");
        mu_searchButton = m_buttonFactory.createButton("search");
        mu_validateButton = m_buttonFactory.createButton("validate");
        mu_catalogButton = m_buttonFactory.createButton("catalog");
        mu_toolsButton = m_buttonFactory.createButton("tools");
                
        mu_mainContainer.add(mu_importButton);
        mu_mainContainer.add(mu_maintainButton);
        mu_mainContainer.add(mu_searchButton);
        mu_mainContainer.add(mu_validateButton);
        mu_mainContainer.add(mu_catalogButton);
        mu_mainContainer.add(mu_toolsButton);
        this.pack();
        ZxUIHelper.alignButtonSizes(mu_mainContainer);
        ZxUIHelper.packAndCenter(this);
        m_logger.fine("UI Build");
    }

    public void setDatabaseName (String databaseName) {
        String title = this.getTitle();
        setTitle(title + " -- "+databaseName);
    }
    
    private void executeImportButton() {
        MaintainPictureMediumUI ui = new MaintainPictureMediumUI();
        ui.setVisible(true);
    }
    
    private void executeMaintainButton() {
        MaintainPictureDialog dialog = new MaintainPictureDialog();
        dialog.setVisible(true);
    }
    
    private void executeSearchButton() {
//        SearchDialog dialog = new SearchDialog();
//        dialog.setVisible(true);
        SearchImageDialog dialog = new SearchImageDialog();
    }
    
    private void executeValidateButton() {
        ValidateFilesDialog dialog = new ValidateFilesDialog();
        dialog.setVisible(true);
    }
    
    private void executeCatalogButton() {
        CatalogUI ui = new CatalogUI();
        ui.setVisible(true);
    }
    
    private void executeToolsButton() {
        BaseDialogUI ui = new ToolsUI();
        ui.setVisible(true);
    }
    
    
    
    /*
    Implementation of the ActionListener interface
    */
    @Override
    public void actionPerformed(ActionEvent ae) {
        Object obj = ae.getSource();
        if (obj == mu_importButton) executeImportButton();
        else if (obj == mu_maintainButton) executeMaintainButton();
        else if (obj == mu_searchButton) executeSearchButton();
        else if (obj == mu_validateButton) executeValidateButton();
        else if (obj == mu_catalogButton) executeCatalogButton();
        else if (obj == mu_toolsButton) executeToolsButton();
        // else
    }

}
