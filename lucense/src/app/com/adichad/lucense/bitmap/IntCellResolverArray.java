package com.adichad.lucense.bitmap;

public class IntCellResolverArray implements CellResolver {

  private int cells[] = null;

  private IntInputCellDictionaryHandler cellDict = null;

  public IntCellResolverArray(int[] cells, IntInputCellDictionaryHandler intInputCellDictionaryHandler) {
    this.cells = cells;
    this.cellDict = intInputCellDictionaryHandler;
  }

  public int getSize() {
    return this.cells.length;
  }

  public int get(int i) {
    return cellDict.resolveCell(this.cells[i]);
  }

  @Override
  public String getOriginal(int i) {
    // TODO Auto-generated method stub
    return Integer.toString(cells[i]);
  }
}
