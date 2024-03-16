/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package pictures.ui.elements;

import javax.swing.table.TableModel;
import rzx.ui.ZxResourceFactory;

/**
 *
 * @author rene
 */
public interface TableModelBase extends TableModel {

    public static String[] getLabels(String prefix, String[] labels) {
        ZxResourceFactory m_factory = ZxResourceFactory.getInstance();
        String[] retLabels = new String[labels.length];
        for (int i = 0; i < labels.length; i++)
            retLabels[i] = m_factory.getString(prefix + "." + labels[i]);
        return retLabels;
    }

}
