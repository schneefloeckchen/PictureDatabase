/*
 * ZxGridBagPanel.java
 *
 * Created on 26. Juli 2004, 14:29
 */
package rzx.ui;

import static java.awt.GridBagConstraints.*;
import java.awt.*;
import java.awt.event.ActionListener;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.JTextComponent;

/**
 * Class adds some useful methods to JPanel, for the placement of ui elements in
 * a GridBagLayout, to clean input fields of the container etc.
 *
 * Code required 1.5 at least
 *
 * @author Rene Zillmann
 *
 * 26.12.06 align buttons added, ZxTextField replaced by JTextComponent 21.5.08
 * in 2 addLabelAndField methods JTextComponent replaced by JComponent to allow
 * Comboboxes Nov 2018 - Updated, added the createAndAdd methods
 */
public class ZxGridBagPanel extends ZxPanel implements ZxUIComponent {

  private GridBagConstraints m_gbc = null;
  private ActionListener m_actionListener = null;
  private String trailing = "";

  /**
   * Creates a new instance of ZxPane
   */
  public ZxGridBagPanel() {
    setLayout(new GridBagLayout());
    m_gbc = new GridBagConstraints();
    m_gbc.insets = new Insets(1, 15, 1, 5);
    m_gbc.fill = GridBagConstraints.HORIZONTAL;
  }

  public ZxGridBagPanel(ActionListener actionListener) {
    this();
    m_actionListener = actionListener;
  }

  /**
   * Constructor creates a Panel, which holds a labeled border.
   *
   * @param panelName for the resource file, key there is panelName+".label",
   * for the panel name in the border.
   * @param actionListener
   */
  public ZxGridBagPanel(String panelName, ActionListener actionListener) {
    this(actionListener);
    configureResource(panelName);
    String label = readFromResourceFile("label");
//        m_gbc = new GridBagConstraints();
//        setLayout(new GridBagLayout());
//        m_gbc.ipadx = 10;
    Border border;
    if (label == null)
      border = BorderFactory.createEtchedBorder();
    else
      border = BorderFactory.createTitledBorder(label);
    setBorder(border);
  }

  /**
   * add a component to the panel, using the GridBagConstraint from this class.
   * Return value is used, as the overwriten method returns a component as well.
   * @param comp to add
   * @return the component.
   */
  @Override
  public Component add(Component comp) {
    super.add(comp, m_gbc);
    return comp;
  }
  
  /**
   * returns the GridbBagConstrints from this class for further use in
   * the calling class.
   * 
   * @return 
   */
  public GridBagConstraints getGribGagConstraints () {
    return m_gbc;
  }
  
  /**
   * Method clears the content of all ZxUICompenent components wich are added
   * to this container.
   */
  public void clearAllFields() {
    Component components[] = getComponents();
    for (Component comp : components)
      if (comp instanceof ZxUIComponent)
        ((ZxUIComponent) comp).clear();
  }

  public void newLine() {
    m_gbc.gridx = 0;
    if (m_gbc.gridy < 0)
      m_gbc.gridy = 1;     // if it is -1, some automatic layout is done.
    else
      m_gbc.gridy++;
  }

  /**
   * Adds a component to the panel at the current position defined by m_bgc.
   *
   * @param comp Component to add
   * @param gridCount number of crid-columns to be used
   */
  public void add(JComponent comp, int gridCount) {
    int gridSave = m_gbc.gridwidth;
    m_gbc.gridwidth = gridCount;
    super.add(comp, m_gbc);
    m_gbc.gridwidth = gridSave;
  }

  /**
   * Adds a label and a text field to the panel. The label is right justified,
   * the textfield left justified.
   *
   * @param label String, used for the label field to add to the panel
   * @param field JTextComponent to add
   */
  public void addLabelAndField(int xPos, int yPos,
      String label, JComponent field) {
    m_gbc.gridx = xPos;
    m_gbc.gridy = yPos;
    addLabelAndField(new ZxLabel(label), field);
  }

  /**
   * Adds a label and a text field to the panel. The label is right justified,
   * the textfield left justified.
   *
   * @param xPos pos of the label in the GridBagLayout in x
   * @param yPos pos of the label in the GridBagLayout in y
   * @param gridLength
   * @param label String, used for the label field to add to the panel
   * @param field JTextComponent to add
   */
  public void addLabelAndField(int xPos, int yPos,
      String label, JTextComponent field, int gridLength) {
    m_gbc.gridx = xPos;
    m_gbc.gridy = yPos;
    addLabelAndField(new ZxLabel(label), field, gridLength);
  }

  /**
   * Adds a label and a text field to the panel. The label is right justified,
   * the textfield left justified.
   *
   * @param label String, used for the label field to add to the panel
   * @param field JTextComponent to add
   */
  public void addLabelAndField(String label, JComponent field) {
    addLabelAndField(new ZxLabel(readFromResourceFile("field." + label + ".label")), field);
    field.setToolTipText(readFromResourceFile("field." + label + ".hint"));
  }

  /**
   * Adds a label and a text field to the panel. The label is right justified,
   * the textfield left justified.
   *
   * @param label ZxLabel field to add to the panel
   * @param field JTextComponent to add
   */
  public void addLabelAndField(ZxLabel label, JComponent field) {
    checkForGridbagLayout();
    m_gbc.anchor = EAST;
    add(label, m_gbc);
    m_gbc.gridx = RELATIVE;
    m_gbc.anchor = WEST;
    add(field, m_gbc);
  }

