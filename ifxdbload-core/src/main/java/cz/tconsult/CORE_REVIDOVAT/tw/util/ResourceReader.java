package cz.tconsult.CORE_REVIDOVAT.tw.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.FileConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import cz.tconsult.CORE_REVIDOVAT.tw.util.DeveloperSettingBase;
import cz.tconsult.CORE_REVIDOVAT.tw.util.FileManager;
import cz.tconsult.CORE_REVIDOVAT.tw.util.LoggingFileChangedReloadingStrategy;

/**
 * Práce s resourcy.
 * @author veverka
 *
 */
public class ResourceReader {

  private static final Logger log = LoggerFactory.getLogger(ResourceReader.class);

  private final String encoding;

  public ResourceReader() {
    this.encoding = null;
  }

  public ResourceReader(final String encoding) {
    this.encoding = encoding;
  }

  /**
   * Otevře zadaný resource jako konfiguraci.
   * @param aClassLoader
   * @param aName
   * @return
   */
  public Configuration getResourceAsConfiguration(final ClassLoader aClassLoader, final String aName) {
    try {
      final URL url = aClassLoader.getResource(aName);
      FileConfiguration fileConfiguration;
      fileConfiguration = new PropertiesConfiguration(url);
      fileConfiguration.setReloadingStrategy(new LoggingFileChangedReloadingStrategy());
      return fileConfiguration;
    } catch (final ConfigurationException e) {
      throw new RuntimeException("Cannot open resource '" + aName + "' as configuration", e);
    }

  }

  /**
   * Za normálních okolností, pokud volám classloader.getResourceAsStream("...")
   * java nějak kešuje obsah čteného resourcu i když je v souboru.  To znemožňuje
   * "hot deploy" resourců. Aby se nová verze resourců projevila, musí se apliakce restartovat.
   * Toto nastavení způsobí, že se veškeré resourcy získávané přes FResource.getResourceAsStream
   * otvírají pokaždé znovu, takže mají čerstvý obsah, nyní je to podporováno pouze pro resourcy v souborech.
   * Proto doporučuji používat pro otvírání resourců vždy FResource.getResourceAsStream.
   * @return input stream, což někdy může být obalený fileInputstream.
   */
  public InputStream getResourceAsStream(final ClassLoader aClassLoader, final String aName) {
    final URL url = aClassLoader.getResource(aName);
    if (url == null) {
      return null;
    }
    return openUrl(url);

  }

  /**
   * Za normálních okolností, pokud volám classloader.getResourceAsStream("...")
   * java nějak kešuje obsah čteného resourcu i když je v souboru.  To znemožňuje
   * "hot deploy" resourců. Aby se nová verze resourců projevila, musí se apliakce restartovat.
   * Toto nastavení způsobí, že se veškeré resourcy získávané přes FResource.getResourceAsStream
   * otvírají pokaždé znovu, takže mají čerstvý obsah, nyní je to podporováno pouze pro resourcy v souborech.
   * Proto doporučuji používat pro otvírání resourců vždy FResource.getResourceAsStream.
   * @return input stream, což někdy může být obalený fileInputstream.
   */
  public InputStream getResourceAsStream(final Class<?> aClass, final String aName) {
    final URL url = aClass.getResource(aName);
    if (url == null) {
      return null;
    }
    return openUrl(url);
  }

  /**
   * Za normálních okolností, pokud volám classloader.getResourceAsStream("...")
   * java nějak kešuje obsah čteného resourcu i když je v souboru.  To znemožňuje
   * "hot deploy" resourců. Aby se nová verze resourců projevila, musí se apliakce restartovat.
   * Toto nastavení způsobí, že se veškeré resourcy získávané přes FResource.getResourceAsStream
   * otvírají pokaždé znovu, takže mají čerstvý obsah, nyní je to podporováno pouze pro resourcy v souborech.
   * Proto doporučuji používat pro otvírání resourců vždy FResource.getResourceAsStream.
   * A spúadne na výjimku, pokud se to nepovede
   * @return input stream, což někdy může být obalený fileInputstream.
   */
  public InputStream seekResourceAsStream(final ClassLoader aClassLoader, final String aName) {
    final InputStream istm = getResourceAsStream(aClassLoader, aName);
    if (istm == null) {
      throw new NullPointerException("Method  has returned null: getResource(\"" + aName + "\") on classloader " + aClassLoader);
    }
    return istm;
  }

