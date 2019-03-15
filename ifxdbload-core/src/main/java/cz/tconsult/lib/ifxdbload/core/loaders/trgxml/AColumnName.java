package cz.tconsult.lib.ifxdbload.core.loaders.trgxml;


public class AColumnName   {

  String value;

  public AColumnName(final String value) {
    this.value = value;
  }

  public static AColumnName from(final String value) {
    return value == null ? null : new AColumnName(value);
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public boolean equals(final Object obj) {
    if (obj == null) {
      return  false;
    }
    return value.equals(((AColumnName)obj).toString());
  }

  @Override
  public int hashCode()
  {
    return value.hashCode();
  }


}
