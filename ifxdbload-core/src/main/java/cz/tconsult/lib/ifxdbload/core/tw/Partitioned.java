package cz.tconsult.lib.ifxdbload.core.tw;

import java.util.List;

import lombok.Data;

@Data
public class Partitioned<T> {

  private final List<T> ano;
  private final List<T> ne;
}