  /**
   * Za normálních okolností, pokud volám classloader.getResourceAsStream("...")
   * java nějak kešuje obsah čteného resourcu i když je v souboru.  To znemožňuje
   * "hot deploy" resourců. Aby se nová verze resourců projevila, musí se apliakce restartovat.
   * Toto nastavení způsobí, že se veškeré resourcy získávané přes FResource.getResourceAsStream
   * otvírají pokaždé znovu, takže mají čerstvý obsah, nyní je to podporováno pouze pro resourcy v souborech.
   * Proto doporučuji používat pro otvírání resourců vždy FResource.getResourceAsStream.
   * A spúadne na výjimku, pokud se to nepovede
   * @return input stream, což někdy může být obalený fileInputstream.
   */
  public InputStream seekResourceAsStream(final Class<?> aClass, final String aName) {
    final InputStream istm = getResourceAsStream(aClass, aName);
    if (istm == null) {
      throw new NullPointerException("Method  has returned null: getResource(\"" + aName + "\") on class " + aClass);
    }
    return istm;
  }


  /**
   * Otevře dané URL,pokud je nastaveno notCacheResources a resource je fileresource, neotvírá přímo
   * u ZRL, protože v případně resourců bývá kešováno, ale vytvořín FileInputStream přímo nad souborem.
   * @param url
   * @return
   */
  //@SuppressWarnings("resource") // Smyslem je otvírat resource, takže nemůže být zavírán
  public InputStream openUrl(final URL url) {
    try {
      final File file = FileUtils.toFile(url);
      InputStream istm;
      if (DeveloperSettingBase.cfg.isNotCacheResources() && file != null && file.canRead()) {
        log.debug("Opening resource \"" + url + "\" as file \"" + file + "\"");
        istm = new BufferedInputStream(new FileInputStream(file));
      } else {
        istm = url.openStream();
      }
      return FileManager.getInputStreamToSkipUtfBom(istm);
    } catch (final IOException e) {
      throw new RuntimeException("Cannot open resource \"" + url + "\"", e);
    }
  }

  /**
   * Metoda vrátí ve vývojářském režimu datum poslední aktualizace
   * spouboru, na něž ukazuje URL. Pokud URL není souborové vrací nulu.
   * Pokud nejsme ve vývojářském režimu, vracíme nulu bez jakýchkoli testů.
   *
   * Použití:
   * <pre>
   *   long modif = FResource.getLastChangeForDevelopment(url);
   *     if (modif > iLastModification) {
   *       xxxxx = null;  //Smazání keše
   *     iLastModification = modif;
   *  }
   *  if (xxxx == null) {
   *     natažení xxxx pomcí FResource.openUrl(url)
   *  }
   *  </pre>
   *
   * @param url
   * @return
   */
  public long getLastChangeForDevelopment(final URL url) {
    if (url == null) {
      return 0;
    }
    // Pokud nejsme ve vývojářském módu, vracíme nulu a nic neprověřujeme
    if (! DeveloperSettingBase.cfg.isNotCacheResources()) {
      return 0;
    }
    final File file = FileUtils.toFile(url);
    if (file != null && file.canRead()) {
      log.debug("Inquring last file change\"" + url + "\" as file \"" + file + "\"");
      final long result = file.lastModified();
      return result;
    }
    // není to soubor, tak vracíme nulu
    return 0;
  }

