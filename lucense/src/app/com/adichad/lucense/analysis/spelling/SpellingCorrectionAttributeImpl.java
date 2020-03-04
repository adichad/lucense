package com.adichad.lucense.analysis.spelling;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.apache.lucene.util.AttributeImpl;

import com.adichad.lucense.analysis.spelling.SpellingCorrectionContext.CorrectionContextEntry;

public class SpellingCorrectionAttributeImpl extends AttributeImpl implements SpellingCorrectionAttribute {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  private Map<CorrectionContextEntry, Map<String, Double>> map;

  public SpellingCorrectionAttributeImpl() {
    this.map = new HashMap<CorrectionContextEntry, Map<String, Double>>();
  }

  @Override
  public void addSpellingCorrection(CorrectionContextEntry current, Map<String, Double> filtered, int maxCorrections) {
    if (!this.map.containsKey(current)) {
      this.map.put(current, filtered);
    } else {
      Map<String, Double> innerMap = this.map.get(current);
      if (filtered instanceof TreeMap<?, ?>) {
        TreeMap<String, Double> p = (TreeMap<String, Double>) filtered;
        while (!p.isEmpty()) {
          Map.Entry<String, Double> e = p.pollFirstEntry();
          String corr = e.getKey();
          Double val = e.getValue();
          if (innerMap.containsKey(corr))
            innerMap.put(corr, Math.max(val, innerMap.get(corr)));
          else
            innerMap.put(corr, val);
        }
      } else {
        for (String corr : filtered.keySet()) {
          if (innerMap.containsKey(corr)) {
            innerMap.put(corr, Math.max(filtered.get(corr), innerMap.get(corr)));
          } else {
            innerMap.put(corr, filtered.get(corr));
          }
        }
      }
    }
    Map<String, Double> innerMap = this.map.get(current);
    Map<String, Double> nMap = new LinkedHashMap<String, Double>();
    int i = 0;
    for (String corr : innerMap.keySet()) {
      if (i < maxCorrections) {
        nMap.put(corr, innerMap.get(corr));
      }
      i++;
    }
    this.map.put(current, nMap);
  }

  @Override
  public Map<CorrectionContextEntry, Map<String, Double>> getSpellingCorrections() {
    return this.map;
  }

  @Override
  public Map.Entry<String, Double> getBestCorrection(CorrectionContextEntry current) {
    Iterator<Map.Entry<String, Double>> it = this.map.get(current).entrySet().iterator();
    if (it.hasNext())
      return it.next();
    return null;
  }

  @Override
  public String toString() {
    return this.map.toString();
  }

  @Override
  public void clear() {

  }

  @Override
  public void copyTo(AttributeImpl target) {
    if (target instanceof SpellingCorrectionAttributeImpl) {
      ((SpellingCorrectionAttributeImpl) target).map.clear();
      ((SpellingCorrectionAttributeImpl) target).map.putAll(this.map);
    }

  }

  @Override
  public boolean equals(Object other) {
    if (other instanceof SpellingCorrectionAttributeImpl) {
      return this.map.equals(((SpellingCorrectionAttributeImpl) other).map);
    }
    return false;
  }

  @Override
  public int hashCode() {
    return this.map.hashCode();
  }
}