  /**
   * Adds a label and a text field to the panel. The label is right justified,
   * the textfield left justified. The textfield can span several cells of the
   * grid bag grid.
   *
   * @param label label text for the ZxLabel field to add to the panel
   * @param field JTextComponent to add
   * @param gridCount Number of grid cells used for the textfields compenent
   */
  public void addLabelAndField(String label, JTextComponent field, int gridCount) {
    addLabelAndField(new ZxLabel(label), field, gridCount);
  }

  /**
   * Adds a label and a text field to the panel. The label is right justified,
   * the textfield left justified. The textfield can span several cells of the
   * grid bag grid.
   *
   * @param label ZxLabel field to add to the panel
   * @param field JTextComponent to add
   * @param gridCount Number of grid cells used for the textfields compenent
   */
  public void addLabelAndField(ZxLabel label, JTextComponent field, int gridCount) {
    checkForGridbagLayout();
    m_gbc.anchor = EAST;
    add(label, m_gbc);
    m_gbc.gridx = RELATIVE;
    m_gbc.anchor = WEST;
    int gridSave = m_gbc.gridwidth;
    m_gbc.gridwidth = gridCount;
    add(field, m_gbc);
    m_gbc.gridwidth = gridSave;
  }

  public ZxTextField createAndAddLabelAndField(String key, int length, int gridCount) {
    checkForGridbagLayout();
    ZxTextField textField = new ZxTextField(length);
    textField.setToolTipText(readFromResourceFile("field." + key + ".hint"));
    addLabelAndField(new ZxLabel(readFromResourceFile("field." + key + ".label")), textField, gridCount);
//        add(textField, gridCount);
    return textField;
  }

  public ZxTextField createAndAddLabelAndField(String key, int length) {
    return createAndAddLabelAndField(key, length, 1);
  }

  @Override
  public ZxTextField createAndAddLabelAndField(String key) {
    checkForGridbagLayout();
    m_gbc.anchor = EAST;
    add(new JLabel(readFromResourceFile("field." + key + ".label")), m_gbc);
    m_gbc.gridx = RELATIVE;
    m_gbc.anchor = WEST;
    ZxTextField textField = new ZxTextField();
    textField.setToolTipText(readFromResourceFile("field." + key + ".hint"));
    add(textField, m_gbc);
    return textField;
  }

  public ZxCheckBox createAndAddLabelAndCheckBox(String key, ActionListener listener) {
    ZxCheckBox cb = createAndAddLabelAndCheckBox(key);
    cb.addActionListener(listener);
    return cb;
  }

  public ZxCheckBox createAndAddLabelAndCheckBox(String key) {
    checkForGridbagLayout();
    m_gbc.anchor = EAST;
    add(new JLabel(readFromResourceFile("checkbox." + key + ".label")), m_gbc);
    m_gbc.gridx = RELATIVE;
    m_gbc.anchor = WEST;
    ZxCheckBox cb = new ZxCheckBox();
    cb.setToolTipText(readFromResourceFile("checkbox." + key + ".hint"));
    add(cb, m_gbc);
    return cb;
  }

  public JComboBox createAndAddLabelAndComboBox(String[] values, String key) {
    checkForGridbagLayout();
    m_gbc.anchor = EAST;
    add(new JLabel(readFromResourceFile("combobox." + key + ".label")), m_gbc);
    m_gbc.gridx = RELATIVE;
    m_gbc.anchor = WEST;
    JComboBox newComboBox = new JComboBox(values);
    newComboBox.setToolTipText(readFromResourceFile("combobox." + key + ".hint"));
    add(newComboBox, m_gbc);
    return newComboBox;
  }

  public void createAndAddLabel(String key) {
    checkForGridbagLayout();
    add(new JLabel(readFromResourceFile("field." + key + ".label")), m_gbc);
  }

  @Override
  public JButton createAndAddButton(String key) {
    JButton button = m_uiElementFactory.createButton(key);
    addButton(button);
    return button;
  }

  @Override
  public JButton createAndAddCancelButton() {
    JButton button = m_uiElementFactory.createCancelButton();
    addButton(button);
    return button;
  }

  /**
   * Adds a button to the panel, buttons are always centered in their grid
   * fields
   * 3.11.21 edit RZ: increment of gridx in last line added.
   *
   * @param button the button to place this panel.
   */
  public void addButton(JButton button) {
    checkForGridbagLayout();
    int anchorSave = m_gbc.anchor;
    m_gbc.anchor = CENTER;
    add(button, m_gbc);
    m_gbc.anchor = anchorSave;
    m_gbc.gridx++;
  }

  /**
   * Method locates all buttons on this ZxGridBagPanel and aligns them to
   * equal width and height, The align pricess is done by the UIHelper class.
   */
//    public void alignYourButtons() {
//        ZxVector<ZxButton> buttons = new ZxVector();
//        Component[] components = this.getComponents();
//        for (Component c : components) {
//            if (c instanceof ZxButton) {
//                buttons.add(c);
//            }
//        }
//        UIHelper.alignButtonSize(buttons);
//    }
  private void checkForGridbagLayout() {
    if (!(getLayout() instanceof GridBagLayout))
      System.err.println("Panel has not required GridBagLayout");
  }

  /**
   * Implements the ZxUIComponent interface
   */
  @Override
  public void clear() {
    clearAllFields();
  }
}
