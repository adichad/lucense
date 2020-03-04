package org.apache.lucene.search;

import org.apache.lucene.index.FieldInvertState;

public class CustomSimilarity extends Similarity {

  private Query query;

  public CustomSimilarity(Query query) {
    this.query = query;
  }

  @Override
  public float coord(int overlap, int maxOverlap) {
    return this.query.getBoost();
  }

  @Override
  public float idf(int docFreq, int numDocs) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float queryNorm(float sumOfSquaredWeights) {
    // TODO Auto-generated method stub

    return 1;
  }

  @Override
  public float sloppyFreq(int distance) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float tf(float freq) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public float computeNorm(String field, FieldInvertState state) {
    // TODO Auto-generated method stub
    return 0;
  }

}
