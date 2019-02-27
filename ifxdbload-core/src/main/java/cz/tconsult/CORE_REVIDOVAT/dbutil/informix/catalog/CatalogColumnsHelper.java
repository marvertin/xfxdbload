package cz.tconsult.CORE_REVIDOVAT.dbutil.informix.catalog;

import java.util.HashMap;
import java.util.Map;

public class CatalogColumnsHelper {

  private static final Map<Integer, String> colType2sqlDataType;


  /**
   * Vrátí SQL datového typu sloupce, i s přesnostmi.
   * @param aColType typ sloupce
   * @param aColLength dlžka sloupce
   * @return SQL datového typu sloupce
   */
  public static String getColumnSqlType (final int aColType, final int aColLength) {

    int colType;

    if (aColType >= 256) {

      //+256 znamená, že je "not null"
      colType = aColType - 256;
    }
    else {

      colType = aColType;
    }

    final String mainType = colType2sqlDataType.get(colType);
    if (mainType == null) {

      throw new IllegalArgumentException("Nepodorovaný typ " + aColType + ";" + aColLength);
    }
    String precision;
    switch (colType) {

    case 0: case 13: case 15: case 16: precision = "" + aColLength;break;
    case 5: case 8: precision = aColLength / 256 + "," + aColLength % 256; break;
    default: precision = null;
    }

    final String result = mainType + (precision == null ? "" : "(" + precision + ")");
    return result;
  }

  static {

    colType2sqlDataType = new HashMap<>();
    colType2sqlDataType.put(0, "char");
    colType2sqlDataType.put(1, "smallint");
    colType2sqlDataType.put(2, "integer");
    colType2sqlDataType.put(3, "float");
    colType2sqlDataType.put(4, "smallfloat");
    colType2sqlDataType.put(5, "decimal");
    colType2sqlDataType.put(6, "serial");
    colType2sqlDataType.put(7, "date");
    colType2sqlDataType.put(8, "money");
    colType2sqlDataType.put(9, "null");
    colType2sqlDataType.put(10, "datetime");
    colType2sqlDataType.put(11, "byte");
    colType2sqlDataType.put(12, "text");
    colType2sqlDataType.put(13, "varchar");
    colType2sqlDataType.put(14, "interval");
    colType2sqlDataType.put(15, "nchar");
    colType2sqlDataType.put(16, "nvarchar");
    colType2sqlDataType.put(17, "int8");
    colType2sqlDataType.put(18, "serial8");
    colType2sqlDataType.put(19, "set");
    colType2sqlDataType.put(20, "multiset");
    colType2sqlDataType.put(21, "list");
    colType2sqlDataType.put(22, "row");
    colType2sqlDataType.put(23, "collection");
    colType2sqlDataType.put(24, "rowdef");
    colType2sqlDataType.put(40, "[Variable-length opaque type]{SELECT * FROM sysxtdtypes where extended_id = syscolumns.extended_id}");
    colType2sqlDataType.put(41, "[Fixed-length opaque type]{SELECT * FROM sysxtdtypes where extended_id = syscolumns.extended_id}");
    colType2sqlDataType.put(52, "bigint");
    colType2sqlDataType.put(53, "bigserial");
  }

  public static void main(final String[] aArgs) {

    System.out.println(getColumnSqlType(256, 20));
    System.out.println(getColumnSqlType(5, 4098));
    System.out.println(getColumnSqlType(263, 4));
  }
}

