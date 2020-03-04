package com.cleartrip.sw.search.similarity;

import java.util.Map;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;


public class SmallworldSimilaritySource extends SimilaritySource {

  public SmallworldSimilaritySource(Map<String, ?> params) {
    super(params);
  }

  private static final Similarity similarity = new DefaultSimilarity() {
    @Override
    public float computeNorm(String field, FieldInvertState state) {
      //if(field.equals("name")||field.equals("aliases")) {
        final int numTerms;
        if (discountOverlaps)
          numTerms = state.getLength() - state.getNumOverlap();
        else
          numTerms = state.getLength();
        //float norm = state.getBoost() * ((float) (1.0 / (numTerms*numTerms*numTerms)));
        return numTerms>127?127:numTerms;
      //}
      //return super.computeNorm(field, state);
    }
    
    @Override
    public float queryNorm(float sumOfSquaredWeights) {
      return 1f;// (float)(1.0 / Math.sqrt(sumOfSquaredWeights));
    }

    @Override
    public float tf(float freq) {
      return freq>0f?1f:0f;//(float)Math.sqrt(freq);
    }
    
    @Override
    public float sloppyFreq(int distance) {
      return 1.0f / (distance + 1);
    }
      
    @Override
    public float idf(int docFreq, int numDocs) {
      return 1f;//(float)(Math.log(numDocs/(double)(docFreq+1)) + 1.0);
    }
      
    @Override
    public float coord(int overlap, int maxOverlap) {
      return overlap>0?1f:0f;//overlap / (float)maxOverlap;
    }
    
    @Override
    public String toString() {
      return "smallworld-similarity";
    }
    
    @Override
    public byte encodeNormValue(float f) {
      return (byte)f;
    }
    
    @Override
    public float decodeNormValue(byte f) {
      return (float)f;
    }

  };
  
  @Override
  public Similarity getSimilarity() {
    Similarity.setDefault(similarity);
    return similarity;
  }

}
