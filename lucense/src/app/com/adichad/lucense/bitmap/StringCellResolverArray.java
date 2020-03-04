package com.adichad.lucense.bitmap;

public class StringCellResolverArray implements CellResolver {

  private StringInputCellDictionaryHandler cellDict = null;

  private String cells[] = null;

  public StringCellResolverArray(String[] cells, StringInputCellDictionaryHandler cellDict) {
    this.cells = cells;
    this.cellDict = cellDict;
  }

  @Override
  public int get(int i) {
    return cellDict.resolveCell(cells[i]);
  }

  @Override
  public int getSize() {
    // TODO Auto-generated method stub
    return this.cells.length;

  }

  @Override
  public String getOriginal(int i) {
    // TODO Auto-generated method stub
    return cells[i];
  }

}
