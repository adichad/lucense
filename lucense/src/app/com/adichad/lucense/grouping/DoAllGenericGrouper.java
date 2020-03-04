package com.adichad.lucense.grouping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.FieldComparator;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.SortField;

import com.adichad.lucense.expression.BooleanLucenseExpression;
import com.adichad.lucense.expression.ExpressionFactory;
import com.adichad.lucense.expression.ValueSources;
import com.adichad.lucense.expression.fieldSource.ValueSource;
import com.adichad.lucense.request.Request.FieldType;
import com.adichad.lucense.resource.SearchResourceManager;
import com.adichad.lucense.result.SearchResult;

public class DoAllGenericGrouper extends GenericGrouper {
  private int size;

  private Map<ArrayList<Comparable<?>>, ArrayList<FieldComparator<?>>> selectMap;

  private Map<ArrayList<Comparable<?>>, ArrayList<FieldComparator<?>>> sorterMap;

  private Map<ArrayList<Comparable<?>>, BooleanLucenseExpression> havingMap;

  private BooleanLucenseExpression whereClause;

  private int limit;

  private int offset;

  private GenericGroupCriteria grouper;

  private IndexReader reader;

  private int docBase;

  private Scorer scorer;

  // private long addTime = 0;

  //private int addCount = 0;

  private GenericGrouper inner;

  private String name;

  private SearchResourceManager srm;

  public DoAllGenericGrouper(String name, GenericGroupCriteria grouper, GenericGrouper inner, SearchResourceManager srm)
      throws IOException {
    super(grouper);

    this.name = name;
    this.selectMap = new HashMap<ArrayList<Comparable<?>>, ArrayList<FieldComparator<?>>>();
    this.sorterMap = new HashMap<ArrayList<Comparable<?>>, ArrayList<FieldComparator<?>>>();
    this.havingMap = new HashMap<ArrayList<Comparable<?>>, BooleanLucenseExpression>();
    this.whereClause = grouper.whereExpr;
    this.grouper = grouper;
    this.size = grouper.groupoffset + grouper.grouplimit;
    this.offset = grouper.groupoffset;
    this.limit = grouper.grouplimit;
    this.inner = inner;
    this.srm = srm;
  }

  @Override
  public void collect(int doc) throws IOException {
    if (this.inner != null)
      this.inner.collect(doc);
    ArrayList<Comparable<?>> key = new ArrayList<Comparable<?>>(this.keypartsource.length);

    if ((this.whereClause == null) || this.whereClause.evaluate(doc)) {
      for (ValueSource vs : this.keypartsource) {
        key.add(vs.getComparable(doc));
      }
      
      if (this.selectMap.containsKey(key)) {
        ArrayList<FieldComparator<?>> selectcomparators = this.selectMap.get(key);
        for (FieldComparator<?> comp : selectcomparators) {
          comp.copy(0, doc);
        }

        ArrayList<FieldComparator<?>> sortcomparators = this.sorterMap.get(key);
        for (FieldComparator<?> comp : sortcomparators) {
          comp.copy(0, doc);
        }
        this.havingMap.get(key).evaluate(doc);
      } else {
        ArrayList<FieldComparator<?>> selectcomparators = new ArrayList<FieldComparator<?>>(this.grouper.select.size());
        Collection<SortField> selectfields = this.grouper.select.values();
        for (SortField field : selectfields) {
          FieldComparator<?> comp = field.getComparator(1, 0);
          comp.setNextReader(this.reader, this.docBase);
          comp.setScorer(this.scorer);
          comp.copy(0, doc);
          selectcomparators.add(comp);
        }
        this.selectMap.put(key, selectcomparators);

        ArrayList<FieldComparator<?>> sortcomparators = new ArrayList<FieldComparator<?>>(
            this.grouper.sortfields.length);
        int i = 0;
        for (SortField field : this.grouper.sortfields) {
          FieldComparator<?> comp = field.getComparator(1, i);
          comp.setNextReader(this.reader, this.docBase);
          comp.setScorer(this.scorer);
          comp.copy(0, doc);
          sortcomparators.add(comp);
          i++;
        }
        this.sorterMap.put(key, sortcomparators);

        BooleanLucenseExpression having = (BooleanLucenseExpression) ExpressionFactory.getExpressionFromString(
            this.grouper.havingExpr, FieldType.TYPE_BOOLEAN, this.grouper.cx, this.grouper.scope,
            this.grouper.externalValSource, this.grouper.namedExprs, new ValueSources(this.grouper.intValueSources,
                this.grouper.floatValueSources, this.grouper.doubleValueSources, this.grouper.booleanValueSources,
                this.grouper.stringValueSources), this.srm);

        having.setNextReader(this.reader, this.docBase);
        having.setScorer(this.scorer);
        having.evaluate(doc);
        this.havingMap.put(key, having);

      }
    }
    // this.addTime += (new Date()).getTime() - start.getTime();
    // this.addCount++;
  }

