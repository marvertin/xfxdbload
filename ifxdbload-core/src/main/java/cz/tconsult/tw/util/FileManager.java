package cz.tconsult.tw.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Pattern;

import org.apache.commons.io.ByteOrderMark;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.apache.commons.lang3.StringUtils;



/**
 * <pTřída souborového managera. Doplňuje vlastnosti třídy {@see java.io.File}. Tedy, to co na této třídě najdete,
 * nehledejte v tomto file manageru. Nebude to tam. Třída bude postupně doplnována o metody dle požadavků.
 * * <p>Nyní obsahuje pouze metody pro kopírování a přemísování souborů. Jsou zde zvlášt metody pro kopírování a přenost
 * do souboru a do adresáře. Nemám rád řešení, kdy pokud je cíl adresář, je do něj šupnut soubor a pokud cíl neexistuje
 * nebo je to soubor je považován za soubor. Pokud neexistuje cílový adresář, není vytvářen, k tomu lze použít metodu File.mkdirs().
 * Příklad použití:
 *
 * <pre>
 *
 * FileManager fm = FileManager.getInstance(64 *1024);
 * File zdroj = new File("/home/veverka/zdrojovysoubor.txt");
 * File cil   = new File("/home/veverka/cilovysoubor.txt");
 * fm.copyFileToFile(zdroj, cil);
 *
 * </pre>
 * <pre>
 * Pokud neexistuje cílový adresář, není vytvářen, k tomu lze použít metodu File.mkdirs().*. Příklad:
 *
 * File zdroj = new File("/home/veverka/zdrojovysoubor.txt");
 * File cil   = new File("/home/veverka/cilovysoubor.txt");
 * cil.getParent().mkdirs();
 * fm.copyFileToFile(zdroj, cil);
 *
 * </pre>
 * Pro implemetnaci je využito přímo nové IO rozhraní java.nio, takže kopírování je nejrychlejší možné, které lze jednodušše
 * v Javě realizovat. Pokud se jeví kopírování pomalé, insspirujte se touto implemetnací a použijte direct buffery, ale nejdříve si o tom něco přečtětě.
 * @author Martin Veverka
 * @version 1.0
 */

public class FileManager {
  private static final int MIN_BUFFER_SIZE = 1024 * 4;
  private static final int MAX_BUFFER_SIZE = 1024 * 1024 * 32;

  private int iBufferSize;

  private FileManager() {
  }

  /**
   * Vytvoří isntanci file manageru. Parametrem je velikost bufferu, který bude použit
   * při kopírování. Nutno vhodně zvolit podle velikosti kopírovaných souborů, jejich počtu,
   * množství paměti, požadované odezvy.
   * @param aBufferSize
   * @return
   */
  public static FileManager getInstance(final int aBufferSize) {
    final FileManager fm = new FileManager();
    fm.setBufferSize(aBufferSize);
    return fm;
  }

  /**
   * Binárně zkopíruje soubor na jiné místo. Čas vytvoření se zkopíruje také.
   * @param aFrom Zdrojový soubor
   * @param aTo Cílový soubor, pokud je to adresář, je hlášena chyba.
   * @throws IOException Při chybě
   */
  public void copyFileToFile(final File aFrom, final File aTo) throws IOException {
    if (aTo.isDirectory()) {
      throw new IOException("Path " + aTo + " is directory");
    }
    _copy(aFrom, aTo);
  }

  /**
   * Binárně zkopíruje soubor na do zadaného adresáře. Vlastní jméno souboru bude stejné. Čas vytvoření se zkopíruje také.
   * @param aFrom Zdrojový soubor
   * @param aTo Cílový adresář, pokud to není adresář, je hlášena chyba.
   * @throws IOException Při chybě
   */
  public void copyFileToDir(final File aFrom, File aTo) throws IOException {
    if (! aTo.isDirectory()) {
      throw new IOException("Path " + aTo + " is not directory");
    }
    aTo = new File(aTo, aFrom.getName());
    _copy(aFrom, aTo);
  }

