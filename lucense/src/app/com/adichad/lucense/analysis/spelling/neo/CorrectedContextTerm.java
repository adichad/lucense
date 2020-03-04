package com.adichad.lucense.analysis.spelling.neo;

import java.util.Map;

public class CorrectedContextTerm {
  int posIncr;
  String term;
  Map<Correction, Correction> corrs;
  
  public String toString() {
    
    return term+": "+corrs.toString();
  }
}
