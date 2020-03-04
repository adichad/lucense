package com.adichad.lucense.bitmap;

import java.io.DataInputStream;
import java.io.IOException;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldCache;

public abstract class CellDictionaryHandler {

  public abstract int resolveCell(String cell);

  public abstract int resolveCell(int cell);

  public abstract CellResolver readArrayFrom(DataInputStream dis) throws IOException;

  public abstract CellResolver readArrayFrom(IndexReader indexReader, String luceneField) throws IOException;

  public abstract CellResolver readValueFrom(DataInputStream dis) throws IOException;
}