  @Override
  public void setNextReader(IndexReader reader, int docBase) throws IOException {
    if (this.inner != null)
      this.inner.setNextReader(reader, docBase);
    for (int i = 0; i < this.keypartsource.length; i++) {
      this.keypartsource[i].setNextReader(reader, docBase);
    }
    for (ArrayList<FieldComparator<?>> comps : this.selectMap.values()) {
      for (FieldComparator<?> comp : comps)
        comp.setNextReader(reader, docBase);
    }
    for (ArrayList<FieldComparator<?>> comps : this.sorterMap.values()) {
      for (FieldComparator<?> comp : comps)
        comp.setNextReader(reader, docBase);
    }
    for (BooleanLucenseExpression having : this.havingMap.values()) {
      having.setNextReader(reader, docBase);
    }
    if (this.whereClause != null) {
      this.whereClause.setNextReader(reader, docBase);
    }
    this.reader = reader;
    this.docBase = docBase;
  }

  @Override
  public void setScorer(Scorer scorer) {
    if (this.inner != null)
      this.inner.setScorer(scorer);
    for (int i = 0; i < this.keypartsource.length; i++) {
      this.keypartsource[i].setScorer(scorer);
    }
    for (ArrayList<FieldComparator<?>> comps : this.selectMap.values()) {
      for (FieldComparator<?> comp : comps)
        comp.setScorer(scorer);
    }
    for (ArrayList<FieldComparator<?>> comps : this.sorterMap.values()) {
      for (FieldComparator<?> comp : comps)
        comp.setScorer(scorer);
    }
    for (BooleanLucenseExpression having : this.havingMap.values()) {
      having.setScorer(scorer);
    }
    if (this.whereClause != null)
      this.whereClause.setScorer(scorer);
    this.scorer = scorer;
  }

  @Override
  public void setBottom(int slot) {
    if (this.inner != null)
      this.inner.setBottom(slot);
    for (ArrayList<FieldComparator<?>> comps : this.selectMap.values()) {
      for (FieldComparator<?> comp : comps)
        comp.setBottom(slot);
    }
    for (ArrayList<FieldComparator<?>> comps : this.sorterMap.values()) {
      for (FieldComparator<?> comp : comps)
        comp.setBottom(slot);
    }

  }

  @Override
  public void fillGroupings(SearchResult res) {
    if (this.inner != null)
      this.inner.fillGroupings(res);
    res.addGrouping(this.name, getGrouping(true));
  }

  private GroupingResult getGrouping(boolean sort) {
    if (sort) {
      Map<ArrayList<Comparable<?>>, ArrayList<Comparable<?>>> sorterVals = new HashMap<ArrayList<Comparable<?>>, ArrayList<Comparable<?>>>();
      for (ArrayList<Comparable<?>> key : this.havingMap.keySet()) {
        if (this.havingMap.get(key).evaluateFinal()) {
          sorterVals.put(key, getVals(this.sorterMap.get(key)));
        }
      }
      
      GroupSortingPriorityQueue<ArrayList<Comparable<?>>> pq = new GroupSortingPriorityQueue<ArrayList<Comparable<?>>>(
          this.size, sorterVals, this.grouper.sortfields);

      for (ArrayList<Comparable<?>> key : sorterVals.keySet()) {
        pq.insertWithOverflow(key);
      }

      Stack<ArrayList<Comparable<?>>> stack = new Stack<ArrayList<Comparable<?>>>();
      ArrayList<Comparable<?>> e;
      while ((e = pq.pop()) != null) {
        stack.push(e);
      }

      LinkedHashMap<List<Comparable<?>>, List<Comparable<?>>> lhmObject = new LinkedHashMap<List<Comparable<?>>, List<Comparable<?>>>(
          this.limit);

      for (int i = 0; (i < this.offset) && !stack.isEmpty(); ++i) {
        stack.pop();
      }
      while (!stack.isEmpty()) {
        e = stack.pop();
        lhmObject.put(e, getVals(this.selectMap.get(e)));
      }
      return new GroupingResult(lhmObject, sorterVals.size(), this.grouper.select, this.grouper.subgroupers, this.grouper.getresults);
    } else {
      Map<List<Comparable<?>>, List<Comparable<?>>> selectValMap = new HashMap<List<Comparable<?>>, List<Comparable<?>>>();
      for (ArrayList<Comparable<?>> key : this.selectMap.keySet())
        selectValMap.put(key, getVals(this.selectMap.get(key)));
      return new GroupingResult(selectValMap, selectValMap.size(), this.grouper.select, this.grouper.subgroupers, this.grouper.getresults);
    }
  }

  private ArrayList<Comparable<?>> getVals(ArrayList<FieldComparator<?>> comps) {
    ArrayList<Comparable<?>> vals = new ArrayList<Comparable<?>>(comps.size());
    for (FieldComparator<?> comp : comps) {
      vals.add((Comparable<?>) comp.value(0));
    }
    return vals;
  }

}
