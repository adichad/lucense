package com.adichad.lucense.analysis.spelling;

import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class CorrectionSortingFilter extends SpellingCorrectionFilter {

  public CorrectionSortingFilter(SpellingCorrectionFilter inner) {
    super(inner);
  }

  public static class CustomComparator<T> implements Comparator<String> {
    private Map<String, Double> map;

    public CustomComparator(Map<String, Double> map) {
      this.map = map;
    }

    @Override
    public int compare(String arg0, String arg1) {
      if (this.map.get(arg0) < this.map.get(arg1))
        return 1;
      else if (this.map.get(arg0) > this.map.get(arg1))
        return -1;
      else
        return 0;
    }

  }

  @Override
  public Map<String, Double> filterImpl(Map<String, Double> input) {
    SortedMap<String, Double> pq = new TreeMap<String, Double>(new CustomComparator<String>(input));
    pq.putAll(input);
    return pq;
  }
}
