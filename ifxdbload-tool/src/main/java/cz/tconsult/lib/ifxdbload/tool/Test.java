package cz.tconsult.lib.ifxdbload.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import org.apache.commons.io.IOUtils;

public class Test {

  public static void main(final String[] args) throws SQLException {
    //TODO [jaksik] implementuj - vygenerovana metoda [jaksik 7:45:28]


    try {

      Class.forName("com.informix.jdbc.IfxDriver");
      final Connection c= DriverManager.getConnection("jdbc:informix-sqli://kosatka:1529/zav1_on:;INFORMIXSERVER=kosatkarn;CLIENT_LOCALE=cs_CZ.1250;DB_LOCALE=cs_CZ.1250", "aris", "aris741");
      Statement stm = null;
      try {


        //final InputStream is = new FileInputStream(new File("c:\\tmp\\ifxdbload\\problem\\vytuhlo\\EP_PersonObligatoryCContracts.isql"));
        final InputStream is = new FileInputStream(new File("c:/tmp/ifxdbload/problem/vytuhlo/TW_SmsNahradKlice.isql"));
        //final InputStream is = new FileInputStream(new File("c:/tmp/ifxdbload/AB_Edit_t.isql"));
        //final InputStream is = new FileInputStream(new File("c:/tmp/ifxdbload/AB_KartInsert.isql"));
        final String sql = IOUtils.toString(is, "UTF-8");

        stm = c.createStatement();
        stm.setEscapeProcessing(false);
        stm.executeUpdate("drop procedure TW_SmsNahradKlice; " + sql);
      } catch (final Exception e) {
        System.err.println("sss");
        // TODO Auto-generated catch block
        e.printStackTrace();

      } finally {
        //stm.close();
        //c.close();
        System.out.println("XXX");
      }

    } catch (final Exception e) {
      System.err.println("tady");

      // TODO Auto-generated catch block
      e.printStackTrace();
    }





  }

}
