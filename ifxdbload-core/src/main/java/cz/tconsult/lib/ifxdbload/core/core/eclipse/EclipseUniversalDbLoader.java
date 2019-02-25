/**
 *
 */
package cz.tconsult.lib.ifxdbload.core.core.eclipse;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cz.tconsult.dbloader.itf.EFileCategory;
import cz.tconsult.dbloader.itf.eclipse.IEclipseUniversalDbLoader;
import cz.tconsult.lib.ifxdbload.core.core.FFaze;
import cz.tconsult.lib.ifxdbload.core.core.FazeAnalyzeResult;
import cz.tconsult.lib.ifxdbload.core.core.UniversalDbLoader;
import cz.tconsult.lib.ifxdbload.core.core.UniversalDbLoaderParams;
import cz.tconsult.lib.ifxdbload.core.core.UniversalDbLoaderResult;
import cz.tconsult.lib.ifxdbload.core.core.Zavadenec;

/**
 * @author veverka
 *
 */
public class EclipseUniversalDbLoader  extends UniversalDbLoader  implements IEclipseUniversalDbLoader {

  /**
   * @param aConn
   */
  EclipseUniversalDbLoader(final UniversalDbLoaderParams aUniversalDbLoaderParams) {
    super(aUniversalDbLoaderParams);
  }

  private static Map<String, Object> _toMap(final UniversalDbLoaderResult aResult) {

    final Map<String, Object> m = new HashMap<String, Object>();
    m.put("result", aResult.getMessages());
    final Map<String, Object> result = Collections.unmodifiableMap(m);
    return result;
  }

  /* (non-Javadoc)
   * @see cz.tconsult.tw.oncescript.IEclipseUniversalLoader#loadByEclipse(java.io.File, java.lang.String, byte[], java.nio.charset.Charset)
   */
  @Override
  public Map<String, Object> loadByEclipse(final File aProjectRoot, final String aRelativePath, final byte[] aData, final Charset aCharset) throws SQLException, IOException {
    final Zavadenec zavadenec = new Zavadenec(aProjectRoot, aRelativePath, aData, aCharset);
    final UniversalDbLoaderResult r = load(zavadenec);

    final Map<String, Object> result = _toMap(r);
    return result;
  }

  /* (non-Javadoc)
   * @see cz.tconsult.tw.oncescript.IEclipseUniversalDbLoader#checkByEclipse(java.io.File, java.lang.String, byte[], java.nio.charset.Charset)
   */
  @Override
  public Map<String, Object> checkByEclipse(final File aProjectRoot, final String aRelativePath, final byte[] aData, final Charset aCharset) throws SQLException, IOException {

    final Zavadenec zavadenec = new Zavadenec(aProjectRoot, aRelativePath, aData, aCharset);
    final UniversalDbLoaderResult r = super.checkByEclipse(zavadenec);
    final Map<String, Object> result = _toMap(r);
    return result;
  }

  /* (non-Javadoc)
   * @see cz.tconsult.tw.oncescript.IEclipseUniversalDbLoader#getFileCategory(java.io.File, java.lang.String)
   */
  @Override
  public EFileCategory getFileCategory(final File aProjectRoot, final String aRelativePath) {
    final FazeAnalyzeResult result = FFaze.analyzeEntryName(aRelativePath);
    if (result == null) {
      return null;
    }
    return result.getFileCategory();
  }

}
