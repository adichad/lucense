package com.adichad.lucense.analysis.stem;

import java.util.Set;

public interface InvertingStemmer {

  public void addInversions(Set<String> candidates);

}