  /**
   * Binárně přesune soubor na jiné místo. Čas vytvoření se přesune také.
   * Tato implementace využívá kopie a následného smazání, nespoléhejte však na to, třeba pozdější implementace
   * budou využívat služeb filesystému a přesouvat přímo.
   * @param aFrom Zdrojový soubor
   * @param aTo Cílový soubor, pokud je to adresář, je hlášena chyba.
   * @throws IOException Při chybě
   */
  public void moveFileToFile(final File aFrom, final File aTo) throws IOException {
    if (aTo.isDirectory()) {
      throw new IOException("Path " + aTo + " is directory");
    }
    _copy(aFrom, aTo);
    aFrom.delete();
  }

  /**
   * Binárně přesune soubor na do zadaného adresáře. Vlastní jméno souboru bude stejné. Čas vytvoření se přesune také.
   * Tato implementace využívá kopie a následného smazání, nespoléhejte však na to, třeba pozdější implementace
   * budou využívat služeb filesystému a přesouvat přímo.
   * @param aFrom Zdrojový soubor
   * @param aTo Cílový adresář, pokud to není adresář, je hlášena chyba.
   * @throws IOException Při chybě
   */
  public void moveFileToDir(final File aFrom, File aTo) throws IOException {
    if (! aTo.isDirectory()) {
      throw new IOException("Path " + aTo + " is not directory");
    }
    aTo = new File(aTo, aFrom.getName());
    _copy(aFrom, aTo);
    aFrom.delete();
  }

  /**
   * Vrátí nastavenou velikost bufferu, která se používá pro kopírování.
   * @return
   */
  public int getBufferSize() { return iBufferSize; }

  /**
   * Nastaví velikost bufferu pro kopírování souborů. Pro jedno běžící
   * kopírování bude vytvořen jeden buffer.
   * @param aBufferSize Velikost naastavovaného bufferu. Nejdříve bude upravena na rozumnou velikost, ani velké ani malé. A také zaokrouhlí na nějaký rozumný celý násobek.
   */
  public void setBufferSize(int aBufferSize) {
    aBufferSize = (aBufferSize + MIN_BUFFER_SIZE-1) / MIN_BUFFER_SIZE * MIN_BUFFER_SIZE;
    if (aBufferSize < MIN_BUFFER_SIZE) {
      aBufferSize = MIN_BUFFER_SIZE;
    }
    if (aBufferSize > MAX_BUFFER_SIZE) {
      aBufferSize = MAX_BUFFER_SIZE;
    }
    iBufferSize = aBufferSize;
  }

  /**
   * Informace o UTF nepovinném BOM (Byte Order Mark).
   *
   * https://en.wikipedia.org/wiki/Byte_order_mark<br />
   * http://www.rgagnon.com/javadetails/java-handle-utf8-file-with-bom.html
   *
   * @author polakm
   *
   */
  public enum EUtfBom {NO_UTF(0), UTF8(3), UTF16_BE(2), UTF16_LE(2), UTF32_BE(4), UTF32_LE(4);

    private final int size;
    private EUtfBom(final int aSize) {size = aSize;}
    public int getSize() {return size;}
    @Override public String toString() {return name() + "(" +  size + ")";}
  }

  /**
   * Vrátí informace o UTF BOM (byte order mark) pokud tam je.
   * @param aData
   * @param aDataSize
   * @return
   */
  public static EUtfBom getUtfBomInfo(final byte[] aData, final int aDataSize) {

    final EUtfBom result;
    if (aDataSize < 2) {
      result = EUtfBom.NO_UTF;
    }
    else {
      if (aData[0] == (byte)0xFE && aData[1] == (byte)0xFF) {

        result = EUtfBom.UTF16_BE;
      }
      else if (aData[0] == (byte)0xFF && aData[1] == (byte)0xFE) {

        result = EUtfBom.UTF16_LE;
      }
      else {

        if (aDataSize < 3) {

          result = EUtfBom.NO_UTF;
        }
        else {

          if (aData[0] == (byte)0xEF && aData[1] == (byte)0xBB && aData[2] == (byte)0xBF) {
            result = EUtfBom.UTF8;
          }
          else {
            if (aDataSize < 4) {
              result = EUtfBom.NO_UTF;
            }
            else {

              if (aData[0] == (byte)0x00 && aData[1] == (byte)0x00 && aData[2] == (byte)0xFE && aData[3] == (byte)0xFF) {
                result = EUtfBom.UTF32_BE;
              }
              else if (aData[0] == (byte)0xFF && aData[1] == (byte)0xFE && aData[2] == (byte)0x00 && aData[3] == (byte)0x00) {
                result = EUtfBom.UTF32_LE;
              }
              else {
                result = EUtfBom.NO_UTF;
              }
            }
          }
        }
      }
    }
    return result;
  }

