package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;

public class IntegerNumwordsSource extends NumwordsFieldSource implements IntValueSource {

  int val = -1;

  private Set<String> scoreFields;

  public IntegerNumwordsSource(String field, Set<String> scoreFields) {
    super(field);
    this.scoreFields = scoreFields;
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
    if (doc != this.lastDoc) {
      this.lastDoc = doc;
      this.val = this.scorer.numwords(this.scoreFields).get(this.getfield);
    }

    return this.val;
  }

}
