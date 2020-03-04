package com.adichad.lucense.bitmap;

public class CellDictionaryFactory {

  public static CellDictionaryHandler getInstance(String dictName) {
    if (dictName.equals("int-dict-bypass"))
      return new IntInputByPassingCellDictionaryHandler();
    else if (dictName.equals("string-dict-bypass"))
      return new StringInputByPassingCellDictionaryHandler();
    else
      return null;
  }
}
