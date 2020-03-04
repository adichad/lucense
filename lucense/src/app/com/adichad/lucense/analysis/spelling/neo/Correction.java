package com.adichad.lucense.analysis.spelling.neo;

public class Correction implements Comparable<Correction> {
  String label;
  float score;
  
  @Override
  public String toString() {
    return label+"=>"+score;
  }
  
  @Override
  public int hashCode() {
    return label.hashCode();
  }
  
  @Override
  public boolean equals(Object c) {
    return (c instanceof Correction) && this.label.equals(((Correction)c).label);
  }

  @Override
  public int compareTo(Correction arg0) {
    if(this.equals(arg0))
      return 0;
    if (this.score < arg0.score)
      return 1;
    else if (this.score > arg0.score)
      return -1;
    else
      return this.label.compareTo(arg0.label);
      
  }
}
