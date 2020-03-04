package com.adichad.lucense.analysis.spelling;

import java.util.LinkedHashMap;
import java.util.Map;

public class MaxNumberCorrectionFilter extends SpellingCorrectionFilter {

  private int max;

  protected MaxNumberCorrectionFilter(int max, SpellingCorrectionFilter inner) {
    super(inner);
    this.max = max;
  }

  @Override
  protected Map<String, Double> filterImpl(Map<String, Double> input) {
    Map<String, Double> c = new LinkedHashMap<String, Double>();
    int i = 0;
    for (Map.Entry<String, Double> term : input.entrySet()) {
      if (i < this.max)
        c.put(term.getKey(), term.getValue());
      i++;
    }

    return c;
  }

}
