package cz.tconsult.lib.ifxdbload.core.tw;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class AScalar0 implements Comparable <AScalar0> {
  private final String value;

  public AScalar0(final String value) {
    this.value = value;
    if (value == null) {
      throw new NullPointerException("Nelze vytvaret skalární typy s hodnotou null uvnitř, nutno použít null jako celek nebo Optiona: " + getClass().getName());
    }
  }

  @Override
  public String toString() {
    return value;
  }

  @Override
  public int compareTo(final AScalar0 aObject) {
    if (aObject == null) {
      throw new NullPointerException("Cannot compare with null");
    }
    return value.compareTo(aObject.value);
  }

  public static String toString(final AScalar0 scalar) {
    return scalar == null ? null : scalar.toString();
  }

  protected String value() {
    return value;
  }

}
