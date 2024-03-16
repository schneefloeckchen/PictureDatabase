package pictures.tools;

import java.util.Date;

/**
 * reduced dataset for pictures, to reduce load time
 * @author rene
 */
public class PicShortData {
    
    long id;
    String fileName;
    int occ; // occurrence of this pic in database, >1, if on multiple CDs/DVDs
    Date taken;
//    private final MediumDeleteProcessor outer;

    public PicShortData(long id, String fileName, int occ) { //, final MediumDeleteProcessor outer) {
//  this.outer = outer;
        this.id = id;
        this.fileName = fileName;
        this.occ = occ;
    }
    
}
