package com.cleartrip.sw.search.facets;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.document.FieldSelectorResult;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.FieldCache;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

public class SingleValuedFieldCountSortedFaceterFactory extends
    SearchFaceterFactory {
  private final String field;
  private Term term;
  
  public SingleValuedFieldCountSortedFaceterFactory(Map<String, ?> params,
      Properties env) {
    super(params, env);
    this.field = (String) params.get("field");
    this.term = new Term(field);
    
  }

  private static class SingleValuedFieldCountSortedFaceter extends SearchFaceter {

    //private int                           docBase;
    private final String                  field;
    private String[]                      vals;
    private Object2IntOpenHashMap<String> facets;
    private final FieldSelector fieldSelector;
    

    public SingleValuedFieldCountSortedFaceter(final String field, Collector next) {
      super(next);
      this.field = field;
      facets = new Object2IntOpenHashMap<>();
      fieldSelector = new FieldSelector() {

        @Override
        public FieldSelectorResult accept(String fieldName) {
          if(fieldName.equals(field))
            return FieldSelectorResult.LOAD;
          return FieldSelectorResult.LOAD;
        }
        
      };
    }

    @Override
    public void collect(int doc) throws IOException {
      facets.add(vals[doc], 1);
      if(next!=null)
        next.collect(doc);
    }

    @Override
    public void setNextReader(IndexReader reader, int docBase)
        throws IOException {
      //this.docBase = docBase;
      this.vals = FieldCache.DEFAULT.getStrings(reader, this.field);
      super.setNextReader(reader, docBase);
    }

    @Override
    public String getFacetJson(IndexSearcher searcher, FacetsJsonTopDocs facetsJsonTopDocs)
    		throws CorruptIndexException, IOException {
      
      StringBuilder sb = new StringBuilder();
      sb.append("{ ");
      for (String val : facets.keySet()) {
        sb.append("\"").append(val).append("\": ").append(facets.getInt(val))
            .append(",");
      }
      sb.deleteCharAt(sb.length() - 1).append("}");
      return sb.toString();
    }
  }

  @Override
  public SearchFaceter createFaceter(Collector c) {
    return new SingleValuedFieldCountSortedFaceter(field, c);
  }

  @Override
  public Query createFilter(String[] vals) {
    BooleanQuery q = new BooleanQuery();
    for(String val: vals)
      q.add(new TermQuery(this.term.createTerm(val)), Occur.SHOULD);
    return q;
  }
}
