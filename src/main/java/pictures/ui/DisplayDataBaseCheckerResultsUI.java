/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pictures.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Displays the result of the database check activity
 * Just plain table in the first step.
 * 
 * written 24.3.24
 * @author rene
 */
public class DisplayDataBaseCheckerResultsUI extends BaseDialogUI {
  
  private JTable m_resultTable = new JTable();
  
  public DisplayDataBaseCheckerResultsUI() {
    jInit();
  }
  
  private void jInit() {
    setTitleFromResource("checkResultUi");
    m_uiElementFactory.configure("checkResultUi");
    add(
            new JScrollPane(m_resultTable),
            BorderLayout.CENTER);
    finish();
  }
  
  public void setModel(TableModel model) {
    m_resultTable.setModel(model);
    finish();
  }

  @Override
  public void actionPerformed(ActionEvent e) {
    throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
  }
  
}