  /**
   * Vrátí seznam URL na všechny zadané resourcy ve všech možných jarech.
   * Je sařazen abecedně, ale to jen kvůli dtabilitě chování. Nikdo by se na to neměl spoléhat.
   * @param cl
   * @param aNames
   * @return
   * @throws IOException
   */
  public Set<URL> getResources(final ClassLoader cl, final String ... aNames) {
    try {
      final SortedSet<URL> result = new TreeSet<URL>(new Comparator<URL>() {
        @Override
        public int compare(final URL url1, final URL url2) {
          return url1.toExternalForm().compareTo(url2.toExternalForm());
        }
      });
      for (final String resourceName : aNames) {
        for (final Enumeration<URL> e = cl.getResources(resourceName); e.hasMoreElements(); ) {
          result.add(e.nextElement());
        }
      }
      return result;
    } catch (final Exception e) {
      throw new RuntimeException("Error reading resource " + Arrays.asList(aNames), e);
    }
  }

  /** Vrátí URL na resource odpovídající jednomu ze zadaných jmen.
   *
   * @param cl          ClassLoader, který se použíje k hledání resourců.
   * @param aNames      Seznam jmen, které může resource mít. Očekáváme, že existuje od právě jedním z nich.
   * @return            URL na nalezené resource.
   * @throws IOException
   * @throws RuntimeException - Pokud je nalezen jiný počet, než přesně 1, ze zadaných resourců.
   */
  public URL getExactlyOneResourceUrl(final ClassLoader cl, final String ... aNames) throws IOException {
    final Collection<URL> urls = getResources(cl, aNames);
    if (urls.size() != 1) {
      final String msg = String.format("Expected exactly one resource, but %d of %d found: %s of %s"
          , urls.size(), aNames.length
          , urls, Arrays.asList(aNames)
          );
      throw new RuntimeException(msg);
    }
    // Víme, že je tam jen jeden.
    final URL url = urls.iterator().next();
    return url;
  }

  /** Vrátí URL na resource odpovídající jednomu ze zadaných jmen.
   *
   * @param cl          ClassLoader, který se použíje k hledání resourců.
   * @param aNames      Seznam jmen, které může resource mít. Očekáváme, že existuje od právě jedním z nich.
   * @return            URL na nalezené resource.
   * @throws IOException
   * @throws RuntimeException - Pokud je nalezen jiný počet, než přesně 1, ze zadaných resourců.
   */
  public InputStream getExactlyOneResourceAsStream(final ClassLoader cl, final String ... aNames)  {
    try {
      final URL url = getExactlyOneResourceUrl(cl, aNames);
      return openUrl(url);
    } catch (final IOException e) {
      throw new RuntimeException("Geting resources " + Arrays.asList(aNames), e);
    }
  }



  /** Načte právě jeden resource daného jména.
   *
   * @param cl          ClassLoader, který se použíje k hledání resourců.
   * @param aNames      Seznam jmen, které může resource mít. Očekáváme, že existuje od právě jedním z nich.
   * @return            URL na nalezené resource.
   * @throws IOException
   * @throws RuntimeException - Pokud je nalezen jiný počet, než přesně 1, ze zadaných resourců.
   */
  public byte[] readExactlyOneResourceAsBytes(final ClassLoader cl, final String ... aNames)  {
    try {
      final InputStream istm = getExactlyOneResourceAsStream(cl, aNames);
      try {
        final ByteArrayOutputStream ostm = new ByteArrayOutputStream();
        final byte b[] = new byte[1024 * 8];
        int len;
        while ((len = istm.read(b)) > 0) {
          ostm.write(b, 0, len);
        }
        return ostm.toByteArray();
      } finally {
        istm.close();
      }
    } catch (final Exception e) {
      throw new RuntimeException("Cannot read resource: " + Arrays.asList(aNames), e);
    }
  }


  /**
   * @throws IOException
   *
   */
  public Properties loadExactlyOnePropertyResource(final ClassLoader aClassLoader, final String ... aNames) throws IOException {
    URL url = null;
    try {
      url =  getExactlyOneResourceUrl(aClassLoader, aNames); // je tam jen jeden
      log.trace("Found property resource: " + url);
      final InputStream is = url.openStream();
      final Properties prop = new Properties();
      prop.load(is);
      log.info("Loaded property resource: " + url);
      return prop;
    } catch (final Exception e) {
      throw new RuntimeException("Cannot read resource " + Arrays.asList(aNames) + " url=" + url, e);
    }

  }

