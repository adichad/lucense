package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;

public class FloatNumwordsSource extends NumwordsFieldSource implements FloatValueSource {
  float val = -1f;

  private Set<String> scoreFields;

  public FloatNumwordsSource(String field, Set<String> scoreFields) {
    super(field);
    this.scoreFields = scoreFields;
  }

  @Override
  public float getValue(Document doc) {
    return 0f;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public float getValue(int doc) throws IOException {
    if (doc != this.lastDoc) {
      this.lastDoc = doc;
      this.val = (this.scorer.numwords(this.scoreFields).get(this.getfield)).floatValue();
    }
    return this.val;
  }

}
