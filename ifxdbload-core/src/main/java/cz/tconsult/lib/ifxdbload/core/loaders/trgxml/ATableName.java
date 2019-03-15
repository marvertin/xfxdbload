package cz.tconsult.lib.ifxdbload.core.loaders.trgxml;

public class ATableName {

  String tableName;

  public ATableName(final String tableName) {
    this.tableName = tableName;
  }

  public static ATableName from(final String tableName) {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 8:56:47]
    return tableName == null ? null : new ATableName(tableName);
  }

  public String getPureTableName() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 9:53:23]
    return tableName;
  }

  public String getSchemaForSql() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 9:53:42]
    return null;
  }

  public String getSchemaWithDot() {
    //TODO [veverka] implementuj - vygenerovana metoda [veverka 9:53:51]
    return null;
  }

  public ATableName jmenoArchivniTabulky() {
    // Jméno archivní tabulky není zadáno - musíme je proto vytvořit.
    // Jméno archivní tabulky se vytvoří z původního jména nahrazením přípony _mat příponou _arch.
    // Záměrně není použit String.replace("_mat", "_arch"), protože pokud by v názvu nebyla přípona _mat,
    // zůstal by název beze změny a došlo by k vytvoření triggeru, který by kopíroval upravovaný
    // řádek tabulky do té stejné tabulky
    // takhle to v daném případě spadne
    String archname;
    final int intSuffixIndex = tableName.lastIndexOf("_mat");
    if (intSuffixIndex == -1) {
      archname = tableName + "_arch";
    } else {
      archname = tableName.substring(0, intSuffixIndex) + "_arch";
    }
    return ATableName.from(archname);
  }

  @Override
  public String toString() {
    return tableName;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return  false;
    }
    return getPureTableName().equals(((ATableName)obj).getPureTableName());
  }

  @Override
  public int hashCode()
  {
    return tableName.hashCode();
  }


}
