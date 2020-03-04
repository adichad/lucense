package com.cleartrip.sw.suggest;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcherWrapper;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.SortField;

import com.adichad.lucense.expression.LucenseExpression;
import com.adichad.lucense.expression.ValueSources;
import com.cleartrip.sw.search.context.SortFieldFactory;
import com.cleartrip.sw.search.util.Pair;

public class FullWFSTCollector extends SuggestionCollector {

  private final List<String>                                                fields;
  private final List<String>                                                prePopulatedFields;
  private final Sort                                                       sort;
  // private final boolean searchExactMatches;
  private final int                                                        searchTermLimit;
  private final String                                                     opfield;

  private final Map<Pattern, Pair<String, Pair<Set<String>, Set<String>>>> operators;

	public FullWFSTCollector(List<String> fields,
			List<String> prePopulatedFields,List<SortFieldFactory> sorters,
			Map<String, ?> params) throws Exception {
    this.fields = fields;
    this.prePopulatedFields = prePopulatedFields;
    Map<String, Object2IntOpenHashMap<String>> externalValSource = new HashMap<>();
    Map<String, LucenseExpression> namedExprs = new HashMap<>();
    ValueSources valueSources = new ValueSources();
    Set<String> scoreFields = new HashSet<>(0);
    SortField[] sortFields = new SortField[sorters.size()];
    for (int i = 0; i < sortFields.length; i++) {
      sortFields[i] = sorters.get(i).getSortField(externalValSource,
          namedExprs, valueSources, scoreFields);
    }
    this.sort = new Sort(sortFields);
    BooleanQuery.setMaxClauseCount(10000);
    this.operators = new HashMap<>();
    Map<String, ?> ops = (Map<String, ?>) params.get("operators");

    for (String op : ops.keySet()) {
      int i = 0;
      StringBuilder sb = new StringBuilder();
      Map<String, ?> opparams = (Map<String, ?>) ops.get(op);
      List<String> matches = (List<String>) opparams.get("match");
      for (String part : matches) {
        if (i++ > 0)
          sb.append("|");
        sb.append(part);
      }

      sb.insert(0, "\\b(?:").append(")\\b");
      Set<String> beforeFields = new HashSet<>(
          (List<String>) opparams.get("lhs"));
      Set<String> afterFields = new HashSet<>(
          (List<String>) opparams.get("rhs"));
      Pair<Set<String>, Set<String>> inp = new Pair<Set<String>, Set<String>>(
          beforeFields, afterFields);
      Pair<String, Pair<Set<String>, Set<String>>> p = new Pair<String, Pair<Set<String>, Set<String>>>(
          op, inp);
      this.operators.put(Pattern.compile(sb.toString()), p);

    }
    this.opfield = (String) params.get("operatorField");

    // this.searchExactMatches = (Boolean) params.get("searchExactMatches");
    this.searchTermLimit = (Integer) params.get("searchTermLimit");
  }