  /**
   * Zjistí zda existuje zadaný resource.
   * @param aClassLoader
   * @param aName
   * @return
   */
  public boolean existsResource(final ClassLoader aClassLoader, final String aName) {
    final URL resource = aClassLoader.getResource(aName);
    return resource != null;
  }
  /**
   * Najde všechny resourcy daného jména, soubory považuje za soubory textové
   * v defaultním kódování, vyhodí komentáře, spojí je a vrátí seznam řádků.
   * @param aClassLoader
   * @param aName
   * @return
   */
  public Set<String> readResourcesLinesWithoutComments(final ClassLoader aClassLoader, final String aName) {
    final List<String> lines = readLines(aClassLoader, aName, 0, -1, null);
    final Set<String> result = new HashSet<String>(lines);
    return result;
  }

  /**
   * Načte z více resourců property a zmerguje je.
   * Je sařazen abecedně, ale to jen kvůli dtabilitě chování. Nikdo by se na to neměl spoléhat.
   *
   * @param aClassLoader
   * @param aResourceName
   * @param aMinOccurs
   * @param aMaxOccurs
   * @return
   */
  public Properties readMergedProperties(final ClassLoader aClassLoader, final String aResourceName, final int aMinOccurs, final int aMaxOccurs) {
    final Properties result = new Properties();

    final List<URL> usedResources = new ArrayList<URL>();
    for (final URL url : getResources(aClassLoader, aResourceName)) {
      usedResources.add(url);
      final InputStream istm = openUrl(url);
      try {
        try {
          result.load(istm);
        } catch (final IOException e) {
          throw new RuntimeException(e);
        }
      } finally {
        try {
          istm.close();
        } catch (final IOException e) { // zavrit se musi
        }
      }
    }
    final int počet = usedResources.size();
    if (aMinOccurs >= 0 && počet < aMinOccurs || aMaxOccurs >= 0 && počet > aMaxOccurs) {
      final String msg = String.format("Found %s [%s] resources, but expected between %d and %s of them: %s"
          , počet == 0 ? "no" : Integer.toString(počet)
              , aResourceName
              , aMinOccurs < 0 ? 0 : aMinOccurs
                  , aMaxOccurs < 0 || aMaxOccurs == Integer.MAX_VALUE ? "any number" : Integer.toString(aMaxOccurs)
                      , usedResources
          );
      throw new RuntimeException(msg);
    }
    return result;
  }

