package com.cleartrip.sw.search.query;

import java.io.IOException;
import java.util.Map;

import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.MultiTermQuery;
import org.apache.lucene.search.NumericRangeQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermDocsFactory;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TermRangeQuery;
import org.apache.lucene.search.WildcardQuery;

public class DefaultQueryFactory extends QueryFactory {

  public DefaultQueryFactory(TermDocsFactory tdf,
      Map<String, TermDocsFactory> fieldTermDocsFactories) throws IOException {
  }

  public Query newBooleanQuery(boolean disableCoord) {
    return new BooleanQuery(disableCoord);
  }

  public Query newTermQuery(Term term, int pos) {
    return new TermQuery(term);
  }

  public Query newPhraseQuery(String field) {
    return new PhraseQuery();
  }

  public Query newMultiPhraseQuery() {
    return new MultiPhraseQuery();
  }

  public Query newPrefixQuery(Term prefix, int pos) {
    PrefixQuery query = new PrefixQuery(prefix);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  public Query newFuzzyQuery(Term term, float minimumSimilarity,
      int prefixLength, int pos) {
    // CustomFuzzyQuery doesn't yet allow constant score rewrite
    return new FuzzyQuery(term, minimumSimilarity, prefixLength);
  }

  public Query newRangeQuery(String field, String part1, String part2,
      boolean inclusive, int pos) {
    final TermRangeQuery query = new TermRangeQuery(field, part1, part2,
        inclusive, inclusive, rangeCollator);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  public Query newMatchAllDocsQuery() {
    return new MatchAllDocsQuery();
  }

  public Query newWildcardQuery(Term t) {
    WildcardQuery query = new WildcardQuery(t);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  @Override
  public Query newIntRangeQuery(String field, int precisionStep, int min,
      int max, boolean minInclusive, boolean maxInclusive) {
    NumericRangeQuery<Integer> query = NumericRangeQuery.newIntRange(field,
        precisionStep, min, max, minInclusive, maxInclusive);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  @Override
  public Query newFloatRangeQuery(String field, int precisionStep, float min,
      float max, boolean minInclusive, boolean maxInclusive) {
    NumericRangeQuery<Float> query = NumericRangeQuery.newFloatRange(field,
        precisionStep, min, max, minInclusive, maxInclusive);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  @Override
  public Query newDoubleRangeQuery(String field, int precisionStep, double min,
      double max, boolean minInclusive, boolean maxInclusive) {
    NumericRangeQuery<Double> query = NumericRangeQuery.newDoubleRange(field,
        precisionStep, min, max, minInclusive, maxInclusive);
    query.setRewriteMethod(MultiTermQuery.CONSTANT_SCORE_AUTO_REWRITE_DEFAULT);
    return query;
  }

  @Override
  public BooleanClause[] getClauses(Query bq) {
    return ((BooleanQuery) bq).getClauses();
  }

  @Override
  public void add(Query bq, Query ptq, Occur occur) {
    ((BooleanQuery) bq).add(ptq, occur);
  }

  @Override
  public void add(Query bq, BooleanClause bc) {
    ((BooleanQuery) bq).add(bc);
  }

  @Override
  public void addClauses(Query q, Query src) {
    for (BooleanClause clause : ((BooleanQuery) src).getClauses())
      ((BooleanQuery) q).add(clause);
  }

  @Override
  public void addPhraseTerm(Query q, Term t) {
    ((PhraseQuery) q).add(t);
  }

}
