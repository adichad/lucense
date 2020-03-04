package com.adichad.lucense.analysis.spelling;

import java.util.Map;

public abstract class SpellingCorrectionFilter {
  protected SpellingCorrectionFilter inner;

  protected SpellingCorrectionFilter(SpellingCorrectionFilter inner) {
    this.inner = inner;
  }

  public final Map<String, Double> filter(Map<String, Double> input) {
    if (this.inner != null)
      input = this.inner.filter(input);
    return filterImpl(input);
  }

  protected abstract Map<String, Double> filterImpl(Map<String, Double> input);
}
