package com.adichad.lucense.grouping;

/**
 * @author adichad
 * @date 8 Jun, 2010
 * @desc Class to implement a priority queue for HashMap Entry elements
 */

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;

import org.apache.lucene.search.SortField;
import org.apache.lucene.util.PriorityQueue;

public class GroupSortingPriorityQueue<T> extends PriorityQueue<T> {
  private Map<ArrayList<Comparable<?>>, ArrayList<Comparable<?>>> sorterMap;

  ArrayList<Comparator<Comparable<?>>> comps;

  public GroupSortingPriorityQueue(int count, Map<ArrayList<Comparable<?>>, ArrayList<Comparable<?>>> sorterMap,
      SortField[] sortfields) {
    super();
    initialize(count);
    this.sorterMap = sorterMap;
    this.comps = new ArrayList<Comparator<Comparable<?>>>();
    for (SortField sortfield : sortfields) {
      if (sortfield.getReverse())
        this.comps.add(new Comparator<Comparable<?>>() {
          @SuppressWarnings({ "unchecked", "rawtypes" })
          @Override
          public int compare(Comparable o1, Comparable o2) {
            return o1.compareTo(o2);
          }
        });
      else
        this.comps.add(new Comparator<Comparable<?>>() {
          @SuppressWarnings({ "unchecked", "rawtypes" })
          @Override
          public int compare(Comparable o1, Comparable o2) {
            return o2.compareTo(o1);
          }
        });
    }
  }

  @Override
  protected boolean lessThan(Object a, Object b) {
    Iterator<Comparable<?>> iterA = this.sorterMap.get(a).iterator();
    Iterator<Comparable<?>> iterB = this.sorterMap.get(b).iterator();
    for (Comparator<Comparable<?>> comp : this.comps) {
      int i = comp.compare(iterA.next(), iterB.next());
      if (i < 0)
        return true;
      if (i > 0)
        return false;
    }
    return false;
  }
}
