package com.cleartrip.sw.search.similarity;

import java.util.Map;

import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;


public class DefaultSimilaritySource extends SimilaritySource {

  public DefaultSimilaritySource(Map<String, ?> params) {
    super(params);
  }

  private static final Similarity similarity = new DefaultSimilarity();
  
  @Override
  public Similarity getSimilarity() {
    return similarity;
  }

}
