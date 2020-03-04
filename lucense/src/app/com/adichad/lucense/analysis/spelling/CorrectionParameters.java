package com.adichad.lucense.analysis.spelling;

public class CorrectionParameters {
  public int editDistance;

  public int prefixLen;

  public Double filterProbability;

  public int maxCorrections;

  public double levenshteinPenaltyFactor;

  public CorrectionParameters() {
    this.editDistance = -1;
    this.prefixLen = -1;
    this.filterProbability = -1d;
    this.maxCorrections = 0;
    this.levenshteinPenaltyFactor = 5d;
  }

}
