/**
 * 
 */
package cz.tconsult.lib.ifxdbload.workflow;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import cz.tconsult.lib.ifxdbload.workflow.data.DbpackProperties;

/**
 * 
 * Objekt je určen na jedno provedení čtení pro DbpackReader
 * @author veverka
 *
 */
public class DbpackReaderHelper {
  //private static final Logf log = Logf.wrap(LogFactory.getLog(DbpackReaderHelper.class)); 

  private final DbpackReader iRdr;
  
  private final Stack<DbpackProperties> dbpropstack = new Stack<DbpackProperties>();

  private static final Set<String> ignoredEntries = new HashSet<String>();

  DbpackReaderHelper(DbpackReader rdr, DbpackProperties aDefaultDbpackProperties) {
    iRdr = rdr;
    dbpropstack.push(aDefaultDbpackProperties);
  }
  
  public synchronized void readDir() throws IOException {
    readDirRecursively(null);
  }
  
  
  private void readDirRecursively(String aEntryName) throws IOException {
    File ford = aEntryName == null ? root() : new File(root(), aEntryName);
    if (ford.isDirectory()) { // adresář, budeme ho prohledávat
      String[] listOfSubFords = ford.list();
      DbpackProperties dbprops = readDbpackDirProperties(ford);
      if (dbprops != null) {
        dbpropstack.push(dbprops);
        for (String jm : listOfSubFords) {
          readDirRecursively(jm); 
        }
        dbpropstack.pop();
      } else {
        for (String jm : listOfSubFords) {
          readDirRecursively(aEntryName == null ? jm : aEntryName + "/" + jm); 
        }
      }
    } else {
      if (ford.getName().toLowerCase().endsWith(".zip")) { // je to zip s dbpackem
        String odpriponovaneEntryName = aEntryName != null && aEntryName.toLowerCase().endsWith(".zip")
          ? aEntryName.substring(0, aEntryName.length() - ".zip".length()) : aEntryName;
        readOneDbpackZip(ford, odpriponovaneEntryName);
      } else { // je to běžný soubor
         FileInputStream fis = new FileInputStream(ford);
         try {
           readAndBuild(fis, aEntryName);
         } finally {
           fis.close();
         }
      }
    }
  }
  
  
  /**
   * @return
   */
  private File root() {
    return dbpropstack.peek().root;
  }

  /**
   * @param aFdbpackZip
   * @throws IOException 
   */
  private void readOneDbpackZip(File aFdbpackZip, String aNadrizeneEntryName) throws IOException {
    FileInputStream fis = new FileInputStream(aFdbpackZip);
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
    ZipEntry entry;

    DbpackProperties dbprops = readDbpackZipProperties(aFdbpackZip);
    String korenEntryName = aNadrizeneEntryName == null ? "" : aNadrizeneEntryName + "/";
    if (dbprops != null) {
      dbpropstack.push(dbprops);
      korenEntryName = "";
    }
    //    log.info("Unzipping : \"%s\" into folder \"%s\".", aFile, aAdrearProArchivaciExportu);
    while ((entry = zis.getNextEntry()) != null) {
      if (entry.isDirectory()) continue;
      String entryName = entry.getName();
      readAndBuild(zis, korenEntryName + entryName);
    }
    zis.close();
    fis.close();
    if (dbprops != null) {
      dbpropstack.pop();
    }
    
  }

  
  
  private DbpackProperties readDbpackDirProperties(File aDir) throws IOException {
    File file = new File(aDir, "dbpack.properties");
    if (!file.exists()) return null;
    FileInputStream fis = new FileInputStream(file);
    try {
      Properties properties = new Properties();
      properties.load(fis);
      DbpackProperties dbpackProperties = convertDbpackProperties(properties, aDir);
      return dbpackProperties;
    } finally {
      fis.close();
    }
  }

  
  private DbpackProperties readDbpackZipProperties(File aFdbpackZip) throws IOException {
    FileInputStream fis = new FileInputStream(aFdbpackZip);
    ZipInputStream zis = new ZipInputStream(new BufferedInputStream(fis));
    try {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String name = entry.getName();
        if (name.equals("dbpack.properties")) {
          Properties properties = new Properties();
          properties.load(zis);
          DbpackProperties dbpackProperties = convertDbpackProperties(properties, aFdbpackZip);
          return dbpackProperties;
        }
      }
      return null;
    } finally {
      zis.close();
      fis.close();
    }
  }


  /**
   * @param properties
   * @return
   */
  private DbpackProperties convertDbpackProperties(Properties properties, File root) {
    DbpackProperties dbpackProperties = new DbpackProperties();
    dbpackProperties.dbkind = properties.getProperty("dbkind");
    dbpackProperties.dbschema = properties.getProperty("dbschema");
    dbpackProperties.root = root;
    if (dbpackProperties.dbkind == null) {
      throw new RuntimeException("Dbpack neobsahuje v 'dbpack.properties' property 'dbkind' ");
    }
    if (dbpackProperties.dbschema == null) {
      throw new RuntimeException("Dbpack neobsahuje v 'dbpack.properties' property 'schema' ");
    }
    return dbpackProperties;
  }


  
  
  /**
   * @param istm
   * @param aEntryName
   * @return
   * @throws IOException
   */
  private void readAndBuild(InputStream istm, String aEntryName) throws IOException {
    DbpackProperties dbprops = dbpropstack.peek(); // s temito property zavádíme
    iRdr.readAndCallBuilder(dbprops, istm, aEntryName);
  }
  
  
  

  static {
    ignoredEntries.add("dbpack.properties"); // ignoruje se jen částerčně, ve fázi načítání dbpacku, byl však načten předem
    ignoredEntries.add("once/once.txt"); // ignoruje se jen dokonale
  }
  
}
