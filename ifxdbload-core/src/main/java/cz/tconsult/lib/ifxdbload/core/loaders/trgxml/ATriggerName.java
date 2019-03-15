package cz.tconsult.lib.ifxdbload.core.loaders.trgxml;

public class ATriggerName {

  String value;

  public ATriggerName(final String value) {
    this.value = value;
  }

  public static ATriggerName from(final String value) {
    return value == null ? null : new ATriggerName(value);
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
    return value.equals(((ATriggerName)obj).toString());
  }

  @Override
  public int hashCode()
  {
    return value.hashCode();
  }

}