  /**
   * Vrátí informace o UTF BOM (byte order mark) pokud tam je.
   * @param aData
   * @return
   */
  public static EUtfBom getUtfBomInfo(final byte[] aData) {

    final EUtfBom result = getUtfBomInfo(aData, aData.length);
    return result;
  }

  /**
   * V zadaném InputStream přeskočí UTF BOM (byte order mark) pokud tam je.
   *
   * @param aInputStream
   * @return informace o BOM.
   * @throws IOException
   */
  public static EUtfBom skipUtfBom(final InputStream aInputStream) throws IOException {

    if (!aInputStream.markSupported()) {

      throw new UnsupportedOperationException("No mark operation supported!");
    }
    aInputStream.mark(4);
    final byte[] header = new byte[4];
    final int read = aInputStream.read(header);
    aInputStream.reset();

    final EUtfBom result = getUtfBomInfo(header, read);
    return result;
  }

  /**
   * Zkonstruuje InputStream, aby byl ignorován případný UTF BOM (byte order mark).
   * @param aInputStream
   * @return
   */
  public static InputStream getInputStreamToSkipUtfBom(final InputStream aInputStream) {

    final InputStream result = new BOMInputStream(aInputStream, false/*do not include BOMs*/
        , ByteOrderMark.UTF_8, ByteOrderMark.UTF_16BE, ByteOrderMark.UTF_16LE, ByteOrderMark.UTF_32BE, ByteOrderMark.UTF_32LE);
    return result;
  }

  /**
   * Zkonstruuje Reader, aby byl ignorován případný UTF BOM (byte order mark).
   * @param aInputStream
   * @param aCharSetName
   * @return
   * @throws FileNotFoundException
   */
  public static BufferedReader getReaderToSkipUtfBom(final InputStream aInputStream, final String aCharSetName) {

    final InputStream is = getInputStreamToSkipUtfBom(aInputStream);
    final Charset cs = StringUtils.isBlank(aCharSetName) ? TcSourceCodeInfo.getDefaultCharset() : Charset.forName(aCharSetName);
    final BufferedReader result = new BufferedReader(new InputStreamReader(is, cs));
    return result;
  }

  /**
   * Zkonstruuje Reader, aby byl ignorován případný UTF BOM (byte order mark).
   * @param aInputStream
   * @return
   * @throws FileNotFoundException
   */
  public static BufferedReader getReaderToSkipUtfBom(final InputStream aInputStream) {

    final BufferedReader result = getReaderToSkipUtfBom(aInputStream, null/*aCharSetName*/);
    return result;
  }

  /**
   * Zkonstruuje InputStream, aby byl ignorován případný UTF BOM (byte order mark).
   * @param aFile
   * @return
   * @throws FileNotFoundException
   */
  public static InputStream getInputStreamToSkipUtfBom(final File aFile) throws FileNotFoundException {

    final InputStream result = getInputStreamToSkipUtfBom(new FileInputStream(aFile));
    return result;
  }

  /**
   * Zkonstruuje Reader, aby byl ignorován případný UTF BOM (byte order mark).
   * @param aFile
   * @return
   * @throws FileNotFoundException
   */
  public static BufferedReader getReaderToSkipUtfBom(final File aFile, final String aCharSetName) throws FileNotFoundException {

    final BufferedReader result = getReaderToSkipUtfBom(new FileInputStream(aFile), aCharSetName);
    return result;
  }

  /**
   * Zkonstruuje Reader, aby byl ignorován případný UTF BOM (byte order mark).
   * @param aFile
   * @return
   * @throws FileNotFoundException
   */
  public static BufferedReader getReaderToSkipUtfBom(final File aFile) throws FileNotFoundException {

    final BufferedReader result = getReaderToSkipUtfBom(aFile, null/*aCharSetName*/);
    return result;
  }

