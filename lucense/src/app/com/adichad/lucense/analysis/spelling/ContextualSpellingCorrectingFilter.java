package com.adichad.lucense.analysis.spelling;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;

/**
 * Spews out spelling corrections for terms deamed incorrect/misplaced
 */

public final class ContextualSpellingCorrectingFilter extends TokenFilter {

  private final SpellingCorrectionContext termCorrWindow;

  SpellingCorrectionAttribute spellCorrAttr;

  private SpellingCorrectionFilter filter;

  // private final TermSequenceGraph g;
  private final Map<String, Double> baseCollection;

  private final CorrectionParameters params;

  public ContextualSpellingCorrectingFilter(TermSequenceGraph g, CorrectionParameters params, TokenStream input) {
    super(input);
    this.params = params;
    TermAttribute termAttr = addAttribute(TermAttribute.class);
    PositionIncrementAttribute posIncrAttr = addAttribute(PositionIncrementAttribute.class);
    this.spellCorrAttr = input.addAttribute(SpellingCorrectionAttribute.class);
    this.termCorrWindow = new SpellingCorrectionContext(g, termAttr, posIncrAttr);

    if (params.prefixLen > 0)
      this.filter = new PrefixCorrectionFilter(this.termCorrWindow, params.prefixLen);
    if (params.editDistance > 0)
      this.filter = new EditDistanceCorrectionFilter(this.termCorrWindow, params.editDistance,
          params.levenshteinPenaltyFactor, this.filter);
    this.filter = new ContextualTransformFilter(this.termCorrWindow, g, this.filter);
    if (params.filterProbability >= 0.0d)
      this.filter = new ExclusionProbabilityCorrectionFilter(params.filterProbability, this.filter);
    this.filter = new CorrectionSortingFilter(this.filter);
    if (params.maxCorrections > 0)
      this.filter = new MaxNumberCorrectionFilter(params.maxCorrections, this.filter);

    this.baseCollection = g.getDictionary().getEntrySet();
  }

  /**
   * Processes the next token and emits corrections if any
   */
  @Override
  public final boolean incrementToken() throws IOException {
    boolean next = this.input.incrementToken();

    this.termCorrWindow.incrementToken();
    Map<String, Double> corrs = this.filter.filter(this.baseCollection);
    this.spellCorrAttr.addSpellingCorrection(this.termCorrWindow.getCurrent(), corrs, this.params.maxCorrections);
    this.termCorrWindow.setPreviousCorrection(this.spellCorrAttr.getBestCorrection(this.termCorrWindow.getCurrent()));

    return next;
  }

}
