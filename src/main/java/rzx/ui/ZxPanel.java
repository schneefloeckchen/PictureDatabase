package rzx.ui;

import java.util.function.Consumer;
import java.util.function.Function;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

/**
 * Build a Panel to be used in a dialog.
 * 
 * The methods, which add components (e.g. buttons) on the panel use a naming schema
 * to get the texts from the resource file.
 * 
 * <heading>.<ui Element type> . <key> . <hint/label/text>
 * 
 * heading: Identification of e.g. the panel or the like, set one, when the panel is initiated
 * ui-element type: can be 'button'
 *                         'field' for textfields
 *                         'combobox' or
 *                         'checkbox'
 * key: individual identifier of the element in the 'namespace/context' of the heading
 * hint: displayed as tool tip
 * label: Text of button, label.
 * 
 * @author rene
 */
public class ZxPanel extends JPanel {

    ZxResourceFactory m_resourceFactory = ZxResourceFactory.getInstance();
    ZxUIElementFactory m_uiElementFactory = ZxUIElementFactory.getInstance();
    
    protected String m_resourceHeading = null;
    protected String m_panelName = null;

    public ZxPanel() {
    }

    public ZxPanel(String heading, String key) {
        pInit(heading, key);
    }
    
    private void pInit(String heading, String key) {
        configureResource(heading);
        Border border = BorderFactory.createTitledBorder(readFromResourceFile(key+".label"));
        setBorder(border);
    }
    
    public void configureResource(String heading) {
        m_resourceHeading = heading;
        m_uiElementFactory.configure(heading);

    }

    protected String readFromResourceFile(String key) {
        if (m_resourceHeading == null)
            return key;
        else {
            return m_resourceFactory.getString(m_resourceHeading + "." + key);
        }
    }

    public JButton createAndAddButton(String key) {
        JButton button = m_uiElementFactory.createButton(key);
        add(button);
        return button;
    }
    
    public JButton createAndAddButton(String key, Consumer handler) {
        JButton button = m_uiElementFactory.createButton(key, handler);
        add(button);
        return button;
    }
    
    public JButton createAndAddCancelButton () {
        JButton button = m_uiElementFactory.createCancelButton();
        add(button);
        return button;
    }
    

    
    public ZxTextField createAndAddLabelAndField(String key) {
        add(new JLabel(readFromResourceFile("field."+key+".label")));
        return createAndAddTextfield(key);
    }
    
    public ZxComboBox createAndAddLabelAndComboBox(String key, ZxComboBoxEntry[] entries) {
        add(new JLabel(readFromResourceFile("combobox."+key+".label")));
        return createAndAddComboBox(key, entries);
    }
    
    public ZxComboBox addLabelAndComboBox(String key, ZxComboBox comboBox) {
        add(new JLabel(readFromResourceFile("combobox."+key+".label")));
        return addComboBox(key, comboBox);
    }
    
    protected ZxTextField createAndAddTextfield(String key) {
        return createAndAddTextfield(key, 10);
    }
    
    protected ZxTextField createAndAddTextfield(String key, int columns) {
        ZxTextField textField = new ZxTextField(columns);
        textField.setToolTipText(readFromResourceFile("field."+key+".hint"));
        add(textField);
        return textField;
    }

    protected ZxComboBox createAndAddComboBox(String key, ZxComboBoxEntry[] entries) {
        ZxComboBox comboBox = new ZxComboBox(entries);
        comboBox.setToolTipText(readFromResourceFile("combobox."+key+".hint"));
        add(comboBox);
        return comboBox;
    }

    protected ZxComboBox addComboBox(String key, ZxComboBox comboBox) {
        comboBox.setToolTipText(readFromResourceFile("combobox."+key+".hint"));
        add(comboBox);
        return comboBox;
    }

    public ZxCheckBox createAndAddCheckBox(String key) {
        ZxCheckBox checkBox = new ZxCheckBox(readFromResourceFile("checkbox." + key + ".title"));
        checkBox.setToolTipText(readFromResourceFile("checkbox." + key + ".hint"));
        add(checkBox);
        return checkBox;
    }

}
