package org.apache.lucene.search;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.suggest.Lookup;
import org.apache.lucene.search.suggest.Lookup.LookupResult;
import org.apache.lucene.search.suggest.fst.FSTLookup;

import com.cleartrip.sw.search.util.Pair;
import com.cleartrip.sw.suggest.LookupFactory;

public class IndexSearcherWrapper extends IndexSearcher {

  private final Map<String, Pair<Lookup, Term>> lookups;

  public IndexSearcherWrapper(IndexReader r, ExecutorService executor,
      Map<String, LookupFactory> lookupFactories) throws IOException {
    super(r, executor);
    lookups = new HashMap<>(lookupFactories.size());
    for (String field : lookupFactories.keySet()) {
      lookups.put(field,
          new Pair<>(lookupFactories.get(field).getLookup(r, field), new Term(
              field)));
    }

  }

  private static final class MyFieldSelector implements FieldSelector {
    private static final long serialVersionUID = 1L;
    private final List<String> fields;

    MyFieldSelector(List<String> fields) {
      this.fields = fields;
    }

    @Override
    public FieldSelectorResult accept(String fieldName) {
      if (fields.contains(fieldName))
        return FieldSelectorResult.LOAD_AND_BREAK;

      return FieldSelectorResult.NO_LOAD;
    }

  }

  public LinkedHashSet<String> lookupFST(final List<String> fields,
      final CharSequence key, final boolean onlyMorePopular,
      final int lookupLimit, final Sort sort, final int offset, final int limit)
      throws IOException {
    BooleanQuery bq = new BooleanQuery();
//    LinkedHashSet<String> results = new LinkedHashSet<>(limit - offset);
    
    for (String field : fields) {
      Pair<Lookup, Term> p = lookups.get(field);
      Lookup lookup = p.first;
      Term term = p.second;
      List<LookupResult> res = lookup.lookup(key, onlyMorePopular, lookupLimit);
      int resCount = 0;
      
      for (LookupResult lr : res) {
    	  resCount += 1;
//    	  if (resCount > offset && resCount <= limit) {
//    		  results.add(lr.key.toString());
//    	  }
        bq.add(new TermQuery(term.createTerm(lr.key.toString())), Occur.SHOULD);
      }
    }
    return customSearch(fields, sort, offset, limit, bq);
//    return results;
  }

  public boolean lookupExact(final Set<String> fields, final CharSequence key)
      throws IOException {

    for (String field : fields) {
      Pair<Lookup, Term> p = lookups.get(field);
      Term t;
      if (p == null)
        t = new Term(field);
      else
        t = p.second;
      TermEnum te;
      if ((te = this.reader.terms(t.createTerm(key.toString()))) != null
          && te.term().text().equals(key))
        return true;
    }
    return false;
  }

  public LinkedHashSet<String> lookupFuzzy(final List<String> fields,
      final CharSequence key, final Sort sort, final int offset, final int limit)
      throws IOException {
    BooleanQuery bq = new BooleanQuery();

    for (String field : fields) {
      Pair<Lookup, Term> p = lookups.get(field);
      Term term;
      if (p == null)
        term = new Term(field);
      else
        term = p.second;

      bq.add(new FuzzyQuery(term.createTerm(key.toString()), 0.6f, 2, 300),
          Occur.SHOULD);
    }

    return customSearch(fields, sort, offset, limit, bq);
  }

  private LinkedHashSet<String> customSearch(final List<String> fields,
      final Sort sort, final int offset, final int limit, Query bq)
      throws IOException, CorruptIndexException {
    int lim = offset + limit;
    ScoreDoc[] scoreDocs = search(bq, lim, sort).scoreDocs;
    int len = Math.max(Math.min(limit, scoreDocs.length - offset), 0);
    LinkedHashSet<String> results = new LinkedHashSet<>(len);
    if (len > 0) {
      MyFieldSelector sel = new MyFieldSelector(fields);
      for (int i = offset; i < lim && i < scoreDocs.length; ++i) {
        Document d = doc(scoreDocs[i].doc, sel);
        results.add(d.getFields().get(0).stringValue());
      }
    }

    return results;
  }
}
