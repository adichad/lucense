package com.cleartrip.sw.search.filters;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.document.Fieldable;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.TermQuery;

public class GeoPathDedupFilterFactory extends SearchFilterFactory {

  private static class GeoPathDedupFilter extends SearchFilter {

    int                              i                = 0;
    private String                   matchField;
    private String                   containsField;
    private IndexReader              reader;
    private final String             retainField;
    HashMap<String, TermFreqVector>  retained;
    HashMap<String, HashSet<String>> matched;
    private String[]                 retainFieldVals;
    private String[]                 matchFieldVals;
    private FieldSelector            fieldSelector;
    private HashSet<String>          removableMatches = new HashSet<>();

    public GeoPathDedupFilter(final String retainField, String matchField,
        String containsField, Collector next) {
      super(next);
      this.matchField = matchField;
      this.containsField = containsField;
      this.retainField = retainField;
      this.fieldSelector = new FieldSelector() {

        @Override
        public FieldSelectorResult accept(String fieldName) {
          if (fieldName.equals(retainField)||fieldName.equals("place_type_geo_planet"))
            return FieldSelectorResult.LOAD;
          return FieldSelectorResult.NO_LOAD;
        }
      };
      this.retained = new HashMap<>();
      this.matched = new HashMap<>();
    }

    @Override
    protected void process(int doc) throws IOException {
      
      if (matched.containsKey(matchFieldVals[doc])) {// name
        HashSet<String> olddocs = matched.get(matchFieldVals[doc]);
        TermFreqVector v = reader.getTermFreqVector(doc, containsField);

        HashSet<String> addableMatches = new HashSet<>();
        for (String olddoc : olddocs) {
          
          // if doc is a child of one of olddocs then replace that olddoc with
          // doc
          
          if (v != null && v.indexOf(olddoc) != -1) {
            retained.remove(olddoc);
            removableMatches.add(olddoc);
            addableMatches.add(retainFieldVals[doc]);
            retained.put(retainFieldVals[doc],
                reader.getTermFreqVector(doc, containsField));
            i++;
          } else {
            // if olddoc is a child of doc then ignore doc
            TermFreqVector inv = retained.get(olddoc);// reader.getTermFreqVector(1,
                                     // containsField);
            if (inv != null && inv.indexOf(retainFieldVals[doc]) != -1) {
              removableMatches.add(retainFieldVals[doc]);
              i++;
            } else { // else insert doc along with olddoc (no parent child
                     // relationship)
              retained.put(retainFieldVals[doc],
                  reader.getTermFreqVector(doc, containsField));
              addableMatches.add(retainFieldVals[doc]);
            }
          }
        }

        olddocs.addAll(addableMatches);

      } else {
        retained.put(retainFieldVals[doc],
            reader.getTermFreqVector(doc, containsField));
        HashSet<String> ids = new HashSet<>();
        ids.add(retainFieldVals[doc]);
        matched.put(matchFieldVals[doc], ids);
      }
      matched.get(matchFieldVals[doc]).removeAll(removableMatches);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      super.setNextReader(reader, docBase);
      this.reader = reader;
      this.retainFieldVals = FieldCache.DEFAULT.getStrings(reader, retainField);
      this.matchFieldVals = FieldCache.DEFAULT.getStrings(reader, matchField);
    }

    @Override
    public void setScorer(Scorer s) throws IOException {
      super.setScorer(s);
    }

    @Override
    public int hiddenCount() {
      return i;
    }

    @Override
    public boolean select(ScoreDoc sd, IndexSearcher searcher)
        throws CorruptIndexException, IOException {
      Document doc = searcher.doc(sd.doc, fieldSelector);
      for(Fieldable f: doc.getFieldables("place_type_geo_planet")) {
        if(((Field)f).stringValue().toLowerCase().equals("continent")) {
          return true;
        }
      }
      if (removableMatches.contains(doc.get(retainField)))
        return false;
      return true;
    }

    Term idTerm = new Term("_id", "");

    @Override
    public String selectByValue(String value, IndexSearcher searcher)
        throws IOException {
      if (removableMatches.contains(value)) {
        ScoreDoc[] sds = searcher.search(
            new TermQuery(idTerm.createTerm(value)), 1).scoreDocs;
        if (sds.length > 0) {
          Document doc = searcher.doc(sds[0].doc, new FieldSelector() {

            @Override
            public FieldSelectorResult accept(String fieldName) {

              return fieldName.equals("place_type_geo_planet") ? FieldSelectorResult.LOAD
                  : FieldSelectorResult.NO_LOAD;
            }
          });
          return doc.get("place_type_geo_planet");
        } else {
          return "";
        }
      }
      return "";
    }

  }

  private String matchField;
  private String containsField;
  private String retainField;

  public GeoPathDedupFilterFactory(Map<String, ?> params, Properties env) {
    super(params, env);
    this.matchField = (String) params.get("match");
    this.containsField = (String) params.get("contain");
    this.retainField = (String) params.get("retain");
  }

  @Override
  public SearchFilter createFilter(Collector c) {
    return new GeoPathDedupFilter(retainField, matchField, containsField, c);
  }

}
