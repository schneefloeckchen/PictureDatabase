/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package pictures;

/**
 * for test and migration purpose only
 * Dummy sozusagen
 */

import java.util.Locale;
import rzx.ui.ZxResourceFactory;

/**
 * zum umkopieren auf die finale Quelle:
 * 
 * 10.12.23 Umstellung auf NB 20 und JAVA 21
 * 
 * 
 * @author rene
 */
public class PictureDatabaseMigration {

    public static void main(String[] args) {
      System.out.println("Loading Resources");
    ZxResourceFactory factory = ZxResourceFactory.getInstance();
    factory.loadResorceFile("PicturesDatabase", Locale.GERMANY);

        System.out.println("Hello World!");
    }
}
