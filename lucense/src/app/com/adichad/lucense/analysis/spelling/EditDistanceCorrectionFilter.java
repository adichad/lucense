package com.adichad.lucense.analysis.spelling;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class EditDistanceCorrectionFilter extends SpellingCorrectionFilter {

  private SpellingCorrectionContext correctionContext;

  private int editDistance;

  private double penaltyFactor;

  public EditDistanceCorrectionFilter(SpellingCorrectionContext correctionContext, int editDistance,
      double penaltyFactor, SpellingCorrectionFilter inner) {
    super(inner);
    this.correctionContext = correctionContext;
    this.editDistance = editDistance;
    this.penaltyFactor = penaltyFactor;
  }

  public EditDistanceCorrectionFilter(SpellingCorrectionContext correctionContext, int editDistance,
      SpellingCorrectionFilter inner) {
    this(correctionContext, editDistance, 5d, inner);
  }

  public EditDistanceCorrectionFilter(SpellingCorrectionContext correctionContext, int editDistance) {
    this(correctionContext, editDistance, null);
  }

  @Override
  public Map<String, Double> filterImpl(Map<String, Double> input) {
    String curr = this.correctionContext.getCurrent().getTerm();
    Map<String, Double> c = new HashMap<String, Double>();
    for (Map.Entry<String, Double> term : input.entrySet()) {
      int currEditDistance = StringUtils.getLevenshteinDistance(curr, term.getKey());
      if ((currEditDistance <= this.editDistance)) {
        term.setValue(Math.pow(this.penaltyFactor, -((Integer) currEditDistance).doubleValue()) * term.getValue());
        // term.setValue(
        // (1.0d/(((Integer)currEditDistance).doubleValue()+1.0d))*term.getValue()
        // );
        c.put(term.getKey(), term.getValue());
      }
    }

    return c;
  }

}
