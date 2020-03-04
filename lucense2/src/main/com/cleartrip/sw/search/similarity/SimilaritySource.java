package com.cleartrip.sw.search.similarity;

import java.util.Map;

import org.apache.lucene.search.Similarity;

public abstract class SimilaritySource {
  public SimilaritySource(Map<String, ?> params) {
    
  }
  public abstract Similarity getSimilarity(); 

}
