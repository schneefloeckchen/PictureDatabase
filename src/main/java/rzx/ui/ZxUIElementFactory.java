/*
Idea:

create this as a sigleton, so that automatism can be used. To avoid interferences
if multiple UIs are build at the same time, steps can be:

startBuild
configure
build buttons
endBuild

probably the start and endBuild methods as dummies

 */
package rzx.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.MissingResourceException;
import java.util.function.Consumer;
import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;

/**
 * Factory to create button and checkbox objects. It uses ZxResourceFactory to
 * get the
 * strings for the buttons labels and tool-tips
 *
 * Structure of the keys:
 * <trailing>. The element, which is stored in the factory.
 * <trailing>.<ui element type>. The element type, like button, checkbox,
 * combobox, label, radiobutton
 * <tr>.<type>.<local key> The key to identify the individual element
 * <tr>.<error>.<local key> Error message
 * ....hint: the tooltip
 * ....title: button text
 *
 * Factory has methods to build:
 * -- JButtons
 * -- RadioButtons
 * 
 *
 * 4.11.2021 RZ Documentation amended.
 *
 * @author rene
 */
public class ZxUIElementFactory {

  private static ZxUIElementFactory instance = null;
  private final ZxResourceFactory m_resourceFactory = ZxResourceFactory.getInstance();
  private String m_trailing = "";
  private String m_title = "";
  private String m_toolTip = "";          // for the new created Buttons, Checkboxes ...

  private ActionListener m_actionListener = null;

  private ZxUIElementFactory() {

  }

  public void startBuild() {
  }

  public void endBuild() {
  }

  public static ZxUIElementFactory getInstance() {
    if (instance == null)
      instance = new ZxUIElementFactory();
    return instance;
  }

  public void configure(String trailing, ActionListener listener) {
    configure(trailing);
    setActionListener(listener);
  }

  /**
   * returns the configured trailing string. Just a bypass for elements build
   * outside this framework.
   * 
   * @return the configured trailing String, or empty if not configured.
   */
  public String getTrailing() {
    return m_trailing;
  }
  /**
   * defines the initial part incl. '.' for the names in the resource file.
   * during the calls to build the buttons, the keys are build out of this
   * trailing portion and the 2nd half, which is provided in the call
   *
   * @param trailing
   */
  public void configure(String trailing) {
    m_trailing = trailing + '.';
  }

  /**
   * stores the ActionListener which will later be used to be added to each
   * created button
   *
   * @param listener listener to be added
   */
  public void setActionListener(ActionListener listener) {
    m_actionListener = listener;
  }

  public JButton createCancelButton() {
    return createButton("cancel");
  }

  public JButton createSaveButton() {
    return createButton("save");
  }

  /**
   * Creates a button using button label and tool-tip from the resource file.
   *
   * @param key button specific key into the resource file, The key for the
   * resource file is build as described in the classes header
   *
   * @return the created button.
   */
  public JButton createButton(String key) {
    createTitleAndToolTip("button", key);
    return createButton(m_title, m_toolTip);
  }

  /**
   * Creates the JButton object and assigns an function to it.
   * The function is executed when the button is pressed. The required
   * Action handler is buried in the factor method.
   * @param key for the resource file to get label and tool tip
   * @param handler the function to execute
   * @return the created button
   */
  public JButton createButton (String key, Consumer<ActionEvent> handler) {
    createTitleAndToolTip("button", key);
    JButton button = createButtonOnly(m_title, m_toolTip);
    button.addActionListener((ActionEvent e) -> {
      handler.accept(e);
    });
    return button;
  }
  /**
   * creates a button, using title and hint as entered. If an ActionListener
   * is defined, it is added to the JButton object.
   * The Resource File not used.
   *
   * @param title of the button
   * @param hint Tool Tip of the button
   * @return the created button
   */
  public JButton createButton(String title, String hint) {
    JButton b = createButtonOnly(title, hint);
    if (null != m_actionListener)
      b.addActionListener(m_actionListener);
    return b;
  }
  
  /**
   * Creates just the button with label and hint, ActionListener is
   * not added
   * @param title of button
   * @param hint for button
   * @return 
   */
  public JButton createButtonOnly(String title, String hint) {
    JButton b = new JButton();
    b.setText(title);
    b.setToolTipText(hint);
    return b;
  }

  /**
   * Creates a JCheckBox with a label. The text for label is taken from 
   * the resource file, as described in the classes header.
   * 
   * @param key part of the key for the resource file
   * @return the created checkbox object
   */
  public JCheckBox createCheckBox(String key) {
    createTitleAndToolTip("checkbox", key);
    JCheckBox checkBox = new JCheckBox();
    buildAbstractButton(checkBox);
    return checkBox;
  }

  public JRadioButton createRadioButton(String key) {
    createTitleAndToolTip("radiobutton", key);
    JRadioButton radioButton = new JRadioButton(m_title);
    radioButton.setToolTipText(m_toolTip);
    return radioButton;
  }

  private void buildAbstractButton(AbstractButton button) {
    if (m_actionListener != null)
      button.addActionListener(m_actionListener);
    button.setText(m_title);
    button.setToolTipText(m_toolTip);
  }

  private void createTitleAndToolTip(String type, String key) {
    String key_t = m_trailing + type + "." + key;
    m_title = m_resourceFactory.getString(key_t + ".title");     // hier will ich einen Fehler sehen
    try {               // Falls der hint/tooltip nicht vorgesehen ist
      m_toolTip = m_resourceFactory.getString(key_t + ".hint");
    } catch (MissingResourceException ex) {
      m_toolTip = "";
    }
  }
}
