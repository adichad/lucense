package com.adichad.lucense.analysis.spelling;

import java.util.HashMap;
import java.util.Map;

public class ExclusionProbabilityCorrectionFilter extends SpellingCorrectionFilter {

  private double filterGreaterThan;

  public ExclusionProbabilityCorrectionFilter(double filterGreaterThan, SpellingCorrectionFilter inner) {
    super(inner);
    this.filterGreaterThan = filterGreaterThan;
  }

  public ExclusionProbabilityCorrectionFilter(double filterGreaterThan) {
    this(filterGreaterThan, null);
  }

  @Override
  public Map<String, Double> filterImpl(Map<String, Double> input) {
    Map<String, Double> c = new HashMap<String, Double>();
    for (Map.Entry<String, Double> term : input.entrySet()) {
      if (term.getValue() > this.filterGreaterThan) {
        c.put(term.getKey(), term.getValue());
      }
    }

    return c;
  }

}
