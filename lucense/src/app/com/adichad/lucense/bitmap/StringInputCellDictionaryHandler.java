package com.adichad.lucense.bitmap;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

public abstract class StringInputCellDictionaryHandler extends CellDictionaryHandler {

  @Override
  public final CellResolver readArrayFrom(DataInputStream dis) throws IOException {
    int count = dis.readInt();
    String cells[] = new String[count];
    for (int i = 0; i < count; i++) {
      cells[i] = readString(dis);
    }
    return new StringCellResolverArray(cells, this);
  }

  public final CellResolver readValueFrom(DataInputStream dis) throws IOException {
    String cell = readString(dis);
    return new StringCellResolverValue(cell, this);
  }

  public final CellResolver readArrayFrom(IndexReader indexReader, String luceneField) throws IOException {

    return new StringCellResolverArray(FieldCache.DEFAULT.getStrings(indexReader, luceneField), this);
  }



  final public String readString(DataInputStream dis) throws IOException {
    int len = dis.readInt();
    byte[] b = new byte[len];
    dis.readFully(b, 0, len);

    return new String(b);
  }

  @Override
  public abstract int resolveCell(String cell);

  @Override
  public final int resolveCell(int cell) {
    return -1;
  }
}
