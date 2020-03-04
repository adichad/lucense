package com.adichad.lucense.analysis.stem;

import java.util.Map;
import java.util.Set;

import org.apache.lucene.util.Attribute;

public interface StemInversionAttribute extends Attribute {
  public Map<String, Set<String>> getStemInversions();

  public String addStemInversion(String original, String inversion, InvertingStemmer stemmer);

  public void addSynonymInversion(String original, String inversion);

  public void setLast(InvertingStemmer stemmer);
}
