/*
 * ZxTextField.java
 *
 * Created on 11. Juli 2004, 10:39
 */
package rzx.ui;

import javax.swing.*;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Rene Zillmann 12.6.07 addDocumentListene added
 */
public class ZxTextField extends JTextField implements ZxUIComponent {

  public static final int READWRITE = 0;
  public static final int READONLY = 1;

  /**
   * Creates a new instance of ZxTextField
   */
  public ZxTextField() {
  }

  public ZxTextField(int len) {
    super(len);
  }

  public ZxTextField(int len, int readonly) {
    this(len);
    if (readonly == READONLY) {
      setEditable(false);
    } else {
      setEditable(true);
    }
  }

  public ZxTextField(int len, DocumentListener l) {
    this(len);
    addDocumentListener(l);
  }

  /**
   * returns the content of the textfield as int. In case of
   * a parsing error, a 0 is returned.
   * @return the parsed int value
   */
  public int getInt() {
    String text = super.getText();
    try {
      return Integer.parseInt(text);
    } catch (Exception ex) {
      return 0;
    }
  }

  public void setValue(int value) {
    setText(Integer.toString(value));
  }

  public void setValue(long value) {
    setText(Long.toString(value));
  }

  public void addDocumentListener(DocumentListener l) {
    getDocument().addDocumentListener(l);
  }

  public void removeDocumentListener(DocumentListener l) {
    getDocument().removeDocumentListener(l);
  }

  /**
   * checks, if the text field is empty, e.g. only blanks, tabs and nothing else
   * in the textField.
   * 
   * @return true if empty
   */
  public boolean isEmpty() {
    return getText().trim().length() == 0;
  }
  
  /* Implementation of the ZxUIComponent interface */
  /**
   * erases the content of the text field
   */
  public void clear() {
    super.setText("");
  }

}
