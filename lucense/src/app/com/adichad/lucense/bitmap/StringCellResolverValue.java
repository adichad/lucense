package com.adichad.lucense.bitmap;

public class StringCellResolverValue implements CellResolver {

  private StringInputCellDictionaryHandler cellDict = null;

  private String cell = null;

  public StringCellResolverValue(String cell, StringInputCellDictionaryHandler cellDict) {
    this.cell = cell;
    this.cellDict = cellDict;
  }

  @Override
  public int get(int i) {
    return cellDict.resolveCell(cell);
  }

  @Override
  public int getSize() {
    // TODO Auto-generated method stub
    return 1;

  }

  @Override
  public String getOriginal(int i) {
    // TODO Auto-generated method stub
    return cell;
  }

}
