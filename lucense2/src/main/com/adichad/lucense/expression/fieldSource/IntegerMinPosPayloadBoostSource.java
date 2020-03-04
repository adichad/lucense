package com.adichad.lucense.expression.fieldSource;

import java.io.IOException;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.CustomScorer;
import org.apache.lucene.search.Scorer;

public class IntegerMinPosPayloadBoostSource
    implements IntValueSource {

  private Set<String> scoreFields;
  private int payloadPos;
  private String getfield;
  private CustomScorer scorer;
  private String name;

  public IntegerMinPosPayloadBoostSource(String field, Set<String> scoreFields,
      int payloadPos) {
    this.getfield = field;
    this.scoreFields = scoreFields;
    this.payloadPos = payloadPos;
    this.name = "_boostminpos_"+getfield+"_";
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
    return this.scorer.boostAtMinPos(scoreFields, getfield, payloadPos);//(this.scoreFields, this.getfield);
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    
  }

  @Override
  public void setScorer(Scorer scorer) {
    this.scorer = (CustomScorer)scorer;
    
  }

  @Override
  public String getName() {
    return this.name;
  }

}
