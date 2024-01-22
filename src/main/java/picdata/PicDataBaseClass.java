/*
 * Basis Classe fuer alle Datenobjekte
 */
package picdata;

import java.util.Date;
import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Base Class for all Database Classes
 *
 * @author rene
 */
@MappedSuperclass
public abstract class PicDataBaseClass extends HibRootClass {

    @Column(name="CREATION_DATE")
    @CreationTimestamp
    private Date creationDate;
    @Column (name="REMARK")
    private String remark;
    @Transient
    private boolean dirty = false;


    /**
     * Returns the creation date of the record/object
     *
     * @return
     */
    public Date getCreationDate() {
        return creationDate;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
        setDirty();
    }

    public void setDirty() {
        dirty = true;
    }

    public boolean isDirty() {
        return dirty;
    }

    public void clearDirty() {
        dirty = false;
    }

    @Override
    public void update() {
        super.update();
        clearDirty();
    }
    
    @Override
    public void updateInSession() {
        super.updateInSession();
        clearDirty();
    }
}
