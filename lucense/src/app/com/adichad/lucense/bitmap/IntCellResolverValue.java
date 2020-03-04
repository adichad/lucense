package com.adichad.lucense.bitmap;

public class IntCellResolverValue implements CellResolver {

  int value = -1;
  private IntInputCellDictionaryHandler cellDict = null;
  
  public IntCellResolverValue(int cell,IntInputCellDictionaryHandler intInputCellDictionaryHandler) {
    this.value = cell;
    this.cellDict = intInputCellDictionaryHandler ; 
  }

  @Override
  public int get(int i) {
    return cellDict.resolveCell(this.value);
  }

  @Override
  public int getSize() {
    return 1;
  }

  @Override
  public String getOriginal(int i) {
    // TODO Auto-generated method stub
    return Integer.toString(value);

    // return null;
  }

}
