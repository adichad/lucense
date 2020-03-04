package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.CustomScorer;
import org.apache.lucene.search.Scorer;

public class BooleanIsExactSource implements BooleanValueSource {

  String field = null;

  CustomScorer scorer = null;

  private String name;

  private Set<String> scoreFields;

  public BooleanIsExactSource(String field, Set<String> scoreFields) {
    this.field = field;
    this.name = "_isexact_" + field + "_";
    this.scoreFields = scoreFields;
  }

  @Override
  public boolean getValue(Document doc) {
    return false;
  }

  @Override
  public Comparable<?> getComparable(int doc) throws IOException {
    return getValue(doc);
  }

  @Override
  public boolean getValue(int doc) throws IOException {
    return this.scorer.isExact(this.scoreFields, this.field);
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {

  }

  @Override
  public void setScorer(Scorer scorer) {
    this.scorer = (CustomScorer) scorer;
  }

}
