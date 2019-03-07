package cz.tconsult.lib.ifxdbload.core.tw;

import lombok.Data;

@Data
public class RowCol {

  private final int row;
  private final int col;

  public RowCol withRow(final int row) {
    return new RowCol(row, col);
  }

  public RowCol withCol(final int col) {
    return new RowCol(row, col);
  }

  public RowCol addRow(final int rowDelta) {
    return withRow(row + rowDelta);
  }

  public RowCol addCol(final int colDelta) {
    return withCol(col + colDelta);
  }


}
