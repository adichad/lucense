package com.adichad.lucense.analysis.synonym;

import java.util.HashSet;

public class HighlightObject {

  int maxSpan;

  HashSet<String> entries;

  public HighlightObject(int maxSpan, HashSet<String> entries) {
    this.maxSpan = maxSpan;
    this.entries = entries;
  }

  @Override
  public String toString() {
    String s = "Max Span : " + maxSpan + " :: Entries : " + entries;

    // System.out.println(" : "+s);

    return s;
  }
}
