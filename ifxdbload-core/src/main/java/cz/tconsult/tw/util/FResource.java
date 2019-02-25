package cz.tconsult.tw.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.configuration.Configuration;


/**
 * Práce s resourcy.
 * @author veverka
 *
 */
public class FResource {

  private static final ResourceReader resourceReader = new ResourceReader();

  public static Configuration getResourceAsConfiguration(final ClassLoader aClassLoader, final String aName) {
    return resourceReader.getResourceAsConfiguration(aClassLoader, aName);
  }

  public static InputStream getResourceAsStream(final ClassLoader aClassLoader, final String aName) {
    return resourceReader.getResourceAsStream(aClassLoader, aName);
  }

  public static InputStream getResourceAsStream(final Class<?> aClass, final String aName) {
    return resourceReader.getResourceAsStream(aClass, aName);
  }

  public static InputStream seekResourceAsStream(final ClassLoader aClassLoader, final String aName) {
    return resourceReader.seekResourceAsStream(aClassLoader, aName);
  }

  public static InputStream seekResourceAsStream(final Class<?> aClass, final String aName) {
    return resourceReader.seekResourceAsStream(aClass, aName);
  }

  public static InputStream openUrl(final URL aUrl) {
    return resourceReader.openUrl(aUrl);
  }

  public static long getLastChangeForDevelopment(final URL aUrl) {
    return resourceReader.getLastChangeForDevelopment(aUrl);
  }

  public static Set<URL> getResources(final ClassLoader aCl, final String... aNames) {
    return resourceReader.getResources(aCl, aNames);
  }

  public static URL getExactlyOneResourceUrl(final ClassLoader aCl, final String... aNames) throws IOException {
    return resourceReader.getExactlyOneResourceUrl(aCl, aNames);
  }


  public static InputStream getExactlyOneResourceAsStream(final ClassLoader aCl, final String... aNames) {
    return resourceReader.getExactlyOneResourceAsStream(aCl, aNames);
  }

  public static byte[] readExactlyOneResourceAsBytes(final ClassLoader aCl, final String... aNames) {
    return resourceReader.readExactlyOneResourceAsBytes(aCl, aNames);
  }

  public static Properties loadExactlyOnePropertyResource(final ClassLoader aClassLoader, final String... aNames) throws IOException {
    return resourceReader.loadExactlyOnePropertyResource(aClassLoader, aNames);
  }

  public static boolean existsResource(final ClassLoader aClassLoader, final String aName) {
    return resourceReader.existsResource(aClassLoader, aName);
  }

  public static Set<String> readResourcesLinesWithoutComments(final ClassLoader aClassLoader, final String aName) {
    return resourceReader.readResourcesLinesWithoutComments(aClassLoader, aName);
  }

  public static Properties readMergedProperties(final ClassLoader aClassLoader, final String aResourceName, final int aMinOccurs, final int aMaxOccurs) {
    return resourceReader.readMergedProperties(aClassLoader, aResourceName, aMinOccurs, aMaxOccurs);
  }

  public static List<String> readLines(final ClassLoader aClassLoader, final String aResourceName, final int aMinOccurs, final int aMaxOccurs, final String aLinePattern) {
    return resourceReader.readLines(aClassLoader, aResourceName, aMinOccurs, aMaxOccurs, aLinePattern);
  }

  public static File tryToFind3rdPartyLibsDir() {
    return resourceReader.tryToFind3rdPartyLibsDir();
  }

}
