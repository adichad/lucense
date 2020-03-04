package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Scorer;

public class IntegerFieldNormSource implements IntValueSource {

  byte[] vals;

  private final String field;
  private final String name; 

  public IntegerFieldNormSource(String field) {
    this.field = field;
    this.name = "_fieldnorm_"+field+"_";
  }

  @Override
  public int getValue(Document doc) {
    return 0;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public int getValue(int doc) throws IOException {
    return this.vals[doc];
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    this.vals = reader.norms(this.field);
  }

  @Override
  public void setScorer(Scorer scorer) { 
  }

  @Override
  public String getName() {
    return this.name;
  }

}
