package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;

public class StringNumwordsSource extends NumwordsFieldSource implements StringValueSource {

  private Set<String> scoreFields;

  public StringNumwordsSource(String field, Set<String> scoreFields) {
    super(field);
    this.scoreFields = scoreFields;
  }

  @Override
  public String getValue(Document doc) {
    return null;// new StringExpressionTree(new StringLiteral(null), null);
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public String getValue(int doc) throws IOException {
    return new Integer(this.scorer.numwords(this.scoreFields,this.getfield)).toString();
  }

}
