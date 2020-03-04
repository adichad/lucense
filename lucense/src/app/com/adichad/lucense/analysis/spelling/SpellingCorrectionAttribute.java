package com.adichad.lucense.analysis.spelling;

import java.util.Map;

import org.apache.lucene.util.Attribute;

import com.adichad.lucense.analysis.spelling.SpellingCorrectionContext.CorrectionContextEntry;

public interface SpellingCorrectionAttribute extends Attribute {
  public Map<CorrectionContextEntry, Map<String, Double>> getSpellingCorrections();

  public void addSpellingCorrection(CorrectionContextEntry current, Map<String, Double> filter, int maxCorrections);

  public Map.Entry<String, Double> getBestCorrection(CorrectionContextEntry current);

}
