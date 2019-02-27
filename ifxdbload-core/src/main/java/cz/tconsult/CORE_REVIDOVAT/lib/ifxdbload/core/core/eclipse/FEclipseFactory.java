/**
 *
 */
package cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.core.eclipse;

import java.sql.SQLException;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;

import cz.tconsult.CORE_REVIDOVAT.lib.ifxdbload.core.core.UniversalDbLoaderParams;
import cz.tconsult.dbloader.itf.eclipse.IEclipseUniversalDbLoader;

/**
 * @author veverka
 *
 */
public class FEclipseFactory {


  public static final IEclipseUniversalDbLoader instantiate(final Map<String, Object> aParams) throws SQLException {

    // FIXME předělat přebírání parametrů na expilictní
    final UniversalDbLoaderParams universalDbLoaderParams = new UniversalDbLoaderParams();
    for (final Map.Entry<String, Object> entry : aParams.entrySet()) {

      String name = entry.getKey();
      if (StringUtils.isBlank(name)) {continue;}

      name = name.trim();
      try {
        PropertyUtils.setProperty(universalDbLoaderParams, name, entry.getValue());
      } catch (final Exception e) {
        throw new RuntimeException(e);
      }
    }

    final EclipseUniversalDbLoader universalDbLaoder = new EclipseUniversalDbLoader(universalDbLoaderParams);
    return  universalDbLaoder;

  }
}
