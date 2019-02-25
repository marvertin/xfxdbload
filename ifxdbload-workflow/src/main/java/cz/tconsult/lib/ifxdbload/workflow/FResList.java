/**
 *
 */
package cz.tconsult.lib.ifxdbload.workflow;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import cz.tconsult.tw.util.FResource;


/**
 * @author veverka
 *
 */
public class FResList {

  private static final List<String> entryNames = new ArrayList<String>();

  static {
    entryNames.add("akce/f020clean/executeClean.osql");
    entryNames.add("akce/f260afterobj/publicSynonymAndSerialEmulation.osql");
    entryNames.add("akce/f700finish/finish.osql");
  }

  public static Iterable<Entry> getEntries() {
    final ClassLoader cl = Thread.currentThread().getContextClassLoader();

    return new Iterable<Entry>() {

      private final Iterator<String> it = entryNames.iterator();
      private InputStream lastOpenedStream;

      @Override
      public Iterator<Entry> iterator() {
        return new Iterator<FResList.Entry>() {

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

          @Override
          public Entry next() {
            if (lastOpenedStream != null) {
              try {
                lastOpenedStream.close();
                lastOpenedStream = null;
              } catch (final IOException e) { // vyjimka zavirani neni zajimava
              }
            }
            final String entryName = it.next();
            final Entry entry = new Entry();
            entry.entryName = entryName;
            entry.istm = FResource.getExactlyOneResourceAsStream(cl, entryName);
            lastOpenedStream = entry.istm;
            return entry;
          }

          @Override
          public boolean hasNext() {
            return it.hasNext();
          }
        };
      }
    };
  }

  public static class Entry {
    public InputStream istm;
    public String entryName;
  }


}