  @Override
  public SuggestResult collect(String query, IndexSearcherWrapper searcher,
      int offset, int limit) throws IOException {
    int olimit = limit;
    
    LinkedHashSet<String>  results = searcher.lookupFST(fields, query, true,
            this.searchTermLimit, sort, offset, limit);   
    System.out.println("full fst lookup for: " + results);
    if (results.size() >= olimit)
      return new SuggestResult(results);
    limit = olimit - results.size();
    
//    LinkedHashSet<String> results = searcher.lookupFST(fields,
//			query, true, this.searchTermLimit, sort, offset, limit);
    if (results.size() == 0) {

    	results.addAll(searcher.lookupFST(prePopulatedFields,
    			query, true, this.searchTermLimit, sort, offset, limit));
    	System.out.println("Prepopulated fields FSt results : " + results);

    	if (results.size() >= olimit)
    		return new SuggestResult(results);
    	limit = olimit - results.size();

    }


    String[] qparts = null;
    Pair<String, Pair<Set<String>, Set<String>>> op = null;
    for (Pattern p : operators.keySet()) {
    	System.out.println("qparts Length : " + p.split(query).length);
      if ((qparts = p.split(query)).length > 1) {
        op = operators.get(p);
        break;
      }
    }
    // found operator
    if (op != null) {
      // before part
      LinkedHashSet<String> before = exactOrFuzzy(searcher, op.second.first,
          qparts[0].trim(), offset, limit);
      // after part
      LinkedHashSet<String> after = exactOrFuzzy(searcher, op.second.second,
          qparts[1].trim(), offset, limit);
      // combine
      StringBuilder sb = new StringBuilder();
      int i = 0;
      for (String b : before) {
        for (String a : after) {
          if (i++ >= offset && results.size() < olimit) {
            sb.setLength(0);
            results.add(sb.append(b).append(" ").append(op.first).append(" ")
                .append(a).toString());
          } else if (results.size() >= olimit)
            return new SuggestResult(results);
        }
      }

    }
    System.out.println("operator lookup: " + results);
	if (results.size() >= olimit)
		return new SuggestResult(results);
    limit = olimit - results.size();
    
    // not enough full fst match and not enough operator matches
    String head = query.substring(0, Math.max(query.lastIndexOf(' '), 0));
    if (!head.equals("")) {
      String tail = query.substring(
          Math.max(query.lastIndexOf(' '), head.length()) + 1, query.length());
      List<String> opfields = new ArrayList<>(1);
      opfields.add(opfield);
      op = null;
      Iterator<String> it = searcher.lookupFST(opfields, tail, true, 1, sort,
          0, 1).iterator();
      if (it.hasNext()) {
        String v = it.next();
        for (Pattern p : operators.keySet()) {
        	System.out.println("v :" + v);
          if (p.matcher(v).matches()) {
        	  System.out.println("p :" + p);
            op = operators.get(p);
            break;
          }
        }
      }

      LinkedHashSet<String> b = null;
      if (op == null) {
        b = searcher.lookupFST(fields, head, true, offset + limit, sort,
            offset, limit);
      } else {
        b = new LinkedHashSet<String>(1);
        if (searcher.lookupExact(op.second.first, head))
          b.add(head);
      }

      if (b.size() == 0)
        b = searcher.lookupFuzzy(fields, head, sort, offset, limit);

      if (op != null) {
        for (String before : b) {
          results.add(before + " " + op.first);
        }
      }
    }
    System.out.println("lookup head before op: " + results);

//    System.out.println("prePopulated fields are" + prePopulatedFields);
//	results.addAll(searcher.lookupFST(prePopulatedFields,
//				query, true, this.searchTermLimit, sort, offset, limit));
//	System.out.println("Prepopulated fields FSt results : " + results);
//  
//	if (results.size() >= olimit)
//		return new SuggestResult(results);
//	limit = olimit - results.size();
    
    if (results.size() < olimit) {
      limit = olimit - results.size();
      results.addAll(searcher.lookupFuzzy(fields, query, sort, offset, limit));
    }
    System.out.println("lookup fuzzy: " + results);
    return new SuggestResult(results);

  }

  private LinkedHashSet<String> exactOrFuzzy(IndexSearcherWrapper searcher,
      Set<String> fields, String query, int offset, int limit)
      throws IOException {
    LinkedHashSet<String> before = new LinkedHashSet<>();
    if (searcher.lookupExact(fields, query)) {// found exact match
      before.add(query);
    }
    if (before.size() < limit) {
      before.addAll(searcher.lookupFST(new ArrayList<>(fields), query, true, offset + limit,
          sort, offset, limit - before.size()));
    }
    if (before.size() < limit) {// try fuzzy matches
      before.addAll(searcher.lookupFuzzy(new ArrayList<>(fields), query, sort, offset, limit
          - before.size()));
    }
    return before;
  }
}
