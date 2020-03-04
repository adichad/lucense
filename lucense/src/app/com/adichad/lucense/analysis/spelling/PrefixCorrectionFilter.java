package com.adichad.lucense.analysis.spelling;

import java.util.HashMap;
import java.util.Map;

public class PrefixCorrectionFilter extends SpellingCorrectionFilter {
  private SpellingCorrectionContext correctionContext;

  private int prefixLen;

  public PrefixCorrectionFilter(SpellingCorrectionContext correctionContext, int prefixLen,
      SpellingCorrectionFilter inner) {
    super(inner);
    this.correctionContext = correctionContext;
    this.prefixLen = prefixLen;
  }

  public PrefixCorrectionFilter(SpellingCorrectionContext correctionContext, int prefixLen) {
    this(correctionContext, prefixLen, null);
  }

  @Override
  public Map<String, Double> filterImpl(Map<String, Double> input) {
    Map<String, Double> c = new HashMap<String, Double>();
    String oterm = this.correctionContext.getCurrent().getTerm();
    String prefix = (oterm.length() > this.prefixLen) ? oterm.substring(0, this.prefixLen) : oterm;
    for (Map.Entry<String, Double> term : input.entrySet()) {
      if (term.getKey().startsWith(prefix)) {
        c.put(term.getKey(), term.getValue());
      }
    }
    return c;
  }

}
