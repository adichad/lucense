package com.adichad.lucense.analysis.synonym;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SynonymMap<K, V> {
  private Map<K, SynonymMap<K, V>> submaps;

  Set<V> payload;

  SynonymMap<K, V> parent;

  Integer span;

  public SynonymMap(SynonymMap<K, V> parent) {
    this.submaps = new HashMap<K, SynonymMap<K, V>>();
    this.payload = new HashSet<V>();
    this.span = null;
    this.parent = parent;
  }

  public void put(List<K> match, V marker) {
    put(match.iterator(), marker, 0);
  }

  private void put(Iterator<K> match, V marker, int i) {
    if (match.hasNext()) {
      K curr = match.next();
      if (!this.submaps.containsKey(curr))
        this.submaps.put(curr, new SynonymMap<K, V>(this));
      this.submaps.get(curr).put(match, marker, i + 1);
    } else {
      this.payload.add(marker);
      this.span = i;
    }
  }

  public boolean containsSubmap(K key) {
    return this.submaps.containsKey(key);
  }

  public SynonymMap<K, V> getSubmap(K key) {
    return this.submaps.get(key);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    toString(1, sb);
    return sb.toString();
  }

  protected void toString(int i, StringBuilder sb) {
    sb.append("=>").append(this.payload).append("{").append(this.span).append("}:\n");
    for (K k : this.submaps.keySet()) {
      for (int c = 0; c < i; c++)
        sb.append(" ");
      sb.append(k);
      this.submaps.get(k).toString(i + 1, sb);
    }
  }

  public static void main(String args[]) {
    SynonymMap<String, Integer> sm = new SynonymMap<String, Integer>(null);
    List<String> m = new ArrayList<String>();
    m.add("adi");
    m.add("ms");
    m.add("net");
    sm.put(m, 1);
    sm.put(m, 9);

    m.clear();
    m.add("ms");
    m.add("net");
    m.add("sql");
    sm.put(m, 5);

    m.clear();
    m.add("ms");
    sm.put(m, 6);

    m.clear();
    m.add("ms");
    m.add("get");
    m.add("sql");
    sm.put(m, 2);

    m.clear();
    m.add("ms");
    m.add("net");
    m.add("java");
    sm.put(m, 3);

    m.clear();
    m.add("ms");
    m.add("net");
    m.add("ms");
    sm.put(m, 4);

    System.out.println(sm);

  }
}
