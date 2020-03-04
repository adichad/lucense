package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

import com.adichad.lucense.bitmap.BitMapOperationStatus;
import com.adichad.lucense.bitmap.CellDictionaryHandler;
import com.adichad.lucense.bitmap.CellResolver;
import com.adichad.lucense.bitmap.Row;
import com.sleepycat.db.DatabaseException;

public class BooleanAuxIndexValueSource implements BooleanValueSource {

  private String luField;

  private CellDictionaryHandler celldict;

  private CellResolver cellResolver;

  private Row row;

  private String name;

  public BooleanAuxIndexValueSource(String name, String luField, Row row, CellDictionaryHandler celldict)
      throws DatabaseException {
    this.luField = luField;
    this.row = row;
    this.celldict = celldict;
    this.name = name;
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.cellResolver = this.celldict.readArrayFrom(reader, this.luField);
  }

  @Override
  public void setScorer(Scorer scorer) {

  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public Comparable<?> getComparable(int docid) throws IOException {
    try {
      return this.row.search(this.cellResolver, docid) == BitMapOperationStatus.SUCCESS;
    } catch (DatabaseException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean getValue(Document doc) {
    return false;
  }

  @Override
  public boolean getValue(int doc) throws IOException {
    try {
      return this.row.search(this.cellResolver, doc) == BitMapOperationStatus.SUCCESS;
    } catch (DatabaseException e) {
      throw new RuntimeException(e);
    }
  }

}