  /** Načte řádky z resource zadaného jména.
   *  <li>Při načítání jsou vynechány prázdné řádky a řádky obsahující komentář (tj. první nemezerový znak je '#' nebo ';'.
   *  <li>Z načtených dat jsou odstraněny okrajové mezery.
   *  <li>Řádky jsou vráceny v pořadí, jak byly načteny.
   *      Tj. pořadí v rámci načítaných souborů (resources) je zachováno,
   *      ale uvědomte si, že pořadí resources je dáno použitým class loaderem, tj. pro praktické použití je nepředvídatelné.
   *
   * @param aClassLoader        Class loader použitý pro hledání resource.
   * @param aResourceName       Jméno resource.
   * @param aMinOccurs          Kolik resourců daného jména musí být minimálně nalezeno. < 0 => nekontrolovat.
   * @param aMaxOccurs          Kolik resourců daného jména smí být maximálně nalezeno.  < 0 => nekontrolovat.
   * @param aLinePattern        RE, kterému musejí řádky vyhovovat. null => nekontrolovat.
   * @return
   */
  public List<String> readLines(final ClassLoader aClassLoader, final String aResourceName, final int aMinOccurs, final int aMaxOccurs, final String aLinePattern) {
    final Pattern match = aLinePattern == null ? null : Pattern.compile(aLinePattern);
    final Map<String, List<String>> invalid = aLinePattern == null ? null : new TreeMap<String, List<String>>();
    int invalidLinesCount = 0;

    final List<String> result = new ArrayList<String>();
    final List<URL> usedResources = new ArrayList<URL>();
    for (final URL url : getResources(aClassLoader, aResourceName)) {
      usedResources.add(url);
      final List<String> list = readLines(openUrl(url), true);
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
          List<String> invalidLines = invalid.get(url.toString());
          if (invalidLines == null) {
            invalidLines = new ArrayList<String>();
            invalid.put(url.toString(), invalidLines);
          }
          invalidLinesCount++;
          invalidLines.add(line);
          continue;
        }
        result.add(line);
      }
    }
    final int počet = usedResources.size();
    if (aMinOccurs >= 0 && počet < aMinOccurs || aMaxOccurs >= 0 && počet > aMaxOccurs) {
      final String msg = String.format("Found %s [%s] resources, but expected between %d and %s of them: %s"
          , počet == 0 ? "no" : Integer.toString(počet)
              , aResourceName
              , aMinOccurs < 0 ? 0 : aMinOccurs
                  , aMaxOccurs < 0 || aMaxOccurs == Integer.MAX_VALUE ? "any number" : Integer.toString(aMaxOccurs)
                      , usedResources
          );
      throw new RuntimeException(msg);
    }
    if (invalidLinesCount > 0) {
      final String msg = String.format("Found %d lines not matching RE %s in [%s] resources: %s"
          , invalidLinesCount
          , aLinePattern
          , aResourceName
          , invalid
          );
      throw new RuntimeException(msg);
    }
    return result;
  }

  private List<String> readLines(final InputStream aInputStream, final boolean aCloseInputStream) {
    try {
      try {
        final List<String> result = encoding == null ? IOUtils.readLines(aInputStream) : IOUtils.readLines(aInputStream, encoding);
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

  /**
   * Pokusí se najít 3rdParty/Libs adresář patřící k aktuálně spuštěnému programu.
   * V TC prostředí by měl být úspěšný na 100%, jinde asi ne.
   * @return Vrací 3rdPartyDir (např: "m:\polakm_*our-system|-2008F-D_S\3rdParty\Libs" pokud adresář doopravdy existuje, jinak vrací null
   * @author polakm
   */
  public File tryToFind3rdPartyLibsDir() {

    final URL url = ResourceReader.class.getResource("FResource.class");
    String path = url.getPath();
    /* v path je bud
     1. pripad pokud spusteno z prostredi
     2. pripad pokud pusteno z -jar
     3. pripad s UNC (počítač se zrovna jmenoval satyr)

    /C:/personal/eclipse3.3.1/W_I18NTools/test/build/classes/cz/tconsult/test/Main.class
    file:/C:/personal/eclipse3.3.1/W_I18NTools/test/build/t.jar!/cz/tconsult/test/Main.class
    file://satyr/.clearcase/snapshots/polakm_*our-system|-2008S-TCTW_mp/Tools/Source/W_I18NTools/test/build/t.jar!/cz/tconsult/test/Main.class

     */

    final String lPath = path.toLowerCase();

    int index = lPath.lastIndexOf("/tools/");
    if (index < 0) {index = lPath.lastIndexOf("/libs/");}
    if (index < 0) {index = lPath.lastIndexOf("/cbtctw/");}
    if (index < 0) {index = lPath.lastIndexOf("/cbrsts/");}
    if (index < 0) {index = lPath.lastIndexOf("/cbrbsz/");}
    if (index < 0) {index = lPath.lastIndexOf("/cbmris/");}
    if (index < 0) {
      return null;
    }
    path = path.substring(0, index);
    if (path.startsWith("file:")) {path = path.substring(5);}

    //trošku heuristika, že... Ale podívej se o něco výše do komentáře. [polakm;2008-08-18 17:46:33]
    if (!path.startsWith("//") && path.startsWith("/")) {path = path.substring(1);}

    path += "/3rdParty/Libs";

    final File result = new File(path);
    return result.exists() ? result : null;
  }
}
