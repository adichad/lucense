package com.adichad.lucense.bitmap;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

public abstract class IntInputCellDictionaryHandler extends CellDictionaryHandler {

  @Override
  public final CellResolver readArrayFrom(DataInputStream dis) throws IOException {
    int count = dis.readInt();
    int cells[] = new int[count];
    for (int i = 0; i < count; i++) {
      cells[i] = dis.readInt();
    }
    return new IntCellResolverArray(cells, this);
  }

  public final CellResolver readValueFrom(DataInputStream dis) throws IOException {
    int cellId = dis.readInt();
    return new IntCellResolverValue(cellId, this);
  }

  public final CellResolver readArrayFrom(IndexReader indexReader, String luceneField) throws IOException {
    return new IntCellResolverArray(FieldCache.DEFAULT.getInts(indexReader, luceneField), this);
  }
  
  @Override
  public final int resolveCell(String cell) {
    return -1;
  }

  @Override
  abstract public int resolveCell(int cell);

}