  /**
   * Obsah souboru vrácený jako pole bytů.
   * @param aFile Soubor, který má být čten.
   * @param aEncoding Kódování, pokud je null, nečte nic.
   * @return
   */
  public byte[] readWholeFileAsBytes(final File aFile) throws IOException {
    final FileInputStream in = new FileInputStream(aFile);
    byte vysl[];
    try {
      final FileChannel chan = in.getChannel();
      final ByteBuffer buffer = ByteBuffer.allocate((int)chan.size());
      buffer.clear();
      final int pocet = chan.read(buffer);
      if (pocet != chan.size()) {
        throw new RuntimeException("Z nejakeho duvodu bylo precteno jen " + pocet + " bytu, kdyz melo byt precteno" + chan.size());
      }
      buffer.flip();

      vysl = new byte[(int)chan.size()];
      buffer.get(vysl);
      //if (pocet != chan.size()) throw new RuntimeException("Z nejakeho duvodu bylo zapsano jen " + pocet + " bytu, kdyz melo byt zapsano" + chan.size());
      buffer.clear();
      chan.close();
    } finally {
      in.close();
    }
    return vysl;
  }

  /**
   * Přečte co nejrychleji celý soubor a vrátí ho jako řetězec, přeskočí případný UTF BOM (byte order mark).
   * @param aFile Soubor, který má být čten.
   * @param aEncoding Kódování, pokud je null, nečte nic.
   * @return Obsah souborui vrácený jako řetězec.
   */
  public String readWholeFileAsString(final File aFile, final String aCharSetName) throws IOException {
    final byte[] bb = readWholeFileAsBytes(aFile);
    final EUtfBom utfBom = getUtfBomInfo(bb);
    return new String(bb, utfBom.size, bb.length - utfBom.size, aCharSetName);

  }

  /**
   * Přečte co nejrychleji celý soubor a vrátí ho jako řetězec, přeskočí případný UTF BOM (byte order mark).
   * @param aFile Soubor, který má být čten.
   * @param aEncoding Kódování, pokud je null, nečte nic.
   * @return Obsah souboru vrácený jako řetězec.
   */
  public String readWholeFileAsString(final File aFile) throws IOException {
    final byte[] bb = readWholeFileAsBytes(aFile);
    final EUtfBom utfBom = getUtfBomInfo(bb);
    return new String(bb, utfBom.size, bb.length - utfBom.size);
  }


