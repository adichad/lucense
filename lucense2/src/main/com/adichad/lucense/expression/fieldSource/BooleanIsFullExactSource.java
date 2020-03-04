package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.CustomScorer;
import org.apache.lucene.search.Scorer;

public class BooleanIsFullExactSource implements BooleanValueSource {

  String[]            fields;

  CustomScorer        scorer = null;

  private String      name;

  private Set<String> scoreFields;

  public BooleanIsFullExactSource(Set<String> scoreFields, String... fields) {
    this.fields = fields;
    this.name = "_isfullexact_";
    int i = 0;
    for (String field : fields) {
      if (i > 0)
        this.name += "@";
      this.name += field;
      ++i;
    }
    this.name += "_";
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
    return this.fields.length == 1 ? this.scorer.isFullExact(this.scoreFields,
        fields[0]) : this.scorer.isFullExact(this.scoreFields, fields);
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
