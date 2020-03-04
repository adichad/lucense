package com.adichad.lucense.analysis.spelling;

import java.util.HashMap;
import java.util.Map;

public class ContextualTransformFilter extends SpellingCorrectionFilter {

  private SpellingCorrectionContext correctionContext;

  private TermSequenceGraph g;

  public ContextualTransformFilter(SpellingCorrectionContext termCorrWindow, TermSequenceGraph g,
      SpellingCorrectionFilter inner) {
    super(inner);
    this.correctionContext = termCorrWindow;
    this.g = g;
  }

  @Override
  public Map<String, Double> filterImpl(Map<String, Double> input) {
    // String curr =
    // (correctionContext.getCurrent()==null)?null:correctionContext.getCurrent().getTerm();
    String next = ((this.correctionContext.getNext() == null) || this.correctionContext.getNext().getTerm().trim()
        .equals("")) ? null : this.correctionContext.getNext().getTerm();
    String prev = ((this.correctionContext.getPrevious() == null) || this.correctionContext.getPrevious().getTerm()
        .trim().equals("")) ? null : this.correctionContext.getPrevious().getTerm();
    // String prevPrev = (correctionContext.getPrevPrev()==null||
    // correctionContext.getPrevPrev().getTerm().trim().equals(""))?null:correctionContext.getPrevPrev().getTerm();
    String correctedPrev = (this.correctionContext.getPreviousCorrection() == null) ? null : this.correctionContext
        .getPreviousCorrection().getKey();

    Map<String, Double> c = new HashMap<String, Double>();
    for (Map.Entry<String, Double> term : input.entrySet()) {
      Double p = term.getValue();
      if (next != null) {
        Double q = this.g.getProbabilityGivenNext(term.getKey(), next);
        p *= (q != 0.0d) ? q : 0.0000001d;
      }
      if (prev != null) {
        Double q1 = this.g.getProbabilityGivenPrevious(term.getKey(), correctedPrev);
        Double q = this.g.getProbabilityGivenPrevious(term.getKey(), prev);
        // System.out.println(term.getKey()+"("+prev+":"+q+","+correctedPrev+q1+"):");
        q = Math.max(q, q1);
        p *= (q != 0.0d) ? q : 0.0000001d;

      }
      term.setValue(p);
      c.put(term.getKey(), term.getValue());
    }
    return c;
  }

}