  /**
   * Zapíše řetězec do zadaného souboru. Soubor přepíše.
   * @param aFile Soubor, do kterého se zapisuje.
   * @param aString Zapisovaný řetězec.
   * @param aEncoding Požadované kódování, null pokud defaultní.
   * @throws IOException
   */
  public void writeStringToFile(final File aFile, final String aString, final String aEncoding) throws IOException {
    final Writer wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile), aEncoding));
    //System.out.p rintln(">>>>>>>>"+aString+"<<<<<<<<");
    wrt.write(aString);
    wrt.close();
  }

  /**
   * Zapíše řetězec do zadaného souboru. Soubor přepíše.
   * @param aFile Soubor, do kterého se zapisuje.
   * @param aString Zapisovaný řetězec.
   * @param aEncoding Požadované kódování, null pokud defaultní.
   * @throws IOException
   */
  public void writeStringToFile(final File aFile, final String aString) throws IOException {
    final Writer wrt = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(aFile)));
    wrt.write(aString);
    wrt.close();
  }

  /**
   * Kopíruje vstupní proud do souboru co možná nejefektivnějším způsobem.
   * @param aIs Vstupní proud, po zkopírování bude uzavřen.
   * @param aOut Soubor do něhož se kopíruje. Může, ale nemusí existovat,
   * pokud neexistuje, musí existovat adresářová cesta k němu.
   * @throws IOException
   */
  public void copyInputStreamToFile(final InputStream aIs, final File aOut) throws IOException {
    final FileOutputStream out = new FileOutputStream(aOut);
    final FileChannel outc = out.getChannel();
    final ByteBuffer buffer = ByteBuffer.allocate( iBufferSize );
    final byte[] bb = new byte[iBufferSize];
    while (true) {
      final int len = aIs.read(bb);
      if (len <=0) {
        break;
      }
      buffer.put(bb, 0, len);
      buffer.flip();
      outc.write( buffer );
      buffer.clear(); // Make room for the next read
    }
    outc.close();
    out.close();
    aIs.close();
  }

  ///////////////////////////////////// privátní metody /////////////

  private void _copy(final File aIn, final File aOut) throws IOException {
    final FileInputStream in = new FileInputStream(aIn);
    final FileOutputStream out = new FileOutputStream(aOut);
    pumpFileChannels(in.getChannel(),
        out.getChannel());
    in.close();
    out.close();
    aOut.setLastModified(aIn.lastModified());
  }
  private void pumpFileChannels(final FileChannel inc, final FileChannel outc) throws IOException {
    final ByteBuffer buffer = ByteBuffer.allocate( iBufferSize );
    while (true) {
      final int ret = inc.read( buffer );
      if (ret==-1) {
        break;
      }
      buffer.flip();
      outc.write( buffer );
      buffer.clear(); // Make room for the next read
    }
    inc.close();
    outc.close();
  }

  /**
   * Kopíruje nejefektivnějším způsobem zadaný soubor do výstupního proudu.
   * Výstupní proud nebude uzavřen.
   * @param aIn
   * @param outstm
   * @throws IOException
   */
  public void copyFileToOutputStream(final File aIn, final OutputStream outstm) throws IOException {
    final FileInputStream in = new FileInputStream(aIn);
    try {
      pumpFileChannelToOutputStream(in.getChannel(), outstm);
    } finally {
      in.close();
    }
  }


  private void pumpFileChannelToOutputStream(final FileChannel inc, final OutputStream outstm) throws IOException {
    final ByteBuffer buffer = ByteBuffer.allocate( iBufferSize );
    while (true) {
      final int ret = inc.read( buffer );
      if (ret==-1) {
        break;
      }
      buffer.flip();
      outstm.write( buffer.array(), buffer.position(), buffer.remaining() );
      buffer.clear(); // Make room for the next read
    }
    inc.close();
  }


  /** Načte řádky z file zadaného jména.
   *  <li>Při načítání jsou vynechány prázdné řádky a řádky obsahující komentář (tj. první nemezerový znak je '#' nebo ';'.
   *  <li>Z načtených dat jsou odstraněny okrajové mezery.
   *  <li>Řádky jsou vráceny v pořadí, jak byly načteny.
   *      Tj. pořadí v rámci načítaných souborů (resources) je zachováno,
   *      ale uvědomte si, že pořadí resources je dáno použitým class loaderem, tj. pro praktické použití je nepředvídatelné.
   *
   * @param aClassLoader        Class loader použitý pro hledání resource.
   * @param aResourceName       Jméno resource.
   * @param aLinePattern        RE, kterému musejí řádky vyhovovat. null => nekontrolovat.
   * @return
   */
  public static List<String> readLines(final URL aURL, final String aLinePattern) {
    if (aURL == null){
      return null;
    }

    InputStream is = null;
    try {

      // nejprve se pokusí URL otevřít jako File (proč, nevíme - je to původní implementace, která zde byla, tak ji zatím necháváme)
      final File file = FileUtils.toFile(aURL);
      if (file != null) {
        try {
          is = new FileInputStream(file);
        } catch (final IOException e) {
          throw new RuntimeException("Nenalezen soubor "+file.getName(),e);
        }
      } else { // pokud se nepovede URL otevřít jako File, otevřeme ji jako InputStream
        try {
          is = aURL.openStream();
        } catch (final IOException e) {
          throw new RuntimeException("Nepodařilo se otevřít InputStream z URL: "+ aURL,e);
        }
      }

      final Pattern match = aLinePattern == null ? null : Pattern.compile(aLinePattern);
      final Map<String, List<String>> invalid = aLinePattern == null ? null : new TreeMap<String, List<String>>();
      //int invalidLinesCount = 0;

      final List<String> result = new ArrayList<String>();
      List<String> list;
      list = readLines(is, true);
      for (String line : list) {
        line = line.trim();
        if (StringUtils.isBlank(line)) {
          // prázdné řádky
          continue;
        }
        if (line.startsWith(";") || line.startsWith("#")) {
          // komentáře
          continue;
        }
        if (match != null && !match.matcher(line).matches()) {
          List<String> invalidLines = invalid.get(file.getName());
          if (invalidLines == null) {
            invalidLines = new ArrayList<String>();
            invalid.put(file.getName(), invalidLines);
          }
          //invalidLinesCount++;
          invalidLines.add(line);
          continue;
        }
        result.add(line);
      }
      return result;
    } finally {
      try {
        if (is != null) {
          is.close();
        }
      } catch (final IOException e) {
        // Když nejde zavřít, tak fakt nevíme
      }
    }
  }

  private static List<String> readLines(final InputStream aInputStream, final boolean aCloseInputStream) {
    try {
      try {
        final List<String> result = IOUtils.readLines(aInputStream);
        return result;
      } finally {
        if (aCloseInputStream) {
          aInputStream.close();
        }
      }
    } catch (final IOException e) {
      throw new RuntimeException(e);
    }
  }


  public static List<String> readLines(final File aFile) {
    try {
      final List<String> lines = readLines(new FileInputStream(aFile), true);
      return lines;
    } catch (final FileNotFoundException e) {
      throw new RuntimeException("cannot read file " + aFile, e);
    }
  }

  /**
   * Přečte řádky ze souboru s tím, že odfiltruje prázdné řádky
   * a komentáře (první nemezerový znak je ";" nebo "#". tyto řádky
   * nahradí hodnotou null, klient tedy už testuje jen na nullu.
   * Je to proto, aby klient mohl jednoduše číslovat řádky při vypisování chyb.
   * @param aFile
   * @return
   */
  public static List<String> readLinesFilteringToNull(final File aFile) {
    final List<String> lines = readLines(aFile);
    for (final ListIterator<String> li = lines.listIterator(); li.hasNext(); ) {
      String line = li.next();
      line = line.trim();
      if (jeToNezajimavyRadek(line)) {
        line = null;
      }
      li.set(line);
    }
    return lines;
  }

  /**
   * @param aLine
   * @return
   */
  private static boolean jeToNezajimavyRadek(final String aLine) {
    if (StringUtils.isBlank(aLine)) {
      return true;
    }
    if (aLine.startsWith(";") || aLine.startsWith("#")) {
      return true;
    }
    return false;
  }


  public static void main(final String[] args) throws IOException {

    /*
    final FileManager fm = FileManager.getInstance(MAX_BUFFER_SIZE);

    final File f = new File("c:/.repos/csob/cibisskob/CbSkob/CAdm/MMainOnce/Once/src/2009F/3settings/A22661_RN00152106_03-1_clientos-login-check.isql");
    final String content = fm.readWholeFileAsString(f);
    System.out.println(content);

    final List<String> lines = readLines(f);
    System.out.println(lines);

    final BufferedReader br = new BufferedReader(new InputStreamReader(getInputStreamToSkipUtfBom(f)));
    while (true) {

      final String row = br.readLine();
      if (row == null) {break;}
      System.out.println(row);
    }
    br.close();
     */

    /*
    final FileManager fm = FileManager.getInstance(MAX_BUFFER_SIZE);

    final File f = new File("c:/.repos/csob/cibisskob/CbSkob/CAdm/MMainOnce/Once/src/2009F/3settings/A22661_RN00152106_03-1_clientos-login-check.isql");
    final String content = fm.readWholeFileAsString(f);
    System.out.println(content);

    final List<String> lines = readLines(f);
    System.out.println(lines);

    final BufferedReader br = new BufferedReader(new InputStreamReader(getInputStreamToSkipUtfBom(f)));
    while (true) {
    while (true) {

      final String row = br.readLine();
      if (row == null) {break;}
      System.out.println(row);
    }
    br.close();
     */

    final File f = new File("c:/.repos/csob/cibisskob/CbSkob/AMain/BCredit/MCredit/IfxSpl/src/prc/Adjust/CONSCHECK/CONS_CHECK_CRED_02.isql");
    final BufferedReader br = getReaderToSkipUtfBom(f, "UTF-8");
    while (true) {

      final String row = br.readLine();
      if (row == null) {break;}
      System.out.println(row);
    }
    br.close();
  }

}