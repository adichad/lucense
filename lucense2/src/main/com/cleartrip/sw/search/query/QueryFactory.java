package com.cleartrip.sw.search.query;

import java.io.IOException;
import java.text.Collator;
import java.util.List;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.Query;

public abstract class QueryFactory {

  protected Collator rangeCollator = null;

  public abstract Query newBooleanQuery(boolean disableCoord);

  public abstract Query newTermQuery(Term term, int pos);

  public abstract Query newPhraseQuery(String field);

  public abstract void addPhraseTerm(Query q, Term t);

  public abstract Query newMultiPhraseQuery();

  public abstract Query newPrefixQuery(Term prefix, int pos);

  public abstract Query newFuzzyQuery(Term term, float minimumSimilarity,
      int prefixLength, int pos);

  public Query newTermXorFuzzyQuery(IndexReader reader, Term term,
      float minimumSimilarity, int prefixLength, int pos, int fuzzyThreshold)
      throws IOException {

    TermEnum te = null;
    try {
      te = reader.terms(term);
      if (te == null || te.term() == null || !te.term().equals(term)
          || te.docFreq() < fuzzyThreshold) {
        // System.out.println(term + "<=" + te.term() + ": " + te.docFreq() +
        // "<"
        // + fuzzyThreshold);
        return newFuzzyQuery(term, minimumSimilarity, prefixLength, pos);
      } else
        return newTermQuery(term, pos);
    } finally {
      if (te != null)
        te.close();
    }

  }

  public void newTermXorFuzzyQueries(List<Query> queries, IndexReader reader,
      List<Term> terms, float fuzzySimilarity, int prefixLength, int pos,
      int fuzzyThreshold) throws IOException {

    boolean allFuzzy = true;
    for (Term term : terms) {
      try (TermEnum te = reader.terms(term)) {

        if (!(te == null || te.term() == null || !te.term().equals(term) || te
            .docFreq() < fuzzyThreshold)) {
          allFuzzy = false;
        }
      }
    }
    if (allFuzzy) {
      for (Term term : terms)
        queries.add(newFuzzyQuery(term, fuzzySimilarity, prefixLength, pos));
    } else {
      for (Term term : terms)
        queries.add(newTermQuery(term, pos));
    }

  }

  public abstract void addClauses(Query q, Query src);

  public abstract Query newRangeQuery(String field, String part1, String part2,
      boolean inclusive, int pos);

  public abstract Query newIntRangeQuery(String field, int precisionStep,
      int min, int max, boolean minInclusive, boolean maxInclusive);

  public abstract Query newFloatRangeQuery(String field, int precisionStep,
      float min, float max, boolean minInclusive, boolean maxInclusive);

  public abstract Query newDoubleRangeQuery(String field, int precisionStep,
      double min, double max, boolean minInclusive, boolean maxInclusive);

  public abstract Query newMatchAllDocsQuery();

  public abstract Query newWildcardQuery(Term t);

  public abstract BooleanClause[] getClauses(Query bq);

  public abstract void add(Query bq, Query ptq, Occur occur);

  public abstract void add(Query bq, BooleanClause bc);

}