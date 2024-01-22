package picdata;

import java.util.List;
import rzx.ui.ZxComboBoxEntry;

/**
 * Interface needs to be implemented in all classes, which will be used to
 * store catalog data.
 *
 * @author rene
 */
public interface PicCatalogItem {

    /**
     * return single members of the catalog Item, the fields in database, to
     * display in a table column
     *
     * @param no entry number / table column
     * @return The data for column no
     */
    public String getEntry(int no);

    /**
     * Sets data for a specific member in an PicCatalogItem object
     *
     * @param data data to store
     * @param no column no
     */
    public void setEntry(String data, int no);

    /**
     * To update changes to the database
     */
    public void update();

    /**
     * method in this interface so that a Catalog item can use it
     * 
     * @return  true, if the item has not yet been saved
     */
    public boolean isDirty();
    
    /**
     * 
     * Method creates key value pairs to be used in an e.g. combobox.
     * No filter used, so the data are from all entries in the corresponding
     * class in the database.
     * 
     * @return created List with the key value pairs from type ZxComboBoxEntry
     */
    public List<ZxComboBoxEntry> getKeyValuePairs();


}
