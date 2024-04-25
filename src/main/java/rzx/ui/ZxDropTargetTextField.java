
package rzx.ui;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.dnd.DropTargetEvent;
import java.awt.dnd.DropTargetListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A textfield, where a file can be dropped using drag and drop
 *
 * 13.11.22
 * 
 * @author rene
 */
public class ZxDropTargetTextField extends ZxTextField implements DropTargetListener{
  
  private DropTarget m_dropTarget = new DropTarget(this, this);
  
  public ZxDropTargetTextField() {
    jInit();
  }
  
  public ZxDropTargetTextField(int len) {
    super(len);
    jInit();
  }
  
  private void jInit() {
    
  }

  @Override
  public void dragEnter(DropTargetDragEvent dtde) {
    print("dragEnter");
  }

  @Override
  public void dragOver(DropTargetDragEvent dtde) {
    print ("drag over");
  }

  @Override
  public void dropActionChanged(DropTargetDragEvent dtde) {
    print ("Action Changed");
  }

  @Override
  public void dragExit(DropTargetEvent dte) {
    print ("drag Exit");
  }

  @Override
  public void drop(DropTargetDropEvent dtde) {
    print ("drop");
    dtde.acceptDrop(0);
    Transferable t = dtde.getTransferable();
    print (t.toString());
    try {
      Object o = t.getTransferData(DataFlavor.stringFlavor);
      print (o.toString());
    } catch (UnsupportedFlavorException | IOException ex) {
      Logger.getLogger(ZxDropTargetTextField.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
  
  /**
   * Entrypoint just if we need it again. 
   * @param text 
   */
  private void print(String text) {
//    Logger.getAnonymousLogger().fine(text);
  }
}
